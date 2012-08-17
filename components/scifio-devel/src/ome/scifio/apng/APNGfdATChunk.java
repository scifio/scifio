package ome.scifio.apng;

import ome.scifio.Field;

/**
 * Represents the fdAT chunk of the APNG image format.
 * 
 * The fdAT chunk is identical in concept to the IDAT chunk:
 * a container for compressed image data for a single frame.
 * 
 * In the case of fdAT chunks, the image is of a non-default
 * frame.
 * 
 * Each fdAT chunk is paired with an fcTL chunk.
 *
 */
public class APNGfdATChunk extends APNGChunk {

  // -- Constructor --

  public APNGfdATChunk() {
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x66, 0x64, 0x41, 0x54};
  }

  // -- Fields --

  /** Sequence number of the animation chunk, starting from 0 */
  @Field(label = "sequence_number")
  private int sequenceNumber;

  // -- Methods --

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(final int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
}
