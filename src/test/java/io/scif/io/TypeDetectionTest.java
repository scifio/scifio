/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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

package io.scif.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests compressed IRandomAccess implementation type detection.
 *
 * @see IRandomAccess
 */
public class TypeDetectionTest {

	private final Context context = new Context();

	@Test
	public void testBZip2TypeDetection() throws IOException {
		final File invalidFile = File.createTempFile("invalid", ".bz2");
		invalidFile.deleteOnExit();
		final BZip2Handle handle = new BZip2Handle(context);
		assertEquals(handle.isConstructable(invalidFile.getAbsolutePath()), false);
		handle.close();
	}

	@Test
	public void testGZipTypeDetection() throws IOException {
		final File invalidFile = File.createTempFile("invalid", ".gz");
		invalidFile.deleteOnExit();
		final GZipHandle handle = new GZipHandle(context);
		assertEquals(handle.isConstructable(invalidFile.getAbsolutePath()), false);
		handle.close();
	}

	@Test
	public void testZipTypeDetection() throws IOException {
		final File invalidFile = File.createTempFile("invalid", ".zip");
		invalidFile.deleteOnExit();
		final ZipHandle handle = new ZipHandle(context);
		assertEquals(handle.isConstructable(invalidFile.getAbsolutePath()), false);
		handle.close();
	}
}
