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
package ome.scifio;

import java.util.Hashtable;

import net.imglib2.meta.AxisType;

/**
 * DatasetMetadata represents the metadata for a complete dataset, consisting 
 * of an arbitrary number of images. Metadata for individual images is stored 
 * in {@link ImageMetadata} objects.
 * <p>
 * DatasetMetadata is the lowest level metadata currency of SCIFIO, to which
 * format-specific metadata can be converted and thus compared.
 * </p>
 * @author Mark Hiner
 *
 */
public interface DatasetMetadata<M extends ImageMetadata> extends Metadata {

  /** Looks up the dataset metadata value for the provided key. */
  Object getMetadataValue(String field);

  /** 
   * Looks up the image metadata value for the provided key and specified
   * image index.
   */
  Object getImageMetadataValue(int imageIndex, String field);

  /** Returns the collection of metadata for this dataset. */
  Hashtable<String, Object> getDatasetMetadata();

  /** Returns the collection of metadata for the specified image. */
  Hashtable<String, Object> getImageMetadata(int imageIndex);

  /** Returns the number of images in this dataset. */
  int getImageCount();

  /** Returns the number of planes in the specified image. */
  int getPlaneCount(int imageIndex);

  /** 
   * Returns true if the spcified image stores its channelsRGBRGBRGB...;
   * false if channels are stored RRR...GGG...BBB...
   */
  boolean isInterleaved(int imageIndex);

  /** 
   * Returns the number of bytes per pixel for the specified image.
   * Should correlate with the pixel types in {@link ome.scifio.util.FormatTools}
   */
  int getPixelType(int imageIndex);

  /**
   * Gets the effective size of the C dimension for the specified image,
   * guaranteeing that getEffectiveSizeC() * sizeZ * sizeT ==
   * getPlaneCount() regardless of the result of isRGB().
   */
  int getEffectiveSizeC(int imageIndex);

  /**
   * Gets the number of channels returned with each call to openBytes for the
   * specified image. The most common case where this value is greater than 1 is
   * for interleaved RGB data, such as a 24-bit color image plane. However, it
   * is possible for this value to be greater than 1 for non-interleaved data,
   * such as an RGB TIFF with Planar rather than Chunky configuration.
   */
  int getRGBChannelCount(int imageIndex);

  /**
   * Returns true if for the specified image, each pixel's bytes are in little
   * endian order. 
   */
  boolean isLittleEndian(int imageIndex);

  /** Returns true if the planes within the specified image are stored as indexed color. */
  boolean isIndexed(int imageIndex);

  /** Returns the number of valid bits per pixel in the specified image. */
  int getBitsPerPixel(int imageIndex);

  /**
   * Returns true if the planes are stored as RGB
   * (multiple channels per plane) within the specified image.
   */
  boolean isRGB(int imageIndex);

  /** Returns true if we can ignore the color map (if present) for the specified image. */
  boolean isFalseColor(int imageIndex);

  /** Returns the length of each subdimension of C for the specified image. */
  int[] getChannelDimLengths(int imageIndex);

  /** Returns the name of each subdimension of C for the specified image. */
  String[] getChannelDimTypes(int imageIndex);

  /** Returns the width (in pixles) of the thumbnail planes in the specified image. */
  int getThumbSizeX(int imageIndex);

  /** Returns the height (in pixles) of the thumbnail planes in the specified image. */
  int getThumbSizeY(int imageIndex);

  /**
   * Returns the number of axes (planes) in the
   * specified image.
   * 
   * @param imageIndex - index for multi-image files
   * @return The axis/plane count
   */
  int getAxisCount(int imageIndex);

  /**
   * Gets the type of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Type of the desired plane.
   */
  AxisType getAxisType(int imageIndex, int planeIndex);

  /**
   * Gets the length of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Length of the desired plane.
   */
  int getAxisLength(int imageIndex, int planeIndex);

  /**
   * A convenience method for looking up the length of an axis
   * based on its type. No knowledge of plane ordering is necessary.
   * 
   * @param imageIndex - index for multi-image files
   * @param t - desired axis type
   * @return
   */
  int getAxisLength(int imageIndex, AxisType t);

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
  int getAxisIndex(int imageIndex, AxisType type);

  /**
   * Returns an array of the types for axes associated with
   * the specified image index. Order is consistent with the
   * axis length (int) array returned by 
   * {@link DatasetMetadata#getAxesLengths(int)}.
   * </br></br>
   * AxisType order is sorted and represents order within the image.
   * 
   * @param imageIndex - index for multi-image sources
   * @return An array of AxisTypes in the order they appear.
   */
  AxisType[] getAxes(int imageIndex);

