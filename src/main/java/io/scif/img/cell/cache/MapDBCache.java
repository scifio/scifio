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

import io.scif.img.cell.SCIFIOCell;
import io.scif.refs.RefManagerService;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * {@link CacheService} implementation using <a
 * href="http://www.mapdb.org/">MapDB</a> to store and retrieve
 * {@link SCIFIOCell}s on disk.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class MapDBCache extends AbstractCacheService<SCIFIOCell<?>> {

	// -- Parameters --

	@Parameter
	private ThreadService threadService;

	@Parameter
	private RefManagerService refManagerService;

	@Parameter
	private LogService logService;

	// -- Fields --

	/** Disk-backed database for writing. */
	private DB db;

	/** List of caches. */
	private final Set<String> caches = new TreeSet<>();

	/** Map of keys stored in this cache to the last hash stored on disk. */
	private final Map<Integer, Integer> knownKeys =
		new ConcurrentHashMap<>();

	/**
	 * List of all keys that have been retrieved so far. These keys are OK to
	 * delete from disk.
	 */
	private final Set<Integer> retrievedKeys = new HashSet<>();

	/** Maximum cache size, in bytes. */
	private long maxCacheSize = Long.MAX_VALUE;

	/** Flag for cleaning entries from disk. */
	private final boolean[] cleaning = { false };

	private final Queue<Set<Integer>> cleaningQueue =
		new LinkedList<>();

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
				knownKeys.remove(k);
				retrievedKeys.remove(k);
				final SCIFIOCell<?> cell = (SCIFIOCell<?>) cache.remove(k);
				if (cell != null) cell.cacheOnFinalize(false);
			}
			db().commit();
		}
	}

	@Override
	public void clearAllCaches() {
		synchronized (this) {
			for (final String cache : caches) {
				clearCache(cache);
			}
		}
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
		final SCIFIOCell<?> cell)
	{
		if (!cell.isEnabled()[0]) {
			return CacheResult.CELL_DISABLED;
		}

		// Calculate the current hash for the given cell's data
		cell.update();

		// If the cell isn't dirty and caching isn't forced, we can return.
		// The cell will not be persisted
		if (!(cacheAll() || cell.dirty())) {
			return CacheResult.NOT_DIRTY;
		}
		else if (!caches.contains(cacheId)) {
			return CacheResult.CACHE_NOT_FOUND;
		}
		else {
			// Check to see if we have the latest version of this cell already
			final Integer key = getKey(cacheId, index);
			Integer knownHash = null;
			synchronized (knownKeys) {
				knownHash = knownKeys.get(key);
			}
			if (knownHash != null && cell.getCurrentHash() == knownHash) {
				// Already stored this cell with this data hash.
				return CacheResult.DUPLICATE_FOUND;
			}
			else if (knownHash == null && retrievedKeys.contains(key)) {
				while (retrievedKeys.contains(key)) {
					// This cell was previously loaded, but it's not in
					// knownKeys so it's
					// in the process of being removed. Wait until it's removed
					// to avoid a
					// collision with the file on disk.
					try {
						Thread.sleep(50);
					}
					catch (final InterruptedException e) {
						logService.warn(
							"Interrupted while waiting for retrieved keys to clear", e);
					}
				}
			}

			// Attempt to store the provided cell
			final HTreeMap<Object, Object> cache = db().getHashMap(cacheId);

			int knownSize = 0;
			boolean inClean = false;

			synchronized (knownKeys) {
				knownSize = knownKeys.keySet().size() + 1;
				inClean = cleaning[0];
			}
			// Will another object fit?
			if (knownSize * cell.getElementSize() < maxCacheSize) diskIsFull(false);
			else {
				// We can try to make room by removing keys that have been
				// previously
				// retrieved.
				if (retrievedKeys.size() > 0 || inClean) {
					// We can clean entries
					if (retrievedKeys.size() > 0) {
						cleanRetrieved(cacheId);
					}

					while (knownSize * cell.getElementSize() >= maxCacheSize && inClean) {
						// wait for disk space to clean
						try {
							Thread.sleep(50);
						}
						catch (final InterruptedException e) {
							logService
								.warn("Interrupted while waiting for cache to clean", e);
						}
						synchronized (knownKeys) {
							knownSize = knownKeys.keySet().size() + 1;
							inClean = cleaning[0];
						}
					}
				}
				else {
					// Nothing to clean
					diskIsFull(true);
				}
			}

			// If the cache is enabled and there's room on disk, cache and
			// commit
			if (!enabled()) {
				return CacheResult.CACHE_DISABLED;
			}
			else if (diskFull()) {
				return CacheResult.DISK_FULL;
			}
			else {
				// Create a map of the current hash
				synchronized (knownKeys) {
					knownKeys.put(key, cell.getCurrentHash());

					// Remove any entry in retrieved keys to ensure this entry
					// is
					// not deleted from disk before it's retrieved.
					retrievedKeys.remove(key);
				}
				// Write the cell to disk
				cache.put(key, cell);
				db().commit();
			}
		}
		return CacheResult.SUCCESS;
	}

	@Override
	public SCIFIOCell<?> retrieve(final String cacheId, final int index) {
		// Load the cell
		final SCIFIOCell<?> cell = getCell(cacheId, index);

		if (cell != null) {
			// Mark this entry for possible deletion in the future
			retrievedKeys.add(getKey(cacheId, index));
			refManagerService.manage(cell);
		}
		return cell;
	}

	@Override
	public SCIFIOCell<?> retrieveNoRecache(final String cacheId, final int index)
	{
		final SCIFIOCell<?> cell = getCell(cacheId, index);

		if (cell != null) {
			// Mark this entry for possible deletion in the future
			retrievedKeys.add(getKey(cacheId, index));
			// Ensure this cell is not cached again
			cell.cacheOnFinalize(false);
			refManagerService.manage(cell);
		}

		return cell;
	}

	@Override
	public void cleanRetrieved(final String cacheId) {
		synchronized (knownKeys) {
			for (final Integer oldKey : retrievedKeys) {
				// Remove all the retrieved keys from known keys to ensure they
				// aren't
				// loaded.
				knownKeys.remove(oldKey);
			}
		}
		synchronized (cleaningQueue) {
			cleaningQueue.add(new HashSet<>(retrievedKeys));
		}
		retrievedKeys.clear();

		if (!cleaning[0]) {
			cleaning[0] = true;
			// Not already cleaning, so start a new thread
			threadService.run(new Runnable() {

				@Override
				public void run() {
					Set<Integer> toClean = cleaningQueue.poll();
					while (cleaning[0]) {
						synchronized (knownKeys) {
							for (final Integer oldKey : toClean) {
								// make sure this key hasn't been revived
								if (!knownKeys.containsKey(oldKey)) {
									db().getHashMap(cacheId).remove(oldKey);
								}
							}
							db().commit();
						}
						synchronized (cleaningQueue) {
							toClean = cleaningQueue.poll();
							cleaning[0] = toClean != null;
						}
					}
				}
			});
		}
	}

	@Override
	public void setMaxBytesOnDisk(final long maxBytes) {
		maxCacheSize = maxBytes;
	}

	@Override
	public void dispose() {
		if (db == null) return;
		synchronized (this) {
			for (final String cache : caches) {
				db.delete(cache);
			}
			db.commit();
			caches.clear();
		}
	}

	// -- Helper Methods --

	private SCIFIOCell<?> getCell(final String cacheId, final int index) {
		final Integer key = getKey(cacheId, index);

		final HTreeMap<?, ?> cache = db().getHashMap(cacheId);
		final SCIFIOCell<?> cell = getCellFromCache(cache, key);

		if (cell != null) {
			// Set the transient fields of the cell
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

		if (knownKeys.containsKey(key)) {
			// Check the in-memory map to see if this entry exists on disk.
			cell = (SCIFIOCell<?>) cache.get(key);
		}
		return cell;
	}

	private DB db() {
		if (db == null) createDB();
		return db;
	}

	private synchronized void createDB() {
		if (db != null) return;
		db =
			DBMaker.newTempFileDB().closeOnJvmShutdown().cacheDisable()
				.transactionDisable().deleteFilesAfterClose().make();
	}
}
