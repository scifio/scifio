/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */
package ome.scifio.services;

import java.util.List;

import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.Writer;

import org.scijava.Priority;
import org.scijava.service.Service;

/**
 * 
 * A collection of methods for finding {@link ome.scifio.Format}
 * instances given a child class, disovering available Formats,
 * and managing the list of available Formats.
 * <p>
 * Also provides convenience methods for working with
 * the Format-specific services.
 * </p>
 * 
 * @see ome.scifio.Format
 * @see FormatService#getInstance(Class)
 * 
 * @author Mark Hiner
 */
public interface FormatService extends Service {
  
  // -- Priority constant --
  
  public static final double PRIORITY = Priority.LOW_PRIORITY;
  
  /**
   * Returns a complete list of all suffixes supported within this context. 
   */
  String[] getSuffixes();

  /**
   * Makes the provided {@code Format} available for image IO operations in
   * this context.
   * <p>
   * No effect if the format is already known.
   * </p>
   * @param format a new {@code Format} to support in this context.
   * @return True if the {@code Format} was added successfully.
   */
  <M extends Metadata> boolean addFormat(Format format);

  /**
   * Removes the provided {@code Format} from this context, if it
   * was previously available.
   * 
   * @param format the {@code Format} to stop supporting in this context.
   * @return True if a format was successfully removed.
   */
  boolean removeFormat(Format format);

  /**
   * Lookup method for the Format map. Use this method  when you want a concrete
   * type reference instead of trying to construct a new {@code Format}.
   * <p>
   * NB: because SezPoz is used for automatic detection of {@code Formats} in
   * SCIFIO, all concrete {@code Format} implementations have a zero-parameter
   * constructor. If you manually invoke that constructor and then try to link
   * your {@code Format} to an existing context, e.g. via the {@link #addFormat(Format)}
   * method, it will fail if the {@code Format} was already discovered.
   * The same principle is true if the context-based constructor is invoked. 
   * </p>
   * @param formatClass the class of the desired {@code Format}
   * @return A reference to concrete class of the queried {@code Format}, or null if the 
   *         {@code Format} was not found.
   */
  <F extends Format> F getFormatFromClass(Class<F> formatClass);
  
  /**
   * Returns the Format compatible with this component class, or null if no matching
   * Format can be found.
   */
  Format getFormatFromComponent(final Class<?> componentClass);

  /**
   * {@code Format} lookup method using the {@code Reader} component
   * 
   * @param readerClass the class of the {@code Reader} component for the
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  <R extends Reader> Format getFormatFromReader(Class<R> readerClass);

  /**
   * {@code Format} lookup method using the {@code Writer} component.
   * 
   * @param writerClass the class of the {@code Writer} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  <W extends Writer> Format getFormatFromWriter(Class<W> writerClass);

  /**
   * {@code Format} lookup method using the {@code Checker} component.
   * 
   * @param writerClass the class of the {@code Checker} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  <C extends Checker> Format getFormatFromChecker(Class<C> checkerClass);

  /**
   * {@code Format} lookup method using the {@code Parser} component.
   * 
   * @param writerClass the class of the {@code Parser} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  <P extends Parser> Format getFormatFromParser(Class<P> parserClass);

  /**
   * {@code Format} lookup method using the {@code Metadata} component.
   * 
   * @param writerClass the class of the {@code Metadata} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  <M extends Metadata> Format getFormatFromMetadata(Class<M> metadataClass);

  /**
   * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority. The source is read
   * if necessary to determine compatibility.
   * 
   * @param id the source
   * @return A  Format reference compatible with the provided source.
   */
  Format getFormat(String id) throws FormatException;
  
  /**
   * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority.
   * 
   * @param id the source
   * @param open true if the source can be read while checking for compatibility.
   * @return A Format reference compatible with the provided source.
   */
  Format getFormat(String id, boolean open) throws FormatException;

  /**
   * Returns a list of all formats that are compatible with the source
   * provided, ordered by their priority. The source is read
   * if necessary to determine compatibility.
   * 
   * @param id the source
   * @return An List of Format references compatible with the provided source.
   */
  List<Format> getFormatList(String id) throws FormatException;

  /**
   * Returns a list of all formats that are compatible with the source
   * provided, ordered by their priority.
   * 
   * @param id the source
   * @param open true if the source can be read while checking for compatibility.
   * @param greedy if true, the search will terminate after finding the first compatible format
   * @return A List of Format references compatible with the provided source.
   */
  List<Format> getFormatList(String id, boolean open, boolean greedy) throws FormatException;

  /**
   * Returns a list of all Formats within this context.
   */
  List<Format> getAllFormats();
  
  /**
   * Convenience method to obtain TypedService instances within the current
   * context.
   * 
   * @param type - Service type to instantiate
   * @return An instance of the requested service
   */
  <T extends TypedService> T getInstance(Class<T> type);
}
