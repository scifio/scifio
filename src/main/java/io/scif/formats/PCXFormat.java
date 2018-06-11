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

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

import org.scijava.plugin.Plugin;

/**
 * PCXReader is the file format reader for PCX files (originally used by PC
 * Paintbrush; now used in Zeiss' LSM Image Browser). See
 * http://www.qzx.com/pc-gpe/pcx.txt
 *
 * @author Mark Hiner
 * @author Melissa Linkert
 */
@Plugin(type = Format.class, name = "PCX")
public class PCXFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "pcx" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		/** Offset to pixel data. */
		private long offset;

		/** Number of bytes per scan line - may be different than image width. */
		private int bytesPerLine;

		private int nColorPlanes;

		private ColorTable8 lut;

		// -- PCXMetadata getters and setters --

		public long getOffset() {
			return offset;
		}

		public void setOffset(final long offset) {
			this.offset = offset;
		}

		public int getBytesPerLine() {
			return bytesPerLine;
		}

		public void setBytesPerLine(final int bytesPerLine) {
			this.bytesPerLine = bytesPerLine;
		}

		public int getnColorPlanes() {
			return nColorPlanes;
		}

		public void setnColorPlanes(final int nColorPlanes) {
			this.nColorPlanes = nColorPlanes;
		}

		// -- Metadata API methods --

		@Override
		public void populateImageMetadata() {

			final ImageMetadata iMeta = get(0);
			iMeta.setAxisLength(Axes.CHANNEL, nColorPlanes);
			iMeta.setPlanarAxisCount(iMeta.getAxisLength(Axes.CHANNEL) > 1 ? 3 : 2);
			iMeta.setPixelType(FormatTools.UINT8);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				offset = 0;
				bytesPerLine = 0;
				nColorPlanes = 0;
				lut = null;
			}
		}

		// -- HasColorTable API Methods -

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			return lut;
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final byte PCX_MAGIC_BYTE = 10;

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 1;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			return stream.read() == PCX_MAGIC_BYTE;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			log().info("Reading file header");

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			iMeta.setLittleEndian(true);
			stream.order(true);
			stream.seek(1);
			final int version = stream.read();
			stream.skipBytes(1);
			iMeta.setBitsPerPixel(stream.read());
			final int xMin = stream.readShort();
			final int yMin = stream.readShort();
			final int xMax = stream.readShort();
			final int yMax = stream.readShort();

			iMeta.setAxisLength(Axes.X, xMax - xMin);
			iMeta.setAxisLength(Axes.Y, yMax - yMin);

			stream.skipBytes(version == 5 ? 53 : 51);

			meta.setnColorPlanes(stream.read());
			meta.setBytesPerLine(stream.readShort());
			final int paletteType = stream.readShort();

			meta.setOffset(stream.getFilePointer() + 58);

			if (version == 5 && meta.getnColorPlanes() == 1) {
				stream.seek(stream.length() - 768);
				final byte[][] lut = new byte[3][256];
				for (int i = 0; i < lut[0].length; i++) {
					for (int j = 0; j < lut.length; j++) {
						lut[j][i] = stream.readByte();
					}
				}

				meta.lut = new ColorTable8(lut);
				iMeta.setIndexed(true);
			}

			meta.getTable().put("Palette type", paletteType);
		}

	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			plane.setColorTable(meta.getColorTable(imageIndex, planeIndex));
			final byte[] buf = plane.getData();

			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);

			getStream().seek(meta.getOffset());

			// PCX uses a simple RLE compression algorithm

			final byte[] b = new byte[meta.getBytesPerLine() * (int) meta.get(
				imageIndex).getAxisLength(Axes.Y) * meta.getnColorPlanes()];
			int pt = 0;
			while (pt < b.length) {
				int val = getStream().read() & 0xff;
				if (((val & 0xc0) >> 6) == 3) {
					final int len = val & 0x3f;
					val = getStream().read() & 0xff;
					for (int q = 0; q < len; q++) {
						b[pt++] = (byte) val;
						if ((pt % meta.getBytesPerLine()) == 0) {
							break;
						}
					}
				}
				else b[pt++] = (byte) (val & 0xff);
			}

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);
			final int src = y * meta.getnColorPlanes() * meta.getBytesPerLine();
			for (int row = 0; row < h; row++) {
				int rowOffset = row * meta.getnColorPlanes() * meta.getBytesPerLine();
				for (int c = 0; c < meta.getnColorPlanes(); c++) {
					System.arraycopy(b, src + rowOffset + x, buf, c * w * h + row * w, w);
					rowOffset += meta.getBytesPerLine();
				}
			}

			return plane;
		}
	}
}
