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

package io.scif.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.scif.FormatException;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import net.imagej.axis.Axes;

import org.junit.After;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.Location;
import org.scijava.plugin.PluginInfo;

/**
 * Tests for the {@link Filter} classes.
 *
 * @author Mark Hiner
 */
public class FilterTest {

	private final SCIFIO scifio = makeSCIFIO();

	private final Location id = new TestImgLocation.Builder().name("testImg")
		.lengths(512, 512).build();

	private Reader readerFilter;

	@After
	public void tearDown() throws IOException {
		readerFilter.close();
	}

	private SCIFIO makeSCIFIO() {
		final SCIFIO scifio = new SCIFIO();

		final Context ctx = scifio.getContext();

		final PluginInfo<Filter> enabledInfo = new PluginInfo<>(EnabledFilter.class,
			Filter.class);
		final PluginInfo<Filter> disabledInfo = new PluginInfo<>(
			DisabledFilter.class, Filter.class);

		ctx.getPluginIndex().add(enabledInfo);
		ctx.getPluginIndex().add(disabledInfo);

		return scifio;
	}

	/**
	 * Verifies that opening a file with a filter enabled, then reusing the reader
	 * with another file works as intended (and does not re-opene the original
	 * file).
	 */
	@Test
	public void testSetSource() throws FormatException, IOException {
		final Location id2 = new TestImgLocation.Builder().name("testImg").lengths(
			256, 128,5).axes("X", "Y", "Time").build();

		readerFilter = scifio.initializer().initializeReader(id, new SCIFIOConfig()
			.checkerSetOpen(true));

		((ReaderFilter) readerFilter).enable(PlaneSeparator.class);

		long x = readerFilter.getMetadata().get(0).getAxisLength(Axes.X);
		long y = readerFilter.getMetadata().get(0).getAxisLength(Axes.Y);
		assertEquals(512, x);
		assertEquals(512, y);

		readerFilter.setSource(id2);

		x = readerFilter.getMetadata().get(0).getAxisLength(Axes.X);
		y = readerFilter.getMetadata().get(0).getAxisLength(Axes.Y);
		assertEquals(256, x);
		assertEquals(128, y);
	}

	@Test
	public void testDefaultEnabled() throws FormatException, IOException {
		readerFilter = scifio.initializer().initializeReader(id);

		boolean enabledProperly = false;
		boolean disabledProperly = true;

		// Make sure that our enabled plugin is found, and the disabled one
		// isn't
		while (Filter.class.isAssignableFrom(readerFilter.getClass())) {
			if (EnabledFilter.class.isAssignableFrom(readerFilter.getClass()))
				enabledProperly = true;

			if (DisabledFilter.class.isAssignableFrom(readerFilter.getClass()))
				disabledProperly = false;

			readerFilter = (Reader) ((Filter) readerFilter).getParent();
		}

		assertTrue(enabledProperly);
		assertTrue(disabledProperly);

	}

	@Test
	public void testFilterOrder() throws FormatException, IOException {
		readerFilter = scifio.initializer().initializeReader(id);

		// enable all plugins
		for (final PluginInfo<Filter> pi : scifio.getContext().getPluginIndex()
			.getPlugins(Filter.class))
		{
			((ReaderFilter) readerFilter).enable(pi.getPluginClass());
		}

		double lastPriority = Double.MAX_VALUE;

		// Skip the actual ReaderFilter class
		if (Filter.class.isAssignableFrom(readerFilter.getClass())) readerFilter =
			(Reader) ((Filter) readerFilter).getParent();

		while (Filter.class.isAssignableFrom(readerFilter.getClass())) {
			final double priority = ((Filter) readerFilter).getPriority();

			// Verify all plugins are in proper order
			assertTrue(priority <= lastPriority);
			lastPriority = priority;

			readerFilter = (Reader) ((Filter) readerFilter).getParent();
		}

	}

	// Sample plugin class known to be enabled by default
	public static class EnabledFilter extends AbstractReaderFilter {

		@Override
		public boolean enabledDefault() {
			return true;
		}
	}

	// Sample plugin class known to be disabled by default
	public static class DisabledFilter extends AbstractReaderFilter {

		@Override
		public boolean enabledDefault() {
			return false;
		}
	}
}
