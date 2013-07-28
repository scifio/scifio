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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A wrapper for RandomAccessFile that implements the IRandomAccess interface.
 * 
 * @see IRandomAccess
 * @see java.io.RandomAccessFile
 * @author Curtis Rueden
 */
public class FileHandle implements IRandomAccess {

	// -- Fields --

	/** The random access file object backing this FileHandle. */
	protected RandomAccessFile raf;

	// -- Constructors --

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the File argument.
	 */
	public FileHandle(final File file, final String mode)
		throws FileNotFoundException
	{
		raf = new RandomAccessFile(file, mode);
	}

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, a file with the specified name.
	 */
	public FileHandle(final String name, final String mode)
		throws FileNotFoundException
	{
		raf = new RandomAccessFile(name, mode);
	}

	// -- FileHandle API methods --

	/** Gets the random access file object backing this FileHandle. */
	public RandomAccessFile getRandomAccessFile() {
		return raf;
	}

	// -- IRandomAccess API methods --

	/* @see IRandomAccess.close() */
	public void close() throws IOException {
		raf.close();
	}

	/* @see IRandomAccess.getFilePointer() */
	public long getFilePointer() throws IOException {
		return raf.getFilePointer();
	}

	/* @see IRandomAccess.length() */
	public long length() throws IOException {
		return raf.length();
	}

	/* @see IRandomAccess.read(byte[]) */
	public int read(final byte[] b) throws IOException {
		return raf.read(b);
	}

	/* @see IRandomAccess.read(byte[], int, int) */
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		return raf.read(b, off, len);
	}

	/* @see IRandomAccess.read(ByteBuffer) */
	public int read(final ByteBuffer buffer) throws IOException {
		return read(buffer, 0, buffer.capacity());
	}

	/* @see IRandomAccess.read(ByteBuffer, int, int) */
	public int read(final ByteBuffer buffer, final int off, final int len)
		throws IOException
	{
		final byte[] b = new byte[len];
		final int n = read(b);
		buffer.put(b, off, len);
		return n;
	}

	/* @see IRandomAccess.seek(long) */
	public void seek(final long pos) throws IOException {
		raf.seek(pos);
	}

	/* @see IRandomAccess.write(ByteBuffer) */
	public void write(final ByteBuffer buf) throws IOException {
		write(buf, 0, buf.capacity());
	}

	/* @see IRandomAccess.write(ByteBuffer, int, int) */
	public void write(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		// TODO
	}

	// -- DataInput API methods --

	/* @see java.io.DataInput.readBoolean() */
	public boolean readBoolean() throws IOException {
		return raf.readBoolean();
	}

	/* @see java.io.DataInput.readByte() */
	public byte readByte() throws IOException {
		return raf.readByte();
	}

	/* @see java.io.DataInput.readChar() */
	public char readChar() throws IOException {
		return raf.readChar();
	}

	/* @see java.io.DataInput.readDouble() */
	public double readDouble() throws IOException {
		return raf.readDouble();
	}

	/* @see java.io.DataInput.readFloat() */
	public float readFloat() throws IOException {
		return raf.readFloat();
	}

	/* @see java.io.DataInput.readFully(byte[]) */
	public void readFully(final byte[] b) throws IOException {
		raf.readFully(b);
	}

	/* @see java.io.DataInput.readFully(byte[], int, int) */
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		raf.readFully(b, off, len);
	}

	/* @see java.io.DataInput.readInt() */
	public int readInt() throws IOException {
		return raf.readInt();
	}

	/* @see java.io.DataInput.readLine() */
	public String readLine() throws IOException {
		return raf.readLine();
	}

	/* @see java.io.DataInput.readLong() */
	public long readLong() throws IOException {
		return raf.readLong();
	}

	/* @see java.io.DataInput.readShort() */
	public short readShort() throws IOException {
		return raf.readShort();
	}

	/* @see java.io.DataInput.readUnsignedByte() */
	public int readUnsignedByte() throws IOException {
		return raf.readUnsignedByte();
	}

	/* @see java.io.DataInput.readUnsignedShort() */
	public int readUnsignedShort() throws IOException {
		return raf.readUnsignedShort();
	}

	/* @see java.io.DataInput.readUTF() */
	public String readUTF() throws IOException {
		return raf.readUTF();
	}

	/* @see java.io.DataInput.skipBytes(int) */
	public int skipBytes(final int n) throws IOException {
		return raf.skipBytes(n);
	}

	// -- DataOutput API metthods --

	/* @see java.io.DataOutput.write(byte[]) */
	public void write(final byte[] b) throws IOException {
		raf.write(b);
	}

	/* @see java.io.DataOutput.write(byte[], int, int) */
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		raf.write(b, off, len);
	}

	/* @see java.io.DataOutput.write(int b) */
	public void write(final int b) throws IOException {
		raf.write(b);
	}

	/* @see java.io.DataOutput.writeBoolean(boolean) */
	public void writeBoolean(final boolean v) throws IOException {
		raf.writeBoolean(v);
	}

	/* @see java.io.DataOutput.writeByte(int) */
	public void writeByte(final int v) throws IOException {
		raf.writeByte(v);
	}

	/* @see java.io.DataOutput.writeBytes(String) */
	public void writeBytes(final String s) throws IOException {
		raf.writeBytes(s);
	}

	/* @see java.io.DataOutput.writeChar(int) */
	public void writeChar(final int v) throws IOException {
		raf.writeChar(v);
	}

	/* @see java.io.DataOutput.writeChars(String) */
	public void writeChars(final String s) throws IOException {
		raf.writeChars(s);
	}

	/* @see java.io.DataOutput.writeDouble(double) */
	public void writeDouble(final double v) throws IOException {
		raf.writeDouble(v);
	}

	/* @see java.io.DataOutput.writeFloat(float) */
	public void writeFloat(final float v) throws IOException {
		raf.writeFloat(v);
	}

	/* @see java.io.DataOutput.writeInt(int) */
	public void writeInt(final int v) throws IOException {
		raf.writeInt(v);
	}

	/* @see java.io.DataOutput.writeLong(long) */
	public void writeLong(final long v) throws IOException {
		raf.writeLong(v);
	}

	/* @see java.io.DataOutput.writeShort(int) */
	public void writeShort(final int v) throws IOException {
		raf.writeShort(v);
	}

	/* @see java.io.DataOutput.writeUTF(String)  */
	public void writeUTF(final String str) throws IOException {
		raf.writeUTF(str);
	}

	public ByteOrder getOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOrder(final ByteOrder order) {
		// TODO Auto-generated method stub
	}

}
