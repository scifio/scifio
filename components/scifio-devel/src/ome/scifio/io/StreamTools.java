package ome.scifio.io;

import java.io.IOException;

/**
 * A utility class for RandomAccess Input/Output streams.
 *
 */
public class StreamTools {

  /**
   * Returns true if the given RandomAccessInputStream conatins at least
   * 'len' bytes.
   */
  public static boolean validStream(RandomAccessInputStream stream, int len,
    boolean littleEndian) throws IOException
  {
    stream.seek(0);
    stream.order(littleEndian);
    return stream.length() >= len;
  }
}
