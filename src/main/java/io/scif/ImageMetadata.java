/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
 * Wisconsin-Madison
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
 * #L%
 */

package io.scif;

import org.scijava.Named;

import io.scif.util.FormatTools;
import net.imagej.axis.CalibratedAxis;
import net.imagej.interval.CalibratedInterval;

/**
 * ImageMetadata stores the metadata for a single image within a dataset. Here,
 * information common to every format (e.g. height, width, index information)
 * can be stored and retrieved in a standard way.
 *
 * @author Mark Hiner
 */
public interface ImageMetadata extends CalibratedInterval<CalibratedAxis>,
	Named, HasMetaTable
{

	// getInterval

	/** Sets width (in pixels) of thumbnail blocks in this image. */
		void setThumbSizeX(long thumbSizeX);

	/** Sets height (in pixels) of thumbnail blocks in this image. */
		void setThumbSizeY(long thumbSizeY);

	/**
	 * Sets the data type associated with a pixel. Valid pixel type constants
	 * (e.g., {@link FormatTools#INT8}) are enumerated in {@link FormatTools}.
	 */
		void setPixelType(int pixelType);

	/** Sets the number of valid bits per pixel. */
		void setBitsPerPixel(int bitsPerPixel);

	/**
	 * Sets whether or not we are confident that the dimension order is correct.
	 */
		void setOrderCertain(boolean orderCertain);

	/** Sets whether or not each pixel's bytes are in little endian order. */
		void setLittleEndian(boolean littleEndian);

	/**
	 * Sets whether or not the blocks are stored as indexed color. An indexed
	 * color image treats each pixel value as an index into a color table
	 * containing one or more (typically 3) actual values for the pixel.
	 */
		void setIndexed(boolean indexed);

	/** Sets whether or not we can ignore the color map (if present). */
		void setFalseColor(boolean falseColor);

	/**
	 * Sets whether or not we are confident that all of the metadata stored within
	 * the image has been parsed.
	 */
		void setMetadataComplete(boolean metadataComplete);

	/**
	 * Sets whether or not this image is a lower-resolution copy of another image.
	 */
		void setThumbnail(boolean thumbnail);

	/** Returns the size, in bytes, of this image. */
		long getSize();

	/** Returns the width (in pixels) of the thumbnail blocks in this image. */
		long getThumbSizeX();

	/** Returns the height (in pixels) of the thumbnail blocks in this image. */
		long getThumbSizeY();

	/**
	 * Returns the data type associated with a pixel. Valid pixel type constants
	 * (e.g., {@link FormatTools#INT8}) are enumerated in {@link FormatTools}.
	 */
		int getPixelType();

	/** Returns the number of valid bits per pixel. */
		int getBitsPerPixel();

	/**
	 * Returns true if we are confident that the dimension order is correct.
	 */
		boolean isOrderCertain();

	/** Returns true if each pixel's bytes are in little endian order. */
		boolean isLittleEndian();

	/** Returns true if the blocks are stored as indexed color. */
		boolean isIndexed();

	/** Returns true if we can ignore the color map (if present). */
		boolean isFalseColor();

	/**
	 * Returns true if we are confident that all of the metadata stored within the
	 * image has been parsed.
	 */
		boolean isMetadataComplete();

	/**
	 * Determines whether or not this image is a lower-resolution copy of another
	 * image.
	 *
	 * @return true if this image is a thumbnail
	 */
		boolean isThumbnail();

	/**
	 * @return the number of blocks in this image
	 */
		long getBlockCount();

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

	/**
	 * As
	 * {@link #populate(String,int, boolean, boolean, boolean, boolean, boolean)}
	 * but automatically determines bits per pixel.
	 */
		void populate(String name, int pixelType, boolean orderCertain,
			boolean littleEndian, boolean indexed, boolean falseColor,
			boolean metadataComplete);

	/**
	 * Convenience method for manually populating an ImageMetadata.
	 */
		void populate(String name, int pixelType, int bitsPerPixel,
			boolean orderCertain, boolean littleEndian, boolean indexed,
			boolean falseColor, boolean metadataComplete);
}
