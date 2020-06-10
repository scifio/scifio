/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2020 SCIFIO developers.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.scif.FormatException;
import io.scif.SCIFIOService;
import io.scif.formats.StratecPQCTFormat;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.io.location.FileLocation;
import org.scijava.thread.ThreadService;

/**
 * Tests {@link FormatService}.
 *
 * @author Curtis Rueden
 */
public class FormatServiceTest {

	private static FormatService formatService;

	@BeforeClass
	public static void setUp() {
		final Context context = new Context(SCIFIOService.class, StatusService.class);
		formatService = context.getService(FormatService.class);
	}

	@AfterClass
	public static void tearDown() {
		formatService.getContext().dispose();
	}

	/** Tests {@link FormatService#getSuffixes()}. */
	@Test
	public void testGetSuffixes() {
		final String[] suffixes = formatService.getSuffixes();
		final String[] pQCTSuffixes = StratecPQCTFormat.generateSuffixes();
		final String[] formatSuffixes = { "avi", "bmp", "btf", "dcm", "dic",
			"dicom", "eps", "epsi", "fits", "fts", "gif", "ics", "ids", "ima", "img",
			"isq", "j2k", "j2ki", "j2kr", "jp2", "jpe", "jpeg", "jpf", "jpg", "mng",
			"mov", "msr", "nhdr", "nrrd", "obf", "pct", "pcx", "pgm", "pict", "png",
			"ps", "raw", "tf2", "tf8", "tif", "tiff", "txt", "xml", "scifiotestimg" };

		final Set<String> expectedSuffixes = new HashSet<>();
		Arrays.stream(formatSuffixes).forEach(expectedSuffixes::add);
		Arrays.stream(pQCTSuffixes).forEach(expectedSuffixes::add);

		for (final String suffix : suffixes) {
			assertTrue("Unexpected suffix: " + suffix, expectedSuffixes.contains(
				suffix));
			expectedSuffixes.remove(suffix);
		}

		assertTrue("Suffixes missing: " + expectedSuffixes.toString(),
			expectedSuffixes.isEmpty());
	}

	/**
	 * Test simultaneous format caching on multiple threads.
	 * <p>
	 * NB: not annotated as a unit test due to length of execution.
	 * </p>
	 */
	@Ignore
	@Test
	public void testMultiThreaded() throws InterruptedException {
		final ThreadService ts = formatService.getContext().service(
			ThreadService.class);
		final Random random = new Random();
		final long baseTime = System.currentTimeMillis();

		final int threads = 500;
		final int[] count = new int[1];

		final Runnable runnable = () -> {
			final long time = System.currentTimeMillis();

			while (System.currentTimeMillis() - time < 10000) {
				final String s = new BigInteger(64, random).toString() + ".tif";
				try {
					formatService.getFormat(new FileLocation(s));
				}
				catch (final FormatException exc) {
					return;
				}
			}

			synchronized (count) {
				count[0]++;
			}
		};

		for (int i = 0; i < threads; i++) {
			ts.run(runnable);
		}

		while (System.currentTimeMillis() - baseTime < 30000) {
			Thread.sleep(100);
		}

		assertEquals(threads, count[0]);
	}

}
