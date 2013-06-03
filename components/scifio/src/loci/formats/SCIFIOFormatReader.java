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

import io.scif.ByteArrayPlane;
import io.scif.Checker;
import io.scif.Format;
import io.scif.HasColorTable;
import io.scif.Metadata;
import io.scif.Parser;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;
import loci.legacy.adapter.CommonAdapter;
import loci.legacy.adapter.Wrapper;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import ome.xml.model.enums.AcquisitionMode;
import ome.xml.model.enums.ArcType;
import ome.xml.model.enums.Binning;
import ome.xml.model.enums.Compression;
import ome.xml.model.enums.ContrastMethod;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.FilamentType;
import ome.xml.model.enums.FillRule;
import ome.xml.model.enums.FilterType;
import ome.xml.model.enums.FontFamily;
import ome.xml.model.enums.FontStyle;
import ome.xml.model.enums.IlluminationType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.LineCap;
import ome.xml.model.enums.Marker;
import ome.xml.model.enums.Medium;
import ome.xml.model.enums.MicrobeamManipulationType;
import ome.xml.model.enums.MicroscopeType;
import ome.xml.model.enums.NamingConvention;
import ome.xml.model.enums.PixelType;
import ome.xml.model.enums.Pulse;
import ome.xml.model.enums.handlers.AcquisitionModeEnumHandler;
import ome.xml.model.enums.handlers.ArcTypeEnumHandler;
import ome.xml.model.enums.handlers.BinningEnumHandler;
import ome.xml.model.enums.handlers.CompressionEnumHandler;
import ome.xml.model.enums.handlers.ContrastMethodEnumHandler;
import ome.xml.model.enums.handlers.CorrectionEnumHandler;
import ome.xml.model.enums.handlers.DetectorTypeEnumHandler;
import ome.xml.model.enums.handlers.DimensionOrderEnumHandler;
import ome.xml.model.enums.handlers.ExperimentTypeEnumHandler;
import ome.xml.model.enums.handlers.FilamentTypeEnumHandler;
import ome.xml.model.enums.handlers.FillRuleEnumHandler;
import ome.xml.model.enums.handlers.FilterTypeEnumHandler;
import ome.xml.model.enums.handlers.FontFamilyEnumHandler;
import ome.xml.model.enums.handlers.FontStyleEnumHandler;
import ome.xml.model.enums.handlers.IlluminationTypeEnumHandler;
import ome.xml.model.enums.handlers.ImmersionEnumHandler;
import ome.xml.model.enums.handlers.LaserMediumEnumHandler;
import ome.xml.model.enums.handlers.LaserTypeEnumHandler;
import ome.xml.model.enums.handlers.LineCapEnumHandler;
import ome.xml.model.enums.handlers.MarkerEnumHandler;
import ome.xml.model.enums.handlers.MediumEnumHandler;
import ome.xml.model.enums.handlers.MicrobeamManipulationTypeEnumHandler;
import ome.xml.model.enums.handlers.MicroscopeTypeEnumHandler;
import ome.xml.model.enums.handlers.NamingConventionEnumHandler;
import ome.xml.model.enums.handlers.PixelTypeEnumHandler;
import ome.xml.model.enums.handlers.PulseEnumHandler;
import ome.xml.services.OMEXMLMetadataService;

/**
 * Abstract superclass of all biological file format writers converted to
 * SCIFIO components. Defers to an io.scif.Reader
 * 
 * @see io.scif.Reader
 * 
 * @author Mark Hiner
 *
 * @deprecated see io.scif.Reader
 */
