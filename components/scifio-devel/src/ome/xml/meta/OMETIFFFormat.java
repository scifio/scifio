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

package ome.xml.meta;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.DependencyException;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.SCIFIO;
import io.scif.Translator;
import io.scif.formats.MinimalTIFFFormat;
import io.scif.formats.TIFFFormat;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.PhotoInterp;
import io.scif.formats.tiff.TiffIFDEntry;
import io.scif.formats.tiff.TiffParser;
import io.scif.formats.tiff.TiffSaver;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.services.ServiceException;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;
import ome.xml.services.OMEXMLMetadataService;
import ome.xml.services.OMEXMLService;

import org.scijava.Context;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * OMETiffReader is the file format reader for
 * <a href="http://ome-xml.org/wiki/OmeTiff">OME-TIFF</a> files.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
@Plugin(type = OMETIFFFormat.class, priority = OMETIFFFormat.PRIORITY)
public class OMETIFFFormat extends AbstractFormat {
  
  // -- Constants --
  
  public static final double PRIORITY = TIFFFormat.PRIORITY + 1;
  
  // -- Fields --
  
  private static OMEXMLService service;
  private static OMEXMLMetadataService metaService;

  // -- Format API Methods --
  
  /*
   * @see io.scif.Format#getFormatName()
   */
  public String getFormatName() {
    return "OME-TIFF";
  }

  /*
   * @see io.scif.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[] {"ome.tif", "ome.tiff"};
  }
  
  // -- Nested Classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends TIFFFormat.Metadata {
    
    // -- Constants --
    
    public static final String CNAME = "ome.xml.meta.OMETIFFFormat$Metadata";
    
    // -- Fields --

    /** Mapping from series and plane numbers to files and IFD entries. */
    protected OMETIFFPlane[][] info; // dimensioned [numSeries][numPlanes]
    
    private IFD firstIFD;
    
    private List<Integer> samples;
    private List<Boolean> adjustedSamples;
    
    /** List of used files. */
    protected String[] used;
    
    // TODO maybe this should be an o mexmlmetadata...
    private OMEMetadata omeMeta;

    private int lastPlane = 0;
    private boolean hasSPW;

    private int[] tileWidth;
    private int[] tileHeight;
    
    // -- OMETIFF Metadata API methods --

    /**
     * Returns a MetadataStore that is populated in such a way as to
     * produce valid OME-XML.  The returned MetadataStore cannot be used
     * by an IFormatWriter, as it will not contain the required
     * BinData.BigEndian attributes.
     */
    public MetadataStore getMetadataStoreForDisplay() {
      MetadataStore store = omeMeta.getRoot();
      if (service.isOMEXMLMetadata(store)) {
        service.removeBinData((OMEXMLMetadata) store);
        for (int i=0; i<getImageCount(); i++) {
          if (((OMEXMLMetadata) store).getTiffDataCount(i) == 0) {
            service.addMetadataOnly((OMEXMLMetadata) store, i);
          }
        }
      }
      return store;
    }

    /**
     * Returns a MetadataStore that is populated in such a way as to be
     * usable by an IFormatWriter.  Any OME-XML generated from this
     * MetadataStore is <em>very unlikely</em> to be valid, as more than
     * likely both BinData and TiffData element will be present.
     */
    public MetadataStore getMetadataStoreForConversion() {
      MetadataStore store = omeMeta.getRoot();
      for (int i=0; i<getImageCount(); i++) {
        store.setPixelsBinDataBigEndian(new Boolean(!isLittleEndian(i)), i, 0);
      }
      return store;
    }

    // -- OMETIFFMetadata getters and setters --

    public OMETIFFPlane[][] getPlaneInfo() {
      return info;
    }

    public void setPlaneInfo(OMETIFFPlane[][] info) {
      this.info = info;
    }

    public String[] getUsed() {
      return used;
    }

    public void setUsed(String[] used) {
      this.used = used;
    }

    public OMEMetadata getOmeMeta() {
      return omeMeta;
    }

    public void setOmeMeta(OMEMetadata omeMeta) {
      this.omeMeta = omeMeta;
    }

    public int getLastPlane() {
      return lastPlane;
    }

    public void setLastPlane(int lastPlane) {
      this.lastPlane = lastPlane;
    }

    public IFD getFirstIFD() {
      return firstIFD;
    }

    public void setFirstIFD(IFD firstIFD) {
      this.firstIFD = firstIFD;
    }

    public boolean isHasSPW() {
      return hasSPW;
    }

    public void setHasSPW(boolean hasSPW) {
      this.hasSPW = hasSPW;
    }

    public int[] getTileWidth() {
      return tileWidth;
    }

    public void setTileWidth(int[] tileWidth) {
      this.tileWidth = tileWidth;
    }

    public int[] getTileHeight() {
      return tileHeight;
    }

    public void setTileHeight(int[] tileHeight) {
      this.tileHeight = tileHeight;
    }
    
    // -- Metadata API Methods --
    
