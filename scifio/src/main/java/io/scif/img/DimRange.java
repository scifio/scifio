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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A list of indices for a particular dimension, and is parsed from a properly
 * formatted string. Ranges must match the pattern:
 * [0-9]*(-[0-9]*(:[0-9]*)?)?(,[0-9]*(-[0-9]*(:[0-9])?*)?)*?
 * <p>
 * Practically, ranges are ","-separated list of selected values. For each
 * selection specified, a start value is mandatory. An (inclusive) end value can
 * be specified using a "-". If an end is specified, a step can also be provided
 * using a ":". So, {@code 5,6-10:2} would select planes 5, 6, 8 and 10 from the
 * dimension associated with this DimRange.
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
 * NB: index order is preserved, so if indices are provided out of order, they
 * will remain out of order.
 * </p>
 * 
 * @author Mark Hiner
 */
public class DimRange {

	// -- Constants --

	private static final String REGION_PATTERN =
		"[\\d]*(-[\\d]*(:[\\d]*)?)?(,[\\d]*(-[\\d]*(:[\\d])?)?)*?";

	// -- Fields --

	// Two indices are maintained over the data. A set to allow fast lookup
	// for contains checks, and a list to provide a consistent guaranteed order.
	private final Set<Long> setIndex;
	private final List<Long> listIndex;

	// Head and tail values for this range
	private Long head;
	private Long tail;

	// -- Constructors --

	public DimRange(final String range) {

		// Check for invalid patterns
		if (!range.matches(REGION_PATTERN)) throw new IllegalArgumentException(
			"Invalid range pattern. Must match: " + REGION_PATTERN);

		setIndex = new HashSet<Long>();
		listIndex = new ArrayList<Long>();

		final String[] intervals = range.split(",");

		// Iterate over each axis of the region and extract its constraints
		for (int i = 0; i < intervals.length; i++) {
			final String[] rangeTokens = intervals[i].split("-");

			final long start = Long.parseLong(rangeTokens[0]);
			long end = start;
			long step = 1;

			if (rangeTokens.length == 2) {
				final String[] rangeTail = rangeTokens[1].split(":");
				end = Long.parseLong(rangeTail[0]);

				if (rangeTail.length == 2) step = Long.parseLong(rangeTail[1]);
			}

			for (long j = start; j <= end; j += step) {
				setIndex.add(j);
				listIndex.add(j);
			}

			if (i == 0) head = start;
			if (i == intervals.length - 1) tail = end;
		}
	}

	/**
	 * Creates a singleton DimRange. 
	 * 
	 * @param index single index for this DimRange.
	 */
	public DimRange(final Long index) {
		head = index;
		tail = index;

		setIndex = new HashSet<Long>();
		listIndex = new ArrayList<Long>();

		setIndex.add(index);
		listIndex.add(index);
	}

	/**
	 * Creates the DimRange: [start, end]
	 * 
	 * @param start inclusive start value
	 * @param end inclusive end value
	 */
	public DimRange(final Long start, final Long end) {
		head = start;
		tail = end;

		setIndex = new HashSet<Long>();
		listIndex = new ArrayList<Long>();

		for (long l = start; l <= end; l++) {
			setIndex.add(l);
			listIndex.add(l);
		}
	}

	// -- DimensionRanges methods --
	
	/**
	 * @return a list index over the values in this range
	 */
	public List<Long> indices() {
		return listIndex;
	}
	
	public boolean contains(Long l) {
		return setIndex.contains(l);
	}

	/**
	 * @return The first value in this range.
	 */
	public Long head() {
		return head;
	}

	/**
	 * @return The last value in this range.
	 */
	public Long tail() {
		return tail;
	}
}
