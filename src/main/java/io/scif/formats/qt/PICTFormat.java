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

package io.scif.formats.qt;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.codec.Codec;
import io.scif.codec.CodecOptions;
import io.scif.codec.CodecService;
import io.scif.codec.JPEGCodec;
import io.scif.codec.PackbitsCodec;
import io.scif.config.SCIFIOConfig;
import io.scif.gui.AWTImageTools;
import io.scif.io.ByteArrayHandle;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Vector;

import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * PictReader is the file format reader for Apple PICT files. Most of this code
 * was adapted from the PICT readers in
 * <a href="http://java.sun.com/products/jimi/index.html">JIMI</a>,
 * <a href="http://www.imagemagick.org">ImageMagick</a>, and Java QuickDraw.
 *
 * @author Melissa Linkert
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "PICT")
public class PICTFormat extends AbstractFormat {

	// -- Constants --

	// opcodes that we need
	private static final int PICT_CLIP_RGN = 1;

	private static final int PICT_BITSRECT = 0x90;

	private static final int PICT_BITSRGN = 0x91;

	private static final int PICT_PACKBITSRECT = 0x98;

	private static final int PICT_PACKBITSRGN = 0x99;

	private static final int PICT_9A = 0x9a;

	private static final int PICT_END = 0xff;

	private static final int PICT_LONGCOMMENT = 0xa1;

	private static final int PICT_JPEG = 0x18;

	private static final int PICT_TYPE_1 = 0xa9f;

	private static final int PICT_TYPE_2 = 0x9190;

	/** Table used in expanding pixels that use less than 8 bits. */
	private static final byte[] EXPANSION_TABLE = new byte[256 * 8];

