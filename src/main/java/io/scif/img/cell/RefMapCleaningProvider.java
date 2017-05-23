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

import io.scif.AbstractSCIFIOPlugin;
import io.scif.refs.CleaningRef;
import io.scif.refs.RefProvider;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

import org.scijava.plugin.Plugin;

/**
 * {@link RefProvider} plugin for creating {@link RefMapCleaner} instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = RefProvider.class)
public class RefMapCleaningProvider extends AbstractSCIFIOPlugin implements
	RefProvider
{

	// -- RefProvider API --

	@Override
	public boolean handles(final Object referent, final Object... params) {
		boolean handles = SCIFIOCell.class.isAssignableFrom(referent.getClass());
		handles = handles && params.length == 2;
		handles = handles && Integer.class.isAssignableFrom(params[0].getClass());
		handles = handles && Map.class.isAssignableFrom(params[1].getClass());
		return handles;
	}

	@Override
	public Reference makeRef(final Object referent, final ReferenceQueue queue,
		final Object... params)
	{
		final Reference ref = new RefMapCleaner(referent, queue, params);
		return ref;
	}

	// -- Provided reference class --

	/**
	 * Custom {@link WeakReference} implementation for use with
	 * {@link SCIFIOCellCache}. Used to build the in-memory cache. Removes itself
	 * from the refMap when its referent is garbage collected.
	 *
	 * @author Mark Hiner
	 */
	public static class RefMapCleaner<A extends ArrayDataAccess<A>> extends
		WeakReference<SCIFIOCell<A>> implements CleaningRef
	{

		// -- Fields --

		private Integer key;

		private Map<Integer, RefMapCleaner<A>> refMap;

		// -- Constructor --

		public RefMapCleaner(final Object referent, final ReferenceQueue q,
			final Object... params)
		{
			super((SCIFIOCell<A>) referent, q);
			if (params.length != 2) {
				throw new IllegalArgumentException(
					"RefMapCleaningRef require 2 parameters: an integer key and a"
						+ " map of integers to CellCacheReferences");
			}

			try {
				key = (Integer) params[0];
				refMap = (Map<Integer, RefMapCleaner<A>>) params[1];
			}
			catch (final ClassCastException e) {
				throw new IllegalArgumentException(
					"RefMapCleaningRef - invalid parameters");
			}
		}

		// -- CleaningRef API --

		@Override
		public void cleanup() {
			refMap.remove(key);
		}
	}
}
