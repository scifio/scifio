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

import net.imglib2.meta.AxisType;

import ome.scifio.AbstractDatasetMetadata;
import ome.scifio.ImageMetadata;

import loci.legacy.adapter.AdapterTools;
import loci.legacy.adapter.Wrapper;

/**
 * Wraps an array of legacy {@link loci.formats.CoreMetadata} objects
 * in the equivalent {@link ome.scifio.DatasetMetadata}. Since there are no
 * accessor methods of the array to override, its contents are manually
 * extracted and converted via the {@link CoreImageMetadataAdapter}.
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataListWrapper extends AbstractDatasetMetadata
  implements ome.scifio.DatasetMetadata, Wrapper<List<CoreMetadata>>
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

  public Object getImageMetadataValue(int imageIndex, String field) {
    return get(imageIndex).getImageMetadata().get(field);
  }

  public Hashtable<String, Object> getImageMetadata(int imageIndex) {
    return get(imageIndex).getImageMetadata();
  }

  public ImageMetadata get(int imageIndex) {
    return AdapterTools.getAdapter(CoreImageMetadataAdapter.class).
            getModern(unwrap().get(imageIndex));
  }

  public int getImageCount() {
    return unwrap().size();
  }

  public int getPlaneCount(int imageIndex) {
    return get(imageIndex).getPlaneCount();
  }

  public boolean isInterleaved(int imageIndex) {
    return get(imageIndex).isInterleaved();
  }

  public int getPixelType(int imageIndex) {
    return get(imageIndex).getPixelType();
  }

  public int getEffectiveSizeC(int imageIndex) {
    return get(imageIndex).getEffectiveSizeC();
  }

  public int getRGBChannelCount(int imageIndex) {
    return get(imageIndex).getRGBChannelCount();
  }

  public boolean isLittleEndian(int imageIndex) {
    return get(imageIndex).isLittleEndian();
  }

  public boolean isIndexed(int imageIndex) {
    return get(imageIndex).isIndexed();
  }

  public int getBitsPerPixel(int imageIndex) {
    return get(imageIndex).getBitsPerPixel();
  }

  public boolean isRGB(int imageIndex) {
    return get(imageIndex).isRGB();
  }

  public boolean isFalseColor(int imageIndex) {
    return get(imageIndex).isFalseColor();
  }

  public int[] getChannelDimLengths(int imageIndex) {
    return get(imageIndex).getChannelLengths();
  }

  public String[] getChannelDimTypes(int imageIndex) {
    return get(imageIndex).getChannelTypes();
  }

  public int getThumbSizeX(int imageIndex) {
    return get(imageIndex).getThumbSizeX();
  }

  public int getThumbSizeY(int imageIndex) {
    return get(imageIndex).getThumbSizeY();
  }

  public int getAxisCount(int imageIndex) {
    return 5;
  }

  public AxisType getAxisType(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisType(planeIndex);
  }

  public int getAxisLength(int imageIndex, int planeIndex) {
    return get(imageIndex).getAxisLength(planeIndex);
  }

  public int getAxisLength(int imageIndex, AxisType t) {
    return get(imageIndex).getAxisLength(t);
  }

  public int getAxisIndex(int imageIndex, AxisType type) {
    return get(imageIndex).getAxisIndex(type);
  }

  public AxisType[] getAxes(int imageIndex) {
    return get(imageIndex).getAxes();
  }

  public int[] getAxesLengths(int imageIndex) {
    return get(imageIndex).getAxesLengths();
  }

  public void addAxis(int imageIndex, AxisType type) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  public void addAxis(int imageIndex, AxisType type, int value) {
    throw new UnsupportedOperationException(
        "CoreMetadataListWrapper can not add an AxisType: " + type);
  }

  public boolean isOrderCertain(int imageIndex) {
    return get(imageIndex).isOrderCertain();
  }

  public boolean isThumbnailImage(int imageIndex) {
    return get(imageIndex).isThumbnail();
  }

  public boolean isMetadataComplete(int imageIndex) {
    return get(imageIndex).isMetadataComplete();
  }

  public void putImageMeta(int imageIndex, String key, Object value) {
    get(imageIndex).getImageMetadata().put(key, value);
  }

  public void setThumbSizeX(int imageIndex, int thumbX) {
    get(imageIndex).setThumbSizeX(thumbX);
  }

  public void setThumbSizeY(int imageIndex, int thumbY) {
    get(imageIndex).setThumbSizeY(thumbY);
  }

  public void setPixelType(int imageIndex, int type) {
    get(imageIndex).setPixelType(type);
  }

  public void setBitsPerPixel(int imageIndex, int bpp) {
    get(imageIndex).setBitsPerPixel(bpp);
  }

  public void setChannelDimLengths(int imageIndex, int[] cLengths) {
    get(imageIndex).setChannelLengths(cLengths);
  }

  public void setChannelDimTypes(int imageIndex, String[] cTypes) {
    get(imageIndex).setChannelTypes(cTypes);
  }

  public void setOrderCertain(int imageIndex, boolean orderCertain) {
    get(imageIndex).setOrderCertain(orderCertain);
  }

  public void setRGB(int imageIndex, boolean rgb) {
    get(imageIndex).setRGB(rgb);
  }

  public void setLittleEndian(int imageIndex, boolean littleEndian) {
    get(imageIndex).setLittleEndian(littleEndian);
  }

  public void setInterleaved(int imageIndex, boolean interleaved) {
    get(imageIndex).setInterleaved(interleaved);
  }

  public void setIndexed(int imageIndex, boolean indexed) {
    get(imageIndex).setIndexed(indexed);
  }

  public void setFalseColor(int imageIndex, boolean falseC) {
    get(imageIndex).setFalseColor(falseC);
  }

  public void setMetadataComplete(int imageIndex, boolean metadataComplete) {
    get(imageIndex).setMetadataComplete(metadataComplete);
  }

  public void add(ImageMetadata meta) {
    unwrap().add(new CoreMetadata(meta));
  }

  public void setImageMetadata(int imageIndex, Hashtable<String, Object> meta) {
    get(imageIndex).setImageMetadata(meta);
  }

  public void setThumbnailImage(int imageIndex, boolean thumbnail) {
    get(imageIndex).setThumbnail(thumbnail);
  }

  public void setAxisTypes(int imageIndex, AxisType[] axisTypes) {
    get(imageIndex).setAxisTypes(axisTypes);
  }

  public void setAxisType(int imageIndex, int axisIndex, AxisType axis) {
    get(imageIndex).setAxisType(axisIndex, axis);
  }

  public void setAxisLengths(int imageIndex, int[] axisLengths) {
    get(imageIndex).setAxisLengths(axisLengths);
  }

  public void setAxisLength(int imageIndex, AxisType axis, int length) {
    get(imageIndex).setAxisLength(axis, length);
  }
}
