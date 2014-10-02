/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif.utests;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.filters.ChannelFiller;
import io.scif.filters.DimensionSwapper;
import io.scif.filters.FileStitcher;
import io.scif.filters.Filter;
import io.scif.filters.MinMaxFilter;
import io.scif.filters.PlaneSeparator;
import io.scif.filters.ReaderFilter;

import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link io.scif.Reader} interface methods.
 * 
 * @author Mark Hiner
 */
@Test(groups = "readerTests")
public class ReaderTest {

	private SCIFIO scifio;

	@BeforeMethod
	public void setUp() {
		scifio = new SCIFIO();
	}

	@AfterMethod
	public void tearDown() {
		scifio.getContext().dispose();
	}

	/**
	 * Ensure that using a single {@link Reader} instance multiple times behaves
	 * as intended.
	 */
	@Test
	public void reuseTest() throws FormatException, IOException {
		reuseFilter(null);
	}

	/**
	 * Ensure that using a single {@link Reader} instance multiple times behaves
	 * as intended when {@link Filter}s are enabled.
	 */
	@Test
	public void reuseTestFilters() throws FormatException, IOException {
		// Test each Readerfilter individually
//		reuseFilter(ChannelFiller.class);
//		reuseFilter(PlaneSeparator.class);
		reuseFilter(DimensionSwapper.class);
//		reuseFilter(FileStitcher.class);
//		reuseFilter(MinMaxFilter.class);
	}

	// -- Helper methods --

	private void reuseFilter(final Class<? extends Filter> filterClass)
		throws FormatException, IOException
	{
		final String id1 =
			"8bit-signed&pixelType=int8&axes=X,Y,Time&lengths=50,50,7.fake";
		final String id2 =
			"8bit-signed&pixelType=int8&axes=X,Y,Z,Channel&lengths=50,50,7,3.fake";

		final ReaderFilter reader = scifio.initializer().initializeReader(id1);

		// enable the filter if specified
		if (filterClass != null) reader.enable(filterClass);

		final byte[] bytes1 = reader.openPlane(0, 0).getBytes();

		// Test reader.setSource
		reader.setSource(id2);
		byte[] bytes2 = reader.openPlane(0, 0).getBytes();

		assertFalse(Arrays.equals(bytes1, bytes2));

		// Test reader.setMetadata
		final Metadata meta = scifio.initializer().parseMetadata(id1);
		reader.setMetadata(meta);
		bytes2 = reader.openPlane(0, 0).getBytes();

		assertTrue(Arrays.equals(bytes1, bytes2));
	}
}
