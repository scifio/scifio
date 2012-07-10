package ome.scifio;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import ome.scifio.common.DataTools;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of all SCIFIO Parser components.
 *
 */
public abstract class AbstractParser<M extends Metadata>
  extends AbstractHasContext implements Parser<M> {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

  // -- Fields --

  /** Class pointer to circumvent type erasure */
  private Class<M> metaClass;

  /** Current file. */
  protected RandomAccessInputStream in;

  /** Type-specific metadata values. */
  protected M metadata;

  /** Core metadata values. */
  protected CoreMetadata cMeta;

  /** Name of current file. */
  protected String currentId;

  /** Whether or not to filter out invalid metadata. */
  protected boolean filterMetadata;

  /** Whether or not to save proprietary metadata in the MetadataStore. */
  protected boolean saveOriginalMetadata = false;

  /** Metadata parsing options. */
  protected MetadataOptions metadataOptions = new DefaultMetadataOptions();

  // -- Constructors --

  public AbstractParser(final SCIFIO ctx, Class<M> mClass) {
    super(ctx);
    metaClass = mClass;
    cMeta = new CoreMetadata();
  }

  // -- HasFormat API Methods --

  @SuppressWarnings("unchecked")
  public Format<M, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromParser(getClass());
  }

  // -- Parser API Methods --

  /* @see Parser#parse(File file) */
  public M parse(final File file) throws IOException, FormatException {
    return parse(file.getPath());
  }

  /* @see Parser#parse(String fileName) */
  public M parse(final String fileName) throws IOException, FormatException {
    return parse(new RandomAccessInputStream(fileName));
  }

  /* @see Parser#parse(RandomAccessInputStream stream) */
  public M parse(final RandomAccessInputStream stream)
    throws IOException, FormatException
  {
    try {
      M meta = metaClass.newInstance();
      meta.setContext(this.getContext());
      return parse(stream, meta);
    }
    catch (final InstantiationException e) {
      throw new FormatException(e);
    }
    catch (final IllegalAccessException e) {
      throw new FormatException(e);
    }
  }

  /* @see Parser#parse(File, M) */
  public M parse(final File file, final M meta)
    throws IOException, FormatException
  {
    return parse(file.getPath(), meta);
  }

  /* @see Parser#parse(String, M) */
  public M parse(final String fileName, final M meta)
    throws IOException, FormatException
  {
    return parse(new RandomAccessInputStream(fileName), meta);
  }

  /* @see Parser#parse(RandomAccessInputStream, M) */
  public M parse(final RandomAccessInputStream stream, final M meta)
    throws IOException, FormatException
  {
    metadata = meta;
    if (in == null || !in.getFileName().equals(stream.getFileName())) {
      init(stream);

      if (saveOriginalMetadata) {
        //TODO store all metadata in OMEXML store.. or equivalent function? as per setId.. or handle via annotations
      }
    }
    
    return metadata;
  }

  /* @see Parser#close(boolean) */
  public void close(final boolean fileOnly) throws IOException {
    if (in != null) in.close();
    if (!fileOnly) {
      in = null;
    }
  }

  /* @see Parser#close() */
  public void close() throws IOException {
    close(false);
  }

  /* @see Parser#setOriginalMetadataPopulated(boolean) */
  public void setOriginalMetadataPopulated(final boolean populate) {
    FormatTools.assertStream(in, false, 1);
    saveOriginalMetadata = populate;
  }

  /* @see Parser#isOriginalMetadataPopulated() */
  public boolean isOriginalMetadataPopulated() {
    return saveOriginalMetadata;
  }

  /* @see Parser#getUsedFiles() */
  public String[] getUsedFiles() {
    return getUsedFiles(false);
  }

  /* @see Parser#getUsedFiles() */
  public String[] getUsedFiles(final boolean noPixels) {
    final Vector<String> files = new Vector<String>();
    for (int i = 0; i < cMeta.getImageCount(); i++) {
      final String[] s = getImageUsedFiles(i, noPixels);
      if (s != null) {
        for (final String file : s) {
          if (!files.contains(file)) {
            files.add(file);
          }
        }
      }
    }
    return files.toArray(new String[files.size()]);
  }

  /* @see Parser#setMetadataFiltered(boolean) */
  public void setMetadataFiltered(final boolean filter) {
    FormatTools.assertStream(in, false, 1);
    filterMetadata = filter;
  }

  /* @see Parser#isMetadataFiltered() */
  public boolean isMetadataFiltered() {
    return filterMetadata;
  }

  /* @see Parser#getImageUsedFiles() */
  public String[] getImageUsedFiles(final int imageIndex) {
    return getImageUsedFiles(imageIndex, false);
  }

  /* @see Parser#getImageUsedFiles(boolean) */
  public String[] getImageUsedFiles(final int imageIndex, final boolean noPixels)
  {
    return noPixels ? null : new String[] {in.getFileName()};
  }

  /* @see Parser#getAdvancedUsedFiles(boolean) */
  public FileInfo[] getAdvancedUsedFiles(final boolean noPixels) {
    final String[] files = getUsedFiles(noPixels);
    if (files == null) return null;
    return getFileInfo(files);
  }

  /* @see Parser#getAdvancedSeriesUsedFiles(boolean) */
  public FileInfo[] getAdvancedImageUsedFiles(final int imageIndex,
    final boolean noPixels)
  {
    final String[] files = getImageUsedFiles(imageIndex, noPixels);
    if (files == null) return null;
    return getFileInfo(files);
  }

  /* (non-Javadoc)
   * @see ome.scifio.Parser#getSupportedMetadataLevels()
   */
  public Set<MetadataLevel> getSupportedMetadataLevels() {
    final Set<MetadataLevel> supportedLevels = new HashSet<MetadataLevel>();
    supportedLevels.add(MetadataLevel.ALL);
    supportedLevels.add(MetadataLevel.NO_OVERLAYS);
    supportedLevels.add(MetadataLevel.MINIMUM);
    return supportedLevels;
  }

  /* (non-Javadoc)
   * @see ome.scifio.Parser#getMetadataOptions()
   */
  public MetadataOptions getMetadataOptions() {
    return metadataOptions;
  }

  /* (non-Javadoc)
   * @see ome.scifio.Parser#setMetadataOptions(ome.scifio.MetadataOptions)
   */
  public void setMetadataOptions(final MetadataOptions options) {
    this.metadataOptions = options;
  }

  // -- AbstractParser Methods --

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final Object value) {
    addMeta(key, value, cMeta.getDatasetMetadata());
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final boolean value) {
    addGlobalMeta(key, new Boolean(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final byte value) {
    addGlobalMeta(key, new Byte(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final short value) {
    addGlobalMeta(key, new Short(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final int value) {
    addGlobalMeta(key, new Integer(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final long value) {
    addGlobalMeta(key, new Long(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final float value) {
    addGlobalMeta(key, new Float(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final double value) {
    addGlobalMeta(key, new Double(value));
  }

  /** Adds an entry to the global metadata table. */
  public void addGlobalMeta(final String key, final char value) {
    addGlobalMeta(key, new Character(value));
  }

  /** Gets a value from the global metadata table. */
  public Object getGlobalMeta(final String key) {
    return cMeta.getDatasetMetadata().get(key);
  }

  /** Adds an entry to the specified Hashtable. */
  public void addMeta(String key, Object value,
    final Hashtable<String, Object> meta)
  {
    if (key == null || value == null /* || TODO !isMetadataCollected() */) {
      return;
    }

    key = key.trim();

    final boolean string =
      value instanceof String || value instanceof Character;
    final boolean simple =
      string || value instanceof Number || value instanceof Boolean;

    // string value, if passed in value is a string
    String val = string ? String.valueOf(value) : null;

    if (filterMetadata || (saveOriginalMetadata
    /* TODO: check if this Parser's metadata is OMEXML metadata &&
     *  (getMetadataStore() instanceof OMEXMLMetadata)*/))
    {
      // filter out complex data types
      if (!simple) return;

      // verify key & value are reasonable length
      final int maxLen = 8192;
      if (key.length() > maxLen) return;
      if (string && val.length() > maxLen) return;

      // remove all non-printable characters
      key = DataTools.sanitize(key);
      if (string) val = DataTools.sanitize(val);

      // verify key contains at least one alphabetic character
      if (!key.matches(".*[a-zA-Z].*")) return;

      // remove &lt;, &gt; and &amp; to prevent XML parsing errors
      final String[] invalidSequences =
        new String[] {"&lt;", "&gt;", "&amp;", "<", ">", "&"};
      for (int i = 0; i < invalidSequences.length; i++) {
        key = key.replaceAll(invalidSequences[i], "");
        if (string) val = val.replaceAll(invalidSequences[i], "");
      }

      // verify key & value are not empty
      if (key.length() == 0) return;
      if (string && val.trim().length() == 0) return;

      if (string) value = val;
    }

    meta.put(key, val == null ? value : val);
  }

  /* Sets the input stream for this parser if provided a new stream */
  private void init(final RandomAccessInputStream stream) throws IOException {

    if (in != null) {
      final String[] s = getUsedFiles();
      for (int i = 0; i < s.length; i++) {
        if (in.getFileName().equals(s[i])) return;
      }
    }

    close();
    this.in = stream;
    this.metadata.setSource(in);
  }

  /* Builds a FileInfo array around the provided array of file names */
  private FileInfo[] getFileInfo(String[] files) {
    final FileInfo[] infos = new FileInfo[files.length];
    for (int i = 0; i < infos.length; i++) {
      infos[i] = new FileInfo();
      infos[i].filename = files[i];
      infos[i].reader = getFormat().getReaderClass();
      infos[i].usedToInitialize = files[i].endsWith(in.getFileName());
    }
    return infos;
  }
}