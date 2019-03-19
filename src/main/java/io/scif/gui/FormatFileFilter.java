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

package io.scif.gui;

import io.scif.Checker;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.config.SCIFIOConfig;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.scijava.io.location.FileLocation;

/**
 * A file filter for identifying the files supported by an associated
 * {@link Format}s. Because these filters are queried repeatedly, opening a
 * dataset during the checking process is disabled. Thus this filter will never
 * accept a dataset that need to be opened to determine format support. File
 * choosers should have an "All files" option to allow selection of such
 * datasets.
 *
 * @author Mark Hiner
 */
public class FormatFileFilter extends FileFilter implements java.io.FileFilter,
	Comparable<FileFilter>
{

	// -- Fields --

	/** Format to use for filtering */
	private final Format format;

	/** Format description. */
	private final String desc;

	// -- Constructors --

	/**
	 * Constructs a new filter that accepts files of the given format's type.
	 *
	 * @param format The format to use for verifying a file's type.
	 */
	public FormatFileFilter(final Format format) {
		this.format = format;
		final StringBuilder sb = new StringBuilder(format.getFormatName());
		boolean first = true;
		for (final String suffix : format.getSuffixes()) {
			if (suffix == null || suffix.equals("")) continue;
			if (first) {
				sb.append(" (");
				first = false;
			}
			else sb.append(", ");
			sb.append("*.");
			sb.append(suffix);
		}
		sb.append(")");
		desc = sb.toString();
	}

	// -- FormatFileFilter API --

	/** Returns the filter's format. */
	public Format getFormat() {
		return format;
	}

	// -- FileFilter API methods --

	/**
	 * Accepts files in accordance with the associated format's {@link Checker}.
	 */
	@Override
	public boolean accept(final File f) {
		if (f.isDirectory()) return true;
		try {
			return format.createChecker().isFormat(new FileLocation(f),
				new SCIFIOConfig().checkerSetOpen(false));
		}
		catch (final FormatException e) {
			return false;
		}
	}

	/** Gets the filter's description. */
	@Override
	public String getDescription() {
		return desc;
	}

	// -- Object API methods --

	/** Gets a string representation of this file filter. */
	@Override
	public String toString() {
		return "FormatFileFilter: " + desc;
	}

	// -- Comparable API methods --

	/** Compares two FileFilter objects alphanumerically. */
	@Override
	public int compareTo(final FileFilter o) {
		return desc.compareTo(o.getDescription());
	}

}
