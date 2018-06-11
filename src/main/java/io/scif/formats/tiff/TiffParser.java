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

package io.scif.formats.tiff;

import io.scif.FormatException;
import io.scif.SCIFIO;
import io.scif.codec.BitBuffer;
import io.scif.codec.CodecOptions;
import io.scif.common.Constants;
import io.scif.enumeration.EnumException;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.util.Bytes;
import org.scijava.util.IntRect;

/**
 * Parses TIFF data from an input source.
 *
 * @author Curtis Rueden
 * @author Eric Kjellman
 * @author Melissa Linkert
 * @author Chris Allan
 */
public class TiffParser extends AbstractContextual {

	// -- Fields --

	/** Input source from which to parse TIFF data. */
	private final RandomAccessInputStream in;

	/** Cached tile buffer to avoid re-allocations when reading tiles. */
	private byte[] cachedTileBuffer;

	/** Whether or not the TIFF file contains BigTIFF data. */
	private boolean bigTiff;

	/** Whether or not 64-bit offsets are used for non-BigTIFF files. */
	private boolean fakeBigTiff = false;

	private boolean ycbcrCorrection = true;

	private boolean equalStrips = false;

	private boolean doCaching;

	/** Cached list of IFDs in the current file. */
	private IFDList ifdList;

	/** Cached first IFD in the current file. */
	private IFD firstIFD;

	private final SCIFIO scifio;

	private final LogService log;

	/** Codec options to be used when decoding compressed pixel data. */
	private CodecOptions codecOptions = CodecOptions.getDefaultOptions();

	// -- Constructors --

	/** Constructs a new TIFF parser from the given file name. */
	public TiffParser(final Context context, final String filename)
		throws IOException
	{
		this(context, new RandomAccessInputStream(context, filename));
	}

	/** Constructs a new TIFF parser from the given input source. */
	public TiffParser(final Context context, final RandomAccessInputStream in) {
		setContext(context);
		scifio = new SCIFIO(context);
		log = scifio.log();
		this.in = in;
		doCaching = true;
		try {
			final long fp = in.getFilePointer();
			checkHeader();
			in.seek(fp);
		}
		catch (final IOException e) {}
	}

	// -- TiffParser methods --

	/**
	 * Sets whether or not to assume that strips are of equal size.
	 *
	 * @param equalStrips Whether or not the strips are of equal size.
	 */
	public void setAssumeEqualStrips(final boolean equalStrips) {
		this.equalStrips = equalStrips;
	}

	/**
	 * Sets the codec options to be used when decompressing pixel data.
	 *
	 * @param codecOptions Codec options to use.
	 */
	public void setCodecOptions(final CodecOptions codecOptions) {
		this.codecOptions = codecOptions;
	}

	/**
	 * Retrieves the current set of codec options being used to decompress pixel
	 * data.
	 *
	 * @return See above.
	 */
	public CodecOptions getCodecOptions() {
		return codecOptions;
	}

	/** Sets whether or not IFD entries should be cached. */
	public void setDoCaching(final boolean doCaching) {
		this.doCaching = doCaching;
	}

	/** Sets whether or not 64-bit offsets are used for non-BigTIFF files. */
	public void setUse64BitOffsets(final boolean use64Bit) {
		fakeBigTiff = use64Bit;
	}

	/** Sets whether or not YCbCr color correction is allowed. */
	public void setYCbCrCorrection(final boolean correctionAllowed) {
		ycbcrCorrection = correctionAllowed;
	}

	/** Gets the stream from which TIFF data is being parsed. */
	public RandomAccessInputStream getStream() {
		return in;
	}

	/** Tests this stream to see if it represents a TIFF file. */
	public boolean isValidHeader() {
		try {
			return checkHeader() != null;
		}
		catch (final IOException e) {
			return false;
		}
	}

	/**
	 * Checks the TIFF header.
	 *
	 * @return true if little-endian, false if big-endian, or null if not a TIFF.
	 */
	public Boolean checkHeader() throws IOException {
		if (in.length() < 4) return null;

		// byte order must be II or MM
		in.seek(0);
		final int endianOne = in.read();
		final int endianTwo = in.read();
		final boolean littleEndian = endianOne == TiffConstants.LITTLE &&
			endianTwo == TiffConstants.LITTLE; // II
		final boolean bigEndian = endianOne == TiffConstants.BIG &&
			endianTwo == TiffConstants.BIG; // MM
		if (!littleEndian && !bigEndian) return null;

		// check magic number (42)
		in.order(littleEndian);
		final short magic = in.readShort();
		bigTiff = magic == TiffConstants.BIG_TIFF_MAGIC_NUMBER;
		if (magic != TiffConstants.MAGIC_NUMBER &&
			magic != TiffConstants.BIG_TIFF_MAGIC_NUMBER)
		{
			return null;
		}

		return Boolean.valueOf(littleEndian);
	}

	/** Returns whether or not the current TIFF file contains BigTIFF data. */
	public boolean isBigTiff() {
		return bigTiff;
	}

	// -- TiffParser methods - IFD parsing --

	/** Returns all IFDs in the file. */
	public IFDList getIFDs() throws IOException {
		if (ifdList != null) return ifdList;

		final long[] offsets = getIFDOffsets();
		final IFDList ifds = new IFDList();

		for (final long offset : offsets) {
			final IFD ifd = getIFD(offset);
			if (ifd == null) continue;
			if (ifd.containsKey(IFD.IMAGE_WIDTH)) ifds.add(ifd);
			long[] subOffsets = null;
			try {
				if (!doCaching && ifd.containsKey(IFD.SUB_IFD)) {
					fillInIFD(ifd);
				}
				subOffsets = ifd.getIFDLongArray(IFD.SUB_IFD);
			}
			catch (final FormatException e) {}
			if (subOffsets != null) {
				for (final long subOffset : subOffsets) {
					final IFD sub = getIFD(subOffset);
					if (sub != null) {
						ifds.add(sub);
					}
				}
			}
		}
		if (doCaching) ifdList = ifds;

		return ifds;
	}

