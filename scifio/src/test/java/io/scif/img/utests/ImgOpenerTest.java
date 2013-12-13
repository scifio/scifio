/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.img.utests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgOptions;
import io.scif.img.SubRegion;
import io.scif.img.cell.SCIFIOCellImgFactory;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
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
	private final String id =
		"testImg&lengths=512,512,5&axes=X,Y,Time.fake";

	/**
	 * Verify that SCIFIO Metadata calibration values are preserved in an opened
	 * ImgPlus.
	 */
	@Test
	public void testImgCalibration() throws ImgIOException {
		String calId =
			"testImg&lengths=512,512,3,5&axes=X,Y,Z,Time&scales=5.0,6.0,7.0,8.0.fake";
		
		@SuppressWarnings("rawtypes")
		ImgPlus imgPlus = imgOpener.openImg(calId);
		
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
	public  void testGenerics() throws IncompatibleTypeException, ImgIOException {
		doTestGenerics(new UnsignedByteType());
		doTestGenerics(new FloatType());
		doTestGenerics(new DoubleType());
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

	// Tests the opening various sub-regions of an image
	@SuppressWarnings({ "rawtypes" })
	private void testSubRegion(final ImgFactory factory) throws ImgIOException {
		final ImgOptions options = new ImgOptions();
		// should get an inner left left 128x128 square
		AxisType[] axes = new AxisType[] { Axes.X, Axes.Y };
		String[] ranges = new String[] { "128-255", "128-255" };
		options.setRegion(new SubRegion(axes, ranges));
		doTestSubRegion(factory, options, 128 * 128 * 5);

		axes = new AxisType[] { Axes.TIME };
		ranges = new String[] { "0,2-4:2" };
		// should get the first, 3rd and 5th T slices
		options.setRegion(new SubRegion(axes, ranges));
		doTestSubRegion(factory, options, 512 * 512 * 3);

		// should get the whole image
		options.setRegion(null);
		doTestSubRegion(factory, options, 512 * 512 * 5);
	}

	@SuppressWarnings("rawtypes")
	private void doTestSubRegion(final ImgFactory factory,
		final ImgOptions options, final long size) throws ImgIOException
	{
		ImgPlus imgPlus = null;
		imgPlus = imgOpener.openImg(id, factory, options);
		assertNotNull(imgPlus);
		assertEquals(size, imgPlus.size());
	}

	private <T extends RealType<T> & NativeType<T>> void doTestGenerics(final T type)
		throws IncompatibleTypeException, ImgIOException
	{
		ImgPlus<T> imgPlus = null;

		final ImgFactory<T> factory = new ArrayImgFactory<T>().imgFactory(type);

		// Try each rawtype openImg method
		imgPlus = imgOpener.openImg(id, type);
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImg(id, type, new ImgOptions());
		assertNotNull(imgPlus);
		imgPlus = null;
		imgPlus = imgOpener.openImg(id, factory, type);
		assertNotNull(imgPlus);
		imgPlus = null;
	}
}
