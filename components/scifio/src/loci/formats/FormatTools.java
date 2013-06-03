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

import java.io.IOException;

import loci.common.RandomAccessInputStream;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.formats.services.OMEXMLServiceImpl;
import loci.legacy.adapter.CommonAdapter;

/**
 * A legacy delegator class for io.scif.util.FormatTools.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/FormatTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/FormatTools.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @deprecated see io.scif.util.FormatTools.
 */
@Deprecated
public final class FormatTools {

  // -- Constants - pixel types --

  /** Identifies the <i>INT8</i> data type used to store pixel values. */
  public static final int INT8 = io.scif.util.FormatTools.INT8;

  /** Identifies the <i>UINT8</i> data type used to store pixel values. */
  public static final int UINT8 = io.scif.util.FormatTools.UINT8;

  /** Identifies the <i>INT16</i> data type used to store pixel values. */
  public static final int INT16 = io.scif.util.FormatTools.INT16;

  /** Identifies the <i>UINT16</i> data type used to store pixel values. */
  public static final int UINT16 = io.scif.util.FormatTools.UINT16;

  /** Identifies the <i>INT32</i> data type used to store pixel values. */
  public static final int INT32 = io.scif.util.FormatTools.INT32;

  /** Identifies the <i>UINT32</i> data type used to store pixel values. */
  public static final int UINT32 = io.scif.util.FormatTools.UINT32;

  /** Identifies the <i>FLOAT</i> data type used to store pixel values. */
  public static final int FLOAT = io.scif.util.FormatTools.FLOAT;

  /** Identifies the <i>DOUBLE</i> data type used to store pixel values. */
  public static final int DOUBLE = io.scif.util.FormatTools.DOUBLE;

  // -- Constants - dimensional labels --

  /**
   * Identifies the <i>Channel</i> dimensional type,
   * representing a generic channel dimension.
   */
  public static final String CHANNEL = io.scif.util.FormatTools.CHANNEL;

  /**
   * Identifies the <i>Spectra</i> dimensional type,
   * representing a dimension consisting of spectral channels.
   */
  public static final String SPECTRA = io.scif.util.FormatTools.SPECTRA;

  /**
   * Identifies the <i>Lifetime</i> dimensional type,
   * representing a dimension consisting of a lifetime histogram.
   */
  public static final String LIFETIME = io.scif.util.FormatTools.LIFETIME;

  /**
   * Identifies the <i>Polarization</i> dimensional type,
   * representing a dimension consisting of polarization states.
   */
  public static final String POLARIZATION = io.scif.util.FormatTools.POLARIZATION;

  /**
   * Identifies the <i>Phase</i> dimensional type,
   * representing a dimension consisting of phases.
   */
  public static final String PHASE = io.scif.util.FormatTools.PHASE;

  /**
   * Identifies the <i>Frequency</i> dimensional type,
   * representing a dimension consisting of frequencies.
   */
  public static final String FREQUENCY = io.scif.util.FormatTools.FREQUENCY;

  // -- Constants - miscellaneous --

  /** File grouping options. */
  public static final int MUST_GROUP = io.scif.util.FormatTools.MUST_GROUP;
  public static final int CAN_GROUP = io.scif.util.FormatTools.CAN_GROUP;
  public static final int CANNOT_GROUP = io.scif.util.FormatTools.CANNOT_GROUP;

  /** Patterns to be used when constructing a pattern for output filenames. */
  public static final String SERIES_NUM = io.scif.util.FormatTools.SERIES_NUM;
  public static final String SERIES_NAME = io.scif.util.FormatTools.SERIES_NAME;
  public static final String CHANNEL_NUM = io.scif.util.FormatTools.CHANNEL_NUM;
  public static final String CHANNEL_NAME = io.scif.util.FormatTools.CHANNEL_NAME;
  public static final String Z_NUM = io.scif.util.FormatTools.Z_NUM;
  public static final String T_NUM = io.scif.util.FormatTools.T_NUM;
  public static final String TIMESTAMP = io.scif.util.FormatTools.TIMESTAMP;

  // -- Constants - versioning --

  /**
   * Current SVN revision.
   * @deprecated After Git move, deprecated in favour of {@link #VCS_REVISION}.
   */
  @Deprecated
  public static final String SVN_REVISION = io.scif.util.FormatTools.SVN_REVISION;

  /** Current VCS revision. */
  public static final String VCS_REVISION = io.scif.util.FormatTools.VCS_REVISION;

  /** Date on which this release was built. */
  public static final String DATE = io.scif.util.FormatTools.DATE;

