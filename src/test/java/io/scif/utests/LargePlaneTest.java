package io.scif.utests;

import java.io.IOException;

import io.scif.FormatException;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;


public class LargePlaneTest {

	public static void main(String... args) throws FormatException, IOException {
		final SCIFIO scifio = new SCIFIO();
		final String sampleImage =
				"8bit-unsigned&pixelType=uint8&planarDims=2&lengths=200000,2000001&axes=X,Y.fake";

			final Reader reader = scifio.initializer().initializeReader(sampleImage);
			Plane openPlane = reader.openPlane(0, 0);

			System.out.println(openPlane.getBytes());

			System.out.println("done");
	}
}
