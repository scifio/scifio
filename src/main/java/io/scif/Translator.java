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

package io.scif;

import io.scif.filters.MetadataWrapper;

import java.util.List;

import org.scijava.plugin.SingletonPlugin;

/**
 * Interface for all SCIFIO {@code Translators}.
 * <p>
 * Multiple {@code Translators} can be defined for a given {@code Format}. Each
 * encodes a process for converting from one {@link io.scif.Metadata} type to
 * another (where either the source or the destination is the {@code Metadata}
 * type associated with this {@code Format}).
 * </p>
 * <p>
 * The {@link #translate(Metadata, Metadata)} method always accepts an instance
 * of both source and destination {@code Metadata}. This allows chaining of
 * multiple translators (or other methods) to populate a single instance of
 * {@code Metadata}.
 * </p>
 * <p>
 * If no {@code Metadata} instance is readily available for translation, it can
 * be created through the {@code Format}, and existing {@code Metadata}
 * instances can be reset to ensure no previous information persists.
 * </p>
 *
 * @see io.scif.Format#createMetadata()
 * @see io.scif.services.TranslatorService
 * @author Mark Hiner
 */
public interface Translator extends SCIFIOPlugin, SingletonPlugin {

	// -- Translator API methods --

	/**
	 * @return The Metadata class accepted by this translator as input
	 */
	Class<? extends Metadata> source();

	/**
	 * @return The Metadata class accepted by this translator as output
	 */
	Class<? extends Metadata> dest();

	/**
	 * Uses the source {@code Metadata} to populate the destination
	 * {@code Metadata}. Specifically, the format-specific metadata and/or
	 * {@link ImageMetadata} of the source are used to populate the format-
	 * specific metadata of the destination (from which the destination's
	 * {@link ImageMetadata} can be derived}.
	 * <p>
	 * Note that the destination does not have to be empty, but can be built up
	 * through multiple translations. However each translation step is assumed to
	 * overwrite any previously existing data.
	 * </p>
	 * <p>
	 * For a reference to a fresh {@code Metadata} instance to use in translation,
	 * consider the {@link io.scif.Format#createMetadata()} method.
	 * </p>
	 *
	 * @param source {@code Metadata} to use to populate
	 * @param destination {@code Metadata} to be populated
	 * @see io.scif.Format#createMetadata()
	 * @throws IllegalArgumentException if the arguments don't match the
	 *           {@code Metadata} types of this {@code Translator}.
	 */
	void translate(final Metadata source, final Metadata destination);

	/**
	 * As {@link #translate(Metadata, Metadata)} with the format-specific and
	 * image metadata explicitly split out.
	 * <p>
	 * NB: it is NOT GUARANTEED that the {@link Metadata#getAll()} method of the
	 * source here will provide equivalent {@link ImageMetadata} as sourceImgMeta.
	 * For example, if a {@link MetadataWrapper} was used, the two are unlikely to
	 * match up. This is necessary, as {@code Translator} discovery depends on the
	 * format of source and destination, but augmentation via wrapping must affect
	 * the underlying {@code ImageMetadata} non-destructively.
	 * </p>
	 *
	 * @param source {@code Metadata} to use to populate
	 * @param sourceImgMeta {@link ImageMetadata} to use to populate
	 * @param destination {@code Metadata} to be populated
	 */
	void translate(final Metadata source, final List<ImageMetadata> sourceImgMeta,
		final Metadata destination);
}
