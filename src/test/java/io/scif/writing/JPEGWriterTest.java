/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2019 SCIFIO developers.
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
package io.scif.writing;

import io.scif.img.IO;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class JPEGWriterTest extends AbstractSyntheticWriterTest {

	public JPEGWriterTest() {
		super(".jpeg");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWriting_uint8() throws IOException {

		final ImgPlus<UnsignedByteType> sourceImg = (ImgPlus<UnsignedByteType>) IO
			.open(new TestImgLocation.Builder().name("8bit-unsigned").pixelType(
				"uint8").axes("X", "Y", "Channel").lengths(100, 100, 3).planarDims(3)
				.build());

		testWritingApprox(sourceImg, 58.1647058823);
	}

}
