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
import java.util.List;
import java.util.Map;

import net.imglib2.meta.AxisType;

import ome.scifio.AbstractMetadata;
import ome.scifio.ImageMetadata;
import ome.scifio.MetaTable;
import ome.scifio.Metadata;

import loci.legacy.adapter.Wrapper;

/**
 * Wraps a List of legacy {@link loci.formats.CoreMetadata} objects
 * in the equivalent {@link ome.scifio.Metadata}. All conversion
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
  
  // -- DatasetMetadata API Methods --

  /*
   * @see ome.scifio.AbstractMetadata#get(int)
   */
  public ImageMetadata get(int imageIndex) {
  	// Converts CoreMetadata to ImageMetadata
    return FormatAdapter.get(unwrap().get(imageIndex));
  }

  /*
   * @see ome.scifio.AbstractMetadata#getImageCount()
   */
  public int getImageCount() {
    return unwrap().size();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getPlaneCount(int)
   */
  public int getPlaneCount(int imageIndex) {
    return get(imageIndex).getPlaneCount();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isInterleaved(int)
   */
  public boolean isInterleaved(int imageIndex) {
    return get(imageIndex).isInterleaved();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getPixelType(int)
   */
  public int getPixelType(int imageIndex) {
    return get(imageIndex).getPixelType();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getEffectiveSizeC(int)
   */
  public int getEffectiveSizeC(int imageIndex) {
    return get(imageIndex).getEffectiveSizeC();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getRGBChannelCount(int)
   */
  public int getRGBChannelCount(int imageIndex) {
    return get(imageIndex).getRGBChannelCount();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isLittleEndian(int)
   */
  public boolean isLittleEndian(int imageIndex) {
    return get(imageIndex).isLittleEndian();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    return get(imageIndex).isIndexed();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getBitsPerPixel(int)
   */
  public int getBitsPerPixel(int imageIndex) {
    return get(imageIndex).getBitsPerPixel();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    return get(imageIndex).isRGB();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isFalseColor(int)
   */
  public boolean isFalseColor(int imageIndex) {
    return get(imageIndex).isFalseColor();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getChannelDimLengths(int)
   */
  public int[] getChannelDimLengths(int imageIndex) {
    return get(imageIndex).getChannelLengths();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getChannelDimTypes(int)
   */
  public String[] getChannelDimTypes(int imageIndex) {
    return get(imageIndex).getChannelTypes();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getThumbSizeX(int)
   */
  public int getThumbSizeX(int imageIndex) {
    return get(imageIndex).getThumbSizeX();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getThumbSizeY(int)
   */
  public int getThumbSizeY(int imageIndex) {
    return get(imageIndex).getThumbSizeY();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxisCount(int)
   */
  public int getAxisCount(int imageIndex) {
    return 5;
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxisType(int, int)
   */
  public AxisType getAxisType(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisType(planeIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxisLength(int, int)
   */
  public int getAxisLength(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisLength(planeIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    return get(imageIndex).getAxisLength(t);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxisIndex(int, net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(int imageIndex, AxisType type) {
    return get(imageIndex).getAxisIndex(type);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxes(int)
   */
  public AxisType[] getAxes(int imageIndex) {
    return get(imageIndex).getAxes();
  }

  /*
   * @see ome.scifio.AbstractMetadata#getAxesLengths(int)
   */
  public int[] getAxesLengths(int imageIndex) {
    return get(imageIndex).getAxesLengths();
  }

  /*
   * @see ome.scifio.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType)
   */
  public void addAxis(int imageIndex, AxisType type) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  /*
   * @see ome.scifio.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType, int)
   */
  public void addAxis(int imageIndex, AxisType type, int value) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isOrderCertain(int)
   */
  public boolean isOrderCertain(int imageIndex) {
    return get(imageIndex).isOrderCertain();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isThumbnailImage(int)
   */
  public boolean isThumbnailImage(int imageIndex) {
    return get(imageIndex).isThumbnail();
  }

  /*
   * @see ome.scifio.AbstractMetadata#isMetadataComplete(int)
   */
  public boolean isMetadataComplete(int imageIndex) {
    return get(imageIndex).isMetadataComplete();
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbSizeX(int, int)
   */
  public void setThumbSizeX(int imageIndex, int thumbX) {
    get(imageIndex).setThumbSizeX(thumbX);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbSizeY(int, int)
   */
  public void setThumbSizeY(int imageIndex, int thumbY) {
    get(imageIndex).setThumbSizeY(thumbY);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setPixelType(int, int)
   */
  public void setPixelType(int imageIndex, int type) {
    get(imageIndex).setPixelType(type);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setBitsPerPixel(int, int)
   */
  public void setBitsPerPixel(int imageIndex, int bpp) {
    get(imageIndex).setBitsPerPixel(bpp);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setChannelDimLengths(int, int[])
   */
  public void setChannelDimLengths(int imageIndex, int[] cLengths) {
    get(imageIndex).setChannelLengths(cLengths);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setChannelDimTypes(int, java.lang.String[])
   */
  public void setChannelDimTypes(int imageIndex, String[] cTypes) {
    get(imageIndex).setChannelTypes(cTypes);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setOrderCertain(int, boolean)
   */
  public void setOrderCertain(int imageIndex, boolean orderCertain) {
    get(imageIndex).setOrderCertain(orderCertain);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setRGB(int, boolean)
   */
  public void setRGB(int imageIndex, boolean rgb) {
    get(imageIndex).setRGB(rgb);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setLittleEndian(int, boolean)
   */
  public void setLittleEndian(int imageIndex, boolean littleEndian) {
    get(imageIndex).setLittleEndian(littleEndian);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setInterleaved(int, boolean)
   */
  public void setInterleaved(int imageIndex, boolean interleaved) {
    get(imageIndex).setInterleaved(interleaved);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setIndexed(int, boolean)
   */
  public void setIndexed(int imageIndex, boolean indexed) {
    get(imageIndex).setIndexed(indexed);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setFalseColor(int, boolean)
   */
  public void setFalseColor(int imageIndex, boolean falseC) {
    get(imageIndex).setFalseColor(falseC);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setMetadataComplete(int, boolean)
   */
  public void setMetadataComplete(int imageIndex, boolean metadataComplete) {
    get(imageIndex).setMetadataComplete(metadataComplete);
  }

  /*
   * @see ome.scifio.AbstractMetadata#add(ome.scifio.ImageMetadata)
   */
  public void add(ImageMetadata meta) {
    unwrap().add(new CoreMetadata(meta));
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbnailImage(int, boolean)
   */
  public void setThumbnailImage(int imageIndex, boolean thumbnail) {
    get(imageIndex).setThumbnail(thumbnail);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(int imageIndex, AxisType[] axisTypes) {
    get(imageIndex).setAxisTypes(axisTypes);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisType(int, int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(int imageIndex, int axisIndex, AxisType axis) {
    get(imageIndex).setAxisType(axisIndex, axis);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisLengths(int, int[])
   */
  public void setAxisLengths(int imageIndex, int[] axisLengths) {
    get(imageIndex).setAxisLengths(axisLengths);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(int imageIndex, AxisType axis, int length) {
    get(imageIndex).setAxisLength(axis, length);
  }

  /*
   * @see ome.scifio.Metadata#populateImageMetadata()
   */
  public void populateImageMetadata() { }

  public MetaTable getTable() {
    return null;
  }

  public void setTable(MetaTable table) { }
}
