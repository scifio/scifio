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
import io.scif.UnsupportedCompressionException;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;
import org.scijava.util.ShortArray;

/**
 * Decompresses lossless JPEG images.
 *
 * @author Melissa Linkert
 */
@Plugin(type = Codec.class)
public class LosslessJPEGCodec extends AbstractCodec {

	// -- Constants --

	// Start of Frame markers - non-differential, Huffman coding
	public static final int SOF0 = 0xffc0; // baseline DCT

	public static final int SOF1 = 0xffc1; // extended sequential DCT

	public static final int SOF2 = 0xffc2; // progressive DCT

	public static final int SOF3 = 0xffc3; // lossless (sequential)

	// Start of Frame markers - differential, Huffman coding
	public static final int SOF5 = 0xffc5; // differential sequential DCT

	public static final int SOF6 = 0xffc6; // differential progressive DCT

	public static final int SOF7 = 0xffc7; // differential lossless (sequential)

	// Start of Frame markers - non-differential, arithmetic coding
	public static final int JPG = 0xffc8; // reserved for JPEG extensions

	public static final int SOF9 = 0xffc9; // extended sequential DCT

	public static final int SOF10 = 0xffca; // progressive DCT

	public static final int SOF11 = 0xffcb; // lossless (sequential)

	// Start of Frame markers - differential, arithmetic coding
	public static final int SOF13 = 0xffcd; // differential sequential DCT

	public static final int SOF14 = 0xffce; // differential progressive DCT

	public static final int SOF15 = 0xffcf; // differential lossless
	// (sequential)

	public static final int DHT = 0xffc4; // define Huffman table(s)

	public static final int DAC = 0xffcc; // define arithmetic coding conditions

	// Restart interval termination
	public static final int RST_0 = 0xffd0;

	public static final int RST_1 = 0xffd1;

	public static final int RST_2 = 0xffd2;

	public static final int RST_3 = 0xffd3;

	public static final int RST_4 = 0xffd4;

	public static final int RST_5 = 0xffd5;

	public static final int RST_6 = 0xffd6;

	public static final int RST_7 = 0xffd7;

	public static final int SOI = 0xffd8; // start of image

	public static final int EOI = 0xffd9; // end of image

	public static final int SOS = 0xffda; // start of scan

	public static final int DQT = 0xffdb; // define quantization table(s)

	public static final int DNL = 0xffdc; // define number of lines

	public static final int DRI = 0xffdd; // define restart interval

	public static final int DHP = 0xffde; // define hierarchical progression

	public static final int EXP = 0xffdf; // expand reference components

	public static final int COM = 0xfffe; // comment

	@Parameter
	private CodecService codecService;

