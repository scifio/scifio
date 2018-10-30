
package io.scif.codec;

import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

public class BitWriterTest {

	/**
	 * Tests the BitWriter class.
	 */
	@Test
	public void bitBufferTest() {

		final int max = 50000;
		// randomize values
		final int[] values = new int[max];
		final int[] bits = new int[max];
		final double log2 = Math.log(2);
		for (int i = 0; i < values.length; i++) {
			values[i] = (int) (50000 * Math.random()) + 1;
			final int minBits = (int) Math.ceil(Math.log(values[i] + 1) / log2);
			bits[i] = (int) (10 * Math.random()) + minBits;
		}

		// write values out
		BitWriter out = new BitWriter();
		for (int i = 0; i < values.length; i++)
			out.write(values[i], bits[i]);

		// read values back in
		BitBuffer bb = new BitBuffer(out.toByteArray());
		for (int i = 0; i < values.length; i++) {
			final int value = bb.getBits(bits[i]);
			if (value != values[i]) {
				fail("Value #" + i + " does not match (got " + value + "; expected " +
					values[i] + "; " + bits[i] + " bits)");
			}
		}

		// Testing string functionality
		final Random r = new Random();
		// "Generating 5000 random bits for String test"
		final StringBuilder sb = new StringBuilder(5000);
		for (int i = 0; i < 5000; i++) {
			sb.append(r.nextInt(2));
		}
		out = new BitWriter();
		out.write(sb.toString());
		bb = new BitBuffer(out.toByteArray());
		for (int i = 0; i < 5000; i++) {
			final int value = bb.getBits(1);
			final int expected = (sb.charAt(i) == '1') ? 1 : 0;
			if (value != expected) {
				fail("Bit #" + i + " does not match (got " + value + "; expected " +
					expected + ")");
			}
		}
	}
}
