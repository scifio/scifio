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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * StreamHandle implementation for reading from gzip-compressed files or byte
 * arrays. Instances of GZipHandle are read-only.
 *
 * @see StreamHandle
 * @author Melissa Linkert
 */
@Plugin(type = IStreamAccess.class)
public class GZipHandle extends StreamHandle {

	// -- Constructor --

	/**
	 * Zero-parameter constructor. This instructor can be used first to see if a
	 * given file is constructable from this handle. If so, setFile can then be
	 * used.
	 */
	public GZipHandle() {
		super();
	}

	public GZipHandle(final Context context) {
		super(context);
	}

	/**
	 * Construct a new GZipHandle for the given file.
	 *
	 * @throws HandleException if the given file name is not a GZip file.
	 */
	public GZipHandle(final Context context, final String file)
		throws IOException
	{
		super(context);
		setFile(file);
	}

	// -- IStreamAccess API methods --

	@Override
	public boolean isConstructable(final String file) throws IOException {
		if (!file.toLowerCase().endsWith(".gz")) return false;

		final FileInputStream s = new FileInputStream(file);
		final byte[] b = new byte[2];
		s.read(b);
		s.close();
		return Bytes.toInt(b, true) == GZIPInputStream.GZIP_MAGIC;
	}

	@Override
	public void resetStream() throws IOException {
		if (getStream() != null) getStream().close();
		final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
			getFile()), RandomAccessInputStream.MAX_OVERHEAD);
		setStream(new DataInputStream(new GZIPInputStream(bis)));
	}

	// -- IStreamAccess API methods --

	@Override
	public void setFile(final String file) throws IOException {
		super.setFile(file);
		if (!isConstructable(file)) {
			throw new HandleException(file + " is not a gzip file.");
		}

		resetStream();

		int length = 0;
		while (true) {
			final int skip = getStream().skipBytes(1024);
			if (skip <= 0) break;
			length += skip;
		}

		setLength(length);

		resetStream();
	}

}
