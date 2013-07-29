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
 * Interface for all {@link io.scif.Parser} implementations that use generic
 * parameters.
 * <p>
 * Generics allow each concrete {@code Parser} implementation to type narrow the
 * return the type of {@code Metadata} from its {@link #Parse} methods, as well
 * as the argument {@code Metadata} types for the same methods.
 * </p>
 * 
 * @author Mark Hiner
 * @param <M> The {@link io.scif.Metadata} type that will be returned by this
 *          {@code Parser}.
 */
public interface TypedParser<M extends TypedMetadata> extends Parser {

	/*
	 * @see io.scif.Parser#parse(java.lang.String)
	 */
	M parse(String fileName) throws IOException, FormatException;

	/*
	 * @see io.scif.Parser#parse(java.io.File)
	 */
	M parse(File file) throws IOException, FormatException;

	/*
	 * @see io.scif.Parser#parse(io.scif.io.RandomAccessInputStream)
	 */
	M parse(RandomAccessInputStream stream) throws IOException, FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(String, Metadata)}.
	 * 
	 * @see io.scif.Parser#parse(String, Metadata)
	 */
	M parse(String fileName, M meta) throws IOException, FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(File, Metadata)}.
	 * 
	 * @see io.scif.Parser#parse(File, Metadata)
	 */
	M parse(File file, M meta) throws IOException, FormatException;

	/**
	 * Generic-parameterized {@code parse} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Parser#parse(RandomAccessInputStream, Metadata)}.
	 * 
	 * @see io.scif.Parser#parse(RandomAccessInputStream, Metadata)
	 */
	M parse(RandomAccessInputStream stream, M meta) throws IOException,
		FormatException;

}
