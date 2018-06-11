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

package io.scif.io;

import io.scif.common.Constants;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channel;

/**
 * NIOInputStream provides methods for "intelligent" reading of files and byte
 * arrays.
 */
public class NIOInputStream extends InputStream implements DataInput {

	// -- Constants --

	/**
	 * Block size to use when searching through the stream. This value should not
	 * exceed MAX_OVERHEAD!
	 */
	private static final int DEFAULT_BLOCK_SIZE = 256 * 1024; // 256 KB

	/** Maximum number of bytes to search when searching through the stream. */
	private static final int MAX_SEARCH_SIZE = 512 * 1024 * 1024; // 512 MB

	// -- Fields --

	private final IRandomAccess raf;

	/** The file name. */
	private String filename;

	/** The file. */
	private File file;

	/** The file channel backed by the random access file. */
	private Channel channel;

	/** Endianness of the stream. */
	private boolean isLittleEndian;

	// -- Constructors --

	/** Constructs an NIOInputStream around the given file. */
	public NIOInputStream(final String filename) throws IOException {
		this.filename = filename;
		file = new File(filename);
		raf = new FileHandle(file, "r");
	}

	/** Constructs a random access stream around the given handle. */
	public NIOInputStream(final IRandomAccess handle) {
		raf = handle;
	}

	/** Constructs a random access stream around the given byte array. */
	public NIOInputStream(final byte[] array) {
		this(new ByteArrayHandle(array));
	}

	// -- NIOInputStream API methods --

	/** Returns the underlying InputStream. */
	public DataInputStream getInputStream() {
		// FIXME: To be implemented.
		return null;
	}

	/**
	 * Sets the number of bytes by which to extend the stream. This only applies
	 * to InputStream API methods.
	 */
	public void setExtend(final int extend) {
		// FIXME: WTF? To be implemented?
	}

	/** Seeks to the given offset within the stream. */
	public void seek(final long pos) throws IOException {
		raf.seek(pos);
	}

	/** Alias for readByte(). */
	@Override
	public int read() throws IOException {
		return raf.readUnsignedByte();
	}

	/** Gets the number of bytes in the file. */
	public long length() throws IOException {
		return raf.length();
	}

