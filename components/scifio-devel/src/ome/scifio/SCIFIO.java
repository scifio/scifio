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
 * 
 * @author Mark Hiner
 */
public class SCIFIO {

  // TODO: consider using thread-safe ArrayList using java.util.Collections

  // -- Fields --

  private final SCIFIOComponentFinder scf = new SCIFIOComponentFinder();

  private final Discoverer<SCIFIOFormat, Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
      ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
      ? extends Writer<? extends Metadata>>> discoverer = new FormatDiscoverer();

  /**
   * List of all formats known to this context.
   * 
   */
  private final List<Format<?, ?, ?, ?, ?>> formats = new ArrayList<Format<?, ?, ?, ?, ?>>();

  /**
   * Maps Checker classes to their containing format.
   * 
   */
  private final Map<Class<? extends Checker<? extends Metadata>>, Format<?, ?, ?, ?, ?>> checkerMap =
      new HashMap<Class<? extends Checker<? extends Metadata>>, Format<?, ?, ?, ?, ?>>();

  /**
   * Maps Parser classes to their containing format.
   * 
   */
  private final Map<Class<? extends Parser<? extends Metadata>>, Format<?, ?, ?, ?, ?>> parserMap = 
      new HashMap<Class<? extends Parser<? extends Metadata>>, Format<?, ?, ?, ?, ?>>();

  /**
   * Maps Reader classes to their containing format.
   * 
   */
  private final Map<Class<? extends Reader<? extends Metadata>>, Format<?, ?, ?, ?, ?>> readerMap =
      new HashMap<Class<? extends Reader<? extends Metadata>>, Format<?, ?, ?, ?, ?>>();

  /**
   * Maps Writer classes to their containing formats.
   * 
   */
  private final Map<Class<? extends Writer<? extends Metadata>>, Format<?, ?, ?, ?, ?>> writerMap =
      new HashMap<Class<? extends Writer<? extends Metadata>>, Format<?, ?, ?, ?, ?>>();

  /**
   * 
   * Maps Translator classes to their containing formats.
   * 
   */
  private final Map<Class<? extends Translator<? extends Metadata, ? extends Metadata>>, Format<?, ?, ?, ?, ?>> translatorMap =
      new HashMap<Class<? extends Translator<? extends Metadata, ? extends Metadata>>, Format<?, ?, ?, ?, ?>>();

  /**
   * Maps Metadata classes to their containing formats.
   * 
   */
  private final Map<Class<Metadata>, Format<?, ?, ?, ?, ?>> metadataMap = new HashMap<Class<Metadata>, Format<?, ?, ?, ?, ?>>();

  // -- Constructors --

  /**
   * Constructs a SCIFIO object from the default, discovered Formats.
   * 
   */
  public SCIFIO() throws FormatException {
    this((Format<?, ?, ?, ?, ?>[]) null);
  }

  /**
   * Constructs a SCIFIO object using the provided list of Formats.
   * 
   * No formats will be discovered automatically.
   * 
   * @param formats
   * @throws FormatException
   */
  public SCIFIO(final Format<?, ?, ?, ?, ?>... formats)
      throws FormatException {
    processFormats(formats);
  }

  // TODO: consider having SCIFIO implement List<Format>... but potential
  // problems.

  // -- Public Methods --

  /**
   * Adds the provided format to the list of formats within this context, and
   * constructs Map indexes for each of its non-translator components.
   * 
   * @param format
   * @throws FormatException
   */
  @SuppressWarnings("unchecked")
  public <M extends Metadata> void addFormat(
      final Format<M, ?, ?, ?, ?> format) throws FormatException {
    formats.add(format);
    checkerMap.put(format.getCheckerClass(), format);
    parserMap.put(format.getParserClass(), format);
    readerMap.put(format.getReaderClass(), format);
    writerMap.put(format.getWriterClass(), format);
    metadataMap.put((Class<Metadata>) format.getMetadataClass(), format);
    for (final Class<Translator<?, ?>> translatorClass : format
        .getTranslatorClassList()) {
      translatorMap.put(translatorClass, format);
    }
  }

