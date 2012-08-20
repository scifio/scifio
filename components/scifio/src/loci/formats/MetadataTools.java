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

import java.util.Hashtable;
import java.util.Map;

import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import ome.scifio.services.DependencyException;
import ome.scifio.services.ServiceException;
import ome.scifio.services.ServiceFactory;


/**
 * A utility class for working with metadata objects,
 * including {@link MetadataStore}, {@link MetadataRetrieve},
 * and OME-XML strings.
 * Most of the methods require the optional {@link loci.formats.ome}
 * package, and optional ome-xml.jar library, to be present at runtime.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MetadataTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MetadataTools.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public final class MetadataTools {

  // -- Constants --
  
  // -- Static fields --

  // -- Constructor --

  private MetadataTools() { }

  // -- Utility methods - OME-XML --

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r) {
    ome.xml.meta.MetadataTools.populatePixels(store, r);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r,
    boolean doPlane)
  {
    ome.xml.meta.MetadataTools.populatePixels(store, r, doPlane);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   * If the 'doImageName' flag is set, then the image name will be populated
   * as well.  By default, 'doImageName' is true.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r,
    boolean doPlane, boolean doImageName)
  {
    ome.xml.meta.MetadataTools.populatePixels(store, r, doPlane, doImageName);
  }

  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int series,
    String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    ome.xml.meta.MetadataTools.populateMetadata(store, series, imageName, 
      littleEndian, dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, 
      sizeT, samplesPerPixel);
  }
  
  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the values from the provided {@link CoreMetadata}.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int series,
    String imageName, loci.formats.CoreMetadata coreMeta)
  {
    ome.xml.meta.MetadataTools.populateMetadata(store, series, imageName, coreMeta);
  }
  
  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, String file,
    int series, String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    ome.xml.meta.MetadataTools.populateMetadata(store, file, series, imageName,
      littleEndian, dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT,
      samplesPerPixel);
  }

  public static void populatePixelsOnly(MetadataStore store, IFormatReader r) {
    ome.xml.meta.MetadataTools.populatePixelsOnly(store, r);
  }

  public static void populatePixelsOnly(MetadataStore store, int series,
    boolean littleEndian, String dimensionOrder, String pixelType, int sizeX,
    int sizeY, int sizeZ, int sizeC, int sizeT, int samplesPerPixel)
  {
    ome.xml.meta.MetadataTools.populatePixelsOnly(store, series, littleEndian,
      dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT, 
      samplesPerPixel);
  }

  /**
   * Constructs an LSID, given the object type and indices.
   * For example, if the arguments are "Detector", 1, and 0, the LSID will
   * be "Detector:1:0".
   */
  public static String createLSID(String type, int... indices) {
    return ome.xml.meta.MetadataTools.createLSID(type, indices);
  }

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(MetadataRetrieve src)
    throws FormatException
  {
    try {
      ome.xml.meta.MetadataTools.verifyMinimumPopulated(src);
    }
    catch (ome.scifio.FormatException e) {
      throw (FormatException)e;
    }
  }

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(MetadataRetrieve src, int n)
    throws FormatException
  {
    try {
      ome.xml.meta.MetadataTools.verifyMinimumPopulated(src, n);
    }
    catch (ome.scifio.FormatException e) {
      throw (FormatException)e;
    }
  }

  /**
   * Disables the setting of a default creation date.
   *
   * By default, missing creation dates will be replaced with the corresponding
   * file's last modification date, or the current time if the modification
   * date is not available.
   *
   * Calling this method with the 'enabled' parameter set to 'false' causes
   * missing creation dates to be left as null.
   *
   * @param enabled See above.
   * @see #setDefaultCreationDate(MetadataStore, String, int)
   */
  public static void setDefaultDateEnabled(boolean enabled) {
    ome.xml.meta.MetadataTools.setDefaultDateEnabled(enabled);
  }

  /**
   * Sets a default creation date.  If the named file exists, then the creation
   * date is set to the file's last modification date.  Otherwise, it is set
   * to the current date.
   *
   * @see #setDefaultDateEnabled(boolean)
   */
  public static void setDefaultCreationDate(MetadataStore store, String id,
    int series)
  {
    ome.xml.meta.MetadataTools.setDefaultCreationDate(store, id, series);
  }

  /**
   * Adjusts the given dimension order as needed so that it contains exactly
   * one of each of the following characters: 'X', 'Y', 'Z', 'C', 'T'.
   */
  public static String makeSaneDimensionOrder(String dimensionOrder) {
    return ome.xml.meta.OMEXMLMetadataTools.makeSaneDimensionOrder(dimensionOrder);
  }

  // -- Utility methods - original metadata --

  /** Gets a sorted list of keys from the given hashtable. */
  public static String[] keys(Hashtable<String, Object> meta) {
    return ome.scifio.util.SCIFIOMetadataTools.keys(meta);
  }

  /**
   * Merges the given lists of metadata, prepending the
   * specified prefix for the destination keys.
   */
  public static void merge(Map<String, Object> src, Map<String, Object> dest,
    String prefix)
  {
    ome.scifio.util.SCIFIOMetadataTools.merge(src, dest, prefix);
  }

  // -- Deprecated methods --

  private static OMEXMLService omexmlService = createOMEXMLService();
  private static OMEXMLService createOMEXMLService() {
    try {
      return new ServiceFactory().getInstance(OMEXMLService.class);
    }
    catch (DependencyException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#asRetrieve(MetadataStore)}
   * instead.
   */
  public static MetadataRetrieve asRetrieve(MetadataStore meta) {
    if (omexmlService == null) return null;
    return omexmlService.asRetrieve(meta);
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#getLatestVersion()}
   * instead.
   */
  public static String getLatestVersion() {
    if (omexmlService == null) return null;
    return omexmlService.getLatestVersion();
  }

  /**
   * Creates an OME-XML metadata object using reflection, to avoid
   * direct dependencies on the optional {@link loci.formats.ome} package.
   * @return A new instance of {@link loci.formats.ome.AbstractOMEXMLMetadata},
   *   or null if one cannot be created.
   */
  public static IMetadata createOMEXMLMetadata() {
    try {
      final OMEXMLService service =
        new ServiceFactory().getInstance(OMEXMLService.class);
      if (service == null) return null;
      return service.createOMEXMLMetadata();
    }
    catch (DependencyException exc) {
      return null;
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#createOMEXMLMetadata(String)}
   * instead.
   */
  public static IMetadata createOMEXMLMetadata(String xml) {
    if (omexmlService == null) return null;
    try {
      return omexmlService.createOMEXMLMetadata(xml);
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#createOMEXMLMetadata(String,
   *   String)}
   * instead.
   */
  public static IMetadata createOMEXMLMetadata(String xml, String version) {
    if (omexmlService == null) return null;
    try {
      return omexmlService.createOMEXMLMetadata(xml);
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#createOMEXMLRoot(String)}
   * instead.
   */
  public static Object createOMEXMLRoot(String xml) {
    if (omexmlService == null) return null;
    try {
      return omexmlService.createOMEXMLMetadata(xml);
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#isOMEXMLMetadata(Object)}
   * instead.
   */
  public static boolean isOMEXMLMetadata(Object o) {
    if (omexmlService == null) return false;
    return omexmlService.isOMEXMLMetadata(o);
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#isOMEXMLRoot(Object)}
   * instead.
   */
  public static boolean isOMEXMLRoot(Object o) {
    if (omexmlService == null) return false;
    return omexmlService.isOMEXMLRoot(o);
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#getOMEXMLVersion(Object)}
   * instead.
   */
  public static String getOMEXMLVersion(Object o) {
    if (omexmlService == null) return null;
    return omexmlService.getOMEXMLVersion(o);
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#getOMEMetadata(MetadataRetrieve)}
   * instead.
   */
  public static IMetadata getOMEMetadata(MetadataRetrieve src) {
    if (omexmlService == null) return null;
    try {
      return omexmlService.getOMEMetadata(src);
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#getOMEXML(MetadataRetrieve)}
   * instead.
   */
  public static String getOMEXML(MetadataRetrieve src) {
    if (omexmlService == null) return null;
    try {
      return omexmlService.getOMEXML(src);
    }
    catch (ServiceException exc) {
      return null;
    }
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#validateOMEXML(String)}
   * instead.
   */
  public static boolean validateOMEXML(String xml) {
    if (omexmlService == null) return false;
    return omexmlService.validateOMEXML(xml);
  }

  /**
   * @deprecated This method is not thread-safe; use
   * {@link loci.formats.services.OMEXMLService#validateOMEXML(String, boolean)}
   * instead.
   */
  public static boolean validateOMEXML(String xml, boolean pixelsHack) {
    if (omexmlService == null) return false;
    return omexmlService.validateOMEXML(xml, pixelsHack);
  }

}
