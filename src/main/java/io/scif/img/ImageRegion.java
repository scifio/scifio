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

import java.util.HashMap;
import java.util.Map;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

/**
 * A list of dimensional ranges. Each range is associated with a particular
 * {@link AxisType}, and is used to restrict the indices that are read in the
 * corresponding planes.
 * <p>
 * NB: Subregions must be continuous in the {@link Axes#X} and {@link Axes#Y}
 * dimensions. Discontinuous tiles require multiple openings.
 * </p>
 *
 * @author Mark Hiner
 */
public class ImageRegion {

	// -- Fields --

	private final Map<AxisType, Range> dimRanges;

	// -- Constructors --

	public ImageRegion(final AxisType[] axes, final String[] ranges) {
		dimRanges = new HashMap<>();

		if (axes.length != ranges.length) throw new IllegalArgumentException(
			"Number of axes: " + axes.length + " does not match number of ranges: " +
				ranges.length);

		for (int i = 0; i < axes.length; i++) {
			addRange(axes[i], ranges[i]);
		}
	}

	public ImageRegion(final AxisType[] axes, final Range... ranges) {
		dimRanges = new HashMap<>();

		if (axes.length != ranges.length) throw new IllegalArgumentException(
			"Number of axes: " + axes.length + " does not match number of ranges: " +
				ranges.length);

		for (int i = 0; i < axes.length; i++) {
			dimRanges.put(axes[i], ranges[i]);
		}
	}

	public ImageRegion(final Map<AxisType, Range> ranges) {
		dimRanges = ranges;
	}

	// -- SubRegion methods --

	/**
	 * @param range Dimensional range to add to this SubRegion
	 */
	public void addRange(final AxisType axis, final String range) {
		dimRanges.put(axis, new Range(range));
	}

	/**
	 * @return A list of indices for the specified dimension
	 */
	public Range getRange(final AxisType axisType) {
		return dimRanges.get(axisType);
	}

	/**
	 * @return True if this SubRegion contains a range for the specified AxisType
	 */
	public boolean hasRange(final AxisType axisType) {
		return dimRanges.get(axisType) != null;
	}

	/**
	 * @return Number of ranges in this SubRegion
	 */
	public int size() {
		return dimRanges.size();
	}

}
