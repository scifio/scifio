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

package io.scif.formats.tiff;

import io.scif.enumeration.CodedEnum;
import io.scif.enumeration.EnumException;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for working with TIFF resolution units. From the TIFF
 * specification, a resolution unit is "the unit of measurement for XResolution
 * and YResolution."
 *
 * @author Curtis Rueden
 */
public enum ResolutionUnit implements CodedEnum {

		/**
		 * No absolute unit of measurement. Used for images that may have a
		 * non-square aspect ratio, but no meaningful absolute dimensions.
		 */
		NONE(1, "None"),

		INCH(2, "Inch"),

		CENTIMETER(3, "Centimeter");

	/** Code for the resolution unit in the actual TIFF file. */
	private int code;

	/** Name of the resolution unit. */
	private String name;

	private static final Map<Integer, ResolutionUnit> lookup = new HashMap<>();

	/** Reverse lookup of code to IFD type enumerate value. */
	static {
		for (final ResolutionUnit v : EnumSet.allOf(ResolutionUnit.class)) {
			lookup.put(v.getCode(), v);
		}
	}

	// -- Constructor --

	/**
	 * Default constructor.
	 *
	 * @param code Integer "code" for the resolution unit.
	 * @param name Given name of the resolution unit.
	 */
	private ResolutionUnit(final int code, final String name) {
		this.code = code;
		this.name = name;
	}

	// -- ResolutionUnit methods --

	/**
	 * Retrieves a resolution unit by reverse lookup of its "code".
	 *
	 * @param code The code to look up.
	 * @return The {@code ResolutionUnit} instance for the {@code code} or
	 *         {@code null} if it does not exist.
	 */
	public static ResolutionUnit get(final int code) {
		final ResolutionUnit toReturn = lookup.get(code);
		if (toReturn == null) {
			throw new EnumException("Unable to find ResolutionUnit with code: " +
				code);
		}
		return toReturn;
	}

	@Override
	public int getCode() {
		return code;
	}

	/**
	 * Retrieves the given name of the resolution unit.
	 *
	 * @return See above.
	 */
	public String getName() {
		return name;
	}

}