    /*
     * @see io.scif.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {

      // populate core metadata
      
      OMEXMLMetadata omexmlMeta = getOmeMeta().getRoot();
      
      for (int s=0; s<getImageCount(); s++) {
        
        ImageMetadata m = get(s);
        try {
          m.setAxisLength(Axes.X, omexmlMeta.getPixelsSizeX(s).getValue().intValue());
          int tiffWidth = (int) firstIFD.getImageWidth();
          if (m.getAxisLength(Axes.X) != tiffWidth && s == 0) {
            LOGGER.warn("SizeX mismatch: OME={}, TIFF={}",
              m.getAxisLength(Axes.X), tiffWidth);
          }
          m.setAxisLength(Axes.Y, omexmlMeta.getPixelsSizeY(s).getValue().intValue());
          int tiffHeight = (int) firstIFD.getImageLength();
          if (m.getAxisLength(Axes.Y) != tiffHeight && s ==  0) {
            LOGGER.warn("SizeY mismatch: OME={}, TIFF={}",
              m.getAxisLength(Axes.Y), tiffHeight);
          }
          m.setAxisLength(Axes.Z, omexmlMeta.getPixelsSizeZ(s).getValue().intValue());
          m.setAxisLength(Axes.CHANNEL, omexmlMeta.getPixelsSizeC(s).getValue().intValue());
          m.setAxisLength(Axes.TIME, omexmlMeta.getPixelsSizeT(s).getValue().intValue());
          m.setPixelType(FormatTools.pixelTypeFromString(omexmlMeta.getPixelsType(s).toString()));
          int tiffPixelType = firstIFD.getPixelType();
          if (m.getPixelType() != tiffPixelType && (s == 0 || adjustedSamples.get(s))) {
            LOGGER.warn("PixelType mismatch: OME={}, TIFF={}",
              m.getPixelType(), tiffPixelType);
            m.setPixelType(tiffPixelType);
          }
          m.setBitsPerPixel(FormatTools.getBitsPerPixel(m.getPixelType()));
          m.setPlaneCount(info[s].length);
          String dimensionOrder = omexmlMeta.getPixelsDimensionOrder(s).toString();

          // hackish workaround for files exported by OMERO that have an
          // incorrect dimension order
          String uuidFileName = "";
          try {
            if (omexmlMeta.getTiffDataCount(s) > 0) {
              uuidFileName = omexmlMeta.getUUIDFileName(s, 0);
            }
          }
          catch (NullPointerException e) { }
          if (omexmlMeta.getChannelCount(s) > 0 && omexmlMeta.getChannelName(s, 0) == null &&
              omexmlMeta.getTiffDataCount(s) > 0 &&
            uuidFileName.indexOf("__omero_export") != -1)
          {
            dimensionOrder = "XYZCT";
          }
          m.setAxisTypes(FormatTools.findDimensionList(dimensionOrder));

          m.setOrderCertain(true);
          PhotoInterp photo = firstIFD.getPhotometricInterpretation();
          m.setRGB(samples.get(s) > 1 || photo == PhotoInterp.RGB);
          if ((samples.get(s) != m.getAxisLength(Axes.CHANNEL) && (samples.get(s) % m.getAxisLength(Axes.CHANNEL)) != 0 &&
            (m.getAxisLength(Axes.CHANNEL) % samples.get(s)) != 0) || m.getAxisLength(Axes.CHANNEL) == 1 ||
            adjustedSamples.get(s))
          {
            m.setAxisLength(Axes.CHANNEL, m.getAxisLength(Axes.CHANNEL) * samples.get(s));
          }

          if (m.getAxisLength(Axes.Z) * m.getAxisLength(Axes.TIME) * m.getAxisLength(Axes.CHANNEL) >
            m.getPlaneCount() && !m.isRGB())
          {
            if (m.getAxisLength(Axes.Z) == m.getPlaneCount()) {
              m.setAxisLength(Axes.TIME, 1);
              m.setAxisLength(Axes.CHANNEL, 1);
            }
            else if (m.getAxisLength(Axes.TIME) == m.getPlaneCount()) {
              m.setAxisLength(Axes.Z, 1);
              m.setAxisLength(Axes.CHANNEL, 1);
            }
            else if (m.getAxisLength(Axes.CHANNEL) == m.getPlaneCount()) {
              m.setAxisLength(Axes.TIME, 1);
              m.setAxisLength(Axes.Z, 1);
            }
          }

          if (omexmlMeta.getPixelsBinDataCount(s) > 1) {
            LOGGER.warn("OME-TIFF Pixels element contains BinData elements! " +
                        "Ignoring.");
          }
          m.setLittleEndian(firstIFD.isLittleEndian());
          m.setInterleaved(false);
          m.setIndexed(photo == PhotoInterp.RGB_PALETTE &&
            firstIFD.getIFDValue(IFD.COLOR_MAP) != null);
          if (m.isIndexed()) {
            m.setRGB(false);
          }
          m.setFalseColor(true);
          m.setMetadataComplete(true);
        }
        catch (NullPointerException exc) {
          LOGGER.error("Incomplete Pixels metadata", exc);
        } catch (FormatException exc) {
          LOGGER.error("Format exception when creating ImageMetadata", exc);
        }
      }

//      if (getImageCount() == 1) {
//        CoreMetadata ms0 = core.get(0);
//        ms0.sizeZ = 1;
//        if (!ms0.rgb) {
//          ms0.sizeC = 1;
//        }
//        ms0.sizeT = 1;
//      }
//      metaService.populatePixels(getOmeMeta().getRoot(), this, false, false);
      getOmeMeta().setRoot((OMEXMLMetadata) getMetadataStoreForConversion());
    }
   
    @Override
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (info != null) {
        for (OMETIFFPlane[] dimension : info) {
          for (OMETIFFPlane plane : dimension) {
            if (plane.reader != null) {
              try {
                plane.reader.close();
              }
              catch (Exception e) {
                LOGGER.error("Plane closure failure!", e);
              }
            }
          }
        }
      }
      if (!fileOnly) {
        info = null;
        lastPlane = 0;
        tileWidth = null;
        tileHeight = null;
      }
    }
    
    // -- HasColorTable API Methods --

    public ColorTable getColorTable(int imageIndex, int planeIndex) {
      if (info[imageIndex][lastPlane] == null ||
          info[imageIndex][lastPlane].reader == null ||
          info[imageIndex][lastPlane].id == null)
        {
          return null;
        }
        try {
          info[imageIndex][lastPlane].reader.setSource(info[imageIndex][lastPlane].id);
          return info[imageIndex][lastPlane].reader.getMetadata().getColorTable(imageIndex, planeIndex);
        } catch (IOException e) {
          LOGGER.error("IOException when trying to read color table", e);
          return null;
        }
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {
    
    // -- Constructor --
    
    public Checker() {
      suffixNecessary = false;
      suffixSufficient = false;
    }
    
    // -- Checker API Methods --
    
    @Override
    public boolean isFormat(RandomAccessInputStream stream) throws IOException {
      TiffParser tp = new TiffParser(getContext(), stream);
      tp.setDoCaching(false);
      boolean validHeader = tp.isValidHeader();
      if (!validHeader) return false;
      // look for OME-XML in first IFD's comment
      IFD ifd = tp.getFirstIFD();
      if (ifd == null) return false;
      Object description = ifd.get(IFD.IMAGE_DESCRIPTION);
      if (description == null) {
        return false;
      }
      String comment = null;
      if (description instanceof TiffIFDEntry) {
        comment = tp.getIFDValue((TiffIFDEntry) description).toString();
      }
      else if (description instanceof String) {
        comment = (String) description;
      }
      if (comment == null || comment.trim().length() == 0) return false;

      comment = comment.trim();

      // do a basic sanity check before attempting to parse the comment as XML
      // the parsing step is a bit slow, so there is no sense in trying unless
      // we are reasonably sure that the comment contains XML
      if (!comment.startsWith("<") || !comment.endsWith(">")) {
        return false;
      }

      try {
        if (service == null) setupServices(getContext());
        IMetadata meta = service.createOMEXMLMetadata(comment);
        for (int i=0; i<meta.getImageCount(); i++) {
          meta.setPixelsBinDataBigEndian(Boolean.TRUE, i, 0);
          metaService.verifyMinimumPopulated(meta, i);
        }
        return meta.getImageCount() > 0;
      }
      catch (ServiceException se) {
        LOGGER.debug("OME-XML parsing failed", se);
      }
      catch (NullPointerException e) {
        LOGGER.debug("OME-XML parsing failed", e);
      }
      catch (FormatException e) {
        LOGGER.debug("OME-XML parsing failed", e);
      }
      catch (IndexOutOfBoundsException e) {
        LOGGER.debug("OME-XML parsing failed", e);
      }
      return false;
    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Parser API Methods --
    
    @Override
    public String[] getImageUsedFiles(int imageIndex, boolean noPixels) {
      FormatTools.assertId(currentId, true, 1);
      if (noPixels) return null;
      Vector<String> usedFiles = new Vector<String>();
      for (int i=0; i<metadata.info[imageIndex].length; i++) {
        if (!usedFiles.contains(metadata.info[imageIndex][i].id)) {
          usedFiles.add(metadata.info[imageIndex][i].id);
        }
      }
      return usedFiles.toArray(new String[usedFiles.size()]);
    }

    @Override
    public Metadata parse(String fileName, Metadata meta) throws IOException, FormatException {
      return super.parse(normalizeFilename(null, fileName), meta); 
    }

    @Override
    public Metadata parse(File file, Metadata meta) throws IOException, FormatException {
      return super.parse(normalizeFilename(null, file.getPath()), meta); 
    }

    @Override
    public int fileGroupOption(String id) throws FormatException, IOException {
        boolean single = isSingleFile(id);
        return single ? FormatTools.CAN_GROUP : FormatTools.MUST_GROUP;
    }
    
    @Override
    public Metadata parse(RandomAccessInputStream stream, Metadata meta)
        throws IOException, FormatException {

      super.parse(stream, meta);
      for (int s=0; s<meta.getImageCount(); s++) {
        OMETIFFPlane[][] info = meta.getPlaneInfo();
        
        try {
          if (!info[s][0].reader.getFormat().createChecker().isFormat(info[s][0].id)) {
            info[s][0].id = meta.getSource().getFileName();
          }
          for (int plane=0; plane<info[s].length; plane++) {
            if (!info[s][plane].reader.getFormat().createChecker().isFormat(info[s][plane].id)) {
              info[s][plane].id = info[s][0].id;
            }
          }

          info[s][0].reader.setSource(info[s][0].id);
          meta.getTileWidth()[s] = info[s][0].reader.getOptimalTileWidth(s);
          meta.getTileHeight()[s] = info[s][0].reader.getOptimalTileHeight(s);
        }
        catch (FormatException e) {
          LOGGER.debug("OME-XML parsing failed", e);
        }
      }
      
      return meta;
    }

    // -- Groupable API Methods --
    
    @Override
    public boolean isSingleFile(String id) throws FormatException, IOException {
      return OMETIFFFormat.isSingleFile(getContext(), id);
    }
    
    // -- Abstract Parser API Methods --
    
    @Override
    protected void typedParse(io.scif.io.RandomAccessInputStream stream,
      Metadata meta) throws IOException, io.scif.FormatException {
      // normalize file name
      String id = stream.getFileName();
      
      String dir = new File(id).getParent();

      // parse and populate OME-XML metadata
      String fileName = new Location(getContext(), id).getAbsoluteFile().getAbsolutePath();
      if (!new File(fileName).exists()) {
        fileName = currentId;
      }
      RandomAccessInputStream ras = new RandomAccessInputStream(getContext(), fileName);
      String xml;
      IFD firstIFD;
      try {
        TiffParser tp = new TiffParser(getContext(), ras);
        firstIFD = tp.getFirstIFD();
        xml = firstIFD.getComment();
      }
      finally {
        ras.close();
      }
      
      meta.setFirstIFD(firstIFD);

      if (service == null) setupServices(getContext());
      OMEXMLMetadata omexmlMeta;
      try {
        omexmlMeta = service.createOMEXMLMetadata(xml);
      }
      catch (ServiceException se) {
        throw new FormatException(se);
      }

      meta.setHasSPW(omexmlMeta.getPlateCount() > 0);

      for (int i=0; i<meta.getImageCount(); i++) {
        int sizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
        service.removeChannels(omexmlMeta, i, sizeC);
      }

      Hashtable<String, Object> originalMetadata = service.getOriginalMetadata(omexmlMeta);
      if (originalMetadata != null) meta.getTable().putAll(originalMetadata);

      LOGGER.trace(xml);

      if (omexmlMeta.getRoot() == null) {
        throw new FormatException("Could not parse OME-XML from TIFF comment");
      }
      
      meta.setOmeMeta(new OMEMetadata(getContext(), omexmlMeta));

      String[] acquiredDates = new String[meta.getImageCount()];
      for (int i=0; i<acquiredDates.length; i++) {
        Timestamp acquisitionDate = omexmlMeta.getImageAcquisitionDate(i);
        if (acquisitionDate != null) {
          acquiredDates[i] = acquisitionDate.getValue();
        }
      }

      String currentUUID = omexmlMeta.getUUID();

      // determine series count from Image and Pixels elements
      int imageCount = omexmlMeta.getImageCount();
      meta.createImageMetadata(imageCount);
      
      OMETIFFPlane[][] info = new OMETIFFPlane[imageCount][];
      meta.setPlaneInfo(info);

      int[] tileWidth = new int[imageCount];
      int[] tileHeight = new int[imageCount];
      
      meta.setTileWidth(tileWidth);
      meta.setTileHeight(tileHeight);
      
      // compile list of file/UUID mappings
      Hashtable<String, String> files = new Hashtable<String, String>();
      boolean needSearch = false;
      for (int i=0; i<imageCount; i++) {
        int tiffDataCount = omexmlMeta.getTiffDataCount(i);
        for (int td=0; td<tiffDataCount; td++) {
          String uuid = null;
          try {
            uuid = omexmlMeta.getUUIDValue(i, td);
          }
          catch (NullPointerException e) { }
          String filename = null;
          if (uuid == null) {
            // no UUID means that TiffData element refers to this file
            uuid = "";
            filename = id;
          }
          else {
            filename = omexmlMeta.getUUIDFileName(i, td);
            if (!new Location(getContext(), dir, filename).exists()) filename = null;
            if (filename == null) {
              if (uuid.equals(currentUUID) || currentUUID == null) {
                // UUID references this file
                filename = id;
              }
              else {
                // will need to search for this UUID
                filename = "";
                needSearch = true;
              }
            }
            else filename = normalizeFilename(dir, filename);
          }
          String existing = files.get(uuid);
          if (existing == null) files.put(uuid, filename);
          else if (!existing.equals(filename)) {
            throw new FormatException("Inconsistent UUID filenames");
          }
        }
      }

      // search for missing filenames
      if (needSearch) {
        Enumeration<String> en = files.keys();
        while (en.hasMoreElements()) {
          String uuid = (String) en.nextElement();
          String filename = files.get(uuid);
          if (filename.equals("")) {
            // TODO search...
            // should scan only other .ome.tif files
            // to make this work with OME server may be a little tricky?
            throw new FormatException("Unmatched UUID: " + uuid);
          }
        }
      }

      // build list of used files
      Enumeration<String> en = files.keys();
      int numUUIDs = files.size();
      HashSet<String> fileSet = new HashSet<String>(); // ensure no duplicate filenames
      for (int i=0; i<numUUIDs; i++) {
        String uuid = (String) en.nextElement();
        String filename = files.get(uuid);
        fileSet.add(filename);
      }
      String[] used = new String[fileSet.size()];
      meta.setUsed(used);
      
      Iterator<String> iter = fileSet.iterator();
      for (int i=0; i<used.length; i++) used[i] = (String) iter.next();

      // process TiffData elements
      Hashtable<String, MinimalTIFFFormat.Reader<?>> readers =
        new Hashtable<String, MinimalTIFFFormat.Reader<?>>();
      List<Boolean> adjustedSamples = new ArrayList<Boolean>();
      List<Integer> samples = new ArrayList<Integer>();
      meta.adjustedSamples = adjustedSamples;
      meta.samples = samples;
      
      for (int i=0; i<imageCount; i++) {
        int s = i;
        LOGGER.debug("Image[{}] {", i);
        LOGGER.debug("  id = {}", omexmlMeta.getImageID(i));
        
        adjustedSamples.add(false);

        String order = omexmlMeta.getPixelsDimensionOrder(i).toString();

        PositiveInteger samplesPerPixel = null;
        if (omexmlMeta.getChannelCount(i) > 0) {
          samplesPerPixel = omexmlMeta.getChannelSamplesPerPixel(i, 0);
        }
        samples.add(i, samplesPerPixel == null ?  -1 : samplesPerPixel.getValue());
        int tiffSamples = firstIFD.getSamplesPerPixel();

        if (adjustedSamples.get(i) ||
          (samples.get(i) != tiffSamples && (i == 0 || samples.get(i) < 0)))
        {
          LOGGER.warn("SamplesPerPixel mismatch: OME={}, TIFF={}",
            samples.get(i), tiffSamples);
          samples.set(i, tiffSamples);
          adjustedSamples.set(i,  true);
        }
        else {
          adjustedSamples.set(i,  false);
        }

        if (adjustedSamples.get(i)&& omexmlMeta.getChannelCount(i) <= 1) {
          adjustedSamples.set(i,  false);
        }

        int effSizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
        if (!adjustedSamples.get(i)) {
          effSizeC /= samples.get(i);
        }
        if (effSizeC == 0) effSizeC = 1;
        if (effSizeC * samples.get(i) != omexmlMeta.getPixelsSizeC(i).getValue().intValue()) {
          effSizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
        }
        int sizeT = omexmlMeta.getPixelsSizeT(i).getValue().intValue();
        int sizeZ = omexmlMeta.getPixelsSizeZ(i).getValue().intValue();
        int num = effSizeC * sizeT * sizeZ;

        OMETIFFPlane[] planes = new OMETIFFPlane[num];
        for (int no=0; no<num; no++) planes[no] = new OMETIFFPlane();

        int tiffDataCount = omexmlMeta.getTiffDataCount(i);
        Boolean zOneIndexed = null;
        Boolean cOneIndexed = null;
        Boolean tOneIndexed = null;

        // pre-scan TiffData indices to see if any of them are indexed from 1

        for (int td=0; td<tiffDataCount; td++) {
          NonNegativeInteger firstC = omexmlMeta.getTiffDataFirstC(i, td);
          NonNegativeInteger firstT = omexmlMeta.getTiffDataFirstT(i, td);
          NonNegativeInteger firstZ = omexmlMeta.getTiffDataFirstZ(i, td);
          int c = firstC == null ? 0 : firstC.getValue();
          int t = firstT == null ? 0 : firstT.getValue();
          int z = firstZ == null ? 0 : firstZ.getValue();

          if (c >= effSizeC && cOneIndexed == null) {
            cOneIndexed = true;
          }
          else if (c == 0) {
            cOneIndexed = false;
          }
          if (z >= sizeZ && zOneIndexed == null) {
            zOneIndexed = true;
          }
          else if (z == 0) {
            zOneIndexed = false;
          }
          if (t >= sizeT && tOneIndexed == null) {
            tOneIndexed = true;
          }
          else if (t == 0) {
            tOneIndexed = false;
          }
        }

        for (int td=0; td<tiffDataCount; td++) {
          LOGGER.debug("    TiffData[{}] {", td);
          // extract TiffData parameters
          String filename = null;
          String uuid = null;
          try {
            filename = omexmlMeta.getUUIDFileName(i, td);
          } catch (NullPointerException e) {
            LOGGER.debug("Ignoring null UUID object when retrieving filename.");
          }
          try {
            uuid = omexmlMeta.getUUIDValue(i, td);
          } catch (NullPointerException e) {
            LOGGER.debug("Ignoring null UUID object when retrieving value.");
          }
          NonNegativeInteger tdIFD = omexmlMeta.getTiffDataIFD(i, td);
          int ifd = tdIFD == null ? 0 : tdIFD.getValue();
          NonNegativeInteger numPlanes = omexmlMeta.getTiffDataPlaneCount(i, td);
          NonNegativeInteger firstC = omexmlMeta.getTiffDataFirstC(i, td);
          NonNegativeInteger firstT = omexmlMeta.getTiffDataFirstT(i, td);
          NonNegativeInteger firstZ = omexmlMeta.getTiffDataFirstZ(i, td);
          int c = firstC == null ? 0 : firstC.getValue();
          int t = firstT == null ? 0 : firstT.getValue();
          int z = firstZ == null ? 0 : firstZ.getValue();

          // NB: some writers index FirstC, FirstZ and FirstT from 1
          if (cOneIndexed != null && cOneIndexed) c--;
          if (zOneIndexed != null && zOneIndexed) z--;
          if (tOneIndexed != null && tOneIndexed) t--;

          if (z >= sizeZ || c >= effSizeC || t >= sizeT) {
            LOGGER.warn("Found invalid TiffData: Z={}, C={}, T={}",
              new Object[] {z, c, t});
            break;
          }

          int index = FormatTools.getIndex(order,
            sizeZ, effSizeC, sizeT, num, z, c, t);
          int count = numPlanes == null ? 1 : numPlanes.getValue();
          if (count == 0) {
            meta.get(s);
            break;
          }

          // get reader object for this filename
          if (filename == null) {
            if (uuid == null) filename = id;
            else filename = files.get(uuid);
          }
          else filename = normalizeFilename(dir, filename);
          MinimalTIFFFormat.Reader<?> r = readers.get(filename);
          if (r == null) {
            r = getReader(scifio(), MinimalTIFFFormat.class);
            readers.put(filename, r);
          }

          Location file = new Location(getContext(), filename);
          if (!file.exists()) {
            // if this is an absolute file name, try using a relative name
            // old versions of OMETiffWriter wrote an absolute path to
            // UUID.FileName, which causes problems if the file is moved to
            // a different directory
            filename =
              filename.substring(filename.lastIndexOf(File.separator) + 1);
            filename = dir + File.separator + filename;

            if (!new Location(getContext(), filename).exists()) {
              filename = currentId;
            }
          }

          // populate plane index -> IFD mapping
          for (int q=0; q<count; q++) {
            int no = index + q;
            planes[no].reader = r;
            planes[no].id = filename;
            planes[no].ifd = ifd + q;
            planes[no].certain = true;
            LOGGER.debug("      Plane[{}]: file={}, IFD={}",
              new Object[] {no, planes[no].id, planes[no].ifd});
          }
          if (numPlanes == null) {
            // unknown number of planes; fill down
            for (int no=index+1; no<num; no++) {
              if (planes[no].certain) break;
              planes[no].reader = r;
              planes[no].id = filename;
              planes[no].ifd = planes[no - 1].ifd + 1;
              LOGGER.debug("      Plane[{}]: FILLED", no);
            }
          }
          else {
            // known number of planes; clear anything subsequently filled
            for (int no=index+count; no<num; no++) {
              if (planes[no].certain) break;
              planes[no].reader = null;
              planes[no].id = null;
              planes[no].ifd = -1;
              LOGGER.debug("      Plane[{}]: CLEARED", no);
            }
          }
          LOGGER.debug("    }");
        }

        if (meta.get(s) == null) continue;

        // verify that all planes are available
        LOGGER.debug("    --------------------------------");
        for (int no=0; no<num; no++) {
          LOGGER.debug("    Plane[{}]: file={}, IFD={}",
            new Object[] {no, planes[no].id, planes[no].ifd});
          if (planes[no].reader == null) {
            LOGGER.warn("Image ID '{}': missing plane #{}.  " +
              "Using TiffReader to determine the number of planes.",
              omexmlMeta.getImageID(i), no);
            TIFFFormat.Reader<?> r = getReader(scifio(), TIFFFormat.class);
            r.setSource(currentId);
            try {
              planes = new OMETIFFPlane[r.getImageCount()];
              for (int plane=0; plane<planes.length; plane++) {
                planes[plane] = new OMETIFFPlane();
                planes[plane].id = currentId;
                planes[plane].reader = r;
                planes[plane].ifd = plane;
              }
              num = planes.length;
            }
            finally {
              r.close();
            }
          }
        }
        info[i] = planes;
        LOGGER.debug("  }");

      }
      
      // remove null CoreMetadata entries
      
      Vector<OMETIFFPlane[]> planeInfo = new Vector<OMETIFFPlane[]>();
      for (int i=meta.getImageCount() - 1; i>=0; i--) {
        if (meta.get(i) == null) {
          meta.getAll().remove(i);
          adjustedSamples.remove(i);
          samples.remove(i);
        }
        else {
          planeInfo.add(0, info[i]);
        }
      }
      info = planeInfo.toArray(new OMETIFFPlane[0][0]);
 
//      meta.getOmeMeta().populateImageMetadata();
    }
    
    // -- Helper methods --

    private String normalizeFilename(String dir, String name) {
       File file = new File(dir, name);
       if (file.exists()) return file.getAbsolutePath();
       return name;
    }

  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {
    
    // -- Fields --
    

    
    // -- Constructor --
    
    public Reader() {
      domains = FormatTools.NON_GRAPHICS_DOMAINS;
      hasCompanionFiles = true;
    }

    // -- Reader API Methods --

    @Override
    public int getOptimalTileWidth(int imageIndex) {
      return getMetadata().getTileWidth()[imageIndex];
    }

    @Override
    public int getOptimalTileHeight(int imageIndex) {
      return getMetadata().getTileHeight()[imageIndex];
    }
    
    @Override
    public String[] getDomains() {
      FormatTools.assertId(currentId, true, 1);
      return getMetadata().isHasSPW() ? new String[] {FormatTools.HCS_DOMAIN} :
        FormatTools.NON_SPECIAL_DOMAINS;
    }

    /*
     * @see io.scif.TypedReader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h)
      throws io.scif.FormatException, IOException {
      Metadata meta = getMetadata();
      byte[] buf = plane.getBytes();
      OMETIFFPlane[][] info = meta.getPlaneInfo();
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);
      meta.setLastPlane(planeIndex);
      int i = info[imageIndex][planeIndex].ifd;
      MinimalTIFFFormat.Reader<?> r = (MinimalTIFFFormat.Reader<?>) info[imageIndex][planeIndex].reader;
      if (r.getCurrentFile() == null) {
        r.setSource(info[imageIndex][planeIndex].id);
      }
      IFDList ifdList = r.getMetadata().getIfds();
      if (i >= ifdList.size()) {
        LOGGER.warn("Error untangling IFDs; the OME-TIFF file may be malformed.");
        return plane;
      }
      IFD ifd = ifdList.get(i);
      RandomAccessInputStream s =
        new RandomAccessInputStream(getContext(), info[imageIndex][planeIndex].id);
      TiffParser p = new TiffParser(getContext(), s);
      p.getSamples(ifd, buf, x, y, w, h);
      s.close();
      return plane;
    }
    
    // -- Groupable API Methods --
    
    public boolean isSingleFile(String id) throws FormatException, IOException {
      return OMETIFFFormat.isSingleFile(getContext(), id);
    }
    
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Writer extends TIFFFormat.Writer<Metadata> {

    // -- Constants --

    private static final String WARNING_COMMENT =
      "<!-- Warning: this comment is an OME-XML metadata block, which " +
      "contains crucial dimensional parameters and other important metadata. " +
      "Please edit cautiously (if at all), and back up the original data " +
      "before doing so. For more information, see the OME-TIFF web site: " +
      FormatTools.URL_OME_TIFF + ". -->";

    // -- Fields --

    private List<Integer> imageMap;
    private String[][] imageLocations;
    private OMEXMLMetadata omeMeta;
    private OMEXMLService service;
    private Map<String, Integer> ifdCounts = new HashMap<String, Integer>();

    private Map<String, String> uuids = new HashMap<String, String>();
    
    // -- Writer API Methods --
    
    /* @see IFormatHandler#setId(String) */
    public void setDest(RandomAccessOutputStream out, int imageIndex) throws FormatException, IOException {
      //TODO if already set, return
      super.setDest(out, imageIndex);
      if (imageLocations == null) {
        MetadataRetrieve r = getMetadata().getOmeMeta().getRoot();
        imageLocations = new String[r.getImageCount()][];
        for (int i=0; i<imageLocations.length; i++) {
          imageLocations[i] = new String[planeCount(imageIndex)];
        }
      }
    }
    
