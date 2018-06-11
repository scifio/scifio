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
 * Tests for reading longs from a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class WriteLongTest {

	private static final byte[] PAGE = new byte[] {
		// 64-bit long
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

	private static final String MODE = "rw";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	private final String provider;

	private final boolean checkGrowth, testLength;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("writeTests");
	}

	public WriteLongTest(final String provider, final boolean checkGrowth,
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
		assertEquals(64, fileHandle.length());
	}

	@Test
	public void testSequential() throws IOException {
		fileHandle.writeLong(1L);
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.writeLong(1152921504606846722L);
		if (checkGrowth) {
			assertEquals(16, fileHandle.length());
		}
		fileHandle.writeLong(3L);
		if (checkGrowth) {
			assertEquals(24, fileHandle.length());
		}
		fileHandle.writeLong(1152921504606846724L);
		if (checkGrowth) {
			assertEquals(32, fileHandle.length());
		}
		fileHandle.writeLong(5L);
		if (checkGrowth) {
			assertEquals(40, fileHandle.length());
		}
		fileHandle.writeLong(-1L);
		if (checkGrowth) {
			assertEquals(48, fileHandle.length());
		}
		fileHandle.writeLong(7L);
		if (checkGrowth) {
			assertEquals(56, fileHandle.length());
		}
		fileHandle.writeLong(-2L);
		if (checkGrowth) {
			assertEquals(64, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals(1L, fileHandle.readLong());
		assertEquals(1152921504606846722L, fileHandle.readLong());
		assertEquals(3L, fileHandle.readLong());
		assertEquals(1152921504606846724L, fileHandle.readLong());
		assertEquals(5L, fileHandle.readLong());
		assertEquals(-1L, fileHandle.readLong());
		assertEquals(7L, fileHandle.readLong());
		assertEquals(-2L, fileHandle.readLong());
	}

	@Test
	public void testSeekForward() throws IOException {
		fileHandle.seek(8);
		fileHandle.writeLong(1152921504606846722L);
		if (checkGrowth) {
			assertEquals(16, fileHandle.length());
		}
		fileHandle.writeLong(3L);
		if (checkGrowth) {
			assertEquals(24, fileHandle.length());
		}
		fileHandle.seek(8);
		assertEquals(1152921504606846722L, fileHandle.readLong());
		assertEquals(3L, fileHandle.readLong());
	}

	@Test
	public void testReset() throws IOException {
		fileHandle.writeLong(1L);
		if (checkGrowth) {
			assertEquals(8, fileHandle.length());
		}
		fileHandle.writeLong(1152921504606846722L);
		if (checkGrowth) {
			assertEquals(16, fileHandle.length());
		}
		fileHandle.seek(0);
		assertEquals(1L, fileHandle.readLong());
		assertEquals(1152921504606846722L, fileHandle.readLong());
		fileHandle.seek(0);
		fileHandle.writeLong(1L);
		fileHandle.writeLong(1152921504606846722L);
		fileHandle.seek(0);
		assertEquals(1L, fileHandle.readLong());
		assertEquals(1152921504606846722L, fileHandle.readLong());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
