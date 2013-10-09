/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * ImageIOReader is the superclass for file format readers that use the
 * javax.imageio package.
 * 
 * @author Curtis Rueden
 */
public abstract class ImageIOFormat extends AbstractFormat {

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		public static final String CNAME = "io.scif.formats.ImageIOFormat$Metadata";

		// -- Fields --

		private BufferedImage img;

		// -- ImageIOMetadata API methods --

		public BufferedImage getImg() {
			return img;
		}

		public void setImg(final BufferedImage img) {
			this.img = img;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			if (img != null) {
				iMeta.setAxisLength(Axes.X, img.getWidth());
				iMeta.setAxisLength(Axes.Y, img.getHeight());
				iMeta.setPlanarAxisCount(2);
				int channels = img.getRaster().getNumBands();
				if (channels > 1) {
					iMeta.setPlanarAxisCount(3);
					iMeta.setAxisLength(Axes.CHANNEL, img.getRaster().getNumBands());
				}
				iMeta.setPixelType(AWTImageTools.getPixelType(img));
			}

			iMeta.setLittleEndian(false);
			iMeta.setMetadataComplete(true);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				img = null;
			}
		}
	}

	/**
	 * @author Mark Hiner
	 * @param <M>
	 */
	public static class Parser<M extends Metadata> extends AbstractParser<M> {

		@Override
		protected void
			typedParse(final RandomAccessInputStream stream, final M meta)
				throws IOException, FormatException
		{
			log().info("Populating metadata");
			final DataInputStream dis = new DataInputStream(stream);
			final BufferedImage img = ImageIO.read(dis);
			if (img == null) throw new FormatException("Invalid image stream");
			meta.setImg(img);
			meta.createImageMetadata(1);
		}
	}

	/**
	 * @author Mark Hiner
	 * @param <M>
	 */
	public static class Reader<M extends Metadata> extends BufferedImageReader<M>
	{

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		@Override
		public BufferedImagePlane openPlane(final int imageIndex,
			final int planeIndex, final BufferedImagePlane plane,
			final long[] planeMin, final long[] planeMax) throws FormatException,
			IOException
		{
			final Metadata meta = getMetadata();
			plane.setData(AWTImageTools.getSubimage(meta.getImg(), meta
				.isLittleEndian(imageIndex), planeMin, planeMax));
			return plane;
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			return getMetadata().getAxisLength(imageIndex, Axes.Y);
		}
	}

	/**
	 * @author Mark Hiner
	 * @param <M>
	 */
	public static class Writer<M extends Metadata> extends AbstractWriter<M> {

		// -- Fields --

		protected String kind;

		// -- Constructors --

		public Writer(final String kind) {
			this.kind = kind;
		}

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, planeMin, planeMax)) {
				throw new FormatException(
					"ImageIOWriter does not support writing tiles");
			}

			BufferedImage img = null;

			if (!(plane instanceof BufferedImagePlane)) {
				final int type = meta.getPixelType(imageIndex);
				img =
					AWTImageTools.makeImage(plane.getBytes(), (int) meta.getAxisLength(
						imageIndex, Axes.X), (int) meta.getAxisLength(0, Axes.Y),
						(int) meta.getAxisLength(imageIndex, Axes.CHANNEL), meta
							.isInterleaved(imageIndex), FormatTools.getBytesPerPixel(type),
						FormatTools.isFloatingPoint(type), meta.isLittleEndian(imageIndex),
						FormatTools.isSigned(type));
			}
			else {
				img = ((BufferedImagePlane) plane).getData();
			}

			ImageIO.write(img, kind, out);
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8, FormatTools.UINT16 };
		}
	}

	@Plugin(type = Translator.class, attrs = {
		@Attr(name = ImageIOTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = ImageIOTranslator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class ImageIOTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		@Override
		protected void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			dest.createImageMetadata(1);
			dest.setAxisLength(0, Axes.X, source.getAxisLength(0, Axes.X));
			dest.setAxisLength(0, Axes.Y, source.getAxisLength(0, Axes.Y));
			dest
				.setAxisLength(0, Axes.CHANNEL, source.getAxisLength(0, Axes.CHANNEL));
			dest.setPixelType(0, source.getPixelType(0));
		}
	}
}
