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

package io.scif.io;

import io.scif.common.Constants;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * A wrapper for buffered NIO logic that implements the IRandomAccess interface.
 * 
 * @see IRandomAccess
 * @see java.io.RandomAccessFile
 * @author Chris Allan
 */
public class NIOFileHandle extends AbstractNIOHandle {

	// -- Static fields --

	/** Default NIO buffer size to facilitate buffered I/O. */
	protected static int defaultBufferSize = 1048576;

	/**
	 * Default NIO buffer size to facilitate buffered I/O for read/write streams.
	 */
	protected static int defaultRWBufferSize = 8192;

	// -- Fields --

	/** The random access file object backing this FileHandle. */
	private final RandomAccessFile raf;

	/** The file channel backed by the random access file. */
	private final FileChannel channel;

	/** The absolute position within the file. */
	private long position = 0;

	/** The absolute position of the start of the buffer. */
	private long bufferStartPosition = 0;

	/** The buffer size. */
	private final int bufferSize;

	/** The buffer itself. */
	private ByteBuffer buffer;

	/** The default map mode for the file. */
	private FileChannel.MapMode mapMode = FileChannel.MapMode.READ_ONLY;

	/** The buffer's byte ordering. */
	private ByteOrder order;

	/** Service which provides NIO byte buffers, allocated or memory mapped. */
	private final NIOService nioService;

	// -- Constructors --

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the File argument.
	 */
	public NIOFileHandle(final NIOService nioService, final File file,
		final String mode, final int bufferSize) throws IOException
	{
		this.nioService = nioService;
		this.bufferSize = bufferSize;
		validateMode(mode);
		if (mode.equals("rw")) {
			mapMode = FileChannel.MapMode.READ_WRITE;
		}
		raf = new RandomAccessFile(file, mode);
		channel = raf.getChannel();
		buffer(position, 0);
	}

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the File argument.
	 */
	public NIOFileHandle(final NIOService nioService, final File file,
		final String mode) throws IOException
	{
		this(nioService, file, mode, mode.equals("rw") ? defaultRWBufferSize
			: defaultBufferSize);
	}

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, a file with the specified name.
	 */
	public NIOFileHandle(final NIOService nioService, final String name,
		final String mode) throws IOException
	{
		this(nioService, new File(name), mode);
	}

	// -- NIOFileHandle API methods --

	/**
	 * Set the default buffer size for read-only files. Subsequent uses of the
	 * NIOFileHandle(String, String) and NIOFileHandle(File, String) constructors
	 * will use this buffer size.
	 */
	public static void setDefaultBufferSize(final int size) {
		defaultBufferSize = size;
	}

	/**
	 * Set the default buffer size for read/write files. Subsequent uses of the
	 * NIOFileHandle(String, String) and NIOFileHandle(File, String) constructors
	 * will use this buffer size.
	 */
	public static void setDefaultReadWriteBufferSize(final int size) {
		defaultRWBufferSize = size;
	}

	// -- FileHandle and Channel API methods --

	/** Gets the random access file object backing this FileHandle. */
	public RandomAccessFile getRandomAccessFile() {
		return raf;
	}

	/** Gets the FileChannel from this FileHandle. */
	public FileChannel getFileChannel() {
		return channel;
	}

	/** Gets the current buffer size. */
	public int getBufferSize() {
		return bufferSize;
	}

	// -- AbstractNIOHandle API methods --

	/* @see AbstractNIOHandle.setLength(long) */
	@Override
	public void setLength(final long length) throws IOException {
		raf.seek(length - 1);
		raf.write((byte) 0);
		buffer = null;
	}

	// -- IRandomAccess API methods --

	/* @see IRandomAccess.close() */
	public void close() throws IOException {
		raf.close();
	}

	/* @see IRandomAccess.getFilePointer() */
	public long getFilePointer() {
		return position;
	}

	/* @see IRandomAccess.length() */
	public long length() throws IOException {
		return raf.length();
	}

	/* @see IRandomAccess.getOrder() */
	public ByteOrder getOrder() {
		return buffer == null ? order : buffer.order();
	}

	/* @see IRandomAccess.setOrder(ByteOrder) */
	public void setOrder(final ByteOrder order) {
		this.order = order;
		if (buffer != null) {
			buffer.order(order);
		}
	}

