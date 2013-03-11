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
package ome.scifio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;


import ome.scifio.services.TypedService;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

/**
 * Maps component classes to their associated format
 * 
 * @author Mark Hiner
 *
 */
@Plugin(type=FormatService.class)
public class DefaultFormatService extends AbstractService implements FormatService {

  // -- Parameters --
  
  @Parameter
  private PluginService pluginService;
  
  // -- Fields --

  /**
   * List of all formats known to this context.
   */
  private final List<Format> formats = new ArrayList<Format>();
  
  /**
   * Maps Format classes to their instances.
   */
  private final Map<Class<? extends Format>, Format> formatMap =
      new HashMap<Class<? extends Format>, Format>();
  
  /**
   * Maps Checker classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Checker>, Format> checkerMap =
      new HashMap<Class<? extends Checker>, Format>();

  /**
   * Maps Parser classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Parser>, Format> parserMap = 
      new HashMap<Class<? extends Parser>, Format>();

  /**
   * Maps Reader classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Reader>, Format> readerMap =
      new HashMap<Class<? extends Reader>, Format>();

  /**
   * Maps Writer classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Writer>, Format> writerMap =
      new HashMap<Class<? extends Writer>, Format>();

  /**
   * Maps Metadata classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Metadata>, Format> metadataMap = new HashMap<Class<? extends Metadata>, Format>();

  // -- FormatService API Methods --
  
  /**
   * Returns a complete list of all suffixes supported within this context. 
   * 
   * @return
   */
  public String[] getSuffixes() {
    TreeSet<String> ts = new TreeSet<String>();
    
    for (Format f : formats) {
      for (String s : f.getSuffixes()) {
        ts.add(s);
      }
    }
    
    return ts.toArray(new String[ts.size()]);
  }

  /**
   * Makes the provided {@code Format} available for image IO operations in
   * this context.
   * <p>
   * No effect if the format is already known.
   * </p>
   * @param format a new {@code Format} to support in this context.
   * @return True if the {@code Format} was added successfully.
   */
  public <M extends Metadata> boolean addFormat(
      final Format format) {
    // already have an entry for this format
    if(formatMap.get(format.getClass()) != null)
      return false;
    
    formats.add(format);
    checkerMap.put(format.getCheckerClass(), format);
    parserMap.put(format.getParserClass(), format);
    readerMap.put(format.getReaderClass(), format);
    writerMap.put(format.getWriterClass(), format);
    formatMap.put(format.getClass(), format);
    metadataMap.put(format.getMetadataClass(), format);
    if (format.getContext() == null) format.setContext(getContext());
    return true;
  }

  /**
   * Removes the provided {@code Format} from this context, if it
   * was previously available.
   * 
   * @param format the {@code Format} to stop supporting in this context.
   * @return True if a format was successfully removed.
   */
  public boolean removeFormat(final Format format) {
    checkerMap.remove(format.getCheckerClass());
    parserMap.remove(format.getParserClass());
    readerMap.remove(format.getReaderClass());
    writerMap.remove(format.getWriterClass());
    metadataMap.remove(format.getMetadataClass());
    formatMap.remove(format.getClass());
    return formats.remove(format);
  }
  
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
  public <F extends Format> F getFormatFromClass(
      final Class<F> formatClass) {
    @SuppressWarnings("unchecked")
    final F format = (F) formatMap.get(formatClass);
    return format;
  }
  
  @SuppressWarnings("unchecked")
  public Format getFormatFromComponent(final Class<?> componentClass) {
    Format fmt = null;

    if (Reader.class.isAssignableFrom(componentClass)) {
      fmt = getFormatFromReader((Class<? extends Reader>)componentClass);
    }
    else if (Writer.class.isAssignableFrom(componentClass)) {
      fmt = getFormatFromWriter((Class<? extends Writer>)componentClass);
    }
    else if (Metadata.class.isAssignableFrom(componentClass)) {
      fmt = getFormatFromMetadata((Class<? extends Metadata>)componentClass);
    }
    else if (Parser.class.isAssignableFrom(componentClass)) {
      fmt = getFormatFromParser((Class<? extends Parser>)componentClass);
    }
    else if (Checker.class.isAssignableFrom(componentClass)) {
      fmt = getFormatFromChecker((Class<? extends Checker>)componentClass);
    }

    return fmt;
  }

