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

package io.scif.util;

import org.scijava.util.ArrayUtils;
import org.scijava.util.Bytes;

/**
 * A utility class with convenience methods for manipulating images in primitive
 * array form. To work with images in {@link java.awt.image.BufferedImage} form,
 * use the {@link io.scif.gui.AWTImageTools} class.
 *
 * @author Curtis Rueden
 */
public final class ImageTools {

	// -- Constructor --

	private ImageTools() {}

	// -- Image conversion --

	/**
	 * Convert an arbitrary primitive type array with 3 samples per pixel to a 3 x
	 * (width * height) byte array.
	 */
	public static byte[][] make24Bits(final Object pixels, final int w,
		final int h, final boolean interleaved, final boolean reverse)
	{
		return make24Bits(pixels, w, h, interleaved, reverse, null, null);
	}

	/**
	 * Convert an arbitrary primitive type array with 3 samples per pixel to a 3 x
	 * (width * height) byte array. Scaling is performed according to the
	 * specified minimum and maximum pixel values in the original image. If the
	 * minimum is null, it is assumed to be 0. If the maximum is null, it is
	 * assumed to be 2^nbits - 1.
	 */
	public static byte[][] make24Bits(final Object pixels, final int w,
		final int h, final boolean interleaved, final boolean reverse,
		final Double min, final Double max)
	{
		final int[] pix = make24Bits(pixels, w, h, interleaved, min, max);
		final byte[][] rtn = new byte[3][pix.length];
		for (int i = 0; i < pix.length; i++) {
			final byte r = (byte) ((pix[i] >> 16) & 0xff);
			rtn[1][i] = (byte) ((pix[i] >> 8) & 0xff);
			final byte b = (byte) (pix[i] & 0xff);
			rtn[0][i] = reverse ? b : r;
			rtn[2][i] = reverse ? r : b;
		}
		return rtn;
	}

	/**
	 * Convert an arbitrary primitive type array with 3 samples per pixel to an
	 * int array, i.e. RGB color with 8 bits per pixel.
	 */
	public static int[] make24Bits(final Object pixels, final int w, final int h,
		final boolean interleaved)
	{
		return make24Bits(pixels, w, h, interleaved, null, null);
	}

	/**
	 * Convert an arbitrary primitive type array with 3 samples per pixel to an
	 * int array, i.e. RGB color with 8 bits per pixel. Scaling is performed
	 * according to the specified minimum and maximum pixel values in the original
	 * image. If the minimum is null, it is assumed to be 0. If the maximum is
	 * null, it is assumed to be 2^nbits - 1.
	 */
	public static int[] make24Bits(final Object pixels, final int w, final int h,
		final boolean interleaved, Double min, Double max)
	{
		final int[] rtn = new int[w * h];

		byte[] b = null;

		if (min == null) min = new Double(0);
		final double newRange = 256d;

		// adapted from ImageJ's TypeConverter methods

		if (pixels instanceof byte[]) b = (byte[]) pixels;
		else if (pixels instanceof short[]) {
			if (max == null) max = new Double(0xffff);
			final double range = max.doubleValue() - min.doubleValue() + 1;
			final double mult = newRange / range;

			final short[] s = (short[]) pixels;
			b = new byte[s.length];
			for (int i = 0; i < s.length; i++) {
				b[i] = (byte) (Math.abs(s[i] * mult) - min.doubleValue());
			}
		}
		else if (pixels instanceof int[]) {
			if (max == null) max = new Double(0xffffffffL);
			final double range = max.doubleValue() - min.doubleValue() + 1;
			final double mult = newRange / range;

			final int[] s = (int[]) pixels;
			b = new byte[s.length];
			for (int i = 0; i < s.length; i++) {
				b[i] = (byte) (Math.abs(s[i] * mult) - min.doubleValue());
			}
		}
		else if (pixels instanceof float[]) {
			if (max == null) max = new Double(Float.MAX_VALUE);
			final double range = max.doubleValue() - min.doubleValue() + 1;
			final double mult = newRange / range;

			final float[] s = (float[]) pixels;
			b = new byte[s.length];
			for (int i = 0; i < s.length; i++) {
				b[i] = (byte) (s[i] * mult - min.doubleValue());
			}
		}
		else if (pixels instanceof double[]) {
			if (max == null) max = new Double(Double.MAX_VALUE);
			final double range = max.doubleValue() - min.doubleValue() + 1;
			final double mult = newRange / range;

			final double[] s = (double[]) pixels;
			b = new byte[s.length];
			for (int i = 0; i < s.length; i++) {
				b[i] = (byte) (s[i] * mult - min.doubleValue());
			}
		}

		final int c = b.length / rtn.length;

		for (int i = 0; i < rtn.length; i++) {
			final byte[] a = new byte[4];
			final int maxC = Math.min(c, a.length);
			for (int j = maxC - 1; j >= 0; j--) {
				a[j] = b[interleaved ? i * c + j : i + j * w * h];
			}
			if (c == 1) {
				for (int j = 1; j < a.length; j++) {
					a[j] = a[0];
				}
			}

			final byte tmp = a[0];
			a[0] = a[2];
			a[2] = tmp;
			rtn[i] = Bytes.toInt(a, true);
		}

		return rtn;
	}

