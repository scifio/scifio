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

package io.scif.filters;

import static org.junit.Assert.assertEquals;

import io.scif.FormatException;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import org.junit.Test;
import org.scijava.InstantiableException;
import org.scijava.io.location.Location;

/**
 * Tests for {@link PlaneSeparator}.
 *
 * @author Mark Hiner
 */
public class PlaneSeparatorTest {

	private final SCIFIO scifio = new SCIFIO();

	private final Location id = TestImgLocation.builder().lengths(3, 4, 512, 512)
		.axes("Channel", "Time", "X", "Y").build();

	/**
	 * Verify that multiple interleaved axes are automatically extracted.
	 *
	 * @throws InstantiableException
	 */
	@Test
	public void testMultipleInterleavedAxes() throws FormatException, IOException,
		InstantiableException
	{
		final Reader filter = scifio.initializer().initializeReader(id);
		// Verify that we are starting with 4 planar axes, 2 of which are
		// interleaved
		assertEquals(4, filter.getMetadata().get(0).getPlanarAxisCount());
		assertEquals(2, filter.getMetadata().get(0).getInterleavedAxisCount());
		assertEquals(0, filter.getMetadata().get(0).getAxesNonPlanar().size());

		((ReaderFilter) filter).enable(PlaneSeparator.class);
		// Verify that, after enabling the PlaneSeparator, the default behavior
		// separates out the 2 interleaved axes to non-planar axes.
		assertEquals(2, filter.getMetadata().get(0).getPlanarAxisCount());
		assertEquals(0, filter.getMetadata().get(0).getInterleavedAxisCount());
		assertEquals(2, filter.getMetadata().get(0).getAxesNonPlanar().size());
	}
}
