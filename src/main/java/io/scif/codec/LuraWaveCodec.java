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

import io.scif.DependencyException;
import io.scif.FormatException;
import io.scif.MissingLibraryException;
import io.scif.UnsupportedCompressionException;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.LuraWaveService;
import io.scif.services.ServiceException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * This class provides LuraWave decompression, using LuraWave's Java decoding
 * library. Compression is not supported. Decompression requires a LuraWave
 * license code, specified in the lurawave.license system property (e.g.,
 * {@code -Dlurawave.license=XXXX} on the command line).
 *
 * @author Curtis Rueden
 */
@Plugin(type = Codec.class)
public class LuraWaveCodec extends AbstractCodec {

	// -- Fields --

	@Parameter(required = false)
	private LuraWaveService luraWaveService;

	// -- Codec API methods --

	@Override
	public byte[] compress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		throw new UnsupportedCompressionException(
			"LuraWave compression not supported");
	}

	@Override
	public byte[] decompress(final RandomAccessInputStream in,
		final CodecOptions options) throws FormatException, IOException
	{
		final byte[] buf = new byte[(int) in.length()];
		in.read(buf);
		return decompress(buf, options);
	}

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#maxBytes maxBytes}
	 *
	 * @see Codec#decompress(byte[], CodecOptions)
	 */
	@Override
	public byte[] decompress(final byte[] buf, final CodecOptions options)
		throws FormatException
	{
		checkLuraWaveService();

		final BufferedInputStream stream = new BufferedInputStream(
			new ByteArrayInputStream(buf), 4096);
		try {
			luraWaveService.initialize(stream);
		}
		catch (final DependencyException e) {
			throw new FormatException(LuraWaveService.NO_LICENSE_MSG, e);
		}
		catch (final ServiceException e) {
			throw new FormatException(LuraWaveService.INVALID_LICENSE_MSG, e);
		}
		catch (final IOException e) {
			throw new FormatException(e);
		}

		final int w = luraWaveService.getWidth();
		final int h = luraWaveService.getHeight();

		final int nbits = 8 * (options.maxBytes / (w * h));

		if (nbits == 8) {
			final byte[] image8 = new byte[w * h];
			try {
				luraWaveService.decodeToMemoryGray8(image8, -1, 1024, 0);
			}
			catch (final ServiceException e) {
				throw new FormatException(LuraWaveService.INVALID_LICENSE_MSG, e);
			}
			return image8;
		}
		else if (nbits == 16) {
			final short[] image16 = new short[w * h];
			try {
				luraWaveService.decodeToMemoryGray16(image16, 0, -1, 1024, 0, 1, w, 0,
					0, w, h);
			}
			catch (final ServiceException e) {
				throw new FormatException(LuraWaveService.INVALID_LICENSE_MSG, e);
			}

			final byte[] output = new byte[w * h * 2];
			for (int i = 0; i < image16.length; i++) {
				Bytes.unpack(image16[i], output, i * 2, 2, true);
			}
			return output;
		}

		throw new FormatException("Unsupported bits per pixel: " + nbits);
	}

	// -- Helper methods --

	/**
	 * Checks the LuraWave service, throwing an exception if it is not available.
	 *
	 * @throws FormatException If the LuraWave service is unavailable.
	 */
	private void checkLuraWaveService() throws FormatException {
		if (luraWaveService != null) return;
		throw new MissingLibraryException(LuraWaveService.NO_LURAWAVE_MSG);
	}

}
