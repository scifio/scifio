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

/**
 * Provides reference management utilities.
 * <p>
 * The {@link io.scif.refs.RefManagerService} provides a dedicated thread for
 * polling reference queues, looking for available references. The
 * {@link io.scif.refs.RefManagerService#manage(Object, Object[])} method is
 * used to create and enqueue a new reference. The first parameter is the object
 * to manage, and it must have a corresponding {@link io.scif.refs.RefProvider}
 * that matches the given {@code Object[]} parameter list.
 * </p>
 * <p>
 * The {@link io.scif.refs.RefProvider} is a new plugin type for generating
 * references, and is uniquely defined by the target referent plus an additional
 * set of parameters.
 * </p>
 * <p>
 * A {@link io.scif.refs.CleaningRef} interface is provided for references that
 * have clean-up operations after being enqueued.
 * </p>
 *
 * @author Mark Hiner
 */

package io.scif.refs;
