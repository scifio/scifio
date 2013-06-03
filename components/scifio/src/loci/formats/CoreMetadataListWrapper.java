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

import io.scif.AbstractMetadata;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.imglib2.meta.AxisType;


import loci.legacy.adapter.Wrapper;

/**
 * Wraps a List of legacy {@link loci.formats.CoreMetadata} objects
 * in the equivalent {@link io.scif.Metadata}. All conversion
 * occurs via the underlying {@link CoreImageMetadataAdapter}.
 * 
 * @see loci.formats.CoreMetadataWrapper
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataListWrapper extends AbstractMetadata
  implements Metadata, Wrapper<List<CoreMetadata>>
{

  // -- Fields --
  
  private WeakReference<List<CoreMetadata>> cMeta;
  
  // -- Constructor --
  
  public CoreMetadataListWrapper(List<CoreMetadata> core) {
    cMeta = new WeakReference<List<CoreMetadata>>(core);
  }
  
  // -- Wrapper API Methods --
  
  public List<CoreMetadata> unwrap() {
    return cMeta.get();
  }
  
  // -- Metadata API Methods --

  /*
   * @see io.scif.AbstractMetadata#get(int)
   */
  public ImageMetadata get(int imageIndex) {
  	// Converts CoreMetadata to ImageMetadata
    return FormatAdapter.get(unwrap().get(imageIndex));
  }

  /*
   * @see io.scif.AbstractMetadata#getImageCount()
   */
  public int getImageCount() {
    return unwrap().size();
  }

  /*
   * @see io.scif.AbstractMetadata#getPlaneCount(int)
   */
  public int getPlaneCount(int imageIndex) {
    return get(imageIndex).getPlaneCount();
  }

  /*
   * @see io.scif.AbstractMetadata#isInterleaved(int)
   */
  public boolean isInterleaved(int imageIndex) {
    return get(imageIndex).isInterleaved();
  }

  /*
   * @see io.scif.AbstractMetadata#getPixelType(int)
   */
  public int getPixelType(int imageIndex) {
    return get(imageIndex).getPixelType();
  }

  /*
   * @see io.scif.AbstractMetadata#getEffectiveSizeC(int)
   */
  public int getEffectiveSizeC(int imageIndex) {
    return get(imageIndex).getEffectiveSizeC();
  }

  /*
   * @see io.scif.AbstractMetadata#getRGBChannelCount(int)
   */
  public int getRGBChannelCount(int imageIndex) {
    return get(imageIndex).getRGBChannelCount();
  }

  /*
   * @see io.scif.AbstractMetadata#isLittleEndian(int)
   */
  public boolean isLittleEndian(int imageIndex) {
    return get(imageIndex).isLittleEndian();
  }

  /*
   * @see io.scif.AbstractMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    return get(imageIndex).isIndexed();
  }

  /*
   * @see io.scif.AbstractMetadata#getBitsPerPixel(int)
   */
  public int getBitsPerPixel(int imageIndex) {
    return get(imageIndex).getBitsPerPixel();
  }

  /*
   * @see io.scif.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    return get(imageIndex).isRGB();
  }

  /*
   * @see io.scif.AbstractMetadata#isFalseColor(int)
   */
  public boolean isFalseColor(int imageIndex) {
    return get(imageIndex).isFalseColor();
  }

  /*
   * @see io.scif.AbstractMetadata#getChannelDimLengths(int)
   */
  public int[] getChannelDimLengths(int imageIndex) {
    return get(imageIndex).getChannelLengths();
  }

  /*
   * @see io.scif.AbstractMetadata#getChannelDimTypes(int)
   */
  public String[] getChannelDimTypes(int imageIndex) {
    return get(imageIndex).getChannelTypes();
  }

  /*
   * @see io.scif.AbstractMetadata#getThumbSizeX(int)
   */
  public int getThumbSizeX(int imageIndex) {
    return get(imageIndex).getThumbSizeX();
  }

  /*
   * @see io.scif.AbstractMetadata#getThumbSizeY(int)
   */
  public int getThumbSizeY(int imageIndex) {
    return get(imageIndex).getThumbSizeY();
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisCount(int)
   */
  public int getAxisCount(int imageIndex) {
    return 5;
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisType(int, int)
   */
  public AxisType getAxisType(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisType(planeIndex);
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisLength(int, int)
   */
  public int getAxisLength(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisLength(planeIndex);
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    return get(imageIndex).getAxisLength(t);
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisIndex(int, net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(int imageIndex, AxisType type) {
    return get(imageIndex).getAxisIndex(type);
  }

  /*
   * @see io.scif.AbstractMetadata#getAxes(int)
   */
  public AxisType[] getAxes(int imageIndex) {
    return get(imageIndex).getAxes();
  }

  /*
   * @see io.scif.AbstractMetadata#getAxesLengths(int)
   */
  public int[] getAxesLengths(int imageIndex) {
    return get(imageIndex).getAxesLengths();
  }

  /*
   * @see io.scif.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType)
   */
  public void addAxis(int imageIndex, AxisType type) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  /*
   * @see io.scif.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType, int)
   */
  public void addAxis(int imageIndex, AxisType type, int value) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  /*
   * @see io.scif.AbstractMetadata#isOrderCertain(int)
   */
  public boolean isOrderCertain(int imageIndex) {
    return get(imageIndex).isOrderCertain();
  }

  /*
   * @see io.scif.AbstractMetadata#isThumbnailImage(int)
   */
  public boolean isThumbnailImage(int imageIndex) {
    return get(imageIndex).isThumbnail();
  }

  /*
   * @see io.scif.AbstractMetadata#isMetadataComplete(int)
   */
  public boolean isMetadataComplete(int imageIndex) {
    return get(imageIndex).isMetadataComplete();
  }

  /*
   * @see io.scif.AbstractMetadata#setThumbSizeX(int, int)
   */
  public void setThumbSizeX(int imageIndex, int thumbX) {
    get(imageIndex).setThumbSizeX(thumbX);
  }

  /*
   * @see io.scif.AbstractMetadata#setThumbSizeY(int, int)
   */
  public void setThumbSizeY(int imageIndex, int thumbY) {
    get(imageIndex).setThumbSizeY(thumbY);
  }

  /*
   * @see io.scif.AbstractMetadata#setPixelType(int, int)
   */
  public void setPixelType(int imageIndex, int type) {
    get(imageIndex).setPixelType(type);
  }

  /*
   * @see io.scif.AbstractMetadata#setBitsPerPixel(int, int)
   */
  public void setBitsPerPixel(int imageIndex, int bpp) {
    get(imageIndex).setBitsPerPixel(bpp);
  }

  /*
   * @see io.scif.AbstractMetadata#setChannelDimLengths(int, int[])
   */
  public void setChannelDimLengths(int imageIndex, int[] cLengths) {
    get(imageIndex).setChannelLengths(cLengths);
  }

  /*
   * @see io.scif.AbstractMetadata#setChannelDimTypes(int, java.lang.String[])
   */
  public void setChannelDimTypes(int imageIndex, String[] cTypes) {
    get(imageIndex).setChannelTypes(cTypes);
  }

  /*
   * @see io.scif.AbstractMetadata#setOrderCertain(int, boolean)
   */
  public void setOrderCertain(int imageIndex, boolean orderCertain) {
    get(imageIndex).setOrderCertain(orderCertain);
  }

  /*
   * @see io.scif.AbstractMetadata#setRGB(int, boolean)
   */
  public void setRGB(int imageIndex, boolean rgb) {
    get(imageIndex).setRGB(rgb);
  }

  /*
   * @see io.scif.AbstractMetadata#setLittleEndian(int, boolean)
   */
  public void setLittleEndian(int imageIndex, boolean littleEndian) {
    get(imageIndex).setLittleEndian(littleEndian);
  }

  /*
   * @see io.scif.AbstractMetadata#setInterleaved(int, boolean)
   */
  public void setInterleaved(int imageIndex, boolean interleaved) {
    get(imageIndex).setInterleaved(interleaved);
  }

  /*
   * @see io.scif.AbstractMetadata#setIndexed(int, boolean)
   */
  public void setIndexed(int imageIndex, boolean indexed) {
    get(imageIndex).setIndexed(indexed);
  }

  /*
   * @see io.scif.AbstractMetadata#setFalseColor(int, boolean)
   */
  public void setFalseColor(int imageIndex, boolean falseC) {
    get(imageIndex).setFalseColor(falseC);
  }

  /*
   * @see io.scif.AbstractMetadata#setMetadataComplete(int, boolean)
   */
  public void setMetadataComplete(int imageIndex, boolean metadataComplete) {
    get(imageIndex).setMetadataComplete(metadataComplete);
  }

  /*
   * @see io.scif.AbstractMetadata#add(io.scif.ImageMetadata)
   */
  public void add(ImageMetadata meta) {
    unwrap().add(new CoreMetadata(meta));
  }

  /*
   * @see io.scif.AbstractMetadata#setThumbnailImage(int, boolean)
   */
  public void setThumbnailImage(int imageIndex, boolean thumbnail) {
    get(imageIndex).setThumbnail(thumbnail);
  }

  /*
   * @see io.scif.AbstractMetadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(int imageIndex, AxisType[] axisTypes) {
    get(imageIndex).setAxisTypes(axisTypes);
  }

  /*
   * @see io.scif.AbstractMetadata#setAxisType(int, int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(int imageIndex, int axisIndex, AxisType axis) {
    get(imageIndex).setAxisType(axisIndex, axis);
  }

  /*
   * @see io.scif.AbstractMetadata#setAxisLengths(int, int[])
   */
  public void setAxisLengths(int imageIndex, int[] axisLengths) {
    get(imageIndex).setAxisLengths(axisLengths);
  }

  /*
   * @see io.scif.AbstractMetadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(int imageIndex, AxisType axis, int length) {
    get(imageIndex).setAxisLength(axis, length);
  }

  /*
   * @see io.scif.Metadata#populateImageMetadata()
   */
  public void populateImageMetadata() { }

  // -- MetaTable API Methods --
  
  /*
   * @see io.scif.AbstractMetadata#getTable()
   */
  public MetaTable getTable() {
    return null;
  }

  /*
   * @see io.scif.AbstractMetadata#setTable(io.scif.MetaTable)
   */
  public void setTable(MetaTable table) { }
}
