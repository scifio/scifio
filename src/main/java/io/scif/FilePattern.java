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

package io.scif;

import io.scif.services.FilePatternService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.Location;

/**
 * FilePattern is a collection of methods for handling file patterns, a way of
 * succinctly representing a collection of files meant to be part of the same
 * data series. Examples:
 * <ul>
 * <li>C:\data\BillM\sdub&lt;1-12&gt;.pic</li>
 * <li>C:\data\Kevin\80&lt;01-59&gt;0&lt;2-3&gt;.pic</li>
 * <li>/data/Josiah/cell-Z&lt;0-39&gt;.C&lt;0-1&gt;.tiff</li>
 * </ul>
 *
 * @author Curtis Rueden
 */
public class FilePattern {

	// -- Fields --

	/** The file pattern string. */
	private String pattern;

	/** The Location this pattern is based on */
	private BrowsableLocation baseLoc;

	/** The validity of the file pattern. */
	private boolean valid;

	/** Error message generated during file pattern construction. */
	private String msg;

	/** Indices into the pattern indicating the start of a numerical block. */
	private int[] startIndex;

	/** Indices into the pattern indicating the end of a numerical block. */
	private int[] endIndex;

	/** List of pattern blocks for this file pattern. */
	private FilePatternBlock[] blocks;

	/** File listing for this file pattern. */
	private Location[] files;

	/** Whether or not this FilePattern represents a regular expression. */
	private boolean isRegex = false;

	// -- Constructors --

	/**
	 * Creates a pattern object using the given file as a template.
	 *
	 * @throws IOException
	 */
	public FilePattern(final FilePatternService filePatternService,
		final BrowsableLocation file, final DataHandleService dataHandleService)
		throws IOException
	{
		this(file, filePatternService.findPattern(file), dataHandleService);
	}

	/**
	 * Creates a pattern object using the given filename and directory path as a
	 * template.
	 *
	 * @throws IOException
	 */
	public FilePattern(final FilePatternService filePatternService,
		final BrowsableLocation name, final BrowsableLocation dir,
		final DataHandleService dataHandleService) throws IOException
	{
		this(name, filePatternService.findPattern(name, dir), dataHandleService);
	}

	/**
	 * Creates a pattern object for files with the given pattern string.
	 *
	 * @throws IOException
	 */
	public FilePattern(final BrowsableLocation baseLoc, final String pattern,
		final DataHandleService dataHandleService) throws IOException
	{
		this.baseLoc = baseLoc;
		this.pattern = pattern;
		valid = false;
		if (pattern == null) {
			msg = "Null pattern string.";
			return;
		}

		// locate numerical blocks
		final int len = pattern.length();
		final List<Integer> lt = new ArrayList<>(len);
		final List<Integer> gt = new ArrayList<>(len);
		int left = -1;
		while (true) {
			left = pattern.indexOf(FilePatternBlock.BLOCK_START, left + 1);
			if (left < 0) break;
			lt.add(left);
		}
		int right = -1;
		while (true) {
			right = pattern.indexOf(FilePatternBlock.BLOCK_END, right + 1);
			if (right < 0) break;
			gt.add(right);
		}

		// assemble numerical block indices
		final int num = lt.size();
		if (num != gt.size()) {
			msg = "Mismatched numerical block markers.";
			return;
		}
		startIndex = new int[num];
		endIndex = new int[num];
		for (int i = 0; i < num; i++) {
			int val = lt.get(i);
			if (i > 0 && val < endIndex[i - 1]) {
				msg = "Bad numerical block marker order.";
				return;
			}
			startIndex[i] = val;
			val = gt.get(i);
			if (val <= startIndex[i]) {
				msg = "Bad numerical block marker order.";
				return;
			}
			endIndex[i] = val + 1;
		}

		// parse numerical blocks
		blocks = new FilePatternBlock[num];
		for (int i = 0; i < num; i++) {
			final String block = pattern.substring(startIndex[i], endIndex[i]);
			blocks[i] = new FilePatternBlock(block);
		}

		// build file listing
		final List<Location> fileList = new ArrayList<>();
		buildFiles("", num, fileList, dataHandleService);
		files = fileList.toArray(new Location[fileList.size()]);

		if (files.length == 0) {
			try {
				final BrowsableLocation sibling = baseLoc.sibling(pattern);
				if (dataHandleService.exists(sibling)) {
					files = new Location[] { sibling };
				}
			}
			catch (final IOException e) {
				return;
			}
		}

		valid = true;
	}

	// -- FilePattern API methods --

	/** Returns whether or not this pattern is a regular expression. */
	public boolean isRegex() {
		return isRegex;
	}

	/** Gets the file pattern string. */
	public String getPattern() {
		return pattern;
	}

	/** Gets whether the file pattern string is valid. */
	public boolean isValid() {
		return valid;
	}

