package loci.formats.gui;

import io.scif.BufferedImagePlane;

import java.awt.image.BufferedImage;
import java.io.IOException;


import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.SCIFIOFormatReader;
import loci.legacy.adapter.CommonAdapter;

/**
 * Abstract superclass for file format readers that use
 * java.awt.image.BufferedImage as the native data type.
 * Defers to io.scif.in.BIFormatReader
 *
 * @deprecated see io.scif.gui.BufferedImageReader
 */
@Deprecated
public abstract class SCIFIOBIFormatReader extends SCIFIOFormatReader {

  // -- Constructors --

  /** Constructs a new BIFormatReader. */
  public SCIFIOBIFormatReader(String name, String suffix) {
    super(name, suffix);
  }

  /** Constructs a new BIFormatReader. */
  public SCIFIOBIFormatReader(String name, String[] suffixes) {
    super(name, suffixes);
  }

  // -- IFormatReader API methods --
  
  /*
   * @see loci.formats.SCIFIOFormatReader#openPlane(int, int, int, int, int)
   */
  public Object openPlane(int no, int x, int y, int w, int h) throws FormatException, IOException {
    try {
      return ((BufferedImagePlane)(plane = reader.openPlane(getSeries(), no, x, y, w, h))).getData();
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  } 
  
  /** Reads a raw plane from disk. */
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, byte[] buf) throws IOException
  {
    if (plane == null || !(BufferedImagePlane.class.isAssignableFrom(plane.getClass()))) {
      plane = new BufferedImagePlane(reader.getContext());
      plane.populate(reader.getMetadata().get(getSeries()), x, y, w, h);
    }
    
    return reader.readPlane(
        CommonAdapter.get(s),
        getSeries(), x, y, w, h, plane).getBytes();
  }

  /** Reads a raw plane from disk. */
  @Deprecated
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, int scanlinePad, byte[] buf) throws IOException
  {
    if (plane == null || !(BufferedImagePlane.class.isAssignableFrom(plane.getClass()))) {
      plane = new BufferedImagePlane(reader.getContext());
      plane.populate(reader.getMetadata().get(getSeries()), x, y, w, h);
    }
    
    return reader.readPlane(
        CommonAdapter.get(s),
        getSeries(), x, y, w, h, scanlinePad, plane).getBytes();
  }
  
  /* @see IFormatReader#openBytes(int, byte[]) */
  @Override
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    try {
      return (plane = reader.openPlane(getSeries(), no)).getBytes();
    } catch (io.scif.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }
  
  /*
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  @Override
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      plane = reader.openPlane(getSeries(), no, x, y, w, h);
      byte[] retBytes = plane.getBytes();
      
      System.arraycopy(retBytes, 0, buf, 0, Math.min(retBytes.length, buf.length));
      
      return buf;
    } catch (io.scif.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#getNativeDataType() */
  public Class<?> getNativeDataType() {
    return BufferedImage.class;
  }
}
