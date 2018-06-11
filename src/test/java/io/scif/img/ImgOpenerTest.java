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

import static io.scif.JUnitHelper.assertCloseEnough;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.FakeFormat;
import io.scif.img.cell.SCIFIOCellImgFactory;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;
import java.util.List;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests for the {@link ImgOpener} class.
 *
 * @author Mark Hiner
 */
public class ImgOpenerTest {

	// Use the default constructor, which constructs a minimal context,
	// to ensure all necessary services are present
	private final ImgOpener imgOpener = new ImgOpener();

	private final String id = "testImg&lengths=512,512,5&axes=X,Y,Time.fake";

	/**
	 * Verify that SCIFIO Metadata calibration values are preserved in an opened
	 * ImgPlus.
	 */
	@Test
	public void testImgCalibration() throws ImgIOException {
		final String calId =
			"testImg&lengths=512,512,3,5&axes=X,Y,Z,Time&scales=5.0,6.0,7.0,8.0.fake";

		@SuppressWarnings("rawtypes")
		final ImgPlus imgPlus = imgOpener.openImgs(calId).get(0);

		assertCloseEnough(5.0, imgPlus.averageScale(0));
		assertCloseEnough(6.0, imgPlus.averageScale(1));
		assertCloseEnough(7.0, imgPlus.averageScale(2));
		assertCloseEnough(8.0, imgPlus.averageScale(3));
	}

	/**
	 * Check that having raw typed ImgOpener methods doesn't cause problems. This
	 * test should just fail to compile if there's an issue.
	 */
	@Test
	public void testGenerics() throws IncompatibleTypeException, ImgIOException {
		doTestGenerics(new UnsignedByteType());
		doTestGenerics(new FloatType());
		doTestGenerics(new DoubleType());
	}

	@Test
	public void testCalibrationUnits() throws ImgIOException {
		final String calId =
			"testImg&lengths=512,512,3&axes=X,Y,Z&scales=5.0,6.0,7.0&units=micron,um,inches.fake";

		final ImgPlus<?> imgPlus = imgOpener.openImgs(calId).get(0);

		assertEquals("micron", imgPlus.axis(0).unit());
		assertEquals("um", imgPlus.axis(1).unit());
		assertEquals("inches", imgPlus.axis(2).unit());
	}

	/**
	 * Test opening images with various ImgOptions set.
	 */
	@Test
	public void testImgOptions() throws IncompatibleTypeException,
		ImgIOException
	{
		final UnsignedByteType t = new UnsignedByteType();
		testSubRegion(new ArrayImgFactory<>(t));
		testSubRegion(new PlanarImgFactory<>(t));
		testSubRegion(new SCIFIOCellImgFactory<>(t));
	}

	/**
	 * Tests that opening datasets with multiple images, via
	 * {@link SCIFIOConfig#imgOpenerIsOpenAllImages()} is working as intended.
	 *
	 * @throws ImgIOException
	 */
	@Test
	public void testOpenAllImages() throws ImgIOException {
		final String id = "testImg&images=5&lengths=512,512&axes=X,Y.fake";

		// Open all images
		final List<SCIFIOImgPlus<?>> imgs = new MultiImgOpener().openImgs(id,
			new SCIFIOConfig().imgOpenerSetOpenAllImages(true));

		// Check the size
		assertEquals(5, imgs.size());

		// Check the adjusted dimensions
		SCIFIOImgPlus<?> img = imgs.get(0);
		for (int i = 1; i < imgs.size(); i++) {
			final SCIFIOImgPlus<?> testImg = imgs.get(i);
			assertEquals(img.dimension(0), testImg.dimension(0) + 10);
			assertEquals(img.dimension(1), testImg.dimension(1) + 10);
			img = testImg;
		}
	}

	/**
	 * Tests that opening datasets with multiple images, via
	 * {@link SCIFIOConfig#imgOpenerGetRange()}, is working as intended.
	 *
	 * @throws ImgIOException
	 */
	@Test
	public void testOpenImageRange() throws ImgIOException {
		final String id = "testImg&images=5&lengths=512,512&axes=X,Y.fake";

		// Open images 0 and 3
		final List<SCIFIOImgPlus<?>> imgs = new MultiImgOpener().openImgs(id,
			new SCIFIOConfig().imgOpenerSetRange("0,3"));

		// Check the size
		assertEquals(2, imgs.size());

		// Check the adjusted dimensions
		assertEquals(imgs.get(0).dimension(0), imgs.get(1).dimension(0) + 30);
		assertEquals(imgs.get(0).dimension(1), imgs.get(1).dimension(1) + 30);
	}

