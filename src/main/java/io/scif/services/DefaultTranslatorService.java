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

package io.scif.services;

import io.scif.Metadata;
import io.scif.Translator;
import io.scif.util.SCIFIOMetadataTools;

import java.util.HashMap;
import java.util.Map;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link TranslatorService} implementation. If a false exact flag is
 * passed to {@link #findTranslator(Metadata, Metadata, boolean)} and an exact
 * translator can not be found, this service will first try to match the
 * destination type, then source type, then will accept any generic
 * {@link Metadata} translator.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultTranslatorService extends
	AbstractSingletonService<Translator> implements TranslatorService
{

	// -- Fields --

	Map<Class<? extends Metadata>, Map<Class<? extends Metadata>, Translator>> sourceToDestMap;

	// -- TranslatorService API Methods --

	@Override
	public Translator findTranslator(final Metadata source, final Metadata dest,
		final boolean exact)
	{
		// Unwrap MetadataWrappers to get to the actual format-specific metadata
		final Metadata trueSource = SCIFIOMetadataTools.unwrapMetadata(source);

		return findTranslator(trueSource.getClass(), dest.getClass(), exact);
	}

	@Override
	public Translator findTranslator(final Class<? extends Metadata> source,
		final Class<? extends Metadata> dest, final boolean exact)
	{
		// try to match the source and destination exactly
		Translator t = lookup(source, dest);

		if (!exact) {
			// Try to match the destination exactly
			t = lookup(t, io.scif.Metadata.class, dest);
			// Try to match the source exactly
			t = lookup(t, source, io.scif.Metadata.class);
			// Take any translator
			t = lookup(t, io.scif.Metadata.class, io.scif.Metadata.class);
		}

		return t;
	}

	@Override
	public boolean translate(final Metadata source, final Metadata dest,
		final boolean exact)
	{
		final Translator t = findTranslator(source, dest, exact);

		if (t == null) return false;

		t.translate(source, dest);

		return true;
	}

	// -- SingletonService API --

	@Override
	public Class<Translator> getPluginType() {
		return Translator.class;
	}

	// -- Service API Methods --

	// -- Helper Methods --

	/**
	 * @param source - Source class to match
	 * @param dest - Destination class to match
	 * @return - Translator capable of translating between the given source and
	 *         destination
	 */
	private Translator lookup(final Class<? extends Metadata> source,
		final Class<? extends Metadata> dest)
	{
		if (sourceToDestMap == null) {
			synchronized (this) {
				if (sourceToDestMap == null) {
					createTranslatorMap();
				}
			}
		}

		// Just look up the translator to try and find an exact match
		final Map<Class<? extends Metadata>, Translator> destMap = sourceToDestMap
			.get(source);

		if (destMap != null) {
			return destMap.get(dest);
		}

		return null;
	}

	/**
	 * @param t - If t is null, will attempt to find a suitable translator (in
	 *          priority order)
	 * @param source - Source class to match
	 * @param dest - Destination class to match
	 * @return - Translator capable of translating between the given source and
	 *         destination
	 */
	private Translator lookup(Translator t,
		final Class<? extends Metadata> source,
		final Class<? extends Metadata> dest)
	{
		if (t == null) {
			// Loop over the translators in priority order to see if we have a
			// suitable candidate
			for (int i = 0; i < getInstances().size() && t == null; i++) {
				final Translator translator = getInstances().get(i);
				if (translator.source().isAssignableFrom(source) && translator.dest()
					.isAssignableFrom(dest))
				{
					t = translator;
				}
			}
		}

		return t;
	}

	/**
	 * Create a nested mapping to all known translators. Allows for lazy
	 * instantiation.
	 */
	private void createTranslatorMap() {
		sourceToDestMap = new HashMap<>();
		for (final Translator translator : getInstances()) {
			addToMap(translator.source(), translator.dest(), sourceToDestMap,
				translator);
		}
	}

	/**
	 * Creates a nested mapping of key1 : (key 2 : translator). Creates
	 * intermediate map if it doesn't already exist.
	 *
	 * @param key1 - key to first-level (outer) map
	 * @param key2 - key to second-level (inner) map
	 * @param map - first-level (outer) map
	 * @param translator - value for second-level (inner) map
	 */
	private void addToMap(final Class<? extends Metadata> key1,
		final Class<? extends Metadata> key2,
		final Map<Class<? extends Metadata>, Map<Class<? extends Metadata>, Translator>> map,
		final Translator translator)
	{
		Map<Class<? extends Metadata>, Translator> innerMap = map.get(key1);

		// Create the map if it doesn't already exist
		if (innerMap == null) {
			innerMap = new HashMap<>();
			map.put(key1, innerMap);
		}

		// Create our mapping
		innerMap.put(key2, translator);
	}
}
