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

package io.scif.services;

import io.scif.AxisGuesser;
import io.scif.FilePattern;
import io.scif.NumberFilter;
import io.scif.io.Location;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

	@Override
	public String findPattern(final String path) {
		return findPattern(new Location(getContext(), path));
	}

	@Override
	public String findPattern(final Location file) {
		return findPattern(file.getName(), file.getAbsoluteFile().getParent());
	}

	@Override
	public String findPattern(final File file) {
		return findPattern(file.getName(), file.getAbsoluteFile().getParent());
	}

	@Override
	public String findPattern(final String name, String dir) {
		if (dir == null) dir = ""; // current directory
		else if (!dir.equals("") && !dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		final Location dirFile = new Location(getContext(), dir.equals("") ? "."
			: dir);

		// list files in the given directory
		final Location[] f = dirFile.listFiles();
		if (f == null) return null;
		final String[] nameList = new String[f.length];
		for (int i = 0; i < nameList.length; i++)
			nameList[i] = f[i].getName();

		return findPattern(name, dir, nameList);
	}

	@Override
	public String findPattern(final String name, final String dir,
		final String[] nameList)
	{
		return findPattern(name, dir, nameList, null);
	}

	@Override
	public String findPattern(final String name, String dir,
		final String[] nameList, int[] excludeAxes)
	{
		if (excludeAxes == null) excludeAxes = new int[0];

		if (dir == null) dir = ""; // current directory
		else if (!dir.equals("") && !dir.endsWith(File.separator)) {
			dir += File.separator;
		}

		// compile list of numerical blocks
		final int len = name.length();
		final int bound = (len + 1) / 2;
		final int[] indexList = new int[bound];
		final int[] endList = new int[bound];
		int q = 0;
		boolean num = false;
		int ndx = -1, e = 0;
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
		final StringBuilder sb = new StringBuilder(dir);

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
			final String[] list = matchFiles(nameList, filter);
			if (list == null || list.length == 0) return null;
			if (list.length == 1) {
				// false alarm; this number block is constant
				sb.append(name.substring(indexList[i], endList[i]));
				continue;
			}
			boolean fix = true;
			for (final String s : list) {
				if (s.length() != len) {
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
					for (final String s : list) {
						if (s.charAt(jx) != c) {
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
				final BigInteger[] numbers = new BigInteger[list.length];
				for (int j = 0; j < list.length; j++) {
					numbers[j] = filter.getNumber(list[j]);
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
	public String[] findImagePatterns(final String base) {
		final Location file = new Location(getContext(), base).getAbsoluteFile();
		final Location parent = file.getParentFile();
		final String[] list = parent.list(true);
		return findImagePatterns(base, parent.getAbsolutePath(), list);
	}

	@Override
	public String[] findImagePatterns(final String base, final String dir,
		final String[] nameList)
	{
		String baseSuffix = base.substring(base.lastIndexOf(File.separator) + 1);
		int dot = baseSuffix.indexOf(".");
		if (dot < 0) baseSuffix = "";
		else baseSuffix = baseSuffix.substring(dot + 1);

		final ArrayList<String> patterns = new ArrayList<>();
		final int[] exclude = new int[] { AxisGuesser.S_AXIS };
		for (final String name : nameList) {
			final String pattern = findPattern(name, dir, nameList, exclude);
			if (pattern == null) continue;
			int start = pattern.lastIndexOf(File.separator) + 1;
			if (start < 0) start = 0;
			String patternSuffix = pattern.substring(start);
			dot = patternSuffix.indexOf(".");
			if (dot < 0) patternSuffix = "";
			else patternSuffix = patternSuffix.substring(dot + 1);

			final String checkPattern = findPattern(name, dir, nameList);
			final String[] checkFiles = new FilePattern(getContext(), checkPattern)
				.getFiles();

			if (!patterns.contains(pattern) && (!new Location(getContext(), pattern)
				.exists() || base.equals(pattern)) && patternSuffix.equals(
					baseSuffix) && ArrayUtils.indexOf(checkFiles, base) >= 0)
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
	private String findPattern(final String name, final String[] nameList,
		final int ndx, final int end, final String p)
	{
		if (ndx == end) return p;
		for (int i = end - ndx; i >= 1; i--) {
			final NumberFilter filter = new NumberFilter(name.substring(0, ndx), name
				.substring(ndx + i));
			final String[] list = matchFiles(nameList, filter);
			final BigInteger[] numbers = new BigInteger[list.length];
			for (int j = 0; j < list.length; j++) {
				numbers[j] = new BigInteger(list[j].substring(ndx, ndx + i));
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
	private String getBounds(final BigInteger[] numbers, final boolean fixed) {
		if (numbers.length < 2) return null;
		final BigInteger b = numbers[0];
		final BigInteger e = numbers[numbers.length - 1];
		final BigInteger s = numbers[1].subtract(b);
		if (s.equals(BigInteger.ZERO)) {
			// step size must be positive
			return null;
		}
		for (int i = 2; i < numbers.length; i++) {
			if (!numbers[i].subtract(numbers[i - 1]).equals(s)) {
				// step size is not constant
				return null;
			}
		}
		final String sb = b.toString();
		final String se = e.toString();
		final StringBuilder bounds = new StringBuilder("<");
		if (fixed) {
			final int zeroes = se.length() - sb.length();
			for (int i = 0; i < zeroes; i++)
				bounds.append("0");
		}
		bounds.append(sb);
		bounds.append("-");
		bounds.append(se);
		if (!s.equals(BigInteger.ONE)) {
			bounds.append(":");
			bounds.append(s);
		}
		bounds.append(">");
		return bounds.toString();
	}

	/** Filters the given list of filenames according to the specified filter. */
	private String[] matchFiles(final String[] inFiles,
		final NumberFilter filter)
	{
		final List<String> list = new ArrayList<>();
		for (final String inFile : inFiles) {
			if (filter.accept(inFile)) list.add(inFile);
		}
		return list.toArray(new String[0]);
	}
}
