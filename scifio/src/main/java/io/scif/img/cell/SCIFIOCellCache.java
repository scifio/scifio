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

package io.scif.img.cell;

import io.scif.img.cell.SCIFIOImgCells.CellCache;
import io.scif.img.cell.cache.CacheService;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

/**
 * {@link CellCache} implementation for {@link SCIFIOCell}s. Loads the requested
 * cell if it is not already in the cache.
 * 
 * @author Mark Hiner hinerm at gmail.com
 */
public class SCIFIOCellCache<A extends ArrayDataAccess<?>> implements
	CellCache<A>
{

	// -- Fields --

	final protected SCIFIOArrayLoader<A> loader;

	final private String cacheId = this.toString();

	// TODO: would be nice to replace this with another soft reference cache that
	// was more configurable
	// e.g. controlling max elements and such.
	// In-memory cache.
	final protected ConcurrentHashMap<Integer, SoftReference<SCIFIOCell<A>>> map =
		new ConcurrentHashMap<Integer, SoftReference<SCIFIOCell<A>>>();

	final private CacheService<SCIFIOCell<?>> cacheService;

	// -- Constructor --

	/**
	 * Creates a new SCIFIOCellCache and makes it available to the current
	 * CacheService
	 */
	public SCIFIOCellCache(final CacheService<SCIFIOCell<?>> service,
		final SCIFIOArrayLoader<A> loader)
	{
		this.loader = loader;
		cacheService = service;
		cacheService.addCache(cacheId);
	}

	// -- CellCache API --

	/**
	 * Returns the SCIFIOCell at the specified index from this cache, or null if
	 * index has not been loaded yet.
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
	 */
	public SCIFIOCell<A> load(final int index, final int[] cellDims,
		final long[] cellMin)
	{
		SCIFIOCell<A> cell = checkCache(cacheId, index);

		if (cell != null) {
			return cell;
		}

		cell =
			new SCIFIOCell<A>(cacheService, cacheId, index, cellDims, cellMin, loader
				.loadArray(cellDims, cellMin));

		cache(cacheService.getKey(cacheId, index), cell);

		int c = loader.getBitsPerElement();
		for (final int l : cellDims)
			c *= l;

		return cell;
	}

	@Override
	public void finalize() {
		cacheService.clearCache(cacheId);
	}

	// -- Helper Methods --

	// Maps a soft reference to the given cell
	private void cache(final Integer k, final SCIFIOCell<A> cell) {
		map.put(k, new SoftReference<SCIFIOCell<A>>(cell));
	}

	/*
	 * First checks the local (weak) map. If empty, cache service is checked - which can
	 * potentially deserialize from disk.
	 */
	@SuppressWarnings("unchecked")
	private SCIFIOCell<A> checkCache(final String id, final int index) {
		SCIFIOCell<A> cell = null;

		final Integer k = cacheService.getKey(id, index);

		// Check the local cache
		final SoftReference<SCIFIOCell<A>> ref = map.get(k);

		if (ref != null) {
			cell = ref.get();
		}
		else {
			map.remove(k);
		}

		// Check the cache manager
		if (cell == null) {
			cell = (SCIFIOCell<A>) cacheService.retrieve(id, index);

			if (cell != null) {
				map.put(k, new SoftReference<SCIFIOCell<A>>(cell));
			}
		}

		return cell;
	}
}
