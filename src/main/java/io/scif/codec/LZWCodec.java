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

import io.scif.FormatException;

import java.io.IOException;
import java.util.Arrays;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * This is an optimized LZW codec for use with TIFF files. Most of the code is
 * inlined, and specifics of TIFF usage of LZW (known size of decompressor
 * output; possible lengths of LZW codes; specified values for {@code CLEAR} and
 * {@code END_OF_INFORMATION} codes) are taken in account.
 * <p>
 * Estimating the worst-case size of compressor output:
 * </p>
 * <ul>
 * <li>The worst case means that there is no compression at all, and every input
 * byte generates code to output.</li>
 * <li>This means that the LZW table will be full (and reset) after reading each
 * portion of 4096-256-2-1=3837 bytes of input (first 256 codes are
 * preallocated, 2 codes are used for CLEAR and END_OF_IFORMATION, 1 code is
 * lost due to original bug in TIFF library that now is a feature).</li>
 * <li>Each full portion of 3837 byte will produce in output:
 * <ul>
 * <li>9 bits for CLEAR code;</li>
 * <li>9*253 + 10*512 + 11*1024 + 12*2048 = 43237 bits for character codes.</li>
 * </ul>
 * </li>
 * <li>Let n=3837, m=(number of bytes in the last incomplete portion), N=(number
 * of bytes in compressed complete portion with CLEAR code), M=(number of bytes
 * in compressed last incomplete portion). We have inequalities:
 * <ul>
 * <li>{@code N <= 1.41 * n}</li>
 * <li>{@code M <= 1.41 * m}</li>
 * <li>The last incomplete portion should also include {@code CLEAR} and
 * {@code END_OF_INFORMATION} codes; they occupy less than 3 bytes.
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Thus, we can claim than the number of bytes in compressed output never
 * exceeds 1.41*(number of input bytes)+3.
 * </p>
 *
 * @author Mikhail Kovtun
 */
@Plugin(type = Codec.class)
public class LZWCodec extends AbstractCodec {

	/**
	 * Size of hash table. Must be greater 3837 (the number of possible codes).
	 * Bigger size reduces number of rehashing steps -- at expence of
	 * initialization time.
	 */
	private static final int HASH_SIZE = 7349;

	/** Rehashing step. HASH_SIZE and HASH_STEP shoulg be coprime. */
	private static final int HASH_STEP = 257;

	private static final int CLEAR_CODE = 256;

	private static final int EOI_CODE = 257;

	private static final int FIRST_CODE = 258;

	/** Masks for writing bits in compressor. */
	private static final int[] COMPR_MASKS = { 0xff, 0x7f, 0x3f, 0x1f, 0x0f, 0x07,
		0x03, 0x01 };

	/** Masks for reading bits in decompressor. */
	private static final int[] DECOMPR_MASKS = { 0x00, 0x01, 0x03, 0x07, 0x0f,
		0x1f, 0x3f, 0x7f };

