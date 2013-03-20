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
package ome.scifio.filters;

import java.util.Hashtable;

import net.imglib2.meta.AxisType;
import ome.scifio.AbstractDatasetMetadata;
import ome.scifio.DatasetMetadata;
import ome.scifio.ImageMetadata;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Abstract superclass for concrete implementations of {@code DatasetMetadataWrapper}.
 * <p>
 * Provides delegation methods for all {@code Reader} methods.
 * </p>
 * <p>
 * To create a {@code DatasetMetadataWrapper} that is paired with a specific {@code Filter},
 * simply extend this class, override any methods as desired, and annotate the class using
 * {@code DiscoverableMetadataWrapper} as appropriate. The wrapper will automatically be
 * discovered and applied when a new instance of its {@code Filter} is instantiated,
 * assuming the {@code Filter} is a subclass of {@code AbstractReaderFilter}
 * </p>
 * 
 * @author Mark Hiner
 * 
 * @see ome.scifio.filters.DatasetMetadataWrapper
 * @see ome.scifio.discovery.DiscoverableMetadataWrapper
 * @see ome.scifio.filters.AbstractReaderFilter
 */
public abstract class AbstractDatasetMetadataWrapper extends AbstractDatasetMetadata
  implements DatasetMetadataWrapper {

  // -- Fields --
  
  private DatasetMetadata meta;
  
  // -- Constructor --
  
  public AbstractDatasetMetadataWrapper() {
    this(null);
  }
  
  public AbstractDatasetMetadataWrapper(DatasetMetadata metadata) {
    super(metadata == null ? null : metadata, metadata == null ? null : metadata.getContext());
    meta = metadata;
  }
  
  // -- DatasetMetadataWrapper API Methods --
  
  /*
   * @see ome.scifio.wrappers.DatasetMetadataWrapper#unwrap()
   */
  public DatasetMetadata unwrap() {
    return meta;
  }
  
  /*
   * @see ome.scifio.filters.DatasetMetadataWrapper#wrap(ome.scifio.DatasetMetadata)
   */
  public void wrap(DatasetMetadata meta) {
    this.meta = meta;
  }
  
  // DatasetMetadataWrapper API Methods -- 
  
  public void addAxis(final int imageIndex, final AxisType type, boolean passUp) {
    super.addAxis(imageIndex, type);
    if (passUp) meta.addAxis(imageIndex, type);
  }
  
  public void addAxis(final int imageIndex, final AxisType type, final int value, boolean passUp)
  {
    super.addAxis(imageIndex, type, value);
    if (passUp) meta.addAxis(imageIndex, type, value);
  }
  
  public void putDatasetMeta(String key, Object value, boolean passUp) {
    super.putDatasetMeta(key, value);
    if (passUp) meta.putDatasetMeta(key, value);
  }
  
  public void putImageMeta(final int imageIndex, String key, Object value, boolean passUp) {
    super.putImageMeta(imageIndex, key, value);
    if (passUp) meta.putImageMeta(imageIndex, key, value);
  }

  public void setThumbSizeX(final int imageIndex, final int thumbX, boolean passUp) {
    super.setThumbSizeX(imageIndex, thumbX);
    if (passUp) meta.setThumbSizeX(imageIndex, thumbX);
  }

  public void setThumbSizeY(final int imageIndex, final int thumbY, boolean passUp) {
    super.setThumbSizeY(imageIndex, thumbY);
    if (passUp) meta.setThumbSizeY(imageIndex, thumbY);
  }

  public void setPixelType(final int imageIndex, final int type, boolean passUp) {
    super.setPixelType(imageIndex, type);
    if (passUp) meta.setPixelType(imageIndex, type);
  }

  public void setBitsPerPixel(final int imageIndex, final int bpp, boolean passUp) {
    super.setBitsPerPixel(imageIndex, bpp);
    if (passUp) meta.setBitsPerPixel(imageIndex, bpp);
  }

  public void setChannelDimLengths(final int imageIndex, final int[] cLengths, boolean passUp) {
    super.setChannelDimLengths(imageIndex, cLengths);
    if (passUp) meta.setChannelDimLengths(imageIndex, cLengths);
  }

  public void setChannelDimTypes(final int imageIndex, final String[] cTypes, boolean passUp) {
    super.setChannelDimTypes(imageIndex, cTypes);
    if (passUp) meta.setChannelDimTypes(imageIndex, cTypes);
  }

  public void setOrderCertain(final int imageIndex, final boolean orderCertain, boolean passUp)
  {
    super.setOrderCertain(imageIndex, orderCertain);
    if (passUp) meta.setOrderCertain(imageIndex, orderCertain);
  }

  public void setRGB(final int imageIndex, final boolean rgb, boolean passUp) {
    super.setRGB(imageIndex, rgb);
    if (passUp) meta.setRGB(imageIndex, rgb);
  }

  public void setLittleEndian(final int imageIndex, final boolean littleEndian, boolean passUp)
  {
    super.setLittleEndian(imageIndex, littleEndian);
    if (passUp) meta.setLittleEndian(imageIndex, littleEndian);
  }

  public void setInterleaved(final int imageIndex, final boolean interleaved, boolean passUp) {
    super.setInterleaved(imageIndex, interleaved);
    if (passUp) meta.setInterleaved(imageIndex, interleaved);
  }

  public void setIndexed(final int imageIndex, final boolean indexed, boolean passUp) {
    super.setIndexed(imageIndex, indexed);
    if (passUp) meta.setIndexed(imageIndex, indexed);
  }

  public void setFalseColor(final int imageIndex, final boolean falseC, boolean passUp) {
    super.setFalseColor(imageIndex, falseC);
    if (passUp) meta.setFalseColor(imageIndex, falseC);
  }

  public void setMetadataComplete(final int imageIndex, final boolean metadataComplete, boolean passUp)
  {
    super.setMetadataComplete(imageIndex, metadataComplete);
    if (passUp) meta.setMetadataComplete(imageIndex, metadataComplete);
  }
  
  public void add(final ImageMetadata meta, boolean passUp) {
    super.add(meta);
    if (passUp) this.meta.add(meta);
  }

  public void setImageMetadata(final int imageIndex, final Hashtable<String, Object> meta, boolean passUp)
  {
    super.setImageMetadata(imageIndex, meta);
    if (passUp) this.meta.setImageMetadata(imageIndex, meta);
  }

  public void setThumbnailImage(final int imageIndex, final boolean thumbnail, boolean passUp) {
    super.setThumbnailImage(imageIndex, thumbnail);
    if (passUp) meta.setThumbnailImage(imageIndex, thumbnail);
  }

  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes, boolean passUp) {
    super.setAxisTypes(imageIndex, axisTypes);
    if (passUp) meta.setAxisTypes(imageIndex, axisTypes);
  }
  
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis, boolean passUp) {
    super.setAxisType(imageIndex, axisIndex, axis);
    if (passUp) meta.setAxisType(imageIndex, axisIndex, axis);
  }

  public void setAxisLengths(final int imageIndex, final int[] axisLengths, boolean passUp) {
    super.setAxisLengths(imageIndex, axisLengths);
    if (passUp) meta.setAxisLengths(imageIndex, axisLengths);
  }
  
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length, boolean passUp) {
    super.setAxisLength(imageIndex, axis, length);
    if (passUp) meta.setAxisLength(imageIndex, axis, length);
  }
  
  // -- DatasetMetadata API Methods --
  
  public void addAxis(final int imageIndex, final AxisType type) {
    addAxis(imageIndex, type, true);
  }
  
  public void addAxis(final int imageIndex, final AxisType type, final int value)
  {
    addAxis(imageIndex, type, value, true);
  }
  
  public void putDatasetMeta(String key, Object value) {
    putDatasetMeta(key, value, true);
  }
  
  public void putImageMeta(final int imageIndex, String key, Object value) {
    putImageMeta(imageIndex, key, value, true);
  }

  public void setThumbSizeX(final int imageIndex, final int thumbX) {
    setThumbSizeX(imageIndex, thumbX, true);
  }

  public void setThumbSizeY(final int imageIndex, final int thumbY) {
    setThumbSizeY(imageIndex, thumbY, true);
  }

  public void setPixelType(final int imageIndex, final int type) {
    setPixelType(imageIndex, type, true);
  }

  public void setBitsPerPixel(final int imageIndex, final int bpp) {
    setBitsPerPixel(imageIndex, bpp, true);
  }

  public void setChannelDimLengths(final int imageIndex, final int[] cLengths) {
    setChannelDimLengths(imageIndex, cLengths, true);
  }

  public void setChannelDimTypes(final int imageIndex, final String[] cTypes) {
    setChannelDimTypes(imageIndex, cTypes, true);
  }

  public void setOrderCertain(final int imageIndex, final boolean orderCertain)
  {
    setOrderCertain(imageIndex, orderCertain, true);
  }

  public void setRGB(final int imageIndex, final boolean rgb) {
    setRGB(imageIndex, rgb, true);
  }

  public void setLittleEndian(final int imageIndex, final boolean littleEndian)
  {
    setLittleEndian(imageIndex, littleEndian, true);
  }

  public void setInterleaved(final int imageIndex, final boolean interleaved) {
    setInterleaved(imageIndex, interleaved, true);
  }

  public void setIndexed(final int imageIndex, final boolean indexed) {
    setIndexed(imageIndex, indexed, true);
  }

  public void setFalseColor(final int imageIndex, final boolean falseC) {
    setFalseColor(imageIndex, falseC, true);
  }

  public void setMetadataComplete(final int imageIndex, final boolean metadataComplete)
  {
    setMetadataComplete(imageIndex, metadataComplete, true);
  }
  
  public void add(final ImageMetadata meta) {
    add(meta, true);
  }

  public void setImageMetadata(final int imageIndex, final Hashtable<String, Object> meta)
  {
    setImageMetadata(imageIndex, meta, true);
  }

  public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
    setThumbnailImage(imageIndex, thumbnail, true);
  }

  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
    setAxisTypes(imageIndex, axisTypes, true);
  }
  
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis) {
    setAxisType(imageIndex, axisIndex, axis, true);
  }

  public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
    setAxisLengths(imageIndex, axisLengths, true);
  }
  
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length) {
    setAxisLength(imageIndex, axis, length, true);
  }
  
  // -- AbstractDatasetMetadata API Methods --
  
  
  // -- Metadata API Methods --
  
  public void reset() {
    super.reset();
    meta.reset(meta.getClass());
  }
  
  public void setSource(RandomAccessInputStream source) {
    super.setSource(source);
    meta.setSource(source);
  }
}
