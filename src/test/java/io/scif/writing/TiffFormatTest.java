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

package io.scif.writing;

import io.scif.SCIFIO;
import io.scif.codec.CompressionType;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;
import io.scif.io.location.TestImgLocation;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.imagej.ImgPlus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

public class TiffFormatTest extends AbstractSyntheticWriterTest {

	private static ImgOpener opener;
	private static ImgSaver saver;

	@BeforeClass
	public static void setUp() {
		SCIFIO scifio = new SCIFIO();
		opener = new ImgOpener(scifio.context());
		saver = new ImgSaver(scifio.context());
	}

	@AfterClass
	public static void tearDown() {
		opener.getContext().dispose();
	}

	public TiffFormatTest() {
		super(".tif");
	}

	public void testJPEGCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16 };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.JPEG.getCompression());
		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	@Ignore
	public void testJ2kCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.J2K.getCompression());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testLZWCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.LZW.getCompression());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testZLIBCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.ZLIB.getCompression());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	@Ignore
	public void testJ2kLossyCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.J2K_LOSSY.getCompression());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testNoCompression() throws IOException {
		final int[] formats = new int[] { FormatTools.INT8, FormatTools.UINT8,
			FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
			FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };

		final SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression(CompressionType.UNCOMPRESSED.getCompression());

		for (final int f : formats) {
			final String formatString = FormatTools.getPixelTypeString(f);
			final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType(formatString).axes("X", "Y", "C").lengths(
					100, 100, 3).build()).get(0);
			testWriting(sourceImg, config);
		}
	}

	@Test
	public void testWriting_uint8_funkyDims() throws IOException {
		final ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y", "C").lengths(100, 100,
				3).build()).get(0);
		testWriting(sourceImg);

		final ImgPlus<?> sourceImg2 = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y", "C", "Time").lengths(
				100, 100, 3, 3).build()).get(0);
		testWriting(sourceImg2);

		final ImgPlus<?> sourceImg3 = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y", "Channel", "Z", "Time")
			.lengths(100, 100, 3, 10, 13).build()).get(0);
		testWriting(sourceImg3);

		final ImgPlus<?> sourceImg4 = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y", "C", "Z", "T").lengths(
				100, 100, 3, 3, 3).build()).get(0);
		testWriting(sourceImg4);

		final ImgPlus<?> sourceImg5 = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y", "Z", "Custom").lengths(
				100, 100, 3, 3).build()).get(0);
		testWriting(sourceImg5);

		final ImgPlus<?> sourceImg6 = opener.openImgs(new TestImgLocation.Builder()
			.name("testimg").pixelType("uint8").axes("X", "Y").lengths(100, 100)
			.build()).get(0);
		testWriting(sourceImg6);
	}

	/**
	 * Ensure a valid TIFF is written (i.e. the header is written) when the
	 * destination file doesn't exist (vs. when using
	 * {@link #createTempFileLocation(String)} an empty file is created).
	 */
	@Test
	public void testWriteToNewLocation() throws IOException {
		final Path tempDir = Files.createTempDirectory("scifio-test");
		final Location sampleImage = new TestImgLocation.Builder().name(
			"8bit-unsigned").pixelType("uint8").indexed(false).planarDims(2).lengths(
				10, 10).axes("X", "Y").build();
		SCIFIOImgPlus<?> img = opener.openImgs(sampleImage).get(0);
		FileLocation saveLocation = new FileLocation(new File(tempDir.toFile(),
			"test.tif"));
		saver.saveImg(saveLocation, img);
		opener.openImgs(saveLocation);
	}

	/**
	 * Verify we get an {@link ImgIOException} when writing to a destination that
	 * already exists.
	 */
	@Test(expected = io.scif.img.ImgIOException.class)
	public void testFailIfOverwriting() throws IOException {
		testOverwritingBehavior();
	}

	/**
	 * Verify we can overwrite an existing file with {@link SCIFIOConfig}.
	 * <p>
	 * NB: for TIFF this technically appends the image
	 * </p>
	 */
	@Test
	public void testSuccessfulOverwrite() throws IOException {
		final SCIFIOConfig config = new SCIFIOConfig().writerSetFailIfOverwriting(
			false);
		FileLocation overwritten = testOverwritingBehavior(config);
		opener.openImgs(overwritten);
	}
}
