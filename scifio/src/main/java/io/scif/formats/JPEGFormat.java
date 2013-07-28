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
import io.scif.FormatException;
import io.scif.common.DataTools;
import io.scif.io.ByteArrayHandle;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.awt.color.CMMException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * JPEGReader is the file format reader for JPEG images.
 * 
 * @author Curtis Rueden ctrueden at wisc.edu
 */
@Plugin(type = JPEGFormat.class, priority = Priority.NORMAL_PRIORITY)
public class JPEGFormat extends ImageIOFormat {

	// -- Format API Methods --

	/*
	 * @see io.scif.Format#getFormatName()
	 */
	public String getFormatName() {
		return "JPEG";
	}

	/*
	 * @see io.scif.Format#getSuffixes()
	 */
	public String[] getSuffixes() {
		return new String[] { "jpg", "jpeg", "jpe" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Metadata extends ImageIOFormat.Metadata {

		// -- Metadata API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			scifio().location().mapId(getDatasetName(), null);
			super.close(fileOnly);
		}
	}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final int MAX_SIZE = 8192;

		// -- Constructor --

		public Checker() {
			suffixNecessary = false;
			suffixSufficient = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final String name, final boolean open) {
			if (open) {
				return super.isFormat(name, open);
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
				return false;
			}

			try {
				final io.scif.Metadata m = getFormat().createParser().parse(stream);

				// Need to check dimension lengths
				if (m.getAxisLength(0, Axes.X) > MAX_SIZE &&
					m.getAxisLength(0, Axes.Y) > MAX_SIZE)
				{
					stream.seek(0);
					return false;
				}
			}
			catch (final FormatException e) {
				log().error("Failed to pare JPEG data", e);
				return false;
			}

			return true;
		}
	}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Parser extends ImageIOFormat.Parser<Metadata> {

		@Override
		public void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			final String id = stream.getFileName();
			try {
				super.typedParse(stream, meta);
			}
			catch (final CMMException e) {
				// strip out all but the first application marker
				// ImageIO isn't too keen on supporting multiple application markers
				// in the same stream, as evidenced by:
				//
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6488904

				final ByteArrayOutputStream v = new ByteArrayOutputStream();

				final byte[] tag = new byte[2];
				stream.read(tag);
				v.write(tag);

				stream.read(tag);
				int tagValue = DataTools.bytesToShort(tag, false) & 0xffff;
				boolean appNoteFound = false;
				while (tagValue != 0xffdb) {
					if (!appNoteFound || (tagValue < 0xffe0 && tagValue >= 0xfff0)) {
						v.write(tag);

						stream.read(tag);
						final int len = DataTools.bytesToShort(tag, false) & 0xffff;
						final byte[] tagContents = new byte[len - 2];
						stream.read(tagContents);
						v.write(tag);
						v.write(tagContents);
					}
					else {
						stream.read(tag);
						final int len = DataTools.bytesToShort(tag, false) & 0xffff;
						stream.skipBytes(len - 2);
					}

					if (tagValue >= 0xffe0 && tagValue < 0xfff0 && !appNoteFound) {
						appNoteFound = true;
					}
					stream.read(tag);
					tagValue = DataTools.bytesToShort(tag, false) & 0xffff;
				}
				v.write(tag);
				final byte[] remainder =
					new byte[(int) (stream.length() - stream.getFilePointer())];
				stream.read(remainder);
				v.write(remainder);

				final ByteArrayHandle bytes = new ByteArrayHandle(v.toByteArray());

				scifio().location().mapFile(currentId + ".fixed", bytes);
				super.parse(currentId + ".fixed", meta);
			}

			metadata.setDatasetName(id);
			currentId = id;
		}
	}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Reader extends ImageIOFormat.Reader<Metadata> {}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
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
