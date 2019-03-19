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

package io.scif.gui;

import java.awt.color.ColorSpace;

/**
 * ColorSpace for 2-channel images.
 *
 * @author Melissa Linkert
 */
public class TwoChannelColorSpace extends ColorSpace {

	// -- Constants --

	public static final int CS_2C = -1;

	private static final int NUM_COMPONENTS = 2;

	// -- Constructor --

	protected TwoChannelColorSpace(final int type, final int components) {
		super(type, components);
	}

	// -- ColorSpace API methods --

	@Override
	public float[] fromCIEXYZ(final float[] color) {
		final ColorSpace rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		return rgb.fromCIEXYZ(toRGB(color));
	}

	@Override
	public float[] fromRGB(final float[] rgb) {
		return new float[] { rgb[0], rgb[1] };
	}

	public static ColorSpace getInstance(final int colorSpace) {
		if (colorSpace == CS_2C) {
			return new TwoChannelColorSpace(ColorSpace.TYPE_2CLR, NUM_COMPONENTS);
		}
		return ColorSpace.getInstance(colorSpace);
	}

	@Override
	public String getName(final int idx) {
		return idx == 0 ? "Red" : "Green";
	}

	@Override
	public int getNumComponents() {
		return NUM_COMPONENTS;
	}

	@Override
	public int getType() {
		return ColorSpace.TYPE_2CLR;
	}

	@Override
	public boolean isCS_sRGB() {
		return false;
	}

	@Override
	public float[] toCIEXYZ(final float[] color) {
		final ColorSpace rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		return rgb.toCIEXYZ(toRGB(color));
	}

	@Override
	public float[] toRGB(final float[] color) {
		return new float[] { color[0], color[1], 0 };
	}

}
