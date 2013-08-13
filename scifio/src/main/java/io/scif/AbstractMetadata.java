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

import io.scif.io.RandomAccessInputStream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Metadata} implementations.
 * 
 * @see io.scif.Metadata
 * @see io.scif.MetadataOptions
 * @see io.scif.Parser
 * @see io.scif.HasFormat
 * @author Mark Hiner
 */
public abstract class AbstractMetadata extends AbstractHasSource implements
	TypedMetadata
{

	// -- Fields --

	/* The image source associated with this Metadata. */
	private RandomAccessInputStream source;

	/* Whether the Metadata should be filtered or not. */
	protected boolean filtered;

	/* The MetadataOptions used when parsing this Metadata. */
	protected MetadataOptions metadataOptions;

	/* Contains a list of metadata objects for each image in this dataset */
	@io.scif.Field(label = "imageMeta", isList = true)
	private List<ImageMetadata> imageMeta;

	/* A string id for this dataset. */
	private String datasetName = null;

	/* A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	public AbstractMetadata() {
		this((List<ImageMetadata>) null);
	}

	public AbstractMetadata(final Metadata copy) {
		this(copy.getAll());

		table = new DefaultMetaTable(copy.getTable());
	}

	public AbstractMetadata(final List<ImageMetadata> list) {
		imageMeta = new ArrayList<ImageMetadata>();
		table = new DefaultMetaTable();

		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				final ImageMetadata core = list.get(i);
				imageMeta.add(core.copy());
			}
		}
	}

	// -- Metadata API Methods --

	/* @see Metadata#setSource(RandomAccessInputStream) */
	public void setSource(final RandomAccessInputStream source) {
		this.source = source;

		if (source != null) setDatasetName(source.getFileName());
	}

	/* @see Metadata#getSource() */
	public RandomAccessInputStream getSource() {
		return source;
	}

	/* @see Metadata#isFiltered() */
	public boolean isFiltered() {
		return filtered;
	}

	/* @see Metadata#getMetadataOptions() */
	public MetadataOptions getMetadataOptions() {
		return metadataOptions;
	}

	// -- Getters --

	public String getDatasetName() {
		return datasetName;
	}

	/*
	 * @see io.scif.Metadata#get(int)
	 */
	public ImageMetadata get(final int imageIndex) {
		return imageMeta.get(imageIndex);
	}

	/*
	 * @see io.scif.Metadata#getAll()
	 */
	public List<ImageMetadata> getAll() {
		return imageMeta;
	}

	/*
	 * @see io.scif.Metadata#getImageCount()
	 */
	public int getImageCount() {
		return imageMeta.size();
	}

	/*
	 * @see io.scif.Metadata#getPlaneCount(int)
	 */
	public int getPlaneCount(final int imageIndex) {
		return imageMeta.get(imageIndex).getPlaneCount();
	}

	/*
	 * @see io.scif.Metadata#getDatasetSize()
	 */
	public long getDatasetSize() {
		int size = 0;

		for (int i = 0; i < getAll().size(); i++)
			size += getImageSize(i);

		return size;
	}

	/*
	 * @see io.scif.Metadata#getImageSize(int)
	 */
	public long getImageSize(final int imageIndex) {
		return imageMeta.get(imageIndex).getSize();
	}

	/*
	 * @see io.scif.Metadata#isInterleaved(int)
	 */
	public boolean isInterleaved(final int imageIndex) {
		return imageMeta.get(imageIndex).isInterleaved();
	}

	/*
	 * @see io.scif.Metadata#getPixelType(int)
	 */
	public int getPixelType(final int imageIndex) {
		return imageMeta.get(imageIndex).getPixelType();
	}

	/*
	 * @see io.scif.Metadata#getEffectiveSizeC(int)
	 */
	public int getEffectiveSizeC(final int imageIndex) {
		return imageMeta.get(imageIndex).getEffectiveSizeC();
	}

	/*
	 * @see io.scif.Metadata#getRGBChannelCount(int)
	 */
	public int getRGBChannelCount(final int imageIndex) {
		return imageMeta.get(imageIndex).getRGBChannelCount();
	}

	/*
	 * @see io.scif.Metadata#isLittleEndian(int)
	 */
	public boolean isLittleEndian(final int imageIndex) {
		return imageMeta.get(imageIndex).isLittleEndian();
	}

	/*
	 * @see io.scif.Metadata#isIndexed(int)
	 */
	public boolean isIndexed(final int imageIndex) {
		return imageMeta.get(imageIndex).isIndexed();
	}

	/*
	 * @see io.scif.Metadata#getBitsPerPixel(int)
	 */
	public int getBitsPerPixel(final int imageIndex) {
		return imageMeta.get(imageIndex).getBitsPerPixel();
	}

	/*
	 * @see io.scif.Metadata#isRGB(int)
	 */
	public boolean isRGB(final int imageIndex) {
		return imageMeta.get(imageIndex).isRGB();
	}

	/*
	 * @see io.scif.Metadata#isFalseColor(int)
	 */
	public boolean isFalseColor(final int imageIndex) {
		return imageMeta.get(imageIndex).isFalseColor();
	}

	/*
	 * @see io.scif.Metadata#getThumbSizeX(int)
	 */
	public int getThumbSizeX(final int imageIndex) {
		return imageMeta.get(imageIndex).getThumbSizeX();
	}

	/*
	 * @see io.scif.Metadata#getThumbSizeY(int)
	 */
	public int getThumbSizeY(final int imageIndex) {
		return imageMeta.get(imageIndex).getThumbSizeY();
	}
	
	public CalibratedAxis getAxis(int imageIndex, AxisType type) {
		return imageMeta.get(imageIndex).getAxis(type);
	}
	
	/*
	 * @see io.scif.Metadata#getAxisCount(int)
	 */
	public int getAxisCount(final int imageIndex) {
		return imageMeta.get(imageIndex).getAxesLengths().length;
	}

	/*
	 * @see io.scif.Metadata#getAxisType(int, int)
	 */
	public CalibratedAxis getAxisType(final int imageIndex, final int planeIndex)
	{
		return imageMeta.get(imageIndex).getAxisType(planeIndex);
	}

	/*
	 * @see io.scif.Metadata#getAxisLength(int, int)
	 */
	public int getAxisLength(final int imageIndex, final int planeIndex) {
		return imageMeta.get(imageIndex).getAxisLength(planeIndex);
	}

	/*
	 * @see io.scif.Metadata#getAxisLength(int, net.imglib2.meta.AxisType)
	 */
	public int getAxisLength(final int imageIndex, final CalibratedAxis t) {
		return imageMeta.get(imageIndex).getAxisLength(t);
	}

	public int getAxisLength(final int imageIndex, final AxisType t) {
		return imageMeta.get(imageIndex).getAxisLength(t);
	}

	/*
	 * @see io.scif.Metadata#getAxisIndex(int, net.imglib2.meta.AxisType)
	 */
	public int getAxisIndex(final int imageIndex, final CalibratedAxis type) {
		return imageMeta.get(imageIndex).getAxisIndex(type);
	}

	public int getAxisIndex(final int imageIndex, final AxisType type) {
		return imageMeta.get(imageIndex).getAxisIndex(type);
	}

	/*
	 * @see io.scif.Metadata#getAxes(int)
	 */
	public CalibratedAxis[] getAxes(final int imageIndex) {
		return imageMeta.get(imageIndex).getAxes();
	}

	/*
	 * @see io.scif.Metadata#getAxesLengths(int)
	 */
	public int[] getAxesLengths(final int imageIndex) {
		return imageMeta.get(imageIndex).getAxesLengths();
	}

	/*
	 * @see io.scif.Metadata#isOrderCertain(int)
	 */
	public boolean isOrderCertain(final int imageIndex) {
		return imageMeta.get(imageIndex).isOrderCertain();
	}

	/*
	 * @see io.scif.Metadata#isThumbnailImage(int)
	 */
	public boolean isThumbnailImage(final int imageIndex) {
		return imageMeta.get(imageIndex).isThumbnail();
	}

	/*
	 * @see io.scif.Metadata#isMetadataComplete(int)
	 */
	public boolean isMetadataComplete(final int imageIndex) {
		return imageMeta.get(imageIndex).isMetadataComplete();
	}

	// -- Setters --

	/*
	 * @see io.scif.Metadata#setDatasetName(java.lang.String)
	 */
	public void setDatasetName(final String name) {
		datasetName = name;
	}

	/*
	 * @see io.scif.Metadata#setThumbSizeX(int, int)
	 */
	public void setThumbSizeX(final int imageIndex, final int thumbX) {
		imageMeta.get(imageIndex).setThumbSizeX(thumbX);
	}

	/*
	 * @see io.scif.Metadata#setThumbSizeY(int, int)
	 */
	public void setThumbSizeY(final int imageIndex, final int thumbY) {
		imageMeta.get(imageIndex).setThumbSizeY(thumbY);
	}

	/*
	 * @see io.scif.Metadata#setPixelType(int, int)
	 */
	public void setPixelType(final int imageIndex, final int type) {
		imageMeta.get(imageIndex).setPixelType(type);
	}

	/*
	 * @see io.scif.Metadata#setBitsPerPixel(int, int)
	 */
	public void setBitsPerPixel(final int imageIndex, final int bpp) {
		imageMeta.get(imageIndex).setBitsPerPixel(bpp);
	}

	/*
	 * @see io.scif.Metadata#setOrderCertain(int, boolean)
	 */
	public void setOrderCertain(final int imageIndex, final boolean orderCertain)
	{
		imageMeta.get(imageIndex).setOrderCertain(orderCertain);
	}

	/*
	 * @see io.scif.Metadata#setRGB(int, boolean)
	 */
	public void setRGB(final int imageIndex, final boolean rgb) {
		imageMeta.get(imageIndex).setRGB(rgb);
	}

	/*
	 * @see io.scif.Metadata#setLittleEndian(int, boolean)
	 */
	public void setLittleEndian(final int imageIndex, final boolean littleEndian)
	{
		imageMeta.get(imageIndex).setLittleEndian(littleEndian);
	}

	/*
	 * @see io.scif.Metadata#setInterleaved(int, boolean)
	 */
	public void setInterleaved(final int imageIndex, final boolean interleaved) {
		imageMeta.get(imageIndex).setInterleaved(interleaved);
	}

	/*
	 * @see io.scif.Metadata#setIndexed(int, boolean)
	 */
	public void setIndexed(final int imageIndex, final boolean indexed) {
		imageMeta.get(imageIndex).setIndexed(indexed);
	}

	/*
	 * @see io.scif.Metadata#setFalseColor(int, boolean)
	 */
	public void setFalseColor(final int imageIndex, final boolean falseC) {
		imageMeta.get(imageIndex).setFalseColor(falseC);
	}

	/*
	 * @see io.scif.Metadata#setMetadataComplete(int, boolean)
	 */
	public void setMetadataComplete(final int imageIndex,
		final boolean metadataComplete)
	{
		imageMeta.get(imageIndex).setMetadataComplete(metadataComplete);
	}

	/*
	 * @see io.scif.Metadata#setFiltered(boolean)
	 */
	public void setFiltered(final boolean filtered) {
		this.filtered = filtered;
	}

	/*
	 * @see io.scif.Metadata#setMetadataOptions(io.scif.MetadataOptions)
	 */
	public void setMetadataOptions(final MetadataOptions opts) {
		metadataOptions = opts;
	}

	/*
	 * @see io.scif.Metadata#add(io.scif.ImageMetadata)
	 */
	public void add(final ImageMetadata meta) {
		imageMeta.add(meta);
	}

	/*
	 * @see io.scif.Metadata#setThumbnailImage(int, boolean)
	 */
	public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
		imageMeta.get(imageIndex).setThumbnail(thumbnail);
	}

	/*
	 * @see io.scif.Metadata#addAxis(int, net.imglib2.meta.AxisType)
	 */
	public void addAxis(final int imageIndex, final CalibratedAxis type) {
		imageMeta.get(imageIndex).addAxis(type);
	}

	/*
	 * @see io.scif.Metadata#addAxis(int, net.imglib2.meta.AxisType, int)
	 */
	public void addAxis(final int imageIndex, final CalibratedAxis type,
		final int value)
	{
		imageMeta.get(imageIndex).addAxis(type, value);
	}

	public void
		addAxis(final int imageIndex, final AxisType type, final int value)
	{
		imageMeta.get(imageIndex).addAxis(type, value);
	}

	/*
	 * @see io.scif.Metadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
	 */
	public void setAxisTypes(final int imageIndex,
		final CalibratedAxis[] axisTypes)
	{
		imageMeta.get(imageIndex).setAxisTypes(axisTypes);
	}

	/*
	 * @see io.scif.Metadata#setAxisType(int, int, net.imglib2.meta.AxisType)
	 */
	public void setAxisType(final int imageIndex, final int axisIndex,
		final CalibratedAxis axis)
	{
		imageMeta.get(imageIndex).setAxisType(axisIndex, axis);
	}

	public void setAxisType(final int imageIndex, final int axisIndex,
		final AxisType axis)
	{
		imageMeta.get(imageIndex).setAxisType(axisIndex, axis);
	}

	/*
	 * @see io.scif.Metadata#setAxisLengths(int, int[])
	 */
	public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
		imageMeta.get(imageIndex).setAxisLengths(axisLengths);
	}

	/*
	 * @see io.scif.Metadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
	 */
	public void setAxisLength(final int imageIndex, final CalibratedAxis axis,
		final int length)
	{
		imageMeta.get(imageIndex).setAxisLength(axis, length);
	}

	public void setAxisLength(final int imageIndex, final AxisType axis,
		final int length)
	{
		imageMeta.get(imageIndex).setAxisLength(axis, length);
	}

	/*
	 * @see io.scif.Metadata#createImageMetadata(int)
	 */
	public void createImageMetadata(final int imageCount) {
		imageMeta.clear();

		for (int i = 0; i < imageCount; i++)
			add(new DefaultImageMetadata());
	}

	// -- HasMetaTable API Methods --

	/*
	 * @see io.scif.HasMetaTable#getTable()
	 */
	public MetaTable getTable() {
		if (table == null) table = new DefaultMetaTable();
		return table;
	}

	/*
	 * @see io.scif.HasMetaTable#setTable(io.scif.MetaTable)
	 */
	public void setTable(final MetaTable table) {
		this.table = table;
	}

	// -- HasSource API Methods --

	/*
	 * @see io.scif.HasSource#close(boolean)
	 */
	public void close(final boolean fileOnly) throws IOException {
		if (source != null) {
			source.close();
		}

		if (!fileOnly) reset(getClass());
	}

	// -- Helper Methods --

	private void reset(final Class<?> type) {
		if (type == null || type == AbstractMetadata.class) return;

		for (final Field f : type.getDeclaredFields()) {
			f.setAccessible(true);

			if (Modifier.isFinal(f.getModifiers())) continue;

			// only reset annotated fields
			if (f.getAnnotation(io.scif.Field.class) == null) continue;

			final Class<?> fieldType = f.getType();

			try {
				if (fieldType == boolean.class) f.set(this, false);
				else if (fieldType == char.class) f.set(this, 0);
				else if (fieldType == double.class) f.set(this, 0.0);
				else if (fieldType == float.class) f.set(this, 0f);
				else if (fieldType == int.class) f.set(this, 0);
				else if (fieldType == long.class) f.set(this, 0l);
				else if (fieldType == short.class) f.set(this, 0);
				else f.set(this, null);
			}
			catch (final IllegalArgumentException e) {
				log().debug(e.getMessage());
			}
			catch (final IllegalAccessException e) {
				log().debug(e.getMessage());
			}

			table = new DefaultMetaTable();
			imageMeta = new ArrayList<ImageMetadata>();

			// check superclasses and interfaces
			reset(type.getSuperclass());
			for (final Class<?> c : type.getInterfaces()) {
				reset(c);
			}
		}
	}
}
