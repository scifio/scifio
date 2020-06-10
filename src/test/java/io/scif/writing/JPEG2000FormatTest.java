/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2020 SCIFIO developers.
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

import io.scif.SCIFIOService;
import io.scif.img.ImgOpener;
import io.scif.io.location.TestImgLocation;
import java.io.IOException;

import net.imagej.ImgPlus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.app.StatusService;

public class JPEG2000FormatTest extends AbstractSyntheticWriterTest {

	private static ImgOpener opener;

	@BeforeClass
	public static void createOpener() {
		opener = new ImgOpener(new Context(SCIFIOService.class, StatusService.class));
	}

	@AfterClass
	public static void disposeOpener() {
		opener.context().dispose();
	}

	public JPEG2000FormatTest() {
		super(".j2k");
	}

	@Test
	public void testWriting_uint8() throws IOException {
		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 2.3529411764705);
	}

	@Test
	public void testWriting_int8() throws IOException {
		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("int8").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 29515.294117642843);
	}

	@Test
	public void testWriting_int16() throws IOException {

		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("int16").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 21889.002059970047);
	}

	@Test
	public void testWriting_uint16() throws IOException {

		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint16").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 17.26710917830161);
	}

	@Test
	public void testWriting_int32() throws IOException {

		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("int32").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 14699.999640880153);
	}

	@Test
	public void testWriting_uint32() throws IOException {

		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint32").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWritingApprox(sourceImg, 0.00036388822);
	}

}
