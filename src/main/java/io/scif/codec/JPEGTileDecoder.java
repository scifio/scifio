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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.Hashtable;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.util.IntRect;

/**
 * TODO
 *
 * @author Melissa Linkert
 */
public class JPEGTileDecoder extends AbstractContextual {

	// -- Fields --

	@Parameter
	private LogService log;

	private TileConsumer consumer;

	private TileCache tiles;

	private DataHandle<Location> in;

	public JPEGTileDecoder(final Context ctx) {
		setContext(ctx);
	}

	// -- JPEGTileDecoder API methods --

	public void initialize(final DataHandle<Location> handle,
		final int imageWidth)
	{
		initialize(handle, 0, imageWidth);
	}

	public void initialize(final DataHandle<Location> handle, final int y,
		final int h)
	{
		this.in = handle;
		tiles = new TileCache(getContext(), y, h);

		// pre-process the stream to make sure that the
		// image width and height are non-zero

		try {
			final long fp = in.offset();
			final boolean littleEndian = in.isLittleEndian();
			in.setOrder(ByteOrder.BIG_ENDIAN);

			while (in.offset() < in.length() - 1) {
				final int code = in.readShort() & 0xffff;
				final int length = in.readShort() & 0xffff;
				final long offset = in.offset();
				if (length > 0xff00 || code < 0xff00) {
					in.seek(offset - 3);
					continue;
				}
				if (code == 0xffc0) {
					in.skipBytes(1);
					final int height = in.readShort() & 0xffff;
					final int width = in.readShort() & 0xffff;
					if (height == 0 || width == 0) {
						throw new RuntimeException(
							"Width or height > 65500 is not supported.");
					}
					break;
				}
				else if (offset + length - 2 < in.length()) {
					in.seek(offset + length - 2);
				}
				else {
					break;
				}
			}

			in.seek(fp);
			in.setLittleEndian(littleEndian);
		}
		catch (final IOException e) {}

		try {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			// read remaining bytes, potentially wrong!
			final byte[] data = new byte[(int) (in.length() - in.offset())];
			in.readFully(data);
			final Image image = toolkit.createImage(data);
			final ImageProducer producer = image.getSource();

			consumer = new TileConsumer(producer, y, h);
			producer.startProduction(consumer);
			while (producer.isConsumer(consumer)) { /* Loop over image consumers */}
		}
		catch (final IOException e) {
			log.error("Could not read JPEGTile: " + e);
		}
	}

	public byte[] getScanline(final int y) {
		try {
			return tiles.get(0, y, consumer.getWidth(), 1);
		}
		catch (final FormatException e) {
			log.debug("", e);
		}
		return null;
	}

	public int getWidth() {
		return consumer.getWidth();
	}

	public int getHeight() {
		return consumer.getHeight();
	}

	public void close() {
		try {
			if (in != null) {
				in.close();
			}
		}
		catch (final IOException e) {
			log.debug("", e);
		}
		tiles = null;
		consumer = null;
	}

	// -- Helper classes --

	class TileConsumer implements ImageConsumer {

		private int width, height;

		private final ImageProducer producer;

		private int yy = 0, hh = 0;

		public TileConsumer(final ImageProducer producer) {
			this.producer = producer;
		}

		public TileConsumer(final ImageProducer producer, final int y,
			final int h)
		{
			this(producer);
			this.yy = y;
			this.hh = h;
		}

		// -- TileConsumer API methods --

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		// -- ImageConsumer API methods --

		@Override
		public void imageComplete(final int status) {
			producer.removeConsumer(this);
		}

		@Override
		public void setDimensions(final int width, final int height) {
			this.width = width;
			this.height = height;
			if (hh <= 0) hh = height;
		}

		@Override
		public void setPixels(final int x, final int y, final int w, final int h,
			final ColorModel model, final byte[] pixels, final int off,
			final int scanSize)
		{
			final double percent = ((double) y / height) * 100.0;
			log.debug("Storing row " + y + " of " + height + " (" + percent + "%)");
			if (y >= (yy + hh)) {
				imageComplete(0);
				return;
			}
			else if (y < yy) return;
			try {
				tiles.add(pixels, x, y, w);
			}
			catch (final FormatException e) {
				log.debug("", e);
			}
		}

