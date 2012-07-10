package ome.scifio.ics;

import java.io.IOException;

import ome.scifio.AbstractWriter;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;

/**
 *  SCIFIO file format writer for ICS version 1 and 2 files.
 *
 */
public class ICSWriter extends AbstractWriter<ICSMetadata> {

  // -- Fields --

  // -- Constructor --

  public ICSWriter() {
    this(null);
  }

  public ICSWriter(final SCIFIO ctx) {
    super("Image Cytometry Standard", "ics", ctx);
  }

  // -- Writer API Methods --

  public void saveBytes(final int imageIndex, final int planeIndex,
    final byte[] buf, final int x, final int y, final int w, final int h)
    throws FormatException, IOException
  {
    // TODO Auto-generated method stub

  }

  /* @see ome.scifio.Writer#setMetadata(M) */
  public void setMetadata(final ICSMetadata meta) {
    super.setMetadata(meta, new ICSCoreTranslator());
  }

}
