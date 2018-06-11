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

import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FormatException;
import io.scif.Plane;
import io.scif.config.SCIFIOConfig;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.util.Intervals;

import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * For indexed color data representing true color, factors out the indices,
 * replacing them with the color table values directly. For all other data
 * (either non-indexed, or indexed with "false color" tables), does nothing. NB:
 * lut length is not guaranteed to be accurate until a plane has been read
 */
@Plugin(type = Filter.class)
public class ChannelFiller extends AbstractReaderFilter {

	// -- Fields --

	/**
	 * Last image index opened.
	 */
	private int lastImageIndex = -1;

	/**
	 * Last plane index opened.
	 */
	private long lastPlaneIndex = -1;

	/**
	 * Cached last plane opened.
	 */
	private Plane lastPlane = null;

	/** Bounds of last plane opened. */
	private Interval lastPlaneBounds = null;

	// -- Constructor --

	public ChannelFiller() {
		super(ChannelFillerMetadata.class);
	}

	// -- Filter API Methods --

	@Override
	public boolean isCompatible(final Class<?> c) {
		return ByteArrayReader.class.isAssignableFrom(c);
	}

	// -- Reader API methods --

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
		final Interval bounds = planarBounds(imageIndex);
		return openPlane(imageIndex, planeIndex, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Interval bounds = planarBounds(imageIndex);
		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Plane plane = createPlane(getMetadata().get(imageIndex), bounds);
		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		Plane plane, final Interval bounds, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		// If the wrapped Metadata wasn't indexed, we can use the parent reader
		// directly
		if (getParentMeta().get(imageIndex).isFalseColor() || !getParentMeta().get(
			imageIndex).isIndexed())
		{
			if (!haveCached(imageIndex, planeIndex, bounds)) {
				lastPlaneBounds = new FinalInterval(bounds);
				lastPlaneIndex = planeIndex;
				lastImageIndex = imageIndex;
				lastPlane = getParent().openPlane(imageIndex, planeIndex, plane, bounds,
					config);
			}
			return lastPlane;
		}

		// If we have the cached base plane we can use it to expand, otherwise
		// we'll
		// have to open the plane still.
		final int lutLength = ((ChannelFillerMetadata) getMetadata())
			.getLutLength();

		if (!haveCached(imageIndex, planeIndex, bounds)) {
			updateLastPlaneInfo(imageIndex, lutLength, bounds);

			// Now we can read the desired plane
			lastPlane = getParent().openPlane(imageIndex, planeIndex, //
				lastPlaneBounds, config);
			lastPlaneIndex = planeIndex;
			lastImageIndex = imageIndex;

			lastPlaneBounds = bounds;
		}

		// Make sure we have a compatible plane type
		if (!ByteArrayPlane.class.isAssignableFrom(plane.getClass())) {
			plane = new ByteArrayPlane(getContext(), //
				getMetadata().get(imageIndex), bounds);
		}

		final byte[] buf = plane.getBytes();
		int pt = 0;

		final int bytesPerIndex = getParentMeta().get(imageIndex)
			.getBitsPerPixel() / 8;

		final ColorTable lut = lastPlane.getColorTable();
		final byte[] index = lastPlane.getBytes();

		// Expand the index values to fill the buffer
		if (getMetadata().get(imageIndex).getInterleavedAxisCount() > 0) {
			for (int i = 0; i < index.length / bytesPerIndex &&
				pt < buf.length; i++)
			{
				final int iVal = Bytes.toInt(index, i * bytesPerIndex, bytesPerIndex,
					getMetadata().get(imageIndex).isLittleEndian());
				for (int j = 0; j < lutLength; j++) {
					buf[pt++] = (byte) lut.get(j, iVal);
				}
			}
		}
		else {
			for (int j = 0; j < lutLength; j++) {
				for (int i = 0; i < index.length / bytesPerIndex &&
					pt < buf.length; i++)
				{
					final int iVal = Bytes.toInt(index, i * bytesPerIndex, bytesPerIndex,
						getMetadata().get(imageIndex).isLittleEndian());
					buf[pt++] = (byte) lut.get(j, iVal);
				}
			}
		}

		// Remove the color table for this plane
		plane.setColorTable(null);
		return plane;
	}

	// -- AbstractReaderFilter API Methods --

	/* lutLength is 0 until a plane is opened */
	@Override
	protected void setSourceHelper(final String source,
		final SCIFIOConfig config)
	{
		try {
			cleanUp();
		}
		catch (final IOException e) {
			// Nothing to do (this Filter's cleanUp should never throw this)
		}
	}

	// -- Prioritized API --

	@Override
	public double getPriority() {
		return 1.0;
	}

	// -- Helper Methods --

	/**
	 * Converts the given plane information using the current metadata to a format
	 * usable by the wrapped reader, stored in the "lastPlane"... variables.
	 */
	private void updateLastPlaneInfo(final int imageIndex, final int lutLength,
		final Interval bounds)
	{
		final long[] min = Intervals.minAsLongArray(bounds);
		final long[] max = Intervals.maxAsLongArray(bounds);

		final int cIndex = getMetadata().get(imageIndex).getAxisIndex(Axes.CHANNEL);
		min[cIndex] = min[cIndex] / lutLength;
		max[cIndex] = max[cIndex] / lutLength;
		lastPlaneBounds = new FinalInterval(min, max);
	}

	/**
	 * Returns true if we have a cached copy of the requested plane available.
	 */
	private boolean haveCached(final int imageIndex, final long planeIndex,
		final Interval bounds)
	{
		// TODO It would be nice to fix up this logic so that we can use
		// cached planes when requesting a sub-region of the cached plane.
		// See https://github.com/scifio/scifio/issues/155
		return planeIndex == lastPlaneIndex && //
			imageIndex == lastImageIndex && //
			lastPlane != null && lastPlaneBounds != null && //
			Intervals.equals(bounds, lastPlaneBounds);
	}

	@Override
	protected void cleanUp() throws IOException {
		super.cleanUp();
		lastPlaneIndex = 0;
		lastImageIndex = 0;
		lastPlane = null;
		lastPlaneBounds = null;
	}
}
