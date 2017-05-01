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

/**
 * Provides a {@link io.scif.img.cell.cache.CacheService} interface for dynamically
 * caching and retrieving image pixel data to and from disk.
 * <p>
 * When a service is obtained, the following cache configuration options can be used:
 * </p>
 * <ul>
 *   <li>{@link io.scif.img.cell.cache.CacheService#enable(boolean)} to enable or
 *   disable caching.</li>
 *   <li>{@link io.scif.img.cell.cache.CacheService#setMaxBytesOnDisk(long)} to
 *   limit the amount of disk space used by the cache (argument in bytes)</li>
 *   <li>{@link io.scif.img.cell.cache.CacheService#cacheAll(boolean)} toggles whether
 *   or not non-dirty are cached. By default this is {@code false} and only
 *   dirty cells are cached.</li>
 * </ul>
 * <p>
 * Caching is enabled by default when using {@link io.scif.img.cell.SCIFIOCellImg}
 * types.
 * </p>
 * 
 * @author Mark Hiner
 */
package io.scif.img.cell.cache;
