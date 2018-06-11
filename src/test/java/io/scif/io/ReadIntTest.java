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
 * Tests for reading ints from a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class ReadIntTest {

	private static final byte[] PAGE = new byte[] {
		// 32-bit long
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
		// 32-bit long
		(byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0x02,
		// 32-bit long
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
		// 32-bit long
		(byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0x04,
		// 32-bit long
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
		// 32-bit long (-1)
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
		// 32-bit long
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07,
		// 32-bit long (-2)
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE };

	private static final String MODE = "r";

	private static final int BUFFER_SIZE = 1024;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("readTests");
	}

	private final String provider;

	private final boolean testLength;

	public ReadIntTest(final String provider, final boolean checkGrowth,
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
	public void testSequentialReadInt() throws IOException {
		assertEquals(1, fileHandle.readInt());
		assertEquals(268435202, fileHandle.readInt());
		assertEquals(3, fileHandle.readInt());
		assertEquals(268435204, fileHandle.readInt());
		assertEquals(5, fileHandle.readInt());
		assertEquals(-1, fileHandle.readInt());
		assertEquals(7, fileHandle.readInt());
		assertEquals(-2, fileHandle.readInt());
	}

	@Test
	public void testSeekForwardReadInt() throws IOException {
		fileHandle.seek(8);
		assertEquals(3, fileHandle.readInt());
		assertEquals(268435204, fileHandle.readInt());
	}

	@Test
	public void testResetReadInt() throws IOException {
		assertEquals(1, fileHandle.readInt());
		assertEquals(268435202, fileHandle.readInt());
		fileHandle.seek(0);
		assertEquals(1, fileHandle.readInt());
		assertEquals(268435202, fileHandle.readInt());
	}

	@Test
	public void testSeekBackReadInt() throws IOException {
		fileHandle.seek(16);
		fileHandle.seek(8);
		assertEquals(3, fileHandle.readInt());
		assertEquals(268435204, fileHandle.readInt());
	}

	@Test
	public void testRandomAccessReadInt() throws IOException {
		testSeekForwardReadInt();
		testSeekBackReadInt();
		// The test relies on a "new" file or reset file pointer
		fileHandle.seek(0);
		testResetReadInt();
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