	/** Gets the file pattern error message, if any. */
	public String getErrorMessage() {
		return msg;
	}

	/** Gets a listing of all files matching the given file pattern. */
	public Location[] getFiles() {
		return files;
	}

	public String[][] getElements() {
		final String[][] elements = new String[blocks.length][];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = blocks[i].getElements();
		}
		return elements;
	}

	public int[] getCount() {
		final int[] count = new int[blocks.length];
		for (int i = 0; i < count.length; i++) {
			count[i] = blocks[i].getElements().length;
		}
		return count;
	}

	/** Gets the specified numerical block. */
	public String getBlock(final int i) {
		if (i < 0 || i >= startIndex.length) return null;
		return pattern.substring(startIndex[i], endIndex[i]);
	}

	/** Gets each numerical block. */
	public String[] getBlocks() {
		final String[] s = new String[startIndex.length];
		for (int i = 0; i < s.length; i++)
			s[i] = getBlock(i);
		return s;
	}

	/** Gets the pattern's text string before any numerical ranges. */
	public String getPrefix() {
		final int s = pattern.lastIndexOf(File.separator) + 1;
		int e;
		if (startIndex.length > 0) e = startIndex[0];
		else {
			final int dot = pattern.lastIndexOf(".");
			e = dot < s ? pattern.length() : dot;
		}
		return s <= e ? pattern.substring(s, e) : "";
	}

	/** Gets the pattern's text string after all numerical ranges. */
	public String getSuffix() {
		return endIndex.length > 0 ? pattern.substring(endIndex[endIndex.length -
			1]) : pattern;
	}

	/** Gets the pattern's text string before the given numerical block. */
	public String getPrefix(final int i) {
		if (i < 0 || i >= startIndex.length) return null;
		final int s = i > 0 ? endIndex[i - 1] : (pattern.lastIndexOf(
			File.separator) + 1);
		final int e = startIndex[i];
		return s <= e ? pattern.substring(s, e) : null;
	}

	/** Gets the pattern's text string before each numerical block. */
	public String[] getPrefixes() {
		final String[] s = new String[startIndex.length];
		for (int i = 0; i < s.length; i++)
			s[i] = getPrefix(i);
		return s;
	}

	// -- Helper methods --

	/**
	 * Recursive method for building filenames for the file listing.
	 *
	 * @throws IOException
	 */
	private void buildFiles(final String prefix, int ndx,
		final List<Location> fileList, final DataHandleService dataHandleService)
		throws IOException
	{
		if (blocks.length == 0) {
			// regex pattern

			final Location patternLocation = baseLoc.sibling(pattern);
			if (dataHandleService.exists(patternLocation)) {
				fileList.add(patternLocation);
				return;
			}

			isRegex = true;

			List<Location> localfiles = null;
			String dir;

			final int endRegex = pattern.indexOf(File.separator + "\\E") + 1;
			final int endNotRegex = pattern.lastIndexOf(File.separator) + 1;
			int end;

			// Check if an escaped path has been defined as part of the regex.
			if (pattern.startsWith("\\Q") && endRegex > 0 &&
				endRegex <= endNotRegex)
			{
				dir = pattern.substring(2, endRegex);
				end = endRegex + 2;
			}
			else {
				dir = pattern.substring(0, endNotRegex);
				end = endNotRegex;
			}
			if ("".equals(dir) || !dataHandleService.exists(baseLoc
				.sibling(dir)))
			{
				localfiles = getAllFiles(baseLoc.parent());
			}
			else {
				localfiles = getAllFiles(baseLoc.sibling(dir));
			}

			final String basePattern = pattern.substring(end);
			Pattern regex = null;
			try {
				regex = Pattern.compile(basePattern);
			}
			catch (final PatternSyntaxException e) {
				regex = Pattern.compile(pattern);
			}

			for (final Location f : localfiles) {
				if (regex.matcher(f.getName()).matches()) {
					fileList.add(f);
				}
			}
		}
		else

		{
			// compute bounds for constant (non-block) pattern fragment
			final int num = startIndex.length;
			final int n1 = ndx == 0 ? 0 : endIndex[ndx - 1];
			final int n2 = ndx == num ? pattern.length() : startIndex[ndx];
			final String pre = pattern.substring(n1, n2);

			if (ndx == 0) fileList.add(baseLoc.sibling(pre + prefix));
			else {
				final FilePatternBlock block = blocks[--ndx];
				final String[] blockElements = block.getElements();
				for (final String element : blockElements) {
					buildFiles(element + pre + prefix, ndx, fileList, dataHandleService);
				}
			}
		}
	}

	private List<Location> getAllFiles(final BrowsableLocation dir)
		throws IOException
	{
		final List<Location> subfiles = new ArrayList<>();

		final BrowsableLocation root = dir;
		final Set<BrowsableLocation> children = root.children();

		for (final BrowsableLocation child : children) {
			final List<Location> grandChildren = getAllFiles(child);
			if (grandChildren.isEmpty()) {
				subfiles.add(child);
			}
			else {
				subfiles.addAll(grandChildren);
			}
		}
		return subfiles;
	}
}

