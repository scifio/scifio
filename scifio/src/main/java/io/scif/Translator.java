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
package io.scif;

/**
 * Interface for all SCIFIO {@code Translators}.
 * <p>
 * Multiple {@code Translators} can be defined for a given {@code Format}.
 * Each encodes a process for converting from one {@link io.scif.Metadata}
 * type to another (where either the source or the destination is the {@code Metadata}
 * type associated with this {@code Format}).
 * </p>
 * <p>
 * The {@link #translate(Metadata, Metadata)} method always accepts an instance
 * of both source and destination {@code Metadata}. This allows chaining of
 * multiple translators (or other methods) to populate a single instance of
 * {@code Metadata}.
 * </p>
 * <p>
 * If no {@code Metadata} instance is readily available for translation, it
 * can be created through the {@code Format}, and existing {@code Metadata}
 * instances can be reset to ensure no previous information persists.
 * </p>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 * 
 * @see io.scif.Format#createMetadata()
 * @see io.scif.Metadata#reset(Class)
 * @see io.scif.services.TranslatorService
 * 
 * @author Mark Hiner
 */
public interface Translator extends SCIFIOPlugin, HasSCIFIO {

  // -- Fields --
  
	/** Source key flag, for use in annotations. */
  public static final String SOURCE = "source";

  /** Destination key flag, for use in annotations. */
  public static final String DEST = "dest";
  
  // -- Translator API methods --

  /**
   * Uses the source {@code Metadata} to populate the destination {@code Metadata}
   * <p>
   * NB: this method accepts base {@code Metadata} parameters, but its behavior
   * is undefined if at least one {@code Metadata} instance is not of the type
   * associated with this {@code Translator's Format}. Neither can the other
   * {@code Metadata} be arbitrary, as an appropriate {@code Translator} must
   * be defined for the desired direction of translation. See 
   * {@link io.scif.Format#getTranslatorClassList()} for a list of classes
   * capable of translation with a given {@code Format's Metadata}.
   * </p>
   * <p>
   * Note that the destination does not have to be empty, but can be built up
   * through multiple translations. However each translation step is assumed
   * to overwrite any previously existing data.
   * </p>
   * <p>
   * For a reference to a fresh {@code Metadata} instance to use in translation,
   * consider the {@link io.scif.Format#createMetadata()} and
   * {@link io.scif.Metadata#reset(Class)} methods.
   * </p>
   * 
   * @param source {@code Metadata} to use to populate
   * @param destination {@code Metadata} to be populated
   * @see {@link io.scif.Format#createMetadata()}
   * @see {@link io.scif.Metadata#reset(Class)}
   * @see {@link io.scif.Format#getTranslatorClassList()}
   * @throws IllegalArgumentException if the arguments don't match the
   *         {@code Metadata} types used to query this {@code Translator}
   *         (e.g. via the {@link io.scif.Format#findDestTranslator} or
   *         {@link io.scif.Format#findSourceTranslator} methods).
   */
  void translate(final Metadata source, final Metadata destination);
}
