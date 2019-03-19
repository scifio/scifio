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

package io.scif.img;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A list of {@link Long} values with extra convenience functionality. Values
 * can be parsed from a properly formatted string.
 * <p>
 * A {@link #head()} and {@link #tail()} convenience method are provided.
 * Additionally, a hash index is maintained over all values, for faster
 * {@link #contains(Object)} performance.
 * </p>
 * <p>
 * Practically, ranges are ","-separated list of selected values. For each
 * selection specified, a start value is mandatory. An (inclusive) end value can
 * be specified using a "-". If an end is specified, a step can also be provided
 * using a ":". So, {@code 5,6-10:2} would select planes 5, 6, 8 and 10 from the
 * dimension associated with this Range.
 * </p>
 * <p>
 * Ranges must match the pattern: {@code \d*(-\d*(:\d*)?)?(,\d*(-\d*(:\d)?*)?)*}
 * Valid patterns:
 * </p>
 * <ul>
 * <li>5,10,15</li>
 * <li>5-10,7,10-25:3</li>
 * <li>1-100:1,1-50:2,15-20</li>
 * </ul>
 * <p>
 * NB: index order is preserved, so if indices are provided out of order, they
 * will remain out of order.
 * </p>
 *
 * @author Mark Hiner
 */
public class Range extends ArrayList<Long> {

	// -- Constants --

	private static final String REGION_PATTERN =
		"\\d*(-\\d*(:\\d*)?)?(,\\d*(-\\d*(:\\d)?)?)*";

	// -- Fields --

	// A set index is maintained to allow fast lookup for contains checks
	private final Set<Long> setIndex;

	// -- Constructors --

	private Range() {
		setIndex = new HashSet<>();
	}

	public Range(final String range) {
		this();

		// Check for invalid patterns
		if (!range.matches(REGION_PATTERN)) {
			throw new IllegalArgumentException("Invalid range pattern. Must match: " +
				REGION_PATTERN);
		}

		final String[] intervals = range.split(",");

		// Iterate over each axis of the region and extract its constraints
		for (final String interval : intervals) {
			final String[] rangeTokens = interval.split("-");

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
				add(j);
			}
		}
	}

	/**
	 * Creates a singleton DimRange.
	 *
	 * @param index single index for this DimRange.
	 */
	public Range(final Long index) {
		this();

		setIndex.add(index);
		add(index);
	}

	/**
	 * Creates the DimRange: [start, end]
	 *
	 * @param start inclusive start value
	 * @param end inclusive end value
	 */
	public Range(final Long start, final Long end) {
		this();

		for (long l = start; l <= end; l++) {
			setIndex.add(l);
			add(l);
		}

	}

	/**
	 * Constructs a DimRange that includes only the values contained in the
	 * provided array.
	 *
	 * @param values explicit list of values in this range
	 */
	public Range(final long[] values) {
		this();

		for (final long l : values) {
			setIndex.add(l);
			add(l);
		}
	}

	// -- DimensionRanges methods --

	@Override
	public boolean contains(final Object l) {
		return setIndex.contains(l);
	}

	/**
	 * @return The first value in this range.
	 */
	public Long head() {
		return get(0);
	}

	/**
	 * @return The last value in this range.
	 */
	public Long tail() {
		return get(size() - 1);
	}
}
