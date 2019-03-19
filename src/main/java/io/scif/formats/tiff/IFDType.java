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
 * An enumeration of IFD types.
 */
public enum IFDType implements CodedEnum {

		// IFD types
		BYTE(1, 1), ASCII(2, 1), SHORT(3, 2), LONG(4, 4), RATIONAL(5, 8), SBYTE(6,
			1), UNDEFINED(7, 1), SSHORT(8, 2), SLONG(9, 4), SRATIONAL(10, 8), FLOAT(
				11, 4), DOUBLE(12, 8), IFD(13, 4), LONG8(16, 8), SLONG8(17, 8), IFD8(18,
					8);

	/** Code for the IFD type in the actual TIFF file. */
	private int code;

	/** Number of bytes per element of this type. */
	private int bytesPerElement;

	private static final Map<Integer, IFDType> lookup = new HashMap<>();

	/** Reverse lookup of code to IFD type enumerate value. */
	static {
		for (final IFDType v : EnumSet.allOf(IFDType.class)) {
			lookup.put(v.getCode(), v);
		}
	}

	/**
	 * Retrieves a IFD type by reverse lookup of its "code".
	 *
	 * @param code The code to look up.
	 * @return The {@code IFDType} instance for the {@code code} or {@code null}
	 *         if it does not exist.
	 */
	public static IFDType get(final int code) {
		final IFDType toReturn = lookup.get(code);
		if (toReturn == null) {
			throw new EnumException("Unable to find IFDType with code: " + code);
		}
		return toReturn;
	}

	/**
	 * Default constructor.
	 *
	 * @param code Integer "code" for the IFD type.
	 * @param bytesPerElement Number of bytes per element.
	 */
	private IFDType(final int code, final int bytesPerElement) {
		this.code = code;
		this.bytesPerElement = bytesPerElement;
	}

	@Override
	public int getCode() {
		return code;
	}

	/**
	 * Retrieves the number of bytes per element.
	 *
	 * @return See above.
	 */
	public int getBytesPerElement() {
		return bytesPerElement;
	}

}