    /*
     * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
     */
    public void savePlane(final int imageIndex, final int planeIndex, final Plane plane,
      final int x, final int y, final int w, final int h)
      throws FormatException, IOException
    {
      savePlane(imageIndex, planeIndex, plane, null, x, y, w, h);
    }

    public void savePlane(int imageIndex, int planeIndex, Plane plane, IFD ifd, int x,
      int y, int w, int h) throws io.scif.FormatException, IOException {
      if (imageMap == null) imageMap = new ArrayList<Integer>();
      if (!imageMap.contains(imageIndex)) {
        imageMap.add(new Integer(imageIndex));
      }

      super.savePlane(imageIndex, planeIndex, plane, ifd, x, y, w, h);

      // TODO should this be the output id?
      imageLocations[imageIndex][planeIndex] = getMetadata().getDatasetName();
    }
    
    /* @see loci.formats.IFormatHandler#close() */
    public void close() throws IOException {
      try {
        if (this.out != null) {
          setupServiceAndMetadata();

          // remove any BinData elements from the OME-XML
          service.removeBinData(omeMeta);

          for (int series=0; series<omeMeta.getImageCount(); series++) {
            populateImage(omeMeta, series);
          }

          List<String> files = new ArrayList<String>();
          for (String[] s : imageLocations) {
            for (String f : s) {
              if (!files.contains(f) && f != null) {
                files.add(f);

                String xml = getOMEXML(f);

                // write OME-XML to the first IFD's comment
                saveComment(f, xml);
              }
            }
          }
        }
      }
      catch (DependencyException de) {
        throw new RuntimeException(de);
      }
      catch (ServiceException se) {
        throw new RuntimeException(se);
      }
      catch (FormatException fe) {
        throw new RuntimeException(fe);
      }
      catch (IllegalArgumentException iae) {
        throw new RuntimeException(iae);
      }
      finally {
        super.close();

        boolean canReallyClose =
          omeMeta == null || ifdCounts.size() == omeMeta.getImageCount();

        if (omeMeta != null && canReallyClose) {
          int omePlaneCount = 0;
          for (int i=0; i<omeMeta.getImageCount(); i++) {
            int sizeZ = omeMeta.getPixelsSizeZ(i).getValue();
            int sizeC = omeMeta.getPixelsSizeC(i).getValue();
            int sizeT = omeMeta.getPixelsSizeT(i).getValue();

            omePlaneCount += sizeZ * sizeC * sizeT;
          }

          int ifdCount = 0;
          for (String key : ifdCounts.keySet()) {
            ifdCount += ifdCounts.get(key);
          }

          canReallyClose = omePlaneCount == ifdCount;
        }

        if (canReallyClose) {
          imageMap = null;
          imageLocations = null;
          omeMeta = null;
          service = null;
          ifdCounts.clear();
        }
        else {
          for(String k : ifdCounts.keySet())
          ifdCounts.put(k, 0);
        }
      }
    }
    
