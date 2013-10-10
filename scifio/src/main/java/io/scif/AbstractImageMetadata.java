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

import io.scif.common.DataTools;
import io.scif.util.FormatTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

/**
 * Abstract superclass of all {@link io.scif.ImageMetadata} implementations.
 * 
 * @see io.scif.ImageMetadata
 * @see io.scif.DefaultImageMetadata
 * @author Mark Hiner
 */
public abstract class AbstractImageMetadata implements ImageMetadata {

	// -- Constants --

	/** Default thumbnail width and height. */
	protected static final long THUMBNAIL_DIMENSION = 128;

	// -- Fields --

	/** Cached list of planar axes. */
	private List<CalibratedAxis> planarAxes;

	/** Cached list of non-planar axes. */
	private List<CalibratedAxis> extendedAxes;

	/** Width (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeX")
	private long thumbSizeX;

	/** Height (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeY")
	private long thumbSizeY;

	/**
	 * Describes the number of bytes per pixel. Must be one of the <i>static</i>
	 * pixel types (e.g. <code>INT8</code>) in {@link io.scif.util.FormatTools}.
	 */
	@Field(label = "pixelType")
	private int pixelType;

	/** Number of valid bits per pixel. */
	@Field(label = "bitsPerPixel")
	private int bitsPerPixel;

	/**
	 * The Axes types for this image. Order is implied by ordering within this
	 * array
	 */
	@Field(label = "dimTypes")
	private List<CalibratedAxis> axes;

	/** Lengths of each axis. Order is parallel of dimTypes. */
	@Field(label = "dimLengths")
	private final HashMap<AxisType, Long> axisLengths;

	/**
	 * Indicates whether or not we are confident that the dimension order is
	 * correct.
	 */
	@Field(label = "orderCertain")
	private boolean orderCertain;

	/** Indicates whether or not each pixel's bytes are in little endian order. */
	@Field(label = "littleEndian")
	private boolean littleEndian;

	/** Indicates whether or not the images are stored as indexed color. */
	@Field(label = "indexed")
	private boolean indexed;

	/**
	 * Number of planar axes in this image. These will always be the first axes
	 * in a list of planar and non-planar axes.
	 */
	@Field(label = "planarAxiscount")
	private int planarAxisCount = -1;

	/** Indicates whether or not we can ignore the color map (if present). */
	@Field(label = "falseColor")
	private boolean falseColor = true;

	/**
	 * Indicates whether or not we are confident that all of the metadata stored
	 * within the file has been parsed.
	 */
	@Field(label = "metadataComplete")
	private boolean metadataComplete;

	/**
	 * Indicates whether or not this series is a lower-resolution copy of another
	 * series.
	 */
	@Field(label = "thumbnail")
	private boolean thumbnail;

