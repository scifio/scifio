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

package io.scif.img.cell.loaders;

import io.scif.FormatException;
import io.scif.img.cell.SCIFIOCellImg;

import java.io.IOException;

import net.imglib2.Interval;
import net.imglib2.display.ColorTable;

/**
 * Interface for requesting arrays from SCIFIO {@link io.scif.Reader}s by
 * {@link SCIFIOCellImg}s.
 *
 * @author Mark Hiner
 */
public interface SCIFIOArrayLoader<A> {

	/**
	 * @return Bits per element for the type of this loader.
	 */
	int getBitsPerElement();

	/**
	 * @param bounds bounds of the plane.
	 * @return An opened plane using the given bounds.
	 */
	A loadArray(Interval bounds);

	/**
	 * @param entities Desired entity capacity
	 * @return An empty array of this loader's type, capable of holding the given
	 *         number of entities.
	 */
	A emptyArray(final int entities);

	/**
	 * @param index The image index this loader should use when opening planes.
	 */
	void setIndex(int index);

	/**
	 * Returns the {@link ColorTable} for a given set of indices. If the desired
	 * plane has already been opened, the table should be cached. Otherwise, this
	 * will trigger the loading of a small sub-plane to access the desired table.
	 *
	 * @param imageIndex Image index of desired {@code ColorTable}
	 * @param planeIndex Plane index of desired {@code ColorTable}
	 * @return The {@code ColorTable} for the specified indices
	 */
	ColorTable loadTable(int imageIndex, int planeIndex) throws FormatException,
		IOException;
}
