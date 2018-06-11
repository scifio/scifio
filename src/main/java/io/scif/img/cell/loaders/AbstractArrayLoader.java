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

package io.scif.img.cell.loaders;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.filters.MetadataWrapper;
import io.scif.img.ImageRegion;
import io.scif.img.ImgUtilityService;
import io.scif.img.Range;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.plugin.Parameter;

/**
 * Abstract superclass for all {@link SCIFIOArrayLoader} implementations.
 * <p>
 * Reads the byte array appropriate for the given cell dimensions and delegates
 * to each subclass's array conversion method. See
 * {@link #convertBytes(Object, byte[], int)}.
 * </p>
 *
 * @author Mark Hiner
 */
public abstract class AbstractArrayLoader<A> implements SCIFIOArrayLoader<A> {

	private int index = 0;

	final private Reader reader;

	final private ImageRegion subRegion;

	final private boolean compatible;

	@Parameter
	private ImgUtilityService imgUtilityService;

	private List<List<ColorTable>> tables;

	private boolean[][] loadedTable;

	public AbstractArrayLoader(final Reader reader, final ImageRegion subRegion) {
		this.reader = reader;
		this.subRegion = subRegion;
		reader.getContext().inject(this);
		final RealType<?> inputType = imgUtilityService.makeType(reader
			.getMetadata().get(0).getPixelType());
		compatible = outputClass().isAssignableFrom(inputType.getClass());
	}

	@Override
	public void setIndex(final int index) {
		this.index = index;
	}

	@Override
	public ColorTable loadTable(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		ColorTable ct = getTable(imageIndex, planeIndex);
		if (ct == null && !loadedTable()[imageIndex][planeIndex]) {
			final long[] planeMin = new long[reader.getMetadata().get(imageIndex)
				.getAxesPlanar().size()];
			final long[] planeMax = new long[planeMin.length];
			for (int i = 0; i < planeMax.length; i++)
				planeMax[i] = 1;

			final FinalInterval bounds = new FinalInterval(planeMin, planeMax);
			ct = reader.openPlane(imageIndex, planeIndex, bounds).getColorTable();

			addTable(imageIndex, planeIndex, ct);
		}
		return ct;
	}

	@Override
	public A loadArray(final Interval bounds) {
		synchronized (reader) {
			final Metadata meta = reader.getMetadata();

			int entities = 1;

			// Starting indices for the planar dimensions
			final long[] planarMin = new long[meta.get(0).getAxesPlanar().size()];
			// Lengths in the planar dimensions
			final long[] planarMax = new long[meta.get(0).getAxesPlanar().size()];
			// Non-planar indices to open
			final Range[] npRanges = new Range[meta.get(0).getAxesNonPlanar().size()];
			final long[] npIndices = new long[npRanges.length];

			int axisIndex = 0;
			// Get planar ranges
			for (final CalibratedAxis axis : meta.get(0).getAxesPlanar()) {
				final int index = meta.get(0).getAxisIndex(axis.type());

				// Constrain on passed dims
				if (index < bounds.numDimensions()) {
					planarMin[axisIndex] = bounds.min(index);
					planarMax[axisIndex] = bounds.max(index);
					entities *= bounds.dimension(index);
				}

				axisIndex++;
			}

			axisIndex = 0;
			for (final CalibratedAxis axis : meta.get(0).getAxesNonPlanar()) {
				final int index = meta.get(0).getAxisIndex(axis.type());

				// otherwise just make a straightforward range
				// spanning the passed dimensional constraints
				npRanges[axisIndex] = new Range(bounds.min(index), bounds.max(index));

				if (subRegion != null) {
					entities *= subRegion.getRange(axis.type()).size();
				}
				else {
					entities *= npRanges[axisIndex].size();
				}

				axisIndex++;
			}

			A data = null;

			data = emptyArray(entities);

			try {
				final Interval planarBounds = new FinalInterval(planarMin, planarMax);
				read(data, planarBounds, npRanges, npIndices);
			}
			catch (final FormatException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}
			catch (final IOException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}

			return data;
		}
	}

