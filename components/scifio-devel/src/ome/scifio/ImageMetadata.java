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

import java.util.Arrays;
import java.util.Hashtable;

import net.imglib2.meta.AxisType;

/**
 * ImageMetadata stores the metadata for a single image
 * within a dataset. Here, information common to every
 * image type (e.g. height, width, index information)
 * can be stored in a standard way.
 * 
 * @author Mark Hiner
 *
 */
public interface ImageMetadata {

  /** Sets width (in pixels) of thumbnail planes in this image. */
  void setThumbSizeX(int thumbSizeX);

  /** Sets height (in pixels) of thumbnail planes in this image. */
  void setThumbSizeY(int thumbSizeY);

  /**
   * Sets the number of bytes per pixel.  Must be one of the <i>static</i>
   * pixel types (e.g. <code>INT8</code>) in {@link ome.scifio.util.FormatTools}.
   */
  void setPixelType(int pixelType);

  /** Sets the number of valid bits per pixel. */
  void setBitsPerPixel(int bitsPerPixel);

  /** Sets the length of each subdimension of C. */
  void setChannelLengths(int[] cLengths);

  /** Sets the name of each subdimension of C. */
  void setChannelTypes(String[] cTypes);

  /**
   * Sets whether or not we are confident that the
   * dimension order is correct.
   */
  void setOrderCertain(boolean orderCertain);

  /**
   * Sets whether or not the planes are stored as RGB
   * (multiple channels per plane).
   */
  void setRGB(boolean rgb);

  /** Sets whether or not each pixel's bytes are in little endian order. */
  void setLittleEndian(boolean littleEndian);

  /**
   * Set true if channels are stored RGBRGBRGB...; false if channels are stored
   * RRR...GGG...BBB...
   */
  void setInterleaved(boolean interleaved);

  /** Sets whether or not the planes are stored as indexed color. */
  void setIndexed(boolean indexed);

  /** Sets whether or not we can ignore the color map (if present). */
  void setFalseColor(boolean falseColor);

  /**
   * Sets whether or not we are confident that all of the metadata stored
   * within the image has been parsed.
   */
  void setMetadataComplete(boolean metadataComplete);

  /** Sets a collection of non-core metadata associated with this image. */
  void setImageMetadata(Hashtable<String, Object> imageMetadata);

  /** 
   * Sets whether or not this image is a lower-resolution copy of
   * another image.
   */
  void setThumbnail(boolean thumbnail);

  /** 
   * Sets the Axes types for this image. 
   * Order is implied by ordering within this array
   */
  void setAxisTypes(AxisType[] axisTypes);

  /** Sets the lengths of each axis. Order is parallel of axisTypes. */
  void setAxisLengths(int[] axisLengths);

  /** 
   * Sets the length for the specified axis, 
   * if its type is present in the image. 
   */
  void setAxisLength(AxisType axis, int length);

  /** Sets the type of the axis at the specified index. */
  void setAxisType(int index, AxisType axis);

  /** Sets the number of planes within this image. */
  void setPlaneCount(int planeCount);

  /** Returns the number of planes within this image. */
  int getPlaneCount();

  /** Returns the width (in pixles) of the thumbnail planes in this image. */
  int getThumbSizeX();

  /** Returns the height (in pixles) of the thumbnail planes in this image. */
  int getThumbSizeY();

  /** 
   * Returns the number of bytes per pixel. Should correlate with the
   * pixel types in {@link ome.scifio.util.FormatTools}
   */
  int getPixelType();

  /** Returns the number of valid bits per pixel. */
  int getBitsPerPixel();

  /** Returns the length of each subdimension of C. */
  int[] getChannelLengths();

  /** Returns the name of each subdimension of C. */
  String[] getChannelTypes();

  /** 
   * Returns the Axes types for this image. 
   * Order is implied by ordering within this array.
   */
  AxisType[] getAxisTypes();

  /** Returns the lengths of each axis. Order is parallel of axisTypes. */
  int[] getAxisLengths();

  /**
   * Returns true if we are confident that the
   * dimension order is correct.
   */
  boolean isOrderCertain();

  /**
   * Returns true if the planes are stored as RGB
   * (multiple channels per plane).
   */
  boolean isRGB();

  /** Returns true if each pixel's bytes are in little endian order. */
  boolean isLittleEndian();

  /**
   * Returns true if channels are stored RGBRGBRGB...; false if channels are stored
   * RRR...GGG...BBB...
   */
  boolean isInterleaved();

  /** Returns true if the planes are stored as indexed color. */
  boolean isIndexed();

  /** Returns true if we can ignore the color map (if present). */
  boolean isFalseColor();

  /**
   * Returns true if we are confident that all of the metadata stored
   * within the image has been parsed.
   */
  boolean isMetadataComplete();

  /** Returns a collection of non-core metadata associated with this image. */
  Hashtable<String, Object> getImageMetadata();

  /** 
   * Returns whether or not this image is a lower-resolution copy of
   * another image.
   */
  boolean isThumbnail();

  /**
   * Gets the type of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Type of the desired plane.
   */
  AxisType getAxisType(final int planeIndex);

  /**
   * Gets the length of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param planeIndex - index of the desired plane within the specified image
   * @return Length of the desired plane.
   */
  int getAxisLength(final int planeIndex);
  
  /**
   * A convenience method for looking up the length of an axis
   * based on its type. No knowledge of plane ordering is necessary.
   * 
   * @param imageIndex - index for multi-image files
   * @param t - desired axis type
   * @return
   */
  int getAxisLength(final AxisType t);

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
  int getAxisIndex(final AxisType type);
  
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
  AxisType[] getAxes();
  
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
  int[] getAxesLengths();

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates corresponding length = 0 entry in the axis lengths
   * array.
   * 
   * @param imageIndex
   * @param type
   */
  void addAxis(final AxisType type);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates a corresponding entry with the specified value in
   * axis lengths.
   * 
   * @param imageIndex
   * @param type
   * @param value
   */
  void addAxis(final AxisType type, final int value);

}