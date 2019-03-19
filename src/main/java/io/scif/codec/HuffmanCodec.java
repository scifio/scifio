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

import java.io.IOException;
import java.util.HashMap;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * This class implements Huffman decoding.
 *
 * @author Melissa Linkert
 */
@Plugin(type = Codec.class)
public class HuffmanCodec extends AbstractCodec {

	// -- Constants --

	private static final int LEAVES_OFFSET = 16;

	// -- Fields --

	private int leafCounter;

	private final HashMap<short[], Decoder> cachedDecoders = new HashMap<>();

	// -- Codec API methods --

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		throw new UnsupportedCompressionException(
			"Huffman encoding not currently supported");
	}

	/**
	 * The CodecOptions parameter must be an instance of
	 * {@link HuffmanCodecOptions}, and should have the following fields set:
	 * {@link HuffmanCodecOptions#table table} {@link CodecOptions#bitsPerSample
	 * bitsPerSample} {@link CodecOptions#maxBytes maxBytes}
	 *
	 * @see Codec#decompress(DataHandle, CodecOptions)
	 */
	@Override
	public byte[] decompress(final DataHandle<Location> in,
		final CodecOptions options) throws FormatException, IOException
	{
		if (in == null) throw new IllegalArgumentException(
			"No data to decompress.");
		if (options == null || !(options instanceof HuffmanCodecOptions)) {
			throw new FormatException("Options must be an instance of " +
				"loci.formats.codec.HuffmanCodecOptions.");
		}

		final HuffmanCodecOptions huffman = (HuffmanCodecOptions) options;
		final byte[] pix = new byte[huffman.maxBytes];
		in.read(pix);

		final BitBuffer bb = new BitBuffer(pix);

		final int nSamples = (huffman.maxBytes * 8) / huffman.bitsPerSample;
		int bytesPerSample = huffman.bitsPerSample / 8;
		if ((huffman.bitsPerSample % 8) != 0) bytesPerSample++;

		final BitWriter out = new BitWriter();

		for (int i = 0; i < nSamples; i++) {
			final int sample = getSample(bb, options);
			out.write(sample, bytesPerSample * 8);
		}

		return out.toByteArray();
	}

	// -- HuffmanCodec API methods --

	public int getSample(final BitBuffer bb, final CodecOptions options)
		throws FormatException
	{
		if (bb == null) {
			throw new IllegalArgumentException("No data to handle.");
		}
		if (options == null || !(options instanceof HuffmanCodecOptions)) {
			throw new FormatException("Options must be an instance of " +
				"loci.formats.codec.HuffmanCodecOptions.");
		}

		final HuffmanCodecOptions huffman = (HuffmanCodecOptions) options;
		Decoder decoder = cachedDecoders.get(huffman.table);
		if (decoder == null) {
			decoder = new Decoder(huffman.table);
			cachedDecoders.put(huffman.table, decoder);
		}

		int bitCount = decoder.decode(bb);
		if (bitCount == 16) {
			return 0x8000;
		}
		if (bitCount < 0) bitCount = 0;
		int v = bb.getBits(bitCount) & ((int) Math.pow(2, bitCount) - 1);
		if ((v & (1 << (bitCount - 1))) == 0) {
			v -= (1 << bitCount) - 1;
		}

		return v;
	}

	// -- Helper class --

	class Decoder {

		public Decoder[] branch = new Decoder[2];

		private int leafValue = -1;

		public Decoder() {}

		public Decoder(final short[] source) {
			leafCounter = 0;
			createDecoder(this, source, 0, 0);
		}

		private Decoder createDecoder(final short[] source, final int start,
			final int level)
		{
			final Decoder dest = new Decoder();
			createDecoder(dest, source, start, level);
			return dest;
		}

		private void createDecoder(final Decoder dest, final short[] source,
			final int start, final int level)
		{
			int next = 0;
			int i = 0;
			while (i <= leafCounter && next < LEAVES_OFFSET) {
				i += source[start + next++] & 0xff;
			}

			if (level < next && next < LEAVES_OFFSET) {
				dest.branch[0] = createDecoder(source, start, level + 1);
				dest.branch[1] = createDecoder(source, start, level + 1);
			}
			else {
				i = start + LEAVES_OFFSET + leafCounter++;
				if (i < source.length) {
					dest.leafValue = source[i] & 0xff;
				}
			}
		}

		public int decode(final BitBuffer bb) {
			Decoder d = this;
			while (d.branch[0] != null) {
				final int v = bb.getBits(1);
				if (v < 0) break; // eof
				d = d.branch[v];
			}
			return d.leafValue;
		}

	}

}
