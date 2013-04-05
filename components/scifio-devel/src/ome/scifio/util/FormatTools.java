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

package ome.scifio.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.Writer;
import ome.scifio.common.ReflectException;
import ome.scifio.common.ReflectedUniverse;
import ome.scifio.io.RandomAccessInputStream;

/**
 * A collection of constants and utility methods applicable for all
 * cycles of image processing within SCIFIO.
 * 
 */
public class FormatTools {

  // -- Constants --
  
  public static final String[] COMPRESSION_SUFFIXES = {"bz2", "gz"};
  
  // -- Constants - Thumbnail dimensions --

  /** Default height and width for thumbnails. */
  public static final int THUMBNAIL_DIMENSION = 128;

  // -- Constants - pixel types --

  /** Identifies the <i>INT8</i> data type used to store pixel values. */
  public static final int INT8 = 0;

  /** Identifies the <i>UINT8</i> data type used to store pixel values. */
  public static final int UINT8 = 1;

  /** Identifies the <i>INT16</i> data type used to store pixel values. */
  public static final int INT16 = 2;

  /** Identifies the <i>UINT16</i> data type used to store pixel values. */
  public static final int UINT16 = 3;

  /** Identifies the <i>INT32</i> data type used to store pixel values. */
  public static final int INT32 = 4;

  /** Identifies the <i>UINT32</i> data type used to store pixel values. */
  public static final int UINT32 = 5;

  /** Identifies the <i>FLOAT</i> data type used to store pixel values. */
  public static final int FLOAT = 6;

  /** Identifies the <i>DOUBLE</i> data type used to store pixel values. */
  public static final int DOUBLE = 7;

  /** Human readable pixel type. */
  private static final String[] pixelTypes = makePixelTypes();

  static String[] makePixelTypes() {
    String[] pixelTypes = new String[8];
    pixelTypes[INT8] = "int8";
    pixelTypes[UINT8] = "uint8";
    pixelTypes[INT16] = "int16";
    pixelTypes[UINT16] = "uint16";
    pixelTypes[INT32] = "int32";
    pixelTypes[UINT32] = "uint32";
    pixelTypes[FLOAT] = "float";
    pixelTypes[DOUBLE] = "double";
    return pixelTypes;
  }

  // -- Constants - dimensional labels --

  /**
   * Identifies the <i>Channel</i> dimensional type,
   * representing a generic channel dimension.
   */
  public static final String CHANNEL = "Channel";

  /**
   * Identifies the <i>Spectra</i> dimensional type,
   * representing a dimension consisting of spectral channels.
   */
  public static final String SPECTRA = "Spectra";

  /**
   * Identifies the <i>Lifetime</i> dimensional type,
   * representing a dimension consisting of a lifetime histogram.
   */
  public static final String LIFETIME = "Lifetime";

  /**
   * Identifies the <i>Polarization</i> dimensional type,
   * representing a dimension consisting of polarization states.
   */
  public static final String POLARIZATION = "Polarization";

  /**
   * Identifies the <i>Phase</i> dimensional type,
   * representing a dimension consisting of phases.
   */
  public static final String PHASE = "Phase";

  /**
   * Identifies the <i>Frequency</i> dimensional type,
   * representing a dimension consisting of frequencies.
   */
  public static final String FREQUENCY = "Frequency";

  // -- Constants - miscellaneous --

  /** File grouping options. */
  public static final int MUST_GROUP = 0;
  public static final int CAN_GROUP = 1;
  public static final int CANNOT_GROUP = 2;

  /** Patterns to be used when constructing a pattern for output filenames. */
  public static final String SERIES_NUM = "%s";
  public static final String SERIES_NAME = "%n";
  public static final String CHANNEL_NUM = "%c";
  public static final String CHANNEL_NAME = "%w";
  public static final String Z_NUM = "%z";
  public static final String T_NUM = "%t";
  public static final String TIMESTAMP = "%A";

  // -- Constants - versioning --

  /**
   * Current SVN revision.
   * @deprecated After Git move, deprecated in favour of {@link #VCS_REVISION}.
   */
  @Deprecated
  public static final String SVN_REVISION = "@vcs.revision@";

  /** Current VCS revision. */
  public static final String VCS_REVISION = "@vcs.revision@";

  /** Date on which this release was built. */
  public static final String DATE = "@date@";

  /** Version number of this release. */
  public static final String VERSION = "4.5-DEV";

  // -- Constants - domains --

  /** Identifies the high content screening domain. */
  public static final String HCS_DOMAIN = "High-Content Screening (HCS)";

  /** Identifies the light microscopy domain. */
  public static final String LM_DOMAIN = "Light Microscopy";

  /** Identifies the electron microscopy domain. */
  public static final String EM_DOMAIN = "Electron Microscopy (EM)";

  /** Identifies the scanning probe microscopy domain. */
  public static final String SPM_DOMAIN = "Scanning Probe Microscopy (SPM)";

  /** Identifies the scanning electron microscopy domain. */
  public static final String SEM_DOMAIN = "Scanning Electron Microscopy (SEM)";