	// -- Image manipulation --

	/**
	 * Splits the given multi-channel array into a 2D array. The "reverse"
	 * parameter is false if channels are in RGB order, true if channels are in
	 * BGR order.
	 */
	public static byte[] splitChannels(final byte[] array, final long[] pos,
		final long[] maxLengths, final int bytes, final boolean reverse,
		final boolean interleaved)
	{
		long splitCount = 1;
		for (final long l : maxLengths) {
			splitCount *= l;
		}
		return splitChannels(array, null, pos, maxLengths, bytes, reverse,
			interleaved, array.length / splitCount);
	}

	/**
	 * Splits the given multi-channel array into a 2D array. The "reverse"
	 * parameter is false if channels are in RGB order, true if channels are in
	 * BGR order. If the 'rtn' parameter is not null, the specified channel will
	 * be copied into 'rtn'. The 'channelLength' parameter specifies the number of
	 * bytes that are expected to be in a single channel. In many cases, this will
	 * match the value of 'rtn.length', but specifying it separately allows 'rtn'
	 * to be larger than the size of a single channel.
	 *
	 * @param pos - positional axes index of the plane to extract
	 * @param maxLengths - lengths of each split positional axis
	 * @param planeLength - number of bytes in a split out plane
	 */
	public static byte[] splitChannels(final byte[] array, byte[] rtn,
		final long[] pos, final long[] maxLengths, final int bytes,
		final boolean reverse, final boolean interleaved, final long planeLength)
	{
		if (planeLength == array.length) {
			if (rtn != null) {
				System.arraycopy(array, 0, rtn, 0, rtn.length);
			}
			return array;
		}

		if (rtn == null) {
			rtn = new byte[(int) (planeLength)];
		}

		if (reverse) {
			for (int i = 0; i < pos.length; i++) {
				pos[i] = maxLengths[i] - pos[i] - 1;
			}
		}

		final long index = FormatTools.positionToRaster(maxLengths, pos);

		if (!interleaved) {
			System.arraycopy(array, (int) (planeLength * index), rtn, 0,
				(int) planeLength);
		}
		else {
			int next = 0;
			// TODO may need to do more to sort out the actual axis order
			for (int i = 0; i < array.length; i += bytes * ArrayUtils.safeMultiply32(
				maxLengths))
			{
				for (int k = 0; k < bytes; k++) {
					if (next < rtn.length) rtn[next] = array[(int) (i + index * bytes +
						k)];
					next++;
				}
			}
		}
		return rtn;
	}

