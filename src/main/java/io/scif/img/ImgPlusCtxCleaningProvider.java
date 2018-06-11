/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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

package io.scif.img;

import io.scif.AbstractSCIFIOPlugin;
import io.scif.refs.CleaningRef;
import io.scif.refs.RefProvider;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;

import net.imagej.ImgPlus;

import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * {@link RefProvider} plugin for generating {@link ImgPlusCtxCleaner}
 * instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = RefProvider.class)
public class ImgPlusCtxCleaningProvider extends AbstractSCIFIOPlugin implements
	RefProvider
{

	// -- RefProvider API --

	@Override
	public boolean handles(final Object referent, final Object... params) {
		boolean matches = ImgPlus.class.isAssignableFrom(referent.getClass());
		matches = matches && params.length == 1;
		matches = matches && Context.class.isAssignableFrom(params[0].getClass());
		return matches;
	}

	@Override
	public Reference makeRef(final Object referent, final ReferenceQueue queue,
		final Object... params)
	{
		final Reference ref = new ImgPlusCtxCleaner(referent, queue, params);
		return ref;
	}

	// -- Provided reference class --

	/**
	 * {@link PhantomReference} implementation used to dispose a {@link Context}
	 * when all {@link ImgPlus} references have been garbage collected. Create
	 * this reference as new ImgPlus instances are generated with an anonymous
	 * Context. This will ensure that when the ImgPlus instances go out of scope,
	 * the Context is shut down properly.
	 *
	 * @author Mark Hiner
	 */
	public static class ImgPlusCtxCleaner extends PhantomReference<ImgPlus>
		implements CleaningRef
	{

		// -- Fields --

		private static Map<Context, Integer> ctxRefs = new HashMap<>();

		private Context ctx;

		// -- Constructor --

		public ImgPlusCtxCleaner(final Object imgPlus, final ReferenceQueue queue,
			final Object... params)
		{
			super((ImgPlus) imgPlus, queue);
			if (params.length != 1) {
				throw new IllegalArgumentException(
					"ImgPlusCleanerRef require 1 parameter: a scijava-common Context");
			}

			try {
				ctx = (Context) params[0];
			}
			catch (final ClassCastException e) {
				throw new IllegalArgumentException(
					"ImgPlusCleanerRef: invalid parameter");
			}

			synchronized (ctx) {
				final Integer count = ctxRefs.get(ctx);

				if (count == null) {
					// Create a new entry for this context
					ctxRefs.put(ctx, 1);
				}
				else {
					// Increase the count for this context
					ctxRefs.put(ctx, ctxRefs.get(ctx) + 1);
				}
			}
		}

		// -- CleaningRef API --

		@Override
		public void cleanup() {
			if (ctx == null) return;

			synchronized (ctx) {
				int count = ctxRefs.get(ctx);
				// Decrease the count for this context
				count--;
				// If the count is now 0, dispose the context
				if (count == 0) {
					ctxRefs.remove(ctx);
					ctx.dispose();
				}
				// Otherwise, update the stored count
				else {
					ctxRefs.put(ctx, count);
				}
			}
		}
	}

}
