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
 * Interface for all {@link io.scif.Parser} implementations that use generic
 * parameters.
 * <p>
 * Generics allow each concrete {@code Parser} implementation to type narrow the
 * return the type of {@code Metadata} from its {@link #parse} methods, as well
 * as the argument {@code Metadata} types for the same methods.
 * </p>
 *
 * @author Mark Hiner
 * @param <M> The {@link io.scif.Metadata} type that will be returned by this
 *          {@code Parser}.
 */
public interface TypedParser<M extends TypedMetadata> extends Parser {

	@Override
	M parse(Location loc) throws IOException, FormatException;

	@Override
	M parse(DataHandle<Location> handle) throws IOException, FormatException;

	@Override
	M parse(Location loc, SCIFIOConfig config) throws IOException,
		FormatException;

	@Override
	M parse(DataHandle<Location> handle, SCIFIOConfig config) throws IOException,
		FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(DataHandle, Metadata)}.
	 *
	 * @see io.scif.Parser#parse(DataHandle, Metadata)
	 */
	M parse(DataHandle<Location> stream, M meta) throws IOException,
		FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(Location, Metadata)}.
	 *
	 * @see io.scif.Parser#parse(Location, Metadata, SCIFIOConfig)
	 */
	M parse(Location fileName, M meta, SCIFIOConfig config) throws IOException,
		FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(DataHandle, Metadata)}.
	 *
	 * @see io.scif.Parser#parse(DataHandle, Metadata, SCIFIOConfig)
	 */
	M parse(DataHandle<Location> stream, M meta, SCIFIOConfig config)
		throws IOException, FormatException;
}