	/**
	 * Pads (or crops) the byte array to the given width and height. The image
	 * will be centered within the new bounds.
	 */
	public static byte[] padImage(final byte[] b, final boolean interleaved,
		final int c, final int oldWidth, final int width, final int height)
	{
		final int oldHeight = b.length / (oldWidth * c);
		final byte[] padded = new byte[height * width * c];

		final int wClip = (width - oldWidth) / 2;
		final int hClip = (height - oldHeight) / 2;

		final int h = height < oldHeight ? height : oldHeight;

		if (interleaved) {
			final int len = oldWidth < width ? oldWidth : width;
			if (h == oldHeight) {
				for (int y = 0; y < h * c; y++) {
					final int oldIndex = oldWidth * y;
					final int index = width * y;
					System.arraycopy(b, oldIndex, padded, index, len);
				}
			}
			else {
				for (int ch = 0; ch < c; ch++) {
					for (int y = 0; y < h; y++) {
						final int oldIndex = oldWidth * ch * oldHeight + oldWidth * y;
						final int index = width * ch * height + width * y;
						System.arraycopy(b, oldIndex, padded, index, len);
					}
				}
			}
		}
		else {
			final int len = oldWidth < width ? oldWidth * c : width * c;
			for (int oy = 0, y = 0; oy < oldHeight; oy++, y++) {
				final int oldIndex = oldWidth * c * y;
				final int index = width * c * (y + hClip) + c * wClip;
				System.arraycopy(b, oldIndex, padded, index, len);
			}
		}
		return padded;
	}

	/**
	 * Pads (or crops) the short array to the given width and height. The image
	 * will be centered within the new bounds.
	 */
	public static short[] padImage(final short[] b, final boolean interleaved,
		final int c, final int oldWidth, final int width, final int height)
	{
		final int oldHeight = b.length / (oldWidth * c);
		final short[] padded = new short[height * width * c];

		final int wClip = (width - oldWidth) / 2;
		final int hClip = (height - oldHeight) / 2;

		final int h = height < oldHeight ? height : oldHeight;

		if (interleaved) {
			final int len = oldWidth < width ? oldWidth : width;
			if (h == oldHeight) {
				for (int y = 0; y < h * c; y++) {
					final int oldIndex = oldWidth * y;
					final int index = width * y;
					System.arraycopy(b, oldIndex, padded, index, len);
				}
			}
			else {
				for (int ch = 0; ch < c; ch++) {
					for (int y = 0; y < h; y++) {
						final int oldIndex = oldWidth * ch * oldHeight + oldWidth * y;
						final int index = width * ch * height + width * y;
						System.arraycopy(b, oldIndex, padded, index, len);
					}
				}
			}
		}
		else {
			final int len = oldWidth < width ? oldWidth * c : width * c;
			for (int oy = 0, y = 0; oy < oldHeight; oy++, y++) {
				final int oldIndex = oldWidth * c * y;
				final int index = width * c * (y + hClip) + c * wClip;
				System.arraycopy(b, oldIndex, padded, index, len);
			}
		}
		return padded;
	}

	/**
	 * Pads (or crops) the int array to the given width and height. The image will
	 * be centered within the new bounds.
	 */
	public static int[] padImage(final int[] b, final boolean interleaved,
		final int c, final int oldWidth, final int width, final int height)
	{
		final int oldHeight = b.length / (oldWidth * c);
		final int[] padded = new int[height * width * c];

		final int wClip = (width - oldWidth) / 2;
		final int hClip = (height - oldHeight) / 2;

		final int h = height < oldHeight ? height : oldHeight;

		if (interleaved) {
			final int len = oldWidth < width ? oldWidth : width;
			if (h == oldHeight) {
				for (int y = 0; y < h * c; y++) {
					final int oldIndex = oldWidth * y;
					final int index = width * y;
					System.arraycopy(b, oldIndex, padded, index, len);
				}
			}
			else {
				for (int ch = 0; ch < c; ch++) {
					for (int y = 0; y < h; y++) {
						final int oldIndex = oldWidth * ch * oldHeight + oldWidth * y;
						final int index = width * ch * height + width * y;
						System.arraycopy(b, oldIndex, padded, index, len);
					}
				}
			}
		}
		else {
			final int len = oldWidth < width ? oldWidth * c : width * c;
			for (int oy = 0, y = 0; oy < oldHeight; oy++, y++) {
				final int oldIndex = oldWidth * c * y;
				final int index = width * c * (y + hClip) + c * wClip;
				System.arraycopy(b, oldIndex, padded, index, len);
			}
		}
		return padded;
	}

