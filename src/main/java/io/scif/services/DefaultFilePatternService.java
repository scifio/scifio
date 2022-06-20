/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2022 SCIFIO developers.
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

import io.scif.AxisGuesser;
import io.scif.FilePattern;
import io.scif.NumberFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ArrayUtils;

/**
 * Default {@link FilePatternService} implementation.
 *
 * @see FilePatternService
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultFilePatternService extends AbstractService implements
	FilePatternService
{

	@Parameter
	private DataHandleService dataHandleService;

	@Override
	public String findPattern(final String path) throws IOException {
		return findPattern(new FileLocation(path));
	}

	@Override
	public String findPattern(final BrowsableLocation file) throws IOException {
		return findPattern(file, file.parent());
	}

	@Override
	public String findPattern(final BrowsableLocation name,
		final BrowsableLocation dir) throws IOException
	{
		// list files in the given directory
		final Set<BrowsableLocation> f = dir.children();
		if (f.isEmpty()) return null;
		return findPattern(name, dir, f);
	}

	@Override
	public String findPattern(final BrowsableLocation name,
		final BrowsableLocation dir, final Collection<BrowsableLocation> f)
	{
		return findPattern(name, dir, f, null);
	}

	@Override
	public String findPattern(final BrowsableLocation file,
		final BrowsableLocation dir, final Collection<BrowsableLocation> nameList,
		int[] excludeAxes)
	{
		if (excludeAxes == null) excludeAxes = new int[0];

		final String name = file.getName();

		// compile list of numerical blocks
		final int len = name.length();
		final int bound = (len + 1) / 2;
		final int[] indexList = new int[bound];
		final int[] endList = new int[bound];
		int q = 0;
		boolean num = false;
		int ndx = -1;
		int e = 0;
		for (int i = 0; i < len; i++) {
			final char c = name.charAt(i);
			if (c >= '0' && c <= '9') {
				if (num) e++;
				else {
					num = true;
					ndx = i;
					e = ndx + 1;
				}
			}
			else if (num) {
				num = false;
				indexList[q] = ndx;
				endList[q] = e;
				q++;
			}
		}
		if (num) {
			indexList[q] = ndx;
			endList[q] = e;
			q++;
		}

		// analyze each block, building pattern as we go
		final StringBuilder sb = new StringBuilder("");

		for (int i = 0; i < q; i++) {
			final int last = i > 0 ? endList[i - 1] : 0;
			final String prefix = name.substring(last, indexList[i]);
			final int axisType = AxisGuesser.getAxisType(prefix);
			if (ArrayUtils.contains(excludeAxes, axisType)) {
				sb.append(name.substring(last, endList[i]));
				continue;
			}

			sb.append(prefix);
			final String pre = name.substring(0, indexList[i]);
			final String post = name.substring(endList[i]);

			final NumberFilter filter = new NumberFilter(pre, post);
			final List<BrowsableLocation> list = matchFiles(nameList, filter);
			if (list == null || list.isEmpty()) return null;
			if (list.size() == 1) {
				// false alarm; this number block is constant
				sb.append(name.substring(indexList[i], endList[i]));
				continue;
			}
			boolean fix = true;
			for (final BrowsableLocation s : list) {
				if (s.getName().length() != len) {
					fix = false;
					break;
				}
			}
			if (fix) {
				// tricky; this fixed-width block could represent multiple
				// numberings
				final int width = endList[i] - indexList[i];

				// check each character for duplicates
				final boolean[] same = new boolean[width];
				for (int j = 0; j < width; j++) {
					same[j] = true;
					final int jx = indexList[i] + j;
					final char c = name.charAt(jx);
					for (final BrowsableLocation s : list) {
						if (s.getName().charAt(jx) != c) {
							same[j] = false;
							break;
						}
					}
				}

				// break down each sub-block
				int j = 0;
				while (j < width) {
					final int jx = indexList[i] + j;
					if (same[j]) {
						sb.append(name.charAt(jx));
						j++;
					}
					else {
						while (j < width && !same[j])
							j++;
						final String p = findPattern(name, nameList, jx, indexList[i] + j,
							"");
						final char c = indexList[i] > 0 ? name.charAt(indexList[i] - 1)
							: '.';
						// check if this block represents the series axis
						if (p == null && c != 'S' && c != 's' && c != 'E' && c != 'e') {
							// unable to find an appropriate breakdown of
							// numerical blocks
							return null;
						}
						else if (p == null) {
							sb.append(name.charAt(endList[i] - 1));
						}
						else sb.append(p);
					}
				}
			}
			else {
				// assume variable-width block represents only one numbering
				final int[] numbers = new int[list.size()];
				for (int j = 0; j < list.size(); j++) {
					numbers[j] = filter.getNumber(list.get(j).getName());
				}
				Arrays.sort(numbers);
				final String bounds = getBounds(numbers, false);
				if (bounds == null) return null;
				sb.append(bounds);
			}
		}
		sb.append(q > 0 ? name.substring(endList[q - 1]) : name);

		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '\\') {
				sb.insert(i, '\\');
				i++;
			}
		}

		return sb.toString();
	}

	@Override
	public String findPattern(final String[] names) {
		final String dir = names[0].substring(0, names[0].lastIndexOf(
			File.separator) + 1);

		final StringBuilder pattern = new StringBuilder();
		pattern.append(Pattern.quote(dir));

		for (int i = 0; i < names.length; i++) {
			pattern.append("(?:");
			final String name = names[i].substring(names[i].lastIndexOf(
				File.separator) + 1);
			pattern.append(Pattern.quote(name));
			pattern.append(")");
			if (i < names.length - 1) {
				pattern.append("|");
			}
		}
		return pattern.toString();
	}

	@Override
	public String[] findImagePatterns(final BrowsableLocation base)
		throws IOException
	{
		final BrowsableLocation file = base;
		final BrowsableLocation parent = file.parent();
		final Set<BrowsableLocation> list = parent.children();
		return findImagePatterns(base, parent, list);
	}

	@Override
	public String[] findImagePatterns(final BrowsableLocation base,
		final BrowsableLocation dir, final Collection<BrowsableLocation> nameList)
		throws IOException
	{
		final String name = base.getName();
		int dot = name.lastIndexOf('.');
		String baseSuffix;
		if (dot < 0) baseSuffix = "";
		else baseSuffix = name.substring(dot + 1);

		final List<String> patterns = new ArrayList<>();
		final int[] exclude = new int[] { AxisGuesser.S_AXIS };
		for (final BrowsableLocation loc : nameList) {
			final String pattern = findPattern(loc, dir, nameList, exclude);
			if (pattern == null) continue;
			int start = pattern.lastIndexOf(File.separator) + 1;
			if (start < 0) start = 0;
			String patternSuffix = pattern.substring(start);
			dot = patternSuffix.indexOf('.');
			if (dot < 0) patternSuffix = "";
			else patternSuffix = patternSuffix.substring(dot + 1);

			final String checkPattern = findPattern(loc, dir, nameList);
			final Location[] checkFiles = new FilePattern(base, checkPattern,
				dataHandleService).getFiles();

			if (!patterns.contains(pattern) && (!dataHandleService.exists(base
				.sibling(pattern))) && patternSuffix.equals(baseSuffix) && ArrayUtils
					.indexOf(checkFiles, base) >= 0)
			{
				patterns.add(pattern);
			}
		}
		final String[] s = patterns.toArray(new String[patterns.size()]);
		Arrays.sort(s);
		return s;
	}

	// -- Utility helper methods --

	/** Recursive method for parsing a fixed-width numerical block. */
	private String findPattern(final String name,
		final Collection<BrowsableLocation> nameList, final int ndx, final int end,
		final String p)
	{
		if (ndx == end) return p;
		for (int i = end - ndx; i >= 1; i--) {
			final NumberFilter filter = new NumberFilter(name.substring(0, ndx), name
				.substring(ndx + i));
			final List<BrowsableLocation> list = matchFiles(nameList, filter);
			final int[] numbers = new int[list.size()];
			for (int j = 0; j < list.size(); j++) {
				numbers[j] = Integer.parseInt(list.get(j).getName().substring(ndx, ndx +
					i));
			}
			Arrays.sort(numbers);
			final String bounds = getBounds(numbers, true);
			if (bounds == null) continue;
			final String pat = findPattern(name, nameList, ndx + i, end, p + bounds);
			if (pat != null) return pat;
		}
		// no combination worked; this parse path is infeasible
		return null;
	}

	/**
	 * Gets a string containing start, end and step values for a sorted list of
	 * numbers.
	 */
	private String getBounds(final int[] numbers, final boolean fixed) {
		if (numbers.length < 2) return null;
		final int b = numbers[0];
		final int e = numbers[numbers.length - 1];
		final int s = numbers[1] - b;
		if (s == 0) {
			// step size must be positive
			return null;
		}
		for (int i = 2; i < numbers.length; i++) {
			if (numbers[i] - numbers[i - 1] != s) {
				// step size is not constant
				return null;
			}
		}
		final String sb = "" + b;
		final String se = "" + e;
		final StringBuilder bounds = new StringBuilder("<");
		if (fixed) {
			final int zeroes = se.length() - sb.length();
			for (int i = 0; i < zeroes; i++)
				bounds.append("0");
		}
		bounds.append(sb);
		bounds.append("-");
		bounds.append(se);
		if (s != 1) {
			bounds.append(":");
			bounds.append(s);
		}
		bounds.append(">");
		return bounds.toString();
	}

	/** Filters the given list of filenames according to the specified filter. */
	private List<BrowsableLocation> matchFiles(
		final Collection<BrowsableLocation> nameList, final NumberFilter filter)
	{
		final List<BrowsableLocation> list = new ArrayList<>();
		for (final BrowsableLocation inFile : nameList) {
			if (filter.accept(inFile.getName())) list.add(inFile);
		}
		return list;
	}
}
