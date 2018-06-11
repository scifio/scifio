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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A special IRandomAccess handle for files that don't exist physically. This
 * will allow {@link RandomAccessInputStream} creation using arbitrary Strings,
 * e.g. for {@link io.scif.formats.FakeFormat}. All reading/writing operations
 * throw {@link UnsupportedOperationException}.
 */
public class VirtualHandle extends StreamHandle {

	// -- Constants --

	private static final String FAIL_MSG =
		"Attempting to read or write from a io.scif.io.VirtualHandle." +
			" There is no source to operate on.";

	// -- Constructor --

	public VirtualHandle() throws IOException {
		this("");
	}

	/**
	 * Constructs a new handle around the provided id. No action needs to be taken
	 * except setting the file field, as the id does not represent a physical
	 * file.
	 */
	public VirtualHandle(final String id) throws IOException {
		super();
		setFile(id);
	}

	// -- IStreamAccess API Methods --

	@Override
	public boolean isConstructable(final String id) throws IOException {
		// Can always be created
		return true;
	}

	@Override
	public void resetStream() throws IOException {
		// no-op
	}

	// -- IRandomAccess API Methods --

	@Override
	public int read(final byte[] b) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int read(final ByteBuffer buffer) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int read(final ByteBuffer buffer, final int off, final int len)
		throws IOException
	{
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void seek(final long pos) throws IOException {
		// no-op
	}

	@Override
	public void write(final ByteBuffer buf) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void write(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	// -- DataInput API Methods --

	@Override
	public boolean readBoolean() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public byte readByte() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int readInt() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public long readLong() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public short readShort() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int readUnsignedByte() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	// -- DataOutput API methods --

	@Override
	public void write(final byte[] b) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void write(final int b) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeChars(final String s) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}

	@Override
	public void writeUTF(final String str) throws IOException {
		throw new UnsupportedOperationException(FAIL_MSG);
	}
}
