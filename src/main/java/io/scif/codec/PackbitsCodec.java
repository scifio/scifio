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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * This class implements packbits decompression. Compression is not yet
 * implemented.
 *
 * @author Melissa Linkert
 */
@Plugin(type = Codec.class)
public class PackbitsCodec extends AbstractCodec {

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		// TODO: Add compression support.
		throw new UnsupportedCompressionException(
			"Packbits Compression not currently supported");
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
		if (options == null) options = CodecOptions.getDefaultOptions();
		if (in == null) throw new IllegalArgumentException(
			"No data to decompress.");
		final long fp = in.offset();
		// Adapted from the TIFF 6.0 specification, page 42.
		final ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
		int nread = 0;
		while (output.size() < options.maxBytes) {
			final byte n = (byte) (in.read() & 0xff);
			nread++;
			if (n >= 0) { // 0 <= n <= 127
				byte[] b = new byte[n + 1];
				in.read(b);
				nread += n + 1;
				output.write(b);
				b = null;
			}
			else if (n != -128) { // -127 <= n <= -1
				final int len = -n + 1;
				final byte inp = (byte) (in.read() & 0xff);
				nread++;
				for (int i = 0; i < len; i++)
					output.write(inp);
			}
		}
		if (fp + nread < in.length()) in.seek(fp + nread);
		return output.toByteArray();
	}
}
