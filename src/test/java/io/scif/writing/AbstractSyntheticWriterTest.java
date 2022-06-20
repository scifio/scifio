/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2022 SCIFIO developers.
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

package io.scif.writing;

import static org.junit.Assert.assertEquals;

import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.io.location.TestImgLocation;
import io.scif.util.ImageHash;

import java.io.IOException;
import java.nio.file.Files;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.Context;
import org.scijava.io.location.FileLocation;

/**
 * Abstract superclass for synthetic data writer tests.
 * 
 * @author Gabriel Einsdorf, KNIME GmbH
 */
public abstract class AbstractSyntheticWriterTest {

	private final String suffix;

	public AbstractSyntheticWriterTest(final String suffix) {
		this.suffix = suffix;
	}

	public static FileLocation createTempFileLocation(final String suffix)
		throws IOException
	{
		return new FileLocation(Files.createTempFile("", suffix).toUri());
	}

	public void testWriting(final ImgPlus<?> sourceImg) throws IOException {
		testWriting(sourceImg, new SCIFIOConfig());
	}

	public void testWriting(final ImgPlus<?> sourceImg, final SCIFIOConfig config)
		throws IOException
	{
		final FileLocation out = createTempFileLocation(suffix);
		final Context ctx = new Context();

		new ImgSaver(ctx).saveImg(out, sourceImg, config);
		final ImgPlus<?> written = new ImgOpener(ctx).openImgs(out).get(0);
		ctx.dispose();
		assertEquals(ImageHash.hashImg(written), ImageHash.hashImg(sourceImg));
	}

	public void testWritingApprox(final ImgPlus<?> sourceImg, final double delta)
		throws IOException
	{
		testWritingApprox(sourceImg, new SCIFIOConfig(), delta);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testWritingApprox(final ImgPlus<?> sourceImg,
		final SCIFIOConfig config, final double delta) throws IOException
	{
		final FileLocation out = createTempFileLocation(suffix);
		final Context ctx = new Context();

		new ImgSaver(ctx).saveImg(out, sourceImg, config);
		final ImgPlus<?> written = new ImgOpener(ctx).openImgs(out).get(0);

		ctx.dispose();

		final Cursor<RealType> inC = (Cursor<RealType>) sourceImg.cursor();
		final RandomAccess<RealType> readRA = (RandomAccess<RealType>) written
			.randomAccess();

		double source;
		double read;
		double totalDelta = 0;
		final int[] pos = Intervals.dimensionsAsIntArray(sourceImg);

		final NormalizerUtil n = new NormalizerUtil(inC.get().getMaxValue(), inC
			.get().getMinValue());

		while (inC.hasNext()) {
			source = n.normalize(inC.next().getRealDouble());
			inC.localize(pos);
			readRA.setPosition(pos);
			read = n.normalize(readRA.get().getRealDouble());
			totalDelta += Math.abs(source - read);
		}
		assertEquals(delta, totalDelta, 0.000_0001);
	}

	/**
	 * @see #testOverwritingBehavior(SCIFIOConfig)
	 */
	public FileLocation testOverwritingBehavior()
		throws IOException
	{
		return testOverwritingBehavior(new SCIFIOConfig());
	}

	/**
	 * @param configuration Configuration to use for saving
	 * @return The {@link FileLocation} saeved to.
	 */
	public FileLocation testOverwritingBehavior(SCIFIOConfig configuration)
		throws IOException
	{
		Context ctx = new Context();
		try {
			final FileLocation saveLocation = createTempFileLocation("test" + suffix);
			ImgSaver saver = new ImgSaver(ctx);
			ImgOpener opener = new ImgOpener(ctx);
			ImgPlus<?> sourceImg = opener.openImgs(new TestImgLocation.Builder()
				.name("testimg").pixelType("uint8").axes("X", "Y").lengths(100, 100)
				.build()).get(0);

			saver.saveImg(saveLocation, sourceImg, configuration);
			saver.saveImg(saveLocation, sourceImg, configuration);
			return saveLocation;
		}
		finally {
			ctx.dispose();
		}
	}

	private class NormalizerUtil {

		private final double min;
		private final double max;

		public NormalizerUtil(final double min, final double max) {
			this.min = min;
			this.max = max;
		}

		public double normalize(final double x) {
			return ((x - min) / (max - min));
		}
	}

}
