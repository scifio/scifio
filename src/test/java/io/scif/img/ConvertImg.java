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

import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;

import java.io.File;

import javax.swing.JFileChooser;

import net.imagej.ImgPlus;

import org.scijava.Context;

/**
 * A simple manual test opening and saving {@link ImgPlus}es.
 *
 * @author Mark Hiner
 */
public class ConvertImg {

	public static void main(final String[] args) throws Exception {
		final JFileChooser opener = new JFileChooser(System.getProperty(
			"user.home"));
		final int result = opener.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			convertImg(opener.getSelectedFile());
		}
		System.exit(0);
	}

	private static void convertImg(final File file) throws Exception {
		final Context c = new Context();
		final SCIFIOConfig config = new SCIFIOConfig().imgOpenerSetImgModes(
			ImgMode.ARRAY);
		final ImgPlus<?> img = new ImgOpener(c).openImgs(file.getAbsolutePath(),
			config).get(0);

		final String outPath = file.getParent() + File.separator + "out_" + img
			.getName();
		new ImgSaver(c).saveImg(outPath, img);

		c.dispose();
	}

}
