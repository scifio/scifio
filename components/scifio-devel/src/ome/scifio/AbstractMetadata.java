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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.meta.AxisType;
import ome.scifio.io.RandomAccessInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of all SCIFIO {@link ome.scifio.Metadata} implementations.
 * 
 * @see ome.scifio.Metadata
 * @see ome.scifio.MetadataOptions
 * @see ome.scifio.Parser
 * @see ome.scifio.HasFormat
 * 
 * @author Mark Hiner
 */
public abstract class AbstractMetadata extends AbstractHasFormat
  implements TypedMetadata {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

  // -- Fields --
  
  /* The image source associated with this Metadata. */
  private RandomAccessInputStream source;
  
  /* Whether the Metadata should be filtered or not. */
  protected boolean filtered;
  
  /* The MetadataOptions used when parsing this Metadata. */
  protected MetadataOptions metadataOptions;

  /* Contains a list of metadata objects for each image in this dataset */
  @ome.scifio.Field(label = "imageMeta", isList = true)
  private List<ImageMetadata> imageMeta;
  
  /* A string id for this dataset. */
  private String datasetName = null;
  
  /* A table of Field key, value pairs */
  private MetaTable table;
  
  // -- Constructors --
  
  public AbstractMetadata() {
    this((List<ImageMetadata>)null);
  }
  
  public AbstractMetadata(final Metadata copy) {
    this(copy.getAll());
    
    table = new DefaultMetaTable(copy.getTable());
  }
  
  public AbstractMetadata(final List<ImageMetadata> list) {
    imageMeta = new ArrayList<ImageMetadata>();
    table = new DefaultMetaTable();

    if (list != null) {
      for(int i = 0; i < list.size(); i++) {
        ImageMetadata core = list.get(i);
        imageMeta.add(core.copy());
      }
    }
  }

  // -- Metadata API Methods --
  
  public void reset() {
    reset(getClass());
  }


  /* @see Metadata#setSource(RandomAccessInputStream) */
  public void setSource(final RandomAccessInputStream source) {
    this.source = source;
    
    if (source != null) setDatasetName(source.getFileName());
  }

  /* @see Metadata#getSource() */
  public RandomAccessInputStream getSource() {
    return source;
  }
  
  public void close() {
    if (source != null) {
      try {
        source.close();
      } catch (IOException e) {
        LOGGER.error("Failed to close stream in Metadata", e);
      }
    }
  }
  
  /* @see Metadata#isFiltered() */
  public boolean isFiltered() {
    return filtered;
  }
  
  /* @see Metadata#getMetadataOptions() */
  public MetadataOptions getMetadataOptions() {
    return metadataOptions;
  }
  
  // -- Getters --
  
  public String getDatasetName() {
    return datasetName;
  }
  
  /*
   * @see ome.scifio.Metadata#get(int)
   */
  public ImageMetadata get(int imageIndex) {
    return imageMeta.get(imageIndex);
  }
  
  /*
   * @see ome.scifio.Metadata#getAll()
   */
  public List<ImageMetadata> getAll() {
    return imageMeta;
  }

  /*
   * @see ome.scifio.Metadata#getImageCount()
   */
  public int getImageCount() {
    return imageMeta.size();
  }

  /*
   * @see ome.scifio.Metadata#getPlaneCount(int)
   */
  public int getPlaneCount(final int imageIndex) {
    return imageMeta.get(imageIndex).getPlaneCount();
  }

  /*
   * @see ome.scifio.Metadata#isInterleaved(int)
   */
  public boolean isInterleaved(final int imageIndex) {
    return imageMeta.get(imageIndex).isInterleaved();
  }

  /*
   * @see ome.scifio.Metadata#getPixelType(int)
   */
  public int getPixelType(final int imageIndex) {
    return imageMeta.get(imageIndex).getPixelType();
  }

  /*
   * @see ome.scifio.Metadata#getEffectiveSizeC(int)
   */
  public int getEffectiveSizeC(final int imageIndex) {
    return imageMeta.get(imageIndex).getEffectiveSizeC();
  }

  /*
   * @see ome.scifio.Metadata#getRGBChannelCount(int)
   */
  public int getRGBChannelCount(final int imageIndex) {
    return imageMeta.get(imageIndex).getRGBChannelCount();
  }

  /*
   * @see ome.scifio.Metadata#isLittleEndian(int)
   */
  public boolean isLittleEndian(final int imageIndex) {
    return imageMeta.get(imageIndex).isLittleEndian();
  }

  /*
   * @see ome.scifio.Metadata#isIndexed(int)
   */
  public boolean isIndexed(final int imageIndex) {
    return imageMeta.get(imageIndex).isIndexed();
  }

  /*
   * @see ome.scifio.Metadata#getBitsPerPixel(int)
   */
  public int getBitsPerPixel(final int imageIndex) {
    return imageMeta.get(imageIndex).getBitsPerPixel();
  }

  /*
   * @see ome.scifio.Metadata#isRGB(int)
   */
  public boolean isRGB(final int imageIndex) {
    return imageMeta.get(imageIndex).isRGB();
  }

  /*
   * @see ome.scifio.Metadata#isFalseColor(int)
   */
  public boolean isFalseColor(final int imageIndex) {
    return imageMeta.get(imageIndex).isFalseColor();
  }

  /*
   * @see ome.scifio.Metadata#getChannelDimLengths(int)
   */
  public int[] getChannelDimLengths(final int imageIndex) {
    return imageMeta.get(imageIndex).getChannelLengths();
  }

  /*
   * @see ome.scifio.Metadata#getChannelDimTypes(int)
   */
  public String[] getChannelDimTypes(final int imageIndex) {
    return imageMeta.get(imageIndex).getChannelTypes();
  }

  /*
   * @see ome.scifio.Metadata#getThumbSizeX(int)
   */
  public int getThumbSizeX(final int imageIndex) {
    return imageMeta.get(imageIndex).getThumbSizeX();
  }

  /*
   * @see ome.scifio.Metadata#getThumbSizeY(int)
   */
  public int getThumbSizeY(final int imageIndex) {
    return imageMeta.get(imageIndex).getThumbSizeY();
  }

  /*
   * @see ome.scifio.Metadata#getAxisCount(int)
   */
  public int getAxisCount(final int imageIndex) {
    return imageMeta.get(imageIndex).getAxesLengths().length;
  }

  /*
   * @see ome.scifio.Metadata#getAxisType(int, int)
   */
  public AxisType getAxisType(final int imageIndex, final int planeIndex) {
    return imageMeta.get(imageIndex).getAxisType(planeIndex);
  }

  /*
   * @see ome.scifio.Metadata#getAxisLength(int, int)
   */
  public int getAxisLength(final int imageIndex, final int planeIndex) {
    return imageMeta.get(imageIndex).getAxisLength(planeIndex);
  }
  
  /*
   * @see ome.scifio.Metadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(final int imageIndex, final AxisType t) {
    return imageMeta.get(imageIndex).getAxisLength(t);
  }

  /*
   * @see ome.scifio.Metadata#getAxisIndex(int, net.imglib2.meta.AxisType)
   */
  public int getAxisIndex(final int imageIndex, final AxisType type) {
    return imageMeta.get(imageIndex).getAxisIndex(type);
  }

  /*
   * @see ome.scifio.Metadata#getAxes(int)
   */
  public AxisType[] getAxes(int imageIndex) {
    return imageMeta.get(imageIndex).getAxes();
  }

  /*
   * @see ome.scifio.Metadata#getAxesLengths(int)
   */
  public int[] getAxesLengths(int imageIndex) {
    return imageMeta.get(imageIndex).getAxesLengths();
  }

  /*
   * @see ome.scifio.Metadata#isOrderCertain(int)
   */
  public boolean isOrderCertain(final int imageIndex) {
    return imageMeta.get(imageIndex).isOrderCertain();
  }

  /*
   * @see ome.scifio.Metadata#isThumbnailImage(int)
   */
  public boolean isThumbnailImage(final int imageIndex) {
    return imageMeta.get(imageIndex).isThumbnail();
  }

  /*
   * @see ome.scifio.Metadata#isMetadataComplete(int)
   */
  public boolean isMetadataComplete(final int imageIndex) {
    return imageMeta.get(imageIndex).isMetadataComplete();
  }

  // -- Setters --
  
  /*
   * @see ome.scifio.Metadata#setDatasetName(java.lang.String)
   */
  public void setDatasetName(String name) {
    datasetName = name;
  }
  
  /*
   * @see ome.scifio.Metadata#setThumbSizeX(int, int)
   */
  public void setThumbSizeX(final int imageIndex, final int thumbX) {
    imageMeta.get(imageIndex).setThumbSizeX(thumbX);
  }

  /*
   * @see ome.scifio.Metadata#setThumbSizeY(int, int)
   */
  public void setThumbSizeY(final int imageIndex, final int thumbY) {
    imageMeta.get(imageIndex).setThumbSizeY(thumbY);
  }

  /*
   * @see ome.scifio.Metadata#setPixelType(int, int)
   */
  public void setPixelType(final int imageIndex, final int type) {
    imageMeta.get(imageIndex).setPixelType(type);
  }

  /*
   * @see ome.scifio.Metadata#setBitsPerPixel(int, int)
   */
  public void setBitsPerPixel(final int imageIndex, final int bpp) {
    imageMeta.get(imageIndex).setBitsPerPixel(bpp);
  }

  /*
   * @see ome.scifio.Metadata#setChannelDimLengths(int, int[])
   */
  public void setChannelDimLengths(final int imageIndex, final int[] cLengths) {
    imageMeta.get(imageIndex).setChannelLengths(cLengths);
  }

  /*
   * @see ome.scifio.Metadata#setChannelDimTypes(int, java.lang.String[])
   */
  public void setChannelDimTypes(final int imageIndex, final String[] cTypes) {
    imageMeta.get(imageIndex).setChannelTypes(cTypes);
  }

  /*
   * @see ome.scifio.Metadata#setOrderCertain(int, boolean)
   */
  public void setOrderCertain(final int imageIndex, final boolean orderCertain)
  {
    imageMeta.get(imageIndex).setOrderCertain(orderCertain);
  }

  /*
   * @see ome.scifio.Metadata#setRGB(int, boolean)
   */
  public void setRGB(final int imageIndex, final boolean rgb) {
    imageMeta.get(imageIndex).setRGB(rgb);
  }

  /*
   * @see ome.scifio.Metadata#setLittleEndian(int, boolean)
   */
  public void setLittleEndian(final int imageIndex, final boolean littleEndian)
  {
    imageMeta.get(imageIndex).setLittleEndian(littleEndian);
  }

  /*
   * @see ome.scifio.Metadata#setInterleaved(int, boolean)
   */
  public void setInterleaved(final int imageIndex, final boolean interleaved) {
    imageMeta.get(imageIndex).setInterleaved(interleaved);
  }

  /*
   * @see ome.scifio.Metadata#setIndexed(int, boolean)
   */
  public void setIndexed(final int imageIndex, final boolean indexed) {
    imageMeta.get(imageIndex).setIndexed(indexed);
  }

  /*
   * @see ome.scifio.Metadata#setFalseColor(int, boolean)
   */
  public void setFalseColor(final int imageIndex, final boolean falseC) {
    imageMeta.get(imageIndex).setFalseColor(falseC);
  }

  /*
   * @see ome.scifio.Metadata#setMetadataComplete(int, boolean)
   */
  public void setMetadataComplete(final int imageIndex,
    final boolean metadataComplete)
  {
    imageMeta.get(imageIndex).setMetadataComplete(metadataComplete);
  }
  
  /*
   * @see ome.scifio.Metadata#setFiltered(boolean)
   */
  public void setFiltered(boolean filtered) {
    this.filtered = filtered;
  }
  
  /*
   * @see ome.scifio.Metadata#setMetadataOptions(ome.scifio.MetadataOptions)
   */
  public void setMetadataOptions(MetadataOptions opts) {
    metadataOptions = opts;
  }

  /*
   * @see ome.scifio.Metadata#add(ome.scifio.ImageMetadata)
   */
  public void add(final ImageMetadata meta) {
    imageMeta.add(meta);
  }

  /*
   * @see ome.scifio.Metadata#setThumbnailImage(int, boolean)
   */
  public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
    imageMeta.get(imageIndex).setThumbnail(thumbnail);
  }

  /*
   * @see ome.scifio.Metadata#addAxis(int, net.imglib2.meta.AxisType)
   */
  public void addAxis(final int imageIndex, final AxisType type) {
    imageMeta.get(imageIndex).addAxis(type);
  }

  /*
   * @see ome.scifio.Metadata#addAxis(int, net.imglib2.meta.AxisType, int)
   */
  public void addAxis(final int imageIndex, final AxisType type, final int value)
  {
    imageMeta.get(imageIndex).addAxis(type, value);
  }

  /*
   * @see ome.scifio.Metadata#setAxisTypes(int, net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
    imageMeta.get(imageIndex).setAxisTypes(axisTypes);
  }
  
  /*
   * @see ome.scifio.Metadata#setAxisType(int, int, net.imglib2.meta.AxisType)
   */
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis) {
    imageMeta.get(imageIndex).setAxisType(axisIndex, axis);
  }

  /*
   * @see ome.scifio.Metadata#setAxisLengths(int, int[])
   */
  public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
    imageMeta.get(imageIndex).setAxisLengths(axisLengths);
  }
  
  /*
   * @see ome.scifio.Metadata#setAxisLength(int, net.imglib2.meta.AxisType, int)
   */
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length) {
    imageMeta.get(imageIndex).setAxisLength(axis, length);
  }
  
  // -- HasMetaTable API Methods --
  
  public MetaTable getTable() {
    if (table == null) table = new DefaultMetaTable();
    return table;
  }
  
  public void setTable(MetaTable table) {
    this.table = table;
  }
  
  // -- Helper methods --

  /* @see Metadata#resetMeta(Class<?>) */
  private void reset(final Class<?> type) {
    if (type == null || type == AbstractMetadata.class) return;

    for (final Field f : type.getDeclaredFields()) {
      f.setAccessible(true);

      if (Modifier.isFinal(f.getModifiers())) continue;
      final Class<?> fieldType = f.getType();
      try {
        if (fieldType == boolean.class) f.set(this, false);
        else if (fieldType == char.class) f.set(this, 0);
        else if (fieldType == double.class) f.set(this, 0.0);
        else if (fieldType == float.class) f.set(this, 0f);
        else if (fieldType == int.class) f.set(this, 0);
        else if (fieldType == long.class) f.set(this, 0l);
        else if (fieldType == short.class) f.set(this, 0);
        else f.set(this, null);
      }
      catch (final IllegalArgumentException e) {
        LOGGER.debug(e.getMessage());
      }
      catch (final IllegalAccessException e) {
        LOGGER.debug(e.getMessage());
      }
      
      table = new DefaultMetaTable();
      imageMeta = new ArrayList<ImageMetadata>();

      // check superclasses and interfaces
      reset(type.getSuperclass());
      for (final Class<?> c : type.getInterfaces()) {
        reset(c);
      }
    }
  }

  /* TODO
  public boolean isSingleFile() {
    return this.size() <= 1;
  }
  
  public boolean hasCompanionFiles() {
    return false;
  }
  */
}
