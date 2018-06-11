/*-
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
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Interval;

import org.scijava.plugin.Plugin;

/**
 * The file format reader for Stratec pQCT files.
 * <p>
 * A pQCT (peripheral Quantitative Computed Tomography) device is used to study
 * the mechanical properties of bone and muscle. Specifically this reader is for
 * the reconstructed Iddddddd.Mhh CT-image files (d = digit, h = hex).
 * </p>
 * In theory the format supports 3D files, i.e. image stacks but they are very
 * rare. More often several slices are scanned from the same sample, but they
 * are saved as different image files. In this case "slice distance" is
 * meaningful metadata, because it helps to position the separate slice files.
 * This plugin has NOT been tested with a true Stratec 3D file.
 *
 * @author Timo Rantalainen (School of Exercise &amp; Nut. Sci., Deakin
 *         University, Melbourne 2011)
 * @author Michael Doube (Royal Veterinary College, London)
 * @author Richard Domander (Royal Veterinary College, London)
 */
@Plugin(type = Format.class, name = "Stratec pQCT")
public class StratecPQCTFormat extends AbstractFormat {

	/** Bytes in a for a valid pQCT file */
	public static final int HEADER_SIZE = 1609;

	/** Index of device name in the header */
	public static final int DEVICE_NAME_INDEX = 1050;

	/**
	 * Read a Pascal style "short string" from the stream
	 * <p>
	 * In a short string the first byte marks the length, and 1..n are the
	 * characters
	 * </p>
	 *
	 * @param stream The stream at the first byte of the string
	 * @return The string read from the stream
	 * @throws IOException If reading fails
	 */
	private static String readShortString(final RandomAccessInputStream stream)
		throws IOException
	{
		final byte length = stream.readByte();
		if (length == 0) {
			return "";
		}
		return stream.readString(length);
	}

	public static String[] generateSuffixes() {
		final String[] suffixes = new String[256];

		int count = 0;
		for (int i = 0; i < 16; i++) {
			final String hexTens = Integer.toHexString(i);
			for (int j = 0; j < 16; j++) {
				final String hexOnes = Integer.toHexString(j);
				final String suffix = "m" + hexTens + hexOnes;
				suffixes[count] = suffix;
				count++;
			}
		}

		return suffixes;
	}

	@Override
	protected String[] makeSuffixArray() {
		return generateSuffixes();
	}

	public static class Checker extends AbstractChecker {

		/** The regexp for valid pQCT filenames (without extension) */
		// Definition from ij.plugins.HandleExtraFileTypes
		public static final String NAME_FORMAT = "^[iI]\\d{7}";

		@Override
		public boolean suffixSufficient() {
			return false;
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final String fileName = Paths.get(stream.getFileName()).getFileName()
				.toString();
			final String mainPart = fileName.substring(0, 8);
			if (!mainPart.matches(NAME_FORMAT) || stream.length() < HEADER_SIZE) {
				return false;
			}

			stream.seek(DEVICE_NAME_INDEX);
			final String deviceName = readShortString(stream);

			return isDeviceNameValid(deviceName);
		}

		private boolean isDeviceNameValid(final String deviceName) {
			final String lowerCase = deviceName.toLowerCase();
			return lowerCase.length() > 4 && lowerCase.indexOf(".typ", deviceName
				.length() - 4) >= 0;
		}
	}

	public static class Metadata extends AbstractMetadata {

		/** Positions of the four measurement info strings in the header */
		public static final int[] INFO_INDICES = { 662, 743, 824, 905 };
		/** The date format in Stratec files */
		private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd");
		/** Date used when parsing fails */
		public static final Date DEFAULT_DATE = new Date(0);
		/** Unit used in spatial calibration */
		public static final String UNIT = "mm";
		/** Height in pixels */
		private short height;
		/** Width in pixels */
		private short width;
		/**
		 * Number of slices
		 * <p>
		 * NB in theory the format supports 3D files, but they are are exceedingly
		 * rare. However slice number is still often used in metadata. People take
		 * individual images of the same object, and mark down slice info as if they
		 * were in the same stack.
		 * </p>
		 */
		private short slices;
		/** Position of the first slice (mm) */
		private double sliceStart;
		/** Distance between slices (mm) */
		private double sliceDistance;
		/** Patient name (surname, forename) */
		private String patientName;
		private int patientNumber;
		private int patientAge;
		private short patientMeasurementNumber;
		private Date patientBirthDate;
		private String patientId;
		private Date measurementDate;
		private String measurementInfo;
		/** Image resolution in mm. Same for X and Y axes */
		private double resolution;
		private short topEdge;
		private short leftEdge;
		/** Name of device used in imaging */
		private String deviceName;
		/** Size of the sample in mm */
		private double objectSize;

		public String getPatientName() {
			return patientName;
		}

		public void setPatientName(final String patientName) {
			this.patientName = patientName;
		}

		public long getPatientNumber() {
			return patientNumber;
		}

		public void setPatientNumber(final int patientNumber) {
			this.patientNumber = patientNumber;
		}

		public int getPatientMeasurementNumber() {
			return patientMeasurementNumber;
		}

