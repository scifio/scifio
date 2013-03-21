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

import org.scijava.Context;
import org.scijava.plugin.SortablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.discovery.PluginAttributeService;
import ome.scifio.discovery.PluginClassService;
import ome.scifio.util.SCIFIOMetadataTools;

/**
 * Abstract superclass of all SCIFIO components that implement
 * ome.scifio.Format.
 * 
 * @author Mark Hiner
 */
public abstract class AbstractFormat<M extends TypedMetadata, C extends Checker, P extends TypedParser<M>, R extends TypedReader<M, ? extends DataPlane<?>>, W extends TypedWriter<M>>
    extends SortablePlugin implements TypedFormat<M, C, P, R, W> {
  
  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(Format.class);
  
  // -- Fields --

  /** Name of this file format. */
  protected String formatName;

  /** Valid suffixes for this file format. */
  protected String[] suffixes;

  private Class<M> metadataClass;
  private Class<C> checkerClass;
  private Class<P> parserClass;
  private Class<R> readerClass;
  private Class<W> writerClass;

  private final List<Class<? extends Translator>> translatorClassList = new ArrayList<Class<? extends Translator>>();

  // -- Constructor --

  public AbstractFormat(final SCIFIO scifio, String formatName, String suffix,
      Class<M> mClass, Class<C> cClass, Class<P> pClass, Class<R> rClass,
      Class<W> wClass) throws FormatException {
    this(scifio, formatName, new String[]{suffix}, mClass, cClass, pClass, rClass, wClass);
  }
  
  public AbstractFormat(final SCIFIO scifio, String formatName, String[] suffixes,
      Class<M> mClass, Class<C> cClass, Class<P> pClass, Class<R> rClass,
      Class<W> wClass) throws FormatException {
    if (scifio != null) {
      setContext(scifio.getContext()); 
      scifio.addFormat(this);
      translatorClassList.addAll(findTranslatorClassList());
    }
    this.formatName = formatName;
    this.suffixes = suffixes == null ? new String[0] : suffixes;
    metadataClass = mClass;
    checkerClass = cClass;
    parserClass = pClass;
    readerClass = rClass;
    writerClass = wClass;
  }
  
  // -- TypedFormat API Methods --

  /*
   * @see ome.scifio.TypedFormat#findSourceTranslator(ome.scifio.TypedMetadata)
   */
  public <N extends TypedMetadata> TypedTranslator<M, N> findSourceTranslator(
      final N targetMeta) throws FormatException {
    
    return this.<M,N>findTranslator(getFormatName(), targetMeta.getFormatName());
  }

  /*
   * @see ome.scifio.TypedFormat#findDestTranslator(ome.scifio.TypedMetadata)
   */
  public <N extends TypedMetadata> TypedTranslator<N, M> findDestTranslator(
      final N targetMeta) throws FormatException {
    
    return this.<N,M>findTranslator(targetMeta.getFormatName(), getFormatName());
  }

  // -- Format API Methods --

  /* @see Format#getFormatName() */
  public String getFormatName() {
    return formatName;
  }

  /* @see Format#getSuffixes() */
  public String[] getSuffixes() {
    return suffixes.clone();
  }

  /* @see Format#getTranslatorclassList() */
  public List<Class<? extends Translator>> getTranslatorClassList() {
    return translatorClassList;
  }
  
  /*
   * @see ome.scifio.Format#createMetadata()
   */
  public M createMetadata() throws FormatException {
    return createContextualObject(getMetadataClass());
  }

  /*
   * @see ome.scifio.Format#createChecker()
   */
  public C createChecker() throws FormatException {
    return createContextualObject(getCheckerClass());
  }

  /*
   * @see ome.scifio.Format#createParser()
   */
  public P createParser() throws FormatException {
    return createContextualObject(getParserClass());
  }

  /*
   * @see ome.scifio.Format#createReader()
   */
  public R createReader() throws FormatException {
    return createContextualObject(getReaderClass());
  }

  /*
   * @see ome.scifio.Format#createWriter()
   */
  public W createWriter() throws FormatException {
    return createContextualObject(getWriterClass());
  }
  
  /*
   * @see ome.scifio.Format#getMetadataClass()
   */
  public Class<M> getMetadataClass() {
    return metadataClass;
  }

  /*
   * @see ome.scifio.Format#getCheckerClass()
   */
  public Class<C> getCheckerClass() {
    return checkerClass;
  }

  /*
   * @see ome.scifio.Format#getParserClass()
   */
  public Class<P> getParserClass() {
    return parserClass;
  }

  /*
   * @see ome.scifio.Format#getReaderClass()
   */
  public Class<R> getReaderClass() {
    return readerClass;
  }

  /*
   * @see ome.scifio.Format#getWriterClass()
   */
  public Class<W> getWriterClass() {
    return writerClass;
  }
  
  /*
   * @see ome.scifio.Format#findSourceTranslator(ome.scifio.Metadata)
   */
  public Translator findSourceTranslator(Metadata targetMeta)
       throws FormatException {
    return this.findSourceTranslator(SCIFIOMetadataTools.<TypedMetadata>castMeta(targetMeta));
  }
  
  /*
   * @see ome.scifio.Format#findDestTranslator(ome.scifio.Metadata)
   */
  public Translator findDestTranslator(Metadata targetMeta)
      throws FormatException {
    return this.findDestTranslator(SCIFIOMetadataTools.<TypedMetadata>castMeta(targetMeta));
  }
  
  // -- Contextual API Methods --
  
  /*
   * @see org.scijava.AbstractContextual#setContext(org.scijava.Context)
   */
  public void setContext(Context context) {
    super.setContext(context);
    translatorClassList.addAll(findTranslatorClassList());
  }
  
  // -- Helper Methods --

  /**
   * Populates the list of Translators associated with this Format
   */
  private List<Class<? extends Translator>> findTranslatorClassList() {
    Map<String, String> kvPairs = new HashMap<String,String>();
    kvPairs.put(Translator.SOURCE, metadataClass.getName());
    kvPairs.put(Translator.DEST, metadataClass.getName());
    
    return getContext().getService(PluginClassService.class).
        getPluginClasses(Translator.class, null, kvPairs);
  }

  /**
   * Returns a SCIFIO component from its object. Also sets its context based
   * on the current context.
   * 
   * @param <T>
   * @param c
   * @return
   * @throws FormatException
   */
  private <T extends HasFormat> T createContextualObject(final Class<T> c)
      throws FormatException {
    final T t = createObject(c);
    t.setContext(getContext());
    t.setFormat(this);
    return t;
  }

  /**
   * Returns an instance of an object from its Class
   * 
   * @param <T>
   * @param c
   * @return
   * @throws FormatException
   */
  private <T extends HasFormat> T createObject(final Class<T> c) throws FormatException {
    try {
      return c.newInstance();
    } catch (InstantiationException e) {
      throw new FormatException(e);
    } catch (IllegalAccessException e) {
      throw new FormatException(e);
    }
  }
  
  /*
   * Returns a translator object translating from metaIn to metaOut
   */
  private <S extends TypedMetadata, T extends TypedMetadata> TypedTranslator<S, T> 
  findTranslator(final String inFormat, final String outFormat) throws FormatException {
    Map<String, String> kvPairs = new HashMap<String,String>();
    kvPairs.put(Translator.SOURCE, inFormat);
    kvPairs.put(Translator.DEST, outFormat);
    
    @SuppressWarnings("unchecked")
    TypedTranslator<S, T> trans = 
        (TypedTranslator<S, T>) getContext().getService(PluginAttributeService.class).
        createInstance(Translator.class, kvPairs, null);
    
    return trans;
  }
}
