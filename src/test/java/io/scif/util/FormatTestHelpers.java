/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2023 SCIFIO developers.
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

package io.scif.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.io.nio.ByteBufferByteBank;

public class FormatTestHelpers {

	public static DataHandle<Location> createLittleEndianHandle(
		final int capacity, final DataHandleService dataHandleService)
	{
		try {
			return createLittleEndianHandle(capacity, dataHandleService, false);
		}
		catch (IOException exc) {
			throw new IllegalStateException(exc);
		}
	}

	public static DataHandle<Location> createLittleEndianHandle(
		final int capacity, final DataHandleService dataHandleService,
		boolean zerofill) throws IOException
	{
		// little endian bytebank
		final ByteBufferByteBank buffer = new ByteBufferByteBank(cap -> {
			return ByteBuffer.allocate(cap).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		}, capacity);
		DataHandle<Location> handle = dataHandleService.create(new BytesLocation(
			buffer));
		if (zerofill) {
			handle.write(new byte[capacity]);
			handle.seek(0l);
		}
		handle.setLittleEndian(true);
		return handle;
	}
}
