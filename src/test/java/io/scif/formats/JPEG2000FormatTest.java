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

package io.scif.formats;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;
import org.scijava.io.location.FileLocation;

public class JPEG2000FormatTest extends AbstractFormatTest {

	public JPEG2000FormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-jpeg2000.zip"));
	}

	/**
	 */
	@Test
	public void testOne() {
		final String meta =
			"{\"pixelsOffset\":85,\"lastIndex\":{\"imageIndex\":-1,\"planeIndex\":-1},\"resolutionLevels\":5,\"filtered\":false,\"datasetName\":\"scifio-test.jp2\",\"table\":{\"Comment\":\"Created with GIMP\"},\"priority\":0.0}";
		final String hash = "c6a27eedfc8880ef46d49cc5f02f0002cde48200";
		testImg(baseFolder().child("scifio-test.jp2"), hash, meta, new int[] { 500,
			500, 3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

	@Test
	public void testTwo() {
		final String meta =
			"{\"pixelsOffset\":119,\"lastIndex\":{\"imageIndex\":-1,\"planeIndex\":-1},\"resolutionLevels\":5,\"filtered\":false,\"datasetName\":\"scifio-test-with-alpha.jp2\",\"table\":{\"Comment\":\"Created with GIMP\"},\"priority\":0.0}";
		final String hash = "d550d2268c47f8038db80dd066ebe0d807e3b5f2";
		testImg(baseFolder().child("scifio-test-with-alpha.jp2"), hash, meta,
			new int[] { 500, 500, 4 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}
}
