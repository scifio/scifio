/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2023 SCIFIO developers.
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

import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;

public class BitBufferTest {

	/**
	 * Testing method.
	 */
	@Test
	public void testBitbuffer() {

		final int trials = 50000;
		final int[] nums = new int[trials];
		final int[] len = new int[trials];
		final BitWriter bw = new BitWriter();
		int totallen = 0;

		final Random r = new Random();

		// we want the trials to be able to be all possible bit lengths.
		// r.nextInt() by itself is not sufficient... in 50000 trials it would
		// be extremely unlikely to produce bit strings of 1 bit.
		// instead, we randomly choose from 0 to 2^(i % 32).
		// Except, 1 << 31 is a negative number in two's complement, so we make
		// it a random number in the entire range.
		for (int i = 0; i < trials; i++) {
			if (31 == i % 32) {
				nums[i] = r.nextInt();
			}
			else {
				nums[i] = r.nextInt(1 << (i % 32));
			}
			// How many bits are required to represent this number?
			len[i] = (Integer.toBinaryString(nums[i])).length();
			totallen += len[i];
			bw.write(nums[i], len[i]);
		}
		BitBuffer bb = new BitBuffer(bw.toByteArray());
		int readint;

		// Randomly skip or read bytes
		for (int i = 0; i < trials; i++) {
			final int c = r.nextInt(100);
			if (c > 50) {
				readint = bb.getBits(len[i]);
				if (readint != nums[i]) {
					fail("Error at #" + i + ": " + readint + " received, " + nums[i] +
						" expected.");
				}
			}
			else {
				bb.skipBits(len[i]);
			}
		}
		// Test reading past end of buffer.
		bb = new BitBuffer(bw.toByteArray());
		// The total length could be mid byte. Add one byte to test.
		bb.skipBits(totallen + 8);
		final int read = bb.getBits(1);
		if (-1 != read) {
			fail("-1 expected at end of buffer, " + read + " received.");
		}
	}
}
