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

package io.scif.filters;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.common.DataTools;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Logic to compute minimum and maximum values for each channel.
 */
@Plugin(type = Filter.class, attrs = {
	@Attr(name = MinMaxFilter.FILTER_KEY, value = MinMaxFilter.FILTER_VALUE),
	@Attr(name = MinMaxFilter.ENABLED_KEY, value = MinMaxFilter.ENABLED_VAULE) })
public class MinMaxFilter extends AbstractReaderFilter {

	// -- Constants --

	public static final String FILTER_VALUE = "io.scif.Reader";

	// -- Fields --

	/** Min values for each channel. */
	protected double[][] sliceMin;

	/** Max values for each channel. */
	protected double[][] sliceMax;

	/** Min values for each plane. */
	protected double[][] planeMin;

	/** Max values for each plane. */
	protected double[][] planeMax;

	/** Number of planes for which min/max computations have been completed. */
	protected int[] minMaxDone;

	// -- MinMaxCalculator API methods --

	/**
	 * Retrieves a specified channel's global minimum. Returns null if some of the
	 * image planes have not been read.
	 * 
	 * @throws IOException Not actually thrown.
	 */
	public Double getChannelGlobalMinimum(final int imageIndex, final int theC)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		if (theC < 0 ||
			theC >= getMetadata().getAxisLength(imageIndex, Axes.CHANNEL))
		{
			throw new FormatException("Invalid channel index: " + theC);
		}

