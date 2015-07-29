/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
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

package io.scif;

import java.util.List;

import org.scijava.Named;

import io.scif.util.FormatTools;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;

/**
 * ImageMetadata stores the metadata for a single image within a dataset. Here,
 * information common to every format (e.g. height, width, index information)
 * can be stored and retrieved in a standard way.
 *
 * @author Mark Hiner
 */
public interface ImageMetadata extends Named, HasMetaTable {

	/** Sets width (in pixels) of thumbnail blocks in this image. */
	void setThumbSizeX(long thumbSizeX);

	/** Sets height (in pixels) of thumbnail blocks in this image. */
	void setThumbSizeY(long thumbSizeY);

	/**
	 * Sets the data type associated with a pixel. Valid pixel type constants
	 * (e.g., {@link FormatTools#INT8}) are enumerated in {@link FormatTools}.
	 */
	void setPixelType(int pixelType);

	/** Sets the number of valid bits per pixel. */
	void setBitsPerPixel(int bitsPerPixel);

	/**
	 * Sets whether or not we are confident that the dimension order is correct.
	 */
	void setOrderCertain(boolean orderCertain);

	/** Sets whether or not each pixel's bytes are in little endian order. */
	void setLittleEndian(boolean littleEndian);

	/**
	 * Sets whether or not the blocks are stored as indexed color. An indexed
	 * color image treats each pixel value as an index into a color table
	 * containing one or more (typically 3) actual values for the pixel.
	 */
	void setIndexed(boolean indexed);

	/** Sets whether or not we can ignore the color map (if present). */
	void setFalseColor(boolean falseColor);

	/**
	 * Sets whether or not we are confident that all of the metadata stored within
	 * the image has been parsed.
	 */
	void setMetadataComplete(boolean metadataComplete);

	/**
	 * Sets whether or not this image is a lower-resolution copy of another image.
	 */
	void setThumbnail(boolean thumbnail);

	/**
	 * Convenience method to set both the axis types and lengths for this
	 * ImageMetadata.
	 */
	void setAxes(CalibratedAxis[] axes, long[] axisLengths);

	/**
	 * Sets the Axes types for this image. Order is implied by ordering within
	 * this array
	 */
	void setAxisTypes(AxisType... axisTypes);

	/**
	 * Sets the Axes types for this image. Order is implied by ordering within
	 * this array
	 */
	void setAxes(CalibratedAxis... axes);

	/**
	 * Sets the lengths of each axis. Order is parallel of {@code axes}.
	 * <p>
	 * NB: axes must already exist for this method to be called. Use
	 * {@link #setAxes(CalibratedAxis[])} or {@link #setAxes}
	 */
	void setAxisLengths(long[] axisLengths);

	/**
	 * Sets the length for the specified axis. Adds the axis if if its type is not
	 * already present in the image.
	 */
	void setAxisLength(CalibratedAxis axis, long length);

	/**
	 * As {@link #setAxisLength(CalibratedAxis, long)} but requires only the
	 * AxisType.
	 */
	void setAxisLength(AxisType axis, long length);

	/**
	 * Sets the axis at the specified index, if an axis with a matching type is
	 * not already defined. Otherwise the axes are re-ordered, per
	 * {@link java.util.List#add(int, Object)}.
	 */
	void setAxis(int index, CalibratedAxis axis);

	/**
	 * As {@link #setAxis(int, CalibratedAxis)} but using the default calibration
	 * values, per {@link FormatTools#createAxis(AxisType)}.
	 */
	void setAxisType(int index, AxisType axis);

	/** Returns the size, in bytes, of this image. */
	long getSize();

	/** Returns the size, in bytes, of one block in this image. */
	long getBlockSize();

	/** Returns the width (in pixels) of the thumbnail blocks in this image. */
	long getThumbSizeX();

	/** Returns the height (in pixels) of the thumbnail blocks in this image. */
	long getThumbSizeY();

	/**
	 * Returns the CalibratedAxis associated with the given type. Useful to
	 * retrieve calibration information.
	 */
	CalibratedAxis getAxis(AxisType axisType);

	/**
	 * Returns the data type associated with a pixel. Valid pixel type constants
	 * (e.g., {@link FormatTools#INT8}) are enumerated in {@link FormatTools}.
	 */
	int getPixelType();

	/** Returns the number of valid bits per pixel. */
	int getBitsPerPixel();

	/**
	 * Returns true if we are confident that the dimension order is correct.
	 */
	boolean isOrderCertain();

	/** Returns true if each pixel's bytes are in little endian order. */
	boolean isLittleEndian();

	/** Returns true if the blocks are stored as indexed color. */
	boolean isIndexed();

	/** Returns true if we can ignore the color map (if present). */
	boolean isFalseColor();

	/**
	 * Returns true if we are confident that all of the metadata stored within the
	 * image has been parsed.
	 */
	boolean isMetadataComplete();

