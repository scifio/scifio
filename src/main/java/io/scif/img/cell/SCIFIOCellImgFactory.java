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

import static net.imglib2.img.cell.CellImgFactory.getCellDimensions;
import static net.imglib2.img.cell.CellImgFactory.verifyDimensions;

import io.scif.Reader;
import io.scif.filters.ReaderFilter;
import io.scif.img.ImageRegion;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.CharArrayLoader;
import io.scif.img.cell.loaders.DoubleArrayLoader;
import io.scif.img.cell.loaders.FloatArrayLoader;
import io.scif.img.cell.loaders.IntArrayLoader;
import io.scif.img.cell.loaders.LongArrayLoader;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;
import io.scif.img.cell.loaders.ShortArrayLoader;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.img.cell.LazyCellImg.LazyCells;
import net.imglib2.type.NativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.util.Fraction;

/**
 * {@link ImgFactory} implementation for working with {@link SCIFIOCell}s.
 *
 * @author Mark Hiner
 * @author Tobias Pietzsch
 */
public final class SCIFIOCellImgFactory<T extends NativeType<T>> extends NativeImgFactory<T>
{

	// -- Fields --

	private int index;

	private Reader reader;

	private ImageRegion subregion;

	private int[] defaultCellDimensions;

	// -- Constructors --

	public SCIFIOCellImgFactory(final T type) {
		this(type, 10);
	}

	public SCIFIOCellImgFactory(final T type, final int... cellDimensions) {
		super(type);
		defaultCellDimensions = cellDimensions.clone();
		verifyDimensions( defaultCellDimensions );
	}

	// -- ImgFactory API Methods --

	@Override
	public SCIFIOCellImg<T, ?> create(final long... dimensions) {
		if (reader == null) {
			throw new IllegalStateException(
					"Tried to create a new SCIFIOCellImg without a Reader to "
							+ "use for opening planes.\nCall setReader(Reader) before invoking create()");
		}
		@SuppressWarnings( { "unchecked", "rawtypes" } )
		SCIFIOCellImg<T, ?> img = createInstance(dimensions, type(), (NativeTypeFactory) type().getNativeTypeFactory());
		return img;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <S> ImgFactory<S> imgFactory(final S type)
		throws IncompatibleTypeException
	{
		if (NativeType.class.isInstance(type)) return new SCIFIOCellImgFactory(
				(NativeType) type, defaultCellDimensions);
		throw new IncompatibleTypeException(this, type.getClass()
			.getCanonicalName() +
			" does not implement NativeType.");
	}

	/**
	 * @return The {@link Reader} attached to this factory. Will be used for any
	 *         {@link SCIFIOCellImg} instances created for dynamic loading.
	 */
	public Reader reader() {
		return reader;
	}

	/**
	 * @param r Reader to use for any created {@link SCIFIOCellImg}s.
	 * @param imageIndex Image index within the given reader that will be loaded
	 *          by {@link SCIFIOCellImg}s.
	 */
	public void setReader(Reader r, final int imageIndex) {
		reader = r;
		index = imageIndex;

		if (r instanceof ReaderFilter) r = ((ReaderFilter) r).getTail();

		defaultCellDimensions =
			new int[] { (int) reader.getOptimalTileWidth(imageIndex),
				(int) reader.getOptimalTileHeight(imageIndex), 1, 1, 1 };
	}

	/**
	 * @param region The {@link ImageRegion} that will be operated on by any
	 *          created {@link SCIFIOCellImg}s.
	 */
	public void setSubRegion(final ImageRegion region) {
		subregion = region;
	}

	// -- Helper Methods --

	@SuppressWarnings( "unchecked" )
	private <A extends ArrayDataAccess<A>, L extends SCIFIOArrayLoader<A>> L createArrayLoader(
			final NativeTypeFactory<?, ? super A> typeFactory)
	{
		switch ( typeFactory.getPrimitiveType() )
		{
		case BYTE:
			return (L) new ByteArrayLoader(reader(), subregion);
		case CHAR:
			return (L) new CharArrayLoader(reader(), subregion);
		case DOUBLE:
			return (L) new DoubleArrayLoader(reader(), subregion);
		case FLOAT:
			return (L) new FloatArrayLoader(reader(), subregion);
		case INT:
			return (L) new IntArrayLoader(reader(), subregion);
		case LONG:
			return (L) new LongArrayLoader(reader(), subregion);
		case SHORT:
			return (L) new ShortArrayLoader(reader(), subregion);
		default:
			throw new IllegalArgumentException();
		}
	}

	private <A extends ArrayDataAccess<A>> SCIFIOCellImg<T, A> createInstance(
			final long[] dimensions,
			final T type,
			final NativeTypeFactory< T, A > typeFactory)
	{
		verifyDimensions( dimensions );

		final int n = dimensions.length;
		final Fraction entitiesPerPixel = type.getEntitiesPerPixel();
		final int[] cellDimensions = getCellDimensions( defaultCellDimensions, n, entitiesPerPixel );

		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		SCIFIOArrayLoader<A> loader = createArrayLoader(typeFactory);
		loader.setIndex( index );
		final SCIFIOCellCache<A> cache = new SCIFIOCellCache<>(reader.getContext(), loader);

		final LazyCellImg.Get<SCIFIOCell<A>> getter =
			new LazyCellImg.Get<SCIFIOCell<A>>()
		{

				@Override
				public SCIFIOCell<A> get(final long index) {
					// Attempt to get the cell from memory
					final SCIFIOCell<A> cell = cache.get((int) index);
					if (cell != null) return cell;
					// Load the cell
					final long[] cellMin = new long[grid.numDimensions()];
					final int[] cellDims = new int[grid.numDimensions()];
					grid.getCellDimensions(index, cellMin, cellDims);
					return cache.load((int) index, cellDims, cellMin);
				}
			};

		final SCIFIOCellImg<T, A> cellImg = new SCIFIOCellImg<>(this, grid, new LazyCells<>(grid.getGridDimensions(), getter), entitiesPerPixel);
		cellImg.setLinkedType( typeFactory.createLinkedType(cellImg) );
		cellImg.setLoader(loader);

		return cellImg;
	}


	/*
	 * -----------------------------------------------------------------------
	 *
	 * Deprecated API.
	 *
	 * Supports backwards compatibility with ImgFactories that are constructed
	 * without a type instance or supplier.
	 *
	 * -----------------------------------------------------------------------
	 */

	@Deprecated
	public SCIFIOCellImgFactory() {
		this( 10 );
	}

	@Deprecated
	public SCIFIOCellImgFactory(final int... cellDimensions) {
		defaultCellDimensions = cellDimensions.clone();
		verifyDimensions( defaultCellDimensions );
	}

	@Deprecated
	@Override
	public SCIFIOCellImg<T, ?> create(final long[] dim, final T type) {
		if (reader == null) {
			throw new IllegalStateException(
					"Tried to create a new SCIFIOCellImg without a Reader to "
							+ "use for opening planes.\nCall setReader(Reader) before invoking create()");
		}
		cache( type );
		@SuppressWarnings( { "unchecked", "rawtypes" } )
		SCIFIOCellImg<T, ?> img = createInstance( dim, type, (NativeTypeFactory) type.getNativeTypeFactory() );
		return img;
	}
}
