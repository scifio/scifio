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

public class APNGFormatTest extends AbstractFormatTest {

	public APNGFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-png.zip"));
	}

	@Test
	public void baseTest() {
		final String metaJson =
			"{\"ihdr\":{\"width\":500,\"height\":500,\"bitDepth\":8,\"colourType\":2,\"compressionMethod\":0,\"filterMethod\":0,\"interlaceMethod\":0,\"offset\":16,\"length\":13,\"chunkSignature\":[73,72,68,82]},\"iend\":{\"offset\":72475,\"length\":0,\"chunkSignature\":[73,69,78,68]},\"separateDefault\":true,\"signed\":false,\"littleEndian\":false,\"filtered\":false,\"datasetName\":\"scifio-test.png\",\"table\":{},\"priority\":0.0}";

		testImg(baseFolder().child("scifio-test.png"),
			"c6a27eedfc8880ef46d49cc5f02f0002cde48200", metaJson, new int[] { 500,
				500, 3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

	@Test
	public void testAlpha() {
		final String meta =
			"{\"ihdr\":{\"width\":500,\"height\":500,\"bitDepth\":8,\"colourType\":6,\"compressionMethod\":0,\"filterMethod\":0,\"interlaceMethod\":0,\"offset\":16,\"length\":13,\"chunkSignature\":[73,72,68,82]},\"iend\":{\"offset\":103072,\"length\":0,\"chunkSignature\":[73,69,78,68]},\"separateDefault\":true,\"signed\":false,\"littleEndian\":false,\"filtered\":false,\"datasetName\":\"scifio-test-with-alpha.png\",\"table\":{},\"priority\":0.0}";
		testImg(baseFolder().child("scifio-test-with-alpha.png"),
			"d550d2268c47f8038db80dd066ebe0d807e3b5f2", meta, new int[] { 500, 500,
				4 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

	@Test
	public void testAnimated() {
		final String meta =
			"{\"actl\":{\"sequenceNumber\":0,\"numFrames\":453,\"numPlays\":0,\"offset\":41,\"length\":8,\"chunkSignature\":[97,99,84,76]},\"ihdr\":{\"width\":530,\"height\":480,\"bitDepth\":8,\"colourType\":0,\"compressionMethod\":0,\"filterMethod\":0,\"interlaceMethod\":0,\"offset\":16,\"length\":13,\"chunkSignature\":[73,72,68,82]},\"iend\":{\"offset\":975537,\"length\":0,\"chunkSignature\":[73,69,78,68]},\"separateDefault\":false,\"signed\":false,\"littleEndian\":false,\"filtered\":false,\"datasetName\":\"scifio-test-animated.png\",\"table\":{},\"priority\":0.0}";
		testImg(baseFolder().child("scifio-test-animated.png"),
			"b73af3c4d7ae198eb8a3156af8ac0736c1cbec07", meta, new int[] { 530, 480,
				453 }, Axes.X, Axes.Y, Axes.TIME);
	}

}
