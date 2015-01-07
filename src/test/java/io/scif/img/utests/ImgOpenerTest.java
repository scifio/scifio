/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
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

package io.scif.img.utests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.FakeFormat;
import io.scif.img.ImageRegion;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.img.cell.SCIFIOCellImgFactory;

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

import org.testng.annotations.Test;

/**
 * Tests for the {@link ImgOpener} class.
 * 
 * @author Mark Hiner
 */
@Test
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

		assertEquals(5.0, imgPlus.averageScale(0));
		assertEquals(6.0, imgPlus.averageScale(1));
		assertEquals(7.0, imgPlus.averageScale(2));
		assertEquals(8.0, imgPlus.averageScale(3));
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testImgOptions() throws IncompatibleTypeException, ImgIOException
	{
		final NativeType t = new UnsignedByteType();

		final ImgFactory aif = new ArrayImgFactory().imgFactory(t);
		final ImgFactory pif = new PlanarImgFactory().imgFactory(t);
		final ImgFactory sif = new SCIFIOCellImgFactory().imgFactory(t);

		for (final ImgFactory f : new ImgFactory[] { aif, pif, sif }) {
			testSubRegion(f);
		}
	}

	/**
	 * Tests that opening datasets with multiple images, via
	 * {@link SCIFIOConfig#imgOpenerIsOpenAllImages()} is working as intended.
	 * 
	 * @throws ImgIOException
	 */
	@Test
	public void testOpenAllImages() throws ImgIOException {
		String id = "testImg&images=5&lengths=512,512&axes=X,Y.fake";

		// Open all images
		List<SCIFIOImgPlus<?>> imgs =
			new MultiImgOpener().openImgs(id, new SCIFIOConfig()
				.imgOpenerSetOpenAllImages(true));

		// Check the size
		assertEquals(5, imgs.size());

		// Check the adjusted dimensions
		SCIFIOImgPlus<?> img = imgs.get(0);
		for (int i = 1; i < imgs.size(); i++) {
			SCIFIOImgPlus<?> testImg = imgs.get(i);
			assertEquals(img.dimension(0), testImg.dimension(0) + 10);
			assertEquals(img.dimension(1), testImg.dimension(1) + 10);
			img = testImg;
		}
	}

	/**
	 * Tests that opening datasets with multiple images, via
	 * {@link SCIFIOConfig#imgOpenerGetRange()}, is working as intended.
	 * @throws ImgIOException 
	 */
	public void testOpenImageRange() throws ImgIOException {
		String id = "testImg&images=5&lengths=512,512&axes=X,Y.fake";

		// Open images 0 and 3
		List<SCIFIOImgPlus<?>> imgs =
			new MultiImgOpener().openImgs(id, new SCIFIOConfig()
				.imgOpenerSetRange("0,3"));

		// Check the size
		assertEquals(2, imgs.size());

		// Check the adjusted dimensions
		assertEquals(imgs.get(0).dimension(0), imgs.get(1).dimension(0) + 30);
		assertEquals(imgs.get(0).dimension(1), imgs.get(1).dimension(1) + 30);
	}

	// Tests the opening various sub-regions of an image
	@SuppressWarnings({ "rawtypes" })
	private void testSubRegion(final ImgFactory factory) throws ImgIOException {
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

	@SuppressWarnings("rawtypes")
	private void doTestSubRegion(final ImgFactory factory,
		final SCIFIOConfig options, final long size) throws ImgIOException
	{
		ImgPlus imgPlus = null;
		imgPlus = imgOpener.openImgs(id, factory, options).get(0);
		assertNotNull(imgPlus);
		assertEquals(size, imgPlus.size());
	}

	private <T extends RealType<T> & NativeType<T>> void doTestGenerics(
		final T type) throws IncompatibleTypeException, ImgIOException
	{
		ImgPlus<T> imgPlus = null;

		final ImgFactory<T> factory = new ArrayImgFactory<T>().imgFactory(type);

		// Try each rawtype openImg method
		imgPlus = imgOpener.openImgs(id, type).get(0);
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImgs(id, type, new SCIFIOConfig()).get(0);
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImgs(id, factory, type).get(0);
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
			final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config) throws ImgIOException
		{
			final Metadata m = reader.getMetadata();

			int offset = 0;
			for (int i = 0; i < m.getImageCount(); i++) {
				m.get(i).setAxisLength(Axes.X, m.get(i).getAxisLength(Axes.X) - offset);
				m.get(i).setAxisLength(Axes.Y, m.get(i).getAxisLength(Axes.Y) - offset);
				offset += 10;
			}

			return super.openImgs(reader, type, imgFactory, config);
		}
	}
}