  /** Version number of this release. */
  public static final String VERSION = io.scif.util.FormatTools.VERSION;

  // -- Constants - domains --

  /** Identifies the high content screening domain. */
  public static final String HCS_DOMAIN = io.scif.util.FormatTools.HCS_DOMAIN;

  /** Identifies the light microscopy domain. */
  public static final String LM_DOMAIN = io.scif.util.FormatTools.LM_DOMAIN;

  /** Identifies the electron microscopy domain. */
  public static final String EM_DOMAIN = io.scif.util.FormatTools.EM_DOMAIN;

  /** Identifies the scanning probe microscopy domain. */
  public static final String SPM_DOMAIN = io.scif.util.FormatTools.SPM_DOMAIN;

  /** Identifies the scanning electron microscopy domain. */
  public static final String SEM_DOMAIN = io.scif.util.FormatTools.SEM_DOMAIN;

  /** Identifies the fluorescence-lifetime domain. */
  public static final String FLIM_DOMAIN = io.scif.util.FormatTools.FLIM_DOMAIN;

  /** Identifies the medical imaging domain. */
  public static final String MEDICAL_DOMAIN = io.scif.util.FormatTools.MEDICAL_DOMAIN;

  /** Identifies the histology domain. */
  public static final String HISTOLOGY_DOMAIN = io.scif.util.FormatTools.HISTOLOGY_DOMAIN;

  /** Identifies the gel and blot imaging domain. */
  public static final String GEL_DOMAIN = io.scif.util.FormatTools.GEL_DOMAIN;

  /** Identifies the astronomy domain. */
  public static final String ASTRONOMY_DOMAIN = io.scif.util.FormatTools.ASTRONOMY_DOMAIN;

  /**
   * Identifies the graphics domain.
   * This includes formats used exclusively by analysis software.
   */
  public static final String GRAPHICS_DOMAIN = io.scif.util.FormatTools.GRAPHICS_DOMAIN;

  /** Identifies an unknown domain. */
  public static final String UNKNOWN_DOMAIN = io.scif.util.FormatTools.UNKNOWN_DOMAIN;

  /** List of non-graphics domains. */
  public static final String[] NON_GRAPHICS_DOMAINS = io.scif.util.FormatTools.NON_GRAPHICS_DOMAINS;

  /** List of non-HCS domains. */
  public static final String[] NON_HCS_DOMAINS = io.scif.util.FormatTools.NON_HCS_DOMAINS;

  /**
   * List of domains that do not require special handling.  Domains that
   * require special handling are {@link #GRAPHICS_DOMAIN} and
   * {@link #HCS_DOMAIN}.
   */
  public static final String[] NON_SPECIAL_DOMAINS = io.scif.util.FormatTools.NON_SPECIAL_DOMAINS;

  /** List of all supported domains. */
  public static final String[] ALL_DOMAINS = io.scif.util.FormatTools.ALL_DOMAINS;

  // -- Constants - web pages --

  /** URL of Bio-Formats web page. */
  public static final String URL_BIO_FORMATS =
    io.scif.util.FormatTools.URL_BIO_FORMATS;

  /** URL of 'Bio-Formats as a Java Library' web page. */
  public static final String URL_BIO_FORMATS_LIBRARIES =
    io.scif.util.FormatTools.URL_BIO_FORMATS_LIBRARIES;

  /** URL of OME-TIFF web page. */
  public static final String URL_OME_TIFF =
    io.scif.util.FormatTools.URL_OME_TIFF;

  // -- Constructor --

  private FormatTools() { }

  // -- Utility methods - dimensional positions --

  /**
   * Gets the rasterized index corresponding
   * to the given Z, C and T coordinates.
   */
  public static int getIndex(IFormatReader reader, int z, int c, int t) {
    return io.scif.util.FormatTools.getIndex(FormatAdapter.get(reader),
      reader.getSeries(), z, c, t);
  }

  /**
   * Gets the rasterized index corresponding
   * to the given Z, C and T coordinates.
   *
   * @param order Dimension order.
   * @param zSize Total number of focal planes.
   * @param cSize Total number of channels.
   * @param tSize Total number of time points.
   * @param num Total number of image planes (zSize * cSize * tSize),
   *   specified as a consistency check.
   * @param z Z coordinate of ZCT coordinate triple to convert to 1D index.
   * @param c C coordinate of ZCT coordinate triple to convert to 1D index.
   * @param t T coordinate of ZCT coordinate triple to convert to 1D index.
   */
  public static int getIndex(String order, int zSize, int cSize, int tSize,
    int num, int z, int c, int t)
  {
    return io.scif.util.FormatTools.getIndex(order, zSize, cSize, tSize, num, z, c, t);
  }


