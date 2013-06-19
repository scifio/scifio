/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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
package io.scif;

import io.scif.common.DataTools;
import io.scif.util.FormatTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * Abstract superclass of all {@link io.scif.ImageMetadata} implementations.
 * 
 * @see io.scif.ImageMetadata
 * @see io.scif.DefaultImageMetadata
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
   * pixel types (e.g. <code>INT8</code>) in {@link io.scif.util.FormatTools}.
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
  private List<AxisType> axisTypes;
  
  /** Lengths of each axis. Order is parallel of dimTypes. */
  @Field(label = "dimLengths")
  private HashMap<AxisType, Integer> axisLengths;

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

  /**
   * Indicates whether or not this series is a lower-resolution copy of
   * another series.
   */
  @Field(label = "thumbnail")
  private boolean thumbnail;
  
  /* A table of Field key, value pairs */
  private MetaTable table;
  
  // -- Constructors --

  public AbstractImageMetadata() {
    axisTypes = new ArrayList<AxisType>();
    axisLengths = new HashMap<AxisType, Integer>();
  }
  
  public AbstractImageMetadata(ImageMetadata copy) {
    this();
    copy(copy);
  }

  // -- Setters -- 

  /*
   * @see io.scif.ImageMetadata#setThumbSizeX(int)
   */
  public void setThumbSizeX(final int thumbSizeX) {
    this.thumbSizeX = thumbSizeX;
  }

  /*
   * @see io.scif.ImageMetadata#setThumbSizeY(int)
   */
  public void setThumbSizeY(final int thumbSizeY) {
    this.thumbSizeY = thumbSizeY;
  }

  /*
   * @see io.scif.ImageMetadata#setPixelType(int)
   */
  public void setPixelType(final int pixelType) {
    this.pixelType = pixelType;
  }

  /*
   * @see io.scif.ImageMetadata#setBitsPerPixel(int)
   */
  public void setBitsPerPixel(final int bitsPerPixel) {
    this.bitsPerPixel = bitsPerPixel;
  }

  /*
   * @see io.scif.ImageMetadata#setChannelLengths(int[])
   */
  public void setChannelLengths(final int[] cLengths) {
    this.cLengths = cLengths;
  }

  /*
   * @see io.scif.ImageMetadata#setChannelTypes(java.lang.String[])
   */
  public void setChannelTypes(final String[] cTypes) {
    this.cTypes = cTypes;
  }

  /*
   * @see io.scif.ImageMetadata#setOrderCertain(boolean)
   */
  public void setOrderCertain(final boolean orderCertain) {
    this.orderCertain = orderCertain;
  }

  /*
   * @see io.scif.ImageMetadata#setRGB(boolean)
   */
  public void setRGB(final boolean rgb) {
    this.rgb = rgb;
  }

  /*
   * @see io.scif.ImageMetadata#setLittleEndian(boolean)
   */
  public void setLittleEndian(final boolean littleEndian) {
    this.littleEndian = littleEndian;
  }

  /*
   * @see io.scif.ImageMetadata#setInterleaved(boolean)
   */
  public void setInterleaved(final boolean interleaved) {
    this.interleaved = interleaved;
  }

  /*
   * @see io.scif.ImageMetadata#setIndexed(boolean)
   */
  public void setIndexed(final boolean indexed) {
    this.indexed = indexed;
  }

  /*
   * @see io.scif.ImageMetadata#setFalseColor(boolean)
   */
  public void setFalseColor(final boolean falseColor) {
    this.falseColor = falseColor;
  }

  /*
   * @see io.scif.ImageMetadata#setMetadataComplete(boolean)
   */
  public void setMetadataComplete(final boolean metadataComplete) {
    this.metadataComplete = metadataComplete;
  }

  /*
   * @see io.scif.ImageMetadata#setThumbnail(boolean)
   */
  public void setThumbnail(final boolean thumbnail) {
    this.thumbnail = thumbnail;
  }

  /*
   * @see io.scif.ImageMetadata#setAxes(net.imglib2.meta.AxisType[], int[])
   */
  public void setAxes(AxisType[] axisTypes, int[] axisLengths) {
    setAxisTypes(axisTypes);
    setAxisLengths(axisLengths);
  }
  
  /*
   * @see io.scif.ImageMetadata#setAxisTypes(net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final AxisType[] axisTypes) {
    this.axisTypes = new ArrayList<AxisType>(Arrays.asList(axisTypes));
  }

  /*
   * @see io.scif.ImageMetadata#setAxisLengths(int[])
   */
  public void setAxisLengths(final int[] axisLengths) {
    if (axisLengths.length != axisTypes.size())
      throw new IllegalArgumentException("Tried to set " + axisLengths.length +
          " axis lengths, but " + axisTypes.size() + " axes present." +
          " Call setAxisTypes first.");
    
    for (int i=0; i<axisTypes.size(); i++) {
      this.axisLengths.put(axisTypes.get(i), axisLengths[i]);
    }
  }

  /*
   * @see io.scif.ImageMetadata#setAxisLength(net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(final AxisType axis, final int length) {
    addAxis(axis, length);
  }

  /*
   * @see io.scif.ImageMetadata#setAxisType(int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(final int index, final AxisType axis) {
    int oldIndex = getAxisIndex(axis);
    
    // Replace existing axis
    if (oldIndex == -1) {
      int length = axisLengths.remove(axisTypes.get(index));

      axisTypes.set(index, axis);
      axisLengths.put(axis, length);
    }
    // Axis is already in the list. Move it here.
    else {
      axisTypes.remove(axis);
      axisTypes.add(index, axis);
    }
  }

  /*
   * @see io.scif.ImageMetadata#setPlaneCount(int)
   */
  public void setPlaneCount(final int planeCount) {
    this.planeCount = planeCount;
  }
  
  // -- Getters --
    
  /*
   * @see io.scif.ImageMetadata#getPlaneCount()
   */
  public int getPlaneCount() {
    return planeCount;
  }
  
  
  /*
   * @see io.scif.ImageMetadata#getSize()
   */
  public long getSize() {
    long size = 1;
    
    for (AxisType a : axisTypes) {
      size = DataTools.safeMultiply64(size, getAxisLength(a));
    }
    
    int bytesPerPixel = getBitsPerPixel() / 8;
    
    return DataTools.safeMultiply64(size, bytesPerPixel);
  }

  /*
   * @see io.scif.ImageMetadata#getThumbSizeX()
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
   * @see io.scif.ImageMetadata#getThumbSizeY()
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
   * @see io.scif.ImageMetadata#getPixelType()
   */
  public int getPixelType() {
    return pixelType;
  }

  /*
   * @see io.scif.ImageMetadata#getBitsPerPixel()
   */
  public int getBitsPerPixel() {
    return bitsPerPixel;
  }

  /*
   * @see io.scif.ImageMetadata#getChannelLengths()
   */
  public int[] getChannelLengths() {
    if (cLengths == null) {
      cLengths = new int[] {getAxisLength(Axes.CHANNEL)};
    }
    return cLengths;
  }

  /*
   * @see io.scif.ImageMetadata#getChannelTypes()
   */
  public String[] getChannelTypes() {
    if (cTypes == null) {
      cTypes = new String[] {FormatTools.CHANNEL};
    }
    return cTypes;
  }

  /*
   * @see io.scif.ImageMetadata#getAxes()
   */
  public AxisType[] getAxes() {
    return axisTypes.toArray(new AxisType[axisTypes.size()]);
  }

  /*
   * @see io.scif.ImageMetadata#getAxesLengths()
   */
  public int[] getAxesLengths() {
    int[] lengths = new int[axisTypes.size()];
    
    for (int i=0; i<axisTypes.size(); i++) {
      lengths[i] = getAxisLength(axisTypes.get(i));
    }
    
    return lengths;
  }

  /*
   * @see io.scif.ImageMetadata#isOrderCertain()
   */
  public boolean isOrderCertain() {
    return orderCertain;
  }

  /*
   * @see io.scif.ImageMetadata#isRGB()
   */
  public boolean isRGB() {
    return rgb;
  }

  /*
   * @see io.scif.ImageMetadata#isLittleEndian()
   */
  public boolean isLittleEndian() {
    return littleEndian;
  }

  /*
   * @see io.scif.ImageMetadata#isInterleaved()
   */
  public boolean isInterleaved() {
    return interleaved;
  }

  /*
   * @see io.scif.ImageMetadata#isIndexed()
   */
  public boolean isIndexed() {
    return indexed;
  }

  /*
   * @see io.scif.ImageMetadata#isFalseColor()
   */
  public boolean isFalseColor() {
    return falseColor;
  }

  /*
   * @see io.scif.ImageMetadata#isMetadataComplete()
   */
  public boolean isMetadataComplete() {
    return metadataComplete;
  }

  /*
   * @see io.scif.ImageMetadata#isThumbnail()
   */
  public boolean isThumbnail() {
    return thumbnail;
  }

  /*
   * @see io.scif.ImageMetadata#getEffectiveSizeC()
   */
  public int getEffectiveSizeC() {
    final int sizeZT = getAxisLength(Axes.Z)* getAxisLength(Axes.TIME);
    if(sizeZT == 0) return 0;
    return getPlaneCount() / sizeZT;
  }

  /*
   * @see io.scif.ImageMetadata#getRGBChannelCount()
   */
  public int getRGBChannelCount() {
    if (!isRGB()) return 1;
    
    final int effC = getEffectiveSizeC();
    if (effC == 0) return 0;
    return getAxisLength(Axes.CHANNEL) / effC;
  } 

  /*
   * @see io.scif.ImageMetadata#getAxisType(int)
   */
  public AxisType getAxisType(final int axisIndex) {
    return axisTypes.get(axisIndex);
  }

  /*
   * @see io.scif.ImageMetadata#getAxisLength(int)
   */
  public int getAxisLength(final int axisIndex) {
    if (axisIndex < 0 || axisIndex >= axisTypes.size())
      throw new IllegalArgumentException("Invalid axisIndex: " + axisIndex
          + ". " + axisTypes.size() + " axes present.");

    return axisLengths.get(axisTypes.get(axisIndex));
  }

  /*
   * @see io.scif.ImageMetadata#getAxisLength(net.imglib2.meta.AxisType)
   */
  public int getAxisLength(final AxisType t) {
    if (axisLengths == null || !axisLengths.containsKey(t)) return 1;
    
    return axisLengths.get(t);
  }

  /*
   * @see io.scif.ImageMetadata#getAxisIndex(net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(final AxisType type) {
    if (axisTypes == null) return -1;
    
    return axisTypes.indexOf(type);
  }

  /*
   * @see io.scif.ImageMetadata#addAxis(net.imglib2.meta.AxisType)
   */
  public void addAxis(final AxisType type) {
    addAxis(type, 0);
  }

  /*
   * @see io.scif.ImageMetadata#addAxis(net.imglib2.meta.AxisType, int)
   */
  public void addAxis(final AxisType type, final int value)
  {
    if (axisTypes == null) axisTypes = new ArrayList<AxisType>();
    
    // See if the axis already exists
    if (!axisTypes.contains(type)) axisTypes.add(type);
    
    axisLengths.put(type, value);
  }
  
  /*
   * @see io.scif.ImageMetadata#copy(io.scif.ImageMetadata)
   */
  public void copy(ImageMetadata toCopy) {
    table = new DefaultMetaTable(toCopy.getTable());
    
    axisTypes = new ArrayList<AxisType>(Arrays.asList(toCopy.getAxes()));
    setAxisLengths(toCopy.getAxesLengths().clone());
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
  
  // -- HasTable API Methods --

  /*
   * @see io.scif.HasMetaTable#getTable()
   */
  public MetaTable getTable() {
    if (table == null) table = new DefaultMetaTable();
    return table;
  }

  /*
   * @see io.scif.HasMetaTable#setTable(io.scif.MetaTable)
   */
  public void setTable(MetaTable table) {
    this.table = table;
  }
 
  // -- Serializable API Methods --

  // -- Object API --
  
  @Override
  public String toString() {
    return new FieldPrinter(this).toString();
  }
}
