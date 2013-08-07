/*
 * #%L
 * SCIFIO Bio-Formats compatibility format.
 * %%
 * Copyright (C) 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package io.scif.img;

import java.io.File;

import javax.swing.JFileChooser;

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;

/**
 * A simple manual test of the {@link IO#open(String)} convenience method.
 * 
 * @author Curtis Rueden
 */
public class ReadImg {

	public static void main(final String[] args) throws Exception {
		final JFileChooser opener =
			new JFileChooser(System.getProperty("user.home"));
		final int result = opener.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			readImg(opener.getSelectedFile());
		}
		System.exit(0);
	}

	private static void readImg(final File file) throws ImgIOException {
		final ImgPlus<?> img = IO.open(file.getAbsolutePath());

		System.out.println("file = " + file);
		System.out.println("Dimensions:");
		for (int d=0; d<img.numDimensions(); d++) {
			final AxisType axisType = img.axis(d);
			final long axisLength = img.dimension(d);
			System.out.println("\t" + axisLength + " : " + axisType);
		}
	}

}
