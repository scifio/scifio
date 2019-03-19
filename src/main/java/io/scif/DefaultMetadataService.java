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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;

/**
 * Default service for working with SCIFIO metadata.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultMetadataService extends AbstractService implements
	MetadataService
{

	@Parameter
	private LogService log;

	@Override
	public Map<String, Object> parse(final String data) {
		return parse(data, "[&\n]");
	}

	@Override
	public Map<String, Object> parse(final String data, final String regex) {
		final HashMap<String, Object> map = new HashMap<>();

		final String[] tokens = data.split(regex);

		// parse tokens from filename
		for (final String token : tokens) {
			final int equals = token.indexOf("=");
			if (!map.containsKey(NAME_KEY) && equals < 0) {
				// first token is the name
				map.put(NAME_KEY, token);
				continue;
			}
			if (equals < 0) {
				log.warn("Ignoring token: " + token);
				continue;
			}
			final String key = token.substring(0, equals);
			Object value;
			final String rawValue = token.substring(equals + 1);
			// Check for a list of values
			if (rawValue.indexOf(',') >= 0) {
				final String[] values = rawValue.split(",");
				value = Arrays.asList(values);
			}
			else {
				value = rawValue;
			}
			map.put(key, value);
		}

		return map;
	}

	@Override
	public void populate(final Object metadata, final Map<String, Object> map) {
		final List<java.lang.reflect.Field> fields = ClassUtils.getAnnotatedFields(
			metadata.getClass(), Field.class);
		for (final java.lang.reflect.Field field : fields) {
			final String name = field.getName();
			if (!map.containsKey(name)) {
				// no value given for this field
				continue;
			}
			final Object value = map.get(name);

			// TEMP: Until bug is fixed; see
			// https://github.com/scijava/scijava-common/issues/31
			if (value == null) continue;

			ClassUtils.setValue(field, metadata, value);
		}
	}
}
