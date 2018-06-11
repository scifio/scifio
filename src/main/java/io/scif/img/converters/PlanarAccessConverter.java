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

import io.scif.Metadata;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgUtilityService;
import io.scif.util.FormatTools;

import net.imagej.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * {@link PlaneConverter} implementation specialized for populating
 * {@link PlanarAccess} instances.
 *
 * @author Mark Hiner
 */
@Plugin(type = PlaneConverter.class, name = "PlanarAccess")
public class PlanarAccessConverter extends AbstractPlaneConverter {

	@Parameter
	private ImgUtilityService imgUtilService;

	/** Populates plane by reference using {@link PlanarAccess} interface. */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends RealType<T>> void populatePlane(final Reader reader,
		final int imageIndex, final int planeIndex, final byte[] plane,
		final ImgPlus<T> planarImg, final SCIFIOConfig config)
	{
		final Metadata m = reader.getMetadata();

		@SuppressWarnings("rawtypes")
		final PlanarAccess planarAccess = imgUtilService.getPlanarAccess(planarImg);
		final int pixelType = m.get(imageIndex).getPixelType();
		final int bpp = FormatTools.getBytesPerPixel(pixelType);
		final boolean fp = FormatTools.isFloatingPoint(pixelType);
		final boolean little = m.get(imageIndex).isLittleEndian();
		Object planeArray = Bytes.makeArray(plane, bpp, fp, little);
		if (planeArray == plane) {
			// array was returned by reference; make a copy
			final byte[] planeCopy = new byte[plane.length];
			System.arraycopy(plane, 0, planeCopy, 0, plane.length);
			planeArray = planeCopy;
		}
		planarAccess.setPlane(planeIndex, imgUtilService.makeArray(planeArray));
	}

}
