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

package io.scif.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with the {@link java.nio} package, particularly
 * NIO {@link ByteBuffer} objects.
 *
 * @author Chris Allan
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultNIOService extends AbstractService implements NIOService {

	// -- Fields --

	@Parameter
	private LogService log;

	/** Whether or not we are to use memory mapped I/O. */
	private final boolean useMappedByteBuffer = Boolean.parseBoolean(System
		.getProperty("mappedBuffers"));

	// -- NIOService API methods --

	@Override
	public ByteBuffer allocate(final FileChannel channel, final MapMode mapMode,
		final long bufferStartPosition, final int newSize) throws IOException
	{
		log.debug("NIO: allocate: mapped=" + useMappedByteBuffer + ", start=" +
			bufferStartPosition + ", size=" + newSize);
		if (useMappedByteBuffer) {
			return allocateMappedByteBuffer(channel, mapMode, bufferStartPosition,
				newSize);
		}
		return allocateDirect(channel, bufferStartPosition, newSize);
	}

	// -- Helper methods --

	/**
	 * Allocates memory and copies the desired file data into it.
	 *
	 * @param channel File channel to allocate or map byte buffers from.
	 * @param bufferStartPosition The absolute position of the start of the
	 *          buffer.
	 * @param newSize The buffer size.
	 * @return A newly allocated NIO byte buffer.
	 * @throws IOException If there is an issue aligning or allocating the buffer.
	 */
	private ByteBuffer allocateDirect(final FileChannel channel,
		final long bufferStartPosition, final int newSize) throws IOException
	{
		final ByteBuffer buffer = ByteBuffer.allocate(newSize);
		channel.read(buffer, bufferStartPosition);
		return buffer;
	}

	/**
	 * Memory maps the desired file data into memory.
	 *
	 * @param channel File channel to allocate or map byte buffers from.
	 * @param mapMode The map mode. Required but only used if memory mapped I/O is
	 *          to occur.
	 * @param bufferStartPosition The absolute position of the start of the
	 *          buffer.
	 * @param newSize The buffer size.
	 * @return A newly mapped NIO byte buffer.
	 * @throws IOException If there is an issue mapping, aligning or allocating
	 *           the buffer.
	 */
	private ByteBuffer allocateMappedByteBuffer(final FileChannel channel,
		final MapMode mapMode, final long bufferStartPosition, final int newSize)
		throws IOException
	{
		return channel.map(mapMode, bufferStartPosition, newSize);
	}

}
