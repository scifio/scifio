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

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;

/**
 * Default {@link io.scif.Parser} implementation.
 * <p>
 * Populates the following fields:
 * </p>
 * <ul>
 * <li>{@link io.scif.Metadata#isFiltered()}</li>
 * <li>{@link io.scif.Metadata#getSource()}</li>
 * <li>{@link io.scif.Metadata#getDatasetName()}</li>
 * </ul>
 *
 * @see io.scif.Parser
 * @author Mark Hiner
 */
public class DefaultParser extends AbstractParser<DefaultMetadata> implements
	DefaultComponent
{

	// -- Fields --

	private Format format;

	// -- HasFormatAPI Methods --

	@Override
	public Format getFormat() {
		return format;
	}

	// -- AbstractParser API Methods --

	/*
	 * Non-functional typedParse implementation.
	 */
	@Override
	protected void typedParse(final DataHandle<Location> stream,
		final DefaultMetadata meta, final SCIFIOConfig config) throws IOException,
		FormatException
	{
		// No implementation
	}
}
