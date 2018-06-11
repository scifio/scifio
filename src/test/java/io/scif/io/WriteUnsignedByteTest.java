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

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import io.scif.io.providers.IRandomAccessProvider;
import io.scif.io.providers.IRandomAccessProviderFactory;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for reading unsigned bytes from a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class WriteUnsignedByteTest {

	private static final byte[] PAGE = new byte[] { (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("writeTests");
	}

	private final String provider;

	private final boolean checkGrowth, testLength;

	public WriteUnsignedByteTest(final String provider, final boolean checkGrowth,
		final boolean testLength)
	{
		this.provider = provider;
		this.checkGrowth = checkGrowth;
		this.testLength = testLength;
	}

	@Before
	public void setUp() throws IOException {
		final IRandomAccessProviderFactory factory =
			new IRandomAccessProviderFactory();
		final IRandomAccessProvider instance = factory.getInstance(provider);
		fileHandle = instance.createMock(PAGE, MODE, BUFFER_SIZE);
	}

	@Test
	public void testLength() throws IOException {
		assumeTrue(testLength);
		assertEquals(16, fileHandle.length());
	}

	@Test
	public void testSequential() throws IOException {
		fileHandle.writeByte(1);
		if (checkGrowth) {
			assertEquals(1, fileHandle.length());
		}
		fileHandle.writeByte(2);
		if (checkGrowth) {
			assertEquals(2, fileHandle.length());
		}
		fileHandle.writeByte(3);
		if (checkGrowth) {
			assertEquals(3, fileHandle.length());
		}
		fileHandle.writeByte(4);
		if (checkGrowth) {
			assertEquals(4, fileHandle.length());
		}
		fileHandle.writeByte(5);
		if (checkGrowth) {
			assertEquals(5, fileHandle.length());
		}
		fileHandle.writeByte(6);
		if (checkGrowth) {
			assertEquals(6, fileHandle.length());
		}
		fileHandle.writeByte(7);
		if (checkGrowth) {
			assertEquals(7, fileHandle.length());
		}
		fileHandle.writeByte(8);
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.writeByte(9);
		if (checkGrowth) {
			assertEquals(9, fileHandle.length());
		}
		fileHandle.writeByte(10);
		if (checkGrowth) {
			assertEquals(10, fileHandle.length());
		}
		fileHandle.writeByte(11);
		if (checkGrowth) {
			assertEquals(11, fileHandle.length());
		}
		fileHandle.writeByte(12);
		if (checkGrowth) {
			assertEquals(12, fileHandle.length());
		}
		fileHandle.writeByte(13);
		if (checkGrowth) {
			assertEquals(13, fileHandle.length());
		}
		fileHandle.writeByte(14);
		if (checkGrowth) {
			assertEquals(14, fileHandle.length());
		}
		fileHandle.writeByte((byte) 0xFF);
		if (checkGrowth) {
			assertEquals(15, fileHandle.length());
		}
		fileHandle.writeByte((byte) 0xFE);
		if (checkGrowth) {
			assertEquals(16, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals(1, fileHandle.readUnsignedByte());
		assertEquals(2, fileHandle.readUnsignedByte());
		assertEquals(3, fileHandle.readUnsignedByte());
		assertEquals(4, fileHandle.readUnsignedByte());
		assertEquals(5, fileHandle.readUnsignedByte());
		assertEquals(6, fileHandle.readUnsignedByte());
		assertEquals(7, fileHandle.readUnsignedByte());
		assertEquals(8, fileHandle.readUnsignedByte());
		assertEquals(9, fileHandle.readUnsignedByte());
		assertEquals(10, fileHandle.readUnsignedByte());
		assertEquals(11, fileHandle.readUnsignedByte());
		assertEquals(12, fileHandle.readUnsignedByte());
		assertEquals(13, fileHandle.readUnsignedByte());
		assertEquals(14, fileHandle.readUnsignedByte());
		assertEquals(255, fileHandle.readUnsignedByte());
		assertEquals(254, fileHandle.readUnsignedByte());
	}

	@Test
	public void testSeekForward() throws IOException {
		fileHandle.seek(7);
		fileHandle.writeByte(8);
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.writeByte(9);
		if (checkGrowth) {
			assertEquals(9, fileHandle.length());
		}
		fileHandle.seek(7);
		fileHandle.writeByte((byte) 0xFF);
		fileHandle.writeByte((byte) 0xFE);
		fileHandle.seek(7);
		assertEquals(255, fileHandle.readUnsignedByte());
		assertEquals(254, fileHandle.readUnsignedByte());
	}

	@Test
	public void testReset() throws IOException {
		fileHandle.writeByte(1);
		if (checkGrowth) {
			assertEquals(1, fileHandle.length());
		}
		fileHandle.writeByte(2);
		if (checkGrowth) {
			assertEquals(2, fileHandle.length());
		}
		fileHandle.seek(0);
		fileHandle.writeByte(1);
		fileHandle.writeByte(2);
		fileHandle.seek(0);
		assertEquals(1, fileHandle.readUnsignedByte());
		assertEquals(2, fileHandle.readUnsignedByte());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