  /**
   * Returns an array of the lengths for axes associated with
   * the specified image index.
   * 
   * Ordering is consistent with the 
   * AxisType array returned by {@link DatasetMetadata#getAxes(int)}.
   * 
   * @param imageIndex
   * @return
   */
  int[] getAxesLengths(int imageIndex);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates corresponding length = 0 entry in the axis lengths
   * array.
   * 
   * @param imageIndex
   * @param type
   */
  void addAxis(int imageIndex, AxisType type);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates a corresponding entry with the specified value in
   * axis lengths.
   * 
   * @param imageIndex
   * @param type
   * @param value
   */
  void addAxis(int imageIndex, AxisType type, int value);

  /**
   * Returns true if we are confident that the
   * dimension order is correct for the specified image.
   */
  boolean isOrderCertain(int imageIndex);

  /** 
   * Returns true if the specified image is a lower-resolution copy of
   * another image.
   */
  boolean isThumbnailImage(int imageIndex);

  /**
   * Returns true if we are confident that all of the metadata stored
   * within the specified image has been parsed.
   */
  boolean isMetadataComplete(int imageIndex);

  /** Convenience method for storing Dataset-level metadata. */
  void putDatasetMeta(String key, Object value);

  /** Convenience method for storing metadata at the specified image-level. */
  void putImageMeta(int imageIndex, String key, Object value);

  /** Sets width (in pixels) of thumbnail planes for the specified image. */
  void setThumbSizeX(int imageIndex, int thumbX);

  /** Sets height (in pixels) of thumbnail planes for the specified image. */
  void setThumbSizeY(int imageIndex, int thumbY);

  /**
   * Sets the number of bytes per pixel for the specified image.
   * Must be one of the <i>static</i>
   * pixel types (e.g. <code>INT8</code>) in {@link ome.scifio.util.FormatTools}.
   */
  void setPixelType(int imageIndex, int type);

  /** Sets the number of valid bits per pixel for the specified image. */
  void setBitsPerPixel(int imageIndex, int bpp);

  /** Sets the length of each subdimension of C for the specified image. */
  void setChannelDimLengths(int imageIndex, int[] cLengths);

  /** Sets the name of each subdimension of C for the specified image. */
  void setChannelDimTypes(int imageIndex, String[] cTypes);

  /**
   * Sets whether or not we are confident that the
   * dimension order is correct for the specified image.
   */
  void setOrderCertain(int imageIndex, boolean orderCertain);

  /**
   * Sets whether or not the planes are stored as RGB
   * (multiple channels per plane) for the specified image.
   */
  void setRGB(int imageIndex, boolean rgb);

  /**
   *  Sets whether or not the bytes of the specified image's pixels
   *  are in little endian order.
   */
  void setLittleEndian(int imageIndex, boolean littleEndian);

  /**
   * Set true if the specified image's channels are stored RGBRGBRGB...;
   * false if channels are stored RRR...GGG...BBB...
   */
  void setInterleaved(int imageIndex, boolean interleaved);

  /** Sets whether or not the planes are stored as indexed color for the specified image. */
  void setIndexed(int imageIndex, boolean indexed);

  /** Sets whether or not we can ignore the color map (if present) of the specified image. */
  void setFalseColor(int imageIndex, boolean falseC);

  /**
   * Sets whether or not we are confident that all of the metadata stored
   * within the specified image has been parsed.
   */
  void setMetadataComplete(int imageIndex, boolean metadataComplete);

  /** Sets a collection of non-core metadata associated with the specified image. */
  void setImageMetadata(int imageIndex, Hashtable<String, Object> meta);

  /** 
   * Sets whether or not the specified image is a lower-resolution copy of
   * another image.
   */
  void setThumbnailImage(int imageIndex, boolean thumbnail);

  /** 
   * Sets the Axes types for the specified image. 
   * Order is implied by ordering within this array
   */
  void setAxisTypes(int imageIndex, AxisType[] axisTypes);

  /** Sets the type of the axis at the specified index, for the specified image. */
  void setAxisType(int imageIndex, int axisIndex, AxisType axis);

  /** 
   * Sets the lengths of each axis for the specified image.
   * Order is parallel of axisTypes.
   */
  void setAxisLengths(int imageIndex, int[] axisLengths);

  /** 
   * Sets the length for the specified axis, 
   * if its type is present in the specified image. 
   */
  void setAxisLength(int imageIndex, AxisType axis, int length);
}