	/** Returns thumbnail IFDs. */
	public IFDList getThumbnailIFDs() throws IOException {
		final IFDList ifds = getIFDs();
		final IFDList thumbnails = new IFDList();
		for (final IFD ifd : ifds) {
			final Number subfile = (Number) ifd.getIFDValue(IFD.NEW_SUBFILE_TYPE);
			final int subfileType = subfile == null ? 0 : subfile.intValue();
			if (subfileType == 1) {
				thumbnails.add(ifd);
			}
		}
		return thumbnails;
	}

	/** Returns non-thumbnail IFDs. */
	public IFDList getNonThumbnailIFDs() throws IOException {
		final IFDList ifds = getIFDs();
		final IFDList nonThumbs = new IFDList();
		for (final IFD ifd : ifds) {
			final Number subfile = (Number) ifd.getIFDValue(IFD.NEW_SUBFILE_TYPE);
			final int subfileType = subfile == null ? 0 : subfile.intValue();
			if (subfileType != 1 || ifds.size() <= 1) {
				nonThumbs.add(ifd);
			}
		}
		return nonThumbs;
	}

	/** Returns EXIF IFDs. */
	public IFDList getExifIFDs() throws FormatException, IOException {
		final IFDList ifds = getIFDs();
		final IFDList exif = new IFDList();
		for (final IFD ifd : ifds) {
			final long offset = ifd.getIFDLongValue(IFD.EXIF, 0);
			if (offset != 0) {
				final IFD exifIFD = getIFD(offset);
				if (exifIFD != null) {
					exif.add(exifIFD);
				}
			}
		}
		return exif;
	}

	/** Gets the offsets to every IFD in the file. */
	public long[] getIFDOffsets() throws IOException {
		// check TIFF header
		final int bytesPerEntry = bigTiff ? TiffConstants.BIG_TIFF_BYTES_PER_ENTRY
			: TiffConstants.BYTES_PER_ENTRY;

		final Vector<Long> offsets = new Vector<>();
		long offset = getFirstOffset();
		while (offset > 0 && offset < in.length()) {
			in.seek(offset);
			offsets.add(offset);
			final int nEntries = bigTiff ? (int) in.readLong() : in
				.readUnsignedShort();
			in.skipBytes(nEntries * bytesPerEntry);
			offset = getNextOffset(offset);
		}

		final long[] f = new long[offsets.size()];
		for (int i = 0; i < f.length; i++) {
			f[i] = offsets.get(i).longValue();
		}

		return f;
	}

	/**
	 * Gets the first IFD within the TIFF file, or null if the input source is not
	 * a valid TIFF file.
	 */
	public IFD getFirstIFD() throws IOException {
		if (firstIFD != null) return firstIFD;
		final long offset = getFirstOffset();
		final IFD ifd = getIFD(offset);
		if (doCaching) firstIFD = ifd;
		return ifd;
	}

	/**
	 * Retrieve a given entry from the first IFD in the stream.
	 *
	 * @param tag the tag of the entry to be retrieved.
	 * @return an object representing the entry's fields.
	 * @throws IOException when there is an error accessing the stream.
	 * @throws IllegalArgumentException when the tag number is unknown.
	 */
	// TODO : Try to remove this method. It is only being used by
	// loci.formats.in.MetamorphReader.
	public TiffIFDEntry getFirstIFDEntry(final int tag) throws IOException {
		// Get the offset of the first IFD
		final long offset = getFirstOffset();
		if (offset < 0) return null;

		// The following loosely resembles the logic of getIFD()...
		in.seek(offset);
		final long numEntries = bigTiff ? in.readLong() : in.readUnsignedShort();

		for (int i = 0; i < numEntries; i++) {
			in.seek(offset + // The beginning of the IFD
				(bigTiff ? 8 : 2) + // The width of the initial numEntries field
				(bigTiff ? TiffConstants.BIG_TIFF_BYTES_PER_ENTRY
					: TiffConstants.BYTES_PER_ENTRY) * i);

			final TiffIFDEntry entry = readTiffIFDEntry();
			if (entry.getTag() == tag) {
				return entry;
			}
		}
		throw new IllegalArgumentException("Unknown tag: " + tag);
	}

	/**
	 * Gets offset to the first IFD, or -1 if stream is not TIFF.
	 */
	public long getFirstOffset() throws IOException {
		final Boolean header = checkHeader();
		if (header == null) return -1;
		if (bigTiff) in.skipBytes(4);
		return getNextOffset(0);
	}

