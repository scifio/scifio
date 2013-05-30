package ome.scifio.io.img.cell;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.AbstractCellImgFactory;
import net.imglib2.type.NativeType;
import ome.scifio.Reader;
import ome.scifio.filters.ReaderFilter;
import ome.scifio.io.img.cell.loaders.BitArrayLoader;
import ome.scifio.io.img.cell.loaders.ByteArrayLoader;
import ome.scifio.io.img.cell.loaders.CharArrayLoader;
import ome.scifio.io.img.cell.loaders.DoubleArrayLoader;
import ome.scifio.io.img.cell.loaders.FloatArrayLoader;
import ome.scifio.io.img.cell.loaders.IntArrayLoader;
import ome.scifio.io.img.cell.loaders.LongArrayLoader;
import ome.scifio.io.img.cell.loaders.SCIFIOArrayLoader;
import ome.scifio.io.img.cell.loaders.ShortArrayLoader;

public final class SCIFIOCellImgFactory<T extends NativeType<T>> extends
    AbstractCellImgFactory<T> {

  // -- Fields --

  protected Reader reader;

  // -- Constuctors --

  public SCIFIOCellImgFactory() {
  }

  public SCIFIOCellImgFactory(final int cellSize) {
    super(cellSize);
  }

  public SCIFIOCellImgFactory(final int[] cellDimensions) {
    super(cellDimensions);
  }

  // -- CellImgFactory API Methods --

  @Override
  public SCIFIOCellImg<T, BitArray, SCIFIOCell<BitArray>> createBitInstance(
    long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new BitArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, ByteArray, SCIFIOCell<ByteArray>> createByteInstance(
    long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new ByteArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, CharArray, SCIFIOCell<CharArray>> createCharInstance(
    long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new CharArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, ShortArray, SCIFIOCell<ShortArray>>
    createShortInstance(long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new ShortArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, IntArray, SCIFIOCell<IntArray>> createIntInstance(
    long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new IntArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, LongArray, SCIFIOCell<LongArray>> createLongInstance(
    long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new LongArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, FloatArray, SCIFIOCell<FloatArray>>
    createFloatInstance(long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new FloatArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @Override
  public SCIFIOCellImg<T, DoubleArray, SCIFIOCell<DoubleArray>>
    createDoubleInstance(long[] dimensions, final int entitiesPerPixel) {
    return createInstance(new DoubleArrayLoader(reader()), dimensions,
        entitiesPerPixel);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SCIFIOCellImg<T, ?, ?> create(final long[] dim, final T type) {
    if (reader == null)
      throw new IllegalStateException(
          "Tried to create a new SCIFIOCellImg without a Reader to "
              + "use for opening planes.\nCall setReader(Reader) before invoking create()");

    return (SCIFIOCellImg<T, ?, ?>) type.createSuitableNativeImg(this, dim);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <S> ImgFactory<S> imgFactory(final S type)
    throws IncompatibleTypeException {
    if (NativeType.class.isInstance(type))
      return new SCIFIOCellImgFactory(defaultCellDimensions);
    throw new IncompatibleTypeException(this, type.getClass()
        .getCanonicalName()
        + " does not implement NativeType.");
  }

  public Reader reader() {
    return reader;
  }

  public void setReader(Reader r) {
    reader = r;
    // TODO make N-d
    
    if (r instanceof ReaderFilter) r = ((ReaderFilter)r).getTail();
    
    defaultCellDimensions = new int[] { r.getOptimalTileWidth(0),
        r.getOptimalTileHeight(0), 1, 1, 1 };
  }

  @Override
  public void finalize() throws Throwable {
    try {
      reader.close(); // close open files
    } finally {
      super.finalize();
    }
  }

  private <A, L extends SCIFIOArrayLoader<A>>
    SCIFIOCellImg<T, A, SCIFIOCell<A>> createInstance(L loader,
      long[] dimensions, final int entitiesPerPixel) {
    dimensions = checkDimensions(dimensions);
    final int[] cellSize = checkCellSize(defaultCellDimensions, dimensions);
    final SCIFIOCellCache<A> c = new SCIFIOCellCache<A>(loader);

    return new SCIFIOCellImg<T, A, SCIFIOCell<A>>(this, new SCIFIOImgCells<A>(
        c, entitiesPerPixel, dimensions, cellSize));

  }
}
