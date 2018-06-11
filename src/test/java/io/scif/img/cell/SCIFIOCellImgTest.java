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

package io.scif.img.cell;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.scif.img.IO;
import io.scif.img.SCIFIOImgPlus;
import io.scif.io.TestParameters;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the {@link SCIFIOCellImg} and related classes.
 *
 * @author Mark Hiner
 */
@RunWith(Parameterized.class)
public class SCIFIOCellImgTest {

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("cellTests");
	}

	private final String provider;

	public SCIFIOCellImgTest(final String provider, final boolean checkGrowth,
		final boolean testLength)
	{
		this.provider = provider;
	}

	/**
	 * Test that when a {@link SCIFIOCellImg} is opened and disposed, the
	 * associated reader is closed.
	 */
	@Test
	public void testReaderCleanup() {
		// Make an id that will trigger cell creation
		final String id = "lotsofplanes&axes=X,Y,Z&lengths=256,256,100000.fake";
		final SCIFIOImgPlus<?> img = IO.open(id);
		assertNotNull(((SCIFIOCellImg) img.getImg()).reader().getMetadata());
		img.dispose();
		assertNull(((SCIFIOCellImg) img.getImg()).reader().getMetadata());
	}

	// This test is currently disabled because it fails for unknown reasons.
	// It passes from Eclipse, it passes from Maven on the command line, but it
	// fails when run by Jenkins using Maven.
	// We're testing the dispose behavior above, the purpose of this test is
	// just
	// to confirm that disposal occurs when an ImgPlus is garbage collected.
	// Unfortunately, this will have to wait until we have a better
	// understanding
	// of how to test post-GC events.
//	/**
//	 * Test that when a {@link SCIFIOCellImg} is opened and goes out of scope, the
//	 * associated reader is closed.
//	 */
//	@Test
//	public void testReaderOutOfScopeCleanup() {
//		// Make an id that will trigger cell creation
//		final String id = "lotsofplanes&axes=X,Y,Z&lengths=256,256,100000.fake";
//		SCIFIOImgPlus<?> img = IO.open(id);
//		assertNotNull(((SCIFIOCellImg) img.getImg()).reader().getMetadata());
//		final WeakReference<Metadata> wr =
//			new WeakReference<Metadata>(((SCIFIOCellImg) img.getImg()).reader()
//				.getMetadata());
//		img = null;
//		long arraySize = MemoryTools.totalAvailableMemory();
//		if (arraySize > Integer.MAX_VALUE) {
//			arraySize = Integer.MAX_VALUE;
//		}
//		final long maxCounts =
//			Math.round(Math.max(100,
//				(2 * Math.ceil(((double)Runtime.getRuntime().maxMemory() / arraySize)))));
//		for (int i = 0; i < maxCounts &&
//			wr.get() != null; i++)
//		{
//			final byte[] tmp = new byte[(int) arraySize];
//		}
//		assertNull(wr.get());
//	}
}