	public void loadArray(final Interval bounds, final A data) {
		synchronized (reader) {
			final Metadata meta = reader.getMetadata();

			final List<CalibratedAxis> planarAxes = meta.get(0).getAxesPlanar();
			final List<CalibratedAxis> nonPlanarAxes = meta.get(0).getAxesNonPlanar();
			final int planarAxisCount = planarAxes.size();
			final int nonPlanarAxisCount = nonPlanarAxes.size();

			// Starting indices for the planar dimensions
			final long[] planarMin = new long[planarAxisCount];
			// Lengths in the planar dimensions
			final long[] planarMax = new long[planarAxisCount];
			// Non-planar indices to open
			final Range[] npRanges = new Range[nonPlanarAxisCount];
			final long[] npIndices = new long[npRanges.length];

			int axisIndex = 0;
			// Get planar ranges
			for (final CalibratedAxis axis : planarAxes) {
				final int index = meta.get(0).getAxisIndex(axis.type());

				// Constrain on passed dims
				if (index < bounds.numDimensions()) {
					planarMin[axisIndex] = bounds.min(index);
					planarMax[axisIndex] = bounds.max(index);
				}

				axisIndex++;
			}

			axisIndex = 0;
			for (final CalibratedAxis axis : nonPlanarAxes) {
				final int index = meta.get(0).getAxisIndex(axis.type());

				// otherwise just make a straightforward range spanning the
				// passed
				// dimensional constraints
				npRanges[axisIndex] = new Range(bounds.min(index), bounds.max(index));

				axisIndex++;
			}

			try {
				final Interval planarBounds = new FinalInterval(planarMin, planarMax);
				read(data, planarBounds, npRanges, npIndices);
			}
			catch (final FormatException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}
			catch (final IOException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}
		}
	}

	/**
	 * Entry point for
	 * {@link #read(Object, Plane, Interval, Range[], long[], int, int)}
	 */
	private void read(final A data, final Interval bounds, final Range[] npRanges,
		final long[] npIndices) throws FormatException, IOException
	{
		read(data, null, bounds, npRanges, npIndices, 0, 0);
	}

	/**
	 * Recurses over all the provided {@link Range}s, reading the corresponding
	 * bytes and storing them in the provided data object.
	 */
	private void read(final A data, Plane tmpPlane, final Interval bounds,
		final Range[] npRanges, final long[] npIndices, final int depth,
		int planeCount) throws FormatException, IOException
	{
		if (depth < npRanges.length) {
			// We need to invert the depth index to get the current non-planar
			// axis index, to ensure axes are iteratead in fastest to slowest
			// order
			final int npPosition = npRanges.length - 1 - depth;
			for (int i = 0; i < npRanges[npPosition].size(); i++) {
				npIndices[npPosition] = npRanges[npPosition].get(i);
				read(data, tmpPlane, bounds, npRanges, npIndices, depth + 1,
					planeCount);
				planeCount++;
			}
		}
		else if (inSubregion(npIndices)) {
			final int planeIndex = (int) FormatTools.positionToRaster(0, reader,
				npIndices);

			validateBounds(getPlanarAxisLengths(reader.getMetadata()), bounds);

			if (tmpPlane == null) {
				tmpPlane = reader.openPlane(index, planeIndex, bounds);
			}
			else {
				// Sanity check!
				final long expectedLength = Intervals.numElements(bounds);
				if (tmpPlane.getBytes().length != expectedLength) {
					throw new IllegalArgumentException("Expected tmpPlane length " +
						expectedLength + " but was " + tmpPlane.getBytes().length);
				}
				tmpPlane = reader.openPlane(index, planeIndex, tmpPlane, bounds);
			}
			convertBytes(data, tmpPlane.getBytes(), planeCount);

			// update color table
			if (!loadedTable()[index][planeIndex]) {
				addTable(index, planeIndex, tmpPlane.getColorTable());
			}
		}

	}

