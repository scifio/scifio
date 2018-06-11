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
public class WriteUTFTest {

	private static final byte[] PAGE = new byte[] { (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00 };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("writeTests");
	}

	private final String provider;

	private final boolean testLength;

	public WriteUTFTest(final String provider, final boolean checkGrowth,
		final boolean testLength)
	{
		this.provider = provider;
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
		assertEquals(10, fileHandle.length());
	}

	@Test
	public void testWriteSequential() throws IOException {
		fileHandle.writeUTF("\u00A9"); // Copyright sign (2 bytes)
		fileHandle.writeUTF("\u2260"); // Not equal to (3 bytes)
		fileHandle.writeUTF("\u00A9"); // Copyright sign (2 bytes)
		fileHandle.writeUTF("\u2260"); // Not equal to (3 bytes)
		fileHandle.seek(0);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x02, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x03, fileHandle.readByte());
		assertEquals((byte) 0xE2, fileHandle.readByte());
		assertEquals((byte) 0x89, fileHandle.readByte());
		assertEquals((byte) 0xA0, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x02, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x03, fileHandle.readByte());
		assertEquals((byte) 0xE2, fileHandle.readByte());
		assertEquals((byte) 0x89, fileHandle.readByte());
		assertEquals((byte) 0xA0, fileHandle.readByte());
		fileHandle.seek(0);
		assertEquals("\u00A9", fileHandle.readUTF());
		assertEquals("\u2260", fileHandle.readUTF());
		assertEquals("\u00A9", fileHandle.readUTF());
		assertEquals("\u2260", fileHandle.readUTF());
	}

	@Test
	public void testWrite() throws IOException {
		fileHandle.writeUTF("\u00A9\n"); // Copyright sign (2 bytes)
		assertEquals(5, fileHandle.getFilePointer());
		fileHandle.seek(0);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x03, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		fileHandle.seek(0);
		assertEquals("\u00A9\n", fileHandle.readUTF());
	}

	@Test
	public void testWriteTwoCharacters() throws IOException {
		fileHandle.seek(0);
		fileHandle.writeUTF("\u00A9\u2260");
		assertEquals(7, fileHandle.getFilePointer());
		fileHandle.seek(0);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x05, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		fileHandle.seek(0);
		assertEquals("\u00A9\u2260", fileHandle.readUTF());
	}

	@Test
	public void testWriteOffEnd() throws IOException {
		fileHandle.seek(10);
		fileHandle.writeUTF("\u00A9"); // Copyright sign (2 bytes)
		assertEquals(14, fileHandle.getFilePointer());
		assertEquals(14, fileHandle.length());
		fileHandle.seek(10);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x02, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		fileHandle.seek(10);
		assertEquals("\u00A9", fileHandle.readUTF());
	}

	@Test
	public void testWriteTwiceOffEnd() throws IOException {
		fileHandle.seek(10);
		fileHandle.writeUTF("\u00A9"); // Copyright sign (2 bytes)
		fileHandle.writeUTF("\u2260"); // Not equal to (3 bytes)
		assertEquals(19, fileHandle.getFilePointer());
		assertEquals(19, fileHandle.length());
		fileHandle.seek(10);
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x02, fileHandle.readByte());
		assertEquals((byte) 0xC2, fileHandle.readByte());
		assertEquals((byte) 0xA9, fileHandle.readByte());
		assertEquals((byte) 0x00, fileHandle.readByte());
		assertEquals((byte) 0x03, fileHandle.readByte());
		assertEquals((byte) 0xE2, fileHandle.readByte());
		assertEquals((byte) 0x89, fileHandle.readByte());
		assertEquals((byte) 0xA0, fileHandle.readByte());
		fileHandle.seek(10);
		assertEquals("\u00A9", fileHandle.readUTF());
		assertEquals("\u2260", fileHandle.readUTF());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
