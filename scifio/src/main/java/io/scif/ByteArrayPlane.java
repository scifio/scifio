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

package io.scif;

import io.scif.common.DataTools;
import io.scif.util.FormatTools;

import org.scijava.Context;

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
		final long[] planeOffsets, final long[] planeLengths)
	{
		super(context, meta, planeOffsets, planeLengths);
	}

	// -- Plane API methods --

	@Override
	public byte[] getBytes() {
		return getData();
	}

	// -- AbstractPlane API --

	@Override
	protected byte[] blankPlane(long[] planeOffsets, long[] planeBounds) {
		byte[] buf = null;

		final long[] lengths = new long[planeOffsets.length + 1];
		for (int i = 0; i < lengths.length - 1; i++) {
			lengths[i] = planeBounds[i];
		}
		lengths[lengths.length - 1] =
			FormatTools.getBytesPerPixel(getImageMetadata().getPixelType());

		buf = DataTools.allocate(lengths);
		return buf;
	}
}