	/** Gets the current (absolute) file pointer. */
	public long getFilePointer() {
		// FIXME: Change interface?
		try {
			return raf.getFilePointer();
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** Closes the streams. */
	@Override
	public void close() throws IOException {
		// FIXME: To be implemented.
	}

	/** Sets the endianness of the stream. */
	public void order(final boolean isLittleEndian) {
		this.isLittleEndian = isLittleEndian;
	}

	/** Gets the endianness of the stream. */
	public boolean isLittleEndian() {
		return isLittleEndian;
	}

	/**
	 * Reads a string ending with one of the characters in the given string.
	 *
	 * @see #findString(String...)
	 */
	public String readString(final String lastChars) throws IOException {
		if (lastChars.length() == 1) return findString(lastChars);

		final String[] terminators = new String[lastChars.length()];
		for (int i = 0; i < terminators.length; i++) {
			terminators[i] = lastChars.substring(i, i + 1);
		}
		return findString(terminators);
	}

	/**
	 * Reads a string ending with one of the given terminating substrings.
	 *
	 * @param terminators The strings for which to search.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found.
	 */
	public String findString(final String... terminators) throws IOException {
		return findString(true, DEFAULT_BLOCK_SIZE, terminators);
	}

	/**
	 * Reads or skips a string ending with one of the given terminating
	 * substrings.
	 *
	 * @param saveString Whether to collect the string from the current file
	 *          pointer to the terminating bytes, and return it. If false, returns
	 *          null.
	 * @param terminators The strings for which to search.
	 * @throws IOException If saveString flag is set and the maximum search length
	 *           (512 MB) is exceeded.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found, or null if saveString flag is unset.
	 */
	public String findString(final boolean saveString,
		final String... terminators) throws IOException
	{
		return findString(saveString, DEFAULT_BLOCK_SIZE, terminators);
	}

	/**
	 * Reads a string ending with one of the given terminating substrings, using
	 * the specified block size for buffering.
	 *
	 * @param blockSize The block size to use when reading bytes in chunks.
	 * @param terminators The strings for which to search.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found.
	 */
	public String findString(final int blockSize, final String... terminators)
		throws IOException
	{
		return findString(true, blockSize, terminators);
	}

	/**
	 * Reads or skips a string ending with one of the given terminating
	 * substrings, using the specified block size for buffering.
	 *
	 * @param saveString Whether to collect the string from the current file
	 *          pointer to the terminating bytes, and return it. If false, returns
	 *          null.
	 * @param blockSize The block size to use when reading bytes in chunks.
	 * @param terminators The strings for which to search.
	 * @throws IOException If saveString flag is set and the maximum search length
	 *           (512 MB) is exceeded.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found, or null if saveString flag is unset.
	 */
	public String findString(final boolean saveString, final int blockSize,
		final String... terminators) throws IOException
	{
		final StringBuilder out = new StringBuilder();
		final long startPos = getFilePointer();
		long bytesDropped = 0;
		final long inputLen = length();
		long maxLen = inputLen - startPos;
		final boolean tooLong = saveString && maxLen > MAX_SEARCH_SIZE;
		if (tooLong) maxLen = MAX_SEARCH_SIZE;
		boolean match = false;
		int maxTermLen = 0;
		for (final String term : terminators) {
			final int len = term.length();
			if (len > maxTermLen) maxTermLen = len;
		}

		final InputStreamReader in = new InputStreamReader(this,
			Constants.ENCODING);
		final char[] buf = new char[blockSize];
		long loc = 0;
		while (loc < maxLen) {
			final long pos = startPos + loc;

			// if we're not saving the string, drop any old, unnecessary output
			if (!saveString) {
				final int outLen = out.length();
				if (outLen >= maxTermLen) {
					final int dropIndex = outLen - maxTermLen + 1;
					final String last = out.substring(dropIndex, outLen);
					out.setLength(0);
					out.append(last);
					bytesDropped += dropIndex;
				}
			}

			// read block from stream
			int num = blockSize;
			if (pos + blockSize > inputLen) num = (int) (inputLen - pos);
			final int r = in.read(buf, 0, num);
			if (r <= 0) throw new IOException("Cannot read from stream: " + r);

			// append block to output
			out.append(buf, 0, r);

			// check output, returning smallest possible string
			int min = Integer.MAX_VALUE, tagLen = 0;
			for (final String terminator : terminators) {
				final int len = terminator.length();
				final int start = (int) (loc - bytesDropped - len);
				final int value = out.indexOf(terminator, start < 0 ? 0 : start);
				if (value >= 0 && value < min) {
					match = true;
					min = value;
					tagLen = len;
				}
			}

			if (match) {
				// reset stream to proper location
				seek(startPos + bytesDropped + min + tagLen);

				// trim output string
				if (saveString) {
					out.setLength(min + tagLen);
					return out.toString();
				}
				return null;
			}

			loc += r;
		}

		// no match
		if (tooLong) throw new IOException("Maximum search length reached.");
		return null;
	}

	// -- DataInput API methods --

	/** Read an input byte and return true if the byte is nonzero. */
	@Override
	public boolean readBoolean() throws IOException {
		return raf.readBoolean();
	}

	/** Read one byte and return it. */
	@Override
	public byte readByte() throws IOException {
		return raf.readByte();
	}

	/** Read an input char. */
	@Override
	public char readChar() throws IOException {
		return raf.readChar();
	}

	/** Read eight bytes and return a double value. */
	@Override
	public double readDouble() throws IOException {
		return raf.readDouble();
	}

	/** Read four bytes and return a float value. */
	@Override
	public float readFloat() throws IOException {
		return raf.readFloat();
	}

	/** Read four input bytes and return an int value. */
	@Override
	public int readInt() throws IOException {
		return raf.readInt();
	}

	/** Read the next line of text from the input stream. */
	@Override
	public String readLine() throws IOException {
		return findString("\n");
	}

	/** Read a string of arbitrary length, terminated by a null char. */
	public String readCString() throws IOException {
		return findString("\0");
	}

	/** Read a string of length n. */
	public String readString(final int n) throws IOException {
		final byte[] b = new byte[n];
		readFully(b);
		return new String(b, Constants.ENCODING);
	}

	/** Read eight input bytes and return a long value. */
	@Override
	public long readLong() throws IOException {
		return raf.readLong();
	}

	/** Read two input bytes and return a short value. */
	@Override
	public short readShort() throws IOException {
		return raf.readShort();
	}

	/** Read an input byte and zero extend it appropriately. */
	@Override
	public int readUnsignedByte() throws IOException {
		return raf.readUnsignedByte();
	}

	/** Read two bytes and return an int in the range 0 through 65535. */
	@Override
	public int readUnsignedShort() throws IOException {
		return raf.readUnsignedShort();
	}

	/** Read a string that has been encoded using a modified UTF-8 format. */
	@Override
	public String readUTF() throws IOException {
		return null; // not implemented yet...we don't really need this
	}

	/** Skip n bytes within the stream. */
	@Override
	public int skipBytes(final int n) throws IOException {
		return raf.skipBytes(n);
	}

	/** Read bytes from the stream into the given array. */
	@Override
	public int read(final byte[] array) throws IOException {
		return read(array, 0, array.length);
	}

	/**
	 * Read n bytes from the stream into the given array at the specified offset.
	 */
	@Override
	public int read(final byte[] array, final int offset, final int n)
		throws IOException
	{
		return raf.read(array, offset, n);
	}

	/** Read bytes from the stream into the given array. */
	@Override
	public void readFully(final byte[] array) throws IOException {
		readFully(array, 0, array.length);
	}

	/**
	 * Read n bytes from the stream into the given array at the specified offset.
	 */
	@Override
	public void readFully(final byte[] array, final int offset, final int n)
		throws IOException
	{
		raf.readFully(array, offset, n);
	}

	// -- InputStream API methods --

	@Override
	public int available() throws IOException {
		// FIXME: Need to implement this in sub-classes of IRandomAccess.
		return 0;
	}

	@Override
	public void mark(final int readLimit) {
		// XXX: No-op
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void reset() throws IOException {
		raf.seek(0);
	}

}
