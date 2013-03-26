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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import loci.legacy.context.LegacyContext;

import org.scijava.plugin.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a list of {@link LegacyAdapter}s for wrapping (and thereby delegating)
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
  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(LegacyAdapter.class);
  
  // -- Fields --
  
  /*
   *  Maps Legacy and Modern classes to an instance of the adapter handling that class,
   *  to facilitate quick lookup when adaptation is requested.
   */
  private HashMap<Class<?>, LegacyAdapter> adapterIndex =
      new HashMap<Class<?>, LegacyAdapter>();

  /*
   * Maps LegacyAdapter classes to the corresponding instance of the adapter.
   */
  private HashMap<Class<? extends LegacyAdapter>, LegacyAdapter> classIndex =
      new HashMap<Class<? extends LegacyAdapter>, LegacyAdapter>();
  
  // -- Constructor --
  
  public AdapterHelper() {
    PluginService pService = LegacyContext.get().getService(PluginService.class);
    List<LegacyAdapter> adapterList = pService.createInstancesOfType(LegacyAdapter.class);
    
    for (LegacyAdapter adapter : adapterList) {
      classIndex.put(adapter.getClass(), adapter);
    }
  }
  
  // -- Adapter Retrieval --
  
  /**
   * Uses an appropriate LegacyAdapter, if it exists, to return a paired
   * instance for the provided object. This allows the object to be used
   * in contexts it was not originally developed for.
   * 
   * @param toGet - A legacy or modern instance
   * @return The corresponding modern or legacy instance
   */
  public Object get(Object toGet) {
    if (toGet == null) return null;
    
    // Look up the adapter
    LegacyAdapter adapter = getAdapterByObject(toGet.getClass());
    
    // If the adapter isn't null, delegate to it
    if (adapter == null) return null;
    
    return adapter.get(toGet);
  }
  
  /**
   * Uses an appropriate LegacyAdapter, if it exists, to map the
   * provided key (weakly) to the provided value.
   * 
   * @param key - Map key
   * @param value - Map value
   */
  public void map(Object key, Object value) {
  	// Look up the adapter
    LegacyAdapter adapter = getAdapterByObject(key.getClass());
    
    // If the adapter isn't null, perform the mapping
    if (adapter != null) adapter.map(key,  value);
  }
  
  // -- Deprecated Methods --
  
  /**
   * Looks up the cached adapter instance of the provided class type.
   * 
   * @param adapterClass - Class of the desired adapter
   * @return An instance of the requested adapter
   * 
   * @deprecated Use {@link loci.legacy.AdapterTools#get(Object)}
   */
  @Deprecated
  public <T extends LegacyAdapter> T getAdapter(Class<T> adapterClass) {
    T adapter = this.<T>safeCast(classIndex.get(adapterClass));

    return adapter;
  }
  
  // -- Helper Methods --
  
  /*
   * Convenience method for casting.
   */
  @SuppressWarnings("unchecked")
  private <T> T safeCast(Object o) {
    return (T)o;
  }
  
  /*
   * Returns an adapter capable of adapting to or from the provided
   * class.
   */
  private LegacyAdapter getAdapterByObject(Class<?> objectClass) {
    LegacyAdapter adapter = adapterIndex.get(objectClass);
    
    Iterator<LegacyAdapter> adapterIter = classIndex.values().iterator();
    
    /* If an adapter wasn't found, we don't have a mapping for this class
     * yet. So we search the list.
     */
    while (adapter == null && adapterIter.hasNext()) {
      LegacyAdapter tmpAdapter = adapterIter.next();
      
      if (tmpAdapter.getModernClass().isAssignableFrom(objectClass) ||
          tmpAdapter.getLegacyClass().isAssignableFrom(objectClass)) {

        // Found an adapter, so index it under this class.
        adapter = tmpAdapter;
        adapterIndex.put(objectClass, adapter);
      }
    }
    
    return adapter;
  }

}