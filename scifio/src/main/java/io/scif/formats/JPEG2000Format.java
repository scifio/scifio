/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
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
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.codec.CompressionType;
import io.scif.codec.JPEG2000BoxType;
import io.scif.codec.JPEG2000Codec;
import io.scif.codec.JPEG2000CodecOptions;
import io.scif.codec.JPEG2000SegmentMarker;
import io.scif.common.DataTools;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * JPEG2000Reader is the file format reader for JPEG-2000 images.
 */
@Plugin(type = Format.class)
public class JPEG2000Format extends AbstractFormat {

	// -- Format API methods --

	@Override
	public String getFormatName() {
		return "JPEG-2000";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "jp2", "j2k", "jpf" };
	}

	// -- Nested Classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		private long pixelsOffset;

		private Index lastIndex = new Index();
		private byte[] lastIndexBytes;

		/** The number of JPEG 2000 resolution levels the file has. */
		private Integer resolutionLevels;

		/** The color lookup table associated with this file. */
		private int[][] lut;
		byte[][] byteLut;
		short[][] shortLut;

		// -- JPEG2000Metadata getters and setters --

		public long getPixelsOffset() {
			return pixelsOffset;
		}

		public void setPixelsOffset(final long pixelsOffset) {
			this.pixelsOffset = pixelsOffset;
		}

		public Index getLastIndex() {
			if (lastIndex == null) lastIndex = new Index();
			return lastIndex;
		}

		public void setLastIndex(final int imageIndex, final long planeIndex) {
			if (lastIndex == null) lastIndex = new Index(imageIndex, planeIndex);
			else {
				lastIndex.setImageIndex(imageIndex);
				lastIndex.setPlaneIndex(imageIndex);
			}
		}

		public byte[] getLastIndexBytes() {
			return lastIndexBytes;
		}

		public void setLastIndexBytes(final byte[] lastIndexBytes) {
			this.lastIndexBytes = lastIndexBytes;
		}

		public Integer getResolutionLevels() {
			return resolutionLevels;
		}

		public void setResolutionLevels(final Integer resolutionLevels) {
			this.resolutionLevels = resolutionLevels;
		}

		public int[][] getLut() {
			return lut;
		}

		public void setLut(final int[][] lut) {
			this.lut = lut;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);
			iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
			iMeta.setIndexed(!(iMeta.getAxisLength(Axes.CHANNEL) > 1) &&
				getLut() != null);
			iMeta.setPlanarAxisCount(3);

			// New core metadata now that we know how many sub-resolutions we have.
			if (getResolutionLevels() != null) {
				final int imageCount = resolutionLevels + 1;
				// TODO set resolution count get(0).resolutionCount = imageCount;

				for (int i = 1; i < imageCount; i++) {
					final ImageMetadata ms = new DefaultImageMetadata(iMeta);
					add(ms);
					ms.setAxisLength(Axes.X, iMeta.getAxisLength(Axes.X) / 2);
					ms.setAxisLength(Axes.Y, iMeta.getAxisLength(Axes.Y) / 2);
					ms.setThumbnail(true);
				}
			}
		}

		@Override
		public int getImageCount() {
			return 1;
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				resolutionLevels = null;
				lut = null;
				byteLut = null;
				shortLut = null;
				pixelsOffset = 0;
				lastIndex = null;
				lastIndexBytes = null;
			}
		}

		// -- HasColorTable API Methods --

		@Override
		public ColorTable
			getColorTable(final int imageIndex, final long planeIndex)
		{
			if (lut == null) return null;

			if (FormatTools.getBytesPerPixel(get(0).getPixelType()) == 1) {
				if (byteLut == null) {
					byteLut = new byte[lut.length][lut[0].length];
					for (int i = 0; i < lut.length; i++) {
						for (int j = 0; j < lut[i].length; j++) {
							byteLut[i][j] = (byte) (lut[i][j] & 0xff);
						}
					}
				}
				return new ColorTable8(byteLut);
			}
			else if (FormatTools.getBytesPerPixel(get(0).getPixelType()) == 1) {
				if (shortLut == null) {
					shortLut = new short[lut.length][lut[0].length];
					for (int i = 0; i < lut.length; i++) {
						for (int j = 0; j < lut[i].length; j++) {
							shortLut[i][j] = (short) (lut[i][j] & 0xffff);
						}
					}
				}

				return new ColorTable16(shortLut);
			}

			return null;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constructor --

		public Checker() {
			suffixSufficient = false;
			suffixNecessary = false;
		}

		// -- Checker API methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 40;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			boolean validStart = (stream.readShort() & 0xffff) == 0xff4f;
			if (!validStart) {
				stream.skipBytes(2);
				validStart = stream.readInt() == JPEG2000BoxType.SIGNATURE.getCode();

				if (validStart) {
					stream.skipBytes(12);
					validStart = !stream.readString(4).equals("jpx ");
				}
			}
			stream.seek(stream.length() - 2);
			final boolean validEnd = (stream.readShort() & 0xffff) == 0xffd9;
			return validStart && validEnd;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Fields --

		/** Offset to first contiguous codestream. */
		private long codestreamOffset;

		/** Maximum read offset within in the stream. */
		private long maximumReadOffset;

		/** Width of the image as specified in the header. */
		private Integer headerSizeX;

		/** Height of the image as specified in the header. */
		private Integer headerSizeY;

		/** Number of channels the image has as specified in the header. */
		private Short headerSizeC;

		/** Pixel type as specified in the header. */
		private Integer headerPixelType;

		/** Width of the image as specified in the JPEG 2000 codestream. */
		private Integer codestreamSizeX;

		/** Height of the image as specified in the JPEG 2000 codestream. */
		private Integer codestreamSizeY;

		/** Number of channels the image as specified in the JPEG 2000 codestream. */
		private Short codestreamSizeC;

		/** Pixel type as specified in the JPEG 2000 codestream.. */
		private Integer codestreamPixelType;

		/** Whether or not the codestream is raw and not JP2 boxed. */
		private boolean isRawCodestream = false;

		/** List of comments stored in the file. */
		private ArrayList<String> comments;

		// -- JPEG2000Parse methods --

		public void parse(final RandomAccessInputStream stream,
			final Metadata meta, final long maximumReadOffset) throws IOException
		{

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			int sizeX, sizeY, sizeC, pixelType;

			in = stream;
			this.maximumReadOffset = maximumReadOffset;
			comments = new ArrayList<String>();
			final boolean isLittleEndian = stream.isLittleEndian();
			try {
				// Parse boxes may need to change the endianness of the input stream so
				// we're going to reset it when we're done.
				parseBoxes(meta);
			}
			finally {
				in.order(isLittleEndian);
			}

			if (isRawCodestream()) {
				log().info("Codestream is raw, using codestream dimensions.");
				sizeX = getCodestreamSizeX();
				sizeY = getCodestreamSizeY();
				sizeC = getCodestreamSizeC();
				pixelType = getCodestreamPixelType();
			}
			else {
				log().info("Codestream is JP2 boxed, using header dimensions.");
				sizeX = getHeaderSizeX();
				sizeY = getHeaderSizeY();
				sizeC = getHeaderSizeC();
				pixelType = getHeaderPixelType();
			}
			iMeta.setAxisLength(Axes.X, sizeX);
			iMeta.setAxisLength(Axes.Y, sizeY);
			iMeta.setAxisLength(Axes.CHANNEL, sizeC);
			iMeta.setPixelType(pixelType);

			meta.setPixelsOffset(getCodestreamOffset());

			iMeta.setLittleEndian(false);

			final ArrayList<String> comments = getComments();
			for (int i = 0; i < comments.size(); i++) {
				final String comment = comments.get(i);
				final int equal = comment.indexOf("=");
				if (equal >= 0) {
					final String key = comment.substring(0, equal);
					final String value = comment.substring(equal + 1);

					addGlobalMeta(key, value);
				}
				else {
					meta.getTable().put("Comment", comment);
				}
			}
		}

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			parse(stream, meta, stream.length());
		}

		/** Retrieves the offset to the first contiguous codestream. */
		public long getCodestreamOffset() {
			return codestreamOffset;
		}

		/** Retrieves the list of comments stored in the file. */
		public ArrayList<String> getComments() {
			return comments;
		}

		/**
		 * Parses the JPEG 2000 JP2 metadata boxes.
		 * 
		 * @throws IOException Thrown if there is an error reading from the file.
		 */
		private void parseBoxes(final Metadata meta) throws IOException {
			final long originalPos = in.getFilePointer();
			long nextPos = 0;
			long pos = originalPos;
			log().trace("Parsing JPEG 2000 boxes at " + pos);
			int length = 0, boxCode;
			JPEG2000BoxType boxType;

			while (pos < maximumReadOffset) {
				pos = in.getFilePointer();
				length = in.readInt();
				boxCode = in.readInt();
				boxType = JPEG2000BoxType.get(boxCode);
				if (boxType == JPEG2000BoxType.SIGNATURE_WRONG_ENDIANNESS) {
					log().trace("Swapping endianness during box parsing.");
					in.order(!in.isLittleEndian());
					length = DataTools.swap(length);
				}
				nextPos = pos + length;
				if (length >= 8) {
					length -= 8;
				}
				if (boxType == null) {
					log().warn(
						"Unknown JPEG 2000 box 0x" + Integer.toHexString(boxCode) + " at " +
							pos);
					if (pos == originalPos) {
						in.seek(originalPos);
						if (JPEG2000SegmentMarker.get(in.readUnsignedShort()) != null) {
							log().info("File is a raw codestream not a JP2.");
							isRawCodestream = true;
							in.seek(originalPos);
							parseContiguousCodestream(meta, in.length());
						}
					}
				}
				else {
					log().trace(
						"Found JPEG 2000 '" + boxType.getName() + "' box at " + pos);
					switch (boxType) {
						case CONTIGUOUS_CODESTREAM: {
							try {
								parseContiguousCodestream(meta, length == 0 ? in.length()
									: length);
							}
							catch (final Exception e) {
								log().warn("Could not parse contiguous codestream.", e);
							}
							break;
						}
						case HEADER: {
							in.skipBytes(4);
							final String s = in.readString(4);
							if (s.equals("ihdr")) {
								headerSizeY = in.readInt();
								headerSizeX = in.readInt();
								headerSizeC = in.readShort();
								final int type = in.read();
								in.skipBytes(3);
								headerPixelType = convertPixelType(type);
							}
							parseBoxes(meta);
							break;
						}
						case PALETTE:
							final int nEntries = in.readShort();
							final int nColumns = in.read();
							final int[] bitDepths = new int[nColumns];
							for (int i = 0; i < bitDepths.length; i++) {
								bitDepths[i] = in.read() & 0x7f;
								while ((bitDepths[i] % 8) != 0) {
									bitDepths[i]++;
								}
							}
							final int[][] lut = new int[nColumns][nEntries];

							for (int i = 0; i < nColumns; i++) {
								for (int j = 0; j < lut[i].length; j++) {
									if (bitDepths[i] == 8) {
										lut[i][j] = in.read();
									}
									else if (bitDepths[i] == 16) {
										lut[i][j] = in.readShort();
									}
								}
							}

							meta.setLut(lut);

							break;
						default:
							// No-op
							break;
					}
				}
				// Exit or seek to the next metadata box
				if (nextPos < 0 || nextPos >= maximumReadOffset || length == 0) {
					log().trace("Exiting box parser loop.");
					break;
				}
				log().trace("Seeking to next box at " + nextPos);
				in.seek(nextPos);
			}
		}

		/**
		 * Parses the JPEG 2000 codestream metadata.
		 * 
		 * @param length Total length of the codestream block.
		 * @throws IOException Thrown if there is an error reading from the file.
		 */
		private void parseContiguousCodestream(final Metadata meta,
			final long length) throws IOException
		{
			if (codestreamOffset == 0) {
				codestreamOffset = in.getFilePointer();
			}

			JPEG2000SegmentMarker segmentMarker;
			int segmentMarkerCode = 0, segmentLength = 0;
			long pos = in.getFilePointer(), nextPos = 0;
			log().trace(
				"Parsing JPEG 2000 contiguous codestream of length " + length + " at " +
					pos);
			final long maximumReadOffset = pos + length;
			boolean terminate = false;
			while (pos < maximumReadOffset && !terminate) {
				pos = in.getFilePointer();
				segmentMarkerCode = in.readUnsignedShort();
				segmentMarker = JPEG2000SegmentMarker.get(segmentMarkerCode);
				if (segmentMarker == JPEG2000SegmentMarker.SOC_WRONG_ENDIANNESS) {
					log().trace("Swapping endianness during segment marker parsing.");
					in.order(!in.isLittleEndian());
					segmentMarkerCode = JPEG2000SegmentMarker.SOC.getCode();
					segmentMarker = JPEG2000SegmentMarker.SOC;
				}
				if (segmentMarker == JPEG2000SegmentMarker.SOC ||
					segmentMarker == JPEG2000SegmentMarker.SOD ||
					segmentMarker == JPEG2000SegmentMarker.EPH ||
					segmentMarker == JPEG2000SegmentMarker.EOC ||
					(segmentMarkerCode >= JPEG2000SegmentMarker.RESERVED_DELIMITER_MARKER_MIN
						.getCode() && segmentMarkerCode <= JPEG2000SegmentMarker.RESERVED_DELIMITER_MARKER_MAX
						.getCode()))
				{
					// Delimiter marker; no segment.
					segmentLength = 0;
				}
				else {
					segmentLength = in.readUnsignedShort();
				}
				nextPos = pos + segmentLength + 2;
				if (segmentMarker == null) {
					log().warn(
						"Unknown JPEG 2000 segment marker 0x" +
							Integer.toHexString(segmentMarkerCode) + " at " + pos);
				}
				else {
					if (log().isTrace()) {
						log().trace(
							String.format(
								"Found JPEG 2000 segment marker '%s' of length %d at %d",
								segmentMarker.getName(), segmentLength, pos));
					}
					switch (segmentMarker) {
						case SOT:
						case SOD:
						case EOC:
							terminate = true;
							break;
						case SIZ: {
							// Skipping:
							// * Capability (uint16)
							in.skipBytes(2);
							codestreamSizeX = in.readInt();
							log().trace(
								"Read reference grid width " + codestreamSizeX + " at " +
									in.getFilePointer());
							codestreamSizeY = in.readInt();
							log().trace(
								"Read reference grid height " + codestreamSizeY + " at " +
									in.getFilePointer());
							// Skipping:
							// * Horizontal image offset (uint32)
							// * Vertical image offset (uint32)
							// * Tile width (uint32)
							// * Tile height (uint32)
							// * Horizontal tile offset (uint32)
							// * Vertical tile offset (uint32)
							in.skipBytes(24);
							codestreamSizeC = in.readShort();
							log().trace(
								"Read total components " + codestreamSizeC + " at " +
									in.getFilePointer());
							final int type = in.read();
							in.skipBytes(3);
							codestreamPixelType = convertPixelType(type);
							log().trace(
								"Read codestream pixel type " + codestreamPixelType + " at " +
									in.getFilePointer());
							break;
						}
						case COD: {
							// Skipping:
							// * Segment coding style (uint8)
							// * Progression order (uint8)
							// * Total quality layers (uint16)
							// * Multiple component transform (uint8)
							in.skipBytes(5);
							meta.setResolutionLevels(in.readUnsignedByte());
							log().trace(
								"Found number of resolution levels " +
									meta.getResolutionLevels() + " at " + in.getFilePointer());
							break;
						}
						case COM:
							in.skipBytes(2);
							final String comment = in.readString(segmentLength - 4);
							comments.add(comment);
							break;
						default:
							// No-op
							break;
					}
				}
				// Exit or seek to the next metadata box
				if (nextPos < 0 || nextPos >= maximumReadOffset || terminate) {
					log().trace("Exiting segment marker parse loop.");
					break;
				}
				log().trace("Seeking to next segment marker at " + nextPos);
				in.seek(nextPos);
			}
		}

		/**
		 * Whether or not the codestream is raw and not JP2 boxed.
		 * 
		 * @return <code>true</code> if the codestream is raw and <code>false</code>
		 *         otherwise.
		 */
		public boolean isRawCodestream() {
			return isRawCodestream;
		}

		/**
		 * Returns the width of the image as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getHeaderSizeX() {
			return headerSizeX;
		}

		/**
		 * Returns the height of the image as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getHeaderSizeY() {
			return headerSizeY;
		}

		/**
		 * Returns the number of channels the image has as specified in the header.
		 * 
		 * @return See above.
		 */
		public Short getHeaderSizeC() {
			return headerSizeC;
		}

		/**
		 * Returns the pixel type as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getHeaderPixelType() {
			return headerPixelType;
		}

		/**
		 * Returns the width of the image as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getCodestreamSizeX() {
			return codestreamSizeX;
		}

		/**
		 * Returns the height of the image as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getCodestreamSizeY() {
			return codestreamSizeY;
		}

		/**
		 * Returns the number of channels the image has as specified in the header.
		 * 
		 * @return See above.
		 */
		public Short getCodestreamSizeC() {
			return codestreamSizeC;
		}

		/**
		 * Returns the pixel type as specified in the header.
		 * 
		 * @return See above.
		 */
		public Integer getCodestreamPixelType() {
			return codestreamPixelType;
		}

		private int convertPixelType(final int type) {
			final int bits = (type & 0x7f) + 1;
			final boolean isSigned = ((type & 0x80) >> 7) == 1;

			if (bits <= 8) {
				return isSigned ? FormatTools.INT8 : FormatTools.UINT8;
			}
			else if (bits <= 16) {
				return isSigned ? FormatTools.INT16 : FormatTools.UINT16;
			}
			else if (bits <= 32) {
				return isSigned ? FormatTools.INT32 : FormatTools.UINT32;
			}
			return FormatTools.UINT8;
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

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex,
			final long planeIndex, final ByteArrayPlane plane, final long[] planeMin,
			final long[] planeMax) throws FormatException, IOException
		{
			final byte[] buf = plane.getBytes();
			final Metadata meta = getMetadata();

			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);

			if (meta.getLastIndex().getImageIndex() == imageIndex &&
				meta.getLastIndex().getPlaneIndex() == planeIndex &&
				meta.getLastIndexBytes() != null)
			{
				final RandomAccessInputStream s =
					new RandomAccessInputStream(getContext(), meta.getLastIndexBytes());
				readPlane(s, imageIndex, planeMin, planeMax, plane);
				s.close();
				return plane;
			}

			final JPEG2000CodecOptions options =
				JPEG2000CodecOptions.getDefaultOptions();
			options.interleaved = meta.get(imageIndex).getInterleavedAxisCount() > 0;
			options.littleEndian = meta.get(imageIndex).isLittleEndian();
			if (meta.getResolutionLevels() != null) {
				options.resolution = Math.abs(imageIndex - meta.getResolutionLevels());
			}
			else if (meta.getAll().size() > 1) {
				options.resolution = imageIndex;
			}

			getStream().seek(meta.getPixelsOffset());
			final JPEG2000Codec codec = new JPEG2000Codec();
			codec.setContext(getContext());
			final byte[] lastIndexPlane = codec.decompress(getStream(), options);
			meta.setLastIndexBytes(lastIndexPlane);
			final RandomAccessInputStream s =
				new RandomAccessInputStream(getContext(), lastIndexPlane);
			readPlane(s, imageIndex, planeMin, planeMax, plane);
			s.close();
			meta.setLastIndex(imageIndex, planeIndex);
			return plane;
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constructor --

		public Writer() {
			compressionTypes =
				new String[] { CompressionType.J2K_LOSSY.getCompression(),
					CompressionType.J2K.getCompression() };
			// The default codec options
			options = JPEG2000CodecOptions.getDefaultOptions();
		}

		// -- Writer API Methods --

		@Override
		public void savePlane(final int imageIndex, final long planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final byte[] buf = plane.getBytes();
			checkParams(imageIndex, planeIndex, buf, planeMin, planeMax);

			/*
			if (!isFullPlane(x, y, w, h)) {
			  throw new FormatException(
			    "JPEG2000Writer does not yet support saving image tiles.");
			}
			*/
			// MetadataRetrieve retrieve = getMetadataRetrieve();
			// int width = retrieve.getPixelsSizeX(series).getValue().intValue();
			// int height = retrieve.getPixelsSizeY(series).getValue().intValue();

			out
				.write(compressBuffer(imageIndex, planeIndex, buf, planeMin, planeMax));
		}

		/**
		 * Compresses the buffer.
		 * 
		 * @param imageIndex the image index within the dataset
		 * @param planeIndex the plane index within the image
		 * @param buf the byte array that represents the image tile.
		 * @param planeMin minimal bounds of the planar axes
		 * @param planeMax maximum bounds of the planar axes
		 * @throws FormatException if one of the parameters is invalid.
		 * @throws IOException if there was a problem writing to the file.
		 */
		public byte[] compressBuffer(final int imageIndex, final long planeIndex,
			final byte[] buf, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			checkParams(imageIndex, planeIndex, buf, planeMin, planeMax);
			final boolean littleEndian =
				getMetadata().get(imageIndex).isLittleEndian();
			final int bytesPerPixel =
				getMetadata().get(imageIndex).getBitsPerPixel() / 8;
			final int nChannels =
				(int) getMetadata().get(imageIndex).getAxisLength(Axes.CHANNEL);

			// To be on the save-side
			if (options == null) options = JPEG2000CodecOptions.getDefaultOptions();
			options = new JPEG2000CodecOptions(options);
			options.width = (int) planeMax[0];
			options.height = (int) planeMax[1];
			options.channels = nChannels;
			options.bitsPerSample = bytesPerPixel * 8;
			options.littleEndian = littleEndian;
			options.interleaved =
				getMetadata().get(imageIndex).getInterleavedAxisCount() > 0;
			options.lossless =
				compression == null ||
					compression.equals(CompressionType.J2K.getCompression());
			options.colorModel = getColorModel();

			final JPEG2000Codec codec = new JPEG2000Codec();
			codec.setContext(getContext());
			return codec.compress(buf, options);
		}

		/**
		 * Overridden to indicate that stacks are not supported.
		 */
		@Override
		public boolean canDoStacks() {
			return false;
		}

		/**
		 * Overridden to return the formats supported by the writer.
		 */
		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
				FormatTools.UINT32 };
		}
	}

	// -- Helper class --

	public static class Index {

		private int imageIndex;
		private long planeIndex;

		public Index() {
			this(-1, -1);
		}

		public Index(final int image, final long plane) {
			imageIndex = image;
			planeIndex = plane;
		}

		public void setImageIndex(final int image) {
			imageIndex = image;
		}

		public void setPlaneIndex(final long plane) {
			planeIndex = plane;
		}

		public int getImageIndex() {
			return imageIndex;
		}

		public long getPlaneIndex() {
			return planeIndex;
		}
	}
}