	/**
	 * Pads (or crops) the float array to the given width and height. The image
	 * will be centered within the new bounds.
	 */
	public static float[] padImage(final float[] b, final boolean interleaved,
		final int c, final int oldWidth, final int width, final int height)
	{
		final int oldHeight = b.length / (oldWidth * c);
		final float[] padded = new float[height * width * c];

		final int wClip = (width - oldWidth) / 2;
		final int hClip = (height - oldHeight) / 2;

		final int h = height < oldHeight ? height : oldHeight;

		if (interleaved) {
			final int len = oldWidth < width ? oldWidth : width;
			if (h == oldHeight) {
				for (int y = 0; y < h * c; y++) {
					final int oldIndex = oldWidth * y;
					final int index = width * y;
					System.arraycopy(b, oldIndex, padded, index, len);
				}
			}
			else {
				for (int ch = 0; ch < c; ch++) {
					for (int y = 0; y < h; y++) {
						final int oldIndex = oldWidth * ch * oldHeight + oldWidth * y;
						final int index = width * ch * height + width * y;
						System.arraycopy(b, oldIndex, padded, index, len);
					}
				}
			}
		}
		else {
			final int len = oldWidth < width ? oldWidth * c : width * c;
			for (int oy = 0, y = 0; oy < oldHeight; oy++, y++) {
				final int oldIndex = oldWidth * c * y;
				final int index = width * c * (y + hClip) + c * wClip;
				System.arraycopy(b, oldIndex, padded, index, len);
			}
		}
		return padded;
	}

	/**
	 * Pads (or crops) the double array to the given width and height. The image
	 * will be centered within the new bounds.
	 */
	public static double[] padImage(final double[] b, final boolean interleaved,
		final int c, final int oldWidth, final int width, final int height)
	{
		final int oldHeight = b.length / (oldWidth * c);
		final double[] padded = new double[height * width * c];

		final int wClip = (width - oldWidth) / 2;
		final int hClip = (height - oldHeight) / 2;

		final int h = height < oldHeight ? height : oldHeight;

		if (interleaved) {
			final int len = oldWidth < width ? oldWidth : width;
			if (h == oldHeight) {
				for (int y = 0; y < h * c; y++) {
					final int oldIndex = oldWidth * y;
					final int index = width * y;
					System.arraycopy(b, oldIndex, padded, index, len);
				}
			}
			else {
				for (int ch = 0; ch < c; ch++) {
					for (int y = 0; y < h; y++) {
						final int oldIndex = oldWidth * ch * oldHeight + oldWidth * y;
						final int index = width * ch * height + width * y;
						System.arraycopy(b, oldIndex, padded, index, len);
					}
				}
			}
		}
		else {
			final int len = oldWidth < width ? oldWidth * c : width * c;
			for (int oy = 0, y = 0; oy < oldHeight; oy++, y++) {
				final int oldIndex = oldWidth * c * y;
				final int index = width * c * (y + hClip) + c * wClip;
				System.arraycopy(b, oldIndex, padded, index, len);
			}
		}
		return padded;
	}

	/**
	 * Perform autoscaling on the given byte array; map min to 0 and max to 255.
	 * If the number of bytes per pixel is 1, then nothing happens.
	 */
	public static byte[] autoscale(final byte[] b, final int min, final int max,
		final int bpp, final boolean little)
	{
		if (bpp == 1) return b;

		final byte[] out = new byte[b.length / bpp];

		for (int i = 0; i < b.length; i += bpp) {
			int s = Bytes.toInt(b, i, bpp, little);

			if (s >= max) s = 255;
			else if (s <= min) s = 0;
			else {
				final int diff = max - min;
				final float dist = (s - min) / diff;

				s = (int) dist * 256;
			}

			out[i / bpp] = (byte) s;
		}
		return out;
	}

