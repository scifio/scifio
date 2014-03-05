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

package io.scif.io.utests;

import static org.testng.AssertJUnit.assertEquals;
import io.scif.io.IRandomAccess;
import io.scif.io.utests.providers.IRandomAccessProvider;
import io.scif.io.utests.providers.IRandomAccessProviderFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests for reading bytes from a loci.common.IRandomAccess.
 * 
 * @see io.scif.io.IRandomAccess
 */
@Test(groups = "writeTests")
public class BufferAlignmentWriteTest {

	private static final byte[] PAGE = new byte[] {
		// 16-byte page
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00 };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters({ "provider" })
	@BeforeMethod
	public void setUp(final String provider) throws IOException {
		final IRandomAccessProviderFactory factory =
			new IRandomAccessProviderFactory();
		final IRandomAccessProvider instance = factory.getInstance(provider);
		fileHandle = instance.createMock(PAGE, MODE, BUFFER_SIZE);
	}

	@Test(groups = "initialLengthTest")
	public void testLength() throws IOException {
		assertEquals(16, fileHandle.length());
	}

	@Test
	public void testWriteArrayOffEnd() throws IOException {
		fileHandle.seek(16);
		fileHandle.write(new byte[] { (byte) 1 });
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteArrayTwiceOffEnd() throws IOException {
		fileHandle.seek(16);
		fileHandle.write(new byte[] { (byte) 1 });
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.write(new byte[] { (byte) 1 });
		assertEquals(18, fileHandle.getFilePointer());
		assertEquals(18, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteBufferOffEnd() throws IOException {
		fileHandle.seek(16);
		final ByteBuffer b = ByteBuffer.allocate(1).put((byte) 1);
		fileHandle.write(b);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteBufferTwiceOffEnd() throws IOException {
		fileHandle.seek(16);
		final ByteBuffer b = ByteBuffer.allocate(1).put((byte) 1);
		fileHandle.write(b);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		b.position(0);
		fileHandle.write(b);
		assertEquals(18, fileHandle.getFilePointer());
		assertEquals(18, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteOffEndSubArray() throws IOException {
		fileHandle.seek(16);
		fileHandle.write(new byte[] { (byte) (0), (byte) 1 }, 1, 1);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteTwiceOffEndSubArray() throws IOException {
		fileHandle.seek(16);
		fileHandle.write(new byte[] { (byte) (0), (byte) 1 }, 1, 1);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.write(new byte[] { (byte) (0), (byte) 1 }, 1, 1);
		assertEquals(18, fileHandle.getFilePointer());
		assertEquals(18, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteOffEndSubBuffer() throws IOException {
		fileHandle.seek(16);
		final ByteBuffer b = ByteBuffer.allocate(2);
		b.put((byte) 0);
		b.put((byte) 1);
		fileHandle.write(b, 1, 1);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
	}

	@Test
	public void testWriteTwiceOffEndSubBuffer() throws IOException {
		fileHandle.seek(16);
		final ByteBuffer b = ByteBuffer.allocate(2);
		b.put((byte) 0);
		b.put((byte) 1);
		fileHandle.write(b, 1, 1);
		assertEquals(17, fileHandle.getFilePointer());
		assertEquals(17, fileHandle.length());
		fileHandle.write(b, 1, 1);
		assertEquals(18, fileHandle.getFilePointer());
		assertEquals(18, fileHandle.length());
		fileHandle.seek(16);
		assertEquals(1, fileHandle.readByte());
		assertEquals(1, fileHandle.readByte());
	}

	@AfterMethod
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
