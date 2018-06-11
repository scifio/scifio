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

import io.scif.SCIFIO;
import io.scif.common.Constants;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * StreamHandle implementation for reading from Zip-compressed files or byte
 * arrays. Instances of ZipHandle are read-only.
 *
 * @see StreamHandle
 * @author Melissa Linkert
 */
@Plugin(type = IStreamAccess.class)
public class ZipHandle extends StreamHandle {

	// -- Fields --

	private boolean resetStream;

	private RandomAccessInputStream in;

	private ZipInputStream zip;

	private String entryName;

	private int entryCount;

	// -- Constructor --

	/**
	 * Zero-parameter constructor. This instructor can be used first to see if a
	 * given file is constructable from this handle. If so, setFile can then be
	 * used.
	 */
	public ZipHandle() {
		super();
	}

	public ZipHandle(final Context context) {
		super(context);
	}

	public ZipHandle(final Context context, final String file)
		throws IOException
	{
		super(context);
		setFile(file);
	}

	/**
	 * Constructs a new ZipHandle corresponding to the given entry of the
	 * specified Zip file.
	 *
	 * @throws HandleException if the given file is not a Zip file.
	 */
	public ZipHandle(final Context context, final String file,
		final ZipEntry entry) throws IOException
	{
		super(context);

		setFile(file, entry);
	}

	// -- IStreamAccess API methods --

	/** Returns true if the given filename is a Zip file. */
	@Override
	public boolean isConstructable(final String file) throws IOException {
		if (!file.toLowerCase().endsWith(".zip")) return false;

		final IRandomAccess handle = getHandle(file);
		final byte[] b = new byte[2];
		if (handle.length() >= 2) {
			handle.read(b);
		}
		handle.close();
		return new String(b, Constants.ENCODING).equals("PK");
	}

	// -- ZipHandle API methods --

	/** Get the name of the backing Zip entry. */
	public String getEntryName() {
		return entryName;
	}

	/** Returns the DataInputStream corresponding to the backing Zip entry. */
	public DataInputStream getInputStream() {
		return getStream();
	}

	/**
	 * Returns the number of entries.
	 *
	 * @deprecated The value returned by this method is inconsistent, and not used
	 *             internally at all, either.
	 */
	@Deprecated
	public int getEntryCount() {
		return entryCount;
	}

	// -- IStreamAccess API methods --

	public void setFile(final String file, final ZipEntry entry)
		throws IOException
	{
		super.setFile(file);

		setLength(-1);

		in = openStream(file);
		zip = new ZipInputStream(in);
		entryName = entry == null ? null : entry.getName();
		entryCount = entryName == null ? 0 : 1;

		if (entryName == null) {
			// strip off .zip extension and directory prefix
			String innerFile = file.substring(0, file.length() - 4);
			int slash = innerFile.lastIndexOf(File.separator);
			if (slash < 0) slash = innerFile.lastIndexOf("/");
			if (slash >= 0) innerFile = innerFile.substring(slash + 1);

			// look for Zip entry with same prefix as the Zip file itself
			boolean matchFound = false;
			while (true) {
				final ZipEntry ze = zip.getNextEntry();
				if (ze == null) break;
				if (entryName == null) entryName = ze.getName();
				if (!matchFound && ze.getName().startsWith(innerFile)) {
					// found entry with matching name
					entryName = ze.getName();
					matchFound = true;
				}
				entryCount++;
			}
		}

		resetStream();
	}

	@Override
	public void setFile(final String file) throws IOException {
		setFile(file, null);
	}

	@Override
	public void resetStream() throws IOException {
		if (getStream() != null) getStream().close();
		if (in != null) {
			in.close();
			in = openStream(getFile());
		}
		if (zip != null) zip.close();
		zip = new ZipInputStream(in);

		setStream(new DataInputStream(new BufferedInputStream(zip,
			RandomAccessInputStream.MAX_OVERHEAD)));
		getStream().mark(RandomAccessInputStream.MAX_OVERHEAD);

		seekToEntry();

		if (resetStream) resetStream();
	}

	// -- IRandomAccess API methods --

	@Override
	public void close() throws IOException {
		super.close();
		zip = null;
		entryName = null;
		setLength(-1);
		if (in != null) in.close();
		in = null;
		entryCount = 0;
	}

	// -- Helper methods --

	/**
	 * Seeks to the relevant ZIP entry, populating the stream length accordingly.
	 */
	private void seekToEntry() throws IOException {
		resetStream = false;

		while (true) {
			final ZipEntry entry = zip.getNextEntry();
			if (entryName == null || entryName.equals(entry.getName())) {
				// found the matching entry name (or first entry if the name is
				// null)
				if (getLength() < 0) populateLength(entry.getSize());
				break;
			}
		}
	}

	/** Sets the stream length, computing it by force if necessary. */
	private void populateLength(final long size) throws IOException {
		if (size >= 0) {
			setLength(size);
			return;
		}
		// size is unknown, so we must read the stream manually
		long length = 0;
		final DataInputStream stream = getStream();
		while (true) {
			final long skipped = stream.skip(Long.MAX_VALUE);
			if (skipped == 0) {
				// NB: End of stream, we hope. Technically there is no contract
				// for
				// when skip(long) returns 0, but in practice it seems to be
				// when end
				// of stream is reached.
				break;
			}
			length += skipped;
		}

		setLength(length);

		resetStream = true;
	}

	private IRandomAccess getHandle(final String file) throws IOException {
		final SCIFIO scifio = new SCIFIO(getContext());
		return scifio.location().getHandle(file, false, false);
	}

	private RandomAccessInputStream openStream(final String file)
		throws IOException
	{
		return new RandomAccessInputStream(getContext(), getHandle(file), file);
	}

}
