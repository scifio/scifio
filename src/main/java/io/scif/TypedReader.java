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

import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import net.imglib2.type.NativeType;

import java.io.IOException;

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
		B
		openBlock(int imageIndex, long blockIndex, long[] blockMin, long[] blockMax)
			throws FormatException, IOException;

	/**
	 * Generic-parameterized {@code openBlock} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#openBlock(int, long, Block)}.
	 *
	 * @see io.scif.Reader#openBlock(int, long, Block)
	 */
	B openBlock(int imageIndex, long blockIndex, B block) throws FormatException,
		IOException;

	/**
	 * Generic-parameterized {@code openBlock} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#openBlock(int, long, Block, long[], long[])}.
	 *
	 * @see io.scif.Reader#openBlock(int, long, Block, long[], long[])
	 */
	B openBlock(int imageIndex, long blockIndex, B block, long[] blockMin,
		long[] blockMax) throws FormatException, IOException;

	@Override
	B openBlock(int imageIndex, long blockIndex, SCIFIOConfig config)
		throws FormatException, IOException;

	@Override
	B openBlock(int imageIndex, long blockIndex, long[] blockMin,
		long[] blockMax, SCIFIOConfig config) throws FormatException, IOException;

	/**
	 * @see io.scif.TypedReader#openBlock(int, long, DataBlock)
	 */
	B openBlock(int imageIndex, long blockIndex, B block, SCIFIOConfig config)
		throws FormatException, IOException;

	/**
	 * @see io.scif.TypedReader#openBlock(int, long, DataBlock, long[], long[])
	 */
	B openBlock(int imageIndex, long blockIndex, B block, long[] blockMin,
		long[] blockMax, SCIFIOConfig config) throws FormatException, IOException;

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
	 * {@link io.scif.Reader#readBlock(RandomAccessInputStream, int, long[], long[], Block)}
	 *
	 * @see io.scif.Reader#readBlock(RandomAccessInputStream, int, long[], long[],
	 *      Block)
	 */
	B readBlock(RandomAccessInputStream s, int imageIndex, long[] blockMin,
		long[] blockMax, B block) throws IOException;

	/**
	 * Generic-parameterized {@code readBlock} method, using
	 * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
	 * {@link io.scif.Reader#readBlock(RandomAccessInputStream, int, long[], long[], int, Block)}
	 *
	 * @see io.scif.Reader#readBlock(RandomAccessInputStream, int, long[], long[],
	 *      int, Block)
	 */
	B readBlock(RandomAccessInputStream s, int imageIndex, long[] blockMin,
		long[] blockMax, int scanlinePad, B block) throws IOException;

	@Override
	B createBlock(long[] blockOffsets, long[] blockBounds);

	/**
	 * Returns the class of {@code Blocks} associated with this {@code Reader}.
	 */
	Class<B> getBlockClass();

}
