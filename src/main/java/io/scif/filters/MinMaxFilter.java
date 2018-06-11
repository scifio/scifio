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

package io.scif.filters;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * Logic to compute minimum and maximum values for each plane. For each plane,
 * the min/max values for a given value of a specific planar axis can also be
 * queried.
 */
@Plugin(type = Filter.class)
public class MinMaxFilter extends AbstractReaderFilter {

	// -- Fields --

	/**
	 * For each image in the dataset, map each planar axis type to a list of
	 * minimum values for each index of that plane.
	 */
	private List<Map<AxisType, double[]>> planarAxisMin;

	/**
	 * For each image in the dataset, map each planar axis type to a list of
	 * maximum values for each index of that plane.
	 */
	private List<Map<AxisType, double[]>> planarAxisMax;

	/** Minimum values for each plane, for each image. */
	private double[][] planeMins;

	/** Maximum values for each plane, for each image. */
	private double[][] planeMaxs;

	/**
	 * Number of planes for which min/max computations have been completed, per
	 * image.
	 */
	private int[] minMaxDone;

	// -- MinMaxFilter API methods --

	/**
	 * Retrieves a specified planar axis's global minimum. Returns null if some of
	 * the image planes have not been read.
	 */
	public Double getAxisGlobalMinimum(final int imageIndex, final AxisType type,
		final int index) throws FormatException
	{
		return getAxisGlobalValue(imageIndex, type, index, planarAxisMin);
	}

	/**
	 * Retrieves a specified planar axis's global maximum. Returns null if some of
	 * the image planes have not been read.
	 */
	public Double getAxisGlobalMaximum(final int imageIndex, final AxisType type,
		final int index) throws FormatException
	{
		return getAxisGlobalValue(imageIndex, type, index, planarAxisMax);
	}

	/**
	 * Retrieves the specified planar axis's minimum based on the images that have
	 * been read. Returns null if no image planes have been read yet.
	 */
	public Double getAxisKnownMinimum(final int imageIndex, final AxisType type,
		final int index)
	{
		return getAxisKnownValue(imageIndex, type, index, planarAxisMin);
	}

	/**
	 * Retrieves the specified planar axis's maximum based on the images that have
	 * been read. Returns null if no image planes have been read yet.
	 */
	public Double getAxisKnownMaximum(final int imageIndex, final AxisType type,
		final int index)
	{
		return getAxisKnownValue(imageIndex, type, index, planarAxisMax);
	}

	/**
	 * Retrieves the minimum pixel value for each planar axis of the specified
	 * plane. Returns null if the plane has not already been read.
	 */
	public Double getPlaneMinimum(final int imageIndex, final long planeIndex) {
		return getPlaneValue(imageIndex, planeIndex, planeMins);
	}

	/**
	 * Retrieves the maximum pixel value for each planar axis of the specified
	 * plane. Returns null if the plane has not already been read.
	 */
	public Double getPlaneMaximum(final int imageIndex, final long planeIndex) {
		return getPlaneValue(imageIndex, planeIndex, planeMaxs);
	}

	/**
	 * Returns true if the values returned by getAxisGlobalMinimum/Maximum can be
	 * trusted.
	 */
	public boolean isMinMaxPopulated(final int imageIndex) {
//		FormatTools.assertId(getCurrentFile(), true, 2);
		return minMaxDone != null && minMaxDone[imageIndex] == getImageCount();
	}

	// -- IFormatReader API methods --

