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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter that recognizes files from a union of other filters.
 */
public class ComboFileFilter extends FileFilter implements java.io.FileFilter,
	Comparable<Object>
{

	// -- Fields --

	/** List of filters to be combined. */
	private final FileFilter[] fileFilters;

	/** Description. */
	private final String desc;

	// -- Constructor --

	/** Constructs a new filter from a list of other filters. */
	public ComboFileFilter(final FileFilter[] filters, final String description) {
		fileFilters = new FileFilter[filters.length];
		System.arraycopy(filters, 0, fileFilters, 0, filters.length);
		desc = description;
	}

	// -- ComboFileFilter API methods --

	/** Gets the list of file filters forming this filter combination. */
	public FileFilter[] getFilters() {
		final FileFilter[] ff = new FileFilter[fileFilters.length];
		System.arraycopy(fileFilters, 0, ff, 0, fileFilters.length);
		return ff;
	}

	// -- Static ComboFileFilter API methods --

	/**
	 * Sorts the given list of file filters, and combines filters with identical
	 * descriptions into a combination filter that accepts anything any of its
	 * constituant filters do.
	 */
	public static FileFilter[] sortFilters(final FileFilter[] filters) {
		return sortFilters(new Vector(Arrays.asList(filters)));
	}

	/**
	 * Sorts the given list of file filters, and combines filters with identical
	 * descriptions into a combination filter that accepts anything any of its
	 * constituant filters do.
	 */
	public static FileFilter[] sortFilters(final Vector filters) {
		// sort filters alphanumerically
		Collections.sort(filters);

		// combine matching filters
		final int len = filters.size();
		final Vector v = new Vector(len);
		for (int i = 0; i < len; i++) {
			final FileFilter ffi = (FileFilter) filters.elementAt(i);
			int ndx = i + 1;
			while (ndx < len) {
				final FileFilter ff = (FileFilter) filters.elementAt(ndx);
				if (!ffi.getDescription().equals(ff.getDescription())) break;
				ndx++;
			}
			if (ndx > i + 1) {
				// create combination filter for matching filters
				final FileFilter[] temp = new FileFilter[ndx - i];
				for (int j = 0; j < temp.length; j++) {
					temp[j] = (FileFilter) filters.elementAt(i + j);
				}
				v.add(new ComboFileFilter(temp, temp[0].getDescription()));
				i += temp.length - 1; // skip next temp-1 filters
			}
			else v.add(ffi);
		}
		final FileFilter[] result = new FileFilter[v.size()];
		v.copyInto(result);
		return result;
	}

	// -- FileFilter API methods --

	/** Accepts files with the proper filename prefix. */
	@Override
	public boolean accept(final File file) {
		for (final FileFilter filter : fileFilters) {
			if (filter.accept(file)) return true;
		}
		return false;
	}

	/** Returns the filter's description. */
	@Override
	public String getDescription() {
		return desc;
	}

	// -- Object API methods --

	/** Gets a string representation of this file filter. */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ComboFileFilter: ");
		sb.append(desc);
		for (final FileFilter fileFilter : fileFilters) {
			sb.append("\n\t");
			sb.append(fileFilter.toString());
		}
		return sb.toString();
	}

	// -- Comparable API methods --

	/** Compares two FileFilter objects alphanumerically. */
	@Override
	public int compareTo(final Object o) {
		return desc.compareToIgnoreCase(((FileFilter) o).getDescription());
	}

}