// -- Notes --

// Some patterns observed:
//
//   TAABA1.PIC TAABA2.PIC TAABA3.PIC ... TAABA45.PIC
//
//   0m.tiff 3m.tiff 6m.tiff ... 36m.tiff
//
//   cell-Z0.C0.tiff cell-Z1.C0.tiff cell-Z2.C0.tiff ... cell-Z39.C0.tiff
//   cell-Z0.C1.tiff cell-Z1.C1.tiff cell-Z2.C1.tiff ... cell-Z39.C1.tiff
//
//   CRG401.PIC
//
//   TST00101.PIC TST00201.PIC TST00301.PIC
//   TST00102.PIC TST00202.PIC TST00302.PIC
//
//   800102.pic 800202.pic 800302.pic ... 805902.pic
//   800103.pic 800203.pic 800303.pic ... 805903.pic
//
//   nd400102.pic nd400202.pic nd400302.pic ... nd406002.pic
//   nd400103.pic nd400203.pic nd400303.pic ... nd406003.pic
//
//   WTERZ2_Series13_z000_ch00.tif ... WTERZ2_Series13_z018_ch00.tif
//
// --------------------------------------------------------------------------
//
// The file pattern notation defined here encompasses all patterns above.
//
//   TAABA<1-45>.PIC
//   <0-36:3>m.tiff
//   cell-Z<0-39>.C<0-1>.tiff
//   CRG401.PIC
//   TST00<1-3>0<1-2>.PIC
//   80<01-59>0<2-3>.pic
//   nd40<01-60>0<2-3>.pic
//   WTERZ2_Series13_z0<00-18>_ch00.tif
//
// In general: <B-E:S> where B is the start number, E is the end number, and S
// is the step increment. If zero padding has been used, the start number B
// will have leading zeroes to indicate that. If the step increment is one, it
// can be omitted.
//
// --------------------------------------------------------------------------
//
// If file groups not limited to numbering need to be handled, we can extend
// the notation as follows:
//
// A pattern such as:
//
//   ABCR.PIC ABCG.PIC ABCB.PIC
//
// Could be represented as:
//
//   ABC<R|G|B>.PIC
//
// If such cases come up, they will need to be identified heuristically and
// incorporated into the detection algorithm.
//
// --------------------------------------------------------------------------
//
// Here is a sketch of the algorithm for determining the pattern from a given
// file within a particular group:
//
//   01 - Detect number blocks within the file name, marking them with stars.
//        For example:
//
//          xyz800303b.pic -> xyz<>b.pic
//
//        Where <> represents a numerical block with unknown properties.
//
//   02 - Get a file listing for all files matching the given pattern. In the
//        example above, we'd get:
//
//        xyz800102b.pic, xyz800202b.pic, ..., xyz805902b.pic,
//        xyz800103b.pic, xyz800203b.pic, ..., xyz805903b.pic
//
//   03 - There are two possibilities: "fixed width" and "variable width."
//
//        Variable width: Not all filenames are the same length in characters.
//        Assume the block only covers a single number. Extract that number
//        from each filename, sort them and analyze as described below.
//
//        Fixed width: All filenames are the same length in characters. The
//        block could represent more than one number.
//
//        First, for each character, determine if that character varies between
//        filenames. If not, lock it down, splitting the block as necessary
//        into fixed-width blocks. When finished, the above example looks like:
//
//          xyz80<2>0<1>b.pic
//
//        Where <N> represents a numerical block of width N.
//
//        For each remaining block, extract the numbers from each matching
//        filename, sort the lists, and analyze as described below.
//
//   04 - In either case, analyze each list of numbers. The first on the list
//        is B. The last one is E. And S is the second one minus B. But check
//        the list to make sure no numbers are missing for that step size.
//
// NOTE: The fixed width algorithm above is insufficient for patterns like
// "0101.pic" through "2531.pic," where no fixed constant pads the two
// numerical counts. An additional step is required, as follows:
//
//   05 - For each fixed-width block, recursively divide it into pieces, and
//        analyze the numerical scheme according to those pieces. For example,
//        in the problem case given above, we'd have:
//
//          <4>.pic
//
//        Recursively, we'd analyze:
//
//          <4>.pic
//          <3><R1>.pic
//          <2><R2>.pic
//          <1><R3>.pic
//
//        The <Rx> blocks represent recursive calls to analyze the remainder of
//        the width.
//
//        The function decides if a given combination of widths is valid by
//        determining if each individual width is valid. An individual width
//        is valid if the computed B, S and E properly cover the numerical set.
//
//        If no combination of widths is found to be valid, the file numbering
//        is screwy. Print an error message.
