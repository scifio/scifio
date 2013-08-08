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

import java.util.ArrayList;
import java.util.List;

/**
 * A list of dimensional ranges. Each range is assumed to be in dimensional
 * order, and is parsed from a properly formatted string. Ranges must match the
 * pattern: [0-9]*(-[0-9]*(:[0-9]*)?)?(,[0-9]*(-[0-9]*(:[0-9])?*)?)*?
 * <p>
 * Practically, ranges are ","-separated list of selected values. For each
 * selection specified, a start value is mandatory. An end value can be
 * specified using a "-". If an end is specified, a step can also be provided
 * using a ":". So, {@code 5,6-10:2} would select planes 5, 6, 8 and 10 from
 * that dimension.
 * <p>
 * Valid patterns:
 * <ul>
 * <li>5,10,15</li>
 * <li>5-10,7,10-25:3</li>
 * <li>1-100:1,1-50:2,15-20</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * NB: Subregions must be continuous in the X and Y dimensions. Discontinuous
 * tiles require multiple openings.
 * </p>
 * 
 * @author Mark Hiner
 */
public class SubRegion {

	// -- Fields --

	private List<DimRange> dimRanges;

	// -- Constructor --

	public SubRegion(final String... ranges) {
		dimRanges = new ArrayList<DimRange>();

		for (String range : ranges)
			addRange(range);
	}

	/**
	 * @param range Dimensional range to add to this SubRegion
	 */
	public void addRange(String range) {
		dimRanges.add(new DimRange(range));
	}

	/**
	 * @return A list of indices for the specified dimension
	 */
	public List<Long> indices(int dim) {
		return dimRanges.get(dim).indices();
	}
	
	/**
	 * @return Number of ranges in this SubRegion
	 */
	public int size() {
		return dimRanges.size();
	}

	private static class DimRange {

		// -- Constants --

		private static final String REGION_PATTERN =
			"[\\d]*(-[\\d]*(:[\\d]*)?)?(,[\\d]*(-[\\d]*(:[\\d])?)?)*?";

		// -- Fields --

		private List<Long> indices;

		// -- Constructor --

		public DimRange(final String range) {

			// Check for invalid patterns
			if (!range.matches(REGION_PATTERN)) throw new IllegalArgumentException(
				"Invalid range pattern. Must match: " + REGION_PATTERN);
			
			indices = new ArrayList<Long>();

			final String[] intervals = range.split(",");

			// Iterate over each axis of the region and extract its constraints
			for (int i = 0; i < intervals.length; i++) {
				final String[] rangeTokens = intervals[i].split("-");

				long start = Long.parseLong(rangeTokens[0]);
				long end = start;
				long step = 1;

				if (rangeTokens.length == 2) {
					final String[] rangeTail = rangeTokens[1].split(":");
					end = Long.parseLong(rangeTail[0]);

					if (rangeTail.length == 2) step = Long.parseLong(rangeTail[1]);
				}

				for (long j = start; j <= end; j += step) {
					indices.add(j);
				}
			}
		}

		// -- DimensionRanges methods --

		/**
		 * @return start value for the ith dimension
		 */
		public List<Long> indices() {
			return indices;
		}
	}
}
