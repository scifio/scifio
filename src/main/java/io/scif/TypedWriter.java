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

import org.scijava.io.location.Location;

/**
 * Interface for all {@link io.scif.Writer} implementations that use generic
 * parameters.
 * <p>
 * Generics all each concrete {@code Writer} implementation to guarantee, by
 * type narrowing, any methods that return SCIFIO components. Also, parallel
 * methods that take SCIFIO component arguments are defined to be type narrowed.
 * </p>
 *
 * @author Mark Hiner
 * @param <M> The {@link io.scif.Metadata} type associated with this Writer.
 */
public interface TypedWriter<M extends TypedMetadata> extends Writer {

	/**
	 * Generic-parameterized {@code setMetadata} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Writer#setMetadata(Metadata)}.
	 *
	 * @see io.scif.Parser#parse(Location, Metadata)
	 */
	void setMetadata(M meta) throws FormatException;

	@Override
	M getMetadata();

}
