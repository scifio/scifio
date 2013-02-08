package loci.formats.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import ome.scifio.BufferedImagePlane;

import loci.common.RandomAccessInputStream;
import loci.common.adapter.RandomAccessInputStreamAdapter;
import loci.formats.FormatException;
import loci.formats.SCIFIOFormatReader;
import loci.legacy.adapter.AdapterTools;

/**
 * Abstract superclass for file format readers that use
 * java.awt.image.BufferedImage as the native data type.
 * Defers to ome.scifio.in.BIFormatReader
 *
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
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  } 
  
  /** Reads a raw plane from disk. */
  @Deprecated
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, byte[] buf) throws IOException
  {
    if (plane == null || !(BufferedImagePlane.class.isAssignableFrom(plane.getClass()))) {
      plane = new BufferedImagePlane(reader.getContext());
      plane.populate(reader.getDatasetMetadata().get(getSeries()), x, y, w, h);
    }
    
    return reader.readPlane(
        AdapterTools.getAdapter(RandomAccessInputStreamAdapter.class).getModern(s),
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
      plane.populate(reader.getDatasetMetadata().get(getSeries()), x, y, w, h);
    }
    
    return reader.readPlane(
        AdapterTools.getAdapter(RandomAccessInputStreamAdapter.class).getModern(s),
        getSeries(), x, y, w, h, scanlinePad, plane).getBytes();
  }
  
  /* @see IFormatReader#openBytes(int, byte[]) */
  @Deprecated
  @Override
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    try {
      return (plane = reader.openPlane(getSeries(), no)).getBytes();
    } catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }
  
  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  @Deprecated
  @Override
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return (plane = reader.openPlane(getSeries(), no, x, y, w, h)).getBytes();
    } catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#getNativeDataType() */
  public Class<?> getNativeDataType() {
    return BufferedImage.class;
  }
}
