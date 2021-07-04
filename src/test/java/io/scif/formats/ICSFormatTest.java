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
import io.scif.img.axes.SCIFIOAxes;

public class ICSFormatTest extends AbstractFormatTest {

	public ICSFormatTest() throws URISyntaxException, MalformedURLException {
		super(new HTTPLocation("https://samples.scif.io/test-ics.zip"),
			new HTTPLocation("https://samples.scif.io/qdna1.zip"),
			new HTTPLocation("https://samples.scif.io/Gray-FLIM-datasets.zip"));
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

	@Test
	public void testGrayOldICS() {
		testImg(baseFolder().child("Gray-FLIM-datasets.zip").child("Csarseven.ics"),
			"88002ed5d209804b593af044fa0bee070f36ddc7", new int[] { 128, 128, 256, }, Axes.X, Axes.Y, SCIFIOAxes.LIFETIME);
	}

	@Test
	public void testGrayICS1() {
		final String meta =
			"{\"versionTwo\":true,\"offset\":3856,\"hasInstrumentData\":true,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"history stage positionx\":\"3515.51\",\"history stage positiony\":\"-2079.3\",\"history extents\":\"1.500896e-008 1.0000e-006 1.0000e-006\",\"history stage positionz\":\"3.75512\",\"ics_version\":\"2.0\",\"layout sizes\":\"16 256 1 1\",\"history filterset emm\":\"nm min 573 max 613\",\"history laser power\":\"0\",\"layout significant_bits\":\"16\",\"history filterset exc\":\"nm min 533 max 548\",\"representation format\":\"integer\",\"history microscope\":\"built on 2015/09/09 15:08:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative ns microns microns\",\"history filterset\":\"semrock cy3\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.058629 1.000000 1.000000\",\"history labels\":\"t x y\",\"history filterset dichroic name\":\"nt48-492 30r/70t\",\"history filterset exc name\":\"ff01-540/15-25\",\"history experimenter\":\"gregory\",\"history creation date\":\"10:29:38 28\\\\10\\\\16\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 420 max 1000\",\"history filterset dichroic\":\"nm 0\",\"representation sign\":\"unsigned\",\"history laser rep rate\":\"40 mhz\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 330 max 380\",\"layout order\":\"bits x y z\",\"history units\":\"s m m\",\"history filterset emm name\":\"ff01-593/40-25\",\"history laser manufacturer\":\"fianium\",\"history laser model\":\"wl-sc-400-4 101784\"},\"filtered\":false,\"datasetName\":\"Gray_Galileo_Prompt.ics\",\"table\":{\"history stage positionx\":\"3515.51\",\"history stage positiony\":\"-2079.3\",\"history extents\":\"1.500896e-008 1.0000e-006 1.0000e-006\",\"history stage positionz\":\"3.75512\",\"ics_version\":\"2.0\",\"layout sizes\":\"16 256 1 1\",\"history filterset emm\":\"nm min 573 max 613\",\"history laser power\":\"0\",\"layout significant_bits\":\"16\",\"history filterset exc\":\"nm min 533 max 548\",\"representation format\":\"integer\",\"history microscope\":\"built on 2015/09/09 15:08:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative ns microns microns\",\"history filterset\":\"semrock cy3\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.058629 1.000000 1.000000\",\"history labels\":\"t x y\",\"history filterset dichroic name\":\"nt48-492 30r/70t\",\"history filterset exc name\":\"ff01-540/15-25\",\"history experimenter\":\"gregory\",\"history creation date\":\"10:29:38 28\\\\10\\\\16\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 420 max 1000\",\"history filterset dichroic\":\"nm 0\",\"representation sign\":\"unsigned\",\"history laser rep rate\":\"40 mhz\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 330 max 380\",\"layout order\":\"bits x y z\",\"history units\":\"s m m\",\"history filterset emm name\":\"ff01-593/40-25\",\"history laser manufacturer\":\"fianium\",\"history laser model\":\"wl-sc-400-4 101784\"},\"priority\":0.0}";
		testImg(baseFolder().child("Gray-FLIM-datasets.zip").child("Gray_Galileo_Prompt.ics"),
			"da39a3ee5e6b4b0d3255bfef95601890afd80709", meta, new int[] { 1, 1, 256, },
			Axes.X, Axes.Y, SCIFIOAxes.LIFETIME);
	}

	@Test
	public void testGrayICS2() {
		final String meta =
			"{\"versionTwo\":true,\"offset\":2162,\"hasInstrumentData\":true,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"history stage positionx\":\"6707.31\",\"history stage positiony\":\"-10912.3\",\"history extents\":\"3.293e-004 3.293e-004\",\"history stage positionz\":\"-36.9077\",\"ics_version\":\"2.0\",\"history gain\":\"255.00\",\"layout sizes\":\"16 1024 1024\",\"history camera model\":\"hamamatsu 1394 orca-era\",\"layout significant_bits\":\"16\",\"representation format\":\"integer\",\"history microscope\":\"built on 2012/12/13 09:22:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative microns microns\",\"parameter scale\":\"1.000000 0.321543 0.321543\",\"history labels\":\"x y\",\"history experimenter\":\"gregory\",\"history creation date\":\"12:47:54 21\\\\12\\\\12\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 573 max 648\",\"representation sign\":\"unsigned\",\"parameter labels\":\"intensity x-position y-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 530 max 560\",\"layout order\":\"bits x y\",\"history units\":\"m m\"},\"filtered\":false,\"datasetName\":\"Gray_Metabric_HER23_D_Cy3 NX.ics\",\"table\":{\"history stage positionx\":\"6707.31\",\"history stage positiony\":\"-10912.3\",\"history extents\":\"3.293e-004 3.293e-004\",\"history stage positionz\":\"-36.9077\",\"ics_version\":\"2.0\",\"history gain\":\"255.00\",\"layout sizes\":\"16 1024 1024\",\"history camera model\":\"hamamatsu 1394 orca-era\",\"layout significant_bits\":\"16\",\"representation format\":\"integer\",\"history microscope\":\"built on 2012/12/13 09:22:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative microns microns\",\"parameter scale\":\"1.000000 0.321543 0.321543\",\"history labels\":\"x y\",\"history experimenter\":\"gregory\",\"history creation date\":\"12:47:54 21\\\\12\\\\12\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 573 max 648\",\"representation sign\":\"unsigned\",\"parameter labels\":\"intensity x-position y-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 530 max 560\",\"layout order\":\"bits x y\",\"history units\":\"m m\"},\"priority\":0.0}";
		testImg(baseFolder().child("Gray-FLIM-datasets.zip").child("Gray_Metabric_HER23_D_Cy3 NX.ics"),
			"0985024ea32be0b5d84fbf14c649971606ea02dc", meta, new int[] { 1024, 1024, },
			Axes.X, Axes.Y);
	}

	@Test
	public void testGrayICS3() {
		final String meta =
			"{\"versionTwo\":true,\"offset\":3893,\"hasInstrumentData\":true,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"history stage positionx\":\"6707.36\",\"history stage positiony\":\"-10912.2\",\"history extents\":\"1.500896e-008 3.2926e-004 3.2926e-004\",\"history stage positionz\":\"-36.9077\",\"ics_version\":\"2.0\",\"layout sizes\":\"16 256 256 256\",\"history filterset emm\":\"nm min 573 max 613\",\"history laser power\":\"0\",\"layout significant_bits\":\"16\",\"history filterset exc\":\"nm min 533 max 548\",\"representation format\":\"integer\",\"history microscope\":\"built on 2012/12/13 09:22:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative ns microns microns\",\"history filterset\":\"semroc - cy3 with dichroic\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.058629 1.286172 1.286172\",\"history labels\":\"t x y\",\"history filterset dichroic name\":\"ff562-di02-25x36\",\"history filterset exc name\":\"ff01-540/15-25\",\"history experimenter\":\"gregory\",\"history creation date\":\"12:53:15 21\\\\12\\\\12\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 420 max 1000\",\"history filterset dichroic\":\"nm 562\",\"representation sign\":\"unsigned\",\"history laser rep rate\":\"20 mhz\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 330 max 380\",\"layout order\":\"bits x y z\",\"history units\":\"s m m\",\"history filterset emm name\":\"ff01-593/40-25\",\"history laser manufacturer\":\"fianium\",\"history laser model\":\"sc400-4-20\"},\"filtered\":false,\"datasetName\":\"Gray_Metabric_HER23_D_FLIM.ics\",\"table\":{\"history stage positionx\":\"6707.36\",\"history stage positiony\":\"-10912.2\",\"history extents\":\"1.500896e-008 3.2926e-004 3.2926e-004\",\"history stage positionz\":\"-36.9077\",\"ics_version\":\"2.0\",\"layout sizes\":\"16 256 256 256\",\"history filterset emm\":\"nm min 573 max 613\",\"history laser power\":\"0\",\"layout significant_bits\":\"16\",\"history filterset exc\":\"nm min 533 max 548\",\"representation format\":\"integer\",\"history microscope\":\"built on 2012/12/13 09:22:50\",\"history objective na\":\"0.500000\",\"parameter units\":\"relative ns microns microns\",\"history filterset\":\"semroc - cy3 with dichroic\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.058629 1.286172 1.286172\",\"history labels\":\"t x y\",\"history filterset dichroic name\":\"ff562-di02-25x36\",\"history filterset exc name\":\"ff01-540/15-25\",\"history experimenter\":\"gregory\",\"history creation date\":\"12:53:15 21\\\\12\\\\12\",\"representation compression\":\"gzip\",\"history cube emm nm\":\"min 420 max 1000\",\"history filterset dichroic\":\"nm 562\",\"representation sign\":\"unsigned\",\"history laser rep rate\":\"20 mhz\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history manufacturer\":\"gray institute\",\"history objective mag\":\"20\",\"representation byte_order\":\"1 2\",\"history cube exc nm\":\"min 330 max 380\",\"layout order\":\"bits x y z\",\"history units\":\"s m m\",\"history filterset emm name\":\"ff01-593/40-25\",\"history laser manufacturer\":\"fianium\",\"history laser model\":\"sc400-4-20\"},\"priority\":0.0}";
		testImg(baseFolder().child("Gray-FLIM-datasets.zip").child("Gray_Metabric_HER23_D_FLIM.ics"),
			"8d1e61db0feaf40e4d1dd0e9882f1770e177429f", meta, new int[] { 256, 256, 256, },
			Axes.X, Axes.Y, SCIFIOAxes.LIFETIME);
	}

	@Test
	public void testSimulatedICS() {
		final String meta =
			"{\"versionTwo\":true,\"offset\":1072,\"hasInstrumentData\":false,\"storedRGB\":false,\"icsId\":\"\",\"idsId\":\"\",\"keyValPairs\":{\"history extents\":\"1.000000e-08 128 128\",\"ics_version\":\"2.0\",\"representation compression\":\"gzip\",\"layout sizes\":\"64 256 128 128\",\"layout significant_bits\":\"64\",\"history author\":\"pbarber\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"relative ns microns microns\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.039000 1.000000 1.000000\",\"history created on\":\"23:14:09 18-11-2008\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history labels\":\"t x y\",\"representation byte_order\":\"1 2 3 4 5 6 7 8\",\"layout order\":\"bits x y z\"},\"filtered\":false,\"datasetName\":\"A1A2Gradient 1.ics\",\"table\":{\"history extents\":\"1.000000e-08 128 128\",\"ics_version\":\"2.0\",\"representation compression\":\"gzip\",\"layout sizes\":\"64 256 128 128\",\"layout significant_bits\":\"64\",\"history author\":\"pbarber\",\"representation format\":\"real\",\"representation sign\":\"signed\",\"parameter units\":\"relative ns microns microns\",\"history type\":\"fluorescencelifetime\",\"parameter scale\":\"1.000000 0.039000 1.000000 1.000000\",\"history created on\":\"23:14:09 18-11-2008\",\"parameter labels\":\"intensity x-position y-position z-position\",\"history labels\":\"t x y\",\"representation byte_order\":\"1 2 3 4 5 6 7 8\",\"layout order\":\"bits x y z\"},\"priority\":0.0}";
		testImg(baseFolder().child("Gray-FLIM-datasets.zip").child("A1A2Gradient 1.ics"),
			"2729daf548e65378d93b24da8ee6a583819a34f0", meta, new int[] { 128, 128, 256, },
			Axes.X, Axes.Y, SCIFIOAxes.LIFETIME);
	}
}
