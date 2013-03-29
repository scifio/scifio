/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */
package ome.scifio;

import java.util.Hashtable;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * Abstract superclass of all {@link ome.scifio.ImageMetadata} implementations.
 * 
 * @see ome.scifio.ImageMetadata
 * @see ome.scifio.DefaultImageMetadata
 * 
 * @author Mark Hiner
 */
public abstract class AbstractImageMetadata implements ImageMetadata {

  // -- Constants --
  
  /** Default thumbnail width and height. */
  protected static final int THUMBNAIL_DIMENSION = 128;

  // -- Fields --
  
  /** Number of planes in this image */
  @Field(label = "planeCount")
  private int planeCount;

  /** Width (in pixels) of thumbnail planes in this image. */
  @Field(label = "thumbSizeX")
  private int thumbSizeX = 0;

  /** Height (in pixels) of thumbnail planes in this image. */
  @Field(label = "thumbSizeY")
  private int thumbSizeY = 0;

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

  // -- Constructors --

  public AbstractImageMetadata() {
    imageMetadata = new Hashtable<String, Object>();
  }
  
  public AbstractImageMetadata(ImageMetadata copy) {
    copy(copy);
  }

  // -- Setters -- 

  /*
   * @see ome.scifio.ImageMetadata#setThumbSizeX(int)
   */
  public void setThumbSizeX(final int thumbSizeX) {
    this.thumbSizeX = thumbSizeX;
  }

  /*
   * @see ome.scifio.ImageMetadata#setThumbSizeY(int)
   */
  public void setThumbSizeY(final int thumbSizeY) {
    this.thumbSizeY = thumbSizeY;
  }

  /*
   * @see ome.scifio.ImageMetadata#setPixelType(int)
   */
  public void setPixelType(final int pixelType) {
    this.pixelType = pixelType;
  }

  /*
   * @see ome.scifio.ImageMetadata#setBitsPerPixel(int)
   */
  public void setBitsPerPixel(final int bitsPerPixel) {
    this.bitsPerPixel = bitsPerPixel;
  }

  /*
   * @see ome.scifio.ImageMetadata#setChannelLengths(int[])
   */
  public void setChannelLengths(final int[] cLengths) {
    this.cLengths = cLengths;
  }

  /*
   * @see ome.scifio.ImageMetadata#setChannelTypes(java.lang.String[])
   */
  public void setChannelTypes(final String[] cTypes) {
    this.cTypes = cTypes;
  }

  /*
   * @see ome.scifio.ImageMetadata#setOrderCertain(boolean)
   */
  public void setOrderCertain(final boolean orderCertain) {
    this.orderCertain = orderCertain;
  }

  /*
   * @see ome.scifio.ImageMetadata#setRGB(boolean)
   */
  public void setRGB(final boolean rgb) {
    this.rgb = rgb;
  }

  /*
   * @see ome.scifio.ImageMetadata#setLittleEndian(boolean)
   */
  public void setLittleEndian(final boolean littleEndian) {
    this.littleEndian = littleEndian;
  }

  /*
   * @see ome.scifio.ImageMetadata#setInterleaved(boolean)
   */
  public void setInterleaved(final boolean interleaved) {
    this.interleaved = interleaved;
  }

  /*
   * @see ome.scifio.ImageMetadata#setIndexed(boolean)
   */
  public void setIndexed(final boolean indexed) {
    this.indexed = indexed;
  }

  /*
   * @see ome.scifio.ImageMetadata#setFalseColor(boolean)
   */
  public void setFalseColor(final boolean falseColor) {
    this.falseColor = falseColor;
  }

  /*
   * @see ome.scifio.ImageMetadata#setMetadataComplete(boolean)
   */
  public void setMetadataComplete(final boolean metadataComplete) {
    this.metadataComplete = metadataComplete;
  }

  /*
   * @see ome.scifio.ImageMetadata#setImageMetadata(java.util.Hashtable)
   */
  public void setImageMetadata(final Hashtable<String, Object> imageMetadata) {
    this.imageMetadata = imageMetadata;
  }

  /*
   * @see ome.scifio.ImageMetadata#setThumbnail(boolean)
   */
  public void setThumbnail(final boolean thumbnail) {
    this.thumbnail = thumbnail;
  }

