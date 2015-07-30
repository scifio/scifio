/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
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

package io.scif;

import java.util.List;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.Dimensions;
import net.imglib2.Interval;

/**
 * Default implementation of {@link ImageMetadata}. No added functionality over
 * {@link io.scif.AbstractImageMetadata}.
 *
 * @see io.scif.ImageMetadata
 * @see io.scif.AbstractImageMetadata
 * @author Mark Hiner
 */
public class DefaultImageMetadata extends AbstractImageMetadata {

	// -- Constructors --


	public DefaultImageMetadata(final int n) {
		super(n);
	}

	public DefaultImageMetadata(final int n, final CalibratedAxis... axes) {
		super(n, axes);
	}

	public DefaultImageMetadata(final int n, final List<CalibratedAxis> axes) {
		super(n, axes);
	}

	public DefaultImageMetadata(final Interval interval) {
		super(interval);
	}

	public DefaultImageMetadata(final Interval interval, final CalibratedAxis... axes) {
		super(interval, axes);
	}

	public DefaultImageMetadata(final Interval interval, final List<CalibratedAxis> axes) {
		super(interval, axes);
	}

	public DefaultImageMetadata(final Dimensions dimensions) {
		super(dimensions);
	}

	public DefaultImageMetadata(final Dimensions dimensions, final CalibratedAxis... axes) {
		super(dimensions, axes);
	}

	public DefaultImageMetadata(final Dimensions dimensions, final List<CalibratedAxis> axes) {
		super(dimensions, axes);
	}

	public DefaultImageMetadata(final long[] dimensions) {
		super(dimensions);
	}

	public DefaultImageMetadata(final long[] dimensions, final CalibratedAxis... axes) {
		super(dimensions, axes);
	}

	public DefaultImageMetadata(final long[] dimensions, final List<CalibratedAxis> axes) {
		super(dimensions, axes);
	}

	public DefaultImageMetadata(final long[] min, final long[] max) {
		super(min, max);
	}

	public DefaultImageMetadata(final long[] min, final long[] max,
		final CalibratedAxis... axes)
	{
		super(min, max, axes);
	}

	public DefaultImageMetadata(final long[] min, final long[] max,
		final List<CalibratedAxis> axes)
	{
		super(min, max, axes);
	}

	public DefaultImageMetadata(final ImageMetadata source) {
		super(source);
	}

	// -- ImageMetadata API Methods --

	@Override
	public ImageMetadata copy() {
		return new DefaultImageMetadata(this);
	}

}
