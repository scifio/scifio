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
import java.util.Set;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;

/**
 * Interface for all SCIFIO Parsers.
 * <p>
 * {@code Parsers} are used to create type-specific {@link io.scif.Metadata}
 * appropriate for their {@code Format} by reading from an image source.
 * </p>
 *
 * @see io.scif.Format
 * @author Mark Hiner
 */
public interface Parser extends HasFormat, HasSource, Groupable {

	// -- Parser API methods --

	/**
	 * Creates a {@code Metadata} object using the provided location of an image
	 * source.
	 *
	 * @param location the {@link Location} of the image source to parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(Location location) throws IOException, FormatException;

	/**
	 * Creates a {@code Metadata} object from the provided image source.
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(DataHandle<Location> stream) throws IOException,
		FormatException;

	/**
	 * Parses metadata using the provided name of an image source, and writes to
	 * an existing {@code Metadata} object (overwriting may occur).
	 *
	 * @param location the {@link Location} of the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(Location location, Metadata meta) throws IOException,
		FormatException;

	/**
	 * Parses metadata from the provided image source to an existing
	 * {@code Metadata} object (overwriting may occur).
	 *
	 * @param stream A {@link DataHandle} to the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(DataHandle<Location> stream, Metadata meta) throws IOException,
		FormatException;

	/**
	 * As {@link #parse(Location)} with configuration options.
	 *
	 * @param fileName Name of the image source to parse.
	 * @param config Configuration information to use for this parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(Location fileName, SCIFIOConfig config) throws IOException,
		FormatException;

	/**
	 * As {@link #parse(DataHandle)} with configuration options.
	 *
	 * @param handle a {@link DataHandle} to the image source to parse.
	 * @param config Configuration information to use for this parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(DataHandle<Location> handle, SCIFIOConfig config)
		throws IOException, FormatException;

	/**
	 * As {@link #parse(Location, Metadata)} with configuration options.
	 *
	 * @param fileName the {@link Location} of the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @param config Configuration information to use for this parse.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(Location fileName, Metadata meta, SCIFIOConfig config)
		throws IOException, FormatException;

	/**
	 * /** As {@link #parse(DataHandle, Metadata)} with configuration options.
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @param config Configuration information to use for this parse.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(DataHandle<Location> stream, Metadata meta,
		SCIFIOConfig config) throws IOException, FormatException;

	/**
	 * @return The last metadata instance parsed by this parser.
	 */
	Metadata getMetadata();

	/**
	 * @return The last {@link DataHandle} read by this parser.
	 */
	DataHandle<Location> getSource();

	/**
	 * @return The last Location read from by this parser.
	 */
	Location getSourceLocation();

	/**
	 * Updates the source being operated on by this parser (e.g. in multi-file
	 * formats).
	 */
	void updateSource(Location source) throws IOException;

	/** Returns an array of filenames needed to open this dataset. */
	Location[] getUsedFiles();

	/**
	 * Returns an array of filenames needed to open this dataset. If the
	 * 'noPixels' flag is set, then only files that do not contain pixel data will
	 * be returned.
	 */
	Location[] getUsedLocations(boolean noPixels);

	/** Returns an array of filenames needed to open the indicated image index. */
	Location[] getImageUsedFiles(int imageIndex);

	/**
	 * Returns an array of filenames needed to open the indicated image. If the
	 * 'noPixels' flag is set, then only files that do not contain pixel data will
	 * be returned.
	 */
	Location[] getImageUsedFiles(int imageIndex, boolean noPixels);

	/**
	 * Returns an array of LocationInfo objects representing the Locations needed to open
	 * this dataset. If the 'noPixels' flag is set, then only files that do not
	 * contain pixel data will be returned.
	 */
	LocationInfo[] getAdvancedUsedLocations(boolean noPixels);

	/**
	 * Returns an array of LocationInfo objects representing the Locations needed to open
	 * the current series. If the 'noPixels' flag is set, then only files that do
	 * not contain pixel data will be returned.
	 */
	LocationInfo[] getAdvancedImageUsedLocations(int imageIndex, boolean noPixels);

	/**
	 * Returns a list of MetadataLevel options for determining the granularity of
	 * Metadata collection.
	 */
	Set<MetadataLevel> getSupportedMetadataLevels();

}
