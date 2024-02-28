/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2024 SCIFIO developers.
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
import io.scif.DefaultTranslator;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Translator;
import io.scif.config.SCIFIOConfig;
import io.scif.services.FormatService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;
import io.scif.xml.BaseHandler;
import io.scif.xml.XMLService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * MicromanagerReader is the file format reader for Micro-Manager files.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Micro-Manager")
public class MicromanagerFormat extends AbstractFormat {

	// -- Constants --

	/** File containing extra metadata. */
	private static final String METADATA = "metadata.txt";

	/**
	 * Optional file containing additional acquisition parameters. (And yes, the
	 * spelling is correct.)
	 */
	private static final String XML = "Acqusition.xml";

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "tif", "tiff", "txt", "xml" };
	}

	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		private List<Position> positions;

		// -- MicromanagerMetadata getters and setters --

		public List<Position> getPositions() {
			return positions;
		}

		public void setPositions(final List<Position> positions) {
			this.positions = positions;
		}

		// -- Metadata API methods --

		@Override
		public void populateImageMetadata() {

			for (int i = 0; i < getImageCount(); i++) {
				final ImageMetadata ms = get(i);

				ms.setAxisTypes(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME);
				ms.setPlanarAxisCount(2);
				ms.setLittleEndian(false);
				ms.setIndexed(false);
				ms.setFalseColor(false);
				ms.setMetadataComplete(true);
			}
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				positions = null;
			}
		}

	}

	public static class Checker extends AbstractChecker {

		@Parameter
		private FormatService formatService;

		@Parameter
		private DataHandleService dataHandleService;

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final Location location,
			final SCIFIOConfig config)
		{
			// not allowed to touch the file system
			if (!config.checkerIsOpen()) return false;

			try {
				// check metadata file
				if (validMetadataFile(location)) {
					try (DataHandle<Location> handle = dataHandleService.create(
						location))
					{
						return checkMetadataHandle(handle);
					}
				}

				// Ensure we can look for neighbors
				if (!(location instanceof BrowsableLocation)) return false;

				// Search for metadata file in the vicinity + check image file
				try (DataHandle<Location> handle = dataHandleService.create(location)) {
					return checkImageFile((BrowsableLocation) location, config, handle);
				}
			}
			catch (final IOException e) {
				log().error("Error when checking format: " + e);
				return false;
			}
		}

		private boolean checkImageFile(final BrowsableLocation location,
			final SCIFIOConfig config, final DataHandle<Location> handle)
		{
			try {
				final Location metaFile = location.sibling(METADATA);
				final boolean validMetaData = isFormat(handle);
				if (!validMetaData) return false;
				final io.scif.Checker checker;
				checker = formatService.getFormatFromClass(MinimalTIFFFormat.class)
					.createChecker();
				final boolean validTIFF = checker.isFormat(handle);
				return validTIFF && isFormat(metaFile, config);
			}
			catch (final FormatException | IOException e) {
				log().error("Error when checking format: ", e);
				return false;
			}
		}

		@Override
		public boolean isFormat(final DataHandle<Location> handle)
			throws IOException
		{
			final Location location = handle.get();
			if (validMetadataFile(location)) {
				handle.seek(0l);
				return checkMetadataHandle(handle);
			}

			// ensure we can look for neighbors
			return location instanceof BrowsableLocation;
		}

		private boolean validMetadataFile(final Location location) {
			if (location == null) return false;
			final String name = location.getName();
			return name.equals(METADATA) || name.endsWith(File.separator +
				METADATA) || name.equals(XML) || name.endsWith(File.separator + XML);
		}

		private boolean checkMetadataHandle(final DataHandle<Location> handle)
			throws IOException
		{
			if (!handle.exists()) return false;
			final int blockSize = 1048576;

			final long length = handle.length();
			final String data = handle.readString((int) Math.min(blockSize, length));
			return length > 0 && (data.contains("Micro-Manager") || data.contains(
				"micromanager"));
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

		// -- Fields --

		@Parameter
		private DataHandleService dataHandleService;

		@Parameter
		private TranslatorService translatorService;

		@Parameter
		private XMLService xmlService;

		// -- MicromanagerParser API methods --

		public void populateMetadata(final String[] jsonData, final Metadata source,
			final io.scif.Metadata dest) throws FormatException, IOException
		{
			source.createImageMetadata(jsonData.length);
			final List<Position> positions = new ArrayList<>();
			for (int pos = 0; pos < jsonData.length; pos++) {
				final Position p = new Position();
//				p.metadataFile = "Position #" + (pos + 1); // FIXME what to put here?
				positions.add(p);
				parsePosition(jsonData[pos], source, pos);
			}

			translatorService.translate(source, dest, true);
		}

		// -- Parser API methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			final List<Position> positions = new ArrayList<>();
			meta.setPositions(positions);

			log().info("Reading metadata file");

			// find metadata.txt

			final BrowsableLocation file = asBrowsableLocation(stream);
			BrowsableLocation parentFile = file.parent();
			final BrowsableLocation metadataFile = file.sibling(METADATA);

			if (metadataFile == null || parentFile == null) {
				throw new IOException(
					"MicromanagerFormat: No companion metadata file found!");
			}

			// look for other positions

			if (parentFile.getName().contains("Pos_")) {
				parentFile = parentFile.parent();
				final Set<BrowsableLocation> dirs = parentFile.children();

				for (final BrowsableLocation dir : dirs) {
					if (dir.getName().contains("Pos_")) {
						final Position pos = new Position();
						pos.metadataFile = dir.child(METADATA);
						positions.add(pos);
					}
				}
			}
			else {
				final Position pos = new Position();
				pos.metadataFile = metadataFile;
				positions.add(pos);
			}

			final int imageCount = positions.size();
			meta.createImageMetadata(imageCount);

			for (int i = 0; i < imageCount; i++) {
				parsePosition(meta, i);
			}
		}

		@Override
		public Location[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(getSource(), true, 1);
			final List<Location> files = new ArrayList<>();
			for (final Position pos : getMetadata().getPositions()) {
				files.add(pos.metadataFile);
				if (pos.xmlFile != null) {
					files.add(pos.xmlFile);
				}
				if (!noPixels) {
					for (final Location tiff : pos.tiffs) {
						try {
							if (dataHandleService.exists(tiff)) files.add(tiff);
						}
						catch (final IOException exc) {
							log().error("Could not check if location: " + tiff.getURI()
								.toString() + " encountered exception: " + exc);
						}
					}
				}
			}
			return files.toArray(new Location[files.size()]);
		}

		// -- Groupable API Methods --

		@Override
		public boolean isSingleFile(final Location id) {
			return false;
		}

		@Override
		public int fileGroupOption(final Location id) {
			return FormatTools.MUST_GROUP;
		}

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		// -- Helper methods --

		private void parsePosition(final Metadata meta, final int posIndex)
			throws IOException, FormatException
		{
			final Position pos = meta.getPositions().get(posIndex);

			try (DataHandle<Location> handle = dataHandleService.create(
				pos.metadataFile))
			{
				final long len = handle.length();
				if (len > Integer.MAX_VALUE) {
					throw new FormatException("MetadataFile at: " + pos.metadataFile
						.getURI() + " is too large to be parsed!");
				}
				final String metaData = handle.readString((int) handle.length());
				parsePosition(metaData, meta, posIndex);
				buildTIFFList(meta, posIndex);
			}
		}

		private void buildTIFFList(final Metadata meta, final int posIndex)
			throws FormatException
		{
			try {
				final Position p = meta.getPositions().get(posIndex);
				final ImageMetadata ms = meta.get(posIndex);
				final BrowsableLocation parent = p.metadataFile.parent();

				log().info("Finding image file names");

				// find the name of a TIFF file
				p.tiffs = new ArrayList<>();

				// build list of TIFF files

				buildTIFFList(meta, posIndex, p.baseTiff);

				if (p.tiffs.isEmpty()) {
					final List<String> uniqueZ = new ArrayList<>();
					final List<String> uniqueC = new ArrayList<>();
					final List<String> uniqueT = new ArrayList<>();

					final Set<BrowsableLocation> fSet = parent.children();
					final Location[] files = fSet.toArray(new Location[fSet.size()]);
					Arrays.sort(files);
					for (final Location file : files) {
						final String name = file.getName();
						if (FormatTools.checkSuffix(name, "tif") || FormatTools.checkSuffix(
							name, "tiff"))
						{
							final String[] blocks = name.split("_");
							if (!uniqueT.contains(blocks[1])) uniqueT.add(blocks[1]);
							if (!uniqueC.contains(blocks[2])) uniqueC.add(blocks[2]);
							if (!uniqueZ.contains(blocks[3])) uniqueZ.add(blocks[3]);

							p.tiffs.add(file);
						}
					}

					ms.setAxisLength(Axes.Z, uniqueZ.size());
					ms.setAxisLength(Axes.CHANNEL, uniqueC.size());
					ms.setAxisLength(Axes.TIME, uniqueT.size());

					if (p.tiffs.isEmpty()) {
						throw new FormatException("Could not find TIFF files.");
					}
				}
			}
			catch (final IOException e) {
				throw new FormatException(
					"Encountered error when trying to find TIFF files.", e);
			}
		}

		private void parsePosition(final String jsonData, final Metadata meta,
			final int posIndex) throws IOException, FormatException
		{
			final Position p = meta.getPositions().get(posIndex);
			final ImageMetadata ms = meta.get(posIndex);
			final BrowsableLocation metadataFile = p.metadataFile;

			// now parse the rest of the metadata

			// metadata.txt looks something like this:
			//
			// {
			// "Section Name": {
			// "Key": "Value",
			// "Array key": [
			// first array value, second array value
			// ]
			// }
			//
			// }

			log().info("Populating metadata");

			final List<Double> stamps = new ArrayList<>();
			p.voltage = new ArrayList<>();

			final StringTokenizer st = new StringTokenizer(jsonData, "\n");
			final int[] slice = new int[3];
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				final boolean open = token.contains("[");
				boolean closed = token.contains("]");
				if (open || (!open && !closed && !token.equals("{") && !token
					.startsWith("}")))
				{
					final int quote = token.indexOf("\"") + 1;
					final String key = token.substring(quote, token.indexOf("\"", quote));
					String value = null;

					if (open == closed) {
						value = token.substring(token.indexOf(":") + 1);
					}
					else if (!closed) {
						final StringBuilder valueBuffer = new StringBuilder();
						while (!closed) {
							token = st.nextToken();
							closed = token.contains("]");
							valueBuffer.append(token);
						}
						value = valueBuffer.toString();
						value = value.replaceAll("\n", "");
					}
					if (value == null) continue;

					final int startIndex = value.indexOf("[");
					int endIndex = value.indexOf("]");
					if (endIndex == -1) endIndex = value.length();

					value = value.substring(startIndex + 1, endIndex).trim();
					if (value.length() == 0) {
						continue;
					}
					value = value.substring(0, value.length() - 1);
					value = value.replaceAll("\"", "");
					if (value.endsWith(",")) value = value.substring(0, value.length() -
						1);
					meta.getTable().put(key, value);
					if (key.equals("Channels")) {
						ms.setAxisLength(Axes.CHANNEL, Integer.parseInt(value));
					}
					else if (key.equals("ChNames")) {
						p.channels = value.split(",");
						for (int q = 0; q < p.channels.length; q++) {
							p.channels[q] = p.channels[q].replaceAll("\"", "").trim();
						}
					}
					else if (key.equals("Frames")) {
						ms.setAxisLength(Axes.TIME, Integer.parseInt(value));
					}
					else if (key.equals("Slices")) {
						ms.setAxisLength(Axes.Z, Integer.parseInt(value));
					}
					else if (key.equals("PixelSize_um")) {
						p.pixelSize = new Double(value);
					}
					else if (key.equals("z-step_um")) {
						p.sliceThickness = new Double(value);
					}
					else if (key.equals("Time")) {
						p.time = value;
					}
					else if (key.equals("Comment")) {
						p.comment = value;
					}
					else if (key.equals("FileName")) {
						final Location file = metadataFile.sibling(value);
						p.locationMap.put(new Index(slice), file);
						if (p.baseTiff == null) {
							p.baseTiff = file;
						}
					}
					else if (key.equals("Width")) {
						ms.setAxisLength(Axes.X, Integer.parseInt(value));
					}
					else if (key.equals("Height")) {
						ms.setAxisLength(Axes.Y, Integer.parseInt(value));
					}
					else if (key.equals("IJType")) {
						final int type = Integer.parseInt(value);

						switch (type) {
							case 0:
								ms.setPixelType(FormatTools.UINT8);
								break;
							case 1:
								ms.setPixelType(FormatTools.UINT16);
								break;
							default:
								throw new FormatException("Unknown type: " + type);
						}
					}
				}

				if (token.startsWith("\"FrameKey")) {
					int dash = token.indexOf("-") + 1;
					int nextDash = token.indexOf("-", dash);
					slice[2] = Integer.parseInt(token.substring(dash, nextDash));
					dash = nextDash + 1;
					nextDash = token.indexOf("-", dash);
					slice[1] = Integer.parseInt(token.substring(dash, nextDash));
					dash = nextDash + 1;
					slice[0] = Integer.parseInt(token.substring(dash, token.indexOf("\"",
						dash)));

					token = st.nextToken().trim();
					String key = "", value = "";
					boolean valueArray = false;
					int nestedCount = 0;

					while (!token.startsWith("}") || nestedCount > 0) {

						if (token.trim().endsWith("{")) {
							nestedCount++;
							token = st.nextToken().trim();
							continue;
						}
						else if (token.trim().startsWith("}")) {
							nestedCount--;
							token = st.nextToken().trim();
							continue;
						}

						if (valueArray) {
							if (token.trim().equals("],")) {
								valueArray = false;
							}
							else {
								value += token.trim().replaceAll("\"", "");
								token = st.nextToken().trim();
								continue;
							}
						}
						else {
							final int colon = token.indexOf(":");
							key = token.substring(1, colon).trim();
							value = token.substring(colon + 1, token.length() - 1).trim();

							key = key.replaceAll("\"", "");
							value = value.replaceAll("\"", "");

							if (token.trim().endsWith("[")) {
								valueArray = true;
								token = st.nextToken().trim();
								continue;
							}
						}

						meta.getTable().put(key, value);

						if (key.equals("Exposure-ms")) {
							final double t = Double.parseDouble(value);
							p.exposureTime = new Double(t / 1000);
						}
						else if (key.equals("ElapsedTime-ms")) {
							final double t = Double.parseDouble(value);
							stamps.add(new Double(t / 1000));
						}
						else if (key.equals("Core-Camera")) p.cameraRef = value;
						else if (key.equals(p.cameraRef + "-Binning")) {
							if (value.contains("x")) p.binning = value;
							else p.binning = value + "x" + value;
						}
						else if (key.equals(p.cameraRef + "-CameraID")) p.detectorID =
							value;
						else if (key.equals(p.cameraRef + "-CameraName")) {
							p.detectorModel = value;
						}
						else if (key.equals(p.cameraRef + "-Gain")) {
							p.gain = (int) Double.parseDouble(value);
						}
						else if (key.equals(p.cameraRef + "-Name")) {
							p.detectorManufacturer = value;
						}
						else if (key.equals(p.cameraRef + "-Temperature")) {
							p.temperature = Double.parseDouble(value);
						}
						else if (key.equals(p.cameraRef + "-CCDMode")) {
							p.cameraMode = value;
						}
						else if (key.startsWith("DAC-") && key.endsWith("-Volts")) {
							p.voltage.add(new Double(value));
						}
						else if (key.equals("FileName")) {
							final Location file = metadataFile.sibling(value);
							p.locationMap.put(new Index(slice), file);
							if (p.baseTiff == null) {
								p.baseTiff = file;
							}
						}

						token = st.nextToken().trim();
					}
				}
			}

			p.timestamps = stamps.toArray(new Double[stamps.size()]);
			Arrays.sort(p.timestamps);

			// look for the optional companion XML file

			p.xmlFile = p.metadataFile.sibling(XML);
			if (dataHandleService.exists(p.xmlFile)) {
				parseXMLFile(meta, posIndex);
			}
		}

		/**
		 * Populate the list of TIFF files using the given file name as a pattern.
		 *
		 * @throws IOException
		 */
		private void buildTIFFList(final Metadata meta, final int posIndex,
			final Location baseTiff) throws IOException
		{
			log().info("Building list of TIFFs");
			final Position p = meta.getPositions().get(posIndex);

			final String[] blocks = baseTiff.getName().split("_");
			final StringBuilder filename = new StringBuilder();
			for (int t = 0; t < meta.get(posIndex).getAxisLength(Axes.TIME); t++) {
				for (int c = 0; c < meta.get(posIndex).getAxisLength(
					Axes.CHANNEL); c++)
				{
					for (int z = 0; z < meta.get(posIndex).getAxisLength(Axes.Z); z++) {
						filename.append(blocks[0]);
						filename.append("_");

						int zeros = blocks[1].length() - String.valueOf(t).length();
						for (int q = 0; q < zeros; q++) {
							filename.append("0");
						}
						filename.append(t);
						filename.append("_");

						String channel = p.channels[c];
						if (channel.contains("-")) {
							channel = channel.substring(0, channel.indexOf("-"));
						}
						filename.append(channel);
						filename.append("_");

						zeros = blocks[3].length() - String.valueOf(z).length() - 4;
						for (int q = 0; q < zeros; q++) {
							filename.append("0");
						}
						filename.append(z);
						filename.append(".tif");

						p.tiffs.add(p.metadataFile.sibling(filename.toString()));
						filename.delete(0, filename.length());
					}
				}
			}
		}

		/**
		 * Parse metadata values from the Acqusition.xml file.
		 */
		private void parseXMLFile(final Metadata meta, final int imageIndex)
			throws IOException, FormatException
		{
			final Position p = meta.getPositions().get(imageIndex);

			try (DataHandle<Location> handle = dataHandleService.create(p.xmlFile)) {
				final long len = handle.length();
				if (len > Integer.MAX_VALUE) {
					throw new FormatException("MetadataFile at: " + p.xmlFile.getURI() +
						" is too large to be parsed!");
				}
				String xmlData = handle.readString((int) handle.length());
				xmlData = xmlService.sanitizeXML(xmlData);

				final DefaultHandler handler = new MicromanagerHandler();
				xmlService.parseXML(xmlData, handler);
			}
		}

		// -- Helper classes --

		/** SAX handler for parsing Acqusition.xml. */
		private class MicromanagerHandler extends BaseHandler {

			public MicromanagerHandler() {
				super(log());
			}

			@Override
			public void startElement(final String uri, final String localName,
				final String qName, final Attributes attributes)
			{
				if (qName.equals("entry")) {
					final String key = attributes.getValue("key");
					final String value = attributes.getValue("value");

					getMetadata().getTable().put(key, value);
				}
			}
		}

	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Fields --

		@Parameter
		private FormatService formatService;

		@Parameter
		private DataHandleService dataHandleService;

		/** Helper reader for TIFF files. */
		private MinimalTIFFFormat.Reader<?> tiffReader;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.LM_DOMAIN };
		}

		// -- Reader API methods --

		@Override
		public void setMetadata(final Metadata meta) throws IOException {
			tiffReader = null;
			super.setMetadata(meta);
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			if (tiffReader == null) {
				tiffReader = (MinimalTIFFFormat.Reader<?>) formatService
					.getFormatFromClass(MinimalTIFFFormat.class).createReader();
			}

			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);
			
			final Location file =
					meta.getPositions().get(imageIndex).getLocation(meta, imageIndex,
						planeIndex);
			
			if (file != null && dataHandleService.supports(file) &&
					dataHandleService.exists(file)) {
				tiffReader.setSource(file, config);
				return tiffReader.openPlane(imageIndex, 0, plane, bounds);
			}
			log().warn("File for image #" + imageIndex + " (" + file +
					") is missing or cannot be opened.");
			return plane;
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (tiffReader != null) tiffReader.close(fileOnly);
		}

		@Override
		public long getOptimalTileWidth(final int imageIndex) {
			if (tiffReader == null || tiffReader.getCurrentLocation() == null) {
				setupReader(imageIndex);
			}
			return tiffReader.getOptimalTileWidth(imageIndex);
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			if (tiffReader == null || tiffReader.getCurrentLocation() == null) {
				setupReader(imageIndex);
			}
			return tiffReader.getOptimalTileHeight(imageIndex);
		}

		// -- Groupable API Methods --

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		// -- Helper methods --

		private boolean setupReader(final int imageIndex) {
			try {
				final Location file = getMetadata().getPositions().get(imageIndex)
					.getLocation(getMetadata(), imageIndex, 0);

				if (file != null && dataHandleService.supports(file) &&
					dataHandleService.exists(file))
				{
					if (tiffReader == null) {
						tiffReader = (MinimalTIFFFormat.Reader<?>) formatService
							.getFormatFromClass(MinimalTIFFFormat.class).createReader();
					}
					tiffReader.setSource(file);
					return true;
				}
				log().warn("File for image #" + imageIndex + " (" + file +
					") is missing or cannot be opened.");
			}
			catch (final Exception e) {
				log().debug("", e);
			}
			return false;
		}

	}

	/**
	 * Necessary dummy translator, so that a Micromanager-OMEXML translator can be
	 * used
	 */
	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class MicromanagerTranslator extends DefaultTranslator {

		@Override
		public Class<? extends io.scif.Metadata> source() {
			return io.scif.Metadata.class;
		}

		@Override
		public Class<? extends io.scif.Metadata> dest() {
			return Metadata.class;
		}
	}

	// -- Helper classes --

	public static class Position {

		public Location baseTiff;

		public List<Location> tiffs;

		public Map<Index, Location> locationMap = new HashMap<>();

		public BrowsableLocation metadataFile;

		public BrowsableLocation xmlFile;

		public String[] channels;

		public String comment, time;

		public Double exposureTime, sliceThickness, pixelSize;

		public Double[] timestamps;

		public int gain;

		public String binning, detectorID, detectorModel, detectorManufacturer;

		public double temperature;

		public List<Double> voltage;

		public String cameraRef;

		public String cameraMode;

		public Location getLocation(final Metadata meta, final int imageIndex,
			final long planeIndex)
		{
			final long[] zct = FormatTools.rasterToPosition(imageIndex, planeIndex,
				meta, Index.expectedAxes);

			// Look for file associated with computed zct position
			for (final Index key : locationMap.keySet()) {
				if (key.z == zct[0] && key.c == zct[1] && key.t == zct[2]) {
					final Location file = locationMap.get(key);

					if (tiffs != null) {
						for (final Location tiff : tiffs) {
							if (tiff.getName().equals(file.getName())) {
								return tiff;
							}
						}
					}
				}
			}
			return locationMap.size() == 0 ? tiffs.get((int) planeIndex) : null;
		}
	}

	private static class Index {

		public int z;

		public int c;

		public int t;

		public static final List<CalibratedAxis> expectedAxes = Arrays.asList(
			new CalibratedAxis[] { new DefaultLinearAxis(Axes.Z),
				new DefaultLinearAxis(Axes.CHANNEL), new DefaultLinearAxis(
					Axes.TIME) });

		public Index(final int[] zct) {
			z = zct[0];
			c = zct[1];
			t = zct[2];
		}
	}
}