	/* A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	public AbstractImageMetadata() {
		axes = new ArrayList<CalibratedAxis>();
		axisLengths = new HashMap<AxisType, Long>();
	}

	public AbstractImageMetadata(final ImageMetadata copy) {
		this();
		copy(copy);
	}

	// -- Setters --

	@Override
	public void setThumbSizeX(final long thumbSizeX) {
		this.thumbSizeX = thumbSizeX;
	}

	@Override
	public void setThumbSizeY(final long thumbSizeY) {
		this.thumbSizeY = thumbSizeY;
	}

	@Override
	public void setPixelType(final int pixelType) {
		this.pixelType = pixelType;
	}

	@Override
	public void setBitsPerPixel(final int bitsPerPixel) {
		this.bitsPerPixel = bitsPerPixel;
	}

	@Override
	public void setOrderCertain(final boolean orderCertain) {
		this.orderCertain = orderCertain;
	}

	@Override
	public void setLittleEndian(final boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	@Override
	public void setIndexed(final boolean indexed) {
		this.indexed = indexed;
	}

	@Override
	public void setPlanarAxisCount(final int count) {
		planarAxisCount = count;
		clearCachedAxes();
	}

	@Override
	public void setFalseColor(final boolean falseColor) {
		this.falseColor = falseColor;
	}

	@Override
	public void setMetadataComplete(final boolean metadataComplete) {
		this.metadataComplete = metadataComplete;
	}

	@Override
	public void setThumbnail(final boolean thumbnail) {
		this.thumbnail = thumbnail;
	}

	@Override
	public void setAxes(final CalibratedAxis[] axes, final long[] axisLengths) {
		setAxes(axes);
		setAxisLengths(axisLengths);
	}

	@Override
	public void setAxisTypes(final AxisType... axisTypes) {
		CalibratedAxis[] axes = new CalibratedAxis[axisTypes.length];
		
		for (int i=0; i<axisTypes.length; i++) {
			AxisType t = axisTypes[i];
			CalibratedAxis c = getAxis(t);
			if (c == null) c = FormatTools.createAxis(t);
			axes[i] = c;
		}
		setAxes(axes);
	}

	@Override
	public void setAxes(final CalibratedAxis... axisTypes) {
		this.axes = new ArrayList<CalibratedAxis>(Arrays.asList(axisTypes));
		clearCachedAxes();
	}

	@Override
	public void setAxisLengths(final long[] axisLengths) {
		if (axisLengths.length > axes.size()) throw new IllegalArgumentException(
			"Tried to set " + axisLengths.length + " axis lengths, but " +
				axes.size() + " axes present." + " Call setAxisTypes first.");

		for (int i = 0; i < axisLengths.length; i++) {
			updateLength(axes.get(i).type(), axisLengths[i]);
		}
	}

	@Override
	public void setAxisLength(final CalibratedAxis axis, final long length) {
		setAxisLength(axis.type(), length);
	}

	@Override
	public void setAxisLength(final AxisType axisType, final long length) {
		if (getAxisIndex(axisType) == -1) {
			addAxis(FormatTools.createAxis(axisType), length);
		}
		else {
			updateLength(axisType, length);
		}
	}

	@Override
	public void setAxis(final int index, final CalibratedAxis axis) {
		final int oldIndex = getAxisIndex(axis);

		// Replace existing axis

		if (oldIndex >= 0) {
			final long length = axisLengths.remove(axes.get(oldIndex).type());
			axes.remove(oldIndex);

			if (index == axes.size()) {
				axes.add(axis);
			}
			else {
				axes.add(index, axis);
			}
			axisLengths.put(axis.type(), length);
		}
		// Axis is already in the list. Move it here.
		else {
			axes.remove(axes.get(oldIndex));
			axes.add(index, axis);
		}
	}

	@Override
	public void setAxisType(final int index, final AxisType axisType) {
		setAxis(index, FormatTools.createAxis(axisType));
	}

	// -- Getters --

	@Override
	public long getSize() {
		long size = 1;

		for (final CalibratedAxis a : axes) {
			size = DataTools.safeMultiply64(size, getAxisLength(a));
		}

		final int bytesPerPixel = getBitsPerPixel() / 8;

		return DataTools.safeMultiply64(size, bytesPerPixel);
	}

	@Override
	public long getPlaneSize() {
		return getSize() / getPlaneCount();
	}

	@Override

	public long getThumbSizeX() {
		long thumbX = thumbSizeX;

		// If the X thumbSize isn't explicitly set, scale the actual width using
		// the thumbnail dimension constant
		if (thumbX == 0) {
			final long sx = getAxisLength(Axes.X);
			final long sy = getAxisLength(Axes.Y);

			if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION) thumbX = sx;
			else if (sx > sy) thumbX = THUMBNAIL_DIMENSION;
			else if (sy > 0) thumbX = sx * THUMBNAIL_DIMENSION / sy;
			if (thumbX == 0) thumbX = 1;
		}

		return thumbX;
	}

	@Override
	public long getThumbSizeY() {
		long thumbY = thumbSizeY;

		// If the Y thumbSize isn't explicitly set, scale the actual width using
		// the thumbnail dimension constant
		if (thumbY == 0) {
			final long sx = getAxisLength(Axes.X);
			final long sy = getAxisLength(Axes.Y);
			thumbY = 1;

			if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION) thumbY = sy;
			else if (sy > sx) thumbY = THUMBNAIL_DIMENSION;
			else if (sx > 0) thumbY = sy * THUMBNAIL_DIMENSION / sx;
			if (thumbY == 0) thumbY = 1;
		}

		return thumbY;
	}

	@Override
	public CalibratedAxis getAxis(AxisType axisType) {
		for (CalibratedAxis axis : axes) {
			if (axis.type().equals(axisType)) return axis;
		}
		return null;
	}
	
	@Override
	public int getPixelType() {
		return pixelType;
	}

	@Override
	public int getBitsPerPixel() {
		if (bitsPerPixel <= 0) return FormatTools.getBitsPerPixel(pixelType);
		return bitsPerPixel;
	}

	@Override
	public List<CalibratedAxis> getAxes() {
		return axes;
	}

	@Override
	public List<CalibratedAxis> getAxesPlanar() {
		initializedCheck();
		return getAxisList(true);
	}

	@Override
	public List<CalibratedAxis> getAxesNonPlanar() {
		initializedCheck();
		return getAxisList(false);
	}

	@Override
	public int getPlaneCount() {
		initializedCheck();
		int length = 1;

		for (final CalibratedAxis t : getAxesNonPlanar()) {
			length *= getAxisLength(t);
		}

		return length;
	}

	@Override
	public long[] getAxesLengths() {
		return getAxesLengths(axes);
	}

	@Override
	public long[] getAxesLengths(final List<CalibratedAxis> axes) {
		final long[] lengths = new long[axes.size()];

		for (int i = 0; i < axes.size(); i++) {
			lengths[i] = getAxisLength(axes.get(i));
		}

		return lengths;
	}

	@Override
	public long[] getAxesLengthsPlanar() {
		initializedCheck();
		return getAxesLengths(getAxesPlanar());
	}

	@Override
	public long[] getAxesLengthsNonPlanar() {
		initializedCheck();
		return getAxesLengths(getAxesNonPlanar());
	}

	@Override
	public boolean isOrderCertain() {
		return orderCertain;
	}

	@Override
	public boolean isLittleEndian() {
		return littleEndian;
	}

	@Override
	public boolean isIndexed() {
		return indexed;
	}

	@Override
	public int getPlanarAxisCount() {
		initializedCheck();
		return planarAxisCount;
	}

	@Override
	public boolean isInterleaved() {
		return getAxisIndex(Axes.X) != 0 || getAxisIndex(Axes.Y) != 1;
	}

	@Override
	public boolean isMultichannel() {
		final int cIndex = getAxisIndex(Axes.CHANNEL);
		return (cIndex < getPlanarAxisCount() && cIndex >= 0);
	}

	@Override
	public boolean isFalseColor() {
		return falseColor;
	}

	@Override
	public boolean isMetadataComplete() {
		return metadataComplete;
	}

	@Override
	public boolean isThumbnail() {
		return thumbnail;
	}

	@Override
	public CalibratedAxis getAxis(final int axisIndex) {
		return axes.get(axisIndex);
	}

	@Override

	public long getAxisLength(final int axisIndex) {
		if (axisIndex < 0 || axisIndex >= axes.size()) throw new IllegalArgumentException(
			"Invalid axisIndex: " + axisIndex + ". " + axes.size() +
				" axes present.");

		return getAxisLength(axes.get(axisIndex).type());
	}

	@Override
	public long getAxisLength(final CalibratedAxis t) {
		return getAxisLength(t.type());
	}

	@Override
	public long getAxisLength(final AxisType t) {
		if (axisLengths == null || !axisLengths.containsKey(t)) return 1;

		return axisLengths.get(t);
	}

	@Override
	public int getAxisIndex(final CalibratedAxis axis) {
		// FIXME: It is unintuitive that you can pass a CalibratedAxis that is *not*
		// one of the ImageMetadata's actual axes, and get back a value other than
		// -1 (since it merely looks up the matching type). We may want to have a
		// hash on CalibratedAxis objects, too, and change behavior of this method.
		return getAxisIndex(axis.type());
	}

	@Override
	public int getAxisIndex(final AxisType axisType) {
		if (axes == null) return -1;

		int index = -1;

		for (int i = 0; index == -1 && i < axes.size(); i++) {
			if (axes.get(i).type().equals(axisType)) index = i;
		}

		return index;
	}

	@Override
	public void addAxis(final CalibratedAxis axis) {
		addAxis(axis, 1);
	}

	@Override
	public void addAxis(final CalibratedAxis axis, final long value) {
		if (axes == null) axes = new ArrayList<CalibratedAxis>();

		// See if the axis already exists
		if (!axes.contains(axis)) {
			axes.add(axis);
			clearCachedAxes();
		}

		updateLength(axis.type(), value);
	}

	@Override
	public void addAxis(final AxisType axisType, final long value) {
		addAxis(FormatTools.createAxis(axisType), value);
	}

	@Override
	public void copy(final ImageMetadata toCopy) {
		table = new DefaultMetaTable(toCopy.getTable());

		axes = new ArrayList<CalibratedAxis>(toCopy.getAxes());
		setAxisLengths(toCopy.getAxesLengths().clone());
		bitsPerPixel = toCopy.getBitsPerPixel();
		falseColor = toCopy.isFalseColor();
		indexed = toCopy.isIndexed();
		planarAxisCount = toCopy.getPlanarAxisCount();
		littleEndian = toCopy.isLittleEndian();
		metadataComplete = toCopy.isMetadataComplete();
		orderCertain = toCopy.isOrderCertain();
		pixelType = toCopy.getPixelType();
		thumbnail = toCopy.isThumbnail();
		thumbSizeX = toCopy.getThumbSizeX();
		thumbSizeY = toCopy.getThumbSizeY();
	}

	// -- HasTable API Methods --

	@Override
	public MetaTable getTable() {
		if (table == null) table = new DefaultMetaTable();
		return table;
	}

	@Override
	public void setTable(final MetaTable table) {
		this.table = table;
	}

	// -- Object API --

	@Override
	public String toString() {
		return new FieldPrinter(this).toString();
	}

	// -- Helper methods --

	/**
	 * Resets the cached planar and non-planar axes. Used after the axes
	 * or planarAxisCount are modified.
	 */
	private void clearCachedAxes() {
		planarAxes = null;
		extendedAxes = null;
	}

