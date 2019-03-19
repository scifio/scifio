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

package io.scif.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A simple writer for INI configuration files.
 *
 * @author Melissa Linkert
 */
public class IniWriter {

	// -- IniWriter API methods --

	/**
	 * Saves the given IniList to the given file. If the given file already
	 * exists, then the IniList will be appended.
	 */
	public void saveINI(final IniList ini, final String path) throws IOException {
		saveINI(ini, path, true);
	}

	/** Saves the given IniList to the given file. */
	public void saveINI(final IniList ini, final String path,
		final boolean append) throws IOException
	{
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(path, append), Constants.ENCODING));

		for (final IniTable table : ini) {
			final String header = table.get(IniTable.HEADER_KEY);
			out.write("[" + header + "]\n");
			for (final String key : table.keySet()) {
				out.write(key + " = " + table.get(key) + "\n");
			}
			out.write("\n");
		}

		out.close();
	}

}
