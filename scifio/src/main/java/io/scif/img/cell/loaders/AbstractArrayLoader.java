/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
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

package io.scif.img.cell.loaders;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.common.DataTools;
import io.scif.img.DimRange;
import io.scif.img.ImgUtilityService;
import io.scif.img.SubRegion;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.CalibratedAxis;
import net.imglib2.type.numeric.RealType;

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

	final private Reader reader;
	final private SubRegion subRegion;
	final private boolean compatible;

	@Parameter
	private ImgUtilityService imgUtilityService;

	public AbstractArrayLoader(final Reader reader, final SubRegion subRegion) {
		this.reader = reader;
		this.subRegion = subRegion;
		reader.getContext().inject(this);
		RealType<?> inputType =
			imgUtilityService.makeType(reader.getMetadata().get(0).getPixelType());
		compatible = outputClass().isAssignableFrom(inputType.getClass());
	}

	@Override
	public A loadArray(final int[] dimensions, final long[] min) {
		synchronized (reader) {
			final Metadata meta = reader.getMetadata();

			int entities = 1;

			// Starting indices for the planar dimensions
			long[] planarMin = new long[meta.get(0).getAxesPlanar().size()];
			// Lengths in the planar dimensions
			long[] planarLength = new long[meta.get(0).getAxesPlanar().size()];
			// Non-planar indices to open
			DimRange[] npRanges = new DimRange[meta.get(0).getAxesNonPlanar().size()];
			long[] npIndices = new long[npRanges.length];

			int axisIndex = 0;
			// Get planar ranges
			for (CalibratedAxis axis : meta.get(0).getAxesPlanar()) {
				int index = meta.get(0).getAxisIndex(axis.type());

				// Constrain on passed dims
				if (index < dimensions.length) {
					planarMin[axisIndex] = min[index];
					planarLength[axisIndex] = dimensions[index];
				}
				else {
					planarLength[axisIndex] = 1;
				}

				entities *= planarLength[axisIndex];

				axisIndex++;
			}

			axisIndex = 0;
			for (CalibratedAxis axis : meta.get(0).getAxesNonPlanar()) {
				int index = meta.get(0).getAxisIndex(axis.type());

				// otherwise just make a straightforward range spanning the passed
				// dimensional constraints
				npRanges[axisIndex] =
					new DimRange(min[index], min[index] + dimensions[index] - 1);

				if (subRegion != null) {
					entities *= subRegion.getRange(axis.type()).indices().size();
				}
				else {
					entities *= npRanges[axisIndex].indices().size();
				}

				axisIndex++;
			}

			A data = null;

			data = emptyArray(entities);

			try {
				read(data, planarMin, planarLength, npRanges, npIndices);
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

	/**
	 * Entry point for
	 * {@link #read(Object, Plane, long[], long[], DimRange[], long[], int, int)}
	 */
	private void
		read(final A data, final long[] planarMin, final long[] planarLength,
			final DimRange[] npRanges, final long[] npIndices)
			throws FormatException, IOException
	{
		read(data, null, planarMin, planarLength, npRanges, npIndices, 0, 0);
	}

	/**
	 * Recurses over all the provided {@link DimRange}s, reading the corresponding
	 * bytes and storing them in the provided data object.
	 */
	private void read(final A data, Plane tmpPlane, final long[] planarMin,
		final long[] planarLength, final DimRange[] npRanges,
		final long[] npIndices, final int depth, int planeCount)
		throws FormatException, IOException
	{
		if (depth < npRanges.length) {
			// We need to invert the depth index to get the current non-planar
			// axis index, to ensure axes are iteratead in fastest to slowest order
			final int npPosition = npRanges.length - 1 - depth;
			for (int i = 0; i < npRanges[npPosition].indices().size(); i++) {
				npIndices[npPosition] = npRanges[npPosition].indices().get(i);
				read(data, tmpPlane, planarMin, planarLength, npRanges, npIndices,
					depth + 1, planeCount);
				planeCount++;
			}
		}
		else if (inSubregion(npIndices)) {
			final int planeIndex =
				(int) FormatTools.positionToRaster(0, reader, npIndices);

			if (tmpPlane == null) tmpPlane =
				reader.openPlane(0, planeIndex, planarMin, planarLength);
			else tmpPlane =
				reader.openPlane(0, planeIndex, tmpPlane, planarMin, planarLength);
			convertBytes(data, tmpPlane.getBytes(), planeCount);
		}

	}

	/**
	 * Returns true if this loader's {@link SubRegion} contains all of the given
	 * indices
	 */
	private boolean inSubregion(final long[] npIndices) {
		boolean inSubregion = true;

		if (subRegion != null) {
			int index = 0;
			for (CalibratedAxis axis : reader.getMetadata().get(0).getAxesNonPlanar())
			{
				inSubregion =
					inSubregion &&
						inRange(subRegion.getRange(axis.type()), npIndices[index++]);
			}
		}

		return inSubregion;
	}

	/**
	 * Returns true if the provided {@link DimRange} contains the given index
	 */
	private boolean inRange(final DimRange range, final long value) {
		if (range == null) return true;
		if (range.contains(value)) return true;

		return false;
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