  /*
   * @see ome.scifio.ImageMetadata#setAxisTypes(net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final AxisType[] axisTypes) {
    this.axisTypes = axisTypes;
  }

  /*
   * @see ome.scifio.ImageMetadata#setAxisLengths(int[])
   */
  public void setAxisLengths(final int[] axisLengths) {
    this.axisLengths = axisLengths;
  }

  /*
   * @see ome.scifio.ImageMetadata#setAxisLength(net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(final AxisType axis, final int length) {
    int axisIndex = getAxisIndex(axis);
    
    if (axisIndex == -1)
      addAxis(axis, length);
    else
      axisLengths[axisIndex] = length;
  }

  /*
   * @see ome.scifio.ImageMetadata#setAxisType(int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(final int index, final AxisType axis) {
    axisTypes[index] = axis;
  }

  /*
   * @see ome.scifio.ImageMetadata#setPlaneCount(int)
   */
  public void setPlaneCount(final int planeCount) {
    this.planeCount = planeCount;
  }
  
  // -- Getters --
    
  /*
   * @see ome.scifio.ImageMetadata#getPlaneCount()
   */
  public int getPlaneCount() {
    return planeCount;
  }

  /*
   * @see ome.scifio.ImageMetadata#getThumbSizeX()
   */
  public int getThumbSizeX() {
    int thumbX = thumbSizeX;
    
    // If the X thumbSize isn't explicitly set, scale the actual width using
    // the thumbnail dimension constant
    if (thumbX == 0) {
      int sx = getAxisLength(Axes.X);
      int sy = getAxisLength(Axes.Y);
      
      if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION)
        thumbX = sx;
      else if (sx > sy) thumbX = THUMBNAIL_DIMENSION;
      else if (sy > 0) thumbX = sx * THUMBNAIL_DIMENSION / sy;
      if (thumbX == 0) thumbX = 1;
    }
    
