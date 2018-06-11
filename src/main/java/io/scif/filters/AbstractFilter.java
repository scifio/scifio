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

package io.scif.filters;

import io.scif.AbstractSCIFIOPlugin;

/**
 * Abstract superclass for {@link io.scif.filters.Filter} implementations.
 * <p>
 * NB: the {@link #compareTo(org.scijava.Prioritized)} implementation provided
 * produces a natural ordering in descending order of {@link #getPriority()}.
 * </p>
 * <p>
 * NB: the {@link #setParent(Object)} method can not be typed narrowed with
 * generics due to erasure. Type checking is performed during this call,
 * however, to guarantee type safety for the cached parent instance. An
 * {@link IllegalArgumentException} will be thrown if provided the wrong type of
 * parent for this filter.
 * </p>
 *
 * @author Mark Hiner
 * @see io.scif.filters.Filter
 * @param <T> - Parent type of this filter.
 */
public abstract class AbstractFilter<T> extends AbstractSCIFIOPlugin implements
	Filter
{

	// -- Fields --

	/* Parent to delegate to. */
	private T parent = null;

	/* For checking in setParent */
	private final Class<? extends T> parentClass;

	// -- Constructor --

	public AbstractFilter(final Class<? extends T> parentClass) {
		this.parentClass = parentClass;
	}

	// -- Filter API Methods --

	@Override
	public boolean enabledDefault() {
		return false;
	}

	@Override
	public T getParent() {
		return parent;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setParent(final Object parent) {
		if (parentClass.isAssignableFrom(parent.getClass())) this.parent =
			(T) parent;
		else {
			throw new IllegalArgumentException("Invalid parent. Provided: " + parent
				.getClass() + " Expected: " + parentClass);
		}
	}

	@Override
	public void reset() {
		parent = null;
	}

	@Override
	public boolean isCompatible(final Class<?> c) {
		return parentClass.isAssignableFrom(c);
	}
}
