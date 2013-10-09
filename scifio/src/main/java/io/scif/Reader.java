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

import java.io.File;
import java.io.IOException;

/**
 * Interface for all SCIFIO Readers.
 * <p>
 * {@code Reader} components generate {@link io.scif.Plane} representations of
 * images via the {@link #openPlane} methods. These planes can then be used by
 * calling software (e.g. for display) or passed to another method for writing
 * to an output source (e.g. via the {@link io.scif.Writer#savePlane} methods).
 * </p>
 * <p>
 * Before a {@code Reader} can be used, it must be initialized via
 * {@link #setSource} and {@link #setMetadata} calls.
 * </p>
 * 
 * @see io.scif.Plane
 * @see io.scif.Metadata
 * @see io.scif.Writer#savePlane
 * @author Mark Hiner
 */
public interface Reader extends HasFormat, HasSource, Groupable {

	// -- Reader API methods --

	/**
	 * Creates a {@link io.scif.Plane} representation of the pixels at the
	 * specified indices.
	 * 
	 * @param imageIndex the image index within the dataset.
	 * @param planeIndex the plane index within the image.
	 * @return The complete {@code Plane} at the specified indices.
	 */
	Plane openPlane(int imageIndex, int planeIndex) throws FormatException,
		IOException;

	/**
	 * Creates a {@link io.scif.Plane} representation of a desired sub-region from
	 * the pixels at the specified indices.
	 * 
	 * @param imageIndex the image index within the dataset.
	 * @param planeIndex the plane index within the image.
	 * @param planeMin minimal bounds of the planar axes
	 * @param planeMax maximum bounds of the planar axes
	 * @return The desired sub-region at the specified indices.
	 */
	Plane openPlane(int imageIndex, int planeIndex, long[] planeMin,
		long[] planeMax) throws FormatException, IOException; 

	/**
	 * Allows a single {@code Plane} object to be reused by reference when opening
	 * complete planes.
	 * 
	 * @see #openPlane(int, int)
	 * @throws IllegalArgumentException If the provided {@code Plane} type is not
	 *           compatible with this {@code Reader}.
	 */
	Plane openPlane(int imageIndex, int planeIndex, Plane plane)
		throws FormatException, IOException;

	/**
	 * Allows a single {@code Plane} object to be reused by reference when opening
	 * sub-regions of planes.
	 * 
	 * @see #openPlane(int, int, int, int, int, int)
	 * @throws IllegalArgumentException If the provided {@code Plane} type is not
	 *           compatible with this {@code Reader}.
	 */
	Plane openPlane(int imageIndex, int planeIndex, Plane plane,
		long[] planeMin, long[] planeMax) throws FormatException, IOException; 

	/**
	 * Obtains a thumbnail version of the {@code Plane} at the specified image and
	 * plane indices.
	 * 
	 * @param imageIndex the image index within the dataset.
	 * @param planeIndex the plane index within the image.
	 * @return A thumbnail version of the {@code Plane} at the specified indices.
	 */
	Plane openThumbPlane(int imageIndex, int planeIndex) throws FormatException,
		IOException;

	/** Returns the current file. */
	String getCurrentFile();

	/** Returns the list of domains represented by the current file. */
	String[] getDomains();

	/**
	 * Retrieves the current input stream for this reader.
	 * 
	 * @return A RandomAccessInputStream
	 */
	RandomAccessInputStream getStream();

	/**
	 * Retrieves all underlying readers. Returns null if there are no underlying
	 * readers.
	 */
	Reader[] getUnderlyingReaders();

	/** Returns the optimal sub-image width for use with {@link #openPlane}. */
	long getOptimalTileWidth(int imageIndex);

	/** Returns the optimal sub-image height for use with {@link #openPlane}. */
	long getOptimalTileHeight(int imageIndex);

	/**
	 * Sets the Metadata for this Reader.
	 * <p>
	 * NB: This method has accepts a general {@code Metadata} so that this
	 * signature can appear in the base interface for all {@code Readers}, but
	 * behavior if provided with a {@code Metadata} instance not associated with
	 * this {@code Reader} is undefined and should throw an exception.
	 * </p>
	 * 
	 * @throws IllegalArgumentException If the provided {@code Metadata} type is
	 *           not compatible with this {@code Reader}.
	 */
	void setMetadata(Metadata meta) throws IOException;

	/** Gets the type-specific Metadata for this Reader */
	Metadata getMetadata();

	// TODO remove normalization methods
	/** Specifies whether or not to normalize float data. */
	void setNormalized(boolean normalize);

	/** Returns true if we should normalize float data. */
	boolean isNormalized();

	/** Returns true if this format supports multi-file datasets. */
	@Override
	boolean hasCompanionFiles();

	/**
	 * Sets the source for this reader to read from.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	void setSource(String fileName) throws IOException;

	/**
	 * Sets the source for this reader to read from.
	 * 
	 * @param file
	 * @throws IOException
	 */
	void setSource(File file) throws IOException;

	/**
	 * Sets the source for this reader to read from.
	 * 
	 * @param stream - The stream to read from
	 */
	void setSource(RandomAccessInputStream stream) throws IOException;

	/**
	 * Reads a raw plane from disk.
	 * 
	 * @throws IllegalArgumentException If the provided {@code Plane} type is not
	 *           compatible with this {@code Reader}.
	 */
	Plane readPlane(RandomAccessInputStream s, int imageIndex, long[] planeMin,
		long[] planeMax, Plane plane) throws IOException;

	/**
	 * Reads a raw plane from disk.
	 * 
	 * @throws IllegalArgumentException If the provided {@code Plane} type is not
	 *           compatible with this {@code Reader}.
	 */
	Plane readPlane(RandomAccessInputStream s, int imageIndex, long[] planeMin,
		long[] planeMax, int scanlinePad, Plane plane) throws IOException;

	/** Determines the number of planes in the current file. */
	int getPlaneCount(int imageIndex);

	/** Determines the number of images in the current file. */
	int getImageCount();

	/**
	 * Creates a blank plane compatible with this reader.
	 * 
	 * @param planeOffsets minimal offsets of the planar axes
	 * @param planeBounds maximum values of the planar axes
	 * @return The created plane
	 */
	Plane createPlane(long[] planeOffsets, long[] planeBounds);

	/**
	 * Creates a blank plane compatible with this reader.
	 * 
	 * @param meta - ImageMetadata to use to populate the new plane.
	 * @param planeOffsets minimal offsets of the planar axes
	 * @param planeBounds maximum values of the planar axes
	 * @return The created plane
	 */
	Plane createPlane(ImageMetadata meta, long[] planeMin, long[] planeMax);

	/**
	 * Convenience method for casting {@code Plane} implementations to the type
	 * associated with this {@code Reader}.
	 * <p>
	 * NB: this method will fail if the provided {@code Plane} is not compatible
	 * with this {@code Reader}.
	 * </p>
	 * 
	 * @param P The specific {@code Plane} implementation to return.
	 * @param plane - The base {@link io.scif.Plane} to cast.
	 * @return The {@code Plane} argument cast to {@code P}.
	 * @throws IllegalArgumentException If the provided {@code Plane} type is not
	 *           compatible with this {@code Reader}.
	 */
	<P extends Plane> P castToTypedPlane(Plane plane);
}