		// check that all planes have been read
		if (minMaxDone == null || minMaxDone[imageIndex] < getImageCount()) {
			return null;
		}
		return new Double(sliceMin[imageIndex][theC]);
	}

	/**
	 * Retrieves a specified channel's global maximum. Returns null if some of the
	 * image planes have not been read.
	 * 
	 * @throws IOException Not actually thrown.
	 */
	public Double getChannelGlobalMaximum(final int imageIndex, final int theC)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		if (theC < 0 ||
			theC >= getMetadata().getAxisLength(imageIndex, Axes.CHANNEL))
		{
			throw new FormatException("Invalid channel index: " + theC);
		}

		// check that all planes have been read
		if (minMaxDone == null || minMaxDone[imageIndex] < getImageCount()) {
			return null;
		}
		return new Double(sliceMax[imageIndex][theC]);
	}

	/**
	 * Retrieves the specified channel's minimum based on the images that have
	 * been read. Returns null if no image planes have been read yet.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	public Double getChannelKnownMinimum(final int imageIndex, final int theC)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		return sliceMin == null ? null : new Double(sliceMin[imageIndex][theC]);
	}

	/**
	 * Retrieves the specified channel's maximum based on the images that have
	 * been read. Returns null if no image planes have been read yet.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	public Double getChannelKnownMaximum(final int imageIndex, final int theC)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		return sliceMax == null ? null : new Double(sliceMax[imageIndex][theC]);
	}

	/**
	 * Retrieves the minimum pixel value for the specified plane. If each image
	 * contains multiple representations of a given plane (i.e., with an RGB
	 * image), returns the maximum value for each individual plane representation.
	 * Returns null if the plane has not already been read.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	public Double[] getPlaneMinimum(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		if (planeMin == null) return null;

		final int numXY = countRGB(imageIndex, planeIndex);
		final int pBase = planeIndex * numXY;
		if (Double.isNaN(planeMin[imageIndex][pBase])) return null;

		final Double[] min = new Double[numXY];
		for (int c = 0; c < numXY; c++) {
			min[c] = new Double(planeMin[imageIndex][pBase + c]);
		}
		return min;
	}

	/**
	 * Retrieves the maximum pixel value for the specified plane. If each image
	 * plane contains more than one channel (i.e., {@link #getRGBChannelCount()}
	 * &gt; 1), returns the maximum value for each embedded channel. Returns null
	 * if the plane has not already been read.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	public Double[] getPlaneMaximum(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		if (planeMax == null) return null;

		final int numXY = countRGB(imageIndex, planeIndex);
		final int pBase = planeIndex * numXY;
		if (Double.isNaN(planeMax[imageIndex][pBase])) return null;

		final Double[] max = new Double[numXY];
		for (int c = 0; c < numXY; c++) {
			max[c] = new Double(planeMax[imageIndex][pBase + c]);
		}
		return max;
	}

	/**
	 * Returns true if the values returned by getChannelGlobalMinimum/Maximum can
	 * be trusted.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	public boolean isMinMaxPopulated(final int imageIndex)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		return minMaxDone != null && minMaxDone[imageIndex] == getImageCount();
	}

	// -- IFormatReader API methods --

	@Override
	public int getPlaneCount(final int imageIndex) {
		return getMetadata().get(imageIndex).getPlaneCount();
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		int planarAxes = getMetadata().getPlanarAxisCount(imageIndex);
		return openPlane(imageIndex, planeIndex, new long[planarAxes],
			getMetadata().getAxesLengthsPlanar(imageIndex));
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		int planarAxes = getMetadata().getPlanarAxisCount(imageIndex);
		return openPlane(imageIndex, planeIndex, plane, new long[planarAxes],
			getMetadata().getAxesLengthsPlanar(imageIndex));
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final long[] planeMin, final long[] planeMax) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, createPlane(planeMin, planeMax),
			planeMin, planeMax);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final long[] offsets, final long[] lengths)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		super.openPlane(imageIndex, planeIndex, plane, offsets, lengths);

		updateMinMax(imageIndex, planeIndex, plane.getBytes(), FormatTools
			.getBytesPerPixel(getMetadata().getPixelType(imageIndex)) *
			DataTools.safeMultiply32(lengths));
		return plane;
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		if (!fileOnly) {
			sliceMin = null;
			sliceMax = null;
			planeMin = null;
			planeMax = null;
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
	 * @param no the image index within the file.
	 * @param buf a pre-allocated buffer.
	 * @param len as <code>buf</code> may be larger than the actual pixel count
	 *          having been written to it, the length (in bytes) of the those
	 *          pixels.
	 */
	protected void updateMinMax(final int imageIndex, final int planeIndex,
		final byte[] buf, final int len) throws FormatException, IOException
	{
		if (buf == null) return;
		initMinMax(imageIndex, planeIndex);

		final io.scif.Metadata m = getMetadata();
		final int numRGB = countRGB(imageIndex, planeIndex);
		final int pixelType = m.getPixelType(imageIndex);
		final int bpp = FormatTools.getBytesPerPixel(pixelType);
		final long planeSize = m.getPlaneSize(imageIndex);
		// check whether min/max
		// values have already
		// been computed for
		// this plane
		// and that the buffer requested is actually the entire plane
		if (len == planeSize &&
			!Double.isNaN(planeMin[imageIndex][planeIndex * numRGB])) return;

		final boolean little = m.isLittleEndian(imageIndex);

		final int pixels = len / (bpp * numRGB);
		final boolean interleaved = m.isInterleaved(imageIndex);

		final long[] coords =
			FormatTools.rasterToPosition(imageIndex, planeIndex, m);
		int cIndex = m.getAxisIndex(imageIndex, Axes.CHANNEL);
		final int cBase =
			m.isMultichannel(imageIndex) ? (int) coords[cIndex] * numRGB : 0;
		final int pBase = (int) planeIndex * numRGB;
		for (int c = 0; c < numRGB; c++) {
			planeMin[imageIndex][pBase + c] = Double.POSITIVE_INFINITY;
			planeMax[imageIndex][pBase + c] = Double.NEGATIVE_INFINITY;
		}

		final boolean signed = FormatTools.isSigned(pixelType);

		final long threshold = (long) Math.pow(2, bpp * 8 - 1);
		for (int i = 0; i < pixels; i++) {
			for (int c = 0; c < numRGB; c++) {
				final int idx = bpp * (interleaved ? i * numRGB + c : c * pixels + i);
				long bits = DataTools.bytesToLong(buf, idx, bpp, little);
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

				if (v > sliceMax[imageIndex][cBase + c]) {
					sliceMax[imageIndex][cBase + c] = v;
				}
				if (v < sliceMin[imageIndex][cBase + c]) {
					sliceMin[imageIndex][cBase + c] = v;
				}
			}
		}

		for (int c = 0; c < numRGB; c++) {
			if (sliceMin[imageIndex][cBase + c] < planeMin[imageIndex][pBase + c]) {
				planeMin[imageIndex][pBase + c] = sliceMin[imageIndex][cBase + c];
			}
			if (sliceMax[imageIndex][cBase + c] > planeMax[imageIndex][pBase + c]) {
				planeMax[imageIndex][pBase + c] = sliceMax[imageIndex][cBase + c];
			}
		}
		minMaxDone[imageIndex] = Math.max(minMaxDone[imageIndex], planeIndex + 1);
	}

	/**
	 * Ensures internal min/max variables are initialized properly.
	 * 
	 * @throws FormatException Not actually thrown.
	 * @throws IOException Not actually thrown.
	 */
	protected void initMinMax(int imageIndex, int planeIndex)
		throws FormatException, IOException
	{
		final io.scif.Metadata m = getMetadata();
		final int imageCount = m.getImageCount();
		final int xyRepresentations =
			countRGB(imageIndex, planeIndex);

		if (sliceMin == null) {
			sliceMin = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				sliceMin[i] = new double[xyRepresentations];
				Arrays.fill(sliceMin[i], Double.POSITIVE_INFINITY);
			}
		}
		if (sliceMax == null) {
			sliceMax = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				sliceMax[i] = new double[xyRepresentations];
				Arrays.fill(sliceMax[i], Double.NEGATIVE_INFINITY);
			}
		}
		if (planeMin == null) {
			planeMin = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				planeMin[i] = new double[getPlaneCount(i) * xyRepresentations];
				Arrays.fill(planeMin[i], Double.NaN);
			}
		}
		if (planeMax == null) {
			planeMax = new double[imageCount][];
			for (int i = 0; i < imageCount; i++) {
				planeMax[i] = new double[getPlaneCount(i) * xyRepresentations];
				Arrays.fill(planeMax[i], Double.NaN);
			}
		}
		if (minMaxDone == null) minMaxDone = new int[imageCount];
	}


	/**
	 * Count how many color channel planes are present.
	 */
	private int countRGB(int imageIndex, int planeIndex) {
		Metadata meta = getMetadata();
		if (meta.getAxisIndex(imageIndex, Axes.CHANNEL) < meta
			.getPlanarAxisCount(imageIndex))
		{
			return (int) meta.getAxisLength(imageIndex, Axes.CHANNEL);
		}
		return 1;
	}
}