  /**
   * Gets the Z, C and T coordinates corresponding
   * to the given rasterized index value.
   */
  public static int[] getZCTCoords(IFormatReader reader, int index) {
//    new OldToNewAdapter(CONTEXT, reader);
    return io.scif.util.FormatTools.getZCTCoords(FormatAdapter.get(reader),
      reader.getSeries(), index);
  }

  /**
   * Gets the Z, C and T coordinates corresponding to the given rasterized
   * index value.
   *
   * @param order Dimension order.
   * @param zSize Total number of focal planes.
   * @param cSize Total number of channels.
   * @param tSize Total number of time points.
   * @param num Total number of image planes (zSize * cSize * tSize),
   *   specified as a consistency check.
   * @param index 1D (rasterized) index to convert to ZCT coordinate triple.
   */
  public static int[] getZCTCoords(String order,
    int zSize, int cSize, int tSize, int num, int index)
  {
    return io.scif.util.FormatTools.getZCTCoords(order, zSize, cSize, tSize, num, 0, index);
  }

  /**
   * Converts index from the given dimension order to the reader's native one.
   * This method is useful for shuffling the planar order around
   * (rather than eassigning ZCT sizes as {@link DimensionSwapper} does).
   *
   * @throws FormatException Never actually thrown.
   */
  public static int getReorderedIndex(IFormatReader reader,
    String newOrder, int newIndex) throws FormatException
  {
   try {
    return io.scif.util.FormatTools.getReorderedIndex(FormatAdapter.get(reader),
      reader.getSeries(), newOrder, newIndex);
  }
  catch (io.scif.FormatException e) {
    throw new FormatException(e);
  }
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
    int zSize, int cSize, int tSize, int num, int newIndex)
  {
    return io.scif.util.FormatTools.getReorderedIndex(origOrder, newOrder, 
      zSize, cSize, tSize, num, 0, newIndex);
  }

  /**
   * Computes a unique 1-D index corresponding
   * to the given multidimensional position.
   * @param lengths the maximum value for each positional dimension
   * @param pos position along each dimensional axis
   * @return rasterized index value
   */
  public static int positionToRaster(int[] lengths, int[] pos) {
    return io.scif.util.FormatTools.positionToRaster(lengths, pos);
  }

  /**
   * Computes a unique N-D position corresponding
   * to the given rasterized index value.
   * @param lengths the maximum value at each positional dimension
   * @param raster rasterized index value
   * @return position along each dimensional axis
   */
  public static int[] rasterToPosition(int[] lengths, int raster) {
    return io.scif.util.FormatTools.rasterToPosition(lengths, raster);
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
    return io.scif.util.FormatTools.rasterToPosition(lengths, raster, pos);
  }

  /**
   * Computes the number of raster values for a positional array
   * with the given lengths.
   */
  public static int getRasterLength(int[] lengths) {
    return io.scif.util.FormatTools.getRasterLength(lengths);
  }

  // -- Utility methods - pixel types --

  /**
   * Takes a string value and maps it to one of the pixel type enumerations.
   * @param pixelTypeAsString the pixel type as a string.
   * @return type enumeration value for use with class constants.
   */
  public static int pixelTypeFromString(String pixelTypeAsString) {
    return io.scif.util.FormatTools.pixelTypeFromString(pixelTypeAsString);
  }

  /**
   * Takes a pixel type value and gets a corresponding string representation.
   * @param pixelType the pixel type.
   * @return string value for human-readable output.
   */
  public static String getPixelTypeString(int pixelType) {
    return io.scif.util.FormatTools.getPixelTypeString(pixelType);
  }

  /**
   * Retrieves how many bytes per pixel the current plane or section has.
   * @param pixelType the pixel type as retrieved from
   *   {@link IFormatReader#getPixelType()}.
   * @return the number of bytes per pixel.
   * @see IFormatReader#getPixelType()
   */
  public static int getBytesPerPixel(int pixelType) {
    return io.scif.util.FormatTools.getBytesPerPixel(pixelType);
  }

  /**
   * Retrieves the number of bytes per pixel in the current plane.
   * @param pixelType the pixel type, as a String.
   * @return the number of bytes per pixel.
   * @see #pixelTypeFromString(String)
   * @see #getBytesPerPixel(int)
   */
  public static int getBytesPerPixel(String pixelType) {
    return io.scif.util.FormatTools.getBytesPerPixel(pixelType);
  }