		public void setPatientMeasurementNumber(
			final short patientMeasurementNumber)
		{
			this.patientMeasurementNumber = patientMeasurementNumber;
		}

		public Date getPatientBirthDate() {
			return patientBirthDate;
		}

		public void setPatientBirthDate(final int patientBirthDate) {
			this.patientBirthDate = parseDate(patientBirthDate);
		}

		public Date getMeasurementDate() {
			return measurementDate;
		}

		public void setMeasurementDate(final int measurementDate) {
			this.measurementDate = parseDate(measurementDate);
		}

		private Date parseDate(final int measurementDate) {
			try {
				return DATE_FORMAT.parse(String.valueOf(measurementDate));
			}
			catch (final ParseException e) {
				return DEFAULT_DATE;
			}
		}

		public double getResolution() {
			return resolution;
		}

		public void setResolution(final double resolution) {
			this.resolution = resolution;
		}

		public int getLeftEdge() {
			return leftEdge;
		}

		public void setLeftEdge(final short leftEdge) {
			this.leftEdge = leftEdge;
		}

		public int getTopEdge() {
			return topEdge;
		}

		public void setTopEdge(final short topEdge) {
			this.topEdge = topEdge;
		}

		public String getMeasurementInfo() {
			return measurementInfo;
		}

		public void setMeasurementInfo(final RandomAccessInputStream stream) {
			final StringBuffer info = new StringBuffer(320);

			try {
				for (int i = 0; i < 4; i++) {
					stream.seek(INFO_INDICES[i]);
					final String infoBit = readShortString(stream);
					if (!infoBit.isEmpty()) {
						info.append(infoBit).append("\n");
					}
				}
				this.measurementInfo = info.toString();
			}
			catch (final IOException e) {
				this.measurementInfo = "";
			}
		}

		public String getDeviceName() {
			return deviceName;
		}

		public void setDeviceName(final String deviceName) {
			this.deviceName = deviceName;
		}

		public String getPatientId() {
			return patientId;
		}

		public void setPatientId(final String patientId) {
			this.patientId = patientId;
		}

		public double getObjectSize() {
			return objectSize;
		}

		public void setObjectSize(final double objectSize) {
			this.objectSize = objectSize;
		}

		public void setHeight(final short height) {
			this.height = height;
		}

		public void setWidth(final short width) {
			this.width = width;
		}

		// TODO Populate calibration metadata when it's supported
		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);
			final ImageMetadata metadata = get(0);
			metadata.setLittleEndian(true);
			metadata.setBitsPerPixel(16);
			metadata.setPixelType(FormatTools.INT16);
			metadata.setOrderCertain(true);
			metadata.setPlanarAxisCount(2);
			metadata.setAxes(new DefaultLinearAxis(Axes.X, UNIT, resolution),
				new DefaultLinearAxis(Axes.Y, UNIT, resolution));
			metadata.setAxisLengths(new long[] { width, height });
			// TODO add calibration function { -32.768, 0.001 } 1/cm
		}

		public short getWidth() {
			return width;
		}

		public short getHeight() {
			return height;
		}

		public int getPatientAge() {
			return patientAge;
		}

		public void setPatientAge(final int patientAge) {
			this.patientAge = patientAge;
		}

		public void setSlices(final short slices) {
			this.slices = (short) Math.max(1, slices);
		}

		public short getSlices() {
			return slices;
		}

		public void setSliceStart(final double sliceStart) {
			this.sliceStart = sliceStart;
		}

		public void setSliceDistance(final double sliceDistance) {
			this.sliceDistance = sliceDistance;
		}

		public double getSliceStart() {
			return sliceStart;
		}

		public double getSliceDistance() {
			return sliceDistance;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		@Override
		public void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			config.imgOpenerSetComputeMinMax(true);
			stream.order(true);

			stream.seek(12);
			meta.setResolution(stream.readDouble());

			stream.seek(30);
			meta.setSlices(stream.readShort());
			meta.setSliceStart(stream.readDouble());
			meta.setSliceDistance(stream.readDouble());

			stream.seek(318);
			meta.setObjectSize(stream.readDouble());
			meta.setMeasurementInfo(stream);

			stream.seek(986);
			meta.setMeasurementDate(stream.readInt());

			stream.seek(DEVICE_NAME_INDEX);
			meta.setDeviceName(readShortString(stream));

			stream.seek(1085);
			meta.setPatientMeasurementNumber(stream.readShort());
			meta.setPatientNumber(stream.readInt());
			meta.setPatientBirthDate(stream.readInt());
			meta.setPatientAge(stream.readInt());
			meta.setPatientName(readShortString(stream));

			stream.seek(1282);
			meta.setPatientId(readShortString(stream));

			stream.seek(1525);
			meta.setLeftEdge(stream.readShort());
			meta.setTopEdge(stream.readShort());
			meta.setWidth(stream.readShort());
			meta.setHeight(stream.readShort());
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.MEDICAL_DOMAIN };
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final RandomAccessInputStream stream = getStream();
			stream.seek(HEADER_SIZE);
			return readPlane(stream, imageIndex, bounds, plane);
		}
	}
}
