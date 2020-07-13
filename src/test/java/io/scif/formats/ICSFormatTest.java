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

package io.scif.formats;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;

public class ICSFormatTest extends AbstractFormatTest {

	public ICSFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-ics.zip"),
			new HTTPLocation("https://samples.scif.io/qdna1.zip"));
	}

	private static final String hash_qdna1 =
		"2ec90191e91e76fc8db8d570f47d5713fbdc7df5";

	@Test
	public void testQdna1ICS() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"representation sign\":\"unsigned\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"8 256 256\",\"parameter labels\":\"intensity x-position y-position\",\"layout significant_bits\":\"8\",\"representation byte_order\":\"1\",\"layout order\":\"bits x y\",\"representation format\":\"integer\"},\"filtered\":false,\"datasetName\":\"qdna1.ids\",\"table\":{\"representation sign\":\"unsigned\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"8 256 256\",\"parameter labels\":\"intensity x-position y-position\",\"layout significant_bits\":\"8\",\"representation byte_order\":\"1\",\"layout order\":\"bits x y\",\"representation format\":\"integer\"},\"priority\":0.0}";
		testImg(baseFolder().child("qdna1.zip").child("qdna1.ics"), hash_qdna1,
			meta, new int[] { 256, 256, }, Axes.X, Axes.Y);
	}

	@Test
	public void testQdna1IDS() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"representation sign\":\"unsigned\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"8 256 256\",\"parameter labels\":\"intensity x-position y-position\",\"layout significant_bits\":\"8\",\"representation byte_order\":\"1\",\"layout order\":\"bits x y\",\"representation format\":\"integer\"},\"filtered\":false,\"datasetName\":\"qdna1.ids\",\"table\":{\"representation sign\":\"unsigned\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"8 256 256\",\"parameter labels\":\"intensity x-position y-position\",\"layout significant_bits\":\"8\",\"representation byte_order\":\"1\",\"layout order\":\"bits x y\",\"representation format\":\"integer\"},\"priority\":0.0}";
		testImg(baseFolder().child("qdna1.zip").child("qdna1.ids"), hash_qdna1,
			meta, new int[] { 256, 256, }, Axes.X, Axes.Y);
	}

	@Test
	public void testICSBenchmark() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"representation sign\":\"unsigned\",\"parameter units\":\"bits micrometers micrometers micrometers seconds\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"parameter scale\":\"1.000000 3.6260623 3.6260623 10.0 0.0 1.0\",\"layout sizes\":\"16 64 64 5 1 2\",\"layout significant_bits\":\"16\",\"representation byte_order\":\"1 2\",\"layout order\":\"bits x y z t ch\",\"representation format\":\"integer\"},\"filtered\":false,\"datasetName\":\"benchmark_v1_2018_x64y64z5c2s1t1.ids\",\"table\":{\"representation sign\":\"unsigned\",\"parameter units\":\"bits micrometers micrometers micrometers seconds\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"parameter scale\":\"1.000000 3.6260623 3.6260623 10.0 0.0 1.0\",\"layout sizes\":\"16 64 64 5 1 2\",\"layout significant_bits\":\"16\",\"representation byte_order\":\"1 2\",\"layout order\":\"bits x y z t ch\",\"representation format\":\"integer\"},\"priority\":0.0}";
		testImg(baseFolder().child("test-ics.zip").child(
			"benchmark_v1_2018_x64y64z5c2s1t1.ics"),
			"8519bc6a422f69a9d112afa34b164b29df68f1fe", meta, new int[] { 64, 64, 5,
				1, 2, }, Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL);

	}

	@Test
	public void testICSBenchmark2() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"representation sign\":\"unsigned\",\"parameter units\":\"bits micrometers micrometers micrometers seconds\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"parameter scale\":\"1.000000 3.6260623 3.6260623 10.0 0.0 1.0\",\"layout sizes\":\"16 64 64 5 1 1\",\"layout significant_bits\":\"16\",\"representation byte_order\":\"1 2\",\"layout order\":\"bits x y z t ch\",\"representation format\":\"integer\"},\"filtered\":false,\"datasetName\":\"benchmark_v1_2018_x64y64z5c2s1t1_C0.ids\",\"table\":{\"representation sign\":\"unsigned\",\"parameter units\":\"bits micrometers micrometers micrometers seconds\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"parameter scale\":\"1.000000 3.6260623 3.6260623 10.0 0.0 1.0\",\"layout sizes\":\"16 64 64 5 1 1\",\"layout significant_bits\":\"16\",\"representation byte_order\":\"1 2\",\"layout order\":\"bits x y z t ch\",\"representation format\":\"integer\"},\"priority\":0.0}";
		testImg(baseFolder().child("test-ics.zip").child("benchmark_v1_2018_x64y64z5c2s1t1_C0.ics"),
			"1f5f6ee975d2bf566a19e3e8728f1a9e351bd774", meta, new int[] { 64, 64, 5 },
			Axes.X, Axes.Y, Axes.Z);
	}

	@Test
	public void testICSBenchmark3() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"sensor s_params lambdaem\":\"450.000000 520.000000\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"32 64 64 5 1 2\",\"layout significant_bits\":\"32\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"bits micrometers micrometers micrometers seconds undefined\",\"sensor s_params pinholeradius\":\"1250.000000 1250.000000\",\"parameter scale\":\"1.000000 3.600000 3.600000 10.000000 1.000000 1.000000\",\"parameter labels\":\"intensity x y z t ch\",\"sensor s_params lambdaex\":\"405.000000 488.000000\",\"representation byte_order\":\"1 2 3 4\",\"layout order\":\"bits x y z t ch\"},\"filtered\":false,\"datasetName\":\"benchmark_v1_2018_x64y64z5c2s1t11_5b7fee6e758d1_hrm.ids\",\"table\":{\"sensor s_params lambdaem\":\"450.000000 520.000000\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"32 64 64 5 1 2\",\"layout significant_bits\":\"32\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"bits micrometers micrometers micrometers seconds undefined\",\"sensor s_params pinholeradius\":\"1250.000000 1250.000000\",\"parameter scale\":\"1.000000 3.600000 3.600000 10.000000 1.000000 1.000000\",\"parameter labels\":\"intensity x y z t ch\",\"sensor s_params lambdaex\":\"405.000000 488.000000\",\"representation byte_order\":\"1 2 3 4\",\"layout order\":\"bits x y z t ch\"},\"priority\":0.0}";
		testImg(baseFolder().child("test-ics.zip").child(
			"benchmark_v1_2018_x64y64z5c2s1t11_5b7fee6e758d1_hrm.ics"),
			"9cb8c14508b513b7b34ca82d15a56c153ac6561b", meta, new int[] { 64, 64, 5,
				1, 2 }, Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL);
	}

	@Test
	public void testICSBenchmark4() {
		final String meta =
			"{\"versionTwo\":false,\"offset\":0,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"sensor s_params lambdaem\":\"450.000000\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"32 64 64 5\",\"layout significant_bits\":\"32\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"bits micrometers micrometers micrometers\",\"sensor s_params pinholeradius\":\"1250.000000\",\"parameter scale\":\"1.000000 3.600000 3.600000 10.000000\",\"parameter labels\":\"intensity x y z\",\"sensor s_params lambdaex\":\"405.000000\",\"representation byte_order\":\"1 2 3 4\",\"layout order\":\"bits x y z\"},\"filtered\":false,\"datasetName\":\"benchmark_v1_2018_x64y64z5c2s1t11_w1Laser4054BD4BP_5b8e487168590_hrm.ids\",\"table\":{\"sensor s_params lambdaem\":\"450.000000\",\"ics_version\":\"1.0\",\"representation compression\":\"uncompressed\",\"layout sizes\":\"32 64 64 5\",\"layout significant_bits\":\"32\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"bits micrometers micrometers micrometers\",\"sensor s_params pinholeradius\":\"1250.000000\",\"parameter scale\":\"1.000000 3.600000 3.600000 10.000000\",\"parameter labels\":\"intensity x y z\",\"sensor s_params lambdaex\":\"405.000000\",\"representation byte_order\":\"1 2 3 4\",\"layout order\":\"bits x y z\"},\"priority\":0.0}";
		testImg(baseFolder().child("test-ics.zip").child(
			"benchmark_v1_2018_x64y64z5c2s1t11_w1Laser4054BD4BP_5b8e487168590_hrm.ics"),
			"73ac1eabfabb7aea92a35b1f7697e2370ac0c60a", meta, new int[] { 64, 64, 5 },
			Axes.X, Axes.Y, Axes.Z);
	}
}
