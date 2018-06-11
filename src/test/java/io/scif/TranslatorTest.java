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
import io.scif.formats.FakeFormat;
import io.scif.formats.ICSFormat;
import io.scif.io.TestParameters;
import io.scif.services.TranslatorService;

import java.io.IOException;
import java.util.Collection;

import net.imagej.axis.Axes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit tests for {@link io.scif.Translator} interface methods.
 *
 * @author Mark Hiner
 */
@RunWith(Parameterized.class)
public class TranslatorTest {

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("translatorTests");
	}

	private final String provider;

	public TranslatorTest(final String provider, final boolean checkGrowth,
		final boolean testLength)
	{
		this.provider = provider;
	}

	private final String id =
		"interleaved&pixelType=int8&axes=Channel,X,Y,Z&lengths=3,256,256,5.fake";

	private final String output = "testFile.ics";

	private final SCIFIO scifio = new SCIFIO();

	/**
	 * Basic translation test. Ensures that we can always translate naively
	 * between two classes.
	 */
	@Test
	public void testDirectTranslation() throws IOException, FormatException {
		final Metadata source = scifio.initializer().parseMetadata(id);
		final Metadata dest = scifio.format().getFormat(output).createMetadata();

		assertTrue(scifio.translator().translate(source, dest, false));
	}

	/**
	 * Verifies that the appropriate translator is discovered even when
	 * translating from a metadata instance wrapped by {@link MetadataWrapper}.
	 */
	@Test
	public void testWrappedTranslation() throws IOException, FormatException {
		final ReaderFilter rf = scifio.initializer().initializeReader(id);

		// enable a reader filter to trigger metadata wrapping
		rf.enable(PlaneSeparator.class).separate(Axes.CHANNEL);

		final Metadata source = rf.getMetadata();
		final Metadata dest = scifio.format().getFormat(output).createMetadata();

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
		final Metadata source = scifio.initializer().parseMetadata(id);
		final Metadata dest = scifio.format().getFormat(output).createMetadata();

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
			io.scif.Metadata.class, FakeFormat.Metadata.class, true);
		assertNotNull(t);
	}
}
