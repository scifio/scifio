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
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.codec.JPEGTileDecoder;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Reader for decoding JPEG images using java.awt.Toolkit. This reader is useful
 * for reading very large JPEG images, as it supports tile-based access.
 * 
 * @author Melissa Linkert
 */
@Plugin(type = Format.class, priority = Priority.LOW_PRIORITY)
public class JPEGTileFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Tile JPEG";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "jpg", "jpeg" };
	}

	// -- Nested Classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		private JPEGTileDecoder decoder;

		// -- JPEGTileMetadata API getters and setters --

		public JPEGTileDecoder getDecoder() {
			return decoder;
		}

		public void setDecoder(final JPEGTileDecoder decoder) {
			this.decoder = decoder;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);
			final ImageMetadata iMeta = get(0);

			iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
			iMeta.setPlanarAxisCount(3);
			iMeta.setLittleEndian(false);
			iMeta.setAxisLength(Axes.X, decoder.getWidth());
			iMeta.setAxisLength(Axes.Y, decoder.getHeight());
			iMeta.setAxisLength(Axes.CHANNEL, decoder.getScanline(0).length /
				iMeta.getAxisLength(Axes.X));
			iMeta.setPixelType(FormatTools.UINT8);
			iMeta.setMetadataComplete(true);
			iMeta.setIndexed(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			if (!fileOnly) {
				if (decoder != null) {
					decoder.close();
				}
				decoder = null;
			}

			super.close(fileOnly);
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			final JPEGTileDecoder decoder = new JPEGTileDecoder(getContext());
			meta.setDecoder(decoder);
			decoder.initialize(in, 0, 1, 0);
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();
			final int xAxis = meta.getAxisIndex(imageIndex, Axes.X);
			final int yAxis = meta.getAxisIndex(imageIndex, Axes.Y);
			final int x = (int) planeMin[xAxis],
								y = (int) planeMin[yAxis],
								w = (int) planeMax[xAxis],
								h = (int) planeMax[yAxis];
			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);

			final int c = (int)meta.getAxisLength(imageIndex, Axes.CHANNEL);

			for (int ty = y; ty < y + h; ty++) {
				byte[] scanline = meta.getDecoder().getScanline(ty);
				if (scanline == null) {
					meta.getDecoder().initialize(getStream().getFileName(), 0);
					scanline = meta.getDecoder().getScanline(ty);
				}
				System.arraycopy(scanline, c * x, buf, (ty - y) * c * w, c * w);
			}

			return plane;
		}

	}
}
