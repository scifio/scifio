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

package io.scif.services;

import io.scif.Checker;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Parser;
import io.scif.Reader;
import io.scif.SCIFIOService;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.scijava.Priority;
import org.scijava.Versioned;

/**
 * A collection of methods for finding {@link io.scif.Format} instances given a
 * child class, discovering available formats, and managing the list of
 * available formats.
 *
 * @see io.scif.Format
 * @author Mark Hiner
 */
public interface FormatService extends SCIFIOService, Versioned {

	// -- Priority constant --

	public static final double PRIORITY = Priority.LOW_PRIORITY;

	/**
	 * Returns a complete list of all suffixes supported within this context.
	 */
	String[] getSuffixes();

	/**
	 * Makes the provided {@code Format} available for image IO operations in this
	 * context.
	 * <p>
	 * No effect if the format is already known.
	 * </p>
	 *
	 * @param format a new {@code Format} to support in this context.
	 * @return True if the {@code Format} was added successfully.
	 */
	<M extends Metadata> boolean addFormat(Format format);

	/**
	 * Creates mappings between this Format and its components.
	 */
	void addComponents(final Format format);

	/**
	 * Removes any mappings involving this Format's components.
	 */
	void removeComponents(final Format format);

	/**
	 * Removes the provided {@code Format} from this context, if it was previously
	 * available.
	 *
	 * @param format the {@code Format} to stop supporting in this context.
	 * @return True if a format was successfully removed.
	 */
	boolean removeFormat(Format format);

	/**
	 * Lookup method for the Format map. Use this method when you want a concrete
	 * type reference instead of trying to construct a new {@code Format}.
	 * <p>
	 * NB: because SezPoz is used for automatic detection of {@code Formats} in
	 * SCIFIO, all concrete {@code Format} implementations have a zero-parameter
	 * constructor. If you manually invoke that constructor and then try to link
	 * your {@code Format} to an existing context, e.g. via the
	 * {@link #addFormat(Format)} method, it will fail if the {@code Format} was
	 * already discovered. The same principle is true if the context-based
	 * constructor is invoked.
	 * </p>
	 *
	 * @param formatClass the class of the desired {@code Format}
	 * @return A reference to concrete class of the queried {@code Format}, or
	 *         null if the {@code Format} was not found.
	 */
	<F extends Format> F getFormatFromClass(Class<F> formatClass);

	/**
	 * Returns the Format compatible with this component class, or null if no
	 * matching Format can be found.
	 */
	Format getFormatFromComponent(final Class<?> componentClass);

	/**
	 * {@code Format} lookup method using the {@code Reader} component
	 *
	 * @param readerClass the class of the {@code Reader} component for the
	 *          desired {@code Format}
	 * @return A reference to the queried {@code Format}, or null if the
	 *         {@code Format} was not found.
	 */
	<R extends Reader> Format getFormatFromReader(Class<R> readerClass);

	/**
	 * {@code Format} lookup method using the {@code Writer} component.
	 *
	 * @param writerClass the class of the {@code Writer} component for the
	 *          desired {@code Format}
	 * @return A reference to the queried {@code Format}, or null if the
	 *         {@code Format} was not found.
	 */
	<W extends Writer> Format getFormatFromWriter(Class<W> writerClass);

	/**
	 * {@code Writer} lookup method using exclusively the supported suffix list.
	 * This bypasses the {@code Checker} logic, and thus does not guarantee the
	 * associated {@code Format} can read image sources of the provided type.
	 *
	 * @throws FormatException
	 */
	Writer getWriterByExtension(String fileId) throws FormatException;

	/**
	 * {@code Format} lookup method using the {@code Checker} component.
	 *
	 * @param checkerClass the class of the {@code Checker} component for the
	 *          desired {@code Format}
	 * @return A reference to the queried {@code Format}, or null if the
	 *         {@code Format} was not found.
	 */
	<C extends Checker> Format getFormatFromChecker(Class<C> checkerClass);