    // -- Helper methods --

    /** Gets the UUID corresponding to the given filename. */
    private String getUUID(String filename) {
      String uuid = uuids.get(filename);
      if (uuid == null) {
        uuid = UUID.randomUUID().toString();
        uuids.put(filename, uuid);
      }
      return uuid;
    }

    private void setupServiceAndMetadata()
      throws DependencyException, ServiceException
    {
      // extract OME-XML string from metadata object
      MetadataRetrieve retrieve = getMetadata().getOmeMeta().getRoot();

      service = getContext().getService(OMEXMLService.class);
      OMEXMLMetadata originalOMEMeta = service.getOMEMetadata(retrieve);
      originalOMEMeta.resolveReferences();

      String omexml = service.getOMEXML(originalOMEMeta);
      omeMeta = service.createOMEXMLMetadata(omexml);
    }

    private String getOMEXML(String file) throws FormatException, IOException {
      // generate UUID and add to OME element
      String uuid = "urn:uuid:" + getUUID(new Location(getContext(), file).getName());
      omeMeta.setUUID(uuid);

      String xml;
      try {
        xml = service.getOMEXML(omeMeta);
      }
      catch (ServiceException se) {
        throw new FormatException(se);
      }

      // insert warning comment
      String prefix = xml.substring(0, xml.indexOf(">") + 1);
      String suffix = xml.substring(xml.indexOf(">") + 1);
      return prefix + WARNING_COMMENT + suffix;
    }

