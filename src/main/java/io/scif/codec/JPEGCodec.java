/*
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

import io.scif.FormatException;
import io.scif.gui.AWTImageTools;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleInputStream;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * This class implements JPEG compression and decompression.
 */
@Plugin(type = Codec.class)
public class JPEGCodec extends AbstractCodec {

	@Parameter
	private CodecService codecService;

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#width width} {@link CodecOptions#height height}
	 * {@link CodecOptions#channels channels} {@link CodecOptions#bitsPerSample
	 * bitsPerSample} {@link CodecOptions#interleaved interleaved}
	 * {@link CodecOptions#littleEndian littleEndian} {@link CodecOptions#signed
	 * signed}
	 *
	 * @see Codec#compress(byte[], CodecOptions)
	 */
	@Override
	public byte[] compress(final byte[] data, CodecOptions options)
		throws FormatException
	{
		if (data == null || data.length == 0) return data;
		if (options == null) options = CodecOptions.getDefaultOptions();

		if (options.bitsPerSample > 8) {
			throw new FormatException("> 8 bit data cannot be compressed with JPEG.");
		}

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final BufferedImage img = AWTImageTools.makeImage(data, options.width,
			options.height, options.channels, options.interleaved,
			options.bitsPerSample / 8, false, options.littleEndian, options.signed);

		try {
			ImageIO.write(img, "jpeg", out);
		}
		catch (final IOException e) {
			throw new FormatException("Could not write JPEG data", e);
		}
		return out.toByteArray();
	}

	/**
	 * The CodecOptions parameter should have the following fields set:
	 * {@link CodecOptions#interleaved interleaved}
	 * {@link CodecOptions#littleEndian littleEndian}
	 *
	 * @see Codec#decompress(DataHandle, CodecOptions)
	 */
	@Override
	public byte[] decompress(final DataHandle<Location> in, CodecOptions options)
		throws FormatException, IOException
	{
		final boolean ycbcr = options != null && options.ycbcr;
		BufferedImage b = null;
		WritableRaster stored = null;
		int nextByte;
		final long offset = in.offset();
		try {
			try {
				do {
					nextByte = in.read();
					if (nextByte == -1)
					{
						throw new EOFException();
					}
				} while(nextByte != (byte) 0xff || nextByte != (byte) 0xd8);
				in.seek(in.offset() - 2);
			}
			catch (final EOFException e) {
				in.seek(offset);
			}

			final InputStream s = new BufferedInputStream(
				new DataHandleInputStream<>(in), 8192);
			// ImageIO converts YCbCr to RGB on its own whenever it judges the data
			// to be YCbCr, so read the stored samples and convert them here.
			if (ycbcr) stored = readStoredSamples(s);
			else b = ImageIO.read(s);
		}
		catch (final IOException exc) {
			// probably a lossless JPEG; delegate to LosslessJPEGCodec
			in.seek(offset);
			final Codec codec = codecService.getCodec(LosslessJPEGCodec.class);
			return codec.decompress(in, options);
		}

		if (options == null) options = CodecOptions.getDefaultOptions();

		final byte[][] buf = ycbcr ? AWTImageTools.getPixelBytes(stored,
			options.littleEndian) : AWTImageTools.getPixelBytes(b,
				options.littleEndian);

		// correct for YCbCr encoding, if necessary
		if (ycbcr && buf.length == 3) {
			final int nBytes = buf[0].length / (stored.getWidth() * stored
				.getHeight());
			final int mask = (int) (Math.pow(2, nBytes * 8) - 1);
			for (int i = 0; i < buf[0].length; i += nBytes) {
				final int y = Bytes.toInt(buf[0], i, nBytes, options.littleEndian);
				final int cb = Bytes.toInt(buf[1], i, nBytes, options.littleEndian) -
					128;
				final int cr = Bytes.toInt(buf[2], i, nBytes, options.littleEndian) -
					128;

				final int red = clamp(y + 1.402 * cr, mask);
				final int green = clamp(y - 0.34414 * cb - 0.71414 * cr, mask);
				final int blue = clamp(y + 1.772 * cb, mask);

				Bytes.unpack(red, buf[0], i, nBytes, options.littleEndian);
				Bytes.unpack(green, buf[1], i, nBytes, options.littleEndian);
				Bytes.unpack(blue, buf[2], i, nBytes, options.littleEndian);
			}
		}

		byte[] rtn = new byte[buf.length * buf[0].length];
		if (buf.length == 1) rtn = buf[0];
		else {
			if (options.interleaved) {
				int next = 0;
				for (int i = 0; i < buf[0].length; i++) {
					for (int j = 0; j < buf.length; j++) {
						rtn[next++] = buf[j][i];
					}
				}
			}
			else {
				for (int i = 0; i < buf.length; i++) {
					System.arraycopy(buf[i], 0, rtn, i * buf[0].length, buf[i].length);
				}
			}
		}
		return rtn;
	}

	/**
	 * Reads the samples as stored in the JPEG stream, without the colour
	 * conversion {@link ImageIO#read(InputStream)} would apply. Returns null if
	 * no ImageIO reader recognizes the stream.
	 */
	private static WritableRaster readStoredSamples(final InputStream s)
		throws IOException
	{
		try (ImageInputStream iis = ImageIO.createImageInputStream(s)) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext()) return null;
			final ImageReader reader = readers.next();
			try {
				reader.setInput(iis);
				final Raster r = reader.readRaster(0, null);
				final WritableRaster w = r.createCompatibleWritableRaster();
				w.setRect(r);
				return w;
			}
			finally {
				reader.dispose();
			}
		}
	}

	private static int clamp(final double value, final int max) {
		return (int) Math.max(0, Math.min(max, Math.round(value)));
	}
}
