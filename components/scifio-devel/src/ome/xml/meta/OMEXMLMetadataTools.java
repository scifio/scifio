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

package ome.xml.meta;

import net.imglib2.meta.Axes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.DatasetMetadata;
import ome.scifio.FormatException;
import ome.scifio.Reader;
import ome.scifio.common.DateTools;
import ome.scifio.io.Location;
import ome.scifio.services.DependencyException;
import ome.scifio.services.ServiceFactory;
import ome.scifio.util.FormatTools;
import ome.xml.model.*;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.PixelType;
import ome.xml.model.enums.handlers.CorrectionEnumHandler;
import ome.xml.model.enums.handlers.DetectorTypeEnumHandler;
import ome.xml.model.enums.handlers.ExperimentTypeEnumHandler;
import ome.xml.model.enums.handlers.ImmersionEnumHandler;
import ome.xml.model.enums.handlers.LaserMediumEnumHandler;
import ome.xml.model.enums.handlers.LaserTypeEnumHandler;
import ome.xml.model.primitives.*;
import ome.xml.services.OMEXMLService;

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
public class OMEXMLMetadataTools {
  
  // -- Constants --
  
  private static final Logger LOGGER =
    LoggerFactory.getLogger(OMEXMLMetadataTools.class);
  
  // -- Static fields --

  private static boolean defaultDateEnabled = true;
  
