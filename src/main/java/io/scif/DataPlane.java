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

import net.imglib2.Interval;

/**
 * Extension of the base {@link Plane} interface. Adds the concept of a native
 * underlying data representation.
 *
 * @see io.scif.Plane
 * @author Mark Hiner
 * @param <T> the native data type used to store this plane's pixel information.
 */
public interface DataPlane<T> extends Plane {

	/**
	 * Sets the native pixel data for this plane. If {@link #getColorTable()}
	 * returns null, then this is the pixel data. Otherwise, this data should be
	 * used as an index into the associated ColorTable.
	 *
	 * @param data - an object matching the native data type of this plane.
	 */
	void setData(T data);

	/**
	 * Gets this plane's type-specific pixel data.
	 *
	 * @return The native representation for this plane's data.
	 */
	T getData();

	/**
	 * Populate this plane by copying the provided plane.
	 *
	 * @param plane the plane to copy
	 * @return A reference to this plane
	 */
	DataPlane<T> populate(DataPlane<T> plane);

	/**
	 * Populate this plane using the specified lengths and offsets, with the
	 * provided data representation of the pixels in that region.
	 *
	 * @param data Pixel information for the region of this plane.
	 * @param bounds bounds of the planar axes.
	 * @return A reference to this plane
	 */
	DataPlane<T> populate(T data, Interval bounds);

	/**
	 * As {@link #populate(Object, Interval)}, but also sets the ImageMetadata of
	 * the source from which this Plane was read.
	 *
	 * @param meta ImageMetadata of the source associated with this Plane
	 * @param data Pixel information for the region of this plane.
	 * @param bounds bounds of the planar axes.
	 * @return A reference to this plane
	 */
	DataPlane<T> populate(ImageMetadata meta, T data, Interval bounds);
}
