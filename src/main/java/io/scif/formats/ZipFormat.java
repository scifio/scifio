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
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.config.SCIFIOConfig;
import io.scif.io.IRandomAccess;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.ZipHandle;
import io.scif.services.FormatService;
import io.scif.services.InitializeService;
import io.scif.services.LocationService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.imglib2.Interval;
import net.imglib2.display.ColorTable;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Reader for Zip files.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Zip")
public class ZipFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "zip" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		@Parameter
		private LocationService locationService;

		private io.scif.Metadata metadata;

		private List<String> mappedFiles = new ArrayList<>();

		// -- ZipMetadata methods --

		public List<String> getMappedFiles() {
			return mappedFiles;
		}

		public void setMetadata(final io.scif.Metadata m) throws IOException {
			if (metadata != null) metadata.close();

			metadata = m;
		}

		// -- HasColorTable API methods --

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			if (HasColorTable.class.isAssignableFrom(metadata.getClass()))
				return ((HasColorTable) metadata).getColorTable(imageIndex, planeIndex);
			return null;
		}

		// -- Metadata API Methods --

		@Override
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
				final IRandomAccess handle = locationService.getMappedFile(name);
				locationService.mapFile(name, null);
				if (handle != null) {
					handle.close();
				}
			}
			mappedFiles.clear();

			super.close(fileOnly);

			if (metadata != null) metadata.close(fileOnly);
			if (!fileOnly) metadata = null;

			mappedFiles = new ArrayList<>();
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		@Parameter
		private LocationService locationService;

		@Parameter
		private FormatService formatService;

		@Override
		public Metadata parse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			return super.parse(ZipUtilities.getRawStream(locationService, stream),
				meta, config);
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			final String baseId = ZipUtilities.unzipId(locationService, stream, meta
				.getMappedFiles());

			final io.scif.Parser p = formatService.getFormat(baseId, config)
				.createParser();
			final io.scif.Metadata m = p.parse(baseId, config);

			meta.setMetadata(m);
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.UNKNOWN_DOMAIN };
		}

		// -- Fields --

		@Parameter
		private LocationService locationService;

		@Parameter
		private FormatService formatService;

		@Parameter
		private InitializeService initializeService;

		private io.scif.Reader reader;

		// -- Reader API Methods --

		@Override
		public void setSource(final RandomAccessInputStream stream,
			final SCIFIOConfig config) throws IOException
		{
			super.setSource(ZipUtilities.getRawStream(locationService, stream),
				config);

			if (reader != null) reader.close();

			final String baseId = ZipUtilities.unzipId(locationService, stream, null);

			try {
				reader = formatService.getFormat(baseId, config).createReader();
				reader.setSource(baseId, config);
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
				final String baseId = ZipUtilities.unzipId(locationService, meta
					.getSource(), null);

				reader = initializeService.initializeReader(baseId);
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

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Plane p = reader.openPlane(imageIndex, planeIndex, plane, bounds,
				config);
			System.arraycopy(p.getBytes(), 0, plane.getData(), 0, plane
				.getData().length);
			plane.setColorTable(getMetadata().getColorTable(imageIndex, planeIndex));
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
		 * @param locationService - The location service to use for mapping files
		 * @param stream - Stream, built around a .zip file, to extract the actual
		 *          id from
		 * @param mappedFiles - Optional param. If provided, all discovered entries
		 *          in the underlying archive will be added to this list.
		 * @return An id of the base entry in the .zip
		 * @throws IOException
		 */
		public static String unzipId(final LocationService locationService,
			final RandomAccessInputStream stream, final List<String> mappedFiles)
			throws IOException
		{
			final ZipInputStream zip = new ZipInputStream(stream);
			ZipEntry ze = null;

			while (true) {
				ze = zip.getNextEntry();
				if (ze == null) break;
				final ZipHandle handle = new ZipHandle(locationService.getContext(),
					stream.getFileName(), ze);
				locationService.mapFile(ze.getName(), handle);
				if (mappedFiles != null) mappedFiles.add(ze.getName());
			}

			final ZipHandle base = new ZipHandle(locationService.getContext(), stream
				.getFileName());
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
		public static RandomAccessInputStream getRawStream(
			final LocationService locationService,
			final RandomAccessInputStream stream) throws IOException
		{
			// NB: We need a raw handle on the ZIP data itself, not a ZipHandle.
			final String id = stream.getFileName();
			final IRandomAccess rawHandle = locationService.getHandle(id, false,
				false);
			return new RandomAccessInputStream(locationService.getContext(),
				rawHandle, id);
		}
	}
}