  /** Identifies the fluorescence-lifetime domain. */
  public static final String FLIM_DOMAIN = "Fluorescence-Lifetime Imaging";

  /** Identifies the medical imaging domain. */
  public static final String MEDICAL_DOMAIN = "Medical Imaging";

  /** Identifies the histology domain. */
  public static final String HISTOLOGY_DOMAIN = "Histology";

  /** Identifies the gel and blot imaging domain. */
  public static final String GEL_DOMAIN = "Gel/Blot Imaging";

  /** Identifies the astronomy domain. */
  public static final String ASTRONOMY_DOMAIN = "Astronomy";

  /**
   * Identifies the graphics domain.
   * This includes formats used exclusively by analysis software.
   */
  public static final String GRAPHICS_DOMAIN = "Graphics";

  /** Identifies an unknown domain. */
  public static final String UNKNOWN_DOMAIN = "Unknown";

  /** List of non-graphics domains. */
  public static final String[] NON_GRAPHICS_DOMAINS = new String[] {
      LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN,
      MEDICAL_DOMAIN, HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN,
      HCS_DOMAIN, UNKNOWN_DOMAIN};

  /** List of non-HCS domains. */
  public static final String[] NON_HCS_DOMAINS = new String[] {
      LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN,
      MEDICAL_DOMAIN, HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN,
      UNKNOWN_DOMAIN};

  /**
   * List of domains that do not require special handling.  Domains that
   * require special handling are {@link #GRAPHICS_DOMAIN} and
   * {@link #HCS_DOMAIN}.
   */
  public static final String[] NON_SPECIAL_DOMAINS = new String[] {
      LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN,
      MEDICAL_DOMAIN, HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN,
      UNKNOWN_DOMAIN};

  /** List of all supported domains. */
  public static final String[] ALL_DOMAINS = new String[] {
      HCS_DOMAIN, LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN,
      MEDICAL_DOMAIN, HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN,
      GRAPHICS_DOMAIN, UNKNOWN_DOMAIN};

  // -- Constants - web pages --

  /** URL of Bio-Formats web page. */
  public static final String URL_BIO_FORMATS =
    "http://www.openmicroscopy.org/site/products/bio-formats";

  /** URL of 'Bio-Formats as a Java Library' web page. */
  public static final String URL_BIO_FORMATS_LIBRARIES =
    "http://www.openmicroscopy.org/site/support/bio-formats/developers/java-library.html";

  /** URL of OME-TIFF web page. */
  public static final String URL_OME_TIFF = "http://ome-xml.org/wiki/OmeTiff";

  // -- Constructor --

  private FormatTools() {
  }
  
  // Utility methods -- dimensional positions --
  
  /**
   * Gets the rasterized index corresponding
   * to the given Z, C and T coordinates.
   */
  public static int getIndex(Reader reader, int imageIndex, int z, int c, int t)
  {
    int zSize = reader.getMetadata().getAxisLength(imageIndex, Axes.Z);
    int cSize = reader.getMetadata().getEffectiveSizeC(imageIndex);
    int tSize = reader.getMetadata().getAxisLength(imageIndex, Axes.TIME);
    int numPlanes = reader.getMetadata().getPlaneCount(imageIndex);
    return getIndex(
      findDimensionOrder(reader, imageIndex), zSize, cSize, tSize, numPlanes, z, c, t);
  }
  
  /**
   * Gets the rasterized index corresponding
   * to the given Z, C and T coordinates.
   *
   * @param order Dimension order.
   * @param zSize Total number of focal planes.
   * @param cSize Total number of channels.
   * @param tSize Total number of time points.
   * @param numPlanes Total number of image planes (zSize * cSize * tSize),
   *   specified as a consistency check.
   * @param z Z coordinate of ZCT coordinate triple to convert to 1D index.
   * @param c C coordinate of ZCT coordinate triple to convert to 1D index.
   * @param t T coordinate of ZCT coordinate triple to convert to 1D index.
   */
  public static int getIndex(String order, int zSize, int cSize, int tSize,
    int numPlanes, int z, int c, int t)
  {
    // check DimensionOrder
    if (order == null) {
      throw new IllegalArgumentException("Dimension order is null");
    }
    if (!order.startsWith("XY") && !order.startsWith("YX")) {
      throw new IllegalArgumentException("Invalid dimension order: " + order);
    }
    int iz = order.indexOf("Z") - 2;
    int ic = order.indexOf("C") - 2;
    int it = order.indexOf("T") - 2;
    if (iz < 0 || iz > 2 || ic < 0 || ic > 2 || it < 0 || it > 2) {
      throw new IllegalArgumentException("Invalid dimension order: " + order);
    }

    // check SizeZ
    if (zSize <= 0) {
      throw new IllegalArgumentException("Invalid Z size: " + zSize);
    }
    if (z < 0 || z >= zSize) {
      throw new IllegalArgumentException("Invalid Z index: " + z + "/" + zSize);
    }

    // check SizeC
    if (cSize <= 0) {
      throw new IllegalArgumentException("Invalid C size: " + cSize);
    }
    if (c < 0 || c >= cSize) {
      throw new IllegalArgumentException("Invalid C index: " + c + "/" + cSize);
    }

    // check SizeT
    if (tSize <= 0) {
      throw new IllegalArgumentException("Invalid T size: " + tSize);
    }
    if (t < 0 || t >= tSize) {
      throw new IllegalArgumentException("Invalid T index: " + t + "/" + tSize);
    }

    // check image count
    if (numPlanes <= 0) {
      throw new IllegalArgumentException("Invalid image count: " + numPlanes);
    }
    if (numPlanes != zSize * cSize * tSize) {
      // if this happens, there is probably a bug in metadata population --
      // either one of the ZCT sizes, or the total number of images --
      // or else the input file is invalid
      throw new IllegalArgumentException("ZCT size vs image count mismatch " +
        "(sizeZ=" + zSize + ", sizeC=" + cSize + ", sizeT=" + tSize +
        ", total=" + numPlanes + ")");
    }

    // assign rasterization order
    int v0 = iz == 0 ? z : (ic == 0 ? c : t);
    int v1 = iz == 1 ? z : (ic == 1 ? c : t);
    int v2 = iz == 2 ? z : (ic == 2 ? c : t);
    int len0 = iz == 0 ? zSize : (ic == 0 ? cSize : tSize);
    int len1 = iz == 1 ? zSize : (ic == 1 ? cSize : tSize);

    return v0 + v1 * len0 + v2 * len0 * len1;
  }

