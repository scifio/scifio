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
 * Interface for all {@link ome.scifio.Translator} implementations that use
 * generic parameters.
 * <p>
 * Generics allow each concrete {@code Translator} interface to type narrow
 * the return types of {@code Translator} methods that return SCIFIO components.
 * Additionally, parallel methods are defined that accept SCIFIO components,
 * allowing the parameters to be type narrowed as well.
 * </p>
 * 
 * @author Mark Hiner
 *
 * @param <M> The translation source's {@link ome.scifio.Metadata} type.
 * @param <N> The translation destination's {@code Metadata} type.
 */
public interface TypedTranslator<M extends TypedMetadata, N extends TypedMetadata> extends Translator {

  /**
   * Generic-parameterized {@code translate} method, using 
   * {@link ome.scifio.TypedMetadata} to avoid type erasure conflicts with
   * {@link ome.scifio.Translator#translate(Metadata, Metadata)}.
   * 
   * @see {@link ome.scifio.Translator#translate(Metadata, Metadata)}
   */
  void translate(final M source, final N destination);
}