	/** Gets the IFD stored at the given offset. */
	public IFD getIFD(final long offset) throws IOException {
		if (offset < 0 || offset >= in.length()) return null;
		final IFD ifd = new IFD(log);

		// save little-endian flag to internal LITTLE_ENDIAN tag
		ifd.put(new Integer(IFD.LITTLE_ENDIAN), Boolean.valueOf(in
			.isLittleEndian()));
		ifd.put(new Integer(IFD.BIG_TIFF), Boolean.valueOf(bigTiff));

		// read in directory entries for this IFD
		log.trace("getIFDs: seeking IFD at " + offset);
		in.seek(offset);
		final long numEntries = bigTiff ? in.readLong() : in.readUnsignedShort();
		log.trace("getIFDs: " + numEntries + " directory entries to read");
		if (numEntries == 0 || numEntries == 1) return ifd;

		final int bytesPerEntry = bigTiff ? TiffConstants.BIG_TIFF_BYTES_PER_ENTRY
			: TiffConstants.BYTES_PER_ENTRY;
		final int baseOffset = bigTiff ? 8 : 2;

		for (int i = 0; i < numEntries; i++) {
			in.seek(offset + baseOffset + bytesPerEntry * i);

			TiffIFDEntry entry = null;
			try {
				entry = readTiffIFDEntry();
			}
			catch (final EnumException e) {
				log.debug("", e);
			}
			if (entry == null) break;
			int count = entry.getValueCount();
			final int tag = entry.getTag();
			final long pointer = entry.getValueOffset();
			final int bpe = entry.getType().getBytesPerElement();

			if (count < 0 || bpe <= 0) {
				// invalid data
				in.skipBytes(bytesPerEntry - 4 - (bigTiff ? 8 : 4));
				continue;
			}
			Object value = null;

			final long inputLen = in.length();
			if (count * bpe + pointer > inputLen) {
				final int oldCount = count;
				count = (int) ((inputLen - pointer) / bpe);
				log.trace("getIFDs: truncated " + (oldCount - count) +
					" array elements for tag " + tag);
				if (count < 0) count = oldCount;
				entry = new TiffIFDEntry(entry.getTag(), entry.getType(), count, entry
					.getValueOffset());
			}
			if (count < 0 || count > in.length()) break;

			if (pointer != in.getFilePointer() && !doCaching) {
				value = entry;
			}
			else value = getIFDValue(entry);

			if (value != null && !ifd.containsKey(new Integer(tag))) {
				ifd.put(new Integer(tag), value);
			}
		}

		in.seek(offset + baseOffset + bytesPerEntry * numEntries);

		return ifd;
	}

	/** Fill in IFD entries that are stored at an arbitrary offset. */
	public void fillInIFD(final IFD ifd) throws IOException {
		final HashSet<TiffIFDEntry> entries = new HashSet<>();
		for (final Object key : ifd.keySet()) {
			if (ifd.get(key) instanceof TiffIFDEntry) {
				entries.add((TiffIFDEntry) ifd.get(key));
			}
		}

		for (final TiffIFDEntry entry : entries) {
			if (entry.getValueCount() < 10 * 1024 * 1024 || entry.getTag() < 32768) {
				ifd.put(new Integer(entry.getTag()), getIFDValue(entry));
			}
		}
	}

	/** Retrieve the value corresponding to the given TiffIFDEntry. */
	public Object getIFDValue(final TiffIFDEntry entry) throws IOException {
		final IFDType type = entry.getType();
		final int count = entry.getValueCount();
		final long offset = entry.getValueOffset();

		log.trace("Reading entry " + entry.getTag() + " from " + offset +
			"; type=" + type + ", count=" + count);

		if (offset >= in.length()) {
			return null;
		}

		if (offset != in.getFilePointer()) {
			in.seek(offset);
		}

		if (type == IFDType.BYTE) {
			// 8-bit unsigned integer
			if (count == 1) return new Short(in.readByte());
			final byte[] bytes = new byte[count];
			in.readFully(bytes);
			// bytes are unsigned, so use shorts
			final short[] shorts = new short[count];
			for (int j = 0; j < count; j++)
				shorts[j] = (short) (bytes[j] & 0xff);
			return shorts;
		}
		else if (type == IFDType.ASCII) {
			// 8-bit byte that contain a 7-bit ASCII code;
			// the last byte must be NUL (binary zero)
			final byte[] ascii = new byte[count];
			in.read(ascii);

			// count number of null terminators
			int nullCount = 0;
			for (int j = 0; j < count; j++) {
				if (ascii[j] == 0 || j == count - 1) nullCount++;
			}

			// convert character array to array of strings
			final String[] strings = nullCount == 1 ? null : new String[nullCount];
			String s = null;
			int c = 0, ndx = -1;
			for (int j = 0; j < count; j++) {
				if (ascii[j] == 0) {
					s = new String(ascii, ndx + 1, j - ndx - 1, Constants.ENCODING);
					ndx = j;
				}
				else if (j == count - 1) {
					// handle non-null-terminated strings
					s = new String(ascii, ndx + 1, j - ndx, Constants.ENCODING);
				}
				else s = null;
				if (strings != null && s != null) strings[c++] = s;
			}
			return strings == null ? (Object) s : strings;
		}
		else if (type == IFDType.SHORT) {
			// 16-bit (2-byte) unsigned integer
			if (count == 1) return new Integer(in.readUnsignedShort());
			final int[] shorts = new int[count];
			for (int j = 0; j < count; j++) {
				shorts[j] = in.readUnsignedShort();
			}
			return shorts;
		}
		else if (type == IFDType.LONG || type == IFDType.IFD) {
			// 32-bit (4-byte) unsigned integer
			if (count == 1) return new Long(in.readInt());
			final long[] longs = new long[count];
			for (int j = 0; j < count; j++) {
				if (in.getFilePointer() + 4 <= in.length()) {
					longs[j] = in.readInt();
				}
			}
			return longs;
		}
		else if (type == IFDType.LONG8 || type == IFDType.SLONG8 ||
			type == IFDType.IFD8)
		{
			if (count == 1) return new Long(in.readLong());
			long[] longs = null;

			if (equalStrips && (entry.getTag() == IFD.STRIP_BYTE_COUNTS || entry
				.getTag() == IFD.TILE_BYTE_COUNTS))
			{
				longs = new long[1];
				longs[0] = in.readLong();
			}
			else if (equalStrips && (entry.getTag() == IFD.STRIP_OFFSETS || entry
				.getTag() == IFD.TILE_OFFSETS))
			{
				final OnDemandLongArray offsets = new OnDemandLongArray(in);
				offsets.setSize(count);
				return offsets;
			}
			else {
				longs = new long[count];
				for (int j = 0; j < count; j++)
					longs[j] = in.readLong();
			}
			return longs;
		}
		else if (type == IFDType.RATIONAL || type == IFDType.SRATIONAL) {
			// Two LONGs or SLONGs: the first represents the numerator
			// of a fraction; the second, the denominator
			if (count == 1) return new TiffRational(in.readInt(), in.readInt());
			final TiffRational[] rationals = new TiffRational[count];
			for (int j = 0; j < count; j++) {
				rationals[j] = new TiffRational(in.readInt(), in.readInt());
			}
			return rationals;
		}
		else if (type == IFDType.SBYTE || type == IFDType.UNDEFINED) {
			// SBYTE: An 8-bit signed (twos-complement) integer
			// UNDEFINED: An 8-bit byte that may contain anything,
			// depending on the definition of the field
			if (count == 1) return new Byte(in.readByte());
			final byte[] sbytes = new byte[count];
			in.read(sbytes);
			return sbytes;
		}
		else if (type == IFDType.SSHORT) {
			// A 16-bit (2-byte) signed (twos-complement) integer
			if (count == 1) return new Short(in.readShort());
			final short[] sshorts = new short[count];
			for (int j = 0; j < count; j++)
				sshorts[j] = in.readShort();
			return sshorts;
		}
		else if (type == IFDType.SLONG) {
			// A 32-bit (4-byte) signed (twos-complement) integer
			if (count == 1) return new Integer(in.readInt());
			final int[] slongs = new int[count];
			for (int j = 0; j < count; j++)
				slongs[j] = in.readInt();
			return slongs;
		}
		else if (type == IFDType.FLOAT) {
			// Single precision (4-byte) IEEE format
			if (count == 1) return new Float(in.readFloat());
			final float[] floats = new float[count];
			for (int j = 0; j < count; j++)
				floats[j] = in.readFloat();
			return floats;
		}
		else if (type == IFDType.DOUBLE) {
			// Double precision (8-byte) IEEE format
			if (count == 1) return new Double(in.readDouble());
			final double[] doubles = new double[count];
			for (int j = 0; j < count; j++) {
				doubles[j] = in.readDouble();
			}
			return doubles;
		}

		return null;
	}

