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
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * Reader for text files containing tables of data. All image planes are stored
 * in memory as 32-bit floats until the file is closed, so very large text
 * documents will require commensurate available RAM. Text format is flexible,
 * but assumed to be in tabular form with a consistent number of columns, and a
 * labeled header line immediately preceding the data.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Text")
public class TextFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "txt", "csv" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		/**
		 * Because we have no way of indexing into the text file efficiently in
		 * general, we cheat and store the entire file's data in a giant array.
		 */
		private float[][] data;

		/** Current row number. */
		private int row;

		/** Number of tokens per row. */
		private int rowLength;

		/** Column index for X coordinate. */
		private int xIndex = -1;

		/** Column index for Y coordinate. */
		private int yIndex = -1;

		/** List of channel labels. */
		private String[] channels;

		/** Image width. */
		private int sizeX;

		/** Image height. */
		private int sizeY;

		// -- TextMetadata getters and setters --

		public float[][] getData() {
			return data;
		}

		public void setData(final float[][] data) {
			this.data = data;
		}

		public int getRow() {
			return row;
		}

		public void setRow(final int row) {
			this.row = row;
		}

		public int getRowLength() {
			return rowLength;
		}

		public void setRowLength(final int rowLength) {
			this.rowLength = rowLength;
		}

		public int getxIndex() {
			return xIndex;
		}

		public void setxIndex(final int xIndex) {
			this.xIndex = xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public void setyIndex(final int yIndex) {
			this.yIndex = yIndex;
		}

		public String[] getChannels() {
			return channels;
		}

		public void setChannels(final String[] channels) {
			this.channels = channels;
		}

		public int getSizeX() {
			return sizeX;
		}

		public void setSizeX(final int sizeX) {
			this.sizeX = sizeX;
		}

		public int getSizeY() {
			return sizeY;
		}

		public void setSizeY(final int sizeY) {
			this.sizeY = sizeY;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			iMeta.setPlanarAxisCount(2);
			iMeta.setPixelType(FormatTools.FLOAT);
			iMeta.setBitsPerPixel(32);
			iMeta.setOrderCertain(true);
			iMeta.setLittleEndian(TextUtils.LITTLE_ENDIAN);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				data = null;
				rowLength = 0;
				xIndex = yIndex = -1;
				channels = null;
				sizeX = sizeY = 0;
				row = 0;
			}
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Checker API Methods --

		@Override
		public boolean suffixSufficient() {
			return false;
		}

		@Override
		public boolean isFormat(final DataHandle<Location> stream)
			throws IOException
		{
			final int blockLen = 8192;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			final String data = stream.readString(blockLen);
			final List<String> lines = Arrays.asList(data.split("\n"));
			Metadata meta = null;
			try {
				meta = (Metadata) getFormat().createMetadata();
			}
			catch (final FormatException e) {
				log().error("Failed to create TextMetadata", e);
				return false;
			}
			meta.createImageMetadata(1);
			meta.setRow(0);

			final String[] line = TextUtils.getNextLine(lines, meta);
			if (line == null) return false;
			int headerRows = 0;
			try {
				headerRows = TextUtils.parseFileHeader(lines, meta, log());
			}
			catch (final FormatException e) {}
			return headerRows > 0;
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		/** How often to report progress during initialization, in milliseconds. */
		private static final long TIME_OFFSET = 2000;

		// -- Fields --

		// -- AbstractParser API Methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			meta.createImageMetadata(1);
			final ImageMetadata iMeta = meta.get(0);

			// read file into memory
			log().info("Reading file");
			final List<String> lines = readFile(stream);

			// parse file header
			log().info("Parsing file header");
			final int headerRows = TextUtils.parseFileHeader(lines, meta, log());

			// allocate memory for image data
			final int sizeZ = 1, sizeT = 1; // no Z or T for now
			final int sizeC = meta.getChannels().length;
			final int planeCount = sizeZ * sizeC * sizeT;
			final int planeSize = (int) iMeta.getAxisLength(Axes.X) * (int) iMeta
				.getAxisLength(Axes.Y);
			final float[][] data = new float[planeCount][planeSize];
			iMeta.setAxisLength(Axes.Z, sizeZ);
			iMeta.setAxisLength(Axes.CHANNEL, sizeC);
			iMeta.setAxisLength(Axes.TIME, 1);
			meta.setData(data);

			// flag all values as missing by default
			for (int i = 0; i < planeCount; i++)
				Arrays.fill(data[i], Float.NaN);

			// read data into float array
			parseTableData(lines, headerRows, meta);
		}

		// -- Helper Methods --

		/** Reads the tabular data into the data array. */
		private void parseTableData(final List<String> lines, final int linesToSkip,
			final Metadata meta)
		{
			meta.setRow(linesToSkip); // skip header lines

			final double[] rowData = new double[meta.getRowLength()];
			while (true) {
				final String[] tokens = TextUtils.getNextLine(lines, meta);
				if (tokens == null) break; // eof
				if (tokens.length != meta.getRowLength()) {
					log().warn("Ignoring deviant row #" + meta.getRow());
					continue;
				}

				// parse values from row
				final boolean success = TextUtils.getRowData(tokens, rowData);
				if (!success) {
					log().warn("Ignoring non-numeric row #" + meta.getRow());
					continue;
				}

				// copy values into array
				assignValues(rowData, meta);
			}
		}

		/** Assigns values from the given row into the data array. */
		private void assignValues(final double[] rowData, final Metadata meta) {
			final int x = TextUtils.getX(rowData, meta);
			final int y = TextUtils.getY(rowData, meta);
			int c = 0;
			final int index = (int) meta.get(0).getAxisLength(Axes.X) * y + x;
			for (int i = 0; i < meta.getRowLength(); i++) {
				if (i == meta.getxIndex() || i == meta.getyIndex()) continue;
				meta.getData()[c++][index] = (float) rowData[i];
			}
		}

		private List<String> readFile(final DataHandle<Location> handle)
			throws IOException
		{
			final List<String> lines = new ArrayList<>();
			long time = System.currentTimeMillis();

			// read data using RandomAccessInputStream (data may not be a
			// file)
			int no = 0;
			while (true) {
				no++;
				time = checkTime(time, no, handle.offset(), handle.length());
				final String line = handle.readLine();
				if (line == null) break; // eof
				lines.add(line);
			}
			return lines;
		}

		private long checkTime(long time, final int no, final long pos,
			final long len)
		{
			final long t = System.currentTimeMillis();
			if (t - time > TIME_OFFSET) {
				// some time has passed; report progress
				if (len > 0) {
					final int percent = (int) (100 * pos / len);
					log().info("Reading line " + no + " (" + percent + "%)");
				}
				else log().info("Reading line " + no);
				time = t;
			}
			return time;
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.UNKNOWN_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();

			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);
			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);
			// copy floating point data into byte buffer
			final float[] planeFloats = getMetadata().getData()[(int) planeIndex];
			int q = 0;
			for (int j = 0; j < h; j++) {
				final int yy = y + j;
				for (int i = 0; i < w; i++) {
					final int xx = x + i;
					final int index = yy * (int) meta.get(0).getAxisLength(Axes.X) + xx;
					final int bits = Float.floatToIntBits(planeFloats[index]);
					Bytes.unpack(bits, buf, q, 4, TextUtils.LITTLE_ENDIAN);
					q += 4;
				}
			}

			return plane;
		}
	}

	private static class TextUtils {

		// -- Constants --

		private static final String LABEL_X = "x";

		private static final String LABEL_Y = "y";

		private static final boolean LITTLE_ENDIAN = false;

		/**
		 * Parses the file looking for the file header. Determines image extents
		 * (sets sizeX and sizeY). Determines channel names (populates channels
		 * array).
		 *
		 * @return number of rows in the header
		 */
		private static int parseFileHeader(final List<String> lines,
			final Metadata meta, final LogService log) throws FormatException
		{
			String[] lastTokens = null;
			double[] rowData = null;
			while (true) {
				final String[] tokens = getNextLine(lines, meta);
				if (tokens == null) throw new FormatException("No tabular data found");
				if (tokens.length >= 3 && // need at least 3 columns of data
					lastTokens != null && lastTokens.length == tokens.length)
				{
					// consistent number of tokens; might be the header and
					// first data row

					// allocate rowData as needed
					if (rowData == null || rowData.length != tokens.length) {
						rowData = new double[tokens.length];
					}

					// try to parse the first data row
					if (getRowData(tokens, rowData)) {
						log.info("Found header on line " + (meta.getRow() - 1));
						// looks like tabular data; assume previous line is the
						// header
						parseHeaderRow(lastTokens, meta);
						break;
					}
				}
				lastTokens = tokens;
			}
			final int headerRows = meta.getRow() - 1;

			if (meta.getxIndex() < 0) throw new FormatException(
				"No X coordinate column found");
			if (meta.getyIndex() < 0) throw new FormatException(
				"No Y coordinate column found");

			// search remainder of tabular data for X and Y extents
			boolean checkRow = true;
			while (true) {
				if (checkRow) {
					// expand dimensional extents as needed
					final int x = getX(rowData, meta);
					if (x < 0) {
						throw new FormatException("Row #" + meta.getRow() +
							": invalid X: " + x);
					}
					if (meta.get(0).getAxisLength(Axes.X) <= x) meta.get(0).setAxisLength(
						Axes.X, x + 1);
					final int y = getY(rowData, meta);
					if (y < 0) {
						throw new FormatException("Row #" + meta.getRow() +
							": invalid Y: " + x);
					}
					if (meta.get(0).getAxisLength(Axes.Y) <= y) meta.get(0).setAxisLength(
						Axes.Y, y + 1);
				}

				// parse next row
				final String[] tokens = getNextLine(lines, meta);
				if (tokens == null) break; // eof
				checkRow = getRowData(tokens, rowData);
			}

			return headerRows;
		}

		/**
		 * Parses numerical row data from the given tokens.
		 *
		 * @param tokens list of token strings to parse
		 * @param rowData array to fill in with the data; length must match tokens
		 * @return true if the data could be parsed
		 */
		private static boolean getRowData(final String[] tokens,
			final double[] rowData)
		{
			try {
				for (int i = 0; i < tokens.length; i++) {
					rowData[i] = Double.parseDouble(tokens[i]);
				}
				return true;
			}
			catch (final NumberFormatException exc) {
				// not a data row
				return false;
			}
		}

		/** Populates rowLength, xIndex, yIndex, and channels. */
		private static void parseHeaderRow(final String[] tokens,
			final Metadata meta)
		{
			meta.setRowLength(tokens.length);
			final List<String> channelsList = new ArrayList<>();
			for (int i = 0; i < meta.getRowLength(); i++) {
				final String token = tokens[i];
				if (token.equals(LABEL_X)) meta.setxIndex(i);
				else if (token.equals(LABEL_Y)) meta.setyIndex(i);
				else {
					// treat column as a channel
					channelsList.add(token);
				}
			}
			meta.setChannels(channelsList.toArray(new String[0]));
		}

		private static String[] getNextLine(final List<String> lines,
			final Metadata meta)
		{
			while (true) {
				if (meta.getRow() >= lines.size()) return null; // end of list
				String line = lines.get(meta.getRow());
				meta.setRow(meta.getRow() + 1);
				line = line.trim();
				if (line.equals("")) continue; // skip blank lines
				return line.split("[\\s,]");
			}
		}

		private static int getX(final double[] rowData, final Metadata meta) {
			return (int) rowData[meta.getxIndex()];
		}

		private static int getY(final double[] rowData, final Metadata meta) {
			return (int) rowData[meta.getyIndex()];
		}
	}
}
