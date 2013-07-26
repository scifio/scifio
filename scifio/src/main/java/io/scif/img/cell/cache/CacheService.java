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

package io.scif.img.cell.cache;

import java.io.Serializable;

import org.scijava.service.Service;

/**
 * Interface for caching and retrieving objects.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public interface CacheService<T extends Serializable> extends Service {
  
  /**
   * Removes all entries from the specified cache
   * @param cacheId cache name to clear
   */
  void clearCache(String cacheId);
  
  /**
   * Removes all entries for all caches
   */
  void clearAllCaches();
  
  /**
   * Closes and removes the specified cache.
   * @param cacheId cache name to remove
   */
  void dropCache(String cacheId);
  
  /**
   * Creates a cache using the specified id
   * @param cacheId cache to create
   */
  void addCache(String cacheId);
  
  /**
   * Caches the provided object. The cacheId and index
   * are used as a hash key.
   * <p>
   * NB: If successful, after invoking this method, the cached object
   * should be considered "clean" in a dirty/clean context.
   * </p>
   * 
   * @param cacheId - Cache this object belongs to
   * @param index - Index in the cache of this object
   * @param object - object to store
   * @return CacheResult based on the outcome
   */
  CacheResult cache(String cacheId, int index, T object);
  
  /**
   * Removes and returns the object at the desired
   * index from the specified index.
   * 
   * @param cacheId - Cache the desired object belongs to
   * @param index - Index in the cache of the desired object
   * @return The cached object for the specified id and index
   */
  T retrieve(String cacheId, int index);
  
  /**
   * @param cacheId - Cache the desired object belongs to
   * @param index - Index in the cache of the desired object
   * @return Hashed value of the cacheId and index
   */
  Integer getKey(String cacheId, int index);

  /**
   * Sets the amount of disk space available to caches
   * created by this service's manager.
   * 
   * @param maxBytes - max bytes usable by this manager's disk stores.
   */
  void setMaxBytesOnDisk(long maxBytes);
  
  /**
   * @return True if this CacheService can write to disk.
   */
  boolean enabled();
  
  /**
   * @param enabled If true, disk caching will be enabled. If false,
   *        no writes to disk will occur.
   */
  void enable(boolean enabled);
  
  /**
   * @param enabled If true, all records will be cached, not just
   *        those that are dirty.
   */
  void cacheAll(boolean enabled);
}