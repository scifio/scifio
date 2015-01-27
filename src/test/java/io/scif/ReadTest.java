
package io.scif;

import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.services.DatasetIOService;

import java.io.IOException;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.event.DefaultEventService;

public class ReadTest {

	public static void main(String... args) throws IOException {
		Context ctx = new Context();
		DatasetIOService io = ctx.getService(DatasetIOService.class);
		SCIFIOConfig cfg = new SCIFIOConfig().imgOpenerSetComputeMinMax(false).imgOpenerSetImgModes(ImgMode.PLANAR);
		while (true) {
			io.open("C:\\cygwin64\\home\\Mark\\data\\imagej\\img_0_Cy5_1.tif", cfg);
//			io.open("C:\\cygwin64\\home\\Mark\\data\\imagej\\tubhiswt4D.ome.tif", cfg);
		}
//			ctx.dispose();
	}

	@Test
	public void timeTest() throws IOException {
		Context ctx = new Context();
		DatasetIOService io = ctx.getService(DatasetIOService.class);
		SCIFIOConfig cfg = new SCIFIOConfig().imgOpenerSetComputeMinMax(false).imgOpenerSetImgModes(ImgMode.PLANAR);
		for (int i = 0; i < 5000; i++) {
			io.open("C:\\cygwin64\\home\\Mark\\data\\imagej\\img_0_Cy5_1.tif", cfg);
//			io.open("C:\\cygwin64\\home\\Mark\\data\\imagej\\tubhiswt4D.ome.tif", cfg);
		}

		ctx.dispose();
	}
}
