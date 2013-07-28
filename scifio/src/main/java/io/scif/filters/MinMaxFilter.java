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

import io.scif.FormatException;
import io.scif.Plane;
import io.scif.common.DataTools;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Logic to compute minimum and maximum values for each channel.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MinMaxCalculator.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MinMaxCalculator.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type=MinMaxFilter.class, attrs={
  @Attr(name=MinMaxFilter.FILTER_KEY, value=MinMaxFilter.FILTER_VALUE),
  @Attr(name=MinMaxFilter.ENABLED_KEY, value=MinMaxFilter.ENABLED_VAULE)
  })
public class MinMaxFilter extends AbstractReaderFilter {

  // -- Constants --

  public static final String FILTER_VALUE = "io.scif.Reader";

  // -- Fields --

  /** Min values for each channel. */
  protected double[][] chanMin;

  /** Max values for each channel. */
  protected double[][] chanMax;

  /** Min values for each plane. */
  protected double[][] planeMin;

  /** Max values for each plane. */
  protected double[][] planeMax;

  /** Number of planes for which min/max computations have been completed. */
  protected int[] minMaxDone;

  // -- MinMaxCalculator API methods --

  /**
   * Retrieves a specified channel's global minimum.
   * Returns null if some of the image planes have not been read.
   *
   * @throws IOException Not actually thrown.
   */
  public Double getChannelGlobalMinimum(int imageIndex, int theC)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (theC < 0 || theC >= getMetadata().getAxisLength(imageIndex, Axes.CHANNEL)) {
      throw new FormatException("Invalid channel index: " + theC);
    }

