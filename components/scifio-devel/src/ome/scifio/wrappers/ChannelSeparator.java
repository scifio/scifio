/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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
package ome.scifio.wrappers;

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;
import ome.scifio.DatasetMetadata;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.common.DataTools;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;
import ome.scifio.util.ImageTools;

/**
 * Logic to automatically separate the channels in a file.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ChannelSeparator.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ChannelSeparator.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class ChannelSeparator<M extends Metadata> extends ReaderWrapper<M> {

  // -- Utility methods --

  /** Converts the given reader into a ChannelSeparator, wrapping if needed. */
  public static ChannelSeparator makeChannelSeparator(Reader r) {
    if (r instanceof ChannelSeparator) return (ChannelSeparator) r;
    return new ChannelSeparator(r);
  }

  // -- Fields --

  //TODO remove state..
  /** Last plane opened. */
  private byte[] lastPlane;

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

  // -- Constructors --

  /** Constructs a ChannelSeparator around a new image reader. */
  public ChannelSeparator() { super(); }

  /** Constructs a ChannelSeparator with the given reader. */
  public ChannelSeparator(Reader<M> r) { super(r); }
  
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
    int originalCount = datasetMeta().getPlaneCount(imageIndex);

    if (planeCount == originalCount) return planeIndex;
    int[] coords = getZCTCoords(imageIndex, planeIndex);
    coords[1] /= datasetMeta().getRGBChannelCount(imageIndex);
    return FormatTools.getIndex(this, imageIndex, coords[0], coords[1], coords[2]);
  }
  
  /**
   * 
   * @param imageIndex
   * @return
   */
  public String getDimensionOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    String order = FormatTools.findDimensionOrder(getReader(), imageIndex);
    if (datasetMeta().isRGB(imageIndex) && !datasetMeta().isIndexed(imageIndex)) {
      String newOrder = "XYC";
      if (order.indexOf("Z") > order.indexOf("T")) newOrder += "TZ";
      else newOrder += "ZT";
      return newOrder;
    }
    return order;
  }
  
  /**
   * 
   * @return
   */
  public boolean isRGB(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return datasetMeta().isIndexed(imageIndex) && !datasetMeta().isFalseColor(imageIndex)
      && datasetMeta().getAxisLength(imageIndex, Axes.CHANNEL) > 1;
  }
  
  // -- Reader API methods --

  /* @see Reader#getImageCount(int) */
  public int getPlaneCount(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return (datasetMeta().isRGB(imageIndex) && !datasetMeta().isIndexed(imageIndex)) ?
      datasetMeta().getRGBChannelCount(imageIndex) * datasetMeta().getImageCount() :
        datasetMeta().getImageCount();
  }

  /* @see Reader#openBytes(int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    return openBytes(imageIndex, planeIndex, 0, 0, datasetMeta().getAxisLength(imageIndex, Axes.X),
      datasetMeta().getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, byte[]) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException
  {
    return openBytes(imageIndex, planeIndex, buf, 0, 0, datasetMeta().getAxisLength(imageIndex, Axes.X),
      datasetMeta().getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    byte[] buf =
      DataTools.allocate(w, h, FormatTools.getBytesPerPixel(datasetMeta().getPixelType(imageIndex)));
    return openBytes(imageIndex, planeIndex, buf, x, y, w, h);
  }

  /* @see Reader#openBytes(int, byte[], int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    FormatTools.checkPlaneNumber(this, imageIndex, planeIndex);

    if (datasetMeta().isRGB(imageIndex) && !datasetMeta().isIndexed(imageIndex)) {
      int c = datasetMeta().getAxisLength(imageIndex, Axes.CHANNEL) / datasetMeta().getEffectiveSizeC(imageIndex);
      int source = getOriginalIndex(imageIndex, planeIndex);
      int channel = planeIndex % c;
      int bpp = FormatTools.getBytesPerPixel(datasetMeta().getPixelType(imageIndex));

      //TODO not clear if this should be planeIndex or imageIndex.. it was series originally.. but not sure why?
      if (source != lastPlaneIndex || planeIndex != lastImageIndex ||
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
        byte[] strip = strips == 1 ? buf : new byte[stripHeight * w * bpp];
        for (int i=0; i<strips; i++) {
          lastPlane = getReader().openBytes(imageIndex, source, x, y + i * stripHeight, w,
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

          ImageTools.splitChannels(lastPlane, strip, channel, c, bpp,
            false, datasetMeta().isInterleaved(imageIndex), strips == 1 ? w * h * bpp : strip.length);
          if (strips != 1) {
            System.arraycopy(strip, 0, buf, i * stripHeight * w * bpp,
              strip.length);
          }
        }
      }
      else {
        ImageTools.splitChannels(lastPlane, buf, channel, c, bpp,
          false, datasetMeta().isInterleaved(imageIndex), w * h * bpp);
      }

      return buf;
    }
    return getReader().openBytes(imageIndex, planeIndex, buf, x, y, w, h);
  }

  /* @see Reader#openThumbBytes(int) */
  public byte[] openThumbBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);

    int source = getOriginalIndex(imageIndex, planeIndex);
    byte[] thumb = getReader().openThumbBytes(imageIndex, planeIndex);

    int c = datasetMeta().getAxisLength(imageIndex, Axes.CHANNEL) /
      datasetMeta().getEffectiveSizeC(imageIndex);
    int channel = planeIndex % c;
    int bpp = FormatTools.getBytesPerPixel(datasetMeta().getPixelType(imageIndex));

    return ImageTools.splitChannels(thumb, channel, c, bpp, false, false);
  }

  /* @see Reader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      lastPlane = null;
      lastPlaneIndex = -1;
      lastImageIndex = -1;
      lastPlaneX = -1;
      lastPlaneY = -1;
      lastPlaneWidth = -1;
      lastPlaneHeight = -1;
    }
  }
  
  /* @see Reader#getZCTCoords(int) */
  public int[] getZCTCoords(int imageIndex, int index) {
    return FormatTools.getZCTCoords(this, imageIndex, index);
  }

  /* @see Reader#setSource(String) */
  public void setSource(String id) throws IOException {
    setSource(new RandomAccessInputStream(id));
  }
  
  /* @see Reader#setSource(File) */
  public void setSource(File file) throws IOException {
    setSource(new RandomAccessInputStream(file.getAbsolutePath()));
  }
  
  /* @see Reader#setSource(RandomAccessInputStream) */
  public void setSource(RandomAccessInputStream stream) throws IOException {
    super.setSource(stream);
    
    // clear last image cache
    lastPlane = null;
    lastPlaneIndex = -1;
    lastImageIndex = -1;
    lastPlaneX = -1;
    lastPlaneY = -1;
    lastPlaneWidth = -1;
    lastPlaneHeight = -1;
  }
}
