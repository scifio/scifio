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

import io.scif.io.RandomAccessInputStream;

import java.io.Serializable;
import java.util.List;

import net.imglib2.meta.AxisType;

/**
 * Interface for all SCIFIO Metadata objects.
 * Based on the format, a Metadata object can
 * be a single N-dimensional collection of bytes
 * (an image) or a list of multiple images.
 * <p>
 * NB: When defining a new Metadata implementation, you will likely want to
 * create a corresponding {@link io.scif.Format} implementation. Also,
 * at a minimum, a {@link io.scif.Translator} should be implemented
 * that can convert between {@link io.scif.Metadata} and the new
 * Metadata.
 * </p>
 *
 * @see io.scif.Translator
 * @see io.scif.AbstractFormat
 * 
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Metadata extends Serializable, HasFormat, HasSource,
  HasMetaTable
{

  // -- Static Constents --

  /**
   * String representation of this classes package-qualified name.
   * <p>
   * Necessary for dynamic use annotations. All Metadata implementations should
   * override this constant.
   * </p>
   */
  public static final String CNAME = "io.scif.Metadata";

  // -- Metadata API --

  /**
   * Sets the input source attached to this Metadata object.
   * Note that calling this method does not affect the structure
   * of this Metadata object.
   * 
   * @param in - Input source for this Metadata
   */
  void setSource(RandomAccessInputStream in);

  /**
   * Returns the source used to generate this Metadata object.
   * 
   * @return - The associated RandomAccessInputStream
   */
  RandomAccessInputStream getSource();

  /**
   * Returns whether or not filterMetadata was set when parsing this
   * Metadata object.
   * 
   * @return True if Metadata was filtered when parsing
   */
  boolean isFiltered();

  /**
   * Returns the MetadataOptions object that was used to parse this
   * Metadata.
   * 
   * @return The MetadataOptions used to parse this Metadata
   */
  MetadataOptions getMetadataOptions();

  /**
   * Generates format-agnostic image metadata structures
   * based on this instance's format-specific metadata.
   */
  void populateImageMetadata();

  // -- Format-agnostic Metadata API Methods --

  /** Returns a String representation of this Dataset's name */
  String getDatasetName();

  /** Returns the ImageMetadata at the specified image within this dataset. */
  ImageMetadata get(int imageIndex);

  /** Returns the list of ImageMetadata associated with this dataset. */
  List<ImageMetadata> getAll();

  /** Returns the number of images in this dataset. */
  int getImageCount();

  /** Returns the number of planes in the specified image. */
  int getPlaneCount(int imageIndex);

  /** Returns the size, in bytes, of the current dataset. */
  long getDatasetSize();

  /** Returns the size, in bytes, of the specified image. */
  long getImageSize(int imageIndex);

  /** 
   * Returns true if the spcified image stores its channels RGBRGBRGB... and
   * false if channels are stored RRR...GGG...BBB...
   */
  boolean isInterleaved(int imageIndex);

  /** 
   * Returns the number of bytes per pixel for the specified image.
   * Should correlate with the pixel types in {@link io.scif.util.FormatTools}
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

  /** Returns the width (in pixles) of the thumbnail planes in the specified image. */
  int getThumbSizeX(int imageIndex);

  /** Returns the height (in pixles) of the thumbnail planes in the specified image. */
  int getThumbSizeY(int imageIndex);

  /**
   * Returns the number of axes (planes) in the
   * specified image.
   * 
   * @param imageIndex - index for multi-image files
   * @return The axis count
   */
  int getAxisCount(int imageIndex);

  /**
   * Gets the type of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param axisIndex - index of the desired axis within the specified image
   * @return Type of the desired plane.
   */
  AxisType getAxisType(int imageIndex, int axisIndex);

  /**
   * Gets the length of the (zero-indexed) specified plane.
   * 
   * @param imageIndex - index for multi-image files
   * @param axisIndex - index of the desired axis within the specified image
   * @return Length of the desired plane.
   */
  int getAxisLength(int imageIndex, int axisIndex);

  /**
   * A convenience method for looking up the length of an axis
   * based on its type. No knowledge of plane ordering is necessary.
   * 
   * @param imageIndex - index for multi-image files
   * @param t - desired axis type
   * @return Length of axis t
   */
  int getAxisLength(int imageIndex, AxisType t);

  /**
   * Returns the array index for the specified AxisType. This index
   * can be used in other Axes methods for looking up lengths, etc...
   * <p>
   * This method can also be used as an existence check for the
   * target AxisType.
   * </p>
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
   * {@link #getAxesLengths(int)}.
   * <p>
   * AxisType order is sorted and represents order within the image.
   * </p>
   * 
   * @param imageIndex - index for multi-image sources
   * @return An array of AxisTypes in the order they appear.
   */
  AxisType[] getAxes(int imageIndex);

  /**
   * Returns an array of the lengths for axes associated with
   * the specified image index.
   * <p>
   * Ordering is consistent with the
   * AxisType array returned by {@link #getAxes(int)}.
   * </p>
   * 
   * @param imageIndex - index for multi-image sources
   * @return Sorted axis length array
   */
  int[] getAxesLengths(int imageIndex);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates corresponding length = 0 entry in the axis lengths
   * array.
   * 
   * @param imageIndex - index for multi-image sources
   * @param type - Type of the new axis
   */
  void addAxis(int imageIndex, AxisType type);

  /**
   * Appends the provided AxisType to the current AxisType array
   * and creates a corresponding entry with the specified value in
   * axis lengths.
   * 
   * @param imageIndex - index for multi-image sources
   * @param type - Type of the new axis
   * @param value - Value of the new axis
   */
  void addAxis(int imageIndex, AxisType type, int value);

  /**
   * Returns true if we are confident that the
   * dimension order is correct for the specified image.
   * 
   * @param imageIndex - index for multi-image sources
   */
  boolean isOrderCertain(int imageIndex);

  /** 
   * Returns true if the specified image is a lower-resolution copy of
   * another image.
   * 
   * @param imageIndex - index for multi-image sources
   */
  boolean isThumbnailImage(int imageIndex);

  /**
   * Returns true if we are confident that all of the metadata stored
   * within the specified image has been parsed.
   * 
   * @param imageIndex - index for multi-image sources
   */
  boolean isMetadataComplete(int imageIndex);

  /**
   * Sets the name for this dataset.
   * 
   * @param name - the dataset name
   */
  void setDatasetName(String name);

  /** Sets width (in pixels) of thumbnail planes for the specified image. */
  void setThumbSizeX(int imageIndex, int thumbX);

  /** Sets height (in pixels) of thumbnail planes for the specified image. */
  void setThumbSizeY(int imageIndex, int thumbY);

  /**
   * Sets the number of bytes per pixel for the specified image.
   * Must be one of the <i>static</i>
   * pixel types (e.g. <code>INT8</code>) in {@link io.scif.util.FormatTools}.
   */
  void setPixelType(int imageIndex, int type);

  /** Sets the number of valid bits per pixel for the specified image. */
  void setBitsPerPixel(int imageIndex, int bpp);

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

  /** Sets whether this metadata was filtered when parsed. */
  void setFiltered(boolean filtered);

  /** Sets the metadata options that were used during parsing. */
  void setMetadataOptions(MetadataOptions opts);

  /**
   * Sets whether or not we are confident that all of the metadata stored
   * within the specified image has been parsed.
   */
  void setMetadataComplete(int imageIndex, boolean metadataComplete);

  /**
   * Adds the provided image metadata to this dataset metadata
   */
  void add(final ImageMetadata meta);

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

  /**
   * Creates the specified number of blank ImageMetadata.
   * 
   * @param imageCount - Number of ImageMetadata to create.
   */
  void createImageMetadata(int imageCount);
}