@Deprecated
public abstract class SCIFIOFormatReader extends FormatReader 
  implements Wrapper<Reader>
{

  // -- Fields --
  
  /**
   * Caches the last plane returned by this reader.
   */
  protected Plane plane;
  
  /** SCIFIO Format, used to generate the other components */
  protected Format format;

  /** SCIFIO Checker for deference */
  protected Checker checker;

  /** SCIFIO Parser for deference */
  protected Parser parser;

  /** SCIFIO Reader for deference */
  protected Reader reader;

  /** SCIFIO Translator for deference */
  protected Translator translator;

  // -- Constructors --

  public SCIFIOFormatReader(String format, String suffix) {
    super(format, suffix);
  }

  public SCIFIOFormatReader(String format, String[] suffixes) {
    super(format, suffixes);
  }
  
  // -- Wrapper API methods --
  
  public Reader unwrap() {
    return reader;
  }

  // -- Internal FormatReader API methods --

  /** Returns true if the given file name is in the used files list. */
  @Override
  protected boolean isUsedFile(String file) {
    String[] usedFiles = getUsedFiles();
    for (String used : usedFiles) {
      if (used.equals(file)) return true;
      String path = new Location(file).getAbsolutePath();
      if (used.equals(path)) return true;
    }
    return false;
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, Object value) {
    reader.getMetadata().getTable().put(key, value);
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, boolean value) {
    addGlobalMeta(key, new Boolean(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, byte value) {
    addGlobalMeta(key, new Byte(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, short value) {
    addGlobalMeta(key, new Short(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, int value) {
    addGlobalMeta(key, new Integer(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, long value) {
    addGlobalMeta(key, new Long(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, float value) {
    addGlobalMeta(key, new Float(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, double value) {
    addGlobalMeta(key, new Double(value));
  }

  /** Adds an entry to the global metadata table. */
  @Override
  protected void addGlobalMeta(String key, char value) {
    addGlobalMeta(key, new Character(value));
  }

  /** Gets a value from the global metadata table. */
  @Override
  protected Object getGlobalMeta(String key) {
    return metadata.get(key);
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, Object value) {
    reader.getMetadata().get(getSeries()).getTable().put(key, value);
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, boolean value) {
    addSeriesMeta(key, new Boolean(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, byte value) {
    addSeriesMeta(key, new Byte(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, short value) {
    addSeriesMeta(key, new Short(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, int value) {
    addSeriesMeta(key, new Integer(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, long value) {
    addSeriesMeta(key, new Long(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, float value) {
    addSeriesMeta(key, new Float(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, double value) {
    addSeriesMeta(key, new Double(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Override
  protected void addSeriesMeta(String key, char value) {
    addSeriesMeta(key, new Character(value));
  }

  /** Gets an entry from the metadata table for the current series. */
  @Override
  protected Object getSeriesMeta(String key) {
    return reader.getMetadata().get(getSeries()).getTable().get(key);
  }

  /** Reads a raw plane from disk. */
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, byte[] buf) throws IOException
  {
    if(plane == null || !(ByteArrayPlane.class.isAssignableFrom(plane.getClass()))) {
      plane = new ByteArrayPlane(reader.getContext());
      ((ByteArrayPlane)plane).populate(reader.getMetadata().get(getSeries()), buf,
          x, y, w, h);
      
      return reader.readPlane(CommonAdapter.get(s),
          getSeries(), x, y, w, h, plane).getBytes();
    }
    else {
      ((ByteArrayPlane)plane).populate(buf, x, y, w, h);
      return reader.readPlane(
          CommonAdapter.get(s),
          getSeries(), x, y, w, h, plane).getBytes();
    }
  }

  /** Reads a raw plane from disk. */
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, int scanlinePad, byte[] buf) throws IOException
  {
    if(plane == null || !(ByteArrayPlane.class.isAssignableFrom(plane.getClass()))) {
      plane = new ByteArrayPlane(reader.getContext());
      ((ByteArrayPlane)plane).populate(reader.getMetadata().get(getSeries()), buf,
          x, y, w, h);
      
      return reader.readPlane(CommonAdapter.get(s),
          getSeries(), x, y, w, h, scanlinePad, plane).getBytes();
    }
    else {
      ((ByteArrayPlane)plane).populate(buf, x, y, w, h);
      return reader.readPlane(
          CommonAdapter.get(s),
          getSeries(), x, y, w, h, scanlinePad, plane).getBytes();
    }
  }

  /** Return a properly configured loci.formats.meta.FilterMetadata. */
  protected MetadataStore makeFilterMetadata() {
    return new FilterMetadata(getMetadataStore(), isMetadataFiltered());
  }

  // -- IMetadataConfigurable API methods --

  /* 
   * @see loci.formats.IMetadataConfigurable#getSupportedMetadataLevels()
   */
  public Set<MetadataLevel> getSupportedMetadataLevels() {
    Set<MetadataLevel> supportedLevels = new HashSet<MetadataLevel>();
    supportedLevels.add(MetadataLevel.ALL);
    supportedLevels.add(MetadataLevel.NO_OVERLAYS);
    supportedLevels.add(MetadataLevel.MINIMUM);
    return supportedLevels;
  }

  /* 
   * @see loci.formats.IMetadataConfigurable#getMetadataOptions()
   */
  @Override
  public MetadataOptions getMetadataOptions() {
    return metadataOptions;
  }

  /* 
   * @see loci.formats.IMetadataConfigurable#setMetadataOptions(loci.formats.in.MetadataOptions)
   */
  @Override
  public void setMetadataOptions(MetadataOptions options) {
    this.metadataOptions = options;
    
    parser.setMetadataOptions(FormatAdapter.get(options));
  }

  // -- IFormatReader API methods --

  /**
   * Checks if a file matches the type of this format reader.
   * Checks filename suffixes against those known for this format.
   * If the suffix check is inconclusive and the open parameter is true,
   * the file is opened and tested with
   * {@link #isThisType(RandomAccessInputStream)}.
   *
   * @param open If true, and the file extension is insufficient to determine
   *   the file type, the (existing) file is opened for further analysis.
   */
  @Override
  public boolean isThisType(String name, boolean open) {
    return checker.isFormat(name, open);
  }

  /* @see IFormatReader#isThisType(byte[]) */
  @Override
  public boolean isThisType(byte[] block) {
    return checker.isFormat(new String(block));
  }

  /* @see IFormatReader#isThisType(RandomAccessInputStream) */
  @Override
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    return checker.isFormat(CommonAdapter.get(stream));
  }

  /* @see IFormatReader#getImageCount() */
  @Override
  public int getImageCount() {
    return reader.getMetadata().getPlaneCount(getSeries());
  }

  /* @see IFormatReader#isRGB() */
  @Override
  public boolean isRGB() {
    return reader.getMetadata().isRGB(getSeries());
  }

  /* @see IFormatReader#getSizeX() */
  @Override
  public int getSizeX() {
    return reader.getMetadata().getAxisLength(getSeries(), Axes.X);
  }

  /* @see IFormatReader#getSizeY() */
  @Override
  public int getSizeY() {
    return reader.getMetadata().getAxisLength(getSeries(), Axes.Y);
  }

  /* @see IFormatReader#getSizeZ() */
  @Override
  public int getSizeZ() {
    return reader.getMetadata().getAxisLength(getSeries(), Axes.Z);
  }

  /* @see IFormatReader#getSizeC() */
  @Override
  public int getSizeC() {
    return reader.getMetadata().getAxisLength(getSeries(), Axes.CHANNEL);
  }

  /* @see IFormatReader#getSizeT() */
  @Override
  public int getSizeT() {
    return reader.getMetadata().getAxisLength(getSeries(), Axes.TIME);
  }

  /* @see IFormatReader#getPixelType() */
  @Override
  public int getPixelType() {
    return reader.getMetadata().getPixelType(getSeries());
  }

  /* @see IFormatReader#getBitsPerPixel() */
  @Override
  public int getBitsPerPixel() {
    return reader.getMetadata().getBitsPerPixel(getSeries());
  }

  /* @see IFormatReader#getEffectiveSizeC() */
  @Override
  public int getEffectiveSizeC() {
    // NB: by definition, imageCount == effectiveSizeC * sizeZ * sizeT
    int sizeZT = getSizeZ() * getSizeT();
    if (sizeZT == 0) return 0;
    return getImageCount() / sizeZT;
  }

  /* @see IFormatReader#getRGBChannelCount() */
  @Override
  public int getRGBChannelCount() {
    int effSizeC = getEffectiveSizeC();
    if (effSizeC == 0) return 0;
    return getSizeC() / effSizeC;
  }

  /* @see IFormatReader#isIndexed() */
  @Override
  public boolean isIndexed() {
    return reader.getMetadata().isIndexed(getSeries());
  }

  /* @see IFormatReader#isFalseColor() */
  @Override
  public boolean isFalseColor() {
    return reader.getMetadata().isFalseColor(getSeries());
  }

  /* @see IFormatReader#get8BitLookupTable() */
  @Override
  public byte[][] get8BitLookupTable() throws FormatException, IOException {
    Metadata m = reader.getMetadata();
    ColorTable ct = null;
    
    if (HasColorTable.class.isAssignableFrom(m.getClass()))
      ct = ((HasColorTable)m).getColorTable(getSeries(), 0); 
    else if (plane != null) 
      ct = plane.getColorTable();
    
    if(ct == null || !(ColorTable8.class.isAssignableFrom(ct.getClass()))) return null;
    else
      return ((ColorTable8)ct).getValues();
  }

  /* @see IFormatReader#get16BitLookupTable() */
  @Override
  public short[][] get16BitLookupTable() throws FormatException, IOException {
    Metadata m = reader.getMetadata();
    ColorTable ct = null;
    
    if (HasColorTable.class.isAssignableFrom(m.getClass()))
      ct = ((HasColorTable)m).getColorTable(getSeries(), 0); 
    else if (plane != null) 
      ct = plane.getColorTable();
    
    if(ct == null || !(ColorTable16.class.isAssignableFrom(ct.getClass()))) return null;
    else
      return ((ColorTable16)ct).getValues();
  }

  /* @see IFormatReader#getChannelDimLengths() */
  @Override
  public int[] getChannelDimLengths() {
    return reader.getMetadata().getChannelDimLengths(getSeries());
  }

  /* @see IFormatReader#getChannelDimTypes() */
  @Override
  public String[] getChannelDimTypes() {
    return reader.getMetadata().getChannelDimTypes(getSeries());
  }

  /* @see IFormatReader#getThumbSizeX() */
  @Override
  public int getThumbSizeX() {
    return reader.getMetadata().getThumbSizeX(getSeries());
  }

  /* @see IFormatReader#getThumbSizeY() */
  @Override
  public int getThumbSizeY() {
    return reader.getMetadata().getThumbSizeY(getSeries());
  }

  /* @see IFormatReader.isLittleEndian() */
  @Override
  public boolean isLittleEndian() {
    return reader.getMetadata().isLittleEndian(getSeries());
  }

  /* @see IFormatReader#getDimensionOrder() */
  @Override
  public String getDimensionOrder() {
    return io.scif.util.FormatTools.findDimensionOrder(reader, getSeries());
  }

  /* @see IFormatReader#isOrderCertain() */
  @Override
  public boolean isOrderCertain() {
    return reader.getMetadata().isOrderCertain(getSeries());
  }

  /* @see IFormatReader#isThumbnailSeries() */
  @Override
  public boolean isThumbnailSeries() {
    return reader.getMetadata().isThumbnailImage(getSeries());
  }

  /* @see IFormatReader#isInterleaved() */
  @Override
  public boolean isInterleaved() {
    return reader.getMetadata().isInterleaved(getSeries());
  }

  /* @see IFormatReader#isInterleaved(int) */
  @Override
  public boolean isInterleaved(int subC) {
  	//TODO no current equivalent for this method in SCIFIO
    return reader.getMetadata().isInterleaved(getSeries());
  }

  /* @see IFormatReader#openBytes(int) */
  @Override
  public byte[] openBytes(int no) throws FormatException, IOException {
    try {
      return (plane = reader.openPlane(getSeries(), no)).getBytes();
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#openBytes(int, byte[]) */
  @Override
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    try {
      if(plane == null || !(ByteArrayPlane.class.isAssignableFrom(plane.getClass()))) {
        return (plane = reader.openPlane(getSeries(), no)).getBytes();
      }
      else {
        ((ByteArrayPlane)plane).setData(buf);
        return reader.openPlane(getSeries(), no, plane).getBytes();
      }
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#openBytes(int, int, int, int, int) */
  @Override
  public byte[] openBytes(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return (plane = reader.openPlane(getSeries(), no, x, y, w, h)).getBytes();
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      //TODO wrong check.. need to check the reader's plane compatibility type
      if(plane == null || !(ByteArrayPlane.class.isAssignableFrom(plane.getClass()))) {
        plane = new ByteArrayPlane(reader.getContext());
      }
      ((ByteArrayPlane)plane).populate(reader.getMetadata().get(getSeries()), buf,
          x, y, w, h);
      return reader.openPlane(getSeries(), no, plane, x, y, w, h).getBytes();
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#openPlane(int, int, int, int, int int) */
  @Override
  public Object openPlane(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    // NB: Readers use byte arrays by default as the native type.
    return openBytes(no, x, y, w, h);
  }

  /* @see IFormatReader#openThumbBytes(int) */
  @Override
  public byte[] openThumbBytes(int no) throws FormatException, IOException {
    try {
      return (plane = reader.openThumbPlane(getSeries(), no)).getBytes();
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#close(boolean) */
  @Override
  public void close(boolean fileOnly) throws IOException {
    parser.close(fileOnly);
    reader.close(fileOnly);
    plane = null;
    super.close(fileOnly);
  }

  /* @see IFormatReader#getSeriesCount() */
  @Override
  public int getSeriesCount() {
    return reader.getMetadata().getImageCount();
  }

  /* @see IFormatReader#setSeries(int) */
  public void setSeries(int no) {
    if (no < 0 || no >= getSeriesCount()) {
      throw new IllegalArgumentException("Invalid series: " + no);
    }
    super.setSeries(no);
  }

  /* @see IFormatReader#getSeries() */
  public int getSeries() {
    return series;
  }

  /* @see IFormatReader#setGroupFiles(boolean) */
  @Override
  public void setGroupFiles(boolean groupFiles) {
    parser.setGroupFiles(groupFiles);
  }

  /* @see IFormatReader#isGroupFiles() */
  @Override
  public boolean isGroupFiles() {
    return parser.isGroupFiles();
  }

  /* @see IFormatReader#fileGroupOption(String) */
  @Override
  public int fileGroupOption(String id) throws FormatException, IOException {
    try {
      return parser.fileGroupOption(id);
    } catch (io.scif.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#isMetadataComplete() */
  @Override
  public boolean isMetadataComplete() {
    return reader.getMetadata().isMetadataComplete(getSeries());
  }

  /* @see IFormatReader#setNormalized(boolean) */
  @Override
  public void setNormalized(boolean normalize) {
    reader.setNormalized(normalize);
  }

  /* @see IFormatReader#isNormalized() */
  @Override
  public boolean isNormalized() {
    return reader.isNormalized();
  }

  /**
   * @deprecated
   * @see IFormatReader#setMetadataCollected(boolean)
   */
  public void setMetadataCollected(boolean collect) {
    FormatTools.assertId(currentId, false, 1);
    MetadataLevel level = collect ? MetadataLevel.ALL : MetadataLevel.MINIMUM;
    setMetadataOptions(new DefaultMetadataOptions(level));
  }

  /**
   * @deprecated
   * @see IFormatReader#isMetadataCollected()
   */
  public boolean isMetadataCollected() {
    return getMetadataOptions().getMetadataLevel() == MetadataLevel.ALL;
  }

  /* @see IFormatReader#setOriginalMetadataPopulated(boolean) */
  @Override
  public void setOriginalMetadataPopulated(boolean populate) {
    parser.setOriginalMetadataPopulated(populate);
  }

  /* @see IFormatReader#isOriginalMetadataPopulated() */
  @Override
  public boolean isOriginalMetadataPopulated() {
    return parser.isOriginalMetadataPopulated();
  }

  /* @see IFormatReader#getUsedFiles() */
  @Override
  public String[] getUsedFiles() {
    return parser.getUsedFiles();
  }

  /* @see IFormatReader#getUsedFiles() */
  @Override
  public String[] getUsedFiles(boolean noPixels) {
    return parser.getUsedFiles(noPixels);
  }

  /* @see IFormatReader#getSeriesUsedFiles() */
  @Override
  public String[] getSeriesUsedFiles() {
    return parser.getImageUsedFiles(getSeries());
  }

  /* @see IFormatReader#getSeriesUsedFiles(boolean) */
  @Override
  public String[] getSeriesUsedFiles(boolean noPixels) {
    return parser.getImageUsedFiles(getSeries(), noPixels);
  }

  /* @see IFormatReader#getAdvancedUsedFiles(boolean) */
  @Override
  public FileInfo[] getAdvancedUsedFiles(boolean noPixels) {
    io.scif.FileInfo[] tmpInfo = parser.getAdvancedUsedFiles(noPixels);
    return convertFileInfo(tmpInfo);
  }

  /* @see IFormatReader#getAdvancedSeriesUsedFiles(boolean) */
  @Override
  public FileInfo[] getAdvancedSeriesUsedFiles(boolean noPixels) {
    io.scif.FileInfo[] tmpInfo =
      parser.getAdvancedImageUsedFiles(getSeries(), noPixels);
    return convertFileInfo(tmpInfo);
  }

  /* @see IFormatReader#getCurrentFile() */
  public String getCurrentFile() {
    return reader.getMetadata().getDatasetName();
  }

  /* @see IFormatReader#getIndex(int, int, int) */
  @Override
  public int getIndex(int z, int c, int t) {
    return io.scif.util.FormatTools.getIndex(reader, getSeries(), z, c, t);
  }

  /* @see IFormatReader#getZCTCoords(int) */
  @Override
  public int[] getZCTCoords(int index) {
    return io.scif.util.FormatTools.getZCTCoords(reader, getSeries(), index);
  }

  /* @see IFormatReader#getMetadataValue(String) */
  @Override
  public Object getMetadataValue(String field) {
    return reader.getMetadata().getTable().get(field);
  }

  /* @see IFormatReader#getSeriesMetadataValue(String) */
  @Override
  public Object getSeriesMetadataValue(String field) {
    return reader.getMetadata().get(getSeries()).getTable().get(field);
  }

  /* @see IFormatReader#getGlobalMetadata() */
  @Override
  public Hashtable<String, Object> getGlobalMetadata() {
    return new Hashtable<String, Object>(reader.getMetadata().getTable());
  }

  /* @see IFormatReader#getSeriesMetadata() */
  @Override
  public Hashtable<String, Object> getSeriesMetadata() {
    return new Hashtable<String, Object>(reader.getMetadata().get(getSeries()).getTable());
  }

  @Override
  public Hashtable<String, Object> getMetadata() {
    FormatTools.assertId(currentId, true, 1);
    Hashtable<String, Object> h =
      new Hashtable<String, Object>(getGlobalMetadata());
    int oldSeries = getSeries();

    for (int series = 0; series < getSeriesCount(); series++) {
      String name = "Series " + series;
      setSeries(series);
      io.scif.util.SCIFIOMetadataTools.merge(getSeriesMetadata(), h, name + " ");
    }

    setSeries(oldSeries);
    return h;
  }
  
  /*
   * @see loci.formats.FormatReader#getCoreMetadataList()
   */
  @Override
  public List<CoreMetadata> getCoreMetadataList() {
    return FormatAdapter.get(reader.getMetadata());
  }

  /* @see IFormatReader#setMetadataFiltered(boolean) */
  @Override
  public void setMetadataFiltered(boolean filter) {
    parser.setMetadataFiltered(filter);
  }

  /* @see IFormatReader#isMetadataFiltered() */
  @Override
  public boolean isMetadataFiltered() {
    return parser.isMetadataFiltered();
  }

  /* @see IFormatReader#setMetadataStore(MetadataStore) */
  public void setMetadataStore(MetadataStore store) {
    FormatTools.assertId(currentId, false, 1);
    if (store == null) {
      throw new IllegalArgumentException("Metadata object cannot be null; "
        + "use loci.formats.meta.DummyMetadata instead");
    }
    metadataStore = store;
  }

  /* @see IFormatReader#getMetadataStore() */
  public MetadataStore getMetadataStore() {
    return metadataStore;
  }

  /* @see IFormatReader#getMetadataStoreRoot() */
  public Object getMetadataStoreRoot() {
    FormatTools.assertId(currentId, true, 1);
    return getMetadataStore().getRoot();
  }

  /* @see IFormatReader#getUnderlyingReaders() */
  public IFormatReader[] getUnderlyingReaders() {
    return null;
  }

  /* @see IFormatReader#isSingleFile(String) */
  public boolean isSingleFile(String id) throws FormatException, IOException {
    return true;
  }

  /* @see IFormatReader#hasCompanionFiles() */
  public boolean hasCompanionFiles() {
    return reader.hasCompanionFiles();
  }

  /* @see IFormatReader#getPossibleDomains(String) */
  @Override
  public String[] getPossibleDomains(String id)
    throws FormatException, IOException
  {
    return reader.getDomains();
  }

  /* @see IFormatReader#getDomains() */
  @Override
  public String[] getDomains() {
    FormatTools.assertId(currentId, true, 1);
    return reader.getDomains();
  }

  /* @see IFormatReader#getOptimalTileWidth() */
  @Override
  public int getOptimalTileWidth() {
    return reader.getOptimalTileWidth(getSeries());
  }

  /* @see IFormatReader#getOptimalTileHeight() */
  @Override
  public int getOptimalTileHeight() {
    return reader.getOptimalTileHeight(getSeries());
  }
  
  // -- Sub-resolution API methods --

  public int seriesToCoreIndex(int series)
  {
    //TODO
    return super.seriesToCoreIndex(series);
  }

  public int coreIndexToSeries(int index)
  {
    //TODO
    return super.coreIndexToSeries(index);
  }

  /* @see IFormatReader#getResolutionCount() */
  public int getResolutionCount() {
    //TODO
    return 0;
  }

  /* @see IFormatReader#setResolution(int) */
  public void setResolution(int no) {
    //TODO
    super.setResolution(no);
  }

  /* @see IFormatReader#getResolution() */
  public int getResolution() {
    //TODO
    return super.getResolution();
  }

  /* @see IFormatReader#hasFlattenedResolutions */
  public boolean hasFlattenedResolutions() {
    //TODO
    return super.hasFlattenedResolutions();
  }

  /* @see IFormatReader#setFlattenedResolutions(boolean) */
  public void setFlattenedResolutions(boolean flattened) {
    //TODO
    super.setFlattenedResolutions(flattened);
  }

  public int getCoreIndex() {
    //TODO
    return super.getCoreIndex();
  }

  public void setCoreIndex(int no) {
    //TODO
    super.setCoreIndex(no);
  }

  // -- IFormatHandler API methods --

  /* @see IFormatHandler#isThisType(String) */
  @Override
  public boolean isThisType(String name) {
    // if necessary, open the file for further analysis
    return isThisType(name, true);
  }

  /* @see IFormatHandler#setId(String) */
  @Override
  public void setId(String id) throws FormatException, IOException {
    super.setId(id);
    if (reader.getCurrentFile() == null || !reader.getCurrentFile().equals(currentId)) {
      
      setupMetdata();
      Metadata meta = reader.getMetadata();
      
      try {
        if (meta == null) meta = reader.getFormat().createMetadata();

        meta = parser.parse(id, meta);
      }
      catch (io.scif.FormatException e) {
        throw new FormatException(e.getCause());
      }
      reader.setMetadata(meta);
      
      reader.getContext().getService(OMEXMLMetadataService.class).
        populateMetadata(getMetadataStore(), getSeries(), currentId, meta);
      
      // NB: this has to happen after init file, instead of within init file
      // to allow the file to be parsed
      core = new ArrayList<CoreMetadata>();
      for (int i = 0; i < getSeriesCount(); i++) {
        CoreMetadata c = new CoreMetadata(reader.getMetadata(), i);
        c.orderCertain = true;
        core.add(c);
      }
    }
  }
  
  // -- Helper methods --
  
  // Converts io.scif.FileInfo to loci.formats.FileInfo
  private FileInfo[] convertFileInfo(io.scif.FileInfo[] source) {
    FileInfo[] info = new FileInfo[source.length];

    for (int i = 0; i < source.length; i++) {
      info[i] = new FileInfo();
      info[i].filename = source[i].filename;
      info[i].reader = source[i].reader;
      info[i].usedToInitialize = source[i].usedToInitialize;
    }

    return info;
  }

  // -- SCIFIO Utility Methods --
  
  /** 
   * Because the legacy workflow does not map perfectly to the SCIFIO workflow,
   * some formats may need to perform additional steps before parsing to
   * preserve correct behavior (e.g. attaching the metadata store).
   */
  protected void setupMetdata() { }

  /** Returns the SCIFIO reader being used for deferrment */
  public io.scif.Reader getReader() {
    return reader;
  }

  /** Returns the SCIFIO checker being used for deferrment */
  public Checker getChecker() {
    return checker;
  }

  /** Returns the SCIFIO parser being used for deferrment */
  public Parser getParser() {
    return parser;
  }

  /** Returns the SCIFIO translator being used for deferrment */
  public Translator getTranslator() {
    return translator;
  }

  // -- Metadata enumeration convenience methods --
  // TODO convert so SCIFIO?
  /**
   * Retrieves an {@link ome.xml.model.enums.AcquisitionMode} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected AcquisitionMode getAcquisitionMode(String value)
    throws FormatException
  {
    AcquisitionModeEnumHandler handler = new AcquisitionModeEnumHandler();
    try {
      return (AcquisitionMode) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("AcquisitionMode creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.ArcType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected ArcType getArcType(String value) throws FormatException {
    ArcTypeEnumHandler handler = new ArcTypeEnumHandler();
    try {
      return (ArcType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("ArcType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Binning} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Binning getBinning(String value) throws FormatException {
    BinningEnumHandler handler = new BinningEnumHandler();
    try {
      return (Binning) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Binning creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Compression} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Compression getCompression(String value) throws FormatException {
    CompressionEnumHandler handler = new CompressionEnumHandler();
    try {
      return (Compression) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Compression creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.ContrastMethod} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected ContrastMethod getContrastMethod(String value)
    throws FormatException
  {
    ContrastMethodEnumHandler handler = new ContrastMethodEnumHandler();
    try {
      return (ContrastMethod) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("ContrastMethod creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Correction} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Correction getCorrection(String value) throws FormatException {
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
  protected DetectorType getDetectorType(String value) throws FormatException {
    DetectorTypeEnumHandler handler = new DetectorTypeEnumHandler();
    try {
      return (DetectorType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("DetectorType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.DimensionOrder} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected DimensionOrder getDimensionOrder(String value)
    throws FormatException
  {
    DimensionOrderEnumHandler handler = new DimensionOrderEnumHandler();
    try {
      return (DimensionOrder) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("DimensionOrder creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.ExperimentType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected ExperimentType getExperimentType(String value)
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
   * Retrieves an {@link ome.xml.model.enums.FilamentType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected FilamentType getFilamentType(String value) throws FormatException {
    FilamentTypeEnumHandler handler = new FilamentTypeEnumHandler();
    try {
      return (FilamentType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("FilamentType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.FillRule} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected FillRule getFillRule(String value) throws FormatException {
    FillRuleEnumHandler handler = new FillRuleEnumHandler();
    try {
      return (FillRule) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("FillRule creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.FilterType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected FilterType getFilterType(String value) throws FormatException {
    FilterTypeEnumHandler handler = new FilterTypeEnumHandler();
    try {
      return (FilterType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("FilterType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.FontFamily} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected FontFamily getFontFamily(String value) throws FormatException {
    FontFamilyEnumHandler handler = new FontFamilyEnumHandler();
    try {
      return (FontFamily) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("FontFamily creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.FontStyle} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected FontStyle getFontStyle(String value) throws FormatException {
    FontStyleEnumHandler handler = new FontStyleEnumHandler();
    try {
      return (FontStyle) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("FontStyle creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.IlluminationType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected IlluminationType getIlluminationType(String value)
    throws FormatException
  {
    IlluminationTypeEnumHandler handler = new IlluminationTypeEnumHandler();
    try {
      return (IlluminationType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("IlluminationType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Immersion} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Immersion getImmersion(String value) throws FormatException {
    ImmersionEnumHandler handler = new ImmersionEnumHandler();
    try {
      return (Immersion) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Immersion creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserMedium} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected LaserMedium getLaserMedium(String value) throws FormatException {
    LaserMediumEnumHandler handler = new LaserMediumEnumHandler();
    try {
      return (LaserMedium) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserMedium creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected LaserType getLaserType(String value) throws FormatException {
    LaserTypeEnumHandler handler = new LaserTypeEnumHandler();
    try {
      return (LaserType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LaserType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.LineCap} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected LineCap getLineCap(String value) throws FormatException {
    LineCapEnumHandler handler = new LineCapEnumHandler();
    try {
      return (LineCap) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("LineCap creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Marker} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Marker getMarker(String value) throws FormatException {
    MarkerEnumHandler handler = new MarkerEnumHandler();
    try {
      return (Marker) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Marker creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Medium} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Medium getMedium(String value) throws FormatException {
    MediumEnumHandler handler = new MediumEnumHandler();
    try {
      return (Medium) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Medium creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.MicrobeamManipulationType}
   * enumeration value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected MicrobeamManipulationType getMicrobeamManipulationType(String value)
    throws FormatException
  {
    MicrobeamManipulationTypeEnumHandler handler =
      new MicrobeamManipulationTypeEnumHandler();
    try {
      return (MicrobeamManipulationType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("MicrobeamManipulationType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.MicroscopeType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected MicroscopeType getMicroscopeType(String value)
    throws FormatException
  {
    MicroscopeTypeEnumHandler handler = new MicroscopeTypeEnumHandler();
    try {
      return (MicroscopeType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("MicroscopeType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.NamingConvention} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected NamingConvention getNamingConvention(String value)
    throws FormatException
  {
    NamingConventionEnumHandler handler = new NamingConventionEnumHandler();
    try {
      return (NamingConvention) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("NamingConvention creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.PixelType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected PixelType getPixelType(String value) throws FormatException {
    PixelTypeEnumHandler handler = new PixelTypeEnumHandler();
    try {
      return (PixelType) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("PixelType creation failed", e);
    }
  }

  /**
   * Retrieves an {@link ome.xml.model.enums.Pulse} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  protected Pulse getPulse(String value) throws FormatException {
    PulseEnumHandler handler = new PulseEnumHandler();
    try {
      return (Pulse) handler.getEnumeration(value);
    }
    catch (EnumerationException e) {
      throw new FormatException("Pulse creation failed", e);
    }
  }

}
