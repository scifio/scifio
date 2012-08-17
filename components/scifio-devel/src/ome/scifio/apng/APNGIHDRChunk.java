package ome.scifio.apng;

import ome.scifio.Field;

/**
 * Represents the IHDR chunk of the APNG image format.
 * 
 * The IHDR chunk is a critical chunk for all APNG
 * and PNG images. It contains basic information
 * about the image.
 * 
 * The IHDR is always the first chunk of a correct
 * PNG or APNG image file.
 *
 */
public class APNGIHDRChunk extends APNGChunk {

  // -- Constructor --

  public APNGIHDRChunk() {
    this.CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x48, 0x44, 0x52};
  }

  // -- Fields --

  @Field(label = "Width")
  private int width;

  @Field(label = "height")
  private int height;

  @Field(label = "Bit depth")
  private byte bitDepth;

  @Field(label = "Colour type")
  private byte colourType;

  @Field(label = "Compression Method")
  private byte compressionMethod;

  @Field(label = "Filter method")
  private byte filterMethod;

  @Field(label = "Interlace method")
  private byte interlaceMethod;

  // -- Methods --

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

  public byte getBitDepth() {
    return bitDepth;
  }

  public void setBitDepth(final byte bitDepth) {
    this.bitDepth = bitDepth;
  }

  public byte getColourType() {
    return colourType;
  }

  public void setColourType(final byte colourType) {
    this.colourType = colourType;
  }

  public byte getCompressionMethod() {
    return compressionMethod;
  }

  public void setCompressionMethod(final byte compressionMethod) {
    this.compressionMethod = compressionMethod;
  }

  public byte getFilterMethod() {
    return filterMethod;
  }

  public void setFilterMethod(final byte filterMethod) {
    this.filterMethod = filterMethod;
  }

  public byte getInterlaceMethod() {
    return interlaceMethod;
  }

  public void setInterlaceMethod(final byte interlaceMethod) {
    this.interlaceMethod = interlaceMethod;
  }
}