	/** Convenience method for obtaining a stream's first ImageDescription. */
	public String getComment() throws IOException {
		final IFD firstIFD = getFirstIFD();
		if (firstIFD == null) {
			return null;
		}
		fillInIFD(firstIFD);
		return firstIFD.getComment();
	}

	// -- TiffParser methods - image reading --

	public byte[] getTile(final IFD ifd, byte[] buf, final int row, final int col)
		throws FormatException, IOException
	{
		final byte[] jpegTable = (byte[]) ifd.getIFDValue(IFD.JPEG_TABLES);

		codecOptions.interleaved = true;
		codecOptions.littleEndian = ifd.isLittleEndian();

		final long tileWidth = ifd.getTileWidth();
		final long tileLength = ifd.getTileLength();
		final int samplesPerPixel = ifd.getSamplesPerPixel();
		final int planarConfig = ifd.getPlanarConfiguration();
		final TiffCompression compression = ifd.getCompression();

		final long numTileCols = ifd.getTilesPerRow();

		final int pixel = ifd.getBytesPerSample()[0];
		final int effectiveChannels = planarConfig == 2 ? 1 : samplesPerPixel;

		final long[] stripByteCounts = ifd.getStripByteCounts();
		final long[] rowsPerStrip = ifd.getRowsPerStrip();

		final int offsetIndex = (int) (row * numTileCols + col);
		int countIndex = offsetIndex;
		if (equalStrips) {
			countIndex = 0;
		}
		if (stripByteCounts[countIndex] == (rowsPerStrip[0] * tileWidth) &&
			pixel > 1)
		{
			stripByteCounts[countIndex] *= pixel;
		}

		long stripOffset = 0;
		long nStrips = 0;

		if (ifd.getOnDemandStripOffsets() != null) {
			final OnDemandLongArray stripOffsets = ifd.getOnDemandStripOffsets();
			stripOffset = stripOffsets.get(offsetIndex);
			nStrips = stripOffsets.size();
		}
		else {
			final long[] stripOffsets = ifd.getStripOffsets();
			stripOffset = stripOffsets[offsetIndex];
			nStrips = stripOffsets.length;
		}

		final int size = (int) (tileWidth * tileLength * pixel * effectiveChannels);

		if (buf == null) buf = new byte[size];
		if (stripByteCounts[countIndex] == 0 || stripOffset >= in.length()) {
			return buf;
		}
		byte[] tile = new byte[(int) stripByteCounts[countIndex]];

		log.debug("Reading tile Length " + tile.length + " Offset " + stripOffset);
		in.seek(stripOffset);
		in.read(tile);

		codecOptions.maxBytes = Math.max(size, tile.length);
		codecOptions.ycbcr = ifd
			.getPhotometricInterpretation() == PhotoInterp.Y_CB_CR && ifd
				.getIFDIntValue(IFD.Y_CB_CR_SUB_SAMPLING) == 1 && ycbcrCorrection;

		if (jpegTable != null) {
			final byte[] q = new byte[jpegTable.length + tile.length - 4];
			System.arraycopy(jpegTable, 0, q, 0, jpegTable.length - 2);
			System.arraycopy(tile, 2, q, jpegTable.length - 2, tile.length - 2);
			tile = compression.decompress(scifio.codec(), q, codecOptions);
		}
		else tile = compression.decompress(scifio.codec(), tile, codecOptions);
		scifio.tiff().undifference(tile, ifd);
		unpackBytes(buf, 0, tile, ifd);

		if (planarConfig == 2 && !ifd.isTiled() && ifd.getSamplesPerPixel() > 1) {
			final int channel = (int) (row % nStrips);
			if (channel < ifd.getBytesPerSample().length) {
				final int realBytes = ifd.getBytesPerSample()[channel];
				if (realBytes != pixel) {
					// re-pack pixels to account for differing bits per sample

					final boolean littleEndian = ifd.isLittleEndian();
					final int[] samples = new int[buf.length / pixel];
					for (int i = 0; i < samples.length; i++) {
						samples[i] = Bytes.toInt(buf, i * realBytes, realBytes,
							littleEndian);
					}

					for (int i = 0; i < samples.length; i++) {
						Bytes.unpack(samples[i], buf, i * pixel, pixel, littleEndian);
					}
				}
			}
		}

		return buf;
	}