    private void saveComment(String file, String xml) throws IOException {
      if (out != null) out.close();
      out = new RandomAccessOutputStream(getContext(), file);
      RandomAccessInputStream in = null;
      try {
        TiffSaver saver = new TiffSaver(getContext(), out, file);
        saver.setBigTiff(isBigTiff);
        in = new RandomAccessInputStream(getContext(), file);
        saver.overwriteLastIFDOffset(in);
        saver.overwriteComment(in, xml);
        in.close();
      }
      catch (FormatException exc) {
        IOException io = new IOException("Unable to append OME-XML comment");
        io.initCause(exc);
        throw io;
      }
      finally {
        if (out != null) out.close();
        if (in != null) in.close();
      }
    }

    private void populateTiffData(OMEXMLMetadata omeMeta, int[] zct,
      int ifd, int series, int plane)
    {
      omeMeta.setTiffDataFirstZ(new NonNegativeInteger(zct[0]), series, plane);
      omeMeta.setTiffDataFirstC(new NonNegativeInteger(zct[1]), series, plane);
      omeMeta.setTiffDataFirstT(new NonNegativeInteger(zct[2]), series, plane);
      omeMeta.setTiffDataIFD(new NonNegativeInteger(ifd), series, plane);
      omeMeta.setTiffDataPlaneCount(new NonNegativeInteger(1), series, plane);
    }

