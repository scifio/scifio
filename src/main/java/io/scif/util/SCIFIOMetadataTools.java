/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.filters.MetadataWrapper;
import io.scif.io.RandomAccessOutputStream;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.space.DefaultCalibratedSpace;
import net.imglib2.Interval;

/**
 * A utility class for working with {@link io.scif.Metadata} objects.
 *
 * @see io.scif.Metadata
 * @author Mark Hiner
 */
public class SCIFIOMetadataTools {

	// -- Constructor --

	private SCIFIOMetadataTools() {}

	// -- Utility Methods -- Metadata --

	/**
	 * Counts the number of interleaved axes for specified image of the given
	 * {@link Metadata}
	 */
	public static int countInterleavedAxes(final Metadata meta,
		final int imageIndex)
	{
		return countInterleavedAxes(meta.get(imageIndex));
	}

	/**
	 * Counts the number of interleaved axes in a given {@link ImageMetadata}
	 */
	public static int countInterleavedAxes(final ImageMetadata iMeta) {
		return getInterleavedAxes(iMeta).numDimensions();
	}

	/**
	 * Returns the interleaved axes for specified image of the given
	 * {@link Metadata}
	 */
	public static DefaultCalibratedSpace getInterleavedAxes(
		final Metadata metadata, final int imageIndex)
	{
		return getInterleavedAxes(metadata.get(imageIndex));
	}

	/**
	 * Returns the interleaved axes in a given {@link ImageMetadata}
	 */
	public static DefaultCalibratedSpace getInterleavedAxes(
		final ImageMetadata iMeta)
	{
		return getPlanarAxes(iMeta, false);
	}

	/**
	 * Counts the number of planar axes for specified image of the given
	 * {@link Metadata}
	 */
	public static int countPlanarAxes(final Metadata meta, final int imageIndex) {
		return countPlanarAxes(meta.get(imageIndex));
	}

	/**
	 * Counts the number of planar axes in a given {@link ImageMetadata}
	 */
	public static int countPlanarAxes(final ImageMetadata iMeta) {
		return getPlanarAxes(iMeta).numDimensions();
	}

	/**
	 * Returns the planar axes for specified image of the given {@link Metadata}
	 */
	public static DefaultCalibratedSpace getPlanarAxes(final Metadata metadata,
		final int imageIndex)
	{
		return getPlanarAxes(metadata.get(imageIndex));
	}

	/**
	 * Returns the planar axes in a given {@link ImageMetadata}
	 */
	public static DefaultCalibratedSpace getPlanarAxes(
		final ImageMetadata iMeta)
	{
		return getPlanarAxes(iMeta, true);
	}

	/**
	 * @param metadata
	 * @param imageIndex
	 * @return True if {@link Axes#CHANNEL} appears before {@link Axes#X} or
	 *         {@link Axes#Y} in the specified image of the given {@link Metadata}
	 *         .
	 */
	public boolean isMultichannel(final Metadata metadata, final int imageIndex) {
		return isMultichannel(metadata.get(imageIndex));
	}

	/**
	 * @param iMeta
	 * @return True if {@link Axes#CHANNEL} appears before {@link Axes#X} or
	 *         {@link Axes#Y} in the target {@link ImageMetadata}.
	 */
	public boolean isMultichannel(final ImageMetadata iMeta) {
		final int cIndex = iMeta.dimensionIndex(Axes.CHANNEL);
		return cIndex < iMeta.dimensionIndex(Axes.X) || cIndex < iMeta
			.dimensionIndex(Axes.Y);
	}

	/**
	 * Returns true if the provided axes correspond to a complete image plane
	 */
	public static boolean wholeBlock(final int imageIndex, final Metadata meta,
		final Interval range)
	{
		final boolean wholePlane = wholeRow(imageIndex, meta, range);
		final int yIndex = meta.get(imageIndex).dimensionIndex(Axes.Y);
		return wholePlane && range.min(yIndex) == 0 && range.dimension(
			yIndex) == meta.get(imageIndex).dimension(Axes.Y);
	}

	/**
	 * Returns true if the provided axes correspond to a complete image row
	 */
	public static boolean wholeRow(final int imageIndex, final Metadata meta,
		final Interval range)
	{
		boolean wholeRow = true;
		final int yIndex = meta.get(imageIndex).dimensionIndex(Axes.Y);

		for (int i = 0; wholeRow && i < range.numDimensions(); i++) {
			if (i == yIndex) continue;
			if (range.min(i) != 0 || range.dimension(i) != meta.get(imageIndex)
				.dimension(i)) wholeRow = false;
		}

		return wholeRow;
	}