	private void validateBounds(final long[] lengths, final Interval bounds) {
		if (lengths.length != bounds.numDimensions()) {
			throw new IllegalArgumentException("Expected bounds of dimensionality " +
				lengths.length + " but was " + bounds.numDimensions());
		}
		for (int d = 0; d < bounds.numDimensions(); d++) {
			if (bounds.min(d) < 0 || bounds.max(d) >= lengths[d]) {
				throw new IllegalArgumentException("Bound #" + d + " of " + //
					"[" + bounds.min(d) + ", " + bounds.max(d) + "] " + //
					"is not contained in [0, " + lengths[d] + "]");
			}
		}
	}

	private long[] getPlanarAxisLengths(final Metadata meta) {
		final ImageMetadata imgMeta = unwrap(meta);
		return imgMeta.getAxesLengthsPlanar();
	}

	private ImageMetadata unwrap(final Metadata meta) {
		if (meta instanceof MetadataWrapper) return unwrap(((MetadataWrapper) meta)
			.unwrap());
		return meta.get(0);
	}

	private boolean[][] loadedTable() {
		if (loadedTable == null) {
			final Metadata m = reader.getMetadata();
			loadedTable = new boolean[m.getImageCount()][(int) m.get(0)
				.getPlaneCount()];
		}
		return loadedTable;
	}

	/**
	 * Lazy accessor for the 2D {@link ColorTable} list.
	 */
	private List<List<ColorTable>> tables() {
		if (tables == null) {
			tables = new ArrayList<>();
		}
		return tables;
	}

	/**
	 * @return the possibly null {@link ColorTable} at the specified image and
	 *         plane indices
	 */
	private ColorTable getTable(final int imageIndex, final int planeIndex) {
		final List<List<ColorTable>> tables = tables();

		// Ensure capacity
		if (imageIndex >= tables.size()) {
			for (int i = tables.size(); i <= imageIndex; i++) {
				tables.add(new ArrayList<ColorTable>());
			}
		}

		final List<ColorTable> imageTable = tables.get(imageIndex);

		return planeIndex >= imageTable.size() ? null : imageTable.get(planeIndex);
	}

	/**
	 * Inserts the given {@link ColorTable} at the specified indices.
	 */
	private void addTable(final int imageIndex, final int planeIndex,
		final ColorTable colorTable)
	{
		final ColorTable ct = getTable(imageIndex, planeIndex);
		if (ct == null) {
			final List<ColorTable> imageTable = tables.get(imageIndex);

			// Ensure capacity
			if (imageTable.size() <= planeIndex) {
				for (int i = imageTable.size(); i <= planeIndex; i++) {
					imageTable.add(null);
				}
			}

			final boolean[][] isLoaded = loadedTable();
			isLoaded[imageIndex][planeIndex] = true;

			imageTable.set(planeIndex, colorTable);
		}
	}

	/**
	 * Returns true if this loader's {@link ImageRegion} contains all of the given
	 * indices
	 */
	private boolean inSubregion(final long[] npIndices) {
		boolean inSubregion = true;

		if (subRegion != null) {
			int index = 0;
			for (final CalibratedAxis axis : reader.getMetadata().get(0)
				.getAxesNonPlanar())
			{
				inSubregion = inSubregion && inRange(subRegion.getRange(axis.type()),
					npIndices[index++]);
			}
		}

		return inSubregion;
	}

	/**
	 * Returns true if the provided {@link Range} contains the given index
	 */
	private boolean inRange(final Range range, final long value) {
		if (range == null) return true;
		return range.contains(value);

	}

	// -- AbstractArrayLoader API --

	/**
	 * @return Reader used for plane loading
	 */
	protected Reader reader() {
		return reader;
	}

	/**
	 * @return true iff the byte[]'s passed to convertBytes will match the generic
	 *         type of this loader.
	 */
	protected boolean isCompatible() {
		return compatible;
	}

	/**
	 * @return an ImgUtilityService instance for this loader.
	 */
	protected ImgUtilityService utils() {
		return imgUtilityService;
	}

	// -- Abstract methods --

	/**
	 * Type-specific conversion method. The given data is populated using the
	 * provided byte array, at a position based on planes read, assuming the
	 * length of the given byte array corresponds to one plane.
	 */
	public abstract void convertBytes(A data, byte[] bytes, int planesRead);

	/**
	 * @return The generic type of this loader.
	 */
	public abstract Class<?> outputClass();
}
