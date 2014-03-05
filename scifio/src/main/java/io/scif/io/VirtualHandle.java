/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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
 * e.g. for {@link io.scif.formats.FakeFormat}. All operations are no-ops.
 */
public class VirtualHandle extends StreamHandle {

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
		// no-op as there is no backing stream
	}

	// -- IRandomAccess API Methods --

	@Override
	public int read(final byte[] b) throws IOException {
		// no-op
		return 0;
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		// no-op
		return 0;
	}

	@Override
	public int read(final ByteBuffer buffer) throws IOException {
		// no-op
		return 0;
	}

	@Override
	public int read(final ByteBuffer buffer, final int off, final int len)
		throws IOException
	{
		return 0;
	}

	@Override
	public void seek(final long pos) throws IOException {
		// no-op
	}

	@Override
	public void write(final ByteBuffer buf) throws IOException {
		// no-op
	}

	@Override
	public void write(final ByteBuffer buf, final int off, final int len)
		throws IOException
	{
		// no-op
	}

	// -- DataInput API Methods --

	@Override
	public char readChar() throws IOException {
		// no-op
		return 0;
	}

	@Override
	public double readDouble() throws IOException {
		// no-op
		return 0.0;
	}

	@Override
	public float readFloat() throws IOException {
		// no-op
		return 0f;
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		// no-op
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		// no-op
	}

	@Override
	public int readInt() throws IOException {
		// no-op
		return 0;
	}

	@Override
	public String readLine() throws IOException {
		throw new IOException("Unimplemented");
	}

	@Override
	public long readLong() throws IOException {
		// no-op
		return 0l;
	}

	@Override
	public short readShort() throws IOException {
		// no-op
		return 0;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		// no-op
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		// no-op
		return 0;
	}

	@Override
	public String readUTF() throws IOException {
		// no-op
		return "";
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		// no-op
		return 0;
	}

	// -- DataOutput API methods --

	@Override
	public void write(final byte[] b) throws IOException {
		// no-op
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		// no-op
	}

	@Override
	public void write(final int b) throws IOException {
		// no-op
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		// no-op
	}

	@Override
	public void writeByte(final int v) throws IOException {
		// no-op
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		// no-op
	}

	@Override
	public void writeChar(final int v) throws IOException {
		// no-op
	}

	@Override
	public void writeChars(final String s) throws IOException {
		// no-op
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		// no-op
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		// no-op
	}

	@Override
	public void writeInt(final int v) throws IOException {
		// no-op
	}

	@Override
	public void writeLong(final long v) throws IOException {
		// no-op
	}

	@Override
	public void writeShort(final int v) throws IOException {
		// no-op
	}

	@Override
	public void writeUTF(final String str) throws IOException {
		// no-op
	}
}
