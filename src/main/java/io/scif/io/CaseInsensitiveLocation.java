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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.scijava.Context;

/**
 * Case insensitive variant of Location.
 */
public class CaseInsensitiveLocation extends Location {

	// Fields

	private static final Cache cache = new Cache();

	// -- Constructors (no caching) --

	public CaseInsensitiveLocation(final Context context, final String pathname)
		throws IOException
	{
		super(context, findCaseInsensitive(new File(pathname)));
	}

	public CaseInsensitiveLocation(final Context context, final File file)
		throws IOException
	{
		super(context, findCaseInsensitive(file));
	}

	public CaseInsensitiveLocation(final Context context, final String parent,
		final String child) throws IOException
	{
		super(context, findCaseInsensitive(new File(parent + File.separator +
			child)));
	}

	public CaseInsensitiveLocation(final Context context,
		final CaseInsensitiveLocation parent, final String child) throws IOException
	{
		super(context, findCaseInsensitive(new File(parent.getAbsolutePath(),
			child)));
	}

	// -- Methods --

	/**
	 * Remove (invalidate) cached content all directories.
	 */
	public static void invalidateCache() {
		cache.invalidate();
	}

	/**
	 * Remove (invalidate) cached content for the specified directory.
	 *
	 * @param dir the directory to remove,
	 */
	public static void invalidateCache(final File dir) {
		cache.invalidate(dir);
	}

	private static File findCaseInsensitive(final File name) throws IOException {
		// The file we're looking for doesn't exist, so look for it in the
		// same directory in a case-insensitive manner. Note that this will
		// throw an exception if multiple copies are found.
		return cache.lookup(name);
	}

	// Helper class

	/**
	 * Caching for CaseInsensitiveLocation. A case insensitive path lookup
	 * requires a full scan of the containing directory, which can be very
	 * expensive. This class caches insensitive-to-sensitive name mappings, so the
	 * correct casing on filesystem is returned.
	 */
	private static final class Cache {

		/**
		 * Mapping of directory names to directory content, the content being a
		 * mapping of case insensitive name to case sensitive (real) name on disc.
		 */
		private final HashMap<String, HashMap<String, String>> cache =
			new HashMap<>();

		/**
		 * The constructor.
		 */
		public Cache() {
			super();
		}

		/**
		 * Fill the cache with the content for the specified directory.
		 *
		 * @param dir the directory to cache.
		 * @return the filename mappings for the directory, or null if the directory
		 *         did not exist.
		 */
		// Cache the whole directory content in a single pass
		private HashMap<String, String> fill(final File dir) throws IOException {
			final String dirname = dir.getAbsolutePath();
			HashMap<String, String> s = cache.get(dirname);
			if (s == null && dir.exists()) {
				s = new HashMap<>();
				cache.put(dirname, s);

				final String[] files = dir.list();
				for (final String name : files) {
					final String lower = name.toLowerCase();
					if (s.containsKey(lower)) throw new IOException(
						"Multiple files found for case-insensitive path");
					s.put(lower, name);
				}
			}
			return s;
		}

		/**
		 * Remove a directory from the cache.
		 *
		 * @param dir the directory to remove.
		 */
		public void invalidate(final File dir) {
			final String dirname = dir.getAbsolutePath();
			cache.remove(dirname);
		}

		/**
		 * Remove all content from the cache.
		 */
		public void invalidate() {
			cache.clear();
		}

		/**
		 * Look up a filename in the cache.
		 *
		 * @param name the name to look up (case insensitive).
		 * @return the filename on disc (case sensitive).
		 * @throws IOException
		 */
		public File lookup(final File name) throws IOException {

			final File parent = name.getParentFile();
			if (parent != null) {
				final HashMap<String, String> s = fill(parent);

				if (s != null) {
					final String realname = name.getName();
					final String lower = realname.toLowerCase();
					final String f = s.get(lower);
					if (f != null) return new File(parent, f);
				}
			}
			return name;
		}

	}

}
