/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
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
import io.scif.ImageMetadata;
import io.scif.io.ByteArrayHandle;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.StringTokenizer;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class PGMFormat extends AbstractFormat {

	// -- Format API Methods --

	/*
	 * @see io.scif.Format#getFormatName()
	 */
	public String getFormatName() {
		return "Portable Gray Map";
	}

	/*
	 * @see io.scif.Format#getSuffixes()
	 */
	public String[] getSuffixes() {
		return new String[] { "pgm" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		private boolean rawBits;

		/** Offset to pixel data. */
		private long offset;

		// -- PGMMetadata getters and setters --

		public boolean isRawBits() {
			return rawBits;
		}

		public void setRawBits(final boolean rawBits) {
			this.rawBits = rawBits;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(final long offset) {
			this.offset = offset;
		}

		// -- Metadata API Methods --

		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);
			iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));

			iMeta.setRGB(iMeta.getAxisLength(Axes.CHANNEL) == 3);
			iMeta.setAxisLength(Axes.Z, 1);
			iMeta.setAxisLength(Axes.TIME, 1);
			iMeta.setLittleEndian(true);
			iMeta.setInterleaved(false);
			iMeta.setPlaneCount(1);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		/* @see loci.formats.IFormatReader#close(boolean) */
		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				setRawBits(false);
				setOffset(0);
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final char PGM_MAGIC_CHAR = 'P';

		// -- Constructor --

		public Checker() {
			suffixNecessary = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 2;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			return stream.read() == PGM_MAGIC_CHAR &&
				Character.isDigit((char) stream.read());
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			final String magic = stream.readLine().trim();

			boolean isBlackAndWhite = false;

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			String line = readNextLine();

			line = line.replaceAll("[^0-9]", " ");
			final int space = line.indexOf(" ");
			iMeta.setAxisLength(Axes.X, Integer.parseInt(line.substring(0, space)
				.trim()));
			iMeta.setAxisLength(Axes.Y, Integer.parseInt(line.substring(space + 1)
				.trim()));

			meta.setRawBits(magic.equals("P4") || magic.equals("P5") ||
				magic.equals("P6"));

			iMeta.setAxisLength(Axes.CHANNEL, (magic.equals("P3") || magic
				.equals("P6")) ? 3 : 1);
			isBlackAndWhite = magic.equals("P1") || magic.equals("P4");

			if (!isBlackAndWhite) {
				final int max = Integer.parseInt(readNextLine());
				if (max > 255) iMeta.setPixelType(FormatTools.UINT16);
				else iMeta.setPixelType(FormatTools.UINT8);
			}

			meta.setOffset(stream.getFilePointer());

			addGlobalMeta("Black and white", isBlackAndWhite);
		}

		// -- Helper Methods --

		private String readNextLine() throws IOException {
			String line = in.readLine().trim();
			while (line.startsWith("#") || line.length() == 0) {
				line = in.readLine().trim();
			}
			return line;
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		/*
		 * @see io.scif.Reader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
		 */
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneParameters(this, imageIndex, planeIndex,
				buf.length, x, y, w, h);

			getStream().seek(meta.getOffset());
			if (meta.isRawBits()) {
				readPlane(getStream(), imageIndex, x, y, w, h, plane);
			}
			else {
				final ByteArrayHandle handle = new ByteArrayHandle();
				final RandomAccessOutputStream out =
					new RandomAccessOutputStream(handle);
				out.order(meta.isLittleEndian(imageIndex));

				while (getStream().getFilePointer() < getStream().length()) {
					String line = getStream().readLine().trim();
					line = line.replaceAll("[^0-9]", " ");
					final StringTokenizer t = new StringTokenizer(line, " ");
					while (t.hasMoreTokens()) {
						final int q = Integer.parseInt(t.nextToken().trim());
						if (meta.getPixelType(imageIndex) == FormatTools.UINT16) {
							out.writeShort(q);
						}
						else out.writeByte(q);
					}
				}

				out.close();
				final RandomAccessInputStream s =
					new RandomAccessInputStream(getContext(), handle);
				s.seek(0);
				readPlane(s, imageIndex, x, y, w, h, plane);
				s.close();
			}

			return plane;
		}
	}
}
