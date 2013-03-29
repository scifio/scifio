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
package ome.scifio.filters;

import java.util.Hashtable;

import net.imglib2.meta.AxisType;
import ome.scifio.AbstractMetadata;
import ome.scifio.ImageMetadata;
import ome.scifio.Metadata;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Abstract superclass for concrete implementations of {@code MetadataWrapper}.
 * <p>
 * To create a {@code MetadataWrapper} that is paired with a specific {@code Filter},
 * simply extend this class, override any methods as desired, and annotate the class using
 * {@code DiscoverableMetadataWrapper} as appropriate. The wrapper will automatically be
 * discovered and applied when a new instance of its {@code Filter} is instantiated,
 * assuming the {@code Filter} is a subclass of {@code AbstractReaderFilter}
 * </p>
 * 
 * @author Mark Hiner
 * 
 * @see ome.scifio.filters.MetadataWrapper
 * @see ome.scifio.discovery.DiscoverableMetadataWrapper
 * @see ome.scifio.filters.AbstractReaderFilter
 */
public abstract class AbstractMetadataWrapper extends AbstractMetadata
  implements MetadataWrapper
{

  // -- Fields --
  
  private Metadata meta;
  
  // -- Constructor --
  
  public AbstractMetadataWrapper() {
    this(null);
  }
  
  public AbstractMetadataWrapper(Metadata metadata) {
    meta = metadata;
  }
  
  // -- MetadataWrapper API Methods --
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#unwrap()
   */
  public Metadata unwrap() {
    return meta;
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#wrap(ome.scifio.Metadata)
   */
  public void wrap(Metadata meta) {
    this.meta = meta;
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#addAxis(int, net.imglib2.meta.AxisType, boolean)
   */
  public void addAxis(final int imageIndex, final AxisType type, boolean passUp) {
    super.addAxis(imageIndex, type);
    if (passUp) meta.addAxis(imageIndex, type);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#addAxis(int, net.imglib2.meta.AxisType, int, boolean)
   */
  public void addAxis(final int imageIndex, final AxisType type, final int value, boolean passUp)
  {
    super.addAxis(imageIndex, type, value);
    if (passUp) meta.addAxis(imageIndex, type, value);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#putDatasetMeta(java.lang.String, java.lang.Object, boolean)
   */
  public void putDatasetMeta(String key, Object value, boolean passUp) {
    super.putDatasetMeta(key, value);
    if (passUp) meta.putDatasetMeta(key, value);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#putImageMeta(int, java.lang.String, java.lang.Object, boolean)
   */
  public void putImageMeta(final int imageIndex, String key, Object value, boolean passUp) {
    super.putImageMeta(imageIndex, key, value);
    if (passUp) meta.putImageMeta(imageIndex, key, value);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setThumbSizeX(int, int, boolean)
   */
  public void setThumbSizeX(final int imageIndex, final int thumbX, boolean passUp) {
    super.setThumbSizeX(imageIndex, thumbX);
    if (passUp) meta.setThumbSizeX(imageIndex, thumbX);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setThumbSizeY(int, int, boolean)
   */
  public void setThumbSizeY(final int imageIndex, final int thumbY, boolean passUp) {
    super.setThumbSizeY(imageIndex, thumbY);
    if (passUp) meta.setThumbSizeY(imageIndex, thumbY);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setPixelType(int, int, boolean)
   */
  public void setPixelType(final int imageIndex, final int type, boolean passUp) {
    super.setPixelType(imageIndex, type);
    if (passUp) meta.setPixelType(imageIndex, type);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setBitsPerPixel(int, int, boolean)
   */
  public void setBitsPerPixel(final int imageIndex, final int bpp, boolean passUp) {
    super.setBitsPerPixel(imageIndex, bpp);
    if (passUp) meta.setBitsPerPixel(imageIndex, bpp);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setChannelDimLengths(int, int[], boolean)
   */
  public void setChannelDimLengths(final int imageIndex, final int[] cLengths, boolean passUp) {
    super.setChannelDimLengths(imageIndex, cLengths);
    if (passUp) meta.setChannelDimLengths(imageIndex, cLengths);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setChannelDimTypes(int, java.lang.String[], boolean)
   */
  public void setChannelDimTypes(final int imageIndex, final String[] cTypes, boolean passUp) {
    super.setChannelDimTypes(imageIndex, cTypes);
    if (passUp) meta.setChannelDimTypes(imageIndex, cTypes);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setOrderCertain(int, boolean, boolean)
   */
  public void setOrderCertain(final int imageIndex, final boolean orderCertain, boolean passUp)
  {
    super.setOrderCertain(imageIndex, orderCertain);
    if (passUp) meta.setOrderCertain(imageIndex, orderCertain);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setRGB(int, boolean, boolean)
   */
  public void setRGB(final int imageIndex, final boolean rgb, boolean passUp) {
    super.setRGB(imageIndex, rgb);
    if (passUp) meta.setRGB(imageIndex, rgb);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setLittleEndian(int, boolean, boolean)
   */
  public void setLittleEndian(final int imageIndex, final boolean littleEndian, boolean passUp)
  {
    super.setLittleEndian(imageIndex, littleEndian);
    if (passUp) meta.setLittleEndian(imageIndex, littleEndian);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setInterleaved(int, boolean, boolean)
   */
  public void setInterleaved(final int imageIndex, final boolean interleaved, boolean passUp) {
    super.setInterleaved(imageIndex, interleaved);
    if (passUp) meta.setInterleaved(imageIndex, interleaved);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setIndexed(int, boolean, boolean)
   */
  public void setIndexed(final int imageIndex, final boolean indexed, boolean passUp) {
    super.setIndexed(imageIndex, indexed);
    if (passUp) meta.setIndexed(imageIndex, indexed);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setFalseColor(int, boolean, boolean)
   */
  public void setFalseColor(final int imageIndex, final boolean falseC, boolean passUp) {
    super.setFalseColor(imageIndex, falseC);
    if (passUp) meta.setFalseColor(imageIndex, falseC);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setMetadataComplete(int, boolean, boolean)
   */
  public void setMetadataComplete(final int imageIndex, final boolean metadataComplete, boolean passUp)
  {
    super.setMetadataComplete(imageIndex, metadataComplete);
    if (passUp) meta.setMetadataComplete(imageIndex, metadataComplete);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#add(ome.scifio.ImageMetadata, boolean)
   */
  public void add(final ImageMetadata meta, boolean passUp) {
    super.add(meta);
    if (passUp) this.meta.add(meta);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setImageMetadata(int, java.util.Hashtable, boolean)
   */
  public void setImageMetadata(final int imageIndex, final Hashtable<String, Object> meta, boolean passUp)
  {
    super.setImageMetadata(imageIndex, meta);
    if (passUp) this.meta.setImageMetadata(imageIndex, meta);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setThumbnailImage(int, boolean, boolean)
   */
  public void setThumbnailImage(final int imageIndex, final boolean thumbnail, boolean passUp) {
    super.setThumbnailImage(imageIndex, thumbnail);
    if (passUp) meta.setThumbnailImage(imageIndex, thumbnail);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setAxisTypes(int, net.imglib2.meta.AxisType[], boolean)
   */
  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes, boolean passUp) {
    super.setAxisTypes(imageIndex, axisTypes);
    if (passUp) meta.setAxisTypes(imageIndex, axisTypes);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#setAxisType(int, int, net.imglib2.meta.AxisType, boolean)
   */
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis, boolean passUp) {
    super.setAxisType(imageIndex, axisIndex, axis);
    if (passUp) meta.setAxisType(imageIndex, axisIndex, axis);
  }

  /*
   * @see ome.scifio.filters.MetadataWrapper#setAxisLengths(int, int[], boolean)
   */
  public void setAxisLengths(final int imageIndex, final int[] axisLengths, boolean passUp) {
    super.setAxisLengths(imageIndex, axisLengths);
    if (passUp) meta.setAxisLengths(imageIndex, axisLengths);
  }
  
  /*
   * @see ome.scifio.filters.MetadataWrapper#setAxisLength(int, net.imglib2.meta.AxisType, int, boolean)
   */
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length, boolean passUp) {
    super.setAxisLength(imageIndex, axis, length);
    if (passUp) meta.setAxisLength(imageIndex, axis, length);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType)
   */
  public void addAxis(final int imageIndex, final AxisType type) {
    addAxis(imageIndex, type, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#addAxis(int, net.imglib2.meta.AxisType, int)
   */
  public void addAxis(final int imageIndex, final AxisType type, final int value)
  {
    addAxis(imageIndex, type, value, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#putDatasetMeta(java.lang.String, java.lang.Object)
   */
  public void putDatasetMeta(String key, Object value) {
    putDatasetMeta(key, value, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#putImageMeta(int, java.lang.String, java.lang.Object)
   */
  public void putImageMeta(final int imageIndex, String key, Object value) {
    putImageMeta(imageIndex, key, value, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbSizeX(int, int)
   */
  public void setThumbSizeX(final int imageIndex, final int thumbX) {
    setThumbSizeX(imageIndex, thumbX, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbSizeY(int, int)
   */
  public void setThumbSizeY(final int imageIndex, final int thumbY) {
    setThumbSizeY(imageIndex, thumbY, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setPixelType(int, int)
   */
  public void setPixelType(final int imageIndex, final int type) {
    setPixelType(imageIndex, type, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setBitsPerPixel(int, int)
   */
  public void setBitsPerPixel(final int imageIndex, final int bpp) {
    setBitsPerPixel(imageIndex, bpp, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setChannelDimLengths(int, int[])
   */
  public void setChannelDimLengths(final int imageIndex, final int[] cLengths) {
    setChannelDimLengths(imageIndex, cLengths, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setChannelDimTypes(int, java.lang.String[])
   */
  public void setChannelDimTypes(final int imageIndex, final String[] cTypes) {
    setChannelDimTypes(imageIndex, cTypes, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setOrderCertain(int, boolean)
   */
  public void setOrderCertain(final int imageIndex, final boolean orderCertain)
  {
    setOrderCertain(imageIndex, orderCertain, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setRGB(int, boolean)
   */
  public void setRGB(final int imageIndex, final boolean rgb) {
    setRGB(imageIndex, rgb, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setLittleEndian(int, boolean)
   */
  public void setLittleEndian(final int imageIndex, final boolean littleEndian)
  {
    setLittleEndian(imageIndex, littleEndian, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setInterleaved(int, boolean)
   */
  public void setInterleaved(final int imageIndex, final boolean interleaved) {
    setInterleaved(imageIndex, interleaved, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setIndexed(int, boolean)
   */
  public void setIndexed(final int imageIndex, final boolean indexed) {
    setIndexed(imageIndex, indexed, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setFalseColor(int, boolean)
   */
  public void setFalseColor(final int imageIndex, final boolean falseC) {
    setFalseColor(imageIndex, falseC, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setMetadataComplete(int, boolean)
   */
  public void setMetadataComplete(final int imageIndex, final boolean metadataComplete)
  {
    setMetadataComplete(imageIndex, metadataComplete, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#add(ome.scifio.ImageMetadata)
   */
  public void add(final ImageMetadata meta) {
    add(meta, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setImageMetadata(int, java.util.Hashtable)
   */
  public void setImageMetadata(final int imageIndex, final Hashtable<String, Object> meta)
  {
    setImageMetadata(imageIndex, meta, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setThumbnailImage(int, boolean)
   */
  public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
    setThumbnailImage(imageIndex, thumbnail, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
    setAxisTypes(imageIndex, axisTypes, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#setAxisType(int, int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis) {
    setAxisType(imageIndex, axisIndex, axis, true);
  }

  /*
   * @see ome.scifio.AbstractMetadata#setAxisLengths(int, int[])
   */
  public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
    setAxisLengths(imageIndex, axisLengths, true);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length) {
    setAxisLength(imageIndex, axis, length, true);
  }
  
  // -- Metadata API Methods --
  
  /*
   * @see ome.scifio.AbstractMetadata#setSource(ome.scifio.io.RandomAccessInputStream)
   */
  public void setSource(RandomAccessInputStream source) {
    super.setSource(source);
    meta.setSource(source);
  }
  
  public void populateImageMetadata() { 
    meta.populateImageMetadata();
  }
  
  // -- Helper Reset Method --
  
  public void reset() {
    super.reset(getClass());
    meta.reset(meta.getClass());
  }

}
