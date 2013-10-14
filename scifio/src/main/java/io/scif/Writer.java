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

import io.scif.codec.CodecOptions;
import io.scif.io.RandomAccessOutputStream;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

/**
 * Interface for all SCIFIO writers.
 * <p>
 * Writer components are used to save {@link io.scif.Plane} objects (output from
 * the {@link Reader#openPlane} methods) to a destination image location, via
 * the {@link #savePlane} methods.
 * </p>
 * 
 * @author Mark Hiner
 * @see io.scif.Plane
 * @see io.scif.Reader#openPlane
 */
public interface Writer extends HasFormat, HasSource {

	// -- Writer API methods --

	/**
	 * Saves the provided plane to the specified image and plane index of this
	 * {@code Writer's} destination image.
	 * 
	 * @param imageIndex the image index within the dataset.
	 * @param planeIndex the plane index within the image.
	 * @param plane the pixels save
	 * @throws FormatException if one of the parameters is invalid.
	 * @throws IOException if there was a problem writing to the file.
	 */
	void savePlane(final int imageIndex, final long planeIndex, Plane plane)
		throws FormatException, IOException;

	/**
	 * Saves the specified tile (sub-region) of the provided plane to the
	 * specified image and plane index of this {@code Writer's} destination image.
	 * 
	 * @param imageIndex the image index within the dataset.
	 * @param planeIndex the plane index within the image.
	 * @param plane the pixels save
	 * @param planeMin minimal bounds of the planar axes
	 * @param planeMax maximum bounds of the planar axes
	 * @throws FormatException if one of the parameters is invalid.
	 * @throws IOException if there was a problem writing to the file.
	 */
	void savePlane(final int imageIndex, final long planeIndex, final Plane plane,
		final long[] planeMin, final long[] planeMax) throws FormatException,
		IOException;

	/**
	 * @return True if this {@code Writer} can save multiple images to a single
	 *         file.
	 */
	boolean canDoStacks();

	/**
	 * Provides this {@code Writer} with a {@code Metadata} object to use when
	 * interpreting {@code Planes} during calls to {@link #savePlane}.
	 * <p>
	 * NB: This method has accepts a general {@code Metadata} so that this
	 * signature can appear in the base interface for all {@code Writers}, but
	 * behavior if provided with a {@code Metadata} instance not associated with
	 * this {@code Writer} is undefined and should throw an exception.
	 * </p>
	 * 
	 * @param meta The {@code Metadata} to associate with this {@code Writer}.
	 * @throws IllegalArgumentException If the provided {@code Metadata} type is
	 *           not compatible with this {@code Writer}.
	 */
	void setMetadata(Metadata meta) throws FormatException;

	/**
	 * Gets the {@code Metadata} that will be used when saving planes.
	 * 
	 * @return The {@code Metadata} associated with this {@code Writer}.
	 */
	Metadata getMetadata();

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param fileName The name of an image source to be written.
	 */
	void setDest(String fileName) throws FormatException, IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param file A file-based image source to write to.
	 */
	void setDest(File file) throws FormatException, IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param stream The image source to write to.
	 */
	void setDest(RandomAccessOutputStream stream) throws FormatException,
		IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param fileName The name of an image source to be written.
	 * @param imageIndex The index of the source to write to (default = 0)
	 */
	void setDest(String fileName, int imageIndex) throws FormatException,
		IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param file A file-based image source to write to.
	 * @param imageIndex The index of the source to write to (default = 0)
	 */
	void setDest(File file, int imageIndex) throws FormatException, IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * 
	 * @param stream The image source to write to.
	 * @param imageIndex The index of the source to write to (default = 0)
	 */
	void setDest(RandomAccessOutputStream stream, int imageIndex)
		throws FormatException, IOException;

	/**
	 * Retrieves a reference to the output source that will be written to during
	 * {@link #savePlane} calls.
	 * 
	 * @return The {@code RandomAccessOutputStream} associated with this
	 *         {@code Writer}.
	 */
	RandomAccessOutputStream getStream();

	/** Sets the color model. */
	void setColorModel(ColorModel cm);

	/** Gets the color model. */
	ColorModel getColorModel();

	/** Sets the frames per second to use when writing. */
	void setFramesPerSecond(int rate);

	/** Gets the frames per second to use when writing. */
	int getFramesPerSecond();

	/** Gets the available compression types. */
	String[] getCompressionTypes();

	/** Gets the supported pixel types. */
	int[] getPixelTypes();

	/** Gets the supported pixel types for the given codec. */
	int[] getPixelTypes(String codec);

	/** Checks if the given pixel type is supported. */
	boolean isSupportedType(int type);

	/** Sets the current compression type. */
	void setCompression(String compress) throws FormatException;

	/** Gets the current compression type. */
	String getCompression();

	/**
	 * Sets the codec options.
	 * 
	 * @param options The options to set.
	 */
	void setCodecOptions(CodecOptions options);

	/** Switch the output file for the current dataset. */
	void changeOutputFile(String id) throws FormatException, IOException;

	/**
	 * Sets whether or not we know that planes will be written sequentially. If
	 * planes are written sequentially and this flag is set, then performance will
	 * be slightly improved.
	 */
	void setWriteSequentially(boolean sequential);
}
