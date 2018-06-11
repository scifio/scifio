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

package io.scif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.scif.filters.MetadataWrapper;
import io.scif.filters.PlaneSeparator;
import io.scif.filters.ReaderFilter;
import io.scif.formats.ICSFormat;
import io.scif.formats.TestImgFormat;
import io.scif.io.location.TestImgLocation;
import io.scif.services.TranslatorService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.imagej.axis.Axes;

import org.junit.Before;
import org.junit.Test;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

/**
 * Unit tests for {@link io.scif.Translator} interface methods.
 *
 * @author Mark Hiner
 */
public class TranslatorTest {

	private final SCIFIO scifio = new SCIFIO();
	private Location in;
	private FileLocation out;

	@Before
	public void setup() throws IOException {
		in = TestImgLocation.builder().name("interleaved").pixelType("int8").axes(
			"Channel", "X", "Y", "Z").lengths(3, 256, 256, 5).build();

		final String outid = "testFile.ics";
		final File testDir = Files.createTempDirectory("scifio-tests").toFile();
		out = new FileLocation(new File(testDir, outid));
	}

	/**
	 * Basic translation test. Ensures that we can always translate naively
	 * between two classes.
	 */
	@Test
	public void testDirectTranslation() throws IOException, FormatException {
		final Metadata source = scifio.initializer().parseMetadata(in);
		final Metadata dest = scifio.format().getFormat(out).createMetadata();

		assertTrue(scifio.translator().translate(source, dest, false));
	}

	/**
	 * Verifies that the appropriate translator is discovered even when
	 * translating from a metadata instance wrapped by {@link MetadataWrapper}.
	 */
	@Test
	public void testWrappedTranslation() throws IOException, FormatException {
		final ReaderFilter rf = scifio.initializer().initializeReader(in);

		// enable a reader filter to trigger metadata wrapping
		rf.enable(PlaneSeparator.class).separate(Axes.CHANNEL);

		final Metadata source = rf.getMetadata();
		final Metadata dest = scifio.format().getFormat(out).createMetadata();

		// Verify that the ICSTranslator is discovered
		final Translator t = scifio.translator().findTranslator(source, dest,
			false);
		assertEquals(ICSFormat.ICSTranslator.class, t.getClass());

		// Translate
		t.translate(source, dest);

		// Test that the image metadata is still separated as intended
		// (we took interleaved data and separated out the channels via the
		// plane separator)
		assertEquals(Axes.X, dest.get(0).getAxis(0).type());
		assertEquals(Axes.Y, dest.get(0).getAxis(1).type());
		assertEquals(Axes.CHANNEL, dest.get(0).getAxis(2).type());
	}

	/**
	 * Tests the {@code exact = true} flag in
	 * {@link TranslatorService#findTranslator(Metadata, Metadata, boolean)} and
	 * similar methods when an appropriate translator does not exist.
	 */
	@Test()
	public void testNoTranslator() throws IOException, FormatException {
		final Metadata source = scifio.initializer().parseMetadata(in);
		final Metadata dest = scifio.format().getFormat(out).createMetadata();

		// This translation should fail, as there is no "Fake to ICS" translator
		assertFalse(scifio.translator().translate(source, dest, true));
	}

	/**
	 * Tests the {@code exact = true} flag in
	 * {@link TranslatorService#findTranslator(Metadata, Metadata, boolean)} and
	 * similar methods when an appropriate translator does exist.
	 */
	@Test
	public void testHasTranslator() {
		final Translator t = scifio.translator().findTranslator(
			io.scif.Metadata.class, TestImgFormat.Metadata.class, true);
		assertNotNull(t);
	}
}
