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

import io.scif.io.Location;

import java.io.IOException;
import java.util.Arrays;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * AxisGuesser guesses which blocks in a file pattern correspond to which
 * dimensional axes (Z, T or C), potentially recommending an adjustment in
 * dimension order within the files, depending on the confidence of each guess.
 *
 * @author Curtis Rueden
 */
public class AxisGuesser {

	// -- Constants --

	/** Axis type for unclassified axes. */
	public static final int UNKNOWN_AXIS = 0;

	/** Axis type for focal planes. */
	public static final int Z_AXIS = 1;

	/** Axis type for time points. */
	public static final int T_AXIS = 2;

	/** Axis type for channels. */
	public static final int C_AXIS = 3;

	/** Axis type for series. */
	public static final int S_AXIS = 4;

	/** Prefix endings indicating space dimension. */
	private static final String[] Z = { "fp", "sec", "z", "zs", "focal",
		"focalplane" };

	/** Prefix endings indicating time dimension. */
	private static final String[] T = { "t", "tl", "tp", "time" };

	/** Prefix endings indicating channel dimension. */
	private static final String[] C = { "c", "ch", "w", "wavelength" };

	/** Prefix endings indicating series dimension. */
	private static final String[] S = { "s", "series", "sp" };

	private static final String ONE = "1";

	private static final String TWO = "2";

	private static final String THREE = "3";

	// -- Fields --

	/** File pattern identifying dimensional axis blocks. */
	private final FilePattern fp;

	/** Original ordering of internal dimensional axes. */
	private final AxisType[] dimOrder;

	/** Adjusted ordering of internal dimensional axes. */
	private final AxisType[] newOrder;

	/** Guessed axis types. */
	private int[] axisTypes;

	/** Whether the guesser is confident that all axis types are correct. */
	private boolean certain;

	// -- Constructor --

