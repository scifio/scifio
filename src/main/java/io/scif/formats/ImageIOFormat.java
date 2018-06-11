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
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.config.SCIFIOConfig;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * ImageIOReader is the superclass for file format readers that use the
 * javax.imageio package.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public abstract class ImageIOFormat extends AbstractFormat {

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

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
				final int channels = img.getRaster().getNumBands();
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

	public static class Parser<M extends Metadata> extends AbstractParser<M> {

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final M meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			log().info("Populating metadata");
			final DataInputStream dis = new DataInputStream(stream);
			final BufferedImage img = ImageIO.read(dis);
			if (img == null) throw new FormatException("Invalid image stream");
			meta.setImg(img);
			meta.createImageMetadata(1);
		}
	}

	public static class Reader<M extends Metadata> extends
		BufferedImageReader<M>
	{

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
			final Metadata meta = getMetadata();
			plane.setData(AWTImageTools.getSubimage(meta.getImg(), //
				meta.get(imageIndex).isLittleEndian(), bounds));
			return plane;
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			return getMetadata().get(imageIndex).getAxisLength(Axes.Y);
		}
	}

	public static class Writer<M extends Metadata> extends AbstractWriter<M> {

		// -- Fields --

		private final String kind;

		// -- Constructors --

		public Writer(final String kind) {
			this.kind = kind;
		}

		// -- AbstractWriter Methods --

		@Override
		protected String[] makeCompressionTypes() {
			return new String[0];
		}

		@Override
		public void writePlane(final int imageIndex, final long planeIndex,
			final Plane plane, final Interval bounds) throws FormatException,
			IOException
		{
			final Metadata meta = getMetadata();
			if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, bounds)) {
				throw new FormatException(
					"ImageIOWriter does not support writing tiles");
			}

			BufferedImage img = null;

			if (!(plane instanceof BufferedImagePlane)) {
				final int type = meta.get(imageIndex).getPixelType();
				img = AWTImageTools.makeImage(plane.getBytes(), (int) meta.get(
					imageIndex).getAxisLength(Axes.X), (int) meta.get(imageIndex)
						.getAxisLength(Axes.Y), (int) meta.get(imageIndex).getAxisLength(
							Axes.CHANNEL), plane.getImageMetadata()
								.getInterleavedAxisCount() > 0, FormatTools.getBytesPerPixel(
									type), FormatTools.isFloatingPoint(type), meta.get(imageIndex)
										.isLittleEndian(), FormatTools.isSigned(type));
			}
			else {
				img = ((BufferedImagePlane) plane).getData();
			}

			ImageIO.write(img, kind, getStream());
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8, FormatTools.UINT16 };
		}
	}

	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class ImageIOTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public Class<? extends io.scif.Metadata> source() {
			return io.scif.Metadata.class;
		}

		@Override
		public Class<? extends io.scif.Metadata> dest() {
			return Metadata.class;
		}

		@Override
		protected void translateImageMetadata(final List<ImageMetadata> source,
			final Metadata dest)
		{
			dest.createImageMetadata(1);
			final ImageMetadata imgMeta = source.get(0);
			dest.setImg(AWTImageTools.blankImage(imgMeta, imgMeta
				.getAxesLengthsPlanar(), imgMeta.getPixelType()));
		}
	}
}
