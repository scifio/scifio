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

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;

/**
 * FitsReader is the file format reader for Flexible Image Transport System
 * (FITS) images. Much of this code was adapted from
 * <a href="http://imagej.net/">ImageJ</a>.
 *
 * @author Mark Hiner
 * @author Melissa Linkert
 */
@Plugin(type = Format.class, name = "Flexible Image Transport System")
public class FITSFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "fits", "fts" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		private long pixelOffset;

		// -- FITS Metadata getters and setters --

		public long getPixelOffset() {
			return pixelOffset;
		}

		public void setPixelOffset(final long pixelOffset) {
			this.pixelOffset = pixelOffset;
		}

		// -- Metadata API methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			if (iMeta.getAxisLength(Axes.Z) == 0) iMeta.setAxisLength(Axes.Z, 1);

			// correct for truncated files
			final int planeSize = (int) iMeta.getAxisLength(Axes.X) * (int) iMeta
				.getAxisLength(Axes.Y) * FormatTools.getBytesPerPixel(iMeta
					.getPixelType());

			try {
				if (ArrayUtils.safeMultiply64(planeSize, iMeta.getAxisLength(
					Axes.Z)) > (getSource().length() - pixelOffset))
				{
					iMeta.setAxisLength(Axes.Z, (int) ((getSource().length() -
						pixelOffset) / planeSize));
				}
			}
			catch (final IOException e) {
				log().error("Failed to determine input stream length", e);
			}

			iMeta.setAxisTypes(Axes.X, Axes.Y, Axes.Z);
			iMeta.setPlanarAxisCount(2);
			iMeta.setLittleEndian(false);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close() {
			pixelOffset = 0;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		private static final int LINE_LENGTH = 80;

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			String line = getSource().readString(LINE_LENGTH);
			if (!line.startsWith("SIMPLE")) {
				throw new FormatException("Unsupported FITS file.");
			}

			String key = "", value = "";
			while (true) {
				line = getSource().readString(LINE_LENGTH);

				// parse key/value pair
				final int ndx = line.indexOf("=");
				int comment = line.indexOf("/", ndx);
				if (comment < 0) comment = line.length();

				if (ndx >= 0) {
					key = line.substring(0, ndx).trim();
					value = line.substring(ndx + 1, comment).trim();
				}
				else key = line.trim();

				// if the file has an extended header, "END" will appear twice
				// the first time marks the end of the extended header
				// the second time marks the end of the standard header
				// image dimensions are only populated by the standard header
				if (key.equals("END") && iMeta.getAxisLength(Axes.X) > 0) break;

				if (key.equals("BITPIX")) {
					final int bits = Integer.parseInt(value);
					final boolean fp = bits < 0;
					final boolean signed = bits != 8;
					final int bytes = Math.abs(bits) / 8;
					iMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed, fp));
					iMeta.setBitsPerPixel(Math.abs(bits));
				}
				else if (key.equals("NAXIS1")) iMeta.setAxisLength(Axes.X, Integer
					.parseInt(value));
				else if (key.equals("NAXIS2")) iMeta.setAxisLength(Axes.Y, Integer
					.parseInt(value));
				else if (key.equals("NAXIS3")) iMeta.setAxisLength(Axes.Z, Integer
					.parseInt(value));

				meta.getTable().put(key, value);
			}
			while (getSource().read() == 0x20) { /* Read to pixel data. */}
			meta.setPixelOffset(getSource().getFilePointer() - 1);
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.ASTRONOMY_DOMAIN,
				FormatTools.UNKNOWN_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();

			FormatTools.checkPlaneForReading(getMetadata(), imageIndex, planeIndex,
				buf.length, bounds);

			getStream().seek(getMetadata().getPixelOffset() + planeIndex * FormatTools
				.getPlaneSize(this, imageIndex));
			return readPlane(getStream(), imageIndex, bounds, plane);
		}
	}
}
