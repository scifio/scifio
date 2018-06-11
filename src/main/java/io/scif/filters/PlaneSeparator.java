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
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.ImageTools;
import io.scif.util.MemoryTools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;

/**
 * Logic to automatically separate the channels in a file.
 */
@Plugin(type = Filter.class)
public class PlaneSeparator extends AbstractReaderFilter {

	// -- Fields --

	/** Last plane opened. */
	private Plane lastPlane = null;

	/** Index of last plane opened. */
	private long lastPlaneIndex = -1;

	/** Index of last plane opened. */
	private int lastImageIndex = -1;

	/** Offsets of last plane opened. */
	private long[] lastPlaneMin = null;

	/** Lengths of last plane opened. */
	private long[] lastPlaneMax = null;

	// -- Constructor --

	public PlaneSeparator() {
		super(PlaneSeparatorMetadata.class);
	}

	// -- PlanarAxisSeparator API methods --

	/**
	 * Specify which AxisTypes should be separated.
	 */
	public void separate(final AxisType... types) {
		if (metaCheck()) {
			((PlaneSeparatorMetadata) getMetadata()).separate(types);
		}
	}

	/**
	 * Returns the image number in the original dataset that corresponds to the
	 * given image number. For instance, if the original dataset was a single RGB
	 * image and the given plane index is 2, the return value will be 0.
	 *
	 * @param planeIndex is a plane number greater than or equal to 0 and less
	 *          than getPlaneCount()
	 * @return the corresponding plane number in the original (unseparated) data.
	 */
	public long getOriginalIndex(final int imageIndex, final long planeIndex) {
		final long planeCount = getPlaneCount(imageIndex);
		final long originalCount = getParent().getPlaneCount(imageIndex);

		if (planeCount == originalCount) return planeIndex;
		final long[] coords = FormatTools.rasterToPosition(imageIndex, planeIndex,
			this);
		int offset = 0;
		if (PlaneSeparatorMetadata.class.isAssignableFrom(getMetadata()
			.getClass()))
		{
			offset = ((PlaneSeparatorMetadata) getMetadata()).offset();
		}
		final long[] originalCoords = new long[coords.length - offset];
		final long[] lengths = new long[coords.length - offset];
		for (int i = 0; i < originalCoords.length; i++) {
			originalCoords[i] = coords[i + offset];
			lengths[i] = getMetadata().get(imageIndex).getAxesLengthsNonPlanar()[i +
				offset];
		}
		return FormatTools.positionToRaster(lengths, originalCoords);
	}

	// -- AbstractReaderFilter API Methods --

	@Override
	public void setSource(final String source) throws IOException {
		cleanUp();
		super.setSource(source);
	}

	@Override
	public void setSource(final File file) throws IOException {
		cleanUp();
		super.setSource(file);
	}

