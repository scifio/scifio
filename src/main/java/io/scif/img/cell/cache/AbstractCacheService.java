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

package io.scif.img.cell.cache;

import java.io.Serializable;

import org.scijava.service.AbstractService;

/**
 * Abstract superclass for {@link CacheService} implementations. Provides a base
 * {@link #getKey(String, int)} implementation.
 *
 * @author Mark Hiner
 */
public abstract class AbstractCacheService<T extends Serializable> extends
	AbstractService implements CacheService<T>
{

	// -- Fields --

	// Whether this service should cache anything
	private boolean enabled = true;

	// Whether non-dirty records should be cached or not
	private boolean cacheAll = false;

	// Whether the cache has reached the disk allocation limit
	private boolean diskFull = false;

	// -- CacheService API methods --

	@Override
	public boolean enabled() {
		return enabled;
	}

	@Override
	public void enable(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Integer getKey(final String cacheId, final int index) {

		int result = 17;
		result = 31 * result + index;
		result = 31 * result + cacheId.hashCode();

		return result;
	}

	@Override
	public void setMaxBytesOnDisk(final long maxBytes) {
		throw new UnsupportedOperationException(
			"Setting the disk store size not currently supported. Please use enable(false) instead.");
	}

	@Override
	public void cacheAll(final boolean enabled) {
		cacheAll = enabled;
	}

	// -- AbstractCacheService methods --

	// Accessor for CacheAll
	protected boolean cacheAll() {
		return cacheAll;
	}

	// Accessor for diskFull
	protected boolean diskFull() {
		return diskFull;
	}

	// Setter for diskFull
	protected void diskIsFull(final boolean full) {
		diskFull = full;
	}
}