	/**
	 * Guesses dimensional axis assignments corresponding to the given file
	 * pattern, using the specified dimensional information from within each file
	 * as a guide.
	 *
	 * @param fp The file pattern of the files
	 * @param dimOrder The dimension order (e.g., XYZTC) within each file
	 * @param sizeZ The number of Z positions within each file
	 * @param sizeT The number of T positions within each file
	 * @param sizeC The number of C positions within each file
	 * @param isCertain Whether the dimension order given is known to be good, or
	 *          merely a guess
	 * @see FilePattern
	 */
	public AxisGuesser(final FilePattern fp, final AxisType[] dimOrder,
		long sizeZ, long sizeT, final long sizeC, final boolean isCertain)
	{
		this.fp = fp;
		this.dimOrder = dimOrder;

		newOrder = dimOrder;
		final String[] prefixes = fp.getPrefixes();
		final String suffix = fp.getSuffix();
		final String[][] elements = fp.getElements();
		axisTypes = new int[elements.length];
		boolean foundZ = false, foundT = false, foundC = false;

		// -- 1) fill in "known" axes based on known patterns and conventions --

		for (int i = 0; i < axisTypes.length; i++) {
			String p = prefixes[i].toLowerCase();

			// strip trailing digits and divider characters
			final char[] ch = p.toCharArray();
			int l = ch.length - 1;
			while (l >= 0 && (ch[l] >= '0' && ch[l] <= '9' || ch[l] == ' ' ||
				ch[l] == '-' || ch[l] == '_' || ch[l] == '.'))
			{
				l--;
			}

			// useful prefix segment consists of trailing alphanumeric
			// characters
			int f = l;
			while (f >= 0 && ch[f] >= 'a' && ch[f] <= 'z')
				f--;
			p = p.substring(f + 1, l + 1);

			// check against known Z prefixes
			for (final String zPre : Z) {
				if (p.equals(zPre)) {
					axisTypes[i] = Z_AXIS;
					foundZ = true;
					break;
				}
			}
			if (axisTypes[i] != UNKNOWN_AXIS) continue;

			// check against known T prefixes
			for (final String tPre : T) {
				if (p.equals(tPre)) {
					axisTypes[i] = T_AXIS;
					foundT = true;
					break;
				}
			}
			if (axisTypes[i] != UNKNOWN_AXIS) continue;

			// check against known C prefixes
			for (final String cPre : C) {
				if (p.equals(cPre)) {
					axisTypes[i] = C_AXIS;
					foundC = true;
					break;
				}
			}
			if (axisTypes[i] != UNKNOWN_AXIS) continue;

			// check against known series prefixes
			for (final String sPre : S) {
				if (p.equals(sPre)) {
					axisTypes[i] = S_AXIS;
					break;
				}
			}
			if (axisTypes[i] != UNKNOWN_AXIS) continue;

			// check special case: <2-3>, <1-3> (Bio-Rad PIC)
			if (suffix.equalsIgnoreCase(".pic") && i == axisTypes.length - 1 &&
				((elements[i].length == 2 && (elements[i][0].equals(ONE) ||
					elements[i][0].equals(TWO)) && (elements[i][1].equals(TWO) ||
						elements[i][1].equals(THREE))) || (elements[i].length == 3 &&
							elements[i][0].equals(ONE) && elements[i][1].equals(TWO) &&
							elements[i][2].equals(THREE))))
			{
				axisTypes[i] = C_AXIS;
				continue;
			}
			else if (elements[i].length == 2 || elements[i].length == 3) {
				final char first = elements[i][0].toLowerCase().charAt(0);
				final char second = elements[i][1].toLowerCase().charAt(0);
				final char third = elements[i].length == 2 ? 'b' : elements[i][2]
					.toLowerCase().charAt(0);

				if ((first == 'r' || second == 'r' || third == 'r') && (first == 'g' ||
					second == 'g' || third == 'g') && (first == 'b' || second == 'b' ||
						third == 'b'))
				{
					axisTypes[i] = C_AXIS;
					continue;
				}
			}
		}

		// -- 2) check for special cases where dimension order should be swapped
		// --

		if (!isCertain) { // only switch if dimension order is uncertain
			if (foundZ && !foundT && sizeZ > 1 && sizeT == 1 || foundT && !foundZ &&
				sizeT > 1 && sizeZ == 1)
			{
				// swap Z and T dimensions
				final int indexZ = indexOf(newOrder, Axes.Z);
				final int indexT = indexOf(newOrder, Axes.TIME);
				final long sz = sizeT;
				newOrder[indexZ] = Axes.TIME;
				newOrder[indexT] = Axes.Z;
				sizeT = sizeZ;
				sizeZ = sz;
			}
		}

		// -- 3) fill in remaining axis types --

		boolean canBeZ = !foundZ && sizeZ == 1;
		boolean canBeT = !foundT && sizeT == 1;
		boolean canBeC = !foundC && sizeC == 1;

		certain = isCertain;

		for (int i = 0; i < axisTypes.length; i++) {
			if (axisTypes[i] != UNKNOWN_AXIS) continue;
			certain = false;

			if (canBeZ) {
				axisTypes[i] = Z_AXIS;
				canBeZ = false;
			}
			else if (canBeT) {
				axisTypes[i] = T_AXIS;
				canBeT = false;
			}
			else if (canBeC) {
				axisTypes[i] = C_AXIS;
				canBeC = false;
			}
			else {
				final AxisType lastAxis = newOrder[newOrder.length - 1];
				if (lastAxis.equals(Axes.CHANNEL)) {
					axisTypes[i] = C_AXIS;
				}
				else if (lastAxis.equals(Axes.Z)) {
					axisTypes[i] = Z_AXIS;
				}
				else axisTypes[i] = T_AXIS;
			}
		}
	}

	// -- AxisGuesser API methods --

	/** Gets the file pattern. */
	public FilePattern getFilePattern() {
		return fp;
	}

