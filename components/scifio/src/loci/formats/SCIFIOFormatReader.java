package loci.formats;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import net.imglib2.meta.Axes;

import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;
import loci.legacy.adapter.AdapterTools;
import loci.legacy.adapter.Wrapper;
import net.imglib2.meta.Axes;
import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.Translator;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
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

/**
 * Abstract superclass of all biological file format writers.
 * Defers to ome.scifio.Reader
 *
 */
@Deprecated
public abstract class SCIFIOFormatReader<T extends Metadata> extends FormatReader 
  implements Wrapper<Reader<T>>
  {

  // -- Fields --
  
  /** SCIFIO Format, used to generate the other components */
  protected Format<?, ?, ?, ?, ?> format;

  /** SCIFIO Checker for deference */
  protected Checker<T> checker;

  /** SCIFIO Parser for deference */
  protected Parser<T> parser;

  /** SCIFIO Reader for deference */
  protected Reader<T> reader;

  /** SCIFIO Translator for deference */
  protected Translator<T, ome.scifio.CoreMetadata> translator;

  // -- Constructors --

  public SCIFIOFormatReader(String format, String suffix) {
    super(format, suffix);
  }

  public SCIFIOFormatReader(String format, String[] suffixes) {
    super(format, suffixes);
  }
  
  // -- Wrapper API methods --
  
  public Reader<T> unwrap() {
    return reader;
  }

  // -- Internal FormatReader API methods --

  /**
   * Initializes the given file (parsing header information, etc.).
   * Most subclasses should override this method to perform
   * initialization operations such as parsing metadata.
   *
   * @throws FormatException if a parsing error occurs processing the file.
   * @throws IOException if an I/O error occurs processing the file
   */
  @Deprecated
  @Override
  protected void initFile(String id) throws FormatException, IOException {
    LOGGER.debug("{}.initFile({})", this.getClass().getName(), id);
    if (currentId != null) {
      String[] s = getUsedFiles();
      for (int i = 0; i < s.length; i++) {
        if (id.equals(s[i])) return;
      }
    }

    core = new CoreMetadata[1];
    core[0] = new CoreMetadata();
    series = 0;
    close();
    currentId = id;
    // reinitialize the MetadataStore
    // NB: critical for metadata conversion to work properly!
    getMetadataStore().createRoot();
  }

  /** Returns true if the given file name is in the used files list. */
  @Deprecated
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

  /** Adds an entry to the specified Hashtable. */
  @Deprecated
  @Override
  protected void addMeta(String key, Object value,
    Hashtable<String, Object> meta)
  {
    parser.addMeta(key, value, meta);
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, Object value) {
    addMeta(key, value, getGlobalMetadata());
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, boolean value) {
    addGlobalMeta(key, new Boolean(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, byte value) {
    addGlobalMeta(key, new Byte(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, short value) {
    addGlobalMeta(key, new Short(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, int value) {
    addGlobalMeta(key, new Integer(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, long value) {
    addGlobalMeta(key, new Long(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, float value) {
    addGlobalMeta(key, new Float(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, double value) {
    addGlobalMeta(key, new Double(value));
  }

  /** Adds an entry to the global metadata table. */
  @Deprecated
  @Override
  protected void addGlobalMeta(String key, char value) {
    addGlobalMeta(key, new Character(value));
  }

  /** Gets a value from the global metadata table. */
  @Deprecated
  @Override
  protected Object getGlobalMeta(String key) {
    return metadata.get(key);
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, Object value) {
    addMeta(key, value, reader.getCoreMetadata().getImageMetadata(getSeries()));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, boolean value) {
    addSeriesMeta(key, new Boolean(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, byte value) {
    addSeriesMeta(key, new Byte(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, short value) {
    addSeriesMeta(key, new Short(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, int value) {
    addSeriesMeta(key, new Integer(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, long value) {
    addSeriesMeta(key, new Long(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, float value) {
    addSeriesMeta(key, new Float(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, double value) {
    addSeriesMeta(key, new Double(value));
  }

  /** Adds an entry to the metadata table for the current series. */
  @Deprecated
  @Override
  protected void addSeriesMeta(String key, char value) {
    addSeriesMeta(key, new Character(value));
  }

  /** Gets an entry from the metadata table for the current series. */
  @Deprecated
  @Override
  protected Object getSeriesMeta(String key) {
    return reader.getCoreMetadata().getImageMetadata(getSeries()).get(key);
  }

  /** Reads a raw plane from disk. */
  @Deprecated
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, byte[] buf) throws IOException
  {
    return reader.readPlane(s, getSeries(), x, y, w, h, buf);
  }

  /** Reads a raw plane from disk. */
  @Deprecated
  @Override
  protected byte[] readPlane(RandomAccessInputStream s, int x, int y, int w,
    int h, int scanlinePad, byte[] buf) throws IOException
  {
    return reader.readPlane(s, getSeries(), x, y, w, h, buf);
  }

  /** Return a properly configured loci.formats.meta.FilterMetadata. */
  protected MetadataStore makeFilterMetadata() {
    return new FilterMetadata(getMetadataStore(), isMetadataFiltered());
  }

  // -- IMetadataConfigurable API methods --

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#getSupportedMetadataLevels()
   */
  public Set<MetadataLevel> getSupportedMetadataLevels() {
    Set<MetadataLevel> supportedLevels = new HashSet<MetadataLevel>();
    supportedLevels.add(MetadataLevel.ALL);
    supportedLevels.add(MetadataLevel.NO_OVERLAYS);
    supportedLevels.add(MetadataLevel.MINIMUM);
    return supportedLevels;
  }

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#getMetadataOptions()
   */
  @Deprecated
  @Override
  public MetadataOptions getMetadataOptions() {
    return metadataOptions;
  }

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#setMetadataOptions(loci.formats.in.MetadataOptions)
   */
  @Deprecated
  @Override
  public void setMetadataOptions(MetadataOptions options) {
    this.metadataOptions = options;
    
    ome.scifio.MetadataOptions sOpts = new ome.scifio.DefaultMetadataOptions();

    switch(options.getMetadataLevel()) {
      case ALL:
        sOpts.setMetadataLevel(ome.scifio.MetadataLevel.ALL);
        break;
      case NO_OVERLAYS:
        sOpts.setMetadataLevel(ome.scifio.MetadataLevel.NO_OVERLAYS);
        break;
      case MINIMUM:
        sOpts.setMetadataLevel(ome.scifio.MetadataLevel.MINIMUM);
        break;
    }
    
    parser.setMetadataOptions(sOpts);
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
  @Deprecated
  @Override
  public boolean isThisType(String name, boolean open) {
    return checker.isFormat(name, open);
  }

  /* @see IFormatReader#isThisType(byte[]) */
  @Deprecated
  @Override
  public boolean isThisType(byte[] block) {
    return checker.isFormat(block);
  }

  /* @see IFormatReader#isThisType(RandomAccessInputStream) */
  @Deprecated
  @Override
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    return checker.isFormat(stream);
  }

  /* @see IFormatReader#getImageCount() */
  @Deprecated
  @Override
  public int getImageCount() {
    return reader.getCoreMetadata().getPlaneCount(getSeries());
  }

  /* @see IFormatReader#isRGB() */
  @Deprecated
  @Override
  public boolean isRGB() {
    return reader.getCoreMetadata().isRGB(getSeries());
  }

  /* @see IFormatReader#getSizeX() */
  @Deprecated
  @Override
  public int getSizeX() {
    return reader.getCoreMetadata().getAxisLength(getSeries(), Axes.X);
  }

  /* @see IFormatReader#getSizeY() */
  @Deprecated
  @Override
  public int getSizeY() {
    return reader.getCoreMetadata().getAxisLength(getSeries(), Axes.Y);
  }

  /* @see IFormatReader#getSizeZ() */
  @Deprecated
  @Override
  public int getSizeZ() {
    return reader.getCoreMetadata().getAxisLength(getSeries(), Axes.Z);
  }

  /* @see IFormatReader#getSizeC() */
  @Deprecated
  @Override
  public int getSizeC() {
    return reader.getCoreMetadata().getAxisLength(getSeries(), Axes.CHANNEL);
  }

  /* @see IFormatReader#getSizeT() */
  @Deprecated
  @Override
  public int getSizeT() {
    return reader.getCoreMetadata().getAxisLength(getSeries(), Axes.TIME);
  }

  /* @see IFormatReader#getPixelType() */
  @Deprecated
  @Override
  public int getPixelType() {
    return reader.getCoreMetadata().getPixelType(getSeries());
  }

  /* @see IFormatReader#getBitsPerPixel() */
  @Deprecated
  @Override
  public int getBitsPerPixel() {
    return reader.getCoreMetadata().getBitsPerPixel(getSeries());
  }

  /* @see IFormatReader#getEffectiveSizeC() */
  @Deprecated
  @Override
  public int getEffectiveSizeC() {
    // NB: by definition, imageCount == effectiveSizeC * sizeZ * sizeT
    int sizeZT = getSizeZ() * getSizeT();
    if (sizeZT == 0) return 0;
    return getImageCount() / sizeZT;
  }

  /* @see IFormatReader#getRGBChannelCount() */
  @Deprecated
  @Override
  public int getRGBChannelCount() {
    int effSizeC = getEffectiveSizeC();
    if (effSizeC == 0) return 0;
    return getSizeC() / effSizeC;
  }

  /* @see IFormatReader#isIndexed() */
  @Deprecated
  @Override
  public boolean isIndexed() {
    return reader.getCoreMetadata().isIndexed(getSeries());
  }

  /* @see IFormatReader#isFalseColor() */
  @Deprecated
  @Override
  public boolean isFalseColor() {
    return reader.getCoreMetadata().isFalseColor(getSeries());
  }

  /* @see IFormatReader#get8BitLookupTable() */
  @Deprecated
  @Override
  public byte[][] get8BitLookupTable() throws FormatException, IOException {
    try {
      return reader.getCoreMetadata().get8BitLookupTable(getSeries());
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#get16BitLookupTable() */
  @Deprecated
  @Override
  public short[][] get16BitLookupTable() throws FormatException, IOException {
    try {
      return reader.getCoreMetadata().get16BitLookupTable(getSeries());
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#getChannelDimLengths() */
  @Deprecated
  @Override
  public int[] getChannelDimLengths() {
    return reader.getCoreMetadata().getChannelDimLengths(getSeries());
  }

  /* @see IFormatReader#getChannelDimTypes() */
  @Deprecated
  @Override
  public String[] getChannelDimTypes() {
    return reader.getCoreMetadata().getChannelDimTypes(getSeries());
  }

  /* @see IFormatReader#getThumbSizeX() */
  @Deprecated
  @Override
  public int getThumbSizeX() {
    return reader.getCoreMetadata().getThumbSizeX(getSeries());
  }

  /* @see IFormatReader#getThumbSizeY() */
  @Deprecated
  @Override
  public int getThumbSizeY() {
    return reader.getCoreMetadata().getThumbSizeY(getSeries());
  }

  /* @see IFormatReader.isLittleEndian() */
  @Deprecated
  @Override
  public boolean isLittleEndian() {
    return reader.getCoreMetadata().isLittleEndian(getSeries());
  }

  /* @see IFormatReader#getDimensionOrder() */
  @Deprecated
  @Override
  public String getDimensionOrder() {
    return ome.scifio.util.FormatTools.findDimensionOrder(reader, getSeries());
  }

  /* @see IFormatReader#isOrderCertain() */
  @Deprecated
  @Override
  public boolean isOrderCertain() {
    return reader.getCoreMetadata().isOrderCertain(getSeries());
  }

  /* @see IFormatReader#isThumbnailSeries() */
  @Deprecated
  @Override
  public boolean isThumbnailSeries() {
    return reader.getCoreMetadata().isThumbnailImage(getSeries());
  }

  /* @see IFormatReader#isInterleaved() */
  @Deprecated
  @Override
  public boolean isInterleaved() {
    return reader.getCoreMetadata().isInterleaved(0);
  }

  /* @see IFormatReader#isInterleaved(int) */
  @Deprecated
  @Override
  public boolean isInterleaved(int subC) {
    return reader.getCoreMetadata().isInterleaved(getSeries());
  }

  /* @see IFormatReader#openBytes(int) */
  @Deprecated
  @Override
  public byte[] openBytes(int no) throws FormatException, IOException {
    try {
      return reader.openBytes(getSeries(), no);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#openBytes(int, byte[]) */
  @Deprecated
  @Override
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    try {
      return reader.openBytes(getSeries(), no, buf);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#openBytes(int, int, int, int, int) */
  @Deprecated
  @Override
  public byte[] openBytes(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return reader.openBytes(getSeries(), no, x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#openBytes(int, byte[], int, int, int, int) */
  public abstract byte[] openBytes(int no, byte[] buf, int x, int y, int w,
    int h) throws FormatException, IOException;

  /* @see IFormatReader#openPlane(int, int, int, int, int int) */
  @Deprecated
  @Override
  public Object openPlane(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    // NB: Readers use byte arrays by default as the native type.
    return openBytes(no, x, y, w, h);
  }

  /* @see IFormatReader#openThumbBytes(int) */
  @Deprecated
  @Override
  public byte[] openThumbBytes(int no) throws FormatException, IOException {
    try {
      return reader.openThumbBytes(getSeries(), no);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatReader#close(boolean) */
  @Deprecated
  @Override
  public void close(boolean fileOnly) throws IOException {
    parser.close(fileOnly);
    reader.close(fileOnly);
  }

  /* @see IFormatReader#getSeriesCount() */
  @Deprecated
  @Override
  public int getSeriesCount() {
    return reader.getCoreMetadata().getImageCount();
  }

  /* @see IFormatReader#setSeries(int) */
  public void setSeries(int no) {
    if (no < 0 || no >= getSeriesCount()) {
      throw new IllegalArgumentException("Invalid series: " + no);
    }
    series = no;
  }

  /* @see IFormatReader#getSeries() */
  public int getSeries() {
    return series;
  }

  /* @see IFormatReader#setGroupFiles(boolean) */
  @Deprecated
  @Override
  public void setGroupFiles(boolean groupFiles) {
    reader.setGroupFiles(groupFiles);
  }

  /* @see IFormatReader#isGroupFiles() */
  @Deprecated
  @Override
  public boolean isGroupFiles() {
    return reader.isGroupFiles();
  }

  /* @see IFormatReader#fileGroupOption(String) */
  @Deprecated
  @Override
  public int fileGroupOption(String id) throws FormatException, IOException {
    return ome.scifio.util.FormatTools.CANNOT_GROUP;
  }

  /* @see IFormatReader#isMetadataComplete() */
  @Deprecated
  @Override
  public boolean isMetadataComplete() {
    return reader.getCoreMetadata().isMetadataComplete(getSeries());
  }

  /* @see IFormatReader#setNormalized(boolean) */
  @Deprecated
  @Override
  public void setNormalized(boolean normalize) {
    reader.setNormalized(normalize);
  }

  /* @see IFormatReader#isNormalized() */
  @Deprecated
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
  @Deprecated
  @Override
  public void setOriginalMetadataPopulated(boolean populate) {
    parser.setOriginalMetadataPopulated(populate);
  }

  /* @see IFormatReader#isOriginalMetadataPopulated() */
  @Deprecated
  @Override
  public boolean isOriginalMetadataPopulated() {
    return parser.isOriginalMetadataPopulated();
  }

  /* @see IFormatReader#getUsedFiles() */
  @Deprecated
  @Override
  public String[] getUsedFiles() {
    return parser.getUsedFiles();
  }

  /* @see IFormatReader#getUsedFiles() */
  @Deprecated
  @Override
  public String[] getUsedFiles(boolean noPixels) {
    return parser.getUsedFiles(noPixels);
  }

  /* @see IFormatReader#getSeriesUsedFiles() */
  @Deprecated
  @Override
  public String[] getSeriesUsedFiles() {
    return parser.getImageUsedFiles(getSeries());
  }

  /* @see IFormatReader#getSeriesUsedFiles(boolean) */
  @Deprecated
  @Override
  public String[] getSeriesUsedFiles(boolean noPixels) {
    return parser.getImageUsedFiles(getSeries(), noPixels);
  }

  /* @see IFormatReader#getAdvancedUsedFiles(boolean) */
  @Deprecated
  @Override
  public FileInfo[] getAdvancedUsedFiles(boolean noPixels) {
    ome.scifio.FileInfo[] tmpInfo = parser.getAdvancedUsedFiles(noPixels);
    return convertFileInfo(tmpInfo);
  }

  /* @see IFormatReader#getAdvancedSeriesUsedFiles(boolean) */
  @Deprecated
  @Override
  public FileInfo[] getAdvancedSeriesUsedFiles(boolean noPixels) {
    ome.scifio.FileInfo[] tmpInfo =
      parser.getAdvancedImageUsedFiles(getSeries(), noPixels);
    return convertFileInfo(tmpInfo);
  }

  private FileInfo[] convertFileInfo(ome.scifio.FileInfo[] source) {
    FileInfo[] info = new FileInfo[source.length];

    for (int i = 0; i < source.length; i++) {
      info[i] = new FileInfo();
      info[i].filename = source[i].filename;
      info[i].reader = source[i].reader;
      info[i].usedToInitialize = source[i].usedToInitialize;
    }

    return info;
  }

  /* @see IFormatReader#getCurrentFile() */
  public String getCurrentFile() {
    return currentId;
  }

  /* @see IFormatReader#getIndex(int, int, int) */
  @Deprecated
  @Override
  public int getIndex(int z, int c, int t) {
    return ome.scifio.util.FormatTools.getIndex(reader, getSeries(), z, c, t);
  }

  /* @see IFormatReader#getZCTCoords(int) */
  @Deprecated
  @Override
  public int[] getZCTCoords(int index) {
    return ome.scifio.util.FormatTools.getZCTCoords(reader, index);
  }

  /* @see IFormatReader#getMetadataValue(String) */
  @Deprecated
  @Override
  public Object getMetadataValue(String field) {
    return reader.getCoreMetadata().getMetadataValue(getSeries(), field);
  }

  /* @see IFormatReader#getSeriesMetadataValue(String) */
  @Deprecated
  @Override
  public Object getSeriesMetadataValue(String field) {
    return reader.getCoreMetadata().getImageMetadataValue(getSeries(), field);
  }

  /* @see IFormatReader#getGlobalMetadata() */
  @Deprecated
  @Override
  public Hashtable<String, Object> getGlobalMetadata() {
    return reader.getCoreMetadata().getGlobalMetadata();
  }

  /* @see IFormatReader#getSeriesMetadata() */
  @Deprecated
  @Override
  public Hashtable<String, Object> getSeriesMetadata() {
    return reader.getCoreMetadata().getImageMetadata(getSeries());
  }

  @Deprecated
  @Override
  public Hashtable<String, Object> getMetadata() {
    FormatTools.assertId(currentId, true, 1);
    Hashtable<String, Object> h =
      new Hashtable<String, Object>(getGlobalMetadata());
    int oldSeries = getSeries();

    for (int series = 0; series < getSeriesCount(); series++) {
      String name = "Series " + series;
      setSeries(series);
      ome.scifio.util.SCIFIOMetadataTools.merge(getSeriesMetadata(), h, name + " ");
    }

    setSeries(oldSeries);
    return h;
  }

  /* @see IFormatReader#getCoreMetadata() */
  @Deprecated
  @Override
  public CoreMetadata[] getCoreMetadata() {
    return core;
  }

  /* @see IFormatReader#setMetadataFiltered(boolean) */
  @Deprecated
  @Override
  public void setMetadataFiltered(boolean filter) {
    parser.setMetadataFiltered(filter);
  }

  /* @see IFormatReader#isMetadataFiltered() */
  @Deprecated
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
  @Deprecated
  @Override
  public String[] getPossibleDomains(String id)
    throws FormatException, IOException
  {
    return reader.getDomains();
  }

  /* @see IFormatReader#getDomains() */
  @Deprecated
  @Override
  public String[] getDomains() {
    FormatTools.assertId(currentId, true, 1);
    return reader.getDomains();
  }

  /* @see IFormatReader#getOptimalTileWidth() */
  @Deprecated
  @Override
  public int getOptimalTileWidth() {
    return reader.getOptimalTileWidth(getSeries());
  }

  /* @see IFormatReader#getOptimalTileHeight() */
  @Deprecated
  @Override
  public int getOptimalTileHeight() {
    return reader.getOptimalTileHeight(getSeries());
  }

  // -- IFormatHandler API methods --

  /* @see IFormatHandler#isThisType(String) */
  @Deprecated
  @Override
  public boolean isThisType(String name) {
    // if necessary, open the file for further analysis
    return checker.isFormat(name, true);
  }

  /* @see IFormatHandler#setId(String) */
  @Deprecated
  @Override
  public void setId(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) {
      initFile(id);
      // NB: this has to happen after init file, instead of within init file
      // to allow the file to be parsed
      core = new CoreMetadata[getSeriesCount()];
      for (int i = 0; i < getSeriesCount(); i++) {
        core[i] = new CoreMetadata(reader.getCoreMetadata(), i);
        core[i].orderCertain = true;
      }
    }
    reader.setSource(id);
  }

  /* @see IFormatHandler#close() */
  @Deprecated
  @Override
  public void close() throws IOException {
    parser.close();
    reader.close();
  }

  // -- SCIFIO Utility Methods --

  /** Returns the SCIFIO reader being used for deferrment */
  public ome.scifio.Reader<T> getReader() {
    return reader;
  }

  /** Returns the SCIFIO checker being used for deferrment */
  public Checker<T> getChecker() {
    return checker;
  }

  /** Returns the SCIFIO parser being used for deferrment */
  public Parser<T> getParser() {
    return parser;
  }

  /** Returns the SCIFIO translator being used for deferrment */
  public Translator<T, ome.scifio.CoreMetadata> getTranslator() {
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
