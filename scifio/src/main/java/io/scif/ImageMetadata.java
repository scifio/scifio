/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif;

import io.scif.util.FormatTools;

import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

/**
 * ImageMetadata stores the metadata for a single image within a dataset. Here,
 * information common to every format (e.g. height, width, index information)
 * can be stored and retrieved in a standard way.
 * 
 * @author Mark Hiner
 */
public interface ImageMetadata extends HasMetaTable {

	/** Sets width (in pixels) of thumbnail planes in this image. */
	void setThumbSizeX(long thumbSizeX);

	/** Sets height (in pixels) of thumbnail planes in this image. */
	void setThumbSizeY(long thumbSizeY);

	/**
	 * Sets the number of bytes per pixel. Valid pixel type constants (e.g.,
	 * {@link FormatTools#INT8}) are enumerated in {@link FormatTools}.
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
	 * Sets whether or not the planes are stored as indexed color. An indexed
	 * color image treats each pixel value as an index into a color table
	 * containing one or more (typically 3) actual values for the pixel.
	 */
	void setIndexed(boolean indexed);

	/**
	 * Sets the number of planar axes in this image. This value represents the
	 * number of dimensional axes constituting each {@link Plane} (as returned by
	 * the {@link Reader#openPlane} methods). This value is necessary to determine
	 * the total plane count for an N-dimensional image, as well as how many
	 * pixels there are per plane.
	 * <p>
	 * For example, suppose we have a 4-dimensional image with axes (X, Y, Z, T)
	 * and extents (768, 512, 7, 13). If there are two planar axes, then each
	 * plane is 768 x 512 and there are 7 x 13 = 91 total planes. But if we have
	 * three planar axes, then each plane is 768 x 512 x 7 and there are 13 total
	 * planes.
	 * </p>
	 * 
	 * @see Reader#openPlane(int, int)
	 */
	void setPlanarAxisCount(final int count);

	/**
	 * Sets the number of interleaved axes in this image. This must be a value
	 * between [0, planarAxisCount). Interleaved axes are planar axes that do not
	 * constitute the "canonical" axes - e.g., in a CXY image with an interleaved
	 * axis count of 1, the C axis is interleaved, and each plane is an XY plane
	 * with C different representations.
	 */
	void setInterleavedAxisCount(final int count);

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
	 * Sets the type of the axis at the specified index, creating a default
	 * {@link CalibratedAxis} using {@link FormatTools#createAxis(AxisType)} if
	 * one is not already defined.
	 */
	void setAxisType(int index, AxisType axis);

	/** Returns the size, in bytes, of all planes in this image. */
	long getSize();

	/** Returns the size, in bytes, of one plane in this image. */
	long getPlaneSize();

	/** Returns the width (in pixels) of the thumbnail planes in this image. */
	long getThumbSizeX();

	/** Returns the height (in pixels) of the thumbnail planes in this image. */
	long getThumbSizeY();

	/**
	 * Returns the CalibratedAxis associated with the given type. Useful to
	 * retrieve calibration information.
	 */	
	CalibratedAxis getAxis(AxisType axisType);

	/**
	 * Returns the number of bytes per pixel. Should correlate with the pixel
	 * types in {@link io.scif.util.FormatTools}
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

	/** Returns true if the planes are stored as indexed color. */
	boolean isIndexed();

	/** Returns the number of planar axes in this image. */
	int getPlanarAxisCount();

	/** 
	 * Returns the number of interleaved axes in this image.
	 */
	int getInterleavedAxisCount();

	/** Returns true if the {@link Axes#CHANNEL} axis is a planar axis. */
	boolean isMultichannel();

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
	 * Gets the axis of the (zero-indexed) specified plane.
	 * 
	 * @param axisIndex - index of the desired axis within this image
	 * @return Type of the desired plane.
	 */
	CalibratedAxis getAxis(final int axisIndex);

	/**
	 * Gets the length of the (zero-indexed) specified plane.
	 * 
	 * @param axisIndex - index of the desired axis within this image
	 * @return Length of the desired axis, or 0 if the axis is not found.
	 */
	long getAxisLength(final int axisIndex);

	/**
	 * A convenience method for looking up the length of an axis based on its
	 * type. No knowledge of plane ordering is necessary.
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
	 * Returns an array of the AxisTypes that, together, define the bounds of a
	 * single plane in the dataset.
	 * 
	 * @return List of CalibratedAxes. Ordering in the list indicates the axis
	 *         order in the image.
	 */
	List<CalibratedAxis> getAxesPlanar();

	/**
	 * Returns an array of the AxisTypes that define the number of planes in the
	 * dataset.
	 * 
	 * @return List of CalibratedAxes. Ordering in the list indicates the axis
	 *         order in the image.
	 */
	List<CalibratedAxis> getAxesNonPlanar();

	/**
	 * @return the number of planes in this image
	 */
	long getPlaneCount();

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
	 * Returns an array of the lengths for the planar axes in this image.
	 * 
	 * @return Sorted axis length array
	 */
	long[] getAxesLengthsPlanar();

	/**
	 * Returns an array of the lengths for the non-planar axes in this image.
	 * 
	 * @return Sorted axis length array
	 */
	long[] getAxesLengthsNonPlanar();

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
	 * As {@link #addAxis(CalibratedAxis, long)} using a default
	 * {@link CalibratedAxis} created by {@link FormatTools#createAxis(AxisType)}.
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
}
