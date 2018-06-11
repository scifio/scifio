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

import net.imglib2.Interval;
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

	/** Bounds of the underlying image. */
	private Interval bounds;

	// -- Constructor --

	public AbstractPlane(final Context context) {
		setContext(context);
	}

	public AbstractPlane(final Context context, final ImageMetadata meta,
		final Interval bounds)
	{
		setContext(context);
		// TODO bounds checking?
		populate(meta, bounds);
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
	public P populate(final Plane p) {
		return populate(p.getImageMetadata(), p.getBounds());
	}

	@Override
	public P populate(final DataPlane<T> plane) {
		return populate(plane.getImageMetadata(), plane.getData(), //
			plane.getBounds());
	}

	@Override
	public P populate(final ImageMetadata meta, final Interval bounds) {
		return populate(meta, null, bounds);
	}

	@Override
	public P populate(final T data, final Interval bounds) {
		return populate(null, data, bounds);
	}

	@Override
	public P populate(final ImageMetadata meta, T data, final Interval bounds) {
		setImageMetadata(meta);
		if (data == null) data = blankPlane(bounds);
		setData(data);
		setBounds(bounds);

		@SuppressWarnings("unchecked")
		final P pl = (P) this;
		return pl;
	}

	@Override
	public void setImageMetadata(final ImageMetadata meta) {
		this.meta = meta;
	}

	@Override
	public Interval getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(final Interval bounds) {
		this.bounds = bounds;
	}

	// -- Internal AbstractPlane API --

	/**
	 * @return an empty data conforming to the given planar bounds.
	 */
	protected abstract T blankPlane(final Interval bounds);
}
