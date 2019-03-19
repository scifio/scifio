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

package io.scif.formats.tiff;

import io.scif.FormatException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.Bytes;

/**
 * Default service for working with TIFF files.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultTiffService extends AbstractService implements TiffService {

	@Parameter
	private LogService log;

	@Override
	public void difference(final byte[] input, final IFD ifd)
		throws FormatException
	{
		final int predictor = ifd.getIFDIntValue(IFD.PREDICTOR, 1);
		if (predictor == 2) {
			log.debug("performing horizontal differencing");
			final int[] bitsPerSample = ifd.getBitsPerSample();
			final long width = ifd.getImageWidth();
			final boolean little = ifd.isLittleEndian();
			final int planarConfig = ifd.getPlanarConfiguration();
			final int bytes = ifd.getBytesPerSample()[0];
			final int len = bytes * (planarConfig == 2 ? 1 : bitsPerSample.length);

			for (int b = input.length - bytes; b >= 0; b -= bytes) {
				if (b / len % width == 0) continue;
				int value = Bytes.toInt(input, b, bytes, little);
				value -= Bytes.toInt(input, b - len, bytes, little);
				Bytes.unpack(value, input, b, bytes, little);
			}
		}
		else if (predictor != 1) {
			throw new FormatException("Unknown Predictor (" + predictor + ")");
		}
	}

	@Override
	public void undifference(final byte[] input, final IFD ifd)
		throws FormatException
	{
		final int predictor = ifd.getIFDIntValue(IFD.PREDICTOR, 1);
		if (predictor == 2) {
			log.debug("reversing horizontal differencing");
			final int[] bitsPerSample = ifd.getBitsPerSample();
			int len = bitsPerSample.length;
			final long width = ifd.getImageWidth();
			final boolean little = ifd.isLittleEndian();
			final int planarConfig = ifd.getPlanarConfiguration();

			final int bytes = ifd.getBytesPerSample()[0];

			if (planarConfig == 2 || bitsPerSample[len - 1] == 0) len = 1;
			len *= bytes;

			for (int b = 0; b <= input.length - bytes; b += bytes) {
				if (b / len % width == 0) continue;
				int value = Bytes.toInt(input, b, bytes, little);
				value += Bytes.toInt(input, b - len, bytes, little);
				Bytes.unpack(value, input, b, bytes, little);
			}
		}
		else if (predictor != 1) {
			throw new FormatException("Unknown Predictor (" + predictor + ")");
		}
	}

}
