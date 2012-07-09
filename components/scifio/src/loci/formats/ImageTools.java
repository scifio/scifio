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

package loci.formats;

/**
 * A legacy delegator class for ome.scifio.util.ImageTools.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ImageTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ImageTools.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public final class ImageTools {

  // -- Constructor --

  private ImageTools() { }

  // -- Image conversion --

  /**
   * Convert an arbitrary primitive type array with 3 samples per pixel to
   * a 3 x (width * height) byte array.
   */
  public static byte[][] make24Bits(Object pixels, int w, int h,
    boolean interleaved, boolean reverse)
  {
    return ome.scifio.util.ImageTools.make24Bits(pixels, w, h, interleaved, reverse);
  }

  /**
   * Convert an arbitrary primitive type array with 3 samples per pixel to
   * a 3 x (width * height) byte array.  Scaling is performed according to
   * the specified minimum and maximum pixel values in the original image.
   *
   * If the minimum is null, it is assumed to be 0.
   * If the maximum is null, it is assumed to be 2^nbits - 1.
   */
  public static byte[][] make24Bits(Object pixels, int w, int h,
    boolean interleaved, boolean reverse, Double min, Double max)
  {
    return ome.scifio.util.ImageTools.make24Bits(pixels, w, h, interleaved, reverse, min, max);
  }

  /**
   * Convert an arbitrary primitive type array with 3 samples per pixel to
   * an int array, i.e. RGB color with 8 bits per pixel.
   */
  public static int[] make24Bits(Object pixels, int w, int h,
    boolean interleaved)
  {
    return ome.scifio.util.ImageTools.make24Bits(pixels, w, h, interleaved);
  }

  /**
   * Convert an arbitrary primitive type array with 3 samples per pixel to
   * an int array, i.e. RGB color with 8 bits per pixel.
   *
   * Scaling is performed according to
   * the specified minimum and maximum pixel values in the original image.
   *
   * If the minimum is null, it is assumed to be 0.
   * If the maximum is null, it is assumed to be 2^nbits - 1.
   */
  public static int[] make24Bits(Object pixels, int w, int h,
    boolean interleaved, Double min, Double max)
  {
    return ome.scifio.util.ImageTools.make24Bits(pixels, w, h, interleaved, min, max);
  }

  // -- Image manipulation --

  /**
   * Splits the given multi-channel array into a 2D array.
   * The "reverse" parameter is false if channels are in RGB order, true if
   * channels are in BGR order.
   */
  public static byte[] splitChannels(byte[] array, int index, int c, int bytes,
    boolean reverse, boolean interleaved)
  {
    return ome.scifio.util.ImageTools.splitChannels(array, index, c, bytes, reverse, interleaved);
  }

  /**
   * Splits the given multi-channel array into a 2D array.
   * The "reverse" parameter is false if channels are in RGB order, true if
   * channels are in BGR order.  If the 'rtn' parameter is not null, the
   * specified channel will be copied into 'rtn'.
   *
   * The 'channelLength' parameter specifies the number of bytes that are
   * expected to be in a single channel.  In many cases, this will match
   * the value of 'rtn.length', but specifying it separately allows 'rtn' to
   * be larger than the size of a single channel.
   */
  public static byte[] splitChannels(byte[] array, byte[] rtn, int index, int c,
    int bytes, boolean reverse, boolean interleaved, int channelLength)
  {
    return ome.scifio.util.ImageTools.splitChannels(array, rtn, index, c, bytes,
      reverse, interleaved, channelLength);
  }

  /**
   * Pads (or crops) the byte array to the given width and height.
   * The image will be centered within the new bounds.
   */
  public static byte[] padImage(byte[] b, boolean interleaved, int c,
    int oldWidth, int width, int height)
  {
    return ome.scifio.util.ImageTools.padImage(b, interleaved, c, oldWidth, width, height);
  }

  /**
   * Pads (or crops) the short array to the given width and height.
   * The image will be centered within the new bounds.
   */
  public static short[] padImage(short[] b, boolean interleaved, int c,
    int oldWidth, int width, int height)
  {
    return ome.scifio.util.ImageTools.padImage(b, interleaved, c, oldWidth, width, height);
  }

  /**
   * Pads (or crops) the int array to the given width and height.
   * The image will be centered within the new bounds.
   */
  public static int[] padImage(int[] b, boolean interleaved, int c,
    int oldWidth, int width, int height)
  {
    return ome.scifio.util.ImageTools.padImage(b, interleaved, c, oldWidth, width, height);
  }

  /**
   * Pads (or crops) the float array to the given width and height.
   * The image will be centered within the new bounds.
   */
  public static float[] padImage(float[] b, boolean interleaved, int c,
    int oldWidth, int width, int height)
  {
    return ome.scifio.util.ImageTools.padImage(b, interleaved, c, oldWidth, width, height);
  }

  /**
   * Pads (or crops) the double array to the given width and height.
   * The image will be centered within the new bounds.
   */
  public static double[] padImage(double[] b, boolean interleaved, int c,
    int oldWidth, int width, int height)
  {
    return ome.scifio.util.ImageTools.padImage(b, interleaved, c, oldWidth, width, height);
  }

  /**
   * Perform autoscaling on the given byte array;
   * map min to 0 and max to 255.  If the number of bytes per pixel is 1, then
   * nothing happens.
   */
  public static byte[] autoscale(byte[] b, int min, int max, int bpp,
    boolean little)
  {
    return ome.scifio.util.ImageTools.autoscale(b, min, max, bpp, little);
  }

  /** Scan a plane for the channel min and max values. */
  public static Double[] scanData(byte[] plane, int bits, boolean littleEndian)
  {
    return ome.scifio.util.ImageTools.scanData(plane, bits, littleEndian);
  }

  public static byte[] getSubimage(byte[] src, byte[] dest, int originalWidth,
    int originalHeight, int x, int y, int w, int h, int bpp, int channels,
    boolean interleaved)
  {
    return ome.scifio.util.ImageTools.getSubimage(src, dest, originalWidth, originalHeight,
      x, y, w, h, bpp, channels, interleaved);
  }

  // -- Indexed color conversion --

  /** Converts a LUT and an array of indices into an array of RGB tuples. */
  public static byte[][] indexedToRGB(byte[][] lut, byte[] b) {
    return ome.scifio.util.ImageTools.indexedToRGB(lut, b);
  }

  /** Converts a LUT and an array of indices into an array of RGB tuples. */
  public static short[][] indexedToRGB(short[][] lut, byte[] b, boolean le) {
    return ome.scifio.util.ImageTools.indexedToRGB(lut, b, le);
  }

  public static byte[] interpolate(short[] s, byte[] buf, int[] bayerPattern,
    int width, int height, boolean littleEndian)
  {
    return ome.scifio.util.ImageTools.interpolate(s, buf, bayerPattern, width,
      height, littleEndian);
  }

  public static void bgrToRgb(byte[] buf, boolean interleaved, int bpp, int c) {
    ome.scifio.util.ImageTools.bgrToRgb(buf, interleaved, bpp, c);
  }

}
