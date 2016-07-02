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

package io.scif.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// TODO: Deprecate in favor of org.scijava.util.DebugUtils.

/**
 * A utility class with convenience methods for debugging.
 *
 * @author Curtis Rueden
 * @deprecated Use {@link org.scijava.util.DebugUtils} instead.
 */
@Deprecated
public final class DebugTools {

	// -- Constructor --

	private DebugTools() {}

	// -- DebugTools methods --

	/** Extracts the given exception's corresponding stack trace to a string. */
	public static String getStackTrace(final Throwable t) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out, false, Constants.ENCODING));
			return new String(out.toByteArray(), Constants.ENCODING);
		}
		catch (final IOException e) {}
		return null;
	}

	/**
	 * This method uses reflection to scan the values of the given class's static
	 * fields, returning the first matching field's name.
	 */
	public static String getFieldName(final Class<?> c, final int value) {
		final Field[] fields = c.getDeclaredFields();
		for (final Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);
			try {
				if (field.getInt(null) == value) return field.getName();
			}
			catch (final IllegalAccessException exc) {}
			catch (final IllegalArgumentException exc) {}
		}
		return "" + value;
	}

}