	/** Gets the original dimension order. */
	public AxisType[] getOriginalOrder() {
		return dimOrder;
	}

	/** Gets the adjusted dimension order. */
	public AxisType[] getAdjustedOrder() {
		return newOrder;
	}

	/** Gets whether the guesser is confident that all axes are correct. */
	public boolean isCertain() {
		return certain;
	}

	/**
	 * Gets the guessed axis type for each dimensional block.
	 *
	 * @return An array containing values from the enumeration:
	 *         <ul>
	 *         <li>Z_AXIS: focal planes</li>
	 *         <li>T_AXIS: time points</li>
	 *         <li>C_AXIS: channels</li>
	 *         <li>S_AXIS: series</li>
	 *         </ul>
	 */
	public int[] getAxisTypes() {
		return axisTypes;
	}

	/**
	 * Sets the axis type for each dimensional block.
	 *
	 * @param axes An array containing values from the enumeration:
	 *          <ul>
	 *          <li>Z_AXIS: focal planes</li>
	 *          <li>T_AXIS: time points</li>
	 *          <li>C_AXIS: channels</li>
	 *          <li>S_AXIS: series</li>
	 *          </ul>
	 */
	public void setAxisTypes(final int[] axes) {
		axisTypes = axes;
	}

	/** Gets the number of Z axes in the pattern. */
	public int getAxisCountZ() {
		return getAxisCount(Z_AXIS);
	}

	/** Gets the number of T axes in the pattern. */
	public int getAxisCountT() {
		return getAxisCount(T_AXIS);
	}

	/** Gets the number of C axes in the pattern. */
	public int getAxisCountC() {
		return getAxisCount(C_AXIS);
	}

	/** Gets the number of S axes in the pattern. */
	public int getAxisCountS() {
		return getAxisCount(S_AXIS);
	}

	/**
	 * Gets the number of axes in the pattern of the given type.
	 *
	 * @param axisType One of:
	 *          <ul>
	 *          <li>Z_AXIS: focal planes</li>
	 *          <li>T_AXIS: time points</li>
	 *          <li>C_AXIS: channels</li>
	 *          <li>S_AXIS: series</li>
	 *          </ul>
	 */
	public int getAxisCount(final int axisType) {
		int num = 0;
		for (final int type : axisTypes) {
			if (type == axisType) num++;
		}
		return num;
	}

	// -- Static API methods --

	/** Returns a best guess of the given label's axis type. */
	public static int getAxisType(final String label) {
		final String lowerLabel = label.toLowerCase();
		for (final String p : Z) {
			if (p.equals(lowerLabel) || lowerLabel.endsWith(p)) return Z_AXIS;
		}
		for (final String p : C) {
			if (p.equals(lowerLabel) || lowerLabel.endsWith(p)) return C_AXIS;
		}
		for (final String p : T) {
			if (p.equals(lowerLabel) || lowerLabel.endsWith(p)) return T_AXIS;
		}
		for (final String p : S) {
			if (p.equals(lowerLabel) || lowerLabel.endsWith(p)) return S_AXIS;
		}
		return UNKNOWN_AXIS;
	}

	// -- Helper methods --

	private int indexOf(final AxisType[] axes, final AxisType type) {
		int index = -1;
		for (int i = 0; i < axes.length && index == -1; i++) {
			if (axes[i].equals(type)) index = i;
		}
		return index;
	}

	// -- Main method --

	public static void main(final String[] args) throws FormatException,
		IOException
	{
		main(args, new Context());
	}

