package ome.scifio.ics;

import java.util.Hashtable;

import ome.scifio.AbstractMetadata;
import ome.scifio.SCIFIO;

/**
 * SCIFIO Metadata object for ICS images. 
 *
 */
public class ICSMetadata extends AbstractMetadata {

  // -- Fields -- 

  /** Whether this file is ICS version 2,
   *  and thus does not have an IDS companion */
  protected boolean versionTwo = false;

  /** Offset to pixel data */
  protected long offset = -1;

  /** */
  protected boolean hasInstrumentData = false;

  /** ICS file name */
  protected String icsId = "";

  /** IDS file name */
  protected String idsId = "";

  /** ICS Metadata */
  protected Hashtable<String, String> keyValPairs;
  
  // -- Constructor --

  public ICSMetadata() {
    this(null);
  }
  
  public ICSMetadata(SCIFIO ctx) {
	  super(ctx);
	  this.keyValPairs = new Hashtable<String, String>();
  }


  // -- Helper Methods --

  /* @see Metadata#resetMeta() */
  public void reset() {
    super.reset(this.getClass());
    this.keyValPairs = new Hashtable<String, String>();
  }

  /**
   * Convenience method to directly access the hashtable.
   * @param key
   * @return
   */
  public String get(final String key) {
    return this.keyValPairs.get(key);
  }

  public String getIcsId() {
    return icsId;
  }

  public void setIcsId(final String icsId) {
    this.icsId = icsId;
  }

  public String getIdsId() {
    return idsId;
  }

  public void setIdsId(final String idsId) {
    this.idsId = idsId;
  }

  public boolean hasInstrumentData() {
    return hasInstrumentData;
  }

  public void setHasInstrumentData(final boolean hasInstrumentData) {
    this.hasInstrumentData = hasInstrumentData;
  }

  public boolean isVersionTwo() {
    return versionTwo;
  }

  public void setVersionTwo(final boolean versionTwo) {
    this.versionTwo = versionTwo;
  }

  public void setKeyValPairs(Hashtable<String, String> keyValPairs) {
    this.keyValPairs = keyValPairs;
  }

  public Hashtable<String, String> getKeyValPairs() {
    return keyValPairs;
  }
}
