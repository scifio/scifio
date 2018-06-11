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
import java.nio.ByteOrder;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests swapping the endianness of a loci.common.IRandomAccess.
 *
 * @see io.scif.io.IRandomAccess
 */
@RunWith(Parameterized.class)
public class EndiannessTest {

	private static final byte[] PAGE = new byte[] { (byte) 0x0F, (byte) 0x0E,
		(byte) 0x0F, (byte) 0x0E, (byte) 0x0F, (byte) 0x0E, (byte) 0x0F,
		(byte) 0x0E };

	private static final String MODE = "r";

	private static final int BUFFER_SIZE = 4;

	private IRandomAccess fileHandle;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("readTests");
	}

	private final String provider;

	private final boolean testLength;

	public EndiannessTest(final String provider, final boolean checkGrowth,
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
		assertEquals(8, fileHandle.length());
	}

	@Test
	public void testSeekBigEndian() throws IOException {
		fileHandle.seek(6);
		assertEquals(3854, fileHandle.readShort());
	}

	@Test
	public void testSeekLittleEndian() throws IOException {
		fileHandle.setOrder(ByteOrder.LITTLE_ENDIAN);
		fileHandle.seek(6);
		assertEquals(3599, fileHandle.readShort());
	}

	@Test
	public void testReadShortBigEndian() throws IOException {
		assertEquals(3854, fileHandle.readShort());
	}

	@Test
	public void testReadShortLittleEndian() throws IOException {
		fileHandle.setOrder(ByteOrder.LITTLE_ENDIAN);
		assertEquals(3599, fileHandle.readShort());
	}

	@Test
	public void testReadIntBigEndian() throws IOException {
		assertEquals(252579598, fileHandle.readInt());
	}

	@Test
	public void testReadIntLittleEndian() throws IOException {
		fileHandle.setOrder(ByteOrder.LITTLE_ENDIAN);
		assertEquals(235867663, fileHandle.readInt());
	}

	@Test
	public void testReadLongBigEndian() throws IOException {
		assertEquals(1084821113299406606L, fileHandle.readLong());
	}

	@Test
	public void testReadLongLittleEndian() throws IOException {
		fileHandle.setOrder(ByteOrder.LITTLE_ENDIAN);
		assertEquals(1013043899004816911L, fileHandle.readLong());
	}

	@After
	public void tearDown() throws IOException {
		fileHandle.close();
	}
}
