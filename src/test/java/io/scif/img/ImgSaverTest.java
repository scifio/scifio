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
import io.scif.io.location.TestImgLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

/**
 * Tests for the {@link ImgSaver} class.
 *
 * @author Mark Hiner
 * @author Gabriel Einsdorf
 */
@RunWith(Parameterized.class)
public class ImgSaverTest {

	private final Location id;
	private Location out;
	private final Context ctx = new Context();
	private final String format;
	private final boolean checkUnits;

	public ImgSaverTest(final String format, final String lengths,
		final String axes, final boolean checkUnits) throws IOException
	{

		// parse lengths
		final String[] ls = lengths.split(",");
		final long[] lens = new long[ls.length];
		for (int i = 0; i < ls.length; i++) {
			lens[i] = Integer.parseInt(ls[i]);
		}

		id = new TestImgLocation.Builder().axes(axes.split(",")).lengths(lens)
			.build();

		this.format = format;
		this.checkUnits = checkUnits;
	}

	@Parameters(name = "Format: {0}, Size: {1}, Dims: {2}, testUnits: {3}")
	public static Collection<Object[]> getParams() {
		final ArrayList<Object[]> o = new ArrayList<>();

		// TIF //

// TIF not working
//		o.add(new Object[] { ".tif", "100,100", "X,Y", true });
//		o.add(new Object[] { ".tif", "100,100,3", "X,Y,Channel", true });
//		o.add(new Object[] { ".tif", "100,100,5,4,2", "X,Y,Z,C,Time", true });
//		o.add(new Object[] { ".tif", "100,100,5,4,2", "X,Y,Z,C,T", true });
//		o.add(new Object[] { ".tif", "100,100,5,4,2", "X,Y,C,T,Z", true });
//		o.add(new Object[] { ".tif", "100,100,5,4,2", "X,Y,T,C,Z", true });
//		o.add(new Object[] { ".tif", "100,100,4,2", "X,Y,Z,Time", true });

		// PNG //

// PNG not working
//		o.add(new Object[] { ".png", "100,100,3,2", "X,Y,Time, Channel", false });
//		o.add(new Object[] { ".png", "100,100,4", "X,Y,Channel", false });
//		o.add(new Object[] { ".png", "100,100,3,2", "X,Y,Channel, Time", false });
//		o.add(new Object[] { ".png", "100,100,3,2", "X,Y,Time,Channel", false });

// PNG working
		o.add(new Object[] { ".png", "100,100,3", "X,Y,Time", false });
		o.add(new Object[] { ".png", "100,100", "X,Y", false });

		// ICS //

// ICS Working
//		o.add(new Object[] { ".ics", "100,100,3,4,3", "X,Y,Z,Channel,Time",
//			false });
//		o.add(new Object[] { ".ics", "100,100,3,4,3", "X,Y,Channel,Time,Z",
//			false });
//		o.add(new Object[] { ".ics", "100,100,4,3", "X,Y,Channel,Time", false });
//		o.add(new Object[] { ".ics", "100,100,3", "X,Y,Time", false });
//		o.add(new Object[] { ".ics", "100,100,3", "X,Y,Z", false });
//		o.add(new Object[] { ".ics", "100,100,3", "X,Y,Channel", false });
//		o.add(new Object[] { ".ics", "100,100,3,2", "X,Time,Y,Channel", false });

		// AVI //

// AVI working
//		o.add(new Object[] { ".avi", "100,100,3,3", "X,Y,Channel,Time", false });

// AVI not working
// Any other combination

		// MOV //

// MOV working
//		o.add(new Object[] { ".mov", "100,100,3", "X,Y,Time", false});

// MOV not working
//		o.add(new Object[] { ".mov", "100,100,3,3", "X,Y,Channel,Time" , true});

		// EPS //

// EPS working
//		o.add(new Object[] { ".eps", "100,100", "X,Y", false });
//		o.add(new Object[] { ".eps", "100,100,3", "X,Y,Channel", false });

// EPS not working
//		o.add(new Object[] { ".eps", "100,100,3,3", "X,Y,Channel,Time", true });
//		o.add(new Object[] { ".eps", "100,100,3,3,3", "X,Y,Z,Channel,Time", true });
//		o.add(new Object[] { ".eps", "100,100,3", "X,Y,Z", true });

		// JPG //

// JPG not working
//		o.add(new Object[] { ".jpg", "100,100", "X,Y" , false});
//		o.add(new Object[] { ".jpg", "100,100,3", "X,Y,Channel", false });


		return o;
	}

	@Before
	public void setup() throws IOException {
		final File tmpFile = File.createTempFile("test", format);
		tmpFile.deleteOnExit();
		out = new FileLocation(tmpFile);
	}

	@After
	public void cleanup() {
		final File f = new File(out.getURI());
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
//		final ByteArrayHandle  = new ByteArrayHandle();
//		locationService.mapFile(out, bah);

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
		config.put("buffer", true);

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
		assertImagesEqual(before, after, checkUnits);
	}

	// TODO: Migrate this into a shared ImageJ Common test library Assert class.
	private static <T> void assertImagesEqual(final ImgPlus<T> expected,
		final ImgPlus<T> actual, final boolean checkUnits)
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
		assertAxesEquals(eAxes, aAxes, checkUnits);

		// check pixels
		assertIterationsEqual(expected, actual);
	}

	private static void assertAxesEquals(final CalibratedAxis[] eAxes,
		final CalibratedAxis[] aAxes, final boolean checkUnits)
	{
		for (int i = 0; i < eAxes.length; i++) {
			final CalibratedAxis e = eAxes[i];
			final CalibratedAxis a = aAxes[i];
			assertEquals("equation not equal", e.particularEquation(), a
				.particularEquation());
			assertEquals("type not equal", e.type(), a.type());
			if (checkUnits) assertEquals("units not equal", e.unit(), a.unit());
		}
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
