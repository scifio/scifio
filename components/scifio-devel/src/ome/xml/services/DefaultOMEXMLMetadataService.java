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

package ome.xml.services;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.common.DateTools;
import io.scif.io.Location;
import io.scif.services.FormatService;
import io.scif.services.ServiceException;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;
import net.imglib2.meta.Axes;
import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.MetadataStore;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.BinData;
import ome.xml.model.OME;
import ome.xml.model.enums.Binning;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.PixelType;
import ome.xml.model.enums.handlers.BinningEnumHandler;
import ome.xml.model.enums.handlers.CorrectionEnumHandler;
import ome.xml.model.enums.handlers.DetectorTypeEnumHandler;
import ome.xml.model.enums.handlers.ExperimentTypeEnumHandler;
import ome.xml.model.enums.handlers.ImmersionEnumHandler;
import ome.xml.model.enums.handlers.LaserMediumEnumHandler;
import ome.xml.model.enums.handlers.LaserTypeEnumHandler;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.NonNegativeLong;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link OMEXMLMetadataService}.
 */
@Plugin(type = Service.class)
public class DefaultOMEXMLMetadataService extends AbstractService
  implements OMEXMLMetadataService {
  
  // -- Constants --
  
  private static final Logger LOGGER =
    LoggerFactory.getLogger(DefaultOMEXMLMetadataService.class);
  
  // -- Static fields --

  private boolean defaultDateEnabled = true;
  
  // -- Parameters --
  
  @Parameter
  private FormatService formatService;
  
  // -- Utility methods - OME-XML --

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populatePixels(ome.xml.meta.MetadataStore, io.scif.Metadata)
   */
  public void populatePixels(MetadataStore store, Metadata meta) {
    populatePixels(store, meta, false, true);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populatePixels(ome.xml.meta.MetadataStore, io.scif.Metadata, boolean)
   */
  public void populatePixels(MetadataStore store, Metadata meta,
    boolean doPlane)
  {
    populatePixels(store, meta, doPlane, true);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populatePixels(ome.xml.meta.MetadataStore, io.scif.Metadata, boolean, boolean)
   */
  public void populatePixels(MetadataStore store, Metadata meta,
    boolean doPlane, boolean doImageName)
  {
    if (store == null || meta == null) return;
    for (int i=0; i<meta.getImageCount(); i++) {

      String imageName = null;
      if (doImageName) {
        Location f = new Location(getContext(), meta.getDatasetName());
        imageName = f.getName();
      }
      String pixelType = FormatTools.getPixelTypeString(meta.getPixelType(i));
      String order = FormatTools.findDimensionOrder(meta, i);
      
      int xSize = meta.getAxisLength(i, Axes.X);
      int ySize = meta.getAxisLength(i, Axes.Y);
      int zSize = meta.getAxisLength(i, Axes.Z);
      int cSize = meta.getAxisLength(i, Axes.CHANNEL);
      int tSize = meta.getAxisLength(i, Axes.TIME);
      
      populateMetadata(store, meta.getDatasetName(), i, imageName,
        meta.isLittleEndian(i), order, pixelType, xSize,
        ySize, zSize, cSize, tSize,
        meta.getRGBChannelCount(i));

      OMEXMLService service = formatService.getInstance(OMEXMLService.class);
      if (service.isOMEXMLRoot(store.getRoot())) {
        //TODO any way or reason to access a base store?
        if(service.isOMEXMLMetadata(store)) {
          OMEXMLMetadata omeMeta;
          try {
            omeMeta = service.getOMEMetadata(service.asRetrieve(store));
            omeMeta.resolveReferences();
          }
          catch (ServiceException e) {
            LOGGER.warn("Failed to resolve references", e);
          }
        }

        OME root = (OME) store.getRoot();
        BinData bin = root.getImage(i).getPixels().getBinData(0);
        bin.setLength(new NonNegativeLong(0L));
        store.setRoot(root);
      }

      if (doPlane) {
        for (int q=0; q<meta.getPlaneCount(i); q++) {
          int[] coords = FormatTools.getZCTCoords(meta, i, q);
          store.setPlaneTheZ(new NonNegativeInteger(coords[0]), i, q);
          store.setPlaneTheC(new NonNegativeInteger(coords[1]), i, q);
          store.setPlaneTheT(new NonNegativeInteger(coords[2]), i, q);
        }
      }
    }
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#populateMetadata(
   * ome.xml.meta.MetadataStore, int, java.lang.String, boolean, 
   * java.lang.String, java.lang.String, int, int, int, int, int, int)
   */
  public void populateMetadata(MetadataStore store, int imageIndex,
    String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    populateMetadata(store, null, imageIndex, imageName, littleEndian,
      dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT,
      samplesPerPixel);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#populateMetadata(
   * ome.xml.meta.MetadataStore, int, java.lang.String, io.scif.Metadata)
   */
  public void populateMetadata(MetadataStore store, int imageIndex,
    String imageName, Metadata meta)
  {
    
    int sizeX = meta.getAxisLength(imageIndex, Axes.X);
    int sizeY = meta.getAxisLength(imageIndex, Axes.Y);
    int sizeZ = meta.getAxisLength(imageIndex, Axes.Z);
    int sizeC = meta.getAxisLength(imageIndex, Axes.CHANNEL);
    int sizeT = meta.getAxisLength(imageIndex, Axes.TIME);
    
    final String pixelType = 
      FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
    final int effSizeC = meta.getPlaneCount(imageIndex) / sizeZ / sizeT;
    final int samplesPerPixel = sizeC / effSizeC;
    populateMetadata(store, null, imageIndex, imageName, 
      meta.isLittleEndian(imageIndex), 
      FormatTools.findDimensionOrder(meta, imageIndex), pixelType,
      sizeX, sizeY, sizeZ, sizeC, sizeT, samplesPerPixel);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populateMetadata(ome.xml.meta.MetadataStore,
   * java.lang.String, int, java.lang.String, boolean,
   * java.lang.String, java.lang.String, int, int, int, int, int, int)
   */
  public void populateMetadata(MetadataStore store, String file,
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

  /*
   * @see ome.xml.services.OMEXMLMetadataService#populatePixelsOnly(
   * ome.xml.meta.MetadataStore, io.scif.Reader)
   */
  public void populatePixelsOnly(MetadataStore store, Reader r) {
    Metadata dMeta = r.getMetadata();

    for (int imageIndex=0; imageIndex<r.getImageCount(); imageIndex++) {
      String pixelType = FormatTools.getPixelTypeString(dMeta.getPixelType(imageIndex));

      populatePixelsOnly(store, imageIndex, dMeta.isLittleEndian(imageIndex), 
        FormatTools.findDimensionOrder(dMeta, imageIndex), pixelType, 
        dMeta.getAxisLength(imageIndex, Axes.X), dMeta.getAxisLength(imageIndex, Axes.Y),
        dMeta.getAxisLength(imageIndex, Axes.Z), dMeta.getAxisLength(imageIndex, Axes.CHANNEL),
        dMeta.getAxisLength(imageIndex, Axes.TIME), dMeta.getRGBChannelCount(imageIndex));
    }
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#populatePixelsOnly(
   * ome.xml.meta.MetadataStore, int, boolean, java.lang.String, 
   * java.lang.String, int, int, int, int, int, int)
   */
  public void populatePixelsOnly(MetadataStore store, int imageIndex,
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

  /*
   * @see ome.xml.services.OMEXMLMetadataService#setDefaultDateEnabled(boolean)
   */
  public void setDefaultDateEnabled(boolean enabled) {
    defaultDateEnabled = enabled;
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * setDefaultCreationDate(ome.xml.meta.MetadataStore, java.lang.String, int)
   */
  public void setDefaultCreationDate(MetadataStore store, String id,
    int imageIndex)
  {
    if (!defaultDateEnabled) {
      return;
    }
    Location file = id == null ? null : new Location(getContext(), id).getAbsoluteFile();
    long time = System.currentTimeMillis();
    if (file != null && file.exists()) time = file.lastModified();
    store.setImageAcquisitionDate(new Timestamp(DateTools.convertDate(
      time, DateTools.UNIX)), imageIndex);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * verifyMinimumPopulated(ome.xml.meta.MetadataRetrieve)
   */
  public void verifyMinimumPopulated(MetadataRetrieve src)
    throws FormatException
  {
    verifyMinimumPopulated(src, 0);
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * verifyMinimumPopulated(ome.xml.meta.MetadataRetrieve, int)
   */
  public void verifyMinimumPopulated(MetadataRetrieve src, int n)
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

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * makeSaneDimensionOrder(java.lang.String)
   */
  public String makeSaneDimensionOrder(String dimensionOrder) {
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

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * createLSID(java.lang.String, int[])
   */
  public String createLSID(String type, int... indices) {
    StringBuffer lsid = new StringBuffer(type);
    for (int index : indices) {
      lsid.append(":");
      lsid.append(index);
    }
    return lsid.toString();
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * getExperimentType(java.lang.String)
   */
  public ExperimentType getExperimentType(String value)
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

  /*
   * @see ome.xml.services.OMEXMLMetadataService#getLaserType(java.lang.String)
   */
  public LaserType getLaserType(String value) throws FormatException {
    LaserTypeEnumHandler handler = new LaserTypeEnumHandler();
    try {
      return (LaserType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserType creation failed", e);
    }
  }

  /*
   * @see ome.xml.services.OMEXMLMetadataService#getLaserMedium(java.lang.String)
   */
  public LaserMedium getLaserMedium(String value) throws FormatException {
    LaserMediumEnumHandler handler = new LaserMediumEnumHandler();
    try {
      return (LaserMedium) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserMedium creation failed", e);
    }
  }
  
  /*
   * @see ome.xml.services.OMEXMLMetadataService#getImmersion(java.lang.String)
   */
  public Immersion getImmersion(String value) throws FormatException {
    ImmersionEnumHandler handler = new ImmersionEnumHandler();
    try {
      return (Immersion) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Immersion creation failed", e);
    }
  }
    
  /*
   * @see ome.xml.services.OMEXMLMetadataService#getCorrection(java.lang.String)
   */
  public Correction getCorrection(String value) throws FormatException {
    CorrectionEnumHandler handler = new CorrectionEnumHandler();
    try {
      return (Correction) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Correction creation failed", e);
    }
  }
  
  /*
   * @see ome.xml.services.OMEXMLMetadataService#getDetectorType(java.lang.String)
   */
  public DetectorType getDetectorType(String value) throws FormatException {
    DetectorTypeEnumHandler handler = new DetectorTypeEnumHandler();
    try {
      return (DetectorType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("DetectorType creation failed", e);
    }
  }
  
  /*
   * @see ome.xml.services.OMEXMLMetadataService#getBinning(java.lang.String)
   */
  public Binning getBinning(String value) throws FormatException {
    BinningEnumHandler handler = new BinningEnumHandler();
    try {
      return (Binning) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Binning creation failed", e);
    }
  }
  
  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populateMetadata(ome.xml.meta.MetadataRetrieve, io.scif.Metadata)
   */
  public void populateMetadata(MetadataRetrieve retrieve, Metadata meta) {
    
    int numImages = retrieve.getImageCount();
    
    if (numImages > 0) meta.setDatasetName(retrieve.getImageName(0));
    
    meta.createImageMetadata(numImages);
    
    for (int i=0; i<numImages; i++) {
      populateImageMetadata(retrieve, i, meta.get(i));
    }
  }
  
  /*
   * @see ome.xml.services.OMEXMLMetadataService#
   * populateImageMetadata(ome.xml.meta.MetadataRetrieve, int, io.scif.ImageMetadata)
   */
  public void populateImageMetadata(MetadataRetrieve retrieve,
      int imageIndex, ImageMetadata iMeta)
  {
    int sizeX = retrieve.getPixelsSizeX(imageIndex).getValue();
    int sizeY = retrieve.getPixelsSizeY(imageIndex).getValue();
    int sizeZ = retrieve.getPixelsSizeZ(imageIndex).getValue();
    int sizeC = retrieve.getPixelsSizeC(imageIndex).getValue();
    int sizeT = retrieve.getPixelsSizeT(imageIndex).getValue();
    
    String dimensionOrder = retrieve.getPixelsDimensionOrder(imageIndex).getValue();
    boolean little = !retrieve.getPixelsBinDataBigEndian(imageIndex, 0);
    String pixelType = retrieve.getPixelsType(imageIndex).getValue();
    
    PositiveInteger spp = retrieve.getChannelCount(imageIndex) <= 0 ? null :
                          retrieve.getChannelSamplesPerPixel(imageIndex, 0);
    
    int rgbCCount = spp == null ? 1 : spp.getValue();
    
    SCIFIOMetadataTools.populateDimensions(iMeta, dimensionOrder,
        sizeX, sizeY, sizeZ, sizeC, sizeT);
    
    iMeta.setLittleEndian(little);
    iMeta.setPixelType(FormatTools.pixelTypeFromString(pixelType));
    iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));
    if (rgbCCount > 1) iMeta.setRGB(true);
    
    iMeta.setPlaneCount(sizeZ * (sizeC / rgbCCount) * sizeT);
  }
}