	/**
	 * {@code Format} lookup method using the {@code Parser} component.
	 *
	 * @param parserClass the class of the {@code Parser} component for the
	 *          desired {@code Format}
	 * @return A reference to the queried {@code Format}, or null if the
	 *         {@code Format} was not found.
	 */
	<P extends Parser> Format getFormatFromParser(Class<P> parserClass);

	/**
	 * {@code Format} lookup method using the {@code Metadata} component.
	 *
	 * @param metadataClass the class of the {@code Metadata} component for the
	 *          desired {@code Format}
	 * @return A reference to the queried {@code Format}, or null if the
	 *         {@code Format} was not found.
	 */
	<M extends Metadata> Format getFormatFromMetadata(Class<M> metadataClass);

	/**
	 * Returns the first Format known to be compatible with the source provided.
	 * Formats are checked in ascending order of their priority. The source is
	 * read if necessary to determine compatibility.
	 *
	 * @param id the source
	 * @return A Format reference compatible with the provided source.
	 */
	Format getFormat(String id) throws FormatException;

	/**
	 * Returns the first Format known to be compatible with the source provided.
	 * Formats are checked in ascending order of their priority.
	 *
	 * @param id the source
	 * @param config Configuration for this method execution.
	 * @return A Format reference compatible with the provided source.
	 */
	Format getFormat(String id, SCIFIOConfig config) throws FormatException;

	/**
	 * Returns a list of all formats that are compatible with the source provided,
	 * ordered by their priority. The source is read if necessary to determine
	 * compatibility.
	 *
	 * @param id the source
	 * @return An List of Format references compatible with the provided source.
	 */
	List<Format> getFormatList(String id) throws FormatException;

	/**
	 * Returns a list of all formats that are compatible with the source provided,
	 * ordered by their priority.
	 *
	 * @param id the source
	 * @param config Configuration for this method execution.
	 * @param greedy if true, the search will terminate after finding the first
	 *          compatible format
	 * @return A List of Format references compatible with the provided source.
	 */
	List<Format> getFormatList(String id, SCIFIOConfig config, boolean greedy)
		throws FormatException;

	/**
	 * As {@link #getFormat(String)} but takes an initialized
	 * {@link RandomAccessInputStream}.
	 *
	 * @param source the source
	 * @return A Format reference compatible with the provided source.
	 */
	Format getFormat(RandomAccessInputStream source) throws FormatException;

	/**
	 * As {@link #getFormat(String, SCIFIOConfig)} but takes an initialized
	 * {@link RandomAccessInputStream}.
	 *
	 * @param source the source
	 * @param config Configuration for this method execution.
	 * @return A Format reference compatible with the provided source.
	 */
	Format getFormat(RandomAccessInputStream source, SCIFIOConfig config) throws FormatException;

	/**
	 * As {@link #getFormatList(String)} but takes an initialized
	 * {@link RandomAccessInputStream}.
	 *
	 * @param source the source
	 * @return An List of Format references compatible with the provided source.
	 */
	List<Format> getFormatList(RandomAccessInputStream source) throws FormatException;

	/**
	 * As {@link #getFormatList(String, SCIFIOConfig, boolean)} but takes an initialized
	 * {@link RandomAccessInputStream}.
	 *
	 * @param source the source
	 * @param config Configuration for this method execution.
	 * @param greedy if true, the search will terminate after finding the first
	 *          compatible format
	 * @return A List of Format references compatible with the provided source.
	 */
	List<Format> getFormatList(RandomAccessInputStream source, SCIFIOConfig config, boolean greedy)
		throws FormatException;

	/**
	 * Returns a list of all Formats within this context.
	 */
	Set<Format> getAllFormats();

	/**
	 * @return A list of all Formats that have {@link Writer} implementations.
	 */
	Collection<Format> getOutputFormats();

}
