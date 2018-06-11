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

package io.scif.formats;

import static org.junit.Assert.assertEquals;

import io.scif.img.IO;

import java.net.URL;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.location.FileLocation;

/**
 * Tests reading of TIFF files without (dimensional) metainformation.
 *
 * @author Stefan Helfrich
 * @author Curtis Rueden
 */
public class TIFFFormatTest {

	/**
	 * Tests if TIFFs without metadata in the header are read sensibly.
	 */
	@Test
	public void testTiffWithoutMetadata() {
		final URL tiffWithoutMetadata = getClass().getResource("tiny-10x10x3.tif");
		final ImgPlus<?> img = IO.open(new FileLocation(tiffWithoutMetadata
			.getPath())).get(0);

		assertEquals(3, img.numDimensions());
		assertEquals(10, img.dimension(0));
		assertEquals(10, img.dimension(1));
		assertEquals(3, img.dimension(2));
		assertEquals(Axes.UNKNOWN_LABEL, img.axis(2).type().getLabel());
	}

}