  /**
   * Returns the dimension order for the provided reader.
   * Currently limited to 5D orders.
   */
  public static String findDimensionOrder(Reader r, int imageIndex) {
    return findDimensionOrder(r.getMetadata(), imageIndex);
  }

  /**
   * Returns the dimension order for the provided Metadata object.
   * Currently limited to 5D orders.
   */
  public static String findDimensionOrder(Metadata meta, int imageIndex) {
    return findDimensionOrder(meta.getAxes(imageIndex));
  }
  
  public static String findDimensionOrder(AxisType[] axes) {
    String order = "";
    
    //TODO currently this list is restricted to the traditional 5D axes compatible with Bio-Formats
    ArrayList<AxisType> validAxes =
      new ArrayList<AxisType>(Arrays.asList(new AxisType[]{Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL}));

    for (int i = 0; i < axes.length; i++) {
      AxisType type = axes[i];
      if(validAxes.contains(type))
        order += axes[i].toString().charAt(0);
    }
    
    return order;
  }
  
  /**
   * Attempts to convert the provided String dimension order to an array
   * of AxisTypes.
   * @param dimensionOrder
   * @return
   */
  public static AxisType[] findDimensionList(String dimensionOrder) {
    AxisType[] axes = new AxisType[dimensionOrder.length()];
    
    for(int i = 0; i < dimensionOrder.length(); i++) {
      switch(dimensionOrder.toUpperCase().charAt(i)) {
        case 'X': axes[i] = Axes.X;
          break;
        case 'Y': axes[i] = Axes.Y;
          break;
        case 'Z': axes[i] = Axes.Z;
          break;
        case 'C': axes[i] = Axes.CHANNEL;
          break;
        case 'T': axes[i] = Axes.TIME;
          break;
        default: axes[i] = Axes.UNKNOWN;
      }
    }
    
    return axes;
  }

  /**
   * Gets the Z, C and T coordinates corresponding
   * to the given rasterized index value.
   */
  public static int[] getZCTCoords(Reader reader, int imageIndex, int planeIndex) {
    return getZCTCoords(reader.getMetadata(), imageIndex, planeIndex);
  }
  
  public static int[] getZCTCoords(Metadata meta, int imageIndex, int planeIndex) {
    int zSize = meta.getAxisLength(imageIndex, Axes.Z);
    int cSize = meta.getEffectiveSizeC(imageIndex);
    int tSize = meta.getAxisLength(imageIndex, Axes.TIME);
    int numPlanes = meta.getPlaneCount(imageIndex);

    return getZCTCoords(
      findDimensionOrder(meta, imageIndex), zSize, cSize, tSize, numPlanes, imageIndex, planeIndex);
  }
  
