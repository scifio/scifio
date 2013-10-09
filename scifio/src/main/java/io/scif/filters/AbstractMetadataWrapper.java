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
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

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

	@Override
	public Metadata unwrap() {
		return meta;
	}

	@Override
	public void wrap(final Metadata meta) {
		this.meta = meta;
		setSource(meta.getSource());
		populateImageMetadata();
	}

	@Override
	public void addAxis(final int imageIndex, final CalibratedAxis axis,
		final boolean passUp)
	{
		super.addAxis(imageIndex, axis);
		if (passUp) meta.addAxis(imageIndex, axis);
	}

	@Override
	public void addAxis(final int imageIndex, final CalibratedAxis axis,
		final long value, final boolean passUp)
	{
		super.addAxis(imageIndex, axis, value);
		if (passUp) meta.addAxis(imageIndex, axis, value);
	}

	@Override
	public MetaTable getTable() {
		return super.getTable();
	}

	public void setTable(final MetaTable table, final boolean passUp) {
		super.setTable(table);
		if (passUp) meta.setTable(table);
	}

	@Override
	public void setThumbSizeX(final int imageIndex, final long thumbX,
		final boolean passUp)
	{
		super.setThumbSizeX(imageIndex, thumbX);
		if (passUp) meta.setThumbSizeX(imageIndex, thumbX);
	}

	@Override
	public void setThumbSizeY(final int imageIndex, final long thumbY,
		final boolean passUp)
	{
		super.setThumbSizeY(imageIndex, thumbY);
		if (passUp) meta.setThumbSizeY(imageIndex, thumbY);
	}

	@Override
	public void setPixelType(final int imageIndex, final int type,
		final boolean passUp)
	{
		super.setPixelType(imageIndex, type);
		if (passUp) meta.setPixelType(imageIndex, type);
	}

	@Override
	public void setBitsPerPixel(final int imageIndex, final int bpp,
		final boolean passUp)
	{
		super.setBitsPerPixel(imageIndex, bpp);
		if (passUp) meta.setBitsPerPixel(imageIndex, bpp);
	}

	@Override
	public void setOrderCertain(final int imageIndex, final boolean orderCertain,
		final boolean passUp)
	{
		super.setOrderCertain(imageIndex, orderCertain);
		if (passUp) meta.setOrderCertain(imageIndex, orderCertain);
	}

	@Override
	public void setLittleEndian(final int imageIndex, final boolean littleEndian,
		final boolean passUp)
	{
		super.setLittleEndian(imageIndex, littleEndian);
		if (passUp) meta.setLittleEndian(imageIndex, littleEndian);
	}

	@Override
	public void setIndexed(final int imageIndex, final boolean indexed,
		final boolean passUp)
	{
		super.setIndexed(imageIndex, indexed);
		if (passUp) meta.setIndexed(imageIndex, indexed);
	}

	@Override
	public void
		setPlanarAxisCount(int imageIndex, final int count, boolean passUp)
	{
		super.setPlanarAxisCount(imageIndex, count);
		if (passUp) meta.setPlanarAxisCount(imageIndex, count);
	}

	@Override
	public void setFalseColor(final int imageIndex, final boolean falseC,
		final boolean passUp)
	{
		super.setFalseColor(imageIndex, falseC);
		if (passUp) meta.setFalseColor(imageIndex, falseC);
	}

	@Override
	public void setMetadataComplete(final int imageIndex,
		final boolean metadataComplete, final boolean passUp)
	{
		super.setMetadataComplete(imageIndex, metadataComplete);
		if (passUp) meta.setMetadataComplete(imageIndex, metadataComplete);
	}

	@Override
	public void add(final ImageMetadata meta, final boolean passUp) {
		super.add(meta);
		if (passUp) this.meta.add(meta);
	}

	@Override
	public void setThumbnailImage(final int imageIndex, final boolean thumbnail,
		final boolean passUp)
	{
		super.setThumbnailImage(imageIndex, thumbnail);
		if (passUp) meta.setThumbnailImage(imageIndex, thumbnail);
	}

	@Override
	public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes,
		boolean passUp) {
		setAxes(imageIndex, FormatTools.createAxes(axisTypes), passUp);
	}

	@Override
	public void setAxes(final int imageIndex,
		final CalibratedAxis[] axes, final boolean passUp)
	{
		super.setAxes(imageIndex, axes);
		if (passUp) meta.setAxes(imageIndex, axes);
	}

	@Override
	public void setAxis(final int imageIndex, final int axisIndex,
		final CalibratedAxis axis, final boolean passUp)
	{
		super.setAxis(imageIndex, axisIndex, axis);
		if (passUp) meta.setAxis(imageIndex, axisIndex, axis);
	}

	@Override
	public void setAxisLengths(final int imageIndex, final long[] axisLengths,
		final boolean passUp)
	{
		super.setAxisLengths(imageIndex, axisLengths);
		if (passUp) meta.setAxisLengths(imageIndex, axisLengths);
	}

	@Override
	public void setAxisLength(final int imageIndex, final CalibratedAxis axis,
		final long length, final boolean passUp)
	{
		super.setAxisLength(imageIndex, axis, length);
		if (passUp) meta.setAxisLength(imageIndex, axis, length);
	}
	
	@Override
	public void addAxis(int imageIndex, AxisType axisType, int value,
		boolean passUp)
	{
		super.addAxis(imageIndex, axisType, value);
		if (passUp) meta.addAxis(imageIndex, axisType, value);
		
	}

	@Override
	public void setAxisType(int imageIndex, int axisIndex, AxisType axisType,
		boolean passUp)
	{
		super.setAxisType(imageIndex, axisIndex, axisType);
		if (passUp) meta.setAxisType(imageIndex, axisIndex, axisType);
	}

	@Override
	public void setAxisLength(int imageIndex, AxisType axisType, long length,
		boolean passUp)
	{
		super.setAxisLength(imageIndex, axisType, length);
		if (passUp) meta.setAxisLength(imageIndex, axisType, length);
	}

	@Override
	public void addAxis(final int imageIndex, final CalibratedAxis axis) {
		addAxis(imageIndex, axis, true);
	}

	@Override
	public void addAxis(final int imageIndex, final CalibratedAxis axis,
		final long value)
	{
		addAxis(imageIndex, axis, value, true);
	}

	@Override
	public void setThumbSizeX(final int imageIndex, final long thumbX) {
		setThumbSizeX(imageIndex, thumbX, true);
	}

	@Override
	public void setThumbSizeY(final int imageIndex, final long thumbY) {
		setThumbSizeY(imageIndex, thumbY, true);
	}

	@Override
	public void setPixelType(final int imageIndex, final int type) {
		setPixelType(imageIndex, type, true);
	}

	@Override
	public void setBitsPerPixel(final int imageIndex, final int bpp) {
		setBitsPerPixel(imageIndex, bpp, true);
	}

	@Override
	public void setOrderCertain(final int imageIndex, final boolean orderCertain)
	{
		setOrderCertain(imageIndex, orderCertain, true);
	}

	@Override
	public void setLittleEndian(final int imageIndex, final boolean littleEndian)
	{
		setLittleEndian(imageIndex, littleEndian, true);
	}

	@Override
	public void setIndexed(final int imageIndex, final boolean indexed) {
		setIndexed(imageIndex, indexed, true);
	}

	@Override
	public void setFalseColor(final int imageIndex, final boolean falseC) {
		setFalseColor(imageIndex, falseC, true);
	}

	@Override
	public void setMetadataComplete(final int imageIndex,
		final boolean metadataComplete)
	{
		setMetadataComplete(imageIndex, metadataComplete, true);
	}

	@Override
	public void add(final ImageMetadata meta) {
		add(meta, true);
	}

	@Override
	public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
		setThumbnailImage(imageIndex, thumbnail, true);
	}

	@Override
	public void setAxes(final int imageIndex,
		final CalibratedAxis[] axes)
	{
		setAxes(imageIndex, axes, true);
	}

	@Override
	public void setAxis(final int imageIndex, final int axisIndex,
		final CalibratedAxis axis)
	{
		setAxis(imageIndex, axisIndex, axis, true);
	}

	@Override
	public void setAxisLengths(final int imageIndex, final long[] axisLengths) {
		setAxisLengths(imageIndex, axisLengths, true);
	}

	@Override
	public void setAxisLength(final int imageIndex, final CalibratedAxis axis,
		final long length)
	{
		setAxisLength(imageIndex, axis, length, true);
	}

	// -- Metadata API Methods --

	@Override
	public void setSource(final RandomAccessInputStream source) {
		super.setSource(source);
		meta.setSource(source);
	}

	@Override
	public void populateImageMetadata() {
		meta.populateImageMetadata();
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		meta.close(fileOnly);
	}

}
