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

import io.scif.io.RandomAccessInputStream;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for all SCIFIO Metadata objects. Based on the format, a Metadata
 * object can be a single N-dimensional collection of bytes (an image) or a list
 * of multiple images.
 * <p>
 * NB: When defining a new Metadata implementation, you will likely want to
 * create a corresponding {@link io.scif.Format} implementation. Also, at a
 * minimum, a {@link io.scif.Translator} should be implemented that can convert
 * between {@link io.scif.Metadata} and the new Metadata.
 * </p>
 *
 * @see io.scif.Translator
 * @see io.scif.AbstractFormat
 */
public interface Metadata extends Serializable, HasFormat, HasSource,
	HasMetaTable
{

	// -- Metadata API --

	/**
	 * Sets the input source attached to this Metadata object. Note that calling
	 * this method does not affect the structure of this Metadata object.
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
	 * Returns whether or not filterMetadata was set when parsing this Metadata
	 * object.
	 *
	 * @return True if Metadata was filtered when parsing
	 */
	boolean isFiltered();

	/**
	 * Generates format-agnostic image metadata structures based on this
	 * instance's format-specific metadata.
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

	/** Returns the size, in bytes, of the current dataset. */
	long getDatasetSize();

	/**
	 * Sets the name for this dataset.
	 *
	 * @param name - the dataset name
	 */
	void setDatasetName(String name);

	/** Sets whether this metadata was filtered when parsed. */
	void setFiltered(boolean filtered);

	/**
	 * Adds the provided image metadata to this dataset metadata
	 */
	void add(final ImageMetadata meta);

	/**
	 * Creates the specified number of blank ImageMetadata.
	 *
	 * @param imageCount - Number of ImageMetadata to create.
	 */
	void createImageMetadata(int imageCount);
}
