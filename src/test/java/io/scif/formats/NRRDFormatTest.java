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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;

public class NRRDFormatTest extends AbstractFormatTest {

	public NRRDFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-nrrd.zip"));
	}

	// TEMP: Disable tests until remote test file is in place.

//	@Test
	public void baseTest() {
		final String metaJson = "{\"encoding\":\"raw\",\"offset\":0,\"lookForCompanion\":true,\"initializeHelper\":false,\"filtered\":false,\"datasetName\":\"dt-helix.nhdr\",\"table\":{\"axis maxs\":\"NaN 2 2 2\",\"sizes\":\"7 38 39 40\",\"data file\":\"./dt-helix.raw\",\"type\":\"float\",\"axis mins\":\"NaN -2 -2 -2\",\"encoding\":\"raw\",\"dimension\":\"4\",\"endian\":\"big\"},\"priority\":0.0}";
		testImg(baseFolder().child("dt-helix.nhdr"),
			"7e36a3c1ba03af681db51fdb78c95e6da31b8a4b", metaJson, new int[] { 38,
				39, 7 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}

//	@Test
	public void baseTest2() {
		final String metaJson = "{\"encoding\":\"raw\",\"offset\":0,\"lookForCompanion\":true,\"initializeHelper\":false,\"filtered\":false,\"datasetName\":\"dt-helix.nhdr\",\"table\":{\"axis maxs\":\"NaN 2 2 2\",\"sizes\":\"7 38 39 40\",\"data file\":\"./dt-helix.raw\",\"type\":\"float\",\"axis mins\":\"NaN -2 -2 -2\",\"encoding\":\"raw\",\"dimension\":\"4\",\"endian\":\"big\"},\"priority\":0.0}";
		testImg(baseFolder().child("dt-helix.raw"),
			"7e36a3c1ba03af681db51fdb78c95e6da31b8a4b", metaJson, new int[] { 38,
				39, 7 }, Axes.X, Axes.Y, Axes.CHANNEL);
	}
}