    private void populateImage(OMEXMLMetadata omeMeta, int imageIndex) {
      String dimensionOrder = omeMeta.getPixelsDimensionOrder(imageIndex).toString();
      int sizeZ = omeMeta.getPixelsSizeZ(imageIndex).getValue().intValue();
      int sizeC = omeMeta.getPixelsSizeC(imageIndex).getValue().intValue();
      int sizeT = omeMeta.getPixelsSizeT(imageIndex).getValue().intValue();

      int planeCount = getPlaneCount(imageIndex);
      int ifdCount = imageMap.size();

      if (planeCount == 0) {
        omeMeta.setTiffDataPlaneCount(new NonNegativeInteger(0), imageIndex, 0);
        return;
      }

      PositiveInteger samplesPerPixel =
        new PositiveInteger((sizeZ * sizeC * sizeT) / planeCount);
      for (int c=0; c<omeMeta.getChannelCount(imageIndex); c++) {
        omeMeta.setChannelSamplesPerPixel(samplesPerPixel, imageIndex, c);
      }
      sizeC /= samplesPerPixel.getValue();

      int nextPlane = 0;
      for (int plane=0; plane<planeCount; plane++) {
        int[] zct = FormatTools.getZCTCoords(dimensionOrder,
          sizeZ, sizeC, sizeT, planeCount, imageIndex, plane);

        int planeIndex = plane;
        if (imageLocations[imageIndex].length < planeCount) {
          planeIndex /= (planeCount / imageLocations[imageIndex].length);
        }

        String filename = imageLocations[imageIndex][planeIndex];
        if (filename != null) {
          filename = new Location(getContext(), filename).getName();

          Integer ifdIndex = ifdCounts.get(filename);
          int ifd = ifdIndex == null ? 0 : ifdIndex.intValue();

          omeMeta.setUUIDFileName(filename, imageIndex, nextPlane);
          String uuid = "urn:uuid:" + getUUID(filename);
          omeMeta.setUUIDValue(uuid, imageIndex, nextPlane);

          // fill in any non-default TiffData attributes
          populateTiffData(omeMeta, zct, ifd, imageIndex, nextPlane);
          ifdCounts.put(filename, ifd + 1);
          nextPlane++;
        }
      }
    }

