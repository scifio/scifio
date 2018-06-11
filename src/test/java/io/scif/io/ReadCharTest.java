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
import static org.junit.Assert.assertTrue;
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
public class ReadCharTest {

	private static final byte[] PAGE = new byte[] {
		// 16-bit unicode encoding
		(byte) 0x00, (byte) 0x61, (byte) 0x00, (byte) 0x62, // a, b
		(byte) 0x00, (byte) 0x63, (byte) 0x00, (byte) 0x64, // c, d
		(byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x66, // e, f
		(byte) 0x00, (byte) 0x67, (byte) 0x00, (byte) 0x68, // g, h
		(byte) 0x00, (byte) 0x69, (byte) 0x00, (byte) 0x6A, // i, j
		(byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x6C, // k, l
		(byte) 0x00, (byte) 0x6D, (byte) 0x00, (byte) 0x6E, // m, n
		(byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x70 // o, p
	};

	private static final String MODE = "r";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("readTests");
	}

	private final String provider;

	private final boolean testLength;

	public ReadCharTest(final String provider, final boolean checkGrowth,
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
		assertEquals(32, fileHandle.length());
	}

	@Test
	public void testSequentialReadChar() throws IOException {
		final char[] expectedValues = new char[] { 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p' };
		for (final char expectedValue : expectedValues) {
			final char value = fileHandle.readChar();
			assertTrue(Character.isLetter(value));
			assertEquals(expectedValue, value);
		}
	}

	@Test
	public void testSeekForwardReadChar() throws IOException {
		fileHandle.seek(8);
		char value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('e', value);
		value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('f', value);
	}

	@Test
	public void testResetReadChar() throws IOException {
		char value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('a', value);
		value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('b', value);
		fileHandle.seek(0);
		value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('a', value);
		value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('b', value);
	}

	@Test
	public void testSeekBackReadChar() throws IOException {
		fileHandle.seek(16);
		fileHandle.seek(8);
		char value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('e', value);
		value = fileHandle.readChar();
		assertTrue(Character.isLetter(value));
		assertEquals('f', value);
	}

	@Test
	public void testRandomAccessReadChar() throws IOException {
		testSeekForwardReadChar();
		testSeekBackReadChar();
		// The test relies on a "new" file or reset file pointer
		fileHandle.seek(0);
		testResetReadChar();
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
