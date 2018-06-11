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
import io.scif.FilePattern;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetadataLevel;
import io.scif.UnsupportedCompressionException;
import io.scif.codec.Codec;
import io.scif.codec.CodecOptions;
import io.scif.codec.CodecService;
import io.scif.codec.JPEG2000Codec;
import io.scif.codec.JPEGCodec;
import io.scif.codec.PackbitsCodec;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.dicom.DICOMDictionary;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;
import org.scijava.util.DigestUtils;

/**
 * DICOMReader is the file format reader for DICOM files. Much of this code is
 * adapted from
 * <a href="http://imagej.net/developer/source/ij/plugin/DICOM.java.html">ImageJ
 * 's DICOM reader</a>.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "DICOM")
public class DICOMFormat extends AbstractFormat {

	// -- Constants --

	public static final String DICOM_MAGIC_STRING = "DICM";

	private static final DICOMDictionary TYPES = new DICOMDictionary();

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "dic", "dcm", "dicom", "jp2", "j2ki", "j2kr", "raw",
			"ima" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		byte[][] lut = null;
		short[][] shortLut = null;
		private ColorTable8 lut8;
		private ColorTable16 lut16;
		private long[] offsets = null;
		private boolean isJP2K = false;
		private boolean isJPEG = false;
		private boolean isRLE = false;
		private boolean isDeflate = false;
		private boolean oddLocations = false;
		private int maxPixelValue;
		private int imagesPerFile = 0;
		private double rescaleSlope = 1.0, rescaleIntercept = 0.0;
		private Hashtable<Integer, Vector<String>> fileList;
		private boolean inverted = false;

		private String pixelSizeX, pixelSizeY;
		private Double pixelSizeZ;

		private String date, time, imageType;
		private String originalDate, originalTime, originalInstance;
		private int originalSeries;

		private Vector<String> companionFiles = new Vector<>();

		// Getters and Setters

		public long[] getOffsets() {
			return offsets;
		}

		public void setOffsets(final long[] offsets) {
			this.offsets = offsets;
		}

		public double getRescaleSlope() {
			return rescaleSlope;
		}

		public void setRescaleSlope(final double rescaleSlope) {
			this.rescaleSlope = rescaleSlope;
		}

		public double getRescaleIntercept() {
			return rescaleIntercept;
		}

		public void setRescaleIntercept(final double rescaleIntercept) {
			this.rescaleIntercept = rescaleIntercept;
		}

		public String getPixelSizeX() {
			return pixelSizeX;
		}

		public void setPixelSizeX(final String pixelSizeX) {
			this.pixelSizeX = pixelSizeX;
		}

		public String getPixelSizeY() {
			return pixelSizeY;
		}

		public void setPixelSizeY(final String pixelSizeY) {
			this.pixelSizeY = pixelSizeY;
		}

		public Double getPixelSizeZ() {
			return pixelSizeZ;
		}

		public void setPixelSizeZ(final Double pixelSizeZ) {
			this.pixelSizeZ = pixelSizeZ;
		}

		public boolean isInverted() {
			return inverted;
		}

		public void setInverted(final boolean inverted) {
			this.inverted = inverted;
		}

		public boolean isJP2K() {
			return isJP2K;
		}

		public void setJP2K(final boolean isJP2K) {
			this.isJP2K = isJP2K;
		}

		public boolean isJPEG() {
			return isJPEG;
		}

		public void setJPEG(final boolean isJPEG) {
			this.isJPEG = isJPEG;
		}

		public boolean isRLE() {
			return isRLE;
		}

		public void setRLE(final boolean isRLE) {
			this.isRLE = isRLE;
		}

		public boolean isDeflate() {
			return isDeflate;
		}

		public void setDeflate(final boolean isDeflate) {
			this.isDeflate = isDeflate;
		}

		public boolean isOddLocations() {
			return oddLocations;
		}

		public void setOddLocations(final boolean oddLocations) {
			this.oddLocations = oddLocations;
		}

		public int getMaxPixelValue() {
			return maxPixelValue;
		}

		public void setMaxPixelValue(final int maxPixelValue) {
			this.maxPixelValue = maxPixelValue;
		}

		public int getImagesPerFile() {
			return imagesPerFile;
		}

		public void setImagesPerFile(final int imagesPerFile) {
			this.imagesPerFile = imagesPerFile;
		}

		public Hashtable<Integer, Vector<String>> getFileList() {
			return fileList;
		}

		public void setFileList(final Hashtable<Integer, Vector<String>> fileList) {
			this.fileList = fileList;
		}

		public String getDate() {
			return date;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public String getTime() {
			return time;
		}

		public void setTime(final String time) {
			this.time = time;
		}

		public String getImageType() {
			return imageType;
		}

		public void setImageType(final String imageType) {
			this.imageType = imageType;
		}

		public String getOriginalDate() {
			return originalDate;
		}

		public void setOriginalDate(final String originalDate) {
			this.originalDate = originalDate;
		}

		public String getOriginalTime() {
			return originalTime;
		}

		public void setOriginalTime(final String originalTime) {
			this.originalTime = originalTime;
		}

		public String getOriginalInstance() {
			return originalInstance;
		}

		public void setOriginalInstance(final String originalInstance) {
			this.originalInstance = originalInstance;
		}

		public int getOriginalSeries() {
			return originalSeries;
		}

		public void setOriginalSeries(final int originalSeries) {
			this.originalSeries = originalSeries;
		}

		public Vector<String> getCompanionFiles() {
			return companionFiles;
		}

		public void setCompanionFiles(final Vector<String> companionFiles) {
			this.companionFiles = companionFiles;
		}

		// -- ColorTable API Methods --

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			final int pixelType = get(0).getPixelType();

			switch (pixelType) {
				case FormatTools.INT8:
				case FormatTools.UINT8:
					if (lut8 == null) {
						// Need to create the lut8
						if (isInverted()) {
							// If inverted then lut shall be inverted
							if (lut == null) {
								// If lut does not exists create an inverted one
								lut = createInvertedLut8();
							}
							else {
								// If lut does exists inverted it
								invertLut8(lut);
							}
						}
						if (lut != null) {
							lut8 = new ColorTable8(lut);
						}
					}
					return lut8;
				case FormatTools.INT16:
				case FormatTools.UINT16:
					if (lut16 == null) {
						// Need to create the lut16
						if (isInverted()) {
							// If inverted then lut shall be inverted
							if (shortLut == null) {
								// If lut does not exists create an inverted one
								shortLut = createInvertedLut16();
							}
							else {
								// If lut does exists inverted it
								invertLut16(shortLut);
							}
						}
						if (shortLut != null) {
							lut16 = new ColorTable16(shortLut);
						}
					}
					return lut16;
			}

			return null;
		}

		private static byte[][] createInvertedLut8() {
			final byte[][] lut = new byte[3][256];
			for (int i = 0; i < lut.length; i++) {
				for (int j = 0; j < lut[i].length; j++) {
					lut[i][lut[i].length - 1 - j] = (byte) (j & 0xff);
				}
			}
			return lut;
		}

		private static void invertLut8(final byte[][] lut) {
			for (int i = 0; i < lut.length; i++) {
				for (int j = 0; j < lut[i].length; j++) {
					final byte v0 = lut[i][j];
					final byte v1 = lut[i][lut[i].length - 1 - j];
					lut[i][i] = v1;
					lut[i][lut[i].length - 1 - j] = v0;
				}
			}
		}

		private static short[][] createInvertedLut16() {
			final short[][] lut = new short[3][65536];
			for (int i = 0; i < lut.length; i++) {
				for (int j = 0; j < lut[i].length; j++) {
					lut[i][lut[i].length - 1 - j] = (short) (j & 0xffff);
				}
			}
			return lut;
		}

		private static void invertLut16(final short[][] lut) {
			for (int i = 0; i < lut.length; i++) {
				for (int j = 0; j < lut[i].length; j++) {
					final short v0 = lut[i][j];
					final short v1 = lut[i][lut[i].length - 1 - j];
					lut[i][i] = v1;
					lut[i][lut[i].length - 1 - j] = v0;
				}
			}
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			log().info("Populating metadata");

			// TODO this isn't going to work because each parsing will
			// get the same filelist size and repeat infinitely
			final int seriesCount = fileList.size();

			final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);

			for (int i = 0; i < seriesCount; i++) {
				get(i).setAxisTypes(Axes.X, Axes.Y);
				int sizeZ = 0;
				if (seriesCount == 1) {
					sizeZ = getOffsets().length * fileList.get(keys[i]).size();

					get(i).setMetadataComplete(true);
					get(i).setFalseColor(false);
					if (isRLE) {
						get(i).setAxisTypes(Axes.X, Axes.Y, Axes.CHANNEL);
					}
					if (get(i).getAxisLength(Axes.CHANNEL) > 1) {
						get(i).setPlanarAxisCount(3);
					}
					else {
						get(i).setPlanarAxisCount(2);
					}
				}
				else {

					try {
						final Parser p = (Parser) getFormat().createParser();
						final Metadata m = p.parse(fileList.get(keys[i]).get(0),
							new SCIFIOConfig().groupableSetGroupFiles(false));
						add(m.get(0));
						sizeZ *= fileList.get(keys[i]).size();
					}
					catch (final IOException e) {
						log().error("Error creating Metadata from DICOM companion files.",
							e);
					}
					catch (final FormatException e) {
						log().error("Error creating Metadata from DICOM companion files.",
							e);
					}

				}

				get(i).setAxisLength(Axes.Z, sizeZ);
			}
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				oddLocations = false;
				isJPEG = isJP2K = isRLE = isDeflate = false;
				lut = null;
				offsets = null;
				shortLut = null;
				maxPixelValue = 0;
				rescaleSlope = 1.0;
				rescaleIntercept = 0.0;
				pixelSizeX = pixelSizeY = null;
				pixelSizeZ = null;
				imagesPerFile = 0;
				fileList = null;
				inverted = false;
				date = time = imageType = null;
				originalDate = originalTime = originalInstance = null;
				originalSeries = 0;
				// TODO the resetting is a bit too aggressive, perhaps it should just
				// clear out fields..
				// companionFiles.clear();
			}
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final String[] DICOM_SUFFIXES = { "dic", "dcm", "dicom",
			"j2ki", "j2kr" };

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
			// extension is sufficient as long as it is DIC, DCM, DICOM, J2KI, or J2KR
			if (FormatTools.checkSuffix(name, DICOM_SUFFIXES)) return true;
			return super.isFormat(name, config);
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 2048;
			if (!FormatTools.validStream(stream, blockLen, true)) return false;

			stream.seek(128);
			if (stream.readString(4).equals(DICOM_MAGIC_STRING)) return true;
			stream.seek(0);

			try {
				final int tag = DICOMUtils.getNextTag(stream).get();
				return TYPES.has(tag);
			}
			catch (final NullPointerException e) {}
			catch (final FormatException e) {}
			return false;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		private static final int PIXEL_REPRESENTATION = 0x00280103;
		private static final int PIXEL_SIGN = 0x00281041;
		private static final int TRANSFER_SYNTAX_UID = 0x00020010;
		private static final int SLICE_SPACING = 0x00180088;
		private static final int SAMPLES_PER_PIXEL = 0x00280002;
		private static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
		private static final int PLANAR_CONFIGURATION = 0x00280006;
		private static final int NUMBER_OF_FRAMES = 0x00280008;
		private static final int ROWS = 0x00280010;
		private static final int COLUMNS = 0x00280011;
		private static final int PIXEL_SPACING = 0x00280030;
		private static final int BITS_ALLOCATED = 0x00280100;
		private static final int WINDOW_CENTER = 0x00281050;
		private static final int WINDOW_WIDTH = 0x00281051;
		private static final int RESCALE_INTERCEPT = 0x00281052;
		private static final int RESCALE_SLOPE = 0x00281053;
		private static final int ICON_IMAGE_SEQUENCE = 0x00880200;
		private static final int ITEM = 0xFFFEE000;
		private static final int ITEM_DELIMINATION = 0xFFFEE00D;
		private static final int SEQUENCE_DELIMINATION = 0xFFFEE0DD;
		private static final int PIXEL_DATA = 0x7FE00010;

		@Parameter
		private CodecService codecService;

		// -- Parser API Methods --

		@Override
		public int fileGroupOption(final String id) throws FormatException,
			IOException
		{
			return FormatTools.CAN_GROUP;
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			meta.createImageMetadata(1);

			stream.order(true);

			final ImageMetadata iMeta = meta.get(0);

			// look for companion files
			final Vector<String> companionFiles = new Vector<>();
			attachCompanionFiles(companionFiles);
			meta.setCompanionFiles(companionFiles);

			int location = 0;
			boolean isJP2K = false;
			boolean isJPEG = false;
			boolean isRLE = false;
			boolean isDeflate = false;
			boolean oddLocations = false;
			int maxPixelValue = -1;
			int imagesPerFile = 0;
			boolean bigEndianTransferSyntax = false;
			long[] offsets = null;

			int sizeX = 0;
			int sizeY = 0;
			int bitsPerPixel = 0;
			boolean interleaved;

			// some DICOM files have a 128 byte header followed by a 4 byte identifier

			log().info("Verifying DICOM format");
			final MetadataLevel level = config.parserGetLevel();

			getSource().seek(128);
			if (getSource().readString(4).equals("DICM")) {
				if (level != MetadataLevel.MINIMUM) {
					// header exists, so we'll read it
					getSource().seek(0);
					meta.getTable().put("Header information", getSource().readString(
						128));
					getSource().skipBytes(4);
				}
				location = 128;
			}
			else getSource().seek(0);

			log().info("Reading tags");

			long baseOffset = 0;

			boolean decodingTags = true;
			boolean signed = false;

			while (decodingTags) {
				if (getSource().getFilePointer() + 4 >= getSource().length()) {
					break;
				}
				log().debug("Reading tag from " + getSource().getFilePointer());
				final DICOMTag tag = DICOMUtils.getNextTag(getSource(),
					bigEndianTransferSyntax, oddLocations);
				iMeta.setLittleEndian(tag.isLittleEndian());

				if (tag.getElementLength() <= 0) continue;

				oddLocations = (location & 1) != 0;

				log().debug("  tag=" + tag.get() + " len=" + tag.getElementLength() +
					" fp=" + getSource().getFilePointer());

				String s = null;
				short ss;
				switch (tag.get()) {
					case TRANSFER_SYNTAX_UID:
						// this tag can indicate which compression scheme is used
						s = getSource().readString(tag.getElementLength());
						addInfo(meta, tag, s);
						if (s.startsWith("1.2.840.10008.1.2.4.9")) isJP2K = true;
						else if (s.startsWith("1.2.840.10008.1.2.4")) isJPEG = true;
						else if (s.startsWith("1.2.840.10008.1.2.5")) isRLE = true;
						else if (s.equals("1.2.8.10008.1.2.1.99")) isDeflate = true;
						else if (s.contains("1.2.4") || s.contains("1.2.5")) {
							throw new UnsupportedCompressionException(
								"Sorry, compression type " + s + " not supported");
						}
						if (s.contains("1.2.840.10008.1.2.2")) {
							bigEndianTransferSyntax = true;
						}
						break;
					case NUMBER_OF_FRAMES:
						s = getSource().readString(tag.getElementLength());
						addInfo(meta, tag, s);
						final double frames = Double.parseDouble(s);
						if (frames > 1.0) imagesPerFile = (int) frames;
						break;
					case SAMPLES_PER_PIXEL:
						addInfo(meta, tag, getSource().readShort());
						break;
					case PLANAR_CONFIGURATION:
						final int configuration = getSource().readShort();
						interleaved = configuration == 0;
						if (interleaved) {
							iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
							iMeta.setPlanarAxisCount(3);
						}
						addInfo(meta, tag, configuration);
						break;
					case ROWS:
						if (sizeY == 0) {
							sizeY = getSource().readShort();
							iMeta.addAxis(Axes.Y, sizeY);
						}
						else getSource().skipBytes(2);
						addInfo(meta, tag, sizeY);
						break;
					case COLUMNS:
						if (sizeX == 0) {
							sizeX = getSource().readShort();
							iMeta.addAxis(Axes.X, sizeX);
						}
						else getSource().skipBytes(2);
						addInfo(meta, tag, sizeX);
						break;
					case PHOTOMETRIC_INTERPRETATION:
					case PIXEL_SPACING:
					case SLICE_SPACING:
					case RESCALE_INTERCEPT:
					case WINDOW_CENTER:
					case RESCALE_SLOPE:
						addInfo(meta, tag, getSource().readString(tag.getElementLength()));
						break;
					case BITS_ALLOCATED:
						if (bitsPerPixel == 0) bitsPerPixel = getSource().readShort();
						else getSource().skipBytes(2);
						addInfo(meta, tag, bitsPerPixel);
						break;
					case PIXEL_REPRESENTATION:
						ss = getSource().readShort();
						signed = ss == 1;
						addInfo(meta, tag, ss);
						break;
					case PIXEL_SIGN:
						ss = getSource().readShort();
						addInfo(meta, tag, ss);
						break;
					case 537262910:
					case WINDOW_WIDTH:
						final String t = getSource().readString(tag.getElementLength());
						if (t.trim().length() == 0) maxPixelValue = -1;
						else {
							try {
								maxPixelValue = new Double(t.trim()).intValue();
							}
							catch (final NumberFormatException e) {
								maxPixelValue = -1;
							}
						}
						addInfo(meta, tag, t);
						break;
					case PIXEL_DATA:
					case ITEM:
					case 0xffee000:
						if (tag.getElementLength() != 0) {
							baseOffset = getSource().getFilePointer();
							addInfo(meta, tag, location);
							decodingTags = false;
						}
						else addInfo(meta, tag, null);
						break;
					case 0x7f880010:
						if (tag.getElementLength() != 0) {
							baseOffset = location + 4;
							decodingTags = false;
						}
						break;
					case 0x7fe00000:
						getSource().skipBytes(tag.getElementLength());
						break;
					case 0:
						getSource().seek(getSource().getFilePointer() - 4);
						break;
					default:
						final long oldfp = getSource().getFilePointer();
						addInfo(meta, tag, s);
						getSource().seek(oldfp + tag.getElementLength());
				}
				if (getSource().getFilePointer() >= (getSource().length() - 4)) {
					decodingTags = false;
				}
			}
			if (imagesPerFile == 0) imagesPerFile = 1;

			int bpp = bitsPerPixel;

			while (bitsPerPixel % 8 != 0)
				bitsPerPixel++;
			if (bitsPerPixel == 24 || bitsPerPixel == 48) {
				bitsPerPixel /= 3;
				bpp /= 3;
			}

			final int pixelType = FormatTools.pixelTypeFromBytes(bitsPerPixel / 8,
				signed, false);

			iMeta.setBitsPerPixel(bpp);
			iMeta.setPixelType(pixelType);

			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);

			final int planeSize = sizeX * sizeY * (int) (meta.getColorTable(0,
				0) == null ? meta.get(0).getAxisLength(Axes.CHANNEL) : 1) *
				bytesPerPixel;

			meta.setJP2K(isJP2K);
			meta.setJPEG(isJPEG);
			meta.setImagesPerFile(imagesPerFile);
			meta.setRLE(isRLE);
			meta.setDeflate(isDeflate);
			meta.setMaxPixelValue(maxPixelValue);
			meta.setOddLocations(oddLocations);

			log().info("Calculating image offsets");

			// calculate the offset to each plane

			getSource().seek(baseOffset - 12);
			final int len = getSource().readInt();
			if (len >= 0 && len + getSource().getFilePointer() < getSource()
				.length())
			{
				getSource().skipBytes(len);
				final int check = getSource().readShort() & 0xffff;
				if (check == 0xfffe) {
					baseOffset = getSource().getFilePointer() + 2;
				}
			}

			offsets = new long[imagesPerFile];
			meta.setOffsets(offsets);

			for (int i = 0; i < imagesPerFile; i++) {
				if (isRLE) {
					if (i == 0) getSource().seek(baseOffset);
					else {
						getSource().seek(offsets[i - 1]);
						final CodecOptions options = new CodecOptions();
						options.maxBytes = planeSize / bytesPerPixel;
						for (int q = 0; q < bytesPerPixel; q++) {
							final PackbitsCodec codec = codecService.getCodec(
								PackbitsCodec.class);
							codec.decompress(getSource(), options);
							while (getSource().read() == 0) { /* Read to non-0 data */}
							getSource().seek(getSource().getFilePointer() - 1);
						}
					}
					getSource().skipBytes(i == 0 ? 64 : 53);
					while (getSource().read() == 0) { /* Read to non-0 data */}
					offsets[i] = getSource().getFilePointer() - 1;
				}
				else if (isJPEG || isJP2K) {
					// scan for next JPEG magic byte sequence
					if (i == 0) offsets[i] = baseOffset;
					else offsets[i] = offsets[i - 1] + 3;

					final byte secondCheck = isJPEG ? (byte) 0xd8 : (byte) 0x4f;

					getSource().seek(offsets[i]);
					final byte[] buf = new byte[8192];
					int n = getSource().read(buf);
					boolean found = false;
					while (!found) {
						for (int q = 0; q < n - 2; q++) {
							if (buf[q] == (byte) 0xff && buf[q + 1] == secondCheck && buf[q +
								2] == (byte) 0xff)
							{
								if (isJPEG || (isJP2K && buf[q + 3] == 0x51)) {
									found = true;
									offsets[i] = getSource().getFilePointer() + q - n;
									break;
								}
							}
						}
						if (!found) {
							for (int q = 0; q < 4; q++) {
								buf[q] = buf[buf.length + q - 4];
							}
							n = getSource().read(buf, 4, buf.length - 4) + 4;
						}
					}
				}
				else offsets[i] = baseOffset + planeSize * i;
			}

			makeFileList(config);
		}

		@Override
		public String[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(getSource(), true, 1);
			if (noPixels || getMetadata().getFileList() == null) return null;
			final Integer[] keys = getMetadata().getFileList().keySet().toArray(
				new Integer[0]);
			Arrays.sort(keys);
			final Vector<String> files = getMetadata().getFileList().get(
				keys[imageIndex]);
			for (final String f : getMetadata().getCompanionFiles()) {
				files.add(f);
			}
			return files == null ? null : files.toArray(new String[files.size()]);
		}

		// -- Helper methods --

		private void makeFileList(final SCIFIOConfig config) throws FormatException,
			IOException
		{
			log().info("Building file list");

			if (getMetadata().getFileList() == null && getMetadata()
				.getOriginalInstance() != null && getMetadata()
					.getOriginalDate() != null && getMetadata()
						.getOriginalTime() != null && config.groupableIsGroupFiles())
			{
				final Hashtable<Integer, Vector<String>> fileList = new Hashtable<>();
				final Integer s = new Integer(getMetadata().getOriginalSeries());
				fileList.put(s, new Vector<String>());

				final int instanceNumber = Integer.parseInt(getMetadata()
					.getOriginalInstance()) - 1;
				if (instanceNumber == 0) fileList.get(s).add(getSource().getFileName());
				else {
					while (instanceNumber > fileList.get(s).size()) {
						fileList.get(s).add(null);
					}
					fileList.get(s).add(getSource().getFileName());
				}

				// look for matching files in the current directory
				final Location currentFile = new Location(getContext(), getSource()
					.getFileName()).getAbsoluteFile();
				Location directory = currentFile.getParentFile();
				scanDirectory(fileList, directory, false);

				// move up a directory and look for other directories that
				// could contain matching files

				directory = directory.getParentFile();
				final String[] subdirs = directory.list(true);
				if (subdirs != null) {
					for (final String subdir : subdirs) {
						final Location f = new Location(getContext(), directory, subdir)
							.getAbsoluteFile();
						if (!f.isDirectory()) continue;
						scanDirectory(fileList, f, true);
					}
				}

				final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
				Arrays.sort(keys);
				for (final Integer key : keys) {
					for (int j = 0; j < fileList.get(key).size(); j++) {
						if (fileList.get(key).get(j) == null) {
							fileList.get(key).remove(j);
							j--;
						}
					}
				}

				getMetadata().setFileList(fileList);
			}
			else if (getMetadata().getFileList() == null) {
				final Hashtable<Integer, Vector<String>> fileList = new Hashtable<>();
				fileList.put(0, new Vector<String>());
				fileList.get(0).add(getSource().getFileName());
				getMetadata().setFileList(fileList);
			}
		}

		/**
		 * DICOM datasets produced by:
		 * http://www.ct-imaging.de/index.php/en/ct-systeme-e/mikro-ct-e.html
		 * contain a bunch of extra metadata and log files. We do not parse these
		 * extra files, but do locate and attach them to the DICOM file(s).
		 */
		private void attachCompanionFiles(final Vector<String> companionFiles) {
			final Location parent = new Location(getContext(), getSource()
				.getFileName()).getAbsoluteFile().getParentFile();
			final Location grandparent = parent.getParentFile();

			if (new Location(getContext(), grandparent, parent.getName() + ".mif")
				.exists())
			{
				final String[] list = grandparent.list(true);
				for (final String f : list) {
					final Location file = new Location(getContext(), grandparent, f);
					if (!file.isDirectory()) {
						companionFiles.add(file.getAbsolutePath());
					}
				}
			}
		}

		/**
		 * Scan the given directory for files that belong to this dataset.
		 */
		private void scanDirectory(
			final Hashtable<Integer, Vector<String>> fileList, final Location dir,
			final boolean checkSeries) throws FormatException, IOException
		{
			final Location currentFile = new Location(getContext(), getSource()
				.getFileName()).getAbsoluteFile();
			final FilePattern pattern = new FilePattern(getContext(), currentFile
				.getName(), dir.getAbsolutePath());
			String[] patternFiles = pattern.getFiles();
			if (patternFiles == null) patternFiles = new String[0];
			Arrays.sort(patternFiles);
			final String[] files = dir.list(true);
			if (files == null) return;
			Arrays.sort(files);
			for (final String f : files) {
				final String file = new Location(getContext(), dir, f)
					.getAbsolutePath();
				log().debug("Checking file " + file);
				if (!f.equals(getSource().getFileName()) && !file.equals(getSource()
					.getFileName()) && getFormat().createChecker().isFormat(file) &&
					Arrays.binarySearch(patternFiles, file.replaceAll("\\\\",
						"\\\\\\\\")) >= 0)
				{
					addFileToList(fileList, file, checkSeries);
				}
			}
		}

		/**
		 * Determine if the given file belongs in the same dataset as this file.
		 */

		private void addFileToList(
			final Hashtable<Integer, Vector<String>> fileList, final String file,
			final boolean checkSeries) throws FormatException, IOException
		{
			final RandomAccessInputStream stream = new RandomAccessInputStream(
				getContext(), file);
			if (!getFormat().createChecker().isFormat(stream)) {
				stream.close();
				return;
			}
			stream.order(true);

			stream.seek(128);
			if (!stream.readString(4).equals("DICM")) stream.seek(0);

			int fileSeries = -1;

			String date = null, time = null, instance = null;
			while (date == null || time == null || instance == null || (checkSeries &&
				fileSeries < 0))
			{
				final long fp = stream.getFilePointer();
				if (fp + 4 >= stream.length() || fp < 0) break;
				final DICOMTag tag = DICOMUtils.getNextTag(stream);
				final String key = TYPES.name(tag.get());
				if ("Instance Number".equals(key)) {
					instance = stream.readString(tag.getElementLength()).trim();
					if (instance.length() == 0) instance = null;
				}
				else if ("Acquisition Time".equals(key)) {
					time = stream.readString(tag.getElementLength());
				}
				else if ("Acquisition Date".equals(key)) {
					date = stream.readString(tag.getElementLength());
				}
				else if ("Series Number".equals(key)) {
					fileSeries = Integer.parseInt(stream.readString(tag
						.getElementLength()).trim());
				}
				else stream.skipBytes(tag.getElementLength());
			}
			stream.close();

			if (date == null || time == null || instance == null || (checkSeries &&
				fileSeries == getMetadata().getOriginalSeries()))
			{
				return;
			}

			int stamp = 0;
			try {
				stamp = Integer.parseInt(time);
			}
			catch (final NumberFormatException e) {}

			int timestamp = 0;
			try {
				timestamp = Integer.parseInt(getMetadata().getOriginalTime());
			}
			catch (final NumberFormatException e) {}

			if (date.equals(getMetadata().getOriginalDate()) && (Math.abs(stamp -
				timestamp) < 150))
			{
				int position = Integer.parseInt(instance) - 1;
				if (position < 0) position = 0;
				if (fileList.get(fileSeries) == null) {
					fileList.put(fileSeries, new Vector<String>());
				}
				if (position < fileList.get(fileSeries).size()) {
					while (position < fileList.get(fileSeries).size() && fileList.get(
						fileSeries).get(position) != null)
					{
						position++;
					}
					if (position < fileList.get(fileSeries).size()) {
						fileList.get(fileSeries).setElementAt(file, position);
					}
					else fileList.get(fileSeries).add(file);
				}
				else {
					while (position > fileList.get(fileSeries).size()) {
						fileList.get(fileSeries).add(null);
					}
					fileList.get(fileSeries).add(file);
				}
			}
		}

		private void addInfo(final Metadata meta, final DICOMTag tag,
			final String value) throws IOException
		{
			final String oldValue = value;
			String info = getHeaderInfo(tag, value);

			if (info != null && tag.get() != ITEM) {
				info = info.trim();
				if (info.equals("")) info = oldValue == null ? "" : oldValue.trim();

				String key = TYPES.name(tag.get());
				if (key == null) {
					key = formatTag(tag.get());
				}
				if (key.equals("Samples per pixel")) {
					final int sizeC = Integer.parseInt(info);
					if (sizeC > 1) {
						meta.get(0).setAxisLength(Axes.CHANNEL, sizeC);
						meta.get(0).setPlanarAxisCount(2);
					}
				}
				else if (key.equals("Photometric Interpretation")) {
					if (info.equals("PALETTE COLOR")) {
						meta.get(0).setIndexed(true);
						meta.get(0).setAxisLength(Axes.CHANNEL, 1);
						meta.lut = new byte[3][];
						meta.shortLut = new short[3][];

					}
					else if (info.startsWith("MONOCHROME")) {
						meta.setInverted(info.endsWith("1"));
					}
				}
				else if (key.equals("Acquisition Date")) meta.setOriginalDate(info);
				else if (key.equals("Acquisition Time")) meta.setOriginalTime(info);
				else if (key.equals("Instance Number")) {
					if (info.trim().length() > 0) {
						meta.setOriginalInstance(info);
					}
				}
				else if (key.equals("Series Number")) {
					try {
						meta.setOriginalSeries(Integer.parseInt(info));
					}
					catch (final NumberFormatException e) {}
				}
				else if (key.contains("Palette Color LUT Data")) {
					final String color = key.substring(0, key.indexOf(" ")).trim();
					final int ndx = color.equals("Red") ? 0 : color.equals("Green") ? 1
						: 2;
					final long fp = getSource().getFilePointer();
					getSource().seek(getSource().getFilePointer() - tag
						.getElementLength() + 1);
					meta.shortLut[ndx] = new short[tag.getElementLength() / 2];
					meta.lut[ndx] = new byte[tag.getElementLength() / 2];
					for (int i = 0; i < meta.lut[ndx].length; i++) {
						meta.shortLut[ndx][i] = getSource().readShort();
						meta.lut[ndx][i] = (byte) (meta.shortLut[ndx][i] & 0xff);
					}
					getSource().seek(fp);
				}
				else if (key.equals("Content Time")) meta.setTime(info);
				else if (key.equals("Content Date")) meta.setDate(info);
				else if (key.equals("Image Type")) meta.setImageType(info);
				else if (key.equals("Rescale Intercept")) {
					meta.setRescaleIntercept(Double.parseDouble(info));
				}
				else if (key.equals("Rescale Slope")) {
					meta.setRescaleSlope(Double.parseDouble(info));
				}
				else if (key.equals("Pixel Spacing")) {
					meta.setPixelSizeX(info.substring(0, info.indexOf("\\")));
					meta.setPixelSizeY(info.substring(info.lastIndexOf("\\") + 1));
				}
				else if (key.equals("Spacing Between Slices")) {
					meta.setPixelSizeZ(new Double(info));
				}

				if (((tag.get() & 0xffff0000) >> 16) != 0x7fe0) {
					key = formatTag(tag.get()) + " " + key;
					final int imageIndex = meta.getImageCount() - 1;

					Object v;
					if ((v = meta.get(imageIndex).getTable().get(key)) != null) {
						// make sure that values are not overwritten
						meta.get(imageIndex).getTable().remove(key);
						meta.get(imageIndex).getTable().putList(key, v);
						meta.get(imageIndex).getTable().putList(key, info);
					}
					else {
						meta.get(imageIndex).getTable().put(key, info);
					}
				}
			}

		}

		private String formatTag(final int tag) {
			String s = Integer.toHexString(tag);
			while (s.length() < 8) {
				s = "0" + s;
			}
			return s.substring(0, 4) + "," + s.substring(4);
		}

		private void addInfo(final Metadata meta, final DICOMTag tag,
			final int value) throws IOException
		{
			addInfo(meta, tag, Integer.toString(value));
		}

		private String getHeaderInfo(final DICOMTag tag, String value)
			throws IOException
		{
			if (tag.get() == ITEM_DELIMINATION || tag
				.get() == SEQUENCE_DELIMINATION)
			{
				tag.setInSequence(false);
			}

			String id = TYPES.name(tag.get());
			int vr = tag.getVR();

			if (id != null) {
				if (vr == DICOMUtils.IMPLICIT_VR) {
					final String vrName = TYPES.vr(tag.get());
					vr = (vrName.charAt(0) << 8) + vrName.charAt(1);
					tag.setVR(vr);
				}
				if (id.length() > 2) id = id.substring(2);
			}

			if (tag.get() == ITEM) return id != null ? id : null;
			if (value != null) return value;

			boolean skip = false;
			switch (vr) {
				case DICOMUtils.AE:
				case DICOMUtils.AS:
				case DICOMUtils.AT:
					// Cannot fix element length to 4, because AT value representation is
					// always
					// 4 bytes long (DICOM specs PS3.5 par.6.2), but value multiplicity is
					// 1-n
					final byte[] bytes = new byte[tag.getElementLength()];
					// Read from stream
					getSource().readFully(bytes);
					// If little endian, swap bytes to get a string with a user friendly
					// representation of tag group and tag element
					if (tag.littleEndian) {
						for (int i = 0; i < bytes.length / 2; ++i) {
							final byte t = bytes[2 * i];
							bytes[2 * i] = bytes[2 * i + 1];
							bytes[2 * i + 1] = t;
						}
					}
					// Convert the bytes to a string
					value = DigestUtils.hex(bytes);
					break;
				case DICOMUtils.CS:
				case DICOMUtils.DA:
				case DICOMUtils.DS:
				case DICOMUtils.DT:
				case DICOMUtils.IS:
				case DICOMUtils.LO:
				case DICOMUtils.LT:
				case DICOMUtils.PN:
				case DICOMUtils.SH:
				case DICOMUtils.ST:
				case DICOMUtils.TM:
				case DICOMUtils.UI:
					value = getSource().readString(tag.getElementLength());
					break;
				case DICOMUtils.US:
					if (tag.getElementLength() == 2) value = Integer.toString(getSource()
						.readShort());
					else {
						value = "";
						final int n = tag.getElementLength() / 2;
						for (int i = 0; i < n; i++) {
							value += Integer.toString(getSource().readShort()) + " ";
						}
					}
					break;
				case DICOMUtils.IMPLICIT_VR:
					value = getSource().readString(tag.getElementLength());
					if (tag.getElementLength() <= 4 || tag.getElementLength() > 44)
						value = null;
					break;
				case DICOMUtils.SQ:
					value = "";
					final boolean privateTag = ((tag.getElementLength() >> 16) & 1) != 0;
					if (tag.get() == ICON_IMAGE_SEQUENCE || privateTag) skip = true;
					break;
				default:
					skip = true;
			}
			if (skip) {
				final long skipCount = tag.getElementLength();
				if (getSource().getFilePointer() + skipCount <= getSource().length()) {
					getSource().skipBytes((int) skipCount);
				}
				tag.addLocation(tag.getElementLength());
				value = "";
			}

			if (value != null && id == null && !value.equals("")) return value;
			else if (id == null) return null;
			else return value;
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private InitializeService initializeService;

		@Parameter
		private CodecService codecService;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.MEDICAL_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			plane.setColorTable(meta.getColorTable(imageIndex, planeIndex));
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, plane
				.getData().length, bounds);

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.max(xAxis), h = (int) bounds.max(yAxis);

			final Hashtable<Integer, Vector<String>> fileList = meta.getFileList();

			final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);
			if (fileList.get(keys[imageIndex]).size() > 1) {
				final int fileNumber = (int) (planeIndex / meta.getImagesPerFile());
				planeIndex = planeIndex % meta.getImagesPerFile();
				final String file = fileList.get(keys[imageIndex]).get(fileNumber);
				final io.scif.Reader r = initializeService.initializeReader(file,
					new SCIFIOConfig().checkerSetOpen(true));
				return (ByteArrayPlane) r.openPlane(imageIndex, planeIndex, plane,
					bounds, config);
			}

			final int ec = meta.get(0).isIndexed() ? 1 : (int) meta.get(imageIndex)
				.getAxisLength(Axes.CHANNEL);
			final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
				.getPixelType());
			final int bytes = (int) (meta.get(imageIndex).getAxisLength(Axes.X) * meta
				.get(imageIndex).getAxisLength(Axes.Y) * bpp * ec);
			getStream().seek(meta.getOffsets()[(int) planeIndex]);

			if (meta.isRLE()) {
				// plane is compressed using run-length encoding
				final CodecOptions options = new CodecOptions();
				options.maxBytes = (int) (meta.get(imageIndex).getAxisLength(Axes.X) *
					meta.get(imageIndex).getAxisLength(Axes.Y));
				final PackbitsCodec codec = codecService.getCodec(PackbitsCodec.class);
				for (int c = 0; c < ec; c++) {
					byte[] t = null;

					if (bpp > 1) {
						// TODO unused int planeSize = bytes / (bpp * ec);
						final byte[][] tmp = new byte[bpp][];
						for (int i = 0; i < bpp; i++) {
							tmp[i] = codec.decompress(getStream(), options);
							if (planeIndex < meta.getImagesPerFile() - 1 || i < bpp - 1) {
								while (getStream().read() == 0) { /* Read to non-0 data */}
								getStream().seek(getStream().getFilePointer() - 1);
							}
						}
						t = new byte[bytes / ec];
						for (int i = 0; i < planeIndex; i++) {
							for (int j = 0; j < bpp; j++) {
								final int byteIndex = meta.get(imageIndex).isLittleEndian()
									? bpp - j - 1 : j;
								if (i < tmp[byteIndex].length) {
									t[i * bpp + j] = tmp[byteIndex][i];
								}
							}
						}
					}
					else {
						t = codec.decompress(getStream(), options);
						if (t.length < (bytes / ec)) {
							final byte[] tmp = t;
							t = new byte[bytes / ec];
							System.arraycopy(tmp, 0, t, 0, tmp.length);
						}
						if (planeIndex < meta.getImagesPerFile() - 1 || c < ec - 1) {
							while (getStream().read() == 0) { /* Read to non-0 data */}
							getStream().seek(getStream().getFilePointer() - 1);
						}
					}

					final int rowLen = w * bpp;
					final int srcRowLen = (int) meta.get(imageIndex).getAxisLength(
						Axes.X) * bpp;

					// TODO unused int srcPlane = meta.getAxisLength(imageIndex, Axes.Y) *
					// srcRowLen;

					for (int row = 0; row < h; row++) {
						final int src = (row + y) * srcRowLen + x * bpp;
						final int dest = (h * c + row) * rowLen;
						final int len = Math.min(rowLen, t.length - src - 1);
						if (len < 0) break;
						System.arraycopy(t, src, plane.getBytes(), dest, len);
					}
				}
			}
			else if (meta.isJPEG() || meta.isJP2K()) {
				// plane is compressed using JPEG or JPEG-2000
				final long end = planeIndex < meta.getOffsets().length - 1 ? meta
					.getOffsets()[(int) planeIndex + 1] : getStream().length();
				byte[] b = new byte[(int) (end - getStream().getFilePointer())];
				getStream().read(b);

				if (b[2] != (byte) 0xff) {
					final byte[] tmp = new byte[b.length + 1];
					tmp[0] = b[0];
					tmp[1] = b[1];
					tmp[2] = (byte) 0xff;
					System.arraycopy(b, 2, tmp, 3, b.length - 2);
					b = tmp;
				}
				if ((b[3] & 0xff) >= 0xf0) {
					b[3] -= (byte) 0x30;
				}

				int pt = b.length - 2;
				while (pt >= 0 && b[pt] != (byte) 0xff || b[pt + 1] != (byte) 0xd9) {
					pt--;
				}
				if (pt < b.length - 2) {
					final byte[] tmp = b;
					b = new byte[pt + 2];
					System.arraycopy(tmp, 0, b, 0, b.length);
				}

				final CodecOptions options = new CodecOptions();
				options.littleEndian = meta.get(imageIndex).isLittleEndian();
				options.interleaved = meta.get(imageIndex)
					.getInterleavedAxisCount() > 0;
				final Codec codec = meta.isJPEG() ? //
					codecService.getCodec(JPEGCodec.class) : //
					codecService.getCodec(JPEG2000Codec.class);
				b = codec.decompress(b, options);

				final int rowLen = w * bpp;
				final int srcRowLen = (int) meta.get(imageIndex).getAxisLength(Axes.X) *
					bpp;

				final int srcPlane = (int) meta.get(imageIndex).getAxisLength(Axes.Y) *
					srcRowLen;

				for (int c = 0; c < ec; c++) {
					for (int row = 0; row < h; row++) {
						System.arraycopy(b, c * srcPlane + (row + y) * srcRowLen + x * bpp,
							plane.getBytes(), h * rowLen * c + row * rowLen, rowLen);
					}
				}
			}
			else if (meta.isDeflate()) {
				// TODO
				throw new UnsupportedCompressionException(
					"Deflate data is not supported.");
			}
			else {
				// plane is not compressed
				readPlane(getStream(), imageIndex, bounds, plane);
			}

			// NB: do *not* apply the rescale function

			return plane;
		}
	}

	// -- DICOM Helper Classes --

	private static class DICOMUtils {

		private static final int AE = 0x4145, AS = 0x4153, AT = 0x4154, CS = 0x4353;
		private static final int DA = 0x4441, DS = 0x4453, DT = 0x4454, FD = 0x4644;
		private static final int FL = 0x464C, IS = 0x4953, LO = 0x4C4F, LT = 0x4C54;
		private static final int PN = 0x504E, SH = 0x5348, SL = 0x534C, SS = 0x5353;
		private static final int ST = 0x5354, TM = 0x544D, UI = 0x5549, UL = 0x554C;
		private static final int US = 0x5553, UT = 0x5554, OB = 0x4F42, OW = 0x4F57;
		private static final int SQ = 0x5351, UN = 0x554E, QQ = 0x3F3F;

		private static final int IMPLICIT_VR = 0x2d2d;

		private static DICOMTag getNextTag(final RandomAccessInputStream stream)
			throws FormatException, IOException
		{
			return getNextTag(stream, false);
		}

		private static DICOMTag getNextTag(final RandomAccessInputStream stream,
			final boolean bigEndianTransferSyntax) throws FormatException, IOException
		{
			return getNextTag(stream, bigEndianTransferSyntax, false);
		}

		private static DICOMTag getNextTag(final RandomAccessInputStream stream,
			final boolean bigEndianTransferSyntax, final boolean isOddLocations)
			throws FormatException, IOException
		{
			final long fp = stream.getFilePointer();
			int groupWord = stream.readShort() & 0xffff;
			final DICOMTag diTag = new DICOMTag();
			boolean littleEndian = true;

			if (groupWord == 0x0800 && bigEndianTransferSyntax) {
				littleEndian = false;
				groupWord = 0x0008;
				stream.order(false);
			}
			else if (groupWord == 0xfeff || groupWord == 0xfffe) {
				stream.skipBytes(6);
				return DICOMUtils.getNextTag(stream, bigEndianTransferSyntax);
			}

			int elementWord = stream.readShort();
			int tag = ((groupWord << 16) & 0xffff0000) | (elementWord & 0xffff);

			diTag.setElementLength(getLength(stream, diTag));
			if (diTag.getElementLength() > stream.length()) {
				stream.seek(fp);
				littleEndian = !littleEndian;
				stream.order(littleEndian);

				groupWord = stream.readShort() & 0xffff;
				elementWord = stream.readShort();
				tag = ((groupWord << 16) & 0xffff0000) | (elementWord & 0xffff);
				diTag.setElementLength(getLength(stream, diTag));

				if (diTag.getElementLength() > stream.length()) {
					throw new FormatException("Invalid tag length " + diTag
						.getElementLength());
				}
				diTag.setTagValue(tag);
				return diTag;
			}

			if (diTag.getElementLength() < 0 && groupWord == 0x7fe0) {
				stream.skipBytes(12);
				diTag.setElementLength(stream.readInt());
				if (diTag.getElementLength() < 0) diTag.setElementLength(stream
					.readInt());
			}

			if (diTag.getElementLength() == 0 && (groupWord == 0x7fe0 ||
				tag == 0x291014))
			{
				diTag.setElementLength(getLength(stream, diTag));
			}
			else if (diTag.getElementLength() == 0) {
				stream.seek(stream.getFilePointer() - 4);
				final String v = stream.readString(2);
				if (v.equals("UT")) {
					stream.skipBytes(2);
					diTag.setElementLength(stream.readInt());
				}
				else stream.skipBytes(2);
			}

			// HACK - needed to read some GE files
			// The element length must be even!
			if (!isOddLocations && (diTag.getElementLength() % 2) == 1) diTag
				.incrementElementLength();

			// "Undefined" element length.
			// This is a sort of bracket that encloses a sequence of elements.
			if (diTag.getElementLength() == -1) {
				diTag.setElementLength(0);
				diTag.setInSequence(true);
			}
			diTag.setTagValue(tag);
			diTag.setLittleEndian(littleEndian);

			return diTag;
		}

		private static int getLength(final RandomAccessInputStream stream,
			final DICOMTag tag) throws IOException
		{
			final byte[] b = new byte[4];
			stream.read(b);

			// We cannot know whether the VR is implicit or explicit
			// without the full DICOM Data Dictionary for public and
			// private groups.

			// We will assume the VR is explicit if the two bytes
			// match the known codes. It is possible that these two
			// bytes are part of a 32-bit length for an implicit VR.

			final int vr = ((b[0] & 0xff) << 8) | (b[1] & 0xff);
			tag.setVR(vr);
			switch (vr) {
				case OB:
				case OW:
				case SQ:
				case UN:
					// Explicit VR with 32-bit length if other two bytes are zero
					if ((b[2] == 0) || (b[3] == 0)) {
						return stream.readInt();
					}
					tag.setVR(IMPLICIT_VR);
					return Bytes.toInt(b, stream.isLittleEndian());
				case AE:
				case AS:
				case AT:
				case CS:
				case DA:
				case DS:
				case DT:
				case FD:
				case FL:
				case IS:
				case LO:
				case LT:
				case PN:
				case SH:
				case SL:
				case SS:
				case ST:
				case TM:
				case UI:
				case UL:
				case US:
				case UT:
				case QQ:
					// Explicit VR with 16-bit length
					if (tag.get() == 0x00283006) {
						return Bytes.toInt(b, 2, 2, stream.isLittleEndian());
					}
					int n1 = Bytes.toShort(b, 2, 2, stream.isLittleEndian());
					int n2 = Bytes.toShort(b, 2, 2, !stream.isLittleEndian());
					n1 &= 0xffff;
					n2 &= 0xffff;
					if (n1 < 0 || n1 + stream.getFilePointer() > stream.length())
						return n2;
					if (n2 < 0 || n2 + stream.getFilePointer() > stream.length())
						return n1;
					return n1;
				case 0xffff:
					tag.setVR(IMPLICIT_VR);
					return 8;
				default:
					tag.setVR(IMPLICIT_VR);
					int len = Bytes.toInt(b, stream.isLittleEndian());
					if (len + stream.getFilePointer() > stream.length() || len < 0) {
						len = Bytes.toInt(b, 2, 2, stream.isLittleEndian());
						len &= 0xffff;
					}
					return len;
			}
		}
	}

	public static class DICOMTag {

		private int elementLength = 0;
		private int tagValue;
		private int vr = 0;
		private boolean inSequence = false;
		private int location = 0;
		private boolean littleEndian;

		public int getLocation() {
			return location;
		}

		public void setLocation(final int location) {
			this.location = location;
		}

		public void addLocation(final int offset) {
			location += offset;
		}

		public int getVR() {
			return vr;
		}

		public void setVR(final int vr) {
			this.vr = vr;
		}

		public int getElementLength() {
			return elementLength;
		}

		public void setElementLength(final int elementLength) {
			this.elementLength = elementLength;
		}

		public void incrementElementLength() {
			elementLength++;
		}

		public int get() {
			return tagValue;
		}

		public void setTagValue(final int tagValue) {
			this.tagValue = tagValue;
		}

		public boolean isInSequence() {
			return inSequence;
		}

		public void setInSequence(final boolean inSequence) {
			this.inSequence = inSequence;
		}

		public boolean isLittleEndian() {
			return littleEndian;
		}

		public void setLittleEndian(final boolean littleEndian) {
			this.littleEndian = littleEndian;
		}
	}
}
