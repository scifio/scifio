/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.util;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.io.RandomAccessOutputStream;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

/**
 * A utility class for working with {@link io.scif.Metadata} objects.
 * 
 * @see io.scif.Metadata
 * @author Mark Hiner
 */
public class SCIFIOMetadataTools {

	// -- Constructor --

	private SCIFIOMetadataTools() {}

	// -- Utility Methods -- DatasetMetadata --

	/**
	 * Returns true if the provided axes correspond to a complete image plane
	 */
	public static boolean wholePlane(final int imageIndex, Metadata meta,
		final long[] planeMin, final long[] planeMax)
	{
		final boolean wholePlane = wholeRow(imageIndex, meta, planeMin, planeMax);
		final int yIndex = meta.getAxisIndex(imageIndex, Axes.Y);
		return wholePlane && planeMin[yIndex] == 0 &&
			planeMax[yIndex] == meta.getAxisLength(imageIndex, Axes.Y);
	}

	/**
	 * Returns true if the provided axes correspond to a complete image row
	 */
	public static boolean wholeRow(final int imageIndex, Metadata meta,
		final long[] planeMin, final long[] planeMax)
	{
		boolean wholeRow = true;
		final int yIndex = meta.getAxisIndex(imageIndex, Axes.Y);

		for (int i = 0; wholeRow && i < planeMin.length; i++) {
			if (i == yIndex) continue;
			if (planeMin[i] != 0 || planeMax[i] != meta.getAxisLength(imageIndex, i)) wholeRow =
				false;
		}

		return wholeRow;
	}

	/**
	 * Replaces the X and Y lengths of the provided Metadata's planar axes with
	 * the specified values.
	 */
	public static long[] modifyPlanarXY(final int imageIndex,
		final Metadata meta, final long x, final long y)
	{
		return modifyPlanar(imageIndex, meta, new AxisValue(Axes.X, x),
			new AxisValue(Axes.Y, y));
	}

	/**
	 * Iterates over the provided Metadata's planar axes, replacing any instances
	 * of axes with the paired values.
	 */
	public static long[] modifyPlanar(final int imageIndex, final Metadata meta,
		final AxisValue... axes)
	{
		final long[] planarAxes = meta.getAxesLengthsPlanar(imageIndex);

		for (final AxisValue v : axes) {
			planarAxes[meta.getAxisIndex(imageIndex, v.getType())] = v.getLength();
		}

	    return planarAxes;
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
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe an Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out) throws FormatException
	{
		verifyMinimumPopulated(src, out, 0, 0);
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe an Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out, final int imageIndex)
		throws FormatException
	{
		verifyMinimumPopulated(src, out, imageIndex, 0);
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe the nth Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out, final int imageIndex,
		final int planeIndex) throws FormatException
	{
		if (src == null) {
			throw new FormatException("Metadata object is null; "
				+ "call Writer.setMetadata() first");
		}

		if (out == null) {
			throw new FormatException("RandomAccessOutputStream object is null; "
				+ "call Writer.setSource(<String/File/RandomAccessOutputStream>) first");
		}

		if (src.getAxisCount(0) == 0) {
			throw new FormatException("Axiscount #" + imageIndex + " is 0");
		}
	}

	/**
	 * Populates the provided ImageMetadata. Automatically looks up bits per pixel
	 * for the provided pixel type.
	 */
	public static void populate(final ImageMetadata iMeta,
		final CalibratedAxis[] axisTypes, final long[] axisLengths, final int pixelType,
		final boolean orderCertain, final boolean littleEndian,
		final boolean indexed, final boolean falseColor,
		final boolean metadataComplete)
	{
		populate(iMeta, axisTypes, axisLengths, pixelType, FormatTools
			.getBitsPerPixel(pixelType), orderCertain, littleEndian, indexed,
			falseColor, metadataComplete);
	}

	/**
	 * Populates the provided ImageMetadata.
	 */
	public static void populate(final ImageMetadata iMeta,
		final CalibratedAxis[] axisTypes, final long[] axisLengths,
		final int pixelType, final int bitsPerPixel, final boolean orderCertain,
		final boolean littleEndian, final boolean indexed,
		final boolean falseColor, final boolean metadataComplete)
	{
		iMeta.setPixelType(pixelType);
		iMeta.setBitsPerPixel(bitsPerPixel);
		iMeta.setOrderCertain(orderCertain);
		iMeta.setLittleEndian(littleEndian);
		iMeta.setIndexed(indexed);
		iMeta.setFalseColor(falseColor);
		iMeta.setMetadataComplete(metadataComplete);
		iMeta.setAxes(axisTypes, axisLengths);
	}

	// Utility methods -- original metadata --

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