	public byte[] getSamples(final IFD ifd, final byte[] buf)
		throws FormatException, IOException
	{
		final long width = ifd.getImageWidth();
		final long length = ifd.getImageLength();
		return getSamples(ifd, buf, 0, 0, width, length);
	}

	public byte[] getSamples(final IFD ifd, final byte[] buf, final int x,
		final int y, final long width, final long height) throws FormatException,
		IOException
	{
		return getSamples(ifd, buf, x, y, width, height, 0, 0);
	}

	public byte[] getSamples(final IFD ifd, final byte[] buf, final int x,
		final int y, final long width, final long height, final int overlapX,
		final int overlapY) throws FormatException, IOException
	{
		log.trace("parsing IFD entries");

		// get internal non-IFD entries
		final boolean littleEndian = ifd.isLittleEndian();
		in.order(littleEndian);

		// get relevant IFD entries
		final int samplesPerPixel = ifd.getSamplesPerPixel();
		final long tileWidth = ifd.getTileWidth();
		long tileLength = ifd.getTileLength();
		if (tileLength <= 0) {
			log.trace("Tile length is " + tileLength + "; setting it to " + height);
			tileLength = height;
		}

		long numTileRows = ifd.getTilesPerColumn();
		final long numTileCols = ifd.getTilesPerRow();

		final PhotoInterp photoInterp = ifd.getPhotometricInterpretation();
		final int planarConfig = ifd.getPlanarConfiguration();
		final int pixel = ifd.getBytesPerSample()[0];
		final int effectiveChannels = planarConfig == 2 ? 1 : samplesPerPixel;

		if (log.isTrace()) {
			ifd.printIFD();
		}

		if (width * height > Integer.MAX_VALUE) {
			throw new FormatException("Sorry, ImageWidth x ImageLength > " +
				Integer.MAX_VALUE + " is not supported (" + width + " x " + height +
				")");
		}
		if (width * height * effectiveChannels * pixel > Integer.MAX_VALUE) {
			throw new FormatException("Sorry, ImageWidth x ImageLength x " +
				"SamplesPerPixel x BitsPerSample > " + Integer.MAX_VALUE +
				" is not supported (" + width + " x " + height + " x " +
				samplesPerPixel + " x " + (pixel * 8) + ")");
		}

		// casting to int is safe because we have already determined that
		// width * height is less than Integer.MAX_VALUE
		final int numSamples = (int) (width * height);

		// read in image strips
		log.trace("reading image data (samplesPerPixel=" + samplesPerPixel +
			"; numSamples=" + numSamples + ")");

		final TiffCompression compression = ifd.getCompression();

		if (compression == TiffCompression.JPEG_2000 ||
			compression == TiffCompression.JPEG_2000_LOSSY)
		{
			codecOptions = compression.getCompressionCodecOptions(ifd, codecOptions);
		}
		else codecOptions = compression.getCompressionCodecOptions(ifd);
		codecOptions.interleaved = true;
		codecOptions.littleEndian = ifd.isLittleEndian();
		final long imageLength = ifd.getImageLength();

		// special case: if we only need one tile, and that tile doesn't need
		// any special handling, then we can just read it directly and return
		if ((x % tileWidth) == 0 && (y % tileLength) == 0 && width == tileWidth &&
			height == imageLength && samplesPerPixel == 1 && (ifd
				.getBitsPerSample()[0] % 8) == 0 &&
			photoInterp != PhotoInterp.WHITE_IS_ZERO &&
			photoInterp != PhotoInterp.CMYK && photoInterp != PhotoInterp.Y_CB_CR &&
			compression == TiffCompression.UNCOMPRESSED)
		{
			final long[] stripOffsets = ifd.getStripOffsets();
			final long[] stripByteCounts = ifd.getStripByteCounts();

			if (stripOffsets != null && stripByteCounts != null) {
				final long column = x / tileWidth;
				final int firstTile = (int) ((y / tileLength) * numTileCols + column);
				int lastTile = (int) (((y + height) / tileLength) * numTileCols +
					column);
				lastTile = Math.min(lastTile, stripOffsets.length - 1);

				int offset = 0;
				for (int tile = firstTile; tile <= lastTile; tile++) {
					long byteCount = equalStrips ? stripByteCounts[0]
						: stripByteCounts[tile];
					if (byteCount == numSamples && pixel > 1) {
						byteCount *= pixel;
					}

					in.seek(stripOffsets[tile]);
					final int len = (int) Math.min(buf.length - offset, byteCount);
					in.read(buf, offset, len);
					offset += len;
				}
			}
			return adjustFillOrder(ifd, buf);
		}

		final long nrows = numTileRows;
		if (planarConfig == 2) numTileRows *= samplesPerPixel;

		final IntRect imageBounds = new IntRect(x, y, (int) width, (int) height);

		final int endX = (int) width + x;
		final int endY = (int) height + y;

		final long w = tileWidth;
		final long h = tileLength;
		final int rowLen = pixel * (int) w;// tileWidth;
		final int tileSize = (int) (rowLen * h);// tileLength);

		final int planeSize = (int) (width * height * pixel);
		final int outputRowLen = (int) (pixel * width);

		int bufferSizeSamplesPerPixel = samplesPerPixel;
		if (ifd.getPlanarConfiguration() == 2) bufferSizeSamplesPerPixel = 1;
		final int bpp = ifd.getBytesPerSample()[0];
		final int bufferSize = (int) tileWidth * (int) tileLength *
			bufferSizeSamplesPerPixel * bpp;

		cachedTileBuffer = new byte[bufferSize];

		final IntRect tileBounds = new IntRect(0, 0, (int) tileWidth,
			(int) tileLength);

		for (int row = 0; row < numTileRows; row++) {
			// make the first row shorter to account for row overlap
			if (row == 0) {
				tileBounds.height = (int) (tileLength - overlapY);
			}

			for (int col = 0; col < numTileCols; col++) {
				// make the first column narrower to account for column overlap
				if (col == 0) {
					tileBounds.width = (int) (tileWidth - overlapX);
				}

				tileBounds.x = col * (int) (tileWidth - overlapX);
				tileBounds.y = row * (int) (tileLength - overlapY);

				if (planarConfig == 2) {
					tileBounds.y = (int) ((row % nrows) * (tileLength - overlapY));
				}

				if (!imageBounds.intersects(tileBounds)) continue;

				getTile(ifd, cachedTileBuffer, row, col);

				// adjust tile bounds, if necessary

				final int tileX = Math.max(tileBounds.x, x);
				final int tileY = Math.max(tileBounds.y, y);
				int realX = tileX % (int) (tileWidth - overlapX);
				int realY = tileY % (int) (tileLength - overlapY);

				int twidth = (int) Math.min(endX - tileX, tileWidth - realX);
				if (twidth <= 0) {
					twidth = (int) Math.max(endX - tileX, tileWidth - realX);
				}
				int theight = (int) Math.min(endY - tileY, tileLength - realY);
				if (theight <= 0) {
					theight = (int) Math.max(endY - tileY, tileLength - realY);
				}

				// copy appropriate portion of the tile to the output buffer

				final int copy = pixel * twidth;

				realX *= pixel;
				realY *= rowLen;

				for (int q = 0; q < effectiveChannels; q++) {
					int src = q * tileSize + realX + realY;
					int dest = q * planeSize + pixel * (tileX - x) + outputRowLen *
						(tileY - y);
					if (planarConfig == 2) dest += (planeSize * (row / nrows));

					// copying the tile directly will only work if there is no
					// overlap;
					// otherwise, we may be overwriting a previous tile
					// (or the current tile may be overwritten by a subsequent
					// tile)
					if (rowLen == outputRowLen && overlapX == 0 && overlapY == 0) {
						System.arraycopy(cachedTileBuffer, src, buf, dest, copy * theight);
					}
					else {
						for (int tileRow = 0; tileRow < theight; tileRow++) {
							System.arraycopy(cachedTileBuffer, src, buf, dest, copy);
							src += rowLen;
							dest += outputRowLen;
						}
					}
				}
			}
		}

		return adjustFillOrder(ifd, buf);
	}

