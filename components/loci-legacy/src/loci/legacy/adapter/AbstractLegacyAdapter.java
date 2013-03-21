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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Abstract superclass of all {@link LegacyAdapter} implementations.
 * <p>
 * Uses a {@link WeakHashMap} implementation for maintaining associations between
 * legacy and modern classes. "True" instances of each class map to wrappers of their
 * companion classes.
 * </p>
 * <p>
 * NB: because wrappers are the values in the map, they must be expected to delegate to their key
 * objects (wrapper values wrap their keys). <br>WeakHashMaps are used to ensure garbage collection when
 * there are no other references to the Legacy/modern pairings. <br>However, since the values in each
 * pairing wraps the key, there is always a strong reference to each key. Thus we map each key 
 * to a {@link WeakReference} of the wrapper.
 * </p>
 * 
 * @author Mark Hiner
 *
 */
public abstract class AbstractLegacyAdapter<L, M> implements LegacyAdapter {
  
  // -- Fields --
  
  private WeakHashMap<L, M> legacyToModern = new WeakHashMap<L, M>();
  private WeakHashMap<M, L> modernToLegacy = new WeakHashMap<M, L>();
  
  private Class<L> legacyClass;
  private Class<M> modernClass;
  
  // -- Constructor --
  
  public AbstractLegacyAdapter(Class<L> legacyClass, Class<M> modernClass) {
    this.legacyClass = legacyClass;
    this.modernClass = modernClass;
  }
  
  // -- LegacyAdapter API --
  
  /*
   * @see loci.legacy.adapter.LegacyAdapter#get(java.lang.Object)
   */
  public Object get(Object toAdapt) {
    // Check if we were given a modern instance
    M modern = modernCheck(toAdapt);
    if (modern != null) return getTyped(modern, modernToLegacy);
    
    // Check for a legacy instance
    L legacy = legacyCheck(toAdapt);
    if (legacy != null) return getTyped(legacy, legacyToModern);
    
    return null;
  }
  
  /*
   * @see loci.legacy.adapter.LegacyAdapter#mapModern(java.lang.Object, java.lang.Object)
   */
  public void map(Object key, Object value) {
    // Check to see if we have a modern key with legacy value
    M modern = modernCheck(key);
    L legacy = legacyCheck(value);
    
    if (modern != null && legacy != null) {
      // got the right types. Put them in the appropriate map.
      mapTyped(modern, legacy, modernToLegacy);
      return;
    }
    
    // Didn't have modern:legacy, so we try legacy:modern
    modern = modernCheck(value);
    legacy = legacyCheck(key);
    
    if (modern != null && legacy != null)
      mapTyped(legacy, modern, legacyToModern);
  }
  
  /* See LegacyAdapter#clear() */
  public void clear() {
    synchronized (legacyToModern) {
      legacyToModern.clear();
    }
    synchronized (modernToLegacy) {
      modernToLegacy.clear();
    }
  }
  
  /*
   * @see loci.legacy.adapter.LegacyAdapter#wrap(java.lang.Object)
   */
  public Object wrap(Object toWrap) {
    M modern = modernCheck(toWrap);
    if (modern != null) return wrapToLegacy(modern);
    
    L legacy = legacyCheck(toWrap);
    if (legacy != null) return wrapToModern(legacy);
    
    return null;
  }
  
  /*
   * @see loci.legacy.adapter.LegacyAdapter#getLegacyClass()
   */
  public Class<L> getLegacyClass() {
    return legacyClass;
  }
  
  /*
   * @see loci.legacy.adapter.LegacyAdapter#getModernClass()
   */
  public Class<M> getModernClass() {
    return modernClass;
  }
  
  // -- Abstract API Methods --

  /**
   * Used Wraps the given modern object to a new instance of its legacy equivalent.
   * <p>
   * This is a "stupid" operation that always wraps to a new instance.
   * </p>
   * <p>
   * This method must be defined at the concrete implementation level as it requires
   * knowledge of a specific class that extends L but is capable of wrapping M.
   * Doing so reduces code/logic repetition by maintaining a single getLegacy
   * implementation.
   * </p>
   * @param modern - An instance of the modern class.
   * @return A legacy instance wrapping the provided modern instance.
   */
  protected abstract L wrapToLegacy(M modern);

  /**
   * Wraps the given legacy object to a new instance of its modern equivalent.
   * <p>
   * This is a "stupid" operation that always wraps to a new instance.
   * </p>
   * <p>
   * This method must be defined at the concrete implementation level as it requires
   * knowledge of a specific class that extends M but is capable of wrapping L. 
   * Doing so reduces code/logic repetition by maintaining a single getModern
   * implementation.
   * </p>
   * @param legacy - An instance of the legacy class.
   * @return A modern instance wrapping the provided legacy instance.
   */ 
  protected abstract M wrapToModern(L legacy);
  
  // -- Helper methods --
  
  private <T, S> T getTyped(S toAdapt, WeakHashMap<S, T> map) {
    T ret = this.<T>wrapperCheck(toAdapt);
    
    if (ret != null) return ret;
    
    synchronized (map) {
      ret = map.get(toAdapt);
    }
    
    if (ret == null) {
      ret = (T) wrap(toAdapt);
      
      synchronized(map) {
        map.put(toAdapt, ret);
      }
    }
    
    return ret;
  }
  
  /*
   * Creates a key:value mapping in the provided HashMap.
   * A thread-safe operation.
   */
  private <T, S> void mapTyped(T key, S value, WeakHashMap<T, S> map) {
    synchronized (map) {
      map.put(key, value);
    }
  }
  
  /*
   * Returns the given object cast to an M, or null if the types are incompatible,
   * or obj is null;
   */
  private M modernCheck(Object obj) {
    return typeCheck(obj, modernClass);
  }
 
  /*
   * Returns the given object cast to an M, or null if the types are incompatible,
   * or obj is null;
   */
  private L legacyCheck(Object obj) {
    return typeCheck(obj, legacyClass);
  }
  
  /*
   * Casts the provided obj to the specified typeClass if possible.
   * Returns null if the cast wasn't allowed, or if obj is null.
   */
  private <T> T typeCheck(Object obj, Class<T> typeClass) {
    if (obj == null) return null;
    
    if (!typeClass.isAssignableFrom(obj.getClass())) return null;
    
    @SuppressWarnings("unchecked")
    T typed = (T)obj;
    
    return typed;
  }
  
  /*
   * Unwraps the provided object, if possible.
   */
  private <T> T wrapperCheck(Object obj) {
    if (obj instanceof Wrapper) {
      // object is a wrapper, so unwrap it
      @SuppressWarnings("unchecked")
      Wrapper<T> fake = (Wrapper<T>)obj;
      return fake.unwrap();
    }
    return null;
  }
}