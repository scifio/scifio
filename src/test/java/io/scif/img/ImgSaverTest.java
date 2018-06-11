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

package io.scif.img;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.io.ByteArrayHandle;
import io.scif.services.LocationService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.scijava.Context;

/**
 * Tests for the {@link ImgSaver} class.
 *
 * @author Mark Hiner
 */
@RunWith(Parameterized.class)
public class ImgSaverTest {

	private final String id;
	private final String out;

	private final Context ctx = new Context();
	private final LocationService locationService = ctx.getService(
		LocationService.class);

	public ImgSaverTest(final String format, final String lengths,
		final String axes)
	{
		id = "testImg&lengths=" + lengths + "&axes=" + axes + ".fake";
		out = "test." + format;
	}

	@Parameters(name = "Format: {0}; Lengths: {1}; Channels: {2}")
	public static Collection<Object[]> getParams() {
		final List<Object[]> params = new ArrayList<>();
		params.add(new Object[] { ".tiff", "100,100,3,2", "X,Y,Z,Time" });
		params.add(new Object[] { ".tiff", "100,100,3,2", "X,Y,Channel,Z" });
		params.add(new Object[] { ".tiff", "100,100,3,2", "X,Y,Channel,Time" });
		params.add(new Object[] { ".tiff", "100,100,3,3,2", "X,Y,Channel,Z,Time" });
		params.add(new Object[] { ".tiff", "100,100,3", "X,Y,Channel" });
		return params;
	}

	@After
	public void cleanup() {
		final File f = new File(out);
		if (f.exists()) f.delete();
	}

	/**
	 * Write an image to memory using the {@link ImgSaver} and verify that the
	 * given {@link ImgPlus} is not corrupted during the process.
	 */
	@Test
	public void testImgPlusIntegrity() throws ImgIOException,
		IncompatibleTypeException
	{
		final ImgOpener o = new ImgOpener(ctx);
		final ImgSaver s = new ImgSaver(ctx);
		final SCIFIOConfig config = new SCIFIOConfig().imgOpenerSetImgModes(
			ImgMode.PLANAR);
		final ByteArrayHandle bah = new ByteArrayHandle();
		locationService.mapFile(out, bah);

		final SCIFIOImgPlus<?> openImg = o.openImgs(id, config).get(0);
		final String source = openImg.getSource();
		s.saveImg(out, openImg);
		assertEquals(source, openImg.getSource());
	}

	/**
	 * Test that ImgSaver writes each plane of a multi-plane PlanarImg correctly
	 */
	@Test
	public void testPlanarPlaneSaving() throws ImgIOException,
		IncompatibleTypeException
	{
		final SCIFIOConfig config = new SCIFIOConfig().imgOpenerSetImgModes(
			ImgMode.PLANAR);

		testPlaneSavingForConfig(config);
	}

	/**
	 * Test that ImgSaver writes each plane of a multi-plane CellImg correctly
	 */
	@Test
	public void testCellPlaneSaving() throws ImgIOException,
		IncompatibleTypeException
	{
		final SCIFIOConfig config = new SCIFIOConfig().imgOpenerSetImgModes(
			ImgMode.CELL);

		testPlaneSavingForConfig(config);
	}

	/**
	 * Test that ImgSaver writes each plane of a multi-plane ArrayImg correctly
	 */
	@Test
	public void testArrayPlaneSaving() throws ImgIOException,
		IncompatibleTypeException
	{
		final SCIFIOConfig config = new SCIFIOConfig().imgOpenerSetImgModes(
			ImgMode.ARRAY);

		testPlaneSavingForConfig(config);
	}

	// -- Helper methods --

	private void testPlaneSavingForConfig(final SCIFIOConfig config)
		throws ImgIOException, IncompatibleTypeException
	{
		final ImgOpener o = new ImgOpener(ctx);
		final ImgSaver s = new ImgSaver(ctx);

		// write the image
		final SCIFIOImgPlus<UnsignedByteType> before = o.openImgs(id,
			new UnsignedByteType(), config).get(0);
		s.saveImg(out, before);

		// re-read the written image and check for consistency
		final SCIFIOImgPlus<UnsignedByteType> after = o.openImgs(out,
			new UnsignedByteType()).get(0);
		assertImagesEqual(before, after);
	}

	// TODO: Migrate this into a shared ImageJ Common test library Assert class.
	private static <T> void assertImagesEqual(final ImgPlus<T> expected,
		final ImgPlus<T> actual)
	{
		// check dimensional lengths
		final long[] eDims = new long[expected.numDimensions()];
		expected.dimensions(eDims);
		final long[] aDims = new long[actual.numDimensions()];
		actual.dimensions(aDims);
		assertArrayEquals(eDims, aDims);

		// check dimensional axes
		final CalibratedAxis[] eAxes = new CalibratedAxis[expected.numDimensions()];
		expected.axes(eAxes);
		final CalibratedAxis[] aAxes = new CalibratedAxis[actual.numDimensions()];
		actual.axes(aAxes);
		assertArrayEquals(eAxes, aAxes);

		// check pixels
		assertIterationsEqual(expected, actual);
	}

	// TODO: This was stolen from ImageJ Ops AbstractOpTest.
	// It should really live in a shared test library of ImgLib2 (probably).
	private static <T> void assertIterationsEqual(final Iterable<T> expected,
		final Iterable<T> actual)
	{
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue("Fewer elements than expected", a.hasNext());
			assertEquals(e.next(), a.next());
		}
		assertFalse("More elements than expected", a.hasNext());
	}
}
