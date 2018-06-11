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
 * Provides wrapping classes for modifying the performance of one or more of a
 * {link io.scif.Format}'s components.
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 * <li>Implemented SCIFIO {@link io.scif.filters.Filter}s. Filters are designed
 * to wrap arbitrary classes. An implementation is currently provided for
 * Readers: {@link io.scif.filters.ReaderFilter}.
 * <p>
 * Filters are analogous to the ReaderWrappers of Bio-Formats, but function a
 * bit differently. In Bio-Formats, you would create a stack of ReaderWrappers
 * based on what functionality you wanted to add to a given Reader instance.
 * </p>
 * <p>
 * In SCIFIO, the Filters are discoverable plugins (via the scijava-common
 * Context). When you create a ReaderFilter (e.g. via
 * {@link io.scif.services.InitializeService#initializeReader}) you get the
 * whole stack of discovered plugins. Each can be individually configured to be
 * enabled by default. This allows dynamic extensibility, as new Filters can be
 * added to the classpath and automatically discovered and enabled.
 * </p>
 * <p>
 * The ReaderFilter is then passed around and functions like a normal Reader.
 * But as plugins are enabled and disabled within the Filter, it automatically
 * maintains the stack (based on each Filter's priority), obviating the need for
 * user knowledge of a construction order.
 * </p>
 * </li>
 * </ul>
 * <h3>Future plans</h3>
 * <ul>
 * <li>Implement Filters for the remaining component types.</li>
 * </ul>
 *
 * @author Mark Hiner
 */

package io.scif.filters;
