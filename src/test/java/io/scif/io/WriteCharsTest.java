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
 * Tests for writing bytes to a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class WriteCharsTest {

	private static final byte[] PAGE = new byte[] { (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("writeTests");
	}

	private final String provider;

	private final boolean checkGrowth, testLength;

	public WriteCharsTest(final String provider, final boolean checkGrowth,
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
	public void testWriteSequential() throws IOException {
		fileHandle.writeChars("ab");
		if (checkGrowth) {
			assertEquals(4, fileHandle.length());
		}
		fileHandle.writeChars("cd");
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.writeChars("ef");
		if (checkGrowth) {
			assertEquals(12, fileHandle.length());
		}
		fileHandle.writeChars("gh");
		assertEquals(16, fileHandle.length());
		fileHandle.seek(0);
		for (byte i = (byte) 0x61; i < 0x69; i++) {
			assertEquals((byte) 0x00, fileHandle.readByte());
			assertEquals(i, fileHandle.readByte());
		}
	}

	@Test
	public void testWrite() throws IOException {
		fileHandle.writeChars("ab");
		assertEquals(4, fileHandle.getFilePointer());
		if (checkGrowth) {
			assertEquals(4, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x61, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x62, fileHandle.readByte());
	}

	@Test
	public void testWriteOffEnd() throws IOException {
		fileHandle.seek(16);
		fileHandle.writeChars("wx");
		assertEquals(20, fileHandle.getFilePointer());
		assertEquals(20, fileHandle.length());
		fileHandle.seek(16);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x77, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x78, fileHandle.readByte());
	}

	@Test
	public void testWriteTwiceOffEnd() throws IOException {
		fileHandle.seek(16);
		fileHandle.writeChars("wx");
		fileHandle.writeChars("yz");
		assertEquals(24, fileHandle.getFilePointer());
		assertEquals(24, fileHandle.length());
		fileHandle.seek(16);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x77, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x78, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x79, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x7A, fileHandle.readByte());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