	/**
	 * Casts the provided Metadata object to the generic type of this method.
	 * <p>
	 * Usage: To cast a Metadata instance to ConcreteMetadata, use:
	 * <p>
	 * {@code SCIFIOMetadataTools.<ConcreteMetadata>castMeta(meta)}
	 * </p>
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public static <M extends Metadata> M castMeta(final Metadata meta) {
		// TODO need to check for safe casting here..

		return (M) meta;
	}

	/**
	 * Unwraps the provided {@link Metadata} class if it has been wrapped in
	 * {@link MetadataWrapper}(s).
	 *
	 * @param meta Metadata instance to unwrap
	 * @return If meta is a MetadataWrapper, the tail wrapped Metadata. Otherwise
	 *         meta is returned directly.
	 */
	public static Metadata unwrapMetadata(final Metadata meta) {
		Metadata unwrappedMeta = meta;

		// Unwrap MetadataWrappers to get to the actual format-specific metadata
		while (unwrappedMeta instanceof MetadataWrapper) {
			unwrappedMeta = ((MetadataWrapper) unwrappedMeta).unwrap();
		}

		return unwrappedMeta;
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe an Image.
	 *
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out) throws FormatException
	{
		verifyMinimumPopulated(src, out, 0);
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe the nth Image.
	 *
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out, final int imageIndex)
			throws FormatException
	{
		if (src == null) {
			throw new FormatException("Metadata object is null; " +
				"call Writer.setMetadata() first");
		}

		if (out == null) {
			throw new FormatException("RandomAccessOutputStream object is null; " +
				"call Writer.setSource(<String/File/RandomAccessOutputStream>) first");
		}

		if (src.get(imageIndex).numDimensions() == 0) {
			throw new FormatException("Axiscount #" + imageIndex + " is 0");
		}
	}

	// -- Utility methods -- dimensional axes --

	/**
	 * Guesses at a reasonable default planar axis count for the given list of
	 * dimensional axes.
	 * <p>
	 * The heuristic looks for the first index following both {@link Axes#X} and
	 * {@link Axes#Y}. If the list does not contain both {@code X} and {@code Y}
	 * then the guess will equal the total number of axes.
	 * </p>
	 */
	public static int guessPlanarAxisCount(final List<CalibratedAxis> axes) {
		boolean xFound = false, yFound = false;
		int d;
		for (d = 0; d < axes.size(); d++) {
			if (xFound && yFound) break;
			final AxisType type = axes.get(d).type();
			if (type == Axes.X) xFound = true;
			else if (type == Axes.Y) yFound = true;
		}
		return d;
	}

	/**
	 * Guesses at a reasonable default interleaved axis count for the given list
	 * of dimensional axes.
	 * <p>
	 * The heuristic looks for the last index preceding both {@link Axes#X} and
	 * {@link Axes#Y}. If the list does not contain either {@code X} or {@code Y}
	 * then the guess will equal the total number of axes.
	 * </p>
	 */
	public static int guessInterleavedAxisCount(final List<CalibratedAxis> axes) {
		for (int d = 0; d < axes.size(); d++) {
			final AxisType type = axes.get(d).type();
			if (type == Axes.X || type == Axes.Y) return d;
		}
		return axes.size();
	}

	// -- Utility methods -- original metadata --

	/**
	 * Merges the given lists of metadata, prepending the specified prefix for the
	 * destination keys.
	 */
	public static void merge(final Map<String, Object> src,
		final Map<String, Object> dest, final String prefix)
	{
		for (final String key : src.keySet()) {
			dest.put(prefix + key, src.get(key));
		}
	}

	/** Gets a sorted list of keys from the given hashtable. */
	public static String[] keys(final Hashtable<String, Object> meta) {
		final String[] keys = new String[meta.size()];
		meta.keySet().toArray(keys);
		Arrays.sort(keys);
		return keys;
	}

	// -- Utility Methods --

	/**
	 * Helper method to get all the planar axes in a given ImageMetadata. Returns
	 * the minimal set of leading axes that includes both {@link Axes.X} and
	 * {@link Axes.Y}. Whether or not X and Y are themselves included in the
	 * returned set can be controlled with {@code includeXY} - if false, the
	 * resulting set is the list of interleaved axes.
	 */
	private static DefaultCalibratedSpace getPlanarAxes(final ImageMetadata iMeta,
		final boolean includeXY)
	{
		final List<CalibratedAxis> axes = new ArrayList<CalibratedAxis>();

		int xyCount = 0;

		// Iterate over the axes in the target ImageMetadata.
		// Once we have seen both Axes.X and Axes.Y we have identified all planar
		// axes.
		for (int i = 0; xyCount < 2 && i < iMeta.numDimensions(); i++) {
			final CalibratedAxis axis = iMeta.axis(i);
			if (axis.type().equals(Axes.X) || axis.type().equals(Axes.Y)) {
				if (includeXY) axes.add(axis);
				xyCount++;
			}
			else axes.add(axis);
		}

		return new DefaultCalibratedSpace(axes);
	}
}
