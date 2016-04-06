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

package io.scif.gui;

import io.scif.AbstractReader;
import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.TypedMetadata;
import io.scif.util.FormatTools;

import java.io.IOException;

/**
 * BufferedImageReader is the superclass for file format readers that use
 * java.awt.image.BufferedImage as the native data type.
 *
 * @author Curtis Rueden
 */
public abstract class BufferedImageReader<M extends TypedMetadata> extends
	AbstractReader<M, BufferedImagePlane>
{

	// -- Constructors --

	/** Constructs a new BIFormatReader. */
	public BufferedImageReader() {
		super(BufferedImagePlane.class);
	}

	// -- Reader API Methods --

	@Override
	public BufferedImagePlane openThumbPlane(final int imageIndex,
		final long planeIndex) throws FormatException, IOException
	{
		FormatTools.assertStream(getStream(), true, 1);
		final Metadata meta = getMetadata();
		final long[] planeBounds = meta.get(imageIndex).getAxesLengthsPlanar();
		final long[] planeOffsets = new long[planeBounds.length];

		final BufferedImagePlane plane = createPlane(planeOffsets, planeBounds);

		plane.setData(AWTImageTools.openThumbImage(
			openPlane(imageIndex, planeIndex), this, imageIndex, planeBounds,
			(int) meta.get(imageIndex).getThumbSizeX(), (int) meta.get(imageIndex)
				.getThumbSizeY(), false));

		return plane;
	}

	@Override
	public BufferedImagePlane createPlane(final long[] planeOffsets,
		final long[] planeBounds)
	{
		return createPlane(getMetadata().get(0), planeOffsets, planeBounds);
	}

	@Override
	public BufferedImagePlane createPlane(final ImageMetadata meta,
		final long[] planeOffsets, final long[] planeBounds)
	{
		return new BufferedImagePlane(getContext(), meta, planeOffsets, planeBounds);
	}
}
