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

package io.scif.img.cell;

import io.scif.FormatException;
import io.scif.Reader;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;
import io.scif.refs.RefManagerService;

import java.io.IOException;

import net.imglib2.display.ColorTable;
import net.imglib2.img.cell.AbstractCell;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.img.cell.Cells;
import net.imglib2.type.NativeType;

import org.scijava.Disposable;

/**
 * {@link AbstractCellImg} implementation for working with {@link SCIFIOCell}s.
 * 
 * @author Mark Hiner
 */
public class SCIFIOCellImg<T extends NativeType<T>, A, C extends AbstractCell<A>>
	extends AbstractCellImg<T, A, C, SCIFIOCellImgFactory<T>> implements
	Disposable
{

	// -- Fields --

	private final Reader reader;

	private SCIFIOArrayLoader<?> loader;

	// -- Constructor --

	public SCIFIOCellImg(final SCIFIOCellImgFactory<T> factory,
		final Cells<A, C> cells)
	{
		super(factory, cells);
		reader = factory.reader();
		reader.getContext().getService(RefManagerService.class).manage(this);
	}

	// -- SCIFIOCellImg methods --

	/**
	 * Returns the ColorTable of the specified image and plane index.
	 * <p>
	 * NB: opens the underlying image.
	 * </p>
	 */
	public ColorTable getColorTable(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		if (loader != null) return loader.loadTable(imageIndex, planeIndex);

		final long[] planeMin =
			new long[reader.getMetadata().get(imageIndex).getAxesPlanar().size()];
		final long[] planeMax = new long[planeMin.length];
		for (int i = 0; i < planeMax.length; i++)
			planeMax[i] = 1;

		return reader.openPlane(imageIndex, planeIndex, planeMin, planeMax)
			.getColorTable();
	}

	@Override
	@SuppressWarnings("unchecked")
	public A update(final Object cursor) {
		return ((CellContainerSampler<T, A, C>) cursor).getCell().getData();
	}

	@Override
	public SCIFIOCellImgFactory<T> factory() {
		return factory;
	}

	public Reader reader() {
		return reader;
	}

	public void setLoader(final SCIFIOArrayLoader<?> loader) {
		this.loader = loader;
	}

	@Override
	public SCIFIOCellImg<T, A, C> copy() {
		@SuppressWarnings("unchecked")
		final SCIFIOCellImg<T, A, C> copy =
			(SCIFIOCellImg<T, A, C>) factory().create(dimension,
				firstElement().createVariable());
		super.copyDataTo(copy);
		return copy;
	}

	@Override
	public void dispose() {
		try {
			reader.close();
		}
		catch (final IOException e) {}
	}
}
