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

import java.io.IOException;

/**
 * Interface for all SCIFIO Checker components.
 * <p>
 * {@code Checker} components are used to determine if the {@code Format} they
 * are associated with is compatibile with a given image. This is accomplished
 * via the {@link #isFormat} methods.
 * </p>
 * 
 * @see io.scif.Format
 * @see io.scif.HasFormat
 * @author Mark Hiner
 */
public interface Checker extends HasFormat {

	// -- Checker API methods --

	/**
	 * Checks if the provided image source is compatible with this {@code Format}.
	 * Will not open the source during this process.
	 * 
	 * @param name path to the image source to check.
	 * @return True if the image source is compatible with this {@code Format}.
	 */
	boolean isFormat(String name);

	/**
	 * Checks if the provided image source is compatible with this {@code Format}.
	 * <p>
	 * If {@code open} is true and the source name is insufficient to determine
	 * the image type, the source may be opened for further analysis, or other
	 * relatively expensive file system operations (such as file existence tests
	 * and directory listings) may be performed.
	 * </p>
	 * 
	 * @param name path to the image source to check.
	 * @param open if true, allows file access during the checking process.
	 * @return True if the image source is compatible with this {@code Format}.
	 */
	boolean isFormat(String name, boolean open);

	/**
	 * Checks if the given stream is a valid stream for this {@code Format}.
	 * 
	 * @param stream the image source to check.
	 * @return True if {@code stream} is compatible with this {@code Format}.
	 * @throws IOException
	 */
	boolean isFormat(RandomAccessInputStream stream) throws IOException;

	/**
	 * Checks if the given bytes are a valid header for this {@code Format}.
	 * 
	 * @param block the byte array to check.
	 * @return True if {@code block} is compatible with this {@code Format}.
	 */
	boolean checkHeader(byte[] block);
}
