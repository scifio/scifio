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

import java.io.File;

import javax.swing.JFileChooser;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;

/**
 * A simple manual test of the {@link IO#open(String)} convenience method.
 *
 * @author Curtis Rueden
 */
public class ReadImg {

	public static void main(final String[] args) throws Exception {
		final JFileChooser opener = new JFileChooser(System.getProperty(
			"user.home"));
		final int result = opener.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			readImg(opener.getSelectedFile());
		}
		System.exit(0);
	}

	private static void readImg(final File file) throws Exception {
		final ImgPlus<?> img = IO.openImgs(file.getAbsolutePath()).get(0);

		System.out.println("file = " + file);
		System.out.println("Dimensions:");
		for (int d = 0; d < img.numDimensions(); d++) {
			final CalibratedAxis axisType = img.axis(d);
			final long axisLength = img.dimension(d);
			System.out.println("\t" + axisLength + " : " + axisType);
		}
	}

}
