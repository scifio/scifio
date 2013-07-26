/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.io;

import io.scif.common.Constants;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * StreamHandle implementation for reading from BZip2-compressed files or byte
 * arrays. Instances of BZip2Handle are read-only.
 * <dl>
 * <dt><b>Source code:</b></dt>
 * <dd><a href=
 * "http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/src/loci/common/BZip2Handle.java"
 * >Trac</a>, <a href=
 * "http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/src/loci/common/BZip2Handle.java;hb=HEAD"
 * >Gitweb</a></dd>
 * </dl>
 * 
 * @see StreamHandle
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
@Plugin(type = IStreamAccess.class)
public class BZip2Handle extends StreamHandle {

	// -- Constructor --

	/**
	 * Zero-parameter constructor. This instructor can be used first to see if a
	 * given file is constructable from this handle. If so, setFile can then be
	 * used.
	 */
	public BZip2Handle() {
		super();
	}

	public BZip2Handle(final Context context) {
		super(context);
	}

	/**
	 * Construct a new BZip2Handle corresponding to the given file.
	 * 
	 * @throws HandleException if the given file is not a BZip2 file.
	 */
	public BZip2Handle(final Context context, final String file)
		throws IOException
	{
		super(context);
		setFile(file);
	}

	// -- IStreamAccess API methods --

	/* @see IStreamAccess#isConstructable(String) */
	public boolean isConstructable(final String file) throws IOException {
		if (!file.toLowerCase().endsWith(".bz2")) return false;

		final FileInputStream s = new FileInputStream(file);
		final byte[] b = new byte[2];
		s.read(b);
		s.close();
		return new String(b, Constants.ENCODING).equals("BZ");
	}

	/* @see IStreamAccess#resetStream() */
	public void resetStream() throws IOException {
		final BufferedInputStream bis =
			new BufferedInputStream(new FileInputStream(getFile()),
				RandomAccessInputStream.MAX_OVERHEAD);
		int skipped = 0;
		while (skipped < 2) {
			skipped += bis.skip(2 - skipped);
		}
		setStream(new DataInputStream(new CBZip2InputStream(bis)));
	}

	// -- IStreamAccess API methods --

	/* @see IStreamAccess#setFile(String) */
	@Override
	public void setFile(final String file) throws IOException {
		super.setFile(file);
		if (!isConstructable(file)) {
			throw new HandleException(file + " is not a BZip2 file.");
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
