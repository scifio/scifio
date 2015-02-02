/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif.services;

import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link FormatService}.
 *
 * @author Curtis Rueden
 */
public class FormatServiceTest {

	private FormatService formatService;

	@Before
	public void setUp() {
		final Context context = new Context(FormatService.class);
		formatService = context.getService(FormatService.class);
	}

	@After
	public void tearDown() {
		formatService.getContext().dispose();
	}

	/** Tests {@link FormatService#getSuffixes()}. */
	@Test
	public void testGetSuffixes() {
		final String[] suffixes = formatService.getSuffixes();
		final String[] expectedSuffixes =
			{ "avi", "bmp", "btf", "csv", "dcm", "dic", "dicom", "eps", "epsi",
				"fake", "fits", "fts", "gif", "ics", "ids", "ima", "j2k", "j2ki",
				"j2kr", "java", "jp2", "jpe", "jpeg", "jpf", "jpg", "mng", "mov",
				"msr", "nhdr", "nrrd", "obf", "pct", "pcx", "pgm", "pict", "png", "ps",
				"raw", "tf2", "tf8", "tif", "tiff", "txt", "xml", "zip" };
		assertArrayEquals(expectedSuffixes, suffixes);
	}

}
