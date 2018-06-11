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

package io.scif.io;

import io.scif.img.IO;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

/**
 * A simple test for {@link ImgOpener}.
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class ReadImage {

	public static void main(final String[] args) throws ImgIOException {
		final ImgOpener imageOpener = new ImgOpener();

		final Location[] ids;
		if (args.length == 0) {
			final Location userHome = new FileLocation(System.getProperty(
				"user.home"));
			ids = new Location[] {
				// userHome + "/data/Spindle_Green_d3d.dv",
				new FileLocation(userHome + "/data/mitosis-test.ipw"),
				// userHome + "/data/test_greys.lif",
				new FileLocation(userHome +
					"/data/slice1_810nm_40x_z1_pcc100_scanin_20s_01.sdt") };
		}
		else ids = parseArgs(args);

		// read all arguments using auto-detected type with default container
		System.out.println("== AUTO-DETECTED TYPE, DEFAULT CONTAINER ==");
		for (final Location id : ids) {
			final ImgPlus<?> img = imageOpener.openImgs(id).get(0);
			reportInformation(img);
		}

		// read all arguments using auto-detected type with default container
		System.out.println("== AUTO-DETECTED TYPE, CELL CONTAINER ==");
		for (final Location id : ids) {
			final ImgPlus<?> img = imageOpener.openImgs(id).get(0);
			reportInformation(img);
		}

		// read all arguments using FloatType with ArrayContainer
//    System.out.println();
//    System.out.println("== FLOAT TYPE, ARRAY CONTAINER ==");
//    for (final String arg : args) {
//      final ImgPlus<FloatType> img = ImgOpener.openFloat(arg, 0);
//      reportInformation(img);
//    }

		// read all arguments using FloatType with PlanarImg
		System.out.println();
		System.out.println("== FLOAT TYPE, DEFAULT CONTAINER ==");
		for (final Location id : ids) {
			final ImgPlus<FloatType> img = IO.openFloat(id).get(0);
			reportInformation(img);
		}

		// read all arguments using FloatType with PlanarImg
		System.out.println();
		System.out.println("== DOUBLE TYPE, DEFAULT CONTAINER ==");
		for (final Location id : ids) {
			final ImgPlus<DoubleType> img = IO.openDouble(id).get(0);
			reportInformation(img);
		}
	}

	private static Location[] parseArgs(final String[] args) {
		final Location[] out = new Location[args.length];
		for (int i = 0; i < args.length; i++) {
			out[i] = new FileLocation(args[i]);
		}
		return out;
	}

	/** Prints out some useful information about the {@link Img}. */
	public static void reportInformation(final Img<?> img) {
		System.out.println(img);
		final Cursor<?> cursor = img.cursor();
		cursor.fwd();
		System.out.println("\tType = " + cursor.get().getClass().getName());
		System.out.println("\tImg = " + img.getClass().getName());
	}
}
