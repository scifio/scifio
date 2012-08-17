package ome.scifio.ics;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Collection of utility methods and constants
 * for working with ICS images.
 *
 */
public class ICSUtils {

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
