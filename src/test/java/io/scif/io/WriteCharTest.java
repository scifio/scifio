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
 * Tests for reading characters from a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class WriteCharTest {

	private static final byte[] PAGE = new byte[] { (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("writeTests");
	}

	private final String provider;

	private final boolean checkGrowth, testLength;

	public WriteCharTest(final String provider, final boolean checkGrowth,
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
		assertEquals(32, fileHandle.length());
	}

	@Test
	public void testSequential() throws IOException {
		fileHandle.seek(0);
		fileHandle.writeChar('a');
		if (checkGrowth) {
			assertEquals(2, fileHandle.length());
		}
		fileHandle.writeChar('b');
		if (checkGrowth) {
			assertEquals(4, fileHandle.length());
		}
		fileHandle.writeChar('c');
		if (checkGrowth) {
			assertEquals(6, fileHandle.length());
		}
		fileHandle.writeChar('d');
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals('a', fileHandle.readChar());
		assertEquals('b', fileHandle.readChar());
		assertEquals('c', fileHandle.readChar());
		assertEquals('d', fileHandle.readChar());
	}

	@Test
	public void testSeekForward() throws IOException {
		fileHandle.seek(8);
		fileHandle.writeChar('e');
		fileHandle.writeChar('f');
		if (checkGrowth) {
			assertEquals(12, fileHandle.length());
		}
		fileHandle.seek(8);
		assertEquals('e', fileHandle.readChar());
		assertEquals('f', fileHandle.readChar());
	}

	@Test
	public void testReset() throws IOException {
		fileHandle.seek(0);
		fileHandle.writeChar('a');
		if (checkGrowth) {
			assertEquals(2, fileHandle.length());
		}
		fileHandle.writeChar('b');
		if (checkGrowth) {
			assertEquals(4, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals('a', fileHandle.readChar());
		assertEquals('b', fileHandle.readChar());
		fileHandle.seek(0);
		fileHandle.writeChar('c');
		fileHandle.writeChar('d');
		fileHandle.seek(0);
		assertEquals('c', fileHandle.readChar());
		assertEquals('d', fileHandle.readChar());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