	public TiffIFDEntry readTiffIFDEntry() throws IOException {
		final int entryTag = in.readUnsignedShort();

		// Parse the entry's "Type"
		IFDType entryType;
		try {
			entryType = IFDType.get(in.readUnsignedShort());
		}
		catch (final EnumException e) {
			log.error("Error reading IFD type at: " + in.getFilePointer());
			throw e;
		}

		// Parse the entry's "ValueCount"
		final int valueCount = bigTiff ? (int) in.readLong() : in.readInt();
		if (valueCount < 0) {
			throw new RuntimeException("Count of '" + valueCount + "' unexpected.");
		}

		final int nValueBytes = valueCount * entryType.getBytesPerElement();
		final int threshhold = bigTiff ? 8 : 4;
		final long offset = nValueBytes > threshhold ? getNextOffset(0) : in
			.getFilePointer();

		return new TiffIFDEntry(entryTag, entryType, valueCount, offset);
	}

	// -- Helper methods - byte stream decoding --

	/**
	 * Extracts pixel information from the given byte array according to the bits
	 * per sample, photometric interpretation and color map IFD directory entry
	 * values, and the specified byte ordering. No error checking is performed.
	 */
	private void unpackBytes(final byte[] samples, final int startIndex,
		final byte[] bytes, final IFD ifd) throws FormatException
	{
		final boolean planar = ifd.getPlanarConfiguration() == 2;

		final TiffCompression compression = ifd.getCompression();
		PhotoInterp photoInterp = ifd.getPhotometricInterpretation();
		if (compression == TiffCompression.JPEG) photoInterp = PhotoInterp.RGB;

		final int[] bitsPerSample = ifd.getBitsPerSample();
		int nChannels = bitsPerSample.length;

		int sampleCount = (int) (((long) 8 * bytes.length) / bitsPerSample[0]);
		if (photoInterp == PhotoInterp.Y_CB_CR) sampleCount *= 3;
		if (planar) {
			nChannels = 1;
		}
		else {
			sampleCount /= nChannels;
		}

		log.trace("unpacking " + sampleCount + " samples (startIndex=" +
			startIndex + "; totalBits=" + (nChannels * bitsPerSample[0]) +
			"; numBytes=" + bytes.length + ")");

		final long imageWidth = ifd.getImageWidth();
		final long imageHeight = ifd.getImageLength();

		final int bps0 = bitsPerSample[0];
		final int numBytes = ifd.getBytesPerSample()[0];
		final int nSamples = samples.length / (nChannels * numBytes);

		final boolean noDiv8 = bps0 % 8 != 0;
		final boolean bps8 = bps0 == 8;
		final boolean bps16 = bps0 == 16;

		final boolean littleEndian = ifd.isLittleEndian();

		final BitBuffer bb = new BitBuffer(bytes);

		// Hyper optimisation that takes any 8-bit or 16-bit data, where there
		// is
		// only one channel, the source byte buffer's size is less than or equal
		// to
		// that of the destination buffer and for which no special unpacking is
		// required and performs a simple array copy. Over the course of reading
		// semi-large datasets this can save **billions** of method calls.
		// Wed Aug 5 19:04:59 BST 2009
		// Chris Allan <callan@glencoesoftware.com>
		if ((bps8 || bps16) && bytes.length <= samples.length && nChannels == 1 &&
			photoInterp != PhotoInterp.WHITE_IS_ZERO &&
			photoInterp != PhotoInterp.CMYK && photoInterp != PhotoInterp.Y_CB_CR)
		{
			System.arraycopy(bytes, 0, samples, 0, bytes.length);
			return;
		}

		long maxValue = (long) Math.pow(2, bps0) - 1;
		if (photoInterp == PhotoInterp.CMYK) maxValue = Integer.MAX_VALUE;

		int skipBits = (int) (8 - ((imageWidth * bps0 * nChannels) % 8));
		if (skipBits == 8 || (bytes.length * 8 < bps0 * (nChannels * imageWidth +
			imageHeight)))
		{
			skipBits = 0;
		}

		// set up YCbCr-specific values
		float lumaRed = PhotoInterp.LUMA_RED;
		float lumaGreen = PhotoInterp.LUMA_GREEN;
		float lumaBlue = PhotoInterp.LUMA_BLUE;
		int[] reference = ifd.getIFDIntArray(IFD.REFERENCE_BLACK_WHITE);
		if (reference == null) {
			reference = new int[] { 0, 0, 0, 0, 0, 0 };
		}
		final int[] subsampling = ifd.getIFDIntArray(IFD.Y_CB_CR_SUB_SAMPLING);
		final TiffRational[] coefficients = (TiffRational[]) ifd.getIFDValue(
			IFD.Y_CB_CR_COEFFICIENTS);
		if (coefficients != null) {
			lumaRed = coefficients[0].floatValue();
			lumaGreen = coefficients[1].floatValue();
			lumaBlue = coefficients[2].floatValue();
		}
		final int subX = subsampling == null ? 2 : subsampling[0];
		final int subY = subsampling == null ? 2 : subsampling[1];
		final int block = subX * subY;
		final int nTiles = (int) (imageWidth / subX);

		// unpack pixels
		for (int sample = 0; sample < sampleCount; sample++) {
			final int ndx = startIndex + sample;
			if (ndx >= nSamples) break;

			for (int channel = 0; channel < nChannels; channel++) {
				final int index = numBytes * (sample * nChannels + channel);
				final int outputIndex = (channel * nSamples + ndx) * numBytes;

				// unpack non-YCbCr samples
				if (photoInterp != PhotoInterp.Y_CB_CR) {
					long value = 0;

					if (noDiv8) {
						// bits per sample is not a multiple of 8

						if ((channel == 0 && photoInterp == PhotoInterp.RGB_PALETTE) ||
							(photoInterp != PhotoInterp.CFA_ARRAY &&
								photoInterp != PhotoInterp.RGB_PALETTE))
						{
							value = bb.getBits(bps0) & 0xffff;
							if ((ndx % imageWidth) == imageWidth - 1) {
								bb.skipBits(skipBits);
							}
						}
					}
					else {
						value = Bytes.toLong(bytes, index, numBytes, littleEndian);
					}

					if (photoInterp == PhotoInterp.WHITE_IS_ZERO ||
						photoInterp == PhotoInterp.CMYK)
					{
						value = maxValue - value;
					}

					if (outputIndex + numBytes <= samples.length) {
						Bytes.unpack(value, samples, outputIndex, numBytes, littleEndian);
					}
				}
				else {
					// unpack YCbCr samples; these need special handling, as
					// each of
					// the RGB components depends upon two or more of the YCbCr
					// components
					if (channel == nChannels - 1) {
						final int lumaIndex = sample + (2 * (sample / block));
						final int chromaIndex = (sample / block) * (block + 2) + block;

						if (chromaIndex + 1 >= bytes.length) break;

						final int tile = ndx / block;
						final int pixel = ndx % block;
						final long r = subY * (tile / nTiles) + (pixel / subX);
						final long c = subX * (tile % nTiles) + (pixel % subX);

						final int idx = (int) (r * imageWidth + c);

						if (idx < nSamples) {
							final int y = (bytes[lumaIndex] & 0xff) - reference[0];
							final int cb = (bytes[chromaIndex] & 0xff) - reference[2];
							final int cr = (bytes[chromaIndex + 1] & 0xff) - reference[4];

							final int red = (int) (cr * (2 - 2 * lumaRed) + y);
							final int blue = (int) (cb * (2 - 2 * lumaBlue) + y);
							final int green = (int) ((y - lumaBlue * blue - lumaRed * red) /
								lumaGreen);

							samples[idx] = (byte) (red & 0xff);
							samples[nSamples + idx] = (byte) (green & 0xff);
							samples[2 * nSamples + idx] = (byte) (blue & 0xff);
						}
					}
				}
			}
		}
	}

