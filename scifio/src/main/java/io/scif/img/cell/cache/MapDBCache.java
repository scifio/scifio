/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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
 * {@link CacheService} implementation using <a
 * href="http://www.mapdb.org/">MapDB</a> to store and retrieve
 * {@link SCIFIOCell}s on disk.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class MapDBCache extends AbstractCacheService<SCIFIOCell<?>> {

	// -- Fields --

	/** Disk-backed database for writing. */
	private DB db;

	/** List of caches. */
	private final Set<String> caches = new TreeSet<String>();

	/** Maximum cache size, in bytes. */
	private long maxCacheSize = Long.MAX_VALUE;

	// -- CacheService API Methods --

	/**
	 * NB: Disables caching on finalize in cleared cells. This is done to prevent
	 * immortal cells. Caching must be re-enabled per-cell if desired.
	 * 
	 * @see CacheService#clearCache(String)
	 */
	@Override
	public void clearCache(final String cacheId) {
		if (caches.contains(cacheId)) {
			final HTreeMap<?, ?> cache = db().getHashMap(cacheId);

			// Disable re-caching in all cells of this cache and remove them.
			for (final Object k : cache.keySet()) {
				final SCIFIOCell<?> cell = getCellFromCache(cache, (Integer) k);
				if (cell != null) cell.cacheOnFinalize(false);
				cache.remove(k);
			}
			db().commit();
		}
	}

	@Override
	public void clearAllCaches() {
		for (final String cache : caches)
			clearCache(cache);
	}

	@Override
	public void dropCache(final String cacheId) {
		if (caches.contains(cacheId)) {
			db().getHashMap(cacheId).close();
			caches.remove(cacheId);
		}
	}

	@Override
	public void addCache(final String cacheId) {
		caches.add(cacheId);
	}

	@Override
	public CacheResult cache(final String cacheId, final int index,
		final SCIFIOCell<?> object)
	{
		object.update();
		if (!(cacheAll() || object.dirty())) {
			return CacheResult.NOT_DIRTY;
		}
		else if (!caches.contains(cacheId)) {
			return CacheResult.CACHE_NOT_FOUND;
		}
		else {
			// Check to see if we have the latest version of this cell already
			final SCIFIOCell<?> cell = getCell(cacheId, index);
			if (cell != null && cell.equals(object)) return CacheResult.DUPLICATE_FOUND;

			// Store the provided cell

			final HTreeMap<Object, Object> cache = db().getHashMap(cacheId);

			// Will another object fit?
			if ((cache.size() + 1) * object.getElementSize() < maxCacheSize) diskIsFull(false);
			else diskIsFull(true);

			// If the cache is enabled and there's room on disk, cache and commit
			if (!enabled()) {
				return CacheResult.CACHE_DISABLED;
			}
			else if (diskFull()) {
				return CacheResult.DISK_FULL;
			}
			else {
				cache.put(getKey(cacheId, index), object);
				db().commit();
				object.cacheOnFinalize(false);
			}
		}
		return CacheResult.SUCCESS;
	}

	@Override
	public SCIFIOCell<?> retrieve(final String cacheId, final int index) {

		final SCIFIOCell<?> cell = getCell(cacheId, index);

		if (cell != null) {
			db().getHashMap(cacheId).remove(getKey(cacheId, index));
			db().commit();
		}

		return cell;
	}

	@Override
	public SCIFIOCell<?> retrieveNoRecache(final String cacheId, final int index)
	{

		final SCIFIOCell<?> cell = retrieve(cacheId, index);

		if (cell != null) {
			cell.cacheOnFinalize(false);
		}

		return cell;
	}

	@Override
	public void setMaxBytesOnDisk(final long maxBytes) {
		maxCacheSize = maxBytes;
	}

	// -- Service API Methods --

	@Override
	public void dispose() {
		if (db == null) return;
		clearAllCaches();
		db.close();
	}

	// -- Helper Methods --

	private SCIFIOCell<?> getCell(final String cacheId, final int index) {
		final Integer key = getKey(cacheId, index);

		final HTreeMap<?, ?> cache = db.getHashMap(cacheId);

		final SCIFIOCell<?> cell = getCellFromCache(cache, key);

		if (cell != null) {
			cell.setCacheId(cacheId);
			cell.setIndex(index);
			cell.setService(this);
			cell.cacheOnFinalize(true);
		}

		return cell;
	}

	private SCIFIOCell<?> getCellFromCache(final HTreeMap<?, ?> cache,
		final int key)
	{
		SCIFIOCell<?> cell = null;
		boolean success = false;

		// wait for memory to clear and the read to succeed
		while (!success) {
			try {
				cell = (SCIFIOCell<?>) cache.get(key);
				success = true;
			}
			catch (final OutOfMemoryError e) {}
		}
		return cell;
	}

	private DB db() {
		if (db == null) {
			db =
				DBMaker.newTempFileDB().closeOnJvmShutdown().cacheDisable()
					.asyncWriteDisable().writeAheadLogDisable()
					.randomAccessFileEnableIfNeeded().deleteFilesAfterClose().make();
		}
		return db;
	}

}
