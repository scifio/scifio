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

import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.AbstractCells;
import net.imglib2.img.list.AbstractListImg;
import net.imglib2.util.IntervalIndexer;

/**
 * {@link AbstractCells} implementation for
 * working with {@link SCIFIOCell}s.
 * 
 * @author Mark Hiner hinerm at gmail.com
 */
public class SCIFIOImgCells<A extends ArrayDataAccess<?>> extends
    AbstractCells<A, SCIFIOCell<A>, SCIFIOImgCells<A>.CachedCells> {
  public static interface CellCache<A extends ArrayDataAccess<?>> {
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
