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
	protected static final int THUMBNAIL_DIMENSION = 128;

	// -- Fields --

	/** Number of planes in this image */
	@Field(label = "planeCount")
	private int planeCount;

	/** Width (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeX")
	private int thumbSizeX = 0;

	/** Height (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeY")
	private int thumbSizeY = 0;

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
	private List<CalibratedAxis> axisTypes;

	/** Lengths of each axis. Order is parallel of dimTypes. */
	@Field(label = "dimLengths")
	private final HashMap<AxisType, Integer> axisLengths;

	/**
	 * Indicates whether or not we are confident that the dimension order is
	 * correct.
	 */
	@Field(label = "orderCertain")
	private boolean orderCertain;

	/**
	 * Indicates whether or not the images are stored as RGB (multiple channels
	 * per plane).
	 */
	@Field(label = "rgb")
	private boolean rgb;

	/** Indicates whether or not each pixel's bytes are in little endian order. */
	@Field(label = "littleEndian")
	private boolean littleEndian;

	/**
	 * True if channels are stored RGBRGBRGB...; false if channels are stored
	 * RRR...GGG...BBB...
	 */
	@Field(label = "interleaved")
	private boolean interleaved;

	/** Indicates whether or not the images are stored as indexed color. */
	@Field(label = "indexed")
	private boolean indexed;

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
		axisTypes = new ArrayList<CalibratedAxis>();
		axisLengths = new HashMap<AxisType, Integer>();
	}

	public AbstractImageMetadata(final ImageMetadata copy) {
		this();
		copy(copy);
	}

	// -- Setters --

	public void setThumbSizeX(final int thumbSizeX) {
		this.thumbSizeX = thumbSizeX;
	}

	public void setThumbSizeY(final int thumbSizeY) {
		this.thumbSizeY = thumbSizeY;
	}

	public void setPixelType(final int pixelType) {
		this.pixelType = pixelType;
	}

	public void setBitsPerPixel(final int bitsPerPixel) {
		this.bitsPerPixel = bitsPerPixel;
	}

	public void setOrderCertain(final boolean orderCertain) {
		this.orderCertain = orderCertain;
	}

	public void setRGB(final boolean rgb) {
		this.rgb = rgb;
	}

	public void setLittleEndian(final boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	public void setInterleaved(final boolean interleaved) {
		this.interleaved = interleaved;
	}

	public void setIndexed(final boolean indexed) {
		this.indexed = indexed;
	}

	public void setFalseColor(final boolean falseColor) {
		this.falseColor = falseColor;
	}

	public void setMetadataComplete(final boolean metadataComplete) {
		this.metadataComplete = metadataComplete;
	}

	public void setThumbnail(final boolean thumbnail) {
		this.thumbnail = thumbnail;
	}

	public void
		setAxes(final CalibratedAxis[] axisTypes, final int[] axisLengths)
	{
		setAxisTypes(axisTypes);
		setAxisLengths(axisLengths);
	}

	public void setAxisTypes(final CalibratedAxis[] axisTypes) {
		this.axisTypes = new ArrayList<CalibratedAxis>(Arrays.asList(axisTypes));
	}

	public void setAxisLengths(final int[] axisLengths) {
		if (axisLengths.length != axisTypes.size()) throw new IllegalArgumentException(
			"Tried to set " + axisLengths.length + " axis lengths, but " +
				axisTypes.size() + " axes present." + " Call setAxisTypes first.");

		for (int i = 0; i < axisTypes.size(); i++) {
			updateLength(axisTypes.get(i).type(), axisLengths[i]);
		}
	}

	public void setAxisLength(final CalibratedAxis axis, final int length) {
		setAxisLength(axis.type(), length);
	}

	public void setAxisLength(final AxisType axis, final int length) {
		if (getAxisIndex(axis) == -1) addAxis(FormatTools.calibrate(axis), length);
		else updateLength(axis, length);
	}

	public void setAxisType(final int index, final CalibratedAxis axis) {
		final int oldIndex = getAxisIndex(axis);

		// Replace existing axis
		if (oldIndex == -1) {
			final int length = axisLengths.remove(axisTypes.get(index));

			axisTypes.set(index, axis);
			axisLengths.put(axis.type(), length);
		}
		// Axis is already in the list. Move it here.
		else {
			axisTypes.remove(axis);
			axisTypes.add(index, axis);
		}
	}

	public void setAxisType(final int index, final AxisType axis) {
		setAxisType(index, FormatTools.calibrate(axis));
	}

	public void setPlaneCount(final int planeCount) {
		this.planeCount = planeCount;
	}

	// -- Getters --

	public int getPlaneCount() {
		return planeCount;
	}

	public long getSize() {
		long size = 1;

		for (final CalibratedAxis a : axisTypes) {
			size = DataTools.safeMultiply64(size, getAxisLength(a));
		}

		final int bytesPerPixel = getBitsPerPixel() / 8;

		return DataTools.safeMultiply64(size, bytesPerPixel);
	}

	public int getThumbSizeX() {
		int thumbX = thumbSizeX;

		// If the X thumbSize isn't explicitly set, scale the actual width using
		// the thumbnail dimension constant
		if (thumbX == 0) {
			final int sx = getAxisLength(Axes.X);
			final int sy = getAxisLength(Axes.Y);

			if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION) thumbX = sx;
			else if (sx > sy) thumbX = THUMBNAIL_DIMENSION;
			else if (sy > 0) thumbX = sx * THUMBNAIL_DIMENSION / sy;
			if (thumbX == 0) thumbX = 1;
		}

		return thumbX;
	}

	public int getThumbSizeY() {
		int thumbY = thumbSizeY;

		// If the Y thumbSize isn't explicitly set, scale the actual width using
		// the thumbnail dimension constant
		if (thumbY == 0) {
			final int sx = getAxisLength(Axes.X);
			final int sy = getAxisLength(Axes.Y);
			thumbY = 1;

			if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION) thumbY = sy;
			else if (sy > sx) thumbY = THUMBNAIL_DIMENSION;
			else if (sx > 0) thumbY = sy * THUMBNAIL_DIMENSION / sx;
			if (thumbY == 0) thumbY = 1;
		}

		return thumbY;
	}

	public CalibratedAxis getAxis(AxisType type) {
		for (CalibratedAxis axis : axisTypes) {
			if (axis.type().equals(type)) return axis;
		}
		return null;
	}
	
	public int getPixelType() {
		return pixelType;
	}

	public int getBitsPerPixel() {
		return bitsPerPixel;
	}

	public CalibratedAxis[] getAxes() {
		return axisTypes.toArray(new CalibratedAxis[axisTypes.size()]);
	}

	public int[] getAxesLengths() {
		final int[] lengths = new int[axisTypes.size()];

		for (int i = 0; i < axisTypes.size(); i++) {
			lengths[i] = getAxisLength(axisTypes.get(i));
		}

		return lengths;
	}

	public boolean isOrderCertain() {
		return orderCertain;
	}

	public boolean isRGB() {
		return rgb;
	}

	public boolean isLittleEndian() {
		return littleEndian;
	}

	public boolean isInterleaved() {
		return interleaved;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public boolean isFalseColor() {
		return falseColor;
	}

	public boolean isMetadataComplete() {
		return metadataComplete;
	}

	public boolean isThumbnail() {
		return thumbnail;
	}

	public int getEffectiveSizeC() {
		final int sizeZT = getAxisLength(Axes.Z) * getAxisLength(Axes.TIME);
		if (sizeZT == 0) return 0;
		return getPlaneCount() / sizeZT;
	}

	public int getRGBChannelCount() {
		if (!isRGB()) return 1;

		final int effC = getEffectiveSizeC();
		if (effC == 0) return 0;
		return getAxisLength(Axes.CHANNEL) / effC;
	}

	public CalibratedAxis getAxisType(final int axisIndex) {
		return axisTypes.get(axisIndex);
	}

	public int getAxisLength(final int axisIndex) {
		if (axisIndex < 0 || axisIndex >= axisTypes.size()) throw new IllegalArgumentException(
			"Invalid axisIndex: " + axisIndex + ". " + axisTypes.size() +
				" axes present.");

		return getAxisLength(axisTypes.get(axisIndex).type());
	}

	public int getAxisLength(final CalibratedAxis t) {
		return getAxisLength(t.type());
	}

	public int getAxisLength(final AxisType t) {
		if (axisLengths == null || !axisLengths.containsKey(t)) return 0;

		return axisLengths.get(t);
	}

	public int getAxisIndex(final CalibratedAxis type) {
		return getAxisIndex(type.type());
	}

	public int getAxisIndex(final AxisType type) {
		if (axisTypes == null) return -1;

		int index = -1;

		for (int i = 0; index == -1 && i < axisTypes.size(); i++) {
			if (axisTypes.get(i).type().equals(type)) index = i;
		}

		return index;
	}

	public void addAxis(final CalibratedAxis type) {
		addAxis(type, 0);
	}

	public void addAxis(final CalibratedAxis type, final int value) {
		if (axisTypes == null) axisTypes = new ArrayList<CalibratedAxis>();

		// See if the axis already exists
		if (!axisTypes.contains(type)) axisTypes.add(type);

		updateLength(type.type(), value);
	}

	public void addAxis(final AxisType type, final int value) {
		addAxis(FormatTools.calibrate(type), value);
	}

	public void copy(final ImageMetadata toCopy) {
		table = new DefaultMetaTable(toCopy.getTable());

		axisTypes = new ArrayList<CalibratedAxis>(Arrays.asList(toCopy.getAxes()));
		setAxisLengths(toCopy.getAxesLengths().clone());
		bitsPerPixel = toCopy.getBitsPerPixel();
		falseColor = toCopy.isFalseColor();
		indexed = toCopy.isIndexed();
		interleaved = toCopy.isInterleaved();
		littleEndian = toCopy.isLittleEndian();
		metadataComplete = toCopy.isMetadataComplete();
		orderCertain = toCopy.isOrderCertain();
		pixelType = toCopy.getPixelType();
		planeCount = toCopy.getPlaneCount();
		rgb = toCopy.isRGB();
		thumbnail = toCopy.isThumbnail();
		thumbSizeX = toCopy.getThumbSizeX();
		thumbSizeY = toCopy.getThumbSizeY();
	}

	// -- HasTable API Methods --

	public MetaTable getTable() {
		if (table == null) table = new DefaultMetaTable();
		return table;
	}

	public void setTable(final MetaTable table) {
		this.table = table;
	}

	// -- Serializable API Methods --

	// -- Object API --

	@Override
	public String toString() {
		return new FieldPrinter(this).toString();
	}

	// -- Helper methods --

	private void updateLength(final AxisType type, final int value) {
		axisLengths.put(type, value);
		
	}
}