	static {
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 8; j++) {
				EXPANSION_TABLE[i * 8 + j] = (byte) ((i & (int) Math.pow(2, 7 -
					j)) >> 7 - j);
			}
		}
	}

	// -- Fields --

	private boolean legacy;

	// -- PICTFormat API --

	public void setLegacy(final boolean legacy) {
		this.legacy = legacy;
	}

	public boolean isLegacy() {
		return legacy;
	}

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "pict", "pct" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		/** Number of bytes in a row of pixel data (variable). */
		private int rowBytes;

		/**
		 * Vector of {@code byte[]} and/or {@code int[]} representing individual
		 * rows.
		 */
		private Vector<Object> strips;

		/** Whether or not the file is PICT v1. */
		private boolean versionOne;

		/** Color lookup table for palette color images. */
		private byte[][] lookup;

		private Vector<Long> jpegOffsets = new Vector<>();

		// -- PICTFormat Metadata getters and setters --

		public int getRowBytes() {
			return rowBytes;
		}

		public void setRowBytes(final int rowBytes) {
			this.rowBytes = rowBytes;
		}

		public Vector<Object> getStrips() {
			return strips;
		}

		public void setStrips(final Vector<Object> strips) {
			this.strips = strips;
		}

		public boolean isVersionOne() {
			return versionOne;
		}

		public void setVersionOne(final boolean versionOne) {
			this.versionOne = versionOne;
		}

		public byte[][] getLookup() {
			return lookup;
		}

		public void setLookup(final byte[][] lookup) {
			this.lookup = lookup;
		}

		public Vector<Long> getJpegOffsets() {
			return jpegOffsets;
		}

		public void setJpegOffsets(final Vector<Long> jpegOffsets) {
			this.jpegOffsets = jpegOffsets;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			int planarAxes = 2;
			if (iMeta.getAxisLength(Axes.CHANNEL) > 1) planarAxes = 3;
			iMeta.setPlanarAxisCount(planarAxes);
			iMeta.setLittleEndian(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
			iMeta.setPixelType(FormatTools.UINT8);
			iMeta.setBitsPerPixel(8);

			iMeta.setIndexed(!(iMeta.isMultichannel()) && lookup != null);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				rowBytes = 0;
				strips = null;
				versionOne = false;
				lookup = null;
				if (jpegOffsets != null) jpegOffsets.clear();
				else jpegOffsets = new Vector<>();
			}
		}

		// -- HasColorTable API Methods --

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			return lookup == null ? null : new ColorTable8(lookup);
		}

	}

	public static class Parser extends AbstractParser<Metadata> {

		@Parameter
		private CodecService codecService;

		// -- Parser API methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			stream.seek(518);
			short sizeY = stream.readShort();
			short sizeX = stream.readShort();

			final Vector<Object> strips = new Vector<>();
			final byte[][] lookup = null;
			boolean versionOne = false;
			meta.setStrips(strips);
			meta.setLookup(lookup);
			meta.setRowBytes(0);

			int opcode;

			final int verOpcode = stream.read();
			final int verNumber = stream.read();

			if (verOpcode == 0x11 && verNumber == 0x01) versionOne = true;
			else if (verOpcode == 0x00 && verNumber == 0x11) {
				versionOne = false;
				final int verNumber2 = stream.readShort();

				if (verNumber2 != 0x02ff) {
					throw new FormatException("Invalid PICT file : " + verNumber2);
				}

				// skip over v2 header -- don't need it here
				// stream.skipBytes(26);
				stream.skipBytes(6);
				final int pixelsPerInchX = stream.readInt();
				final int pixelsPerInchY = stream.readInt();
				iMeta.addAxis(new DefaultLinearAxis(Axes.X, "in", pixelsPerInchX));
				iMeta.addAxis(new DefaultLinearAxis(Axes.Y, "in", pixelsPerInchY));
				stream.skipBytes(4);
				final int y = stream.readShort();
				final int x = stream.readShort();
				if (x > 0) sizeX = (short) x;
				if (y > 0) sizeY = (short) y;
				stream.skipBytes(4);
			}
			else throw new FormatException("Invalid PICT file");

			iMeta.setAxisLength(Axes.X, sizeX);
			iMeta.setAxisLength(Axes.Y, sizeY);

			meta.getTable().put("Version", versionOne ? 1 : 2);
			meta.setVersionOne(versionOne);

			do {
				if (versionOne) opcode = stream.read();
				else {
					// if at odd boundary skip a byte for opcode in PICT v2

					if ((stream.getFilePointer() & 0x1L) != 0) {
						stream.skipBytes(1);
					}
					if (stream.getFilePointer() + 2 >= stream.length()) {
						break;
					}
					opcode = stream.readShort() & 0xffff;
				}
			}
			while (drivePictDecoder(meta, opcode));
		}

		// -- Helper methods --

		/** Handles the opcodes in the PICT file. */
		private boolean drivePictDecoder(final Metadata meta, final int opcode)
			throws FormatException, IOException
		{
			log().debug("drivePictDecoder(" + opcode + ") @ " + getSource()
				.getFilePointer());

			switch (opcode) {
				case PICT_BITSRGN: // rowBytes must be < 8
				case PICT_PACKBITSRGN: // rowBytes must be < 8
				case PICT_BITSRECT: // rowBytes must be < 8
				case PICT_PACKBITSRECT:
					meta.setRowBytes(getSource().readShort());
					if (meta.isVersionOne() || (meta.getRowBytes() & 0x8000) == 0)
						handleBitmap(meta, opcode);
					else handlePixmap(meta, opcode);
					break;
				case PICT_9A:
					handlePixmap(meta, opcode);
					break;
				case PICT_CLIP_RGN:
					int x = getSource().readShort();
					getSource().skipBytes(x - 2);
					break;
				case PICT_LONGCOMMENT:
					getSource().skipBytes(2);
					x = getSource().readShort();
					getSource().skipBytes(x);
					break;
				case PICT_END: // end of PICT
					return false;
				case PICT_TYPE_1:
				case PICT_TYPE_2:
					x = getSource().read();
					getSource().skipBytes(x);
					break;
				case PICT_JPEG:
					meta.getJpegOffsets().add(getSource().getFilePointer() + 2);
					meta.get(0).setAxisLength(Axes.CHANNEL, 3);
					while ((getSource().readShort() & 0xffff) != 0xffd9 && getSource()
						.getFilePointer() < getSource().length())
					{ /* Read to end of 0xffd9 */}
					while (getSource().getFilePointer() < getSource().length()) {
						while ((getSource().readShort() & 0xffff) != 0xffd8 && getSource()
							.getFilePointer() < getSource().length())
						{ /* Read to jpeg offsets */}
						if (getSource().getFilePointer() < getSource().length()) {
							meta.getJpegOffsets().add(getSource().getFilePointer() - 2);
						}
					}
					meta.get(0).setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
					break;
				default:
					if (opcode < 0) {
						// throw new FormatException("Invalid opcode: " + opcode);
						log().warn("Invalid opcode: " + opcode);
					}
			}

			return getSource().getFilePointer() < getSource().length();
		}

		/** Extract the image data in a PICT bitmap structure. */
		private void handleBitmap(final Metadata meta, final int opcode)
			throws FormatException, IOException
		{
			readImageHeader(meta, opcode);
			handlePixmap(meta, 1, 1);
		}

		/** Extracts the image data in a PICT pixmap structure. */
		private void handlePixmap(final Metadata meta, final int opcode)
			throws FormatException, IOException
		{
			readImageHeader(meta, opcode);
			log().debug("handlePixmap(" + opcode + ")");

			final int pixelSize = getSource().readShort();
			final int compCount = getSource().readShort();
			getSource().skipBytes(14);

			if (opcode == PICT_9A) {
				// rowBytes doesn't exist, so set it to its logical value
				switch (pixelSize) {
					case 32:
						meta.setRowBytes((int) meta.get(0).getAxisLength(Axes.X) *
							compCount);
						break;
					case 16:
						meta.setRowBytes((int) meta.get(0).getAxisLength(Axes.X) * 2);
						break;
					default:
						throw new FormatException("Sorry, vector data not supported.");
				}
			}
			else {
				// read the lookup table

				getSource().skipBytes(4);
				getSource().readShort(); // flags
				int count = getSource().readShort();

				count++;
				final byte[][] lookup = new byte[3][count];

				for (int i = 0; i < count; i++) {
					getSource().skipBytes(2);
					lookup[0][i] = getSource().readByte();
					getSource().skipBytes(1);
					lookup[1][i] = getSource().readByte();
					getSource().skipBytes(1);
					lookup[2][i] = getSource().readByte();
					getSource().skipBytes(1);
				}
				meta.setLookup(lookup);
			}

			// skip over two rectangles
			getSource().skipBytes(18);

			if (opcode == PICT_BITSRGN || opcode == PICT_PACKBITSRGN) getSource()
				.skipBytes(2);

			handlePixmap(meta, pixelSize, compCount);
		}

		/** Handles the unpacking of the image data. */
		private void handlePixmap(final Metadata meta, final int pixelSize,
			final int compCount) throws FormatException, IOException
		{
			log().debug("handlePixmap(" + meta.getRowBytes() + ", " + pixelSize +
				", " + compCount + ")");
			int rawLen;
			byte[] buf; // row raw bytes
			byte[] uBuf = null; // row uncompressed data
			int[] uBufI = null; // row uncompressed data - 16+ bit pixels
			final int bufSize = meta.getRowBytes();
			final int outBufSize = (int) meta.get(0).getAxisLength(Axes.X);
			byte[] outBuf = null; // used to expand pixel data

			final boolean compressed = (meta.getRowBytes() >= 8) || (pixelSize == 32);

			// allocate buffers

			switch (pixelSize) {
				case 32:
					if (!compressed) uBufI = new int[(int) meta.get(0).getAxisLength(
						Axes.X)];
					else uBuf = new byte[bufSize];
					break;
				case 16:
					uBufI = new int[(int) meta.get(0).getAxisLength(Axes.X)];
					break;
				case 8:
					uBuf = new byte[bufSize];
					break;
				default:
					outBuf = new byte[outBufSize];
					uBuf = new byte[bufSize];
					break;
			}

			if (!compressed) {
				log().debug("Pixel data is uncompressed (pixelSize=" + pixelSize +
					").");
				buf = new byte[bufSize];
				for (int row = 0; row < meta.get(0).getAxisLength(Axes.X); row++) {
					getSource().read(buf, 0, meta.getRowBytes());

					switch (pixelSize) {
						case 16:
							for (int i = 0; i < meta.get(0).getAxisLength(Axes.X); i++) {
								uBufI[i] = Bytes.toShort(buf, i * 2, 2, false);
							}
							meta.getStrips().add(uBufI);
							buf = null;
							meta.get(0).setAxisLength(Axes.CHANNEL, 3);
							break;
						case 8:
							meta.getStrips().add(buf);
							break;
						default: // pixel size < 8
							expandPixels(pixelSize, buf, outBuf, outBuf.length);
							meta.getStrips().add(outBuf);
							buf = null;
					}
				}
			}
			else {
				log().debug("Pixel data is compressed (pixelSize=" + pixelSize +
					"; compCount=" + compCount + ").");
				buf = new byte[bufSize + 1 + bufSize / 128];
				for (int row = 0; row < meta.get(0).getAxisLength(Axes.Y); row++) {
					if (meta.getRowBytes() > 250) rawLen = getSource().readShort();
					else rawLen = getSource().read();

					if (rawLen > buf.length) rawLen = buf.length;

					if ((getSource().length() - getSource().getFilePointer()) <= rawLen) {
						rawLen = (int) (getSource().length() - getSource()
							.getFilePointer() - 1);
					}

					if (rawLen < 0) {
						rawLen = 0;
						getSource().seek(getSource().length() - 1);
					}

					getSource().read(buf, 0, rawLen);

					if (pixelSize == 16) {
						uBufI = new int[(int) meta.get(0).getAxisLength(Axes.X)];
						unpackBits(buf, uBufI);
						meta.getStrips().add(uBufI);
						meta.get(0).setAxisLength(Axes.CHANNEL, 3);
					}
					else {
						final PackbitsCodec c = codecService.getCodec(PackbitsCodec.class);
						final CodecOptions options = new CodecOptions();
						options.maxBytes = (int) meta.get(0).getAxisLength(Axes.X) * 4;
						uBuf = c.decompress(buf, options);
					}

					if (pixelSize < 8) {
						expandPixels(pixelSize, uBuf, outBuf, outBuf.length);
						meta.getStrips().add(outBuf);
					}
					else if (pixelSize == 8) {
						meta.getStrips().add(uBuf);
					}
					else if (pixelSize == 24 || pixelSize == 32) {
						byte[] newBuf = null;

						for (int q = 0; q < compCount; q++) {
							final int offset = q * (int) meta.get(0).getAxisLength(Axes.X);
							final int len = Math.min((int) meta.get(0).getAxisLength(Axes.X),
								uBuf.length - offset);
							newBuf = new byte[(int) meta.get(0).getAxisLength(Axes.X)];
							if (offset < uBuf.length) {
								System.arraycopy(uBuf, offset, newBuf, 0, len);
							}
							meta.getStrips().add(newBuf);
						}
						meta.get(0).setAxisLength(Axes.CHANNEL, 3);
					}
				}
			}
		}

		private void readImageHeader(final Metadata meta, final int opcode)
			throws IOException
		{
			int rowBytes = meta.getRowBytes();
			if (opcode == PICT_9A) getSource().skipBytes(6);
			else rowBytes &= 0x3fff;
			meta.setRowBytes(rowBytes);

			final int tlY = getSource().readShort();
			final int tlX = getSource().readShort();
			final int brY = getSource().readShort();
			final int brX = getSource().readShort();

			if (brX - tlX > 0) meta.get(0).setAxisLength(Axes.X, brX - tlX);
			if (brY - tlY > 0) meta.get(0).setAxisLength(Axes.Y, brY - tlY);

			getSource().skipBytes(18);
		}

		/** Expand an array of bytes. */
		private void expandPixels(final int bitSize, final byte[] ib,
			final byte[] ob, final int outLen) throws FormatException
		{
			log().debug("expandPixels(" + bitSize + ", " + ib.length + ", " +
				ob.length + ", " + outLen + ")");
			if (bitSize == 1) {
				final int remainder = outLen % 8;
				final int max = outLen / 8;
				for (int i = 0; i < max; i++) {
					if (i < ib.length) {
						final int look = (ib[i] & 0xff) * 8;
						System.arraycopy(EXPANSION_TABLE, look, ob, i * 8, 8);
					}
					else i = max;
				}

				if (remainder != 0) {
					if (max < ib.length) {
						System.arraycopy(EXPANSION_TABLE, (ib[max] & 0xff) * 8, ob, max * 8,
							remainder);
					}
				}

				return;
			}

			byte v;
			final int count = 8 / bitSize; // number of pixels in a byte
			final int maskshift = bitSize; // num bits to shift mask
			final int pixelshift = 8 - bitSize; // num bits to shift pixel
			int tpixelshift = 0;
			final int pixelshiftdelta = bitSize;
			int tmask; // temp mask

			if (bitSize != 1 && bitSize != 2 && bitSize != 4) {
				throw new FormatException("Can only expand 1, 2, and 4 bit values");
			}

			final int mask = ((int) Math.pow(2, bitSize) - 1) << (8 - bitSize);

			int i = 0;
			for (int o = 0; o < ob.length; i++) {
				tmask = mask;
				tpixelshift = pixelshift;
				v = ib[i];
				for (int t = 0; t < count && o < ob.length; t++, o++) {
					ob[o] = (byte) (((v & tmask) >>> tpixelshift) & 0xff);
					tmask = (byte) ((tmask & 0xff) >>> maskshift);
					tpixelshift -= pixelshiftdelta;
				}
			}
		}

		/** PackBits variant that outputs an int array. */
		private void unpackBits(final byte[] ib, final int[] ob) {
			log().debug("unpackBits(...)");
			int i = 0;
			int b;
			int rep;
			int end;

			for (int o = 0; o < ob.length;) {
				if (i + 1 < ib.length) {
					b = ib[i++];
					if (b >= 0) {
						end = o + b + 1;
						while (o < end && o < ob.length && (i + 1) < ib.length) {
							ob[o++] = Bytes.toShort(ib, i, 2, false);
							i += 2;
						}
					}
					else if (b != -128) {
						rep = Bytes.toShort(ib, i, 2, false);
						i += 2;
						end = o - b + 1;
						while (o < end && o < ob.length) {
							ob[o++] = rep;
						}
					}
				}
				else o = ob.length;
			}
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private QTJavaService qtJavaService;

		@Parameter
		private CodecService codecService;

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
			final byte[] buf = plane.getBytes();

			if (meta.getJpegOffsets().size() > 0) {
				final ByteArrayHandle v = new ByteArrayHandle();
				getStream().seek(meta.getJpegOffsets().get(0));
				final byte[] b = new byte[(int) (getStream().length() - getStream()
					.getFilePointer())];
				getStream().read(b);
				RandomAccessInputStream s = new RandomAccessInputStream(getContext(),
					b);
				for (final long jpegOffset : meta.getJpegOffsets()) {
					s.seek(jpegOffset - meta.getJpegOffsets().get(0));

					final CodecOptions options = new CodecOptions();
					options.interleaved = meta.get(0).getInterleavedAxisCount() > 0;
					options.littleEndian = meta.get(0).isLittleEndian();

					final Codec codec = codecService.getCodec(JPEGCodec.class);
					v.write(codec.decompress(s, options));
				}

				s.close();
				s = new RandomAccessInputStream(getContext(), v);
				s.seek(0);
				try {
					readPlane(s, imageIndex, bounds, plane);
				}
				finally {
					s.close();
				}

				return plane;
			}

			if (((PICTFormat) getFormat()).isLegacy() || meta.getStrips()
				.size() == 0)
			{
				getStream().seek(512);
				byte[] pix = new byte[(int) (getStream().length() - getStream()
					.getFilePointer())];
				getStream().read(pix);
				byte[][] b = AWTImageTools.getBytes(AWTImageTools.makeBuffered(
					qtJavaService.pictToImage(pix)));
				pix = null;
				for (int i = 0; i < b.length; i++) {
					System.arraycopy(b[i], 0, buf, i * b[i].length, b[i].length);
				}
				b = null;
				return plane;
			}

			// combine everything in the strips Vector

			if ((meta.get(0).getAxisLength(Axes.Y) * 4 < meta.getStrips().size()) &&
				(((meta.getStrips().size() / 3) % meta.get(0).getAxisLength(
					Axes.Y)) != 0))
			{
				meta.get(0).setAxisLength(Axes.Y, meta.getStrips().size());
			}
			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);
			final int planeSize = w * h;

			if (meta.getLookup() != null) {
				// 8 bit data

				byte[] row;

				for (int i = y; i < y + h; i++) {
					row = (byte[]) meta.getStrips().get(i);
					final int len = Math.min(row.length, w);
					System.arraycopy(row, x, buf, (i - y) * w, len);
				}
			}
			else if (meta.get(0).getAxisLength(Axes.Y) * 3 == meta.getStrips()
				.size() || meta.get(0).getAxisLength(Axes.Y) * 4 == meta.getStrips()
					.size())
			{
				// 24 or 32 bit data

				final int nc = meta.getStrips().size() / (int) meta.get(0)
					.getAxisLength(Axes.Y);

				byte[] c0 = null;
				byte[] c1 = null;
				byte[] c2 = null;

				for (int i = y; i < h + y; i++) {
					c0 = (byte[]) meta.getStrips().get(i * nc + nc - 3);
					c1 = (byte[]) meta.getStrips().get(i * nc + nc - 2);
					c2 = (byte[]) meta.getStrips().get(i * nc + nc - 1);
					final int baseOffset = (i - y) * w;
					System.arraycopy(c0, x, buf, baseOffset, w);
					System.arraycopy(c1, x, buf, planeSize + baseOffset, w);
					System.arraycopy(c2, x, buf, 2 * planeSize + baseOffset, w);
				}
			}
			else {
				// RGB value is packed into a single short: xRRR RRGG GGGB BBBB
				int[] row = null;
				for (int i = y; i < h + y; i++) {
					row = (int[]) meta.getStrips().get(i);

					for (int j = x; j < w + x; j++) {
						final int base = (i - y) * w + (j - x);
						buf[base] = (byte) ((row[j] & 0x7c00) >> 10);
						buf[planeSize + base] = (byte) ((row[j] & 0x3e0) >> 5);
						buf[2 * planeSize + base] = (byte) (row[j] & 0x1f);
					}
				}
			}
			return plane;
		}

	}
}
