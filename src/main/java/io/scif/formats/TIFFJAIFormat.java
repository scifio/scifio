/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.BufferedImagePlane;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.MissingLibraryException;
import io.scif.config.SCIFIOConfig;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.FileHandle;
import io.scif.io.IRandomAccess;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.FormatService;
import io.scif.services.LocationService;
import io.scif.util.FormatTools;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ReflectException;
import org.scijava.util.ReflectedUniverse;

/**
 * TiffJAIReader is a file format reader for TIFF images. It uses the Java
 * Advanced Imaging library (javax.media.jai) to read the data. Much of this
 * code was adapted from <a href=
 * "http://java.sun.com/products/java-media/jai/forDevelopers/samples/MultiPageRead.java"
 * >this example</a>.
 */
@Plugin(type = Format.class, name = "Tagged Image File Format",
	priority = MinimalTIFFFormat.PRIORITY - 1)
public class TIFFJAIFormat extends AbstractFormat {

	@Parameter
	private FormatService formatService;

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return formatService.getFormatFromClass(TIFFFormat.class).getSuffixes();
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		/** Reflection tool for JAI calls. */
		private ReflectedUniverse r;

		private int numPages;

		// -- TIFFJAIMetadata getters and setters --

		public ReflectedUniverse universe() {
			return r;
		}

		public void setUniverse(final ReflectedUniverse r) {
			this.r = r;
		}

		public int getNumPages() {
			return numPages;
		}

		public void setNumPages(final int numPages) {
			this.numPages = numPages;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);
			final ImageMetadata m = get(0);

			// decode first image plane
			BufferedImage img = null;
			try {
				img = openBufferedImage(this, 0);
			}
			catch (final FormatException e) {
				log().error("Invalid image stream", e);
				return;
			}

			m.setAxisLength(Axes.CHANNEL, img.getSampleModel().getNumBands());
			m.setAxisLength(Axes.X, img.getWidth());
			m.setAxisLength(Axes.Y, img.getHeight());
			m.setAxisLength(Axes.TIME, numPages);
			m.setPlanarAxisCount(3);

			m.setPixelType(AWTImageTools.getPixelType(img));
			m.setLittleEndian(false);
			m.setMetadataComplete(true);
			m.setIndexed(false);
			m.setFalseColor(false);
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		private static final String NO_JAI_MSG =
			"Java Advanced Imaging (JAI) is required to read some TIFF files. " +
				"Please install JAI from https://jai.dev.java.net/";

		// -- Fields --

		@Parameter
		private LocationService locationService;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			log().info("Checking for JAI");
			ReflectedUniverse r = null;

			try {
				r = new ReflectedUniverse();
				r.exec("import javax.media.jai.NullOpImage");
				r.exec("import javax.media.jai.OpImage");
				r.exec("import com.sun.media.jai.codec.FileSeekableStream");
				r.exec("import com.sun.media.jai.codec.ImageDecoder");
				r.exec("import com.sun.media.jai.codec.ImageCodec");
			}
			catch (final ReflectException exc) {
				throw new MissingLibraryException(NO_JAI_MSG, exc);
			}

			meta.setUniverse(r);

			final String id = stream.getFileName();

			log().info("Reading movie dimensions");

			// map Location to File or RandomAccessFile, if possible
			final IRandomAccess ira = locationService.getMappedFile(id);
			if (ira != null) {
				if (ira instanceof FileHandle) {
					final FileHandle fh = (FileHandle) ira;
					r.setVar("file", fh.getRandomAccessFile());
				}
				else {
					throw new FormatException("Unsupported handle type" + ira.getClass()
						.getName());
				}
			}
			else {
				final String mapId = locationService.getMappedId(id);
				final File file = new File(mapId);
				if (file.exists()) {
					r.setVar("file", file);
				}
				else throw new FileNotFoundException(id);
			}
			r.setVar("tiff", "tiff");
			r.setVar("param", null);

			// create TIFF decoder
			int numPages;
			try {
				r.exec("s = new FileSeekableStream(file)");
				r.exec("dec = ImageCodec.createImageDecoder(tiff, s, param)");
				numPages = ((Integer) r.exec("dec.getNumPages()")).intValue();
			}
			catch (final ReflectException exc) {
				throw new FormatException(exc);
			}
			if (numPages < 0) {
				throw new FormatException("Invalid page count: " + numPages);
			}
		}

	}

	public static class Reader extends BufferedImageReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		@Override
		public BufferedImagePlane openPlane(final int imageIndex,
			final long planeIndex, final BufferedImagePlane plane,
			final Interval bounds, final SCIFIOConfig config) throws FormatException,
			IOException
		{
			FormatTools.checkPlaneForReading(getMetadata(), imageIndex, planeIndex,
				-1, bounds);
			final BufferedImage img = openBufferedImage(getMetadata(), planeIndex);
			plane.setData(AWTImageTools.getSubimage(img, getMetadata().get(imageIndex)
				.isLittleEndian(), bounds));
			return plane;
		}
	}

	// -- Helper methods --

	/** Obtains a BufferedImage from the given data source using JAI. */
	protected static BufferedImage openBufferedImage(final Metadata meta,
		final long planeIndex) throws FormatException
	{
		meta.universe().setVar("planeIndex", planeIndex);
		RenderedImage img;
		try {
			meta.universe().exec("img = dec.decodeAsRenderedImage(planeIndex)");
			img = (RenderedImage) meta.universe().exec(
				"new NullOpImage(img, null, OpImage.OP_IO_BOUND, null)");
		}
		catch (final ReflectException exc) {
			throw new FormatException(exc);
		}
		return AWTImageTools.convertRenderedImage(img);
	}
}
