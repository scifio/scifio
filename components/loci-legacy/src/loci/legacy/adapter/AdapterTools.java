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

/**
 * Serves as a static context definition of {@link AdapterHelper} 
 * so that a consistent {@link LegacyAdapter} mapping can be maintained.
 * 
 * @author Mark Hiner
 */
public final class AdapterTools {

  private static AdapterHelper helper;
  
  static {
    helper = new AdapterHelper();
  }
  
  // -- Adapter Retrieval --
  
  /**
   * Uses an appropriate LegacyAdapter, if it exists, to return a paired
   * instance for the provided object. This allows the object to be used
   * in contexts it was not originally developed for.
   * 
   * @param modern
   * @return
   */
  public static Object get(Object toAdapt) {
    return helper.get(toAdapt);
  }

  /**
   * Uses an appropriate LegacyAdapter, if it exists, to map the
   * provided key (weakly) to the provided value.
   * 
   * @param key
   * @param value
   */
  public static void map(Object key, Object value) {
    helper.map(key, value);
  }
  
  // -- Deprecated Methods --
  
  /**
   * Returns an instance of the requested adapter. This will always be
   * the same instance for a given class, essentially creating a context
   * for consistent mapping.
   * 
   * @param adapterClass
   * @return
   */
  @Deprecated
  public static <T extends LegacyAdapter> T getAdapter(Class<T> adapterClass) {
    return helper.getAdapter(adapterClass);
  }
}