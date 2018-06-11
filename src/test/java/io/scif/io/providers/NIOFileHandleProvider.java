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

package io.scif.io.providers;

import io.scif.io.IRandomAccess;
import io.scif.io.NIOFileHandle;
import io.scif.io.NIOService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.scijava.Context;

/**
 * Implementation of IRandomAccessProvider that produces instances of
 * loci.common.NIOFileHandle.
 *
 * @see IRandomAccessProvider
 * @see io.scif.io.NIOFileHandle
 */
class NIOFileHandleProvider implements IRandomAccessProvider {

	@Override
	public IRandomAccess createMock(final byte[] page, final String mode,
		final int bufferSize) throws IOException
	{
		final File pageFile = File.createTempFile("page", ".dat");
		final OutputStream stream = new FileOutputStream(pageFile);
		try {
			stream.write(page);
		}
		finally {
			stream.close();
		}
		final Context context = new Context(NIOService.class);
		final NIOService nioService = context.getService(NIOService.class);
		return new NIOFileHandle(nioService, pageFile, mode, bufferSize);
	}

}
