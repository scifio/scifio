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

package io.scif.filters;

import io.scif.AbstractMetadata;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;

import net.imglib2.meta.AxisType;

/**
 * Abstract superclass for concrete implementations of {@code MetadataWrapper}.
 * <p>
 * To create a {@code MetadataWrapper} that is paired with a specific
 * {@code Filter}, simply extend this class, override any methods as desired,
 * and annotate the class using {@code DiscoverableMetadataWrapper} as
 * appropriate. The wrapper will automatically be discovered and applied when a
 * new instance of its {@code Filter} is instantiated, assuming the
 * {@code Filter} is a subclass of {@code AbstractReaderFilter}
 * </p>
 * 
 * @author Mark Hiner
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.discovery.DiscoverableMetadataWrapper
 * @see io.scif.filters.AbstractReaderFilter
 */
public abstract class AbstractMetadataWrapper extends AbstractMetadata
	implements MetadataWrapper
{

	// -- Fields --

	private Metadata meta;

	// -- Constructor --

	public AbstractMetadataWrapper() {
		this(null);
	}

	public AbstractMetadataWrapper(final Metadata metadata) {
		meta = metadata;
	}

	// -- MetadataWrapper API Methods --

	/*
	 * @see io.scif.filters.MetadataWrapper#unwrap()
	 */
	public Metadata unwrap() {
		return meta;
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#wrap(io.scif.Metadata)
	 */
	public void wrap(final Metadata meta) {
		this.meta = meta;
		populateImageMetadata();
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#addAxis(int, net.imglib2.meta.AxisType, boolean)
	 */
	public void addAxis(final int imageIndex, final AxisType type,
		final boolean passUp)
	{
		super.addAxis(imageIndex, type);
		if (passUp) meta.addAxis(imageIndex, type);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#addAxis(int, net.imglib2.meta.AxisType, int, boolean)
	 */
	public void addAxis(final int imageIndex, final AxisType type,
		final int value, final boolean passUp)
	{
		super.addAxis(imageIndex, type, value);
		if (passUp) meta.addAxis(imageIndex, type, value);
	}

	/*
	 * @see io.scif.AbstractMetadata#getTable()
	 */
	@Override
	public MetaTable getTable() {
		return super.getTable();
	}

	public void setTable(final MetaTable table, final boolean passUp) {
		super.setTable(table);
		if (passUp) meta.setTable(table);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setThumbSizeX(int, int, boolean)
	 */
	public void setThumbSizeX(final int imageIndex, final int thumbX,
		final boolean passUp)
	{
		super.setThumbSizeX(imageIndex, thumbX);
		if (passUp) meta.setThumbSizeX(imageIndex, thumbX);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setThumbSizeY(int, int, boolean)
	 */
	public void setThumbSizeY(final int imageIndex, final int thumbY,
		final boolean passUp)
	{
		super.setThumbSizeY(imageIndex, thumbY);
		if (passUp) meta.setThumbSizeY(imageIndex, thumbY);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setPixelType(int, int, boolean)
	 */
	public void setPixelType(final int imageIndex, final int type,
		final boolean passUp)
	{
		super.setPixelType(imageIndex, type);
		if (passUp) meta.setPixelType(imageIndex, type);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setBitsPerPixel(int, int, boolean)
	 */
	public void setBitsPerPixel(final int imageIndex, final int bpp,
		final boolean passUp)
	{
		super.setBitsPerPixel(imageIndex, bpp);
		if (passUp) meta.setBitsPerPixel(imageIndex, bpp);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setOrderCertain(int, boolean, boolean)
	 */
	public void setOrderCertain(final int imageIndex, final boolean orderCertain,
		final boolean passUp)
	{
		super.setOrderCertain(imageIndex, orderCertain);
		if (passUp) meta.setOrderCertain(imageIndex, orderCertain);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setRGB(int, boolean, boolean)
	 */
	public void setRGB(final int imageIndex, final boolean rgb,
		final boolean passUp)
	{
		super.setRGB(imageIndex, rgb);
		if (passUp) meta.setRGB(imageIndex, rgb);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setLittleEndian(int, boolean, boolean)
	 */
	public void setLittleEndian(final int imageIndex, final boolean littleEndian,
		final boolean passUp)
	{
		super.setLittleEndian(imageIndex, littleEndian);
		if (passUp) meta.setLittleEndian(imageIndex, littleEndian);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setInterleaved(int, boolean, boolean)
	 */
	public void setInterleaved(final int imageIndex, final boolean interleaved,
		final boolean passUp)
	{
		super.setInterleaved(imageIndex, interleaved);
		if (passUp) meta.setInterleaved(imageIndex, interleaved);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setIndexed(int, boolean, boolean)
	 */
	public void setIndexed(final int imageIndex, final boolean indexed,
		final boolean passUp)
	{
		super.setIndexed(imageIndex, indexed);
		if (passUp) meta.setIndexed(imageIndex, indexed);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setFalseColor(int, boolean, boolean)
	 */
	public void setFalseColor(final int imageIndex, final boolean falseC,
		final boolean passUp)
	{
		super.setFalseColor(imageIndex, falseC);
		if (passUp) meta.setFalseColor(imageIndex, falseC);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setMetadataComplete(int, boolean, boolean)
	 */
	public void setMetadataComplete(final int imageIndex,
		final boolean metadataComplete, final boolean passUp)
	{
		super.setMetadataComplete(imageIndex, metadataComplete);
		if (passUp) meta.setMetadataComplete(imageIndex, metadataComplete);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#add(io.scif.ImageMetadata, boolean)
	 */
	public void add(final ImageMetadata meta, final boolean passUp) {
		super.add(meta);
		if (passUp) this.meta.add(meta);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setThumbnailImage(int, boolean, boolean)
	 */
	public void setThumbnailImage(final int imageIndex, final boolean thumbnail,
		final boolean passUp)
	{
		super.setThumbnailImage(imageIndex, thumbnail);
		if (passUp) meta.setThumbnailImage(imageIndex, thumbnail);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setAxisTypes(int, net.imglib2.meta.AxisType[], boolean)
	 */
	public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes,
		final boolean passUp)
	{
		super.setAxisTypes(imageIndex, axisTypes);
		if (passUp) meta.setAxisTypes(imageIndex, axisTypes);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setAxisType(int, int, net.imglib2.meta.AxisType, boolean)
	 */
	public void setAxisType(final int imageIndex, final int axisIndex,
		final AxisType axis, final boolean passUp)
	{
		super.setAxisType(imageIndex, axisIndex, axis);
		if (passUp) meta.setAxisType(imageIndex, axisIndex, axis);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setAxisLengths(int, int[], boolean)
	 */
	public void setAxisLengths(final int imageIndex, final int[] axisLengths,
		final boolean passUp)
	{
		super.setAxisLengths(imageIndex, axisLengths);
		if (passUp) meta.setAxisLengths(imageIndex, axisLengths);
	}

	/*
	 * @see io.scif.filters.MetadataWrapper#setAxisLength(int, net.imglib2.meta.AxisType, int, boolean)
	 */
	public void setAxisLength(final int imageIndex, final AxisType axis,
		final int length, final boolean passUp)
	{
		super.setAxisLength(imageIndex, axis, length);
		if (passUp) meta.setAxisLength(imageIndex, axis, length);
	}

	/*
	 * @see io.scif.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType)
	 */
	@Override
	public void addAxis(final int imageIndex, final AxisType type) {
		addAxis(imageIndex, type, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType, int)
	 */
	@Override
	public void
		addAxis(final int imageIndex, final AxisType type, final int value)
	{
		addAxis(imageIndex, type, value, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setThumbSizeX(int, int)
	 */
	@Override
	public void setThumbSizeX(final int imageIndex, final int thumbX) {
		setThumbSizeX(imageIndex, thumbX, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setThumbSizeY(int, int)
	 */
	@Override
	public void setThumbSizeY(final int imageIndex, final int thumbY) {
		setThumbSizeY(imageIndex, thumbY, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setPixelType(int, int)
	 */
	@Override
	public void setPixelType(final int imageIndex, final int type) {
		setPixelType(imageIndex, type, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setBitsPerPixel(int, int)
	 */
	@Override
	public void setBitsPerPixel(final int imageIndex, final int bpp) {
		setBitsPerPixel(imageIndex, bpp, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setOrderCertain(int, boolean)
	 */
	@Override
	public void setOrderCertain(final int imageIndex, final boolean orderCertain)
	{
		setOrderCertain(imageIndex, orderCertain, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setRGB(int, boolean)
	 */
	@Override
	public void setRGB(final int imageIndex, final boolean rgb) {
		setRGB(imageIndex, rgb, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setLittleEndian(int, boolean)
	 */
	@Override
	public void setLittleEndian(final int imageIndex, final boolean littleEndian)
	{
		setLittleEndian(imageIndex, littleEndian, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setInterleaved(int, boolean)
	 */
	@Override
	public void setInterleaved(final int imageIndex, final boolean interleaved) {
		setInterleaved(imageIndex, interleaved, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setIndexed(int, boolean)
	 */
	@Override
	public void setIndexed(final int imageIndex, final boolean indexed) {
		setIndexed(imageIndex, indexed, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setFalseColor(int, boolean)
	 */
	@Override
	public void setFalseColor(final int imageIndex, final boolean falseC) {
		setFalseColor(imageIndex, falseC, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setMetadataComplete(int, boolean)
	 */
	@Override
	public void setMetadataComplete(final int imageIndex,
		final boolean metadataComplete)
	{
		setMetadataComplete(imageIndex, metadataComplete, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#add(io.scif.ImageMetadata)
	 */
	@Override
	public void add(final ImageMetadata meta) {
		add(meta, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setThumbnailImage(int, boolean)
	 */
	@Override
	public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
		setThumbnailImage(imageIndex, thumbnail, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
	 */
	@Override
	public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
		setAxisTypes(imageIndex, axisTypes, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setAxisType(int, int, net.imglib2.meta.AxisType)
	 */
	@Override
	public void setAxisType(final int imageIndex, final int axisIndex,
		final AxisType axis)
	{
		setAxisType(imageIndex, axisIndex, axis, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setAxisLengths(int, int[])
	 */
	@Override
	public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
		setAxisLengths(imageIndex, axisLengths, true);
	}

	/*
	 * @see io.scif.AbstractMetadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
	 */
	@Override
	public void setAxisLength(final int imageIndex, final AxisType axis,
		final int length)
	{
		setAxisLength(imageIndex, axis, length, true);
	}

	// -- Metadata API Methods --

	/*
	 * @see io.scif.AbstractMetadata#setSource(io.scif.io.RandomAccessInputStream)
	 */
	@Override
	public void setSource(final RandomAccessInputStream source) {
		super.setSource(source);
		meta.setSource(source);
	}

	public void populateImageMetadata() {
		meta.populateImageMetadata();
	}

	// -- HasSource API Methods --

	/*
	 * @see io.scif.AbstractMetadata#close(boolean)
	 */
	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		meta.close(fileOnly);
	}

}
