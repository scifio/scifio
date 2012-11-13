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

import java.util.Hashtable;

import net.imglib2.meta.AxisType;
import loci.legacy.adapter.Wrapper;

/**
 * This class is used for delegation in the Legacy
 * to Modern direction. It can be used in method singatures
 * expecting an {@link ome.scifio.ImageMetadata} - or,
 * more significantly, stored within an {@link ome.scifio.DatasetMetadata}
 *  - but delegates all functionality to the wrapped @{link loci.formats.CoreMetadata}
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataWrapper extends ome.scifio.DefaultImageMetadata
  implements Wrapper<CoreMetadata> 
{

  // -- Fields --
  
  private CoreMetadata coreMeta;
  
  // -- Constructor --
  
  public CoreMetadataWrapper(CoreMetadata cMeta) {
    coreMeta = cMeta;
  }
  
  // -- Wrapper API Methods --

  public CoreMetadata unwrap() {
    return coreMeta;
  }
  
  // -- CoreImageMetadata methods --
  
  // -- Setters -- 

  public void setLut(final byte[][] lut) {
    throw new UnsupportedOperationException();
  }

  public void setThumbSizeX(final int thumbSizeX) {
    coreMeta.thumbSizeX = thumbSizeX;
  }

  public void setThumbSizeY(final int thumbSizeY) {
    coreMeta.thumbSizeY = thumbSizeY;
  }

  public void setPixelType(final int pixelType) {
    coreMeta.pixelType = pixelType;
  }

  public void setBitsPerPixel(final int bitsPerPixel) {
    coreMeta.bitsPerPixel = bitsPerPixel;
  }

  public void setcLengths(final int[] cLengths) {
    coreMeta.cLengths = cLengths;
  }

  public void setcTypes(final String[] cTypes) {
    coreMeta.cTypes = cTypes;
  }

  public void setOrderCertain(final boolean orderCertain) {
    coreMeta.orderCertain = orderCertain;
  }

  public void setRgb(final boolean rgb) {
    coreMeta.rgb = rgb;
  }

  public void setLittleEndian(final boolean littleEndian) {
    coreMeta.littleEndian = littleEndian;
  }

  public void setInterleaved(final boolean interleaved) {
    coreMeta.interleaved = interleaved;
  }

  public void setIndexed(final boolean indexed) {
    coreMeta.indexed = indexed;
  }

  public void setFalseColor(final boolean falseColor) {
    coreMeta.falseColor = falseColor;
  }

  public void setMetadataComplete(final boolean metadataComplete) {
    coreMeta.metadataComplete = metadataComplete;
  }

  public void setImageMetadata(final Hashtable<String, Object> imageMetadata) {
    coreMeta.seriesMetadata = imageMetadata;
  }

  public void setThumbnail(final boolean thumbnail) {
    coreMeta.thumbnail = thumbnail;
  }

  public void setAxisTypes(final AxisType[] axisTypes) {
    coreMeta.dimensionOrder = 
        ome.scifio.util.FormatTools.findDimensionOrder(axisTypes);
  }

  public void setAxisLengths(final int[] axisLengths) {
    
    for(int i = 0; i < 5; i++) {
      switch(coreMeta.dimensionOrder.charAt(i)) {
      case 'X': coreMeta.sizeX = axisLengths[i];
        break;
      case 'Y': coreMeta.sizeY = axisLengths[i];
        break;
      case 'Z': coreMeta.sizeZ = axisLengths[i];
        break;
      case 'C': coreMeta.sizeC = axisLengths[i];
        break;
      case 'T': coreMeta.sizeT = axisLengths[i];
        break;
      default:
      }
    }
  }

  public void setAxisLength(final AxisType axis, final int length) {
    switch(axis.getLabel().toUpperCase().charAt(0)) {
    case 'X': coreMeta.sizeX = length;
      break;
    case 'Y': coreMeta.sizeY = length;
      break;
    case 'Z': coreMeta.sizeZ = length;
      break;
    case 'C': coreMeta.sizeC = length;
      break;
    case 'T': coreMeta.sizeT = length;
      break;
    default:
    }
  }

  public void setAxisType(final int index, final AxisType axis) {
    String order = "";
    
    for(int i = 0; i < coreMeta.dimensionOrder.length(); i++) {
      if(i == index) {
        order += axis.getLabel().toUpperCase().charAt(0);
      }
      else
        order += coreMeta.dimensionOrder.charAt(i);
    }
      
    coreMeta.dimensionOrder = order;
  }

  public void setPlaneCount(final int planeCount) {
    coreMeta.imageCount = planeCount;
  }
  
  // -- Getters --
    
  public int getPlaneCount() {
    return coreMeta.imageCount;
  }

  public byte[][] getLut() {
    return null;
  }

  public int getThumbSizeX() {
    return coreMeta.thumbSizeX;
  }

  public int getThumbSizeY() {
    return coreMeta.thumbSizeY;
  }

  public int getPixelType() {
    return coreMeta.pixelType;
  }

  public int getBitsPerPixel() {
    return coreMeta.bitsPerPixel;
  }

  public int[] getcLengths() {
    return coreMeta.cLengths;
  }

  public String[] getcTypes() {
    return coreMeta.cTypes;
  }

  public AxisType[] getAxisTypes() {
    return ome.scifio.util.FormatTools.findDimensionList(coreMeta.dimensionOrder);
  }

  public int[] getAxisLengths() {
    int[] lengths = new int[5];
    
    lengths[0] = coreMeta.sizeX;
    lengths[1] = coreMeta.sizeY;
    
    for(int i = 2; i < 5; i++) {
      switch(coreMeta.dimensionOrder.charAt(i)) {
      case 'Z': lengths[i] = coreMeta.sizeZ;
        break;
      case 'C': lengths[i] = coreMeta.sizeC;
        break;
      case 'T': lengths[i] = coreMeta.sizeT;
        break;
      default:
        lengths[i] = 1;
      }
    }
    
    return lengths;
  }

  public boolean isOrderCertain() {
    return coreMeta.orderCertain;
  }

  public boolean isRgb() {
    return coreMeta.rgb;
  }

  public boolean isLittleEndian() {
    return coreMeta.littleEndian;
  }

  public boolean isInterleaved() {
    return coreMeta.interleaved;
  }

  public boolean isIndexed() {
    return coreMeta.indexed;
  }

  public boolean isFalseColor() {
    return coreMeta.falseColor;
  }

  public boolean isMetadataComplete() {
    return coreMeta.metadataComplete;
  }

  public Hashtable<String, Object> getImageMetadata() {
    return coreMeta.seriesMetadata;
  }

  public boolean isThumbnail() {
    return coreMeta.thumbnail;
  }
}
