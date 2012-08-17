package ome.scifio.apng;

/**
 * Represents the IDAT chunk of the APNG image format.
 * 
 * The IDAT chunk is simply a dump of compressed image
 * data for a single plane (the default image for the file).
 *
 */
public class APNGIDATChunk extends APNGChunk {

  // -- Constructor --

  public APNGIDATChunk() {
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x44, 0x41, 0x54};
  }

}
