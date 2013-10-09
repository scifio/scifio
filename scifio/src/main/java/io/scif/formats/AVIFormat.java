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

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.UnsupportedCompressionException;
import io.scif.codec.BitBuffer;
import io.scif.codec.CodecOptions;
import io.scif.codec.JPEGCodec;
import io.scif.codec.MSRLECodec;
import io.scif.codec.MSVideoCodec;
import io.scif.common.Constants;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.ImageTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Vector;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * AVIReader is the file format reader for AVI files. Much of this code was
 * adapted from Wayne Rasband's AVI Movie Reader plugin for ImageJ (available at
 * <a href="http://rsb.info.nih.gov/ij">http://rsb.info.nih.gov/ij</a>).
 * 
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class AVIFormat extends AbstractFormat {

	// -- Supported compression types --

	private static final int MSRLE = 1;
	private static final int MS_VIDEO = 1296126531;
	// private static final int CINEPAK = 1684633187;
	private static final int JPEG = 1196444237;
	private static final int Y8 = 538982489;

	// -- Constants --

	/** Huffman table for MJPEG data. */
	private static final byte[] MJPEG_HUFFMAN_TABLE = new byte[] { (byte) 0xff,
		(byte) 0xc4, 1, (byte) 0xa2, 0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0,
		0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 0, 3, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0x10, 0, 2, 1,
		3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7D, 1, 2, 3, 0, 4, 0x11, 5, 0x12,
		0x21, 0x31, 0x41, 6, 0x13, 0x51, 0x61, 7, 0x22, 0x71, 0x14, 0x32,
		(byte) 0x81, (byte) 0x91, (byte) 0xa1, 8, 0x23, 0x42, (byte) 0xb1,
		(byte) 0xc1, 0x15, 0x52, (byte) 0xd1, (byte) 0xf0, 0x24, 0x33, 0x62, 0x72,
		(byte) 0x82, 9, 10, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
		0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x43, 0x44, 0x45,
		0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
		0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
		0x76, 0x77, 0x78, 0x79, 0x7a, (byte) 0x83, (byte) 0x84, (byte) 0x85,
		(byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8a,
		(byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96,
		(byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0x9a, (byte) 0xa2,
		(byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7,
		(byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xb2, (byte) 0xb3,
		(byte) 0xb4, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7, (byte) 0xb8,
		(byte) 0xb9, (byte) 0xba, (byte) 0xc2, (byte) 0xc3, (byte) 0xc4,
		(byte) 0xc5, (byte) 0xc6, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9,
		(byte) 0xca, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5,
		(byte) 0xd6, (byte) 0xd7, (byte) 0xd8, (byte) 0xd9, (byte) 0xda,
		(byte) 0xe1, (byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe5,
		(byte) 0xe6, (byte) 0xe7, (byte) 0xe8, (byte) 0xe9, (byte) 0xea,
		(byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5,
		(byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, 0x11, 0,
		2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77, 0, 1, 2, 3, 0x11, 4, 5,
		0x21, 0x31, 6, 0x12, 0x41, 0x51, 7, 0x61, 0x71, 0x13, 0x22, 0x32,
		(byte) 0x81, 8, 0x14, 0x42, (byte) 0x91, (byte) 0xa1, (byte) 0xb1,
		(byte) 0xc1, 9, 0x23, 0x33, 0x52, (byte) 0xf0, 0x15, 0x62, 0x72,
		(byte) 0xd1, 10, 0x16, 0x24, 0x34, (byte) 0xe1, 0x25, (byte) 0xf1, 0x17,
		0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38,
		0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54,
		(byte) 0x55, (byte) 0x56, (byte) 0x57, (byte) 0x58, (byte) 0x59, 0x5a,
		0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76,
		0x77, 0x78, 0x79, 0x7a, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85,
		(byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8a,
		(byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96,
		(byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0x9a, (byte) 0xa2,
		(byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7,
		(byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xb2, (byte) 0xb3,
		(byte) 0xb4, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7, (byte) 0xb8,
		(byte) 0xb9, (byte) 0xba, (byte) 0xc2, (byte) 0xc3, (byte) 0xc4,
		(byte) 0xc5, (byte) 0xc6, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9,
		(byte) 0xca, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5,
		(byte) 0xd6, (byte) 0xd7, (byte) 0xd8, (byte) 0xd9, (byte) 0xda,
		(byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe5, (byte) 0xe6,
		(byte) 0xe7, (byte) 0xe8, (byte) 0xe9, (byte) 0xea, (byte) 0xf2,
		(byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7,
		(byte) 0xf8, (byte) 0xf9, (byte) 0xfa };

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Audio Video Interleave";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "avi" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Constants --
		public static final String CNAME = "io.scif.formats.AVIFormat$Metadata";

		// -- AVI Metadata --

		/* Offset to each plane. */
		private Vector<Long> offsets;

		/* Number of bytes in each plane. */
		private Vector<Long> lengths;

		private short bmpBitsPerPixel;
		private int bmpCompression, bmpScanLineSize;
		private int bmpColorsUsed, bmpWidth;

		private int bytesPerPlane;

		private ColorTable lut;

		// -- Cached plane --

		private ByteArrayPlane lastPlane;
		private int lastPlaneIndex;

		// -- Metadata Accessors --

		public short getBmpBitsPerPixel() {
			return bmpBitsPerPixel;
		}

		public void setBmpBitsPerPixel(final short bmpBitsPerPixel) {
			this.bmpBitsPerPixel = bmpBitsPerPixel;
		}

		public int getBmpCompression() {
			return bmpCompression;
		}

		public void setBmpCompression(final int bmpCompression) {
			this.bmpCompression = bmpCompression;
		}

		public int getBmpScanLineSize() {
			return bmpScanLineSize;
		}

		public void setBmpScanLineSize(final int bmpScanLineSize) {
			this.bmpScanLineSize = bmpScanLineSize;
		}

		public Vector<Long> getOffsets() {
			return offsets;
		}

		public void setOffsets(final Vector<Long> offsets) {
			this.offsets = offsets;
		}

		public Vector<Long> getLengths() {
			return lengths;
		}

		public void setLengths(final Vector<Long> lengths) {
			this.lengths = lengths;
		}

		public int getBmpColorsUsed() {
			return bmpColorsUsed;
		}

		public void setBmpColorsUsed(final int bmpColorsUsed) {
			this.bmpColorsUsed = bmpColorsUsed;
		}

		public int getBmpWidth() {
			return bmpWidth;
		}

		public void setBmpWidth(final int bmpWidth) {
			this.bmpWidth = bmpWidth;
		}

		public ByteArrayPlane getLastPlane() {
			return lastPlane;
		}

		public byte[] getLastPlaneBytes() {
			return lastPlane == null ? null : lastPlane.getBytes();
		}

		public void setLastPlane(final ByteArrayPlane lastPlane) {
			this.lastPlane = lastPlane;
		}

		public int getLastPlaneIndex() {
			return lastPlaneIndex;
		}

		public void setLastPlaneIndex(final int lastPlaneIndex) {
			this.lastPlaneIndex = lastPlaneIndex;
		}

		public int getBytesPerPlane() {
			return bytesPerPlane;
		}

		public void setBytesPerPlane(final int bytesPerPlane) {
			this.bytesPerPlane = bytesPerPlane;
		}

		// -- HasColorTable API Methods --

		@Override
		public ColorTable getColorTable(final int imageIndex, final int planeIndex)
		{
			return lut;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			iMeta.setLittleEndian(true);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);

			// All planes are timepoints
			int sizeT = getOffsets().size();

			getTable().put("Compression", AVIUtils.getCodecName(getBmpCompression()));
			iMeta.setPlanarAxisCount(2);

			if (getBmpCompression() == JPEG) {
				final long fileOff = getOffsets().get(0).longValue();

				final CodecOptions options = AVIUtils.createCodecOptions(this, 0, 0);

				int nBytes = 0;
				try {
					nBytes =
						AVIUtils.extractCompression(this, options, getSource(), null, 0).length /
							(int)(iMeta.getAxisLength(Axes.X) * iMeta.getAxisLength(Axes.Y));
				}
				catch (final IOException e) {
					log().error("IOException while decompressing", e);
				}
				catch (final FormatException e) {
					log().error("FormatException while decompressing", e);
				}

				try {
					getSource().seek(fileOff);
				}
				catch (final IOException e) {
					log().error("Error seeking to position: " + fileOff, e);
				}

				if (getBmpCompression() == 16) {
					nBytes /= 2;
				}
				if (nBytes > 1) {
					iMeta.addAxis(Axes.CHANNEL, nBytes); 
					iMeta.setPlanarAxisCount(3);
				}
			}
			else if (getBmpBitsPerPixel() == 32) {
				iMeta.addAxis(Axes.CHANNEL, 4); 
				iMeta.setPlanarAxisCount(3);
			}
			else if (getBytesPerPlane() == 0 || getBmpBitsPerPixel() == 24) {
				if (getBmpBitsPerPixel() > 8 ||
					(getBmpCompression() != 0 && getColorTable(0, 0) == null))
				{
					iMeta.addAxis(Axes.CHANNEL, 3);
					iMeta.setPlanarAxisCount(3);
				}
			}
			else if (getBmpCompression() == MS_VIDEO) {
				iMeta.addAxis(Axes.CHANNEL, 3);
				iMeta.setPlanarAxisCount(3);
			}
			else {
				final long sizeC =
					getBytesPerPlane() /
						(iMeta.getAxisLength(Axes.X) * iMeta.getAxisLength(Axes.Y) * (getBmpBitsPerPixel() / 8));
				if (sizeC > 1) {
					iMeta.addAxis(Axes.CHANNEL, sizeC); 
					iMeta.setPlanarAxisCount(3);
				}
			}

			if (getColorTable(0, 0) != null && !iMeta.isMultichannel()) {
				iMeta.setIndexed(true);
				iMeta.addAxis(Axes.CHANNEL, 1);
				iMeta.setAxisType(2, Axes.CHANNEL);
				iMeta.setPlanarAxisCount(3);
			}

			iMeta.setBitsPerPixel(getBmpBitsPerPixel());
			if (getBmpBitsPerPixel() <= 8) {
				iMeta.setPixelType(FormatTools.UINT8);
			}
			else if (getBmpBitsPerPixel() == 16) iMeta
				.setPixelType(FormatTools.UINT16);
			else if (getBmpBitsPerPixel() == 24 || getBmpBitsPerPixel() == 32) {
				iMeta.setPixelType(FormatTools.UINT8);
			}
			else {
				log().error(
					"Unknown matching for pixel bit width of: " + getBmpBitsPerPixel());
			}

			if (getBmpCompression() != 0) iMeta.setPixelType(FormatTools.UINT8);

			int effectiveWidth = getBmpScanLineSize() / (getBmpBitsPerPixel() / 8);
			if (effectiveWidth == 0) {
				effectiveWidth = (int)iMeta.getAxisLength(Axes.X);
			}
			if (effectiveWidth < iMeta.getAxisLength(Axes.X)) {
				iMeta.setAxisLength(Axes.X, effectiveWidth);
			}

			if (getBmpBitsPerPixel() != 16 && iMeta.isMultichannel()) {
				iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y); 
			}

			iMeta.addAxis(Axes.TIME, sizeT); 
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				lastPlane = null;
				lastPlaneIndex = -1;
				bmpColorsUsed = bmpWidth = bmpCompression = bmpScanLineSize = 0;
				bmpBitsPerPixel = 0;
				bytesPerPlane = 0;
				offsets = null;
				lengths = null;
				lut = null;
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final String AVI_MAGIC_STRING = "RIFF";

		// -- Constructor --

		public Checker() {
			suffixNecessary = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 12;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			final String type = stream.readString(4);
			stream.skipBytes(4);
			final String format = stream.readString(4);
			return type.equals(AVI_MAGIC_STRING) && format.equals("AVI ");
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Fields --
		private String type = "error";
		private String fcc = "error";
		private int size = -1;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			stream.order(true);

			log().info("Verifying AVI format");

			meta.setLastPlaneIndex(-1);
			meta.setLengths(new Vector<Long>());
			meta.setOffsets(new Vector<Long>());

			meta.createImageMetadata(1);

			while (stream.getFilePointer() < stream.length() - 8) {
				// NB: size x and size y are implicitly set here
				readChunk(meta);
			}

			log().info("Populating metadata");

		}

		// -- Helper Methods --

		private void readChunkHeader() throws IOException {
			readTypeAndSize();
			fcc = in.readString(4);
		}

		private void readTypeAndSize() throws IOException {
			type = in.readString(4);
			size = in.readInt();
		}

		private void readChunk(final Metadata meta) throws FormatException,
			IOException
		{
			String listString;

			long pos;

			readChunkHeader();
			final ImageMetadata m = meta.get(0);

			if (type.equals("RIFF")) {
				if (!fcc.startsWith("AVI")) {
					throw new FormatException("Sorry, AVI RIFF format not found.");
				}
			}
			else if (in.getFilePointer() == 12) {
				throw new FormatException("Not an AVI file");
			}
			else {
				if (in.getFilePointer() + size - 4 <= in.length()) {
					in.skipBytes(size - 4);
				}
				return;
			}

			pos = in.getFilePointer();
			long spos = pos;

			log().info("Searching for image data");

			while ((in.length() - in.getFilePointer()) > 4) {
				listString = in.readString(4);
				if (listString.equals("RIFF")) {
					in.seek(in.getFilePointer() - 4);
					return;
				}
				in.seek(pos);
				if (listString.equals(" JUN")) {
					in.skipBytes(1);
					pos++;
				}

				if (listString.equals("JUNK")) {
					readTypeAndSize();

					if (type.equals("JUNK")) {
						in.skipBytes(size);
					}
				}
				else if (listString.equals("LIST")) {
					spos = in.getFilePointer();
					readChunkHeader();

					in.seek(spos);
					if (fcc.equals("hdrl")) {
						readChunkHeader();

						if (type.equals("LIST")) {
							if (fcc.equals("hdrl")) {
								readTypeAndSize();
								if (type.equals("avih")) {
									spos = in.getFilePointer();

									addGlobalMeta("Microseconds per frame", in.readInt());
									addGlobalMeta("Max. bytes per second", in.readInt());

									in.skipBytes(8);

									addGlobalMeta("Total frames", in.readInt());
									addGlobalMeta("Initial frames", in.readInt());

									in.skipBytes(8);
									m.addAxis(Axes.X, in.readInt());

									addGlobalMeta("Frame height", in.readInt());
									addGlobalMeta("Scale factor", in.readInt());
									addGlobalMeta("Frame rate", in.readInt());
									addGlobalMeta("Start time", in.readInt());
									addGlobalMeta("Length", in.readInt());

									addGlobalMeta("Frame width", m.getAxisLength(Axes.X));

									if (spos + size <= in.length()) {
										in.seek(spos + size);
									}
								}
							}
						}
					}
					else if (fcc.equals("strl")) {
						final long startPos = in.getFilePointer();
						final long streamSize = size;

						readChunkHeader();

						if (type.equals("LIST")) {
							if (fcc.equals("strl")) {
								readTypeAndSize();

								if (type.equals("strh")) {
									spos = in.getFilePointer();
									in.skipBytes(40);

									addGlobalMeta("Stream quality", in.readInt());
									meta.setBytesPerPlane(in.readInt());
									addGlobalMeta("Stream sample size", meta.getBytesPerPlane());

									if (spos + size <= in.length()) {
										in.seek(spos + size);
									}
								}

								readTypeAndSize();
								if (type.equals("strf")) {
									spos = in.getFilePointer();

									if (meta.getAxisIndex(0, Axes.Y) != -1) {
										in.skipBytes(size);
										readTypeAndSize();
										while (type.equals("indx")) {
											in.skipBytes(size);
											readTypeAndSize();
										}
										pos = in.getFilePointer() - 4;
										in.seek(pos - 4);
										continue;
									}

									in.skipBytes(4);
									meta.setBmpWidth(in.readInt());
									m.addAxis(Axes.Y, in.readInt());
									in.skipBytes(2);
									meta.setBmpBitsPerPixel(in.readShort());
									meta.setBmpCompression(in.readInt());
									in.skipBytes(4);

									addGlobalMeta("Horizontal resolution", in.readInt());
									addGlobalMeta("Vertical resolution", in.readInt());

									meta.setBmpColorsUsed(in.readInt());
									in.skipBytes(4);

									addGlobalMeta("Bitmap compression value", meta
										.getBmpCompression());
									addGlobalMeta("Number of colors used", meta
										.getBmpColorsUsed());
									addGlobalMeta("Bits per pixel", meta.getBmpBitsPerPixel());

									// scan line is padded with zeros to be a multiple of 4 bytes
									int npad = meta.getBmpWidth() % 4;
									if (npad > 0) npad = 4 - npad;

									meta.setBmpScanLineSize((meta.getBmpWidth() + npad) *
										(meta.getBmpBitsPerPixel() / 8));

									int bmpActualColorsUsed = 0;
									if (meta.getBmpColorsUsed() != 0) {
										bmpActualColorsUsed = meta.getBmpColorsUsed();
									}
									else if (meta.getBmpBitsPerPixel() < 16) {
										// a value of 0 means we determine this based on the
										// bits per pixel
										bmpActualColorsUsed = 1 << meta.getBmpBitsPerPixel();
										meta.setBmpColorsUsed(bmpActualColorsUsed);
									}

									if (meta.getBmpCompression() != MSRLE &&
										meta.getBmpCompression() != 0 &&
										meta.getBmpCompression() != MS_VIDEO &&
										meta.getBmpCompression() != JPEG &&
										meta.getBmpCompression() != Y8)
									{
										throw new UnsupportedCompressionException(meta
											.getBmpCompression() +
											" not supported");
									}

									if (!(meta.getBmpBitsPerPixel() == 4 ||
										meta.getBmpBitsPerPixel() == 8 ||
										meta.getBmpBitsPerPixel() == 24 ||
										meta.getBmpBitsPerPixel() == 16 || meta
											.getBmpBitsPerPixel() == 32))
									{
										throw new FormatException(meta.getBmpBitsPerPixel() +
											" bits per pixel not supported");
									}

									if (bmpActualColorsUsed != 0) {
										// read the palette
										final byte[][] lut = new byte[3][meta.getBmpColorsUsed()];

										for (int i = 0; i < meta.getBmpColorsUsed(); i++) {
											if (meta.getBmpCompression() != Y8) {
												lut[2][i] = in.readByte();
												lut[1][i] = in.readByte();
												lut[0][i] = in.readByte();
												in.skipBytes(1);
											}
											else {
												lut[0][i] = (byte) i;
												lut[1][i] = (byte) i;
												lut[2][i] = (byte) i;
											}
										}

										meta.lut = new ColorTable8(lut[0], lut[1], lut[2]);
									}

									in.seek(spos + size);
								}
							}

							spos = in.getFilePointer();
							readTypeAndSize();
							if (type.equals("strd")) {
								in.skipBytes(size);
							}
							else {
								in.seek(spos);
							}

							spos = in.getFilePointer();
							readTypeAndSize();
							if (type.equals("strn") || type.equals("indx")) {
								in.skipBytes(size);
							}
							else {
								in.seek(spos);
							}
						}

						if (startPos + streamSize + 8 <= in.length()) {
							in.seek(startPos + 8 + streamSize);
						}
					}
					else if (fcc.equals("movi")) {
						readChunkHeader();

						if (type.equals("LIST")) {
							if (fcc.equals("movi")) {
								spos = in.getFilePointer();
								if (spos >= in.length() - 12) break;
								readChunkHeader();
								if (!(type.equals("LIST") && (fcc.equals("rec ") || fcc
									.equals("movi"))))
								{
									in.seek(spos);
								}

								spos = in.getFilePointer();
								boolean end = false;
								while (!end) {
									readTypeAndSize();
									final String oldType = type;
									while (type.startsWith("ix") || type.endsWith("tx") ||
										type.equals("JUNK"))
									{
										in.skipBytes(size);
										readTypeAndSize();
									}

									String check = type.substring(2);
									boolean foundPixels = false;
									while (check.equals("db") || check.equals("dc") ||
										check.equals("wb"))
									{
										foundPixels = true;
										if (check.startsWith("d")) {
											if (size > 0 || meta.getBmpCompression() != 0) {
												meta.getOffsets().add(new Long(in.getFilePointer()));
												meta.getLengths().add(new Long(size));
												in.skipBytes(size);
											}
										}

										spos = in.getFilePointer();
										if (spos + 8 >= in.length()) return;

										readTypeAndSize();
										if (type.equals("JUNK")) {
											in.skipBytes(size);
											spos = in.getFilePointer();
											if (spos + 8 >= in.length()) return;
											readTypeAndSize();
										}
										check = type.substring(2);
										if (check.equals("0d")) {
											in.seek(spos + 1);
											readTypeAndSize();
											check = type.substring(2);
										}
									}
									in.seek(spos);
									if (!oldType.startsWith("ix") && !foundPixels) {
										end = true;
									}
								}
							}
						}
					}
					else {
						final int oldSize = size;
						size = in.readInt() - 8;
						if (size > oldSize) {
							size = oldSize;
							in.seek(in.getFilePointer() - 4);
						}
						// skipping unknown block
						if (size + 8 >= 0) in.skipBytes(8 + size);
					}
				}
				else {
					// skipping unknown block
					readTypeAndSize();
					if (in.getFilePointer() + 8 < in.length() && !type.equals("idx1")) {
						readTypeAndSize();
					}
					else if (!type.equals("idx1")) break;
					if (in.getFilePointer() + size + 4 <= in.length()) {
						in.skipBytes(size);
					}
					if (type.equals("idx1")) break;
				}
				pos = in.getFilePointer();
			}
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final byte[] buf = plane.getBytes();
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);
			plane.setColorTable(meta.getColorTable(0, 0));

			final int bytes =
				FormatTools.getBytesPerPixel(meta.getPixelType(imageIndex));
			final double p =
				((double) meta.getBmpScanLineSize()) / meta.getBmpBitsPerPixel();
			int effectiveWidth = (int) (meta.getBmpScanLineSize() / p);
			if (effectiveWidth == 0 ||
				effectiveWidth < meta.getAxisLength(imageIndex, Axes.X))
			{
				effectiveWidth = (int)meta.getAxisLength(imageIndex, Axes.X);
			}
			final int xAxis = meta.getAxisIndex(imageIndex, Axes.X);
			final int yAxis = meta.getAxisIndex(imageIndex, Axes.Y);
			final int x = (int) planeMin[xAxis],
								y = (int) planeMin[yAxis],
								w = (int) planeMax[xAxis],
								h = (int) planeMax[yAxis];

			final long fileOff = meta.getOffsets().get(planeIndex).longValue();
			final long end =
				planeIndex < meta.getOffsets().size() - 1 ? meta.getOffsets().get(
					planeIndex + 1) : getStream().length();
			final long maxBytes = end - fileOff;
			getStream().seek(fileOff);

			if (meta.getBmpCompression() != 0 && meta.getBmpCompression() != Y8) {
				uncompress(imageIndex, planeIndex, plane, x, y, w, h);
//        byte[] b = uncompress(imageIndex, planeIndex, plane, x, y, w, h).getBytes();

//        b = null;
				return plane;
			}

			if (meta.getBmpBitsPerPixel() < 8) {
				int rawSize =
					(int)FormatTools.getPlaneSize(meta, effectiveWidth, (int)meta.getAxisLength(
						imageIndex, Axes.Y), imageIndex);
				rawSize /= (8 / meta.getBmpBitsPerPixel());

				final byte[] b = new byte[rawSize];

				final int len = rawSize / (int)meta.getAxisLength(imageIndex, Axes.Y);
				getStream().read(b);

				final BitBuffer bb = new BitBuffer(b);
				bb.skipBits(meta.getBmpBitsPerPixel() * len *
					(meta.getAxisLength(imageIndex, Axes.Y) - h - y));

				for (int row = h; row >= y; row--) {
					bb.skipBits(meta.getBmpBitsPerPixel() * x);
					for (int col = 0; col < len; col++) {
						buf[(row - y) * len + col] =
							(byte) bb.getBits(meta.getBmpBitsPerPixel());
					}
					bb.skipBits(meta.getBmpBitsPerPixel() *
						(meta.getAxisLength(imageIndex, Axes.X)) - w - x);
				}

				return plane;
			}

			final int pad = (int)
				((meta.getBmpScanLineSize() / meta.getAxisLength(imageIndex,
					Axes.CHANNEL)) -
					meta.getAxisLength(imageIndex, Axes.X) * bytes);
			final int scanline =
				w *
					bytes *
					(int)(meta.isInterleaved(imageIndex) ? meta.getAxisLength(imageIndex, Axes.CHANNEL)
						: 1);

			getStream().skipBytes(
				(int)((meta.getAxisLength(imageIndex, Axes.X) + pad) * bytes *
					(meta.getAxisLength(imageIndex, Axes.Y) - h - y)));

			if (meta.getAxisLength(imageIndex, Axes.X) == w && pad == 0) {
				for (int row = 0; row < meta.getAxisLength(imageIndex, Axes.Y); row++) {
					final int outputRow =
						(int) (meta.getBmpCompression() == Y8 ? row : meta.getAxisLength(
							imageIndex, Axes.Y) -
							row - 1);
					getStream().read(buf, outputRow * scanline, scanline);
				}

				// swap channels
				if (meta.getBmpBitsPerPixel() == 24 || meta.getBmpBitsPerPixel() == 32)
				{
					for (int i = 0; i < buf.length /
						meta.getAxisLength(imageIndex, Axes.CHANNEL); i++)
					{
						final byte r =
							buf[i * (int)meta.getAxisLength(imageIndex, Axes.CHANNEL) + 2];
						buf[i * (int)meta.getAxisLength(imageIndex, Axes.CHANNEL) + 2] =
							buf[i * (int)meta.getAxisLength(imageIndex, Axes.CHANNEL)];
	            buf[i * (int)meta.getAxisLength(imageIndex, Axes.CHANNEL)] = r; 
					}
				}
			}
			else {
				int skip =
					(int) FormatTools.getPlaneSize(meta, (int) meta.getAxisLength(
						imageIndex, Axes.X) -
						w - x + pad, 1, imageIndex);
				if ((meta.getAxisLength(imageIndex, Axes.X) + pad) *
					meta.getAxisLength(imageIndex, Axes.Y) *
					meta.getAxisLength(imageIndex, Axes.CHANNEL) > maxBytes)
				{
					skip /= meta.getAxisLength(imageIndex, Axes.CHANNEL);
				}
				for (int i = h - 1; i >= 0; i--) {
					getStream().skipBytes(x * (meta.getBmpBitsPerPixel() / 8));
					getStream().read(buf, (i - y) * scanline, scanline);
					if (meta.getBmpBitsPerPixel() == 24) {
						for (int j = 0; j < w; j++) {
							final byte r = buf[i * scanline + j * 3 + 2];
							buf[i * scanline + j * 3 + 2] = buf[i * scanline + j * 3];
							buf[i * scanline + j * 3] = r;
						}
					}
					if (i > 0) getStream().skipBytes(skip);
				}
			}

			if (meta.getBmpBitsPerPixel() == 16 && meta.isMultichannel(imageIndex)) {
				// channels are stored as BGR, need to swap them
				ImageTools.bgrToRgb(plane.getBytes(), meta.isInterleaved(imageIndex),
					2, (int)meta.getAxisLength(imageIndex, Axes.CHANNEL));
			}

			return plane;
		}

		// -- Helper methods --

		private ByteArrayPlane uncompress(final int imageIndex,
			final int planeIndex, final ByteArrayPlane plane, final int x,
			final int y, final int w, final int h) throws FormatException,
			IOException
		{
			final Metadata meta = getMetadata();
			byte[] buf = null;

			if (meta.getLastPlaneIndex() == planeIndex) {
				buf = meta.getLastPlane().getBytes();
			}
			else {
				final CodecOptions options =
					AVIUtils.createCodecOptions(meta, imageIndex, planeIndex);

				// if not full plane, open the full plane and then decompress

				final ByteArrayPlane tmpPlane =
					createPlane(new long[meta.getPlanarAxisCount(imageIndex)], meta
						.getAxesLengthsPlanar(imageIndex));

				if (options.previousImage == null && meta.getBmpCompression() != JPEG) {
					while (meta.getLastPlaneIndex() < planeIndex - 1) {
						openPlane(imageIndex, meta.getLastPlaneIndex() + 1, tmpPlane);
					}
					options.previousImage = meta.getLastPlaneBytes();
				}

				final long fileOff = meta.getOffsets().get(planeIndex).longValue();
				getStream().seek(fileOff);

				buf =
					AVIUtils.extractCompression(meta, options, getStream(), tmpPlane,
						planeIndex);
			}

			final int rowLen = (int)FormatTools.getPlaneSize(meta, w, 1, imageIndex);
			final int bytes =
				FormatTools.getBytesPerPixel(meta.getPixelType(imageIndex));
			final int inputRowLen =
				(int)FormatTools.getPlaneSize(meta, (int)meta.getAxisLength(imageIndex, Axes.X),
					1, imageIndex);

			for (int row = 0; row < h; row++) {
				System.arraycopy(buf, (row + y) * inputRowLen + x * bytes, plane
					.getBytes(), row * rowLen, rowLen);
			}

			return plane;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constants --

		private static final long SAVE_MOVI = 4092;
		private static final long SAVE_FILE_SIZE = 4;

		// location of length of strf CHUNK - not including the first 8 bytes with
		// strf and size. strn follows the end of this CHUNK.
		private static final long SAVE_STRF_SIZE = 168;

		private static final long SAVE_STRN_POS = SAVE_STRF_SIZE + 1068;
		private static final long SAVE_JUNK_SIG = SAVE_STRN_POS + 24;

		// location of length of CHUNK with first LIST - not including first 8
		// bytes with LIST and size. JUNK follows the end of this CHUNK
		private static final long SAVE_LIST1_SIZE = 16;

		// location of length of CHUNK with second LIST - not including first 8
		// bytes with LIST and size. Note that saveLIST1subSize = saveLIST1Size +
		// 76, and that the length size written to saveLIST2Size is 76 less than
		// that written to saveLIST1Size. JUNK follows the end of this CHUNK.
		private static final long SAVE_LIST1_SUBSIZE = 92;

		private static final long FRAME_OFFSET = 48;
		private static final long FRAME_OFFSET_2 = 140;
		private static final long PADDING_BYTES = 4076 - SAVE_JUNK_SIG;
		private static final long SAVE_LIST2_SIZE = 4088;
		private static final String DATA_SIGNATURE = "00db";

		// -- Fields --

		private int planesWritten = 0;

		private int bytesPerPixel;
		private int xDim, yDim, zDim, tDim, xPad;
		private int microSecPerFrame;

		private Vector<Long> savedbLength;
		private long idx1Pos;
		private long endPos;
		private long saveidx1Length;

		// -- Writer API Methods --

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();

			checkParams(imageIndex, planeIndex, buf, planeMin, planeMax);
			if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, planeMin, planeMax)) {
				throw new FormatException(
					"AVIWriter does not yet support saving image tiles.");
			}

			final int nChannels = (int)meta.getAxisLength(imageIndex, Axes.CHANNEL);

			if (!initialized[imageIndex][planeIndex]) {
				initialized[imageIndex][planeIndex] = true;
			}

			// Write the data. Each 3-byte triplet in the bitmap array represents the
			// relative intensities of blue, green, and red, respectively, for a
			// pixel.
			// The color bytes are in reverse order from the Windows convention.

			final int width = xDim - xPad;
			final int height = buf.length / (width * bytesPerPixel);

			out.seek(idx1Pos);
			out.writeBytes(DATA_SIGNATURE);
			savedbLength.add(new Long(out.getFilePointer()));

			// Write the data length
			out.writeInt(bytesPerPixel * xDim * yDim);

			final int rowPad = xPad * bytesPerPixel;

			final byte[] rowBuffer = new byte[width * bytesPerPixel + rowPad];

			for (int row = height - 1; row >= 0; row--) {
				for (int col = 0; col < width; col++) {
					int offset = row * width + col;
					if (interleaved) offset *= nChannels;
					final byte r = buf[offset];
					if (nChannels > 1) {
						final byte g = buf[offset + (interleaved ? 1 : width * height)];
						byte b = 0;
						if (nChannels > 2) {
							b = buf[offset + (interleaved ? 2 : 2 * width * height)];
						}

						rowBuffer[col * bytesPerPixel] = b;
						rowBuffer[col * bytesPerPixel + 1] = g;
					}
					rowBuffer[col * bytesPerPixel + bytesPerPixel - 1] = r;
				}
				out.write(rowBuffer);
			}

			planesWritten++;

			// Write the idx1 CHUNK
			// Write the 'idx1' signature
			idx1Pos = out.getFilePointer();
			out.seek(SAVE_LIST2_SIZE);
			out.writeInt((int) (idx1Pos - (SAVE_LIST2_SIZE + 4)));

			out.seek(idx1Pos);
			out.writeBytes("idx1");

			saveidx1Length = out.getFilePointer();

			// Write the length of the idx1 CHUNK not including the idx1 signature
			out.writeInt(4 + (planesWritten * 16));

			for (int z = 0; z < planesWritten; z++) {
				// In the ckid field write the 4 character code to identify the chunk
				// 00db or 00dc
				out.writeBytes(DATA_SIGNATURE);
				// Write the flags - select AVIIF_KEYFRAME
				if (z == 0) out.writeInt(0x10);
				else out.writeInt(0x00);

				// AVIIF_KEYFRAME 0x00000010L
				// The flag indicates key frames in the video sequence.
				// Key frames do not need previous video information to be
				// decompressed.
				// AVIIF_NOTIME 0x00000100L The CHUNK does not influence video timing
				// (for example a palette change CHUNK).
				// AVIIF_LIST 0x00000001L Marks a LIST CHUNK.
				// AVIIF_TWOCC 2L
				// AVIIF_COMPUSE 0x0FFF0000L These bits are for compressor use.
				out.writeInt((int) (savedbLength.get(z) - 4 - SAVE_MOVI));

				// Write the offset (relative to the 'movi' field) to the relevant
				// CHUNK. Write the length of the relevant CHUNK. Note that this length
				// is also written at savedbLength
				out.writeInt(bytesPerPixel * xDim * yDim);
			}
			endPos = out.getFilePointer();
			out.seek(SAVE_FILE_SIZE);
			out.writeInt((int) (endPos - (SAVE_FILE_SIZE + 4)));

			out.seek(saveidx1Length);
			out.writeInt((int) (endPos - (saveidx1Length + 4)));

			// write the total number of planes
			out.seek(FRAME_OFFSET);
			out.writeInt(planesWritten);
			out.seek(FRAME_OFFSET_2);
			out.writeInt(planesWritten);
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8 };
		}

		@Override
		public void close() throws IOException {
			super.close();
			planesWritten = 0;
			bytesPerPixel = 0;
			xDim = yDim = zDim = tDim = xPad = 0;
			microSecPerFrame = 0;
			savedbLength = null;
			idx1Pos = 0;
			endPos = 0;
			saveidx1Length = 0;
		}

		@Override
		public void
			setDest(final RandomAccessOutputStream out, final int imageIndex)
				throws FormatException, IOException
		{
			super.setDest(out, imageIndex);
			initialize(imageIndex);
		}

		// -- Helper Methods --

		private void initialize(final int imageIndex) throws IOException {
			savedbLength = new Vector<Long>();

			final Metadata meta = getMetadata();

			if (out.length() > 0) {
				final RandomAccessInputStream in =
					new RandomAccessInputStream(getContext(), meta.getDatasetName());
				in.order(true);
				in.seek(FRAME_OFFSET);
				planesWritten = in.readInt();

				in.seek(SAVE_FILE_SIZE);
				endPos = in.readInt() + SAVE_FILE_SIZE + 4;

				in.seek(SAVE_LIST2_SIZE);
				idx1Pos = in.readInt() + SAVE_LIST2_SIZE + 4;
				saveidx1Length = idx1Pos + 4;

				if (planesWritten > 0) in.seek(saveidx1Length + 4);
				for (int z = 0; z < planesWritten; z++) {
					in.skipBytes(8);
					savedbLength.add(in.readInt() + 4 + SAVE_MOVI);
					in.skipBytes(4);
				}
				in.close();
				out.seek(idx1Pos);
			}

			out.order(true);

			tDim = (int)meta.getAxisLength(imageIndex, Axes.Z);
			zDim = (int)meta.getAxisLength(imageIndex, Axes.TIME);
			yDim = (int)meta.getAxisLength(imageIndex, Axes.Y);
			xDim = (int)meta.getAxisLength(imageIndex, Axes.X);
			final String type =
				FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
			final int pixelType = FormatTools.pixelTypeFromString(type);
			bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
			bytesPerPixel *= meta.getAxisLength(imageIndex, Axes.CHANNEL);

			xPad = 0;
			final int xMod = xDim % 4;
			if (xMod != 0) {
				xPad = 4 - xMod;
				xDim += xPad;
			}

			byte[][] lut = null;

			if (getColorModel() instanceof IndexColorModel) {
				lut = new byte[4][256];
				final IndexColorModel model = (IndexColorModel) getColorModel();
				model.getReds(lut[0]);
				model.getGreens(lut[1]);
				model.getBlues(lut[2]);
				model.getAlphas(lut[3]);
			}

			if (out.length() == 0) {
				out.writeBytes("RIFF"); // signature
				// Bytes 4 thru 7 contain the length of the file. This length does
				// not include bytes 0 thru 7.
				out.writeInt(0); // for now write 0 for size
				out.writeBytes("AVI "); // RIFF type
				// Write the first LIST chunk, which contains
				// information on data decoding
				out.writeBytes("LIST"); // CHUNK signature
				// Write the length of the LIST CHUNK not including the first 8 bytes
				// with LIST and size. Note that the end of the LIST CHUNK is followed
				// by JUNK.
				out.writeInt((bytesPerPixel == 1) ? 1240 : 216);
				out.writeBytes("hdrl"); // CHUNK type
				out.writeBytes("avih"); // Write the avih sub-CHUNK

				// Write the length of the avih sub-CHUNK (38H) not including the
				// the first 8 bytes for avihSignature and the length
				out.writeInt(0x38);

				// dwMicroSecPerFrame - Write the microseconds per frame
				microSecPerFrame = (int) (1.0 / fps * 1.0e6);
				out.writeInt(microSecPerFrame);

				// Write the maximum data rate of the file in bytes per second
				out.writeInt(0); // dwMaxBytesPerSec

				out.writeInt(0); // dwReserved1 - set to 0
				// dwFlags - just set the bit for AVIF_HASINDEX
				out.writeInt(0x10);

				// 10H AVIF_HASINDEX: The AVI file has an idx1 chunk containing
				// an index at the end of the file. For good performance, all
				// AVI files should contain an index.
				// 20H AVIF_MUSTUSEINDEX: Index CHUNK, rather than the physical
				// ordering of the chunks in the file, must be used to determine the
				// order of the frames.
				// 100H AVIF_ISINTERLEAVED: Indicates that the AVI file is interleaved.
				// This is used to read data from a CD-ROM more efficiently.
				// 800H AVIF_TRUSTCKTYPE: USE CKType to find key frames
				// 10000H AVIF_WASCAPTUREFILE: The AVI file is used for capturing
				// real-time video. Applications should warn the user before
				// writing over a file with this fla set because the user
				// probably defragmented this file.
				// 20000H AVIF_COPYRIGHTED: The AVI file contains copyrighted data
				// and software. When, this flag is used, software should not
				// permit the data to be duplicated.

				// dwTotalFrames - total frame number
				out.writeInt(0);

				// dwInitialFrames -Initial frame for interleaved files.
				// Noninterleaved files should specify 0.
				out.writeInt(0);

				// dwStreams - number of streams in the file - here 1 video and
				// zero audio.
				out.writeInt(1);

				// dwSuggestedBufferSize - Suggested buffer size for reading the file.
				// Generally, this size should be large enough to contain the largest
				// chunk in the file.
				out.writeInt(0);

				// dwWidth - image width in pixels
				out.writeInt(xDim - xPad);
				out.writeInt(yDim); // dwHeight - height in pixels

				// dwReserved[4] - Microsoft says to set the following 4 values to 0.
				out.writeInt(0);
				out.writeInt(0);
				out.writeInt(0);
				out.writeInt(0);

				// Write the Stream line header CHUNK
				out.writeBytes("LIST");

				// Write the size of the first LIST subCHUNK not including the first 8
				// bytes with LIST and size. Note that saveLIST1subSize = saveLIST1Size
				// + 76, and that the length written to saveLIST1subSize is 76 less than
				// the length written to saveLIST1Size. The end of the first LIST
				// subCHUNK is followed by JUNK.

				out.writeInt((bytesPerPixel == 1) ? 1164 : 140);
				out.writeBytes("strl"); // Write the chunk type
				out.writeBytes("strh"); // Write the strh sub-CHUNK
				out.writeInt(56); // Write length of strh sub-CHUNK

				// fccType - Write the type of data stream - here vids for video stream
				out.writeBytes("vids");

				// Write DIB for Microsoft Device Independent Bitmap.
				// Note: Unfortunately, at least 3 other four character codes are
				// sometimes used for uncompressed AVI videos: 'RGB ', 'RAW ',
				// 0x00000000
				out.writeBytes("DIB ");

				out.writeInt(0); // dwFlags

				// 0x00000001 AVISF_DISABLED The stram data should be rendered only when
				// explicitly enabled.
				// 0x00010000 AVISF_VIDEO_PALCHANGES Indicates that a palette change is
				// included in the AVI file. This flag warns the playback software that
				// it will need to animate the palette.

				// dwPriority - priority of a stream type. For example, in a file with
				// multiple audio streams, the one with the highest priority might be
				// the default one.
				out.writeInt(0);

				// dwInitialFrames - Specifies how far audio data is skewed ahead of
				// video frames in interleaved files. Typically, this is about 0.75
				// seconds. In interleaved files specify the number of frames in the
				// file prior to the initial frame of the AVI sequence.
				// Noninterleaved files should use zero.
				out.writeInt(0);

				// rate/scale = samples/second
				out.writeInt(1); // dwScale

				// dwRate - frame rate for video streams
				out.writeInt(fps);

				// dwStart - this field is usually set to zero
				out.writeInt(0);

				// dwLength - playing time of AVI file as defined by scale and rate
				// Set equal to the number of frames
				out.writeInt(tDim * zDim);

				// dwSuggestedBufferSize - Suggested buffer size for reading the stream.
				// Typically, this contains a value corresponding to the largest chunk
				// in a stream.
				out.writeInt(0);

				// dwQuality - encoding quality given by an integer between 0 and
				// 10,000. If set to -1, drivers use the default quality value.
				out.writeInt(-1);

				// dwSampleSize #
				// 0 if the video frames may or may not vary in size
				// If 0, each sample of data(such as a video frame) must be in a
				// separate chunk. If nonzero, then multiple samples of data can be
				// grouped into a single chunk within the file.
				out.writeInt(0);

				// rcFrame - Specifies the destination rectangle for a text or video
				// stream within the movie rectangle specified by the dwWidth and
				// dwHeight members of the AVI main header structure. The rcFrame member
				// is typically used in support of multiple video streams. Set this
				// rectangle to the coordinates corresponding to the movie rectangle to
				// update the whole movie rectangle. Units for this member are pixels.
				// The upper-left corner of the destination rectangle is relative to the
				// upper-left corner of the movie rectangle.
				out.writeShort((short) 0); // left
				out.writeShort((short) 0); // top
				out.writeShort((short) 0); // right
				out.writeShort((short) 0); // bottom

				// Write the size of the stream format CHUNK not including the first 8
				// bytes for strf and the size. Note that the end of the stream format
				// CHUNK is followed by strn.
				out.writeBytes("strf"); // Write the stream format chunk

				// write the strf CHUNK size
				out.writeInt((bytesPerPixel == 1) ? 1068 : 44);

				// Applications should use this size to determine which BITMAPINFO
				// header structure is being used. This size includes this biSize field.
				// biSize- Write header size of BITMAPINFO header structure

				out.writeInt(40);

				// biWidth - image width in pixels
				out.writeInt(xDim);

				// biHeight - image height in pixels. If height is positive, the bitmap
				// is a bottom up DIB and its origin is in the lower left corner. If
				// height is negative, the bitmap is a top-down DIB and its origin is
				// the upper left corner. This negative sign feature is supported by the
				// Windows Media Player, but it is not supported by PowerPoint.
				out.writeInt(yDim);

				// biPlanes - number of color planes in which the data is stored
				// This must be set to 1.
				out.writeShort(1);

				final int bitsPerPixel = (bytesPerPixel == 3) ? 24 : 8;

				// biBitCount - number of bits per pixel #
				// 0L for BI_RGB, uncompressed data as bitmap
				out.writeShort((short) bitsPerPixel);

				out.writeInt(0); // biSizeImage #
				out.writeInt(0); // biCompression - compression type
				// biXPelsPerMeter - horizontal resolution in pixels
				out.writeInt(0);
				// biYPelsPerMeter - vertical resolution in pixels per meter
				out.writeInt(0);

				final int nColors = 256;
				out.writeInt(nColors);

				// biClrImportant - specifies that the first x colors of the color table
				// are important to the DIB. If the rest of the colors are not
				// available, the image still retains its meaning in an acceptable
				// manner. When this field is set to zero, all the colors are important,
				// or, rather, their relative importance has not been computed.
				out.writeInt(0);

				// Write the LUTa.getExtents()[1] color table entries here. They are
				// written: blue byte, green byte, red byte, 0 byte
				if (bytesPerPixel == 1) {
					if (lut != null) {
						for (int i = 0; i < 256; i++) {
							out.write(lut[2][i]);
							out.write(lut[1][i]);
							out.write(lut[0][i]);
							out.write(lut[3][i]);
						}
					}
					else {
						final byte[] lutWrite = new byte[4 * 256];
						for (int i = 0; i < 256; i++) {
							lutWrite[4 * i] = (byte) i; // blue
							lutWrite[4 * i + 1] = (byte) i; // green
							lutWrite[4 * i + 2] = (byte) i; // red
							lutWrite[4 * i + 3] = 0;
						}
						out.write(lutWrite);
					}
				}

				out.seek(SAVE_STRF_SIZE);
				out.writeInt((int) (SAVE_STRN_POS - (SAVE_STRF_SIZE + 4)));
				out.seek(SAVE_STRN_POS);

				// Use strn to provide zero terminated text string describing the stream
				out.writeBytes("strn");
				out.writeInt(16); // Write length of strn sub-CHUNK
				out.writeBytes("FileAVI write  ");

				out.seek(SAVE_LIST1_SIZE);
				out.writeInt((int) (SAVE_JUNK_SIG - (SAVE_LIST1_SIZE + 4)));
				out.seek(SAVE_LIST1_SUBSIZE);
				out.writeInt((int) (SAVE_JUNK_SIG - (SAVE_LIST1_SUBSIZE + 4)));
				out.seek(SAVE_JUNK_SIG);

				// write a JUNK CHUNK for padding
				out.writeBytes("JUNK");
				out.writeInt((int) PADDING_BYTES);
				for (int i = 0; i < PADDING_BYTES / 2; i++) {
					out.writeShort((short) 0);
				}

				// Write the second LIST chunk, which contains the actual data
				out.writeBytes("LIST");

				out.writeInt(4); // For now write 0
				out.writeBytes("movi"); // Write CHUNK type 'movi'
				idx1Pos = out.getFilePointer();
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	@Plugin(type = io.scif.Translator.class, attrs = {
		@Attr(name = Translator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = Translator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class Translator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		@Override
		protected void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			final Vector<Long> offsets = new Vector<Long>();
			final Vector<Long> lengths = new Vector<Long>();
			dest.setOffsets(offsets);
			dest.setLengths(lengths);
			dest.createImageMetadata(1);

			int sizeX = (int)source.getAxisLength(0, Axes.X);
			final int sizeY = (int)source.getAxisLength(0, Axes.Y);
			final int bpp = source.getBitsPerPixel(0);
			long length = bpp;
			for (long l : source.getAxesLengthsPlanar(0)) {
				length *= l;
			}
			long offset = 0;

			dest.setAxisLength(0, Axes.X, sizeX);
			dest.setAxisLength(0, Axes.Y, sizeY);

			final int npad = sizeX % 4;

			if (npad != 0) sizeX += (4 - npad);

			dest.setBmpBitsPerPixel((short) (bpp * source.getAxisLength(0,
				Axes.CHANNEL)));

			dest.setBmpWidth(sizeX * (bpp / 8));
			dest.setBmpScanLineSize(dest.getBmpWidth() *
				(int)source.getAxisLength(0, Axes.CHANNEL));

			try {
				if (source.getSource() == null) offset = 0;
				else offset = source.getSource().getFilePointer();
			}
			catch (final IOException e) {
				log().error("Error retrieving AVI plane offset", e);
			}

			for (int i = 0; i < source.getPlaneCount(0); i++) {
				offsets.add(offset);

				lengths.add(length);
				offset += length;
			}

			dest.setBmpColorsUsed((int) Math.pow(2.0, bpp));

			dest.setBmpCompression(0);

			if (HasColorTable.class.isAssignableFrom(source.getClass())) {
				final ColorTable ct = ((HasColorTable) source).getColorTable(0, 0);
				dest.setBmpColorsUsed(ct.getLength());
			}

			dest.setBytesPerPlane((int) length / 8);
		}
	}

	/*
	 * Utility Helper class
	 */
	private static class AVIUtils {

		public static CodecOptions createCodecOptions(final Metadata meta,
			final int imageIndex, final int planeIndex)
		{
			final CodecOptions options = new CodecOptions();
			options.width = (int)meta.getAxisLength(imageIndex, Axes.X);
			options.height = (int)meta.getAxisLength(imageIndex, Axes.Y);
			options.previousImage =
				(meta.getLastPlaneIndex() == planeIndex - 1) ? meta.getLastPlaneBytes()
					: null;

			options.bitsPerSample = meta.getBmpBitsPerPixel();
			options.interleaved = meta.isInterleaved(imageIndex);
			options.littleEndian = meta.isLittleEndian(imageIndex);
			return options;
		}

		private static String getCodecName(final int bmpCompression) {
			switch (bmpCompression) {
				case 0:
					return "Raw (uncompressed)";
				case MSRLE:
					return "Microsoft Run-Length Encoding (MSRLE)";
				case MS_VIDEO:
					return "Microsoft Video (MSV1)";
				case JPEG:
					return "JPEG";
					// case CINEPAK:
					// return "Cinepak";
				default:
					return "Unknown";
			}
		}

		public static byte[] extractCompression(final Metadata meta,
			final CodecOptions options, final RandomAccessInputStream stream,
			final ByteArrayPlane plane, final int planeIndex) throws IOException,
			FormatException
		{
			final int bmpCompression = meta.getBmpCompression();

			byte[] buf = null;

			if (bmpCompression == MSRLE) {
				final byte[] b =
					new byte[(int) meta.getLengths().get(planeIndex).longValue()];
				stream.read(b);
				final MSRLECodec codec = new MSRLECodec();
				codec.setContext(meta.getContext());
				buf = codec.decompress(b, options);
				plane.setData(buf);
				meta.setLastPlane(plane);
				meta.setLastPlaneIndex(planeIndex);
			}
			else if (bmpCompression == MS_VIDEO) {
				final MSVideoCodec codec = new MSVideoCodec();
				buf = codec.decompress(stream, options);
				plane.setData(buf);
				meta.setLastPlane(plane);
				meta.setLastPlaneIndex(planeIndex);
			}
			else if (bmpCompression == JPEG) {
				final JPEGCodec codec = new JPEGCodec();

				byte[] tmpPlane =
					new byte[(int) meta.getLengths().get(planeIndex).longValue()];
				stream.read(tmpPlane);

				final boolean motionJPEG =
					new String(tmpPlane, 6, 4, Constants.ENCODING).equals("AVI1");

				if (motionJPEG) {
					// this is Motion JPEG data
					// we must manually insert the Huffman table, as Motion JPEG
					// uses a fixed (but not stored) Huffman table for all planes

					final byte[] fixedPlane =
						new byte[tmpPlane.length + MJPEG_HUFFMAN_TABLE.length];
					System.arraycopy(plane, 0, fixedPlane, 0, 20);
					System.arraycopy(MJPEG_HUFFMAN_TABLE, 0, fixedPlane, 20,
						MJPEG_HUFFMAN_TABLE.length);
					System.arraycopy(plane, 20, fixedPlane,
						20 + MJPEG_HUFFMAN_TABLE.length, tmpPlane.length - 20);

					tmpPlane = fixedPlane;
				}

				buf = codec.decompress(tmpPlane, options);

				if (motionJPEG) {
					// transform YCbCr data to RGB
					// see http://en.wikipedia.org/wiki/YCbCr#JPEG_conversion

					buf = plane.getBytes();

					for (int i = 0; i < buf.length; i += 3) {
						final int y = buf[i] & 0xff;
						final int cb = (buf[i + 1] & 0xff) - 128;
						final int cr = (buf[i + 2] & 0xff) - 128;

						int red = (int) (y + 1.402 * cr);
						int green = (int) (y - 0.34414 * cb - 0.71414 * cr);
						int blue = (int) (y + 1.772 * cb);

						if (red < 0) {
							red = 0;
						}
						else if (red > 255) {
							red = 255;
						}
						if (green < 0) {
							green = 0;
						}
						else if (green > 255) {
							green = 255;
						}
						if (blue < 0) {
							blue = 0;
						}
						else if (blue > 255) {
							blue = 255;
						}

						buf[i] = (byte) (red & 0xff);
						buf[i + 1] = (byte) (green & 0xff);
						buf[i + 2] = (byte) (blue & 0xff);
					}
				}
			}
			/*
			else if (bmpCompression == CINEPAK) {
			  Object[] options = new Object[2];
			  options[0] = new Integer(bmpBitsPerPixel);
			  options[1] = lastImage;

			  CinepakCodec codec = new CinepakCodec();
			  buf = codec.decompress(b, options);
			  lastImage = buf;
			  if (no == m.imageCount - 1) lastImage = null;
			  return buf;
			}
			*/
			else {
				throw new UnsupportedCompressionException(bmpCompression +
					" not supported");
			}

			return buf;
		}
	}
}
