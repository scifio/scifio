package io.scif;

import java.io.IOException;

import io.scif.services.DatasetIOService;

import org.junit.Test;
import org.scijava.Context;


public class ReadTest {
	
	
	@Test
	public void readerTest() throws IOException {
		Context ctx = new Context();
		DatasetIOService io = ctx.getService(DatasetIOService.class);
		while (true) {
			io.open("C:\\cygwin64\\home\\Mark\\data\\imagej\\img_0_Cy5_1.tif");
		}
	}

}
