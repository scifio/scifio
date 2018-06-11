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

import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * ColorModel that handles 8, 16 and 32 bits per channel signed data.
 */
public class SignedColorModel extends ColorModel {

	// -- Fields --

	private final int pixelBits;

	private final int nChannels;

	private final ComponentColorModel helper;

	private final int max;

	// -- Constructors --

	public SignedColorModel(final int pixelBits, final int dataType,
		final int nChannels)
	{
		super(pixelBits, makeBitArray(nChannels, pixelBits), AWTImageTools
			.makeColorSpace(nChannels), nChannels == 4, false,
			Transparency.TRANSLUCENT, dataType);

		int type = dataType;
		if (type == DataBuffer.TYPE_SHORT) {
			type = DataBuffer.TYPE_USHORT;
		}

		helper = new ComponentColorModel(AWTImageTools.makeColorSpace(nChannels),
			nChannels == 4, false, Transparency.TRANSLUCENT, type);

		this.pixelBits = pixelBits;
		this.nChannels = nChannels;

		max = (int) Math.pow(2, pixelBits) - 1;
	}

	// -- ColorModel API methods --

	@Override
	public synchronized Object getDataElements(final int rgb,
		final Object pixel)
	{
		return helper.getDataElements(rgb, pixel);
	}

	@Override
	public boolean isCompatibleRaster(final Raster raster) {
		if (pixelBits == 16) {
			return raster.getTransferType() == DataBuffer.TYPE_SHORT;
		}
		return helper.isCompatibleRaster(raster);
	}

	@Override
	public WritableRaster createCompatibleWritableRaster(final int w,
		final int h)
	{
		if (pixelBits == 16) {
			final int[] bandOffsets = new int[nChannels];
			for (int i = 0; i < nChannels; i++)
				bandOffsets[i] = i;

			final SampleModel m = new ComponentSampleModel(DataBuffer.TYPE_SHORT, w,
				h, nChannels, w * nChannels, bandOffsets);
			final DataBuffer db = new DataBufferShort(w * h, nChannels);
			return Raster.createWritableRaster(m, db, null);
		}
		return helper.createCompatibleWritableRaster(w, h);
	}

	@Override
	public int getAlpha(final int pixel) {
		if (nChannels < 4) return 255;
		return rescale(pixel, max);
	}

	@Override
	public int getBlue(final int pixel) {
		if (nChannels == 1) return getRed(pixel);
		return rescale(pixel, max);
	}

	@Override
	public int getGreen(final int pixel) {
		if (nChannels == 1) return getRed(pixel);
		return rescale(pixel, max);
	}

	@Override
	public int getRed(final int pixel) {
		return rescale(pixel, max);
	}

	@Override
	public int getAlpha(final Object data) {
		if (data instanceof byte[]) {
			final byte[] b = (byte[]) data;
			if (b.length == 1) return getAlpha(b[0]);
			return rescale(b.length == 4 ? b[0] : max, max);
		}
		else if (data instanceof short[]) {
			final short[] s = (short[]) data;
			if (s.length == 1) return getAlpha(s[0]);
			return rescale(s.length == 4 ? s[0] : max, max);
		}
		else if (data instanceof int[]) {
			final int[] i = (int[]) data;
			if (i.length == 1) return getAlpha(i[0]);
			return rescale(i.length == 4 ? i[0] : max, max);
		}
		return 0;
	}

	@Override
	public int getRed(final Object data) {
		if (data instanceof byte[]) {
			final byte[] b = (byte[]) data;
			if (b.length == 1) return getRed(b[0]);
			return rescale(b.length != 4 ? b[0] : b[1]);
		}
		else if (data instanceof short[]) {
			final short[] s = (short[]) data;
			if (s.length == 1) return getRed(s[0]);
			return rescale(s.length != 4 ? s[0] : s[1], max);
		}
		else if (data instanceof int[]) {
			final int[] i = (int[]) data;
			if (i.length == 1) return getRed(i[0]);
			return rescale(i.length != 4 ? i[0] : i[1], max);
		}
		return 0;
	}

	@Override
	public int getGreen(final Object data) {
		if (data instanceof byte[]) {
			final byte[] b = (byte[]) data;
			if (b.length == 1) return getGreen(b[0]);
			return rescale(b.length != 4 ? b[1] : b[2]);
		}
		else if (data instanceof short[]) {
			final short[] s = (short[]) data;
			if (s.length == 1) return getGreen(s[0]);
			return rescale(s.length != 4 ? s[1] : s[2], max);
		}
		else if (data instanceof int[]) {
			final int[] i = (int[]) data;
			if (i.length == 1) return getGreen(i[0]);
			return rescale(i.length != 4 ? i[1] : i[2], max);
		}
		return 0;
	}

	@Override
	public int getBlue(final Object data) {
		if (data instanceof byte[]) {
			final byte[] b = (byte[]) data;
			if (b.length == 1) return getBlue(b[0]);
			return rescale(b.length > 2 ? b[b.length - 1] : 0);
		}
		else if (data instanceof short[]) {
			final short[] s = (short[]) data;
			if (s.length == 1) return getBlue(s[0]);
			return rescale(s.length > 2 ? s[s.length - 1] : 0, max);
		}
		else if (data instanceof int[]) {
			final int[] i = (int[]) data;
			if (i.length == 1) return getBlue(i[0]);
			return rescale(i.length > 2 ? i[i.length - 1] : 0, max);
		}
		return 0;
	}

	// -- Helper methods --

	private int rescale(final int value, final int max) {
		float v = (float) value / (float) max;
		v *= 255;
		return rescale((int) v);
	}

	private int rescale(int value) {
		if (value < 128) {
			value += 128; // [0, 127] -> [128, 255]
		}
		else {
			value -= 128; // [128, 255] -> [0, 127]
		}
		return value;
	}

	private static int[] makeBitArray(final int nChannels, final int nBits) {
		final int[] bits = new int[nChannels];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = nBits;
		}
		return bits;
	}

}
