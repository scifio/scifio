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

package io.scif.formats;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Vector;

import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

import org.scijava.plugin.Plugin;

/**
 * Handler for the GIF file format.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Graphics Interchange Format")
public class GIFFormat extends AbstractFormat {

	// -- Constants --

	public static final String GIF_MAGIC_STRING = "GIF";

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "gif" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		private ColorTable8 cachedTable;

		/** Global color table. */
		private int[] gct;

		/** Active color table. */
		private int[] act;

		/** Interlace flag. */
		private boolean interlace;

		/** Current image rectangle. */
		private int ix, iy, iw, ih;

		/** Current data block. */
		private byte[] dBlock = new byte[256];

		/** Block size. */
		private int blockSize = 0;

		private int dispose = 0;

		private int lastDispose = 0;

		/** Use transparent color. */
		private boolean transparency = false;

		/** Transparent color index. */
		private int transIndex;

		// LZW working arrays
		private short[] prefix;

		private byte[] suffix;

		private byte[] pixelStack;

		private byte[] pixels;

		private Vector<byte[]> images;

		private Vector<int[]> colorTables;

		// -- GIFMetadata getters and setters --

		/**
		 * @return Global color table (raw) for this dataset
		 */
		public int[] getGct() {
			return gct;
		}

		/**
		 * Sets the global color table (raw values) for this dataset
		 */
		public void setGct(final int[] gct) {
			this.gct = gct;
		}

		/**
		 * @return Active color table
		 */
		public int[] getAct() {
			return act;
		}

		/**
		 * Sets the active color table
		 */
		public void setAct(final int[] act) {
			this.act = act;
		}

		public boolean isInterlace() {
			return interlace;
		}

		public void setInterlace(final boolean interlace) {
			this.interlace = interlace;
		}

		public int getIx() {
			return ix;
		}

		public void setIx(final int ix) {
			this.ix = ix;
		}

		public int getIy() {
			return iy;
		}

		public void setIy(final int iy) {
			this.iy = iy;
		}

		public int getIw() {
			return iw;
		}

		public void setIw(final int iw) {
			this.iw = iw;
		}

		public int getIh() {
			return ih;
		}

		public void setIh(final int ih) {
			this.ih = ih;
		}

		public byte[] getdBlock() {
			return dBlock;
		}

		public void setdBlock(final byte[] dBlock) {
			this.dBlock = dBlock;
		}

		public int getBlockSize() {
			return blockSize;
		}

		public void setBlockSize(final int blockSize) {
			this.blockSize = blockSize;
		}

		public int getDispose() {
			return dispose;
		}

		public void setDispose(final int dispose) {
			this.dispose = dispose;
		}

		public int getLastDispose() {
			return lastDispose;
		}

		public void setLastDispose(final int lastDispose) {
			this.lastDispose = lastDispose;
		}

		public boolean isTransparency() {
			return transparency;
		}

		public void setTransparency(final boolean transparency) {
			this.transparency = transparency;
		}

		public int getTransIndex() {
			return transIndex;
		}

		public void setTransIndex(final int transIndex) {
			this.transIndex = transIndex;
		}

		public short[] getPrefix() {
			return prefix;
		}

		public void setPrefix(final short[] prefix) {
			this.prefix = prefix;
		}

		public byte[] getSuffix() {
			return suffix;
		}

		public void setSuffix(final byte[] suffix) {
			this.suffix = suffix;
		}

		public byte[] getPixelStack() {
			return pixelStack;
		}

		public void setPixelStack(final byte[] pixelStack) {
			this.pixelStack = pixelStack;
		}

		public byte[] getPixels() {
			return pixels;
		}

		public void setPixels(final byte[] pixels) {
			this.pixels = pixels;
		}

		public Vector<byte[]> getImages() {
			return images;
		}

		public void setImages(final Vector<byte[]> images) {
			this.images = images;
		}

		public Vector<int[]> getColorTables() {
			return colorTables;
		}

		public void setColorTables(final Vector<int[]> colorTables) {
			this.colorTables = colorTables;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			iMeta.setAxisLength(Axes.CHANNEL, 1);

			iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y, Axes.TIME);
			iMeta.setPlanarAxisCount(3);
			iMeta.setLittleEndian(true);
			iMeta.setMetadataComplete(true);
			iMeta.setIndexed(true);
			iMeta.setFalseColor(false);
			iMeta.setPixelType(FormatTools.UINT8);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			final int length = dBlock.length;
			super.close(fileOnly);
			if (!fileOnly) {
				interlace = transparency = false;
				ix = iy = iw = ih = blockSize = 0;
				dispose = lastDispose = transIndex = 0;
				gct = act;
				prefix = null;
				suffix = pixelStack = pixels = null;
				images = null;
				colorTables = null;
				dBlock = new byte[length];
			}
		}

		// -- HasColorTable API Methods --

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{

			if (cachedTable == null) {
				final byte[][] table = new byte[3][act.length];
				for (int i = 0; i < act.length; i++) {
					table[0][i] = (byte) ((act[i] >> 16) & 0xff);
					table[1][i] = (byte) ((act[i] >> 8) & 0xff);
					table[2][i] = (byte) (act[i] & 0xff);
				}
				cachedTable = new ColorTable8(table);

			}

			return cachedTable;
		}

	}

	public static class Checker extends AbstractChecker {

		// -- Checker API methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream in)
			throws IOException
		{
			final int blockLen = GIF_MAGIC_STRING.length();
			if (!FormatTools.validStream(in, blockLen, false)) return false;
			return in.readString(blockLen).startsWith(GIF_MAGIC_STRING);
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		private static final int IMAGE_SEPARATOR = 0x2c;

		private static final int EXTENSION = 0x21;

		private static final int END = 0x3b;

		private static final int GRAPHICS = 0xf9;

		/** Maximum buffer size. */
		private static final int MAX_STACK_SIZE = 4096;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			log().info("Verifying GIF format");

			stream.order(true);
			meta.setImages(new Vector<byte[]>());
			meta.setColorTables(new Vector<int[]>());

			final String ident = getSource().readString(6);

			if (!ident.startsWith(GIF_MAGIC_STRING)) {
				throw new FormatException("Not a valid GIF file.");
			}

			log().info("Reading dimensions");

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			// Read plane extents
			iMeta.setAxisLength(Axes.X, stream.readShort());
			iMeta.setAxisLength(Axes.Y, stream.readShort());
			iMeta.setAxisLength(Axes.TIME, 0);

			// Check for a global color table, and populate if present.
			int packed = stream.read() & 0xff;
			final boolean gctFlag = (packed & 0x80) != 0;
			final int gctSize = 2 << (packed & 7);
			stream.skipBytes(2);
			meta.getTable().put("Global lookup table size", gctSize);

			if (gctFlag) {
				meta.setGct(readLut(gctSize));
			}

			log().info("Reading data blocks");

			// Reading the GIF metadata. Checks for dispose and transparency
			// flags.
			boolean done = false;
			while (!done) {
				int code = stream.read() & 0xff;
				switch (code) {
					case IMAGE_SEPARATOR:
						readImageBlock();
						break;
					case EXTENSION:
						code = stream.read() & 0xff;
						switch (code) {
							case GRAPHICS:
								stream.skipBytes(1);
								packed = stream.read() & 0xff;
								meta.setDispose((packed & 0x1c) >> 1);
								meta.setTransparency((packed & 1) != 0);
								stream.skipBytes(2);
								meta.setTransIndex(stream.read() & 0xff);
								stream.skipBytes(1);
								break;
							default:
								if (readBlock() == -1) {
									done = true;
									break;
								}
								skipBlocks();
						}
						break;
					case END:
						done = true;
						break;
				}
			}

			meta.setAct(meta.getColorTables().get(0));
		}

		// -- Helper Methods --

		private void skipBlocks() throws IOException {
			int check = 0;
			do {
				check = readBlock();
			}
			while (getMetadata().getBlockSize() > 0 && check != -1);
		}

		private void readImageBlock() throws FormatException, IOException {
			getMetadata().setIx(getSource().readShort());
			getMetadata().setIy(getSource().readShort());
			getMetadata().setIw(getSource().readShort());
			getMetadata().setIh(getSource().readShort());

			final int packed = getSource().read();
			final boolean lctFlag = (packed & 0x80) != 0;
			getMetadata().setInterlace((packed & 0x40) != 0);
			final int lctSize = 2 << (packed & 7);

			getMetadata().setAct(lctFlag ? readLut(lctSize) : getMetadata().getGct());

			if (getMetadata().getAct() == null) throw new FormatException(
				"Color table not found.");

			int save = 0;

			if (getMetadata().isTransparency()) {
				save = getMetadata().getAct()[getMetadata().getTransIndex()];
				getMetadata().getAct()[getMetadata().getTransIndex()] = 0;
			}

			decodeImageData();
			skipBlocks();

			// Update the plane count
			getMetadata().get(0).setAxisLength(Axes.TIME, getMetadata().get(0)
				.getAxisLength(Axes.TIME) + 1);

			if (getMetadata().isTransparency()) getMetadata().getAct()[getMetadata()
				.getTransIndex()] = save;

			getMetadata().setLastDispose(getMetadata().getDispose());
		}

		/** Decodes LZW image data into a pixel array. Adapted from ImageMagick. */
		private void decodeImageData() throws IOException {
			final int nullCode = -1;
			final int npix = getMetadata().getIw() * getMetadata().getIh();

			byte[] pixels = getMetadata().getPixels();

			if (pixels == null || pixels.length < npix) pixels = new byte[npix];

			short[] prefix = getMetadata().getPrefix();
			byte[] suffix = getMetadata().getSuffix();
			byte[] pixelStack = getMetadata().getPixelStack();

			if (prefix == null) prefix = new short[MAX_STACK_SIZE];
			if (suffix == null) suffix = new byte[MAX_STACK_SIZE];
			if (pixelStack == null) pixelStack = new byte[MAX_STACK_SIZE + 1];

			getMetadata().setPrefix(prefix);
			getMetadata().setSuffix(suffix);
			getMetadata().setPixelStack(pixelStack);

			// initialize GIF data stream decoder

			final int dataSize = getSource().read() & 0xff;

			final int clear = 1 << dataSize;
			final int eoi = clear + 1;
			int available = clear + 2;
			int oldCode = nullCode;
			int codeSize = dataSize + 1;
			int codeMask = (1 << codeSize) - 1;
			int code = 0, inCode = 0;
			for (code = 0; code < clear; code++) {
				prefix[code] = 0;
				suffix[code] = (byte) code;
			}

			// decode GIF pixel stream

			int datum = 0, first = 0, top = 0, pi = 0, bi = 0, bits = 0, count = 0;
			int i = 0;

			for (i = 0; i < npix;) {
				if (top == 0) {
					if (bits < codeSize) {
						if (count == 0) {
							count = readBlock();
							if (count <= 0) break;
							bi = 0;
						}
						datum += (getMetadata().getdBlock()[bi] & 0xff) << bits;
						bits += 8;
						bi++;
						count--;
						continue;
					}

					// get the next code
					code = datum & codeMask;
					datum >>= codeSize;
					bits -= codeSize;

					// interpret the code

					if ((code > available) || (code == eoi)) {
						break;
					}
					if (code == clear) {
						// reset the decoder
						codeSize = dataSize + 1;
						codeMask = (1 << codeSize) - 1;
						available = clear + 2;
						oldCode = nullCode;
						continue;
					}

					if (oldCode == nullCode) {
						pixelStack[top++] = suffix[code];
						oldCode = code;
						first = code;
						continue;
					}

					inCode = code;
					if (code == available) {
						pixelStack[top++] = (byte) first;
						code = oldCode;
					}

					while (code > clear) {
						pixelStack[top++] = suffix[code];
						code = prefix[code];
					}
					first = suffix[code] & 0xff;

					if (available >= MAX_STACK_SIZE) break;
					pixelStack[top++] = (byte) first;
					prefix[available] = (short) oldCode;
					suffix[available] = (byte) first;
					available++;

					if (((available & codeMask) == 0) && (available < MAX_STACK_SIZE)) {
						codeSize++;
						codeMask += available;
					}
					oldCode = inCode;
				}
				top--;
				pixels[pi++] = pixelStack[top];
				i++;
			}

			for (i = pi; i < npix; i++)
				pixels[i] = 0;
			getMetadata().setPixels(pixels);
			setPixels();
		}

		private void setPixels() {
			// expose destination image's pixels as an int array
			final byte[] dest = new byte[(int) (getMetadata().get(0).getAxisLength(
				Axes.X) * getMetadata().get(0).getAxisLength(Axes.Y))];
			long lastImage = -1;

			// fill in starting image contents based on last image's dispose
			// code
			if (getMetadata().getLastDispose() > 0) {
				if (getMetadata().getLastDispose() == 3) { // use image before last
					final long n = getMetadata().get(0).getPlaneCount() - 2;
					if (n > 0) lastImage = n - 1;
				}

				if (lastImage != -1) {
					final byte[] prev = getMetadata().getImages().get((int) lastImage);
					System.arraycopy(prev, 0, dest, 0, (int) (getMetadata().get(0)
						.getAxisLength(Axes.X) * getMetadata().get(0).getAxisLength(
							Axes.Y)));
				}
			}

			// copy each source line to the appropriate place in the destination

			int pass = 1;
			int inc = 8;
			int iline = 0;
			for (int i = 0; i < getMetadata().getIh(); i++) {
				int line = i;
				if (getMetadata().isInterlace()) {
					if (iline >= getMetadata().getIh()) {
						pass++;
						switch (pass) {
							case 2:
								iline = 4;
								break;
							case 3:
								iline = 2;
								inc = 4;
								break;
							case 4:
								iline = 1;
								inc = 2;
								break;
						}
					}
					line = iline;
					iline += inc;
				}
				line += getMetadata().getIy();
				if (line < getMetadata().get(0).getAxisLength(Axes.Y)) {
					final int k = line * (int) getMetadata().get(0).getAxisLength(Axes.X);
					int dx = k + getMetadata().getIx(); // start of line in dest
					int dlim = dx + getMetadata().getIw(); // end of dest line
					if ((k + getMetadata().get(0).getAxisLength(Axes.X)) < dlim) dlim =
						k + (int) getMetadata().get(0).getAxisLength(Axes.X);
					int sx = i * getMetadata().getIw(); // start of line in
					// source
					while (dx < dlim) {
						// map color and insert in destination
						final int index = getMetadata().getPixels()[sx++] & 0xff;
						dest[dx++] = (byte) index;
					}
				}
			}
			getMetadata().getColorTables().add(getMetadata().getAct());
			getMetadata().getImages().add(dest);
		}

		/** Reads the next variable length block. */
		private int readBlock() throws IOException {
			if (getSource().getFilePointer() == getSource().length()) return -1;
			getMetadata().setBlockSize(getSource().read() & 0xff);
			int n = 0;
			int count;

			if (getMetadata().getBlockSize() > 0) {
				try {
					while (n < getMetadata().getBlockSize()) {
						count = getSource().read(getMetadata().getdBlock(), n, getMetadata()
							.getBlockSize() - n);
						if (count == -1) break;
						n += count;
					}
				}
				catch (final IOException e) {
					log().trace("Truncated block", e);
				}
			}
			return n;
		}

		/** Read a color lookup table of the specified size. */
		private int[] readLut(final int size) throws FormatException {
			final int nbytes = 3 * size;
			final byte[] c = new byte[nbytes];
			int n = 0;
			try {
				n = getSource().read(c);
			}
			catch (final IOException e) {}

			if (n < nbytes) {
				throw new FormatException("Color table not found");
			}

			final int[] lut = new int[256];
			int j = 0;
			for (int i = 0; i < size; i++) {
				final int r = c[j++] & 0xff;
				final int g = c[j++] & 0xff;
				final int b = c[j++] & 0xff;
				lut[i] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
			return lut;
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();
			final int xIndex = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yIndex = meta.get(imageIndex).getAxisIndex(Axes.Y);
			plane.setColorTable(meta.getColorTable(0, 0));
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);
			final int x = (int) bounds.min(xIndex), y = (int) bounds.min(yIndex), //
					w = (int) bounds.dimension(xIndex), h = (int) bounds.dimension(
						yIndex);
			final int[] act = meta.getColorTables().get((int) planeIndex);

			final byte[] b = meta.getImages().get((int) planeIndex);
			if (planeIndex > 0 && meta.isTransparency()) {
				final byte[] prev = meta.getImages().get((int) planeIndex - 1);
				int idx = meta.getTransIndex();
				if (idx >= 127) idx = 0;
				for (int i = 0; i < b.length; i++) {
					if ((act[b[i] & 0xff] & 0xffffff) == idx) {
						b[i] = prev[i];
					}
				}
				meta.getImages().setElementAt(b, (int) planeIndex);
			}

			for (int row = 0; row < h; row++) {
				System.arraycopy(b, (row + y) * (int) meta.get(imageIndex)
					.getAxisLength(Axes.X) + x, buf, row * w, w);
			}

			return plane;
		}
	}
}