		@Override
		public void setPixels(final int x, final int y, final int w, final int h,
			final ColorModel model, final int[] pixels, final int off,
			final int scanSize)
		{
			final double percent = ((double) y / (yy + hh)) * 100.0;
			log.debug("Storing row " + y + " of " + (yy + hh) + " (" + percent +
				"%)");
			if (y >= (yy + hh)) {
				imageComplete(0);
				return;
			}
			else if (y < yy) return;
			try {
				tiles.add(pixels, x, y, w);
			}
			catch (final FormatException e) {
				log.debug("", e);
			}
		}

		@Override
		public void setProperties(final Hashtable<?, ?> props) {}

		@Override
		public void setColorModel(final ColorModel model) {}

		@Override
		public void setHints(final int hintFlags) {}
	}

	class TileCache {

		private static final int ROW_COUNT = 128;

		private final Hashtable<IntRect, byte[]> compressedTiles =
			new Hashtable<>();

		private final JPEGCodec codec;

		private final CodecOptions options = new CodecOptions();

		private final ByteVector toCompress = new ByteVector();

		@Parameter
		private CodecService codecService;

		private int row = 0;

		private IntRect lastRegion = null;

		private byte[] lastTile = null;

		private int yy = 0, hh = 0;

		public TileCache(final Context ctx, final int yy, final int hh) {
			ctx.inject(this);
			options.interleaved = true;
			options.littleEndian = false;
			this.yy = yy;
			this.hh = hh;
			codec = codecService.getCodec(JPEGCodec.class);
		}

		public void add(final byte[] pixels, final int x, final int y, final int w)
			throws FormatException
		{
			toCompress.add(pixels);
			row++;

			if ((y % ROW_COUNT) == ROW_COUNT - 1 || y == getHeight() - 1 || y == yy +
				hh - 1)
			{
				final IntRect r = new IntRect(x, y - row + 1, w, row);
				options.width = w;
				options.height = row;
				options.channels = 1;
				options.bitsPerSample = 8;
				options.signed = false;

				final byte[] compressed = codec.compress(toCompress.toByteArray(),
					options);
				compressedTiles.put(r, compressed);
				toCompress.clear();
			}
		}

		public void add(final int[] pixels, final int x, final int y, final int w)
			throws FormatException
		{
			final byte[] buf = new byte[pixels.length * 3];
			for (int i = 0; i < pixels.length; i++) {
				buf[i * 3] = (byte) ((pixels[i] & 0xff0000) >> 16);
				buf[i * 3 + 1] = (byte) ((pixels[i] & 0xff00) >> 8);
				buf[i * 3 + 2] = (byte) (pixels[i] & 0xff);
			}

			toCompress.add(buf);
			row++;

			if ((y % ROW_COUNT) == ROW_COUNT - 1 || y == getHeight() - 1 || y == yy +
				hh - 1)
			{
				final IntRect r = new IntRect(x, y - row + 1, w, row);
				options.width = w;
				options.height = row;
				options.channels = 3;
				options.bitsPerSample = 8;
				options.signed = false;

				final byte[] compressed = codec.compress(toCompress.toByteArray(),
					options);
				compressedTiles.put(r, compressed);
				toCompress.clear();
				row = 0;
			}
		}

		public byte[] get(final int x, final int y, final int w, final int h)
			throws FormatException
		{
			final IntRect[] keys = compressedTiles.keySet().toArray(new IntRect[0]);
			IntRect r = new IntRect(x, y, w, h);
			for (final IntRect key : keys) {
				if (key.intersects(r)) {
					r = key;
				}
			}
			if (!r.equals(lastRegion)) {
				lastRegion = r;
				byte[] compressed = null;
				compressed = compressedTiles.get(r);
				if (compressed == null) return null;
				lastTile = codec.decompress(compressed, options);
			}

			final int pixel = options.channels * (options.bitsPerSample / 8);
			final byte[] buf = new byte[w * h * pixel];

			for (int i = 0; i < h; i++) {
				System.arraycopy(lastTile, r.width * pixel * (i + y - r.y) + (x - r.x),
					buf, i * w * pixel, pixel * w);
			}

			return buf;
		}
	}

}
