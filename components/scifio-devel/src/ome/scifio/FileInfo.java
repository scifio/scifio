package ome.scifio;

/**
 * Encompasses basic metadata about a file.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/FileInfo.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/FileInfo.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class FileInfo {

  // -- Fields --

  /** Absolute path to this file. */
  public String filename;

  /** ome.scifio.Reader implementation that would be used to read this file. */
  public Class<?> reader;

  /**
   * Whether or not this file can be passed to the appropriate reader's
   * setId(String) method.
   */
  public boolean usedToInitialize;

  // -- Object API methods --

  @Override
  public String toString() {
    return "filename = " + filename + "\nreader = " + reader.getName() +
      "\nused to initialize = " + usedToInitialize;
  }

}