  /**
   * {@code Format} lookup method using the {@code Reader} component
   * 
   * @param readerClass the class of the {@code Reader} component for the
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <R extends Reader> Format getFormatFromReader(
      final Class<R> readerClass) {
    final Format format =  readerMap.get(readerClass);
    return format;
  }

  /**
   * {@code Format} lookup method using the {@code Writer} component.
   * 
   * @param writerClass the class of the {@code Writer} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <W extends Writer> Format getFormatFromWriter(
      final Class<W> writerClass) {
    final Format format = writerMap.get(writerClass);
    return format;
  }

  /**
   * {@code Format} lookup method using the {@code Checker} component.
   * 
   * @param writerClass the class of the {@code Checker} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <C extends Checker> Format getFormatFromChecker(
      final Class<C> checkerClass) {
    final Format format = checkerMap.get(checkerClass);
    return format;
  }

  /**
   * {@code Format} lookup method using the {@code Parser} component.
   * 
   * @param writerClass the class of the {@code Parser} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <P extends Parser> Format getFormatFromParser(
      final Class<P> parserClass) {
    final Format format = parserMap.get(parserClass);
    return format;
  }

  /**
   * {@code Format} lookup method using the {@code Metadata} component.
   * 
   * @param writerClass the class of the {@code Metadata} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <M extends Metadata> Format getFormatFromMetadata(
      final Class<M> metadataClass) {
    final Format format = metadataMap.get(metadataClass);
    return format;
  }
  
  public Format getFormatFromFormatName(final String formatName) {
    for (Format format : formats) {
      if (format.getFormatName().equals(formatName))
        return format;
    }
    
    return null;
  }

  /**
   * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority.
   * 
   * @param id the source
   * @param open true if the source can be read while checking for compatibility.
   * @return A Format reference compatible with the provided source.
   */
  public Format getFormat(final String id, final boolean open)
      throws FormatException {
    return getFormatList(id, open).get(0);
  }

  /**
   * Returns a list of all formats that are compatible with the source
   * provided, ordered by their priority.
   * 
   * @param id the source
   * @param open true if the source can be read while checking for compatibility.
   * @return A List of Format references compatible with the provided source.
   */
  public List<Format> getFormatList(final String id,
      final boolean open) throws FormatException {
    
    final PriorityQueue<Format> formatPriorities = new PriorityQueue<Format>(formats);
    final List<Format> formatList = new ArrayList<Format>();

    boolean found = false;
    while (!formatPriorities.isEmpty() && !found) {
      final Format format = formatPriorities.poll();

      if (format.createChecker().isFormat(id, open)) {
        found = true;
        formatList.add(format);
      }
    }

    if (formatList.isEmpty())
      throw new FormatException(id + ": No supported format found.");

    return formatList;
  }

  /**
 * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority. The source is read
   * if necessary to determine compatibility.
   * 
   * @param id the source
   * @return A  Format reference compatible with the provided source.
   */
  public Format getFormat(final String id)
      throws FormatException {
    return getFormat(id, false);
  }

  /**
   * Returns a list of all formats that are compatible with the source
   * provided, ordered by their priority. The source is read
   * if necessary to determine compatibility.
   * 
   * @param id the source
   * @return An List of Format references compatible with the provided source.
   */
  public List<Format> getFormatList(final String id)
      throws FormatException {
    return getFormatList(id, false);
  }
  
  /**
   * Returns a list of all Formats within this context.
   * 
   * @return
   */
  public List<Format> getAllFormats() {
    return formats;
  }
  
  /*
   * @see ome.scifio.FormatService#getInstance(java.lang.Class)
   */
  public <T extends TypedService> T getInstance(Class<T> type) { 
    return getContext().getService(type);
  }

  // -- Service API Methods --
  
  public void initialize() {
    for (Format format : pluginService.createInstancesOfType(Format.class))
    {
      addFormat(format); 
    }
  }
}
