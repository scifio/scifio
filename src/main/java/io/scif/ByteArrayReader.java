/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
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

import io.scif.util.FormatTools;

import java.io.IOException;

import net.imagej.axis.Axes;

/**
 * Abstract superclass for all {@link io.scif.Reader} implementations that
 * return a {@link io.scif.ByteArrayPlane} when reading datasets.
 *
 * @see io.scif.Reader
 * @see io.scif.ByteArrayPlane
 * @author Mark Hiner
 * @param <M> - The Metadata type required by this Reader.
 */
public abstract class ByteArrayReader<M extends TypedMetadata> extends
	AbstractReader<M, ByteArrayPlane>
{

	// -- Constructor --

	public ByteArrayReader() {
		super(ByteArrayPlane.class);
	}

	// -- Reader API Methods --

	@Override
	public ByteArrayPlane openThumbPlane(final int imageIndex,
		final long planeIndex) throws FormatException, IOException
	{
		FormatTools.assertStream(getStream(), true, 1);
		final Metadata meta = getMetadata();
		final long[] planeBounds = meta.get(imageIndex).getAxesLengthsPlanar();
		final long[] planeOffsets = new long[planeBounds.length];

		planeBounds[meta.get(imageIndex).getAxisIndex(Axes.X)] =
			meta.get(imageIndex).getThumbSizeX();
		planeBounds[meta.get(imageIndex).getAxisIndex(Axes.Y)] =
			meta.get(imageIndex).getThumbSizeX();

		final ByteArrayPlane plane = createPlane(planeOffsets, planeBounds);

		plane.setData(FormatTools.openThumbBytes(this, imageIndex, planeIndex));

		return plane;
	}

	@Override
	public ByteArrayPlane createPlane(final long[] planeOffsets,
		final long[] planeBounds)
	{
		return createPlane(getMetadata().get(0), planeOffsets, planeBounds);
	}

	@Override
	public ByteArrayPlane createPlane(final ImageMetadata meta,
		final long[] planeOffsets, final long[] planeBounds)
	{
		return new ByteArrayPlane(getContext(), meta, planeOffsets, planeBounds);
	}

}
