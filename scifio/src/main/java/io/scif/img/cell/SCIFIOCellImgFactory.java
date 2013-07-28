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

package io.scif.img.cell;

import io.scif.Metadata;
import io.scif.Reader;
import io.scif.common.DataTools;
import io.scif.filters.ReaderFilter;
import io.scif.img.cell.cache.CacheService;
import io.scif.img.cell.loaders.BitArrayLoader;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.CharArrayLoader;
import io.scif.img.cell.loaders.DoubleArrayLoader;
import io.scif.img.cell.loaders.FloatArrayLoader;
import io.scif.img.cell.loaders.IntArrayLoader;
import io.scif.img.cell.loaders.LongArrayLoader;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;
import io.scif.img.cell.loaders.ShortArrayLoader;
import io.scif.util.FormatTools;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.AbstractCellImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.type.NativeType;

/**
 * {@link AbstractCellImgFactory} implementation for working with
 * {@link SCIFIOCell}s.
 * 
 * @author Mark Hiner
 */
public final class SCIFIOCellImgFactory<T extends NativeType<T>> extends
	AbstractCellImgFactory<T>
{

	// -- Fields --

	protected Reader reader;

	// -- Constuctors --

	public SCIFIOCellImgFactory() {}

	public SCIFIOCellImgFactory(final int cellSize) {
		super(cellSize);
	}

	public SCIFIOCellImgFactory(final int[] cellDimensions) {
		super(cellDimensions);
	}

	// -- CellImgFactory API Methods --

	@Override
	public SCIFIOCellImg<T, BitArray, SCIFIOCell<BitArray>> createBitInstance(
		final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new BitArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, ByteArray, SCIFIOCell<ByteArray>> createByteInstance(
		final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new ByteArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, CharArray, SCIFIOCell<CharArray>> createCharInstance(
		final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new CharArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, ShortArray, SCIFIOCell<ShortArray>>
		createShortInstance(final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new ShortArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, IntArray, SCIFIOCell<IntArray>> createIntInstance(
		final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new IntArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, LongArray, SCIFIOCell<LongArray>> createLongInstance(
		final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new LongArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, FloatArray, SCIFIOCell<FloatArray>>
		createFloatInstance(final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new FloatArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@Override
	public SCIFIOCellImg<T, DoubleArray, SCIFIOCell<DoubleArray>>
		createDoubleInstance(final long[] dimensions, final int entitiesPerPixel)
	{
		return createInstance(new DoubleArrayLoader(reader()), dimensions,
			entitiesPerPixel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SCIFIOCellImg<T, ?, ?> create(final long[] dim, final T type) {
		if (reader == null) throw new IllegalStateException(
			"Tried to create a new SCIFIOCellImg without a Reader to "
				+ "use for opening planes.\nCall setReader(Reader) before invoking create()");

		return (SCIFIOCellImg<T, ?, ?>) type.createSuitableNativeImg(this, dim);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <S> ImgFactory<S> imgFactory(final S type)
		throws IncompatibleTypeException
	{
		if (NativeType.class.isInstance(type)) return new SCIFIOCellImgFactory(
			defaultCellDimensions);
		throw new IncompatibleTypeException(this, type.getClass()
			.getCanonicalName() +
			" does not implement NativeType.");
	}

	public Reader reader() {
		return reader;
	}

	public void setReader(Reader r) {
		reader = r;
		// TODO make N-d

		if (r instanceof ReaderFilter) r = ((ReaderFilter) r).getTail();

		final int[] cellXY = getOptimalCellXY(reader);

		defaultCellDimensions = new int[] { cellXY[0], cellXY[1], 1, 1, 1 };
	}

	@Override
	public void finalize() throws Throwable {
		try {
			reader.close(); // close open files
		}
		finally {
			super.finalize();
		}
	}

	// -- Helper Methods --

	private <A extends ArrayDataAccess<?>, L extends SCIFIOArrayLoader<A>>
		SCIFIOCellImg<T, A, SCIFIOCell<A>> createInstance(final L loader,
			long[] dimensions, final int entitiesPerPixel)
	{
		dimensions = checkDimensions(dimensions);
		final int[] cellSize = checkCellSize(defaultCellDimensions, dimensions);

		@SuppressWarnings("unchecked")
		final CacheService<SCIFIOCell<?>> service =
			reader.getContext().getServiceIndex().getService(CacheService.class);

		final SCIFIOCellCache<A> c = new SCIFIOCellCache<A>(service, loader);

		return new SCIFIOCellImg<T, A, SCIFIOCell<A>>(this, new SCIFIOImgCells<A>(
			c, entitiesPerPixel, dimensions, cellSize));

	}

	// -- Static helper methods --

	/**
	 * See {@link #getOptimalCellXY(Reader, int, int)}. Defaults to
	 * {@link Reader#getOptimalTileHeight(int)} and
	 * {@link Reader#getOptimalTileWidth(int)}.
	 * 
	 * @throws IllegalArgumentException if no cell can be found that fits in 2MB
	 * @return an array containing the optimal celll width in [0] and height in
	 *         [1];
	 */
	public static int[] getOptimalCellXY(final Reader reader) {
		return getOptimalCellXY(reader, reader.getOptimalTileWidth(0), reader
			.getOptimalTileHeight(0));
	}

	/**
	 * Returns optimal default cell dimensions given a reader with an open dataset
	 * and a base tileWidth and tileHeight. Negative height/width are set to 1 and
	 * expanded. Height/width > the x, y sizes are capped to x, y. The algorithm
	 * used tries to return strips in multiples of the given tile dimensions, but
	 * will expand horizontally to fill the complete image, and will expand
	 * vertically to the size of the image if memory permits.
	 * <p>
	 * Will not return cell sizes > 2MB.
	 * </p>
	 * 
	 * @throws IllegalArgumentException if no cell can be found that fits in 2MB
	 * @return an array containing the optimal celll width in [0] and height in
	 *         [1];
	 */
	public static int[] getOptimalCellXY(final Reader reader, int tileWidth,
		int tileHeight)
	{
		// No cell will occupy more than 2MB
		final int maxBytes = 2 * 1024 * 1024;

		final Metadata meta = reader.getMetadata();

		final int sizeX = meta.getAxisLength(0, Axes.X);
		final int sizeY = meta.getAxisLength(0, Axes.Y);

		// Invalid sizes default to 1, 1, which are automatically expanded.
		if (tileWidth <= 0) tileWidth = 1;
		if (tileHeight <= 0) tileHeight = 1;

		// Similarly, if the tiles are too big, cap them
		if (tileHeight > sizeX) tileHeight = sizeX;
		if (tileWidth > sizeY) tileWidth = sizeY;

		int cellWidth = -1, cellHeight = -1;
		final int bpp = FormatTools.getBytesPerPixel(meta.getPixelType(0));

		// Compute the size, in bytes, of a single tile. We do not consider RGB
		// channel count because ChannelSeparator is assumed.
		final int tileSize = DataTools.safeMultiply32(tileWidth, tileHeight, bpp);

		// Tile is too large
		if (tileSize > maxBytes) throw new IllegalArgumentException(
			"Tiles too large: " + tileSize + ". Please use a tile size < 2MB");

		// Determine how many tiles we have to work with
		final int numTiles = maxBytes / tileSize;

		// How many tiles wide can we make our strips
		final int tilesWide = Math.min(sizeX / tileWidth, numTiles);

		// Compute cellWidth
		cellWidth = tilesWide * tileWidth;

		if (cellWidth < sizeX && numTiles > tilesWide) cellWidth = sizeX;

		// How many strips of tiles can we make
		final int tilesHigh =
			Math.min(maxBytes / (cellWidth * tileHeight * bpp), sizeY / tileHeight);

		// compute cellHeight
		cellHeight = tilesHigh * tileHeight;

		if (cellHeight < sizeY && maxBytes > cellWidth * sizeY * bpp) cellHeight =
			sizeY;

		return new int[] { cellWidth, cellHeight };
	}
}
