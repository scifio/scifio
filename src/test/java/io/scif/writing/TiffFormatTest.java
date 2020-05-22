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

import io.scif.codec.CompressionType;
import io.scif.config.SCIFIOConfig;
import io.scif.img.IO;
import io.scif.io.location.TestImgLocation;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imagej.ImgPlus;

import org.junit.Test;

public class TiffFormatTest extends AbstractSyntheticWriterTest {

	public TiffFormatTest() {
		super(".tif");
	}

	public void testJPEGCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16 };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.JPEG.toString());
		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType(formatString).axes("X", "Y", "C").lengths(100, 100,
					3).build());
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testJ2kCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.J2K.toString());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType(formatString).axes("X", "Y", "C").lengths(100, 100,
					3).build());
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testLZWCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.LZW.toString());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType(formatString).axes("X", "Y", "C").lengths(100, 100,
					3).build());
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testJ2kLossyCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.J2K_LOSSY.toString());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType(formatString).axes("X", "Y", "C").lengths(100, 100,
					3).build());
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testNoCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.UNCOMPRESSED.toString());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType(formatString).axes("X", "Y", "C").lengths(100, 100,
					3).build());
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testWriting_uint8_funkyDims() throws IOException {
		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C").lengths(100, 100, 3)
			.build());
		testWriting(sourceImg);

		final ImgPlus<?> sourceImg2 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C", "Time").lengths(100,
				100, 3, 3).build());
		testWriting(sourceImg2);

		final ImgPlus<?> sourceImg3 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "Channel", "Z", "Time")
			.lengths(100, 100, 3, 10, 13).build());
		testWriting(sourceImg3);

		final ImgPlus<?> sourceImg4 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C", "Z", "T").lengths(100,
				100, 3, 3, 3).build());
		testWriting(sourceImg4);

		final ImgPlus<?> sourceImg5 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "Z", "Custom").lengths(100,
				100, 3, 3).build());
		testWriting(sourceImg5);

		final ImgPlus<?> sourceImg6 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y").lengths(100, 100).build());
		testWriting(sourceImg6);
	}

}
