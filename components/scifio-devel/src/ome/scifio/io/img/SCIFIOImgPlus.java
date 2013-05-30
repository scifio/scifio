package ome.scifio.io.img;

import java.io.IOException;

import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.Metadata;
import ome.scifio.FormatException;
import ome.scifio.io.img.cell.SCIFIOCellImg;

public class SCIFIOImgPlus<T> extends ImgPlus<T> {

  // -- Constructors --

  public SCIFIOImgPlus(final Img<T> img) {
    super(img);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name) {
    super(img, name);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name, final AxisType[] axes) {
    super(img, name, axes);
  }

  public SCIFIOImgPlus(final Img<T> img, final Metadata metadata) {
    super(img, metadata);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name, final AxisType[] axes,
    final double[] cal)
  {
    super(img, name, axes, cal);
  }
  
  @Override
  public ColorTable getColorTable(final int planeIndex) {
    return getColorTable(0, planeIndex);
  }
  
  public ColorTable getColorTable(int imageIndex, final int planeIndex) {
    ColorTable table = super.getColorTable(planeIndex);
    
    if (table == null && SCIFIOCellImg.class.isAssignableFrom(getImg().getClass()))
    {
      try {
        table = ((SCIFIOCellImg<?, ?, ?>)getImg()).getColorTable(imageIndex, planeIndex);
      } catch (FormatException e) {
        return null;
      } catch (IOException e) {
        return null;
      }
      
      setColorTable(table, planeIndex);
    }
    
    return table;
  }
}
