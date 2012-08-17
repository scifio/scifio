package ome.scifio.apng;

import ome.scifio.FieldPrinter;

/**
 * A parent class for all APNG Chunk classes.
 * 
 * Provides a length and offset (in the overall file stream)
 * field.
 * 
 * Each chunk should instantiate and define its own CHUNK_SIGNATURE.
 *
 */
public class APNGChunk {

  // -- Constants --
  public static final byte[] PNG_SIGNATURE = new byte[] {
      (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};

  // -- Fields --

  // Offset in the file data stream. Points to the start of the
  // data of the chunk, which comes after an entry for the length
  // and the chunk's signature.
  private long offset;

  // Length of the chunk
  private int length;

  // Unique chunk type signature (e.g. "IHDR")
  protected byte[] CHUNK_SIGNATURE;

  // -- Methods --

  public byte[] getCHUNK_SIGNATURE() {
    return CHUNK_SIGNATURE;
  }

  public int[] getFrameCoordinates() {
    return new int[0];
  }

  public void setOffset(final long offset) {
    this.offset = offset;
  }

  public long getOffset() {
    return offset;
  }

  public void setLength(final int length) {
    this.length = length;
  }

  public int getLength() {
    return length;
  }
  
  @Override
  public String toString() {
    return new FieldPrinter(this).toString();
  }

}