	/** Scan a plane for the channel min and max values. */
	public static Double[] scanData(final byte[] plane, final int bits,
		final boolean littleEndian)
	{
		int max = 0;
		int min = Integer.MAX_VALUE;

		if (bits <= 8) {
			for (final byte planeVal : plane) {
				if (planeVal < min) min = planeVal;
				if (planeVal > max) max = planeVal;
			}
		}
		else if (bits == 16) {
			for (int j = 0; j < plane.length; j += 2) {
				final short s = Bytes.toShort(plane, j, 2, littleEndian);
				if (s < min) min = s;
				if (s > max) max = s;
			}
		}
		else if (bits == 32) {
			for (int j = 0; j < plane.length; j += 4) {
				final int s = Bytes.toInt(plane, j, 4, littleEndian);
				if (s < min) min = s;
				if (s > max) max = s;
			}
		}

		final Double[] rtn = new Double[2];
		rtn[0] = new Double(min);
		rtn[1] = new Double(max);
		return rtn;
	}

	public static byte[] getSubimage(final byte[] src, final byte[] dest,
		final int originalWidth, final int originalHeight, final int x, final int y,
		final int w, final int h, final int bpp, final int channels,
		final boolean interleaved)
	{
		for (int yy = y; yy < y + h; yy++) {
			for (int xx = x; xx < x + w; xx++) {
				for (int cc = 0; cc < channels; cc++) {
					int oldNdx = -1, newNdx = -1;
					if (interleaved) {
						oldNdx = yy * originalWidth * bpp * channels + xx * bpp * channels +
							cc * bpp;
						newNdx = (yy - y) * w * bpp * channels + (xx - x) * bpp * channels +
							cc * bpp;
					}
					else {
						oldNdx = bpp * (cc * originalWidth * originalHeight + yy *
							originalWidth + xx);
						newNdx = bpp * (cc * w * h + (yy - y) * w + (xx - x));
					}
					System.arraycopy(src, oldNdx, dest, newNdx, bpp);
				}
			}
		}
		return dest;
	}

	// -- Indexed color conversion --

