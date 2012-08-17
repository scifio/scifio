package ome.scifio.apng;

import ome.scifio.Field;

/**
 * Represents the acTL chunk of the APNG image format.
 * 
 * There is one acTL chunk per APNG image, and is not
 * present in PNG files.
 * 
 * The acTL chunk contains metadata describing the number
 * of frames in the image, and how many times the animation
 * sequence should be played.
 *
 */
public class APNGacTLChunk extends APNGChunk {

  // -- Constructor --

  public APNGacTLChunk() {
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x61, 0x63, 0x54, 0x4C};
  }

  // -- Fields --

  /** Sequence number of the animation chunk, starting from 0 */
  @Field(label = "sequence_number")
  private int sequenceNumber;

  /** Number of frames in this APNG file */
  @Field(label = "num_frames")
  private int numFrames;

  /** Times to play the animation sequence */
  @Field(label = "num_plays")
  private int numPlays;

  // -- Methods --

  public int getNumFrames() {
    return numFrames;
  }

  public void setNumFrames(final int numFrames) {
    this.numFrames = numFrames;
  }

  public int getNumPlays() {
    return numPlays;
  }

  public void setNumPlays(final int numPlays) {
    this.numPlays = numPlays;
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(final int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

}
