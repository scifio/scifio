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

import io.scif.SCIFIOService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Interface for services that work with the {@link java.nio} package,
 * particularly NIO {@link ByteBuffer} objects.
 *
 * @author Chris Allan
 * @author Curtis Rueden
 */
public interface NIOService extends SCIFIOService {

	/**
	 * Allocates or maps the desired file data into memory.
	 * <p>
	 * This method provides a facade to byte buffer allocation that enables
	 * {@code FileChannel.map()} usage on platforms where it's unlikely to give us
	 * problems and heap allocation where it is.
	 * </p>
	 *
	 * @param channel File channel to allocate or map byte buffers from.
	 * @param mapMode The map mode. Required but only used if memory mapped I/O is
	 *          to occur.
	 * @param bufferStartPosition The absolute position of the start of the
	 *          buffer.
	 * @param newSize The buffer size.
	 * @return A newly allocated or mapped NIO byte buffer.
	 * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092131"
	 * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6417205"
	 * @throws IOException If there is an issue mapping, aligning or allocating
	 *           the buffer.
	 */
	ByteBuffer allocate(FileChannel channel, MapMode mapMode,
		long bufferStartPosition, int newSize) throws IOException;

}
