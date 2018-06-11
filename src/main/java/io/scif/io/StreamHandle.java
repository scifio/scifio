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

import io.scif.AbstractSCIFIOPlugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.scijava.Context;
import org.scijava.util.Bytes;

/**
 * Abstract IRandomAccess implementation for reading from InputStreams and
 * writing to OutputStreams.
 *
 * @see IRandomAccess
 * @author Melissa Linkert
 */
public abstract class StreamHandle extends AbstractSCIFIOPlugin implements
	IStreamAccess
{

	// -- Fields --

	/** Name of the open stream. */
	private String file;

	/** InputStream to be used for reading. */
	private DataInputStream stream;

	/** OutputStream to be used for writing. */
	private DataOutputStream outStream;

	/** Length of the stream. */
	private long length;

	/** Current position within the stream. */
	private long fp;

	/** Marked position within the stream. */
	private long mark;

	/** Byte ordering of this stream. */
	private ByteOrder order;

	// -- Constructor --

	public StreamHandle() {
		this(null);
	}

	/**
	 * Construct a new StreamHandle. The file pointer will be set to 0, and the
	 * byte ordering will be big-endian.
	 */
	public StreamHandle(final Context context) {
		if (context != null) setContext(context);
		fp = 0;
		order = ByteOrder.BIG_ENDIAN;
		file = null;
		stream = null;
		outStream = null;
		length = 0;
		mark = 0;
	}

	// -- Field getters/setters --

	public String getFile() {
		return file;
	}

	public DataInputStream getStream() {
		return stream;
	}

	public void setStream(final DataInputStream stream) {
		this.stream = stream;
	}

	public DataOutputStream getOutStream() {
		return outStream;
	}

	public void setOutStream(final DataOutputStream outStream) {
		this.outStream = outStream;
	}

	public long getLength() {
		return length;
	}

	public void setLength(final long length) {
		this.length = length;
	}

	public long getFp() {
		return fp;
	}

	public void setFp(final long fp) {
		this.fp = fp;
	}

	public long getMark() {
		return mark;
	}

	public void setMark(final long mark) {
		this.mark = mark;
	}

	// -- IStreamAccess API methods --

	@Override
	public void setFile(final String file) throws IOException {
		this.file = file;
	}

	// -- IRandomAccess API methods --

	@Override
	public void close() throws IOException {
		length = fp = mark = 0;
		if (stream != null) stream.close();
		if (outStream != null) outStream.close();
		stream = null;
		outStream = null;
		file = null;
	}

	@Override
	public long getFilePointer() throws IOException {
		return fp;
	}

	@Override
	public long length() throws IOException {
		return length;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		int n = stream.read(b, off, len);
		if (n >= 0) fp += n;
		else n = 0;
		markManager();
		while (n < len && fp < length()) {
			final int s = stream.read(b, off + n, len - n);
			fp += s;
			n += s;
		}
		return n == -1 ? 0 : n;
	}

	@Override
	public int read(final ByteBuffer buffer) throws IOException {
		return read(buffer, 0, buffer.capacity());
	}

	@Override
	public int read(final ByteBuffer buffer, final int off, final int len)
		throws IOException
	{
		if (buffer.hasArray()) {
			return read(buffer.array(), off, len);
		}

		final byte[] b = new byte[len];
		final int n = read(b);
		buffer.put(b, off, len);
		return n;
	}

	@Override
	public void seek(final long pos) throws IOException {
		long diff = pos - fp;
		fp = pos;

		if (diff < 0) {
			resetStream();
			diff = fp;
		}
		int skipped = stream.skipBytes((int) diff);
		while (skipped < diff) {
			final int n = stream.skipBytes((int) (diff - skipped));
			if (n == 0) break;
			skipped += n;
		}
	}

	@Override
	public void write(final ByteBuffer buf) throws IOException {
		write(buf, 0, buf.capacity());
	}

	@Override
	public void write(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		buf.position(off);
		if (buf.hasArray()) {
			write(buf.array(), off, len);
		}
		else {
			final byte[] b = new byte[len];
			buf.get(b);
			write(b);
		}
	}

	@Override
	public ByteOrder getOrder() {
		return order;
	}

	@Override
	public void setOrder(final ByteOrder order) {
		this.order = order;
	}

	// -- DataInput API methods --

	@Override
	public boolean readBoolean() throws IOException {
		fp++;
		return stream.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		fp++;
		return stream.readByte();
	}

	@Override
	public char readChar() throws IOException {
		fp++;
		return stream.readChar();
	}

	@Override
	public double readDouble() throws IOException {
		fp += 8;
		final double v = stream.readDouble();
		return order.equals(ByteOrder.LITTLE_ENDIAN) ? Bytes.swap(v) : v;
	}

	@Override
	public float readFloat() throws IOException {
		fp += 4;
		final float v = stream.readFloat();
		return order.equals(ByteOrder.LITTLE_ENDIAN) ? Bytes.swap(v) : v;
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		stream.readFully(b);
		fp += b.length;
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		stream.readFully(b, off, len);
		fp += len;
	}

	@Override
	public int readInt() throws IOException {
		fp += 4;
		final int v = stream.readInt();
		return order.equals(ByteOrder.LITTLE_ENDIAN) ? Bytes.swap(v) : v;
	}

	@Override
	public String readLine() throws IOException {
		throw new IOException("Unimplemented");
	}

	@Override
	public long readLong() throws IOException {
		fp += 8;
		final long v = stream.readLong();
		return order.equals(ByteOrder.LITTLE_ENDIAN) ? Bytes.swap(v) : v;
	}

	@Override
	public short readShort() throws IOException {
		fp += 2;
		final short v = stream.readShort();
		return order.equals(ByteOrder.LITTLE_ENDIAN) ? Bytes.swap(v) : v;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		fp++;
		return stream.readUnsignedByte();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return readShort() & 0xffff;
	}

	@Override
	public String readUTF() throws IOException {
		final String s = stream.readUTF();
		fp += s.length();
		return s;
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		int skipped = 0;
		try {
			for (int i = 0; i < n; i++) {
				if (readUnsignedByte() != -1) skipped++;
				markManager();
			}
		}
		catch (final EOFException e) {}
		return skipped;
	}

	// -- DataOutput API methods --

	@Override
	public void write(final byte[] b) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) b = Bytes.swap(b);
		outStream.write(b);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeByte(v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.writeBytes(s);
	}

	@Override
	public void writeChar(int v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeChar(v);
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.writeChars(s);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeDouble(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeFloat(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeLong(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		if (order.equals(ByteOrder.LITTLE_ENDIAN)) v = Bytes.swap(v);
		outStream.writeShort(v);
	}

	@Override
	public void writeUTF(final String str) throws IOException {
		if (outStream == null) {
			throw new HandleException("This stream is read-only.");
		}
		outStream.writeUTF(str);
	}

	// -- Helper methods --

	/** Reset the marked position, if necessary. */
	private void markManager() {
		if (fp >= mark + RandomAccessInputStream.MAX_OVERHEAD - 1) {
			mark = fp;
			stream.mark(RandomAccessInputStream.MAX_OVERHEAD);
		}
	}

}
