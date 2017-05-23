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

package io.scif.img.cell;

import io.scif.img.cell.cache.CacheService;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;
import io.scif.refs.RefManagerService;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;

/**
 * A cache for {@link SCIFIOCell}s. Loads the requested cell if it is not
 * already in the cache.
 *
 * @author Mark Hiner
 */
public class SCIFIOCellCache<A extends ArrayDataAccess<A>>
{

	// -- Parameters --

	@Parameter
	private CacheService<SCIFIOCell<?>> cacheService;

	@Parameter
	private RefManagerService refManagerService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService logService;

	// -- Fields --

	/**
	 * Used to load image planes when they aren't in the cache.
	 */
	final private SCIFIOArrayLoader<A> loader;

	/**
	 * Unique ID for this cache.
	 */
	final private String cacheId = this.toString();

	// TODO: would be nice to replace this with another soft reference cache
	// that
	// was more configurable
	// e.g. controlling max elements and such.
	// In-memory cache.
	final private Map<Integer, WeakReference<SCIFIOCell<A>>> map =
		new ConcurrentHashMap<>();

	// -- Constructor --

	/**
	 * Creates a new SCIFIOCellCache and makes it available to the current
	 * CacheService
	 */
	public SCIFIOCellCache(final Context context,
		final SCIFIOArrayLoader<A> loader)
	{
		this.loader = loader;
		context.inject(this);
		cacheService.addCache(cacheId);
		refManagerService.manage(this);
	}

	// -- CellCache API --

	/**
	 * Returns the SCIFIOCell at the specified index from this cache, or null if
	 * index has not been loaded yet.
	 *
	 * @param index - linearized cell index to load
	 * @return The cell at the given index, if found, else null
	 */
	public SCIFIOCell<A> get(final int index) {
		final SCIFIOCell<A> cell = checkCache(cacheId, index);

		if (cell != null) return cell;

		return null;
	}

	/**
	 * Loads the cell at the specified index within the specified cell
	 * dimensionality, at the specified origin
	 *
	 * @param index - linearized cell index to load
	 * @param cellDims - lengths of each axis of a cell
	 * @param cellMin - origin position of each cell axis
	 * @return The cell at the specified index
	 */
	public SCIFIOCell<A> load(final int index, final int[] cellDims,
		final long[] cellMin)
	{
		SCIFIOCell<A> cell = checkCache(cacheId, index);

		if (cell != null) {
			return cell;
		}

		cell =
			new SCIFIOCell<>(cacheService, cacheId, index, cellDims, cellMin, loader
				.loadArray(cellDims, cellMin));
		refManagerService.manage(cell);

		cache(cacheService.getKey(cacheId, index), cell);

		return cell;
	}

	/**
	 * @return The id for this cache. Matches the id provided to the
	 *         {@link CacheService}.
	 */
	public String getCacheId() {
		return cacheId;
	}

	// -- Helper Methods --

	/**
	 * Maps a weak reference to the given cell. This ensures the cell can be
	 * garbage collected when it's no longer in use, but can still be returned
	 * from memory until that point.
	 *
	 * @param k - Key to map to the given cell
	 * @param cell - Cell to put in the in-memory cache
	 */
	private void cache(final Integer k, final SCIFIOCell<A> cell) {
		map.put(k, new WeakReference<>(cell));
		refManagerService.manage(cell, k, map);
	}

	/**
	 * First checks the local (weak) map. If empty, cache service is checked -
	 * which can potentially deserialize from disk.
	 *
	 * @param id - cache id to look up
	 * @param index - cell index
	 * @return The cached cell if it was found, or null
	 */
	@SuppressWarnings("unchecked")
	private SCIFIOCell<A> checkCache(final String id, final int index) {
		SCIFIOCell<A> cell = null;

		final Integer k = cacheService.getKey(id, index);
		WeakReference<SCIFIOCell<A>> ref = null;
		// Check the local cache
		synchronized (map) {
			ref = map.get(k);
		}

		if (ref != null) {
			// Cell is in memory
			cell = ref.get();
		}
		else {
			// make sure the weak ref to this cell is removed from memory
			synchronized (map) {
				map.remove(k);
			}
		}

		// Check the cache manager
		if (cell == null) {
			cell = (SCIFIOCell<A>) cacheService.retrieve(id, index);

			if (cell != null) {
				// Put the cell back in the memory cache
				cache(k, cell);
			}
		}

		return cell;
	}
}