  /**
   * Gets the Z, C and T coordinates corresponding to the given rasterized
   * index value.
   *
   * @param zSize Total number of focal planes.
   * @param cSize Total number of channels.
   * @param tSize Total number of time points.
   * @param numPlanes Total number of image planes (zSize * cSize * tSize),
   *   specified as a consistency check.
   * @param imageIndex 1D (rasterized) index to convert to ZCT coordinate triple.
   */
  public static int[] getZCTCoords(String order, int zSize, int cSize,
    int tSize, int numPlanes, int imageIndex, int planeIndex)
  {
    // check DimensionOrder

    if (!order.startsWith("XY") && !order.startsWith("YX")) {
      throw new IllegalArgumentException("Invalid dimension order: " + order);
    }
    int iz = order.indexOf("Z") - 2;
    int ic = order.indexOf("C") - 2;
    int it = order.indexOf("T") - 2;
    if (iz < 0 || iz > 2 || ic < 0 || ic > 2 || it < 0 || it > 2) {
      throw new IllegalArgumentException("Invalid dimension order: " + order);
    }

    // check SizeZ
    if (zSize <= 0) {
      throw new IllegalArgumentException("Invalid Z size: " + zSize);
    }

    // check SizeC
    if (cSize <= 0) {
      throw new IllegalArgumentException("Invalid C size: " + cSize);
    }

    // check SizeT
    if (tSize <= 0) {
      throw new IllegalArgumentException("Invalid T size: " + tSize);
    }

    // check image count
    if (numPlanes <= 0) {
      throw new IllegalArgumentException("Invalid image count: " + numPlanes);
    }
    if (numPlanes != zSize * cSize * tSize) {
      // if this happens, there is probably a bug in metadata population --
      // either one of the ZCT sizes, or the total number of images --
      // or else the input file is invalid
      throw new IllegalArgumentException("ZCT size vs image count mismatch " +
        "(sizeZ=" + zSize + ", sizeC=" + cSize + ", sizeT=" + tSize +
        ", total=" + numPlanes + ")");
    }
    if (planeIndex < 0 || planeIndex >= numPlanes) {
      throw new IllegalArgumentException("Invalid plane index: " + planeIndex + "/" +
        numPlanes);
    }

    // assign rasterization order
    int len0 = iz == 0 ? zSize : (ic == 0 ? cSize : tSize);
    int len1 = iz == 1 ? zSize : (ic == 1 ? cSize : tSize);
    //int len2 = iz == 2 ? sizeZ : (ic == 2 ? sizeC : sizeT);
    int v0 = planeIndex % len0;
    int v1 = planeIndex / len0 % len1;
    int v2 = planeIndex / len0 / len1;
    int z = iz == 0 ? v0 : (iz == 1 ? v1 : v2);
    int c = ic == 0 ? v0 : (ic == 1 ? v1 : v2);
    int t = it == 0 ? v0 : (it == 1 ? v1 : v2);

    return new int[] {z, c, t};
  }


  /**
   * Converts index from the given dimension order to the reader's native one.
   * This method is useful for shuffling the planar order around
   * (rather than eassigning ZCT sizes as {@link DimensionSwapper} does).
   *
   * @throws FormatException Never actually thrown.
   */
  public static int getReorderedIndex(Reader reader, int imageIndex,
    String newOrder, int newIndex) throws FormatException
  {
    Metadata meta = reader.getMetadata();
    int zSize = meta.getAxisLength(imageIndex, Axes.Z);
    int cSize = meta.getEffectiveSizeC(imageIndex);
    int tSize = meta.getAxisLength(imageIndex, Axes.TIME);
    int numPlanes = meta.getPlaneCount(imageIndex);
    
    return getReorderedIndex(findDimensionOrder(reader, imageIndex), newOrder,
      zSize, cSize, tSize, numPlanes, imageIndex, newIndex);
  }
  
  /**
   * Converts index from one dimension order to another.
   * This method is useful for shuffling the planar order around
   * (rather than eassigning ZCT sizes as {@link DimensionSwapper} does).
   *
   * @param origOrder Original dimension order.
   * @param newOrder New dimension order.
   * @param zSize Total number of focal planes.
   * @param cSize Total number of channels.
   * @param tSize Total number of time points.
   * @param num Total number of image planes (zSize * cSize * tSize),
   *   specified as a consistency check.
   * @param newIndex 1D (rasterized) index according to new dimension order.
   * @return rasterized index according to original dimension order.
   */
  public static int getReorderedIndex(String origOrder, String newOrder,
    int zSize, int cSize, int tSize, int numPlanes, int imageIndex, int newIndex)
  {
    int[] zct = getZCTCoords(newOrder, zSize, cSize, tSize, numPlanes, imageIndex, newIndex);
    return getIndex(origOrder,
      zSize, cSize, tSize, numPlanes, zct[0], zct[1], zct[2]);
  }
  
  
  /**
   * Computes a unique 1-D index corresponding
   * to the given multidimensional position.
   * @param lengths the maximum value for each positional dimension
   * @param pos position along each dimensional axis
   * @return rasterized index value
   */
  public static int positionToRaster(int[] lengths, int[] pos) {
    int offset = 1;
    int raster = 0;
    for (int i=0; i<pos.length; i++) {
      raster += offset * pos[i];
      offset *= lengths[i];
    }
    return raster;
  }

  /**
   * Computes a unique N-D position corresponding
   * to the given rasterized index value.
   * @param lengths the maximum value at each positional dimension
   * @param raster rasterized index value
   * @return position along each dimensional axis
   */
  public static int[] rasterToPosition(int[] lengths, int raster) {
    return rasterToPosition(lengths, raster, new int[lengths.length]);
  }

