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
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * SCIFIO Format supporting the <a
 * href="http://en.wikipedia.org/wiki/Image_Cytometry_Standard">ICS</a> image
 * format.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class ICSFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Image Cytometry Standard";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "ics", "ids" };
	}

	// -- Nested Classes --

	/**
	 * SCIFIO Metadata object for ICS images.
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Static Constants
		public static final String CNAME = "io.scif.formats.ICSFormat$Metadata";

		// -- Fields --

		/**
		 * Whether this file is ICS version 2, and thus does not have an IDS
		 * companion
		 */
		protected boolean versionTwo = false;

		/** Offset to pixel data */
		protected long offset = -1;

		/** True if instrument information was discovered. */
		protected boolean hasInstrumentData = false;

		/** True if this planes were stored in RGB order. */
		private boolean storedRGB;

		/** ICS file name */
		protected String icsId = "";

		/** IDS file name */
		protected String idsId = "";

		/** ICS Metadata */
		protected Hashtable<String, String> keyValPairs;

		// -- Constructor --

		public Metadata() {
			keyValPairs = new Hashtable<String, String>();
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			// Common metadata population
			createImageMetadata(1);

			final ImageMetadata imageMeta = get(0);
			imageMeta.setRGB(false);

			// find axis sizes

			final double[] axesSizes = getAxesSizes();
			final String[] axes = getAxes();

			// Reset the existing axes
			imageMeta.setAxes(new CalibratedAxis[0], new int[0]);

			int bitsPerPixel = 0;

			// interpret axis information
			for (int n = 0; n < axes.length; n++) {
				final String axis = axes[n].toLowerCase();
				if (axis.equals("x")) {
					imageMeta.addAxis(Axes.X, new Double(axesSizes[n]).intValue());
				}
				else if (axis.equals("y")) {
					imageMeta.addAxis(Axes.Y, new Double(axesSizes[n]).intValue());
				}
				else if (axis.equals("z")) {
					imageMeta.addAxis(Axes.Z, new Double(axesSizes[n]).intValue());
				}
				else if (axis.equals("t")) {
					final int tIndex = imageMeta.getAxisIndex(Axes.TIME);

					if (tIndex == -1) {
						imageMeta.addAxis(Axes.TIME, new Double(axesSizes[n]).intValue());
					}
					else imageMeta.setAxisLength(Axes.TIME, imageMeta
						.getAxisLength(Axes.TIME) *
						new Double(axesSizes[n]).intValue());
				}
				else if (axis.equals("bits")) {
					bitsPerPixel = new Double(axesSizes[n]).intValue();
					while (bitsPerPixel % 8 != 0)
						bitsPerPixel++;
					if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
					imageMeta.addAxis(new Axes.CustomType("bits"), bitsPerPixel);
				}
				else {
					final int cIndex = imageMeta.getAxisIndex(Axes.CHANNEL);

					if (cIndex == -1) {
						imageMeta
							.addAxis(Axes.CHANNEL, new Double(axesSizes[n]).intValue());
					}
					else {
						imageMeta.setAxisLength(Axes.CHANNEL, imageMeta
							.getAxisLength(Axes.CHANNEL) *
							new Double(axesSizes[n]).intValue());
					}

					if (imageMeta.getAxisIndex(Axes.X) == -1) {
						setStoredRGB(true);

						final int sizeC = imageMeta.getAxisLength(Axes.CHANNEL);

						imageMeta.setRGB(sizeC <= 4 && sizeC > 1);
					}

					if (axis.startsWith("c")) {
						imageMeta.setAxisType(n, Axes.CHANNEL);
					}
					else if (axis.startsWith("p")) {
						imageMeta.setAxisType(n, Axes.PHASE);
					}
					else if (axis.startsWith("f")) {
						imageMeta.setAxisType(n, Axes.FREQUENCY);
					}
					else {
						imageMeta.setAxisType(n, Axes.unknown());
					}
				}
			}

			// Re-order axes if RGB
			if (imageMeta.getAxisIndex(Axes.CHANNEL) >= 0 &&
				imageMeta.getAxisIndex(Axes.CHANNEL) < imageMeta.getAxisIndex(Axes.Y))
			{
				imageMeta.setAxisType(imageMeta.getAxisIndex(Axes.Y), Axes.CHANNEL);
			}

			if (getBitsPerPixel() != null) bitsPerPixel = getBitsPerPixel();

			imageMeta.setBitsPerPixel(bitsPerPixel);

			if (imageMeta.isRGB() && getEMWaves() != null &&
				getEMWaves().length == imageMeta.getAxisLength(Axes.CHANNEL))
			{
				imageMeta.setRGB(false);
				setStoredRGB(true);
			}

			if (imageMeta.getAxisIndex(Axes.Z) == -1) {
				imageMeta.addAxis(Axes.Z, 1);
			}
			if (imageMeta.getAxisIndex(Axes.CHANNEL) == -1) {
				imageMeta.addAxis(Axes.CHANNEL, 1);
			}
			if (imageMeta.getAxisIndex(Axes.TIME) == -1) {
				imageMeta.addAxis(Axes.TIME, 1);
			}

			if (imageMeta.getAxisLength(Axes.Z) == 0) imageMeta.setAxisLength(Axes.Z,
				1);
			if (imageMeta.getAxisLength(Axes.CHANNEL) == 0) imageMeta.setAxisLength(
				Axes.CHANNEL, 1);
			if (imageMeta.getAxisLength(Axes.TIME) == 0) imageMeta.setAxisLength(
				Axes.TIME, 1);

			imageMeta.setInterleaved(imageMeta.isRGB());
			imageMeta.setIndexed(false);
			imageMeta.setFalseColor(false);
			imageMeta.setMetadataComplete(true);
			imageMeta.setLittleEndian(true);

			final boolean lifetime = getLifetime();
			final String label = getLabels();

			// HACK - support for Gray Institute at Oxford's ICS lifetime data
			if (lifetime && label != null) {
				int binCount = 0;
				String newOrder = null;

				if (label.equalsIgnoreCase("t x y")) {
					newOrder = "XYCZT";
					imageMeta.setInterleaved(true);
					binCount = imageMeta.getAxisLength(Axes.X);
					imageMeta.setAxisLength(Axes.X, imageMeta.getAxisLength(Axes.Y));
					imageMeta.setAxisLength(Axes.Y, imageMeta.getAxisLength(Axes.Z));
					imageMeta.setAxisLength(Axes.Z, 1);
				}
				else if (label.equalsIgnoreCase("x y t")) {
					newOrder = "XYCZT";
					binCount = imageMeta.getAxisLength(Axes.Z);
					imageMeta.setAxisLength(Axes.Z, 1);
				}
				else {
					log().debug("Lifetime data, unexpected 'history labels' " + label);
				}

				if (newOrder != null) {
					// FIXME: Make sure this works.
					imageMeta.setAxisTypes(FormatTools.findDimensionList(newOrder));
					imageMeta.setAxisLength(Axes.LIFETIME, binCount);
				}
			}

			imageMeta.setPlaneCount(getAxisLength(0, Axes.Z) *
				getAxisLength(0, Axes.TIME));

			if (!imageMeta.isRGB()) {
				imageMeta.setPlaneCount(imageMeta.getPlaneCount() *
					imageMeta.getAxisLength(Axes.CHANNEL));
			}

			final String byteOrder = getByteOrder();
			final String rFormat = getRepFormat();

			if (byteOrder != null) {
				final String firstByte = byteOrder.split(" ")[0];
				final int first = Integer.parseInt(firstByte);
				final boolean little = rFormat.equals("real") ? first == 1 : first != 1;
				imageMeta.setLittleEndian(little);
			}

			final int bytes = bitsPerPixel / 8;

			if (bitsPerPixel < 32) imageMeta.setLittleEndian(!isLittleEndian(0));

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

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) keyValPairs = new Hashtable<String, String>();
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
		 * 
		 * @param key
		 * @return
		 */
		public String get(final String key) {
			return keyValPairs.get(key);
		}

		public String getIcsId() {
			return icsId;
		}

		public void setIcsId(final String icsId) {
			this.icsId = icsId;
		}

		public String getIdsId() {
			return idsId;
		}

		public void setIdsId(final String idsId) {
			this.idsId = idsId;
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

		public void setKeyValPairs(final Hashtable<String, String> keyValPairs) {
			this.keyValPairs = keyValPairs;
		}

		public Hashtable<String, String> getKeyValPairs() {
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

		public void putChannelNames(final Hashtable<Integer, String> cNames) {
			put("parameter ch", merge(cNames.values()));
		}

		public void putPinholes(final Hashtable<Integer, Double> pins) {
			put("sensor s_params PinholeRadius", merge(pins.values()));
		}

		public void putEMWaves(final Integer[] emWaves) {
			put("sensor s_params LambdaEm", merge(emWaves));
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

		public void putWavelengths(final Hashtable<Integer, Integer> waves) {
			put("history Wavelength*", merge(waves.values()));
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
			put("history laser model", laserRep.toString());
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
			put("history objective NA", lensNA.toString());
		}

		public void putWorkingDistance(final Double wd) {
			put("history objective WorkingDistance", wd.toString());
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

		public void putGains(final Hashtable<Integer, Double> gains) {
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
			put("history Exposure", expTime.toString());
		}

		// -- Accessor methods for dynamically retrieving common metadata --

		public String getDate() {
			String date = null;
			final String[] kv =
				findValueForKey("history date", "history created on",
					"history creation date");
			if (kv != null) {
				if (kv[0].equalsIgnoreCase("history date") ||
					kv[0].equalsIgnoreCase("history created on"))
				{
					if (kv[1].indexOf(" ") > 0) {
						date = kv[1].substring(0, kv[1].lastIndexOf(" "));
					}
				}
				else if (kv[0].equalsIgnoreCase("history creation date")) {
					date = kv[1];
				}
			}

			if (date != null) date =
				DateTools.formatDate(date, ICSFormat.ICSUtils.DATE_FORMATS);

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
			if (kv != null &&
				(kv[1].equalsIgnoreCase("time resolved") || kv[1]
					.equalsIgnoreCase("FluorescenceLifetime")))
			{
				lifetime = true;
			}
			return lifetime;
		}

		public String getLabels() {
			return findStringValueForKey("history labels");
		}

		public String getExperimentType() {
			return findStringValueForKey("history type");
		}

		public Double[] getPixelSizes() {
			final String[] kv = findValueForKey("parameter scale");
			return kv == null ? null : splitDoubles(kv[1]);
		}

		public String[] getUnits() {
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

		public double[] getAxesSizes() {
			final String[] kv = findValueForKey("layout sizes");
			double[] sizes = null;
			if (kv != null) {
				final String[] lengths = kv[1].split(" ");
				sizes = new double[lengths.length];
				for (int n = 0; n < sizes.length; n++) {
					try {
						sizes[n] = Double.parseDouble(lengths[n].trim());
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse axis length", e);
					}
				}
			}
			return sizes;
		}

		public double[] getPhysicalPixelSizes() {
			final String[] kv = findValueForKey("history extents");
			double[] sizes = null;
			if (kv != null) {
				final String[] lengths = kv[1].split(" ");
				sizes = new double[lengths.length];
				for (int n = 0; n < sizes.length; n++) {
					try {
						sizes[n] = Double.parseDouble(lengths[n].trim());
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse pixel sizes", e);
					}
				}
			}
			return sizes;
		}

		public Double[] getTimestamps() {
			final String[] kv = findValueForKey("parameter t");
			return kv == null ? null : splitDoubles(kv[1]);
		}

		public Hashtable<Integer, String> getChannelNames() {
			final String[] kv = findValueForKey("parameter ch");
			final Hashtable<Integer, String> channelNames =
				new Hashtable<Integer, String>();
			if (kv != null) {
				final String[] names = kv[1].split(" ");
				for (int n = 0; n < names.length; n++) {
					channelNames.put(new Integer(n), names[n].trim());
				}
			}
			return channelNames;
		}

		public void addStepChannel(final Hashtable<Integer, String> channelNames) {
			final String[] kv = findValueIteration("history step", "name");
			if (kv != null) channelNames.put(new Integer(kv[0].substring(12, kv[0]
				.indexOf(" ", 12))), kv[1]);
		}

		public void addCubeChannel(final Hashtable<Integer, String> channelNames) {
			final String[] kv = findValueForKey("history cube");
			if (kv != null) channelNames.put(new Integer(channelNames.size()), kv[1]);
		}

		public Hashtable<Integer, Double> getPinholes() {
			final String[] kv = findValueForKey("sensor s_params PinholeRadius");
			final Hashtable<Integer, Double> pinholes =
				new Hashtable<Integer, Double>();
			if (kv != null) {
				final String pins[] = kv[1].split(" ");
				int channel = 0;
				for (int n = 0; n < pins.length; n++) {
					if (pins[n].trim().equals("")) continue;
					try {
						pinholes.put(new Integer(channel++), new Double(pins[n]));
					}
					catch (final NumberFormatException e) {
						log().debug("Could not parse pinhole", e);
					}
				}
			}
			return pinholes;
		}

		public Integer[] getEMWaves() {
			final String[] kv = findValueForKey("sensor s_params LambdaEm");
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
			final String[] kv = findValueForKey("sensor s_params LambdaEx");
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

		public Hashtable<Integer, Integer> getWavelengths() {
			final String[] kv = findValueForKey("history Wavelength*");
			final Hashtable<Integer, Integer> wavelengths =
				new Hashtable<Integer, Integer>();
			if (kv != null) {
				final String[] waves = kv[1].split(" ");
				for (int n = 0; n < waves.length; n++) {
					wavelengths.put(new Integer(n), new Integer(waves[n]));
				}
			}
			return wavelengths;
		}

		public void
			addLaserWavelength(final Hashtable<Integer, Integer> wavelengths)
		{
			final String[] kv = findValueIteration("history laser", "wavelength");
			if (kv != null) {
				final int laser =
					Integer.parseInt(kv[0].substring(13, kv[0].indexOf(" ", 13))) - 1;
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
			return findDoubleValueForKey("history laser model");
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
			return findDoubleValueForKey("history objective NA");
		}

		public Double getWorkingDistance() {
			return findDoubleValueForKey("history objective WorkingDistance");
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

		public Hashtable<Integer, Double> getGains() {
			final String[] kv = findValueForKey("history gain");
			final Hashtable<Integer, Double> gains = new Hashtable<Integer, Double>();
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
			final String[] kv = findValueForKey("history Exposure");
			Double exposureTime = null;
			if (kv != null) {
				final String expTime = kv[1];
				if (expTime.indexOf(" ") != -1) {
					exposureTime = new Double(expTime.indexOf(" "));
				}
			}
			return exposureTime;
		}

		// -- Helper methods for finding key values --

		/* Given a list of tokens and an array of lists of regular expressions, tries
		 * to find a match.  If no match is found, looks in OTHER_KEYS.
		 */
		String[] findKeyValue(final String[] tokens, final String[][] regexesArray)
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
			final StringBuffer returnValue = new StringBuffer();
			for (int i = start; i < tokens.length && i < stop; ++i) {
				returnValue.append(tokens[i]);
				if (i < stop - 1) {
					returnValue.append(' ');
				}
			}
			return returnValue.toString();
		}

		private Double findDoubleValueForKey(final String... keys) {
			final String kv[] = findValueForKey(keys);

			return kv == null ? null : new Double(kv[1]);
		}

		private Integer findIntValueForKey(final String... keys) {
			final String kv[] = findValueForKey(keys);

			return kv == null ? null : new Integer(kv[1]);
		}

		private String findStringValueForKey(final String... keys) {
			final String kv[] = findValueForKey(keys);

			return kv == null ? null : kv[1];
		}

		/*
		 * Checks the list of keys for non-null values in the global hashtable.
		 * 
		 * If a non-null value is found, it is returned.
		 * 
		 * The returned array includes the matching key first, and the value second.
		 * 
		 */
		private String[] findValueForKey(final String... keys) {

			for (final String key : keys) {
				final String value = keyValPairs.get(key);
				if (value != null) return new String[] { key, value };
			}

			return null;
		}

		/*
		 * Iterates through the key set, looking for a key that starts
		 * and/or ends with the provided partial keys.
		 * 
		 * Returns an array containing the first matching key and its
		 * corresponding value if found, and an empty array otherwise.
		 * 
		 */
		private String[] findValueIteration(final String starts, final String ends)
		{

			for (final String key : keyValPairs.keySet()) {
				if ((starts == null || key.startsWith(starts)) &&
					(ends == null || key.endsWith(ends))) return new String[] { key,
					keyValPairs.get(key) };
			}

			return null;
		}

		/*
		 * Given a list of tokens and an array of lists of regular expressions, finds
		 * a match.  Returns key/value pair if matched, null otherwise.
		 *
		 * The first element, tokens[0], has already been matched to a category, i.e.
		 * 'history', and the regexesArray is category-specific.
		 */
		private String[] findKeyValueForCategory(final String[] tokens,
			final String[][] regexesArray)
		{
			String[] keyValue = null;
			for (final String[] regexes : regexesArray) {
				if (compareTokens(tokens, 1, regexes, 0)) {
					final int splitIndex = 1 + regexes.length; // add one for the category
					final String key = concatenateTokens(tokens, 0, splitIndex);
					final String value =
						concatenateTokens(tokens, splitIndex, tokens.length);
					keyValue = new String[] { key, value };
					break;
				}
			}
			return keyValue;
		}

		/* Given a list of tokens and an array of lists of regular expressions, finds
		 * a match.  Returns key/value pair if matched, null otherwise.
		 *
		 * The first element, tokens[0], represents a category and is skipped.  Look
		 * for a match of a list of regular expressions anywhere in the list of tokens.
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
						if (1 == regexes.length || compareTokens(tokens, i + 1, regexes, 1))
						{
							// if so, return key/value
							final int splitIndex = i + regexes.length;
							final String key = concatenateTokens(tokens, 0, splitIndex);
							final String value =
								concatenateTokens(tokens, splitIndex, tokens.length);
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
				if (i >= tokens.length || !tokens[i].toLowerCase().matches(regexes[j]))
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
			String s = "";

			for (final T v : values)
				s += (v.toString() + " ");

			return s;
		}
	}

	/**
	 * SCIFIO file format Checker for ICS images.
	 */
	public static class Checker extends AbstractChecker {

		// -- Checker API Methods --

	}

	/**
	 * SCIFIO file format Parser for ICS images.
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- ICSFormat.Parser Methods --

		/**
		 * @return True if the provided id is a version 2 ics id.
		 */
		public boolean isVersionTwo(final String id) throws IOException {
			final RandomAccessInputStream f =
				new RandomAccessInputStream(getContext(), id);
			final boolean singleFile =
				f.readString(17).trim().equals("ics_version\t2.0");
			f.close();
			return singleFile;
		}

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			findCompanion(stream, meta);

			final RandomAccessInputStream reader =
				new RandomAccessInputStream(getContext(), meta.getIcsId());

			reader.seek(0);
			reader.readString(ICSUtils.NL);
			String line = reader.readString(ICSUtils.NL);

			// Extracts the key, value pairs from each line and
			// inserts them into the ICSMetadata object
			while (line != null && !line.trim().equals("end") &&
				reader.getFilePointer() < reader.length() - 1)
			{
				line = line.trim();
				final String[] tokens = line.split("[ \t]");
				final StringBuffer key = new StringBuffer();
				for (int q = 0; q < tokens.length; q++) {
					tokens[q] = tokens[q].trim();
					if (tokens[q].length() == 0) continue;

					boolean foundValue = true;
					for (int i = 0; i < ICSUtils.CATEGORIES.length; i++) {
						if (tokens[q].matches(ICSUtils.CATEGORIES[i])) {
							foundValue = false;
							break;
						}
					}
					if (!foundValue) {
						key.append(tokens[q]);
						key.append(" ");
						continue;
					}

					final StringBuffer value = new StringBuffer(tokens[q++]);
					for (; q < tokens.length; q++) {
						value.append(" ");
						value.append(tokens[q].trim());
					}
					final String k = key.toString().trim().replaceAll("\t", " ");
					final String v = value.toString().trim();
					addGlobalMeta(k, v);
					meta.keyValPairs.put(k.toLowerCase(), v);
				}
				line = reader.readString(ICSUtils.NL);
			}

			reader.close();
			in.close();

			final String id = meta.isVersionTwo() ? meta.icsId : meta.idsId;
			in = new RandomAccessInputStream(getContext(), id);

			if (meta.versionTwo) {
				String s = in.readString(ICSUtils.NL);
				while (!s.trim().equals("end"))
					s = in.readString(ICSUtils.NL);
			}

			meta.offset = in.getFilePointer();

			in.seek(0);

			meta.setSource(in);

			meta.hasInstrumentData =
				nullKeyCheck(new String[] { "history cube emm nm",
					"history cube exc nm", "history objective NA", "history stage xyzum",
					"history objective magnification", "history objective mag",
					"history objective WorkingDistance", "history objective type",
					"history objective", "history objective immersion" });
		}

		// -- Helper Methods --

		/* Returns true if any of the keys in testKeys has a non-null value */
		private boolean nullKeyCheck(final String[] testKeys) {
			for (final String key : testKeys) {
				if (metadata.get(key) != null) {
					return true;
				}
			}
			return false;
		}

		/* Finds the companion file for a given stream (ICS and IDS are companions) */
		private void findCompanion(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			if (stream.getFileName() == null) return;

			String icsId = stream.getFileName(), idsId = stream.getFileName();
			final int dot = icsId.lastIndexOf(".");
			final String ext = dot < 0 ? "" : icsId.substring(dot + 1).toLowerCase();
			if (ext.equals("ics")) {
				// convert C to D regardless of case
				final char[] c = idsId.toCharArray();
				c[c.length - 2]++;
				idsId = new String(c);
			}
			else if (ext.equals("ids")) {
				// convert D to C regardless of case
				final char[] c = icsId.toCharArray();
				c[c.length - 2]--;
				icsId = new String(c);
			}

			final Location icsFile = new Location(getContext(), icsId);
			if (!icsFile.exists()) throw new FormatException("ICS file not found.");
			meta.icsId = icsId;

			// check if we have a v2 ICS file - means there is no companion IDS file
			final RandomAccessInputStream f =
				new RandomAccessInputStream(getContext(), icsId);
			final String version = f.readString(17).trim();
			f.close();

			if (version.equals("ics_version\t2.0")) {
				meta.versionTwo = true;
				meta.idsId = icsId;
			}
			else {
				final Location idsFile = new Location(getContext(), idsId);
				if (!idsFile.exists()) throw new FormatException(
					"IDS file does not exist.");
				meta.idsId = idsId;
			}
		}
	}

	/**
	 * File format SCIFIO Reader for Image Cytometry Standard (ICS) images.
	 * Version 1 and 2 supported.
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Fields --

		/* Last read plane index. */
		private int prevPlane;

		/* Whether or not the pixels are GZIP-compressed. */
		private boolean gzip;

		/* Cached GZIPInputStream */
		private GZIPInputStream gzipStream;

		/* Whether or not the image is inverted along the Y axis. */
		private boolean invertY; // TODO only in oldInitFile

		/* Image data. */
		private byte[] data;

		// -- Constructor --

		public Reader() {
			domains =
				new String[] { FormatTools.LM_DOMAIN, FormatTools.FLIM_DOMAIN,
					FormatTools.UNKNOWN_DOMAIN };
			hasCompanionFiles = true;
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, plane
				.getData().length, x, y, w, h);

			final int bpp =
				FormatTools.getBytesPerPixel(getMetadata().getPixelType(imageIndex));
			final int len = FormatTools.getPlaneSize(this, imageIndex);
			final int rowLen = FormatTools.getPlaneSize(this, w, 1, imageIndex);

			final int[] coordinates =
				FormatTools.getZCTCoords(this, imageIndex, planeIndex);
			final int[] prevCoordinates =
				FormatTools.getZCTCoords(this, imageIndex, prevPlane);

			if (!gzip) {
				getStream().seek(metadata.offset + planeIndex * (long) len);
			}
			else {
				long toSkip = (planeIndex - prevPlane - 1) * (long) len;
				if (gzipStream == null || planeIndex <= prevPlane) {
					FileInputStream fis = null;
					toSkip = planeIndex * (long) len;
					if (metadata.versionTwo) {
						fis = new FileInputStream(metadata.icsId);
						fis.skip(metadata.offset);
					}
					else {
						fis = new FileInputStream(metadata.idsId);
						toSkip += metadata.offset;
					}
					try {
						gzipStream = new GZIPInputStream(fis);
					}
					catch (final IOException e) {
						// the 'gzip' flag is set erroneously
						gzip = false;
						getStream().seek(metadata.offset + planeIndex * (long) len);
						gzipStream = null;
					}
				}

				if (gzipStream != null) {
					while (toSkip > 0) {
						toSkip -= gzipStream.skip(toSkip);
					}

					data =
						new byte[len *
							(getMetadata().storedRGB() ? getMetadata().getAxisLength(
								imageIndex, Axes.CHANNEL) : 1)];
					int toRead = data.length;
					while (toRead > 0) {
						toRead -= gzipStream.read(data, data.length - toRead, toRead);
					}
				}
			}

			// FIXME: Why not getMetadata().getSizeC()?
			final int sizeC =
				getMetadata().getLifetime() ? 1 : getMetadata().getAxisLength(
					imageIndex, Axes.CHANNEL);

			// FIXME: This logic needs to be reworked!
			if (!getMetadata().isRGB(imageIndex) && getMetadata().storedRGB()) {
				// channels are stored interleaved, but because there are more than we
				// can display as RGB, we need to separate them
				getStream().seek(
					metadata.offset +
						(long) len *
						FormatTools.getIndex(this, imageIndex, coordinates[0], 0,
							coordinates[2]));
				if (!gzip && data == null) {
					data =
						new byte[len *
							getMetadata().getAxisLength(imageIndex, Axes.CHANNEL)];
					getStream().read(data);
				}
				else if (!gzip &&
					(coordinates[0] != prevCoordinates[0] || coordinates[2] != prevCoordinates[2]))
				{
					getStream().read(data);
				}

				for (int row = y; row < h + y; row++) {
					for (int col = x; col < w + x; col++) {
						final int src =
							bpp *
								((planeIndex % getMetadata().getAxisLength(imageIndex,
									Axes.CHANNEL)) + sizeC *
									(row * (row * getMetadata().getAxisLength(imageIndex, Axes.X) + col)));
						final int dest = bpp * ((row - y) * w + (col - x));
						System.arraycopy(data, src, plane.getBytes(), dest, bpp);
					}
				}
			}
			else if (gzip) {
				final RandomAccessInputStream s =
					new RandomAccessInputStream(getContext(), data);
				readPlane(s, imageIndex, x, y, w, h, plane);
				s.close();
			}
			else {
				readPlane(getStream(), imageIndex, x, y, w, h, plane);
			}

			if (invertY) {
				final byte[] row = new byte[rowLen];
				for (int r = 0; r < h / 2; r++) {
					final int topOffset = r * rowLen;
					final int bottomOffset = (h - r - 1) * rowLen;
					System.arraycopy(plane.getBytes(), topOffset, row, 0, rowLen);
					System.arraycopy(plane.getBytes(), bottomOffset, plane, topOffset,
						rowLen);
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
				// TODO hasInstrumentData = false;
				if (gzipStream != null) {
					gzipStream.close();
				}
				gzipStream = null;
			}
		}

		@Override
		public void setMetadata(final Metadata meta) throws IOException {
			super.setMetadata(meta);
			gzip = metadata.get("representation compression").equals("gzip");
			prevPlane = -1;
			gzipStream = null;
			invertY = false;
			data = null;
		}

		@Override
		public void setSource(final RandomAccessInputStream stream)
			throws IOException
		{
			if (!getMetadata().versionTwo) {
				stream.close();
				super.setSource(new RandomAccessInputStream(getContext(),
					getMetadata().idsId));
			}
			else {
				super.setSource(stream);
			}
		}

		@Override
		public String[] getDomains() {
			FormatTools.assertStream(getStream(), true, 0);
			final String[] domain = new String[] { FormatTools.GRAPHICS_DOMAIN };
			if (getMetadata().getAxisLength(0, Axes.LIFETIME) > 1) {
				domain[0] = FormatTools.FLIM_DOMAIN;
			}
			else if (metadata.hasInstrumentData) {
				domain[0] = FormatTools.LM_DOMAIN;
			}

			return domain;
		}
	}

	/**
	 * SCIFIO file format writer for ICS version 1 and 2 files.
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		// -- Fields --

		private long dimensionOffset;
		private int dimensionLength;
		private long pixelOffset;
		private int lastPlane = -1;
		private RandomAccessOutputStream pixels;

		// NB: write in ZTC order by default. Certain software (e.g. Volocity)
		// lacks the capacity to import files with any other dimension
		// ordering. Technically, this is not our problem, but it is
		// easy enough to work around and makes life easier for our users.
		private String outputOrder = "XYZTC";

		// -- ICSWriter API methods --

		/**
		 * Set the order in which dimensions should be written to the file. Valid
		 * values are specified in the documentation for
		 * {@link loci.formats.IFormatReader#getDimensionOrder()} By default, the
		 * ordering is "XYZTC".
		 */
		public void setOutputOrder(final String outputOrder) {
			this.outputOrder = outputOrder;
		}

		// -- Writer API Methods --

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final int x, final int y, final int w, final int h)
			throws FormatException, IOException
		{
			checkParams(imageIndex, planeIndex, plane.getBytes(), x, y, w, h);

			final int rgbChannels = getMetadata().getRGBChannelCount(imageIndex);

			final int sizeZ = getMetadata().getAxisLength(imageIndex, Axes.Z);
			int sizeC = getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);
			if (rgbChannels <= sizeC) {
				sizeC /= rgbChannels;
			}

			final int sizeT = getMetadata().getAxisLength(imageIndex, Axes.TIME);
			final int planes = sizeZ * sizeC * sizeT;

			final int[] coords =
				FormatTools.getZCTCoords(outputOrder, sizeZ, sizeC, sizeT, planes,
					imageIndex, planeIndex);
			final int realIndex =
				FormatTools.getIndex(outputOrder, sizeZ, sizeC, sizeT, planes,
					coords[0], coords[1], coords[2]);

			final int sizeX = getMetadata().getAxisLength(imageIndex, Axes.X);
			final int sizeY = getMetadata().getAxisLength(imageIndex, Axes.Y);
			final int pixelType = getMetadata().getPixelType(imageIndex);
			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
			final int planeSize = sizeX * sizeY * rgbChannels * bytesPerPixel;

			if (!initialized[planeIndex][realIndex]) {
				initialized[planeIndex][realIndex] = true;

				if (!isFullPlane(imageIndex, x, y, w, h)) {
					pixels.seek(pixelOffset + (realIndex + 1) * planeSize);
				}
			}

			pixels.seek(pixelOffset + realIndex * planeSize);
			if (isFullPlane(imageIndex, x, y, w, h) &&
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
							final int index =
								interleaved ? rgbChannels * (row * w + col) + c : w *
									(c * h + row) + col;
							strip.write(plane.getBytes(), index * bytesPerPixel,
								bytesPerPixel);
						}
					}
					pixels.skipBytes(bytesPerPixel * rgbChannels * x);
					pixels.write(strip.toByteArray());
					pixels.skipBytes(bytesPerPixel * rgbChannels * (sizeX - w - x));
				}
			}
			lastPlane = realIndex;
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32,
				FormatTools.UINT32, FormatTools.FLOAT };
		}

		public void close(final int imageIndex) throws IOException {
			if (lastPlane != getMetadata().getPlaneCount(imageIndex) - 1 &&
				out != null)
			{
				overwriteDimensions(getMetadata(), imageIndex);
			}

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
		public void setDest(final String id) throws FormatException, IOException {
			updateMetadataIds(id);
			super.setDest(id);
		}

		@Override
		public void setDest(final String id, final int imageIndex)
			throws FormatException, IOException
		{
			updateMetadataIds(id);
			super.setDest(id, imageIndex);
		}

		@Override
		public void
			setDest(final RandomAccessOutputStream out, final int imageIndex)
				throws FormatException, IOException
		{
			super.setDest(out, imageIndex);
			initialize(imageIndex);
		}

		// -- Helper methods --

		/* Sets the ICS Metadta icsId and idsId fields */
		private void updateMetadataIds(final String id) {
			metadata.idsId = FormatTools.checkSuffix(id, "ids") ? id : makeIdsId(id);
			metadata.icsId = FormatTools.checkSuffix(id, "ics") ? id : makeIcsId(id);
		}

		private void initialize(final int imageIndex) throws FormatException,
			IOException
		{
			final String currentId =
				getMetadata().idsId != null ? getMetadata().idsId : getMetadata().icsId;

			if (FormatTools.checkSuffix(getMetadata().idsId, "ids")) {
				final String metadataFile = makeIcsId(currentId);
				if (out != null) out.close();
				out = new RandomAccessOutputStream(getContext(), metadataFile);
			}

			if (out.length() == 0) {
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

				final int pixelType = meta.getPixelType(imageIndex);

				dimensionOffset = out.getFilePointer();
				final int[] sizes = overwriteDimensions(meta, imageIndex);
				dimensionLength = (int) (out.getFilePointer() - dimensionOffset);

				if (validBits != 0) {
					out.writeBytes("layout\tsignificant_bits\t" + validBits + "\n");
				}

				final boolean signed = FormatTools.isSigned(pixelType);
				final boolean littleEndian = meta.isLittleEndian(imageIndex);

				out.writeBytes("representation\tformat\t" +
					(pixelType == FormatTools.FLOAT ? "real\n" : "integer\n"));
				out.writeBytes("representation\tsign\t" +
					(signed ? "signed\n" : "unsigned\n"));
				out.writeBytes("representation\tcompression\tuncompressed\n");
				out.writeBytes("representation\tbyte_order\t");
				for (int i = 0; i < sizes[0] / 8; i++) {
					if ((littleEndian && (sizes[0] < 32 || pixelType == FormatTools.FLOAT)) ||
						(!littleEndian && sizes[0] >= 32 && pixelType != FormatTools.FLOAT))
					{
						out.writeBytes((i + 1) + "\t");
					}
					else {
						out.writeBytes(((sizes[0] / 8) - i) + "\t");
					}
				}

				out.writeBytes("\nparameter\tscale\t1.000000\t");

				final StringBuffer units = new StringBuffer();
				for (int i = 0; i < outputOrder.length(); i++) {
					final char dim = outputOrder.charAt(i);
					Number value = 1.0;
					if (dim == 'X') {
						if (meta.getAxisIndex(imageIndex, Axes.X) != -1) {
							value = meta.getAxisLength(imageIndex, Axes.X);
						}
						units.append("micrometers\t");
					}
					else if (dim == 'Y') {
						if (meta.getAxisIndex(imageIndex, Axes.Y) != -1) {
							value = meta.getAxisLength(imageIndex, Axes.Y);
						}
						units.append("micrometers\t");
					}
					else if (dim == 'Z') {
						if (meta.getAxisIndex(imageIndex, Axes.Z) != -1) {
							value = meta.getAxisLength(imageIndex, Axes.X);
						}
						units.append("micrometers\t");
					}
					else if (dim == 'T') {
						if (meta.getAxisIndex(imageIndex, Axes.TIME) != -1) {
							value = meta.getAxisLength(imageIndex, Axes.TIME);
						}
						units.append("seconds\t");
					}
					out.writeBytes(value + "\t");
				}

				out.writeBytes("\nparameter\tunits\tbits\t" + units.toString() + "\n");
				out.writeBytes("\nend\n");
				pixelOffset = out.getFilePointer();
			}
			else if (FormatTools.checkSuffix(currentId, "ics")) {
				final RandomAccessInputStream in =
					new RandomAccessInputStream(getContext(), currentId);
				in.findString("\nend\n");
				pixelOffset = in.getFilePointer();
				in.close();
			}

			if (FormatTools.checkSuffix(currentId, "ids")) {
				pixelOffset = 0;
			}

			if (pixels == null) {
				pixels = new RandomAccessOutputStream(getContext(), currentId);
			}
		}

		private int[]
			overwriteDimensions(final Metadata meta, final int imageIndex)
				throws IOException
		{
			out.seek(dimensionOffset);
			final int sizeX = meta.getAxisLength(imageIndex, Axes.X);
			final int sizeY = meta.getAxisLength(imageIndex, Axes.Y);
			final int z = meta.getAxisLength(imageIndex, Axes.Z);
			final int c = meta.getAxisLength(imageIndex, Axes.CHANNEL);
			final int t = meta.getAxisLength(imageIndex, Axes.TIME);
			final int pixelType = meta.getPixelType(imageIndex);
			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
			final int rgbChannels = meta.getRGBChannelCount(imageIndex);

			if (lastPlane < 0) lastPlane = z * c * t - 1;
			final int[] pos =
				FormatTools.getZCTCoords(outputOrder, z, c, t, z * c * t, imageIndex,
					lastPlane);
			lastPlane = -1;

			final StringBuffer dimOrder = new StringBuffer();
			final int[] sizes = new int[6];
			int nextSize = 0;
			sizes[nextSize++] = 8 * bytesPerPixel;

			if (rgbChannels > 1) {
				dimOrder.append("ch\t");
				sizes[nextSize++] = pos[1] + 1;
			}

			for (int i = 0; i < outputOrder.length(); i++) {
				if (outputOrder.charAt(i) == 'X') sizes[nextSize++] = sizeX;
				else if (outputOrder.charAt(i) == 'Y') sizes[nextSize++] = sizeY;
				else if (outputOrder.charAt(i) == 'Z') sizes[nextSize++] = pos[0] + 1;
				else if (outputOrder.charAt(i) == 'T') sizes[nextSize++] = pos[2] + 1;
				else if (outputOrder.charAt(i) == 'C' && dimOrder.indexOf("ch") == -1) {
					sizes[nextSize++] = pos[1] + 1;
					dimOrder.append("ch");
				}
				if (outputOrder.charAt(i) != 'C') {
					dimOrder.append(String.valueOf(outputOrder.charAt(i)).toLowerCase());
				}
				dimOrder.append("\t");
			}
			out.writeBytes("layout\torder\tbits\t" + dimOrder.toString() + "\n");
			out.writeBytes("layout\tsizes\t");
			for (int i = 0; i < sizes.length; i++) {
				out.writeBytes(sizes[i] + "\t");
			}
			while ((out.getFilePointer() - dimensionOffset) < dimensionLength - 1) {
				out.writeBytes(" ");
			}
			out.writeBytes("\n");

			return sizes;
		}

		/* Turns the provided id into a .ics path */
		private String makeIcsId(final String idsId) {
			return setIdExtension(idsId, ".ics");
		}

		/* Turns the provided id into a .ids path */
		private String makeIdsId(final String icsId) {
			return setIdExtension(icsId, ".ids");
		}

		/* Replaces the current extension of the provided id with the
		 * provided extension
		 */
		private String setIdExtension(String id, final String extension) {
			id = id.substring(0, id.lastIndexOf("."));
			id += extension;
			return id;
		}
	}

	/**
	 * Collection of utility methods and constants for working with ICS images.
	 */
	public static class ICSUtils {

		// -- Constants --

		/** Newline characters. */
		public static final String NL = "\r\n";

		public static final String[] DATE_FORMATS = {
			"EEEE, MMMM dd, yyyy HH:mm:ss", "EEE dd MMMM yyyy HH:mm:ss",
			"EEE MMM dd HH:mm:ss yyyy", "EE dd MMM yyyy HH:mm:ss z",
			"HH:mm:ss dd\\MM\\yy" };

		// These strings appeared in the former metadata field categories but are
		// not
		// found in the LOCI sample files.
		//
		// The former metadata field categories table did not save the context, i.e.
		// the first token such as "document" or "history" and other intermediate
		// tokens. The preceding tables such as DOCUMENT_KEYS or HISTORY_KEYS use
		// this full context.
		//
		// In an effort at backward compatibility, these will be used to form key
		// value pairs if key/value pair not already assigned and they match
		// anywhere
		// in the input line.
		//
		public static String[][] OTHER_KEYS = { { "cube", "descriptio" }, // sic;
																																			// also
																																			// listed
																																			// in
																																			// HISTORY_KEYS
			{ "cube", "description" }, // correction; also listed in HISTORY_KEYS
			{ "image", "form" }, // also listed in HISTORY_KEYS
			{ "refinxlensmedium" }, // Could be a mispelling of "refrinxlensmedium";
			// also listed in SENSOR_KEYS
			{ "refinxmedium" }, // Could be a mispelling of "refinxmedium";
			// also listed in SENSOR_KEYS
			{ "scil_type" }, { "source" } };

		/** Metadata field categories. */
		public static final String[] CATEGORIES = new String[] { "ics_version",
			"filename", "source", "layout", "representation", "parameter", "sensor",
			"history", "document", "view.*", "end", "file", "offset", "parameters",
			"order", "sizes", "coordinates", "significant_bits", "format", "sign",
			"compression", "byte_order", "origin", "scale", "units", "t", "labels",
			"SCIL_TYPE", "type", "model", "s_params", "gain.*", "dwell", "shutter.*",
			"pinhole", "laser.*", "version", "objective", "PassCount", "step.*",
			"date", "GMTdate", "label", "software", "author", "length",
			"Z (background)", "dimensions", "rep period", "image form", "extents",
			"offsets", "region", "expon. order", "a.*", "tau.*", "noiseval",
			"excitationfwhm", "created on", "text", "other text", "mode",
			"CFD limit low", "CFD limit high", "CFD zc level", "CFD holdoff",
			"SYNC zc level", "SYNC freq div", "SYNC holdoff", "TAC range",
			"TAC gain", "TAC offset", "TAC limit low", "ADC resolution",
			"Ext latch delay", "collection time", "repeat time", "stop on time",
			"stop on O'flow", "dither range", "count increment", "memory bank",
			"sync threshold", "dead time comp", "polarity", "line compressio",
			"scan flyback", "scan borders", "pixel time", "pixel clock", "trigger",
			"scan pixels x", "scan pixels y", "routing chan x", "routing chan y",
			"detector type", "channel.*", "filter.*", "wavelength.*",
			"black.level.*", "ht.*", "scan resolution", "scan speed", "scan zoom",
			"scan pattern", "scan pos x", "scan pos y", "transmission",
			"x amplitude", "y amplitude", "x offset", "y offset", "x delay",
			"y delay", "beam zoom", "mirror .*", "direct turret", "desc exc turret",
			"desc emm turret", "cube", "Stage_XYZum", "cube descriptio", "camera",
			"exposure", "bits/pixel", "binning", "left", "top", "cols", "rows",
			"significant_channels", "allowedlinemodes", "real_significant_bits",
			"sample_width", "range", "ch", "lower_limit", "higher_limit",
			"passcount", "detector", "dateGMT", "RefrInxMedium", "RefrInxLensMedium",
			"Channels", "PinholeRadius", "LambdaEx", "LambdaEm", "ExPhotonCnt",
			"RefInxMedium", "NumAperture", "RefInxLensMedium", "PinholeSpacing",
			"power", "name", "Type", "Magnification", "NA", "WorkingDistance",
			"Immersion", "Pinhole", "Channel .*", "Gain .*", "Shutter .*",
			"Position", "Size", "Port", "Cursor", "Color", "BlackLevel",
			"Saturation", "Gamma", "IntZoom", "Live", "Synchronize", "ShowIndex",
			"AutoResize", "UseUnits", "Zoom", "IgnoreAspect", "ShowCursor",
			"ShowAll", "Axis", "Order", "Tile", "DimViewOption", "channels",
			"pinholeradius", "refrinxmedium", "numaperture", "refrinxlensmedium",
			"image", "microscope", "stage", "filterset", "dichroic", "exc", "emm",
			"manufacturer", "experiment", "experimenter", "study", "metadata",
			"format", "icsviewer", "illumination", "exposure_time", "model", "ver",
			"creation", "date", "bpp", "bigendian", "size.", "physical_size.",
			"controller", "firmware", "pos", "position.", "mag", "na", "nm", "lamp",
			"built", "on", "rep", "rate", "Exposure", "Wavelength\\*" };

	}

	/**
	 * SCIFIO file format Translator for ICS Metadata objects to the SCIFIO Core
	 * metadata type.
	 */
	@Plugin(type = Translator.class, attrs = {
		@Attr(name = ICSTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = ICSTranslator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class ICSTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			// note that the destination fields will preserve their default values
			// only the keyValPairs will be modified

			Hashtable<String, String> keyValPairs = null;
			if (dest.getKeyValPairs() == null) keyValPairs =
				new Hashtable<String, String>();
			else keyValPairs = dest.getKeyValPairs();

			final int numAxes = source.getAxisCount(0);

			String order = "";
			String sizes = "";

			for (int i = 0; i < numAxes; i++) {
				final AxisType axis = source.getAxisType(0, i).type();

				// flag for RGB images
				if (axis.equals(Axes.X)) {
					order += "x";
				}
				else if (axis.equals(Axes.Y)) {
					order += "y";
				}
				else if (axis.equals(Axes.Z)) {
					order += "z";
				}
				else if (axis.equals(Axes.TIME)) {
					order += "t";
				}
				else if (axis.equals(Axes.CHANNEL)) {
					// ICS flag for RGB images
					if (source.isRGB(0)) {
						order = "c " + order;
						sizes = source.getAxisLength(0, i) + " " + sizes;
						continue;
					}

					order += "c";
				}
				else if (axis.equals(Axes.PHASE)) {
					order += "p";
				}
				else if (axis.equals(Axes.FREQUENCY)) {
					order += "f";
				}
				else {
					if (axis.getLabel().equals("bits")) order += "bits";
					else order += "u";
				}

				order += " ";
				sizes += source.getAxisLength(0, i) + " ";
			}

			keyValPairs.put("layout sizes", sizes);
			keyValPairs.put("layout order", order);

			keyValPairs
				.put("layout significant_bits", "" + source.getBitsPerPixel(0));

			if (source.getAxisLength(0, Axes.LIFETIME) > 1) keyValPairs.put(
				"history type", "time resolved");

			boolean signed = false;
			boolean fPoint = false;

			switch (source.getPixelType(0)) {
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

			String byteOrder = "0";

			if (source.isLittleEndian(0)) byteOrder = fPoint ? "1" : "0";
			else byteOrder = fPoint ? "0" : "1";

			if (source.getBitsPerPixel(0) < 32) {
				if (byteOrder.equals("0")) byteOrder = "1";
				else byteOrder = "0";
			}

			keyValPairs.put("representation byte_order", byteOrder);

			dest.setKeyValPairs(keyValPairs);
		}
	}
}
