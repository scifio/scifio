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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ome.scifio.io.IRandomAccess;
import ome.scifio.io.IStreamAccess;
import ome.scifio.io.Location;
import ome.scifio.io.NIOFileHandle;

import org.scijava.InstantiableException;
import org.scijava.service.Service;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Hiner
 *
 */
@Plugin(type = Service.class)
public class DefaultLocationService extends AbstractService implements LocationService {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Location.class);
 
  // -- Static fields --

  /** Map from given filenames to actual filenames. */
  private HashMap<String, Object> idMap =
    new HashMap<String, Object>();

  private volatile boolean cacheListings = false;
  
  // By default, cache for one hour.
  private volatile long cacheNanos = 60L * 60L * 1000L * 1000L * 1000L;

  protected class ListingsResult {
    public final String [] listing;
    public final long time;
    ListingsResult(String [] listing, long time) {
      this.listing = listing;
      this.time = time;
    }
  }
  private ConcurrentHashMap<String, ListingsResult> fileListings =
    new ConcurrentHashMap<String, ListingsResult>();
  
  // -- Location API methods --

  /**
   * Clear all caches and reset cache-related bookkeeping variables to their
   * original values.
   */
  public void reset() {
    cacheNanos = 60L * 60L * 1000L * 1000L * 1000L;
    fileListings.clear();
    getIdMap().clear();
  }

  /**
   * Turn cacheing of directory listings on or off.
   * Cacheing is turned off by default.
   *
   * Reasons to cache - directory listings over network shares
   * can be very expensive, especially in HCS experiments with thousands
   * of files in the same directory. Technically, if you use a directory
   * listing and then go and access the file, you are using stale information.
   * Unlike a database, there's no transactional integrity to file system
   * operations, so the directory could change by the time you access the file.
   *
   * Reasons not to cache - the contents of the directories might change
   * during the program invocation.
   *
   * @param cache - true to turn cacheing on, false to leave it off.
   */
  public void cacheDirectoryListings(boolean cache) {
    cacheListings = cache;
  }

  /**
   * Cache directory listings for this many seconds before relisting.
   *
   * @param sec - use the cache if a directory list was done within this many
   * seconds.
   */
  public void setCacheDirectoryTimeout(double sec) {
    cacheNanos = (long)(sec * 1000. * 1000. * 1000.);
  }

  /**
   * Clear the directory listings cache.
   *
   * Do this if directory contents might have changed in a significant way.
   */
  public void clearDirectoryListingsCache() {
    fileListings = new ConcurrentHashMap<String, ListingsResult>();
  }

  /**
   * Remove any cached directory listings that have expired.
   */
  public void cleanStaleCacheEntries() {
    long t = System.nanoTime() - cacheNanos;
    ArrayList<String> staleKeys = new ArrayList<String>();
    for (String key : fileListings.keySet()) {
      if (fileListings.get(key).time < t) {
        staleKeys.add(key);
      }
    }
    for (String key : staleKeys) {
      fileListings.remove(key);
    }
  }

  /**
   * Maps the given id to an actual filename on disk. Typically actual
   * filenames are used for ids, making this step unnecessary, but in some
   * cases it is useful; e.g., if the file has been renamed to conform to a
   * standard naming scheme and the original file extension is lost, then
   * using the original filename as the id assists format handlers with type
   * identification and pattern matching, and the id can be mapped to the
   * actual filename for reading the file's contents.
   * @see #getMappedId(String)
   */
  public void mapId(String id, String filename) {
    if (id == null) return;
    if (filename == null) getIdMap().remove(id);
    else getIdMap().put(id, filename);
    LOGGER.debug("Location.mapId: {} -> {}", id, filename);
  }

  /** Maps the given id to the given IRandomAccess object. */
  public void mapFile(String id, IRandomAccess ira) {
    if (id == null) return;
    if (ira == null) getIdMap().remove(id);
    else getIdMap().put(id, ira);
    LOGGER.debug("Location.mapFile: {} -> {}", id, ira);
  }

  /**
   * Gets the actual filename on disk for the given id. Typically the id itself
   * is the filename, but in some cases may not be; e.g., if OMEIS has renamed
   * a file from its original name to a standard location such as Files/101,
   * the original filename is useful for checking the file extension and doing
   * pattern matching, but the renamed filename is required to read its
   * contents.
   * @see #mapId(String, String)
   */
  public String getMappedId(String id) {
    if (getIdMap() == null) return id;
    String filename = null;
    if (id != null && (getIdMap().get(id) instanceof String)) {
      filename = (String) getIdMap().get(id);
    }
    return filename == null ? id : filename;
  }

  /** Gets the random access handle for the given id. */
  public IRandomAccess getMappedFile(String id) {
    if (getIdMap() == null) return null;
    IRandomAccess ira = null;
    if (id != null && (getIdMap().get(id) instanceof IRandomAccess)) {
      ira = (IRandomAccess) getIdMap().get(id);
    }
    return ira;
  }

  /** Return the id mapping. */
  public HashMap<String, Object> getIdMap() { return idMap; }

  /**
   * Set the id mapping using the given HashMap.
   *
   * @throws IllegalArgumentException if the given HashMap is null.
   */
  public void setIdMap(HashMap<String, Object> map) {
    if (map == null) throw new IllegalArgumentException("map cannot be null");
    idMap = map;
  }

  /**
   * Gets an IRandomAccess object that can read from the given file.
   * @see IRandomAccess
   */
  public IRandomAccess getHandle(String id) throws IOException {
    return getHandle(id, false);
  }

  /**
   * Gets an IRandomAccess object that can read from or write to the given file.
   * @see IRandomAccess
   */
  public IRandomAccess getHandle(String id, boolean writable)
    throws IOException
  {
    return getHandle(id, writable, true);
  }

  /**
   * Gets an IRandomAccess object that can read from or write to the given file.
   * @see IRandomAccess
   */
  public IRandomAccess getHandle(String id, boolean writable,
    boolean allowArchiveHandles) throws IOException
  {
    LOGGER.trace("getHandle(id = {}, writable = {})", id, writable);
    IRandomAccess handle = getMappedFile(id);
    if (handle == null) {
      LOGGER.trace("no handle was mapped for this ID");
      String mapId = getMappedId(id);

      final List<PluginInfo<IStreamAccess>> streamInfos = 
          getContext().getPluginIndex().getPlugins(IStreamAccess.class);
     
      if (allowArchiveHandles) {
        for (final PluginInfo<IStreamAccess> info : streamInfos) {
          if (((IStreamAccess)(handle =
              getContext().getService(PluginService.class).createInstance(info))).isConstructable(id)) {
            ((IStreamAccess)handle).setFile(id);
            break;
          }
          handle = null;
        }
      }
      
      if (handle == null)
        handle = new NIOFileHandle(mapId, writable ? "rw" : "r");
      
    }
    LOGGER.trace("Location.getHandle: {} -> {}", id, handle);
    return handle;
  }

  /**
   * Checks that the given id points at a valid data stream.
   * 
   * @param id
   *          The id string to validate.
   * @throws IOException
   *           if the id is not valid.
   */
  public void checkValidId(String id) throws IOException {
    if (getMappedFile(id) != null) {
      // NB: The id maps directly to an IRandomAccess handle, so is valid. Do
      // not destroy an existing mapped IRandomAccess handle by closing it.
      return;
    }
    // NB: Try to actually open a handle to make sure it is valid. Close it
    // afterward so we don't leave it dangling. The process of doing this will
    // throw IOException if something goes wrong.
    getHandle(id).close();
  }
  
  public String[] getCachedListing(String key) {
    ListingsResult listingsResult = null;
    if (cacheListings) {
      cleanStaleCacheEntries();
      listingsResult = fileListings.get(key);
    }
    return listingsResult == null ? null : listingsResult.listing;
  }
  
  public void putCachedListing(String key, String[] listing) {
    if (cacheListings) {
      fileListings.put(key, new ListingsResult(listing, System.nanoTime()));
    }
  }
}
