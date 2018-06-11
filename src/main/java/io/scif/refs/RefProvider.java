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

package io.scif.refs;

import io.scif.SCIFIOPlugin;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.scijava.plugin.SingletonPlugin;

/**
 * Interface for plugins that create references for a given referent. Each
 * RefProvider is uniquely identified by its referent and parameter combination
 * defined by the {@link #handles(Object, Object...)} method. If this method
 * returns true, it is safe to pass the referent and parameters to this
 * RefProvider, along with the {@link ReferenceQueue} the resulting reference
 * should be enqueued in.
 *
 * @author Mark Hiner
 */
public interface RefProvider extends SCIFIOPlugin, SingletonPlugin {

	/**
	 * @param referent - potential referent to test
	 * @param params - potential parameter list to test
	 * @return True iff this RefProvider matches the given referent and parameters
	 *         exactly.
	 */
	boolean handles(Object referent, Object... params);

	/**
	 * @param referent - referent for the new reference
	 * @param queue - ReferenceQueue to enqueue the new reference
	 * @param params - list of parameters required by the new reference
	 * @return The created reference.
	 */
	Reference makeRef(Object referent, ReferenceQueue queue, Object... params);
}
