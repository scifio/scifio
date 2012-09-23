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

import java.io.IOException;
import java.util.Hashtable;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.FormatException;
import ome.scifio.util.FormatTools;
import loci.legacy.adapter.Wrapper;

/**
 * Wraps an array of legacy CoreMetadata objects in a modern, SCIFIO
 * CoreMetadata. This wrapper preserves backwards compatibility, and maintains
 * synchronization between the two layers by delegating all functionality from
 * modern to legacy classes.
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataWrapper extends ome.scifio.CoreMetadata implements Wrapper<CoreMetadata[]> {

  // -- Fields --
  
  private CoreMetadata[] cMeta;
  
  // -- Constructor --
  
  public CoreMetadataWrapper(CoreMetadata[] core) {
    cMeta = core;
  }
  
  // -- Wrapper API Methods --
  
  public CoreMetadata[] unwrap() {
    return cMeta;
  }
  
  // -- CoreMetadata Methods --

  // -- Getters --

  public Object getImageMetadataValue(final int imageIndex, final String field)
  {
    return cMeta[imageIndex].seriesMetadata.get(field);
  }

  public Hashtable<String, Object> getImageMetadata(final int imageIndex) {
    return cMeta[imageIndex].seriesMetadata;
  }

  public int getImageCount() {
    return cMeta.length;
  }

  public int getPlaneCount(final int imageIndex) {
    return cMeta[imageIndex].imageCount;
  }

  public boolean isInterleaved(final int imageIndex) {
    return cMeta[imageIndex].interleaved;
  }

  public int getPixelType(final int imageIndex) {
    return cMeta[imageIndex].pixelType;
  }

  public int getEffectiveSizeC(final int imageIndex) {
    final int sizeZT =
        cMeta[imageIndex].sizeZ * cMeta[imageIndex].sizeT;
    if (sizeZT == 0) return 0;
    return getPlaneCount(imageIndex) / sizeZT;
  }

  public int getRGBChannelCount(final int imageIndex) {
    if(!cMeta[imageIndex].rgb) return 1;
    
    final int effSizeC = getEffectiveSizeC(imageIndex);
    if (effSizeC == 0) return 0;
    return cMeta[imageIndex].sizeC / effSizeC;
  }

  public boolean isLittleEndian(final int imageIndex) {
    return cMeta[imageIndex].littleEndian;
  }

  public boolean isIndexed(final int imageIndex) {
    return cMeta[imageIndex].indexed;
  }

  public int getBitsPerPixel(final int imageIndex) {
    return cMeta[imageIndex].bitsPerPixel;
  }

  public byte[][] get8BitLookupTable(final int imageIndex)
    throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public short[][] get16BitLookupTable(final int imageIndex)
    throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isRGB(final int imageIndex) {
    return getRGBChannelCount(imageIndex) > 1;
  }

  public boolean isFalseColor(final int imageIndex) {
    return cMeta[imageIndex].falseColor;
  }

  public int[] getChannelDimLengths(final int imageIndex) {
    if (cMeta[imageIndex].cLengths == null)
      return new int[] {cMeta[imageIndex].sizeC};
    return cMeta[imageIndex].cLengths;
  }

  public String[] getChannelDimTypes(final int imageIndex) {
    if (cMeta[imageIndex].cTypes == null)
      return new String[] {FormatTools.CHANNEL};
    return cMeta[imageIndex].cTypes;
  }

  public int getThumbSizeX(final int imageIndex) {
    if (cMeta[imageIndex].thumbSizeX == 0) {
      final int sx = cMeta[imageIndex].sizeX;
      final int sy = cMeta[imageIndex].sizeY;
      int thumbSizeX = 0;
      if (sx > sy) thumbSizeX = FormatTools.THUMBNAIL_DIMENSION;
      else if (sy > 0) thumbSizeX = sx * FormatTools.THUMBNAIL_DIMENSION / sy;
      if (thumbSizeX == 0) thumbSizeX = 1;
      return thumbSizeX;
    }
    return cMeta[imageIndex].thumbSizeX;
  }

  public int getThumbSizeY(final int imageIndex) {
    if (cMeta[imageIndex].thumbSizeY == 0) {
      final int sx = cMeta[imageIndex].sizeX;
      final int sy = cMeta[imageIndex].sizeY;
      int thumbSizeY = 1;
      if (sy > sx) thumbSizeY = FormatTools.THUMBNAIL_DIMENSION;
      else if (sx > 0) thumbSizeY = sy * FormatTools.THUMBNAIL_DIMENSION / sx;
      if (thumbSizeY == 0) thumbSizeY = 1;
      return thumbSizeY;
    }
    return cMeta[imageIndex].thumbSizeY;
  }
  
  //TODO valid index checking - fail more gracefully?

  /**
   * Returns the number of axes (planes) in the
   * specified image.
   * 
   * @param imageIndex - index for multi-image files
   * @return The axis/plane count
   */
  public int getAxisCount(final int imageIndex) {
    return cMeta[imageIndex].dimensionOrder.length();
  }

  /**
   * Gets the type of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Type of the desired plane.
   */
  public AxisType getAxisType(final int imageIndex, final int planeIndex) {
    switch(cMeta[imageIndex].dimensionOrder.toUpperCase().charAt(planeIndex)) {
    case 'X': return Axes.X;
    case 'Y': return Axes.Y;
    case 'Z': return Axes.Z;
    case 'C': return Axes.CHANNEL;
    case 'T': return Axes.TIME;
    default : return Axes.UNKNOWN;
    }
  }

  /**
   * Gets the length of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Length of the desired plane.
   */
  public int getAxisLength(final int imageIndex, final int planeIndex) {
    switch(cMeta[imageIndex].dimensionOrder.toUpperCase().charAt(planeIndex)) {
    case 'X': return cMeta[imageIndex].sizeX;
    case 'Y': return cMeta[imageIndex].sizeY;
    case 'Z': return cMeta[imageIndex].sizeZ;
    case 'C': return cMeta[imageIndex].sizeC;
    case 'T': return cMeta[imageIndex].sizeT;
    default : return -1;
    }
  }
  
  /**
   * A convenience method for looking up the length of an axis
   * based on its type. No knowledge of plane ordering is necessary.
   * 
   * @param imageIndex - index for multi-image files
   * @param t - desired axis type
   * @return
   */
  public int getAxisLength(final int imageIndex, final AxisType t) {
    return getAxisLength(imageIndex, getAxisIndex(imageIndex, t));
  }

  /**
   * Returns the array index for the specified AxisType. This index
   * can be used in other Axes methods for looking up lengths, etc...
   * </br></br>
   * This method can also be used as an existence check for the
   * targe AxisType.
   * 
   * @param imageIndex - index for multi-image files
   * @param type - axis type to look up
   * @return The index of the desired axis or -1 if not found.
   */
  public int getAxisIndex(final int imageIndex, final AxisType type) {
    return cMeta[imageIndex].dimensionOrder.toUpperCase().indexOf(
        type.getLabel().toUpperCase().charAt(0));
    // throw exception?
  }
  
  /**
   * Returns an array of the types for axes associated with
   * the specified image index. Order is consistent with the
   * axis length (int) array returned by 
   * {@link CoreMetadata#getAxesLengths(int)}.
   * </br></br>
   * AxisType order is sorted and represents order within the image.
   * 
   * @param imageIndex - index for multi-image sources
   * @return An array of AxisTypes in the order they appear.
   */
  public AxisType[] getAxes(int imageIndex) {
    return FormatTools.findDimensionList(cMeta[imageIndex].dimensionOrder);
  }
  
  /**
   * Returns an array of the lengths for axes associated with
   * the specified image index.
   * 
   * Ordering is consistent with the 
   * AxisType array returned by {@link CoreMetadata#getAxes(int)}.
   * 
   * @param imageIndex
   * @return
   */
  public int[] getAxesLengths(int imageIndex) {
    String order = cMeta[imageIndex].dimensionOrder;
    int[] lengths = new int[order.length()];
    for(int i = 0; i < order.length(); i++) {
      char dim = order.toUpperCase().charAt(i);
      switch(dim) {
      case 'X': lengths[i] = cMeta[imageIndex].sizeX;
        break;
      case 'Y': lengths[i] = cMeta[imageIndex].sizeY;
        break;
      case 'Z': lengths[i] = cMeta[imageIndex].sizeZ;
        break;
      case 'C': lengths[i] = cMeta[imageIndex].sizeC;
        break;
      case 'T': lengths[i] = cMeta[imageIndex].sizeT;
        break;
      }
    }
    
    return lengths;
  }

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates corresponding length = 0 entry in the axis lengths
   * array.
   * 
   * @param imageIndex
   * @param type
   */
  public void addAxis(final int imageIndex, final AxisType type) {
    throw new UnsupportedOperationException();
  }

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates a corresponding entry with the specified value in
   * axis lengths.
   * 
   * @param imageIndex
   * @param type
   * @param value
   */
  public void addAxis(final int imageIndex, final AxisType type, final int value)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isOrderCertain(final int imageIndex) {
    return cMeta[imageIndex].orderCertain;
  }

  public boolean isThumbnailImage(final int imageIndex) {
    return cMeta[imageIndex].thumbnail;
  }

  public boolean isMetadataComplete(final int imageIndex) {
    return cMeta[imageIndex].metadataComplete;
  }

  // -- Setters --
  
  public void set8BitLookupTable(final int imageIndex, final byte[][] lut)
    throws FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public void set16BitLookupTable(final int imageIndex, final short[][] lut)
    throws FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public void setThumbSizeX(final int imageIndex, final int thumbX) {
    cMeta[imageIndex].thumbSizeX = thumbX;
  }

  public void setThumbSizeY(final int imageIndex, final int thumbY) {
    cMeta[imageIndex].thumbSizeY = thumbY;
  }

  public void setPixelType(final int imageIndex, final int type) {
    cMeta[imageIndex].pixelType = type;
  }

  public void setBitsPerPixel(final int imageIndex, final int bpp) {
    cMeta[imageIndex].bitsPerPixel = bpp;
  }

  public void setChannelDimLengths(final int imageIndex, final int[] cLengths) {
    cMeta[imageIndex].cLengths = cLengths;
  }

  public void setChannelDimTypes(final int imageIndex, final String[] cTypes) {
    cMeta[imageIndex].cTypes = cTypes;
  }

  public void setOrderCertain(final int imageIndex, final boolean orderCertain)
  {
    cMeta[imageIndex].orderCertain = orderCertain;
  }

  public void setRGB(final int imageIndex, final boolean rgb) {
    cMeta[imageIndex].rgb = rgb;
  }

  public void setLittleEndian(final int imageIndex, final boolean littleEndian)
  {
    cMeta[imageIndex].littleEndian = littleEndian;
  }

  public void setInterleaved(final int imageIndex, final boolean interleaved) {
    cMeta[imageIndex].interleaved = interleaved;
  }

  public void setIndexed(final int imageIndex, final boolean indexed) {
    cMeta[imageIndex].indexed = indexed;
  }

  public void setFalseColor(final int imageIndex, final boolean falseC) {
    cMeta[imageIndex].falseColor = falseC;
  }

  public void setMetadataComplete(final int imageIndex,
    final boolean metadataComplete)
  {
    cMeta[imageIndex].metadataComplete = metadataComplete;
  }

  public void setImageMetadata(final int imageIndex,
    final Hashtable<String, Object> meta)
  {
    cMeta[imageIndex].seriesMetadata = meta;
  }

  public void setThumbnailImage(final int imageIndex, final boolean thumbnail) {
    cMeta[imageIndex].thumbnail = thumbnail;
  }

  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
    cMeta[imageIndex].dimensionOrder = FormatTools.findDimensionOrder(axisTypes);
  }
  
  public void setAxisType(final int imageIndex, final int axisIndex, final AxisType axis) {
    throw new UnsupportedOperationException();
  }

  public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
    for(int i = 0; i < axisLengths.length; i++) {
      setDimLength(imageIndex, 
          cMeta[imageIndex].dimensionOrder.toUpperCase().charAt(i), axisLengths[i]);
    }
  }
  
  public void setAxisLength(final int imageIndex, final AxisType axis, final int length) {
    setDimLength(imageIndex, axis.getLabel().toUpperCase().charAt(0), length);
  }
  
  // -- Helper methods --
  
  private void setDimLength(final int imageIndex, final char dim, final int length) {
    switch(dim) {
    case 'X': cMeta[imageIndex].sizeX = length;
      break;
    case 'Y': cMeta[imageIndex].sizeY = length;
      break;
    case 'Z': cMeta[imageIndex].sizeZ = length;
      break;
    case 'C': cMeta[imageIndex].sizeC = length;
      break;
    case 'T': cMeta[imageIndex].sizeT = length;
      break;
    }
  }
}
