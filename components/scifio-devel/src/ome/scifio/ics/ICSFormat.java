package ome.scifio.ics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.Axes.CustomAxisType;

import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.AbstractReader;
import ome.scifio.AbstractTranslator;
import ome.scifio.AbstractWriter;
import ome.scifio.CoreImageMetadata;
import ome.scifio.CoreMetadata;
import ome.scifio.CoreTranslator;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.discovery.SCIFIOFormat;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

@SCIFIOFormat
public class ICSFormat
extends
AbstractFormat<ICSFormat.Metadata, ICSFormat.Checker,
    ICSFormat.Parser, ICSFormat.Reader,
    ICSFormat.Writer> {

  // -- Constructor --

  public ICSFormat() throws FormatException {
    this(null);
  }

  public ICSFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, Metadata.class, Checker.class, Parser.class, Reader.class, Writer.class);
  }
  
  // -- Accessor methods for private classes --
  
  public boolean isVersionTwo(Metadata m) {
    return (m instanceof Metadata) && ((Metadata)m).isVersionTwo();
  }

  /**
   * SCIFIO Metadata object for ICS images. 
   *
   */
  public static class Metadata extends AbstractMetadata {
  
    // -- Fields -- 
  
    /** Whether this file is ICS version 2,
     *  and thus does not have an IDS companion */
    protected boolean versionTwo = false;
  
    /** Offset to pixel data */
    protected long offset = -1;
  
    /** */
    protected boolean hasInstrumentData = false;
  
    /** ICS file name */
    protected String icsId = "";
  
    /** IDS file name */
    protected String idsId = "";
  
    /** ICS Metadata */
    protected Hashtable<String, String> keyValPairs;
  
    // -- Constructor --
  
    public Metadata() {
      this(null);
    }
  
    public Metadata(SCIFIO ctx) {
      super(ctx);
      this.keyValPairs = new Hashtable<String, String>();
    }
  
    // -- Helper Methods --
  
    /* @see Metadata#resetMeta() */
    public void reset() {
      super.reset(this.getClass());
      this.keyValPairs = new Hashtable<String, String>();
    }
  
    /**
     * Convenience method to directly access the hashtable.
     * @param key
     * @return
     */
    public String get(final String key) {
      return this.keyValPairs.get(key);
    }
  
    public String getIcsId() {
      return icsId;
    }
  
    public void setIcsId(final String icsId) {
      this.icsId = icsId;
    }
  
    public String getIdsId() {
      return idsId;
    }
  
    public void setIdsId(final String idsId) {
      this.idsId = idsId;
    }
  
    public boolean hasInstrumentData() {
      return hasInstrumentData;
    }
  
    public void setHasInstrumentData(final boolean hasInstrumentData) {
      this.hasInstrumentData = hasInstrumentData;
    }
  
    public boolean isVersionTwo() {
      return versionTwo;
    }
  
    public void setVersionTwo(final boolean versionTwo) {
      this.versionTwo = versionTwo;
    }
  
    public void setKeyValPairs(Hashtable<String, String> keyValPairs) {
      this.keyValPairs = keyValPairs;
    }
  
    public Hashtable<String, String> getKeyValPairs() {
      return keyValPairs;
    }
  }

  /**
   * SCIFIO file format Checker for ICS images.
   * 
   */
  public static class Checker extends AbstractChecker<Metadata> {

    // -- Constructor --

    public Checker() {
      this(null);
    }

    public Checker(final SCIFIO ctx) {
      super("Image Cytometry Standard", new String[] {"ics", "ids"}, ctx);
    }

    // -- Checker API Methods --

  }

  /**
   * SCIFIO file format Parser for ICS images. 
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
  
    // -- Constructor --
  
    public Parser() {
      this(null);
    }
  
    public Parser(final SCIFIO ctx) {
      super(ctx, Metadata.class);
    }
  
    // -- Parser API Methods --
  
    /* @see Parser#parse(RandomAccessInputStream) */
    @Override
    public Metadata parse(final RandomAccessInputStream stream)
      throws IOException, FormatException
      {
      super.parse(stream);
  
      if (stream.getFileName() != null) findCompanion(stream.getFileName());
  
      final RandomAccessInputStream reader =
        new RandomAccessInputStream(metadata.getIcsId());
  
      reader.seek(0);
      reader.readString(ICSUtils.NL);
      String line = reader.readString(ICSUtils.NL);
  
      // Extracts the keys, value pairs from each line and inserts them into the ICSMetadata object
      while (line != null && !line.trim().equals("end") &&
        reader.getFilePointer() < reader.length() - 1)
      {
        line = line.trim();
        final String[] tokens = line.split("[ \t]");
        final StringBuffer key = new StringBuffer();
        for (int q = 0; q < tokens.length; q++) {
          tokens[q] = tokens[q].trim();
          if (tokens[q].length() == 0) continue;
  
          boolean foundValue = true;
          for (int i = 0; i < ICSUtils.CATEGORIES.length; i++) {
            if (tokens[q].matches(ICSUtils.CATEGORIES[i])) {
              foundValue = false;
              break;
            }
          }
          if (!foundValue) {
            key.append(tokens[q]);
            key.append(" ");
            continue;
          }
  
          final StringBuffer value = new StringBuffer(tokens[q++]);
          for (; q < tokens.length; q++) {
            value.append(" ");
            value.append(tokens[q].trim());
          }
          final String k = key.toString().trim().replaceAll("\t", " ");
          final String v = value.toString().trim();
          addGlobalMeta(k, v);
          metadata.keyValPairs.put(k.toLowerCase(), v);
        }
        line = reader.readString(ICSUtils.NL);
      }
      reader.close();
  
      if (metadata.versionTwo) {
        String s = in.readString(ICSUtils.NL);
        while (!s.trim().equals("end"))
          s = in.readString(ICSUtils.NL);
      }
  
      metadata.offset = in.getFilePointer();
  
      metadata.hasInstrumentData =
        nullKeyCheck(new String[] {
          "history cube emm nm", "history cube exc nm", "history objective NA",
          "history stage xyzum", "history objective magnification",
          "history objective mag", "history objective WorkingDistance",
          "history objective type", "history objective",
        "history objective immersion"});
  
      return metadata;
      }
  
    // -- Helper Methods --
  
    /* Returns true if any of the keys in testKeys has a non-null value */
    private boolean nullKeyCheck(final String[] testKeys) {
      for (final String key : testKeys) {
        if (metadata.get(key) != null) {
          return true;
        }
      }
      return false;
    }
  
    /* Finds the companion file (ICS and IDS are companions) */
    private void findCompanion(final String id)
      throws IOException, FormatException
      {
      metadata.icsId = id;
      metadata.idsId = id;
      String icsId = id, idsId = id;
      final int dot = id.lastIndexOf(".");
      final String ext = dot < 0 ? "" : id.substring(dot + 1).toLowerCase();
      if (ext.equals("ics")) {
        // convert C to D regardless of case
        final char[] c = idsId.toCharArray();
        c[c.length - 2]++;
        idsId = new String(c);
      }
      else if (ext.equals("ids")) {
        // convert D to C regardless of case
        final char[] c = icsId.toCharArray();
        c[c.length - 2]--;
        icsId = new String(c);
      }
  
      if (icsId == null) throw new FormatException("No ICS file found.");
      final Location icsFile = new Location(icsId);
      if (!icsFile.exists()) throw new FormatException("ICS file not found.");
  
      // check if we have a v2 ICS file - means there is no companion IDS file
      final RandomAccessInputStream f = new RandomAccessInputStream(icsId);
      if (f.readString(17).trim().equals("ics_version\t2.0")) {
        in = new RandomAccessInputStream(icsId);
        metadata.versionTwo = true;
      }
      else {
        if (idsId == null) throw new FormatException("No IDS file found.");
        final Location idsFile = new Location(idsId);
        if (!idsFile.exists()) throw new FormatException("IDS file not found.");
        //currentIdsId = idsId;
        metadata.idsId = idsId;
        in = new RandomAccessInputStream(idsId);
      }
      f.close();
      }
  }

  /**
   * File format SCIFIO Reader for Image Cytometry Standard (ICS)
   * images. Version 1 and 2 supported.
   * 
   */
  public static class Reader extends AbstractReader<Metadata> {
  
    // -- Fields --
  
    private int prevImage;
  
    /** Whether or not the pixels are GZIP-compressed. */
  
    private boolean gzip;
  
    private GZIPInputStream gzipStream;
  
    /** Whether or not the image is inverted along the Y axis. */
    private boolean invertY; //TODO only in oldInitFile
  
    /** Whether or not the channels represent lifetime histogram bins. */
    private boolean lifetime; //TODO only in oldInitFile
  
    /** Image data. */
    private byte[] data;
  
    private boolean storedRGB = false; // TODO only in oldInitFile
  
    // -- Constructor --
  
    public Reader() {
      this(null);
    }
  
    public Reader(final SCIFIO ctx) {
      super("Image Cytometry Standard", new String[] {"ics", "ids"}, ctx);
      domains =
        new String[] {
        FormatTools.LM_DOMAIN, FormatTools.FLIM_DOMAIN,
        FormatTools.UNKNOWN_DOMAIN};
      hasCompanionFiles = true;
    }
  
    // -- Reader API Methods --
  
    @Override
    public byte[] openBytes(final int imageIndex, final int planeIndex,
      final byte[] buf, final int x, final int y, final int w, final int h)
        throws FormatException, IOException
        {
      FormatTools.checkPlaneParameters(
        this, imageIndex, planeIndex, buf.length, x, y, w, h);
  
      final int bpp =
        FormatTools.getBytesPerPixel(cMeta.getPixelType(imageIndex));
      final int len = FormatTools.getPlaneSize(this, imageIndex);
      final int pixel = bpp * cMeta.getRGBChannelCount(imageIndex);
      final int rowLen = FormatTools.getPlaneSize(this, w, 1, imageIndex);
  
      final int[] coordinates = getZCTCoords(planeIndex);
      final int[] prevCoordinates = getZCTCoords(prevImage);
  
      if (!gzip) {
        in.seek(metadata.offset + planeIndex * (long) len);
      }
      else {
        long toSkip = (planeIndex - prevImage - 1) * (long) len;
        if (gzipStream == null || planeIndex <= prevImage) {
          FileInputStream fis = null;
          toSkip = planeIndex * (long) len;
          if (metadata.versionTwo) {
            fis = new FileInputStream(metadata.icsId);
            fis.skip(metadata.offset);
          }
          else {
            fis = new FileInputStream(metadata.idsId);
            toSkip += metadata.offset;
          }
          try {
            gzipStream = new GZIPInputStream(fis);
          }
          catch (final IOException e) {
            // the 'gzip' flag is set erroneously
            gzip = false;
            in.seek(metadata.offset + planeIndex * (long) len);
            gzipStream = null;
          }
        }
  
        if (gzipStream != null) {
          while (toSkip > 0) {
            toSkip -= gzipStream.skip(toSkip);
          }
  
          data =
            new byte[len *
                     (storedRGB ? cMeta.getAxisLength(imageIndex, Axes.CHANNEL) : 1)];
          int toRead = data.length;
          while (toRead > 0) {
            toRead -= gzipStream.read(data, data.length - toRead, toRead);
          }
        }
      }
  
      final int sizeC =
        lifetime ? 1 : cMeta.getAxisLength(imageIndex, Axes.CHANNEL);
  
      final int channelLengths = 0;
  
      if (!cMeta.isRGB(imageIndex) &&
          cMeta.getChannelDimLengths(imageIndex).length == 1 && storedRGB)
      {
        // channels are stored interleaved, but because there are more than we
        // can display as RGB, we need to separate them
        in.seek(metadata.offset +
            (long) len *
            FormatTools.getIndex(
                this, imageIndex, coordinates[0], 0, coordinates[2]));
        if (!gzip && data == null) {
          data = new byte[len * cMeta.getAxisLength(imageIndex, Axes.CHANNEL)];
          in.read(data);
        }
        else if (!gzip &&
            (coordinates[0] != prevCoordinates[0] || coordinates[2] != prevCoordinates[2]))
        {
          in.read(data);
        }
  
        for (int row = y; row < h + y; row++) {
          for (int col = x; col < w + x; col++) {
            int src =
                bpp * ((planeIndex % cMeta.getAxisLength(imageIndex, Axes.CHANNEL))
                    + sizeC * (row * (row * cMeta.getAxisLength(imageIndex, Axes.X) + col)));
            int dest = bpp * ((row - y) * w + (col - x));
            System.arraycopy(data, src, buf, dest, bpp); 
          }
        }
      }
      else if (gzip) {
        final RandomAccessInputStream s = new RandomAccessInputStream(data);
        readPlane(s, imageIndex, x, y, w, h, buf);
        s.close();
      }
      else {
        readPlane(in, imageIndex, x, y, w, h, buf);
      }
  
      if (invertY) {
        final byte[] row = new byte[rowLen];
        for (int r = 0; r < h / 2; r++) {
          final int topOffset = r * rowLen;
          final int bottomOffset = (h - r - 1) * rowLen;
          System.arraycopy(buf, topOffset, row, 0, rowLen);
          System.arraycopy(buf, bottomOffset, buf, topOffset, rowLen);
          System.arraycopy(row, 0, buf, bottomOffset, rowLen);
        }
      }
  
      prevImage = planeIndex;
  
      return buf;
        }
  
    /* @see Reader#close(boolean) */
    @Override
    public void close(final boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        data = null;
        gzip = false;
        invertY = false;
        lifetime = false;
        prevImage = 0;
        //TODO hasInstrumentData = false;
        storedRGB = false;
        if (gzipStream != null) {
          gzipStream.close();
        }
        gzipStream = null;
      }
    }
  
    /* @see Reader#setMetadata(Metadata) */
    @Override
    public void setMetadata(final Metadata meta) throws IOException {
      super.setMetadata(meta);
      this.metadata = meta;
      final Translator<Metadata, CoreMetadata> t = new ICSCoreTranslator();
      t.translate(meta, cMeta);
      gzip = metadata.get("representation compression").equals("gzip");
    }
  
    /* @see Reader#setSource(RandomAccessInputStream) */
    @Override
    public void setSource(final RandomAccessInputStream stream) throws IOException {
      super.setSource(stream);
      if(!this.getMetadata().versionTwo)
        this.in = new RandomAccessInputStream(this.getMetadata().idsId);
    }
  
    @Override
    public String[] getDomains() {
      FormatTools.assertStream(in, true, 0);
      final String[] domain = new String[] {FormatTools.GRAPHICS_DOMAIN};
      if (cMeta.getChannelDimLengths(0).length > 1) {
        domain[0] = FormatTools.FLIM_DOMAIN;
      }
      else if (metadata.hasInstrumentData) {
        domain[0] = FormatTools.LM_DOMAIN;
      }
  
      return domain;
    }
  
    // -- ICSReader Methods --
  
  }

  /**
   *  SCIFIO file format writer for ICS version 1 and 2 files.
   *
   */
  public static class Writer extends AbstractWriter<Metadata> {
  
    // -- Fields --
  
    // -- Constructor --
  
    public Writer() {
      this(null);
    }
  
    public Writer(final SCIFIO ctx) {
      super("Image Cytometry Standard", "ics", ctx);
    }
  
    // -- Writer API Methods --
  
    public void saveBytes(final int imageIndex, final int planeIndex,
      final byte[] buf, final int x, final int y, final int w, final int h)
        throws FormatException, IOException
        {
      // TODO Auto-generated method stub
  
        }
  
    /* @see ome.scifio.Writer#setMetadata(M) */
    public void setMetadata(final Metadata meta) {
      super.setMetadata(meta, new ICSCoreTranslator());
    }
  
  }

  /**
   * Collection of utility methods and constants
   * for working with ICS images.
   *
   */
   public static class ICSUtils {

    // -- Constants --

    /** Newline characters. */
    public static final String NL = "\r\n";

    public static final String[] DATE_FORMATS = {
      "EEEE, MMMM dd, yyyy HH:mm:ss", "EEE dd MMMM yyyy HH:mm:ss",
      "EEE MMM dd HH:mm:ss yyyy", "EE dd MMM yyyy HH:mm:ss z",
    "HH:mm:ss dd\\MM\\yy"};

    // key token value matching regexes within the "document" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] DOCUMENT_KEYS = { {"date"}, // the full key is "document date"
      {"document", "average"}, {"document"}, {"gmtdate"}, {"label"}};

    // key token value matching regexes within the "history" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] HISTORY_KEYS = {
      {"a\\d"}, // the full key is "history a1", etc.
      {"acquisition", "acquire\\..*."},
      {"acquisition", "laserbox\\..*."},
      {"acquisition", "modules\\(.*."},
      {"acquisition", "objective", "position"},
      {"adc", "resolution"},
      {"atd_hardware", "ver"},
      {"atd_libraries", "ver"},
      {"atd_microscopy", "ver"},
      {"author"},
      {"averagecount"},
      {"averagequality"},
      {"beam", "zoom"},
      {"binning"},
      {"bits/pixel"},
      {"black", "level"},
      {"black", "level\\*"},
      {"black_level"},
      {"camera", "manufacturer"},
      {"camera", "model"},
      {"camera"},
      {"cfd", "holdoff"},
      {"cfd", "limit", "high"},
      {"cfd", "limit", "low"},
      {"cfd", "zc", "level"},
      {"channel\\*"},
      {"collection", "time"},
      {"cols"},
      {"company"},
      {"count", "increment"},
      {"created", "on"},
      {"creation", "date"},
      {"cube", "descriptio"}, // sic; not found in sample files
      {"cube", "description"}, // correction; not found in sample files
      {"cube", "emm", "nm"},
      {"cube", "exc", "nm"},
      {"cube"},
      {"date"},
      {"dategmt"},
      {"dead", "time", "comp"},
      {"desc", "exc", "turret"},
      {"desc", "emm", "turret"},
      {"detector", "type"},
      {"detector"},
      {"dimensions"},
      {"direct", "turret"},
      {"dither", "range"},
      {"dwell"},
      {"excitationfwhm"},
      {"experiment"},
      {"experimenter"},
      {"expon.", "order"},
      {"exposure"},
      {"exposure_time"},
      {"ext", "latch", "delay"},
      {"extents"},
      {"filterset", "dichroic", "name"},
      {"filterset", "dichroic", "nm"},
      {"filterset", "emm", "name"},
      {"filterset", "emm", "nm"},
      {"filterset", "exc", "name"},
      {"filterset", "exc", "nm"},
      {"filterset"},
      {"filter\\*"},
      {"firmware"},
      {"fret", "backgr\\d"},
      {"frametime"},
      {"gain"},
      {"gain\\d"},
      {"gain\\*"},
      {"gamma"},
      {"icsviewer", "ver"},
      {"ht\\*"},
      {"id"},
      {"illumination", "mode", "laser"},
      {"illumination", "mode"},
      {"image", "bigendian"},
      {"image", "bpp"},
      {"image", "form"}, // not found in sample files
      {"image", "physical_sizex"},
      {"image", "physical_sizey"},
      {"image", "sizex"},
      {"image", "sizey"},
      {"labels"},
      {"lamp", "manufacturer"},
      {"lamp", "model"},
      {"laser", "firmware"},
      {"laser", "manufacturer"},
      {"laser", "model"},
      {"laser", "power"},
      {"laser", "rep", "rate"},
      {"laser", "type"},
      {"laser\\d", "intensity"},
      {"laser\\d", "name"},
      {"laser\\d", "wavelength"},
      {"left"},
      {"length"},
      {"line", "compressio"}, // sic
      {"line", "compression"}, // correction; not found in sample files
      {"linetime"},
      {"magnification"},
      {"manufacturer"},
      {"max", "photon", "coun"}, // sic
      {"max", "photon", "count"}, // correction; not found in sample files
      {"memory", "bank"}, {"metadata", "format", "ver"},
      {"microscope", "built", "on"}, {"microscope", "name"}, {"microscope"},
      {"mirror", "\\d"}, {"mode"}, {"noiseval"}, {"no.", "frames"},
      {"objective", "detail"}, {"objective", "immersion"},
      {"objective", "mag"}, {"objective", "magnification"},
      {"objective", "na"}, {"objective", "type"},
      {"objective", "workingdistance"}, {"objective"}, {"offsets"},
      {"other", "text"}, {"passcount"}, {"pinhole"}, {"pixel", "clock"},
      {"pixel", "time"}, {"pmt"}, {"polarity"}, {"region"}, {"rep", "period"},
      {"repeat", "time"}, {"revision"}, {"routing", "chan", "x"},
      {"routing", "chan", "y"}, {"rows"}, {"scan", "borders"},
      {"scan", "flyback"}, {"scan", "pattern"}, {"scan", "pixels", "x"},
      {"scan", "pixels", "y"}, {"scan", "pos", "x"}, {"scan", "pos", "y"},
      {"scan", "resolution"}, {"scan", "speed"}, {"scan", "zoom"},
      {"scanner", "lag"}, {"scanner", "pixel", "time"},
      {"scanner", "resolution"}, {"scanner", "speed"}, {"scanner", "xshift"},
      {"scanner", "yshift"}, {"scanner", "zoom"}, {"shutter\\d"},
      {"shutter", "type"}, {"software"}, {"spectral", "bin_definition"},
      {"spectral", "calibration", "gain", "data"},
      {"spectral", "calibration", "gain", "mode"},
      {"spectral", "calibration", "offset", "data"},
      {"spectral", "calibration", "offset", "mode"},
      {"spectral", "calibration", "sensitivity", "mode"},
      {"spectral", "central_wavelength"}, {"spectral", "laser_shield"},
      {"spectral", "laser_shield_width"}, {"spectral", "resolution"},
      {"stage", "controller"}, {"stage", "firmware"},
      {"stage", "manufacturer"}, {"stage", "model"}, {"stage", "pos"},
      {"stage", "positionx"}, {"stage", "positiony"}, {"stage", "positionz"},
      {"stage_xyzum"}, {"step\\d", "channel", "\\d"},
      {"step\\d", "gain", "\\d"}, {"step\\d", "laser"}, {"step\\d", "name"},
      {"step\\d", "pinhole"}, {"step\\d", "pmt", "ch", "\\d"},
      {"step\\d", "shutter", "\\d"}, {"step\\d"}, {"stop", "on", "o'flow"},
      {"stop", "on", "time"}, {"study"}, {"sync", "freq", "div"},
      {"sync", "holdoff"}, {"sync"}, {"tac", "gain"}, {"tac", "limit", "low"},
      {"tac", "offset"}, {"tac", "range"}, {"tau\\d"}, {"tcspc", "adc", "res"},
      {"tcspc", "adc", "resolution"}, {"tcspc", "approx", "adc", "rate"},
      {"tcspc", "approx", "cfd", "rate"}, {"tcspc", "approx", "tac", "rate"},
      {"tcspc", "bh"}, {"tcspc", "cfd", "holdoff"},
      {"tcspc", "cfd", "limit", "high"}, {"tcspc", "cfd", "limit", "low"},
      {"tcspc", "cfd", "zc", "level"}, {"tcspc", "clock", "polarity"},
      {"tcspc", "collection", "time"}, {"tcspc", "count", "increment"},
      {"tcspc", "dead", "time", "enabled"}, {"tcspc", "delay"},
      {"tcspc", "dither", "range"}, {"tcspc", "left", "border"},
      {"tcspc", "line", "compression"}, {"tcspc", "mem", "offset"},
      {"tcspc", "operation", "mode"}, {"tcspc", "overflow"},
      {"tcspc", "pixel", "clk", "divider"}, {"tcspc", "pixel", "clock"},
      {"tcspc", "routing", "x"}, {"tcspc", "routing", "y"},
      {"tcspc", "scan", "x"}, {"tcspc", "scan", "y"},
      {"tcspc", "sync", "divider"}, {"tcspc", "sync", "holdoff"},
      {"tcspc", "sync", "rate"}, {"tcspc", "sync", "threshold"},
      {"tcspc", "sync", "zc", "level"}, {"tcspc", "tac", "gain"},
      {"tcspc", "tac", "limit", "high"}, {"tcspc", "tac", "limit", "low"},
      {"tcspc", "tac", "offset"}, {"tcspc", "tac", "range"},
      {"tcspc", "time", "window"}, {"tcspc", "top", "border"},
      {"tcspc", "total", "frames"}, {"tcspc", "total", "time"},
      {"tcspc", "trigger"}, {"tcspc", "x", "sync", "polarity"},
      {"tcspc", "y", "sync", "polarity"}, {"text"}, {"time"}, {"title"},
      {"top"}, {"transmission"}, {"trigger"}, {"type"}, {"units"}, {"version"},
      {"wavelength\\*"}, {"x", "amplitude"}, {"y", "amplitude"},
      {"x", "delay"}, {"y", "delay"}, {"x", "offset"}, {"y", "offset"},
      {"z", "\\(background\\)"}};

    // key token value matching regexes within the "layout" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] LAYOUT_KEYS = {
      {"coordinates"}, // the full key is "layout coordinates"
      {"order"}, {"parameters"}, {"real_significant_bits"},
      {"significant_bits"}, {"significant_channels"}, {"sizes"}};

    // key token value matching regexes within the "parameter" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] PARAMETER_KEYS = {
      {"allowedlinemodes"}, // the full key is "parameter allowedlinemodes"
      {"ch"}, {"higher_limit"}, {"labels"}, {"lower_limit"}, {"origin"},
      {"range"}, {"sample_width", "ch"}, {"sample_width"}, {"scale"},
      {"units", "adc-units", "channels"}, {"units", "adc-units", "nm"},
      {"units"}};

    // key token value matching regexes within the "representation" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] REPRESENTATION_KEYS = { {"byte_order"}, // the full key is "representation byte_order"
      {"compression"}, {"format"}, {"sign"}};

    // key token value matching regexes within the "sensor" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] SENSOR_KEYS = {
      {"model"}, // the full key is "sensor model"
      {"s_params", "channels"}, {"s_params", "exphotoncnt"},
      {"s_params", "lambdaem"}, {"s_params", "lambdaex"},
      {"s_params", "numaperture"}, {"s_params", "pinholeradius"},
      {"s_params", "pinholespacing"},
      {"s_params", "refinxlensmedium"}, // sic; not found in sample files
      {"s_params", "refinxmedium"}, // sic; not found in sample files
      {"s_params", "refrinxlensmedium"}, {"s_params", "refrinxmedium"},
      {"type"}};

    // key token value matching regexes within the "view" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] VIEW_KEYS = {
      {"view", "color", "lib", "lut"}, // the full key is
      // "view view color lib lut"
      {"view", "color", "count"}, {"view", "color", "doc", "scale"},
      {"view", "color", "mode", "rgb", "set"},
      {"view", "color", "mode", "rgb"}, {"view", "color", "schemes"},
      {"view", "color", "view", "active"}, {"view", "color"},
      {"view\\d", "alpha"}, {"view\\d", "alphastate"},
      {"view\\d", "annotation", "annellipse"},
      {"view\\d", "annotation", "annpoint"}, {"view\\d", "autoresize"},
      {"view\\d", "axis"}, {"view\\d", "blacklevel"}, {"view\\d", "color"},
      {"view\\d", "cursor"}, {"view\\d", "dimviewoption"},
      {"view\\d", "gamma"}, {"view\\d", "ignoreaspect"},
      {"view\\d", "intzoom"}, {"view\\d", "live"}, {"view\\d", "order"},
      {"view\\d", "port"}, {"view\\d", "position"}, {"view\\d", "saturation"},
      {"view\\d", "scale"}, {"view\\d", "showall"}, {"view\\d", "showcursor"},
      {"view\\d", "showindex"}, {"view\\d", "size"},
      {"view\\d", "synchronize"}, {"view\\d", "tile"}, {"view\\d", "useunits"},
      {"view\\d", "zoom"}, {"view\\d"}, {"view"}};

    // These strings appeared in the former metadata field categories but are not
    // found in the LOCI sample files.
    //
    // The former metadata field categories table did not save the context, i.e.
    // the first token such as "document" or "history" and other intermediate
    // tokens.  The preceding tables such as DOCUMENT_KEYS or HISTORY_KEYS use
    // this full context.
    //
    // In an effort at backward compatibility, these will be used to form key
    // value pairs if key/value pair not already assigned and they match anywhere
    // in the input line.
    //
    public static String[][] OTHER_KEYS = { {"cube", "descriptio"}, // sic; also listed in HISTORY_KEYS
      {"cube", "description"}, // correction; also listed in HISTORY_KEYS
      {"image", "form"}, // also listed in HISTORY_KEYS
      {"refinxlensmedium"}, // Could be a mispelling of "refrinxlensmedium";
      // also listed in SENSOR_KEYS
      {"refinxmedium"}, // Could be a mispelling of "refinxmedium";
      // also listed in SENSOR_KEYS
      {"scil_type"}, {"source"}};

    /** Metadata field categories. */
    public static final String[] CATEGORIES = new String[] {
      "ics_version", "filename", "source", "layout", "representation",
      "parameter", "sensor", "history", "document", "view.*", "end", "file",
      "offset", "parameters", "order", "sizes", "coordinates",
      "significant_bits", "format", "sign", "compression", "byte_order",
      "origin", "scale", "units", "t", "labels", "SCIL_TYPE", "type", "model",
      "s_params", "gain.*", "dwell", "shutter.*", "pinhole", "laser.*",
      "version", "objective", "PassCount", "step.*", "date", "GMTdate",
      "label", "software", "author", "length", "Z (background)", "dimensions",
      "rep period", "image form", "extents", "offsets", "region",
      "expon. order", "a.*", "tau.*", "noiseval", "excitationfwhm",
      "created on", "text", "other text", "mode", "CFD limit low",
      "CFD limit high", "CFD zc level", "CFD holdoff", "SYNC zc level",
      "SYNC freq div", "SYNC holdoff", "TAC range", "TAC gain", "TAC offset",
      "TAC limit low", "ADC resolution", "Ext latch delay", "collection time",
      "repeat time", "stop on time", "stop on O'flow", "dither range",
      "count increment", "memory bank", "sync threshold", "dead time comp",
      "polarity", "line compressio", "scan flyback", "scan borders",
      "pixel time", "pixel clock", "trigger", "scan pixels x", "scan pixels y",
      "routing chan x", "routing chan y", "detector type", "channel.*",
      "filter.*", "wavelength.*", "black.level.*", "ht.*", "scan resolution",
      "scan speed", "scan zoom", "scan pattern", "scan pos x", "scan pos y",
      "transmission", "x amplitude", "y amplitude", "x offset", "y offset",
      "x delay", "y delay", "beam zoom", "mirror .*", "direct turret",
      "desc exc turret", "desc emm turret", "cube", "Stage_XYZum",
      "cube descriptio", "camera", "exposure", "bits/pixel", "binning", "left",
      "top", "cols", "rows", "significant_channels", "allowedlinemodes",
      "real_significant_bits", "sample_width", "range", "ch", "lower_limit",
      "higher_limit", "passcount", "detector", "dateGMT", "RefrInxMedium",
      "RefrInxLensMedium", "Channels", "PinholeRadius", "LambdaEx", "LambdaEm",
      "ExPhotonCnt", "RefInxMedium", "NumAperture", "RefInxLensMedium",
      "PinholeSpacing", "power", "name", "Type", "Magnification", "NA",
      "WorkingDistance", "Immersion", "Pinhole", "Channel .*", "Gain .*",
      "Shutter .*", "Position", "Size", "Port", "Cursor", "Color",
      "BlackLevel", "Saturation", "Gamma", "IntZoom", "Live", "Synchronize",
      "ShowIndex", "AutoResize", "UseUnits", "Zoom", "IgnoreAspect",
      "ShowCursor", "ShowAll", "Axis", "Order", "Tile", "DimViewOption",
      "channels", "pinholeradius", "refrinxmedium", "numaperture",
      "refrinxlensmedium", "image", "microscope", "stage", "filterset",
      "dichroic", "exc", "emm", "manufacturer", "experiment", "experimenter",
      "study", "metadata", "format", "icsviewer", "illumination",
      "exposure_time", "model", "ver", "creation", "date", "bpp", "bigendian",
      "size.", "physical_size.", "controller", "firmware", "pos", "position.",
      "mag", "na", "nm", "lamp", "built", "on", "rep", "rate", "Exposure",
    "Wavelength\\*"};

    // -- Helper Methods --

    /*
     * String tokenizer for parsing metadata. Splits on any white-space
     * characters. Tabs and spaces are often used interchangeably in real-life ICS
     * files.
     *
     * Also splits on 0x04 character which appears in "paul/csarseven.ics" and
     * "paul/gci/time resolved_1.ics".
     *
     * Also respects double quote marks, so that
     *   Modules("Confocal C1 Grabber").BarrierFilter(2)
     * is just one token.
     *
     * If not for the last requirement, the one line
     *   String[] tokens = line.split("[\\s\\x04]+");
     * would work.
     */
    private String[] tokenize(final String line) {
      final List<String> tokens = new ArrayList<String>();
      boolean inWhiteSpace = true;
      boolean withinQuotes = false;
      StringBuffer token = null;
      for (int i = 0; i < line.length(); ++i) {
        final char c = line.charAt(i);
        if (Character.isWhitespace(c) || c == 0x04) {
          if (withinQuotes) {
            // retain white space within quotes
            token.append(c);
          }
          else if (!inWhiteSpace) {
            // put out pending token string
            inWhiteSpace = true;
            if (token.length() > 0) {
              tokens.add(token.toString());
              token = null;
            }
          }
        }
        else {
          if ('"' == c) {
            // toggle quotes
            withinQuotes = !withinQuotes;
          }
          if (inWhiteSpace) {
            inWhiteSpace = false;
            // start a new token string
            token = new StringBuffer();
          }
          // build token string
          token.append(c);
        }
      }
      // put out any pending token strings
      if (null != token && token.length() > 0) {
        tokens.add(token.toString());
      }
      return tokens.toArray(new String[0]);
    }

    /* Given a list of tokens and an array of lists of regular expressions, tries
     * to find a match.  If no match is found, looks in OTHER_KEYS.
     */
    String[] findKeyValue(final String[] tokens, final String[][] regexesArray) {
      String[] keyValue = findKeyValueForCategory(tokens, regexesArray);
      if (null == keyValue) {
        keyValue = findKeyValueOther(tokens, OTHER_KEYS);
      }
      if (null == keyValue) {
        final String key = tokens[0];
        final String value = concatenateTokens(tokens, 1, tokens.length);
        keyValue = new String[] {key, value};
      }
      return keyValue;
    }

    /*
     * Builds a string from a list of tokens.
     */
    private String concatenateTokens(final String[] tokens, final int start,
      final int stop)
    {
      final StringBuffer returnValue = new StringBuffer();
      for (int i = start; i < tokens.length && i < stop; ++i) {
        returnValue.append(tokens[i]);
        if (i < stop - 1) {
          returnValue.append(' ');
        }
      }
      return returnValue.toString();
    }

    /*
     * Given a list of tokens and an array of lists of regular expressions, finds
     * a match.  Returns key/value pair if matched, null otherwise.
     *
     * The first element, tokens[0], has already been matched to a category, i.e.
     * 'history', and the regexesArray is category-specific.
     */
    private String[] findKeyValueForCategory(final String[] tokens,
      final String[][] regexesArray)
    {
      String[] keyValue = null;
      int index = 0;
      for (final String[] regexes : regexesArray) {
        if (compareTokens(tokens, 1, regexes, 0)) {
          final int splitIndex = 1 + regexes.length; // add one for the category
          final String key = concatenateTokens(tokens, 0, splitIndex);
          final String value =
            concatenateTokens(tokens, splitIndex, tokens.length);
          keyValue = new String[] {key, value};
          break;
        }
        ++index;
      }
      return keyValue;
    }

    /* Given a list of tokens and an array of lists of regular expressions, finds
     * a match.  Returns key/value pair if matched, null otherwise.
     *
     * The first element, tokens[0], represents a category and is skipped.  Look
     * for a match of a list of regular expressions anywhere in the list of tokens.
     */
    private String[] findKeyValueOther(final String[] tokens,
      final String[][] regexesArray)
    {
      String[] keyValue = null;
      for (final String[] regexes : regexesArray) {
        for (int i = 1; i < tokens.length - regexes.length; ++i) {
          // does token match first regex?
          if (tokens[i].toLowerCase().matches(regexes[0])) {
            // do remaining tokens match remaining regexes?
            if (1 == regexes.length || compareTokens(tokens, i + 1, regexes, 1)) {
              // if so, return key/value
              final int splitIndex = i + regexes.length;
              final String key = concatenateTokens(tokens, 0, splitIndex);
              final String value =
                concatenateTokens(tokens, splitIndex, tokens.length);
              keyValue = new String[] {key, value};
              break;
            }
          }
        }
        if (null != keyValue) {
          break;
        }
      }
      return keyValue;
    }

    /*
     * Compares a list of tokens with a list of regular expressions.
     */
    private boolean compareTokens(final String[] tokens, final int tokenIndex,
      final String[] regexes, final int regexesIndex)
    {
      boolean returnValue = true;
      int i, j;
      for (i = tokenIndex, j = regexesIndex; j < regexes.length; ++i, ++j) {
        if (i >= tokens.length || !tokens[i].toLowerCase().matches(regexes[j])) {
          returnValue = false;
          break;
        }
      }
      return returnValue;
    }

    /** Splits the given string into a list of {@link Double}s. */
    private Double[] splitDoubles(final String v) {
      final StringTokenizer t = new StringTokenizer(v);
      final Double[] values = new Double[t.countTokens()];
      for (int n = 0; n < values.length; n++) {
        final String token = t.nextToken().trim();
        try {
          values[n] = new Double(token);
        }
        catch (final NumberFormatException e) {
          //LOGGER.debug("Could not parse double value '{}'", token, e);
        }
      }
      return values;
    }

    /** Verifies that a unit matches the expected value. */
    private boolean checkUnit(final String actual, final String... expected) {
      if (actual == null || actual.equals("")) return true; // undefined is OK
      for (final String exp : expected) {
        if (actual.equals(exp)) return true; // unit matches expected value
      }
      //LOGGER.debug("Unexpected unit '{}'; expected '{}'", actual, expected);
      return false;
    }
  }

  /**
   * SCIFIO file format Translator for ICS Metadata objects to the SCIFIO Core
   * metadata type.
   * 
   */
  @SCIFIOTranslator(metaIn = CoreMetadata.class, metaOut = Metadata.class)
  public static class CoreICSTranslator
  extends AbstractTranslator<CoreMetadata, Metadata>
  implements CoreTranslator {
  
    // -- Constructors --
  
    public CoreICSTranslator() {
      this(null);
    }
  
    public CoreICSTranslator(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Translator API Methods --
  
    public void translate(final CoreMetadata source, final Metadata destination)
    {
      // note that the destination fields will preserve their default values
      // only the keyValPairs will be modified
  
      Hashtable<String, String> keyValPairs = null;
      if (destination == null || destination.getKeyValPairs() == null) keyValPairs =
        new Hashtable<String, String>();
      else 
        keyValPairs = destination.getKeyValPairs();
  
      final int numAxes = source.getAxisCount(0);
  
      String order = "";
      String sizes = "";
  
      for (int i = 0; i < numAxes; i++) {
        final AxisType axis = source.getAxisType(0, i);
  
        if (axis.equals(Axes.X)) {
          order += "x";
        }
        else if (axis.equals(Axes.Y)) {
          order += "y";
        }
        else if (axis.equals(Axes.Z)) {
          order += "z";
        }
        else if (axis.equals(Axes.TIME)) {
          order += "t";
        }
        else if (axis.equals(Axes.CHANNEL)) {
          order += "c";
        }
        else if (axis.equals(Axes.PHASE)) {
          order += "p";
        }
        else if (axis.equals(Axes.FREQUENCY)) {
          order += "f";
        }
        else {
          if(axis.getLabel().equals("bits"))
            order += "bits";
          else
            order += "u";
        }
  
        order += " ";
        sizes += source.getAxisLength(0, i) + " ";
      }
  
      keyValPairs.put("layout sizes", sizes);
      keyValPairs.put("layout order", order);
  
      keyValPairs.put("layout significant_bits", "" + source.getBitsPerPixel(0));
  
      if(source.getChannelDimTypes(0).equals(FormatTools.LIFETIME))
        keyValPairs.put("history type", "time resolved");
  
      boolean signed = false;
      boolean fPoint = false;
  
      switch (source.getPixelType(0)) {
        case FormatTools.INT8:
        case FormatTools.INT16:
        case FormatTools.INT32:
          signed = true;
          break;
        case FormatTools.UINT8:
        case FormatTools.UINT16:
        case FormatTools.UINT32:
          break;
        case FormatTools.FLOAT:
        case FormatTools.DOUBLE:
          fPoint = true;
          signed = true;
          break;
  
      }
  
      keyValPairs.put("representation sign", signed ? "signed" : "");
      keyValPairs.put("representation format", fPoint ? "real" : "");
      keyValPairs.put("representation compression", "");
  
      String byteOrder = "0";
  
      if(source.isLittleEndian(0))
        byteOrder = fPoint ? "1" : "0";
      else
        byteOrder = fPoint ? "0" : "1";
  
      keyValPairs.put("representation byte_order", byteOrder);
  
      destination.setKeyValPairs(keyValPairs);
    }
  
  }

  /**
   * SCIFIO file format Translator for ICS Metadata objects
   * to the SCIFIO Core metadata type. 
   *
   */
  @SCIFIOTranslator(metaIn = Metadata.class, metaOut = CoreMetadata.class)
  public static class ICSCoreTranslator
  extends AbstractTranslator<Metadata, CoreMetadata>
  implements CoreTranslator {

    private Metadata curSource;

    // -- Constructors --

    public ICSCoreTranslator() {
      this(null);
    }

    public ICSCoreTranslator(final SCIFIO ctx) {
      super(ctx);
    }

    // -- Translator API Methods --

    public void translate(final Metadata source, final CoreMetadata destination)
    {
      final CoreImageMetadata coreMeta = new CoreImageMetadata();
      destination.add(coreMeta);
      final int index = destination.getImageCount() - 1;

      curSource = source;

      coreMeta.setRgb(false);

      // find axis sizes

      AxisType[] axisTypes = null;
      int[] axisLengths = null;

      int bitsPerPixel = 0;

      StringTokenizer layoutTokens = getTknz("layout sizes");
      axisLengths = new int[layoutTokens.countTokens()];

      for (int n = 0; n < axisLengths.length; n++) {
        try {
          axisLengths[n] = Integer.parseInt(layoutTokens.nextToken().trim());
        }
        catch (final NumberFormatException e) {
          LOGGER.debug("Could not parse axis length", e);
        }
      }

      layoutTokens = getTknz("layout order");
      axisTypes = new AxisType[layoutTokens.countTokens()];

      final Vector<Integer> channelLengths = new Vector<Integer>();
      final Vector<String> channelTypes = new Vector<String>();

      for (int n = 0; n < axisTypes.length; n++) {
        final String tkn = layoutTokens.nextToken().trim();
        if (tkn.equals("x")) {
          axisTypes[n] = Axes.X;
        }
        else if (tkn.equals("y")) {
          axisTypes[n] = Axes.Y;
        }
        else if (tkn.equals("z")) {
          axisTypes[n] = Axes.Z;
        }
        else if (tkn.equals("t")) {
          axisTypes[n] = Axes.TIME;
        }
        else if (tkn.startsWith("c")) {
          axisTypes[n] = Axes.CHANNEL;
          channelTypes.add(FormatTools.CHANNEL);
          channelLengths.add(axisLengths[n]);
        }
        else if (tkn.startsWith("p")) {
          axisTypes[n] = Axes.PHASE;
          channelTypes.add(FormatTools.PHASE);
          channelLengths.add(axisLengths[n]);
        }
        else if (tkn.startsWith("f")) {
          axisTypes[n] = Axes.FREQUENCY;
          channelTypes.add(FormatTools.FREQUENCY);
          channelLengths.add(axisLengths[n]);
        }
        else if (tkn.equals("bits")) {
          axisTypes[n] = new CustomAxisType("bits");
          bitsPerPixel = axisLengths[n];
          while (bitsPerPixel % 8 != 0)
            bitsPerPixel++;
          if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
        }
        else {
          axisTypes[n] = Axes.UNKNOWN;
          channelTypes.add("");
          channelLengths.add(axisLengths[n]);
        }
      }

      if (source.get("layout significant_bits") != null) {
        destination.setBitsPerPixel(
          index, Integer.parseInt(source.get("layout significant_bits")));
      }
      else destination.setBitsPerPixel(index, bitsPerPixel);

      coreMeta.setAxisLengths(axisLengths);
      coreMeta.setAxisTypes(axisTypes);
      //storedRGB = getSizeX() == 0;

      if (channelLengths.size() == 0) {
        channelLengths.add(new Integer(1));
        channelTypes.add(FormatTools.CHANNEL);
      }

      final int[] cLengths = new int[channelLengths.size()];
      final String[] cTypes = new String[channelLengths.size()];

      for (int i = 0; i < channelLengths.size(); i++) {
        cLengths[i] = channelLengths.get(i);
        cTypes[i] = channelTypes.get(i);
      }

      coreMeta.setcLengths(cLengths);
      coreMeta.setcTypes(cTypes);

      if (destination.getAxisIndex(index, Axes.Z) == -1) {
        destination.addAxis(index, Axes.Z, 1);
      }
      if (destination.getAxisIndex(index, Axes.CHANNEL) == -1) {
        destination.addAxis(index, Axes.CHANNEL, 1);
      }
      if (destination.getAxisIndex(index, Axes.TIME) == -1) {
        destination.addAxis(index, Axes.TIME, 1);
      }

      if (destination.getAxisLength(index, Axes.Z) == 0)
        coreMeta.setAxisLength(Axes.Z, 1);
      if (destination.getAxisLength(index, Axes.CHANNEL) == 0)
        coreMeta.setAxisLength(Axes.CHANNEL, 1);
      if (destination.getAxisLength(index, Axes.TIME) == 0)
        coreMeta.setAxisLength(Axes.TIME, 1);

      coreMeta.setPlaneCount(destination.getAxisLength(0, Axes.Z) *
        destination.getAxisLength(0, Axes.TIME));

      coreMeta.setInterleaved(destination.isRGB(0));
      //TODO coreMeta.imageCount = getSizeZ() * getSizeT();
      //TODO if (destination.isRGB(0)) coreMeta.imageCount *= getSizeC();
      destination.setIndexed(index, false);
      destination.setFalseColor(index, false);
      destination.setMetadataComplete(index, true);
      destination.setLittleEndian(index, true);

      final String history = source.get("history type");
      boolean lifetime = false;
      if (history != null &&
        (history.equalsIgnoreCase("time resolved") || history.equalsIgnoreCase("FluorescenceLifetime")))
        lifetime = true;

      // HACK - support for Gray Institute at Oxford's ICS lifetime data
      if (lifetime) {
        final int binCount = destination.getAxisLength(index, Axes.Z);
        coreMeta.setAxisLength(
          Axes.Z, (destination.getAxisLength(index, Axes.CHANNEL)));
        coreMeta.setAxisLength(Axes.CHANNEL, binCount);
        coreMeta.setcLengths(new int[] {binCount});
        coreMeta.setcTypes(new String[] {FormatTools.LIFETIME});
        final int cIndex = destination.getAxisIndex(index, Axes.CHANNEL);
        final int zIndex = destination.getAxisIndex(index, Axes.Z);
        coreMeta.setAxisType(cIndex, Axes.Z);
        coreMeta.setAxisType(zIndex, Axes.CHANNEL);
      }

      final String byteOrder = source.get("representation byte_order");
      final String rFormat = source.get("representation format");
      final String compression = source.get("representation compression");

      if (byteOrder != null) {
        final String firstByte = byteOrder.split(" ")[0];
        final int first = Integer.parseInt(firstByte);
        coreMeta.setLittleEndian(rFormat.equals("real") ? first == 1 : first != 1);
      }

      final boolean gzip =
        (compression == null) ? false : compression.equals("gzip");

      final int bytes = bitsPerPixel / 8;

      if (bitsPerPixel < 32)
        coreMeta.setLittleEndian(!destination.isLittleEndian(0));

      final boolean floatingPt = rFormat.equals("real");
      final boolean signed = source.get("representation sign").equals("signed");

      try {
        coreMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed, floatingPt));
      }
      catch (final FormatException e) {
        e.printStackTrace();
      }

    }

    // -- Helper Methods --

    StringTokenizer getTknz(final String target) {
      return new StringTokenizer(curSource.get(target));
    }
  }
}