	/**
	 * Determines whether or not this image is a lower-resolution copy of another
	 * image.
	 *
	 * @return true if this image is a thumbnail
	 */
	boolean isThumbnail();

	/**
	 * Gets the axis of the (zero-indexed) specified block.
	 *
	 * @param axisIndex - index of the desired axis within this image
	 * @return Type of the desired block.
	 */
	CalibratedAxis getAxis(final int axisIndex);

	/**
	 * Gets the length of the (zero-indexed) specified block.
	 *
	 * @param axisIndex - index of the desired axis within this image
	 * @return Length of the desired axis, or 0 if the axis is not found.
	 */
	long getAxisLength(final int axisIndex);

	/**
	 * A convenience method for looking up the length of an axis based on its
	 * type. No knowledge of block ordering is necessary.
	 *
	 * @param t - CalibratedAxis to look up
	 * @return Length of axis t, or 0 if the axis is not found.
	 */
	long getAxisLength(final CalibratedAxis t);

	/**
	 * As {@link #getAxisLength(CalibratedAxis)} but only requires the
	 * {@link AxisType} of the desired axis.
	 *
	 * @param t - CalibratedAxis to look up
	 * @return Length of axis t, or 0 if the axis is not found.
	 */
	long getAxisLength(final AxisType t);

	/**
	 * Returns the array index for the specified CalibratedAxis. This index can be
	 * used in other Axes methods for looking up lengths, etc...
	 * <p>
	 * This method can also be used as an existence check for the target
	 * CalibratedAxis.
	 * </p>
	 *
	 * @param axis - axis to look up
	 * @return The index of the desired axis or -1 if not found.
	 */
	int getAxisIndex(final CalibratedAxis axis);

	/**
	 * As {@link #getAxisIndex(CalibratedAxis)} but only requires the
	 * {@link AxisType} of the desired axis.
	 *
	 * @param axisType - axis type to look up
	 * @return The index of the desired axis or -1 if not found.
	 */
	int getAxisIndex(final AxisType axisType);

	/**
	 * Returns an array of the types for axes associated with the specified image
	 * index. Order is consistent with the axis length (int) array returned by
	 * {@link #getAxesLengths()}.
	 * <p>
	 * CalibratedAxis order is sorted and represents order within the image.
	 * </p>
	 *
	 * @return List of CalibratedAxes. Ordering in the list indicates the axis
	 *         order in the image.
	 */
	List<CalibratedAxis> getAxes();

	/**
	 * @return the number of blocks in this image
	 */
	long getBlockCount();

	/**
	 * Returns an array of the lengths for axes associated with the specified
	 * image index.
	 * <p>
	 * Ordering is consistent with the CalibratedAxis array returned by
	 * {@link #getAxes()}.
	 * </p>
	 *
	 * @return Sorted axis length array
	 */
	long[] getAxesLengths();

	/**
	 * Returns an array of the lengths for axes in the provided AxisType list.
	 * <p>
	 * Ordering of the lengths is consistent with the provided ordering.
	 * </p>
	 *
	 * @return Sorted axis length array
	 */
	long[] getAxesLengths(final List<CalibratedAxis> axes);

	/**
	 * Appends the provided {@link CalibratedAxis} to the metadata's list of axes,
	 * with a length of 1.
	 *
	 * @param axis - The new axis
	 */
	void addAxis(final CalibratedAxis axis);

	/**
	 * Appends the provided CalibratedAxis to the current CalibratedAxis array and
	 * creates a corresponding entry with the specified value in axis lengths.
	 *
	 * @param axis - The new axis
	 * @param value - length of the new axis
	 */
	void addAxis(final CalibratedAxis axis, final long value);

	/**
	 * As {@link #addAxis(CalibratedAxis, long)} using the default calibration
	 * value, per {@link FormatTools#createAxis(AxisType)}.
	 */
	void addAxis(final AxisType axisType, final long value);

	/**
	 * @return A new copy of this ImageMetadata.
	 */
	ImageMetadata copy();

	/**
	 * Populates this ImageMetadata using the provided instance.
	 *
	 * @param toCopy - ImageMetadata to copy
	 */
	void copy(ImageMetadata toCopy);

	/**
	 * As
	 * {@link #populate(String, List, long[], int, boolean, boolean, boolean, boolean, boolean)}
	 * but automatically determines bits per pixel.
	 */
	void populate(String name, List<CalibratedAxis> axes, long[] lengths,
		int pixelType, boolean orderCertain, boolean littleEndian, boolean indexed,
		boolean falseColor, boolean metadataComplete);

	/**
	 * Convenience method for manually populating an ImageMetadata.
	 */
	void populate(String name, List<CalibratedAxis> axes, long[] lengths,
		int pixelType, int bitsPerPixel, boolean orderCertain,
		boolean littleEndian, boolean indexed, boolean falseColor,
		boolean metadataComplete);
}
