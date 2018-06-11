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
import io.scif.services.LocationService;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.scijava.Context;

/**
 * Tests for reading bytes from a loci.common.RandomAccessInputStream.
 *
 * @see io.scif.io.RandomAccessInputStream
 */
@RunWith(Parameterized.class)
public class RandomAccessInputStreamTest {

	private static final byte[] PAGE = new byte[] { 0, 4, 8, 12, 16, 20, 24, 28,
		32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100,
		104, 108, 112, 116, 120, 124, (byte) 128, (byte) 132, (byte) 136,
		(byte) 140, (byte) 144, (byte) 148, (byte) 152, (byte) 156, (byte) 160,
		(byte) 164, (byte) 168, (byte) 172, (byte) 176, (byte) 180, (byte) 184,
		(byte) 188, (byte) 192, (byte) 196, (byte) 200, (byte) 204, (byte) 208,
		(byte) 212, (byte) 216, (byte) 220, (byte) 224, (byte) 228, (byte) 232,
		(byte) 236, (byte) 240, (byte) 244, (byte) 248, (byte) 252 };

	private static final String MODE = "r";

	private static final int BUFFER_SIZE = 2;

	private RandomAccessInputStream stream;

	private IRandomAccess fileHandle;

	private Context context;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("readTests");
	}

	private final String provider;

	private final boolean testLength;

	public RandomAccessInputStreamTest(final String provider,
		final boolean checkGrowth, final boolean testLength)
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
		context = new Context(LocationService.class);
		stream = new RandomAccessInputStream(context, fileHandle);
	}

	@Test
	public void testLength() throws IOException {
		assumeTrue(testLength);
		assertEquals(PAGE.length, stream.length());
	}

	@Test
	public void testSequentialRead() throws IOException {
		stream.seek(0);
		for (int i = 0; i < stream.length(); i++) {
			assertEquals(PAGE[i], stream.readByte());
		}
	}

	@Test
	public void testReverseSequentialRead() throws IOException {
		stream.seek(stream.length() - 1);
		for (int i = PAGE.length - 1; i >= 0; i--) {
			assertEquals(PAGE[i], stream.readByte());
			if (stream.getFilePointer() >= 2) {
				stream.seek(stream.getFilePointer() - 2);
			}
		}
	}

	@Test
	public void testArrayRead() throws IOException {
		stream.seek(0);
		final byte[] buf = new byte[PAGE.length];
		stream.read(buf);
		for (int i = 0; i < PAGE.length; i++) {
			assertEquals(PAGE[i], buf[i]);
		}
	}

	@Test
	public void testRandomRead() throws IOException {
		final long fp = PAGE.length / 2;
		stream.seek(fp);
		for (int i = 0; i < PAGE.length; i++) {
			final boolean forward = (i % 2) == 0;
			final int step = i / 2;
			if (forward) {
				stream.seek(fp + step);
			}
			else {
				stream.seek(fp - step);
			}
			assertEquals(PAGE[(int) stream.getFilePointer()], stream.readByte());
		}
	}

	@After
	public void tearDown() throws IOException {
		stream.close();
	}

}
