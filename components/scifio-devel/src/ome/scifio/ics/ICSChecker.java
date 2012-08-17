package ome.scifio.ics;

import ome.scifio.AbstractChecker;
import ome.scifio.SCIFIO;

/**
 * SCIFIO file format Checker for ICS images.
 * 
 */
public class ICSChecker extends AbstractChecker<ICSMetadata> {

  // -- Constructor --

  public ICSChecker() {
    this(null);
  }

  public ICSChecker(final SCIFIO ctx) {
    super("Image Cytometry Standard", new String[] {"ics", "ids"}, ctx);
  }

  // -- Checker API Methods --

}
