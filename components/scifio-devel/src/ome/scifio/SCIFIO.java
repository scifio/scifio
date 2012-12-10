/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.scifio.discovery.Discoverer;
import ome.scifio.discovery.FormatDiscoverer;
import ome.scifio.discovery.SCIFIOFormat;
import ome.scifio.io.RandomAccessInputStream;

/**
 * 
 * This class represents a contextual environment of SCIFIO components.
 * <p>
 * The context is the entry point for image IO operations. How this class
 * is constructed determines which formats will be supported in this context.
 * </p>
 * <p>
 * Many of the image IO steps can be abstracted by using the {@link #initializeReader}
 * and {@link #initializeWriter} methods. Alternately, to use a specific {@link ome.scifio.Format}
 * implementation, the {@link #getFormatFromClass(Class)} method can provide a 
 * typed {@code Format} object.
 * </p>
 * <p>
 * Note that all {@code Formats} are singletons in a given context.
 * </p>
 * 
 * @author Mark Hiner
 */
public class SCIFIO {

  // TODO: consider using thread-safe ArrayList using java.util.Collections

  // -- Fields --

  /**
   * Used to generate lists of SCIFIO components that can support a given
   * input.
   */
  private final SCIFIOComponentFinder scf = new SCIFIOComponentFinder();

  private final Discoverer<SCIFIOFormat, Format> discoverer = new FormatDiscoverer();

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
   * 
   * Maps Translator classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Translator>, Format> translatorMap =
      new HashMap<Class<? extends Translator>, Format>();

  /**
   * Maps Metadata classes to their parent Format instance.
   * 
   */
  private final Map<Class<? extends Metadata>, Format> metadataMap = new HashMap<Class<? extends Metadata>, Format>();

  // -- Constructors --

  /**
   * Constructs a new context, using <a href="http://sezpoz.java.net/">SezPoz</a>
   * to discover all available {@code Formats}.
   */
  public SCIFIO() throws FormatException {
    this((Format[]) null);
  }

  /**
   * Constructs a context using the provided list of Formats.
   * <p>
   * NB: No formats will be discovered automatically.
   * </p>
   * @param formats the list of {@code Formats} to use in this context.
   */
  public SCIFIO(final Format... formats)
      throws FormatException {
    processFormats(formats);
  }

  // TODO: consider having SCIFIO implement List<Format>... but potential
  // problems.

  // -- Public Methods --

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
    if(formatMap.get(format.getClass()) == null)
      return false;
    
    formats.add(format);
    checkerMap.put(format.getCheckerClass(), format);
    parserMap.put(format.getParserClass(), format);
    readerMap.put(format.getReaderClass(), format);
    writerMap.put(format.getWriterClass(), format);
    formatMap.put(format.getClass(), format);
    metadataMap.put(format.getMetadataClass(), format);
    for (final Class<? extends Translator> translatorClass : format
        .getTranslatorClassList()) {
      translatorMap.put(translatorClass, format);
    }
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
    for (final Class<? extends Translator> translatorClass : format
        .getTranslatorClassList()) {
      translatorMap.remove(translatorClass);
    }
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
   * {@code Format} lookup method using the {@code Translator} component.
   * 
   * @param writerClass the class of the {@code Translator} component for the 
   *        desired {@code Format}
   * @return A reference to the queried {@code Format}, or null if
   *         the {@code Format} was not found.
   */
  public <T extends Translator> Format getFormatFromTranslator(
      final Class<T> translatorClass) {
    final Format format = translatorMap.get(translatorClass);
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
    return scf.findFormats(id, open, true, formats).get(0);
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
    return scf.findFormats(id, open, false, formats);
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
   * See {@link #initializeReader(String, boolean)}. Will not open the image
   * source while parsing metadata.
   * 
   * @param id Name of the image source to be read.
   * @return An initialized {@code Reader}.
   */
  public Reader initializeReader(final String id) throws FormatException,
      IOException {
    return initializeReader(id, false);
  }

  /**
   * Convenience method for creating a {@code Reader} component that is ready
   * to open planes of the provided image source. The reader's {@code Metadata}
   * and source fields will be populated.
   * 
   * @param id Name of the image source to be read.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Reader}.
   */
  public Reader initializeReader(final String id, final boolean openFile)
      throws FormatException, IOException {
    final Reader r = getFormat(id, openFile).createReader();
    r.setSource(id);
    return r;
  }

  /**
   * See {@link #initializeWriter(String, String, boolean)}. Will not open the
   * image source while parsing metadata.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @return An initialized {@code Writer}.
   */
  public Writer initializeWriter(
      final String source, final String destination) throws FormatException, IOException {
    return initializeWriter(source, destination, false);
  }
  
  /**
   * Convenience method for creating a {@code Writer} component that is ready
   * to save planes to the destination image. {@code Metadata} will be parsed
   * from the source, translated to the destination's type if necessary, and
   * set (along with the destination itself) on the resulting {@code Writer}.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Writer}.
   */
  public Writer initializeWriter(
      final String source, final String destination,
      final boolean openSource) throws FormatException, IOException {
    final Format sFormat = getFormat(source, openSource);
    final Format dFormat = getFormat(destination, false);
    final Parser parser = sFormat.createParser();
    final Metadata sourceMeta = parser.parse(source);
    Metadata destMeta = null;

    // if dest is a different format than source, translate..
    if (sFormat != dFormat) {
      destMeta = dFormat.createMetadata();
      
      // TODO should probably make this general wrt DatasetMetadata,
      // but that also requires having a general way to instantiate DatasetMetadata
      final DefaultDatasetMetadata transMeta = new DefaultDatasetMetadata(this);
      final Translator transToCore = sFormat.findSourceTranslator(transMeta);
      final Translator transFromCore = dFormat.findDestTranslator(transMeta);
      transMeta.setSource(new RandomAccessInputStream(source));
      transToCore.translate(sourceMeta, transMeta);
      transFromCore.translate(transMeta, destMeta);
    } else {
      // otherwise we can directly cast, since they are the same types
      destMeta = castMeta(sourceMeta, destMeta);
    }

    final Writer writer = dFormat.createWriter();
    writer.setMetadata(destMeta);
    writer.setDest(destination);

    return writer;
  }
  
  // -- Helper Methods --
  
  /*
   * Hide the suppress warnings in an atomic cast method
   * <p>
   * NB: endType parameter is just there to guarantee a return type
   * </p>
   */
  private <N extends Metadata, M extends Metadata> M castMeta(N metadata, M endType) {
    @SuppressWarnings("unchecked")
    M meta = (M) metadata;
    return meta;
  }

  /*
   * Processes each format in the provided list. If the list is null,
   * discovers available Formats
   */
  private void processFormats(Format... formats)
      throws FormatException {
    if (formats == null) {
      List<Format> tmpFormats = discoverer.discover();
      formats = tmpFormats.toArray(new Format[tmpFormats.size()]);
    }

    for (final Format format : formats) {
      format.setContext(this);
      addFormat(format);
    }
  }

}
