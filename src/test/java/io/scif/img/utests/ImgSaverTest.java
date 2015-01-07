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
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;
import io.scif.io.ByteArrayHandle;
import io.scif.services.LocationService;
import net.imagej.ImgPlus;
import net.imglib2.exception.IncompatibleTypeException;

import org.scijava.Context;
import org.testng.annotations.Test;

/**
 * Tests for the {@link ImgSaver} class.
 * 
 * @author Mark Hiner
 */
public class ImgSaverTest {

	private final String id = "testImg&lengths=512,512,5&axes=X,Y,Time.fake";
	private final String out = "/Users/mhiner/loci/scifio/test.tif";
	private final Context ctx = new Context();
	private final LocationService locationService = ctx
		.getService(LocationService.class);

	/**
	 * Write an image to memory using the {@link ImgSaver} and verify that the
	 * given {@link ImgPlus} is not corrupted during the process.
	 */
	@Test
	public void testImgPlusIntegrity() throws ImgIOException, IncompatibleTypeException {
		final ImgOpener o = new ImgOpener(ctx);
		final ImgSaver s = new ImgSaver(ctx);
		final SCIFIOConfig config =
			new SCIFIOConfig().imgOpenerSetImgModes(ImgMode.PLANAR);
		final ByteArrayHandle bah = new ByteArrayHandle();
		locationService.mapFile(out, bah);

		final SCIFIOImgPlus<?> openImg = o.openImgs(id, config).get(0);
		final String source = new String(openImg.getSource());
		s.saveImg(out, openImg);
		assertEquals(source, openImg.getSource());
	}
}
