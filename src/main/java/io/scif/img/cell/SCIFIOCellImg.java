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

package io.scif.img.cell;

import io.scif.FormatException;
import io.scif.Reader;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;

import java.io.IOException;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.display.ColorTable;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.util.Fraction;

import org.scijava.Disposable;

/**
 * {@link CachedCellImg} implementation backed by a SCIFIO {@link Reader}.
 *
 * @author Mark Hiner
 * @author Tobias Pietzsch
 */
public class SCIFIOCellImg<T extends NativeType<T>, A> extends
	CachedCellImg<T, A> implements Disposable
{
	// -- Fields --

	private final Reader reader;

	private SCIFIOArrayLoader<?> loader;

	private final SCIFIOCellImgFactory<T> factory;

	// -- Constructor --

	public SCIFIOCellImg(final SCIFIOCellImgFactory<T> factory,
		final CellGrid grid, final Fraction entitiesPerPixel,
		final Cache<Long, Cell<A>> cache, final A accessType)
	{
		super(grid, entitiesPerPixel, cache, accessType);
		this.factory = factory;
		reader = factory.reader();
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

		final int planarAxisCount = //
			reader.getMetadata().get(imageIndex).getAxesPlanar().size();
		final long[] dims = new long[planarAxisCount];
		for (int i = 0; i < dims.length; i++)
			dims[i] = 1;
		final Interval bounds = new FinalInterval(dims);

		return reader.openPlane(imageIndex, planeIndex, bounds).getColorTable();
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
	public SCIFIOCellImg<T, A> copy() {
		@SuppressWarnings("unchecked")
		final SCIFIOCellImg<T, A> copy = (SCIFIOCellImg<T, A>) factory().create(
			dimension);
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
