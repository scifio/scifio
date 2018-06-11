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

package io.scif.util;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.filters.MetadataWrapper;
import io.scif.io.RandomAccessOutputStream;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
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
	 * Returns true if the provided axes correspond to a complete image plane
	 */
	public static boolean wholePlane(final int imageIndex, final Metadata meta,
		final Interval bounds)
	{
		final boolean wholePlane = wholeRow(imageIndex, meta, bounds);
		final int yIndex = meta.get(imageIndex).getAxisIndex(Axes.Y);
		return wholePlane && bounds.min(yIndex) == 0 && bounds.max(yIndex) == meta
			.get(imageIndex).getAxisLength(Axes.Y) - 1;
	}

	/**
	 * Returns true if the provided axes correspond to a complete image row
	 */
	public static boolean wholeRow(final int imageIndex, final Metadata meta,
		final Interval bounds)
	{
		final int yIndex = meta.get(imageIndex).getAxisIndex(Axes.Y);

		for (int d = 0; d < bounds.numDimensions(); d++) {
			if (d == yIndex) continue;
			final long length = meta.get(imageIndex).getAxisLength(d);
			if (bounds.min(d) != 0 || bounds.dimension(d) != length) return false;
		}

		return true;
	}

	/**
	 * Replaces the first values.length of the provided Metadata's planar axes
	 * with the values.
	 */
	public static long[] modifyPlanar(final int imageIndex, final Metadata meta,
		final long... values)
	{
		final AxisValue[] axes = new AxisValue[values.length];
		final List<CalibratedAxis> axisTypes = meta.get(imageIndex).getAxes();

		for (int i = 0; i < axes.length && i < axisTypes.size(); i++) {
			axes[i] = new AxisValue(axisTypes.get(i).type(), values[i]);
		}

		return modifyPlanar(imageIndex, meta, axes);
	}

	/**
	 * Iterates over the provided Metadata's planar axes, replacing any instances
	 * of axes with the paired values.
	 */
	public static long[] modifyPlanar(final int imageIndex, final Metadata meta,
		final AxisValue... axes)
	{
		final long[] planarAxes = meta.get(imageIndex).getAxesLengthsPlanar();

		for (final AxisValue v : axes) {
			planarAxes[meta.get(imageIndex).getAxisIndex(v.getType())] = v
				.getLength();
		}

		return planarAxes;
	}

	/**
	 * Casts the provided Metadata object to the generic type of this method.
	 * <p>
	 * Usage: To cast a Metadata instance to ConcreteMetadata, use:
	 * {@code SCIFIOMetadataTools.<ConcreteMetadata>castMeta(meta)}.
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

		if (src.get(imageIndex).getAxes().size() == 0) {
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

	// -- Utility class --

	/**
	 * Helper class that pairs an AxisType with a length.
	 *
	 * @author Mark Hiner
	 */
	public static class AxisValue {

		private CalibratedAxis type;

		private long length;

		public AxisValue(final AxisType type, final long length) {
			this.type = FormatTools.createAxis(type);
			this.length = length;
		}

		public long getLength() {
			return length;
		}

		public void setLength(final long length) {
			this.length = length;
		}

		public AxisType getType() {
			return type.type();
		}

		public void setType(final CalibratedAxis type) {
			this.type = type;
		}
	}
}
