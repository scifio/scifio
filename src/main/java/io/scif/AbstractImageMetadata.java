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

import io.scif.common.DataTools;
import io.scif.util.FormatTools;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.interval.AbstractCalibratedInterval;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;

/**
 * Abstract superclass of all {@link io.scif.ImageMetadata} implementations.
 *
 * @see io.scif.ImageMetadata
 * @see io.scif.DefaultImageMetadata
 * @author Mark Hiner
 */
public abstract class AbstractImageMetadata extends
	AbstractCalibratedInterval<CalibratedAxis>implements ImageMetadata
{

	// -- Constants --

	/** Default thumbnail width and height. */
	public static final long THUMBNAIL_DIMENSION = 128;

	// -- Fields --

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

	/** The name of the image. */
	private String name;

	/** A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	AbstractImageMetadata(final int n) {
		super(n);
	}

	AbstractImageMetadata(final int n, final CalibratedAxis... axes) {
		super(n, axes);
	}

	AbstractImageMetadata(final int n, final List<CalibratedAxis> axes) {
		super(n, axes);
	}

	AbstractImageMetadata(final Interval interval) {
		super(interval);
	}

	AbstractImageMetadata(final Interval interval, final CalibratedAxis... axes) {
		super(interval, axes);
	}

	AbstractImageMetadata(final Interval interval, final List<CalibratedAxis> axes) {
		super(interval, axes);
	}

	AbstractImageMetadata(final Dimensions dimensions) {
		super(dimensions);
	}

	AbstractImageMetadata(final Dimensions dimensions, final CalibratedAxis... axes) {
		super(dimensions, axes);
	}

	AbstractImageMetadata(final Dimensions dimensions, final List<CalibratedAxis> axes) {
		super(dimensions, axes);
	}

	AbstractImageMetadata(final long[] dimensions) {
		super(dimensions);
	}

	AbstractImageMetadata(final long[] dimensions, final CalibratedAxis... axes) {
		super(dimensions, axes);
	}

	AbstractImageMetadata(final long[] dimensions, final List<CalibratedAxis> axes) {
		super(dimensions, axes);
	}

	AbstractImageMetadata(final long[] min, final long[] max) {
		super(min, max);
	}

	AbstractImageMetadata(final long[] min, final long[] max,
		final CalibratedAxis... axes)
	{
		super(min, max, axes);
	}

	AbstractImageMetadata(final long[] min, final long[] max,
		final List<CalibratedAxis> axes)
	{
		super(min, max, axes);
	}

	public AbstractImageMetadata(final ImageMetadata source) {
		super(source);
		copy(source);
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

	// -- Getters --

	@Override
	public long getSize() {
		long size = 1;

		for (int i=0; i<numDimensions(); i++) {
			//FIXME should we be long-backed instead of doubles?
			//FIXME should we have a method in CalibratedRealInterval that is Max - Min?
			size = DataTools.safeMultiply64(size, (long)(realMax(i) - realMin(i)));
		}

		final int bytesPerPixel = getBitsPerPixel() / 8;

		return DataTools.safeMultiply64(size, bytesPerPixel);
	}

	@Override
	public long getThumbSizeX() {
		long thumbX = thumbSizeX;

		// If the X thumbSize isn't explicitly set, scale the actual width using
		// the thumbnail dimension constant
		if (thumbX == 0) {
			//FIXME want this API in calibratedrealinterval
			final long sx = (long)(realMax(dimensionIndex(Axes.X)) - realMin(dimensionIndex(Axes.X)));
			final long sy = (long)(realMax(dimensionIndex(Axes.Y)) - realMin(dimensionIndex(Axes.Y)));

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
			//FIXME want this API in calibratedrealinterval
			final long sx = (long)(realMax(dimensionIndex(Axes.X)) - realMin(dimensionIndex(Axes.X)));
			final long sy = (long)(realMax(dimensionIndex(Axes.Y)) - realMin(dimensionIndex(Axes.Y)));
			thumbY = 1;

			if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION) thumbY = sy;
			else if (sy > sx) thumbY = THUMBNAIL_DIMENSION;
			else if (sx > 0) thumbY = sy * THUMBNAIL_DIMENSION / sx;
			if (thumbY == 0) thumbY = 1;
		}

		return thumbY;
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
	public long getBlockCount() {
		long length = 1;

		for (final CalibratedAxis t : getAxesNonPlanar()) {
			length *= getAxisLength(t);
		}

		return length;
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
	public void copy(final ImageMetadata toCopy) {
		populate(toCopy.getName(), toCopy.getAxes(), toCopy.getAxesLengths(), toCopy
			.getPixelType(), toCopy.isOrderCertain(), toCopy.isLittleEndian(), toCopy
				.isIndexed(), toCopy.isFalseColor(), toCopy.isMetadataComplete());
		// TODO Use setters, not direct assignment.
		this.table = new DefaultMetaTable(toCopy.getTable());
		this.thumbnail = toCopy.isThumbnail();
		this.thumbSizeX = toCopy.getThumbSizeX();
		this.thumbSizeY = toCopy.getThumbSizeY();
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
		// TODO: Use setters, not direct assignment.
		this.name = name;
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
}
