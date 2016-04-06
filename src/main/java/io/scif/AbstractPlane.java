/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
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

import net.imglib2.display.ColorTable;

import org.scijava.AbstractContextual;
import org.scijava.Context;

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
	AbstractContextual implements DataPlane<T>
{

	// -- Fields --

	/** Native pixel data for this plane. */
	private T data = null;

	/** Color look-up table for this plane. */
	private ColorTable lut = null;

	/** Metadata describing the underlying image. */
	private ImageMetadata meta = null;

	/** offsets into the underlying image. */
	private long[] offsets;

	/** axis lengths */
	private long[] lengths;

	// -- Constructor --

	public AbstractPlane(final Context context) {
		setContext(context);
	}

	public AbstractPlane(final Context context, final ImageMetadata meta,
		final long[] planeOffsets, final long[] planeBounds)
	{
		setContext(context);
		// TODO bounds checking?
		populate(meta, planeOffsets, planeBounds);
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
	public long[] getOffsets() {
		return offsets;
	}

	@Override
	public long[] getLengths() {
		return lengths;
	}

	@Override
	public P populate(final Plane p) {
		return populate(p.getImageMetadata(), p.getOffsets(), p.getLengths());
	}

	@Override
	public P populate(final DataPlane<T> plane) {
		return populate(plane.getImageMetadata(), plane.getData(), plane
			.getOffsets(), plane.getLengths());
	}

	@Override
	public P populate(final ImageMetadata meta, final long[] planeOffsets,
		final long[] planeBounds)
	{
		return populate(meta, null, planeOffsets, planeBounds);
	}

	@Override
	public P populate(final T data, final long[] planeOffsets,
		final long[] planeBounds)
	{
		return populate(null, data, planeOffsets, planeBounds);
	}

	@Override
	public P populate(final ImageMetadata meta, T data,
		final long[] planeOffsets, final long[] planeBounds)
	{
		setImageMetadata(meta);
		if (data == null) data = blankPlane(planeOffsets, planeBounds);
		setData(data);
		setOffsets(planeOffsets);
		setLengths(planeBounds);

		@SuppressWarnings("unchecked")
		final P pl = (P) this;
		return pl;
	}

	@Override
	public void setImageMetadata(final ImageMetadata meta) {
		this.meta = meta;
	}

	@Override
	public void setOffsets(final long[] planeOffsets) {
		offsets = planeOffsets;
	}

	@Override
	public void setLengths(final long[] planeBounds) {
		lengths = planeBounds;
	}

	// -- Internal AbstractPlane API --

	/**
	 * @return an empty data conforming to the given planeOffsets and planeBounds
	 */
	protected abstract T blankPlane(final long[] planeOffsets,
		final long[] planeBounds);
}
