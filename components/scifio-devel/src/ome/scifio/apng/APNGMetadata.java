package ome.scifio.apng;

import java.util.ArrayList;
import java.util.List;

import ome.scifio.AbstractMetadata;
import ome.scifio.SCIFIO;

/**
 * File format SCIFIO Metadata for Animated Portable Network Graphics
 * (APNG) images.
 *
 */
public class APNGMetadata extends AbstractMetadata {

  // -- Fields --

  private List<APNGIDATChunk> idat;

  private List<APNGfcTLChunk> fctl;

  private APNGacTLChunk actl;

  private APNGIHDRChunk ihdr;

  private APNGPLTEChunk plte;
  
  private APNGIENDChunk iend;

  // true if the default image is not part of the animation
  private boolean separateDefault;

  // -- Constructor --

  public APNGMetadata() {
    this(null);
  }

  public APNGMetadata(final SCIFIO context) {
    super(context);
    fctl = new ArrayList<APNGfcTLChunk>();
    idat = new ArrayList<APNGIDATChunk>();
  }

  // -- Getters and Setters --

  public List<APNGIDATChunk> getIdat() {
    return idat;
  }

  public void setIdat(final List<APNGIDATChunk> idat) {
    this.idat = idat;
  }

  public void addIdat(final APNGIDATChunk idat) {
    this.idat.add(idat);
  }

  public void setSeparateDefault(final boolean separateDefault) {
    this.separateDefault = separateDefault;
  }

  public boolean isSeparateDefault() {
    return separateDefault;
  }

  public List<APNGfcTLChunk> getFctl() {
    return fctl;
  }

  public void setFctl(final List<APNGfcTLChunk> fctl) {
    this.fctl = fctl;
  }

  public APNGacTLChunk getActl() {
    return actl;
  }

  public void setActl(final APNGacTLChunk actl) {
    this.actl = actl;
  }

  public APNGIHDRChunk getIhdr() {
    return ihdr;
  }

  public void setIhdr(final APNGIHDRChunk ihdr) {
    this.ihdr = ihdr;
  }

  public APNGPLTEChunk getPlte() {
    return plte;
  }

  public void setPlte(final APNGPLTEChunk plte) {
    this.plte = plte;
  }
  
  public APNGIENDChunk getIend() {
	return iend;
  }

  public void setIend(APNGIENDChunk iend) {
	this.iend = iend;
  }

  // -- Helper Methods --


/* @see Metadata#resetMeta() */
  public void reset() {
    super.reset(this.getClass());
    fctl = new ArrayList<APNGfcTLChunk>();
    idat = new ArrayList<APNGIDATChunk>();
  }
}
