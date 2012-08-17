package ome.scifio.apng;

import java.util.ArrayList;
import java.util.List;

import ome.scifio.Field;

/**
 * Represents the fcTL chunk of the APNG image format.
 * 
 * The fcTL chunk contains metadata for a matching fdAT
 * chunk, or IDAT chunk (if the default image is also
 * the first frame of the animation).
 *
 */
public class APNGfcTLChunk extends APNGChunk {

  // -- Fields --

  /** Sequence number of the animation chunk, starting from 0 */
  @Field(label = "sequence_number")
  private int sequenceNumber;

  /** Width of the following frame */
  @Field(label = "width")
  private int width;

  /** Height of the following frame */
  @Field(label = "height")
  private int height;

  /** X position at which to render the following frame */
  @Field(label = "x_offset")
  private int xOffset;

  /** Y position at which to render the following frame */
  @Field(label = "y_offset")
  private int yOffset;

  /** Frame delay fraction numerator */
  @Field(label = "delay_num")
  private short delayNum;

  /** Frame delay fraction denominator */
  @Field(label = "delay_den")
  private short delayDen;

  /** Type of frame area disposal to be done after rendering this frame */
  @Field(label = "dispose_op")
  private byte disposeOp;

  /** Type of frame area rendering for this frame */
  @Field(label = "blend_op")
  private byte blendOp;

  private final List<APNGfdATChunk> fdatChunks;

  // -- Constructor --

  public APNGfcTLChunk() {
    this.fdatChunks = new ArrayList<APNGfdATChunk>();
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x66, 0x63, 0x54, 0x4C};
  }

  // -- Methods --

  public void addChunk(final APNGfdATChunk chunk) {
    this.fdatChunks.add(chunk);
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(final int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public int getxOffset() {
    return xOffset;
  }

  public void setxOffset(final int xOffset) {
    this.xOffset = xOffset;
  }

  public int getyOffset() {
    return yOffset;
  }

  public void setyOffset(final int yOffset) {
    this.yOffset = yOffset;
  }

  public short getDelayNum() {
    return delayNum;
  }

  public void setDelayNum(final short delayNum) {
    this.delayNum = delayNum;
  }

  public short getDelayDen() {
    return delayDen;
  }

  public void setDelayDen(final short delayDen) {
    this.delayDen = delayDen;
  }

  public byte getDisposeOp() {
    return disposeOp;
  }

  public void setDisposeOp(final byte disposeOp) {
    this.disposeOp = disposeOp;
  }

  public byte getBlendOp() {
    return blendOp;
  }

  public void setBlendOp(final byte blendOp) {
    this.blendOp = blendOp;
  }

  public List<APNGfdATChunk> getFdatChunks() {
    return fdatChunks;
  }

  // -- Helper Method --
  @Override
  public int[] getFrameCoordinates() {
    return new int[] {xOffset, yOffset, width, height};
  }
}
