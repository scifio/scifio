package ome.scifio.io.img.cell;

import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCells;
import net.imglib2.img.list.AbstractListImg;
import net.imglib2.util.IntervalIndexer;

public class SCIFIOImgCells<A> extends
    AbstractCells<A, SCIFIOCell<A>, SCIFIOImgCells<A>.CachedCells> {
  public static interface CellCache<A> {
    /**
     * Get the cell at a specified index.
     * 
     * @return cell at index or null if the cell is not in the cache.
     */
    public SCIFIOCell<A> get(final int index);

    /**
     * Load a cell into memory and put it into the cache at the specified index.
     * 
     * @param index
     *          cell is stored at this index in the cache.
     * @param cellDims
     *          dimensions of the cell.
     * @param cellMin
     *          offset of the cell in image coordinates.
     * @return cell at index
     */
    public SCIFIOCell<A> load(final int index, final int[] cellDims,
      final long[] cellMin);

  }

  protected final CachedCells cells;

  protected final CellCache<A> cache;

  public SCIFIOImgCells(final CellCache<A> cache, final int entitiesPerPixel,
      final long[] dimensions, final int[] cellDimensions) {
    super(entitiesPerPixel, dimensions, cellDimensions);
    this.cache = cache;
    cells = new CachedCells(numCells);
  }

  @Override
  protected CachedCells cells() {
    return cells;
  }

  public class CachedCells extends AbstractListImg<SCIFIOCell<A>> {
    protected CachedCells(final long[] dim) {
      super(dim);
    }

    @Override
    protected SCIFIOCell<A> get(final int index) {
      // TODO is this index just a linear index on cells?
      final SCIFIOCell<A> cell = cache.get(index);
      if (cell != null)
        return cell;
      final long[] cellGridPosition = new long[n];
      final long[] cellMin = new long[n];
      final int[] cellDims = new int[n];
      // TODO here is the index to position logic to re-compute
      IntervalIndexer.indexToPosition(index, dim, cellGridPosition);
      getCellDimensions(cellGridPosition, cellMin, cellDims);
      return cache.load(index, cellDims, cellMin);
    }

    public Img<SCIFIOCell<A>> copy() {
      throw new UnsupportedOperationException("Not supported");
    }

    @Override
    protected void set(final int index, final SCIFIOCell<A> value) {
      throw new UnsupportedOperationException("Not supported");
    }
  }
}
