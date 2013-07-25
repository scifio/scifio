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
package io.scif.filters;

import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FormatException;
import io.scif.Plane;
import io.scif.common.DataTools;

import java.io.IOException;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.display.ColorTable;


/**
 * For indexed color data representing true color, factors out
 * the indices, replacing them with the color table values directly.
 *
 * For all other data (either non-indexed, or indexed with
 * "false color" tables), does nothing.
 * 
 * NB: lut length is not guaranteed to be accurate until a plane has been read
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ChannelFiller.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ChannelFiller.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type=ChannelFiller.class, priority=ChannelFiller.PRIORITY, attrs={
  @Attr(name=ChannelFiller.FILTER_KEY, value=ChannelFiller.FILTER_VALUE),
  @Attr(name=ChannelFiller.ENABLED_KEY, value=ChannelFiller.ENABLED_VAULE)
  })
public class ChannelFiller extends AbstractReaderFilter {

  // -- Constants --

  public static final double PRIORITY = 1.0;
  public static final String FILTER_VALUE = "io.scif.Reader";

  // -- Fields --

  /**
   * Whether to fill in the indices.
   * By default, indices are filled iff data not false color.
   */
  protected Boolean filled = null;

  /** Number of LUT components. */
  protected int lutLength = 0;

  /**
   * Cached parent plane
   */
  private Plane parentPlane = null;

  // -- Constructor --

  public ChannelFiller() {
    super(ChannelFillerMetadata.class);
  }

  // -- ChannelFiller API --

  /** Returns true if the indices are being factored out. */
  public boolean isFilled(int imageIndex) {
    if(metaCheck())
      return ((ChannelFillerMetadata)getMetadata()).isFilled(imageIndex);

    return false;
  }

  /** Toggles whether the indices should be factored out. */
  public void setFilled(boolean filled) {
    if(metaCheck())
      ((ChannelFillerMetadata)getMetadata()).setFilled(filled);
  }

  // -- Filter API Methods --

  /*
   * @see io.scif.filters.AbstractReaderFilter#isCompatible(java.lang.Class)
   */
  @Override
  public boolean isCompatible(Class<?> c) {
    return ByteArrayReader.class.isAssignableFrom(c);
  }

  // -- Reader API methods --

  /*
   * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int)
   */
  @Override
  public Plane openPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    return openPlaneHelper(getParent().openPlane(imageIndex, planeIndex), null, imageIndex);
  }

  /*
   * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, io.scif.Plane)
   */
  @Override
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane)
    throws FormatException, IOException
  {
    if (parentPlane == null) parentPlane = getParent().openPlane(imageIndex, planeIndex);
    else getParent().openPlane(imageIndex, planeIndex, parentPlane);
    return openPlaneHelper(parentPlane, plane, imageIndex);
  }

  /*
   * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, int, int, int, int)
   */
  @Override
  public Plane openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return openPlaneHelper(getParent().openPlane(imageIndex, planeIndex, x, y, w, h), null, imageIndex);
  }

  /*
   * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, io.scif.Plane, int, int, int, int)
   */
  @Override
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    if (parentPlane == null) parentPlane = getParent().openPlane(imageIndex, planeIndex, x, y, w, h);
    else getParent().openPlane(imageIndex, planeIndex, parentPlane, x, y, w, h);
    return openPlaneHelper(parentPlane, plane, imageIndex);
  }

  /*
   * @see io.scif.filters.AbstractReaderFilter#close()
   */
  public void close() throws IOException {
    close(false);
  }

  /*
   * @see io.scif.filters.AbstractReaderFilter#close(boolean)
   */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);

    if (!fileOnly)
      cleanUp();
  }

  // -- AbstractReaderFilter API Methods --

  /* lutLength is 0 until a plane is opened */
  protected void setSourceHelper(String source) {
    cleanUp();
  }

  /*
   * This method performs the actual channel filling on the plane returned
   * by the underlying reader component.
   */
  protected Plane openPlaneHelper(Plane parentPlane, Plane plane, int imageIndex) {
    if(!isFilled(imageIndex)) return parentPlane;

    // TODO: The pixel type should change to match the available color table.
    // That is, even if the indices are uint8, if the color table is 16-bit,
    // The pixel type should change to uint16. Similarly, if the indices are
    // uint16 but we are filling with an 8-bit color table, the pixel type
    // should change to uint8.

    // TODO: This logic below is opaque and could use some comments.

    ColorTable lut = parentPlane.getColorTable();
    byte[] index = parentPlane.getBytes();

    // update lutLength based on the read plane
    lutLength = parentPlane.getColorTable().getComponentCount();

    if(metaCheck()) ((ChannelFillerMetadata)getMetadata()).setLutLength(lutLength);

    if (plane == null || !isCompatible(plane.getClass())) {
      ByteArrayPlane bp = new ByteArrayPlane(parentPlane.getContext());
      bp.populate(parentPlane);
      bp.setData(new byte[lutLength * index.length]);

      plane = bp;
    }

    byte[] buf = plane.getBytes();
    int pt = 0;

    int bytesPerIndex = getMetadata().getBitsPerPixel(imageIndex) / 8;

    if (getMetadata().isInterleaved(imageIndex)) {
      for (int i=0; i<index.length / bytesPerIndex; i++) {
        for (int j=0; j<lutLength; j++) {
          int iVal = DataTools.bytesToInt(index, i * bytesPerIndex, bytesPerIndex,
              getMetadata().isLittleEndian(imageIndex));
          buf[pt++] = (byte) lut.get(j, iVal);
        }
      }
    }
    else {
      for (int j=0; j<lutLength; j++) {
        for (int i=0; i<index.length / bytesPerIndex; i++) {
          int iVal = DataTools.bytesToInt(index, i * bytesPerIndex, bytesPerIndex,
              getMetadata().isLittleEndian(imageIndex));
          buf[pt++] = (byte) lut.get(j, iVal);
        }
      }
    }

    return plane;
  }

  // -- Helper Methods --

  private void cleanUp() {
    parentPlane = null;
    lutLength = 0;
    filled = null;
  }
}
