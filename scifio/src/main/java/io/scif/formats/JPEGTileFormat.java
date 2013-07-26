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
 * <dl>
 * <dt><b>Source code:</b></dt>
 * <dd><a href=
 * "http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/TileJPEGReader.java"
 * >Trac</a>, <a href=
 * "http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/TileJPEGReader.java;hb=HEAD"
 * >Gitweb</a></dd>
 * </dl>
 * 
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
@Plugin(type = JPEGTileFormat.class, priority = Priority.LOW_PRIORITY)
public class JPEGTileFormat extends AbstractFormat {

	// -- Format API Methods --

	public String getFormatName() {
		return "Tile JPEG";
	}

	public String[] getSuffixes() {
		return new String[] { "jpg", "jpeg" };
	}

	// -- Nested Classes --

	/**
	 * @author Mark Hiner hinerm at gmail.com
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

		/*
		 * @see io.scif.Metadata#populateImageMetadata()
		 */
		public void populateImageMetadata() {
			createImageMetadata(1);
			final ImageMetadata iMeta = get(0);

			iMeta.setInterleaved(true);
			iMeta.setLittleEndian(false);
			iMeta.setAxisLength(Axes.X, decoder.getWidth());
			iMeta.setAxisLength(Axes.Y, decoder.getHeight());
			iMeta.setAxisLength(Axes.CHANNEL, decoder.getScanline(0).length /
				iMeta.getAxisLength(Axes.X));
			iMeta.setAxisLength(Axes.Z, 1);
			iMeta.setAxisLength(Axes.TIME, 1);
			iMeta.setRGB(iMeta.getAxisLength(Axes.CHANNEL) > 1);
			iMeta.setPlaneCount(1);
			iMeta.setPixelType(FormatTools.UINT8);
			iMeta.setBitsPerPixel(8);
			iMeta.setMetadataComplete(true);
			iMeta.setIndexed(false);
		}

		/* @see loci.formats.IFormatReader#close(boolean) */
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
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			final JPEGTileDecoder decoder = new JPEGTileDecoder();
			meta.setDecoder(decoder);
			decoder.initialize(getContext(), in, 0, 1, 0);
		}
	}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		/*
		 * @see io.scif.TypedReader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
		 */
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();
			FormatTools.checkPlaneParameters(this, imageIndex, planeIndex,
				buf.length, x, y, w, h);

			final int c = meta.getRGBChannelCount(imageIndex);

			for (int ty = y; ty < y + h; ty++) {
				byte[] scanline = meta.getDecoder().getScanline(ty);
				if (scanline == null) {
					meta.getDecoder().initialize(getContext(), getStream().getFileName(),
						0);
					scanline = meta.getDecoder().getScanline(ty);
				}
				System.arraycopy(scanline, c * x, buf, (ty - y) * c * w, c * w);
			}

			return plane;
		}

	}
}
