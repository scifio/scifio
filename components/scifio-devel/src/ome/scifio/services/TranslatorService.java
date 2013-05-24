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

import ome.scifio.Metadata;
import ome.scifio.Translator;

import org.scijava.service.Service;

/**
 * Collection of methods for creating and using appropriate
 * Translators to convert between Metadata types.
 * 
 * @see ome.scifio.Translator
 * 
 * @author Mark Hiner
 *
 */
public interface TranslatorService extends Service {
  
  /**
   * Returns a translator capable of translating from the source Metadata to
   * the dest Metadata type, or null if no such Translator exists.
   * 
   * @param source - Metadata to read from
   * @param dest - Metadata to populate
   * @param boolean - true if a translator that exactly matches these Metadata is
   *                  desired
   * @return Translator whose translate method will accept these Metadata
   * 				 instances
   */
  Translator findTranslator(Metadata source, Metadata dest, boolean exact);
  
  /**
   * Returns a translator capable of translating from the source Metadata to
   * the dest Metadata type, or null if no such Translator exists.
   * 
   * @param source - Metadata to read from
   * @param dest - Metadata to populate
   * @param boolean - true if a translator that exactly matches these Metadata is
   *                  desired
   * @return Translator whose translate method will accept these Metadata
   * 				 instances
   */
  Translator findTranslator(Class<?> source, Class<?> dest, boolean exact);
  
  /**
   * Convenience method to immediately translate from the source Metadata
   * to the dest Metadata type, assuming an appropriate Translator is found.
   * <p>
   * Useful if you don't need a handle on the Translator itself.
   * </p>
   * 
   * @param source - Metadata to read from
   * @param dest - Metadata to populate
   * @param boolean - true if a translator that exactly matches these Metadata is
   *                  desired
   * @return true if translation was successful, and false otherwise.
   */
  boolean translate(Metadata source, Metadata dest, boolean exact);
}
