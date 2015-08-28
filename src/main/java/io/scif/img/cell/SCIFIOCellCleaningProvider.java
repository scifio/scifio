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
import io.scif.img.cell.cache.CacheResult;
import io.scif.img.cell.cache.CacheService;
import io.scif.refs.CleaningRef;
import io.scif.refs.RefProvider;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link RefProvider} plugin for creating {@link SCIFIOCellCleaner} instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = RefProvider.class)
public class SCIFIOCellCleaningProvider<A extends ArrayDataAccess<?>> extends
	AbstractSCIFIOPlugin implements RefProvider<SCIFIOCell<A>>
{

	// -- RefProvider API --

	@Override
	public boolean handles(final Object referent, final Object... params) {
		boolean handles = SCIFIOCell.class.isAssignableFrom(referent.getClass());
		handles = handles && (params == null || params.length == 0);
		return handles;
	}

	@Override
	public Reference<SCIFIOCell<A>> makeRef(final Object referent,
		final ReferenceQueue<SCIFIOCell<A>> queue, final Object... params)
	{
		if (!(referent instanceof Reference)) {
			throw new IllegalArgumentException("Invalid referent: " +
				referent.getClass().getName());
		}
		// FIXME: Check for compatible generic parameter if possible.
		@SuppressWarnings("unchecked")
		final SCIFIOCell<A> cell = (SCIFIOCell<A>) referent;
		final Reference<SCIFIOCell<A>> ref = new SCIFIOCellCleaner<A>(cell, queue);
		getContext().inject(ref);
		return ref;
	}

	// -- Provided reference class --

	/**
	 * {@link CleaningRef} implementation that uses {@link PhantomReference}s to
	 * ensure a {@link SCIFIOCell} is cached to disk using the
	 * {@link CacheService} after being garbage collected.
	 *
	 * @author Mark Hiner
	 */
	public static class SCIFIOCellCleaner<A extends ArrayDataAccess<?>>
		extends PhantomReference<SCIFIOCell<A>> implements CleaningRef
	{

		// -- Parameters --

		@Parameter
		private LogService logService;

		@Parameter
		private CacheService service;

		// -- Fields --

		private A data;

		private final int[] hashes;

		private final long[] elementSize;

		private final boolean[] enabled;

		private final long[] min;

		private final int[] dims;

		private final String cacheId;

		private final int index;

		// -- Constructor --

		public SCIFIOCellCleaner(final SCIFIOCell<A> cell,
			final ReferenceQueue<SCIFIOCell<A>> queue)
		{
			super(cell, queue);
			// The cell needs to be reconstructed, basically, to cache it.
			// So we need to store every non-transient field.
			data = cell.getData();
			hashes = cell.getHashes();
			elementSize = cell.getESizeArray();
			cacheId = cell.getCacheId();
			index = cell.getIndex();
			enabled = cell.isEnabled();
			dims = new int[cell.dimCount()];
			cell.dimensions(dims);
			min = new long[cell.dimCount()];
			cell.min(min);
		}

		// -- CleaningRef API --

		@Override
		public void cleanup() {
			// Create a new cell using all the non-transient information we
			// stored
			SCIFIOCell<A> cell =
				new SCIFIOCell<A>(data, hashes[1], hashes[0], elementSize[0], dims, min);
			cell.cacheOnFinalize(enabled[0]);
			// Cache the cell
			final CacheResult result = service.cache(cacheId, index, cell);

			cell = null;
			data = null;
			this.clear();
		}
	}

}
