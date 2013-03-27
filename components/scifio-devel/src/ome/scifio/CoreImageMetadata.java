package ome.scifio;

import java.util.Hashtable;

import net.imglib2.meta.AxisType;

/**
 * CoreImageMetadata consists of the metadata fields common 
 * to any image type, expressed in a standardized way within
 *  SCIFIO.
 *
 */
public class CoreImageMetadata {

  // -- Fields --

  /** Number of planes in this image */
  @Field(label = "planeCount")
  private int planeCount;

  /** 8-bit lookup table for this image */
  @Field(label = "lut")
  private byte[][] lut;

  /** Width (in pixels) of thumbnail planes in this image. */
  @Field(label = "thumbSizeX")
  private int thumbSizeX;

  /** Height (in pixels) of thumbnail planes in this image. */
  @Field(label = "thumbSizeY")
  private int thumbSizeY;

  /**
   * Describes the number of bytes per pixel.  Must be one of the <i>static</i>
   * pixel types (e.g. <code>INT8</code>) in {@link ome.scifio.util.FormatTools}.
   */
  @Field(label = "pixelType")
  private int pixelType;

  /** Number of valid bits per pixel. */
  @Field(label = "bitsPerPixel")
  private int bitsPerPixel;

  /** Length of each subdimension of C. */
  @Field(label = "cLengths")
  private int[] cLengths;

  /** Name of each subdimension of C. */
  @Field(label = "cTypes")
  private String[] cTypes;

  /** The Axes types for this image. Order is implied by ordering within this array */
  @Field(label = "dimTypes")
  private AxisType[] axisTypes;

  /** Lengths of each axis. Order is parallel of dimTypes. */
  @Field(label = "dimLengths")
  private int[] axisLengths;

  /**
   * Indicates whether or not we are confident that the
   * dimension order is correct.
   */
  @Field(label = "orderCertain")
  private boolean orderCertain;

  /**
   * Indicates whether or not the images are stored as RGB
   * (multiple channels per plane).
   */
  @Field(label = "rgb")
  private boolean rgb;

  /** Indicates whether or not each pixel's bytes are in little endian order. */
  @Field(label = "littleEndian")
  private boolean littleEndian;

  /**
   * True if channels are stored RGBRGBRGB...; false if channels are stored
   * RRR...GGG...BBB...
   */
  @Field(label = "interleaved")
  private boolean interleaved;

  /** Indicates whether or not the images are stored as indexed color. */
  @Field(label = "indexed")
  private boolean indexed;

  /** Indicates whether or not we can ignore the color map (if present). */
  @Field(label = "falseColor")
  private boolean falseColor = true;

  /**
   * Indicates whether or not we are confident that all of the metadata stored
   * within the file has been parsed.
   */
  @Field(label = "metadataComplete")
  private boolean metadataComplete;

  /** Non-core metadata associated with this series. */
  @Field(label = "imageMetadata")
  private Hashtable<String, Object> imageMetadata;

  /**
   * Indicates whether or not this series is a lower-resolution copy of
   * another series.
   */
  @Field(label = "thumbnail")
  private boolean thumbnail;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  // -- Constructors --

  public CoreImageMetadata() {
    imageMetadata = new Hashtable<String, Object>();
  }

  // -- Setters -- 

  public void setLut(final byte[][] lut) {
    this.lut = lut;
  }

  public void setThumbSizeX(final int thumbSizeX) {
    this.thumbSizeX = thumbSizeX;
  }

  public void setThumbSizeY(final int thumbSizeY) {
    this.thumbSizeY = thumbSizeY;
  }

  public void setPixelType(final int pixelType) {
    this.pixelType = pixelType;
  }

  public void setBitsPerPixel(final int bitsPerPixel) {
    this.bitsPerPixel = bitsPerPixel;
  }

  public void setcLengths(final int[] cLengths) {
    this.cLengths = cLengths;
  }

  public void setcTypes(final String[] cTypes) {
    this.cTypes = cTypes;
  }

  public void setOrderCertain(final boolean orderCertain) {
    this.orderCertain = orderCertain;
  }

  public void setRgb(final boolean rgb) {
    this.rgb = rgb;
  }

  public void setLittleEndian(final boolean littleEndian) {
    this.littleEndian = littleEndian;
  }

  public void setInterleaved(final boolean interleaved) {
    this.interleaved = interleaved;
  }

  public void setIndexed(final boolean indexed) {
    this.indexed = indexed;
  }

  public void setFalseColor(final boolean falseColor) {
    this.falseColor = falseColor;
  }

  public void setMetadataComplete(final boolean metadataComplete) {
    this.metadataComplete = metadataComplete;
  }

  public void setImageMetadata(final Hashtable<String, Object> imageMetadata) {
    this.imageMetadata = imageMetadata;
  }

  public void setThumbnail(final boolean thumbnail) {
    this.thumbnail = thumbnail;
  }

  public void setAxisTypes(final AxisType[] axisTypes) {
    this.axisTypes = axisTypes;
  }

  public void setAxisLengths(final int[] axisLengths) {
    this.axisLengths = axisLengths;
  }

  public void setAxisLength(final AxisType axis, final int length) {
    for (int i = 0; i < axisTypes.length; i++) {
      if (axisTypes[i] == axis) {
        axisLengths[i] = length;
      }
    }
  }

  public void setAxisType(final int index, final AxisType axis) {
    axisTypes[index] = axis;
  }

  public void setPlaneCount(final int planeCount) {
    this.planeCount = planeCount;
  }
  
  // -- Getters --
    
  public int getPlaneCount() {
    return planeCount;
  }

  public byte[][] getLut() {
    return lut;
  }

  public int getThumbSizeX() {
    return thumbSizeX;
  }

  public int getThumbSizeY() {
    return thumbSizeY;
  }

  public int getPixelType() {
    return pixelType;
  }

  public int getBitsPerPixel() {
    return bitsPerPixel;
  }

  public int[] getcLengths() {
    return cLengths;
  }

  public String[] getcTypes() {
    return cTypes;
  }

  public AxisType[] getAxisTypes() {
    return axisTypes;
  }

  public int[] getAxisLengths() {
    return axisLengths;
  }

  public boolean isOrderCertain() {
    return orderCertain;
  }

  public boolean isRgb() {
    return rgb;
  }

  public boolean isLittleEndian() {
    return littleEndian;
  }

  public boolean isInterleaved() {
    return interleaved;
  }

  public boolean isIndexed() {
    return indexed;
  }

  public boolean isFalseColor() {
    return falseColor;
  }

  public boolean isMetadataComplete() {
    return metadataComplete;
  }

  public Hashtable<String, Object> getImageMetadata() {
    return imageMetadata;
  }

  public boolean isThumbnail() {
    return thumbnail;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  // -- Object API --
  
  @Override
  public String toString() {
    return new FieldPrinter(this).toString();
  }
}