	/** Method for testing pattern guessing logic. */
	public static void main(final String[] args, final Context context)
		throws FormatException, IOException
	{
		final SCIFIO scifio = new SCIFIO(context);
		final LogService log = scifio.log();
		final Location file = args.length < 1 ? new Location(context, System
			.getProperty("user.dir")).listFiles()[0] : new Location(context, args[0]);
		log.info("File = " + file.getAbsoluteFile());
		final String pat = scifio.filePattern().findPattern(file);
		if (pat == null) log.info("No pattern found.");
		else {
			log.info("Pattern = " + pat);
			final FilePattern fp = new FilePattern(context, pat);
			if (fp.isValid()) {
				log.info("Pattern is valid.");
				final String id = fp.getFiles()[0];
				if (!new Location(context, id).exists()) {
					log.info("File '" + id + "' does not exist.");
				}
				else {
					// read dimensional information from first file
					log.info("Reading first file ");
					final Reader reader = scifio.initializer().initializeReader(id);
					final AxisType[] dimOrder = (AxisType[]) reader.getMetadata().get(0)
						.getAxes().toArray();
					final long sizeZ = reader.getMetadata().get(0).getAxisLength(Axes.Z);
					final long sizeT = reader.getMetadata().get(0).getAxisLength(
						Axes.TIME);
					final long sizeC = reader.getMetadata().get(0).getAxisLength(
						Axes.CHANNEL);
					final boolean certain = reader.getMetadata().get(0).isOrderCertain();
					reader.close();
					log.info("[done]");
					log.info("\tdimOrder = " + Arrays.toString(dimOrder) + " (" + (certain
						? "certain" : "uncertain") + ")");
					log.info("\tsizeZ = " + sizeZ);
					log.info("\tsizeT = " + sizeT);
					log.info("\tsizeC = " + sizeC);

					// guess axes
					final AxisGuesser ag = new AxisGuesser(fp, dimOrder, sizeZ, sizeT,
						sizeC, certain);

					// output results
					final String[] blocks = fp.getBlocks();
					final String[] prefixes = fp.getPrefixes();
					final int[] axes = ag.getAxisTypes();
					final AxisType[] newOrder = ag.getAdjustedOrder();
					final boolean isCertain = ag.isCertain();
					log.info("Axis types:");
					for (int i = 0; i < blocks.length; i++) {
						String axis;
						switch (axes[i]) {
							case Z_AXIS:
								axis = "Z";
								break;
							case T_AXIS:
								axis = "T";
								break;
							case C_AXIS:
								axis = "C";
								break;
							default:
								axis = "?";
						}
						log.info("\t" + blocks[i] + "\t" + axis + " (prefix = " +
							prefixes[i] + ")");
					}
					if (!Arrays.equals(dimOrder, newOrder)) {
						log.info("Adjusted dimension order = " + Arrays.toString(newOrder) +
							" (" + (isCertain ? "certain" : "uncertain") + ")");
					}
				}
			}
			else log.info("Pattern is invalid: " + fp.getErrorMessage());
		}
	}

}

// -- Notes --

// INPUTS: file pattern, dimOrder, sizeZ, sizeT, sizeC, isCertain
//
// 1) Fill in all "known" dimensional axes based on known patterns and
//    conventions
//      * known internal axes (ZCT) have isCertain == true
//      * known dimensional axes have a known pattern or convention
//    After that, we are left with only unknown slots, which we must guess.
//
// 2) First, we decide whether we really "believe" the reader. There is a
//    special case where we may decide that it got Z and T mixed up:
//      * if a Z block was found, but not a T block:
//          if !isOrderCertain, and sizeZ > 1, and sizeT == 1, swap 'em
//      * else if a T block was found, but not a Z block:
//          if !isOrderCertain and sizeT > 1, and sizeZ == 1, swap 'em
//    At this point, we can (have to) trust the internal ordering, and use it
//    to decide how to fill in the remaining dimensional blocks.
//
// 3) Set canBeZ to true iff no Z block is assigned and sizeZ == 1.
//    Set canBeT to true iff no T block is assigned and sizeT == 1.
//    Go through the blocks in order from left to right:
//      * If canBeZ, assign Z and set canBeZ to false.
//      * If canBeT, assign T and set canBeT to false.
//      * Otherwise, assign C.
//
// OUTPUTS: list of axis assignments, new dimOrder
