/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.filters.utests;

import static org.testng.AssertJUnit.assertEquals;
import io.scif.FormatException;
import io.scif.SCIFIO;
import io.scif.filters.MinMaxFilter;
import io.scif.filters.ReaderFilter;

import java.io.IOException;

import net.imglib2.meta.Axes;

import org.testng.annotations.Test;

/**
 * Tests for {@link MinMaxFilter}.
 * 
 * @author Mark Hiner
 */
public class MinMaxFilterTest {

	private final SCIFIO scifio = new SCIFIO();
	private final String id =
		"testImg&lengths=3,127,127,4&axes=Channel,X,Y,Time&planarDims=3.fake";

	@Test
	public void testMinMax() throws FormatException, IOException {
		ReaderFilter filter = scifio.initializer().initializeReader(id);

		MinMaxFilter minMax = filter.enable(MinMaxFilter.class);

		// open a plane to trigger min/max computation
		filter.openPlane(0, 1);

		// Check known axis min/maxes
		assertEquals(126.0, minMax.getAxisKnownMaximum(0, Axes.CHANNEL, 0));
		assertEquals(126.0, minMax.getAxisKnownMaximum(0, Axes.CHANNEL, 1));
		assertEquals(126.0, minMax.getAxisKnownMaximum(0, Axes.CHANNEL, 2));
		assertEquals(0.0, minMax.getAxisKnownMinimum(0, Axes.CHANNEL, 0));
		assertEquals(0.0, minMax.getAxisKnownMinimum(0, Axes.CHANNEL, 1));
		assertEquals(0.0, minMax.getAxisKnownMinimum(0, Axes.CHANNEL, 2));

		// Check plane min/maxes for opened plane
		assertEquals(126.0, minMax.getPlaneMaximum(0, 1));
		assertEquals(0.0, minMax.getPlaneMinimum(0, 1));

		// Check plane min/maxes for unopened plane - should be null
		assertEquals(null, minMax.getPlaneMaximum(0, 2));
		assertEquals(null, minMax.getPlaneMinimum(0, 2));

		// Check global axis min/maxes - should be null, as not all planes have been
		// read
		assertEquals(null, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 0));
		assertEquals(null, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 1));
		assertEquals(null, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 2));
		assertEquals(null, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 0));
		assertEquals(null, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 1));
		assertEquals(null, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 2));
		
		// open all planes
		for (int i=0; i<minMax.getPlaneCount(0); i++) {
			filter.openPlane(0, i);
		}

		// Check global axis min/maxes
		// should be populated, as all planes have been read
		assertEquals(126.0, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 0));
		assertEquals(126.0, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 1));
		assertEquals(126.0, minMax.getAxisGlobalMaximum(0, Axes.CHANNEL, 2));
		assertEquals(0.0, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 0));
		assertEquals(0.0, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 1));
		assertEquals(0.0, minMax.getAxisGlobalMinimum(0, Axes.CHANNEL, 2));
	}
}
