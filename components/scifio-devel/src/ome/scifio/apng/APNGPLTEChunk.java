package ome.scifio.apng;

/**
 * Represents the PLTE chunk of the APNG image format.
 * 
 * The PLTE chunk contains color palette data for the current
 * image and is only present in certain ARGB color formats.
 *
 */
public class APNGPLTEChunk extends APNGChunk {

  // -- Constructor --

  public APNGPLTEChunk() {
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x50, 0x4C, 0x54, 0x45};
  }

  // -- Fields --

  // Red palette entries
  private byte[] red;

  // Green palette entries
  private byte[] green;

  // Blue palette entries
  private byte[] blue;

  // -- Methods --

  public byte[] getRed() {
    return red;
  }

  public void setRed(final byte[] red) {
    this.red = red;
  }

  public byte[] getGreen() {
    return green;
  }

  public void setGreen(final byte[] green) {
    this.green = green;
  }

  public byte[] getBlue() {
    return blue;
  }

  public void setBlue(final byte[] blue) {
    this.blue = blue;
  }

}