	/**
	 * Read a file offset. For bigTiff, a 64-bit number is read. For other Tiffs,
	 * a 32-bit number is read and possibly adjusted for a possible carry-over
	 * from the previous offset.
	 */
	private long getNextOffset(final long previous) throws IOException {
		if (bigTiff || fakeBigTiff) {
			return in.readLong();
		}
		long offset = (previous & ~0xffffffffL) | (in.readInt() & 0xffffffffL);

		// Only adjust the offset if we know that the file is too large for
		// 32-bit
		// offsets to be accurate; otherwise, we're making the incorrect
		// assumption
		// that IFDs are stored sequentially.
		if (offset < previous && offset != 0 && in.length() > Integer.MAX_VALUE) {
			offset += 0x100000000L;
		}
		return offset;
	}

	/** Bit order mapping for reversed fill order. */
	private static final byte[] REVERSE = { 0x00, -0x80, 0x40, -0x40, 0x20, -0x60,
		0x60, -0x20, 0x10, -0x70, 0x50, -0x30, 0x30, -0x50, 0x70, -0x10, 0x08,
		-0x78, 0x48, -0x38, 0x28, -0x58, 0x68, -0x18, 0x18, -0x68, 0x58, -0x28,
		0x38, -0x48, 0x78, -0x08, 0x04, -0x7c, 0x44, -0x3c, 0x24, -0x5c, 0x64,
		-0x1c, 0x14, -0x6c, 0x54, -0x2c, 0x34, -0x4c, 0x74, -0x0c, 0x0c, -0x74,
		0x4c, -0x34, 0x2c, -0x54, 0x6c, -0x14, 0x1c, -0x64, 0x5c, -0x24, 0x3c,
		-0x44, 0x7c, -0x04, 0x02, -0x7e, 0x42, -0x3e, 0x22, -0x5e, 0x62, -0x1e,
		0x12, -0x6e, 0x52, -0x2e, 0x32, -0x4e, 0x72, -0x0e, 0x0a, -0x76, 0x4a,
		-0x36, 0x2a, -0x56, 0x6a, -0x16, 0x1a, -0x66, 0x5a, -0x26, 0x3a, -0x46,
		0x7a, -0x06, 0x06, -0x7a, 0x46, -0x3a, 0x26, -0x5a, 0x66, -0x1a, 0x16,
		-0x6a, 0x56, -0x2a, 0x36, -0x4a, 0x76, -0x0a, 0x0e, -0x72, 0x4e, -0x32,
		0x2e, -0x52, 0x6e, -0x12, 0x1e, -0x62, 0x5e, -0x22, 0x3e, -0x42, 0x7e,
		-0x02, 0x01, -0x7f, 0x41, -0x3f, 0x21, -0x5f, 0x61, -0x1f, 0x11, -0x6f,
		0x51, -0x2f, 0x31, -0x4f, 0x71, -0x0f, 0x09, -0x77, 0x49, -0x37, 0x29,
		-0x57, 0x69, -0x17, 0x19, -0x67, 0x59, -0x27, 0x39, -0x47, 0x79, -0x07,
		0x05, -0x7b, 0x45, -0x3b, 0x25, -0x5b, 0x65, -0x1b, 0x15, -0x6b, 0x55,
		-0x2b, 0x35, -0x4b, 0x75, -0x0b, 0x0d, -0x73, 0x4d, -0x33, 0x2d, -0x53,
		0x6d, -0x13, 0x1d, -0x63, 0x5d, -0x23, 0x3d, -0x43, 0x7d, -0x03, 0x03,
		-0x7d, 0x43, -0x3d, 0x23, -0x5d, 0x63, -0x1d, 0x13, -0x6d, 0x53, -0x2d,
		0x33, -0x4d, 0x73, -0x0d, 0x0b, -0x75, 0x4b, -0x35, 0x2b, -0x55, 0x6b,
		-0x15, 0x1b, -0x65, 0x5b, -0x25, 0x3b, -0x45, 0x7b, -0x05, 0x07, -0x79,
		0x47, -0x39, 0x27, -0x59, 0x67, -0x19, 0x17, -0x69, 0x57, -0x29, 0x37,
		-0x49, 0x77, -0x09, 0x0f, -0x71, 0x4f, -0x31, 0x2f, -0x51, 0x6f, -0x11,
		0x1f, -0x61, 0x5f, -0x21, 0x3f, -0x41, 0x7f, -0x01 };

	private byte[] adjustFillOrder(final IFD ifd, final byte[] buf)
		throws FormatException
	{
		if (ifd.getFillOrder() == FillOrder.REVERSED) {
			// swap bit order of all bytes
			for (int i = 0; i < buf.length; i++) {
				buf[i] = REVERSE[0xff & buf[i]];
			}
		}
		return buf;
	}

}
