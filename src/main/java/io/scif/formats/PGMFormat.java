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
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.StringTokenizer;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handler plugin for the PGM file format.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Portable Gray Map")
public class PGMFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "pgm" };
	}

	// -- Nested classes --

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

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			iMeta.setPlanarAxisCount(iMeta.getAxisLength(Axes.CHANNEL) == 3 ? 3 : 2);
			iMeta.setLittleEndian(false);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				setRawBits(false);
				setOffset(0);
			}
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final char PGM_MAGIC_CHAR = 'P';

		// -- Checker API Methods --

		@Override
		public boolean suffixNecessary() {
			return false;
		}

		@Override
		public boolean isFormat(final DataHandle<Location> stream)
			throws IOException
		{
			final int blockLen = 2;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			return stream.read() == PGM_MAGIC_CHAR && Character.isDigit((char) stream
				.read());
		}

	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			long height = -1;
			long width = -1;
			int max = -1;
			String magic = null;
			boolean isBlackAndWhite = false;
			int varsRead = 0;
			int numVars = 4;

			while (varsRead < numVars) {
				String line = stream.readLine();
				if (line == null) throw new FormatException(
					"Read entire file without finding complete PGM metadata.");
				// Truncate comments
				if (line.contains("#")) line = line.substring(0, line.indexOf("#"));
				// Metadata should only be numeric, or potentially a key
				// including a "P"
				line = line.replaceAll("[^P0-9]", " ");
				final String[] vars = line.split(" ");

				// Populate the appropriate metadata fields for this line
				for (final String var : vars) {
					varsRead++;
					switch (varsRead) {
						case 1:
							magic = var;
							if (magic.equals("P1") || magic.equals("P4")) {
								numVars = 3;
								isBlackAndWhite = true;
							}
							break;
						case 2:
							width = Integer.parseInt(var);
							break;
						case 3:
							height = Integer.parseInt(var);
							break;
						case 4:
							max = Integer.parseInt(var);
							break;
					}
				}
			}

			// Validate the metadata we found
			if (magic == null || height == -1 || width == -1 || (!isBlackAndWhite &&
				max == -1))
			{
				throw new FormatException(
					"Incomplete PGM metadata found. Read the following metadata: magic = " +
						magic + "; height = " + height + "; width = " + width + "; max = " +
						max);
			}

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			// Populate the image metadata
			iMeta.setAxisLength(Axes.X, width);
			iMeta.setAxisLength(Axes.Y, height);

			meta.setRawBits(magic.equals("P4") || magic.equals("P5") || magic.equals(
				"P6"));

			iMeta.setAxisLength(Axes.CHANNEL, (magic.equals("P3") || magic.equals(
				"P6")) ? 3 : 1);

			if (!isBlackAndWhite) {
				if (max > 255) iMeta.setPixelType(FormatTools.UINT16);
				else iMeta.setPixelType(FormatTools.UINT8);
			}

			meta.setOffset(stream.offset());

			meta.getTable().put("Black and white", isBlackAndWhite);
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private DataHandleService dataHandleService;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);

			getHandle().seek(meta.getOffset());
			if (meta.isRawBits()) {
				readPlane(getHandle(), imageIndex, bounds, plane);
			}
			else {
				try (DataHandle<Location> bytes = dataHandleService.create(
					new BytesLocation(0))) // NB: a size of 0 means the handle will grow as needed
				{

					final boolean littleEndian = meta.get(imageIndex).isLittleEndian();
					bytes.setOrder(littleEndian ? ByteOrder.LITTLE_ENDIAN
						: ByteOrder.BIG_ENDIAN);

					while (getHandle().offset() < getHandle().length()) {
						String line = getHandle().readLine().trim();
						line = line.replaceAll("[^0-9]", " ");
						final StringTokenizer t = new StringTokenizer(line, " ");
						while (t.hasMoreTokens()) {
							final int q = Integer.parseInt(t.nextToken().trim());
							if (meta.get(imageIndex).getPixelType() == FormatTools.UINT16) {
								bytes.writeShort(q);
							}
							else bytes.writeByte(q);
						}
					}

					bytes.seek(0);
					readPlane(bytes, imageIndex, bounds, plane);
				}
			}
			return plane;
		}
	}
}
