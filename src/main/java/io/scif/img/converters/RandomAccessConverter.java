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

package io.scif.img.converters;

import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgUtilityService;
import io.scif.util.FormatTools;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Generalized {@link PlaneConverter} implementation. Can populate any
 * {@link ImgPlus} using its {@link RandomAccess}. May be slower than other,
 * optimized methods.
 *
 * @author Mark Hiner
 */
@Plugin(type = PlaneConverter.class, name = "Default")
public class RandomAccessConverter extends AbstractPlaneConverter {

	@Parameter
	private ImgUtilityService imgUtilService;

	/**
	 * Uses a cursor to populate the plane. This solution is general and works
	 * regardless of container, but at the expense of performance both now and
	 * later.
	 */
	@Override
	public <T extends RealType<T>> void populatePlane(final Reader reader,
		final int imageIndex, final int planeIndex, final byte[] plane,
		final ImgPlus<T> img, final SCIFIOConfig config)
	{
		final Metadata m = reader.getMetadata();

		final int pixelType = m.get(imageIndex).getPixelType();
		final boolean little = m.get(imageIndex).isLittleEndian();

		final long[] dimLengths = imgUtilService.getDimLengths(m, imageIndex,
			config);
		final long[] pos = new long[dimLengths.length];

		final int planeX = 0;
		final int planeY = 1;

		getPosition(m, imageIndex, planeIndex, pos);

		final int sX = (int) img.dimension(0);
		final int sY = (int) img.dimension(1);

		final RandomAccess<T> randomAccess = img.randomAccess();

		int index = 0;

		for (int y = 0; y < sY; ++y) {
			pos[planeX] = 0;
			pos[planeY] = y;

			randomAccess.setPosition(pos);

			for (int x = 1; x < sX; ++x) {
				randomAccess.get().setReal(imgUtilService.decodeWord(plane, index++,
					pixelType, little));
				randomAccess.fwd(planeX);
			}

			randomAccess.get().setReal(imgUtilService.decodeWord(plane, index++,
				pixelType, little));
		}
	}

	/** Copies the current dimensional position into the given array. */
	private void getPosition(final Metadata m, final int imageIndex,
		final int planeIndex, final long[] pos)
	{
		final ImageMetadata meta = m.get(imageIndex);
		final int offset = meta.getAxes().size() - meta.getAxesNonPlanar().size();

		final long[] axesPositions = FormatTools.rasterToPosition(imageIndex,
			planeIndex, m);
		for (int i = 0; i < axesPositions.length; i++) {
			pos[i + offset] = axesPositions[i];
		}
	}
}
