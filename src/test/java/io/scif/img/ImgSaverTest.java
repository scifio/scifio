/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
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

package io.scif.img;

import static org.junit.Assert.assertEquals;

import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.io.ByteArrayHandle;
import io.scif.services.LocationService;

import java.io.File;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.After;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests for the {@link ImgSaver} class.
 *
 * @author Mark Hiner
 */
public class ImgSaverTest {

	private final String id = "testImg&lengths=512,512,5&axes=X,Y,Time.fake";

	private final String out = "test.tif";

	private final Context ctx = new Context();

	private final LocationService locationService = ctx.getService(
		LocationService.class);

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
	public void testSlowPlaneSaving() throws ImgIOException,
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

	private void testPlaneSavingForConfig(final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		final ImgOpener o = new ImgOpener(ctx);
		final ImgSaver s = new ImgSaver(ctx);

		// write the image
		SCIFIOImgPlus<UnsignedByteType> openImg = o.openImgs(id,
			new UnsignedByteType(), config).get(0);
		s.saveImg(out, openImg);

		// re-read the written image and check dimensions
		openImg = o.<UnsignedByteType> openImgs(out, new UnsignedByteType()).get(0);

		// fakes start with 10 0's, then pixel value == plane index.. 
		// so we have to skip along the x-axis a bit
		int[] pos = { 11, 0, 0 };
		for (int i = 0; i < 5; i++) {
			pos[2] = i;
			RandomAccess<UnsignedByteType> randomAccess = openImg.randomAccess();
			randomAccess.setPosition(pos);
			assertEquals(i, randomAccess.get().getRealDouble(), 0.0001);
		}
	}
}
