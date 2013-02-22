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

/**
 * Interface for all SCIFIO {@link ome.scifio.Format} implementations that
 * use generic parameters.
 * <p>
 * The SCIFIO component interfaces were written to be general and flexible.
 * Practically though, concrete implementations of a given component are only
 * going to interact with other components of the same {@code Format}. Thus
 * generics are used to encode this relationship between components.
 * </p>
 * 
 * @author Mark Hiner
 *
 * @param <M> The {@link ome.scifio.Metadata} type of this {@code Format}.
 * @param <C> The {@link ome.scifio.Checker} type of this {@code Format}.
 * @param <P> The {@link ome.scifio.Parser} type of this {@code Format}.
 * @param <R> The {@link ome.scifio.Reader} type of this {@code Format}.
 * @param <W> The {@link ome.scifio.Writer} type of this {@code Format}.
 */
public interface TypedFormat<M extends TypedMetadata, C extends Checker,
  P extends TypedParser<M>, R extends TypedReader<M, ? extends DataPlane<?>>,
  W extends TypedWriter<M>> extends Format {

  /*
   * @see ome.scifio.Format#createMetadata()
   */
  M createMetadata() throws FormatException;

  /*
   * @see ome.scifio.Format#createChecker()
   */
  C createChecker() throws FormatException;

  /*
   * @see ome.scifio.Format#createParser()
   */
  P createParser() throws FormatException;

  /*
   * @see ome.scifio.Format#createReader()
   */
  R createReader() throws FormatException;

  /*
   * @see ome.scifio.Format#createWriter()
   */
  W createWriter() throws FormatException;
   
  /*
   * @see ome.scifio.Format#getMetadataClass()
   */
  Class<M> getMetadataClass();

  /*
   * @see ome.scifio.Format#getCheckerClass()
   */
  Class<C> getCheckerClass();

  /*
   * @see ome.scifio.Format#getParserClass()
   */
  Class<P> getParserClass();

  /*
   * @see ome.scifio.Format#getReaderClass()
   */
  Class<R> getReaderClass();

  /*
   * @see ome.scifio.Format#getWriterClass()
   */
  Class<W> getWriterClass();
}
