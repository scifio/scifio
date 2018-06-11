/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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

import io.scif.codec.CodecOptions;
import io.scif.config.SCIFIOConfig;

import java.awt.image.ColorModel;
import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import net.imglib2.Interval;

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
	 * @param bounds bounds of the planar axes.
	 * @throws FormatException if one of the parameters is invalid.
	 * @throws IOException if there was a problem writing to the file.
	 */
	void savePlane(final int imageIndex, final long planeIndex, final Plane plane,
		final Interval bounds) throws FormatException, IOException;

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
	 * NB: resets any configuration on this writer.
	 *
	 * @param location The {@link Location} to where the image source will be
	 *          written.
	 */
	void setDest(Location location) throws FormatException, IOException;

	/**
	 * Sets the source that will be written to during {@link #savePlane} calls.
	 * NB: resets any configuration on this writer.
	 *
	 * @param handle The image source to write to.
	 */
	void setDest(DataHandle<Location> handle) throws FormatException, IOException;

	/**
	 * As {@link #setDest(Location)} with specification for source image index.
	 *
	 * @param location The {@link Location} of an image source to be written.
	 * @param imageIndex The index within the source that will be written.
	 */
	void setDest(Location location, int imageIndex) throws FormatException,
		IOException;

	/**
	 * As {@link #setDest(DataHandle)}, with specification for source image index.
	 *
	 * @param handle The image source to write to.
	 * @param imageIndex The index within the source that will be written.
	 */
	void setDest(DataHandle<Location> handle, int imageIndex)
		throws FormatException, IOException;

	/**
	 * As {@link #setDest(Location)} with specification for new configuration
	 * options.
	 *
	 * @param location The {@link Location} of an image source to be written.
	 * @param config Configuration information to use for this write.
	 */
	void setDest(Location location, SCIFIOConfig config) throws FormatException,
		IOException;

	/**
	 * As {@link #setDest(DataHandle)}, with specification for new configuration
	 * options.
	 *
	 * @param handle The image source to write to.
	 * @param config Configuration information to use for this write.
	 */
	void setDest(DataHandle<Location> handle, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * As {@link #setDest(Location, int)} with specification for new configuration
	 * options.
	 *
	 * @param location The {@link Location} of an image source to be written.
	 * @param imageIndex The index within the source that will be written.
	 * @param config Configuration information to use for this write.
	 */
	void setDest(Location location, int imageIndex, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * As {@link #setDest(DataHandle, int)}, with specification for new
	 * configuration options.
	 *
	 * @param handle The image source to write to.
	 * @param imageIndex The index within the source that will be written.
	 * @param config Configuration information to use for this write.
	 */
	void setDest(DataHandle<Location> handle, int imageIndex, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * Retrieves a reference to the output source that will be written to during
	 * {@link #savePlane} calls.
	 *
	 * @return The {@link DataHandle}} associated with this {@code Writer}.
	 */
	DataHandle<Location> getHandle();

	/**
	 * Sets the color model.
	 * <p>
	 * NB: the color model should be set before the output destination.
	 * </p>
	 */
	void setColorModel(ColorModel cm);

	/** Gets the color model. */
	ColorModel getColorModel();

	/** Gets the available compression types. */
	String[] getCompressionTypes();

	/** Gets the supported pixel types for the given codec. */
	int[] getPixelTypes(String codec);

	/** Checks if the given pixel type is supported. */
	boolean isSupportedType(int type, String codec);

	/** Checks if the given compression type is supported. */
	void isSupportedCompression(String compress) throws FormatException;

	/**
	 * @return True iff this writer is prepared to write the given plane of the
	 *         given image index.
	 */
	boolean isInitialized(int imageIndex, long planeIndex);

	/**
	 * @return The current compression being used by the writer, or null if not
	 *         set.
	 */
	String getCompression();

	/**
	 * @return The current frames per second being used by the writer.
	 */
	int getFramesPerSecond();

	/**
	 * @return {@link CodecOptions} used by this writer, if applicable.
	 */
	CodecOptions getCodecOptions();

	/**
	 * @return True if this writer should output planes sequentially.
	 */
	boolean writeSequential();

	/**
	 * @return Number of valid bits per pixel.
	 */
	int getValidBits();
}
