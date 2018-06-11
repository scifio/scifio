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

import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;

import org.scijava.util.ArrayUtils;

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
	public static final long THUMBNAIL_DIMENSION = 128;

	// -- Fields --

	/** Cached list of planar axes. */
	private List<CalibratedAxis> planarAxes;

	/** Cached list of non-planar axes. */
	private List<CalibratedAxis> extendedAxes;

	/** Cached list of significant (non-trailing length 1) axes. */
	private List<CalibratedAxis> effectiveAxes;

	/** Width (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeX")
	private long thumbSizeX;

	/** Height (in pixels) of thumbnail planes in this image. */
	@Field(label = "thumbSizeY")
	private long thumbSizeY;

	/**
	 * Describes the number of bytes per pixel. Must be one of the <i>static</i>
	 * pixel types (e.g. {@code INT8}) in {@link io.scif.util.FormatTools}.
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
	 * Number of planar axes in this image. These will always be the first axes in
	 * a list of planar and non-planar axes.
	 */
	@Field(label = "planarAxiscount")
	private int planarAxisCount = -1;

	/**
	 * Number of interleaved axes in this image. These will be the first planar
	 * axes.
	 */
	@Field(label = "interleavedAxisCount")
	private int interleavedAxisCount = -1;

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

	// TODO: Consider typing rois and tables on more specific data structures.

	/** The ROIs for this image. */
	@Field(label = "ROIs")
	private Object rois;

	/** The tables for this image. */
	@Field(label = "tables")
	private Object tables;

	/** The name of the image. */
	private String name;

	/** A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	public AbstractImageMetadata() {
		axes = new ArrayList<>();
		axisLengths = new HashMap<>();
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
	public void setInterleavedAxisCount(final int count) {
		interleavedAxisCount = count;
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
		final CalibratedAxis[] axes = new CalibratedAxis[axisTypes.length];

		for (int i = 0; i < axisTypes.length; i++) {
			final AxisType t = axisTypes[i];
			CalibratedAxis c = getAxis(t);
			if (c == null) c = FormatTools.createAxis(t);
			axes[i] = c;
		}
		setAxes(axes);
	}

	@Override
	public void setAxes(final CalibratedAxis... axisTypes) {
		this.axes = new ArrayList<>(Arrays.asList(axisTypes));
		clearCachedAxes();
	}

	@Override
	public void setAxisLengths(final long[] axisLengths) {
		if (axisLengths.length > axes.size()) throw new IllegalArgumentException(
			"Tried to set " + axisLengths.length + " axis lengths, but " + getAxes()
				.size() + " axes present." + " Call setAxisTypes first.");

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
		if (getAxisIndex(axisType, axes) == -1) {
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
		if (oldIndex < 0) {
			final long length = axisLengths.remove(axes.get(index).type());
			axes.remove(index);

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

		clearCachedAxes();
	}

	@Override
	public void setAxisType(final int index, final AxisType axisType) {
		final CalibratedAxis axis = FormatTools.createAxis(axisType);
		setAxis(index, axis);
	}

	@Override
	public void setROIs(final Object rois) {
		this.rois = rois;
	}

	@Override
	public void setTables(final Object tables) {
		this.tables = tables;
	}

	// -- Getters --

	@Override
	public long getSize() {
		long size = 1;

		for (final CalibratedAxis a : getAxes()) {
			size = ArrayUtils.safeMultiply64(size, getAxisLength(a));
		}

		final int bytesPerPixel = getBitsPerPixel() / 8;

		return ArrayUtils.safeMultiply64(size, bytesPerPixel);
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
	public CalibratedAxis getAxis(final AxisType axisType) {
		for (final CalibratedAxis axis : getAxes()) {
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
		return getEffectiveAxes();
	}

	@Override
	public List<CalibratedAxis> getAxesPlanar() {
		return getAxisList(true);
	}

	@Override
	public List<CalibratedAxis> getAxesNonPlanar() {
		return getAxisList(false);
	}

	@Override
	public long getPlaneCount() {
		long length = 1;

		for (final CalibratedAxis t : getAxesNonPlanar()) {
			length *= getAxisLength(t);
		}

		return length;
	}

	@Override
	public long[] getAxesLengths() {
		return getAxesLengths(getAxes());
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
		return getAxesLengths(getAxesPlanar());
	}

	@Override
	public long[] getAxesLengthsNonPlanar() {
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
		if (planarAxisCount == -1) {
			return SCIFIOMetadataTools.guessPlanarAxisCount(axes);
		}
		return planarAxisCount;
	}

	@Override
	public int getInterleavedAxisCount() {
		if (interleavedAxisCount == -1) {
			return SCIFIOMetadataTools.guessInterleavedAxisCount(axes);
		}
		return interleavedAxisCount;
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
		return getAxes().get(axisIndex);
	}

	@Override
	public long getAxisLength(final int axisIndex) {
		if (axisIndex < 0 || axisIndex >= getAxes().size()) {
			return 1;
		}

		return getAxisLength(getAxis(axisIndex));
	}

	@Override
	public long getAxisLength(final CalibratedAxis t) {
		return t == null ? 1 : getAxisLength(t.type());
	}

	@Override
	public long getAxisLength(final AxisType t) {
		if (axisLengths == null || !axisLengths.containsKey(t) ||
			(effectiveAxes != null && getAxisIndex(t) == -1))
		{
			return 1;
		}

		return axisLengths.get(t);
	}

	@Override
	public int getAxisIndex(final CalibratedAxis axis) {
		return getAxisIndex(axis.type());
	}

	@Override
	public int getAxisIndex(final AxisType axisType) {
		// Use effectiveAxes if possible. If not, default to axes.
		final List<CalibratedAxis> knownAxes = effectiveAxes == null ? axes
			: effectiveAxes;

		return getAxisIndex(axisType, knownAxes);
	}

	@Override
	public Object getROIs() {
		return rois;
	}

	@Override
	public Object getTables() {
		return tables;
	}

	@Override
	public void addAxis(final CalibratedAxis axis) {
		addAxis(axis, 1);
	}

	@Override
	public void addAxis(final CalibratedAxis axis, final long value) {
		if (axes == null) axes = new ArrayList<>();

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
		populate(toCopy.getName(), toCopy.getAxes(), toCopy.getAxesLengths(), toCopy
			.getPixelType(), toCopy.isOrderCertain(), toCopy.isLittleEndian(), toCopy
				.isIndexed(), toCopy.isFalseColor(), toCopy.isMetadataComplete());
		// FIXME: Use setters, not direct assignment.
		this.table = new DefaultMetaTable(toCopy.getTable());
		this.thumbnail = toCopy.isThumbnail();
		this.thumbSizeX = toCopy.getThumbSizeX();
		this.thumbSizeY = toCopy.getThumbSizeY();
		this.planarAxisCount = toCopy.getPlanarAxisCount();
	}

	@Override
	public void populate(final String name, final List<CalibratedAxis> axes,
		final long[] lengths, final int pixelType, final boolean orderCertain,
		final boolean littleEndian, final boolean indexed, final boolean falseColor,
		final boolean metadataComplete)
	{
		populate(name, axes, lengths, pixelType, FormatTools.getBitsPerPixel(
			pixelType), orderCertain, littleEndian, indexed, falseColor,
			metadataComplete);
	}

	@Override
	public void populate(final String name, final List<CalibratedAxis> axes,
		final long[] lengths, final int pixelType, final int bitsPerPixel,
		final boolean orderCertain, final boolean littleEndian,
		final boolean indexed, final boolean falseColor,
		final boolean metadataComplete)
	{
		// FIXME: Use setters, not direct assignment.
		this.name = name;
		this.axes = new ArrayList<>(axes);
		setAxisLengths(lengths.clone());
		this.bitsPerPixel = bitsPerPixel;
		this.falseColor = falseColor;
		this.indexed = indexed;
		this.littleEndian = littleEndian;
		this.orderCertain = orderCertain;
		this.pixelType = pixelType;
	}

	// -- Named API methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
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
	 * Computes and caches the effective (non-trailing-length-1 axes) axis types
	 * for this dataset.
	 */
	private List<CalibratedAxis> getEffectiveAxes() {
		if (effectiveAxes == null && axes != null) {
			int end = axes.size();

			for (; end > getPlanarAxisCount(); end--) {
				final CalibratedAxis axis = axes.get(end - 1);
				if (getAxisLength(axis) > 1) {
					break;
				}
			}

			effectiveAxes = new ArrayList<>();
			for (int i = 0; i < end; i++) {
				effectiveAxes.add(axes.get(i));
			}
		}

		return effectiveAxes;
	}

	/**
	 * Searches the given list of axes for an axis of the given type, returning
	 * the index of the first match.
	 */
	private int getAxisIndex(final AxisType axisType,
		final List<CalibratedAxis> axisList)
	{
		if (axisList == null) return -1;
		int index = -1;
		for (int i = 0; index == -1 && i < axisList.size(); i++) {
			if (axisList.get(i).type().equals(axisType)) index = i;
		}

		return index;
	}

	/**
	 * Resets the cached planar and non-planar axes. Used after the axes or
	 * planarAxisCount are modified.
	 */
	private void clearCachedAxes() {
		planarAxes = null;
		extendedAxes = null;
		effectiveAxes = null;
	}

	private void updateLength(final AxisType axisType, final long value) {
		axisLengths.put(axisType, value);
		// only effectiveAxes needs to be cleared here, because it's the only
		// cached axis that can be affected by axis lengths.
		effectiveAxes = null;
	}

	// If spatial == true, returns every non-CHANNEL axis after both X and Y
	// have been seen. If false, returns every non-CHANNEL axis until both X
	// and Y have been seen.
	private List<CalibratedAxis> getAxisList(final boolean planar) {
		int index = -1;
		int end = -1;
		List<CalibratedAxis> axisList = null;

		if (planar) {
			if (planarAxes == null) planarAxes = new ArrayList<>();
			axisList = planarAxes;
			index = 0;
			end = getPlanarAxisCount();
		}
		else {
			if (extendedAxes == null) extendedAxes = new ArrayList<>();
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
