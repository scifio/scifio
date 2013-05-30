package ome.scifio.io.img.cell;

import java.io.IOException;

import net.imglib2.display.ColorTable;
import net.imglib2.img.cell.AbstractCell;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.img.cell.Cells;
import net.imglib2.type.NativeType;
import ome.scifio.FormatException;
import ome.scifio.Reader;

public class SCIFIOCellImg< T extends NativeType< T >, A, C extends AbstractCell<A> > extends AbstractCellImg< T, A, C, SCIFIOCellImgFactory< T > >
{

  private Reader reader;

  public SCIFIOCellImg(final SCIFIOCellImgFactory<T> factory,
      final Cells<A, C> cells) {
    super(factory, cells);
    reader = factory.reader();
  }

  // -- SCIFIOCellImg methods --

  public ColorTable getColorTable(int imageIndex, int planeIndex)
    throws FormatException, IOException {
    return reader.openPlane(imageIndex, planeIndex, 0, 0, 1, 1).getColorTable();
  }

  @SuppressWarnings("unchecked")
  public A update(final Object cursor) {
    return ((CellContainerSampler<T, A, C>) cursor).getCell().getData();
  }

  @Override
  public SCIFIOCellImgFactory<T> factory() {
    return (SCIFIOCellImgFactory<T>) factory;
  }

  /*
   * @see net.imglib2.img.Img#copy()
   */
  public SCIFIOCellImg< T, A, C > copy() {
    final SCIFIOCellImg<T, A, C> copy = (SCIFIOCellImg<T, A, C>) factory()
        .create(dimension, firstElement().createVariable());
    super.copyDataTo(copy);
    return copy;
  }
}
