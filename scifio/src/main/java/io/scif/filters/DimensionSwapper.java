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
import java.util.List;

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

	// -- Fields --

	private List<AxisType> inputOrder;

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
	public void swapDimensions(final int imageIndex, final AxisType... newOrder) {
		FormatTools.assertId(getCurrentFile(), true, 2);

		// Check for null order
		if (newOrder == null) throw new IllegalArgumentException("order is null");

		final List<AxisType> oldOrder = getDimensionOrder(imageIndex);

		// Check for mis-aligned order
		if (newOrder.length != oldOrder.size()) {
			throw new IllegalArgumentException("newOrder is unexpected length: " +
				newOrder.length + "; expected: " + oldOrder.size());
		}

		// Check for unknown AxisTypes
		for (int i = 0; i < newOrder.length; i++) {
			if (!oldOrder.contains(newOrder[i])) throw new IllegalArgumentException(
				"newOrder specifies different axes");
		}

		if (metaCheck() &&
			!(((DimensionSwapperMetadata) getMetadata()).getOutputOrder() == null))
		{
			((DimensionSwapperMetadata) getMetadata()).getOutputOrder()[imageIndex] =
				getInputOrder(imageIndex);
		}

		getMetadata().get(imageIndex).setAxisTypes(newOrder);
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
		final List<AxisType> outputOrder)
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		if (metaCheck() &&
			!(((DimensionSwapperMetadata) getMetadata()).getOutputOrder() == null))
		{
			((DimensionSwapperMetadata) getMetadata()).getOutputOrder()[imageIndex] =
				outputOrder;
		}
	}

	/**
	 * Returns the order for reading AxisTypes from disk.
	 */
	public List<AxisType> getInputOrder(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		
		if (inputOrder == null) inputOrder = new ArrayList<AxisType>();
		List<CalibratedAxis> axes = getMetadata().get(imageIndex).getAxes();
		
		for (int i=0; i<axes.size(); i++) {
			inputOrder.set(i, axes.get(i).type());
		}
		
		return inputOrder;
	}

	/**
	 * Returns the (potentially swapped) output order of the AxisTypes.
	 */
	public List<AxisType> getDimensionOrder(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		List<AxisType> outOrder = null;

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
			@SuppressWarnings("unchecked")
			List<AxisType>[] axisTypeList = new ArrayList[getImageCount()];
			((DimensionSwapperMetadata) getMetadata())
				.setOutputOrder(axisTypeList);

			// NB: Create our own copy of the Metadata,
			// which we can manipulate safely.
			// TODO should be a copy method
			if (metaCheck()) ((DimensionSwapperMetadata) getMetadata())
				.wrap(getParent().getMetadata());
		}
	}

	// -- Reader API methods --

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex)
		throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex));
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final long[] offsets, final long[] lengths) throws FormatException,
		IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex),
			offsets, lengths);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final long[] offsets, long[] lengths)
		throws FormatException, IOException
	{
		return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane,
			offsets, lengths);
	}

	@Override
	public Plane openThumbPlane(final int imageIndex, final long planeIndex)
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

	/* Computes the reordered plane index for the current axes order */
	private long reorder(final int imageIndex, final long planeIndex) {
		if (!metaCheck()) return planeIndex;

		long[] originalPosition =
			FormatTools.rasterToPosition(getMetadata().get(imageIndex)
				.getAxesLengthsNonPlanar(), planeIndex);

		List<AxisType> swappedOrder = getDimensionOrder(imageIndex);

		long[] swappedPosition = new long[originalPosition.length];
		long[] lengths = new long[originalPosition.length];

		for (int i = 0; i < originalPosition.length; i++) {
			int offset = getMetadata().get(imageIndex).getPlanarAxisCount();
			AxisType type = swappedOrder.get(i + offset);
			lengths[i] = getMetadata().get(imageIndex).getAxisLength(type);
			swappedPosition[i] =
				originalPosition[getMetadata().get(imageIndex).getAxisIndex(type) - offset];
		}

		return (int) FormatTools.positionToRaster(lengths, swappedPosition);
	}
}
