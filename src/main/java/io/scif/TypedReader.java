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

import java.io.IOException;

import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;

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
 * @param <B> - {@link io.scif.Block} return and parameter type for this
 *          reader's {@link #openBlock} and {@link #readBlock} methods.
 * @see #openBlock
 * @see #readBlock
 * @see #setMetadata
 * @see #getMetadata
 */
public interface TypedReader<M extends TypedMetadata, T extends NativeType<T>, B extends TypedBlock<T, ?>>
	extends Reader
{

	@Override
		B openBlock(int imageIndex, long blockIndex) throws FormatException,
			IOException;

	@Override
		B openBlock(int imageIndex, long blockIndex, Block block)
			throws FormatException, IOException;

	/**
	 * Version of {@link #openBlock(int, long, Block)} with type-narrowed input
	 * parameter.
	 */
		B openBlock(int imageIndex, long blockIndex, B block)
			throws FormatException, IOException;

	@Override
		B openBlock(int imageIndex, long blockIndex, SCIFIOConfig config)
			throws FormatException, IOException;

	@Override
		B openBlock(int imageIndex, long blockIndex, Block block,
			SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Version of {@link #openBlock(int, long, Block, SCIFIOConfig)} with
	 * type-narrowed input parameter.
	 */
		B openBlock(int imageIndex, long blockIndex, B block, SCIFIOConfig config)
			throws FormatException, IOException;

	@Override
		B openRegion(int imageIndex, Interval pos, Interval range)
			throws FormatException, IOException;

	@Override
		B openRegion(int imageIndex, Interval pos, Interval range, Block block)
			throws FormatException, IOException;

	/**
	 * Version of {@link #openRegion(int, Interval, Interval, Block)} with
	 * type-narrowed input parameter.
	 */
		B openRegion(int imageIndex, Interval pos, Interval range, B block)
			throws FormatException, IOException;

	@Override
		B openRegion(int imageIndex, Interval pos, Interval range,
			SCIFIOConfig config) throws FormatException, IOException;

	@Override
		B openRegion(int imageIndex, Interval pos, Interval range, Block block,
			SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * Version of
	 * {@link #openRegion(int, Interval, Interval, Block, SCIFIOConfig)} with
	 * type-narrowed input parameter.
	 */
		B openRegion(int imageIndex, Interval pos, Interval range, B block,
			SCIFIOConfig config) throws FormatException, IOException;

	@Override
		B openThumbBlock(int imageIndex, long blockIndex) throws FormatException,
			IOException;

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
	 * Generic-parameterized {@code readBlock} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#readBlock(RandomAccessInputStream, int, Interval, Interval, Block)}
	 *
	 * @see io.scif.Reader#readBlock(RandomAccessInputStream, int, Interval,
	 *      Interval, Block)
	 */
		B readBlock(RandomAccessInputStream s, int imageIndex, Interval pos,
			Interval range, B block) throws IOException;

	/**
	 * Generic-parameterized {@code readBlock} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#readBlock(RandomAccessInputStream, int, Interval, Interval, int, Block)}
	 *
	 * @see io.scif.Reader#readBlock(RandomAccessInputStream, int, Interval,
	 *      Interval, int, Block)
	 */
		B readBlock(RandomAccessInputStream s, int imageIndex, Interval pos,
			Interval range, int scanlinePad, B block) throws IOException;

	@Override
		B createBlock(Interval extents);

	/**
	 * Returns the class of {@code Blocks} associated with this {@code Reader}.
	 */
		Class<B> getBlockClass();

}
