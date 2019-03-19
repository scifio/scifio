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

/**
 * This class represents a single raw TIFF IFD entry. It does not retrieve or
 * store the values from the entry's specific offset and is based on the TIFF
 * 6.0 specification of an IFD entry.
 *
 * @author Chris Allan
 */
public class TiffIFDEntry implements Comparable<Object> {

	/** The <i>Tag</i> that identifies the field. */
	private final int tag;

	/** The field <i>Type</i>. */
	private final IFDType type;

	/** The number of values, <i>Count</i> of the indicated <i>Type</i>. */
	private final int valueCount;

	/**
	 * The <i>Value Offset</i>, the file offset (in bytes) of the <i>Value</i> for
	 * the field.
	 */
	private final long valueOffset;

	public TiffIFDEntry(final int tag, final IFDType type, final int valueCount,
		final long valueOffset)
	{
		this.tag = tag;
		this.type = type;
		this.valueCount = valueCount;
		this.valueOffset = valueOffset;
	}

	/**
	 * Retrieves the entry's <i>Tag</i> value.
	 *
	 * @return the entry's <i>Tag</i> value.
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * Retrieves the entry's <i>Type</i> value.
	 *
	 * @return the entry's <i>Type</i> value.
	 */
	public IFDType getType() {
		return type;
	}

	/**
	 * Retrieves the entry's <i>ValueCount</i> value.
	 *
	 * @return the entry's <i>ValueCount</i> value.
	 */
	public int getValueCount() {
		return valueCount;
	}

	/**
	 * Retrieves the entry's <i>ValueOffset</i> value.
	 *
	 * @return the entry's <i>ValueOffset</i> value.
	 */
	public long getValueOffset() {
		return valueOffset;
	}

	@Override
	public String toString() {
		return "tag = " + tag + ", type = " + type + ", count = " + valueCount +
			", offset = " + valueOffset;
	}

	// -- Comparable API methods --

	@Override
	public int compareTo(final Object o) {
		if (!(o instanceof TiffIFDEntry)) return 1;
		final long offset = ((TiffIFDEntry) o).getValueOffset();

		if (offset == getValueOffset()) return 0;
		return offset < getValueOffset() ? 1 : -1;
	}

}
