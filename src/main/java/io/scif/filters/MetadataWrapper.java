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

import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;

/**
 * Wrapper for {@link io.scif.Metadata}. Used to create defensive copies of
 * metadata for manipulation by {@link io.scif.filters.ReaderFilter}s, while
 * allowing for API modification if needed.
 * <p>
 * If a Reader-based {@code Filter} requires special functionality from its
 * Metadata, a companion MetadataWrapper can be implemented. Concrete
 * implementations of this interface should always be annotated with
 * {@code Plugin} so they can be dynamically found when constructing new
 * {@code Filters}.
 * </p>
 * <p>
 * NB: This interface duplicates the Metadata setter signatures, with the
 * addition of a {@code passUp} flag. If this flag is true, the wrapped metadata
 * will also have the corresponding value set. If not, only the wrapper will be
 * modified.
 * </p>
 *
 * @author Mark Hiner
 * @see io.scif.filters.AbstractReaderFilter
 */
public interface MetadataWrapper extends Metadata {

	/**
	 * @return The {@code Metadata} used for delegation by this wrapper.
	 */
	Metadata unwrap();

	/**
	 * Sets the {@code Metadata} this wrapper will delegate to. Necessary for the
	 * sake of a zero-parameter constructor to allow {@code SezPoz} discovery.
	 *
	 * @param meta - The Metadata instance to wrap
	 */
	void wrap(Metadata meta);

	/**
	 * @return The class of the {@link Filter} this MetadataWrapper is compatible
	 *         with.
	 */
	Class<? extends Filter> filterType();

	// -- Setter Methods with passUp flag --

	void setTable(final MetaTable table, final boolean passUp);

	void add(final ImageMetadata meta, boolean passUp);

}
