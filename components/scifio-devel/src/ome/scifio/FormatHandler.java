package ome.scifio;

/**
 * Interface for all SciFIO components dealing with files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface FormatHandler {

  // -- FormatHandler API --

  /** Gets the name of this file format. */
  String getFormatName();

  /** Gets the default file suffixes for this file format. */
  String[] getSuffixes();
}
