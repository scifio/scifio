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
package io.scif.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.imagej.Dataset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;

public class StringToDatasetConverterTest {
	private Context c;
	private String nonexistentPath;

	@Before
	public void setUp() throws IOException {
		c = new Context();
		nonexistentPath = Files.createTempFile("non-existent", ".file").toString();
	}

	@After
	public void tearDown() {
		c.dispose();
		c = null;
		new File(nonexistentPath).delete();
	}

	@Test
	public void testFileToDatasetConverter() {
		final ConvertService convertService = c.service(ConvertService.class);
		String imagePath = "image&pixelType=uint8&axes=X,Y,Z&lengths=256,128,32.fake";

		Converter<?, ?> handler = convertService.getHandler(imagePath, Dataset.class);
		Converter<?, ?> nonExistentFileHandler = convertService.getHandler(nonexistentPath, Dataset.class);
		// Make sure we got the right converter back
		assertSame(StringToDatasetConverter.class, handler.getClass());
		assertNull(nonExistentFileHandler);

		// Test handler capabilities
		assertTrue(handler.canConvert(imagePath, Dataset.class));
		assertFalse(handler.canConvert((Object) null, Dataset.class));
		assertFalse(handler.canConvert(nonexistentPath, Dataset.class));

		// Make sure we can convert with ConvertService
		assertTrue(convertService.supports(imagePath, Dataset.class));
		assertFalse(convertService.supports(nonexistentPath, Dataset.class));

		// Convert and check dimensions
		Dataset dataset = convertService.convert(imagePath, Dataset.class);
		assertEquals(256, dataset.dimension(0));
		assertEquals(128, dataset.dimension(1));
		assertEquals(32, dataset.dimension(2));
	}
}
