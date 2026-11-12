/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2026 SCIFIO developers.
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

import static org.junit.Assert.assertArrayEquals;

import io.scif.FormatException;
import io.scif.SCIFIO;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@link JPEGCodec}.
 */
public class JPEGCodecTest {

	/**
	 * An 8x8 JPEG of the flat colour RGB (40, 200, 90), written by libjpeg-turbo
	 * at quality 100 with no chroma subsampling. It has a JFIF marker, so the
	 * samples are YCbCr that ImageIO converts to RGB on its own.
	 */
	private static final String JFIF_444 =
		"ffd8ffe000104a46494600010100000100010000ffdb00430001010101010101010101" +
			"0101010101010101010101010101010101010101010101010101010101010101010101" +
			"01010101010101010101010101010101010101ffdb0043010101010101010101010101" +
			"0101010101010101010101010101010101010101010101010101010101010101010101" +
			"010101010101010101010101010101010101ffc0001108000800080301110002110103" +
			"1101ffc4001f0000010501010101010100000000000000000102030405060708090a0b" +
			"ffc400b5100002010303020403050504040000017d0102030004110512213141061351" +
			"6107227114328191a1082342b1c11552d1f02433627282090a161718191a2526272829" +
			"2a3435363738393a434445464748494a535455565758595a636465666768696a737475" +
			"767778797a838485868788898a92939495969798999aa2a3a4a5a6a7a8a9aab2b3b4b5" +
			"b6b7b8b9bac2c3c4c5c6c7c8c9cad2d3d4d5d6d7d8d9dae1e2e3e4e5e6e7e8e9eaf1f2" +
			"f3f4f5f6f7f8f9faffc4001f0100030101010101010101010000000000000102030405" +
			"060708090a0bffc400b511000201020404030407050404000102770001020311040521" +
			"31061241510761711322328108144291a1b1c109233352f0156272d10a162434e125f1" +
			"1718191a262728292a35363738393a434445464748494a535455565758595a63646566" +
			"6768696a737475767778797a82838485868788898a92939495969798999aa2a3a4a5a6" +
			"a7a8a9aab2b3b4b5b6b7b8b9bac2c3c4c5c6c7c8c9cad2d3d4d5d6d7d8d9dae2e3e4e5" +
			"e6e7e8e9eaf2f3f4f5f6f7f8f9faffda000c03010002110311003f00f60afe1f3fe71c" +
			"ffd9";

	/**
	 * An 8x8 JPEG whose every pixel stores the YCbCr triple (140, 100, 57),
	 * which is RGB (40, 200, 90). Its Adobe marker has transform 0, so ImageIO
	 * returns the stored values unconverted. Written by libjpeg-turbo.
	 */
	private static final String ADOBE_UNCONVERTED =
		"ffd8ffee000e41646f626500640000000000ffdb004300020101010101020101010202" +
			"020202040302020202050404030406050606060506060607090806070907060608" +
			"0b08090a0a0a0a0a06080b0c0b0a0c090a0a0affc00011080008000803521100471100" +
			"421100ffc4001f0000010501010101010100000000000000000102030405060708090a" +
			"0bffc400b5100002010303020403050504040000017d010203000411051221314106" +
			"13516107227114328191a1082342b1c11552d1f02433627282090a161718191a2526" +
			"2728292a3435363738393a434445464748494a535455565758595a63646566676869" +
			"6a737475767778797a838485868788898a92939495969798999aa2a3a4a5a6a7a8a9" +
			"aab2b3b4b5b6b7b8b9bac2c3c4c5c6c7c8c9cad2d3d4d5d6d7d8d9dae1e2e3e4e5e6" +
			"e7e8e9eaf1f2f3f4f5f6f7f8f9faffda000c03520047004200003f00ec2bc3ebf38ebf" +
			"ffd9";

	private static final int[] RGB = { 40, 200, 90 };

	private static SCIFIO scifio;

	private static Codec codec;

	@BeforeClass
	public static void createContext() {
		scifio = new SCIFIO();
		codec = scifio.codec().getCodec(JPEGCodec.class);
	}

	@AfterClass
	public static void disposeContext() {
		scifio.getContext().dispose();
	}

	@Test
	public void testYCbCrIsConvertedOnce() throws FormatException,
		IOException
	{
		assertArrayEquals(RGB, firstPixel(decodeYCbCr(JFIF_444)));
	}

	@Test
	public void testYCbCrKeepsNegativeChroma() throws FormatException,
		IOException
	{
		assertArrayEquals(RGB, firstPixel(decodeYCbCr(ADOBE_UNCONVERTED)));
	}

	private static byte[] decodeYCbCr(final String hex) throws FormatException,
		IOException
	{
		final CodecOptions options = CodecOptions.getDefaultOptions();
		options.interleaved = true;
		options.ycbcr = true;
		return codec.decompress(parseHex(hex), options);
	}

	private static int[] firstPixel(final byte[] interleaved) {
		return new int[] { interleaved[0] & 0xff, interleaved[1] & 0xff,
			interleaved[2] & 0xff };
	}

	private static byte[] parseHex(final String hex) {
		final byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
}
