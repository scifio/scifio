
package io.scif.io;

import static org.junit.Assert.assertEquals;

import io.scif.MetadataService;
import io.scif.io.location.TestImgLocationResolver;

import java.net.URISyntaxException;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.LocationService;

/**
 * test for {@link TestImgLocationResolver}
 * 
 * @author Gabriel Einsdorf
 */
public class TestImgResolverTest {

	private Context ctx = new Context(LocationService.class,
		MetadataService.class);
	private LocationService loc = ctx.getService(LocationService.class);

	@Test
	public void testResolveString() throws URISyntaxException {
		final String ex1 = "scifioTestImg://testimg?axes=X,Y";
		final String ex1Full =
			"scifioTestImg://testImage?axes=X,Y&lengths=512,512&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		final String ex2 = "scifioTestImg://testimg?lengths=300,200";
		final String ex2Full =
			"scifioTestImg://testImage?axes=X,Y&lengths=300,200&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		final String ex3 = "scifioTestImg://testimg?axes=X,Y,Z&lengths=300,200,3";
		String ex3Full =
			"scifioTestImg://testImage?axes=X,Y,Z&lengths=300,200,3&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		String[] uriStrings = { ex1, ex2, ex3 };
		String[] fullUriStrings = { ex1Full, ex2Full, ex3Full };

		for (int i = 0; i < uriStrings.length; i++) {
			assertEquals(fullUriStrings[i], loc.resolve(uriStrings[i]).getURI()
				.toString());
		}
	}

}
