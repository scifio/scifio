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

import java.io.File;
import java.io.IOException;

import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import net.imglib2.Interval;

/**
 * Interface for all SCIFIO Readers.
 * <p>
 * {@code Reader} components generate {@link io.scif.Block} representations of
 * images via the {@link #openBlock} methods. These blocks can then be used by
 * calling software (e.g. for display) or passed to another method for writing
 * to an output source (e.g. via the {@link io.scif.Writer#saveBlock} methods).
 * </p>
 * <p>
 * Before a {@code Reader} can be used, it must be initialized via
 * {@link #setSource} and {@link #setMetadata} calls.
 * </p>
 *
 * @see io.scif.Block
 * @see io.scif.Metadata
 * @see io.scif.Writer#saveBlock
 * @author Mark Hiner
 */
public interface Reader extends HasFormat, HasSource, Groupable {

	// -- Reader API methods --

	/**
	 * Creates a {@link io.scif.Block} representation of the pixels at the
	 * specified indices.
	 *
	 * @param imageIndex the image index within the dataset.
	 * @param blockIndex the block index within the image.
	 * @return The complete {@code Block} at the specified indices.
	 */
		Block openBlock(int imageIndex, long blockIndex) throws FormatException,
			IOException;

	/**
	 * Allows a single {@code Block} object to be reused by reference when opening
	 * complete blocks.
	 *
	 * @see #openBlock(int, long)
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block openBlock(int imageIndex, long blockIndex, Block block)
			throws FormatException, IOException;

	/**
	 * As {@link #openBlock(int, long)} with configuration options.
	 *
	 * @see #openBlock(int, long)
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block openBlock(int imageIndex, long blockIndex, SCIFIOConfig config)
			throws FormatException, IOException;

	/**
	 * Allows a single {@code Block} object to be reused by reference when opening
	 * complete blocks.
	 *
	 * @see #openBlock(int, long, SCIFIOConfig)
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block openBlock(int imageIndex, long blockIndex, Block block,
			SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Creates a {@link io.scif.Block} representation of a desired sub-region from
	 * the pixels at the specified indices.
	 *
	 * @param imageIndex the image index within the dataset.
	 * @param pos starting position (offsets) of the range to open
	 * @param range the extents of the range to open
	 * @return The desired sub-region at the specified indices.
	 */
		Block openRegion(int imageIndex, Interval pos, Interval range)
			throws FormatException, IOException;

	/**
	 * Allows a single {@code Block} object to be reused by reference when opening
	 * sub-regions of blocks.
	 *
	 * @see #openRegion(int, Interval, Interval)
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block openRegion(int imageIndex, Interval pos, Interval range, Block block)
			throws FormatException, IOException;

	/**
	 * As {@link #openRegion(int, Interval, Interval)} with configuration options.
	 *
	 * @see #openRegion(int, Interval, Interval)
	 * @return The desired sub-region at the specified indices.
	 */
		Block openRegion(int imageIndex, Interval pos, Interval range,
			SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Allows a single {@code Block} object to be reused by reference when opening
	 * sub-regions of blocks.
	 *
	 * @see #openRegion(int, Interval, Interval)
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block openRegion(int imageIndex, Interval pos, Interval range, Block block,
			SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Obtains a thumbnail version of the {@code Block} at the specified image and
	 * block indices.
	 *
	 * @param imageIndex the image index within the dataset.
	 * @param blockIndex the block index within the image.
	 * @return A thumbnail version of the {@code Block} at the specified indices.
	 */
		Block openThumbBlock(int imageIndex, long blockIndex)
			throws FormatException, IOException;

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

	/** Returns the optimal sub-image extents for use with {@link #openBlock}. */
		Interval getOptimalBlockSize(int imageIndex);

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
	 * As {@link #setSource(String)} with configuration options.
	 *
	 * @param fileName
	 * @param config Configuration information to use for this read.
	 * @throws IOException
	 */
		void setSource(String fileName, SCIFIOConfig config) throws IOException;

	/**
	 * As {@link #setSource(File)} with configuration options.
	 *
	 * @param file
	 * @param config Configuration information to use for this read.
	 * @throws IOException
	 */
		void setSource(File file, SCIFIOConfig config) throws IOException;

	/**
	 * As {@link #setSource(RandomAccessInputStream)} with configuration options.
	 *
	 * @param stream - The stream to read from
	 * @param config Configuration information to use for this read.
	 */
		void setSource(RandomAccessInputStream stream, SCIFIOConfig config)
			throws IOException;

	/**
	 * Reads a raw block from disk.
	 *
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block readBlock(RandomAccessInputStream s, int imageIndex, long[] blockMin,
			long[] blockMax, Block block) throws IOException;

	/**
	 * Reads a raw block from disk.
	 *
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
		Block readBlock(RandomAccessInputStream s, int imageIndex, long[] blockMin,
			long[] blockMax, int scanlinePad, Block block) throws IOException;

	/** Determines the number of blocks in the current file. */
		long getBlockCount(int imageIndex);

	/** Determines the number of images in the current file. */
		int getImageCount();

	/**
	 * Creates a blank block compatible with this reader.
	 *
	 *@param extents The extents of the new block
	 *
	 * @return The created block
	 */
		Block createBlock(Interval extents);

	/**
	 * Convenience method for casting {@code Block} implementations to the type
	 * associated with this {@code Reader}.
	 * <p>
	 * NB: this method will fail if the provided {@code Block} is not compatible
	 * with this {@code Reader}.
	 * </p>
	 *
	 * @param block - The base {@link io.scif.Block} to cast.
	 * @return The {@code Block} argument cast to {@code P}.
	 * @throws IllegalArgumentException If the provided {@code Block} type is not
	 *           compatible with this {@code Reader}.
	 */
	<P extends Block> P castToTypedBlock(Block block);
}
