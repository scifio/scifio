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
import io.scif.util.FormatTools;

import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Checker} implementations.
 *
 * @see io.scif.Checker
 * @see io.scif.HasFormat
 * @author Mark Hiner
 */
public abstract class AbstractChecker extends AbstractHasFormat implements
	Checker
{

	@Parameter
	private DataHandleService handles;

	// -- Checker API Methods --

	@Override
	public boolean suffixNecessary() {
		return true;
	}

	@Override
	public boolean suffixSufficient() {
		return true;
	}

	@Override
	public boolean isFormat(final Location loc) {
		return isFormat(loc, new SCIFIOConfig());
	}

	@Override
	public boolean isFormat(final Location loc, final SCIFIOConfig config) {
		final boolean open = config.checkerIsOpen();

		// if file extension ID is insufficient and we can't open the file, give
		// up
		if (!suffixSufficient() && !open) return false;

		if (suffixNecessary() || suffixSufficient()) {
			// it's worth checking the file extension
			final boolean suffixMatch = FormatTools.checkSuffix(loc.getName(),
				getFormat().getSuffixes());

			// if suffix match is required but it doesn't match, failure
			if (suffixNecessary() && !suffixMatch) return false;

			// if suffix matches and that's all we need, green light it
			if (suffixMatch && suffixSufficient()) return true;
		}

		// suffix matching was inconclusive; we need to analyze the file
		// contents
		if (!open) return false; // not allowed to open any files
		try (DataHandle<Location> handle = handles.create(loc)) {
			if (handle == null) return false;
			return isFormat(handle);
		}
		catch (final IOException exc) {
			log().debug("", exc);
			return false;
		}
	}

	@Override
	public boolean isFormat(final DataHandle<Location> stream)
		throws IOException
	{
		return false;
	}

	@Override
	public boolean checkHeader(final byte[] block) {
		final BytesLocation loc = new BytesLocation(block);
		try (DataHandle<Location> handle = handles.create(loc)) {
			return isFormat(handle);
		}
		catch (final IOException e) {
			log().debug("", e);
		}
		return false;
	}
}
