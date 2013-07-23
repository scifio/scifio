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

import io.scif.img.cell.SCIFIOCell;

import java.util.Set;
import java.util.TreeSet;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * {@link CacheService} implementation using <a href="http://www.mapdb.org/">MapDB</a>
 * to store and retrieve {@link SCIFIOCell}s on disk.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class MapDBCache extends AbstractCacheService<SCIFIOCell<?>> {

  // -- Constants -- 
  
  /**
   *  Number of modifications that are made to the DB before committing
   */
  public static final int CACHE_BUFFER = 100;
  
  // -- Fields --
  
  // Disk-backed database to for writing
  private DB db;
  
  // List of caches
  private Set<String> caches = new TreeSet<String>();

  // Modification tracker
  private int commits = 0;
  
  // Maximum cache size, in bytes
  private long maxCacheSize = Long.MAX_VALUE;

  // -- CacheService API Methods --
  
  /*
   * @see io.scifio.io.img.cell.CacheService#clearCache(java.lang.String)
   */
  public void clearCache(String cacheId) {
    if (caches.contains(cacheId)) {
      db.getHashMap(cacheId).clear();
    }
  }
  
  /*
   * @see io.scif.img.cell.cache.CacheService#clearAllCaches()
   */
  public void clearAllCaches() {
    for (String cache : caches) clearCache(cache);
  }

  /*
   * @see io.scif.io.img.cell.cache.CacheService#dropCache(java.lang.String)
   */
  public void dropCache(String cacheId) {
    if (caches.contains(cacheId)) {
      db.getHashMap(cacheId).close();
      caches.remove(cacheId);
    }
  }
  
  /*
   * @see io.scif.io.img.cell.cache.CacheService#addCache(java.lang.String)
   */
  public void addCache(String cacheId) {
    caches.add(cacheId);
  }
  
  /*
   * @see io.scifio.io.img.cell.CacheService#cache(java.lang.String, int, java.io.Serializable)
   */
  public boolean cache(String cacheId, int index, SCIFIOCell<?> object) {
    boolean success = false;
    object.update();
    if (caches.contains(cacheId) && (cacheAll() || object.dirty())) {
      
      HTreeMap<Object, Object> cache = db.getHashMap(cacheId);
      
      // Will another object fit?
      if ((cache.size() + 1) * object.getElementSize() < maxCacheSize) diskIsFull(false);
      else diskIsFull(true);
      
      // If the cache is enabled and there's room on disk, cache and commit
      if (enabled() && !diskFull()) {
        cache.put(getKey(cacheId, index), object);
        success = true;
        commit();
      }
    }
    return success;
  }

  /*
   * @see io.scifio.io.img.cell.CacheService#get(java.lang.String, int)
   */
  public SCIFIOCell<?> retrieve(String cacheId, int index) {
    
    Integer key = getKey(cacheId, index);
    
    SCIFIOCell<?> cell = null;
    HTreeMap<?, ?> cache = db.getHashMap(cacheId);
    
    boolean success = false;

    // wait for memory to clear and the read to succeed
    while (!success) {
      try {
        if (cache.containsKey(key)) {
          cell = (SCIFIOCell<?>) cache.get(key);
        }
        success = true;
      } catch (OutOfMemoryError e) { }
    }
    
    if (cell != null) {
      cache.remove(key);
      cell.setCacheId(cacheId);
      cell.setIndex(index);
      cell.setService(this);
      
      commit();
    }
    
    return cell;
  }
  
  @Override
  public void setMaxBytesOnDisk(long maxBytes) {
    maxCacheSize = maxBytes;
  }
  
  // -- Service API Methods --
  
  @Override
  public void initialize() {
      db = DBMaker.newTempFileDB()
          .closeOnJvmShutdown()
          .cacheDisable()
          .randomAccessFileEnableIfNeeded()
          .deleteFilesAfterClose()
          .make();
  }
  
  @Override
  public void dispose() {
    clearAllCaches();
    db.close();
  }
  
  // -- Helper Methods --
  
  private void commit() {
    //NB: the more frequently a commit is made, the worse performance, but the less memory usage.
    if (++commits > CACHE_BUFFER) {
      commits = 0;
      db.commit();
    }
  }
}
