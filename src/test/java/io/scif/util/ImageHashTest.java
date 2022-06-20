/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2022 SCIFIO developers.
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

import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.io.location.TestImgLocation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link ImageHash}
 *
 * @author Gabriel Einsdorf, KNIME GmbH Konstanz
 */
public class ImageHashTest {

	private static ImgOpener opener;

	@BeforeClass
	public static void createOpener() {
		opener = new ImgOpener();
	}

	@AfterClass
	public static void disposeOpener() {
		opener.context().dispose();
	}

	@Test
	public void testImageHash() {

		final SCIFIOImgPlus<?> img1 = opener.openImgs(new TestImgLocation.Builder().lengths(
			300, 300).axes("X", "Y").build()).get(0);
		assertEquals("745519b1e8822c774b542bc9cb21df78302dde20", //
			ImageHash.hashImg(img1));

		// swapping axis changes the hash
		final SCIFIOImgPlus<?> img2 = opener.openImgs(new TestImgLocation.Builder().lengths(
			300, 300).axes("Y", "X").build()).get(0);
		assertEquals("a4e1400d3f61073def0652f91fe30110059ef809", //
			ImageHash.hashImg(img2));

		// changing dims changes the hash
		final SCIFIOImgPlus<?> img3 = opener.openImgs(new TestImgLocation.Builder().lengths(
			100, 100).axes("Y", "X").build()).get(0);
		assertEquals("f1f869851a0018dd115baa9bb8550da0f03cf0d6", //
			ImageHash.hashImg(img3));

		// 5D is supported
		final SCIFIOImgPlus<?> img4 = opener.openImgs(new TestImgLocation.Builder().lengths(
			100, 100, 3, 3, 3).axes("Y", "X", "Z", "Channel", "Time").build()).get(0);
		assertEquals("3bbd0424c5b53baad73e969aed7b949a102625bb", //
			ImageHash.hashImg(img4));
	}
}
