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
 * Utility class for working with TIFF fill orders. From the TIFF specification,
 * a fill order is "the logical order of bits within a byte."
 *
 * @author Curtis Rueden
 * @author Eric Kjellman
 * @author Melissa Linkert
 * @author Chris Allan
 */
public enum FillOrder implements CodedEnum {

		/**
		 * Pixels are arranged within a byte such that pixels with lower column
		 * values are stored in the higher-order bits of the byte.
		 * <p>
		 * 1-bit uncompressed data example: Pixel 0 of a row is stored in the
		 * high-order bit of byte 0, pixel 1 is stored in the next-highest bit, ...,
		 * pixel 7 is stored in the low-order bit of byte 0, pixel 8 is stored in
		 * the high-order bit of byte 1, and so on.
		 * </p>
		 * <p>
		 * CCITT 1-bit compressed data example: The high-order bit of the first
		 * compression code is stored in the high-order bit of byte 0, the
		 * next-highest bit of the first compression code is stored in the
		 * next-highest bit of byte 0, and so on.
		 * </p>
		 */
		NORMAL(1, "Normal"),

		/**
		 * Pixels are arranged within a byte such that pixels with lower column
		 * values are stored in the lower-order bits of the byte.
		 * <p>
		 * We recommend that FillOrder=2 be used only in special-purpose
		 * applications. It is easy and inexpensive for writers to reverse bit order
		 * by using a 256-byte lookup table. <em>FillOrder = 2 should be used only
		 * when BitsPerSample = 1 and the data is either uncompressed or compressed
		 * using CCITT 1D or 2D compression, to avoid potentially ambigous
		 * situations.</em>
		 * </p>
		 * <p>
		 * Support for FillOrder=2 is not required in a Baseline TIFF compliant
		 * reader.
		 * </p>
		 */
		REVERSED(2, "Reversed");

	/** Code for the fill order in the actual TIFF file. */
	private int code;

	/** Name of the fill order. */
	private String name;

	private static final Map<Integer, FillOrder> lookup = new HashMap<>();

	/** Reverse lookup of code to IFD type enumerate value. */
	static {
		for (final FillOrder v : EnumSet.allOf(FillOrder.class)) {
			lookup.put(v.getCode(), v);
		}
	}

	// -- Constructor --

	/**
	 * Default constructor.
	 *
	 * @param code Integer "code" for the fill order.
	 * @param name Given name of the fill order.
	 */
	private FillOrder(final int code, final String name) {
		this.code = code;
		this.name = name;
	}

	// -- FillOrder methods --

	/**
	 * Retrieves a fill order by reverse lookup of its "code".
	 *
	 * @param code The code to look up.
	 * @return The {@code FillOrder} instance for the {@code code} or {@code null}
	 *         if it does not exist.
	 */
	public static FillOrder get(final int code) {
		final FillOrder toReturn = lookup.get(code);
		if (toReturn == null) {
			throw new EnumException("Unable to find FillOrder with code: " + code);
		}
		return toReturn;
	}

	@Override
	public int getCode() {
		return code;
	}

	/**
	 * Retrieves the given name of the fill order.
	 *
	 * @return See above.
	 */
	public String getName() {
		return name;
	}

}
