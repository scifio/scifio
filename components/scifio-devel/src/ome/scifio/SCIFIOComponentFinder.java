package ome.scifio;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class contains a collection of convenience methods
 * that can be used to find a desired component of SCIFIO,
 * given the overlaying context's known formats.
 * 
 * @author Mark Hiner
 *
 */
public class SCIFIOComponentFinder {

  /* 
   * Returns a sorted list of Formats that are compatible with the provided source.
   * if takeFirst is set, returns the first matching Format.
   * Formats are searched in priority order
   */
  public List<Format<?, ?, ?, ?, ?>> findFormats(final String id,
    final boolean openFile, final boolean takeFirst,
    final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException
  {

    final PriorityQueue<Format<?, ?, ?, ?, ?>> formatPriorities =
      new PriorityQueue<Format<?, ?, ?, ?, ?>>(formats);
    final List<Format<?, ?, ?, ?, ?>> formatList =
      new ArrayList<Format<?, ?, ?, ?, ?>>();

    boolean found = false;
    while (!formatPriorities.isEmpty() && (!found || !takeFirst)) {
      final Format<?, ?, ?, ?, ?> format = formatPriorities.poll();

      if (format.createChecker().isFormat(id, openFile)) {
        found = true;
        formatList.add(format);
      }
    }

    if (formatList.isEmpty())
      throw new FormatException(id + ": No supported format found.");

    return formatList;
  }

  /*
  public List<Checker<?>> findCheckers(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Parser<?>> findParsers(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Metadata> findMetadata(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Reader<?>> findReaders(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Writer<?>> findWriters(final String id, final boolean openFile, final String output, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Translator<?, ?>> findTranslators(final String id, final boolean openFile, final String output, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  */
}
