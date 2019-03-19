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

package io.scif.services;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIOService;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;

import java.io.IOException;

import org.scijava.io.location.Location;

/**
 * A collection of methods for initializing the IO components of SCIFIO (Readers
 * and Writers). All parsing and Metadata setup is done automatically.
 * <p>
 * NB: The {@link #initializeReader} line of methods return a
 * {@link io.scif.filters.ReaderFilter} instead of a basic Reader. This is a
 * convenience to allow filters to be enabled if desired.
 * </p>
 *
 * @see io.scif.Reader
 * @see io.scif.Writer
 * @see io.scif.filters.ReaderFilter
 * @author Mark Hiner
 */
public interface InitializeService extends SCIFIOService {

	/**
	 * See {@link #initializeReader(Location, SCIFIOConfig)}. Will not open the
	 * image source while parsing metadata.
	 *
	 * @param id Name of the image source to be read.
	 * @return An initialized {@code Reader}.
	 */
	ReaderFilter initializeReader(Location id) throws FormatException,
		IOException;

	/**
	 * Convenience method for creating a {@code Reader} component that is ready to
	 * open planes of the provided image source. The reader's {@code Metadata} and
	 * source fields will be populated.
	 *
	 * @param id Name of the image source to be read.
	 * @param config Configuration for this method execution.
	 * @return An initialized {@code Reader}.
	 */
	ReaderFilter initializeReader(Location id, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * See {@link #initializeWriter(Location, Location, SCIFIOConfig)}. Will not
	 * open the image source while parsing metadata.
	 *
	 * @param source Name of the image source to use for parsing metadata.
	 * @param destination Name of the writing destination.
	 * @return An initialized {@code Writer}.
	 */
	Writer initializeWriter(Location source, Location destination)
		throws FormatException, IOException;

	/**
	 * As {@link #initializeWriter(Location, Location)} with configuration
	 * options.
	 *
	 * @param source Name of the image source to use for parsing metadata.
	 * @param destination Name of the writing destination.
	 * @param config Configuration for this method execution.
	 * @return An initialized {@code Writer}.
	 */
	Writer initializeWriter(Location source, Location destination,
		SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * See {@link #initializeWriter(Location, Location, SCIFIOConfig)}. Will not
	 * open the image source while parsing metadata.
	 *
	 * @param sourceMeta Name of the image source to use for parsing metadata.
	 * @param destination Name of the writing destination.
	 * @return An initialized {@code Writer}.
	 */
	Writer initializeWriter(Metadata sourceMeta, Location destination)
		throws FormatException, IOException;

	/**
	 * As {@link #initializeWriter(Metadata, Location)} with configuration
	 * options.
	 *
	 * @param sourceMeta Name of the image source to use for parsing metadata.
	 * @param destination Name of the writing destination.
	 * @param config Configuration information to use for this parse.
	 * @return An initialized {@code Writer}.
	 */
	Writer initializeWriter(Metadata sourceMeta, Location destination,
		SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Convenience method to parse {@link Metadata} without reading the underlying
	 * pixels. Useful when translating to other formats, or simply extracting
	 * image metadata. Will not open the image source when determining format
	 * compatibility.
	 *
	 * @param id Name of the image source to be read.
	 * @return Parsed {@link Metadata} for the given source.
	 * @throws FormatException
	 * @throws IOException
	 */
	Metadata parseMetadata(Location id) throws IOException, FormatException;

	/**
	 * As {@link #parseMetadata(Location)} with a flag to open the underlying
	 * dataset when parsing, if desired.
	 *
	 * @param id Name of the image source to be read.
	 * @param config Configuration for this method execution.
	 * @return Parsed {@link Metadata} for the given source.
	 * @throws FormatException
	 * @throws IOException
	 */
	Metadata parseMetadata(Location id, SCIFIOConfig config)
		throws FormatException, IOException;
}