  // -- Utility methods - OME-XML --

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.
   */
  public static void populatePixels(MetadataStore store, DatasetMetadata meta) {
    populatePixels(store, meta, false, true);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   */
  public static void populatePixels(MetadataStore store, DatasetMetadata meta,
    boolean doPlane)
  {
    populatePixels(store, meta, doPlane, true);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   * If the 'doImageName' flag is set, then the image name will be populated
   * as well.  By default, 'doImageName' is true.
   */
  public static void populatePixels(MetadataStore store, DatasetMetadata meta,
    boolean doPlane, boolean doImageName)
  {
    if (store == null || meta == null) return;
    for (int i=0; i<meta.getImageCount(); i++) {

      String imageName = null;
      if (doImageName) {
        Location f = new Location(meta.getSource().getFileName());
        imageName = f.getName();
      }
      String pixelType = FormatTools.getPixelTypeString(meta.getPixelType(i));
      String order = FormatTools.findDimensionOrder(meta, i);
      
      int xSize = meta.getAxisLength(i, Axes.X);
      int ySize = meta.getAxisLength(i, Axes.Y);
      int zSize = meta.getAxisLength(i, Axes.Z);
      int cSize = meta.getAxisLength(i, Axes.CHANNEL);
      int tSize = meta.getAxisLength(i, Axes.TIME);
      
      populateMetadata(store, meta.getSource().getFileName(), i, imageName,
        meta.isLittleEndian(i), order, pixelType, xSize,
        ySize, zSize, cSize, tSize,
        meta.getRGBChannelCount(i));

      try {
        OMEXMLService service =
          new ServiceFactory().getInstance(OMEXMLService.class);
        if (service.isOMEXMLRoot(store.getRoot())) {
          OME root = (OME) store.getRoot();
          BinData bin = root.getImage(i).getPixels().getBinData(0);
          bin.setLength(new NonNegativeLong(0L));
          store.setRoot(root);
        }
      }
      catch (DependencyException exc) {
        LOGGER.warn("Failed to set BinData.Length", exc);
      }

      if (doPlane) {
        for (int q=0; q<meta.getPlaneCount(i); q++) {
          int[] coords = FormatTools.getZCTCoords(order, zSize, cSize, tSize,
            meta.getPlaneCount(i), i, q);
          store.setPlaneTheZ(new NonNegativeInteger(coords[0]), i, q);
          store.setPlaneTheC(new NonNegativeInteger(coords[1]), i, q);
          store.setPlaneTheT(new NonNegativeInteger(coords[2]), i, q);
        }
      }
    }
  }

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int imageIndex,
    String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    populateMetadata(store, null, imageIndex, imageName, littleEndian,
      dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT,
      samplesPerPixel);
  }

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the values from the provided {@link DatasetMetadata}.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int imageIndex,
    String imageName, DatasetMetadata datasetMeta)
  {
    
    int sizeX = datasetMeta.getAxisLength(imageIndex, Axes.X);
    int sizeY = datasetMeta.getAxisLength(imageIndex, Axes.Y);
    int sizeZ = datasetMeta.getAxisLength(imageIndex, Axes.Z);
    int sizeC = datasetMeta.getAxisLength(imageIndex, Axes.CHANNEL);
    int sizeT = datasetMeta.getAxisLength(imageIndex, Axes.TIME);
    
    final String pixelType = 
      FormatTools.getPixelTypeString(datasetMeta.getPixelType(imageIndex));
    final int effSizeC = datasetMeta.getImageCount() / sizeZ / sizeT;
    final int samplesPerPixel = sizeC / effSizeC;
    populateMetadata(store, null, imageIndex, imageName, 
      datasetMeta.isLittleEndian(imageIndex), 
      FormatTools.findDimensionOrder(datasetMeta, imageIndex), pixelType,
      sizeX, sizeY, sizeZ, sizeC, sizeT, samplesPerPixel);
  }

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, String file,
    int imageIndex, String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    store.setImageID(createLSID("Image", imageIndex), imageIndex);
    setDefaultCreationDate(store, file, imageIndex);
    if (imageName != null) store.setImageName(imageName, imageIndex);
    populatePixelsOnly(store, imageIndex, littleEndian, dimensionOrder, pixelType,
      sizeX, sizeY, sizeZ, sizeC, sizeT, samplesPerPixel);
  }

  public static void populatePixelsOnly(MetadataStore store, Reader r) {
    DatasetMetadata dMeta = r.getDatasetMetadata();

    for (int imageIndex=0; imageIndex<r.getImageCount(); imageIndex++) {
      String pixelType = FormatTools.getPixelTypeString(dMeta.getPixelType(imageIndex));

      populatePixelsOnly(store, imageIndex, dMeta.isLittleEndian(imageIndex), 
        FormatTools.findDimensionOrder(dMeta, imageIndex), pixelType, 
        dMeta.getAxisLength(imageIndex, Axes.X), dMeta.getAxisLength(imageIndex, Axes.Y),
        dMeta.getAxisLength(imageIndex, Axes.Z), dMeta.getAxisLength(imageIndex, Axes.CHANNEL),
        dMeta.getAxisLength(imageIndex, Axes.TIME), dMeta.getRGBChannelCount(imageIndex));
    }
  }

  public static void populatePixelsOnly(MetadataStore store, int imageIndex,
    boolean littleEndian, String dimensionOrder, String pixelType, int sizeX,
    int sizeY, int sizeZ, int sizeC, int sizeT, int samplesPerPixel)
  {
    store.setPixelsID(createLSID("Pixels", imageIndex), imageIndex);
    store.setPixelsBinDataBigEndian(!littleEndian, imageIndex, 0);
    try {
      store.setPixelsDimensionOrder(
        DimensionOrder.fromString(dimensionOrder), imageIndex);
    }
    catch (EnumerationException e) {
      LOGGER.warn("Invalid dimension order: " + dimensionOrder, e);
    }
    try {
      store.setPixelsType(PixelType.fromString(pixelType), imageIndex);
    }
    catch (EnumerationException e) {
      LOGGER.warn("Invalid pixel type: " + pixelType, e);
    }
    store.setPixelsSizeX(new PositiveInteger(sizeX), imageIndex);
    store.setPixelsSizeY(new PositiveInteger(sizeY), imageIndex);
    store.setPixelsSizeZ(new PositiveInteger(sizeZ), imageIndex);
    store.setPixelsSizeC(new PositiveInteger(sizeC), imageIndex);
    store.setPixelsSizeT(new PositiveInteger(sizeT), imageIndex);
    int effSizeC = sizeC / samplesPerPixel;
    for (int i=0; i<effSizeC; i++) {
      store.setChannelID(createLSID("Channel", imageIndex, i),
        imageIndex, i);
      store.setChannelSamplesPerPixel(new PositiveInteger(samplesPerPixel),
        imageIndex, i);
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
    defaultDateEnabled = enabled;
  }

  /**
   * Sets a default creation date.  If the named file exists, then the creation
   * date is set to the file's last modification date.  Otherwise, it is set
   * to the current date.
   *
   * @see #setDefaultDateEnabled(boolean)
   */
  public static void setDefaultCreationDate(MetadataStore store, String id,
    int imageIndex)
  {
    if (!defaultDateEnabled) {
      return;
    }
    Location file = id == null ? null : new Location(id).getAbsoluteFile();
    long time = System.currentTimeMillis();
    if (file != null && file.exists()) time = file.lastModified();
    store.setImageAcquisitionDate(new Timestamp(DateTools.convertDate(
      time, DateTools.UNIX)), imageIndex);
  }

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(MetadataRetrieve src)
    throws FormatException
    {
    verifyMinimumPopulated(src, 0);
    }

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(MetadataRetrieve src, int n)
    throws FormatException
    {
    if (src == null) {
      throw new FormatException("Metadata object is null; " +
        "call IFormatWriter.setMetadataRetrieve() first");
    }
    if (src instanceof MetadataStore
      && ((MetadataStore) src).getRoot() == null) {
      throw new FormatException("Metadata object has null root; " +
        "call IMetadata.createRoot() first");
    }
    if (src.getImageID(n) == null) {
      throw new FormatException("Image ID #" + n + " is null");
    }
    if (src.getPixelsID(n) == null) {
      throw new FormatException("Pixels ID #" + n + " is null");
    }
    for (int i=0; i<src.getChannelCount(n); i++) {
      if (src.getChannelID(n, i) == null) {
        throw new FormatException("Channel ID #" + i + " in Image #" + n +
          " is null");
      }
    }
    if (src.getPixelsBinDataBigEndian(n, 0) == null) {
      throw new FormatException("BigEndian #" + n + " is null");
    }
    if (src.getPixelsDimensionOrder(n) == null) {
      throw new FormatException("DimensionOrder #" + n + " is null");
    }
    if (src.getPixelsType(n) == null) {
      throw new FormatException("PixelType #" + n + " is null");
    }
    if (src.getPixelsSizeC(n) == null) {
      throw new FormatException("SizeC #" + n + " is null");
    }
    if (src.getPixelsSizeT(n) == null) {
      throw new FormatException("SizeT #" + n + " is null");
    }
    if (src.getPixelsSizeX(n) == null) {
      throw new FormatException("SizeX #" + n + " is null");
    }
    if (src.getPixelsSizeY(n) == null) {
      throw new FormatException("SizeY #" + n + " is null");
    }
    if (src.getPixelsSizeZ(n) == null) {
      throw new FormatException("SizeZ #" + n + " is null");
    }
    }


  /**
   * Adjusts the given dimension order as needed so that it contains exactly
   * one of each of the following characters: 'X', 'Y', 'Z', 'C', 'T'.
   */
  public static String makeSaneDimensionOrder(String dimensionOrder) {
    String order = dimensionOrder.toUpperCase();
    order = order.replaceAll("[^XYZCT]", "");
    String[] axes = new String[] {"X", "Y", "C", "Z", "T"};
    for (String axis : axes) {
      if (order.indexOf(axis) == -1) order += axis;
      while (order.indexOf(axis) != order.lastIndexOf(axis)) {
        order = order.replaceFirst(axis, "");
      }
    }
    return order;
  }

  /**
   * Constructs an LSID, given the object type and indices.
   * For example, if the arguments are "Detector", 1, and 0, the LSID will
   * be "Detector:1:0".
   */
  public static String createLSID(String type, int... indices) {
    StringBuffer lsid = new StringBuffer(type);
    for (int index : indices) {
      lsid.append(":");
      lsid.append(index);
    }
    return lsid.toString();
  }


  /**
   * Retrieves an {@link ome.xml.model.enums.ExperimentType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static ExperimentType getExperimentType(String value)
    throws FormatException
  {
    ExperimentTypeEnumHandler handler = new ExperimentTypeEnumHandler();
    try {
      return (ExperimentType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("ExperimentType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static LaserType getLaserType(String value) throws FormatException {
    LaserTypeEnumHandler handler = new LaserTypeEnumHandler();
    try {
      return (LaserType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserMedium} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static LaserMedium getLaserMedium(String value) throws FormatException {
    LaserMediumEnumHandler handler = new LaserMediumEnumHandler();
    try {
      return (LaserMedium) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserMedium creation failed", e);
    }
  }
  
  /**
   * Retrieves an {@link ome.xml.model.enums.Immersion} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static Immersion getImmersion(String value) throws FormatException {
    ImmersionEnumHandler handler = new ImmersionEnumHandler();
    try {
      return (Immersion) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Immersion creation failed", e);
    }
  }
    
  /**
   * Retrieves an {@link ome.xml.model.enums.Correction} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static Correction getCorrection(String value) throws FormatException {
    CorrectionEnumHandler handler = new CorrectionEnumHandler();
    try {
      return (Correction) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Correction creation failed", e);
    }
  }
  
  /**
   * Retrieves an {@link ome.xml.model.enums.DetectorType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  public static DetectorType getDetectorType(String value) throws FormatException {
    DetectorTypeEnumHandler handler = new DetectorTypeEnumHandler();
    try {
      return (DetectorType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("DetectorType creation failed", e);
    }
  }
}