  /**
   * Computes a unique N-D position corresponding
   * to the given rasterized index value.
   * @param lengths the maximum value at each positional dimension
   * @param raster rasterized index value
   * @param pos preallocated position array to populate with the result
   * @return position along each dimensional axis
   */
  public static int[] rasterToPosition(int[] lengths, int raster, int[] pos) {
    int offset = 1;
    for (int i=0; i<pos.length; i++) {
      int offset1 = offset * lengths[i];
      int q = i < pos.length - 1 ? raster % offset1 : raster;
      pos[i] = q / offset;
      raster -= q;
      offset = offset1;
    }
    return pos;
  }

  /**
   * Computes the number of raster values for a positional array
   * with the given lengths.
   */
  public static int getRasterLength(int[] lengths) {
    int len = 1;
    for (int i=0; i<lengths.length; i++) len *= lengths[i];
    return len;
  }

  // -- Utility methods - sanity checking
  
  /**
   * Asserts that the current file is either null, or not, according to the
   * given flag. If the assertion fails, an IllegalStateException is thrown.
   * @param currentId File name to test.
   * @param notNull True iff id should be non-null.
   * @param depth How far back in the stack the calling method is; this name
   *   is reported as part of the exception message, if available. Use zero
   *   to suppress output of the calling method name.
   */
  public static void assertId(String currentId, boolean notNull, int depth) {
    String msg = null;
    if (currentId == null && notNull) {
      msg = "Current file should not be null; call setId(String) first";
    }
    else if (currentId != null && !notNull) {
      msg = "Current file should be null, but is '" +
        currentId + "'; call close() first";
    }
    if (msg == null) return;

    StackTraceElement[] ste = new Exception().getStackTrace();
    String header;
    if (depth > 0 && ste.length > depth) {
      String c = ste[depth].getClassName();
      if (c.startsWith("ome.scifio.")) {
        c = c.substring(c.lastIndexOf(".") + 1);
      }
      header = c + "." + ste[depth].getMethodName() + ": ";
    }
    else header = "";
    throw new IllegalStateException(header + msg);
  } 
  
  /**
   * Asserts that the current file is either null, or not, according to the
   * given flag. If the assertion fails, an IllegalStateException is thrown.
   * @param currentId File name to test.
   * @param notNull True iff id should be non-null.
   * @param depth How far back in the stack the calling method is; this name
   *   is reported as part of the exception message, if available. Use zero
   *   to suppress output of the calling method name.
   */
  public static void assertStream(RandomAccessInputStream stream,
    boolean notNull, int depth)
  {
    String msg = null;
    if (stream == null && notNull) {
      msg = "Current file should not be null; call setId(String) first";
    }
    else if (stream != null && !notNull) {
      msg =
        "Current file should be null, but is '" + stream +
          "'; call close() first";
    }
    if (msg == null) return;

    StackTraceElement[] ste = new Exception().getStackTrace();
    String header;
    if (depth > 0 && ste.length > depth) {
      String c = ste[depth].getClassName();
      if (c.startsWith("ome.scifio.")) {
        c = c.substring(c.lastIndexOf(".") + 1);
      }
      header = c + "." + ste[depth].getMethodName() + ": ";
    }
    else header = "";
    throw new IllegalStateException(header + msg);
  }

  /**
   * Convenience method for checking that the plane number, tile size and
   * buffer sizes are all valid for the given reader.
   * If 'bufLength' is less than 0, then the buffer length check is not
   * performed.
   */
  public static void checkPlaneParameters(Reader r, int imageIndex,
    int planeIndex, int bufLength, int x, int y, int w, int h)
    throws FormatException
  {
    assertId(r.getCurrentFile(), true, 2);
    checkPlaneNumber(r, imageIndex, planeIndex);
    checkTileSize(r, x, y, w, h, imageIndex);
    if (bufLength >= 0) checkBufferSize(r, bufLength, w, h, imageIndex);
  }
  
  /** Checks that the given plane number is valid for the given reader. */
  public static void checkPlaneNumber(Reader r, int imageIndex, int planeIndex)
    throws FormatException
  {
    int imageCount = r.getMetadata().getPlaneCount(imageIndex);
    if (planeIndex < 0 || planeIndex >= imageCount) {
      throw new FormatException("Invalid plane number: " + planeIndex + " (" +
      /* TODO series=" +
      r.getMetadata().getSeries() + ", */"planeCount=" + planeIndex + ")");
    }
  }
  
  /** Checks that the given tile size is valid for the given reader. */
  public static void checkTileSize(Reader r, int x, int y, int w, int h,
    int imageIndex) throws FormatException
  {
    int width = r.getMetadata().getAxisLength(imageIndex, Axes.X);
    int height = r.getMetadata().getAxisLength(imageIndex, Axes.Y);
    if (x < 0 || y < 0 || w < 0 || h < 0 || (x + w) > width || (y + h) > height)
    {
      throw new FormatException("Invalid tile size: x=" + x + ", y=" + y +
        ", w=" + w + ", h=" + h);
    }
  }

