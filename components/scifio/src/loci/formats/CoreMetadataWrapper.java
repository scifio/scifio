/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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
package loci.formats;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import ome.scifio.AbstractImageMetadata;
import ome.scifio.DefaultMetaTable;
import ome.scifio.ImageMetadata;
import ome.scifio.MetaTable;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import loci.legacy.adapter.Wrapper;

/**
 * This class is used for delegation in the Legacy
 * to Modern direction. It can be used in method signatures
 * expecting an {@link ome.scifio.ImageMetadata} - or,
 * more significantly, stored within an {@link ome.scifio.Metadata}
 * - but dynamically calculates all values using
 * the wrapped @{link loci.formats.CoreMetadata}
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataWrapper extends AbstractImageMetadata
  implements Wrapper<CoreMetadata>, ImageMetadata
{

  // -- Fields --
  
  private WeakReference<CoreMetadata> coreMeta;
  
  private int[] lengths = new int[5];
  private AxisType[] types = new AxisType[5];
  
  // -- Constructor --
  
  public CoreMetadataWrapper(CoreMetadata cMeta) {
    coreMeta = new WeakReference<CoreMetadata>(cMeta);
  }
  
  // -- Wrapper API Methods --

  public CoreMetadata unwrap() {
    return coreMeta.get();
  }
  
  // -- CoreImageMetadata methods --
  
  // -- Setters -- 

  /*
   * @see ome.scifio.AbstractImageMetadata#setThumbSizeX(int)
   */
  public void setThumbSizeX(final int thumbSizeX) {
    unwrap().thumbSizeX = thumbSizeX;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setThumbSizeY(int)
   */
  public void setThumbSizeY(final int thumbSizeY) {
    unwrap().thumbSizeY = thumbSizeY;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setPixelType(int)
   */
  public void setPixelType(final int pixelType) {
    unwrap().pixelType = pixelType;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setBitsPerPixel(int)
   */
  public void setBitsPerPixel(final int bitsPerPixel) {
    unwrap().bitsPerPixel = bitsPerPixel;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setOrderCertain(boolean)
   */
  public void setOrderCertain(final boolean orderCertain) {
    unwrap().orderCertain = orderCertain;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setRGB(boolean)
   */
  public void setRGB(final boolean rgb) {
    unwrap().rgb = rgb;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setLittleEndian(boolean)
   */
  public void setLittleEndian(final boolean littleEndian) {
    unwrap().littleEndian = littleEndian;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setInterleaved(boolean)
   */
  public void setInterleaved(final boolean interleaved) {
    unwrap().interleaved = interleaved;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setIndexed(boolean)
   */
  public void setIndexed(final boolean indexed) {
    unwrap().indexed = indexed;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setFalseColor(boolean)
   */
  public void setFalseColor(final boolean falseColor) {
    unwrap().falseColor = falseColor;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setMetadataComplete(boolean)
   */
  public void setMetadataComplete(final boolean metadataComplete) {
    unwrap().metadataComplete = metadataComplete;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setThumbnail(boolean)
   */
  public void setThumbnail(final boolean thumbnail) {
    unwrap().thumbnail = thumbnail;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setAxisTypes(net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final AxisType[] axisTypes) {
    unwrap().dimensionOrder = 
        ome.scifio.util.FormatTools.findDimensionOrder(axisTypes);
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setAxisLengths(int[])
   */
  public void setAxisLengths(final int[] axisLengths) {
    
    for(int i = 0; i < 5; i++) {
      switch(unwrap().dimensionOrder.charAt(i)) {
      case 'X': unwrap().sizeX = axisLengths[i];
        break;
      case 'Y': unwrap().sizeY = axisLengths[i];
        break;
      case 'Z': unwrap().sizeZ = axisLengths[i];
        break;
      case 'C': unwrap().sizeC = axisLengths[i];
        break;
      case 'T': unwrap().sizeT = axisLengths[i];
        break;
      default:
      }
    }
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setAxisLength(net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(final AxisType axis, final int length) {
    switch(axis.getLabel().toUpperCase().charAt(0)) {
    case 'X': unwrap().sizeX = length;
      break;
    case 'Y': unwrap().sizeY = length;
      break;
    case 'Z': unwrap().sizeZ = length;
      break;
    case 'C': unwrap().sizeC = length;
      break;
    case 'T': unwrap().sizeT = length;
      break;
    default:
    }
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setAxisType(int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(final int index, final AxisType axis) {
    String order = "";
    
    for(int i = 0; i < unwrap().dimensionOrder.length(); i++) {
      if(i == index) {
        order += axis.getLabel().toUpperCase().charAt(0);
      }
      else
        order += unwrap().dimensionOrder.charAt(i);
    }
      
    unwrap().dimensionOrder = order;
  }

  public void setPlaneCount(final int planeCount) {
    unwrap().imageCount = planeCount;
  }
  
  // -- Getters --
    
  /*
   * @see ome.scifio.AbstractImageMetadata#getPlaneCount()
   */
  public int getPlaneCount() {
    return unwrap().imageCount;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getThumbSizeX()
   */
  public int getThumbSizeX() {
    int thumbX = unwrap().thumbSizeX;
    
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
   * @see ome.scifio.AbstractImageMetadata#getThumbSizeY()
   */
  public int getThumbSizeY() {
    int thumbY = unwrap().thumbSizeY;
    
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
   * @see ome.scifio.AbstractImageMetadata#getPixelType()
   */
  public int getPixelType() {
    return unwrap().pixelType;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getBitsPerPixel()
   */
  public int getBitsPerPixel() {
    return unwrap().bitsPerPixel;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isOrderCertain()
   */
  public boolean isOrderCertain() {
    return unwrap().orderCertain;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isRGB()
   */
  public boolean isRGB() {
    return unwrap().rgb;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isLittleEndian()
   */
  public boolean isLittleEndian() {
    return unwrap().littleEndian;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isInterleaved()
   */
  public boolean isInterleaved() {
    return unwrap().interleaved;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isIndexed()
   */
  public boolean isIndexed() {
    return unwrap().indexed;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isFalseColor()
   */
  public boolean isFalseColor() {
    return unwrap().falseColor;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isMetadataComplete()
   */
  public boolean isMetadataComplete() {
    return unwrap().metadataComplete;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#isThumbnail()
   */
  public boolean isThumbnail() {
    return unwrap().thumbnail;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setChannelLengths(int[])
   */
  public void setChannelLengths(int[] cLengths) {
    unwrap().cLengths = cLengths;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#setChannelTypes(java.lang.String[])
   */
  public void setChannelTypes(String[] cTypes) {
    unwrap().cTypes = cTypes;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getChannelLengths()
   */
  public int[] getChannelLengths() {
    return unwrap().cLengths;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getChannelTypes()
   */
  public String[] getChannelTypes() {
    return unwrap().cTypes;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#addAxis(net.imglib2.meta.AxisType)
   */
  public void addAxis(AxisType type) {
    throw new UnsupportedOperationException("Can not add axes to legacy CoreMetadata.");
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#addAxis(net.imglib2.meta.AxisType, int)
   */
  public void addAxis(AxisType type, int value) {
    throw new UnsupportedOperationException("Can not add axes to legacy CoreMetadata.");
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxisType(int)
   */
  public AxisType getAxisType(int planeIndex) {
    return getAxes()[planeIndex];
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxisLength(int)
   */
  public int getAxisLength(int planeIndex) {
    return getAxesLengths()[planeIndex];
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxisLength(net.imglib2.meta.AxisType)
   */
  public int getAxisLength(AxisType t) {
    int index = getAxisIndex(t);
    
    if (index == -1) return -1;
    else return getAxesLengths()[index];
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxisIndex(net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(AxisType type) {
    AxisType[] axes = getAxes();
    
    for (int i=0; i<5; i++) {
      if (axes[i].equals(type)) return i;
    }
    
    return -1;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxes()
   */
  public AxisType[] getAxes() {
    types[0] = Axes.X;
    types[1] = Axes.Y;
    
    for (int i=2; i<5; i++) {
      switch(unwrap().dimensionOrder.charAt(i)) {
      case 'Z': types[i] = Axes.Z;
        break;
      case 'C': types[i] = Axes.CHANNEL;
        break;
      case 'T': types[i] = Axes.TIME;
        break;
      default: types[i] = Axes.UNKNOWN;
      }
    }
    
    return types;
  }

  /*
   * @see ome.scifio.AbstractImageMetadata#getAxesLengths()
   */
  public int[] getAxesLengths() {
    lengths[0] = unwrap().sizeX;
    lengths[1] = unwrap().sizeY;
    
    for(int i = 2; i < 5; i++) {
      switch(unwrap().dimensionOrder.charAt(i)) {
      case 'Z': lengths[i] = unwrap().sizeZ;
        break;
      case 'C': lengths[i] = unwrap().sizeC;
        break;
      case 'T': lengths[i] = unwrap().sizeT;
        break;
      default:
        lengths[i] = 1;
      }
    }
    return lengths;
  }

  /*
   * @see ome.scifio.ImageMetadata#copy()
   */
  public ImageMetadata copy() {
    return unwrap().convert();
  }
  
  // -- HasMetaTable methods --

  /*
   * @see ome.scifio.HasMetaTable#getTable()
   */
  public MetaTable getTable() {
    return new DefaultMetaTable(unwrap().seriesMetadata);
  }

  /*
   * @see ome.scifio.HasMetaTable#setTable(ome.scifio.MetaTable)
   */
  public void setTable(MetaTable table) {
    unwrap().seriesMetadata = new Hashtable<String, Object>(table);
  }
}