	/**
	 * Tests using the {@link ImgOpener} when opening a non-String source.
	 */
	@Test
	public void testOpenNonString() throws ImgIOException, IOException,
		FormatException
	{
		final Context c = imgOpener.getContext();
		final SCIFIO scifio = new SCIFIO(c);

		// Make a GIF byte array
		// - From http://giflib.sourceforge.net/whatsinagif/bits_and_bytes.html
		final byte[] bytes = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x0A,
			0x00, 0x0A, 0x00, (byte) 0x91, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x00, 0x00,
			0x00, 0x21, (byte) 0xF9, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2C, 0x00,
			0x00, 0x00, 0x00, 0x0A, 0x00, 0x0A, 0x00, 0x00, 0x02, 0x16, (byte) 0x8C,
			0x2D, (byte) 0x99, (byte) 0x87, 0x2A, 0x1C, (byte) 0xDC, 0x33,
			(byte) 0xA0, 0x02, 0x75, (byte) 0xEC, (byte) 0x95, (byte) 0xFA,
			(byte) 0xA8, (byte) 0xDE, 0x60, (byte) 0x8C, 0x04, (byte) 0x91, 0x4C,
			0x01, 0x00, 0x3B };

		final RandomAccessInputStream stream = new RandomAccessInputStream(c,
			bytes);

		// Get the appropriate format
		final Format format = scifio.format().getFormat(stream);

		// Create and initialize a reader
		final Reader r = format.createReader();
		r.setSource(stream);

		// Open an ImgPlus from the reader
		final ImgPlus<?> img = imgOpener.openImgs(r).get(0);

		assertNotNull(img);
	}

	// Tests the opening various sub-regions of an image
	private <T extends RealType<T> & NativeType<T>> void testSubRegion(
		final ImgFactory<T> factory) throws ImgIOException
	{
		final SCIFIOConfig config = new SCIFIOConfig();
		// should get an inner left left 128x128 square
		AxisType[] axes = new AxisType[] { Axes.X, Axes.Y };
		String[] ranges = new String[] { "128-255", "128-255" };
		config.imgOpenerSetRegion(new ImageRegion(axes, ranges));
		doTestSubRegion(factory, config, 128 * 128 * 5);

		axes = new AxisType[] { Axes.TIME };
		ranges = new String[] { "0,2-4:2" };
		// should get the first, 3rd and 5th T slices
		config.imgOpenerSetRegion(new ImageRegion(axes, ranges));
		doTestSubRegion(factory, config, 512 * 512 * 3);

		// should get the whole image
		config.imgOpenerSetRegion(null);
		doTestSubRegion(factory, config, 512 * 512 * 5);
	}

	private <T extends RealType<T> & NativeType<T>> void doTestSubRegion(
		final ImgFactory<T> factory, final SCIFIOConfig options, final long size)
		throws ImgIOException
	{
		final ImgPlus<T> imgPlus = imgOpener.openImgs(id, factory, options).get(0);
		assertNotNull(imgPlus);
		assertEquals(size, imgPlus.size());
	}

	private <T extends RealType<T> & NativeType<T>> void doTestGenerics(
		final T type) throws IncompatibleTypeException, ImgIOException
	{
		ImgPlus<T> imgPlus = null;

		final ImgFactory<T> factory = new ArrayImgFactory<>(type);

		// Try each rawtype openImg method
		imgPlus = imgOpener.openImgs(id, type).get(0);
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImgs(id, type, new SCIFIOConfig()).get(0);
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImgs(id, factory).get(0);
		assertNotNull(imgPlus);
		imgPlus = null;
	}

	// Helper classes for testing

	/**
	 * Helper {@link ImgOpener} extension to modify {@link ImageMetadata} in a
	 * multi-series dataset, to verify that specified images are opened when
	 * requested.
	 */
	private class MultiImgOpener extends ImgOpener {

		/**
		 * When using, for example, {@link FakeFormat} for multi-image datasets, all
		 * the images are created with the same dimensions. This method allows us to
		 * add skew to the images, which can then be used to verify that the
		 * requested images are opened via {@link ImgOpener}.
		 */
		@Override
		public <T extends RealType<T>> List<SCIFIOImgPlus<T>> openImgs(
			final Reader reader, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config) throws ImgIOException
		{
			final Metadata m = reader.getMetadata();

			int offset = 0;
			for (int i = 0; i < m.getImageCount(); i++) {
				m.get(i).setAxisLength(Axes.X, m.get(i).getAxisLength(Axes.X) - offset);
				m.get(i).setAxisLength(Axes.Y, m.get(i).getAxisLength(Axes.Y) - offset);
				offset += 10;
			}

			return super.openImgs(reader, imgFactory, config);
		}
	}
}
