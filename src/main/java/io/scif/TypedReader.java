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

import io.scif.config.SCIFIOConfig;

import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import net.imglib2.Interval;

/**
 * Interface for all {@link io.scif.Reader} implementations that use generic
 * parameters.
 * <p>
 * Generics are used in {@code Reader} concrete implementations to type narrow
 * return types, and to provide parallel methods that can type narrow arguments.
 * </p>
 *
 * @author Mark Hiner
 * @param <M> - {@link io.scif.Metadata} used by this reader for reading images.
 * @param <P> - {@link io.scif.Plane} return and parameter type for this
 *          reader's {@link #openPlane} and {@link #readPlane} methods.
 * @see #openPlane
 * @see #readPlane
 * @see #setMetadata
 * @see #getMetadata
 */
public interface TypedReader<M extends TypedMetadata, P extends DataPlane<?>>
	extends Reader
{

	@Override
	P openPlane(int imageIndex, long planeIndex) throws FormatException,
		IOException;

	@Override
	P openPlane(int imageIndex, long planeIndex, Interval bounds)
		throws FormatException, IOException;

	/**
	 * Generic-parameterized {@code openPlane} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#openPlane(int, long, Plane)}.
	 *
	 * @see io.scif.Reader#openPlane(int, long, Plane)
	 */
	P openPlane(int imageIndex, long planeIndex, P plane) throws FormatException,
		IOException;

	/**
	 * Generic-parameterized {@code openPlane} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#openPlane(int, long, Plane, Interval)}.
	 *
	 * @see io.scif.Reader#openPlane(int, long, Plane, Interval)
	 */
	P openPlane(int imageIndex, long planeIndex, P plane, Interval bounds)
		throws FormatException, IOException;

	@Override
	P openPlane(int imageIndex, long planeIndex, SCIFIOConfig config)
		throws FormatException, IOException;

	@Override
	P openPlane(int imageIndex, long planeIndex, Interval bounds,
		SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * @see io.scif.TypedReader#openPlane(int, long, DataPlane)
	 */
	P openPlane(int imageIndex, long planeIndex, P plane, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * @see io.scif.TypedReader#openPlane(int, long, DataPlane, Interval)
	 */
	P openPlane(int imageIndex, long planeIndex, P plane, Interval bounds,
		SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Generic-parameterized {@code setMetadata} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#setMetadata(Metadata)}.
	 *
	 * @see io.scif.Reader#setMetadata(Metadata)
	 */
	void setMetadata(M meta) throws IOException;

	@Override
	M getMetadata();

	/**
	 * Generic-parameterized {@code readPlane} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#readPlane(DataHandle, int, long[], long[], Plane)}
	 * <p>
	 * NB Presumes that the source stream {@code s} is set to the correct offset,
	 * i.e. start of the plane
	 * </p>
	 *
	 * @see io.scif.Reader#readPlane(DataHandle, int, Interval, Plane)
	 */
	P readPlane(DataHandle<Location> s, int imageIndex, Interval bounds,
		P plane) throws IOException;

	/**
	 * Generic-parameterized {@code readPlane} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#readPlane(DataHandle, int, Interval, int, Plane)}
	 * <p>
	 * NB Presumes that the source stream {@code s} is set to the correct offset,
	 * i.e. start of the plane
	 * </p>
	 *
	 * @see io.scif.Reader#readPlane(RandomAccessInputStream, int, Interval,
	 *      int, Plane)
	 */
	P readPlane(DataHandle<Location> s, int imageIndex, Interval bounds,
		int scanlinePad, P plane) throws IOException;

	@Override
	P createPlane(Interval bounds);

	/**
	 * Returns the class of {@code Planes} associated with this {@code Reader}.
	 */
	Class<P> getPlaneClass();

}