  /**
   * Determines whether the given pixel type is floating point or integer.
   * @param pixelType the pixel type as retrieved from
   *   {@link IFormatReader#getPixelType()}.
   * @return true if the pixel type is floating point.
   * @see IFormatReader#getPixelType()
   */
  public static boolean isFloatingPoint(int pixelType) {
    return io.scif.util.FormatTools.isFloatingPoint(pixelType);
  }

  /**
   * Determines whether the given pixel type is signed or unsigned.
   * @param pixelType the pixel type as retrieved from
   *   {@link IFormatReader#getPixelType()}.
   * @return true if the pixel type is signed.
   * @see IFormatReader#getPixelType()
   */
  public static boolean isSigned(int pixelType) {
    return io.scif.util.FormatTools.isSigned(pixelType);
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
    try {
      return io.scif.util.FormatTools.pixelTypeFromBytes(bytes, signed, fp);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
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
    io.scif.util.FormatTools.assertId(currentId, notNull, depth);
  }

  /**
   * Convenience method for checking that the plane number, tile size and
   * buffer sizes are all valid for the given reader.
   * If 'bufLength' is less than 0, then the buffer length check is not
   * performed.
   */
  public static void checkPlaneParameters(IFormatReader r, int no,
    int bufLength, int x, int y, int w, int h) throws FormatException
  {
    try {
      io.scif.util.FormatTools.checkPlaneParameters(FormatAdapter.get(r),
        r.getSeries(), no, bufLength, x, y, w, h);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /** Checks that the given plane number is valid for the given reader. */
  public static void checkPlaneNumber(IFormatReader r, int no)
    throws FormatException
  {
    try {
      io.scif.util.FormatTools.checkPlaneNumber(FormatAdapter.get(r),
        r.getSeries(), no);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /** Checks that the given tile size is valid for the given reader. */
  public static void checkTileSize(IFormatReader r, int x, int y, int w, int h)
    throws FormatException
  {
    try {
      io.scif.util.FormatTools.checkTileSize(FormatAdapter.get(r),
        x, y, w, h, r.getSeries());
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  public static void checkBufferSize(IFormatReader r, int len)
    throws FormatException
  {
    try {
      io.scif.util.FormatTools.checkBufferSize(r.getSeries(), FormatAdapter.get(r), len);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /**
   * Checks that the given buffer size is large enough to hold a w * h
   * image as returned by the given reader.
   * @throws FormatException if the buffer is too small
   */
  public static void checkBufferSize(IFormatReader r, int len, int w, int h)
    throws FormatException
  {
    try {
      io.scif.util.FormatTools.checkBufferSize(FormatAdapter.get(r),
          len, w, h, r.getSeries());
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /**
   * Returns true if the given RandomAccessInputStream conatins at least
   * 'len' bytes.
   */
  public static boolean validStream(RandomAccessInputStream stream, int len,
    boolean littleEndian) throws IOException
  {
    //TODO can not fully read the stream as that would be bad.. need a better way of converting.
//     Also this is destructive as it changes the position of the passed stream
    return io.scif.util.FormatTools.validStream(CommonAdapter.get(stream), len, littleEndian);
  }

  /** Returns the size in bytes of a single plane. */
  public static int getPlaneSize(IFormatReader r) {
    return io.scif.util.FormatTools.getPlaneSize(FormatAdapter.get(r),
      r.getSeries());
  }

  /** Returns the size in bytes of a w * h tile. */
  public static int getPlaneSize(IFormatReader r, int w, int h) {
    return io.scif.util.FormatTools.getPlaneSize(FormatAdapter.get(r),
      w, h, r.getSeries());
  }

  // -- Utility methods -- export

  /**
   * @throws FormatException Never actually thrown.
   * @throws IOException Never actually thrown.
   */
  public static String getFilename(int series, int image, IFormatReader r,
    String pattern) throws FormatException, IOException
  {
    try {
      return io.scif.util.FormatTools.getFilename(series, image,
          FormatAdapter.get(r), pattern);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  public static String[] getFilenames(String pattern, IFormatReader r)
    throws FormatException, IOException
  {
    try {
      return io.scif.util.FormatTools.getFilenames(pattern,
          FormatAdapter.get(r));
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  public static int getImagesPerFile(String pattern, IFormatReader r)
    throws FormatException, IOException
  {
    try {
      return io.scif.util.FormatTools.getImagesPerFile(pattern,
          FormatAdapter.get(r));
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  // -- Utility methods -- other

  /**
   * Recursively look for the first underlying reader that is an
   * instance of the given class.
   */
  public static IFormatReader getReader(IFormatReader r,
    Class<? extends IFormatReader> c)
  {
    IFormatReader[] underlying = r.getUnderlyingReaders();
    if (underlying != null) {
      for (int i=0; i<underlying.length; i++) {
        if (underlying[i].getClass().isInstance(c)) return underlying[i];
      }
      for (int i=0; i<underlying.length; i++) {
        IFormatReader t = getReader(underlying[i], c);
        if (t != null) return t;
      }
    }
    return null;
  }

  /**
   * Default implementation for {@link IFormatReader#openThumbBytes}.
   *
   * At the moment, it uses {@link java.awt.image.BufferedImage} objects
   * to resize thumbnails, so it is not safe for use in headless contexts.
   * In the future, we may reimplement the image scaling logic purely with
   * byte arrays, but handling every case would be substantial effort, so
   * doing so is currently a low priority item.
   */
  public static byte[] openThumbBytes(IFormatReader reader, int no)
    throws FormatException, IOException
  {
    try {
      return io.scif.util.FormatTools.openThumbBytes(
          FormatAdapter.get(reader),
          reader.getSeries(), no);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  // -- Conversion convenience methods --

  /**
   * Convenience method for converting the specified input file to the
   * specified output file.  The ImageReader and ImageWriter classes are used
   * for input and output, respectively.  To use other IFormatReader or
   * IFormatWriter implementation,
   * @see convert(IFormatReader, IFormatWriter, String).
   *
   * @param input the full path name of the existing input file
   * @param output the full path name of the output file to be created
   * @throws FormatException if there is a general problem reading from or
   * writing to one of the files.
   * @throws IOException if there is an I/O-related error.
   */
  public static void convert(String input, String output)
    throws FormatException, IOException
  {
    IFormatReader reader = new ImageReader();
    try {
      ServiceFactory factory = new ServiceFactory();
      OMEXMLService service = factory.getInstance(OMEXMLService.class);
      reader.setMetadataStore(service.createOMEXMLMetadata());
    }
    catch (DependencyException de) {
      throw new MissingLibraryException(OMEXMLServiceImpl.NO_OME_XML_MSG, de);
    }
    catch (ServiceException se) {
      throw new FormatException(se);
    }
    finally {
      reader.close();
    }
    
    reader.setId(input);

    IFormatWriter writer = new ImageWriter();

    convert(reader, writer, output);
  }

  /**
   * Convenience method for writing all of the images and metadata obtained
   * from the specified IFormatReader into the specified IFormatWriter.
   *
   * It is required that setId(String) be called on the IFormatReader
   * object before it is passed to convert(...).  setMetadataStore(...)
   * should also have been called with an appropriate instance of IMetadata.
   *
   * The setId(String) method must not be called on the IFormatWriter
   * object; this is taken care of internally.  Additionally, the
   * setMetadataRetrieve(...) method in IFormatWriter should not be called.
   *
   * @param input the pre-initialized IFormatReader used for reading data.
   * @param output the uninitialized IFormatWriter used for writing data.
   * @param outputFile the full path name of the output file to be created.
   * @throws FormatException if there is a general problem reading from or
   * writing to one of the files.
   * @throws IOException if there is an I/O-related error.
   */
  public static void convert(IFormatReader input, IFormatWriter output,
    String outputFile)
    throws FormatException, IOException
  {
    MetadataStore store = input.getMetadataStore();
    MetadataRetrieve meta = null;
    try {
      ServiceFactory factory = new ServiceFactory();
      OMEXMLService service = factory.getInstance(OMEXMLService.class);
      meta = service.asRetrieve(store);
    }
    catch (DependencyException de) {
      throw new MissingLibraryException(OMEXMLServiceImpl.NO_OME_XML_MSG, de);
    }

    output.setMetadataRetrieve(meta);
    output.setId(outputFile);

    for (int series=0; series<input.getSeriesCount(); series++) {
      input.setSeries(series);
      output.setSeries(series);

      byte[] buf = new byte[getPlaneSize(input)];

      for (int image=0; image<input.getImageCount(); image++) {
        input.openBytes(image, buf);
        output.saveBytes(image, buf);
      }
    }

    input.close();
    output.close();
  }

  public static long[] defaultMinMax(int pixelType) {
    return io.scif.util.FormatTools.defaultMinMax(pixelType);
  }
}
