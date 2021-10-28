/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2021 SCIFIO developers.
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
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.common.DateTools;
import io.scif.config.SCIFIOConfig;
import io.scif.img.axes.SCIFIOAxes;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleInputStream;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * SCIFIO Format supporting the
 * <a href="http://en.wikipedia.org/wiki/Image_Cytometry_Standard">ICS</a> image
 * format.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Image Cytometry Standard")
public class ICSFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "ics", "ids" };
	}

	// -- Nested Classes --

	/**
	 * SCIFIO Metadata object for ICS images.
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		private static final String MICRO_TIME = "micro-time";

		private static final String DEFAULT_LENGTH_UNIT = "um";
		private static final String DEFAULT_TIME_UNIT = "s";
		private static final String DEFAULT_UNKNOWN_UNIT = "unkown";

		private static final Double DEFAULT_AXIS_SCALE = 1.0;
		// -- Fields --

		/**
		 * Whether this file is ICS version 2, and thus does not have an IDS
		 * companion
		 */
		private boolean versionTwo = false;

		/** Offset to pixel data */
		private long offset = -1;

		/** True if instrument information was discovered. */
		private boolean hasInstrumentData = false;

		/** True if this planes were stored in RGB order. */
		private boolean storedRGB;

		/** ICS file name */
		private final String icsId = "";

		/** IDS file name */
		private final String idsId = "";

		/** ICS Metadata */
		private Map<String, String> keyValPairs;

		private Location icsLocation;

		private Location idsLocation;

		// -- Constructor --

		public Metadata() {
			keyValPairs = new HashMap<>();
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			// Common metadata population
			createImageMetadata(1);

			final ImageMetadata imageMeta = get(0);

			// find axis sizes

			final int[] axisSizes = getAxesSizes();
			final String[] axes = getAxes();

			// Reset the existing axes
			imageMeta.setAxes(new CalibratedAxis[0], new long[0]);

			final String[] parameterLabels = getParameterLabels();
			final Double[] parameterScales = getParameterScales();
			final String[] parameterUnits = getParameterUnits();

			final Double[] historyExtents = getHistoryExtents();
			final String[] historyLabels = getHistoryLabels();
			final String[] historyUnits = getHistoryUnits();

			final boolean lifetime = getLifetime();

			int bitsPerPixel = 0;
			int nVirtualAxis = 0;

			// interpret axis information
			for (int n = 0; n < axes.length; n++) {
				final String axis = axes[n].toLowerCase();

				// determine axis type
				AxisType axisType;
				String defaultUnit;
				switch (axis) {
					case "bits":
						bitsPerPixel = axisSizes[n];
						while (bitsPerPixel % 8 != 0)
							bitsPerPixel++;
						if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
						nVirtualAxis++;
						continue;

					case "x":
					case "y":
					case "z":
						String actualAxis = historyLabels == null ? axis : historyLabels[n -
							nVirtualAxis];
						if (!actualAxis.equals("t")) {
							axisType = Axes.get(actualAxis.toUpperCase());
							defaultUnit = DEFAULT_LENGTH_UNIT;
						}
						else {
							axisType = Axes.TIME;
							defaultUnit = DEFAULT_TIME_UNIT;
						}
						break;

					case "t":
						axisType = Axes.TIME;
						defaultUnit = DEFAULT_TIME_UNIT;
						break;

					default:
						if (axis.startsWith("c")) axisType = Axes.CHANNEL;
						else if (axis.startsWith("p")) axisType = SCIFIOAxes.PHASE;
						else if (axis.startsWith("f")) axisType = SCIFIOAxes.FREQUENCY;
						else axisType = Axes.unknown();
						defaultUnit = DEFAULT_UNKNOWN_UNIT;
						break;
				}
				// turn into lifetime axis if is lifetime dataset
				// HACK - support for Gray Institute at Oxford's ICS lifetime data
				// DEPRECATED - future ICS data will use "parameter labels" with a
				// value
				// of "micro-time" for the lifetime axis.
				if (axisType.equals(Axes.TIME) && (MICRO_TIME.equals(
					parameterLabels[n]) || lifetime)) axisType = SCIFIOAxes.LIFETIME;

				// determine scale and unit
				String unit = null;
				Double scale = null;
				String paramUnit = parameterUnits == null || n >= parameterUnits.length
					? null : parameterUnits[n].toLowerCase();
				// tier 1: parameter unit + parameter scale
				if (paramUnit != null && !paramUnit.equals("undefined")) {
					unit = paramUnit;
					scale = parameterScales == null ? null : parameterScales[n];
				}
				// tier 2: history units + history extent
				if (unit == null || scale == null) {
					if (historyUnits != null && historyUnits[n - nVirtualAxis] != null)
						unit = historyUnits[n - nVirtualAxis].toLowerCase();
					if (historyExtents != null && historyExtents[n -
						nVirtualAxis] != null) scale = historyExtents[n - nVirtualAxis] /
							axisSizes[n];
				}
				// tier 3: default unit + history extent
				if (unit == null || scale == null) {
					unit = defaultUnit;
					if (historyExtents != null && historyExtents[n -
						nVirtualAxis] != null) scale = historyExtents[n - nVirtualAxis] /
							axisSizes[n];
				}
				// tier 4" default unit + default scale
				if (unit == null || scale == null) {
					unit = defaultUnit;
					scale = DEFAULT_AXIS_SCALE;
				}

				// size: the number of "ticks" of the axis
				// scale: the physical size of one increment along the axis
				CalibratedAxis newAxis = FormatTools.createAxis(axisType);
				FormatTools.calibrate(newAxis, scale, 0, unit);
				imageMeta.addAxis(newAxis, axisSizes[n]);
			}

			if (getBitsPerPixel() != null) bitsPerPixel = getBitsPerPixel();

			imageMeta.setBitsPerPixel(bitsPerPixel);

			if (imageMeta.isMultichannel() && getEMWaves() != null &&
				getEMWaves().length == imageMeta.getAxisLength(Axes.CHANNEL))
			{
				imageMeta.setAxisLength(Axes.CHANNEL, 1);
				setStoredRGB(true);
			}

			imageMeta.setIndexed(false);
			imageMeta.setFalseColor(false);
			imageMeta.setMetadataComplete(true);
			imageMeta.setLittleEndian(true);

			final String byteOrder = getByteOrder();
			final String rFormat = getRepFormat();

			if (byteOrder != null) {
				final String firstByte = byteOrder.split(" ")[0];
				final int first = Integer.parseInt(firstByte);
				final boolean little = rFormat.equals("real") ? first == 1 : first != 1;
				imageMeta.setLittleEndian(little);
			}

			final int bytes = bitsPerPixel / 8;

			if (bitsPerPixel < 32) imageMeta.setLittleEndian(!get(0)
				.isLittleEndian());

			final boolean floatingPt = rFormat.equals("real");
			final boolean signed = isSigned();

			try {
				imageMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed,
					floatingPt));
			}
			catch (final FormatException e) {
				log().error("Could not get pixel type from bytes: " + bytes, e);
			}
		}

		/**
		 * Helper method to check for the micro-time label.
		 */
		private boolean noMicroTime(final String[] labels) {
			for (final String s : labels) {
				if (s != null && s.equals(MICRO_TIME)) {
					return false;
				}
			}
			return true;
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) keyValPairs = new HashMap<>();
		}

		// -- ICSMetadata Methods --

		public boolean storedRGB() {
			return storedRGB;
		}

		public void setStoredRGB(final boolean rgb) {
			storedRGB = rgb;
		}

		// -- Helper Methods --

		/**
		 * Convenience method to directly access the hashtable.
		 */
		public String get(final String key) {
			return keyValPairs.get(key);
		}

		public void setIcsLocation(final Location icsLocation) {
			this.icsLocation = icsLocation;
		}

		public void setIdsLocation(final Location idsLocation) {
			this.idsLocation = idsLocation;
		}

		public Location getIcsLocation() {
			return icsLocation;
		}

		public Location getIdsLocation() {
			return idsLocation;
		}

		public boolean hasInstrumentData() {
			return hasInstrumentData;
		}

		public void setHasInstrumentData(final boolean hasInstrumentData) {
			this.hasInstrumentData = hasInstrumentData;
		}

		public boolean isVersionTwo() {
			return versionTwo;
		}

		public void setVersionTwo(final boolean versionTwo) {
			this.versionTwo = versionTwo;
		}

		public void setKeyValPairs(final Map<String, String> keyValPairs) {
			this.keyValPairs = keyValPairs;
		}

		public Map<String, String> getKeyValPairs() {
			return keyValPairs;
		}

		// -- Metadata population methods --

		public void put(final String key, final String value) {
			if (value != null) keyValPairs.put(key, value);
		}

		public void putDate(final String value) {
			put("history date", value);
		}

		public void putDescription(final String value) {
			put("history other text", value);
		}

		public void putMicroscopeModel(final String value) {
			put("history microscope", value);
		}

		public void putMicroscopeManufacturer(final String value) {
			put("history manufacturer", value);
		}

		public void putExperimentType(final String value) {
			put("history type", value);
		}

		public void putLifetime(final boolean lifetime) {
			if (lifetime) put("history type", "time resolved");
		}

		public void putPixelSizes(final Double[] pixelSizes) {
			put("parameter scale", merge(pixelSizes));
		}

		public void putUnits(final String[] units) {
			put("parameter units", merge(units));
		}

		public void putAxes(final String[] axes) {
			put("layout order", merge(axes));
		}

		public void putAxesSizes(final double[] axesSizes) {
			put("layout sizes", mergePrimitiveDoubles(axesSizes));
		}

		public void putPhysicalPixelSizes(final double[] physSizes) {
			put("history extents", mergePrimitiveDoubles(physSizes));
		}

		public void putTimestamps(final Double[] timeStamp) {
			put("parameter t", merge(timeStamp));

		}

		public void putChannelNames(final Map<Integer, String> cNames) {
			put("parameter ch", merge(cNames.values()));
		}

		public void putPinholes(final Map<Integer, Double> pins) {
			put("sensor s_params pinholeradius", merge(pins.values()));
		}

		public void putEMWaves(final Integer[] emWaves) {
			put("sensor s_params lambdaem", merge(emWaves));
		}

		public void putEMSingleton(final Integer[] em) {
			put("history cube emm nm", merge(em));
		}

		public void putEXWaves(final Integer[] exWaves) {
			put("sensor s_params LambdaEx", merge(exWaves));
		}

		public void putEXSingleton(final Integer[] ex) {
			put("history cube exc nm", merge(ex));
		}

		public void putWavelengths(final Map<Integer, Integer> waves) {
			put("history wavelength*", merge(waves.values()));
		}

		public void putByteOrder(final String byteOrder) {
			put("representation byte_order", byteOrder);
		}

		public void putRepFormat(final String repFormat) {
			put("representation format", repFormat);
		}

		public void putCompression(final String cmp) {
			put("representation compression", cmp);
		}

		public void putSigned(final boolean signed) {
			put("representation sign", String.valueOf(signed));
		}

		public void putLaserManufacturer(final String laserMan) {
			put("history laser manufacturer", laserMan);
		}

		public void putLaserModel(final String laserMod) {
			put("history laser model", laserMod);
		}

		public void putLaserRepetitionRate(final Double laserRep) {
			put("history laser rep rate", laserRep.toString());
		}

		public void putLaserPower(final Double laserPower) {
			put("history laser power", laserPower.toString());
		}

		public void putDichroicModel(final String diModel) {
			put("history filterset dichroic name", diModel);
		}

		public void putExcitationModel(final String exModel) {
			put("history filterset exc name", exModel);
		}

		public void putEmissionModel(final String emModel) {
			put("history filterset emm name", emModel);
		}

		public void putFilterSetModel(final String fltrModel) {
			put("history filterset", fltrModel);
		}

		public void putObjectiveModel(final String objModel) {
			put("history objective type", objModel);
		}

		public void putImmersion(final String immersion) {
			put("history objective immersion", immersion);
		}

		public void putLensNA(final Double lensNA) {
			put("history objective na", lensNA.toString());
		}

		public void putWorkingDistance(final Double wd) {
			put("history objective workingdistance", wd.toString());
		}

		public void putMagnification(final Double mag) {
			put("history objective magnification", mag.toString());
		}

		public void putDetectorManufacturer(final String detMan) {
			put("history camera manufacturer", detMan);
		}

		public void putDetectorModel(final String detModel) {
			put("history camera model", detModel);
		}

		public void putBitsPerPixel(final Integer bpp) {
			put("layout significant_bits", bpp.toString());
		}

		public void putGains(final Map<Integer, Double> gains) {
			put("history gain", merge(gains.values()));
		}

		public void putAuthorLastName(final String lastName) {
			put("history author", lastName);
		}

		public void putStagePositions(final Double[] positions) {
			put("history stage_xyzum", merge(positions));
		}

		public void putStageX(final Double stageX) {
			put("history stage positionx", stageX.toString());
		}

		public void putStageY(final Double stageY) {
			put("history stage positiony", stageY.toString());
		}

		public void putStageZ(final Double stageZ) {
			put("history stage positionz", stageZ.toString());
		}

		public void putExposureTime(final Double expTime) {
			put("history exposure", expTime.toString());
		}

		// -- Accessor methods for dynamically retrieving common metadata --

		public String getDate() {
			String date = null;
			final String[] kv = findValueForKey("history date", "history created on",
				"history creation date");
			if (kv != null) {
				if (kv[0].equalsIgnoreCase("history date") || kv[0].equalsIgnoreCase(
					"history created on"))
				{
					if (kv[1].indexOf(' ') > 0) {
						date = kv[1].substring(0, kv[1].lastIndexOf(' '));
					}
				}
				else if (kv[0].equalsIgnoreCase("history creation date")) {
					date = kv[1];
				}
			}

			if (date != null) date = DateTools.formatDate(date,
				ICSFormat.ICSUtils.DATE_FORMATS);

			return date;
		}

		public String getDescription() {
			return findStringValueForKey("history other text");
		}

		public String getMicroscopeModel() {
			return findStringValueForKey("history microscope");
		}

		public String getMicroscopeManufacturer() {
			return findStringValueForKey("history manufacturer");
		}

		public boolean getLifetime() {
			final String[] kv = findValueForKey("history type");
			boolean lifetime = false;
			if (kv != null && (kv[1].equalsIgnoreCase("time resolved") || kv[1]
				.equalsIgnoreCase("FluorescenceLifetime")))
			{
				lifetime = true;
			}
			return lifetime;
		}

		public String[] getParameterLabels() {
			final String pLabels = findStringValueForKey("parameter labels");
			if (pLabels == null) return new String[getAxes().length];
			return pLabels.split(" ");
		}

		public String[] getHistoryLabels() {
			final String[] kv = findValueForKey("history labels");
			return kv == null ? null : kv[1].split("\\s+");
		}

		public String getExperimentType() {
			return findStringValueForKey("history type");
		}

		public Double[] getParameterScales() {
			final String[] kv = findValueForKey("parameter scale");
			return kv == null ? null : splitDoubles(kv[1]);
		}

		public String[] getParameterUnits() {
			final String[] kv = findValueForKey("parameter units");
			return kv == null ? null : kv[1].split("\\s+");
		}

		public String[] getAxes() {
			final String[] kv = findValueForKey("layout order");
			String[] axes = null;
			if (kv != null) {
				final StringTokenizer t = new StringTokenizer(kv[1]);
				axes = new String[t.countTokens()];
				for (int n = 0; n < axes.length; n++) {
					axes[n] = t.nextToken().trim();
				}
			}
			return axes;
		}

		public int[] getAxesSizes() {
			final String[] kv = findValueForKey("layout sizes");
			int[] sizes = null;
			if (kv != null) {
				final String[] lengths = kv[1].split(" ");
				sizes = new int[lengths.length];
				for (int n = 0; n < sizes.length; n++) {
					try {
						sizes[n] = Integer.parseInt(lengths[n].trim());
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse axis length", e);
					}
				}
			}
			return sizes;
		}

		public Double[] getHistoryExtents() {
			final String[] kv = findValueForKey("history extents");
			return kv == null ? null : splitDoubles(kv[1]);
		}

		public String[] getHistoryUnits() {
			final String[] kv = findValueForKey("history units");
			return kv == null ? null : kv[1].split("\\s+");
		}

		public Double[] getTimestamps() {
			final String[] kv = findValueForKey("parameter t");
			return kv == null ? null : splitDoubles(kv[1]);
		}

		public Map<Integer, String> getChannelNames() {
			final String[] kv = findValueForKey("parameter ch");
			final Map<Integer, String> channelNames = new HashMap<>();
			if (kv != null) {
				final String[] names = kv[1].split(" ");
				for (int n = 0; n < names.length; n++) {
					channelNames.put(new Integer(n), names[n].trim());
				}
			}
			return channelNames;
		}

		public void addStepChannel(final Map<Integer, String> channelNames) {
			final String[] kv = findValueIteration("history step", "name");
			if (kv != null) channelNames.put(new Integer(kv[0].substring(12, kv[0]
				.indexOf(' ', 12))), kv[1]);
		}

		public void addCubeChannel(final Map<Integer, String> channelNames) {
			final String[] kv = findValueForKey("history cube");
			if (kv != null) channelNames.put(channelNames.size(), kv[1]);
		}

		public Map<Integer, Double> getPinholes() {
			final String[] kv = findValueForKey("sensor s_params pinholeradius");
			final Map<Integer, Double> pinholes = new HashMap<>();
			if (kv != null) {
				final String pins[] = kv[1].split(" ");
				int channel = 0;
				for (final String pin : pins) {
					if (pin.trim().equals("")) continue;
					try {
						pinholes.put(channel++, new Double(pin));
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse pinhole", e);
					}
				}
			}
			return pinholes;
		}

		public Integer[] getEMWaves() {
			final String[] kv = findValueForKey("sensor s_params lambdaem");
			Integer[] emWaves = null;
			if (kv != null) {
				final String[] waves = kv[1].split(" ");
				emWaves = new Integer[waves.length];
				for (int n = 0; n < emWaves.length; n++) {
					try {
						emWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse emission wavelength", e);
					}
				}
			}
			return emWaves;
		}

		public Integer[] getEMSingleton() {
			final String[] kv = findValueForKey("history cube emm nm");
			Integer[] emWaves = null;
			if (kv != null) {
				emWaves = new Integer[1];
				emWaves[0] = new Integer(kv[1].split(" ")[1].trim());
			}
			return emWaves;
		}

		public Integer[] getEXWaves() {
			final String[] kv = findValueForKey("sensor s_params lambdaex");
			Integer[] exWaves = null;
			if (kv != null) {
				final String[] waves = kv[1].split(" ");
				exWaves = new Integer[waves.length];
				for (int n = 0; n < exWaves.length; n++) {
					try {
						exWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse excitation wavelength", e);
					}
				}
			}
			return exWaves;
		}

		public Integer[] getEXSingleton() {
			final String[] kv = findValueForKey("history cube exc nm");
			Integer[] exWaves = null;
			if (kv != null) {
				exWaves = new Integer[1];
				exWaves[0] = new Integer(kv[1].split(" ")[1].trim());
			}
			return exWaves;
		}

		public Map<Integer, Integer> getWavelengths() {
			final String[] kv = findValueForKey("history wavelength*");
			final Map<Integer, Integer> wavelengths = new HashMap<>();
			if (kv != null) {
				final String[] waves = kv[1].split(" ");
				for (int n = 0; n < waves.length; n++) {
					wavelengths.put(n, Integer.parseInt(waves[n]));
				}
			}
			return wavelengths;
		}

		public void addLaserWavelength(final Map<Integer, Integer> wavelengths) {
			final String[] kv = findValueIteration("history laser", "wavelength");
			if (kv != null) {
				final int laser = Integer.parseInt(kv[0].substring(13, kv[0].indexOf(
					' ', 13))) - 1;
				kv[1] = kv[1].replaceAll("nm", "").trim();
				try {
					wavelengths.put(new Integer(laser), new Integer(kv[1]));
				}
				catch (final NumberFormatException e) {
					log().debug("Could not parse wavelength", e);
				}
			}
		}

		public String getByteOrder() {
			return findStringValueForKey("representation byte_order");
		}

		public String getRepFormat() {
			return findStringValueForKey("representation format");
		}

		public String getCompression() {
			return findStringValueForKey("representation compression");
		}

		public boolean isSigned() {
			final String signed = findStringValueForKey("representation sign");
			return signed != null && signed.equals("signed");
		}

		public String getLaserManufacturer() {
			return findStringValueForKey("history laser manufacturer");
		}

		public String getLaserModel() {
			return findStringValueForKey("history laser model");
		}

		public Double getLaserRepetitionRate() {
			return findDoubleValueForKey("history laser rep rate");
		}

		public Double getLaserPower() {
			return findDoubleValueForKey("history laser power");
		}

		public String getDichroicModel() {
			return findStringValueForKey("history filterset dichroic name");
		}

		public String getExcitationModel() {
			return findStringValueForKey("history filterset exc name");
		}

		public String getEmissionModel() {
			return findStringValueForKey("history filterset emm name");
		}

		public String getFilterSetModel() {
			return findStringValueForKey("history filterset");
		}

		public String getObjectiveModel() {
			return findStringValueForKey("history objective type",
				"history objective");
		}

		public String getImmersion() {
			return findStringValueForKey("history objective immersion");
		}

		public Double getLensNA() {
			return findDoubleValueForKey("history objective na");
		}

		public Double getWorkingDistance() {
			return findDoubleValueForKey("history objective workingdistance");
		}

		public Double getMagnification() {
			return findDoubleValueForKey("history objective magnification",
				"history objective mag");
		}

		public String getDetectorManufacturer() {
			return findStringValueForKey("history camera manufacturer");
		}

		public String getDetectorModel() {
			return findStringValueForKey("history camera model");
		}

		public Integer getBitsPerPixel() {
			return findIntValueForKey("layout significant_bits");
		}

		public Map<Integer, Double> getGains() {
			final String[] kv = findValueForKey("history gain");
			final Map<Integer, Double> gains = new HashMap<>();
			if (kv != null) {
				Integer n = new Integer(0);
				try {
					n = new Integer(kv[0].substring(12).trim());
					n = new Integer(n.intValue() - 1);
				}
				catch (final NumberFormatException e) {}
				gains.put(n, new Double(kv[1]));
			}
			return gains;
		}

		public String getAuthorLastName() {
			return findStringValueForKey("history author", "history experimenter");
		}

		public Double[] getStagePositions() {
			final String[] kv = findValueForKey("history stage_xyzum");
			Double[] stagePos = null;
			if (kv != null) {
				final String[] positions = kv[1].split(" ");
				stagePos = new Double[positions.length];
				for (int n = 0; n < stagePos.length; n++) {
					try {
						stagePos[n] = new Double(positions[n]);
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse stage position", e);
					}
				}
			}
			return stagePos;
		}

		public Double getStageX() {
			return findDoubleValueForKey("history stage positionx");
		}

		public Double getStageY() {
			return findDoubleValueForKey("history stage positiony");
		}

		public Double getStageZ() {
			return findDoubleValueForKey("history stage positionz");
		}

		public Double getExposureTime() {
			final String[] kv = findValueForKey("history exposure");
			Double exposureTime = null;
			if (kv != null) {
				final String expTime = kv[1];
				if (expTime.contains(" ")) {
					exposureTime = new Double(expTime.indexOf(' '));
					// TODO: Catch NumberFormatException? Make more DRY with
					// other logic?
				}
			}
			return exposureTime;
		}

		// -- Helper methods for finding key values --

		/*
		 * Given a list of tokens and an array of lists of regular expressions,
		 * tries to find a match. If no match is found, looks in OTHER_KEYS.
		 */
		String[] findKeyValue(final String[] tokens,
			final String[][] regexesArray)
		{
			String[] keyValue = findKeyValueForCategory(tokens, regexesArray);
			if (null == keyValue) {
				keyValue = findKeyValueOther(tokens, ICSFormat.ICSUtils.OTHER_KEYS);
			}
			if (null == keyValue) {
				final String key = tokens[0];
				final String value = concatenateTokens(tokens, 1, tokens.length);
				keyValue = new String[] { key, value };
			}
			return keyValue;
		}

		/*
		 * Builds a string from a list of tokens.
		 */
		private String concatenateTokens(final String[] tokens, final int start,
			final int stop)
		{
			final StringBuilder returnValue = new StringBuilder();
			for (int i = start; i < tokens.length && i < stop; ++i) {
				returnValue.append(tokens[i]);
				if (i < stop - 1) {
					returnValue.append(' ');
				}
			}
			return returnValue.toString();
		}

		private Double findDoubleValueForKey(final String... keys) {
			final String[] kv = findValueForKey(keys);

			return kv == null ? null : new Double(kv[1]);
		}

		private Integer findIntValueForKey(final String... keys) {
			final String[] kv = findValueForKey(keys);

			return kv == null ? null : new Integer(kv[1]);
		}

		private String findStringValueForKey(final String... keys) {
			final String[] kv = findValueForKey(keys);

			return kv == null ? null : kv[1];
		}

		/*
		 * Checks the list of keys for non-null values in the global hashtable.
		 *
		 * If a non-null value is found, it is returned.
		 *
		 * The returned array includes the matching key first, and the value
		 * second.
		 */
		private String[] findValueForKey(final String... keys) {

			for (final String key : keys) {
				final String value = keyValPairs.get(key);
				if (value != null) return new String[] { key, value };
			}

			return null;
		}

		/*
		 * Iterates through the key set, looking for a key that starts and/or
		 * ends with the provided partial keys.
		 *
		 * Returns an array containing the first matching key and its
		 * corresponding value if found, and an empty array otherwise.
		 */
		private String[] findValueIteration(final String starts,
			final String ends)
		{
			// TODO not sure how to represent this in the ICSUtils key tree
			for (final String key : keyValPairs.keySet()) {
				if ((starts == null || key.startsWith(starts)) && (ends == null || key
					.endsWith(ends))) return new String[] { key, keyValPairs.get(key) };
			}

			return null;
		}

		/*
		 * Given a list of tokens and an array of lists of regular expressions,
		 * finds a match. Returns key/value pair if matched, null otherwise.
		 *
		 * The first element, tokens[0], has already been matched to a category,
		 * i.e. 'history', and the regexesArray is category-specific.
		 */
		private String[] findKeyValueForCategory(final String[] tokens,
			final String[][] regexesArray)
		{
			String[] keyValue = null;
			for (final String[] regexes : regexesArray) {
				if (compareTokens(tokens, 1, regexes, 0)) {
					final int splitIndex = 1 + regexes.length; // add one for
					// the category
					final String key = concatenateTokens(tokens, 0, splitIndex);
					final String value = concatenateTokens(tokens, splitIndex,
						tokens.length);
					keyValue = new String[] { key, value };
					break;
				}
			}
			return keyValue;
		}

		/*
		 * Given a list of tokens and an array of lists of regular expressions,
		 * finds a match. Returns key/value pair if matched, null otherwise.
		 *
		 * The first element, tokens[0], represents a category and is skipped.
		 * Look for a match of a list of regular expressions anywhere in the
		 * list of tokens.
		 */
		private String[] findKeyValueOther(final String[] tokens,
			final String[][] regexesArray)
		{
			String[] keyValue = null;
			for (final String[] regexes : regexesArray) {
				for (int i = 1; i < tokens.length - regexes.length; ++i) {
					// does token match first regex?
					if (tokens[i].toLowerCase().matches(regexes[0])) {
						// do remaining tokens match remaining regexes?
						if (1 == regexes.length || compareTokens(tokens, i + 1, regexes,
							1))
						{
							// if so, return key/value
							final int splitIndex = i + regexes.length;
							final String key = concatenateTokens(tokens, 0, splitIndex);
							final String value = concatenateTokens(tokens, splitIndex,
								tokens.length);
							keyValue = new String[] { key, value };
							break;
						}
					}
				}
				if (null != keyValue) {
					break;
				}
			}
			return keyValue;
		}

		/*
		 * Compares a list of tokens with a list of regular expressions.
		 */
		private boolean compareTokens(final String[] tokens, final int tokenIndex,
			final String[] regexes, final int regexesIndex)
		{
			boolean returnValue = true;
			int i, j;
			for (i = tokenIndex, j = regexesIndex; j < regexes.length; ++i, ++j) {
				if (i >= tokens.length || !tokens[i].toLowerCase().matches(
					regexes[j]))
				{
					returnValue = false;
					break;
				}
			}
			return returnValue;
		}

		/** Splits the given string into a list of {@link Double}s. */
		private Double[] splitDoubles(final String v) {
			final StringTokenizer t = new StringTokenizer(v);
			final Double[] values = new Double[t.countTokens()];
			for (int n = 0; n < values.length; n++) {
				final String token = t.nextToken().trim();
				try {
					values[n] = new Double(token);
				}
				catch (final NumberFormatException e) {
					log().debug("Could not parse double value '" + token + "'", e);
				}
			}
			return values;
		}

		// Converts an array of doubles to a space-delimited String
		private String mergePrimitiveDoubles(final double... doubles) {

			final Double[] d = new Double[doubles.length];

			for (int i = 0; i < doubles.length; i++)
				d[i] = doubles[i];

			return this.merge(d);
		}

		// Converts a collection to a space-delimited String
		private <T> String merge(final Collection<T> collection) {
			@SuppressWarnings("unchecked")
			final T[] array = (T[]) collection.toArray();
			return merge(array);
		}

		// Converts a generic array to a space-delimited String
		private <T> String merge(final T... values) {
			final StringBuilder b = new StringBuilder();

			for (final T v : values) {
				b.append(v.toString() + " ");
			}

			return b.toString();
		}
	}

	/**
	 * SCIFIO file format Parser for ICS images.
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- ICSFormat.Parser Methods --

		// -- Parser API Methods --

		@Parameter
		private DataHandleService handles;

		@SuppressWarnings("unchecked")
		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			findCompanion(stream, meta);
			final DataHandle<Location> parseHandle;

			if (stream.get().equals(meta.idsLocation)) { // stream is not on the ics
				parseHandle = handles.readBuffer(meta.icsLocation);
			}
			else {
				parseHandle = stream;
			}

			parseHandle.seek(0);
			String line = parseHandle.findString(ICSUtils.NL);
			// Extracts the key, value pairs from each line and
			// inserts them into the ICSMetadata object
			while (line != null && !line.trim().equals("end") && parseHandle
				.offset() < parseHandle.length() - 1)
			{
				line = line.trim().toLowerCase();
				final String[] tokens = line.split("[ \t]");
				final StringBuilder key = new StringBuilder();
				Map<String, Object> keyMap = ICSUtils.keys;
				boolean validKey = false;
				for (int q = 0; q < tokens.length; q++) {
					tokens[q] = tokens[q].trim();
					if (tokens[q].length() == 0) continue;

					final Object o = keyMap.get(tokens[q]);
					if (o == null) {
						// Reached the value section of the line.
						if (!validKey) {
							// Didn't get a valid key so exit
							break;
						}
						// found a valid key, so build the value and create a
						// mapping
						final StringBuilder value = new StringBuilder(tokens[q++]);
						for (; q < tokens.length; q++) {
							value.append(" ");
							value.append(tokens[q].trim());
						}
						final String k = key.toString().trim().replaceAll("\t", " ");
						final String v = value.toString().trim();
						meta.getTable().put(k, v);
						meta.keyValPairs.put(k.toLowerCase(), v);
					}
					else {
						// Map lookup wasn't null, so we move the keyMap to the
						// next node
						keyMap = (Map<String, Object>) o;
						// if we have a LEAF entry, we know we've found a valid
						// key
						if (keyMap.get(ICSUtils.LEAF) != null) validKey = true;

						// Build the key
						key.append(tokens[q]);
						key.append(" ");
					}
				}

				line = parseHandle.findString(ICSUtils.NL);
			}

			// check which version of ICS
			final Object icsVersion = meta.getTable().get("ics_version");
			if (icsVersion == null) {
				parseHandle.close();
				throw new FormatException("Cannot discern ICS version");
			}
			else if ("1.0".equals(icsVersion)) {
				// split file format: ICS for metadata, IDS for pixel data
				meta.setVersionTwo(false);

				// verify that data file is present
				final Location idsLoc = meta.getIdsLocation();
				if (idsLoc == null) {
					parseHandle.close();
					throw new FormatException(
						"Data file does not exist, should be located next to: " +
							parseHandle.get().toString());
				}

				// close the ICS file
				parseHandle.close();
				getSource().close();
				// NB: Probably not necessary? But just to be safe...

				// switch to the IDS file
				updateSource(meta.getIdsLocation());
			}
			else if (icsVersion.equals("2.0")) {
				// unified file format: ICS contains metadata and pixel data; no
				// IDS file
				meta.setVersionTwo(true);
				meta.setIdsLocation(meta.getIcsLocation());
				meta.setSource(stream);
			}
			else {
				stream.close();
				parseHandle.close();
				throw new FormatException("Unsupported ICS version: " + icsVersion);
			}

			meta.offset = getSource().offset();

			getSource().seek(0);

			meta.hasInstrumentData = nullKeyCheck(new String[] {
				"history cube emm nm", "history cube exc nm", "history objective NA",
				"history stage xyzum", "history objective magnification",
				"history objective mag", "history objective WorkingDistance",
				"history objective type", "history objective",
				"history objective immersion" });
		}

		// -- Helper Methods --

		/* Returns true if any of the keys in testKeys has a non-null value */
		private boolean nullKeyCheck(final String[] testKeys) {
			for (final String key : testKeys) {
				if (getMetadata().get(key) != null) {
					return true;
				}
			}
			return false;
		}

		/*
		 * Finds the companion file for a given stream (ICS and IDS are
		 * companions)
		 */
		private void findCompanion(final DataHandle<Location> stream,
			final Metadata meta) throws FormatException, IOException
		{

			String icsId;
			String idsId;
			BrowsableLocation icsLocation;
			BrowsableLocation idsLocation;
			icsId = idsId = stream.get().getName();
			if ("".equals(icsId) || !(stream.get() instanceof BrowsableLocation)) {
				// handle has no filename, we are probably reading from a byte array.
				// OR we can't browse for companion file as the location does not
				// support it
				return;
			}

			final int dot = icsId.lastIndexOf('.');
			final String ext = dot < 0 ? "" : icsId.substring(dot + 1).toLowerCase();
			if ("ics".equals(ext)) {
				// convert C to D regardless of case
				final char[] c = idsId.toCharArray();
				c[c.length - 2]++;
				idsId = new String(c);
				icsLocation = (BrowsableLocation) stream.get();
				idsLocation = icsLocation.sibling(idsId);
			}
			else if ("ids".equals(ext)) {
				// convert D to C regardless of case
				final char[] c = icsId.toCharArray();
				c[c.length - 2]--;
				icsId = new String(c);

				idsLocation = (BrowsableLocation) stream.get();
				icsLocation = idsLocation.sibling(icsId);
			}
			else {
				throw new FormatException("Companion file not found");
			}

			meta.setIcsLocation(icsLocation);
			meta.setIdsLocation(idsLocation);
		}
	}

	/**
	 * File format SCIFIO Reader for Image Cytometry Standard (ICS) images.
	 * Version 1 and 2 supported.
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private DataHandleService dataHandleService;

		// -- Fields --

		/* Last read plane index. */
		private long prevPlane;

		/* Whether or not the pixels are GZIP-compressed. */
		private boolean gzip;

		/* Cached GZIPInputStream */
		private GZIPInputStream gzipStream;

		/* Whether or not the image is inverted along the Y axis. */
		private boolean invertY; // TODO only in oldInitFile

		/* Image data. */
		private byte[] data;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.LM_DOMAIN, FormatTools.FLIM_DOMAIN,
				FormatTools.UNKNOWN_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, plane
				.getData().length, bounds);
			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);

			final int x = (int) bounds.min(xAxis);
			final int y = (int) bounds.min(yAxis);
			final int w = (int) bounds.dimension(xAxis);
			final int h = (int) bounds.dimension(yAxis);

			final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
				.getPixelType());
			final int len = (int) FormatTools.getPlaneSize(this, imageIndex);
			final int rowLen = (int) FormatTools.getPlaneSize(meta, w, 1, imageIndex);

			final long[] coordinates = FormatTools.rasterToPosition(imageIndex,
				planeIndex, meta);
			final long[] prevCoordinates = FormatTools.rasterToPosition(imageIndex,
				prevPlane, meta);

			if (!gzip) {
				getHandle().seek(getMetadata().offset + planeIndex * len);
			}
			else {
				long toSkip = (planeIndex - prevPlane - 1) * len;
				if (gzipStream == null || planeIndex <= prevPlane) {
					DataHandle<Location> fis = null;
					toSkip = planeIndex * len;
					if (getMetadata().versionTwo) {
						fis = dataHandleService.create(getMetadata().icsLocation);
						fis.skip(getMetadata().offset);
					}
					else {
						fis = dataHandleService.create(getMetadata().idsLocation);
						toSkip += getMetadata().offset;
					}
					try {
						gzipStream = new GZIPInputStream(new DataHandleInputStream<>(fis));
					}
					catch (final IOException e) {
						// the 'gzip' flag is set erroneously
						gzip = false;
						getHandle().seek(getMetadata().offset + planeIndex * len);
						gzipStream = null;
					}
				}

				if (gzipStream != null) {
					while (toSkip > 0) {
						toSkip -= gzipStream.skip(toSkip);
					}

					data = new byte[(int) (len * (meta.storedRGB() ? meta.get(imageIndex)
						.getAxisLength(Axes.CHANNEL) : 1))];
					int toRead = data.length;
					while (toRead > 0) {
						toRead -= gzipStream.read(data, data.length - toRead, toRead);
					}
				}
			}

			// FIXME: Why not getMetadata().getSizeC()?
			final int sizeC = (int) (meta.getLifetime() ? 1 : meta.get(imageIndex)
				.getAxisLength(Axes.CHANNEL));

			// FIXME: This logic needs to be reworked!
			if (!getMetadata().get(imageIndex).isMultichannel() && getMetadata()
				.storedRGB())
			{
				// channels are stored interleaved, but because there are more
				// than we can display as RGB, we need to separate them
				getHandle().seek(getMetadata().offset + len * FormatTools
					.positionToRaster(0, this, new long[] { coordinates[0], 0,
						coordinates[2] }));
				if (!gzip && data == null) {
					data = new byte[(int) (len * getMetadata().get(imageIndex)
						.getAxisLength(Axes.CHANNEL))];
					getHandle().read(data);
				}
				else if (!gzip && (coordinates[0] != prevCoordinates[0] ||
					coordinates[2] != prevCoordinates[2]))
				{
					getHandle().read(data);
				}

				for (int row = y; row < h + y; row++) {
					for (int col = x; col < w + x; col++) {
						final int src = (int) (bpp * ((planeIndex % meta.get(imageIndex)
							.getAxisLength(Axes.CHANNEL)) + sizeC * (row * (row * meta.get(
								imageIndex).getAxisLength(Axes.X) + col))));
						final int dest = bpp * ((row - y) * w + (col - x));
						System.arraycopy(data, src, plane.getBytes(), dest, bpp);
					}
				}
			}
			else if (gzip) {
				try (DataHandle<Location> bytes = dataHandleService.create(
					new BytesLocation(data)))
				{
					readPlane(bytes, imageIndex, bounds, plane);
				}
			}
			else {
				readPlane(getHandle(), imageIndex, bounds, plane);
			}

			if (invertY) {
				final byte[] row = new byte[rowLen];
				for (int r = 0; r < h / 2; r++) {
					final int topOffset = r * rowLen;
					final int bottomOffset = (h - r - 1) * rowLen;
					System.arraycopy(plane.getBytes(), topOffset, row, 0, rowLen);
					System.arraycopy(plane.getBytes(), bottomOffset, plane.getBytes(),
						topOffset, rowLen);
					System.arraycopy(row, 0, plane.getBytes(), bottomOffset, rowLen);
				}
			}

			prevPlane = planeIndex;

			return plane;
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				data = null;
				gzip = false;
				invertY = false;
				prevPlane = 0;
				if (gzipStream != null) {
					gzipStream.close();
				}
				gzipStream = null;
			}
		}

		@Override
		public void setMetadata(final Metadata meta) throws IOException {
			super.setMetadata(meta);
			gzip = getMetadata().get("representation compression").equals("gzip");
			prevPlane = -1;
			gzipStream = null;
			invertY = false;
			data = null;
		}

		@Override
		public void setSource(final DataHandle<Location> stream,
			final SCIFIOConfig config) throws IOException
		{
			// we need to switch to the ids file if the stream points to the ics
			// file, as we
			if (!getMetadata().versionTwo && !getMetadata().getIdsLocation().equals(
				stream.get()))
			{
				stream.close();
				super.setSource(dataHandleService.create(getMetadata()
					.getIdsLocation()), config);
				return;
			}
			super.setSource(stream, config);
		}

		@Override
		public String[] getDomains() {
			FormatTools.assertStream(getHandle(), true, 0);
			final String[] domain = new String[] { FormatTools.GRAPHICS_DOMAIN };
			if (getMetadata().get(0).getAxisLength(SCIFIOAxes.LIFETIME) > 1) {
				domain[0] = FormatTools.FLIM_DOMAIN;
			}
			else if (getMetadata().hasInstrumentData) {
				domain[0] = FormatTools.LM_DOMAIN;
			}

			return domain;
		}
	}

	/**
	 * SCIFIO file format writer for ICS version 1 and 2 files.
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		@Parameter
		private DataHandleService dataHandleService;

		// -- Fields --

		private long dimensionOffset;

		private int dimensionLength;

		private long pixelOffset;

		private long lastPlane = -1;

		private DataHandle<Location> pixels;

		// -- AbstractWriter Methods --

		@Override
		protected String[] makeCompressionTypes() {
			return new String[0];
		}

		@Override
		protected void initialize(final int imageIndex, final long planeIndex,
			final Interval bounds) throws FormatException, IOException
		{
			if (!isInitialized(imageIndex, (int) planeIndex)) {

				if (!SCIFIOMetadataTools.wholePlane(imageIndex, getMetadata(),
					bounds))
				{
					pixels.seek(pixelOffset + (planeIndex + 1) * getMetadata().get(
						imageIndex).getPlaneSize());
				}
			}

			super.initialize(imageIndex, planeIndex, bounds);
		}

		// -- Writer API Methods --

		@Override
		public void writePlane(final int imageIndex, final long planeIndex,
			final Plane plane, final Interval bounds) throws FormatException,
			IOException
		{
			checkParams(imageIndex, planeIndex, plane.getBytes(), bounds);
			final Metadata meta = getMetadata();
			final boolean interleaved = plane.getImageMetadata()
				.getInterleavedAxisCount() > 0;

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);

			int rgbChannels = 1;

			if (meta.get(imageIndex).isMultichannel()) {
				final int cIndex = meta.get(imageIndex).getAxisIndex(Axes.CHANNEL);
				rgbChannels = (int) (bounds.dimension(cIndex));
			}

			final int sizeX = (int) meta.get(imageIndex).getAxisLength(Axes.X);
			final int pixelType = getMetadata().get(imageIndex).getPixelType();
			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
			final int planeSize = (int) (meta.get(0).getSize() / meta.get(0)
				.getPlaneCount());

			pixels.seek(pixelOffset + planeIndex * planeSize);
			if (SCIFIOMetadataTools.wholePlane(imageIndex, meta, bounds) &&
				(interleaved || rgbChannels == 1))
			{
				pixels.write(plane.getBytes());
			}
			else {
				pixels.skipBytes(bytesPerPixel * rgbChannels * sizeX * y);
				for (int row = 0; row < h; row++) {
					final ByteArrayOutputStream strip = new ByteArrayOutputStream();
					for (int col = 0; col < w; col++) {
						for (int c = 0; c < rgbChannels; c++) {
							final int index = interleaved ? rgbChannels * (row * w + col) + c
								: w * (c * h + row) + col;
							strip.write(plane.getBytes(), index * bytesPerPixel,
								bytesPerPixel);
						}
					}
					pixels.skipBytes(bytesPerPixel * rgbChannels * x);
					pixels.write(strip.toByteArray());
					pixels.skipBytes(bytesPerPixel * rgbChannels * (sizeX - w - x));
				}
			}
			lastPlane = planeIndex;
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
				FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32,
				FormatTools.FLOAT };
		}

		public void close(final int imageIndex) throws IOException {

			super.close();
			pixelOffset = 0;
			lastPlane = -1;
			dimensionOffset = 0;
			dimensionLength = 0;
			if (pixels != null) {
				pixels.close();
			}
			pixels = null;
		}

		@Override
		public void setDest(final DataHandle<Location> out, final int imageIndex,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final String currentId = out.get().getName();
			if (FormatTools.checkSuffix(currentId, "ics")) {
				getMetadata().setIcsLocation(out.get());
			}
			else {
				getMetadata().setIdsLocation(out.get());
			}

			super.setDest(out, imageIndex, config);

			if (out.length() <= 0) {
				out.writeBytes("\t\n");
				if (FormatTools.checkSuffix(currentId, "ids")) {
					out.writeBytes("ics_version\t1.0\n");
				}
				else {
					out.writeBytes("ics_version\t2.0\n");
				}
				out.writeBytes("filename\t" + currentId + "\n");
				out.writeBytes("layout\tparameters\t6\n");

				final Metadata meta = getMetadata();
				// SCIFIOMetadataTools.verifyMinimumPopulated(meta, pixels);

				final int pixelType = meta.get(imageIndex).getPixelType();

				dimensionOffset = out.offset();
				final int[] sizes = overwriteDimensions(meta, imageIndex);
				dimensionLength = (int) (out.offset() - dimensionOffset);

				if (getValidBits() != 0) {
					out.writeBytes("layout\tsignificant_bits\t" + getValidBits() + "\n");
				}

				final boolean signed = FormatTools.isSigned(pixelType);
				final boolean littleEndian = meta.get(imageIndex).isLittleEndian();

				out.writeBytes("representation\tformat\t" +
					(pixelType == FormatTools.FLOAT ? "real\n" : "integer\n"));
				out.writeBytes("representation\tsign\t" + (signed ? "signed\n"
					: "unsigned\n"));
				out.writeBytes("representation\tcompression\tuncompressed\n");
				out.writeBytes("representation\tbyte_order\t");
				for (int i = 0; i < sizes[0] / 8; i++) {
					if ((littleEndian && (sizes[0] < 32 ||
						pixelType == FormatTools.FLOAT)) || (!littleEndian &&
							sizes[0] >= 32 && pixelType != FormatTools.FLOAT))
					{
						out.writeBytes((i + 1) + "\t");
					}
					else {
						out.writeBytes(((sizes[0] / 8) - i) + "\t");
					}
				}

				out.writeBytes("\nparameter\tscale\t1.000000\t");

				final StringBuilder units = new StringBuilder();

				for (final CalibratedAxis axis : meta.get(imageIndex).getAxes()) {
					Number value = 1.0;
					if (axis.type() == Axes.X) {
						value = axis.averageScale(0, 1);
					}
					else if (axis.type() == Axes.Y) {
						value = axis.averageScale(0, 1);
					}
					else if (axis.type() == Axes.Z) {
						value = axis.averageScale(0, 1);
					}
					else if (axis.type() == Axes.TIME) {
						value = axis.averageScale(0, 1);
					}
					units.append(axis.unit() + "\t");
					out.writeBytes(value + "\t");
				}

				out.writeBytes("\nparameter\tunits\tbits\t" + units.toString() + "\n");
				out.writeBytes("\nend\n");
				pixelOffset = out.offset();
			}
			else if (FormatTools.checkSuffix(currentId, "ics")) {
				try (DataHandle<Location> tmpin = dataHandleService.create(out.get())) {
					tmpin.findString("\nend\n");
					pixelOffset = tmpin.offset();
				}
			}

			if (FormatTools.checkSuffix(currentId, "ids")) {
				pixelOffset = 0;
			}

			if (pixels == null) {
				pixels = dataHandleService.create(out.get());
			}
		}

		// -- Helper methods --

		private int[] overwriteDimensions(final Metadata meta, final int imageIndex)
			throws IOException
		{
			getHandle().seek(dimensionOffset);
			// final int sizeX = (int) meta.getAxisLength(imageIndex, Axes.X);
			// final int sizeY = (int) meta.getAxisLength(imageIndex, Axes.Y);
			// final int z = (int) meta.getAxisLength(imageIndex, Axes.Z);
			// final int c = (int) meta.getAxisLength(imageIndex, Axes.SPECTRA);
			// final int t = (int) meta.getAxisLength(imageIndex, Axes.TIME);
			final int pixelType = meta.get(imageIndex).getPixelType();
			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);

			final StringBuilder dimOrder = new StringBuilder();
			final int[] sizes = new int[6];
			int nextSize = 0;
			sizes[nextSize++] = 8 * bytesPerPixel;

			if (meta.get(imageIndex).isMultichannel()) {
				dimOrder.append("ch\t");
				sizes[nextSize++] = (int) (meta.get(imageIndex).getAxisLength(
					Axes.CHANNEL));
			}

			for (final CalibratedAxis axis : meta.get(imageIndex).getAxes()) {
				if (axis.type() == Axes.CHANNEL) {
					if (dimOrder.indexOf("ch") == -1) {
						sizes[nextSize++] = (int) meta.get(imageIndex).getAxisLength(
							Axes.CHANNEL);
						dimOrder.append("ch");
					}
					else {
						continue;
					}
				}
				else {
					sizes[nextSize++] = (int) meta.get(imageIndex).getAxisLength(axis
						.type());
					dimOrder.append(String.valueOf(axis.type().getLabel().charAt(0))
						.toLowerCase());
				}
				dimOrder.append("\t");
			}
			getHandle().writeBytes("layout\torder\tbits\t" + dimOrder.toString() +
				"\n");
			getHandle().writeBytes("layout\tsizes\t");
			for (final int size : sizes) {
				getHandle().writeBytes(size + "\t");
			}
			while ((getHandle().offset() - dimensionOffset) < dimensionLength - 1) {
				getHandle().writeBytes(" ");
			}
			getHandle().writeBytes("\n");

			return sizes;
		}

	}

	/**
	 * Collection of utility methods and constants for working with ICS images.
	 */
	public static class ICSUtils {

		private ICSUtils() {
			// NB. Utility class
		}

		// -- Constants --

		/** Newline terminator sequences. */
		private static final String[] NL = { "\n", "\r\n", "\r" };

		public static final String LEAF = "VALID_LEAF";

		public static final String[] DATE_FORMATS = {
			"EEEE, MMMM dd, yyyy HH:mm:ss", "EEE dd MMMM yyyy HH:mm:ss",
			"EEE MMM dd HH:mm:ss yyyy", "EE dd MMM yyyy HH:mm:ss z",
			"HH:mm:ss dd\\MM\\yy" };

		// These strings appeared in the former metadata field categories but
		// are not found in the LOCI sample files.
		//
		// The former metadata field categories table did not save the context,
		// i.e. the first token such as "document" or "history" and other
		// intermediate tokens. The preceding tables such as DOCUMENT_KEYS or
		// HISTORY_KEYS use this full context.
		//
		// In an effort at backward compatibility, these will be used to form
		// key value pairs if key/value pair not already assigned and they match
		// anywhere in the input line.
		//
		public static String[][] OTHER_KEYS = { { "cube", "descriptio" },
			// sic; // also listed in HISTORY_KEYS
			{ "cube", "description" }, // correction; also listed in HISTORY_KEYS
			{ "image", "form" }, // also listed in HISTORY_KEYS
			{ "refinxlensmedium" }, // Could be a mispelling of "refrinxlensmedium";
			// also listed in SENSOR_KEYS
			{ "refinxmedium" }, // Could be a mispelling of "refinxmedium";
			// also listed in SENSOR_KEYS
			{ "scil_type" }, { "source" } };

		/** Metadata field categories. */
		public static final Map<String, Object> keys = createKeyMap();

		private static Map<String, Object> createKeyMap() {
			final Map<String, Object> root = new HashMap<>();

			addKey(root, "ics_version");
			addKey(root, "parameter", "ch");
			addKey(root, "parameter", "scale");
			addKey(root, "parameter", "t");
			addKey(root, "parameter", "units");
			addKey(root, "parameter", "labels");
			addKey(root, "sensor", "s_params", "lambdaem");
			addKey(root, "sensor", "s_params", "lambdaex");
			addKey(root, "sensor", "s_params", "pinholeradius");
			addKey(root, "representation", "byte_order");
			addKey(root, "representation", "compression");
			addKey(root, "representation", "format");
			addKey(root, "representation", "sign");
			addKey(root, "history", "author");
			addKey(root, "history", "camera", "manufcaturer");
			addKey(root, "history", "camera", "model");
			addKey(root, "history", "cube", "emm", "nm");
			addKey(root, "history", "cube", "exc", "nm");
			addKey(root, "history", "date");
			addKey(root, "history", "creation", "date");
			addKey(root, "history", "created", "on");
			addKey(root, "history", "experimenter");
			addKey(root, "history", "exposure");
			addKey(root, "history", "extents");
			addKey(root, "history", "units");
			addKey(root, "history", "filterset");
			addKey(root, "history", "filterset", "dichroic", "name");
			addKey(root, "history", "filterset", "emm", "name");
			addKey(root, "history", "filterset", "exc", "name");
			addKey(root, "history", "gain");
			addKey(root, "history", "laser", "manufacturer");
			addKey(root, "history", "laser", "model");
			addKey(root, "history", "laser", "rep", "rate");
			addKey(root, "history", "laser", "power");
			addKey(root, "history", "labels");
			addKey(root, "history", "manufacturer");
			addKey(root, "history", "microscope");
			addKey(root, "history", "objective", "type");
			addKey(root, "history", "objective", "immersion");
			addKey(root, "history", "objective", "na");
			addKey(root, "history", "objective", "workingdistance");
			addKey(root, "history", "objective", "magnification");
			addKey(root, "history", "objective", "mag");
			addKey(root, "history", "other", "text");
			addKey(root, "history", "stage", "positionx");
			addKey(root, "history", "stage", "positiony");
			addKey(root, "history", "stage", "positionz");
			addKey(root, "history", "stage_xyzum");
			addKey(root, "history", "step");
			addKey(root, "history", "type");
			addKey(root, "history", "wavelength*");
			addKey(root, "layout", "order");
			addKey(root, "layout", "significant_bits");
			addKey(root, "layout", "sizes");

			return root;
		}

		/**
		 * Recursively descends the list of keys (ICS header categories and sub-
		 * categories). When reaching the final subcategory, creates a String:String
		 * mapping using {@link #LEAF}, indicating a valid complete key. When sub-
		 * categories are present, a new map is created at each step, generating a
		 * tree structure of valid keys.
		 */
		@SuppressWarnings("unchecked")
		private static void addKey(final Map<String, Object> parent,
			final String... keys)
		{
			// Create a LEAF entry to indicate this is a valid key
			if (keys.length == 0) {
				parent.put(LEAF, LEAF);
			}
			// the head category may have multiple subcategories, so create a
			// category mapping.
			else {
				final String node = keys[0];
				final Object o = parent.get(node);
				Map<String, Object> child = null;
				// check for existing map
				if (o == null) {
					child = new HashMap<>();
					parent.put(node, child);
				}
				else {
					child = (Map<String, Object>) o;
				}
				// recursive step to build the category tree
				addKey(child, Arrays.copyOfRange(keys, 1, keys.length));
			}
		}
	}

	/**
	 * SCIFIO file format Translator for ICS Metadata objects to the SCIFIO Core
	 * metadata type.
	 */
	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class ICSTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public Class<? extends io.scif.Metadata> source() {
			return io.scif.Metadata.class;
		}

		@Override
		public Class<? extends io.scif.Metadata> dest() {
			return Metadata.class;
		}

		@Override
		public void translateImageMetadata(final List<ImageMetadata> source,
			final Metadata dest)
		{
			// note that the destination fields will preserve their default
			// values only the keyValPairs will be modified

			Map<String, String> keyValPairs = null;
			if (dest.getKeyValPairs() == null) keyValPairs = new HashMap<>();
			else keyValPairs = dest.getKeyValPairs();

			final int numAxes = source.get(0).getAxes().size();

			String order = "";
			String sizes = "";
			String units = "";

			for (int i = 0; i < numAxes; i++) {
				final CalibratedAxis axis = source.get(0).getAxis(i);
				final AxisType axisType = axis.type();

				// flag for RGB images
				if (axisType.equals(Axes.X)) {
					order += "x";
				}
				else if (axisType.equals(Axes.Y)) {
					order += "y";
				}
				else if (axisType.equals(Axes.Z)) {
					order += "z";
				}
				else if (axisType.equals(Axes.TIME)) {
					order += "t";
				}
				else if (axisType.equals(Axes.CHANNEL)) {
					// ICS flag for RGB images
					if (source.get(0).isMultichannel()) {
						order = "c " + order;
						sizes = source.get(0).getAxisLength(i) + " " + sizes;
						units += axis.unit() + " ";
						continue;
					}

					order += "c";
				}
				else if (axisType.equals(SCIFIOAxes.PHASE)) {
					order += "p";
				}
				else if (axisType.equals(SCIFIOAxes.FREQUENCY)) {
					order += "f";
				}
				else {
					if (axisType.getLabel().equals("bits")) order += "bits";
					else order += "u";
				}

				order += " ";
				sizes += source.get(0).getAxisLength(i) + " ";
				units += axis.unit() + " ";
			}

			keyValPairs.put("layout sizes", sizes);
			keyValPairs.put("layout order", order);
			keyValPairs.put("parameter units", units);

			keyValPairs.put("layout significant_bits", "" + source.get(0)
				.getBitsPerPixel());

			if (source.get(0).getAxisLength(SCIFIOAxes.LIFETIME) > 1) keyValPairs.put(
				"history type", "time resolved");

			boolean signed = false;
			boolean fPoint = false;

			switch (source.get(0).getPixelType()) {
				case FormatTools.INT8:
				case FormatTools.INT16:
				case FormatTools.INT32:
					signed = true;
					break;
				case FormatTools.UINT8:
				case FormatTools.UINT16:
				case FormatTools.UINT32:
					break;
				case FormatTools.FLOAT:
				case FormatTools.DOUBLE:
					fPoint = true;
					signed = true;
					break;
			}

			keyValPairs.put("representation sign", signed ? "signed" : "");
			keyValPairs.put("representation format", fPoint ? "real" : "");
			keyValPairs.put("representation compression", "");

			String byteOrder;

			if (source.get(0).isLittleEndian()) byteOrder = fPoint ? "1" : "0";
			else byteOrder = fPoint ? "0" : "1";

			if (source.get(0).getBitsPerPixel() < 32) {
				if ("0".equals(byteOrder)) byteOrder = "1";
				else byteOrder = "0";
			}

			keyValPairs.put("representation byte_order", byteOrder);

			final List<CalibratedAxis> axes = source.get(0).getAxes();

			String scale = "";
			for (int i = 0; i < axes.size(); i++) {
				scale += axes.get(i).averageScale(0, 1) + " ";
			}

			keyValPairs.put("parameter scale", scale);

			dest.setKeyValPairs(keyValPairs);
		}
	}
}
