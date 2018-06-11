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

import io.scif.config.SCIFIOConfig;
import io.scif.formats.TestImgFormat;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.DummyLocation;
import org.scijava.io.location.Location;

/**
 * Unit tests for {@link io.scif.Checker} interface methods.
 *
 * @author Mark Hiner
 */
public class CheckerTest {

	private final Location id = new TestImgLocation.Builder().name("8bit-signed")
		.pixelType("int8").axes("X", "Y", "Z", "C", "T").lengths(50, 50, 4, 5, 7)
		.build();

	private final Location falseId = new DummyLocation();

	private Checker c;

	private FakeChecker fc;

	private Context context;

	private DataHandleService dataHandleService;

	@Before
	public void setUp() throws FormatException {
		context = new Context();
		final SCIFIO scifio = new SCIFIO();
		final Format f = scifio.format().getFormat(id);
		c = f.createChecker();
		fc = new FakeChecker();
		fc.setContext(context);
		dataHandleService = context.getService(DataHandleService.class);
	}

	@Test
	public void isFormatTests() throws IOException {
		boolean isFormat = false;

		isFormat = c.isFormat(id);
		assertTrue(isFormat);

		isFormat = c.isFormat(id, new SCIFIOConfig().checkerSetOpen(false));
		assertTrue(isFormat);

		isFormat = c.isFormat(id, new SCIFIOConfig().checkerSetOpen(true));
		assertTrue(isFormat);

		isFormat = c.isFormat(falseId, new SCIFIOConfig().checkerSetOpen(false));
		assertFalse(isFormat);
	}

	@Test
	public void checkHeaderTest() {
		boolean isFormat = false;

		isFormat = c.checkHeader(id.getName().getBytes());
		assertFalse(isFormat);
	}

	@Test
	public void suffixSufficientTests() throws IOException {

		// test with suffix sufficient
		fc.setSuffixSufficient(true);
		boolean isFormat = false;

		isFormat = fc.isFormat(id);
		assertTrue(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(false));
		assertTrue(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(true));
		assertTrue(isFormat);

		// test with suffix not sufficient
		// will return false because we can not open handles on TestImgLocation
		fc.setSuffixSufficient(false);

		isFormat = fc.isFormat(id);
		assertFalse(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(false));
		assertFalse(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(true));
		assertFalse(isFormat);

		isFormat = fc.checkHeader(id.getName().getBytes());
		assertTrue(isFormat);
	}

	@Test
	public void hasContextTests() {
		assertNotNull(c.getContext());
	}

	public void hasFormatTests() {
		final Format format = c.getFormat();

		assertNotNull(format);

		assertEquals(c.getFormat().getCheckerClass(), c.getClass());
	}

	@After
	public void tearDown() {
		context = null;
		c = null;
		fc = null;
	}

	/*
	 * Private inner class for testing suffix flags.
	 *
	 * @author Mark Hiner
	 */
	private static class FakeChecker extends io.scif.DefaultChecker {

		private boolean suffixSufficient = false;

		// -- FakeChecker Methods --

		public void setSuffixSufficient(final boolean s) {
			suffixSufficient = s;
		}

		@Override
		public boolean suffixSufficient() {
			return suffixSufficient;
		}

		@Override
		public boolean isFormat(final DataHandle<Location> stream)
			throws IOException
		{
			return true;
		}

		// -- HasFormat Methods --

		// When extending an existing component, the getFormat() method should
		// be overriden to ensure the proper format is returned.
		// FIXME: index over all components? make Format.createComponent work
		// more like services where
		// you can have a list of components returned... maybe? Or not..
		@Override
		public Format getFormat() {
			final SCIFIO scifio = new SCIFIO(getContext());
			return scifio.format().getFormatFromClass(TestImgFormat.class);
		}
	}
}
