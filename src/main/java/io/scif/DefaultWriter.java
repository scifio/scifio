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

import java.io.IOException;

import net.imglib2.Interval;

/**
 * Non-functional default {@link io.scif.Writer} implementation. For use in
 * {@link io.scif.Format}s that do not need a Writer.
 *
 * @see io.scif.Writer
 * @see io.scif.Format
 * @author Mark Hiner
 */
public class DefaultWriter extends AbstractWriter<DefaultMetadata> implements
	DefaultComponent
{

	// -- Fields --

	private Format format;

	// -- HasFormat API methods --

	@Override
	public Format getFormat() {
		return format;
	}

	// -- Writer API Methods --

	/**
	 * Non-functional savePlane implementation.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected void writePlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds) throws FormatException,
		IOException
	{
		throw new UnsupportedOperationException(
			"Trying to write using DefaultWriter. " +
				"Must implement a Writer specifically for this Format");
	}

	@Override
	protected String[] makeCompressionTypes() {
		return new String[0];
	}
}
