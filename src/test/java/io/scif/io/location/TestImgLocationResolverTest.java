/*-
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

package io.scif.io.location;

import static org.junit.Assert.assertEquals;

import io.scif.MetadataService;
import io.scif.io.location.TestImgLocationResolver;

import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.LocationService;

/**
 * Test for {@link TestImgLocationResolver}
 * 
 * @author Gabriel Einsdorf
 */
public class TestImgLocationResolverTest {

	private static Context ctx = new Context(LocationService.class,
		MetadataService.class);
	private LocationService loc = ctx.getService(LocationService.class);

	@AfterClass
	public static void dispose() {
		ctx.dispose();
	}

	@Test
	public void testResolveString() throws URISyntaxException {
		final String ex1 = "scifioTestImg://myCoolImage?axes=X,Y";
		final String ex1Full =
			"scifioTestImg://myCoolImage?axes=X,Y&lengths=512,512&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		final String ex2 = "scifioTestImg://myAwesomeImage?lengths=300,200";
		final String ex2Full =
			"scifioTestImg://myAwesomeImage?axes=X,Y&lengths=300,200&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		final String ex3 = "scifioTestImg://great?axes=X,Y,Z&lengths=300,200,3";
		String ex3Full =
			"scifioTestImg://great?axes=X,Y,Z&lengths=300,200,3&scales=1.0,1.0&units=um,um&planarDims=-1&interleavedDims=-1&thumbSizeX=0&thumbSizeY=0&pixelType=uint8&indexed=false&falseColor=false&little=true&metadataComplete=true&thumbnail=false&orderCertain=true&lutLength=3&scaleFactor=1&images=1";

		String[] uriStrings = { ex1, ex2, ex3 };
		String[] fullUriStrings = { ex1Full, ex2Full, ex3Full };

		for (int i = 0; i < uriStrings.length; i++) {
			assertEquals(fullUriStrings[i], loc.resolve(uriStrings[i]).getURI()
				.toString());
		}
	}

}