  /**
   * Removes the provided format from the formats list, and from all
   * associated indexing maps.
   * 
   * @param format
   * @return
   */
  public boolean removeFormat(final Format<?, ?, ?, ?, ?> format) {
    checkerMap.remove(format.getCheckerClass());
    parserMap.remove(format.getParserClass());
    readerMap.remove(format.getReaderClass());
    writerMap.remove(format.getWriterClass());
    metadataMap.remove(format.getMetadataClass());
    for (final Class<? extends Translator<? extends Metadata, ? extends Metadata>> translatorClass : format
        .getTranslatorClassList()) {
      translatorMap.put(translatorClass, format);
    }
    return formats.remove(format);
  }

  /**
   * Lookup method for the Reader map
   * 
   */
  public <M extends Metadata, R extends Reader<M>> Format<M, ?, ?, R, ?> getFormatFromReader(
      final Class<R> readerClass) {
    @SuppressWarnings("unchecked")
    final Format<M, ?, ?, R, ?> format = (Format<M, ?, ?, R, ?>) readerMap
        .get(readerClass);
    return format;
  }

  /**
   * Lookup method for the Writer map
   * 
   */
  public <M extends Metadata, W extends Writer<M>> Format<M, ?, ?, ?, W> getFormatFromWriter(
      final Class<W> writerClass) {
    @SuppressWarnings("unchecked")
    final Format<M, ?, ?, ?, W> format = (Format<M, ?, ?, ?, W>) writerMap
        .get(writerClass);
    return format;
  }

  /**
   * Lookup method for the Checker map
   * 
   */
  public <M extends Metadata, C extends Checker<M>> Format<M, C, ?, ?, ?> getFormatFromChecker(
      final Class<C> checkerClass) {
    @SuppressWarnings("unchecked")
    final Format<M, C, ?, ?, ?> format = (Format<M, C, ?, ?, ?>) checkerMap
        .get(checkerClass);
    return format;
  }

  /**
   * Lookup method for the Parser map
   * 
   */
  public <M extends Metadata, P extends Parser<M>> Format<M, ?, P, ?, ?> getFormatFromParser(
      final Class<P> parserClass) {
    @SuppressWarnings("unchecked")
    final Format<M, ?, P, ?, ?> format = (Format<M, ?, P, ?, ?>) parserMap
        .get(parserClass);
    return format;
  }

  /**
   * Lookup method for the Translator map
   * 
   */
  public <M extends Metadata, T extends Translator<M, ?>> Format<M, ?, ?, ?, ?> getFormatFromTranslator(
      final Class<T> translatorClass) {
    @SuppressWarnings("unchecked")
    final Format<M, ?, ?, ?, ?> format = (Format<M, ?, ?, ?, ?>) translatorMap
        .get(translatorClass);
    return format;
  }

  /**
   * Lookup method for the Metadata map
   * 
   */
  public <M extends Metadata> Format<M, ?, ?, ?, ?> getFormatFromMetadata(
      final Class<M> metadataClass) {
    @SuppressWarnings("unchecked")
    final Format<M, ?, ?, ?, ?> format = (Format<M, ?, ?, ?, ?>) metadataMap
        .get(metadataClass);
    return format;
  }

  /**
   * Returns the first Format known to be compatible with the source provided.
   * Formats are checked in ascending order of their priority.
   */
  public Format<?, ?, ?, ?, ?> getFormat(final String id, final boolean open)
      throws FormatException {
    return scf.findFormats(id, open, true, formats).get(0);
  }

  /**
   * Returns a list of all formats that are compatible with the source
   * provided, ordered by their priority.
   * 
   * @param id
   * @param openFile
   * @return
   * @throws FormatException
   */
  public List<Format<?, ?, ?, ?, ?>> getFormatList(final String id,
      final boolean openFile) throws FormatException {
    return scf.findFormats(id, openFile, false, formats);
  }

