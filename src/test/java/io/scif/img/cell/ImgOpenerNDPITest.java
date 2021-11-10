/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2021 SCIFIO developers.
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

package io.scif.img.cell;

import static org.junit.Assert.assertEquals;

import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import net.imglib2.type.numeric.real.FloatType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the {@link SCIFIOCellImg} and related classes.
 *
 * @author Jon Fuller
 */
public class ImgOpenerNDPITest {

	private static ImgOpener opener;
	private static Path testImageFile;
	private SCIFIOImgPlus<FloatType> img;

	@BeforeClass
	public static void createOpener() {
		opener = new ImgOpener();
	}

	@AfterClass
	public static void disposeOpener() {
		opener.context().dispose();
	}

	@Before
	public void createSCIFIOCellImg() throws IOException {
		testImageFile = Files.createTempFile("test3-DAPI%202%20(387)%20", ".ndpi");

		URL url = new URL(
			"https://downloads.openmicroscopy.org/images/Hamamatsu-NDPI/manuel/test3-DAPI%202%20(387)%20.ndpi");
		ReadableByteChannel readableByteChannel = Channels.newChannel(url
			.openStream());
		try (FileOutputStream fileOutputStream = new FileOutputStream(testImageFile
			.toFile()))
		{
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0,
				Long.MAX_VALUE);
		}

		SCIFIOConfig config = new SCIFIOConfig();
		config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL);
		img = opener.openImgs(testImageFile.toFile().getAbsolutePath(),
			new FloatType(), config).get(0);
	}

	@After
	public void removeTempFile() throws IOException {
		img.dispose();
		Files.delete(testImageFile);
	}

	/**
	 * Test for https://github.com/scifio/scifio/issues/399 Test will fail after
	 * 10 seconds Hangs for scifio version 0.40.0 and 0.41.0 (so timeout should be
	 * hit) fails (with IllegalArgumentException v0.37.3 - and perhaps 0.39.2)
	 */
	@Ignore // This test runs quickly enough in eclipse but is very slow on the command line
	@Test()
	public void testNDPICompositeChannelLoad() {
		assertEquals(img.getCompositeChannelCount(), 3);

		FloatType y = img.firstElement();
		assertEquals(0f, y.get(), 0.1f);
	}
}
