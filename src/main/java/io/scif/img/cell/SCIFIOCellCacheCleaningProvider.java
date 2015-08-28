/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
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

import io.scif.AbstractSCIFIOPlugin;
import io.scif.img.cell.cache.CacheService;
import io.scif.refs.CleaningRef;
import io.scif.refs.RefProvider;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link RefProvider} implementation for creating
 * {@link SCIFIOCellCacheCleaner} instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = RefProvider.class)
public class SCIFIOCellCacheCleaningProvider<A extends ArrayDataAccess<?>>
	extends AbstractSCIFIOPlugin implements RefProvider<SCIFIOCellCache<A>>
{

	// -- RefProvider API --

	@Override
	public boolean handles(final Object referent, final Object... params) {
		boolean handles =
			SCIFIOCellCache.class.isAssignableFrom(referent.getClass());
		handles = handles && (params == null || params.length == 0);
		return handles;
	}

	@Override
	public Reference<SCIFIOCellCache<A>> makeRef(final Object referent,
		final ReferenceQueue<SCIFIOCellCache<A>> queue, final Object... params)
	{
		if (!(referent instanceof Reference)) {
			throw new IllegalArgumentException("Invalid referent: " +
				referent.getClass().getName());
		}
		// FIXME: Check for compatible generic parameter if possible.
		@SuppressWarnings("unchecked")
		final SCIFIOCellCache<A> cache = (SCIFIOCellCache<A>) referent;
		final Reference<SCIFIOCellCache<A>> ref =
			new SCIFIOCellCacheCleaner<A>(cache, queue);
		getContext().inject(ref);
		return ref;
	}

	// -- Provided reference class --

	/**
	 * {@link CleaningRef} implementation that uses {@link PhantomReference}s to
	 * ensure a {@link SCIFIOCellCache} is removed from the {@link CacheService}
	 * after being garbage collected.
	 *
	 * @author Mark Hiner
	 */
	public static class SCIFIOCellCacheCleaner<A extends ArrayDataAccess<?>>
		extends PhantomReference<SCIFIOCellCache<A>> implements CleaningRef
	{

		// -- Parameters --

		@Parameter
		private CacheService cacheService;

		// -- Fields --

		/**
		 * cacheId field of the referent.
		 */
		private final String cacheId;

		// -- Constructors --

		public SCIFIOCellCacheCleaner(final SCIFIOCellCache<A> cache,
			final ReferenceQueue<SCIFIOCellCache<A>> queue)
		{
			super(cache, queue);
			cacheId = ((SCIFIOCellCache<?>) cache).getCacheId();
		}

		// -- RefProvider API --

		@Override
		public void cleanup() {
			// Remove this cache's entry from the cacheService
			cacheService.clearCache(cacheId);
			this.clear();
		}
	}

}