	/** Converts a LUT and an array of indices into an array of RGB tuples. */
	public static byte[][] indexedToRGB(final byte[][] lut, final byte[] b) {
		final byte[][] rtn = new byte[lut.length][b.length];

		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < lut.length; j++) {
				rtn[j][i] = lut[j][b[i] & 0xff];
			}
		}
		return rtn;
	}

	/** Converts a LUT and an array of indices into an array of RGB tuples. */
	public static short[][] indexedToRGB(final short[][] lut, final byte[] b,
		final boolean le)
	{
		final short[][] rtn = new short[lut.length][b.length / 2];
		for (int i = 0; i < b.length / 2; i++) {
			for (int j = 0; j < lut.length; j++) {
				final int index = Bytes.toInt(b, i * 2, 2, le);
				rtn[j][i] = lut[j][index];
			}
		}
		return rtn;
	}

	public static byte[] interpolate(final short[] s, final byte[] buf,
		final int[] bayerPattern, final int width, final int height,
		final boolean littleEndian)
	{
		if (width == 1 && height == 1) {
			for (int i = 0; i < buf.length; i++) {
				buf[i] = (byte) s[0];
			}
			return buf;
		}
		// use linear interpolation to fill in missing components

		final int plane = width * height;

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// boolean evenRow = (row % 2) == 0;
				final boolean evenCol = (col % 2) == 0;

				final int index = (row % 2) * 2 + (col % 2);
				final boolean needGreen = bayerPattern[index] != 1;
				final boolean needRed = bayerPattern[index] != 0;
				final boolean needBlue = bayerPattern[index] != 2;

				if (needGreen) {
					int sum = 0;
					int ncomps = 0;

					if (row > 0) {
						sum += s[plane + (row - 1) * width + col];
						ncomps++;
					}
					if (row < height - 1) {
						sum += s[plane + (row + 1) * width + col];
						ncomps++;
					}
					if (col > 0) {
						sum += s[plane + row * width + col - 1];
						ncomps++;
					}
					if (col < width - 1) {
						sum += s[plane + row * width + col + 1];
						ncomps++;
					}

					final short v = (short) (sum / ncomps);
					Bytes.unpack(v, buf, row * width * 6 + col * 6 + 2, 2, littleEndian);
				}
				else {
					Bytes.unpack(s[plane + row * width + col], buf, row * width * 6 +
						col * 6 + 2, 2, littleEndian);
				}

				if (needRed) {
					int sum = 0;
					int ncomps = 0;
					if (!needBlue) {
						// four corners
						if (row > 0) {
							if (col > 0) {
								sum += s[(row - 1) * width + col - 1];
								ncomps++;
							}
							if (col < width - 1) {
								sum += s[(row - 1) * width + col + 1];
								ncomps++;
							}
						}
						if (row < height - 1) {
							if (col > 0) {
								sum += s[(row + 1) * width + col - 1];
								ncomps++;
							}
							if (col < width - 1) {
								sum += s[(row + 1) * width + col + 1];
								ncomps++;
							}
						}
					}
					else if ((evenCol && bayerPattern[index + 1] == 0) || (!evenCol &&
						bayerPattern[index - 1] == 0))
					{
						// horizontal
						if (col > 0) {
							sum += s[row * width + col - 1];
							ncomps++;
						}
						if (col < width - 1) {
							sum += s[row * width + col + 1];
							ncomps++;
						}
					}
					else {
						// vertical
						if (row > 0) {
							sum += s[(row - 1) * width + col];
							ncomps++;
						}
						if (row < height - 1) {
							sum += s[(row + 1) * width + col];
							ncomps++;
						}
					}

					final short v = (short) (sum / ncomps);
					Bytes.unpack(v, buf, row * width * 6 + col * 6, 2, littleEndian);
				}
				else {
					Bytes.unpack(s[row * width + col], buf, row * width * 6 + col * 6, 2,
						littleEndian);
				}

				if (needBlue) {
					int sum = 0;
					int ncomps = 0;
					if (!needRed) {
						// four corners
						if (row > 0) {
							if (col > 0) {
								sum += s[(2 * height + row - 1) * width + col - 1];
								ncomps++;
							}
							if (col < width - 1) {
								sum += s[(2 * height + row - 1) * width + col + 1];
								ncomps++;
							}
						}
						if (row < height - 1) {
							if (col > 0) {
								sum += s[(2 * height + row + 1) * width + col - 1];
								ncomps++;
							}
							if (col < width - 1) {
								sum += s[(2 * height + row + 1) * width + col + 1];
								ncomps++;
							}
						}
					}
					else if ((evenCol && bayerPattern[index + 1] == 2) || (!evenCol &&
						bayerPattern[index - 1] == 2))
					{
						// horizontal
						if (col > 0) {
							sum += s[(2 * height + row) * width + col - 1];
							ncomps++;
						}
						if (col < width - 1) {
							sum += s[(2 * height + row) * width + col + 1];
							ncomps++;
						}
					}
					else {
						// vertical
						if (row > 0) {
							sum += s[(2 * height + row - 1) * width + col];
							ncomps++;
						}
						if (row < height - 1) {
							sum += s[(2 * height + row + 1) * width + col];
							ncomps++;
						}
					}

					final short v = (short) (sum / ncomps);
					Bytes.unpack(v, buf, row * width * 6 + col * 6 + 4, 2, littleEndian);
				}
				else {
					Bytes.unpack(s[2 * plane + row * width + col], buf, row * width * 6 +
						col * 6 + 4, 2, littleEndian);
				}
			}
		}

		return buf;
	}

	/** Reorganizes the provided BGR buffer to be in RGB order. */
	public static void bgrToRgb(final byte[] buf, final boolean interleaved,
		final int bpp, final int c)
	{
		if (c < 3) return;
		if (interleaved) {
			for (int i = 0; i < buf.length; i += bpp * c) {
				for (int b = 0; b < bpp; b++) {
					final byte tmp = buf[i + b];
					buf[i + b] = buf[i + bpp * 2];
					buf[i + bpp * 2] = tmp;
				}
			}
		}
		else {
			final byte[] channel = new byte[buf.length / (bpp * c)];
			System.arraycopy(buf, 0, channel, 0, channel.length);
			System.arraycopy(buf, channel.length * 2, buf, 0, channel.length);
			System.arraycopy(channel, 0, buf, channel.length * 2, channel.length);
		}
	}

}
