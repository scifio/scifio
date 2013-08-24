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

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.SCIFIO;
import io.scif.io.IRandomAccess;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.ZipHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.imglib2.display.ColorTable;

import org.scijava.plugin.Plugin;

/**
 * Reader for Zip files.
 */
@Plugin(type = Format.class)
public class ZipFormat extends AbstractFormat {

	// -- Format API Methods --

	public String getFormatName() {
		return "Zip";
	}

	public String[] getSuffixes() {
		return new String[] { "zip" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		private io.scif.Metadata metadata;

		private List<String> mappedFiles = new ArrayList<String>();

		// -- ZipMetadata methods --

		public List<String> getMappedFiles() {
			return mappedFiles;
		}

		public void setMetadata(final io.scif.Metadata m) throws IOException {
			if (metadata != null) metadata.close();

			metadata = m;
		}

		// -- HasColorTable API methods --

		public ColorTable getColorTable(final int imageIndex, final int planeIndex)
		{
			if (HasColorTable.class.isAssignableFrom(metadata.getClass())) return ((HasColorTable) metadata)
				.getColorTable(0, 0);
			return null;
		}

		// -- Metadata API Methods --

		public void populateImageMetadata() {
			// clears existing metadata
			createImageMetadata(0);

			// copies the delegate's image metadata
			for (final ImageMetadata meta : metadata.getAll())
				add(meta);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			for (final String name : mappedFiles) {
				final IRandomAccess handle = scifio().location().getMappedFile(name);
				scifio().location().mapFile(name, null);
				if (handle != null) {
					handle.close();
				}
			}
			mappedFiles.clear();

			super.close(fileOnly);

			if (metadata != null) metadata.close(fileOnly);
			if (!fileOnly) metadata = null;

			mappedFiles = new ArrayList<String>();
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		@Override
		public Metadata parse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			return super.parse(ZipUtilities.getRawStream(scifio(), stream), meta);
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			final String baseId =
				ZipUtilities.unzipId(scifio(), stream, meta.getMappedFiles());

			final io.scif.Parser p =
				scifio().format().getFormat(baseId).createParser();
			p.setOriginalMetadataPopulated(isOriginalMetadataPopulated());
			p.setMetadataFiltered(isMetadataFiltered());
			p.setMetadataOptions(getMetadataOptions());
			final io.scif.Metadata m = p.parse(baseId);

			meta.setMetadata(m);
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Fields --

		private io.scif.Reader reader;

		// -- Reader API Methods --

		@Override
		public void setSource(final RandomAccessInputStream stream)
			throws IOException
		{
			super.setSource(ZipUtilities.getRawStream(scifio(), stream));

			if (reader != null) reader.close();

			final String baseId = ZipUtilities.unzipId(scifio(), stream, null);

			try {
				reader = scifio().format().getFormat(baseId).createReader();
				reader.setSource(baseId);
			}
			catch (final FormatException e) {
				log().error("Failed to set delegate Reader's source", e);
			}
		}

		@Override
		public void setMetadata(final Metadata meta) throws IOException {
			super.setMetadata(meta);

			if (reader != null) reader.close();

			try {
				final String baseId =
					ZipUtilities.unzipId(scifio(), meta.getSource(), null);

				reader = scifio().initializer().initializeReader(baseId);
				meta.setMetadata(reader.getMetadata());
			}
			catch (final FormatException e) {
				log().error("Failed to initialize delegate Reader", e);
			}
		}

		/** Specifies whether or not to normalize float data. */
		@Override
		public void setNormalized(final boolean normalize) {
			if (reader != null) reader.setNormalized(normalize);
		}

		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			final Plane p =
				reader.openPlane(imageIndex, planeIndex, plane, x, y, w, h);
			System.arraycopy(p.getBytes(), 0, plane.getData(), 0,
				plane.getData().length);
			return plane;
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (reader != null) reader.close(fileOnly);
			if (!fileOnly) reader = null;
		}
	}

	// -- Helper class --

	private static class ZipUtilities {

		/**
		 * Extracts the String id of the provided stream.
		 * 
		 * @param scifio - A SCIFIO wrapping the current context
		 * @param stream - Stream, built around a .zip file, to extract the actual
		 *          id from
		 * @param mappedFiles - Optional param. If provided, all discovered entries
		 *          in the underlying archive will be added to this list.
		 * @return An id of the base entry in the .zip
		 * @throws IOException
		 */
		public static String unzipId(final SCIFIO scifio,
			final RandomAccessInputStream stream, final List<String> mappedFiles)
			throws IOException
		{
			final ZipInputStream zip = new ZipInputStream(stream);
			ZipEntry ze = null;

			while (true) {
				ze = zip.getNextEntry();
				if (ze == null) break;
				final ZipHandle handle =
					new ZipHandle(scifio.getContext(), stream.getFileName(), ze);
				scifio.location().mapFile(ze.getName(), handle);
				if (mappedFiles != null) mappedFiles.add(ze.getName());
			}

			final ZipHandle base =
				new ZipHandle(scifio.getContext(), stream.getFileName());
			final String id = base.getEntryName();
			base.close();

			return id;
		}

		/**
		 * Returns a new RandomAccessInputStream around the raw handle underlying
		 * the provided stream, instead of using a zip handle.
		 * <p>
		 * NB: closes the provided stream.
		 * </p>
		 */
		public static RandomAccessInputStream getRawStream(final SCIFIO scifio,
			final RandomAccessInputStream stream) throws IOException
		{
			// NB: We need a raw handle on the ZIP data itself, not a ZipHandle.
			final String id = stream.getFileName();
			final IRandomAccess rawHandle =
				scifio.location().getHandle(id, false, false);
			return new RandomAccessInputStream(scifio.getContext(), rawHandle, id);
		}
	}
}
