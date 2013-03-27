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
import java.util.List;

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
 */
public abstract class AbstractFormat extends AbstractHasSCIFIO implements Format
{
  
  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(Format.class);
  
  // -- Fields --

  /** Valid suffixes for this file format. */
  protected String[] suffixes;

  // Class references to the components of this Format
  private Class<? extends Metadata> metadataClass;
  private Class<? extends Checker> checkerClass;
  private Class<? extends Parser> parserClass;
  private Class<? extends Reader> readerClass;
  private Class<? extends Writer> writerClass;

  // -- Constructor --

  public AbstractFormat()
  {
    metadataClass = DefaultMetadata.class;
    checkerClass = DefaultChecker.class;
    parserClass = DefaultParser.class;
    readerClass = DefaultReader.class;
    writerClass = DefaultWriter.class;
    
    updateCustomClasses();
  }

  // -- Format API Methods --
  
  /*
   * @see ome.scifio.Format#createMetadata()
   */
  public Metadata createMetadata() throws FormatException {
    return createContextualObject(getMetadataClass());
  }

  /*
   * @see ome.scifio.Format#createChecker()
   */
  public Checker createChecker() throws FormatException {
    return createContextualObject(getCheckerClass());
  }

  /*
   * @see ome.scifio.Format#createParser()
   */
  public Parser createParser() throws FormatException {
    return createContextualObject(getParserClass());
  }

  /*
   * @see ome.scifio.Format#createReader()
   */
  public Reader createReader() throws FormatException {
    return createContextualObject(getReaderClass());
  }

  /*
   * @see ome.scifio.Format#createWriter()
   */
  public Writer createWriter() throws FormatException {
    return createContextualObject(getWriterClass());
  }
  
  /*
   * @see ome.scifio.Format#getMetadataClass()
   */
  public Class<? extends Metadata> getMetadataClass() {
    return metadataClass;
  }

  /*
   * @see ome.scifio.Format#getCheckerClass()
   */
  public Class<? extends Checker> getCheckerClass() {
    return checkerClass;
  }

  /*
   * @see ome.scifio.Format#getParserClass()
   */
  public Class<? extends Parser> getParserClass() {
    return parserClass;
  }

  /*
   * @see ome.scifio.Format#getReaderClass()
   */
  public Class<? extends Reader> getReaderClass() {
    return readerClass;
  }

  /*
   * @see ome.scifio.Format#getWriterClass()
   */
  public Class<? extends Writer> getWriterClass() {
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

  /*
   * Overrides the default classes with declared custom components.
   */
  @SuppressWarnings("unchecked")
  private void updateCustomClasses() {
    
    for (Class<?> c : buildClassList()) {
      if (Metadata.class.isAssignableFrom(c))
        metadataClass = (Class<? extends Metadata>) c;
      else if (Checker.class.isAssignableFrom(c))
        checkerClass = (Class<? extends Checker>) c;
      else if (Parser.class.isAssignableFrom(c))
        parserClass = (Class<? extends Parser>) c;
      else if (Reader.class.isAssignableFrom(c))
        readerClass = (Class<? extends Reader>) c;
      else if (Writer.class.isAssignableFrom(c))
        writerClass = (Class<? extends Writer>) c;
    }
  }

  /*
   * Searches for all nested classes within this class and recursively
   * adds them to a complete class list.
   */
  private List<Class<?>> buildClassList() {
    Class<?>[] classes = this.getClass().getDeclaredClasses();
    List<Class<?>> classList = new ArrayList<Class<?>>();
    
    for (Class<?> c : classes) {
      check(c, classList);
    }
    
    return classList;
  }

  /*
   * Recursive method to add a class, and all nested classes declared in that
   * class, to the provided list of classes.
   */
  private void check(Class<?> newClass, List<Class<?>> classList) {
    classList.add(newClass);
    
    for (Class<?> c : newClass.getDeclaredClasses())
      check(c, classList);
    
  }
}
