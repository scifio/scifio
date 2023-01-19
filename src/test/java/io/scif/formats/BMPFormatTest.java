/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2023 SCIFIO developers.
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;

public class BMPFormatTest extends AbstractFormatTest {

	public BMPFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/kidney_TFl.zip"));
	}

	@Test
	public void testKidney_TFl_1() {
		final String meta =
			"{\"compression\":0,\"global\":54,\"invertY\":false,\"filtered\":false,\"datasetName\":\"kidney_TFl_1.bmp\",\"table\":{\"Image width\":355,\"Bits per pixel\":24,\"Magic identifier\":\"BM\",\"Compression type\":\"None\",\"Image height\":361,\"Indexed color\":false,\"Y resolution\":3780,\"X resolution\":3780,\"File size (in bytes)\":385602,\"Color planes\":1},\"priority\":0.0}";
		testImg(baseFolder().child("kidney_TFl_1.bmp"),
			"df329c4dadda72cb45e9671c88404fd0edabf916", meta, new int[] { 355, 361,
				3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

	@Test
	public void testKidney_TFl_2() {
		final String meta =
			"{\"compression\":0,\"global\":54,\"invertY\":false,\"filtered\":false,\"datasetName\":\"kidney_TFl_2.bmp\",\"table\":{\"Image width\":384,\"Bits per pixel\":24,\"Magic identifier\":\"BM\",\"Compression type\":\"None\",\"Image height\":385,\"Indexed color\":false,\"Y resolution\":3780,\"X resolution\":3780,\"File size (in bytes)\":443574,\"Color planes\":1},\"priority\":0.0}";
		testImg(baseFolder().child("kidney_TFl_2.bmp"),
			"b53e8d6a4b7a08b9f93855a93f0351889ce2bf28", meta, new int[] { 384, 385,
				3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

}
