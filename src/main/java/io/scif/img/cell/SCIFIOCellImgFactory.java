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

import io.scif.Reader;
import io.scif.filters.ReaderFilter;
import io.scif.img.ImageRegion;
import io.scif.img.cell.loaders.AbstractArrayLoader;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.CharArrayLoader;
import io.scif.img.cell.loaders.DoubleArrayLoader;
import io.scif.img.cell.loaders.FloatArrayLoader;
import io.scif.img.cell.loaders.IntArrayLoader;
import io.scif.img.cell.loaders.LongArrayLoader;
import io.scif.img.cell.loaders.ShortArrayLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.LoaderRemoverCache;
import net.imglib2.cache.img.AccessIo;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DirtyDiskCellCache;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCellCache;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.cache.ref.SoftRefLoaderRemoverCache;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.util.Fraction;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

/**
 * Factory for creating {@link SCIFIOCellImg}s. See
 * {@link DiskCachedCellImgOptions} for available configuration options and
 * defaults.
 *
 * @author Tobias Pietzsch
 * @author Mark Hiner
 */
public class SCIFIOCellImgFactory<T extends NativeType<T>> extends
	NativeImgFactory<T>
{
	// -- Fields --

	private int index;

	private Reader reader;

	private ImageRegion subregion;

	private int[] defaultCellDimensions;

	private DiskCachedCellImgOptions factoryOptions;

	// -- Constructors --

	/**
	 * Create a new {@link SCIFIOCellImgFactory} with default configuration.
	 */
	public SCIFIOCellImgFactory(final T type) {
		this(type, DiskCachedCellImgOptions.options());
	}

	/**
	 * Create a new {@link SCIFIOCellImgFactory} with the specified configuration.
	 *
	 * @param optional configuration options.
	 */
	public SCIFIOCellImgFactory(final T type,
		final DiskCachedCellImgOptions optional)
	{
		super(type);
		this.factoryOptions = optional;
	}

	// -- ImgFactory API Methods --

	@Override
	public SCIFIOCellImg<T, ?> create(final long... dimensions) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final SCIFIOCellImg<T, ?> img = create(dimensions, type(),
			(NativeTypeFactory) type().getNativeTypeFactory());
		return img;
	}

	@Override
	public SCIFIOCellImg<T, ?> create(final Dimensions dimensions) {
		return create(Intervals.dimensionsAsLongArray(dimensions));
	}

	@Override
	public SCIFIOCellImg<T, ?> create(final int[] dimensions) {
		return create(Util.int2long(dimensions));
	}

	/**
	 * @return The {@link Reader} attached to this factory. Will be used for any
	 *         {@link io.scif.img.cell.SCIFIOCellImg} instances created for
	 *         dynamic loading.
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

		defaultCellDimensions = new int[] { (int) reader.getOptimalTileWidth(
			imageIndex), (int) reader.getOptimalTileHeight(imageIndex), 1, 1, 1 };
	}

	/**
	 * @param region The {@link ImageRegion} that will be operated on by any
	 *          created {@link io.scif.img.cell.SCIFIOCellImg}s.
	 */
	public void setSubRegion(final ImageRegion region) {
		subregion = region;
	}

	// -- Helper Methods --

	private static class SCIFIOCellLoader<T extends NativeType<T>, A> implements
		CellLoader<T>
	{

		private final AbstractArrayLoader<A> loader;

		private final Function<Object, A> wrap;

		SCIFIOCellLoader(final AbstractArrayLoader<A> loader,
			final Function<Object, A> wrap)
		{
			this.loader = loader;
			this.wrap = wrap;
		}

		@Override
		public void load(final SingleCellArrayImg<T, ?> cell) throws Exception {
			final A data = wrap.apply(cell.getStorageArray());
			loader.loadArray(cell, data);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <A extends ArrayDataAccess<A>> SCIFIOCellLoader<T, A>
		createCellLoader(final NativeTypeFactory<T, A> typeFactory)
	{
		switch (typeFactory.getPrimitiveType()) {
			case BYTE:
				return new SCIFIOCellLoader(new ByteArrayLoader(reader, subregion),
					o -> new ByteArray((byte[]) o));
			case CHAR:
				return new SCIFIOCellLoader(new CharArrayLoader(reader, subregion),
					o -> new CharArray((char[]) o));
			case DOUBLE:
				return new SCIFIOCellLoader(new DoubleArrayLoader(reader, subregion),
					o -> new DoubleArray((double[]) o));
			case FLOAT:
				return new SCIFIOCellLoader(new FloatArrayLoader(reader, subregion),
					o -> new FloatArray((float[]) o));
			case INT:
				return new SCIFIOCellLoader(new IntArrayLoader(reader, subregion),
					o -> new IntArray((int[]) o));
			case LONG:
				return new SCIFIOCellLoader(new LongArrayLoader(reader, subregion),
					o -> new LongArray((long[]) o));
			case SHORT:
				return new SCIFIOCellLoader(new ShortArrayLoader(reader, subregion),
					o -> new ShortArray((short[]) o));
			default:
				throw new IllegalArgumentException();
		}
	}

	private <A extends ArrayDataAccess<A>> SCIFIOCellImg<T, ? extends A> create(
		final long[] dimensions, final T type,
		final NativeTypeFactory<T, A> typeFactory)
	{
		final SCIFIOCellLoader<T, A> cellLoader = createCellLoader(typeFactory);
		cellLoader.loader.setIndex(index);

		final DiskCachedCellImgOptions.Values options = factoryOptions.values;

		final Fraction entitiesPerPixel = type.getEntitiesPerPixel();

		final CellGrid grid = createCellGrid(dimensions, entitiesPerPixel);

		final CellLoader<T> actualCellLoader = options.initializeCellsAsDirty()
			? cell -> {
				cellLoader.load(cell);
				cell.setDirty();
			} : cellLoader;
		final CacheLoader<Long, Cell<A>> backingLoader = LoadedCellCacheLoader.get(
			grid, actualCellLoader, type, options.accessFlags());

		final Path blockcache = createBlockCachePath(options);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		final DiskCellCache<A> diskcache = options.dirtyAccesses()
			? new DirtyDiskCellCache(blockcache, grid, backingLoader, AccessIo.get(
				type, options.accessFlags()), entitiesPerPixel) : new DiskCellCache<>(
					blockcache, grid, backingLoader, AccessIo.get(type, options
						.accessFlags()), entitiesPerPixel);

		final IoSync<Long, Cell<A>> iosync = new IoSync<>(diskcache, options
			.numIoThreads(), options.maxIoQueueSize());

		LoaderRemoverCache<Long, Cell<A>> listenableCache;
		switch (options.cacheType()) {
			case BOUNDED:
				listenableCache = new GuardedStrongRefLoaderRemoverCache<>(options
					.maxCacheSize());
				break;
			case SOFTREF:
			default:
				listenableCache = new SoftRefLoaderRemoverCache<>();
				break;
		}

		final Cache<Long, Cell<A>> cache = listenableCache.withRemover(iosync)
			.withLoader(iosync);

		final A accessType = ArrayDataAccessFactory.get(typeFactory, options
			.accessFlags());
		final SCIFIOCellImg<T, ? extends A> img = new SCIFIOCellImg<>(this, grid,
			entitiesPerPixel, cache, accessType);
		img.setLinkedType(typeFactory.createLinkedType(img));
		return img;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <S> ImgFactory<S> imgFactory(final S type)
		throws IncompatibleTypeException
	{
		if (NativeType.class.isInstance(type)) return new SCIFIOCellImgFactory(
			(NativeType) type, factoryOptions);
		throw new IncompatibleTypeException(this, type.getClass()
			.getCanonicalName() + " does not implement NativeType.");
	}

	private CellGrid createCellGrid(final long[] dimensions,
		final Fraction entitiesPerPixel)
	{
		CellImgFactory.verifyDimensions(dimensions);
		final int n = dimensions.length;

		final int[] defaultDims = new int[dimensions.length];
		for (int d = 0; d < defaultDims.length; d++) {
			defaultDims[d] = dimensions[d] < defaultCellDimensions[d] ? //
				(int) dimensions[d] : defaultCellDimensions[d];
		}

		final int[] cellDimensions = CellImgFactory.getCellDimensions(defaultDims,
			n, entitiesPerPixel);
		return new CellGrid(dimensions, cellDimensions);
	}

	private Path createBlockCachePath(
		final DiskCachedCellImgOptions.Values options)
	{
		try {
			final Path cache = options.cacheDirectory();
			final Path dir = options.tempDirectory();
			final String prefix = options.tempDirectoryPrefix();
			final boolean deleteOnExit = options.deleteCacheDirectoryOnExit();
			if (cache != null) {
				if (!Files.isDirectory(cache)) {
					Files.createDirectories(cache);
					if (deleteOnExit) DiskCellCache.addDeleteHook(cache);
				}
				return cache;
			}
			else if (dir != null) return DiskCellCache.createTempDirectory(dir,
				prefix, deleteOnExit);
			else return DiskCellCache.createTempDirectory(prefix, deleteOnExit);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
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
		this(10);
	}

	@Deprecated
	public SCIFIOCellImgFactory(final int... cellDimensions) {
		this.factoryOptions = DiskCachedCellImgOptions.options();
		defaultCellDimensions = cellDimensions.clone();
	}

	@Deprecated
	@Override
	public SCIFIOCellImg<T, ?> create(final long[] dim, final T type) {
		if (reader == null) {
			throw new IllegalStateException(
				"Tried to create a new SCIFIOCellImg without a Reader to " +
					"use for opening planes.\nCall setReader(Reader) before invoking create()");
		}
		cache(type);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final SCIFIOCellImg<T, ?> img = create(dim, type, (NativeTypeFactory) type
			.getNativeTypeFactory());
		return img;
	}
}