  /**
   * Checks that the given buffer length is long enough to hold planes
   * of the specified image index, using the provided Reader.
   */
  public static void checkBufferSize(int imageIndex, Reader r, int len)
    throws FormatException
  {
    checkBufferSize(
      r, len, r.getMetadata().getAxisLength(imageIndex, Axes.X),
      r.getMetadata().getAxisLength(imageIndex, Axes.Y), imageIndex);
  }
  
  /**
   * Checks that the given buffer size is large enough to hold a w * h
   * image as returned by the given reader.
   * @throws FormatException if the buffer is too small
   */
  public static void checkBufferSize(Reader r, int len, int w, int h,
    int imageIndex) throws FormatException
  {
    int size = getPlaneSize(r, w, h, imageIndex);
    if (size > len) {
      throw new FormatException("Buffer too small (got " + len + ", expected " +
        size + ").");
    }
  }
  
  /**
   * Returns true if the given RandomAccessInputStream conatins at least
   * 'len' bytes.
   */
  public static boolean validStream(RandomAccessInputStream stream, int len,
    boolean littleEndian) throws IOException
  {
    stream.seek(0);
    stream.order(littleEndian);
    return stream.length() >= len;
  }
  
  /** Returns the size in bytes of a single plane. */
  public static int getPlaneSize(Reader r, int imageIndex) {
    return getPlaneSize(
      r, r.getMetadata().getAxisLength(imageIndex, Axes.X),
      r.getMetadata().getAxisLength(imageIndex, Axes.Y), imageIndex);
  }
  
  /** Returns the size in bytes of a w * h tile. */
  public static int getPlaneSize(Reader r, int w, int h, int imageIndex) {
    return w * h * r.getMetadata().getRGBChannelCount(imageIndex) *
      getBytesPerPixel(r.getMetadata().getPixelType(imageIndex));
  }

  // -- Utility methods - pixel types --

  /**
   * Takes a string value and maps it to one of the pixel type enumerations.
   * @param pixelTypeAsString the pixel type as a string.
   * @return type enumeration value for use with class constants.
   */
  public static int pixelTypeFromString(String pixelTypeAsString) {
    String lowercaseTypeAsString = pixelTypeAsString.toLowerCase();
    for (int i = 0; i < pixelTypes.length; i++) {
      if (pixelTypes[i].equals(lowercaseTypeAsString)) return i;
    }
    throw new IllegalArgumentException("Unknown type: '" + pixelTypeAsString +
      "'");
  }

  /**
   * Takes a pixel type value and gets a corresponding string representation.
   * @param pixelType the pixel type.
   * @return string value for human-readable output.
   */
  public static String getPixelTypeString(int pixelType) {
    if (pixelType < 0 || pixelType >= pixelTypes.length) {
      throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
    }
    return pixelTypes[pixelType];
  }

  /**
   * Retrieves how many bytes per pixel the current plane or section has.
   * @param pixelType the pixel type as retrieved from
   * @return the number of bytes per pixel.
   * @see ome.scifio.Metadata#getPixelType()
   */
  public static int getBytesPerPixel(int pixelType) {
    switch (pixelType) {
      case INT8:
      case UINT8:
        return 1;
      case INT16:
      case UINT16:
        return 2;
      case INT32:
      case UINT32:
      case FLOAT:
        return 4;
      case DOUBLE:
        return 8;
    }
    throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
  }

  /**
   * Retrieves how many bytes per pixel the current plane or section has.
   * @param pixelType the pixel type as retrieved from
   *   {@link ome.scifio.Metadata#getPixelType()}.
   * @return the number of bytes per pixel.
   * @see ome.scifio.Metadata#getPixelType()
   */
  public static int getBitsPerPixel(int pixelType) {
    return 8 * FormatTools.getBytesPerPixel(pixelType);
  }

  /**
   * Retrieves the number of bytes per pixel in the current plane.
   * @param pixelType the pixel type, as a String.
   * @return the number of bytes per pixel.
   * @see #pixelTypeFromString(String)
   * @see #getBytesPerPixel(int)
   */
  public static int getBytesPerPixel(String pixelType) {
    return getBytesPerPixel(pixelTypeFromString(pixelType));
  }

  /**
   * Determines whether the given pixel type is floating point or integer.
   * @param pixelType the pixel type as retrieved from
   *   {@link ome.scifio.Metadata#getPixelType()}.
   * @return true if the pixel type is floating point.
   * @see ome.scifio.Metadata#getPixelType()
   */
  public static boolean isFloatingPoint(int pixelType) {
    switch (pixelType) {
      case INT8:
      case UINT8:
      case INT16:
      case UINT16:
      case INT32:
      case UINT32:
        return false;
      case FLOAT:
      case DOUBLE:
        return true;
    }
    throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
  }

  /**
   * Determines whether the given pixel type is signed or unsigned.
   * @param pixelType the pixel type as retrieved from
   *   {@link ome.scifio.Metadata#getPixelType()}.
   * @return true if the pixel type is signed.
   * @see ome.scifio.Metadata#getPixelType()
   */
  public static boolean isSigned(int pixelType) {
    switch (pixelType) {
      case INT8:
      case INT16:
      case INT32:
      case FLOAT:
      case DOUBLE:
        return true;
      case UINT8:
      case UINT16:
      case UINT32:
        return false;
    }
    throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
  }

