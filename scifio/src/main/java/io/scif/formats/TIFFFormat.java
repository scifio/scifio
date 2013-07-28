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
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetadataLevel;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.codec.CompressionType;
import io.scif.common.Constants;
import io.scif.common.DataTools;
import io.scif.common.DateTools;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.PhotoInterp;
import io.scif.formats.tiff.TiffCompression;
import io.scif.formats.tiff.TiffParser;
import io.scif.formats.tiff.TiffRational;
import io.scif.formats.tiff.TiffSaver;
import io.scif.gui.AWTImageTools;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.ImageTools;

import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

@Plugin(type = TIFFFormat.class, priority = TIFFFormat.PRIORITY)
public class TIFFFormat extends AbstractFormat {

	// -- Constants --

	public static final double PRIORITY = MinimalTIFFFormat.PRIORITY + 1;
	public static final String[] COMPANION_SUFFIXES = { "xml", "txt" };
	public static final String[] TIFF_SUFFIXES = { "tif", "tiff", "tf2", "tf8",
		"btf" };

	// -- Format API methods --

	/*
	 * @see io.scif.Format#getFormatName()
	 */
	public String getFormatName() {
		return "Tagged Image File Format";
	}

	/*
	 * @see io.scif.Format#getSuffixes()
	 */
	public String[] getSuffixes() {
		return TIFF_SUFFIXES;
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Metadata extends MinimalTIFFFormat.Metadata {

		// -- Fields --

		private boolean populateImageMetadata = true;

		// FIXME: these are duplicating metadata store information..
		private String creationDate;
		private String experimenterFirstName;
		private String experimenterLastName;
		private String experimenterEmail;
		private String imageDescription;
		private double physicalSizeX;
		private double physicalSizeY;

		private String companionFile;
		private String description;
		private String calibrationUnit;
		private Double physicalSizeZ;
		private Double timeIncrement;
		private Integer xOrigin, yOrigin;

		// -- Constants --

		public static final String CNAME = "io.scif.formats.TIFFFormat$Metadata";

		// -- TIFFMetadata getters and setters --

		public String getCompanionFile() {
			return companionFile;
		}

		public void setCompanionFile(final String companionFile) {
			this.companionFile = companionFile;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public String getCalibrationUnit() {
			return calibrationUnit;
		}

		public void setCalibrationUnit(final String calibrationUnit) {
			this.calibrationUnit = calibrationUnit;
		}

		public Double getPhysicalSizeZ() {
			return physicalSizeZ == null ? 1.0 : physicalSizeZ;
		}

		public void setPhysicalSizeZ(final Double physicalSizeZ) {
			this.physicalSizeZ = physicalSizeZ;
		}

		public Double getTimeIncrement() {
			return timeIncrement == null ? 1.0 : timeIncrement;
		}

		public void setTimeIncrement(final Double timeIncrement) {
			this.timeIncrement = timeIncrement;
		}

		public Integer getxOrigin() {
			return xOrigin;
		}

		public void setxOrigin(final Integer xOrigin) {
			this.xOrigin = xOrigin;
		}

		public Integer getyOrigin() {
			return yOrigin;
		}

		public void setyOrigin(final Integer yOrigin) {
			this.yOrigin = yOrigin;
		}

		public String getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(final String creationDate) {
			this.creationDate = creationDate;
		}

		public String getExperimenterFirstName() {
			return experimenterFirstName;
		}

		public void setExperimenterFirstName(final String experimenterFirstName) {
			this.experimenterFirstName = experimenterFirstName;
		}

		public String getExperimenterLastName() {
			return experimenterLastName;
		}

		public void setExperimenterLastName(final String experimenterLastName) {
			this.experimenterLastName = experimenterLastName;
		}

		public String getExperimenterEmail() {
			return experimenterEmail;
		}

		public void setExperimenterEmail(final String experimenterEmail) {
			this.experimenterEmail = experimenterEmail;
		}

		public String getImageDescription() {
			return imageDescription;
		}

		public void setImageDescription(final String imageDescription) {
			this.imageDescription = imageDescription;
		}

		public double getPhysicalSizeX() {
			return physicalSizeX;
		}

		public void setPhysicalSizeX(final double physicalSizeX) {
			this.physicalSizeX = physicalSizeX;
		}

		public double getPhysicalSizeY() {
			return physicalSizeY;
		}

		public void setPhysicalSizeY(final double physicalSizeY) {
			this.physicalSizeY = physicalSizeY;
		}

		// -- Metadata API Methods --

		@Override
		public void createImageMetadata(final int imageCount) {
			populateImageMetadata = true;
			super.createImageMetadata(imageCount);
		}

		@Override
		public void populateImageMetadata() {
			if (populateImageMetadata) super.populateImageMetadata();

			final ImageMetadata m = get(0);

			if (getIfds().size() > 1) m.setOrderCertain(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				companionFile = null;
				description = null;
				calibrationUnit = null;
				physicalSizeZ = null;
				timeIncrement = null;
				xOrigin = null;
				yOrigin = null;
			}
		}

	}

	/**
	 * @author Mark Hiner hinerm at gmail.com
	 */
	public static class Parser extends BaseTIFFParser {

		// -- Constants --

		public static final int IMAGEJ_TAG = 50839;

		// -- Parser API Methods --

		@Override
		public String[] getImageUsedFiles(final int ImageIndex,
			final boolean noPixels)
		{
			if (noPixels) {
				return metadata.getCompanionFile() == null ? null
					: new String[] { metadata.getCompanionFile() };
			}
			if (metadata.getCompanionFile() != null) return new String[] {
				metadata.getCompanionFile(), currentId };
			return new String[] { currentId };
		}

		// -- BaseTIFFParser API Methods

		@Override
		protected void initStandardMetadata(final Metadata meta)
			throws FormatException, IOException
		{
			super.initStandardMetadata(meta);
			final IFDList ifds = meta.getIfds();
			final String comment = ifds.get(0).getComment();

			log().info("Checking comment style");

			// check for reusable proprietary tags (65000-65535),
			// which may contain additional metadata

			final MetadataLevel level = getMetadataOptions().getMetadataLevel();
			if (level != MetadataLevel.MINIMUM) {
				final Integer[] tags = ifds.get(0).keySet().toArray(new Integer[0]);
				for (final Integer tag : tags) {
					if (tag.intValue() >= 65000) {
						final Object value = ifds.get(0).get(tag);
						if (value instanceof short[]) {
							final short[] s = (short[]) value;
							final byte[] b = new byte[s.length];
							for (int i = 0; i < b.length; i++) {
								b[i] = (byte) s[i];
							}
							String metadata =
								DataTools.stripString(new String(b, Constants.ENCODING));
							if (metadata.indexOf("xml") != -1) {
								metadata = metadata.substring(metadata.indexOf("<"));
								metadata =
									"<root>" + scifio().xml().sanitizeXML(metadata) + "</root>";
								try {
									final Hashtable<String, String> xmlMetadata =
										scifio().xml().parseXML(metadata);
									for (final String key : xmlMetadata.keySet()) {
										addGlobalMeta(key, xmlMetadata.get(key));
									}
								}
								catch (final IOException e) {}
							}
							else {
								addGlobalMeta(tag.toString(), metadata);
							}
						}
					}
				}
			}

			// check for ImageJ-style TIFF comment
			final boolean ij = checkCommentImageJ(meta, comment);
			if (ij) parseCommentImageJ(meta, comment);

			// check for MetaMorph-style TIFF comment
			final boolean metamorph = checkCommentMetamorph(meta, comment);
			if (metamorph && level != MetadataLevel.MINIMUM) {
				parseCommentMetamorph(meta, comment);
			}
			put("MetaMorph", metamorph ? "yes" : "no");

			// check for other INI-style comment
			if (!ij && !metamorph && level != MetadataLevel.MINIMUM) {
				parseCommentGeneric(meta, comment);
			}

			// check for another file with the same name

			if (isGroupFiles()) {
				final Location currentFile =
					new Location(getContext(), currentId).getAbsoluteFile();
				final String currentName = currentFile.getName();
				final Location directory = currentFile.getParentFile();
				final String[] files = directory.list(true);
				if (files != null) {
					for (final String file : files) {
						String name = file;
						if (name.indexOf(".") != -1) {
							name = name.substring(0, name.indexOf("."));
						}

						if (currentName.startsWith(name) &&
							FormatTools.checkSuffix(name, COMPANION_SUFFIXES))
						{
							meta.setCompanionFile(new Location(getContext(), directory, file)
								.getAbsolutePath());
							break;
						}
					}
				}
			}

			// TODO : parse companion file once loci.parsers package is in place
		}

		@Override
		protected void initMetadataStore(final Metadata meta)
			throws FormatException
		{
			super.initMetadataStore(meta);
//      MetadataStore store = makeFilterMetadata();
//      if (meta.getDescription() != null) {
//        store.setImageDescription(description, 0);
//      }
			populateMetadataStoreImageJ(meta);
		}

		// -- Helper methods --

		private boolean
			checkCommentImageJ(final Metadata meta, final String comment)
		{
			return comment != null && comment.startsWith("ImageJ=");
		}

		private boolean checkCommentMetamorph(final Metadata meta,
			final String comment)
		{
			final String software =
				meta.getIfds().get(0).getIFDTextValue(IFD.SOFTWARE);
			return comment != null && software != null &&
				software.indexOf("MetaMorph") != -1;
		}

		private void parseCommentImageJ(final Metadata meta, String comment)
			throws FormatException, IOException
		{

			meta.populateImageMetadata();
			meta.populateImageMetadata = false;

			final int nl = comment.indexOf("\n");
			put("ImageJ", nl < 0 ? comment.substring(7) : comment.substring(7, nl));
			meta.getTable().remove("Comment");
			meta.setDescription("");

			int z = 1, t = 1;
			int c = meta.getAxisLength(0, Axes.CHANNEL);

			IFDList ifds = meta.getIfds();

			if (ifds.get(0).containsKey(IMAGEJ_TAG)) {
				comment += "\n" + ifds.get(0).getIFDTextValue(IMAGEJ_TAG);
			}

			// parse ImageJ metadata (ZCT sizes, calibration units, etc.)
			final StringTokenizer st = new StringTokenizer(comment, "\n");
			while (st.hasMoreTokens()) {
				final String token = st.nextToken();
				String value = null;
				final int eq = token.indexOf("=");
				if (eq >= 0) value = token.substring(eq + 1);

				if (token.startsWith("channels=")) c = parseInt(value);
				else if (token.startsWith("slices=")) z = parseInt(value);
				else if (token.startsWith("frames=")) t = parseInt(value);
				else if (token.startsWith("mode=")) {
					put("Color mode", value);
				}
				else if (token.startsWith("unit=")) {
					meta.setCalibrationUnit(value);
					put("Unit", meta.getCalibrationUnit());
				}
				else if (token.startsWith("finterval=")) {
					meta.setTimeIncrement(parseDouble(value));
					put("Frame Interval", meta.getTimeIncrement());
				}
				else if (token.startsWith("spacing=")) {
					meta.setPhysicalSizeZ(parseDouble(value));
					put("Spacing", meta.getPhysicalSizeZ());
				}
				else if (token.startsWith("xorigin=")) {
					meta.setxOrigin(parseInt(value));
					put("X Origin", meta.getxOrigin());
				}
				else if (token.startsWith("yorigin=")) {
					meta.setyOrigin(parseInt(value));
					put("Y Origin", meta.getyOrigin());
				}
				else if (eq > 0) {
					put(token.substring(0, eq).trim(), value);
				}
			}
			if (z * c * t == c && meta.isRGB(0)) {
				t = meta.getPlaneCount(0);
			}

			final ImageMetadata m = meta.get(0);
			m.setAxisTypes(FormatTools.findDimensionList("XYCZT"));

			if (z * t * (m.isRGB() ? 1 : c) == ifds.size()) {
				m.setAxisLength(Axes.Z, z);
				m.setAxisLength(Axes.TIME, t);
				m.setAxisLength(Axes.CHANNEL, m.isRGB() ? m.getAxisLength(Axes.CHANNEL)
					: c);
			}
			else if (z * c * t == ifds.size() && m.isRGB()) {
				m.setAxisLength(Axes.Z, z);
				m.setAxisLength(Axes.TIME, t);
				m.setAxisLength(Axes.CHANNEL, m.getAxisLength(Axes.CHANNEL) * c);
			}
			else if (ifds.size() == 1 && z * t > ifds.size() &&
				ifds.get(0).getCompression() == TiffCompression.UNCOMPRESSED)
			{
				// file is likely corrupt (missing end IFDs)
				//
				// ImageJ writes TIFF files like this:
				// IFD #0
				// comment
				// all pixel data
				// IFD #1
				// IFD #2
				// ...
				//
				// since we know where the pixel data is, we can create fake
				// IFDs in an attempt to read the rest of the pixels

				final IFD firstIFD = ifds.get(0);

				final int planeSize =
					m.getAxisLength(Axes.X) * m.getAxisLength(Axes.Y) *
						m.getRGBChannelCount() *
						FormatTools.getBytesPerPixel(m.getPixelType());
				final long[] stripOffsets = firstIFD.getStripOffsets();
				final long[] stripByteCounts = firstIFD.getStripByteCounts();

				final long endOfFirstPlane =
					stripOffsets[stripOffsets.length - 1] +
						stripByteCounts[stripByteCounts.length - 1];
				final long totalBytes = in.length() - endOfFirstPlane;
				final int totalPlanes = (int) (totalBytes / planeSize) + 1;

				ifds = new IFDList();
				ifds.add(firstIFD);
				for (int i = 1; i < totalPlanes; i++) {
					final IFD ifd = new IFD(firstIFD, log());
					ifds.add(ifd);
					final long[] prevOffsets = ifds.get(i - 1).getStripOffsets();
					final long[] offsets = new long[stripOffsets.length];
					offsets[0] =
						prevOffsets[prevOffsets.length - 1] +
							stripByteCounts[stripByteCounts.length - 1];
					for (int j = 1; j < offsets.length; j++) {
						offsets[j] = offsets[j - 1] + stripByteCounts[j - 1];
					}
					ifd.putIFDValue(IFD.STRIP_OFFSETS, offsets);
				}

				if (z * c * t == ifds.size()) {
					m.setAxisLength(Axes.Z, z);
					m.setAxisLength(Axes.TIME, t);
					m.setAxisLength(Axes.CHANNEL, c);
				}
				else if (z * t == ifds.size()) {
					m.setAxisLength(Axes.Z, z);
					m.setAxisLength(Axes.TIME, t);
				}
				else m.setAxisLength(Axes.Z, ifds.size());
				m.setPlaneCount(ifds.size());
			}
			else {
				m.setAxisLength(Axes.TIME, ifds.size());
				m.setPlaneCount(ifds.size());
			}
		}

		/**
		 * Checks the original metadata table for ImageJ-specific information to
		 * propagate into the metadata store.
		 */
		private void populateMetadataStoreImageJ(final Metadata meta) {
			// TODO: Perhaps we should only populate the physical Z size if the unit
			// is
			// a known, physical quantity such as "micron" rather than "pixel".
			// e.g.: if (calibrationUnit.equals("micron"))
			if (meta.getPhysicalSizeZ() != null) {
				double zDepth = meta.getPhysicalSizeZ();
				if (zDepth < 0) zDepth = -zDepth;
				if (zDepth > 0) {
					meta.setPhysicalSizeZ(zDepth);
				}
				else {
					log()
						.warn("Expected positive value for PhysicalSizeZ; got " + zDepth);
				}
			}
		}

		private void
			parseCommentMetamorph(final Metadata meta, final String comment)
		{
			// parse key/value pairs
			final StringTokenizer st = new StringTokenizer(comment, "\n");
			while (st.hasMoreTokens()) {
				final String line = st.nextToken();
				final int colon = line.indexOf(":");
				if (colon < 0) {
					addGlobalMeta("Comment", line);
					meta.setDescription(line);
					continue;
				}
				final String key = line.substring(0, colon);
				final String value = line.substring(colon + 1);
				addGlobalMeta(key, value);
			}
		}

		private void parseCommentGeneric(final Metadata meta, String comment) {
			if (comment == null) return;
			final String[] lines = comment.split("\n");
			if (lines.length > 1) {
				comment = "";
				for (final String line : lines) {
					final int eq = line.indexOf("=");
					if (eq != -1) {
						final String key = line.substring(0, eq).trim();
						final String value = line.substring(eq + 1).trim();
						addGlobalMeta(key, value);
					}
					else if (!line.startsWith("[")) {
						comment += line + "\n";
					}
				}
				addGlobalMeta("Comment", comment);
				meta.setDescription(comment);
			}
		}

		private int parseInt(final String s) {
			try {
				return Integer.parseInt(s);
			}
			catch (final NumberFormatException e) {
				log().debug("Failed to parse integer value", e);
			}
			return 0;
		}

		private double parseDouble(final String s) {
			try {
				return Double.parseDouble(s);
			}
			catch (final NumberFormatException e) {
				log().debug("Failed to parse floating point value", e);
			}
			return 0;
		}
	}

	/**
	 * BaseTiffReader is the superclass for file format readers compatible with or
	 * derived from the TIFF 6.0 file format.
	 * 
	 * @author Curtis Rueden ctrueden at wisc.edu
	 * @author Melissa Linkert melissa at glencoesoftware.com
	 */
	public static abstract class BaseTIFFParser extends
		MinimalTIFFFormat.Parser<Metadata>
	{

		// -- Constants --

		public static final String[] DATE_FORMATS = { "yyyy:MM:dd HH:mm:ss",
			"dd/MM/yyyy HH:mm:ss.SS", "MM/dd/yyyy hh:mm:ss.SSS aa",
			"yyyyMMdd HH:mm:ss.SSS", "yyyy/MM/dd HH:mm:ss" };

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{

			super.typedParse(stream, meta);
			initMetadata(meta);
		}

		// -- Internal BaseTiffReader API methods --

		/** Populates the metadata hashtable and metadata store. */
		protected void initMetadata(final Metadata meta) throws FormatException,
			IOException
		{
			initStandardMetadata(meta);
			initMetadataStore(meta);
		}

		/**
		 * Parses standard metadata. NOTE: Absolutely <b>no</b> calls to the
		 * metadata store should be made in this method or methods that override
		 * this method. Data <b>will</b> be overwritten if you do so.
		 */
		protected void initStandardMetadata(final Metadata meta)
			throws FormatException, IOException
		{
			if (getMetadataOptions().getMetadataLevel() == MetadataLevel.MINIMUM) {
				return;
			}

			final IFDList ifds = meta.getIfds();

			for (int i = 0; i < ifds.size(); i++) {
				put("PageName #" + i, ifds.get(i), IFD.PAGE_NAME);
			}

			final IFD firstIFD = ifds.get(0);
			put("ImageWidth", firstIFD, IFD.IMAGE_WIDTH);
			put("ImageLength", firstIFD, IFD.IMAGE_LENGTH);
			put("BitsPerSample", firstIFD, IFD.BITS_PER_SAMPLE);

			// retrieve EXIF values, if available

			if (ifds.get(0).containsKey(IFD.EXIF)) {
				final IFDList exifIFDs = meta.getTiffParser().getExifIFDs();
				if (exifIFDs.size() > 0) {
					final IFD exif = exifIFDs.get(0);
					for (final Integer key : exif.keySet()) {
						final int k = key.intValue();
						addGlobalMeta(getExifTagName(k), exif.get(key));
					}
				}
			}

			final TiffCompression comp = firstIFD.getCompression();
			put("Compression", comp.getCodecName());

			final PhotoInterp photo = firstIFD.getPhotometricInterpretation();
			final String photoInterp = photo.getName();
			final String metaDataPhotoInterp = photo.getMetadataType();
			put("PhotometricInterpretation", photoInterp);
			put("MetaDataPhotometricInterpretation", metaDataPhotoInterp);

			putInt("CellWidth", firstIFD, IFD.CELL_WIDTH);
			putInt("CellLength", firstIFD, IFD.CELL_LENGTH);

			final int or = firstIFD.getIFDIntValue(IFD.ORIENTATION);

			// adjust the width and height if necessary
			if (or == 8) {
				put("ImageWidth", firstIFD, IFD.IMAGE_LENGTH);
				put("ImageLength", firstIFD, IFD.IMAGE_WIDTH);
			}

			String orientation = null;
			// there is no case 0
			switch (or) {
				case 1:
					orientation = "1st row -> top; 1st column -> left";
					break;
				case 2:
					orientation = "1st row -> top; 1st column -> right";
					break;
				case 3:
					orientation = "1st row -> bottom; 1st column -> right";
					break;
				case 4:
					orientation = "1st row -> bottom; 1st column -> left";
					break;
				case 5:
					orientation = "1st row -> left; 1st column -> top";
					break;
				case 6:
					orientation = "1st row -> right; 1st column -> top";
					break;
				case 7:
					orientation = "1st row -> right; 1st column -> bottom";
					break;
				case 8:
					orientation = "1st row -> left; 1st column -> bottom";
					break;
			}
			put("Orientation", orientation);
			putInt("SamplesPerPixel", firstIFD, IFD.SAMPLES_PER_PIXEL);

			put("Software", firstIFD, IFD.SOFTWARE);
			put("Instrument Make", firstIFD, IFD.MAKE);
			put("Instrument Model", firstIFD, IFD.MODEL);
			put("Document Name", firstIFD, IFD.DOCUMENT_NAME);
			put("DateTime", firstIFD, IFD.DATE_TIME);
			put("Artist", firstIFD, IFD.ARTIST);

			put("HostComputer", firstIFD, IFD.HOST_COMPUTER);
			put("Copyright", firstIFD, IFD.COPYRIGHT);

			put("NewSubfileType", firstIFD, IFD.NEW_SUBFILE_TYPE);

			final int thresh = firstIFD.getIFDIntValue(IFD.THRESHHOLDING);
			String threshholding = null;
			switch (thresh) {
				case 1:
					threshholding = "No dithering or halftoning";
					break;
				case 2:
					threshholding = "Ordered dithering or halftoning";
					break;
				case 3:
					threshholding = "Randomized error diffusion";
					break;
			}
			put("Threshholding", threshholding);

			final int fill = firstIFD.getIFDIntValue(IFD.FILL_ORDER);
			String fillOrder = null;
			switch (fill) {
				case 1:
					fillOrder =
						"Pixels with lower column values are stored "
							+ "in the higher order bits of a byte";
					break;
				case 2:
					fillOrder =
						"Pixels with lower column values are stored "
							+ "in the lower order bits of a byte";
					break;
			}
			put("FillOrder", fillOrder);

			putInt("Make", firstIFD, IFD.MAKE);
			putInt("Model", firstIFD, IFD.MODEL);
			putInt("MinSampleValue", firstIFD, IFD.MIN_SAMPLE_VALUE);
			putInt("MaxSampleValue", firstIFD, IFD.MAX_SAMPLE_VALUE);
			putInt("XResolution", firstIFD, IFD.X_RESOLUTION);
			putInt("YResolution", firstIFD, IFD.Y_RESOLUTION);

			final int planar = firstIFD.getIFDIntValue(IFD.PLANAR_CONFIGURATION);
			String planarConfig = null;
			switch (planar) {
				case 1:
					planarConfig = "Chunky";
					break;
				case 2:
					planarConfig = "Planar";
					break;
			}
			put("PlanarConfiguration", planarConfig);

			putInt("XPosition", firstIFD, IFD.X_POSITION);
			putInt("YPosition", firstIFD, IFD.Y_POSITION);
			putInt("FreeOffsets", firstIFD, IFD.FREE_OFFSETS);
			putInt("FreeByteCounts", firstIFD, IFD.FREE_BYTE_COUNTS);
			putInt("GrayResponseUnit", firstIFD, IFD.GRAY_RESPONSE_UNIT);
			putInt("GrayResponseCurve", firstIFD, IFD.GRAY_RESPONSE_CURVE);
			putInt("T4Options", firstIFD, IFD.T4_OPTIONS);
			putInt("T6Options", firstIFD, IFD.T6_OPTIONS);

			final int res = firstIFD.getIFDIntValue(IFD.RESOLUTION_UNIT);
			String resUnit = null;
			switch (res) {
				case 1:
					resUnit = "None";
					break;
				case 2:
					resUnit = "Inch";
					break;
				case 3:
					resUnit = "Centimeter";
					break;
			}
			put("ResolutionUnit", resUnit);

			putInt("PageNumber", firstIFD, IFD.PAGE_NUMBER);
			putInt("TransferFunction", firstIFD, IFD.TRANSFER_FUNCTION);

			final int predict = firstIFD.getIFDIntValue(IFD.PREDICTOR);
			String predictor = null;
			switch (predict) {
				case 1:
					predictor = "No prediction scheme";
					break;
				case 2:
					predictor = "Horizontal differencing";
					break;
			}
			put("Predictor", predictor);

			putInt("WhitePoint", firstIFD, IFD.WHITE_POINT);
			putInt("PrimaryChromacities", firstIFD, IFD.PRIMARY_CHROMATICITIES);

			putInt("HalftoneHints", firstIFD, IFD.HALFTONE_HINTS);
			putInt("TileWidth", firstIFD, IFD.TILE_WIDTH);
			putInt("TileLength", firstIFD, IFD.TILE_LENGTH);
			putInt("TileOffsets", firstIFD, IFD.TILE_OFFSETS);
			putInt("TileByteCounts", firstIFD, IFD.TILE_BYTE_COUNTS);

			final int ink = firstIFD.getIFDIntValue(IFD.INK_SET);
			String inkSet = null;
			switch (ink) {
				case 1:
					inkSet = "CMYK";
					break;
				case 2:
					inkSet = "Other";
					break;
			}
			put("InkSet", inkSet);

			putInt("InkNames", firstIFD, IFD.INK_NAMES);
			putInt("NumberOfInks", firstIFD, IFD.NUMBER_OF_INKS);
			putInt("DotRange", firstIFD, IFD.DOT_RANGE);
			put("TargetPrinter", firstIFD, IFD.TARGET_PRINTER);
			putInt("ExtraSamples", firstIFD, IFD.EXTRA_SAMPLES);

			final int fmt = firstIFD.getIFDIntValue(IFD.SAMPLE_FORMAT);
			String sampleFormat = null;
			switch (fmt) {
				case 1:
					sampleFormat = "unsigned integer";
					break;
				case 2:
					sampleFormat = "two's complement signed integer";
					break;
				case 3:
					sampleFormat = "IEEE floating point";
					break;
				case 4:
					sampleFormat = "undefined";
					break;
			}
			put("SampleFormat", sampleFormat);

			putInt("SMinSampleValue", firstIFD, IFD.S_MIN_SAMPLE_VALUE);
			putInt("SMaxSampleValue", firstIFD, IFD.S_MAX_SAMPLE_VALUE);
			putInt("TransferRange", firstIFD, IFD.TRANSFER_RANGE);

			final int jpeg = firstIFD.getIFDIntValue(IFD.JPEG_PROC);
			String jpegProc = null;
			switch (jpeg) {
				case 1:
					jpegProc = "baseline sequential process";
					break;
				case 14:
					jpegProc = "lossless process with Huffman coding";
					break;
			}
			put("JPEGProc", jpegProc);

			putInt("JPEGInterchangeFormat", firstIFD, IFD.JPEG_INTERCHANGE_FORMAT);
			putInt("JPEGRestartInterval", firstIFD, IFD.JPEG_RESTART_INTERVAL);

			putInt("JPEGLosslessPredictors", firstIFD, IFD.JPEG_LOSSLESS_PREDICTORS);
			putInt("JPEGPointTransforms", firstIFD, IFD.JPEG_POINT_TRANSFORMS);
			putInt("JPEGQTables", firstIFD, IFD.JPEG_Q_TABLES);
			putInt("JPEGDCTables", firstIFD, IFD.JPEG_DC_TABLES);
			putInt("JPEGACTables", firstIFD, IFD.JPEG_AC_TABLES);
			putInt("YCbCrCoefficients", firstIFD, IFD.Y_CB_CR_COEFFICIENTS);

			final int ycbcr = firstIFD.getIFDIntValue(IFD.Y_CB_CR_SUB_SAMPLING);
			String subSampling = null;
			switch (ycbcr) {
				case 1:
					subSampling = "chroma image dimensions = luma image dimensions";
					break;
				case 2:
					subSampling =
						"chroma image dimensions are " + "half the luma image dimensions";
					break;
				case 4:
					subSampling =
						"chroma image dimensions are " + "1/4 the luma image dimensions";
					break;
			}
			put("YCbCrSubSampling", subSampling);

			putInt("YCbCrPositioning", firstIFD, IFD.Y_CB_CR_POSITIONING);
			putInt("ReferenceBlackWhite", firstIFD, IFD.REFERENCE_BLACK_WHITE);

			// bits per sample and number of channels
			final int[] q = firstIFD.getBitsPerSample();
			final int bps = q[0];
			int numC = q.length;

			// numC isn't set properly if we have an indexed color image, so we need
			// to reset it here

			if (photo == PhotoInterp.RGB_PALETTE || photo == PhotoInterp.CFA_ARRAY) {
				numC = 3;
			}

			put("BitsPerSample", bps);
			put("NumberOfChannels", numC);
		}

		/**
		 * Populates the metadata store using the data parsed in
		 * {@link #initStandardMetadata()} along with some further parsing done in
		 * the method itself. All calls to the active <code>MetadataStore</code>
		 * should be made in this method and <b>only</b> in this method. This is
		 * especially important for sub-classes that override the getters for pixel
		 * set array size, etc.
		 */
		protected void initMetadataStore(final Metadata meta)
			throws FormatException
		{
			log().info("Populating OME metadata");

			final IFD firstIFD = meta.getIfds().get(0);

			// format the creation date to ISO 8601

			final String creationDate = getImageCreationDate(meta);
			final String date = DateTools.formatDate(creationDate, DATE_FORMATS);
			if (creationDate != null && date == null) {
				log().warn("unknown creation date format: " + creationDate);
			}

			meta.setCreationDate(date);

			if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
				// populate Experimenter
				final String artist = firstIFD.getIFDTextValue(IFD.ARTIST);

				if (artist != null) {
					String firstName = null, lastName = null;
					final int ndx = artist.indexOf(" ");
					if (ndx < 0) lastName = artist;
					else {
						firstName = artist.substring(0, ndx);
						lastName = artist.substring(ndx + 1);
					}
					final String email = firstIFD.getIFDStringValue(IFD.HOST_COMPUTER);
					meta.setExperimenterFirstName(firstName);
					meta.setExperimenterLastName(lastName);
					meta.setExperimenterEmail(email);
				}

				meta.setImageDescription(firstIFD.getComment());

				// set the X and Y pixel dimensions

				final double pixX = firstIFD.getXResolution();
				final double pixY = firstIFD.getYResolution();

				if (pixX > 0 && pixX < Double.POSITIVE_INFINITY) {
					meta.setPhysicalSizeX(pixX);
				}
				else {
					log().warn("Expected positive value for PhysicalSizeX; got " + pixX);
				}
				if (pixY > 0 && pixX < Double.POSITIVE_INFINITY) {
					meta.setPhysicalSizeY(pixY);
				}
				else {
					log().warn("Expected positive value for PhysicalSizeY; got " + pixY);
				}
				// meta.setPixelsPhysicalSizeZ(null, 0);
			}
		}

		/**
		 * Retrieves the image creation date.
		 * 
		 * @return the image creation date.
		 */
		protected String getImageCreationDate(final Metadata meta) {
			final Object o = meta.getIfds().get(0).getIFDValue(IFD.DATE_TIME);
			if (o instanceof String) return (String) o;
			if (o instanceof String[]) return ((String[]) o)[0];
			return null;
		}

		// -- Internal FormatReader API methods - metadata convenience --

		// TODO : the 'put' methods that accept primitive types could probably be
		// removed, as there are now 'addGlobalMeta' methods that accept
		// primitive types

		protected void put(final String key, Object value) {
			if (value == null) return;
			if (value instanceof String) value = ((String) value).trim();
			addGlobalMeta(key, value);
		}

		protected void put(final String key, final int value) {
			if (value == -1) return; // indicates missing value
			addGlobalMeta(key, value);
		}

		protected void put(final String key, final boolean value) {
			put(key, new Boolean(value));
		}

		protected void put(final String key, final byte value) {
			put(key, new Byte(value));
		}

		protected void put(final String key, final char value) {
			put(key, new Character(value));
		}

		protected void put(final String key, final double value) {
			put(key, new Double(value));
		}

		protected void put(final String key, final float value) {
			put(key, new Float(value));
		}

		protected void put(final String key, final long value) {
			put(key, new Long(value));
		}

		protected void put(final String key, final short value) {
			put(key, new Short(value));
		}

		protected void put(final String key, final IFD ifd, final int tag) {
			put(key, ifd.getIFDValue(tag));
		}

		protected void putInt(final String key, final IFD ifd, final int tag) {
			put(key, ifd.getIFDIntValue(tag));
		}

		// -- Helper methods --

		public static String getExifTagName(final int tag) {
			return IFD.getIFDTagName(tag);
		}
	}

	/**
	 * TiffReader is the file format reader for regular TIFF files, not of any
	 * specific TIFF variant.
	 * 
	 * @author Curtis Rueden ctrueden at wisc.edu
	 * @author Melissa Linkert melissa at glencoesoftware.com
	 */
	public static class Reader<M extends Metadata> extends
		MinimalTIFFFormat.Reader<M>
	{

	}

	/**
	 * TiffWriter is the file format writer for TIFF files.
	 */
	public static class Writer<M extends Metadata> extends AbstractWriter<M> {

		// -- Constants --

		public static final String COMPRESSION_UNCOMPRESSED =
			CompressionType.UNCOMPRESSED.getCompression();
		public static final String COMPRESSION_LZW = CompressionType.LZW
			.getCompression();
		public static final String COMPRESSION_J2K = CompressionType.J2K
			.getCompression();
		public static final String COMPRESSION_J2K_LOSSY =
			CompressionType.J2K_LOSSY.getCompression();
		public static final String COMPRESSION_JPEG = CompressionType.JPEG
			.getCompression();

		// -- Fields --

		/** Whether or not the output file is a BigTIFF file. */
		protected boolean isBigTiff;

		/** The TiffSaver that will do most of the writing. */
		protected TiffSaver tiffSaver;

		/** Input stream to use when overwriting data. */
		protected RandomAccessInputStream in;

		/** Whether or not to check the parameters passed to saveBytes. */
		private boolean checkParams = true;

		// -- Constructor --

		public Writer() {
			compressionTypes =
				new String[] { COMPRESSION_UNCOMPRESSED, COMPRESSION_LZW,
					COMPRESSION_J2K, COMPRESSION_J2K_LOSSY, COMPRESSION_JPEG };
			isBigTiff = false;
		}

		// -- TIFFWriter API Methods --

		/**
		 * Sets whether or not BigTIFF files should be written. This flag is not
		 * reset when close() is called.
		 */
		public void setBigTiff(final boolean bigTiff) {
			isBigTiff = bigTiff;
		}

		/**
		 * Saves the given image to the specified (possibly already open) file. The
		 * IFD hashtable allows specification of TIFF parameters such as bit depth,
		 * compression and units.
		 */
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final IFD ifd) throws IOException, FormatException
		{
			final Metadata meta = getMetadata();
			final int w = meta.getAxisLength(imageIndex, Axes.X);
			final int h = meta.getAxisLength(imageIndex, Axes.Y);
			savePlane(imageIndex, planeIndex, plane, ifd, 0, 0, w, h);
		}

		/**
		 * Saves the given image to the specified series in the current file. The
		 * IFD hashtable allows specification of TIFF parameters such as bit depth,
		 * compression and units.
		 */
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, IFD ifd, final int x, final int y, final int w,
			final int h) throws IOException, FormatException
		{
			final byte[] buf = plane.getBytes();
			if (checkParams) checkParams(imageIndex, planeIndex, buf, x, y, w, h);
			if (ifd == null) ifd = new IFD(log());
			final int type = getMetadata().getPixelType(imageIndex);
			int index = planeIndex;
			// This operation is synchronized
			synchronized (this) {
				// This operation is synchronized against the TIFF saver.
				synchronized (tiffSaver) {
					index =
						prepareToWriteImage(imageIndex, planeIndex, plane, ifd, x, y, w, h);
					if (index == -1) {
						return;
					}
				}
			}

			tiffSaver.writeImage(buf, ifd, index, type, x, y, w, h,
				planeIndex == getMetadata().get(imageIndex).getPlaneCount() - 1 &&
					imageIndex == getMetadata().getImageCount() - 1);
		}

