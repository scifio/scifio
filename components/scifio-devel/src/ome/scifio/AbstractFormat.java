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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of all SCIFIO {@link ome.scifio.Format} implementations.
 * 
 * @see ome.scifio.Format
 * @see ome.scifio.Metadata
 * @see ome.scifio.Parser
 * @see ome.scifio.Reader
 * @see ome.scifio.Writer
 * @see ome.scifio.Checker
 * @see ome.scifio.services.FormatService
 * 
 * @author Mark Hiner
 * 
 * @param <M> - The Metadata type associated with this Format
 * @param <C> - The Checker type associated with this Format
 * @param <P> - The Parser type associated with this Format
 * @param <R> - The Reader type associated with this Format
 * @param <W> - The Writer type associated with this Format
 */
public abstract class AbstractFormat
    <M extends TypedMetadata, C extends Checker, P extends TypedParser<M>,
    R extends TypedReader<M, ? extends DataPlane<?>>, W extends TypedWriter<M>>
    extends AbstractHasSCIFIO implements TypedFormat<M, C, P, R, W>
{
  
  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(Format.class);
  
  // -- Fields --

  /** Name of this file format. */
  protected String formatName;

  /** Valid suffixes for this file format. */
  protected String[] suffixes;

  // Class references to the components of this Format
  private Class<M> metadataClass;
  private Class<C> checkerClass;
  private Class<P> parserClass;
  private Class<R> readerClass;
  private Class<W> writerClass;

  // -- Constructor --

  public AbstractFormat(String formatName, String suffix,
      Class<M> mClass, Class<C> cClass, Class<P> pClass, Class<R> rClass,
      Class<W> wClass) throws FormatException
  {
    this(formatName, new String[]{suffix}, mClass, cClass, pClass, rClass, wClass);
  }
  
  public AbstractFormat(String formatName, String[] suffixes,
      Class<M> mClass, Class<C> cClass, Class<P> pClass, Class<R> rClass,
      Class<W> wClass) throws FormatException
  {
    this.formatName = formatName;
    this.suffixes = suffixes == null ? new String[0] : suffixes;
    metadataClass = mClass;
    checkerClass = cClass;
    parserClass = pClass;
    readerClass = rClass;
    writerClass = wClass;
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
  
  // -- Helper Methods --

  /*
   * Creates a SCIFIO component from its class. Also sets its context based
   * on this format's context.
   */
  private <T extends HasFormat> T createContextualObject(final Class<T> c)
      throws FormatException {
    final T t = createObject(c);
    t.setContext(getContext());
    return t;
  }

  /*
   * Returns an instance of an object from its Class
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
}
