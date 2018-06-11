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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

/**
 * This class can be used as a flag to ensure all SCIFIO components are printed
 * in a consistent manner via their toString methods. This printing traverses up
 * the dependency tree of the concrete implementation, and will also follow
 * paths down through {@link io.scif.Field}s (of any class in the hierarchy).
 *
 * @see io.scif.Field
 * @author Mark Hiner
 */
public class FieldPrinter {

	private final Object obj;

	public FieldPrinter(final Object o) {
		obj = o;
	}

	// Prints out a list of all the fields of this Metadata object
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString() + "\n");
		sb.append(indent(dumpString(obj.getClass())));
		return sb.toString();
	}

	private String dumpString(final Class<?> type) {
		if (type == null) return "";

		final StringBuilder sb = new StringBuilder();

		sb.append("--" + type + "--\n");

		// iterate over fields
		sb.append("{\n");

		for (final Field f : type.getDeclaredFields()) {
			f.setAccessible(true);

			final int mods = f.getModifiers();
			if (Modifier.isFinal(mods)) continue;
			if (Modifier.isStatic(mods)) continue;

			final String name = f.getName();
			final Object value;
			try {
				value = f.get(obj);
			}
			catch (final IllegalArgumentException e) {
				continue;
			}
			catch (final IllegalAccessException e) {
				continue;
			}
			sb.append(name + " = " + value + "\n");
		}
		sb.append("}\n");

		// Ascends the class hierarchy
		sb.append(indent(dumpString(type.getSuperclass())));
		return sb.toString();
	}

	/*
	 * Adds one tab to each line in the provided string
	 */
	private String indent(final String s) {
		final StringBuilder superSB = new StringBuilder();
		final StringTokenizer stk = new StringTokenizer(s, "\n");
		while (stk.hasMoreTokens()) {
			superSB.append("\t" + stk.nextToken() + "\n");
		}

		return superSB.toString();
	}
}
