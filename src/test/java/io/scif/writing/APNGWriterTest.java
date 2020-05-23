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

import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class APNGWriterTest extends AbstractSyntheticWriterTest {

	private static ImgOpener opener;

	@BeforeClass
	public static void createOpener() {
		opener = new ImgOpener();
	}

	@AfterClass
	public static void disposeOpener() {
		opener.context().dispose();
	}

	public APNGWriterTest() {
		super(".png");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWriting_uint8() throws IOException {

		final SCIFIOImgPlus<?> sourceImg =
				opener.openImgs(new TestImgLocation.Builder().name("8bit-unsigned").pixelType(
				"uint8").axes("X", "Y", "C").lengths(100, 100, 3).build()).get(0);
		testWriting(sourceImg);

		final SCIFIOImgPlus<?> sourceImg2 =
				opener.openImgs(new TestImgLocation.Builder().name("8bit-unsigned").pixelType(
				"uint8").axes("X", "Y", "Channel").lengths(100, 100, 3).build()).get(0);
		testWriting(sourceImg2);

		sourceImg.dispose();
		sourceImg2.dispose();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWriting_uint16() throws IOException {

		final SCIFIOImgPlus<?> sourceImg = opener.openImgs(
			new TestImgLocation.Builder().name("16bit-unsigned").pixelType("uint16")
				.axes("X", "Y", "C").lengths(100, 100, 3).build()).get(0);

		testWriting(sourceImg);

		final SCIFIOImgPlus<?> sourceImg2 = opener.openImgs(
			new TestImgLocation.Builder().name("16bit-unsigned").pixelType("uint16")
				.axes("X", "Y").lengths(100, 100).build()).get(0);

		testWriting(sourceImg2);

		sourceImg.dispose();
	}

}
