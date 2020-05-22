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

package io.scif.formats;

import io.scif.img.IO;
import io.scif.img.SCIFIOImgPlus;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;
import org.scijava.io.location.FileLocation;

public class EPSFormatTest extends AbstractFormatTest {

	private static final String hash_one =
		"c6a27eedfc8880ef46d49cc5f02f0002cde48200";

	public EPSFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-eps.zip"));
	}

	/**
	 */
	@Test
	public void testOne() {
		final String meta =
			"{\"start\":12,\"binary\":false,\"isTiff\":false,\"filtered\":false,\"datasetName\":\"scifio-test.eps\",\"table\":{\"%%Title\":\" scifio-test.eps\",\"Y-coordinate of origin\":0,\"%%Creator\":\" SCIFIO\",\"%%Pages\":\" 1\",\"X-coordinate of origin\":0},\"priority\":0.0}";
		testImg(baseFolder().child("scifio-test.eps"), hash_one, meta, new int[] {
			500, 500, 3 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

//	@Test
	public void createTestImg() {
		final String source = "/home/gabriel/Desktop/input/scifio-test.png";
		final SCIFIOImgPlus<?> img = IO.open(source);
		IO.save(new FileLocation("/home/gabriel/Desktop/input/scifio-test.eps"),
			img);
	}

}