	/**
	 * Sanity check to guarantee the validity of the current metadata. This makes
	 * things slightly annoying, in that we can not choose a sensible default
	 * value (e.g. 2). However, this seems preferable to the alternative of
	 * trusting plane counts, interleaved and multichannel checks, etc... after
	 * setting up the axes correctly but not specifying this count.
	 */
	private void initializedCheck() {
		if (planarAxisCount == -1) {
			throw new IllegalStateException(
				"Invalid Metadata state: planar axis count not set.");
		}
	}

	private void updateLength(final AxisType axisType, final long value) {
		axisLengths.put(axisType, value);
	}

	// If spatial == true, returns every non-CHANNEL axis after both X and Y
	// have been seen. If false, returns every non-CHANNEL axis until both X
	// and Y have been seen.
	private List<CalibratedAxis> getAxisList(final boolean planar) {
		int index = -1;
		int end = -1;
		List<CalibratedAxis> axisList = null;

		if (planar) {
			if (planarAxes == null) planarAxes = new ArrayList<CalibratedAxis>();
			axisList = planarAxes;
			index = 0;
			end = getPlanarAxisCount();
		}
		else {
			if (extendedAxes == null) extendedAxes = new ArrayList<CalibratedAxis>();
			axisList = extendedAxes;
			index = getPlanarAxisCount();
			end = getAxes().size();
		}

		if (axisList.size() == 0) {
			synchronized (axisList) {
				if (axisList.size() == 0) {

					axisList.clear();

					int position = 0;
					for (; index < end; index++) {
						if (position <= axisList.size()) {
							axisList.add(getAxes().get(index));
							position++;
						}
						else {
							axisList.set(position++, getAxes().get(index));
						}
					}
				}
			}
		}

		return axisList;
	}
}
