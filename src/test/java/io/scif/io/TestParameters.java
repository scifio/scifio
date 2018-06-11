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

package io.scif.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestParameters {

	/**
	 * This list contains entries for the providers to test. Each entry consists
	 * of
	 * <ol>
	 * <li>The name of the provider</li>
	 * <li>Whether to check the growth</li>
	 * <li>Whether to test the initial length</li>
	 * <li>(Optional) which groups to include</li>
	 * <li>(Optional) which groups to exclude</li>
	 * </ol>
	 */
	private final static Object[][] parameters = { { "ByteArrayHandle", false,
		true, asSet("readTests", "writeTests"), asSet("readLineTest") }, {
			"NewByteArrayHandle", true, false, asSet("writeTests") }, {
				"ExistingByteArrayHandle", false, false, asSet("writeTests") }, {
					"BZip2Handle", false, true, asSet("readTests"), asSet(
						"readLineTest") }, { "GZipHandle", false, true, asSet("readTests"),
							asSet("readLineTest") }, { "NIOFileHandle", false, true, asSet(
								"readTests", "writeTests") }, { "URLHandle", false, true, asSet(
									"readTests"), asSet("readLineTest") }, { "ZipHandle", false,
										true, asSet("readTests"), asSet("readLineTest") } };

	private final static <T> Set<T> asSet(final T... values) {
		final Set<T> result = new HashSet<>();
		for (final T value : values) {
			result.add(value);
		}
		return result;
	}

	private final static boolean matches(final Set<String> groupSet,
		final Object includes)
	{
		@SuppressWarnings("unchecked")
		final Set<String> includeSet = (Set<String>) includes;
		for (final String group : groupSet) {
			if (includeSet.contains(group)) return true;
		}
		return false;
	}

	public static Collection<Object[]> parameters(final String... groups) {
		final Set<String> groupSet = groups.length == 0 ? null : asSet(groups);
		final Collection<Object[]> result = new ArrayList<>();
		for (final Object[] values : parameters) {
			if (groupSet != null) {
				// includes
				if (values.length > 3 && values[3] != null && !matches(groupSet,
					values[3])) continue;
				if (values.length > 4 && values[4] != null && matches(groupSet,
					values[4])) continue;
			}
			result.add(new Object[] { values[0], values[1], values[2] });
		}
		return result;
	}

}
