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

package io.scif.util;

import java.awt.image.BufferedImage;

/**
 * A utility class for outputting a BufferedImage as ASCII text.
 *
 * @author Mark Hiner
 */
public class AsciiImage {

	// -- Constants --

	private static final String NL = System.getProperty("line.separator");

	private static final String CHARS = " .,-+o*O#";

	// -- Fields --

	private final BufferedImage img;

	// -- Constructor --

	public AsciiImage(final BufferedImage img) {
		this.img = img;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final int width = img.getWidth();
		final int height = img.getHeight();
		final StringBuilder sb = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int pix = img.getRGB(x, y);
				// Note: unused
//				final int a = 0xff & (pix >> 24);
				final int r = 0xff & (pix >> 16);
				final int g = 0xff & (pix >> 8);
				final int b = 0xff & pix;
				final int avg = (r + g + b) / 3;
				sb.append(getChar(avg));
			}
			sb.append(NL);
		}
		return sb.toString();
	}

	// -- Helper methods --

	private char getChar(final int value) {
		final int index = CHARS.length() * value / 256;
		return CHARS.charAt(index);
	}
}
