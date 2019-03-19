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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.scijava.util.StringUtils;

/**
 * Default {@link MetaTable} implementation. Provides a copying constructor and
 * a {@link #putList(String, Object)} implementation.
 *
 * @see MetaTable
 * @author Mark Hiner
 */
public class DefaultMetaTable extends HashMap<String, Object> implements
	MetaTable
{

	// -- Fields --

	private boolean filtered;

	// -- Constructors --

	public DefaultMetaTable() {
		this(false);
	}

	/**
	 * Basic constructor
	 */
	public DefaultMetaTable(final boolean filter) {
		filtered = filter;
	}

	/**
	 * Construct a MetaTable and populate it using an existing map.
	 */
	public DefaultMetaTable(final Map<String, Object> copy) {
		for (final String k : copy.keySet())
			put(k, copy.get(k));
	}

	// -- MetaTable API Methods --

	@Override
	public void putList(final String key, final Object value) {
		Object list = get(key);

		if (list == null) list = new Vector<>();

		if (list instanceof Vector) {
			@SuppressWarnings("unchecked")
			final Vector<Object> valueList = ((Vector<Object>) list);
			valueList.add(value);
		}
		else {
			final Vector<Object> v = new Vector<>();
			v.add(list);
			v.add(value);
			list = v;
		}

		put(key, list);
	}

	@Override
	public Object put(String key, Object value) {
		if (key == null || value == null /* || TODO !isMetadataCollected() */) {
			return null;
		}

		key = key.trim();

		final boolean string = value instanceof String ||
			value instanceof Character;
		final boolean simple = string || value instanceof Number ||
			value instanceof Boolean;

		// string value, if passed in value is a string
		String val = string ? String.valueOf(value) : null;

		if (filtered) {
			// filter out complex data types
			if (!simple) return null;

			// verify key & value are reasonable length
			final int maxLen = 8192;
			if (key.length() > maxLen) return null;
			if (string && val.length() > maxLen) return null;

			// remove all non-printable characters
			key = StringUtils.sanitize(key);
			if (string) val = StringUtils.sanitize(val);

			// verify key contains at least one alphabetic character
			if (!key.matches(".*[a-zA-Z].*")) return null;

			// remove &lt;, &gt; and &amp; to prevent XML parsing errors
			final String[] invalidSequences = new String[] { "&lt;", "&gt;", "&amp;",
				"<", ">", "&" };
			for (final String invalidSequence : invalidSequences) {
				key = key.replaceAll(invalidSequence, "");
				if (string) val = val.replaceAll(invalidSequence, "");
			}

			// verify key & value are not empty
			if (key.length() == 0) return null;
			if (string && val.trim().length() == 0) return null;

			if (string) value = val;
		}

		return super.put(key, val == null ? value : val);
	}
}
