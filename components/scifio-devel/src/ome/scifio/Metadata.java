package ome.scifio;

import java.io.Serializable;

import ome.scifio.io.RandomAccessInputStream;

/**
 * Interface for all SciFIO Metadata objects.
 * Based on the format, a Metadata object can
 * be a single N-dimensional collection of bytes
 * (an image) or a list of multiple images.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Metadata extends Serializable, HasFormat, HasContext {

  /**
   * Resets this Metadata object's values as though it had just been
   * instantiated.
   */
  void reset(Class<?> type);

  /**
   * Sets the input source attached to this Metadata object.
   * Note that calling this method does not affect the structure
   * of this Metadata object.
   * 
   * @param in
   */
  void setSource(RandomAccessInputStream in);

  /**
   * Returns the source used to generate this Metadat object.
   * 
   * @return
   */
  RandomAccessInputStream getSource();
}