  /**
   * See getFormat(String, boolean)
   * 
   * @param id
   * @return
   * @throws FormatException
   */
  public Format<?, ?, ?, ?, ?> getFormat(final String id)
      throws FormatException {
    return getFormat(id, false);
  }

  /**
   * See getFormatList(String, boolean)
   * 
   * @param id
   * @return
   * @throws FormatException
   */
  public List<Format<?, ?, ?, ?, ?>> getFormatList(final String id)
      throws FormatException {
    return getFormatList(id, false);
  }

  /**
   * See initializeReader(String, boolean)
   * 
   * @param id
   * @return
   * @throws FormatException
   * @throws IOException
   */
  public Reader<?> initializeReader(final String id) throws FormatException,
      IOException {
    return initializeReader(id, false);
  }

  /**
   * Returns an initialized Reader (e.g. Metadata and Source have been set)
   * using the provided id as a source.
   * 
   * @param id
   * @param openFile
   * @return
   * @throws FormatException
   * @throws IOException
   */
  public Reader<?> initializeReader(final String id, final boolean openFile)
      throws FormatException, IOException {
    final Reader<?> r = getFormat(id, openFile).createReader();
    r.setSource(id);
    return r;
  }

  /**
   * See initializeWriter(String, String, boolean)
   * 
   * @param <M>
   * @param source
   * @param destination
   * @param openSource
   * @return
   * @throws FormatException
   * @throws IOException
   */
  public <M extends Metadata, N extends Metadata> Writer<M> initializeWriter(
      final String source, final String destination) throws FormatException, IOException {
    return initializeWriter(source, destination, false);
  }
  
  /**
   * Builds and initializes a Writer object for saving the source into the
   * destination. Translates Metadata if necessary. The returned Writer is
   * ready to saveBytes.
   * 
   * @param <M>
   * @param source
   * @param destination
   * @param openSource
   * @return
   * @throws FormatException
   * @throws IOException
   */
  public <M extends Metadata, N extends Metadata> Writer<M> initializeWriter(
      final String source, final String destination,
      final boolean openSource) throws FormatException, IOException {
    @SuppressWarnings("unchecked")
    final Format<N, ?, ?, ?, ?> sFormat = (Format<N, ?, ?, ?, ?>) getFormat(
        source, openSource);
    @SuppressWarnings("unchecked")
    final Format<M, ?, ?, ?, ?> dFormat = (Format<M, ?, ?, ?, ?>) getFormat(
        destination, false);
    final Parser<N> parser = sFormat.createParser();
    final N sourceMeta = parser.parse(source);
    M destMeta = null;

    // if dest is a different format than source, translate..
    if (sFormat != dFormat) {
      destMeta = dFormat.createMetadata();
      
      final Translator<N, DatasetMetadata> transToCore = sFormat
          .findSourceTranslator(DatasetMetadata.class);
      final Translator<DatasetMetadata, M> transFromCore = dFormat
          .findDestTranslator(DatasetMetadata.class);
      final DatasetMetadata transMeta = new DatasetMetadata(this);
      transMeta.setSource(new RandomAccessInputStream(source));
      transToCore.translate(sourceMeta, transMeta);
      transFromCore.translate(transMeta, destMeta);
    } else {
      // otherwise we can directly cast, since they are the same types
      destMeta = castMeta(sourceMeta, destMeta);
    }

    final Writer<M> writer = dFormat.createWriter();
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
  private void processFormats(Format<?, ?, ?, ?, ?>... formats)
      throws FormatException {
    if (formats == null) {
      List<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
          ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>,
          ? extends Writer<? extends Metadata>>> tmpFormats = discoverer.discover();
      formats = tmpFormats.toArray(new Format<?, ?, ?, ?, ?>[tmpFormats.size()]);
    }

    for (final Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
        ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
        ? extends Writer<? extends Metadata>> format : formats) {
      format.setContext(this);
      addFormat(format);
    }
  }

}
