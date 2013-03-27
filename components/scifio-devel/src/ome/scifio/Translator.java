package ome.scifio;

/**
 * Interface for all SciFIO Translators.
 * Translates from Metadata type I to a new Metadata type
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Translator<M extends Metadata, N extends Metadata>
  extends HasContext, HasFormat {

  // -- Translator API methods --

  /**
   * Uses the type M Metadata object to build an instance of Metadata type N
   * 
   * Note that N does not have to be a blank Metadata object, but such can be
   * ensured by calling the reset() method ahead of time.
   * 
   * @param metaIn Metadata object of the Input type
   * @return a new Metadtata object
   */
  void translate(final M source, final N destination);
}
