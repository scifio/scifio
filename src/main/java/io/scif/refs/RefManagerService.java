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

import io.scif.SCIFIOService;

import java.lang.ref.Reference;

import org.scijava.service.Service;

/**
 * A {@link Service} implementation for helping with {@link Reference}
 * management.
 * <p>
 * The {@link #manage(Object, Object...)} method creates and registers a
 * {@link CleaningRef} instance with this service, using the given object as its
 * referent, which allows the {@link CleaningRef#cleanup()} method to be handled
 * on a separate thread.
 * </p>
 *
 * @author Mark Hiner
 */
public interface RefManagerService extends SCIFIOService {

	/**
	 * If there is a corresponding {@link CleaningRef} for the given object, it
	 * will be created using the given object as its referent and attached to a
	 * reference queue. A separate thread will poll the queue, and whenever a
	 * reference is dequeued its {@link CleaningRef#cleanup()} method will be
	 * invoked.
	 * <p>
	 * NB: no more than one of a given Reference type will be managed for a given
	 * referent. An object shouldn't require more than one cleanup for a given
	 * usage.
	 * </p>
	 */
	void manage(Object referent, Object... params);
}