	// -- Codec API methods --

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		throw new UnsupportedCompressionException(
			"Lossless JPEG compression not supported");
	}

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#interleaved interleaved}
	 * {@link CodecOptions#littleEndian littleEndian}
	 *
	 * @see Codec#decompress(RandomAccessInputStream, CodecOptions)
	 */
	@Override
	public byte[] decompress(final RandomAccessInputStream in,
		CodecOptions options) throws FormatException, IOException
	{
		if (in == null) throw new IllegalArgumentException(
			"No data to decompress.");
		if (options == null) options = CodecOptions.getDefaultOptions();
		byte[] buf = new byte[0];

		int width = 0, height = 0;
		int bitsPerSample = 0, nComponents = 0, bytesPerSample = 0;
		int[] horizontalSampling = null, verticalSampling = null;
		int[] quantizationTable = null;
		short[][] huffmanTables = null;

		int startPredictor = 0;

		int[] dcTable = null, acTable = null;

		while (in.getFilePointer() < in.length() - 1) {
			final int code = in.readShort() & 0xffff;
			int length = in.readShort() & 0xffff;
			final long fp = in.getFilePointer();
			if (length > 0xff00) {
				length = 0;
				in.seek(fp - 2);
			}
			else if (code == SOS) {
				nComponents = in.read();
				dcTable = new int[nComponents];
				acTable = new int[nComponents];
				for (int i = 0; i < nComponents; i++) {
					in.read(); // componentSelector
					final int tableSelector = in.read();
					dcTable[i] = (tableSelector & 0xf0) >> 4;
					acTable[i] = tableSelector & 0xf;
				}
				startPredictor = in.read();
				in.read(); // endPredictor
				in.read(); // least significant 4 bits = pointTransform

				// read image data

				byte[] toDecode = new byte[(int) (in.length() - in.getFilePointer())];
				in.read(toDecode);

				// scrub out byte stuffing

				final ByteVector b = new ByteVector();
				for (int i = 0; i < toDecode.length; i++) {
					b.add(toDecode[i]);
					if (toDecode[i] == (byte) 0xff && toDecode[i + 1] == 0) i++;
				}
				toDecode = b.toByteArray();

				final BitBuffer bb = new BitBuffer(toDecode);
				final HuffmanCodec huffman = codecService.getCodec(HuffmanCodec.class);
				final HuffmanCodecOptions huffmanOptions = new HuffmanCodecOptions();
				huffmanOptions.bitsPerSample = bitsPerSample;
				huffmanOptions.maxBytes = buf.length / nComponents;

				int nextSample = 0;
				while (nextSample < buf.length / nComponents) {
					for (int i = 0; i < nComponents; i++) {
						if (huffmanTables != null) {
							huffmanOptions.table = huffmanTables[dcTable[i]];
						}
						int v = 0;

						if (huffmanOptions.table != null) {
							v = huffman.getSample(bb, huffmanOptions);
							if (nextSample == 0) {
								v += (int) Math.pow(2, bitsPerSample - 1);
							}
						}
						else {
							throw new UnsupportedCompressionException(
								"Arithmetic coding not supported");
						}

						// apply predictor to the sample
						int predictor = startPredictor;
						if (nextSample < width * bytesPerSample) predictor = 1;
						else if ((nextSample % (width * bytesPerSample)) == 0) {
							predictor = 2;
						}

						final int componentOffset = i * (buf.length / nComponents);

						final int indexA = nextSample - bytesPerSample + componentOffset;
						final int indexB = nextSample - width * bytesPerSample +
							componentOffset;
						final int indexC = nextSample - (width + 1) * bytesPerSample +
							componentOffset;

						final int sampleA = indexA < 0 ? 0 : Bytes.toInt(buf, indexA,
							bytesPerSample, false);
						final int sampleB = indexB < 0 ? 0 : Bytes.toInt(buf, indexB,
							bytesPerSample, false);
						final int sampleC = indexC < 0 ? 0 : Bytes.toInt(buf, indexC,
							bytesPerSample, false);

						if (nextSample > 0) {
							int pred = 0;
							switch (predictor) {
								case 1:
									pred = sampleA;
									break;
								case 2:
									pred = sampleB;
									break;
								case 3:
									pred = sampleC;
									break;
								case 4:
									pred = sampleA + sampleB + sampleC;
									break;
								case 5:
									pred = sampleA + ((sampleB - sampleC) / 2);
									break;
								case 6:
									pred = sampleB + ((sampleA - sampleC) / 2);
									break;
								case 7:
									pred = (sampleA + sampleB) / 2;
									break;
							}
							v += pred;
						}

						final int offset = componentOffset + nextSample;

						Bytes.unpack(v, buf, offset, bytesPerSample, false);
					}
					nextSample += bytesPerSample;
				}
			}
			else {
				length -= 2; // stored length includes length param
				if (length == 0) continue;

				if (code == EOI) {}
				else if (code == SOF3) {
					// lossless w/Huffman coding
					bitsPerSample = in.read();
					height = in.readShort();
					width = in.readShort();
					nComponents = in.read();
					horizontalSampling = new int[nComponents];
					verticalSampling = new int[nComponents];
					quantizationTable = new int[nComponents];
					for (int i = 0; i < nComponents; i++) {
						in.skipBytes(1);
						final int s = in.read();
						horizontalSampling[i] = (s & 0xf0) >> 4;
						verticalSampling[i] = s & 0x0f;
						quantizationTable[i] = in.read();
					}

					bytesPerSample = bitsPerSample / 8;
					if ((bitsPerSample % 8) != 0) bytesPerSample++;

					buf = new byte[width * height * nComponents * bytesPerSample];
				}
				else if (code == SOF11) {
					throw new UnsupportedCompressionException(
						"Arithmetic coding is not yet supported");
				}
				else if (code == DHT) {
					if (huffmanTables == null) {
						huffmanTables = new short[4][];
					}
					final int s = in.read();
//					final byte tableClass = (byte) ((s & 0xf0) >> 4);
					final byte destination = (byte) (s & 0xf);
					final int[] nCodes = new int[16];
					final ShortArray table = new ShortArray();
					for (int i = 0; i < nCodes.length; i++) {
						nCodes[i] = in.read();
						table.add((short) nCodes[i]);
					}

					for (int i = 0; i < nCodes.length; i++) {
						for (int j = 0; j < nCodes[i]; j++) {
							table.add(new Short((short) (in.read() & 0xff)));
						}
					}
					huffmanTables[destination] = new short[table.size()];
					for (int i = 0; i < huffmanTables[destination].length; i++) {
						huffmanTables[destination][i] = table.getValue(i);
					}
				}
				in.seek(fp + length);
			}
		}

		if (options.interleaved && nComponents > 1) {
			// data is stored in planar (RRR...GGG...BBB...) order
			final byte[] newBuf = new byte[buf.length];
			for (int i = 0; i < buf.length; i += nComponents * bytesPerSample) {
				for (int c = 0; c < nComponents; c++) {
					final int src = c * (buf.length / nComponents) + (i / nComponents);
					final int dst = i + c * bytesPerSample;
					System.arraycopy(buf, src, newBuf, dst, bytesPerSample);
				}
			}
			buf = newBuf;
		}

		if (options.littleEndian && bytesPerSample > 1) {
			// data is stored in big endian order
			// reverse the bytes in each sample
			final byte[] newBuf = new byte[buf.length];
			for (int i = 0; i < buf.length; i += bytesPerSample) {
				for (int q = 0; q < bytesPerSample; q++) {
					newBuf[i + bytesPerSample - q - 1] = buf[i + q];
				}
			}
			buf = newBuf;
		}

		return buf;
	}

}
