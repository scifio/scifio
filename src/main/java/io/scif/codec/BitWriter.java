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

package io.scif.codec;

import java.util.Random;

import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;

/**
 * A class for writing arbitrary numbers of bits to a byte array.
 *
 * @author Curtis Rueden
 */
public class BitWriter {

	// -- Fields --

	/** Buffer storing all bits written thus far. */
	private byte[] buf;

	/** Byte index into the buffer. */
	private int index;

	/** Bit index into current byte of the buffer. */
	private int bit;

	// -- Constructors --

	/** Constructs a new bit writer. */
	public BitWriter() {
		this(10);
	}

	/** Constructs a new bit writer with the given initial buffer size. */
	public BitWriter(final int size) {
		buf = new byte[size];
	}

	// -- BitWriter API methods --

	/** Writes the given value using the given number of bits. */
	public void write(int value, final int numBits) {
		if (numBits <= 0) return;
		final byte[] bits = new byte[numBits];
		for (int i = 0; i < numBits; i++) {
			bits[i] = (byte) (value & 0x0001);
			value >>= 1;
		}
		for (int i = numBits - 1; i >= 0; i--) {
			final int b = bits[i] << (7 - bit);
			buf[index] |= b;
			bit++;
			if (bit > 7) {
				bit = 0;
				index++;
				if (index >= buf.length) {
					// buffer is full; increase the size
					final byte[] newBuf = new byte[buf.length * 2];
					System.arraycopy(buf, 0, newBuf, 0, buf.length);
					buf = newBuf;
				}
			}
		}
	}

	/**
	 * Writes the bits represented by a bit string to the buffer.
	 *
	 * @throws IllegalArgumentException If any characters other than '0' and '1'
	 *           appear in the string.
	 */
	public void write(final String bitString) {
		if (bitString == null) throw new IllegalArgumentException(
			"The string cannot be null.");
		for (int i = 0; i < bitString.length(); i++) {
			if ('1' == bitString.charAt(i)) {
				final int b = 1 << (7 - bit);
				buf[index] |= b;
			}
			else if ('0' != bitString.charAt(i)) {
				throw new IllegalArgumentException(bitString.charAt(i) +
					"found at character " + i +
					"; 0 or 1 expected. Write only partially completed.");
			}
			bit++;
			if (bit > 7) {
				bit = 0;
				index++;
				if (index >= buf.length) {
					// buffer is full; increase the size
					final byte[] newBuf = new byte[buf.length * 2];
					System.arraycopy(buf, 0, newBuf, 0, buf.length);
					buf = newBuf;
				}
			}
		}
	}

	/** Gets an array containing all bits written thus far. */
	public byte[] toByteArray() {
		int size = index;
		if (bit > 0) size++;
		final byte[] b = new byte[size];
		System.arraycopy(buf, 0, b, 0, size);
		return b;
	}

	// -- Main method --

	/** Tests the BitWriter class. */
	public static void main(final String[] args) {
		final LogService log = new StderrLogService();

		final int max = 50000;
		// randomize values
		log.info("Generating random list of " + max + " values");
		final int[] values = new int[max];
		final int[] bits = new int[max];
		final double log2 = Math.log(2);
		for (int i = 0; i < values.length; i++) {
			values[i] = (int) (50000 * Math.random()) + 1;
			final int minBits = (int) Math.ceil(Math.log(values[i] + 1) / log2);
			bits[i] = (int) (10 * Math.random()) + minBits;
		}

		// write values out
		log.info("Writing values to byte array");
		BitWriter out = new BitWriter();
		for (int i = 0; i < values.length; i++)
			out.write(values[i], bits[i]);

		// read values back in
		log.info("Reading values from byte array");
		BitBuffer bb = new BitBuffer(out.toByteArray());
		for (int i = 0; i < values.length; i++) {
			final int value = bb.getBits(bits[i]);
			if (value != values[i]) {
				log.info("Value #" + i + " does not match (got " + value +
					"; expected " + values[i] + "; " + bits[i] + " bits)");
			}
		}

		// Testing string functionality
		final Random r = new Random();
		log.info("Generating 5000 random bits for String test");
		final StringBuilder sb = new StringBuilder(5000);
		for (int i = 0; i < 5000; i++) {
			sb.append(r.nextInt(2));
		}
		out = new BitWriter();
		log.info("Writing values to byte array");
		out.write(sb.toString());
		log.info("Reading values from byte array");
		bb = new BitBuffer(out.toByteArray());
		for (int i = 0; i < 5000; i++) {
			final int value = bb.getBits(1);
			final int expected = (sb.charAt(i) == '1') ? 1 : 0;
			if (value != expected) {
				log.info("Bit #" + i + " does not match (got " + value + "; expected " +
					expected + ")");
			}
		}
	}

}
