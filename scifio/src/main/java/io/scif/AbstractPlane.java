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

import net.imglib2.display.ColorTable;

import org.scijava.Context;
import org.scijava.plugin.SortablePlugin;

/**
 * Abstract superclass for {@link io.scif.Plane} implementations in SCIFIO.
 * 
 * @see io.scif.Plane
 * @see io.scif.DataPlane
 * @author Mark Hiner
 * @param <T> - The underlying data type used by this Plane
 * @param <P> - A recursive reference to this concrete class
 */
public abstract class AbstractPlane<T, P extends DataPlane<T>> extends
	SortablePlugin implements DataPlane<T>
{

	// -- Fields --

	/** Native pixel data for this plane. */
	private T data = null;

	/** Color look-up table for this plane. */
	private ColorTable lut = null;

	/** Metadata describing the underlying image. */
	private ImageMetadata meta = null;

	/** X-axis offset into the underlying image. */
	private int xOffset = 0;

	/** Y-axis offset into the underlying image. */
	private int yOffset = 0;

	/** Length of the plane in the X-axis. */
	private int xLength = 0;

	/** Length of the plane in the Y-axis. */
	private int yLength = 0;

	// -- Constructor --

	public AbstractPlane(final Context context) {
		setContext(context);
	}

	public AbstractPlane(final Context context, final ImageMetadata meta,
		final int xOffset, final int yOffset, final int xLength, final int yLength)
	{
		setContext(context);
		// TODO bounds checking?
		populate(meta, xOffset, yOffset, xLength, yLength);
	}

	// -- DataPlane API methods --

	@Override
	public void setData(final T data) {
		this.data = data;
	}

	@Override
	public T getData() {
		return data;
	}

	// -- Plane API methods --

	@Override
	public void setColorTable(final ColorTable lut) {
		this.lut = lut;
	}

	@Override
	public ColorTable getColorTable() {
		return lut;
	}

	@Override
	public ImageMetadata getImageMetadata() {
		return meta;
	}

	@Override
	public int getxOffset() {
		return xOffset;
	}

	@Override
	public int getyOffset() {
		return yOffset;
	}

	@Override
	public int getxLength() {
		return xLength;
	}

	@Override
	public int getyLength() {
		return yLength;
	}

	@Override
	public P populate(final Plane p) {
		return populate(p.getImageMetadata(), p.getxOffset(), p.getyOffset(), p
			.getxLength(), p.getyLength());
	}

	@Override
	public P populate(final DataPlane<T> plane) {
		return populate(plane.getImageMetadata(), plane.getData(), plane
			.getxOffset(), plane.getyOffset(), plane.getxLength(), plane.getyLength());
	}

	@Override
	public P populate(final ImageMetadata meta, final int xOffset,
		final int yOffset, final int xLength, final int yLength)
	{
		return populate(meta, null, xOffset, yOffset, xLength, yLength);
	}

	@Override
	public P populate(final T data, final int xOffset, final int yOffset,
		final int xLength, final int yLength)
	{
		return populate(null, data, xOffset, yOffset, xLength, yLength);
	}

	@Override
	public P populate(final ImageMetadata meta, final T data, final int xOffset,
		final int yOffset, final int xLength, final int yLength)
	{
		setImageMetadata(meta);
		setData(data);
		setxOffset(xOffset);
		setyOffset(yOffset);
		setxLength(xLength);
		setyLength(yLength);

		@SuppressWarnings("unchecked")
		final P pl = (P) this;
		return pl;
	}

	@Override
	public void setImageMetadata(final ImageMetadata meta) {
		this.meta = meta;
	}

	@Override
	public void setxOffset(final int xOffset) {
		this.xOffset = xOffset;
	}

	@Override
	public void setyOffset(final int yOffset) {
		this.yOffset = yOffset;
	}

	@Override
	public void setxLength(final int xLength) {
		this.xLength = xLength;
	}

	@Override
	public void setyLength(final int yLength) {
		this.yLength = yLength;
	}
}
