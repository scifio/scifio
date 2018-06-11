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

package io.scif;

import io.scif.util.FormatTools;

import net.imglib2.Interval;

import org.scijava.Context;
import org.scijava.util.ArrayUtils;

/**
 * A naive {@link io.scif.Plane} implementation that uses {@code byte[]} for its
 * underlying data type.
 *
 * @see io.scif.Plane
 * @see io.scif.DataPlane
 * @author Mark Hiner
 */
public class ByteArrayPlane extends AbstractPlane<byte[], ByteArrayPlane> {

	// -- Constructor --

	public ByteArrayPlane(final Context context) {
		super(context);
	}

	public ByteArrayPlane(final Context context, final ImageMetadata meta,
		final Interval bounds)
	{
		super(context, meta, bounds);
	}

	// -- Plane API methods --

	@Override
	public byte[] getBytes() {
		return getData();
	}

	// -- AbstractPlane API --

	@Override
	protected byte[] blankPlane(final Interval bounds) {
		byte[] buf = null;

		final long[] sizes = new long[bounds.numDimensions() + 1];
		for (int i = 0; i < sizes.length - 1; i++) {
			sizes[i] = bounds.dimension(i);
		}
		sizes[sizes.length - 1] = FormatTools.getBytesPerPixel(getImageMetadata()
			.getPixelType());

		buf = ArrayUtils.allocate(sizes);
		return buf;
	}
}
