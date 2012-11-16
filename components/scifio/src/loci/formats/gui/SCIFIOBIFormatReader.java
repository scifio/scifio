package loci.formats.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import ome.scifio.Metadata;

import loci.formats.FormatException;
import loci.formats.SCIFIOFormatReader;

/**
 * Abstract superclass for file format readers that use
 * java.awt.image.BufferedImage as the native data type.
 * Defers to ome.scifio.in.BIFormatReader
 *
 */
@Deprecated
public abstract class SCIFIOBIFormatReader<T extends Metadata> extends SCIFIOFormatReader<T> {

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

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  @Deprecated
  @Override
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return reader.openPlane(getSeries(), no, buf, x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#getNativeDataType() */
  public Class<?> getNativeDataType() {
    return BufferedImage.class;
  }
}