	@Override
	public byte[] compress(final byte[] input, final CodecOptions options)
		throws FormatException
	{
		if (input == null || input.length == 0) return input;

		// Output buffer (see class comments for justification of size).
		final long bufferSize = ((long) input.length * 141) / 100 + 3;
		if (bufferSize > Integer.MAX_VALUE) {
			throw new FormatException("Output buffer is greater than 2 GB");
		}
		final byte[] output = new byte[(int) bufferSize];

		// Current size of output buffer (and position to write next byte).
		int outSize = 0;
		// The output always starts with CLEAR code
		output[outSize++] = (byte) (CLEAR_CODE >> 1);
		// Last incomplete byte to be written to output (bits shifted to the
		// right).
		// Always contains at least 1 bit, and may contain 8 bits.
		int currOutByte = CLEAR_CODE & 0x01;
		// Number of unused bits in currOutByte (from 0 to 7).
		int freeBits = 7;

		// Hash table.
		// Keys in the table are pairs (code,byte) and values are codes.
		// Pair (code,byte) is represented as ( (code<<8) | byte ).
		// Unused table entries have key=-1.
		final int[] htKeys = new int[HASH_SIZE];
		final int[] htValues = new int[HASH_SIZE];
		// Initialize hash table: mark all entries as unused
		Arrays.fill(htKeys, -1);

		// Next code to be used by compressor.
		int nextCode = FIRST_CODE;
		// Number of bits to be used to output code. Ranges from 9 to 12.
		int currCodeLength = 9;

		// Names of these variables are taken from TIFF specification.
		// The first byte of input is handled specially.
		int tiffK = input[0] & 0xff;
		int tiffOmega = tiffK;

		// Main loop.
		for (int currInPos = 1; currInPos < input.length; currInPos++) {
			tiffK = input[currInPos] & 0xff;
			final int hashKey = (tiffOmega << 8) | tiffK;
			int hashCode = hashKey % HASH_SIZE;
			do {
				if (htKeys[hashCode] == hashKey) {
					// Omega+K in the table
					tiffOmega = htValues[hashCode];
					break;
				}
				else if (htKeys[hashCode] < 0) {
					// Omega+K not in the table
					// 1) add new entry to hash table
					htKeys[hashCode] = hashKey;
					htValues[hashCode] = nextCode++;
					// 2) output last code
					int shift = currCodeLength - freeBits;
					output[outSize++] = (byte) ((currOutByte << freeBits) |
						(tiffOmega >> shift));
					if (shift > 8) {
						output[outSize++] = (byte) (tiffOmega >> (shift - 8));
						shift -= 8;
					}
					freeBits = 8 - shift;
					currOutByte = tiffOmega & COMPR_MASKS[freeBits];
					// 3) omega = K
					tiffOmega = tiffK;
					break;
				}
				else {
					// we have to rehash
					hashCode = (hashCode + HASH_STEP) % HASH_SIZE;
				}
			}
			while (true);

			switch (nextCode) {
				case 512:
					currCodeLength = 10;
					break;
				case 1024:
					currCodeLength = 11;
					break;
				case 2048:
					currCodeLength = 12;
					break;
				case 4096: // write CLEAR code and reinitialize hash table
					int shift = currCodeLength - freeBits;
					output[outSize++] = (byte) ((currOutByte << freeBits) |
						(CLEAR_CODE >> shift));
					if (shift > 8) {
						output[outSize++] = (byte) (CLEAR_CODE >> (shift - 8));
						shift -= 8;
					}
					freeBits = 8 - shift;
					currOutByte = CLEAR_CODE & COMPR_MASKS[freeBits];
					Arrays.fill(htKeys, -1);
					nextCode = FIRST_CODE;
					currCodeLength = 9;
					break;
			}
		}

		// End of input:
		// 1) write code from tiff_Omega
		{
			int shift = currCodeLength - freeBits;
			output[outSize++] = (byte) ((currOutByte << freeBits) |
				(tiffOmega >> shift));
			if (shift > 8) {
				output[outSize++] = (byte) (tiffOmega >> (shift - 8));
				shift -= 8;
			}
			freeBits = 8 - shift;
			currOutByte = tiffOmega & COMPR_MASKS[freeBits];
		}
		// 2) write END_OF_INFORMATION code
		// -- we write the last incomplete byte here as well
		// !!! We have to increase length of code if needed !!!
		switch (nextCode) {
			case 511:
				currCodeLength = 10;
				break;
			case 1023:
				currCodeLength = 11;
				break;
			case 2047:
				currCodeLength = 12;
				break;
		}

		{
			int shift = currCodeLength - freeBits;
			output[outSize++] = (byte) ((currOutByte << freeBits) |
				(EOI_CODE >> shift));
			if (shift > 8) {
				output[outSize++] = (byte) (EOI_CODE >> (shift - 8));
				shift -= 8;
			}
			freeBits = 8 - shift;
			currOutByte = EOI_CODE & COMPR_MASKS[freeBits];
			output[outSize++] = (byte) (currOutByte << freeBits);
		}

		final byte[] result = new byte[outSize];
		System.arraycopy(output, 0, result, 0, outSize);
		return result;
	}

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#maxBytes maxBytes}
	 *
	 * @see Codec#decompress(DataHandle, CodecOptions)
	 */
	@Override
	public byte[] decompress(final DataHandle<Location> in, CodecOptions options)
		throws FormatException, IOException
	{
		if (in == null || in.length() == 0) return null;
		if (options == null) options = CodecOptions.getDefaultOptions();

		// Output buffer
		final byte[] output = new byte[options.maxBytes];
		// Position in output buffer to write next byte to
		int currOutPos = 0;

		// Table mapping codes to strings.
		// Its structure is based on the fact that a string for a code has form:
		// (string for another code) + (new byte).
		// Thus, at index 'code': first array contains 'another code', second
		// array
		// contains 'new byte', and third array contains length of the string.
		// The length is needed to make retrieving the string faster.
		final int[] anotherCodes = new int[4096];
		final byte[] newBytes = new byte[4096];
		final int[] lengths = new int[4096];
		// We need to initialize only firt 256 entries in the table
		for (int i = 0; i < 256; i++) {
			newBytes[i] = (byte) i;
			lengths[i] = 1;
		}

		// Length of the code to be read from input
		int currCodeLength = 9;
		// Next code to be added to the table
		int nextCode = FIRST_CODE;

		// Variables to handle reading bit stream:
		// Byte from 'input[curr_in_pos-1]' -- only 'bits_read' bits on the
		// right
		// are non-zero
		int currRead = 0;
		// Number of bits in 'curr_read' that were not consumed yet
		int bitsRead = 0;

		// Current code being processed by decompressor.
		int currCode;
		// Previous code processed by decompressor.
		int oldCode = 0; // without initializer, Java reports error later

		try {
			do {
				// read next code
				{
					int bitsLeft = currCodeLength - bitsRead;
					if (bitsLeft > 8) {
						currRead = (currRead << 8) | (in.read() & 0xff);
						bitsLeft -= 8;
					}
					bitsRead = 8 - bitsLeft;
					final int nextByte = in.read() & 0xff;
					currCode = (currRead << bitsLeft) | (nextByte >> bitsRead);
					currRead = nextByte & DECOMPR_MASKS[bitsRead];
				}

				if (currCode == EOI_CODE) break;

				if (currCode == CLEAR_CODE) {
					// initialize table -- nothing to do
					nextCode = FIRST_CODE;
					currCodeLength = 9;
					// read next code
					{
						int bitsLeft = currCodeLength - bitsRead;
						if (bitsLeft > 8) {
							currRead = (currRead << 8) | (in.read() & 0xff);
							bitsLeft -= 8;
						}
						bitsRead = 8 - bitsLeft;

						final int nextByte = in.read() & 0xff;
						currCode = (currRead << bitsLeft) | (nextByte >> bitsRead);
						currRead = nextByte & DECOMPR_MASKS[bitsRead];
					}
					if (currCode == EOI_CODE) break;
					// write string[curr_code] to output
					// -- but here we are sure that string consists of a single
					// byte
					if (currOutPos >= output.length - 1) break;
					output[currOutPos++] = newBytes[currCode];
					oldCode = currCode;
				}
				else if (currCode < nextCode) {
					// Code is already in the table
					// 1) Write strin[curr_code] to output
					final int outLength = lengths[currCode];
					int i = currOutPos + outLength;
					int tablePos = currCode;
					if (i > output.length) break;
					while (i > currOutPos) {
						output[--i] = newBytes[tablePos];
						tablePos = anotherCodes[tablePos];
					}
					currOutPos += outLength;
					// 2) Add string[old_code]+firstByte(string[curr_code]) to
					// the table
					if (nextCode >= anotherCodes.length) break;
					anotherCodes[nextCode] = oldCode;
					newBytes[nextCode] = output[i];
					lengths[nextCode] = lengths[oldCode] + 1;
					oldCode = currCode;
					nextCode++;
				}
				else {
					// Special case: code is not in the table
					// 1) Write string[old_code] to output
					final int outLength = lengths[oldCode];
					int i = currOutPos + outLength;
					int tablePos = oldCode;
					if (i > output.length) break;
					while (i > currOutPos) {
						output[--i] = newBytes[tablePos];
						tablePos = anotherCodes[tablePos];
					}
					currOutPos += outLength;
					// 2) Write firstByte(string[old_code]) to output
					if (currOutPos >= output.length - 1) break;
					output[currOutPos++] = output[i];
					// 3) Add string[old_code]+firstByte(string[old_code]) to
					// the table
					anotherCodes[nextCode] = oldCode;
					newBytes[nextCode] = output[i];
					lengths[nextCode] = outLength + 1;
					oldCode = currCode;
					nextCode++;
				}
				// Increase length of code if needed
				switch (nextCode) {
					case 511:
						currCodeLength = 10;
						break;
					case 1023:
						currCodeLength = 11;
						break;
					case 2047:
						currCodeLength = 12;
						break;
				}
			}
			while (currOutPos < output.length && in.offset() < in.length());
		}
		catch (final ArrayIndexOutOfBoundsException e) {
			throw new FormatException("Invalid LZW data", e);
		}
		return output;
	}
}
