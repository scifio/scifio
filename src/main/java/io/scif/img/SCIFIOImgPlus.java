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

package io.scif.img;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.filters.MetadataWrapper;
import io.scif.img.cell.SCIFIOCellImg;

import java.io.IOException;

import net.imagej.ImgPlus;
import net.imagej.ImgPlusMetadata;
import net.imagej.axis.AxisType;
import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;

import org.scijava.Disposable;

/**
 * SCIFIO extension of {@link ImgPlus} that adds
 * {@link #getColorTable(int, int)} API for specifying both the plane and image
 * index.
 * <p>
 * Also provides direct access to the {@link Metadata} object associated with
 * the underlying dataset.
 * </p>
 *
 * @author Mark Hiner
 */
public class SCIFIOImgPlus<T> extends ImgPlus<T> implements Disposable {

	// -- Constants --

	public static final String GLOBAL_META = "scifio.metadata.global";
	public static final String IMAGE_META = "scifio.metadata.image";

	// -- Constructors --

	public SCIFIOImgPlus(final Img<T> img) {
		super(img);
	}

	public SCIFIOImgPlus(final Img<T> img, final String name) {
		super(img, name);
	}

	public SCIFIOImgPlus(final Img<T> img, final String name,
		final AxisType[] axes)
	{
		super(img, name, axes);
	}

	public SCIFIOImgPlus(final Img<T> img, final ImgPlusMetadata metadata) {
		super(img, metadata);
	}

	public SCIFIOImgPlus(final Img<T> img, final String name,
		final AxisType[] axes, final double[] cal)
	{
		super(img, name, axes, cal);
	}

	public SCIFIOImgPlus(final ImgPlus<T> imgPlus) {
		this(imgPlus.getImg(), imgPlus);
	}

	// -- SCIFIOImgPlus Methods --

	/**
	 * @return The SCIFIO {@link Metadata} object attached to this ImgPlus.
	 */
	public Metadata getMetadata() {
		return (Metadata) getProperties().get(GLOBAL_META);
	}

	/**
	 * Sets the {@link Metadata} object for this ImgPlus.
	 */
	public void setMetadata(final Metadata meta) {
		getProperties().put(GLOBAL_META, meta);
	}

	/**
	 * @return The SCIFIO {@link ImageMetadata} object attached to this ImgPlus.
	 */
	public ImageMetadata getImageMetadata() {
		return (ImageMetadata) getProperties().get(IMAGE_META);
	}

	/**
	 * Sets the {@link ImageMetadata} object for this ImgPlus.
	 */
	public void setImageMetadata(final ImageMetadata imageMeta) {
		getProperties().put(IMAGE_META, imageMeta);
	}

	// -- ImgPlus Methods --

	@Override
	public ColorTable getColorTable(final int planeIndex) {
		return getColorTable(0, planeIndex);
	}

	/**
	 * @param imageIndex - Image index to look up the color table
	 * @param planeIndex - Plane index of the desired color table
	 * @return The ColorTable of the underlying dataset at the specified indices.
	 */
	public ColorTable getColorTable(final int imageIndex, final int planeIndex) {
		ColorTable table = super.getColorTable(planeIndex);

		if (table == null && SCIFIOCellImg.class.isAssignableFrom(getImg()
			.getClass()))
		{
			try {
				table = ((SCIFIOCellImg<?, ?>) getImg()).getColorTable(imageIndex,
					planeIndex);
			}
			catch (final FormatException e) {
				return null;
			}
			catch (final IOException e) {
				return null;
			}

			setColorTable(table, planeIndex);
		}

		return table;
	}

	@Override
	public SCIFIOImgPlus<T> copy() {
		final SCIFIOImgPlus<T> copy = new SCIFIOImgPlus<>(getImg().copy(), this);
		copy.setMetadata(getMetadata());
		return copy;
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		final Object img = getImg();
		if (img instanceof Disposable) {
			((Disposable) img).dispose();
		}
	}

	// -- Internal methods --

	/**
	 * Sets the ROIs and tables properties based on the {@link ImageMetadata}. If
	 * the given {@link Metadata} is a {@link MetadataWrapper}, this method will
	 * recurse through the layers searching for the first filled ROIs and tables
	 * fields.
	 *
	 * @param meta the {@link Metadata} whose associated {@link ImageMetadata}
	 *          will be checked
	 * @param index the index of the {@link ImageMetadata}
	 */
	void setROIsAndTablesProperties(final Metadata meta, final int index) {
		final boolean roisSet = getProperties().get("rois") != null;
		final boolean tablesSet = getProperties().get("tables") != null;
		if (roisSet && tablesSet) return;

		if (!roisSet) getProperties().put("rois", meta.get(index).getROIs());
		if (!tablesSet) getProperties().put("tables", meta.get(index).getTables());

		if (meta instanceof MetadataWrapper) setROIsAndTablesProperties(
			((MetadataWrapper) meta).unwrap(), index);
	}
}
