package ome.scifio;

/**
 * Abstract superclass of all SCIFIO components that are children of
 * ome.scifio.FormatHandler.
 *
 */
public abstract class AbstractFormatHandler extends AbstractHasContext
  implements FormatHandler {

  // -- Fields --
  /** Name of this file format. */
  protected String formatName;

  /** Valid suffixes for this file format. */
  protected String[] suffixes;

  /** Suffixes for supported compression types. */
  public static final String[] COMPRESSION_SUFFIXES = {"bz2", "gz"};

  // -- Constructors --
  // TODO do Parsers and Translators really need to track format and suffixes?

  /** Constructs a format handler with the given name and default suffix. */
  public AbstractFormatHandler(final String formatName, final String suffix, final SCIFIO ctx) {
    this(formatName, suffix == null ? null : new String[] {suffix}, ctx);
  }

  /** Constructs a format handler with the given name and default suffixes. */
  public AbstractFormatHandler(final String formatName, final String[] suffixes, final SCIFIO ctx)
  {
    super(ctx);
    this.formatName = formatName;
    this.suffixes = suffixes == null ? new String[0] : suffixes;
  }

  // -- FormatHandler API Methods --

  /* @see FormatHandler#getFormatName() */
  public String getFormatName() {
    return formatName;
  }

  /* @see FormatHandler#getSuffixes() */
  public String[] getSuffixes() {
    return suffixes.clone();
  }

  // -- Utility methods --

  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String suffix) {
    return checkSuffix(name, new String[] {suffix});
  }

  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String[] suffixList) {
    final String lname = name.toLowerCase();
    for (int i = 0; i < suffixList.length; i++) {
      final String s = "." + suffixList[i];
      if (lname.endsWith(s)) return true;
      for (int j = 0; j < COMPRESSION_SUFFIXES.length; j++) {
        if (lname.endsWith(s + "." + COMPRESSION_SUFFIXES[j])) return true;
      }
    }
    return false;
  }

}