    return thumbX;
  }

  /*
   * @see ome.scifio.ImageMetadata#getThumbSizeY()
   */
  public int getThumbSizeY() {
    int thumbY = thumbSizeY;
    
    // If the Y thumbSize isn't explicitly set, scale the actual width using
    // the thumbnail dimension constant
    if (thumbY == 0) {
      int sx = getAxisLength(Axes.X);
      int sy = getAxisLength(Axes.Y);
      thumbY = 1;
      
      if (sx < THUMBNAIL_DIMENSION && sy < THUMBNAIL_DIMENSION)
        thumbY = sy;
      else if (sy > sx) thumbY = THUMBNAIL_DIMENSION;
      else if (sx > 0) thumbY = sy * THUMBNAIL_DIMENSION / sx;
      if (thumbY == 0) thumbY = 1;
    }
    
    return thumbY;
  }

  /*
   * @see ome.scifio.ImageMetadata#getPixelType()
   */
  public int getPixelType() {
    return pixelType;
  }

  /*
   * @see ome.scifio.ImageMetadata#getBitsPerPixel()
   */
  public int getBitsPerPixel() {
    return bitsPerPixel;
  }

  /*
   * @see ome.scifio.ImageMetadata#getChannelLengths()
   */
  public int[] getChannelLengths() {
    return cLengths;
  }

  /*
   * @see ome.scifio.ImageMetadata#getChannelTypes()
   */
  public String[] getChannelTypes() {
    return cTypes;
  }

  /*
   * @see ome.scifio.ImageMetadata#getAxes()
   */
  public AxisType[] getAxes() {
    return axisTypes;
  }

  /*
   * @see ome.scifio.ImageMetadata#getAxesLengths()
   */
  public int[] getAxesLengths() {
    return axisLengths;
  }

  /*
   * @see ome.scifio.ImageMetadata#isOrderCertain()
   */
  public boolean isOrderCertain() {
    return orderCertain;
  }

  /*
   * @see ome.scifio.ImageMetadata#isRGB()
   */
  public boolean isRGB() {
    return rgb;
  }

  /*
   * @see ome.scifio.ImageMetadata#isLittleEndian()
   */
  public boolean isLittleEndian() {
    return littleEndian;
  }

  /*
   * @see ome.scifio.ImageMetadata#isInterleaved()
   */
  public boolean isInterleaved() {
    return interleaved;
  }

  /*
   * @see ome.scifio.ImageMetadata#isIndexed()
   */
  public boolean isIndexed() {
    return indexed;
  }

  /*
   * @see ome.scifio.ImageMetadata#isFalseColor()
   */
  public boolean isFalseColor() {
    return falseColor;
  }

  /*
   * @see ome.scifio.ImageMetadata#isMetadataComplete()
   */
  public boolean isMetadataComplete() {
    return metadataComplete;
  }

  /*
   * @see ome.scifio.ImageMetadata#getImageMetadata()
   */
  public Hashtable<String, Object> getImageMetadata() {
    return imageMetadata;
  }

  /*
   * @see ome.scifio.ImageMetadata#isThumbnail()
   */
  public boolean isThumbnail() {
    return thumbnail;
  }

  /*
   * @see ome.scifio.ImageMetadata#getEffectiveSizeC()
   */
  public int getEffectiveSizeC() {
    final int sizeZT = getAxisLength(Axes.Z)* getAxisLength(Axes.TIME);
    if(sizeZT == 0) return 0;
    return getPlaneCount() / sizeZT;
  }

  /*
   * @see ome.scifio.ImageMetadata#getRGBChannelCount()
   */
  public int getRGBChannelCount() {
    if (!isRGB()) return 1;
    
    final int effC = getEffectiveSizeC();
    if (effC == 0) return 0;
    return getAxisLength(Axes.CHANNEL) / effC;
  } 

  /*
   * @see ome.scifio.ImageMetadata#getAxisType(int)
   */
  public AxisType getAxisType(final int planeIndex) {
    return getAxes()[planeIndex];
  }

  /*
   * @see ome.scifio.ImageMetadata#getAxisLength(int)
   */
  public int getAxisLength(final int planeIndex) {
    return planeIndex == -1 ? 0 : getAxesLengths()[planeIndex];
  }
  
  /*
   * @see ome.scifio.ImageMetadata#getAxisLength(net.imglib2.meta.AxisType)
   */
  public int getAxisLength(final AxisType t) {
    return getAxisLength(getAxisIndex(t));
  }

  /*
   * @see ome.scifio.ImageMetadata#getAxisIndex(net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(final AxisType type) {
    if (getAxes() == null) return -1;
    
    for (int i = 0; i < getAxes().length; i++) {
      if (getAxes()[i] == type) return i;
    }
    return -1; // throw exception?
  }

  /*
   * @see ome.scifio.ImageMetadata#addAxis(net.imglib2.meta.AxisType)
   */
  public void addAxis(final AxisType type) {
    addAxis(type, 0);
  }

  /*
   * @see ome.scifio.ImageMetadata#addAxis(net.imglib2.meta.AxisType, int)
   */
  public void addAxis(final AxisType type, final int value)
  {
    final int[] axisLengths = getAxesLengths();
    final AxisType[] axisTypes = getAxes();
    int newLength = axisLengths == null ? 1 : axisLengths.length + 1;
    
    final int[] tmpAxisLength = new int[newLength];
    final AxisType[] tmpAxisTypes = new AxisType[newLength];

    for (int i=0; i<newLength-1; i++) {
      tmpAxisLength[i] = axisLengths[i];
      tmpAxisTypes[i] = axisTypes[i];
    }

    tmpAxisLength[tmpAxisLength.length - 1] = value;
    tmpAxisTypes[tmpAxisTypes.length - 1] = type;

    setAxisLengths(tmpAxisLength);
    setAxisTypes(tmpAxisTypes);
  }
  
  public void copy(ImageMetadata toCopy) {
    imageMetadata = (Hashtable<String, Object>) toCopy.getImageMetadata().clone();
    
    axisLengths = toCopy.getAxesLengths().clone();
    axisTypes = toCopy.getAxes().clone();
    bitsPerPixel = toCopy.getBitsPerPixel();
    cLengths = toCopy.getChannelLengths().clone();
    cTypes = toCopy.getChannelTypes().clone();
    falseColor = toCopy.isFalseColor();
    indexed = toCopy.isIndexed();
    interleaved = toCopy.isInterleaved();
    littleEndian = toCopy.isLittleEndian();
    metadataComplete = toCopy.isMetadataComplete();
    orderCertain = toCopy.isOrderCertain();
    pixelType = toCopy.getPixelType();
    planeCount = toCopy.getPlaneCount();
    rgb = toCopy.isRGB();
    thumbnail = toCopy.isThumbnail();
    thumbSizeX = toCopy.getThumbSizeX();
    thumbSizeY = toCopy.getThumbSizeY();
  }
  
  // -- Serializable API Methods --

  // -- Object API --
  
  @Override
  public String toString() {
    return new FieldPrinter(this).toString();
  }
}
