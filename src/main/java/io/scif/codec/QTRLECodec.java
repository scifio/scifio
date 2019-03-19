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

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Methods for compressing and decompressing data using QuickTime RLE.
 */
@Plugin(type = Codec.class)
public class QTRLECodec extends AbstractCodec {

	@Parameter
	DataHandleService dataHandleService;

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		throw new UnsupportedCompressionException(
			"QTRLE compression not supported.");
	}

	@Override
	public byte[] decompress(final DataHandle<Location> in,
		final CodecOptions options) throws FormatException, IOException
	{
		if (in == null) throw new IllegalArgumentException(
			"No data to decompress.");
		final byte[] b = new byte[(int) (in.length() - in.offset())];
		in.read(b);
		return decompress(b, options);
	}

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#width width} {@link CodecOptions#height height}
	 * {@link CodecOptions#bitsPerSample bitsPerSample}
	 * {@link CodecOptions#previousImage previousImage}
	 *
	 * @see Codec#decompress(byte[], CodecOptions)
	 */
	@Override
	public byte[] decompress(final byte[] data, CodecOptions options)
		throws FormatException
	{
		if (options == null) options = CodecOptions.getDefaultOptions();
		if (data == null || data.length == 0) throw new IllegalArgumentException(
			"No data to decompress.");
		int numLines = options.height;

		if (data.length < 8) return options.previousImage;

		final int bpp = options.bitsPerSample / 8;
		final int line = options.width * bpp;

		try {
			final DataHandle<Location> s = dataHandleService.create(new BytesLocation(
				data));

			s.skipBytes(4);

			final int header = s.readShort();
			int off = 0;
			int start = 0;

			final byte[] output = new byte[options.height * line];

			if ((header & 8) == 8) {
				start = s.readShort();
				s.skipBytes(2);
				numLines = s.readShort();
				s.skipBytes(2);

				if (options.previousImage != null) {
					for (int i = 0; i < start; i++) {
						System.arraycopy(options.previousImage, off, output, off, line);
						off += line;
					}
				}

				if (options.previousImage != null) {
					off = line * (start + numLines);
					for (int i = start + numLines; i < options.height; i++) {
						System.arraycopy(options.previousImage, off, output, off, line);
						off += line;
					}
				}
			}
			else throw new FormatException("Unsupported header : " + header);

			// uncompress remaining lines

			int skip = 0; // number of bytes to skip
			byte rle = 0; // RLE code

			int rowPointer = start * line;

			for (int i = 0; i < numLines; i++) {
				skip = s.readUnsignedByte();
				if (skip < 0) skip += 256;

				if (options.previousImage != null) {
					try {
						System.arraycopy(options.previousImage, rowPointer, output,
							rowPointer, (skip - 1) * bpp);
					}
					catch (final ArrayIndexOutOfBoundsException e) {}
				}

				off = rowPointer + ((skip - 1) * bpp);
				while (true) {
					rle = (byte) (s.readUnsignedByte() & 0xff);

					if (rle == 0) {
						skip = s.readUnsignedByte();

						if (options.previousImage != null) {
							try {
								System.arraycopy(options.previousImage, off, output, off,
									(skip - 1) * bpp);
							}
							catch (final ArrayIndexOutOfBoundsException e) {}
						}

						off += (skip - 1) * bpp;
					}
					else if (rle == -1) {
						if (off < (rowPointer + line) && options.previousImage != null) {
							System.arraycopy(options.previousImage, off, output, off,
								rowPointer + line - off);
						}
						break;
					}
					else if (rle < -1) {
						// unpack next pixel and copy it to output -(rle) times
						for (int j = 0; j < (-1 * rle); j++) {
							if (off < output.length) {
								System.arraycopy(data, (int) s.offset(), output, off, bpp);
								off += bpp;
							}
							else break;
						}
						s.skipBytes(bpp);
					}
					else {
						// copy (rle) pixels to output
						int len = rle * bpp;
						if (output.length - off < len) len = output.length - off;
						if (s.length() - s.offset() < len) {
							len = (int) (s.length() - s.offset());
						}
						if (len < 0) len = 0;
						if (off > output.length) off = output.length;
						s.read(output, off, len);
						off += len;
					}
					if (s.offset() >= s.length()) return output;
				}
				rowPointer += line;
			}
			return output;
		}
		catch (final IOException e) {
			throw new FormatException(e);
		}
	}

}
