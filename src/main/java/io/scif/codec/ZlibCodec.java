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

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleInputStream;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * This class implements ZLIB decompression.
 *
 * @author Melissa Linkert
 */
@Plugin(type = Codec.class)
public class ZlibCodec extends AbstractCodec {

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		if (data == null || data.length == 0) throw new IllegalArgumentException(
			"No data to compress");
		final Deflater deflater = new Deflater();
		deflater.setInput(data);
		deflater.finish();
		final byte[] buf = new byte[8192];
		final ByteVector bytes = new ByteVector();
		int r = 0;
		// compress until eof reached
		while ((r = deflater.deflate(buf, 0, buf.length)) > 0) {
			bytes.add(buf, 0, r);
		}
		return bytes.toByteArray();
	}

	@Override
	public byte[] decompress(final DataHandle<Location> in,
		final CodecOptions options) throws FormatException, IOException
	{
		final InflaterInputStream i = new InflaterInputStream(
			new DataHandleInputStream<>(in));
		final ByteVector bytes = new ByteVector();
		final byte[] buf = new byte[8192];
		int r = 0;
		// read until eof reached
		try {
			while ((r = i.read(buf, 0, buf.length)) > 0)
				bytes.add(buf, 0, r);
		}
		catch (final EOFException e) {}
		return bytes.toByteArray();
	}

}
