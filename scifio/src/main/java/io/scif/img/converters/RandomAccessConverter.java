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

package io.scif.img.converters;

import io.scif.Metadata;
import io.scif.Reader;
import io.scif.common.DataTools;
import io.scif.img.ImgOptions;
import io.scif.img.ImgUtilityService;
import io.scif.util.FormatTools;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Generalized {@link PlaneConverter} implementation. Can populate any
 * {@link ImgPlus} using its {@RandomAccess}. May be slower than
 * other, optimized methods.
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
		final ImgPlus<T> img, final ImgOptions imgOptions)
	{
		final Metadata m = reader.getMetadata();

		final int pixelType = m.get(imageIndex).getPixelType();
		final boolean little = m.get(imageIndex).isLittleEndian();

		final long[] dimLengths = imgUtilService.getDimLengths(m, imgOptions);
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
				randomAccess.get().setReal(
					decodeWord(plane, index++, pixelType, little));
				randomAccess.fwd(planeX);
			}

			randomAccess.get().setReal(decodeWord(plane, index++, pixelType, little));
		}
	}

	/** Copies the current dimensional position into the given array. */
	private void getPosition(final Metadata m, final int imageIndex,
		final int planeIndex, final long[] pos)
	{
		final int offset =
			m.get(imageIndex).getAxes().size() -
				m.get(imageIndex).getAxesNonPlanar().size();

		final long[] axesPositions =
			FormatTools.rasterToPosition(imageIndex, planeIndex, m);
		for (int i = 0; i < pos.length; i++) {
			pos[i + offset] = axesPositions[i];
		}
	}

	private static double decodeWord(final byte[] plane, final int index,
		final int pixelType, final boolean little)
	{
		final double value;
		switch (pixelType) {
			case FormatTools.UINT8:
				value = plane[index] & 0xff;
				break;
			case FormatTools.INT8:
				value = plane[index];
				break;
			case FormatTools.UINT16:
				value = DataTools.bytesToShort(plane, 2 * index, 2, little) & 0xffff;
				break;
			case FormatTools.INT16:
				value = DataTools.bytesToShort(plane, 2 * index, 2, little);
				break;
			case FormatTools.UINT32:
				value = DataTools.bytesToInt(plane, 4 * index, 4, little) & 0xffffffffL;
				break;
			case FormatTools.INT32:
				value = DataTools.bytesToInt(plane, 4 * index, 4, little);
				break;
			case FormatTools.FLOAT:
				value = DataTools.bytesToFloat(plane, 4 * index, 4, little);
				break;
			case FormatTools.DOUBLE:
				value = DataTools.bytesToDouble(plane, 8 * index, 8, little);
				break;
			default:
				value = Double.NaN;
		}
		return value;
	}
}