	/* @see IRandomAccess.read(byte[]) */
	public int read(final byte[] b) throws IOException {
		return read(ByteBuffer.wrap(b));
	}

	/* @see IRandomAccess.read(byte[], int, int) */
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		return read(ByteBuffer.wrap(b), off, len);
	}

	/* @see IRandomAccess.read(ByteBuffer) */
	public int read(final ByteBuffer buf) throws IOException {
		return read(buf, 0, buf.capacity());
	}

	/* @see IRandomAccess.read(ByteBuffer, int, int) */
	public int read(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		buf.position(off);
		buf.limit(off + len);
		channel.position(position);
		final int readLength = channel.read(buf);
		buffer(position + readLength, 0);
		// Return value of NIO channel's is -1 when zero bytes are read at the end
		// of the file.
		return readLength == -1 ? 0 : readLength;
	}

	/* @see IRandomAccess.seek(long) */
	public void seek(final long pos) throws IOException {
		if (mapMode == FileChannel.MapMode.READ_WRITE && pos > length()) {
			setLength(pos);
		}
		buffer(pos, 0);
	}

	/* @see java.io.DataInput.readBoolean() */
	public boolean readBoolean() throws IOException {
		return readByte() == 1;
	}

	/* @see java.io.DataInput.readByte() */
	public byte readByte() throws IOException {
		buffer(position, 1);
		position += 1;
		try {
			return buffer.get();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readChar() */
	public char readChar() throws IOException {
		buffer(position, 2);
		position += 2;
		try {
			return buffer.getChar();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readDouble() */
	public double readDouble() throws IOException {
		buffer(position, 8);
		position += 8;
		try {
			return buffer.getDouble();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readFloat() */
	public float readFloat() throws IOException {
		buffer(position, 4);
		position += 4;
		try {
			return buffer.getFloat();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readFully(byte[]) */
	public void readFully(final byte[] b) throws IOException {
		read(b);
	}

	/* @see java.io.DataInput.readFully(byte[], int, int) */
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		read(b, off, len);
	}

	/* @see java.io.DataInput.readInt() */
	public int readInt() throws IOException {
		buffer(position, 4);
		position += 4;
		try {
			return buffer.getInt();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readLine() */
	public String readLine() throws IOException {
		raf.seek(position);
		final String line = raf.readLine();
		buffer(raf.getFilePointer(), 0);
		return line;
	}

	/* @see java.io.DataInput.readLong() */
	public long readLong() throws IOException {
		buffer(position, 8);
		position += 8;
		try {
			return buffer.getLong();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readShort() */
	public short readShort() throws IOException {
		buffer(position, 2);
		position += 2;
		try {
			return buffer.getShort();
		}
		catch (final BufferUnderflowException e) {
			final EOFException eof = new EOFException(EOF_ERROR_MSG);
			eof.initCause(e);
			throw eof;
		}
	}

	/* @see java.io.DataInput.readUnsignedByte() */
	public int readUnsignedByte() throws IOException {
		return readByte() & 0xFF;
	}

	/* @see java.io.DataInput.readUnsignedShort() */
	public int readUnsignedShort() throws IOException {
		return readShort() & 0xFFFF;
	}

	/* @see java.io.DataInput.readUTF() */
	public String readUTF() throws IOException {
		raf.seek(position);
		final String utf8 = raf.readUTF();
		buffer(raf.getFilePointer(), 0);
		return utf8;
	}

	/* @see java.io.DataInput.skipBytes(int) */
	public int skipBytes(final int n) throws IOException {
		if (n < 1) {
			return 0;
		}
		final long oldPosition = position;
		final long newPosition = oldPosition + Math.min(n, length());

		buffer(newPosition, 0);
		return (int) (position - oldPosition);
	}

	// -- DataOutput API methods --

	/* @see java.io.DataOutput.write(byte[]) */
	public void write(final byte[] b) throws IOException {
		write(ByteBuffer.wrap(b));
	}

	/* @see java.io.DataOutput.write(byte[], int, int) */
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		write(ByteBuffer.wrap(b), off, len);
	}

	/* @see IRandomAccess.write(ByteBuffer) */
	public void write(final ByteBuffer buf) throws IOException {
		write(buf, 0, buf.capacity());
	}

	/* @see IRandomAccess.write(ByteBuffer, int, int) */
	public void write(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		writeSetup(len);
		buf.limit(off + len);
		buf.position(off);
		position += channel.write(buf, position);
		buffer = null;
	}

	/* @see java.io.DataOutput.write(int b) */
	public void write(final int b) throws IOException {
		writeByte(b);
	}

	/* @see java.io.DataOutput.writeBoolean(boolean) */
	public void writeBoolean(final boolean v) throws IOException {
		writeByte(v ? 1 : 0);
	}

	/* @see java.io.DataOutput.writeByte(int) */
	public void writeByte(final int v) throws IOException {
		writeSetup(1);
		buffer.put((byte) v);
		doWrite(1);
	}

	/* @see java.io.DataOutput.writeBytes(String) */
	public void writeBytes(final String s) throws IOException {
		write(s.getBytes(Constants.ENCODING));
	}

	/* @see java.io.DataOutput.writeChar(int) */
	public void writeChar(final int v) throws IOException {
		writeSetup(2);
		buffer.putChar((char) v);
		doWrite(2);
	}

	/* @see java.io.DataOutput.writeChars(String) */
	public void writeChars(final String s) throws IOException {
		write(s.getBytes("UTF-16BE"));
	}

	/* @see java.io.DataOutput.writeDouble(double) */
	public void writeDouble(final double v) throws IOException {
		writeSetup(8);
		buffer.putDouble(v);
		doWrite(8);
	}

	/* @see java.io.DataOutput.writeFloat(float) */
	public void writeFloat(final float v) throws IOException {
		writeSetup(4);
		buffer.putFloat(v);
		doWrite(4);
	}

	/* @see java.io.DataOutput.writeInt(int) */
	public void writeInt(final int v) throws IOException {
		writeSetup(4);
		buffer.putInt(v);
		doWrite(4);
	}

	/* @see java.io.DataOutput.writeLong(long) */
	public void writeLong(final long v) throws IOException {
		writeSetup(8);
		buffer.putLong(v);
		doWrite(8);
	}

	/* @see java.io.DataOutput.writeShort(int) */
	public void writeShort(final int v) throws IOException {
		writeSetup(2);
		buffer.putShort((short) v);
		doWrite(2);
	}

	/* @see java.io.DataOutput.writeUTF(String)  */
	public void writeUTF(final String str) throws IOException {
		// NB: number of bytes written is greater than the length of the string
		final int strlen = str.getBytes(Constants.ENCODING).length + 2;
		writeSetup(strlen);
		raf.seek(position);
		raf.writeUTF(str);
		position += strlen;
		buffer = null;
	}

	/**
	 * Aligns the NIO buffer, maps it if it is not currently and sets all relevant
	 * positions and offsets.
	 * 
	 * @param offset The location within the file to read from.
	 * @param size The requested read length.
	 * @throws IOException If there is an issue mapping, aligning or allocating
	 *           the buffer.
	 */
	private void buffer(long offset, final int size) throws IOException {
		position = offset;
		final long newPosition = offset + size;
		if (newPosition < bufferStartPosition ||
			newPosition > bufferStartPosition + bufferSize || buffer == null)
		{
			bufferStartPosition = offset;
			if (length() > 0 && length() - 1 < bufferStartPosition) {
				bufferStartPosition = length() - 1;
			}
			long newSize = Math.min(length() - bufferStartPosition, bufferSize);
			if (newSize < size && newSize == bufferSize) newSize = size;
			if (newSize + bufferStartPosition > length()) {
				newSize = length() - bufferStartPosition;
			}
			offset = bufferStartPosition;
			final ByteOrder byteOrder = buffer == null ? order : getOrder();
			buffer =
				nioService.allocate(channel, mapMode, bufferStartPosition,
					(int) newSize);
			if (byteOrder != null) setOrder(byteOrder);
		}
		buffer.position((int) (offset - bufferStartPosition));
		if (buffer.position() + size > buffer.limit() &&
			mapMode == FileChannel.MapMode.READ_WRITE)
		{
			buffer.limit(buffer.position() + size);
		}
	}

	private void writeSetup(final int length) throws IOException {
		validateLength(length);
		buffer(position, length);
	}

	private void doWrite(final int length) throws IOException {
		buffer.position(buffer.position() - length);
		channel.write(buffer, position);
		position += length;
	}

}