  /**
   * Returns an appropriate pixel type given the number of bytes per pixel.
   *
   * @param bytes number of bytes per pixel.
   * @param signed whether or not the pixel type should be signed.
   * @param fp whether or not these are floating point pixels.
   */
  public static int pixelTypeFromBytes(int bytes, boolean signed, boolean fp)
    throws FormatException
  {
    switch (bytes) {
      case 1:
        return signed ? INT8 : UINT8;
      case 2:
        return signed ? INT16 : UINT16;
      case 4:
        return fp ? FLOAT : signed ? INT32 : UINT32;
      case 8:
        return DOUBLE;
      default:
        throw new FormatException("Unsupported byte depth: " + bytes);
    }
  }
  
  // -- Utility methods -- export
  
  /**
   * @throws FormatException Never actually thrown.
   * @throws IOException Never actually thrown.
   */
  public static String getFilename(int imageIndex, int image, Reader r,
    String pattern) throws FormatException, IOException
  {

    String filename = pattern.replaceAll(SERIES_NUM, String.valueOf(imageIndex));

    String imageName = r.getCurrentFile() ;
    if (imageName == null) imageName = "Image#" + imageIndex;
    imageName = imageName.replaceAll("/", "_");
    imageName = imageName.replaceAll("\\\\", "_");

    filename = filename.replaceAll(SERIES_NAME, imageName);

    int[] coordinates = FormatTools.getZCTCoords(r, imageIndex, image);

    filename = filename.replaceAll(Z_NUM, String.valueOf(coordinates[0]));
    filename = filename.replaceAll(T_NUM, String.valueOf(coordinates[2]));
    filename = filename.replaceAll(CHANNEL_NUM, String.valueOf(coordinates[1]));

    String channelName = r.getMetadata().getAxisType(imageIndex, coordinates[1]).getLabel();
    if (channelName == null) channelName = String.valueOf(coordinates[1]);
    channelName = channelName.replaceAll("/", "_");
    channelName = channelName.replaceAll("\\\\", "_");

    filename = filename.replaceAll(CHANNEL_NAME, channelName);

    /*
    //TODO check for date
    String date = retrieve.getImageAcquisitionDate(imageIndex).getValue();
    long stamp = 0;
    if (retrieve.getPlaneCount(imageIndex) > image) {
      Double deltaT = retrieve.getPlaneDeltaT(imageIndex, image);
      if (deltaT != null) {
        stamp = (long) (deltaT * 1000);
      }
    }
    stamp += DateTools.getTime(date, DateTools.ISO8601_FORMAT);
    date = DateTools.convertDate(stamp, (int) DateTools.UNIX_EPOCH);

    filename = filename.replaceAll(TIMESTAMP, date);
    */
    
    return filename;
  }
  
  /**
   * @throws FormatException
   * @throws IOException
   */
  public static String[] getFilenames(String pattern, Reader r)
    throws FormatException, IOException
  {
    Vector<String> filenames = new Vector<String>();
    String filename = null;
    for (int series=0; series<r.getImageCount(); series++) {
      for (int image=0; image<r.getImageCount(); image++) {
        filename = getFilename(series, image, r, pattern);
        if (!filenames.contains(filename)) filenames.add(filename);
      }
    }
    return filenames.toArray(new String[0]);
  }

  /**
   * @throws FormatException
   * @throws IOException
   */
  public static int getImagesPerFile(String pattern, Reader r)
    throws FormatException, IOException
  {
    String[] filenames = getFilenames(pattern, r);
    int totalPlanes = 0;
    for (int series=0; series<r.getImageCount(); series++) {
      totalPlanes += r.getMetadata().getPlaneCount(series);
    }
    return totalPlanes / filenames.length;
  } 
  
  // -- Utility methods -- other
  