	@Override
	public long getPlaneCount(final int imageIndex) {
		return getMetadata().get(imageIndex).getPlaneCount();
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, bounds, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, plane, bounds, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		final Interval bounds = new FinalInterval(//
			getMetadata().get(imageIndex).getAxesLengthsPlanar());
		return openPlane(imageIndex, planeIndex, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Interval bounds = new FinalInterval(//
			getMetadata().get(imageIndex).getAxesLengthsPlanar());
		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Plane plane = createPlane(bounds);
		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds, final SCIFIOConfig config)
		throws FormatException, IOException
	{
//		FormatTools.assertId(getCurrentFile(), true, 2);
		super.openPlane(imageIndex, planeIndex, plane, bounds, config);

		final int bytesPerPixel = FormatTools.getBytesPerPixel(//
			getMetadata().get(imageIndex).getPixelType());
		final int len = (int) (bytesPerPixel * Intervals.numElements(bounds));
		updateMinMax(imageIndex, planeIndex, plane.getBytes(), len);
		return plane;
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		if (!fileOnly) {
			planarAxisMin = null;
			planarAxisMax = null;
			planeMins = null;
			planeMaxs = null;
			minMaxDone = null;
		}
	}

	// -- IFormatHandler API methods --

	public Class<?> getNativeDataType() {
		return byte[].class;
	}

	// -- Helper methods --

	/**
	 * Updates min/max values based on the given byte array.
	 *
	 * @param imageIndex the image index within the dataset
	 * @param planeIndex the plane index within the image.
	 * @param buf a pre-allocated buffer.
	 * @param len as {@code buf} may be larger than the actual pixel count having
	 *          been written to it, the length (in bytes) of the those pixels.
	 */
	private void updateMinMax(final int imageIndex, final long planeIndex,
		final byte[] buf, final int len)
	{
		if (buf == null) return;
		initMinMax();

		final Metadata m = getMetadata();
		final ImageMetadata iMeta = m.get(imageIndex);
		final int pixelType = iMeta.getPixelType();
		final int bpp = FormatTools.getBytesPerPixel(pixelType);
		final long planeSize = iMeta.getPlaneSize();
		// check whether min/max values have already been computed for this
		// plane
		// and that the buffer requested is actually the entire plane
		if (len == planeSize && !Double.isNaN(
			planeMins[imageIndex][(int) planeIndex])) return;

		final boolean little = iMeta.isLittleEndian();

		final int pixels = len / bpp;

		// populate the plane min/max to default values
		planeMins[imageIndex][(int) planeIndex] = Double.POSITIVE_INFINITY;
		planeMaxs[imageIndex][(int) planeIndex] = Double.NEGATIVE_INFINITY;

		final boolean signed = FormatTools.isSigned(pixelType);
		final long threshold = (long) Math.pow(2, bpp * 8 - 1);

		for (int i = 0; i < pixels; i++) {
			// get the value for this pixel
			final int idx = bpp * i;
			long bits = Bytes.toLong(buf, idx, bpp, little);
			if (signed) {
				if (bits >= threshold) bits -= 2 * threshold;
			}
			double v = bits;
			if (pixelType == FormatTools.FLOAT) {
				v = Float.intBitsToFloat((int) bits);
			}
			else if (pixelType == FormatTools.DOUBLE) {
				v = Double.longBitsToDouble(bits);
			}

			// Update the appropriate planar axis min/max if necessary
			final long[] planarPositions = FormatTools.rasterToPosition(iMeta
				.getAxesLengthsPlanar(), i);

			for (int axis = 0; axis < planarPositions.length; axis++) {
				final AxisType type = iMeta.getAxis(axis).type();
				final double[] planarMin = planarAxisMin.get(imageIndex).get(type);
				if (planarMin[(int) planarPositions[axis]] > v) {
					planarMin[(int) planarPositions[axis]] = v;
				}
				final double[] planarMax = planarAxisMax.get(imageIndex).get(type);
				if (planarMax[(int) planarPositions[axis]] < v) {
					planarMax[(int) planarPositions[axis]] = v;
				}
			}

			// Update the plane min/max if necessary
			if (v > planeMaxs[imageIndex][(int) planeIndex]) {
				planeMaxs[imageIndex][(int) planeIndex] = v;
			}
			if (v < planeMins[imageIndex][(int) planeIndex]) {
				planeMins[imageIndex][(int) planeIndex] = v;
			}
		}

		// Set the number of planes complete for this image
		minMaxDone[imageIndex] = Math.max(minMaxDone[imageIndex], (int) planeIndex +
			1);
	}

	/**
	 * Ensures internal min/max variables are initialized properly.
	 */
	private void initMinMax() {
		final io.scif.Metadata m = getMetadata();
		final int imageCount = m.getImageCount();

		if (planarAxisMin == null) {
			planarAxisMin = new ArrayList<>();
			for (int i = 0; i < imageCount; i++) {
				final HashMap<AxisType, double[]> minMap = new HashMap<>();
				final ImageMetadata iMeta = m.get(i);
				for (final CalibratedAxis axis : iMeta.getAxesPlanar()) {
					final double[] values = new double[(int) iMeta.getAxisLength(axis
						.type())];
					Arrays.fill(values, Double.POSITIVE_INFINITY);
					minMap.put(axis.type(), values);
				}
				planarAxisMin.add(minMap);
			}
		}
		if (planarAxisMax == null) {
			planarAxisMax = new ArrayList<>();
			for (int i = 0; i < imageCount; i++) {
				final HashMap<AxisType, double[]> maxMap = new HashMap<>();
				final ImageMetadata iMeta = m.get(i);
				for (final CalibratedAxis axis : iMeta.getAxesPlanar()) {
					final double[] values = new double[(int) iMeta.getAxisLength(axis
						.type())];
					Arrays.fill(values, Double.NEGATIVE_INFINITY);
					maxMap.put(axis.type(), values);
				}
				planarAxisMax.add(maxMap);
			}
		}
		if (planeMins == null) {
			planeMins = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				planeMins[i] = new double[(int) getPlaneCount(i)];
				Arrays.fill(planeMins[i], Double.NaN);
			}
		}
		if (planeMaxs == null) {
			planeMaxs = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				planeMaxs[i] = new double[(int) getPlaneCount(i)];
				Arrays.fill(planeMaxs[i], Double.NaN);
			}
		}
		if (minMaxDone == null) minMaxDone = new int[imageCount];
	}

	/**
	 * Returns the global min or max (based on the provided list) for the given
	 * image, axis type, and slice for that axis.
	 */
	private Double getAxisGlobalValue(final int imageIndex, final AxisType type,
		final int index, final List<Map<AxisType, double[]>> planarAxisValues)
		throws FormatException
	{
//		FormatTools.assertId(getCurrentFile(), true, 2);
		if (index < 0 || index >= getMetadata().get(imageIndex).getAxisLength(
			type))
		{
			throw new FormatException("Invalid " + type.getLabel() + " index: " +
				index);
		}

		// check that all planes have been read
		if (minMaxDone == null || minMaxDone[imageIndex] < getPlaneCount(
			imageIndex))
		{
			return null;
		}
		return getAxisValue(planarAxisValues.get(imageIndex).get(type), index);
	}

	/**
	 * Returns the known min or max (based on the provided list) for the given
	 * image, axis type, and slice for that axis.
	 */
	private Double getAxisKnownValue(final int imageIndex, final AxisType type,
		final int index, final List<Map<AxisType, double[]>> planarAxisValues)
	{
//		FormatTools.assertId(getCurrentFile(), true, 2);
		return planarAxisValues == null ? null : getAxisValue(planarAxisValues.get(
			imageIndex).get(type), index);
	}

	/**
	 * Safe method for accessing a values array. Returns null if the provided
	 * array is null, else returns values[index].
	 */
	private Double getAxisValue(final double[] values, final int index) {
		return values == null ? null : new Double(values[index]);
	}

	/**
	 * Returns the min or max (based on the provided array) value for a given
	 * plane and image index.
	 */
	private Double getPlaneValue(final int imageIndex, final long planeIndex,
		final double[][] planeValues)
	{
//		FormatTools.assertId(getCurrentFile(), true, 2);
		if (planeValues == null) return null;
		if (Double.isNaN(planeValues[imageIndex][(int) planeIndex])) return null;

		return planeValues[imageIndex][(int) planeIndex];
	}
}