    // check that all planes have been read
    if (minMaxDone == null || minMaxDone[imageIndex] < getImageCount()) {
      return null;
    }
    return new Double(chanMin[imageIndex][theC]);
  }

  /**
   * Retrieves a specified channel's global maximum.
   * Returns null if some of the image planes have not been read.
   * @throws IOException Not actually thrown.
   */
  public Double getChannelGlobalMaximum(int imageIndex, int theC)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (theC < 0 || theC >= getMetadata().getAxisLength(imageIndex, Axes.CHANNEL)) {
      throw new FormatException("Invalid channel index: " + theC);
    }

    // check that all planes have been read
    if (minMaxDone == null || minMaxDone[imageIndex] < getImageCount()) {
      return null;
    }
    return new Double(chanMax[imageIndex][theC]);
  }

  /**
   * Retrieves the specified channel's minimum based on the images that have
   * been read.  Returns null if no image planes have been read yet.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  public Double getChannelKnownMinimum(int imageIndex, int theC)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return chanMin == null ? null : new Double(chanMin[imageIndex][theC]);
  }

  /**
   * Retrieves the specified channel's maximum based on the images that
   * have been read.  Returns null if no image planes have been read yet.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  public Double getChannelKnownMaximum(int imageIndex, int theC)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return chanMax == null ? null : new Double(chanMax[imageIndex][theC]);
  }

  /**
   * Retrieves the minimum pixel value for the specified plane.
   * If each image plane contains more than one channel (i.e.,
   * {@link #getRGBChannelCount()} &gt; 1), returns the maximum value for each
   * embedded channel. Returns null if the plane has not already been read.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  public Double[] getPlaneMinimum(int imageIndex, int planeIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (planeMin == null) return null;

    int numRGB = getMetadata().getRGBChannelCount(imageIndex);
    int pBase = planeIndex * numRGB;
    if (Double.isNaN(planeMin[imageIndex][pBase])) return null;

    Double[] min = new Double[numRGB];
    for (int c=0; c<numRGB; c++) {
      min[c] = new Double(planeMin[imageIndex][pBase + c]);
    }
    return min;
  }

  /**
   * Retrieves the maximum pixel value for the specified plane.
   * If each image plane contains more than one channel (i.e.,
   * {@link #getRGBChannelCount()} &gt; 1), returns the maximum value for each
   * embedded channel. Returns null if the plane has not already been read.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  public Double[] getPlaneMaximum(int imageIndex, int planeIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (planeMax == null) return null;

    int numRGB = getMetadata().getRGBChannelCount(imageIndex);
    int pBase = planeIndex * numRGB;
    if (Double.isNaN(planeMax[imageIndex][pBase])) return null;

    Double[] max = new Double[numRGB];
    for (int c=0; c<numRGB; c++) {
      max[c] = new Double(planeMax[imageIndex][pBase + c]);
    }
    return max;
  }

  /**
   * Returns true if the values returned by
   * getChannelGlobalMinimum/Maximum can be trusted.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  public boolean isMinMaxPopulated(int imageIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return minMaxDone != null && minMaxDone[imageIndex] == getImageCount();
  }

  // -- IFormatReader API methods --

  @Override
  public Plane openPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    io.scif.Metadata m = getMetadata();
    return openPlane(imageIndex, planeIndex, 0, 0, m.getAxisLength(imageIndex, Axes.X),
        m.getAxisLength(imageIndex, Axes.Y));
  }

  @Override
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane)
    throws FormatException, IOException
  {
    io.scif.Metadata m = getMetadata();
    return openPlane(imageIndex, planeIndex, plane, 0, 0, m.getAxisLength(imageIndex, Axes.X),
        m.getAxisLength(imageIndex, Axes.Y));
  }

  @Override
  public Plane openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return openPlane(imageIndex, planeIndex, createPlane(x, y, w, h), x, y, w, h);
  }

  @Override
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);
    super.openPlane(imageIndex, planeIndex, plane, x, y, w, h);

    updateMinMax(imageIndex, planeIndex, plane.getBytes(), FormatTools.getBytesPerPixel(
        getMetadata().getPixelType(imageIndex)) * w * h);
    return plane;
  }

  /* @see IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      chanMin = null;
      chanMax = null;
      planeMin = null;
      planeMax = null;
      minMaxDone = null;
    }
  }

  // -- IFormatHandler API methods --

  /* @see IFormatHandler#getNativeDataType() */
  public Class<?> getNativeDataType() {
    return byte[].class;
  }

  // -- Helper methods --

  /**
   * Updates min/max values based on the given byte array.
   * @param no the image index within the file.
   * @param buf a pre-allocated buffer.
   * @param len as <code>buf</code> may be larger than the actual pixel count
   * having been written to it, the length (in bytes) of the those pixels.
   */
  protected void updateMinMax(int imageIndex, int planeIndex, byte[] buf, int len)
    throws FormatException, IOException
  {
    if (buf == null) return;
    initMinMax();

    io.scif.Metadata m = getMetadata();
    int numRGB = m.getRGBChannelCount(imageIndex);
    int pixelType = m.getPixelType(imageIndex);
    int bpp = FormatTools.getBytesPerPixel(pixelType);
    int planeSize = m.getAxisLength(imageIndex, Axes.X) *
        m.getAxisLength(imageIndex, Axes.Y) * bpp;
    // check whether min/max values have already been computed for this plane
    // and that the buffer requested is actually the entire plane
    if (len == planeSize
        && !Double.isNaN(planeMin[imageIndex][planeIndex * numRGB])) return;

    boolean little = m.isLittleEndian(imageIndex);

    int pixels = len / (bpp * numRGB);
    boolean interleaved = m.isInterleaved(imageIndex);

    int[] coords = FormatTools.getZCTCoords(m, imageIndex, planeIndex);
    int cBase = coords[1] * numRGB;
    int pBase = planeIndex * numRGB;
    for (int c=0; c<numRGB; c++) {
      planeMin[imageIndex][pBase + c] = Double.POSITIVE_INFINITY;
      planeMax[imageIndex][pBase + c] = Double.NEGATIVE_INFINITY;
    }

    boolean signed = FormatTools.isSigned(pixelType);

    long threshold = (long) Math.pow(2, bpp * 8 - 1);
    for (int i=0; i<pixels; i++) {
      for (int c=0; c<numRGB; c++) {
        int idx = bpp * (interleaved ? i * numRGB + c : c * pixels + i);
        long bits = DataTools.bytesToLong(buf, idx, bpp, little);
        if (signed) {
          if (bits >= threshold) bits -= 2*threshold;
        }
        double v = bits;
        if (pixelType == FormatTools.FLOAT) {
          v = Float.intBitsToFloat((int) bits);
        }
        else if (pixelType == FormatTools.DOUBLE) {
          v = Double.longBitsToDouble(bits);
        }

        if (v > chanMax[imageIndex][cBase + c]) {
          chanMax[imageIndex][cBase + c] = v;
        }
        if (v < chanMin[imageIndex][cBase + c]) {
          chanMin[imageIndex][cBase + c] = v;
        }
      }
    }

    for (int c=0; c<numRGB; c++) {
      if (chanMin[imageIndex][cBase + c] < planeMin[imageIndex][pBase + c]) {
        planeMin[imageIndex][pBase + c] = chanMin[imageIndex][cBase + c];
      }
      if (chanMax[imageIndex][cBase + c] > planeMax[imageIndex][pBase + c]) {
        planeMax[imageIndex][pBase + c] = chanMax[imageIndex][cBase + c];
      }
    }
    minMaxDone[imageIndex] = Math.max(minMaxDone[imageIndex], planeIndex + 1);
  }

  /**
   * Ensures internal min/max variables are initialized properly.
   *
   * @throws FormatException Not actually thrown.
   * @throws IOException Not actually thrown.
   */
  protected void initMinMax() throws FormatException, IOException {
    io.scif.Metadata m = getMetadata();
    int imageCount = m.getImageCount();

    if (chanMin == null) {
      chanMin = new double[imageCount][];
      for (int i=0; i<imageCount; i++) {
        chanMin[i] = new double[m.getAxisLength(i, Axes.CHANNEL)];
        Arrays.fill(chanMin[i], Double.POSITIVE_INFINITY);
      }
    }
    if (chanMax == null) {
      chanMax = new double[imageCount][];
      for (int i=0; i<imageCount; i++) {
        chanMax[i] = new double[m.getAxisLength(i, Axes.CHANNEL)];
        Arrays.fill(chanMax[i], Double.NEGATIVE_INFINITY);
      }
    }
    if (planeMin == null) {
      planeMin = new double[imageCount][];
      for (int i=0; i<imageCount; i++) {
        int numRGB = m.getRGBChannelCount(i);
        planeMin[i] = new double[getPlaneCount(i) * numRGB];
        Arrays.fill(planeMin[i], Double.NaN);
      }
    }
    if (planeMax == null) {
      planeMax = new double[imageCount][];
      for (int i=0; i<imageCount; i++) {
        int numRGB = m.getRGBChannelCount(i);
        planeMax[i] = new double[getPlaneCount(i) * numRGB];
        Arrays.fill(planeMax[i], Double.NaN);
      }
    }
    if (minMaxDone == null) minMaxDone = new int[imageCount];
  }

}
