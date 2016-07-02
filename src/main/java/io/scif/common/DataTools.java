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

package io.scif.common;

import io.scif.io.RandomAccessInputStream;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormatSymbols;

import org.scijava.Context;

/**
 * A utility class with convenience methods for reading, writing and decoding
 * words.
 *
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 * @deprecated Use these classes instead: {@link org.scijava.util.Bytes},
 *             {@link org.scijava.util.StringUtils},
 *             {@link org.scijava.util.ArrayUtils}
 */
@Deprecated
public final class DataTools {

	// -- Constants --

	// -- Static fields --

	// -- Constructor --

	private DataTools() {}

	// -- Data reading --

	/** Reads the contents of the given file into a string. */
	public static String readFile(final Context context, final String id)
		throws IOException
	{
		final RandomAccessInputStream in = new RandomAccessInputStream(context, id);
		final long idLen = in.length();
		if (idLen > Integer.MAX_VALUE) {
			in.close();
			throw new IOException("File too large");
		}
		final int len = (int) idLen;
		final String data = in.readString(len);
		in.close();
		return data;
	}

	// -- Word decoding - bytes to primitive types --

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a short. If there are fewer than len bytes available, the MSBs
	 * are all assumed to be zero (regardless of endianness).
	 */
	public static short bytesToShort(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		short total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i
					: len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 2 bytes of a byte array beyond the given offset
	 * to a short. If there are fewer than 2 bytes available the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static short bytesToShort(final byte[] bytes, final int off,
		final boolean little)
	{
		return bytesToShort(bytes, off, 2, little);
	}

	/**
	 * Translates up to the first 2 bytes of a byte array to a short. If there are
	 * fewer than 2 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static short bytesToShort(final byte[] bytes, final boolean little) {
		return bytesToShort(bytes, 0, 2, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array byond the given offset
	 * to a short. If there are fewer than len bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static short bytesToShort(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		short total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= bytes[ndx] << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 2 bytes of a byte array byond the given offset
	 * to a short. If there are fewer than 2 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static short bytesToShort(final short[] bytes, final int off,
		final boolean little)
	{
		return bytesToShort(bytes, off, 2, little);
	}

	/**
	 * Translates up to the first 2 bytes of a byte array to a short. If there are
	 * fewer than 2 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static short bytesToShort(final short[] bytes, final boolean little) {
		return bytesToShort(bytes, 0, 2, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to an int. If there are fewer than len bytes available, the MSBs are
	 * all assumed to be zero (regardless of endianness).
	 */
	public static int bytesToInt(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		int total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i
					: len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 4 bytes of a byte array beyond the given offset
	 * to an int. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static int bytesToInt(final byte[] bytes, final int off,
		final boolean little)
	{
		return bytesToInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a byte array to an int. If there are
	 * fewer than 4 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static int bytesToInt(final byte[] bytes, final boolean little) {
		return bytesToInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to an int. If there are fewer than len bytes available, the MSBs are
	 * all assumed to be zero (regardless of endianness).
	 */
	public static int bytesToInt(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		int total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= bytes[ndx] << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 4 bytes of a byte array beyond the given offset
	 * to an int. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static int bytesToInt(final short[] bytes, final int off,
		final boolean little)
	{
		return bytesToInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a byte array to an int. If there are
	 * fewer than 4 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static int bytesToInt(final short[] bytes, final boolean little) {
		return bytesToInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a float. If there are fewer than len bytes available, the MSBs
	 * are all assumed to be zero (regardless of endianness).
	 */
	public static float bytesToFloat(final byte[] bytes, final int off,
		final int len, final boolean little)
	{
		return Float.intBitsToFloat(bytesToInt(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 4 bytes of a byte array beyond a given offset to
	 * a float. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static float bytesToFloat(final byte[] bytes, final int off,
		final boolean little)
	{
		return bytesToFloat(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a byte array to a float. If there are
	 * fewer than 4 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static float bytesToFloat(final byte[] bytes, final boolean little) {
		return bytesToFloat(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond a given offset
	 * to a float. If there are fewer than len bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static float bytesToFloat(final short[] bytes, final int off,
		final int len, final boolean little)
	{
		return Float.intBitsToFloat(bytesToInt(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 4 bytes of a byte array beyond a given offset to
	 * a float. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static float bytesToFloat(final short[] bytes, final int off,
		final boolean little)
	{
		return bytesToInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a byte array to a float. If there are
	 * fewer than 4 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static float bytesToFloat(final short[] bytes, final boolean little) {
		return bytesToInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a long. If there are fewer than len bytes available, the MSBs are
	 * all assumed to be zero (regardless of endianness).
	 */
	public static long bytesToLong(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		long total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256L + bytes[ndx] : (long) bytes[ndx]) << ((little
					? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 8 bytes of a byte array beyond the given offset
	 * to a long. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static long bytesToLong(final byte[] bytes, final int off,
		final boolean little)
	{
		return bytesToLong(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a byte array to a long. If there are
	 * fewer than 8 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static long bytesToLong(final byte[] bytes, final boolean little) {
		return bytesToLong(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a long. If there are fewer than len bytes available, the MSBs are
	 * all assumed to be zero (regardless of endianness).
	 */
	public static long bytesToLong(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		long total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= ((long) bytes[ndx]) << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 8 bytes of a byte array beyond the given offset
	 * to a long. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static long bytesToLong(final short[] bytes, final int off,
		final boolean little)
	{
		return bytesToLong(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a byte array to a long. If there are
	 * fewer than 8 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static long bytesToLong(final short[] bytes, final boolean little) {
		return bytesToLong(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a double. If there are fewer than len bytes available, the MSBs
	 * are all assumed to be zero (regardless of endianness).
	 */
	public static double bytesToDouble(final byte[] bytes, final int off,
		final int len, final boolean little)
	{
		return Double.longBitsToDouble(bytesToLong(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 8 bytes of a byte array beyond the given offset
	 * to a double. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static double bytesToDouble(final byte[] bytes, final int off,
		final boolean little)
	{
		return bytesToDouble(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a byte array to a double. If there
	 * are fewer than 8 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static double bytesToDouble(final byte[] bytes, final boolean little) {
		return bytesToDouble(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first len bytes of a byte array beyond the given
	 * offset to a double. If there are fewer than len bytes available, the MSBs
	 * are all assumed to be zero (regardless of endianness).
	 */
	public static double bytesToDouble(final short[] bytes, final int off,
		final int len, final boolean little)
	{
		return Double.longBitsToDouble(bytesToLong(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 8 bytes of a byte array beyond the given offset
	 * to a double. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static double bytesToDouble(final short[] bytes, final int off,
		final boolean little)
	{
		return bytesToDouble(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a byte array to a double. If there
	 * are fewer than 8 bytes available, the MSBs are all assumed to be zero
	 * (regardless of endianness).
	 */
	public static double bytesToDouble(final short[] bytes, final boolean little)
	{
		return bytesToDouble(bytes, 0, 8, little);
	}

	/** Translates the given byte array into a String of hexadecimal digits. */
	public static String bytesToHex(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : bytes) {
			final String hexString = Integer.toHexString(b & 0xff);
			if (hexString.length() == 1) sb.append("0");
			sb.append(hexString);
		}
		return sb.toString();
	}

	/** Normalize the decimal separator for the user's locale. */
	public static String sanitizeDouble(String value) {
		value = value.replaceAll("[^0-9,\\.]", "");
		final char separator = new DecimalFormatSymbols().getDecimalSeparator();
		final char usedSeparator = separator == '.' ? ',' : '.';
		value = value.replace(usedSeparator, separator);
		try {
			Double.parseDouble(value);
		}
		catch (final Exception e) {
			value = value.replace(separator, usedSeparator);
		}
		return value;
	}

	// -- Word decoding - primitive types to bytes --

	/** Translates the short value into an array of two bytes. */
	public static byte[] shortToBytes(final short value, final boolean little) {
		final byte[] v = new byte[2];
		unpackBytes(value, v, 0, 2, little);
		return v;
	}

	/** Translates the int value into an array of four bytes. */
	public static byte[] intToBytes(final int value, final boolean little) {
		final byte[] v = new byte[4];
		unpackBytes(value, v, 0, 4, little);
		return v;
	}

	/** Translates the float value into an array of four bytes. */
	public static byte[] floatToBytes(final float value, final boolean little) {
		final byte[] v = new byte[4];
		unpackBytes(Float.floatToIntBits(value), v, 0, 4, little);
		return v;
	}

	/** Translates the long value into an array of eight bytes. */
	public static byte[] longToBytes(final long value, final boolean little) {
		final byte[] v = new byte[8];
		unpackBytes(value, v, 0, 8, little);
		return v;
	}

	/** Translates the double value into an array of eight bytes. */
	public static byte[] doubleToBytes(final double value, final boolean little) {
		final byte[] v = new byte[8];
		unpackBytes(Double.doubleToLongBits(value), v, 0, 8, little);
		return v;
	}

	/** Translates an array of short values into an array of byte values. */
	public static byte[]
		shortsToBytes(final short[] values, final boolean little)
	{
		final byte[] v = new byte[values.length * 2];
		for (int i = 0; i < values.length; i++) {
			unpackBytes(values[i], v, i * 2, 2, little);
		}
		return v;
	}

	/** Translates an array of int values into an array of byte values. */
	public static byte[] intsToBytes(final int[] values, final boolean little) {
		final byte[] v = new byte[values.length * 4];
		for (int i = 0; i < values.length; i++) {
			unpackBytes(values[i], v, i * 4, 4, little);
		}
		return v;
	}

	/** Translates an array of float values into an array of byte values. */
	public static byte[]
		floatsToBytes(final float[] values, final boolean little)
	{
		final byte[] v = new byte[values.length * 4];
		for (int i = 0; i < values.length; i++) {
			unpackBytes(Float.floatToIntBits(values[i]), v, i * 4, 4, little);
		}
		return v;
	}

	/** Translates an array of long values into an array of byte values. */
	public static byte[] longsToBytes(final long[] values, final boolean little) {
		final byte[] v = new byte[values.length * 8];
		for (int i = 0; i < values.length; i++) {
			unpackBytes(values[i], v, i * 8, 8, little);
		}
		return v;
	}

	/** Translates an array of double values into an array of byte values. */
	public static byte[] doublesToBytes(final double[] values,
		final boolean little)
	{
		final byte[] v = new byte[values.length * 8];
		for (int i = 0; i < values.length; i++) {
			unpackBytes(Double.doubleToLongBits(values[i]), v, i * 8, 8, little);
		}
		return v;
	}

	/** @deprecated Use {@link #unpackBytes(long, byte[], int, int, boolean)} */
	@Deprecated
	public static void unpackShort(final short value, final byte[] buf,
		final int ndx, final boolean little)
	{
		unpackBytes(value, buf, ndx, 2, little);
	}

	/**
	 * Translates nBytes of the given long and places the result in the given byte
	 * array.
	 *
	 * @throws IllegalArgumentException if the specified indices fall outside the
	 *           buffer
	 */
	public static void unpackBytes(final long value, final byte[] buf,
		final int ndx, final int nBytes, final boolean little)
	{
		if (buf.length < ndx + nBytes) {
			throw new IllegalArgumentException("Invalid indices: buf.length=" +
				buf.length + ", ndx=" + ndx + ", nBytes=" + nBytes);
		}
		if (little) {
			for (int i = 0; i < nBytes; i++) {
				buf[ndx + i] = (byte) ((value >> (8 * i)) & 0xff);
			}
		}
		else {
			for (int i = 0; i < nBytes; i++) {
				buf[ndx + i] = (byte) ((value >> (8 * (nBytes - i - 1))) & 0xff);
			}
		}
	}

	/**
	 * Convert a byte array to the appropriate 1D primitive type array.
	 *
	 * @param b Byte array to convert.
	 * @param bpp Denotes the number of bytes in the returned primitive type (e.g.
	 *          if bpp == 2, we should return an array of type short).
	 * @param fp If set and bpp == 4 or bpp == 8, then return floats or doubles.
	 * @param little Whether byte array is in little-endian order.
	 */
	public static Object makeDataArray(final byte[] b, final int bpp,
		final boolean fp, final boolean little)
	{
		if (bpp == 1) {
			return b;
		}
		else if (bpp == 2) {
			final short[] s = new short[b.length / 2];
			for (int i = 0; i < s.length; i++) {
				s[i] = bytesToShort(b, i * 2, 2, little);
			}
			return s;
		}
		else if (bpp == 4 && fp) {
			final float[] f = new float[b.length / 4];
			for (int i = 0; i < f.length; i++) {
				f[i] = bytesToFloat(b, i * 4, 4, little);
			}
			return f;
		}
		else if (bpp == 4) {
			final int[] i = new int[b.length / 4];
			for (int j = 0; j < i.length; j++) {
				i[j] = bytesToInt(b, j * 4, 4, little);
			}
			return i;
		}
		else if (bpp == 8 && fp) {
			final double[] d = new double[b.length / 8];
			for (int i = 0; i < d.length; i++) {
				d[i] = bytesToDouble(b, i * 8, 8, little);
			}
			return d;
		}
		else if (bpp == 8) {
			final long[] l = new long[b.length / 8];
			for (int i = 0; i < l.length; i++) {
				l[i] = bytesToLong(b, i * 8, 8, little);
			}
			return l;
		}
		return null;
	}

	/**
	 * @param signed The signed parameter is ignored.
	 * @deprecated Use {@link #makeDataArray(byte[], int, boolean, boolean)}
	 *             regardless of signedness.
	 */
	@Deprecated
	public static Object makeDataArray(final byte[] b, final int bpp,
		final boolean fp, final boolean little, final boolean signed)
	{
		return makeDataArray(b, bpp, fp, little);
	}

	/**
	 * Convert a byte array to the appropriate 2D primitive type array.
	 *
	 * @param b Byte array to convert.
	 * @param bpp Denotes the number of bytes in the returned primitive type (e.g.
	 *          if bpp == 2, we should return an array of type short).
	 * @param fp If set and bpp == 4 or bpp == 8, then return floats or doubles.
	 * @param little Whether byte array is in little-endian order.
	 * @param height The height of the output primitive array (2nd dim length).
	 * @return a 2D primitive array of appropriate type, dimensioned
	 *         [height][b.length / (bpp * height)]
	 * @throws IllegalArgumentException if input byte array does not divide evenly
	 *           into height pieces
	 */
	public static Object makeDataArray2D(final byte[] b, final int bpp,
		final boolean fp, final boolean little, final int height)
	{
		if (b.length % (bpp * height) != 0) {
			throw new IllegalArgumentException("Array length mismatch: " +
				"b.length=" + b.length + "; bpp=" + bpp + "; height=" + height);
		}
		final int width = b.length / (bpp * height);
		if (bpp == 1) {
			final byte[][] b2 = new byte[height][width];
			for (int y = 0; y < height; y++) {
				final int index = width * y;
				System.arraycopy(b, index, b2[y], 0, width);
			}
			return b2;
		}
		else if (bpp == 2) {
			final short[][] s = new short[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 2 * (width * y + x);
					s[y][x] = bytesToShort(b, index, 2, little);
				}
			}
			return s;
		}
		else if (bpp == 4 && fp) {
			final float[][] f = new float[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 4 * (width * y + x);
					f[y][x] = bytesToFloat(b, index, 4, little);
				}
			}
			return f;
		}
		else if (bpp == 4) {
			final int[][] i = new int[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 4 * (width * y + x);
					i[y][x] = bytesToInt(b, index, 4, little);
				}
			}
			return i;
		}
		else if (bpp == 8 && fp) {
			final double[][] d = new double[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 8 * (width * y + x);
					d[y][x] = bytesToDouble(b, index, 8, little);
				}
			}
			return d;
		}
		else if (bpp == 8) {
			final long[][] l = new long[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 8 * (width * y + x);
					l[y][x] = bytesToLong(b, index, 8, little);
				}
			}
			return l;
		}
		return null;
	}

	// -- Byte swapping --

	public static short swap(final short x) {
		return (short) ((x << 8) | ((x >> 8) & 0xFF));
	}

	public static char swap(final char x) {
		return (char) ((x << 8) | ((x >> 8) & 0xFF));
	}

	public static int swap(final int x) {
		return (swap((short) x) << 16) | (swap((short) (x >> 16)) & 0xFFFF);
	}

	public static long swap(final long x) {
		return ((long) swap((int) x) << 32) | (swap((int) (x >> 32)) & 0xFFFFFFFFL);
	}

	public static float swap(final float x) {
		return Float.intBitsToFloat(swap(Float.floatToIntBits(x)));
	}

	public static double swap(final double x) {
		return Double.longBitsToDouble(swap(Double.doubleToLongBits(x)));
	}

	// -- Strings --

	/**
	 * Convert byte array to a hexadecimal string.
	 *
	 * @deprecated Use {@link #bytesToHex(byte[])} instead.
	 */
	@Deprecated
	public static String getHexString(final byte[] b) {
		return bytesToHex(b);
	}

	/** Remove null bytes from a string. */
	public static String stripString(final String toStrip) {
		final StringBuilder s = new StringBuilder();
		for (int i = 0; i < toStrip.length(); i++) {
			if (toStrip.charAt(i) != 0) {
				s.append(toStrip.charAt(i));
			}
		}
		return s.toString().trim();
	}

	/** Check if two filenames have the same prefix. */
	public static boolean samePrefix(final String s1, final String s2) {
		if (s1 == null || s2 == null) return false;
		final int n1 = s1.indexOf(".");
		final int n2 = s2.indexOf(".");
		if ((n1 == -1) || (n2 == -1)) return false;

		final int slash1 = s1.lastIndexOf(File.pathSeparator);
		final int slash2 = s2.lastIndexOf(File.pathSeparator);

		final String sub1 = s1.substring(slash1 + 1, n1);
		final String sub2 = s2.substring(slash2 + 1, n2);
		return sub1.equals(sub2) || sub1.startsWith(sub2) || sub2.startsWith(sub1);
	}

	/** Remove unprintable characters from the given string. */
	public static String sanitize(final String s) {
		if (s == null) return null;
		StringBuffer buf = new StringBuffer(s);
		for (int i = 0; i < buf.length(); i++) {
			final char c = buf.charAt(i);
			if (c != '\t' && c != '\n' && (c < ' ' || c > '~')) {
				buf = buf.deleteCharAt(i--);
			}
		}
		return buf.toString();
	}

	// -- Normalization --

	/**
	 * Normalize the given float array so that the minimum value maps to 0.0 and
	 * the maximum value maps to 1.0.
	 */
	public static float[] normalizeFloats(final float[] data) {
		final float[] rtn = new float[data.length];

		// determine the finite min and max values
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (final float floatValue : data) {
			if (floatValue == Float.POSITIVE_INFINITY ||
				floatValue == Float.NEGATIVE_INFINITY)
			{
				continue;
			}
			if (floatValue < min) min = floatValue;
			if (floatValue > max) max = floatValue;
		}

		// normalize infinity values
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Float.POSITIVE_INFINITY) data[i] = max;
			else if (data[i] == Float.NEGATIVE_INFINITY) data[i] = min;
		}

		// now normalize; min => 0.0, max => 1.0
		final float range = max - min;
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = (data[i] - min) / range;
		}
		return rtn;
	}

	/**
	 * Normalize the given double array so that the minimum value maps to 0.0 and
	 * the maximum value maps to 1.0.
	 */
	public static double[] normalizeDoubles(final double[] data) {
		final double[] rtn = new double[data.length];

		// determine the finite min and max values
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final double doubleValue : data) {
			if (doubleValue == Double.POSITIVE_INFINITY ||
				doubleValue == Double.NEGATIVE_INFINITY)
			{
				continue;
			}
			if (doubleValue < min) min = doubleValue;
			if (doubleValue > max) max = doubleValue;
		}

		// normalize infinity values
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Double.POSITIVE_INFINITY) data[i] = max;
			else if (data[i] == Double.NEGATIVE_INFINITY) data[i] = min;
		}

		// now normalize; min => 0.0, max => 1.0
		final double range = max - min;
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = (data[i] - min) / range;
		}
		return rtn;
	}

	// -- Array handling --

	/**
	 * Allocates a 1-dimensional byte array matching the product of the given
	 * sizes.
	 *
	 * @param sizes list of sizes from which to allocate the array
	 * @return a byte array of the appropriate size
	 * @throws IllegalArgumentException if the total size exceeds 2GB, which is
	 *           the maximum size of an array in Java; or if any size argument is
	 *           zero or negative
	 */
	public static byte[] allocate(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes == null) return null;
		if (sizes.length == 0) return new byte[0];
		final int total = safeMultiply32(sizes);
		return new byte[total];
	}

	/**
	 * Checks that the product of the given sizes does not exceed the 32-bit
	 * integer limit (i.e., {@link Integer#MAX_VALUE}).
	 *
	 * @param sizes list of sizes from which to compute the product
	 * @return the product of the given sizes
	 * @throws IllegalArgumentException if the total size exceeds 2GiB, which is
	 *           the maximum size of an int in Java; or if any size argument is
	 *           zero or negative
	 */
	public static int safeMultiply32(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes.length == 0) return 0;
		long total = 1;
		for (final long size : sizes) {
			if (size < 1) {
				throw new IllegalArgumentException("Invalid array size: " +
					sizeAsProduct(sizes));
			}
			total *= size;
			if (total > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Array size too large: " +
					sizeAsProduct(sizes));
			}
		}
		// NB: The downcast to int is safe here, due to the checks above.
		return (int) total;
	}

	/**
	 * Checks that the product of the given sizes does not exceed the 64-bit
	 * integer limit (i.e., {@link Long#MAX_VALUE}).
	 *
	 * @param sizes list of sizes from which to compute the product
	 * @return the product of the given sizes
	 * @throws IllegalArgumentException if the total size exceeds 8EiB, which is
	 *           the maximum size of a long in Java; or if any size argument is
	 *           zero or negative
	 */
	public static long safeMultiply64(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes.length == 0) return 0;
		long total = 1;
		for (final long size : sizes) {
			if (size < 1) {
				throw new IllegalArgumentException("Invalid array size: " +
					sizeAsProduct(sizes));
			}
			if (willOverflow(total, size)) {
				throw new IllegalArgumentException("Array size too large: " +
					sizeAsProduct(sizes));
			}
			total *= size;
		}
		return total;
	}

	/** Returns true if the given value is contained in the given array. */
	public static boolean containsValue(final int[] array, final int value) {
		return indexOf(array, value) != -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final int[] array, final int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * Object array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final Object[] array, final Object value) {
		for (int i = 0; i < array.length; i++) {
			if (value == null) {
				if (array[i] == null) return i;
			}
			else if (value.equals(array[i])) return i;
		}
		return -1;
	}

	// -- Signed data conversion --

	public static byte[] makeSigned(final byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (b[i] + 128);
		}
		return b;
	}

	public static short[] makeSigned(final short[] s) {
		for (int i = 0; i < s.length; i++) {
			s[i] = (short) (s[i] + 32768);
		}
		return s;
	}

	public static int[] makeSigned(final int[] i) {
		for (int j = 0; j < i.length; j++) {
			i[j] = (int) (i[j] + 2147483648L);
		}
		return i;
	}

	// -- Helper methods --

	private static String sizeAsProduct(final long... sizes) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final long size : sizes) {
			if (first) first = false;
			else sb.append(" x ");
			sb.append(size);
		}
		return sb.toString();
	}

	private static boolean willOverflow(final long v1, final long v2) {
		return Long.MAX_VALUE / v1 < v2;
	}

}
