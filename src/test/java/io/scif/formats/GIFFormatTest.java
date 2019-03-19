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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;

public class GIFFormatTest extends AbstractFormatTest {

	public GIFFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-gif.zip"));
	}

	@Test
	public void testBase() {
		final String meta =
			"{\"interlace\":false,\"ix\":0,\"iy\":0,\"iw\":500,\"ih\":500,\"blockSize\":0,\"dispose\":0,\"lastDispose\":0,\"transparency\":true,\"transIndex\":255,\"filtered\":false,\"datasetName\":\"scifio-test.gif\",\"table\":{\"Global lookup table size\":256},\"priority\":0.0}";
		testImg(baseFolder().child("scifio-test.gif"),
			"8c661ea3f2a593202639f94adf6a3d4f874f8076", meta, new int[] { 500, 500,
				3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

	@Test
	public void testAnimated() {
		final String meta =
			"{\"interlace\":false,\"ix\":529,\"iy\":479,\"iw\":1,\"ih\":1,\"blockSize\":0,\"dispose\":2,\"lastDispose\":2,\"transparency\":true,\"transIndex\":1,\"filtered\":false,\"datasetName\":\"scifio-test-animated.gif\",\"table\":{\"Global lookup table size\":256},\"priority\":0.0}";
		testImg(baseFolder().child("scifio-test-animated.gif"),
			"b73af3c4d7ae198eb8a3156af8ac0736c1cbec07", meta, new int[] { 530, 480, 3,
				151 }, Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME);
	}
}
