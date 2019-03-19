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

import io.scif.SCIFIOPlugin;

/**
 * Interface for modifying object behavior. Equivalent to wrapping/decorating.
 * <p>
 * NB: {@link #getPriority()} is intended to be used to determine a natural
 * order for {@link java.lang.Comparable#compareTo(Object)}.
 * </p>
 * <p>
 * Concrete {@code Filter} implementations should also extend the class they
 * wrap, allowing the Filter to be used in the same way the wrapped class would
 * be.
 * </p>
 *
 * @author Mark Hiner
 */
public interface Filter extends SCIFIOPlugin {

	/**
	 * @return The target type operated on by this filter
	 */
	Class<?> target();

	/**
	 * @return True iff this filter should be enabled by default (e.g. whenever a
	 *         ReaderFilter is constructed)
	 */
	boolean enabledDefault();

	/**
	 * Sets the object which this filter augments.
	 *
	 * @param parent - The object wrapped by this filter
	 */
	void setParent(Object parent);

	/**
	 * Returns the object this filter augments.
	 *
	 * @return The object wrapped by this filter
	 */
	Object getParent();

	/**
	 * Returns true if this filter is capable of wrapping instances of the
	 * provided class.
	 *
	 * @param c - Class to check compatibility
	 * @return True if this filter can call setParent on instances of c
	 */
	boolean isCompatible(Class<?> c);

	/**
	 * Clears any configuration performed on this filter.
	 */
	void reset();
}
