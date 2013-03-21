/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
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
package ome.scifio.filters;

import java.io.IOException;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.FormatException;
import ome.scifio.Plane;
import ome.scifio.common.DataTools;
import ome.scifio.util.FormatTools;
import ome.scifio.util.ImageTools;

/**
 * Logic to automatically separate the channels in a file.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ChannelSeparator.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ChannelSeparator.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type=Filter.class, priority=ChannelSeparator.PRIORITY, attrs={
  @Attr(name=ChannelSeparator.FILTER_KEY, value=ChannelSeparator.FILTER_VALUE),
  @Attr(name=ChannelSeparator.ENABLED_KEY, value=ChannelSeparator.ENABLED_VAULE)
  })
public class ChannelSeparator extends AbstractReaderFilter {

  // -- Constants --
  
  public static final double PRIORITY = 2.0;
  public static final String FILTER_VALUE = "ome.scifio.Reader";
  
  // -- Fields --

  private Plane parentPlane = null;
  
  //TODO I think these can be condensed and referenced off of lastPlane
  /** Last plane opened. */
  private Plane lastPlane = null;

  /** Index of last plane opened. */
  private int lastPlaneIndex = -1;

  /** Index of last plane opened. */
  private int lastImageIndex = -1;

  /** X index of last plane opened. */
  private int lastPlaneX = -1;

  /** Y index of last plane opened. */
  private int lastPlaneY = -1;

  /** Width of last plane opened. */
  private int lastPlaneWidth = -1;

  /** Height of last plane opened. */
  private int lastPlaneHeight = -1;

  // -- ChannelSeparator API methods --

  /**
   * Returns the image number in the original dataset that corresponds to the
   * given image number.  For instance, if the original dataset was a single
   * RGB image and the given image number is 2, the return value will be 0.
   *
   * @param planeIndex is a plane number greater than or equal to 0 and less than
   *   getPlaneCount()
   * @return the corresponding plane number in the original (unseparated) data.
   */
  public int getOriginalIndex(int imageIndex, int planeIndex) {
    int planeCount = getPlaneCount(imageIndex);
    int originalCount = getParent().getPlaneCount(imageIndex);

    if (planeCount == originalCount) return planeIndex;
    int[] coords = getZCTCoords(imageIndex, planeIndex);
    coords[1] /= getParentMeta().getRGBChannelCount(imageIndex);
    return FormatTools.getIndex(getParent(), imageIndex, coords[0], coords[1], coords[2]);
  }
  
  // -- AbstractReaderFilter API Methods --
  
  /*
   * @see ome.scifio.filters.AbstractReaderFilter#setSourceHelper(java.lang.String)
   */
  protected void setSourceHelper(String source){
    cleanUp();
  }
  
  // -- Filter API Methods --

  /*
   */
  @Override
  public boolean isCompatible(Class<?> c) {
    return ByteArrayReader.class.isAssignableFrom(c);
  }
  
  // -- Reader API methods --

  /* @see Reader#getImageCount(int) */
  public int getPlaneCount(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return (getParentMeta().isRGB(imageIndex) && !getParentMeta().isIndexed(imageIndex) ?
      getParentMeta().getRGBChannelCount(imageIndex) : 1) * getParentMeta().getImageCount();
  }

  /* @see Reader#openBytes(int, int) */
  public Plane openPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    return openPlaneHelper(
        getParent().openPlane(imageIndex, planeIndex), null, planeIndex, imageIndex);
  }

  /* @see Reader#openBytes(int, int, byte[]) */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane)
    throws FormatException, IOException
  {
    if (parentPlane == null) parentPlane = getParent().openPlane(imageIndex, planeIndex);
    else getParent().openPlane(imageIndex, planeIndex, parentPlane);
    return openPlaneHelper(parentPlane, plane, planeIndex, imageIndex);
  }

  /* @see Reader#openBytes(int, int, int, int, int, int) */
  public Plane openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return openPlaneHelper(
        getParent().openPlane(imageIndex, planeIndex, x, y, w, h), null, planeIndex, imageIndex);
  }

  /* @see Reader#openBytes(int, byte[], int, int, int, int) */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    if (parentPlane == null) parentPlane = getParent().openPlane(imageIndex, planeIndex, x, y, w, h);
    else getParent().openPlane(imageIndex, planeIndex, parentPlane, x, y, w, h);
    return openPlaneHelper(parentPlane, plane, planeIndex, imageIndex);
  }

  /* @see Reader#openThumbBytes(int) */
  public Plane openThumbPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);

    int source = getOriginalIndex(imageIndex, planeIndex);
    Plane thumb = getParent().openThumbPlane(source, planeIndex);
    
    ByteArrayPlane ret = null;
    
    if (isCompatible(thumb.getClass())) ret = (ByteArrayPlane)thumb;
    else {
      ret = new ByteArrayPlane(thumb.getContext());
      ret.populate(thumb);
    }
    
    //TODO maybe these imageIndices should be source as well?

    int c = getDatasetMetadata().getAxisLength(imageIndex, Axes.CHANNEL) /
      getParentMeta().getEffectiveSizeC(imageIndex);
    int channel = planeIndex % c;
    int bpp = FormatTools.getBytesPerPixel(getDatasetMetadata().getPixelType(imageIndex));

    ret.setData(ImageTools.splitChannels(thumb.getBytes(), channel, c, bpp, false, false));
    return ret;
  }
  
  public void close() throws IOException {
    close(false);
  }

  /* @see Reader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      cleanUp();
    }
  }
  
  /* @see Reader#getZCTCoords(int) */
  public int[] getZCTCoords(int imageIndex, int index) {
    return FormatTools.getZCTCoords(this, imageIndex, index);
  }
  
  // -- Helper Methods --
  
  private void cleanUp() {
    parentPlane = null;
    lastPlane = null;
    lastPlaneIndex = -1;
    lastImageIndex = -1;
    lastPlaneX = -1;
    lastPlaneY = -1;
    lastPlaneWidth = -1;
    lastPlaneHeight = -1;
  }
  
  public Plane openPlaneHelper(Plane parentPlane, Plane plane, int planeIndex, int imageIndex)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    FormatTools.checkPlaneNumber(this, imageIndex, planeIndex);
    
    if (getParentMeta().isRGB(imageIndex) && !getParentMeta().isIndexed(imageIndex)) {
      int c = getDatasetMetadata().getAxisLength(imageIndex, Axes.CHANNEL) / getParentMeta().getEffectiveSizeC(imageIndex);
      int source = getOriginalIndex(imageIndex, planeIndex);
      int channel = planeIndex % c;
      int bpp = FormatTools.getBytesPerPixel(getDatasetMetadata().getPixelType(imageIndex));
      int x = parentPlane.getxOffset(), y = parentPlane.getyOffset(), w = parentPlane.getxLength(), h = parentPlane.getyLength();

      if (plane == null || !isCompatible(plane.getClass())) {
        ByteArrayPlane bp = new ByteArrayPlane(parentPlane.getContext());
        bp.populate(parentPlane);
        bp.setData(new byte[parentPlane.getBytes().length]);
        
        plane = bp;
      }
      
      if (source != lastPlaneIndex || imageIndex != lastImageIndex ||
          x != lastPlaneX || y != lastPlaneY || w != lastPlaneWidth ||
          h != lastPlaneHeight)
      {
        int strips = 1;

        // check how big the original image is; if it's larger than the
        // available memory, we will need to split it into strips

        Runtime rt = Runtime.getRuntime();
        long availableMemory = rt.freeMemory();
        long planeSize = DataTools.safeMultiply64(w, h, bpp, c);

        if (availableMemory < planeSize || planeSize > Integer.MAX_VALUE) {
          strips = (int) Math.sqrt(h);
        }

        int stripHeight = h / strips;
        int lastStripHeight = stripHeight + (h - (stripHeight * strips));
        byte[] strip = strips == 1 ? parentPlane.getBytes() : new byte[stripHeight * w * bpp];
        for (int i=0; i<strips; i++) {
          lastPlane = strips == 1 ? parentPlane : 
              getParent().openPlane(imageIndex, source, x, y + i * stripHeight, w,
                                    i == strips - 1 ? lastStripHeight : stripHeight);
          lastPlaneIndex = source;
          lastImageIndex = imageIndex;
          lastPlaneX = x;
          lastPlaneY = y + i * stripHeight;
          lastPlaneWidth = w;
          lastPlaneHeight = i == strips - 1 ? lastStripHeight : stripHeight;

          if (strips != 1 && lastStripHeight != stripHeight && i == strips - 1)
          {
            strip = new byte[lastStripHeight * w * bpp];
          }

          ImageTools.splitChannels(lastPlane.getBytes(), strip, channel, c, bpp,
              false, getDatasetMetadata().isInterleaved(imageIndex), strips == 1 ? w * h * bpp : strip.length);
          if (strips != 1) {
            System.arraycopy(strip, 0, plane.getBytes(), i * stripHeight * w * bpp,
                strip.length);
          }
        }
      }
      else {
        ImageTools.splitChannels(lastPlane.getBytes(), plane.getBytes(), channel, c, bpp,
            false, getDatasetMetadata().isInterleaved(imageIndex), w * h * bpp);
      }

      return plane;
    }
    return parentPlane;
  }
}