	@Override
	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		cleanUp();
		super.setSource(stream);
	}

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
//		FormatTools.assertId(getCurrentFile(), true, 2);
		FormatTools.checkPlaneNumber(getMetadata(), imageIndex, planeIndex);
		final Metadata meta = getMetadata();
		final Metadata parentMeta = getParentMeta();

		// Get the original index
		final long source = getOriginalIndex(imageIndex, planeIndex);
		// Get the position in the the current separated metadata
		final int splitOffset = metaCheck() ? ((PlaneSeparatorMetadata) meta)
			.offset() : 0;
		final boolean interleaved = parentMeta.get(imageIndex)
			.getInterleavedAxisCount() > 0;

		synchronized (this) {
			if (!parentMeta.get(imageIndex).isIndexed()) {
				// -> if one or more axes was split out...
				// dividing current split axis count by parent effective size c
				// (actual
				// c
				// planes).. shouldn't need to do that any more
				// trying to determine the positions for each converted axis...
				// should
				// just be raster to position on just the converted lengths

				// Get the position of the current plane
				final long[] completePosition = FormatTools.rasterToPosition(imageIndex,
					planeIndex, meta);
				// Isolate the position and lengths of the axis (axes) that have
				// been
				// split
				final long[] separatedPosition = Arrays.copyOf(completePosition,
					splitOffset);
				final long[] separatedLengths = Arrays.copyOf(meta.get(imageIndex)
					.getAxesLengthsNonPlanar(), splitOffset);
				final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
					.getPixelType());

				// Need a byte array plane to copy data into
				if (!ByteArrayPlane.class.isAssignableFrom(plane.getClass())) {
					plane = new ByteArrayPlane(getContext(), //
						meta.get(imageIndex), bounds);
				}

				if (!haveCached(source, imageIndex, bounds)) {
					int strips = 1;

					// check how big the original image is; if it's larger than
					// the available memory, we will need to split it into strips
					// (of the last planar axis)

					final long availableMemory = MemoryTools.totalAvailableMemory() / 16;
					final long planeSize = meta.get(imageIndex).getPlaneSize();
					// If we make strips, they will be of the Y axis
					final long h = //
						bounds.dimension(meta.get(imageIndex).getAxisIndex(Axes.Y));

					if (availableMemory < planeSize || planeSize > Integer.MAX_VALUE) {
						strips = (int) Math.sqrt(h);
					}

					final long[] dims = Intervals.dimensionsAsLongArray(bounds);

					// Compute strip height, and the height of the last strip
					// (in case the plane is not evenly divisible).
					final long stripHeight = h / strips;
					final long lastStripHeight = stripHeight + (h - (stripHeight *
						strips));
					byte[] strip = strips == 1 ? plane.getBytes()
						: new byte[(int) (stripHeight * ArrayUtils.safeMultiply32(Arrays
							.copyOf(dims, dims.length - 1)) * bpp)];
					updateLastPlaneInfo(source, imageIndex, splitOffset, bounds);
					final int parentYIndex = parentMeta.get(imageIndex).getAxisIndex(
						Axes.Y);
					final int yIndex = meta.get(imageIndex).getAxisIndex(Axes.Y);

					// Populate the strips
					for (int i = 0; i < strips; i++) {
						// Update planar bounds for current strip
						lastPlaneMin[parentYIndex] = bounds.min(yIndex) + (i * stripHeight);
						lastPlaneMax[parentYIndex] = lastPlaneMin[parentYIndex] +
							(i == strips - 1 ? lastStripHeight : stripHeight) - 1;

						// Open the plane
						lastPlane = getParent().openPlane(imageIndex, (int) source,
							new FinalInterval(lastPlaneMin, lastPlaneMax), config);
						// store the color table
						plane.setColorTable(lastPlane.getColorTable());

						// Adjust the strip array if this is the last strip, if
						// needed
						if (strips != 1 && lastStripHeight != stripHeight && i == strips -
							1)
						{
							strip = new byte[(int) (lastStripHeight * ArrayUtils
								.safeMultiply32(Arrays.copyOf(dims, dims.length - 1)) * bpp)];
						}

						// Extract the requested channel from the plane
						ImageTools.splitChannels(lastPlane.getBytes(), strip,
							separatedPosition, separatedLengths, bpp, false, interleaved,
							strips == 1 ? bpp * ArrayUtils.safeMultiply32(dims)
								: strip.length);
						if (strips != 1) {
							System.arraycopy(strip, 0, plane.getBytes(), (int) (i *
								stripHeight * ArrayUtils.safeMultiply32(Arrays.copyOf(dims,
									dims.length - 1))) * bpp, strip.length);
						}
					}
				}
				else {
					// Have a cached instance of the plane containing the
					// desired region
					ImageTools.splitChannels(lastPlane.getBytes(), plane.getBytes(),
						separatedPosition, separatedLengths, bpp, false, interleaved, bpp *
							ArrayUtils.safeMultiply32(Intervals.numElements(bounds)));
				}

				return plane;
			}

			if (!haveCached(source, imageIndex, bounds)) {
				// Convert the current positional information to the format of
				// the
				// parent
				updateLastPlaneInfo(source, imageIndex, splitOffset, bounds);
				// Delegate directly to the parent
				lastPlane = getParent().openPlane(imageIndex, planeIndex, plane,
					new FinalInterval(lastPlaneMin, lastPlaneMax), config);
			}
		}
		return lastPlane;
	}

	// -- Prioritized API --

	@Override
	public double getPriority() {
		return 2.0;
	}

	// -- Helper Methods --

	/**
	 * Converts the given plane information using the current metadata to a format
	 * usable by the wrapped reader, stored in the "lastPlane"... variables.
	 */
	private void updateLastPlaneInfo(final long source, final int imageIndex,
		final int splitOffset, final Interval bounds)
	{
		final Metadata meta = getMetadata();
		final Metadata parentMeta = getParentMeta();
		lastPlaneIndex = source;
		lastImageIndex = imageIndex;
		// create the plane offsets and lengths to match the underlying image
		lastPlaneMin = new long[bounds.numDimensions() + splitOffset];
		lastPlaneMax = new long[bounds.numDimensions() + splitOffset];

		// Create the offset and length arrays to match the underlying,
		// unsplit dimensions. This is required to pass to the wrapped reader.
		// The unsplit plane will then have the appropriate region extracted.
		for (final CalibratedAxis axis : parentMeta.get(imageIndex)
			.getAxesPlanar())
		{
			final int parentIndex = parentMeta.get(imageIndex).getAxisIndex(axis
				.type());
			final int currentIndex = meta.get(imageIndex).getAxisIndex(axis.type());
			// This axis is still a planar axis, so we can read it from the
			// current plane offsets/lengths
			if (currentIndex >= 0 && currentIndex < meta.get(imageIndex)
				.getPlanarAxisCount())
			{
				lastPlaneMin[parentIndex] = bounds.min(currentIndex);
				lastPlaneMax[parentIndex] = bounds.max(currentIndex);
			}
			// This axis is a planar axis in the underlying metadata that was
			// split out, so we will insert a [0,length] range
			else if (parentMeta.get(imageIndex).getAxisIndex(axis.type()) < parentMeta
				.get(imageIndex).getPlanarAxisCount())
			{
				lastPlaneMin[parentIndex] = 0;
				lastPlaneMax[parentIndex] = parentMeta.get(imageIndex).getAxisLength(
					axis.type()) - 1;
			}
		}
	}

	/**
	 * Returns true if we have a cached plane matching the given image and plane
	 * indices, with extents to cover the desired offsets and lengths
	 */
	private boolean haveCached(final long source, final int imageIndex,
		final Interval bounds)
	{
		if (source != lastPlaneIndex || imageIndex != lastImageIndex ||
			lastPlane == null || lastPlaneMin == null || lastPlaneMax == null)
		{
			return false;
		}
		// TODO It would be nice to fix up this logic so that we can use
		// cached planes when requesting a sub-region of the cached plane.
		// See https://github.com/scifio/scifio/issues/155
		for (int d = 0; d < bounds.numDimensions(); d++) {
			if (bounds.min(d) != lastPlaneMin[d]) return false;
			if (bounds.max(d) != lastPlaneMax[d]) return false;
		}
		return true;
	}

	/* Resets local fields. */
	@Override
	protected void cleanUp() throws IOException {
		super.cleanUp();
		lastPlane = null;
		lastPlaneIndex = -1;
		lastImageIndex = -1;
		lastPlaneMin = null;
		lastPlaneMax = null;
	}
}
