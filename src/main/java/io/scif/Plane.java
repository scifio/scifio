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
import net.imglib2.display.ColorTable;
import net.imglib2.util.Intervals;

import org.scijava.Contextual;

/**
 * Top-level interface for all Plane representations in SCIFIO.
 * <p>
 * Planes are X,Y slices of pixel data from a parent image, and potentially
 * include a {@link net.imglib2.display.ColorTable} if that parent uses indexed
 * color.
 * </p>
 * <p>
 * This interface guarantees that a Plane will have a ColorTable, and the pixel
 * data of a Plane can ultimately be converted to a byte[].
 *
 * @see net.imglib2.display.ColorTable
 * @author Mark Hiner
 */
public interface Plane extends Contextual {

	/**
	 * Sets the ColorTable for this plane. ColorTables are used for indexed color
	 * planes, where the underlying pixel data is an index into the associated
	 * color (lookup) table.
	 *
	 * @param lut - a ColorTable implementation.
	 */
	void setColorTable(ColorTable lut);

	/**
	 * Gets this plane's ColorTable.
	 *
	 * @return A reference to the ColorTable instance associated with this plane.
	 */
	ColorTable getColorTable();

	/**
	 * Gets this plane's standardized pixel data. This byte[] is guaranteed to be
	 * consistent across multiple calls to this method, for a given native data
	 * object.
	 *
	 * @return The standardized representation of this plane's data.
	 */
	byte[] getBytes();

	/**
	 * Gets the {@link ImageMetadata} associated with this plane. The
	 * ImageMetadata returned by this method can then be used to answer questions
	 * about this plane.
	 *
	 * @return An ImageMetadata instance describing the image associated with this
	 *         plane.
	 */
	ImageMetadata getImageMetadata();

	/**
	 * @return The minimums of this plane relative to the origin image
	 */
	default long[] getMin() {
		return Intervals.minAsLongArray(getBounds());
	}

	/**
	 * @return The maximums of this plane relative to the origin image
	 */
	default long[] getMax() {
		return Intervals.maxAsLongArray(getBounds());
	}

	/**
	 * @return The lengths of each axis of this plane
	 */
	default long[] getLengths() {
		return Intervals.dimensionsAsLongArray(getBounds());
	}

	/**
	 * @return The bounds of this plane relative to the origin image
	 */
	Interval getBounds();

	/**
	 * Populates this planes offsets, dimensions and Metadata.
	 *
	 * @param meta - ImageMetadata to associate with this Plane
	 * @param bounds bounds of the planar axes.
	 * @return A reference to this Plane
	 */
	Plane populate(ImageMetadata meta, Interval bounds);

	/**
	 * Populates this plane by copying the fields of the provided plane
	 *
	 * @param p - A Plane to copy
	 * @return A reference to this Plane
	 */
	Plane populate(Plane p);

	/**
	 * Sets the ImageMetadata representation of the underlying image.
	 *
	 * @param meta - an initialized ImageMetadata instance.
	 */
	void setImageMetadata(ImageMetadata meta);

	/**
	 * Sets this plane's bounds relative to the underlying image.
	 */
	void setBounds(Interval bounds);
}
