/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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
package io.scif;

import net.imglib2.meta.AxisType;

/**
 * ImageMetadata stores the metadata for a single image
 * within a dataset. Here, information common to every
 * format (e.g. height, width, index information)
 * can be stored and retrieved in a standard way.
 * 
 * @author Mark Hiner
 */
public interface ImageMetadata extends HasMetaTable {

  /** Sets width (in pixels) of thumbnail planes in this image. */
  void setThumbSizeX(int thumbSizeX);

  /** Sets height (in pixels) of thumbnail planes in this image. */
  void setThumbSizeY(int thumbSizeY);

  /**
   * Sets the number of bytes per pixel.  Must be one of the <i>static</i>
   * pixel types (e.g. <code>INT8</code>) in {@link io.scif.util.FormatTools}.
   */
  void setPixelType(int pixelType);

  /** Sets the number of valid bits per pixel. */
  void setBitsPerPixel(int bitsPerPixel);

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

  /** 
   * Sets whether or not this image is a lower-resolution copy of
   * another image.
   */
  void setThumbnail(boolean thumbnail);

  /**
   * Convenience method to set both the axis types and lengths
   * for this ImageMetadata.
   */
  void setAxes(AxisType[] axisTypes, int[] axisLengths);

  /** 
   * Sets the Axes types for this image.
   * Order is implied by ordering within this array
   */
  void setAxisTypes(AxisType[] axisTypes);

  /**
   *  Sets the lengths of each axis. Order is parallel of axisTypes.
   *  <p>
   *  NB: axes must already exist for this method to be called.
   *  Use {@link #setAxisTypes(AxisType[])} or {@link #setAxes}
   */
  void setAxisLengths(int[] axisLengths);

  /** 
   * Sets the length for the specified axis. Adds the axis if
   * if its type is not already present in the image.
   */
  void setAxisLength(AxisType axis, int length);

  /** 
   * Sets the type of the axis at the specified index, if {@code axis} is not
   * already defined. Otherwise the axes are re-ordered, per
   * {@link java.util.List#add(int, Object)}.
   */
  void setAxisType(int index, AxisType axis);

  /** Sets the number of planes within this image. */
  void setPlaneCount(int planeCount);

  /** Returns the number of planes within this image. */
  int getPlaneCount();

  /** Returns the size, in bytes, of all planes in this image. */
  long getSize();

  /** Returns the width (in pixles) of the thumbnail planes in this image. */
  int getThumbSizeX();

  /** Returns the height (in pixles) of the thumbnail planes in this image. */
  int getThumbSizeY();

  /** 
   * Returns the number of bytes per pixel. Should correlate with the
   * pixel types in {@link io.scif.util.FormatTools}
   */
  int getPixelType();

  /** Returns the number of valid bits per pixel. */
  int getBitsPerPixel();

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

  /** 
   * Determines whether or not this image is a lower-resolution copy of
   * another image.
   * 
   * @return true if this image is a thumbnail
   */
  boolean isThumbnail();

  /**
   * Calculates the effective number of non-RGB channels in this image.
   * This value may be more useful than {@code getAxisLength(Axes.CHANNEL)}.
   * 
   * @return Count of non-RGB channels in this image.
   */
  int getEffectiveSizeC();

  /**
   * Calculates the number of RGB channels in this image.
   * 
   * @return Count of RGB channels in this image.
   */
  int getRGBChannelCount();

  /**
   * Gets the type of the (zero-indexed) specified plane.
   * 
   * @param axisIndex - index of the desired axis within this image
   * 
   * @return Type of the desired plane.
   */
  AxisType getAxisType(final int axisIndex);

  /**
   * Gets the length of the (zero-indexed) specified plane.
   * 
   * @param axisIndex - index of the desired axis within this image
   * 
   * @return Length of the desired plane.
   */
  int getAxisLength(final int axisIndex);

  /**
   * A convenience method for looking up the length of an axis
   * based on its type. No knowledge of plane ordering is necessary.
   * 
   * @param t - AxisType to look up
   * 
   * @return Length of axis t
   */
  int getAxisLength(final AxisType t);

  /**
   * Returns the array index for the specified AxisType. This index
   * can be used in other Axes methods for looking up lengths, etc...
   * <p>
   * This method can also be used as an existence check for the
   * target AxisType.
   * </p>
   * 
   * @param type - axis type to look up
   * @return The index of the desired axis or -1 if not found.
   */
  int getAxisIndex(final AxisType type);

  /**
   * Returns an array of the types for axes associated with
   * the specified image index. Order is consistent with the
   * axis length (int) array returned by {@link #getAxesLengths()}.
   * <p>
   * AxisType order is sorted and represents order within the image.
   * </p>
   * 
   * @return Sorted AxisType array
   */
  AxisType[] getAxes();

  /**
   * Returns an array of the lengths for axes associated with
   * the specified image index.
   * <p>
   * Ordering is consistent with the AxisType array returned by
   * {@link #getAxes()}.
   * </p>
   * 
   * @return Sorted axis length array
   */
  int[] getAxesLengths();

  /**
   * Appends the provided AxisType to the current AxisTypes, with a length of 0.
   * 
   * @param type - Type of the new axis
   */
  void addAxis(final AxisType type);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates a corresponding entry with the specified value in
   * axis lengths.
   * 
   * @param type - Type of the new axis
   * @param value - length of the new axis
   */
  void addAxis(final AxisType type, final int value);

  /**
   * @return A new copy of this ImageMetadata.
   */
  ImageMetadata copy();

  /**
   * Populates this ImageMetadata using the provided instance.
   * 
   * @param toCopy - ImageMetadata to copy
   */
  void copy(ImageMetadata toCopy);
}
