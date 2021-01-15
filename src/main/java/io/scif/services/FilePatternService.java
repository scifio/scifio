/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2021 SCIFIO developers.
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

package io.scif.services;

import io.scif.SCIFIOService;

import java.io.IOException;
import java.util.Collection;

import org.scijava.io.location.BrowsableLocation;

/**
 * A collection of utility methods to facilitate {@link io.scif.FilePattern}
 * use.
 *
 * @see io.scif.FilePattern
 * @author Mark Hiner
 */
public interface FilePatternService extends SCIFIOService {

	/**
	 * Identifies the group pattern from a given file within that group.
	 *
	 * @param path The file path (to a local file) to use as a template for the
	 *          match.
	 * @throws IOException
	 */
	String findPattern(String path) throws IOException;

	/**
	 * Identifies the group pattern from a given file within that group.
	 *
	 * @param file The file to use as a template for the match.
	 * @throws IOException
	 */
	String findPattern(BrowsableLocation file) throws IOException;

	/**
	 * Identifies the group pattern from a given file within that group.
	 *
	 * @param name The filename to use as a template for the match.
	 * @param dir The directory in which to search for matching files.
	 * @throws IOException
	 */
	String findPattern(BrowsableLocation name, BrowsableLocation dir)
		throws IOException;

	/**
	 * Identifies the group pattern from a given file within that group.
	 *
	 * @param name The filename to use as a template for the match.
	 * @param dir The directory prefix to use for matching files.
	 * @param nameList The names through which to search for matching files.
	 */
	String findPattern(BrowsableLocation name, BrowsableLocation dir,
		Collection<BrowsableLocation> nameList);

	/**
	 * Identifies the group pattern from a given file within that group.
	 *
	 * @param name The filename to use as a template for the match.
	 * @param dir The directory prefix to use for matching files.
	 * @param nameList The names through which to search for matching files.
	 * @param excludeAxes The list of axis types which should be excluded from the
	 *          pattern.
	 */
	String findPattern(BrowsableLocation name, BrowsableLocation dir,
		Collection<BrowsableLocation> nameList, int[] excludeAxes);

	/**
	 * Generate a pattern from a list of file names. The pattern generated will be
	 * a regular expression.
	 * <p>
	 * Currently assumes that all file names are in the same directory.
	 * </p>
	 */
	String findPattern(String[] names);

	/**
	 * Finds the list of names matching the provided base and generates all
	 * patterns that match the list.
	 *
	 * @throws IOException
	 */
	String[] findImagePatterns(BrowsableLocation base) throws IOException;

	/**
	 * Generates a list of all the patterns that match the provided list of file
	 * names.
	 *
	 * @throws IOException
	 */
	String[] findImagePatterns(BrowsableLocation base, BrowsableLocation dir,
		Collection<BrowsableLocation> nameList) throws IOException;

}
