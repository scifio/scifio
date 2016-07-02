/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.FormatService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;
import io.scif.xml.BaseHandler;
import io.scif.xml.XMLService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import net.imagej.axis.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.DigestUtils;
import org.scijava.util.FileUtils;
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

		private Vector<Position> positions;

		// -- MicromanagerMetadata getters and setters --

		public Vector<Position> getPositions() {
			return positions;
		}

		public void setPositions(final Vector<Position> positions) {
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

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final String name, final SCIFIOConfig config) {
			// not allowed to touch the file system
			if (!config.checkerIsOpen()) return false;
			if (name.equals(METADATA) || name.endsWith(File.separator + METADATA) ||
				name.equals(XML) || name.endsWith(File.separator + XML))
			{
				final int blockSize = 1048576;
				try {
					final RandomAccessInputStream stream =
						new RandomAccessInputStream(getContext(), name);
					final long length = stream.length();
					final String data =
						stream.readString((int) Math.min(blockSize, length));
					stream.close();
					return length > 0 &&
						(data.contains("Micro-Manager") || data.contains("micromanager"));
				}
				catch (final IOException e) {
					return false;
				}
			}
			try {
				final Location parent =
					new Location(getContext(), name).getAbsoluteFile().getParentFile();
				final Location metaFile = new Location(getContext(), parent, METADATA);
				final RandomAccessInputStream s =
					new RandomAccessInputStream(getContext(), name);
				boolean validTIFF = isFormat(s);
				final io.scif.Checker checker;
				try {
					checker = formatService.getFormatFromClass(MinimalTIFFFormat.class)
						.createChecker();
					validTIFF = checker.isFormat(s);
				}
				catch (final FormatException e) {
					log().error("Failed to create a MinimalTIFFChecker", e);
					validTIFF = false;
				}
				s.close();
				return validTIFF && metaFile.exists() && isFormat(metaFile
					.getAbsolutePath(), config);
			}
			catch (final NullPointerException e) {}
			catch (final IOException e) {}
			return false;
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			return false;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

		// -- Fields --

		@Parameter
		private TranslatorService translatorService;

		@Parameter
		private XMLService xmlService;

		// -- MicromanagerParser API methods --

		public void populateMetadata(final String[] jsonData,
			final Metadata source, final io.scif.Metadata dest)
			throws FormatException, IOException
		{
			source.createImageMetadata(jsonData.length);
			final Vector<Position> positions = new Vector<>();
			for (int pos = 0; pos < jsonData.length; pos++) {
				final Position p = new Position();
				p.metadataFile = "Position #" + (pos + 1);
				positions.add(p);
				parsePosition(jsonData[pos], source, pos);
			}

			translatorService.translate(source, dest, true);
		}

		// -- Parser API methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			final Vector<Position> positions = new Vector<>();
			meta.setPositions(positions);

			log().info("Reading metadata file");

			// find metadata.txt

			final Location file =
				new Location(getContext(), stream.getFileName()).getAbsoluteFile();
			Location parentFile = file.getParentFile();
			String metadataFile = METADATA;
			if (!file.exists()) throw new IllegalStateException(
					"MicromanagerFormat: No companion metadata file");

			metadataFile =
					new Location(getContext(), parentFile, METADATA).getAbsolutePath();

			// look for other positions

			if (parentFile.getName().contains("Pos_")) {
				parentFile = parentFile.getParentFile();
				final String[] dirs = parentFile.list(true);
				Arrays.sort(dirs);
				for (final String dir : dirs) {
					if (dir.contains("Pos_")) {
						final Position pos = new Position();
						final Location posDir =
								new Location(getContext(), parentFile, dir);
						pos.metadataFile =
								new Location(getContext(), posDir, METADATA).getAbsolutePath();
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
		public String[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(getSource(), true, 1);
			final Vector<String> files = new Vector<>();
			for (final Position pos : getMetadata().getPositions()) {
				files.add(pos.metadataFile);
				if (pos.xmlFile != null) {
					files.add(pos.xmlFile);
				}
				if (!noPixels) {
					for (final String tiff : pos.tiffs) {
						if (new Location(getContext(), tiff).exists()) {
							files.add(tiff);
						}
					}
				}
			}
			return files.toArray(new String[files.size()]);
		}

		// -- Groupable API Methods --

		@Override
		public boolean isSingleFile(final String id) {
			return false;
		}

		@Override
		public int fileGroupOption(final String id) {
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
			final Position p = meta.getPositions().get(posIndex);
			final byte[] bytes = FileUtils.readFile(new File(p.metadataFile));
			final String s = DigestUtils.string(bytes);
			parsePosition(s, meta, posIndex);

			buildTIFFList(meta, posIndex);
		}

		private void buildTIFFList(final Metadata meta, final int posIndex)
			throws FormatException
		{
			final Position p = meta.getPositions().get(posIndex);
			final ImageMetadata ms = meta.get(posIndex);
			final String parent =
				new Location(getContext(), p.metadataFile).getParent();

			log().info("Finding image file names");

			// find the name of a TIFF file
			p.tiffs = new Vector<>();

			// build list of TIFF files

			buildTIFFList(meta, posIndex, parent + File.separator + p.baseTiff);

			if (p.tiffs.size() == 0) {
				final Vector<String> uniqueZ = new Vector<>();
				final Vector<String> uniqueC = new Vector<>();
				final Vector<String> uniqueT = new Vector<>();

				final Location dir =
					new Location(getContext(), p.metadataFile).getAbsoluteFile()
						.getParentFile();
				final String[] files = dir.list(true);
				Arrays.sort(files);
				for (final String f : files) {
					if (FormatTools.checkSuffix(f, "tif") ||
						FormatTools.checkSuffix(f, "tiff"))
					{
						final String[] blocks = f.split("_");
						if (!uniqueT.contains(blocks[1])) uniqueT.add(blocks[1]);
						if (!uniqueC.contains(blocks[2])) uniqueC.add(blocks[2]);
						if (!uniqueZ.contains(blocks[3])) uniqueZ.add(blocks[3]);

						p.tiffs.add(new Location(getContext(), dir, f).getAbsolutePath());
					}
				}

				ms.setAxisLength(Axes.Z, uniqueZ.size());
				ms.setAxisLength(Axes.CHANNEL, uniqueC.size());
				ms.setAxisLength(Axes.TIME, uniqueT.size());

				if (p.tiffs.size() == 0) {
					throw new FormatException("Could not find TIFF files.");
				}
			}
		}

		private void parsePosition(final String jsonData, final Metadata meta,
			final int posIndex) throws IOException, FormatException
		{
			final Position p = meta.getPositions().get(posIndex);
			final ImageMetadata ms = meta.get(posIndex);
			final String parent =
				new Location(getContext(), p.metadataFile).getParent();

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

			final Vector<Double> stamps = new Vector<>();
			p.voltage = new Vector<>();

			final StringTokenizer st = new StringTokenizer(jsonData, "\n");
			final int[] slice = new int[3];
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				final boolean open = token.contains("[");
				boolean closed = token.contains("]");
				if (open ||
					(!open && !closed && !token.equals("{") && !token.startsWith("}")))
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
					if (value.endsWith(",")) value =
						value.substring(0, value.length() - 1);
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
						p.fileNameMap.put(new Index(slice), value);
						if (p.baseTiff == null) {
							p.baseTiff = value;
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
					slice[0] =
						Integer.parseInt(token.substring(dash, token.indexOf("\"", dash)));

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
							p.fileNameMap.put(new Index(slice), value);
							if (p.baseTiff == null) {
								p.baseTiff = value;
							}
						}

						token = st.nextToken().trim();
					}
				}
			}

			p.timestamps = stamps.toArray(new Double[stamps.size()]);
			Arrays.sort(p.timestamps);

			// look for the optional companion XML file

			if (new Location(getContext(), parent, XML).exists()) {
				p.xmlFile = new Location(getContext(), parent, XML).getAbsolutePath();
				parseXMLFile(meta, posIndex);
			}
		}

		/**
		 * Populate the list of TIFF files using the given file name as a pattern.
		 */
		private void buildTIFFList(final Metadata meta, final int posIndex,
			String baseTiff)
		{
			log().info("Building list of TIFFs");
			final Position p = meta.getPositions().get(posIndex);
			String prefix = "";
			if (baseTiff.contains(File.separator)) {
				prefix =
					baseTiff.substring(0, baseTiff.lastIndexOf(File.separator) + 1);
				baseTiff = baseTiff.substring(baseTiff.lastIndexOf(File.separator) + 1);
			}

			final String[] blocks = baseTiff.split("_");
			final StringBuilder filename = new StringBuilder();
			for (int t = 0; t < meta.get(posIndex).getAxisLength(Axes.TIME); t++) {
				for (int c = 0; c < meta.get(posIndex).getAxisLength(Axes.CHANNEL); c++)
				{
					for (int z = 0; z < meta.get(posIndex).getAxisLength(Axes.Z); z++) {
						// file names are of format:
						// img_<T>_<channel name>_<T>.tif
						filename.append(prefix);
						if (!prefix.endsWith(File.separator) &&
							!blocks[0].startsWith(File.separator))
						{
							filename.append(File.separator);
						}
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

						p.tiffs.add(filename.toString());
						filename.delete(0, filename.length());
					}
				}
			}
		}

		/** Parse metadata values from the Acqusition.xml file. */
		private void parseXMLFile(final Metadata meta, final int imageIndex)
			throws IOException
		{
			final Position p = meta.getPositions().get(imageIndex);
			final byte[] bytes = FileUtils.readFile(new File(p.xmlFile));
			String xmlData = DigestUtils.string(bytes);
			xmlData = xmlService.sanitizeXML(xmlData);

			final DefaultHandler handler = new MicromanagerHandler();
			xmlService.parseXML(xmlData, handler);
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
		public ByteArrayPlane openPlane(final int imageIndex,
			final long planeIndex, final ByteArrayPlane plane, final long[] planeMin,
			final long[] planeMax, final SCIFIOConfig config) throws FormatException,
			IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);

			final String file =
				meta.getPositions().get(imageIndex).getFile(meta, imageIndex,
					planeIndex);

			if (file != null && new Location(getContext(), file).exists()) {
				tiffReader.setSource(file, config);
				return tiffReader.openPlane(imageIndex, 0, plane, planeMin, planeMax);
			}
			log().warn(
				"File for image #" + planeIndex + " (" + file + ") is missing.");
			return plane;
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (tiffReader != null) tiffReader.close(fileOnly);
		}

		@Override
		public long getOptimalTileWidth(final int imageIndex) {
			if (tiffReader == null || tiffReader.getCurrentFile() == null) {
				setupReader(imageIndex);
			}
			return tiffReader.getOptimalTileWidth(imageIndex);
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			if (tiffReader == null || tiffReader.getCurrentFile() == null) {
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

		private void setupReader(final int imageIndex) {
			try {
				final String file =
					getMetadata().getPositions().get(imageIndex).getFile(getMetadata(),
						imageIndex, 0);

				if (tiffReader == null) {
					tiffReader =
						(MinimalTIFFFormat.Reader<?>) formatService.getFormatFromClass(
							MinimalTIFFFormat.class).createReader();
				}

				tiffReader.setSource(file);
			}
			catch (final Exception e) {
				log().debug("", e);
			}
		}

	}

	/**
	 * Necessary dummy translator, so that a Micromanager-OMEXML translator can be
	 * used
	 */
	@Plugin(type = Translator.class, priority = Priority.LOW_PRIORITY)
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

		public String baseTiff;

		public Vector<String> tiffs;

		public HashMap<Index, String> fileNameMap = new HashMap<>();

		public String metadataFile;

		public String xmlFile;

		public String[] channels;

		public String comment, time;

		public Double exposureTime, sliceThickness, pixelSize;

		public Double[] timestamps;

		public int gain;

		public String binning, detectorID, detectorModel, detectorManufacturer;

		public double temperature;

		public Vector<Double> voltage;

		public String cameraRef;

		public String cameraMode;

		public String getFile(final Metadata meta, final int imageIndex,
			final long planeIndex)
		{
			final long[] zct =
				FormatTools.rasterToPosition(imageIndex, planeIndex, meta);
			for (final Index key : fileNameMap.keySet()) {
				if (key.z == zct[0] && key.c == zct[1] && key.t == zct[2]) {
					final String file = fileNameMap.get(key);

					if (tiffs != null) {
						for (final String tiff : tiffs) {
							if (tiff.endsWith(File.separator + file)) {
								return tiff;
							}
						}
					}
				}
			}
			return fileNameMap.size() == 0 ? tiffs.get((int) planeIndex) : null;
		}
	}

	private static class Index {

		public int z;

		public int c;

		public int t;

		public Index(final int[] zct) {
			z = zct[0];
			c = zct[1];
			t = zct[2];
		}
	}
}
