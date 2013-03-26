/*
 * #%L
 * Legacy layer preserving compatibility between legacy Bio-Formats and SCIFIO.
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

package loci.legacy.adapter;

import org.scijava.plugin.SciJavaPlugin;

/**
 * This interface represents adapter classes for delegating between equivalent "Legacy"
 * and "Modern" classes. Such delegation is intended to facilitate backwards
 * compatibility: given a subclass that extends or implements
 * a Legacy or Modern class, methods requiring the other can be executed
 * on instances of the subclass by using {@link LegacyAdapter#get()} calls.
 * <p>
 * This adapter is intended to be used to facilitate delegation between
 * interfaces, which can not themselves contain implementation. Abstract and concrete
 * classes can use a "hasa" relationship to delegate functionality. This is useful
 * in conjunction with the {@link Wrapper} API.
 * </p>
 * <p>
 * NB: If a package contains extensions or implementations of a Legacy or
 * Modern class, and methods that operate on the extensions (instead of on the
 * base Legacy or Modern class) it can not be made backwards compatible,
 * even with an adapter, and thus we recommend conversion to a fully
 * Modern-based implementation.
 * </p>
 * @author Mark Hiner
 */
public interface LegacyAdapter extends SciJavaPlugin {

  /**
   * Used to retrieve the paired instance (legacy or modern) associated with a given
   * object (modern or legacy).
   * 
   * @param toAdapt - Object for which to retrieve a paired instance
   * @return The paired instance
   */
  Object get(Object toAdapt);
  
  /**
   * Creates a key:value mapping, so for the lifetime of the key,
   * the value will always be returned if this adapter is queried.
   * 
   * @param key - Key entry
   * @param value - Value entry
   */
  void map(Object key, Object value);
  
  /**
   * @return the class of the associated Legacy type.
   */
  Class<?> getLegacyClass();
  
  /**
   * @return the class of the associated Modern type.
   */
  Class<?> getModernClass();

  /**
   * Resets any mappings in this adapter.
   */
  void clear();
}
