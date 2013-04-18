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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.Writer;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

/**
 * Default {@link FormatService} implementation
 * 
 * @see ome.scifio.services.FormatService
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
  
  /*
   * A  list of all available Formats
   */
  private final List<Format> formats = new ArrayList<Format>();
  
  /*
   * Maps Format classes to their instances.
   */
  private final Map<Class<?>, Format> formatMap = new HashMap<Class<?>, Format>();
  
  /*
   * Maps Checker classes to their parent Format instance.
   */
  private final Map<Class<?>, Format> checkerMap = new HashMap<Class<?>, Format>();

  /*
   * Maps Parser classes to their parent Format instance.
   */
  private final Map<Class<?>, Format> parserMap = new HashMap<Class<?>, Format>();

  /*
   * Maps Reader classes to their parent Format instance.
   */
  private final Map<Class<?>, Format> readerMap = new HashMap<Class<?>, Format>();

  /*
   * Maps Writer classes to their parent Format instance.
   */
  private final Map<Class<?>, Format> writerMap = new HashMap<Class<?>, Format>();

  /*
   * Maps Metadata classes to their parent Format instance.
   */
  private final Map<Class<?>, Format> metadataMap = new HashMap<Class<?>, Format>();

  // -- FormatService API Methods --
  
  /*
   * @see FormatService#getSuffixes()
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

  /*
   * @see FormatService#addFormat(Format)
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

  /*
   * @see FormatService#removeFormat(Format)
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
  
  /*
   * @see FormatService#getFormatFromClass(Class<? extends Format>)
   */
  @SuppressWarnings("unchecked")
  public <F extends Format> F getFormatFromClass(final Class<F> formatClass) {
    return (F)formatMap.get(formatClass);
  }
  
  /*
   * @see FormatService#getFormatFromComponent(Class<?>)
   */
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

  /*
   * @see FormatService#getFormatFromReader(Class<? extends Reader>)
   */
  public <R extends Reader> Format getFormatFromReader(final Class<R> readerClass) {
    return readerMap.get(readerClass);
  }

  /*
   * @see FormatService#getFormatFromWriter(Class<? extends Writer>)
   */
  public <W extends Writer> Format getFormatFromWriter(final Class<W> writerClass) {
    return writerMap.get(writerClass);
  }

  /*
   * @see FormatService#getFormatFromChecker(Class<? extends Checker>)
   */
  public <C extends Checker> Format getFormatFromChecker(final Class<C> checkerClass) {
    return checkerMap.get(checkerClass);
  }

  /*
   * @see FormatService#getFormatFromParser(Class<? extends Parser)
   */
  public <P extends Parser> Format getFormatFromParser(final Class<P> parserClass) {
    return parserMap.get(parserClass);
  }

  /*
   * @see FormatService#getFormatFromMetadata(Class<? extends Metadata>)
   */
  public <M extends Metadata> Format getFormatFromMetadata(final Class<M> metadataClass) {
    return metadataMap.get(metadataClass);
  }

  /**
   * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority. The source is read
   * if necessary to determine compatibility.
   * 
   * @param id the source
   * @return A Format reference compatible with the provided source.
   */
  public Format getFormat(final String id)
      throws FormatException {
    return getFormat(id, false);
  }
  
  /*
   * @see FormatService#getFormat(String, boolean)
   */
  public Format getFormat(final String id, final boolean open)
      throws FormatException {
    return getFormatList(id, open, true).get(0);
  }
  
  /*
   * @see FormatService#getformatList(String)
   */
  public List<Format> getFormatList(final String id)
      throws FormatException {
    return getFormatList(id, false, false);
  }

  /*
   * @see FormatService#getFormatList(String, boolean, boolean)
   */
  public List<Format> getFormatList(final String id,
    final boolean open, final boolean greedy) throws FormatException
  {
    
    final List<Format> formatList = new ArrayList<Format>();

    boolean found = false;
    
    for (int i=0; i<formats.size() && !found; i++) {
      final Format format = formats.get(i);
      if (format.createChecker().isFormat(id, open)) {
        // if greedy is true, we can end after finding the first format
        found = greedy;
        formatList.add(format);
      }
    }

    if (formatList.isEmpty())
      throw new FormatException(id + ": No supported format found.");

    return formatList;
  }
  
  /*
   * @see FormatService#getAllFormats()
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
  
  /*
   * Discovers the list of formats and creates singleton instances of each.
   */
  public void initialize() {
    for (Format format : pluginService.createInstancesOfType(Format.class))
    {
      addFormat(format); 
    }
    
    Collections.sort(formats);
  }
}
