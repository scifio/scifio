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
import io.scif.formats.FakeFormat;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.TestParameters;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.scijava.Context;

/**
 * Unit tests for {@link io.scif.Checker} interface methods.
 *
 * @author Mark Hiner
 */
@RunWith(Parameterized.class)
public class CheckerTest {

	private final String id =
		"8bit-signed&pixelType=int8&axes=X,Y,Z,C,T&lengths=50,50,3,5,7.fake";

	private final String falseId = "testFile.png";

	private Checker c;

	private FakeChecker fc;

	private Context context;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestParameters.parameters("checkerTests");
	}

	private final String provider;

	public CheckerTest(final String provider, final boolean checkGrowth,
		final boolean testLength)
	{
		this.provider = provider;
	}

	@Before
	public void setUp() throws FormatException {
		context = new Context();
		final SCIFIO scifio = new SCIFIO();
		final Format f = scifio.format().getFormat(id);
		c = f.createChecker();
		fc = new FakeChecker();
		fc.setContext(context);
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

		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			id);
		isFormat = c.isFormat(stream);
		assertFalse(isFormat);
		stream.close();

		isFormat = c.isFormat(falseId, new SCIFIOConfig().checkerSetOpen(false));
		assertFalse(isFormat);
	}

	@Test
	public void checkHeaderTest() {
		boolean isFormat = false;

		isFormat = c.checkHeader(id.getBytes());
		assertFalse(isFormat);
	}

	@Test
	public void suffixSufficientTests() throws IOException {
		fc.setSuffixSufficient(false);
		boolean isFormat = false;

		isFormat = fc.isFormat(id);
		assertTrue(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(false));
		assertFalse(isFormat);

		isFormat = fc.isFormat(id, new SCIFIOConfig().checkerSetOpen(true));
		assertTrue(isFormat);

		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			id);
		isFormat = fc.isFormat(stream);
		assertTrue(isFormat);
		stream.close();

		isFormat = fc.checkHeader(id.getBytes());
		assertTrue(isFormat);
	}

	@Test
	public void hasContextTests() {
		assertNotNull(c.getContext());
	}

	public void hasFormatTests() {
		final Format format = c.getFormat();

		assertNotNull(format);

		if (format != null) {
			assertEquals(c.getFormat().getCheckerClass(), c.getClass());
		}
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
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			return true;
		}

		// -- HasFormat Methods --

		// When extending an existing component, the getFormat() method should
		// be
		// overriden to ensure
		// the proper format is returned.
		// FIXME: index over all components? make Format.createComponent work
		// more
		// like services where
		// you can have a list of components returned... maybe? Or not..
		@Override
		public Format getFormat() {
			final SCIFIO scifio = new SCIFIO(getContext());
			return scifio.format().getFormatFromClass(FakeFormat.class);
		}
	}
}