  /**
   * Default implementation for {@link IFormatReader#openThumbBytes}.
   *
   * At the moment, it uses {@link java.awt.image.BufferedImage} objects
   * to resize thumbnails, so it is not safe for use in headless contexts.
   * In the future, we may reimplement the image scaling logic purely with
   * byte arrays, but handling every case would be substantial effort, so
   * doing so is currently a low priority item.
   */
  public static byte[] openThumbBytes(Reader reader, int imageIndex, int planeIndex)
    throws FormatException, IOException
  {
    // NB: Dependency on AWT here is unfortunate, but very difficult to
    // eliminate in general. We use reflection to limit class loading
    // problems with AWT on Mac OS X.
    ReflectedUniverse r = new ReflectedUniverse();
    byte[][] bytes = null;
    try {
      r.exec("import ome.scifio.gui.AWTImageTools");

      int planeSize = getPlaneSize(reader, imageIndex);
      Plane plane = null;
      if (planeSize < 0) {
        int width = reader.getMetadata().getThumbSizeX(imageIndex) * 4;
        int height = reader.getMetadata().getThumbSizeY(imageIndex) * 4;
        int x = (reader.getMetadata().getAxisLength(imageIndex, Axes.X) - width) / 2;
        int y = (reader.getMetadata().getAxisLength(imageIndex, Axes.Y) - height) / 2;
        plane = reader.openPlane(imageIndex, planeIndex, x, y, width, height);
      }
      else {
        plane = reader.openPlane(imageIndex, planeIndex);
      }

      r.setVar("plane", plane);
      r.setVar("reader", reader);
      r.setVar("sizeX", reader.getMetadata().getAxisLength(imageIndex, Axes.X));
      r.setVar("sizeY", reader.getMetadata().getAxisLength(imageIndex, Axes.Y));
      r.setVar("thumbSizeX", reader.getMetadata().getThumbSizeX(imageIndex));
      r.setVar("thumbSizeY", reader.getMetadata().getThumbSizeY(imageIndex));
      r.setVar("little", reader.getMetadata().isLittleEndian(imageIndex));
      r.setVar("imageIndex", imageIndex);
      r.exec("thumb = AWTImageTools.openThumbImage(plane, reader, imageIndex, sizeX, sizeY," +
      		" thumbSizeX, thumbSizeY, false)");
      
      bytes = (byte[][]) r.exec("AWTImageTools.getPixelBytes(thumb, little)");
    }
    catch (ReflectException exc) {
      throw new FormatException(exc);
    }

    if (bytes.length == 1) return bytes[0];
    int rgbChannelCount = reader.getMetadata().getRGBChannelCount(imageIndex);
    byte[] rtn = new byte[rgbChannelCount * bytes[0].length];

    if (!reader.getMetadata().isInterleaved(imageIndex)) {
      for (int i=0; i<rgbChannelCount; i++) {
        System.arraycopy(bytes[i], 0, rtn, bytes[0].length * i, bytes[i].length);
      }
    }
    else {
      int bpp = FormatTools.getBytesPerPixel(reader.getMetadata().getPixelType(imageIndex));

      for (int i=0; i<bytes[0].length/bpp; i+=bpp) {
        for (int j=0; j<rgbChannelCount; j++) {
          System.arraycopy(bytes[j], i, rtn, (i * rgbChannelCount) + j * bpp, bpp);
        }
      }
    }
    return rtn;
  }
  
  // -- Conversion convenience methods --
  
  /**
   * Convenience method for writing all of the images and metadata obtained
   * from the specified Reader into the specified Writer.
   *
   * @param input the pre-initialized Reader used for reading data.
   * @param output the uninitialized Writer used for writing data.
   * @param outputFile the full path name of the output file to be created.
   * @throws FormatException if there is a general problem reading from or
   * writing to one of the files.
   * @throws IOException if there is an I/O-related error.
   */
  public static void convert(Reader input, Writer output,
    String outputFile)
    throws FormatException, IOException
  {
    
    Plane p = null;
    
    for(int i = 0; i < input.getImageCount(); i++) {
      for(int j = 0; j < input.getPlaneCount(i); j++) {
        p = input.openPlane(i, j);
        output.savePlane(i, j, p);
      }
    }
    
    input.close();
    output.close();
  }

  /**
   * Get the default range for the specified pixel type.  Note that
   * this is not necessarily the minimum and maximum value which may
   * be stored, but the minimum and maximum which should be used for
   * rendering.
   *
   * @param pixelType the pixel type.
   * @returns an array containing the min and max as elements 0 and 1,
   * respectively.
   * @throws IOException if the pixel type is floating point or invalid.
   */
  public static long[] defaultMinMax(int pixelType) {
    long min = 0 , max = 0;

    switch (pixelType) {
    case INT8:
      min = Byte.MIN_VALUE;
      max = Byte.MAX_VALUE;
      break;
    case INT16:
      min = Short.MIN_VALUE;
      max = Short.MAX_VALUE;
      break;
    case INT32:
    case FLOAT:
    case DOUBLE:
      min = Integer.MIN_VALUE;
      max = Integer.MAX_VALUE;
      break;
    case UINT8:
      min = 0;
      max=(long) Math.pow(2, 8)-1;
      break;
    case UINT16:
      min = 0;
      max=(long) Math.pow(2, 16)-1;
      break;
    case UINT32:
      min = 0;
      max=(long) Math.pow(2, 32)-1;
      break;
    default:
      throw new IllegalArgumentException("Invalid pixel type");
    }

    long[] values = {min, max};
    return values;
  }
  
  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String suffix) {
    return checkSuffix(name, new String[] {suffix});
  }

  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String[] suffixList) {
    final String lname = name.toLowerCase();
    for (int i = 0; i < suffixList.length; i++) {
      final String s = "." + suffixList[i];
      if (lname.endsWith(s)) return true;
      for (int j = 0; j < COMPRESSION_SUFFIXES.length; j++) {
        if (lname.endsWith(s + "." + COMPRESSION_SUFFIXES[j])) return true;
      }
    }
    return false;
  }
}
