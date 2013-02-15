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

import java.util.List;

/**
 * Interface for all SCIFIO formats.
 * <p>
 * The {@code Format} is a bag for all other SCIFIO components associated
 * with image IO in a given image format. It acts as a bridge and a black box
 * for allowing components to access each other.
 * </p>
 * <p>
 * NB: {@code Formats} are singletons within a given {@link ome.scifio.SCIFIO}
 * context. They can be automatically discovered via SezPoz when constructing a
 * new context, and thus it is rare that they should be manually constructed.
 * </p>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 * 
 * @author Mark Hiner
 * @see ome.scifio.SCIFIO#getFormatFromClass(Class);
 */
public interface Format
  extends HasContext, Comparable<Format> {

  // -- Format API methods --

  /** Returns the priority of this format.  Used for comparison to other formats. */
  Double getPriority();
  
  /** Gets the name of this file format. */
  String getFormatName();

  /** Gets the default file suffixes for this file format. */
  String[] getSuffixes();
  
  /** 
   * Gets a list of all Trnaslator classes capable of translating to or from
   * {@code Metadata} of this {@code Format}.
   * @return
   */
  List<Class<? extends Translator>> getTranslatorClassList();
  
  /**
   * Casts the provided {@link ome.scifio.Metadata} instance to the generic
   * type of this method.
   * <p>
   * NB: This method will fail, throwing an exception, if provided with a
   * {@code Metadata} implementation not associated with this {@code Format}.
   * </p>
   * <p>
   * This method is intended to be used internally or by advanced developers
   * within intermediate layers of SCIFIO code, where generic parameters may exist
   * but types aren't guaranteed (e.g. because {@link ome.scifio.HasFormat#getFormat()}
   * always returns an unparameterized {@link ome.scifio.Format}).
   * </p>
   * <p>
   * This method should be invoked by providing a generic parameter, e.g.:
   * </p>
   * <p>
   * {@code format.<M>castToTypedMetadata(meta)}.
   * </p>
   * <p>
   * where the generic parameter will resolve to the Format's Metadata type. Any
   * other use is unsupported and will throw an exception.
   * </p>
   * 
   * @param M the Metadata type to cast. Should match this Format's Metadata
   *        type.
   * @param meta an unknown Metadata object that will be cast to a more
   *        specific type
   * @throws IllegalArgumentException if meta is not assignable from this
   *         Format's Metadata type.
   * @return The meta parameter, cast as an M.
   * @see {@link ome.scifio.Format#createMetadata()}
   * @see {@link ome.scifio.HasFormat#getFormat()}
   */
//  <M extends Metadata> M castToTypedMetadata(Metadata meta);

  /**
   * Create the Metadata object associated with this format.
   * @return
   * @throws FormatException
   */
  Metadata createMetadata() throws FormatException;

  /**
   * Create the Checker object associated with this format.
   * @return
   * @throws FormatException
   */
  Checker createChecker() throws FormatException;

  /**
   * Create the Parser object associated with this format.
   * @return
   * @throws FormatException
   */
  Parser createParser() throws FormatException;

  /**
   * Creates the Reader object associated with this format.
   * @return
   * @throws FormatException
   */
  Reader createReader() throws FormatException;

  /**
   * Creates the Writer object associated with this format.
   * @return
   * @throws FormatException
   */
  Writer createWriter() throws FormatException;
   
  /**
   * Returns the class of the Metadata associated with this format
   * @return
   */
  Class<? extends Metadata> getMetadataClass();

  /**
   * Returns the class of the Checker associated with this format
   * @return
   */
  Class<? extends Checker> getCheckerClass();

  /**
   * Returns the class of the Parser associated with this format
   * @return
   */
  Class<? extends Parser> getParserClass();

  /**
   * Returns the class of the Reader associated with this format
   * @return
   */
  Class<? extends Reader> getReaderClass();

  /**
   * Returns the class of the Writer associated with this format
   * @return
   */
  Class<? extends Writer> getWriterClass();
  
  /**
   * Returns a translator capable of translating from this format's Metadata
   * to the target Metadata class. (this format's Metadata is the source)
   * 
   * @param <N>
   * @param targetMeta
   * @return
   */
  Translator findSourceTranslator(Metadata targetMeta)
    throws FormatException;

  /**
   * Returns a translator capable of translating from the target Metadata to this
   * format's Metadata (this Format's metadata is the destination).
   * 
   * @param <N>
   * @param targetMeta
   * @return
   */
  Translator findDestTranslator(Metadata targetMeta)
    throws FormatException;
}
