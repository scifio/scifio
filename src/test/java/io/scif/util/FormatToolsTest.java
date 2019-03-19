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

package io.scif.util;

import static org.junit.Assert.assertEquals;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import org.junit.Test;
import org.scijava.io.location.Location;

/**
 * Unit tests for {@link FormatTools}.
 *
 * @author Mark Hiner
 */
public class FormatToolsTest {

	// -- Fields --

	private final SCIFIO scifio = new SCIFIO();

	// -- Indexed color tests --

	/**
	 * Tests that the correct values are given by the various
	 * {@link FormatTools#defaultMinMax} signatures.
	 */
	@Test
	public void testDefaultMinMax() throws FormatException, IOException {

		final Location sampleImage = TestImgLocation.builder().name("8bit-unsigned")
			.pixelType("int8").indexed(true).planarDims(3).lengths(50, 50, 1).axes(
				"X", "Y", "Channel").build();

		final Reader reader = scifio.initializer().initializeReader(sampleImage);
		final ImageMetadata iMeta = reader.getMetadata().get(0);
		iMeta.setBitsPerPixel(7);

		// Test min/max computation using ImageMetadat directly
		assertEquals((long) -Math.pow(2, 6), FormatTools.defaultMinMax(iMeta)[0]);
		assertEquals((long) Math.pow(2, 6) - 1, FormatTools.defaultMinMax(
			iMeta)[1]);

		// Test min/max computation using bits per pixel
		assertEquals((long) -Math.pow(2, 6), FormatTools.defaultMinMax(iMeta
			.getPixelType(), iMeta.getBitsPerPixel())[0]);
		assertEquals((long) Math.pow(2, 6) - 1, FormatTools.defaultMinMax(iMeta
			.getPixelType(), iMeta.getBitsPerPixel())[1]);

		// Test min/max computation using bits per pixel
		assertEquals((long) -Math.pow(2, 7), FormatTools.defaultMinMax(iMeta
			.getPixelType(), -1)[0]);
		assertEquals((long) Math.pow(2, 7) - 1, FormatTools.defaultMinMax(iMeta
			.getPixelType(), -1)[1]);

		// Test min/max computation using pixel type
		assertEquals((long) -Math.pow(2, 7), FormatTools.defaultMinMax(iMeta
			.getPixelType())[0]);
		assertEquals((long) Math.pow(2, 7) - 1, FormatTools.defaultMinMax(iMeta
			.getPixelType())[1]);
	}
}
