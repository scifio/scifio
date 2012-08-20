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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.io.RandomAccessInputStream;

public abstract class AbstractMetadata extends AbstractHasContext
  implements Metadata {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

  // -- Fields --
  private RandomAccessInputStream source;
  
  protected boolean filtered;

  // -- HasFormat API Methods --

  public Format<?, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromMetadata(getClass());
  }

  // -- Constructor --

  public AbstractMetadata(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Metadata API Methods --

  /* @see Metadata#resetMeta(Class<?>) */
  public void reset(final Class<?> type) {
    if (type == null || type == AbstractMetadata.class) return;

    for (final Field f : type.getDeclaredFields()) {
      f.setAccessible(true);

      if (Modifier.isFinal(f.getModifiers())) continue;
      final Class<?> fieldType = f.getType();
      try {
        if (fieldType == boolean.class) f.set(this, false);
        else if (fieldType == char.class) f.set(this, 0);
        else if (fieldType == double.class) f.set(this, 0.0);
        else if (fieldType == float.class) f.set(this, 0f);
        else if (fieldType == int.class) f.set(this, 0);
        else if (fieldType == long.class) f.set(this, 0l);
        else if (fieldType == short.class) f.set(this, 0);
        else f.set(this, null);
      }
      catch (final IllegalArgumentException e) {
        LOGGER.debug(e.getMessage());
      }
      catch (final IllegalAccessException e) {
        LOGGER.debug(e.getMessage());
      }

      // check superclasses and interfaces
      reset(type.getSuperclass());
      for (final Class<?> c : type.getInterfaces()) {
        reset(c);
      }
    }
  }

  /* @see Metadata#setSource(RandomAccessInputStream) */
  public void setSource(final RandomAccessInputStream source) {
    this.source = source;
  }

  /* @see Metadata#getSource() */
  public RandomAccessInputStream getSource() {
    return this.source;
  }
  
  /* @see Metadata#isFiltered() */
  public boolean isFiltered() {
    return this.filtered;
  }

}
