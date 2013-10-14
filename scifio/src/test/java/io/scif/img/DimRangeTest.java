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

package io.scif.img;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

/** Tests {@link DimRange}. */
public class DimRangeTest {

	/** Tests {@link DimRange#indices()}. */
	@Test
	public void testIndices() {
		// test single value
		assertRange(new DimRange("17"), 17);

		// test single range, default step
		assertRange(new DimRange("1-5"), 1, 2, 3, 4, 5);
		
		// test single range, explicit step where max is in range
		assertRange(new DimRange("5-15:5"), 5, 10, 15);

		// test single range, explicit step where max is not in range
		assertRange(new DimRange("3-10:2"), 3, 5, 7, 9);

		// test list of single values (descending order should be preserved too)
		assertRange(new DimRange("3,2,1"), 3, 2, 1);

		// test pair of ranges
		assertRange(new DimRange("7-8,4-6"), 7, 8, 4, 5, 6);

		// test mixed list of ranges and values
		assertRange(new DimRange("1-3,5,8,13"), 1, 2, 3, 5, 8, 13);

		// test range where min and max are equal and step is superfluous
		assertRange(new DimRange("0-0:1"), 0);

		// test range where min is greater than max (has no elements in range)
		assertRange(new DimRange("3-1")); // min > max is invalid
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPattern() {
		new DimRange("3,2,1,blastoff!");
	}

	private void
		assertRange(DimRange range, long... indices)
	{
		final List<Long> rangeIndices = range.indices();
		assertNotNull(rangeIndices);
		assertEquals(indices.length, range.indices().size());
		for (int i=0; i<indices.length; i++) {
			assertEquals(indices[i], rangeIndices.get(i).longValue());
		}
	}

}
