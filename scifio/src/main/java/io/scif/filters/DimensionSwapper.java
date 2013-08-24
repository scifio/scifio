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
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Handles swapping the dimension order of an image series. This class is useful
 * for both reassigning ZCT sizes (the input dimension order), and shuffling
 * around the resultant planar order (the output dimension order).
 */
@Plugin(type = Filter.class, priority = DimensionSwapper.PRIORITY, attrs = {
	@Attr(name = DimensionSwapper.FILTER_KEY,
		value = DimensionSwapper.FILTER_VALUE),
	@Attr(name = DimensionSwapper.ENABLED_KEY,
		value = DimensionSwapper.ENABLED_VAULE) })
public class DimensionSwapper extends AbstractReaderFilter {

	// -- Constants --

	public static final double PRIORITY = 4.0;
	public static final String FILTER_VALUE = "io.scif.Reader";

	// -- Contructor --

	public DimensionSwapper() {
		super(DimensionSwapperMetadata.class);
	}

	// -- DimensionSwapper API methods --

	/**
	 * Sets the input dimension order according to the given string (e.g.,
	 * "XYZCT"). This string indicates the planar rasterization order from the
	 * source, overriding the detected order. It may result in the dimensional
	 * axis sizes changing. If the given order is identical to the file's native
	 * order, then nothing happens. Note that this method will throw an exception
	 * if X and Y do not appear in positions 0 and 1 (although X and Y can be
	 * reversed).
	 */
	public void swapDimensions(final int imageIndex,
		final List<CalibratedAxis> newOrder)
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		if (newOrder == null) throw new IllegalArgumentException("order is null");

		final List<CalibratedAxis> oldOrder = getDimensionOrder(imageIndex);

		if (newOrder.size() != oldOrder.size()) {
			throw new IllegalArgumentException("newOrder is unexpected length: " +
				newOrder.size() + "; expected: " + oldOrder.size());
		}

		for (int i = 0; i < newOrder.size(); i++) {
			if (!oldOrder.contains(newOrder.get(i))) throw new IllegalArgumentException(
				"newOrder specifies different axes");
		}

		if (newOrder.get(0).type() != Axes.X && newOrder.get(1).type() != Axes.X) {
			throw new IllegalArgumentException("X is not in first two positions");
		}
		if (newOrder.get(0).type() != Axes.Y && newOrder.get(1).type() != Axes.Y) {
			throw new IllegalArgumentException("Y is not in first two positions");
		}

		if (newOrder.indexOf(Axes.CHANNEL) != oldOrder.indexOf(Axes.CHANNEL) &&
			getMetadata().getRGBChannelCount(imageIndex) > 1)
		{
			throw new IllegalArgumentException(
				"Cannot swap C dimension when RGB channel count > 1");
		}

		// core.currentOrder[series] = order;
		if (metaCheck() &&
			!(((DimensionSwapperMetadata) getMetadata()).getOutputOrder() == null))
		{
			((DimensionSwapperMetadata) getMetadata()).getOutputOrder()[imageIndex] =
				Arrays.asList(getMetadata().getAxes(imageIndex));
		}

		getMetadata().setAxisTypes(imageIndex,
			newOrder.toArray(new CalibratedAxis[newOrder.size()]));
	}

	/**
	 * Sets the output dimension order according to the given string (e.g.,
	 * "XYZCT"). This string indicates the final planar rasterization
	 * order&mdash;i.e., the mapping from 1D plane number to 3D (Z, C, T) tuple.
	 * Changing it will not affect the Z, C or T sizes but will alter the order in
	 * which planes are returned when iterating. This method is useful when your
	 * application requires a particular output dimension order; e.g., ImageJ
	 * virtual stacks must be in XYCZT order.
	 */
	public void setOutputOrder(final int imageIndex,
		final List<CalibratedAxis> outputOrder)
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		if (metaCheck() &&
			!(((DimensionSwapperMetadata) getMetadata()).getOutputOrder() == null)) ((DimensionSwapperMetadata) getMetadata())
			.getOutputOrder()[imageIndex] = outputOrder;
	}

	/**
	 * Returns the original axis order for this dataset. Not affected by swapping
	 * dimensions.
	 */
	public List<CalibratedAxis> getInputOrder(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return Arrays.asList(getMetadata().getAxes(imageIndex));
	}

	/**
	 * Returns the length of a given axis.
	 */
	public int getDimensionLength(final int imageIndex, final AxisType t) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return getMetadata().getAxisLength(imageIndex, t);
	}

	/**
	 * Returns the current axis order, accounting for any swapped dimensions.
	 */
	public List<CalibratedAxis> getDimensionOrder(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		List<CalibratedAxis> outOrder = null;

		if (metaCheck()) {
			outOrder =
				((DimensionSwapperMetadata) getMetadata()).getOutputOrder()[imageIndex];
		}
		if (outOrder != null) return outOrder;
		return getInputOrder(imageIndex);
	}

	// -- AbstractReaderFilter API Methods --

	@Override
	protected void setSourceHelper(final String source) {
		final String oldFile = getCurrentFile();
		if (!source.equals(oldFile) ||
			metaCheck() &&
			(((DimensionSwapperMetadata) getMetadata()).getOutputOrder() == null || ((DimensionSwapperMetadata) getMetadata())
				.getOutputOrder().length != getImageCount()))
		{
			((DimensionSwapperMetadata) getMetadata())
				.setOutputOrder(new ArrayList[getImageCount()]);

			// NB: Create our own copy of the Metadata,
			// which we can manipulate safely.
			// TODO should be a copy method
			if (metaCheck()) ((DimensionSwapperMetadata) getMetadata())
				.wrap(getParent().getMetadata());
		}
	}

	// -- Reader API methods --

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex));
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final int x, final int y, final int w, final int h) throws FormatException,
		IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), x, y,
			w, h);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final int x, final int y, final int w, final int h)
		throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane,
			x, y, w, h);
	}

	@Override
	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return super.openThumbPlane(imageIndex, reorder(imageIndex, planeIndex));
	}

	@Override
	public Metadata getMetadata() {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return super.getMetadata();
	}

	// -- Helper methods --

	/* Reorders the ImageMetadata axes associated with this filter */
	protected int reorder(final int imageIndex, final int planeIndex) {
		if (getInputOrder(imageIndex) == null) return planeIndex;
		final List<CalibratedAxis> outputOrder = getDimensionOrder(imageIndex);
		final CalibratedAxis[] outputAxes =
			outputOrder.toArray(new CalibratedAxis[outputOrder.size()]);
		final List<CalibratedAxis> inputOrder = getInputOrder(imageIndex);
		final CalibratedAxis[] inputAxes =
			inputOrder.toArray(new CalibratedAxis[inputOrder.size()]);

		return FormatTools.getReorderedIndex(FormatTools
			.findDimensionOrder(inputAxes), FormatTools
			.findDimensionOrder(outputAxes), getDimensionLength(imageIndex, Axes.Z),
			getMetadata().getEffectiveSizeC(imageIndex), getDimensionLength(
				imageIndex, Axes.TIME), getMetadata().getPlaneCount(imageIndex),
			imageIndex, planeIndex);
	}
}