    private int planeCount(int imageIndex) {
      MetadataRetrieve r = getMetadata().getOmeMeta().getRoot();
      int z = r.getPixelsSizeZ(imageIndex).getValue().intValue();
      int t = r.getPixelsSizeT(imageIndex).getValue().intValue();
      int c = r.getChannelCount(imageIndex);
      String pixelType = r.getPixelsType(imageIndex).getValue();
      int bytes = FormatTools.getBytesPerPixel(pixelType);

      if (bytes > 1 && c == 1) {
        c = r.getChannelSamplesPerPixel(imageIndex, 0).getValue();
      }

      return z * c * t;
    }
    
  }
  
  // -- Helper Methods --
  
  @SuppressWarnings("unchecked")
  private static <T extends MinimalTIFFFormat.Reader<?>> T getReader(SCIFIO scifio, Class<? extends Format> formatClass) throws FormatException {
    return (T) scifio.format().getFormatFromClass(formatClass).createReader();
  }

  private static void setupServices(Context ctx) {
    service = ctx.getService(OMEXMLService.class);
    metaService = ctx.getService(OMEXMLMetadataService.class);
  }
  
  private static boolean isSingleFile(Context context, String id) throws FormatException, IOException {
    // parse and populate OME-XML metadata
    String fileName = new Location(context, id).getAbsoluteFile().getAbsolutePath();
    RandomAccessInputStream ras = new RandomAccessInputStream(context, fileName);
    TiffParser tp = new TiffParser(context, ras);
    IFD ifd = tp.getFirstIFD();
    long[] ifdOffsets = tp.getIFDOffsets();
    ras.close();
    String xml = ifd.getComment();

    if (service == null) setupServices(context);
    OMEXMLMetadata meta;
    try {
      meta = service.createOMEXMLMetadata(xml);
    }
    catch (ServiceException se) {
      throw new FormatException(se);
    }

    if (meta.getRoot() == null) {
      throw new FormatException("Could not parse OME-XML from TIFF comment");
    }

    int nImages = 0;
    for (int i=0; i<meta.getImageCount(); i++) {
      int nChannels = meta.getChannelCount(i);
      if (nChannels == 0) nChannels = 1;
      int z = meta.getPixelsSizeZ(i).getValue().intValue();
      int t = meta.getPixelsSizeT(i).getValue().intValue();
      nImages += z * t * nChannels;
    }
    return nImages <= ifdOffsets.length;
  }

  /**
   * This class can be used for translating any io.scif.Metadata
   * to Metadata for writing OME-TIFF.
   * files.
   * <p>
   * Note that Metadata translated from Core is only write-safe.
   * </p>
   * <p>
   * If trying to read, there should already exist an originally-parsed OME-TIFF
   * Metadata object which can be used.
   * </p>
   * <p>
   * Note also that any OME-TIFF image written must be reparsed, as the Metadata used
   * to write it can not be guaranteed valid.
   * </p>
   */
  @Plugin(type = Translator.class, attrs = 
    {@Attr(name = OMETIFFTranslator.SOURCE, value = io.scif.Metadata.CNAME),
     @Attr(name = OMETIFFTranslator.DEST, value = Metadata.CNAME)},
    priority = OMETIFFFormat.PRIORITY)
  public static class OMETIFFTranslator
    extends AbstractTranslator<io.scif.Metadata, Metadata>
  {
    // -- Translator API Methods -- 
    
    public void typedTranslate(io.scif.Metadata source, Metadata dest) {
      
      if (dest.getOmeMeta() == null) {
        OMEMetadata omeMeta = new OMEMetadata(getContext());
        scifio().translator().translate(source, omeMeta, false);
        dest.setOmeMeta(omeMeta);
      }
      
      try {
        TIFFFormat.Metadata tiffMeta = (TIFFFormat.Metadata) 
            scifio().format().getFormatFromClass(TIFFFormat.class).createMetadata();
        
        scifio().translator().translate(source, tiffMeta, false);
        
        dest.setFirstIFD(tiffMeta.getIfds().get(0));
        
      } catch (FormatException e) {
        LOGGER.error("Failed to generate TIFF data", e);
      }
      
      OMETIFFPlane[][] info = new OMETIFFPlane[source.getImageCount()][];
      dest.setPlaneInfo(info);
      
      List<Integer> samples = new ArrayList<Integer>();
      List<Boolean> adjustedSamples = new ArrayList<Boolean>();
      
      dest.samples = samples;
      dest.adjustedSamples = adjustedSamples;
      
      dest.createImageMetadata(0);
      
      for (int i=0; i<source.getImageCount(); i++) {
        info[i] = new OMETIFFPlane[source.getPlaneCount(i)];
        
        for (int j=0; j<source.getPlaneCount(i); j++) {
          info[i][j] = new OMETIFFPlane();
        }
        
        dest.add(new DefaultImageMetadata());
        
        samples.add(source.getRGBChannelCount(i));
        adjustedSamples.add(false);
      }
    }
  }
  
  // -- Helper classes --

  /** Structure containing details on where to find a particular image plane. */
  private static class OMETIFFPlane {
    /** Reader to use for accessing this plane. */
    public MinimalTIFFFormat.Reader<?> reader;
    /** File containing this plane. */
    public String id;
    /** IFD number of this plane. */
    public int ifd = -1;
    /** Certainty flag, for dealing with unspecified NumPlanes. */
    public boolean certain = false;
  }
}
