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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * Provides random access to URLs using the IRandomAccess interface. Instances
 * of URLHandle are read-only.
 *
 * @see IRandomAccess
 * @see StreamHandle
 * @see java.net.URLConnection
 * @author Melissa Linkert
 */
@Plugin(type = IStreamAccess.class)
public class URLHandle extends StreamHandle {

	// -- Constants --

	private static final String[] SUPPORTED_PROTOCOLS = { "http:", "https:",
		"file:" };

	// -- Fields --

	/** URL of open socket */
	private String url;

	/** Socket underlying this stream */
	private URLConnection conn;

	// -- Constructors --

	/**
	 * Zero-parameter constructor. This instructor can be used first to see if a
	 * given file is constructable from this handle. If so, setFile can then be
	 * used.
	 */
	public URLHandle() {
		super();
	}

	public URLHandle(final Context context) {
		super(context);
	}

	/**
	 * Constructs a new URLHandle using the given URL.
	 */
	public URLHandle(final Context context, final String url) throws IOException {
		super(context);
		setURL(url);
	}

	// -- URLHandle API --

	/**
	 * Initializes this URLHandle with the provided url.
	 *
	 * @throws IOException
	 */
	public void setURL(String url) throws IOException {
		if (!isConstructable(url)) {
			throw new HandleException(url + " is not a valid url.");
		}

		boolean hasSupportedProtocol = false;
		for (final String protocol : SUPPORTED_PROTOCOLS) {
			if (url.startsWith(protocol)) {
				hasSupportedProtocol = true;
				break;
			}
		}

		if (!hasSupportedProtocol) {
			url = "http://" + url;
		}

		this.url = url;
		resetStream();
	}

	// -- IRandomAccess API methods --

	@Override
	public void seek(final long pos) throws IOException {
		if (pos < getFp() && pos >= getMark()) {
			getStream().reset();
			setFp(getMark());
			skip(pos - getFp());
		}
		else super.seek(pos);
	}

	// -- IStreamAccess API methods --

	@Override
	public boolean isConstructable(final String id) throws IOException {
		for (final String protocol : SUPPORTED_PROTOCOLS) {
			if (id.startsWith(protocol)) {
				return true;
			}
		}

		return false;
	}

	// -- StreamHandle API methods --

	@Override
	public void setFile(final String file) throws IOException {
		super.setFile(file);
		setURL(file);
	}

	@Override
	public void resetStream() throws IOException {
		conn = (new URL(url)).openConnection();
		setStream(new DataInputStream(new BufferedInputStream(conn.getInputStream(),
			RandomAccessInputStream.MAX_OVERHEAD)));
		setFp(0);
		setMark(0);
		setLength(conn.getContentLength());
		if (getStream() != null) getStream().mark(
			RandomAccessInputStream.MAX_OVERHEAD);
	}

	// -- Helper methods --

	/** Skip over the given number of bytes. */
	private void skip(long bytes) throws IOException {
		while (bytes >= Integer.MAX_VALUE) {
			bytes -= skipBytes(Integer.MAX_VALUE);
		}
		int skipped = skipBytes((int) bytes);
		while (skipped < bytes) {
			final int n = skipBytes((int) (bytes - skipped));
			if (n == 0) break;
			skipped += n;
		}
	}
}
