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

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.scijava.plugin.Plugin;

import io.scif.AbstractSCIFIOPlugin;
import io.scif.Reader;
import io.scif.img.cell.cache.CacheService;
import io.scif.refs.CleaningRef;
import io.scif.refs.RefProvider;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;

/**
 * {@link RefProvider} plugin for creating {@link SCIFIOCellImgCleaner}
 * instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = RefProvider.class)
public class SCIFIOCellImgCleaningProvider extends AbstractSCIFIOPlugin
	implements RefProvider
{

	// -- RefProvider API --

	@Override
	public boolean handles(final Object referent, final Object... params) {
		boolean handles = SCIFIOCellImg.class.isAssignableFrom(referent.getClass());
		handles = handles && (params == null || params.length == 0);
		return handles;
	}

	@Override
	public Reference makeRef(final Object referent, final ReferenceQueue queue,
		final Object... params)
	{
		final Reference ref = new SCIFIOCellImgCleaner(referent, queue);
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
	public static class SCIFIOCellImgCleaner<T extends NativeType<T>, A extends ArrayDataAccess<A>>
		extends PhantomReference<SCIFIOCellImg<T, A>> implements CleaningRef
	{

		private final Reader reader;

		public SCIFIOCellImgCleaner(final Object referent,
			final ReferenceQueue<? super SCIFIOCellImg<T, A>> q)
		{
			super((SCIFIOCellImg<T, A>) referent, q);
			reader = ((SCIFIOCellImg<T, A>) referent).reader();
		}

		@Override
		public void cleanup() {
			try {
				reader.close();
			}
			catch (final IOException e) {}
		}

	}
}
