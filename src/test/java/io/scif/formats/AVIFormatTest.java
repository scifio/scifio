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

public class AVIFormatTest extends AbstractFormatTest {

	public AVIFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-avi.zip"));
	}

	@Test
	public void t1_head() {
		final String hash_one = "41847e0e91a42f25997af1bbf636509c85914fbe";
		final String meta =
			"{\"offsets\":[4104,49456,94808,140160,185512,230864,276216,321568,366920,412272,457624,502976,548328,593680,639032,684384,729736,775088,820440,865792,911144,956496,1001848,1047200,1092552,1137904,1183256,1228608,1273960,1319312,1364664,1410016,1455368,1500720,1546072,1591424],\"lengths\":[45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344,45344],\"bmpBitsPerPixel\":8,\"bmpCompression\":0,\"bmpScanLineSize\":208,\"bmpColorsUsed\":256,\"bmpWidth\":208,\"bytesPerPlane\":0,\"lastPlaneIndex\":-1,\"filtered\":false,\"datasetName\":\"t1-rendering.avi\",\"table\":{\"Bitmap compression value\":0,\"Frame height\":218,\"Compression\":\"Raw (uncompressed)\",\"Number of colors used\":256,\"Microseconds per frame\":100000,\"Horizontal resolution\":0,\"Frame width\":206,\"Frame rate\":0,\"Scale factor\":0,\"Stream sample size\":0,\"Max. bytes per second\":0,\"Vertical resolution\":0,\"Bits per pixel\":8,\"Total frames\":36,\"Length\":0,\"Stream quality\":-1,\"Initial frames\":0,\"Start time\":0},\"priority\":0.0}";
		testImg(baseFolder().child("t1-rendering.avi"), hash_one, meta, new int[] {
			206, 218, 3, 36 }, Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME);
	}

}
