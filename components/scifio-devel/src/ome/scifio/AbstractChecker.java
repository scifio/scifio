package ome.scifio;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Abstract superclass of all SCIFIO checker components. 
 *
 */
public abstract class AbstractChecker<M extends Metadata>
  extends AbstractFormatHandler implements Checker<M> {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

  // -- Fields --

  /**
   * Whether the file extension matching one of the reader's suffixes
   * is necessary to identify the file as an instance of this format.
   */
  protected boolean suffixNecessary = true;

  /**
   * Whether the file extension matching one of the reader's suffixes
   * is sufficient to identify the file as an instance of this format.
   */
  protected boolean suffixSufficient = true;

  // -- Constructors --

  /** Constructs a checker with the given name and default suffix */
  public AbstractChecker(final String format, final String suffix,
    final SCIFIO ctx)
  {
    super(format, suffix, ctx);
  }

  /** Constructs a checker with the given name and default suffixes */
  public AbstractChecker(final String format, final String[] suffixes,
    final SCIFIO ctx)
  {
    super(format, suffixes, ctx);
  }

  // -- HasFormat API --

  @SuppressWarnings("unchecked")
  public Format<M, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromChecker(getClass());
  }

  // -- Checker API Methods --

  /* @see Checker#isFormat(String name, boolean open) */
  public boolean isFormat(final String name, final boolean open) {
    // if file extension ID is insufficient and we can't open the file, give up
    if (!suffixSufficient && !open) return false;

    if (suffixNecessary || suffixSufficient) {
      // it's worth checking the file extension
      final boolean suffixMatch = checkSuffix(name, suffixes);
      ;

      // if suffix match is required but it doesn't match, failure
      if (suffixNecessary && !suffixMatch) return false;

      // if suffix matches and that's all we need, green light it
      if (suffixMatch && suffixSufficient) return true;
    }

    // suffix matching was inconclusive; we need to analyze the file contents
    if (!open) return false; // not allowed to open any files
    try {
      final RandomAccessInputStream stream = new RandomAccessInputStream(name);
      final boolean isFormat = isFormat(stream);
      stream.close();
      return isFormat;
    }
    catch (final IOException exc) {
      LOGGER.debug("", exc);
      return false;
    }
  }

  /* @see Checker#isFormat(byte[] block) */
  public boolean isFormat(final byte[] block) {
    try {
      final RandomAccessInputStream stream = new RandomAccessInputStream(block);
      final boolean isFormat = isFormat(stream);
      stream.close();
      return isFormat;
    }
    catch (final IOException e) {
      LOGGER.debug("", e);
    }
    return false;
  }

  /* @see Checker#isFormat(RandomAccessInputStream) */
  public boolean isFormat(final RandomAccessInputStream stream)
    throws IOException
  {
    return false;
  }

}