		// -- Writer API Methods --

		@Override
		public void setDest(final RandomAccessOutputStream dest,
			final int imageIndex) throws FormatException, IOException
		{
			super.setDest(dest, imageIndex);
			synchronized (this) {
				setupTiffSaver(imageIndex);
			}
		}

		/*
		 * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
		 */
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final int x, final int y, final int w, final int h)
			throws FormatException, IOException
		{
			IFD ifd = new IFD(log());
			if (!sequential) {
				final TiffParser parser =
					new TiffParser(getContext(), getMetadata().getDatasetName());
				try {
					final long[] ifdOffsets = parser.getIFDOffsets();
					if (planeIndex < ifdOffsets.length) {
						ifd = parser.getIFD(ifdOffsets[planeIndex]);
					}
				}
				finally {
					final RandomAccessInputStream tiffParserStream = parser.getStream();
					if (tiffParserStream != null) {
						tiffParserStream.close();
					}
				}
			}

			savePlane(imageIndex, planeIndex, plane, ifd, x, y, w, h);
		}

		/* @see loci.formats.IFormatWriter#canDoStacks(String) */
		@Override
		public boolean canDoStacks() {
			return true;
		}

		/* @see loci.formats.IFormatWriter#getPixelTypes(String) */
		@Override
		public int[] getPixelTypes(final String codec) {
			if (codec != null && codec.equals(COMPRESSION_JPEG)) {
				return new int[] { FormatTools.INT8, FormatTools.UINT8,
					FormatTools.INT16, FormatTools.UINT16 };
			}
			else if (codec != null && codec.equals(COMPRESSION_J2K)) {
				return new int[] { FormatTools.INT8, FormatTools.UINT8,
					FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
					FormatTools.UINT32, FormatTools.FLOAT };
			}
			return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
				FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE };
		}

		@Override
		public int getPlaneCount(final int imageIndex) {
			final Metadata meta = getMetadata();
			final int c = meta.getRGBChannelCount(imageIndex);
			final int type = meta.getPixelType(imageIndex);
			final int bytesPerPixel = FormatTools.getBytesPerPixel(type);

			if (bytesPerPixel > 1 && c != 1 && c != 3) {
				return super.getPlaneCount(imageIndex) * c;
			}
			return super.getPlaneCount(imageIndex);
		}

		@Override
		public void close() throws IOException {
			super.close();
			if (in != null) {
				in.close();
			}
		}

		// -- Helper methods --

		/**
		 * Sets the compression code for the specified IFD.
		 * 
		 * @param ifd The IFD table to handle.
		 */
		private void formatCompression(final IFD ifd) throws FormatException {
			if (compression == null) compression = "";
			TiffCompression compressType = TiffCompression.UNCOMPRESSED;
			if (compression.equals(COMPRESSION_LZW)) {
				compressType = TiffCompression.LZW;
			}
			else if (compression.equals(COMPRESSION_J2K)) {
				compressType = TiffCompression.JPEG_2000;
			}
			else if (compression.equals(COMPRESSION_J2K_LOSSY)) {
				compressType = TiffCompression.JPEG_2000_LOSSY;
			}
			else if (compression.equals(COMPRESSION_JPEG)) {
				compressType = TiffCompression.JPEG;
			}
			final Object v = ifd.get(new Integer(IFD.COMPRESSION));
			if (v == null) ifd.put(new Integer(IFD.COMPRESSION), compressType
				.getCode());
		}

		/**
		 * Performs the preparation for work prior to the usage of the TIFF saver.
		 * This method is factored out from <code>saveBytes()</code> in an attempt
		 * to ensure thread safety.
		 */
		private int prepareToWriteImage(final int imageIndex, final int planeIndex,
			final Plane plane, final IFD ifd, final int x, final int y, final int w,
			final int h) throws IOException, FormatException
		{
			final byte[] buf = plane.getBytes();
			final Metadata meta = getMetadata();
			final Boolean bigEndian = !meta.isLittleEndian(imageIndex);
			final boolean littleEndian =
				bigEndian == null ? false : !bigEndian.booleanValue();

			// Ensure that no more than one thread manipulated the initialized array
			// at one time.
			synchronized (this) {
				if (planeIndex < initialized[imageIndex].length &&
					!initialized[imageIndex][planeIndex])
				{
					initialized[imageIndex][planeIndex] = true;

					final RandomAccessInputStream tmp =
						new RandomAccessInputStream(getContext(), meta.getDatasetName());
					if (tmp.length() == 0) {
						synchronized (this) {
							// write TIFF header
							tiffSaver.writeHeader();
						}
					}
					tmp.close();
				}
			}

			final int type = meta.getPixelType(imageIndex);
			int c = meta.getRGBChannelCount(imageIndex);
			final int bytesPerPixel = FormatTools.getBytesPerPixel(type);

			final int blockSize = w * h * c * bytesPerPixel;
			if (blockSize > buf.length) {
				c = buf.length / (w * h * bytesPerPixel);
			}

			if (bytesPerPixel > 1 && c != 1 && c != 3) {
				// split channels
				checkParams = false;

				if (planeIndex == 0) {
					initialized[imageIndex] =
						new boolean[initialized[imageIndex].length * c];
				}

				for (int i = 0; i < c; i++) {
					final byte[] b =
						ImageTools.splitChannels(buf, i, c, bytesPerPixel, false,
							interleaved);

					final ByteArrayPlane bp = new ByteArrayPlane(getContext());
					bp.populate(getMetadata().get(imageIndex), b, x, y, w, h);

					savePlane(imageIndex, planeIndex * c + i, bp, (IFD) ifd.clone(), x,
						y, w, h);
				}
				checkParams = true;
				return -1;
			}

			formatCompression(ifd);
			final byte[][] lut = AWTImageTools.get8BitLookupTable(cm);
			if (lut != null) {
				final int[] colorMap = new int[lut.length * lut[0].length];
				for (int i = 0; i < lut.length; i++) {
					for (int j = 0; j < lut[0].length; j++) {
						colorMap[i * lut[0].length + j] = (lut[i][j] & 0xff) << 8;
					}
				}
				ifd.putIFDValue(IFD.COLOR_MAP, colorMap);
			}

			final int width = meta.getAxisLength(imageIndex, Axes.X);
			final int height = meta.getAxisLength(imageIndex, Axes.Y);
			ifd.put(new Integer(IFD.IMAGE_WIDTH), new Long(width));
			ifd.put(new Integer(IFD.IMAGE_LENGTH), new Long(height));

			Double physicalSizeX = meta.getPhysicalSizeX();
			if (physicalSizeX == null || physicalSizeX.doubleValue() == 0) {
				physicalSizeX = 0d;
			}
			else physicalSizeX = 1d / physicalSizeX;

			Double physicalSizeY = meta.getPhysicalSizeY();
			if (physicalSizeY == null || physicalSizeY.doubleValue() == 0) {
				physicalSizeY = 0d;
			}
			else physicalSizeY = 1d / physicalSizeY;

			ifd.put(IFD.RESOLUTION_UNIT, 3);
			ifd.put(IFD.X_RESOLUTION, new TiffRational(
				(long) (physicalSizeX * 1000 * 10000), 1000));
			ifd.put(IFD.Y_RESOLUTION, new TiffRational(
				(long) (physicalSizeY * 1000 * 10000), 1000));

			if (!isBigTiff) {
				isBigTiff =
					(out.length() + 2 * (width * height * c * bytesPerPixel)) >= 4294967296L;
				if (isBigTiff) {
					throw new FormatException("File is too large; call setBigTiff(true)");
				}
			}

			// write the image
			ifd.put(new Integer(IFD.LITTLE_ENDIAN), new Boolean(littleEndian));
			if (!ifd.containsKey(IFD.REUSE)) {
				ifd.put(IFD.REUSE, out.length());
				out.seek(out.length());
			}
			else {
				out.seek((Long) ifd.get(IFD.REUSE));
			}

			ifd.putIFDValue(IFD.PLANAR_CONFIGURATION, interleaved ||
				meta.getRGBChannelCount(imageIndex) == 1 ? 1 : 2);

			int sampleFormat = 1;
			if (FormatTools.isSigned(type)) sampleFormat = 2;
			if (FormatTools.isFloatingPoint(type)) sampleFormat = 3;
			ifd.putIFDValue(IFD.SAMPLE_FORMAT, sampleFormat);

			int index = planeIndex;
			final int realSeries = imageIndex;
			for (int i = 0; i < realSeries; i++) {
				index += meta.getPlaneCount(i);
			}
			return index;
		}

		private void setupTiffSaver(final int imageIndex) throws IOException {
			out.close();
			final Metadata meta = getMetadata();
			out = new RandomAccessOutputStream(getContext(), meta.getDatasetName());
			tiffSaver = new TiffSaver(getContext(), out, meta.getDatasetName());

			final Boolean bigEndian = !meta.isLittleEndian(imageIndex);
			final boolean littleEndian =
				bigEndian == null ? false : !bigEndian.booleanValue();

			tiffSaver.setWritingSequentially(sequential);
			tiffSaver.setLittleEndian(littleEndian);
			tiffSaver.setBigTiff(isBigTiff);
			tiffSaver.setCodecOptions(options);
		}

	}

	/**
	 * This class can be used for translating any io.scif.Metadata to Metadata for
	 * writing TIFF. files.
	 * <p>
	 * Note that Metadata translated from Core is only write-safe.
	 * </p>
	 * <p>
	 * If trying to read, there should already exist an originally-parsed TIFF
	 * Metadata object which can be used.
	 * </p>
	 * <p>
	 * Note also that any TIFF image written must be reparsed, as the Metadata
	 * used to write it can not be guaranteed valid.
	 * </p>
	 */
	@Plugin(type = Translator.class, attrs = {
		@Attr(name = TIFFTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = TIFFTranslator.DEST, value = Metadata.CNAME) },
		priority = TIFFFormat.PRIORITY)
	public static class TIFFTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			final IFDList ifds = new IFDList();
			dest.setIfds(ifds);

			final ImageMetadata m = source.get(0);

			for (int i = 0; i < m.getPlaneCount(); i++)
				ifds.add(new IFD(log()));

			final IFD firstIFD = ifds.get(0);

			firstIFD.putIFDValue(IFD.BITS_PER_SAMPLE,
				new int[] { m.getBitsPerPixel() });
			firstIFD.putIFDValue(IFD.SAMPLE_FORMAT, FormatTools.isSigned(m
				.getPixelType()) ? 2 : 1);
			firstIFD.putIFDValue(IFD.LITTLE_ENDIAN, m.isLittleEndian());
			firstIFD.putIFDValue(IFD.IMAGE_WIDTH, m.getAxisLength(Axes.X));
			firstIFD.putIFDValue(IFD.IMAGE_LENGTH, m.getAxisLength(Axes.Y));
			firstIFD.putIFDValue(IFD.SAMPLES_PER_PIXEL, m.getRGBChannelCount());

			firstIFD.putIFDValue(IFD.PHOTOMETRIC_INTERPRETATION,
				PhotoInterp.BLACK_IS_ZERO);
			if (m.isRGB()) firstIFD.putIFDValue(IFD.PHOTOMETRIC_INTERPRETATION,
				PhotoInterp.RGB);
			if (m.isIndexed() &&
				HasColorTable.class.isAssignableFrom(source.getClass()))
			{
				firstIFD.putIFDValue(IFD.PHOTOMETRIC_INTERPRETATION,
					PhotoInterp.RGB_PALETTE);

				final ColorTable table = ((HasColorTable) source).getColorTable(0, 0);
				final int[] flattenedTable =
					new int[table.getComponentCount() * table.getLength()];

				for (int i = 0; i < table.getComponentCount(); i++) {
					for (int j = 0; j < table.getLength(); j++) {
						flattenedTable[(i * table.getLength()) + j] = table.get(i, j);
					}
				}

				firstIFD.putIFDValue(IFD.COLOR_MAP, flattenedTable);
			}
		}
	}
}
