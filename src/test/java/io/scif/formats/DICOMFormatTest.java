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

public class DICOMFormatTest extends AbstractFormatTest {

	public DICOMFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-dicom.zip"));
	}

	// TEMP: Disable tests until remote test file is in place.

//	@Test
	public void baseTest() {
		final String metaJson = "{\"offsets\":[438],\"isJP2K\":false,\"isJPEG\":false,\"isRLE\":false,\"isDeflate\":false,\"oddLocations\":false,\"maxPixelValue\":-1,\"imagesPerFile\":1,\"rescaleSlope\":1.0,\"rescaleIntercept\":0.0,\"inverted\":false,\"imageType\":\"ORIGINAL\\\\SECONDARY\\\\OTHER\\\\ARC\\\\DICOM\\\\VALIDATION\",\"originalInstance\":\"1\",\"originalSeries\":0,\"filtered\":false,\"datasetName\":\"OT-MONO2-8-a7.dcm\",\"table\":{},\"priority\":0.0}";
		testImg(baseFolder().child("OT-MONO2-8-a7.dcm"),
			"ff767bcdc3c29f5d59ba83f5e5bedf82dc9a08a2", metaJson, new int[] { 512,
				512, }, Axes.X, Axes.Y);
	}

//	@Test
	public void baseTest2() {
		final String metaJson = "{\"offsets\":[444],\"isJP2K\":false,\"isJPEG\":false,\"isRLE\":false,\"isDeflate\":false,\"oddLocations\":false,\"maxPixelValue\":-1,\"imagesPerFile\":1,\"rescaleSlope\":1.0,\"rescaleIntercept\":0.0,\"inverted\":false,\"imageType\":\"ORIGINAL\\\\SECONDARY\\\\OTHER\\\\ARC\\\\DICOM\\\\VALIDATION\",\"originalInstance\":\"1\",\"originalSeries\":0,\"filtered\":false,\"datasetName\":\"OT-MONO2-8-colon.dcm\",\"table\":{},\"priority\":0.0}";
		testImg(baseFolder().child("OT-MONO2-8-colon.dcm"),
			"cadcd7255a95e1f92054c00177b15ef1a6f567d4", metaJson, new int[] { 512,
				512, }, Axes.X, Axes.Y);
	}

//	@Test
	public void baseTest3() {
		final String metaJson = "{\"offsets\":[444],\"isJP2K\":false,\"isJPEG\":false,\"isRLE\":false,\"isDeflate\":false,\"oddLocations\":false,\"maxPixelValue\":-1,\"imagesPerFile\":1,\"rescaleSlope\":1.0,\"rescaleIntercept\":0.0,\"inverted\":false,\"imageType\":\"ORIGINAL\\\\SECONDARY\\\\OTHER\\\\ARC\\\\DICOM\\\\VALIDATION\",\"originalInstance\":\"1\",\"originalSeries\":0,\"filtered\":false,\"datasetName\":\"OT-MONO2-8-hip.dcm\",\"table\":{},\"priority\":0.0}";
		testImg(baseFolder().child("OT-MONO2-8-hip.dcm"),
			"1bbaa19529e2d7e689b17ec75f1e1a52379b27f9", metaJson, new int[] { 512,
				512, }, Axes.X, Axes.Y);
	}
}
