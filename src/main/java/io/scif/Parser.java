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
import io.scif.io.RandomAccessInputStream;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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
	 * Creates a {@code Metadata} object using the provided name of an image
	 * source.
	 *
	 * @param fileName Name of the image source to parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(String fileName) throws IOException, FormatException;

	/**
	 * Creates a {@code Metadata} object from the provided image file.
	 *
	 * @param file a path to the image file to parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(File file) throws IOException, FormatException;

	/**
	 * Creates a {@code Metadata} object from the provided image source.
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(RandomAccessInputStream stream) throws IOException,
		FormatException;

	/**
	 * Parses metadata using the provided name of an image source, and writes to
	 * an existing {@code Metadata} object (overwriting may occur).
	 *
	 * @param fileName Name of the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(String fileName, Metadata meta) throws IOException,
		FormatException;

	/**
	 * Parses metadata from the provided file location to an existing
	 * {@code Metadata} object (overwriting may occur).
	 *
	 * @param file a path to the image file to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(File file, Metadata meta) throws IOException, FormatException;

	/**
	 * Parses metadata from the provided image source to an existing
	 * {@code Metadata} object (overwriting may occur).
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(RandomAccessInputStream stream, Metadata meta)
		throws IOException, FormatException;

	/**
	 * As {@link #parse(String)} with configuration options.
	 *
	 * @param fileName Name of the image source to parse.
	 * @param config Configuration information to use for this parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(String fileName, SCIFIOConfig config) throws IOException,
		FormatException;

	/**
	 * As {@link #parse(File)} with configuration options.
	 *
	 * @param file a path to the image file to parse.
	 * @param config Configuration information to use for this parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(File file, SCIFIOConfig config) throws IOException,
		FormatException;

	/**
	 * As {@link #parse(RandomAccessInputStream)} with configuration options.
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @param config Configuration information to use for this parse.
	 * @return A new {@code Metadata} object of the appropriate type.
	 */
	Metadata parse(RandomAccessInputStream stream, SCIFIOConfig config)
		throws IOException, FormatException;

	/**
	 * As {@link #parse(RandomAccessInputStream, Metadata)} with configuration
	 * options.
	 *
	 * @param fileName Name of the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @param config Configuration information to use for this parse.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(String fileName, Metadata meta, SCIFIOConfig config)
		throws IOException, FormatException;

	/**
	 * As {@link #parse(File, Metadata)} with configuration options.
	 *
	 * @param file a path to the image file to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @param config Configuration information to use for this parse.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(File file, Metadata meta, SCIFIOConfig config)
		throws IOException, FormatException;

	/**
	 * As {@link #parse(RandomAccessInputStream, Metadata)} with configuration
	 * options.
	 *
	 * @param stream a random access handle to the image source to parse.
	 * @param meta A base {@code Metadata} to fill.
	 * @param config Configuration information to use for this parse.
	 * @return The provided {@code Metadata} after parsing.
	 * @throws IllegalArgumentException if meta is not assignable from the
	 *           {@code Metadata} associated with this {@code Parser's Format}
	 */
	Metadata parse(RandomAccessInputStream stream, Metadata meta,
		SCIFIOConfig config) throws IOException, FormatException;

	/**
	 * @return The last metadata instance parsed by this parser.
	 */
	Metadata getMetadata();

	/**
	 * @return The last input stream read by this parser.
	 */
	RandomAccessInputStream getSource();

	/**
	 * Updates the source being operated on by this parser (e.g. in multi-file
	 * formats).
	 */
	void updateSource(String source) throws IOException;

	/** Returns an array of filenames needed to open this dataset. */
	String[] getUsedFiles();

	/**
	 * Returns an array of filenames needed to open this dataset. If the
	 * 'noPixels' flag is set, then only files that do not contain pixel data will
	 * be returned.
	 */
	String[] getUsedFiles(boolean noPixels);

	/** Returns an array of filenames needed to open the indicated image index. */
	String[] getImageUsedFiles(int imageIndex);

	/**
	 * Returns an array of filenames needed to open the indicated image. If the
	 * 'noPixels' flag is set, then only files that do not contain pixel data will
	 * be returned.
	 */
	String[] getImageUsedFiles(int imageIndex, boolean noPixels);

	/**
	 * Returns an array of FileInfo objects representing the files needed to open
	 * this dataset. If the 'noPixels' flag is set, then only files that do not
	 * contain pixel data will be returned.
	 */
	FileInfo[] getAdvancedUsedFiles(boolean noPixels);

	/**
	 * Returns an array of FileInfo objects representing the files needed to open
	 * the current series. If the 'noPixels' flag is set, then only files that do
	 * not contain pixel data will be returned.
	 */
	FileInfo[] getAdvancedImageUsedFiles(int imageIndex, boolean noPixels);

	/**
	 * Returns a list of MetadataLevel options for determining the granularity of
	 * Metadata collection.
	 */
	Set<MetadataLevel> getSupportedMetadataLevels();
}
