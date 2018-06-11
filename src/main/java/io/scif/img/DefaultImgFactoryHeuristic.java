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

package io.scif.img;

import io.scif.Metadata;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.cell.SCIFIOCellImgFactory;
import io.scif.util.FormatTools;
import io.scif.util.MemoryTools;

import net.imagej.axis.Axes;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.NativeType;

import org.scijava.util.ArrayUtils;

/**
 * Default {@link ImgFactoryHeuristic} implementation. Uses the following
 * heuristic to select a factory:
 * <ul>
 * <li>Check each ImgMode in order</li>
 * <li>If ImgMode.ARRAY, select if dataset size &lt; 2GB</li>
 * <li>If ImgMode.PLANAR, select if plane size &lt; 2GB and dataset fits memory.
 * <li>
 * <li>If ImgMode.CELL, return a SCIFIOCellImgFactory.</li>
 * <li>If ImgMode.AUTO or none of the requested types could be selected, check
 * as though the order were ARRAY -&gt; PLANAR -&gt; CELL.</li>
 * </ul>
 * <p>
 * NB: ImgMode.CELL is always satisfied. Thus to avoid a particular ImgMode,
 * provide a list excluding the undesired types that includes ImgMode.CELL last.
 * </p>
 *
 * @author Mark Hiner
 */
public class DefaultImgFactoryHeuristic implements ImgFactoryHeuristic {

	// -- Constants --

	// % of available memory to trigger opening as a CellImg, if surpassed
	private static final double MEMORY_THRESHOLD = 0.75;

	// -- ImgFactoryHeuristic API Methods --

	@Override
	public <T extends NativeType<T>> ImgFactory<T> createFactory(final Metadata m,
		final ImgMode[] imgModes, final T type) throws IncompatibleTypeException
	{
		ImgFactory<T> tmpFactory = null;

		// Max size of a plane of a PlanarImg, or total dataset for ArrayImg.
		// 2GB.
		final long maxSize = ArrayUtils.safeMultiply64(2, 1024, 1024, 1024);

		final long availableMem = (long) (MemoryTools.totalAvailableMemory() *
			MEMORY_THRESHOLD);
		long datasetSize = m.getDatasetSize();

		// check for overflow
		if (datasetSize <= 0) datasetSize = Long.MAX_VALUE;

		// divide by 1024 to compare to max_size and avoid overflow
		final long planeSize = m.get(0).getAxisLength(Axes.X) * m.get(0)
			.getAxisLength(Axes.Y) * FormatTools.getBytesPerPixel(m.get(0)
				.getPixelType());

		final boolean fitsInMemory = availableMem > datasetSize;

		boolean decided = false;
		int modeIndex = 0;

		// loop over ImgOptions in preferred order
		while (!decided) {

			// get the current mode, or AUTO if we've exhausted the list of
			// modes
			final ImgMode mode = modeIndex >= imgModes.length ? ImgMode.AUTO
				: imgModes[modeIndex++];

			if (mode.equals(ImgMode.AUTO)) {
				if (!fitsInMemory) tmpFactory = new SCIFIOCellImgFactory<>();
				else if (datasetSize < maxSize) tmpFactory = new ArrayImgFactory<>();
				else tmpFactory = new PlanarImgFactory<>();

				// FIXME: no CellImgFactory right now.. isn't guaranteed to
				// handle all
				// images well (e.g. RGB)
//        else if (planeSize < maxSize) tmpFactory = new PlanarImgFactory<T>();
//        else tmpFactory = new CellImgFactory<T>();

				decided = true;
			}
			else if (mode.equals(ImgMode.ARRAY) && datasetSize < maxSize &&
				fitsInMemory)
			{
				tmpFactory = new ArrayImgFactory<>();
				decided = true;
			}
			else if (mode.equals(ImgMode.PLANAR) && planeSize < maxSize &&
				fitsInMemory)
			{
				tmpFactory = new PlanarImgFactory<>();
				decided = true;
			}
			else if (mode.equals(ImgMode.CELL)) {
				// FIXME: no CellImgFactory right now.. isn't guaranteed to
				// handle all
				// images well (e.g. RGB)
//        if (fitsInMemory) tmpFactory = new CellImgFactory<T>();
//        else tmpFactory = new SCIFIOCellImgFactory<T>();
				tmpFactory = new SCIFIOCellImgFactory<>();

				decided = true;
			}
		}

		return tmpFactory.imgFactory(type);
	}
}
