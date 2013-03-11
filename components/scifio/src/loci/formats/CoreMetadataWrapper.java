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
import ome.scifio.ImageMetadata;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import loci.legacy.adapter.Wrapper;

/**
 * This class is used for delegation in the Legacy
 * to Modern direction. It can be used in method signatures
 * expecting an {@link ome.scifio.ImageMetadata} - or,
 * more significantly, stored within an {@link ome.scifio.DatasetMetadata}
 *  - but delegates all functionality to the wrapped @{link loci.formats.CoreMetadata}
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

  public void setThumbSizeX(final int thumbSizeX) {
    unwrap().thumbSizeX = thumbSizeX;
  }

  public void setThumbSizeY(final int thumbSizeY) {
    unwrap().thumbSizeY = thumbSizeY;
  }

  public void setPixelType(final int pixelType) {
    unwrap().pixelType = pixelType;
  }

  public void setBitsPerPixel(final int bitsPerPixel) {
    unwrap().bitsPerPixel = bitsPerPixel;
  }

  public void setcLengths(final int[] cLengths) {
    unwrap().cLengths = cLengths;
  }

  public void setcTypes(final String[] cTypes) {
    unwrap().cTypes = cTypes;
  }

  public void setOrderCertain(final boolean orderCertain) {
    unwrap().orderCertain = orderCertain;
  }

  public void setRGB(final boolean rgb) {
    unwrap().rgb = rgb;
  }

  public void setLittleEndian(final boolean littleEndian) {
    unwrap().littleEndian = littleEndian;
  }

  public void setInterleaved(final boolean interleaved) {
    unwrap().interleaved = interleaved;
  }

  public void setIndexed(final boolean indexed) {
    unwrap().indexed = indexed;
  }

  public void setFalseColor(final boolean falseColor) {
    unwrap().falseColor = falseColor;
  }

  public void setMetadataComplete(final boolean metadataComplete) {
    unwrap().metadataComplete = metadataComplete;
  }

  public void setImageMetadata(final Hashtable<String, Object> imageMetadata) {
    unwrap().seriesMetadata = imageMetadata;
  }

  public void setThumbnail(final boolean thumbnail) {
    unwrap().thumbnail = thumbnail;
  }

  public void setAxisTypes(final AxisType[] axisTypes) {
    unwrap().dimensionOrder = 
        ome.scifio.util.FormatTools.findDimensionOrder(axisTypes);
  }

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
    
  public int getPlaneCount() {
    return unwrap().imageCount;
  }

  public byte[][] getLut() {
    return null;
  }

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

  public int getPixelType() {
    return unwrap().pixelType;
  }

  public int getBitsPerPixel() {
    return unwrap().bitsPerPixel;
  }

  public int[] getcLengths() {
    return unwrap().cLengths;
  }

  public String[] getcTypes() {
    return unwrap().cTypes;
  }

  public boolean isOrderCertain() {
    return unwrap().orderCertain;
  }

  public boolean isRGB() {
    return unwrap().rgb;
  }

  public boolean isLittleEndian() {
    return unwrap().littleEndian;
  }

  public boolean isInterleaved() {
    return unwrap().interleaved;
  }

  public boolean isIndexed() {
    return unwrap().indexed;
  }

  public boolean isFalseColor() {
    return unwrap().falseColor;
  }

  public boolean isMetadataComplete() {
    return unwrap().metadataComplete;
  }

  public Hashtable<String, Object> getImageMetadata() {
    return unwrap().seriesMetadata;
  }

  public boolean isThumbnail() {
    return unwrap().thumbnail;
  }

  public void setChannelLengths(int[] cLengths) {
    unwrap().cLengths = cLengths;
  }

  public void setChannelTypes(String[] cTypes) {
    unwrap().cTypes = cTypes;
  }

  public int[] getChannelLengths() {
    return unwrap().cLengths;
  }

  public String[] getChannelTypes() {
    return unwrap().cTypes;
  }

  public void addAxis(AxisType type) {
    throw new UnsupportedOperationException("Can not add axes to legacy CoreMetadata.");
  }

  public void addAxis(AxisType type, int value) {
    throw new UnsupportedOperationException("Can not add axes to legacy CoreMetadata.");
  }

  public AxisType getAxisType(int planeIndex) {
    return getAxes()[planeIndex];
  }

  public int getAxisLength(int planeIndex) {
    return getAxesLengths()[planeIndex];
  }

  public int getAxisLength(AxisType t) {
    int index = getAxisIndex(t);
    
    if (index == -1) return -1;
    else return getAxesLengths()[index];
  }

  public int getAxisIndex(AxisType type) {
    AxisType[] axes = getAxes();
    
    for (int i=0; i<5; i++) {
      if (axes[i].equals(type)) return i;
    }
    
    return -1;
  }

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

  public ImageMetadata copy() {
    return unwrap().convert();
  }
}
