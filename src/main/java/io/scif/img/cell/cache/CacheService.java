/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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
 * #L%
 */

package io.scif.img.cell.cache;

import io.scif.SCIFIOService;

import java.io.Serializable;

/**
 * Interface for caching and retrieving objects.
 *
 * @author Mark Hiner
 */
public interface CacheService<T extends Serializable> extends SCIFIOService {

	/**
	 * Removes all entries from the specified cache. Note: this method is
	 * vulnerable to race conditions, as successful clearing requires that the
	 * items to be clear have actually made it to disk. Thus it should not be used
	 * if there is the chance that transactions are still in process.
	 *
	 * @param cacheId cache name to clear
	 */
	void clearCache(String cacheId);

	/**
	 * Removes all entries for all caches
	 *
	 * @see #clearCache(String)
	 */
	void clearAllCaches();

	/**
	 * Closes and removes the specified cache.
	 *
	 * @param cacheId cache name to remove
	 */
	void dropCache(String cacheId);

	/**
	 * Creates a cache using the specified id
	 *
	 * @param cacheId cache to create
	 */
	void addCache(String cacheId);

	/**
	 * Caches the provided object. The cacheId and index are used as a hash key.
	 * <p>
	 * NB: If successful, after invoking this method, the cached object should be
	 * considered "clean" in a dirty/clean context.
	 * </p>
	 *
	 * @param cacheId - Cache this object belongs to
	 * @param index - Index in the cache of this object
	 * @param object - object to store
	 * @return CacheResult based on the outcome
	 */
	CacheResult cache(String cacheId, int index, T object);

	/**
	 * Returns the object at the desired index from the specified index. The entry
	 * is not guaranteed to be removed from disk until the limit (set via
	 * {@link #setMaxBytesOnDisk(long)}) is reached. To encourage the deletion of
	 * these records early, use {@link #cleanRetrieved(String)}.
	 * <p>
	 * NB: the cell returned from this method will automatically attempt to
	 * re-cache itself when finalized. To disable this feature, use
	 * {@link #retrieveNoRecache(String, int)}.
	 * </p>
	 *
	 * @param cacheId - Cache the desired object belongs to
	 * @param index - Index in the cache of the desired object
	 * @return The cached object for the specified id and index
	 */
	T retrieve(String cacheId, int index);

	/**
	 * As {@link #retrieve(String, int)}, but any flag for automatic caching will
	 * be disabled if possible.
	 *
	 * @param cacheId - Cache the desired object belongs to
	 * @param index - Index in the cache of the desired object
	 * @return The cached object for the specified id and index
	 */
	T retrieveNoRecache(String cacheId, int index);

	/**
	 * Start a new thread to remove all previously-retrieved entries for a given
	 * cache from disk. This method will automatically be invoked when the byte
	 * limit, set by {@link #setMaxBytesOnDisk(long)}, is reached. But, although
	 * this process will run on a separate thread, there may be a performance hit
	 * to delete cache entries - e.g. if further caching is blocked. Use this
	 * method to pre-emptively control when records are deleted.
	 *
	 * @param cacheId - Cache to clean
	 */
	void cleanRetrieved(final String cacheId);

	/**
	 * @param cacheId - Cache the desired object belongs to
	 * @param index - Index in the cache of the desired object
	 * @return Hashed value of the cacheId and index
	 */
	Integer getKey(String cacheId, int index);

	/**
	 * Sets the amount of disk space available to caches created by this service's
	 * manager.
	 *
	 * @param maxBytes - max bytes usable by this manager's disk stores.
	 */
	void setMaxBytesOnDisk(long maxBytes);

	/**
	 * @return True if this CacheService can write to disk.
	 */
	boolean enabled();

	/**
	 * @param enabled If true, disk caching will be enabled. If false, no writes
	 *          to disk will occur.
	 */
	void enable(boolean enabled);

	/**
	 * @param enabled If true, all records will be cached, not just those that are
	 *          dirty.
	 */
	void cacheAll(boolean enabled);
}
