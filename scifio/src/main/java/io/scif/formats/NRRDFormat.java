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
import io.scif.DefaultMetadataOptions;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.MetadataLevel;
import io.scif.UnsupportedCompressionException;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * File format reader for NRRD files; see http://teem.sourceforge.net/nrrd.
 */
@Plugin(type = Format.class)
public class NRRDFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "NRRD";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "nrrd", "nhdr" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		public static final String CNAME = "io.scif.formats.NRRDFormat$Metadata";

		// -- Fields --

		/** Name of data file, if the current extension is 'nhdr'. */
		private String dataFile;

		/** Data encoding. */
		private String encoding;

		/** Offset to pixel data. */
		private long offset;

		/** Helper format for reading pixel data. */
		private io.scif.Reader helper;

		private String[] pixelSizes;

		private boolean lookForCompanion = true;
		private boolean initializeHelper = false;

		// -- NRRDMetadata getters and setters --

		public void setHelper(final io.scif.Reader reader) {
			helper = reader;
		}

		public io.scif.Reader getHelper() {
			return helper;
		}

		public String getDataFile() {
			return dataFile;
		}

		public void setDataFile(final String dataFile) {
			this.dataFile = dataFile;
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(final long offset) {
			this.offset = offset;
		}

		public String[] getPixelSizes() {
			return pixelSizes;
		}

		public void setPixelSizes(final String[] pixelSizes) {
			this.pixelSizes = pixelSizes;
		}

		public boolean isLookForCompanion() {
			return lookForCompanion;
		}

		public void setLookForCompanion(final boolean lookForCompanion) {
			this.lookForCompanion = lookForCompanion;
		}

		public boolean isInitializeHelper() {
			return initializeHelper;
		}

		public void setInitializeHelper(final boolean initializeHelper) {
			this.initializeHelper = initializeHelper;
		}

		// -- Metadata API methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			if (iMeta.getAxisLength(Axes.CHANNEL) > 1) {
				iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
				iMeta.setPlanarAxisCount(3);
			}
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				dataFile = encoding = null;
				offset = 0;
				pixelSizes = null;
				initializeHelper = false;
				helper = null;
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final String NRRD_MAGIC_STRING = "NRRD";

		// -- Checker API Methods --

		@Override
		public boolean isFormat(String name, final boolean open) {
			if (super.isFormat(name, open)) return true;
			if (!open) return false;

			// look for a matching .nhdr file
			Location header = new Location(getContext(), name + ".nhdr");
			if (header.exists()) {
				return true;
			}

			if (name.indexOf(".") >= 0) {
				name = name.substring(0, name.lastIndexOf("."));
			}

			header = new Location(getContext(), name + ".nhdr");
			return header.exists();
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = NRRD_MAGIC_STRING.length();
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			return stream.readString(blockLen).startsWith(NRRD_MAGIC_STRING);
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		public String[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(currentId, true, 1);
			if (noPixels) {
				if (metadata.getDataFile() == null) return null;
				return new String[] { currentId };
			}
			if (metadata.getDataFile() == null) return new String[] { currentId };
			return new String[] { currentId, metadata.getDataFile() };
		}

		// -- Abstract Parser API Methods --

		@Override
		public Metadata parse(RandomAccessInputStream stream, final Metadata meta)
			throws IOException, FormatException
		{
			String id = stream.getFileName();

			// make sure we actually have the .nrrd/.nhdr file
			if (!FormatTools.checkSuffix(id, "nhdr") &&
				!FormatTools.checkSuffix(id, "nrrd"))
			{
				id += ".nhdr";

				if (!new Location(getContext(), id).exists()) {
					id = id.substring(0, id.lastIndexOf("."));
					id = id.substring(0, id.lastIndexOf("."));
					id += ".nhdr";
				}
				id = new Location(getContext(), id).getAbsolutePath();
			}
			stream.close();

			stream = new RandomAccessInputStream(getContext(), id);

			return super.parse(stream, meta);
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			String key, v;

			int numDimensions = 0;

			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			iMeta.setAxisLength(Axes.X, 1);
			iMeta.setAxisLength(Axes.Y, 1);
			iMeta.setAxisLength(Axes.Z, 1);
			iMeta.setAxisLength(Axes.CHANNEL, 1);
			iMeta.setAxisLength(Axes.TIME, 1);
			iMeta.setPlanarAxisCount(2);

			String line = in.readLine();
			while (line != null && line.length() > 0) {
				if (!line.startsWith("#") && !line.startsWith("NRRD")) {
					// parse key/value pair
					key = line.substring(0, line.indexOf(":")).trim();
					v = line.substring(line.indexOf(":") + 1).trim();
					addGlobalMeta(key, v);

					if (key.equals("type")) {
						if (v.indexOf("char") != -1 || v.indexOf("8") != -1) {
							iMeta.setPixelType(FormatTools.UINT8);
						}
						else if (v.indexOf("short") != -1 || v.indexOf("16") != -1) {
							iMeta.setPixelType(FormatTools.UINT16);
						}
						else if (v.equals("int") || v.equals("signed int") ||
							v.equals("int32") || v.equals("int32_t") || v.equals("uint") ||
							v.equals("unsigned int") || v.equals("uint32") ||
							v.equals("uint32_t"))
						{
							iMeta.setPixelType(FormatTools.UINT32);
						}
						else if (v.equals("float")) iMeta.setPixelType(FormatTools.FLOAT);
						else if (v.equals("double")) iMeta.setPixelType(FormatTools.DOUBLE);
						else throw new FormatException("Unsupported data type: " + v);
					}
					else if (key.equals("dimension")) {
						numDimensions = Integer.parseInt(v);
					}
					else if (key.equals("sizes")) {
						final String[] tokens = v.split(" ");
						for (int i = 0; i < numDimensions; i++) {
							final int size = Integer.parseInt(tokens[i]);

							if (numDimensions >= 3 && i == 0 && size > 1 && size <= 16) {
								iMeta.setAxisLength(Axes.CHANNEL, size);
								iMeta.setPlanarAxisCount(3);
							}
							else if (i == 0 ||
								(iMeta.getPlanarAxisCount() > 2 && i == 1))
							{
								iMeta.setAxisLength(Axes.X, size);
							}
							else if (i == 1 ||
								(iMeta.getPlanarAxisCount() > 2 && i == 2))
							{
								iMeta.setAxisLength(Axes.Y, size);
							}
							else if (i == 2 ||
								(iMeta.getPlanarAxisCount() > 2 && i == 3))
							{
								iMeta.setAxisLength(Axes.Z, size);
							}
							else if (i == 3 ||
								(iMeta.getPlanarAxisCount() > 2 && i == 4))
							{
								iMeta.setAxisLength(Axes.TIME, size);
							}
						}
					}
					else if (key.equals("data file") || key.equals("datafile")) {
						meta.setDataFile(v);
					}
					else if (key.equals("encoding")) meta.setEncoding(v);
					else if (key.equals("endian")) {
						iMeta.setLittleEndian(v.equals("little"));
					}
					else if (key.equals("spacings")) {
						meta.setPixelSizes(v.split(" "));
					}
					else if (key.equals("byte skip")) {
						meta.setOffset(Long.parseLong(v));
					}
				}

				line = in.readLine();
				if (line != null) line = line.trim();
			}

			// nrrd files store pixel data in addition to metadata
			// nhdr files don't store pixel data, but instead provide a path to the
			// pixels file (this can be any format)

			if (meta.getDataFile() == null) meta.setOffset(stream.getFilePointer());
			else {
				final Location f =
					new Location(getContext(), currentId).getAbsoluteFile();
				final Location parent = f.getParentFile();
				if (f.exists() && parent != null) {
					String dataFile = meta.getDataFile();
					dataFile = dataFile.substring(dataFile.indexOf(File.separator) + 1);
					dataFile =
						new Location(getContext(), parent, dataFile).getAbsolutePath();
				}
				meta.setInitializeHelper(!meta.getEncoding().equals("raw"));
			}

			if (meta.isInitializeHelper()) {
				// Find the highest priority non-NRRD format that can support the
				// current
				// image and cache it as a helper
				final NRRDFormat nrrd =
					scifio().format().getFormatFromClass(NRRDFormat.class);
				scifio().format().removeFormat(nrrd);

				final Format helperFormat =
					scifio().format().getFormat(meta.getDataFile());
				final io.scif.Parser p = helperFormat.createParser();
				p.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.MINIMUM));
				p.setMetadataOptions(meta.getMetadataOptions());
				final io.scif.Reader helper = helperFormat.createReader();
				helper.setMetadata(p.parse(meta.getDataFile()));
				helper.setSource(meta.getDataFile());
				meta.setHelper(helper);

				scifio().format().addFormat(nrrd);
			}
		}

		// -- Groupable API Methods --

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		@Override
		public boolean isSingleFile(final String id) throws FormatException,
			IOException
		{
			return FormatTools.checkSuffix(id, "nrrd");
		}

		@Override
		public int fileGroupOption(final String id) throws FormatException,
			IOException
		{
			return FormatTools.MUST_GROUP;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.UNKNOWN_DOMAIN };
		}

		// -- Groupable API Methods --

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		@Override
		public boolean isSingleFile(final String id) throws FormatException,
			IOException
		{
			return FormatTools.checkSuffix(id, "nrrd");
		}

		@Override
		public int fileGroupOption(final String id) throws FormatException,
			IOException
		{
			return FormatTools.MUST_GROUP;
		}

		// -- Reader API Methods --

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			return getMetadata().getAxisLength(imageIndex, Axes.Y);
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();

			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);

			// TODO : add support for additional encoding types
			if (meta.getDataFile() == null) {
				if (meta.getEncoding().equals("raw")) {
					final long planeSize = FormatTools.getPlaneSize(this, imageIndex);
					getStream().seek(meta.getOffset() + planeIndex * planeSize);

					readPlane(getStream(), imageIndex, planeMin, planeMax, plane);
					return plane;
				}
				throw new UnsupportedCompressionException("Unsupported encoding: " +
					meta.getEncoding());
			}
			else if (meta.getEncoding().equals("raw")) {
				final RandomAccessInputStream s =
					new RandomAccessInputStream(getContext(), meta.getDataFile());
				s.seek(meta.getOffset() + planeIndex *
					FormatTools.getPlaneSize(this, imageIndex));
				readPlane(s, imageIndex, planeMin, planeMax, plane);
				s.close();
				return plane;
			}

			// open the data file using our helper format
			if (meta.isInitializeHelper() && meta.getDataFile() != null &&
				meta.getHelper() != null)
			{
				meta.getHelper().openPlane(imageIndex, planeIndex, plane, planeMin,
					planeMax);
				return plane;
			}

			throw new FormatException("Could not find a supporting Format");
		}

	}
}
