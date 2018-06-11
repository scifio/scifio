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

import io.scif.config.SCIFIOConfig;

import java.io.IOException;

import net.imglib2.Interval;

/**
 * Non-functional default {@link io.scif.Reader} implementation. For use in
 * {@link io.scif.Format}s that do not need a Reader.
 *
 * @see io.scif.Reader
 * @see io.scif.Format
 * @author Mark Hiner
 */
public class DefaultReader extends ByteArrayReader<DefaultMetadata> implements
	DefaultComponent
{

	// -- Fields --

	private Format format;

	// -- HasFormat API Methods --

	@Override
	public Format getFormat() {
		return format;
	}

	// -- AbstractReader API Methods --

	@Override
	protected String[] createDomainArray() {
		return new String[0];
	}

	// -- Reader API Methods --

	/**
	 * Non-functional openPlane implementation.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
		final ByteArrayPlane plane, final Interval bounds,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		throw new UnsupportedOperationException(
			"Trying to read using DefaultReader. " +
				"Must implement a Reader specifically for this Format");
	}
}
