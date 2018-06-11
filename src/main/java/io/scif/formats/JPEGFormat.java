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
import io.scif.Format;
import io.scif.FormatException;
import io.scif.config.SCIFIOConfig;
import io.scif.io.ByteArrayHandle;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.LocationService;
import io.scif.util.FormatTools;

import java.awt.color.CMMException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.imagej.axis.Axes;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * JPEGReader is the file format reader for JPEG images.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "JPEG")
public class JPEGFormat extends ImageIOFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "jpg", "jpeg", "jpe" };
	}

	// -- Nested classes --

	public static class Metadata extends ImageIOFormat.Metadata {

		@Parameter
		private LocationService locationService;

		// -- Metadata API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			locationService.mapId(getDatasetName(), null);
			super.close(fileOnly);
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final int MAX_SIZE = 8192;

		// -- Checker API Methods --

		@Override
		public boolean suffixNecessary() {
			return false;
		}

		@Override
		public boolean suffixSufficient() {
			return false;
		}

		@Override
		public boolean isFormat(final String name, final SCIFIOConfig config) {
			if (config.checkerIsOpen()) {
				return super.isFormat(name, config);
			}

			return FormatTools.checkSuffix(name, getFormat().getSuffixes());
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 4;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;

			final byte[] signature = new byte[blockLen];
			stream.read(signature);

			if (signature[0] != (byte) 0xff || signature[1] != (byte) 0xd8 ||
				signature[2] != (byte) 0xff || (signature[3] & 0xf0) == 0)
			{
				stream.seek(0);
				return false;
			}

			try {
				stream.seek(0);
				final io.scif.Metadata m = getFormat().createParser().parse(stream);

				stream.seek(0);

				// Need to check dimension lengths
				if (m.get(0).getAxisLength(Axes.X) > MAX_SIZE && m.get(0).getAxisLength(
					Axes.Y) > MAX_SIZE)
				{
					return false;
				}
			}
			catch (final FormatException e) {
				log().error("Failed to parse JPEG data", e);
				return false;
			}

			return true;
		}
	}

	public static class Parser extends ImageIOFormat.Parser<Metadata> {

		@Parameter
		private LocationService locationService;

		@Override
		public void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			final String id = stream.getFileName();
			try {
				super.typedParse(stream, meta, config);
			}
			catch (final CMMException e) {
				// strip out all but the first application marker
				// ImageIO isn't too keen on supporting multiple application
				// markers
				// in the same stream, as evidenced by:
				//
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6488904

				final ByteArrayOutputStream v = new ByteArrayOutputStream();

				final byte[] tag = new byte[2];
				stream.read(tag);
				v.write(tag);

				stream.read(tag);
				int tagValue = Bytes.toShort(tag, false) & 0xffff;
				boolean appNoteFound = false;
				while (tagValue != 0xffdb) {
					if (!appNoteFound || (tagValue < 0xffe0 && tagValue >= 0xfff0)) {
						v.write(tag);

						stream.read(tag);
						final int len = Bytes.toShort(tag, false) & 0xffff;
						final byte[] tagContents = new byte[len - 2];
						stream.read(tagContents);
						v.write(tag);
						v.write(tagContents);
					}
					else {
						stream.read(tag);
						final int len = Bytes.toShort(tag, false) & 0xffff;
						stream.skipBytes(len - 2);
					}

					if (tagValue >= 0xffe0 && tagValue < 0xfff0 && !appNoteFound) {
						appNoteFound = true;
					}
					stream.read(tag);
					tagValue = Bytes.toShort(tag, false) & 0xffff;
				}
				v.write(tag);
				final byte[] remainder = new byte[(int) (stream.length() - stream
					.getFilePointer())];
				stream.read(remainder);
				v.write(remainder);

				final ByteArrayHandle bytes = new ByteArrayHandle(v.toByteArray());

				locationService.mapFile(getSource().getFileName() + ".fixed", bytes);
				super.parse(getSource().getFileName() + ".fixed", meta);
			}

			getMetadata().setDatasetName(id);
		}
	}

	public static class Reader extends ImageIOFormat.Reader<Metadata> {}

	public static class Writer extends ImageIOFormat.Writer<Metadata> {

		// -- Constructor --

		public Writer() {
			super("jpeg");
		}

		// -- Writer API methods --

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8 };
		}
	}
}
