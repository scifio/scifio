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
package loci.legacy.adapter;

import java.util.HashMap;

/**
 * Maintains a context of {@link LegacyAdapter}s for wrapping (and thereby delegating)
 * between Legacy classes and their Modern equivalents.
 * <p>
 * As adapters use HashTables to manage delegation between instances, this ensures
 * a single instance of every mapping is available within this context, to avoid unnecessary
 * or repetitive wrappings.
 * </p>
 * 
 * @author Mark Hiner
 *
 */
public class AdapterHelper {

  // -- Fields --
  
  /** Maps LegacyAdapter classes to an instance of that class, so that a single instance of 
   * each adapter is maintained.
   */
  private HashMap<Class<? extends LegacyAdapter<?, ?>>, Object> adapterMap =
         new HashMap<Class<? extends LegacyAdapter<?, ?>>, Object>();
  
  // -- Adapter Retrieval --
  
  /**
   * Looks up the adapter instance 
   * @return An adapter for converting between legacy and modern IRandomAccess objects
   */
  @SuppressWarnings("unchecked")
  public <T extends LegacyAdapter<?, ?>> T getAdapter(Class<T> adapterClass) {    
    T adapter = (T) adapterMap.get(adapterClass);
    
    if(adapter == null) {
      try {
        adapter = adapterClass.newInstance();
        adapterMap.put(adapterClass, adapter);
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return adapter;
  }
}
