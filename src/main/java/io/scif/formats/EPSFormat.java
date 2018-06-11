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
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.DefaultTranslator;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.TiffParser;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Reader is the file format reader for Encapsulated PostScript (EPS) files.
 * Some regular PostScript files are also supported.
 *
 * @author Melissa Linkert
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Encapsulated PostScript")
public class EPSFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "eps", "epsi", "ps" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		/** Starting line of pixel data. */
		private int start;

		/** Flag indicating binary data. */
		private boolean binary;

		private boolean isTiff;

		private IFDList ifds;

		// -- Constructor --

		public Metadata() {
			super();
			add(new DefaultImageMetadata());
			get(0).setLittleEndian(true);
		}

		// -- Field accessors and setters

		public IFDList getIfds() {
			return ifds;
		}

		public void setIfds(final IFDList ifds) {
			this.ifds = ifds;
		}

		public int getStart() {
			return start;
		}

		public void setStart(final int start) {
			this.start = start;
		}

		public boolean isBinary() {
			return binary;
		}

		public void setBinary(final boolean binary) {
			this.binary = binary;
		}

		public boolean isTiff() {
			return isTiff;
		}

		public void setTiff(final boolean isTiff) {
			this.isTiff = isTiff;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			if (get(0).getAxisLength(Axes.CHANNEL) == 0) get(0).setAxisLength(
				Axes.CHANNEL, 1);

			if (get(0).getPixelType() == 0) get(0).setPixelType(FormatTools.UINT8);

			if (get(0).getAxisLength(Axes.CHANNEL) != 3) {
				get(0).setPlanarAxisCount(2);
				get(0).setAxisTypes(Axes.X, Axes.Y, Axes.CHANNEL);
			}
			else {
				get(0).setPlanarAxisCount(3);
				get(0).setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
			}

		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				isTiff = false;
				ifds = null;
				start = 0;
				binary = false;
			}
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			meta.createImageMetadata(1);

			final ImageMetadata m = meta.get(0);
			final MetaTable globalTable = meta.getTable();

			log().info("Verifying EPS format");

			String line = getSource().readLine();
			if (!line.trim().startsWith("%!PS")) {
				// read the TIFF preview

				meta.setTiff(true);

				getSource().order(true);
				getSource().seek(20);
				final int offset = getSource().readInt();
				final int len = getSource().readInt();

				final byte[] b = new byte[len];
				getSource().seek(offset);
				getSource().read(b);

				final RandomAccessInputStream ifdSource = new RandomAccessInputStream(
					getContext(), b);
				final TiffParser tp = new TiffParser(getContext(), ifdSource);
				meta.setIfds(tp.getIFDs());
				ifdSource.close();

				final IFD firstIFD = meta.getIfds().get(0);

				m.setAxisLength(Axes.CHANNEL, firstIFD.getSamplesPerPixel());
				m.setAxisLength(Axes.X, (int) firstIFD.getImageWidth());
				m.setAxisLength(Axes.Y, (int) firstIFD.getImageLength());

				if (m.getAxisLength(Axes.CHANNEL) == 2) m.setAxisLength(Axes.CHANNEL,
					4);

				m.setLittleEndian(firstIFD.isLittleEndian());
				m.setPixelType(firstIFD.getPixelType());
				m.setMetadataComplete(true);
				m.setIndexed(false);
				m.setFalseColor(false);

				return;
			}

			log().info("Finding image data");

			meta.setBinary(false);

			String image = "image";
			int lineNum = 1;

			line = getSource().readLine().trim();

			m.setAxes(FormatTools.createAxes(Axes.X, Axes.Y, Axes.CHANNEL));

			while (line != null && !line.equals("%%EOF")) {
				if (line.endsWith(image)) {
					if (!line.startsWith(image)) {
						if (line.contains("colorimage")) m.setAxisLength(Axes.CHANNEL, 3);
						final String[] t = line.split(" ");
						try {
							m.setAxisLength(Axes.X, Integer.parseInt(t[0]));
							m.setAxisLength(Axes.Y, Integer.parseInt(t[1]));
						}
						catch (final NumberFormatException exc) {
							log().debug("Could not parse image dimensions", exc);
							m.setAxisLength(Axes.CHANNEL, Integer.parseInt(t[3]));
						}
					}

					meta.setStart(lineNum);
					break;
				}
				else if (line.startsWith("%%")) {
					if (line.startsWith("%%BoundingBox:")) {
						line = line.substring(14).trim();
						final String[] t = line.split(" ");
						try {
							final int originX = Integer.parseInt(t[0].trim());
							final int originY = Integer.parseInt(t[1].trim());
							m.setAxisLength(Axes.X, Integer.parseInt(t[2].trim()) - originY);
							m.setAxisLength(Axes.Y, Integer.parseInt(t[3].trim()) - originY);

							globalTable.put("X-coordinate of origin", originX);
							globalTable.put("Y-coordinate of origin", originY);
						}
						catch (final NumberFormatException e) {
							throw new FormatException(
								"Files without image data are not supported.");
						}
					}
					else if (line.startsWith("%%BeginBinary")) {
						meta.setBinary(true);
					}
					else {
						// parse key/value pairs

						final int ndx = line.indexOf(":");
						if (ndx != -1) {
							final String key = line.substring(0, ndx);
							final String value = line.substring(ndx + 1);
							globalTable.put(key, value);
						}
					}
				}
				else if (line.startsWith("%ImageData:")) {
					line = line.substring(11);
					final String[] t = line.split(" ");

					m.setAxisLength(Axes.X, Integer.parseInt(t[0]));
					m.setAxisLength(Axes.Y, Integer.parseInt(t[1]));
					m.setAxisLength(Axes.CHANNEL, Integer.parseInt(t[3]));
					for (int i = 4; i < t.length; i++) {
						image = t[i].trim();
						if (image.length() > 1) {
							image = image.substring(1, image.length() - 1);
						}
					}
				}
				lineNum++;
				line = getSource().readLine().trim();
			}

		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final byte[] buf = plane.getData();
			final Metadata meta = getMetadata();
			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);

			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);

			if (meta.isTiff()) {
				final long[] offsets = meta.getIfds().get(0).getStripOffsets();
				getStream().seek(offsets[0]);

				final int[] map = meta.getIfds().get(0).getIFDIntArray(IFD.COLOR_MAP);
				if (map == null) {
					readPlane(getStream(), imageIndex, bounds, plane);
					return plane;
				}

				final byte[] b = new byte[w * h];
				getStream().skipBytes(2 * y * (int) meta.get(imageIndex).getAxisLength(
					Axes.X));
				for (int row = 0; row < h; row++) {
					getStream().skipBytes(x * 2);
					for (int col = 0; col < w; col++) {
						b[row * w + col] = (byte) (getStream().readShort() & 0xff);
					}
					getStream().skipBytes(2 * (int) (meta.get(imageIndex).getAxisLength(
						Axes.X) - w - x));
				}

				for (int i = 0; i < b.length; i++) {
					final int ndx = b[i] & 0xff;
					for (int j = 0; j < (int) meta.get(imageIndex).getAxisLength(
						Axes.CHANNEL); j++)
					{
						if (j < 3) {
							buf[i * (int) meta.get(imageIndex).getAxisLength(Axes.CHANNEL) +
								j] = (byte) map[ndx + j * 256];
						}
						else {
							final boolean zero = map[ndx] == 0 && map[ndx + 256] == 0 &&
								map[ndx + 512] == 0;
							buf[i * (int) meta.get(imageIndex).getAxisLength(Axes.CHANNEL) +
								j] = zero ? (byte) 0 : (byte) 255;
						}
					}
				}

				return plane;
			}

			if (meta.getStart() == 0) {
				throw new FormatException("Vector data not supported.");
			}

			getStream().seek(0);
			for (int line = 0; line <= meta.getStart(); line++) {
				getStream().readLine();
			}

			final int bytes = FormatTools.getBytesPerPixel(meta.get(imageIndex)
				.getPixelType());
			if (meta.isBinary()) {
				// pixels are stored as raw bytes
				readPlane(getStream(), imageIndex, bounds, plane);
			}
			else {
				// pixels are stored as a 2 character hexadecimal value
				String pix = getStream().readString((int) (getStream().length() -
					getStream().getFilePointer()));
				pix = pix.replaceAll("\n", "");
				pix = pix.replaceAll("\r", "");

				int ndx = (int) (meta.get(imageIndex).getAxisLength(Axes.CHANNEL) * y *
					bytes * meta.get(imageIndex).getAxisLength(Axes.X));
				int destNdx = 0;

				for (int row = 0; row < h; row++) {
					ndx += x * meta.get(imageIndex).getAxisLength(Axes.CHANNEL) * bytes;
					for (int col = 0; col < w * meta.get(imageIndex).getAxisLength(
						Axes.CHANNEL) * bytes; col++)
					{
						buf[destNdx++] = (byte) Integer.parseInt(pix.substring(2 * ndx, 2 *
							(ndx + 1)), 16);
						ndx++;
					}
					ndx += meta.get(imageIndex).getAxisLength(Axes.CHANNEL) * bytes *
						(meta.get(imageIndex).getAxisLength(Axes.X) - w - x);
				}
			}
			return plane;
		}

		@Override
		public long getOptimalTileWidth(final int imageIndex) {
			try {
				if (getMetadata().isTiff) {
					return (int) getMetadata().getIfds().get(0).getTileWidth();
				}
			}
			catch (final FormatException e) {
				log().debug("Could not retrieve tile width", e);
			}
			return super.getOptimalTileWidth(imageIndex);
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			try {
				if (getMetadata().isTiff()) {
					return (int) getMetadata().getIfds().get(0).getTileLength();
				}
			}
			catch (final FormatException e) {
				log().debug("Could not retrieve tile height", e);
			}
			return super.getOptimalTileHeight(imageIndex);
		}
	}

	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constants --

		private static final String DUMMY_PIXEL = "00";

		// -- Fields --

		private long planeOffset = 0;

		// -- AbstractWriter Methods --

		@Override
		protected String[] makeCompressionTypes() {
			return new String[0];
		}

		@Override
		protected void initialize(final int imageIndex, final long planeIndex,
			final Interval bounds) throws IOException, FormatException
		{
			if (!isInitialized(imageIndex, (int) planeIndex)) {

				writeHeader(imageIndex);

				if (!SCIFIOMetadataTools.wholePlane(imageIndex, getMetadata(),
					bounds))
				{
					final int xAxis = getMetadata().get(imageIndex).getAxisIndex(Axes.X);
					final int yAxis = getMetadata().get(imageIndex).getAxisIndex(Axes.Y);
					final int w = (int) bounds.dimension(xAxis);
					final int h = (int) bounds.dimension(yAxis);
					final int nChannels = (int) getMetadata().get(imageIndex)
						.getAxisLength(Axes.CHANNEL);
					// write a dummy plane that will be overwritten in sections
					final int planeSize = w * h * nChannels;
					for (int i = 0; i < planeSize; i++) {
						getStream().writeBytes(DUMMY_PIXEL);
					}
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

			final byte[] buf = plane.getBytes();
			final boolean interleaved = plane.getImageMetadata()
				.getInterleavedAxisCount() > 0;
			checkParams(imageIndex, planeIndex, buf, bounds);
			final int xAxis = getMetadata().get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = getMetadata().get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.max(xAxis), h = (int) bounds.max(yAxis);
			final int sizeX = (int) getMetadata().get(imageIndex).getAxisLength(
				Axes.X);
			final int nChannels = (int) getMetadata().get(imageIndex).getAxisLength(
				Axes.CHANNEL);

			// write pixel data
			// for simplicity, write 80 char lines

			final int planeSize = (int) (bounds.max(xAxis) * bounds.max(yAxis));

			final StringBuilder buffer = new StringBuilder();

			final int offset = y * sizeX * nChannels * 2;
			getStream().seek(planeOffset + offset);
			for (int row = 0; row < h; row++) {
				getStream().skipBytes(nChannels * x * 2);
				for (int col = 0; col < w * nChannels; col++) {
					final int i = row * w * nChannels + col;
					final int index = interleaved || nChannels == 1 ? i : (i %
						nChannels) * planeSize + (i / nChannels);
					final String s = Integer.toHexString(buf[index]);
					// only want last 2 characters of s
					if (s.length() > 1) buffer.append(s.substring(s.length() - 2));
					else {
						buffer.append("0");
						buffer.append(s);
					}
				}
				getStream().writeBytes(buffer.toString());
				buffer.delete(0, buffer.length());
				getStream().skipBytes(nChannels * (sizeX - w - x) * 2);
			}

			// write footer

			getStream().seek(getStream().length());
			getStream().writeBytes("\nshowpage\n");
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8 };
		}

		// -- Helper methods --

		private void writeHeader(final int imageIndex) throws IOException {
			final int width = (int) getMetadata().get(imageIndex).getAxisLength(
				Axes.X);
			final int height = (int) getMetadata().get(imageIndex).getAxisLength(
				Axes.Y);
			final int nChannels = (int) getMetadata().get(imageIndex).getAxisLength(
				Axes.CHANNEL);

			getStream().writeBytes("%!PS-Adobe-2.0 EPSF-1.2\n");
			getStream().writeBytes("%%Title: " + getMetadata().getDatasetName() +
				"\n");
			getStream().writeBytes("%%Creator: SCIFIO\n");
			getStream().writeBytes("%%Pages: 1\n");
			getStream().writeBytes("%%BoundingBox: 0 0 " + width + " " + height +
				"\n");
			getStream().writeBytes("%%EndComments\n\n");

			getStream().writeBytes("/ld {load def} bind def\n");
			getStream().writeBytes("/s /stroke ld /f /fill ld /m /moveto ld /l " +
				"/lineto ld /c /curveto ld /rgb {255 div 3 1 roll 255 div 3 1 " +
				"roll 255 div 3 1 roll setrgbcolor} def\n");
			getStream().writeBytes("0 0 translate\n");
			getStream().writeBytes(((float) width) + " " + ((float) height) +
				" scale\n");
			getStream().writeBytes("/picstr 40 string def\n");
			getStream().writeBytes(width + " " + height + " 8 [" + width + " 0 0 " +
				(-1 * height) + " 0 " + height +
				"] {currentfile picstr readhexstring pop} ");
			if (nChannels == 1) {
				getStream().writeBytes("image\n");
			}
			else {
				getStream().writeBytes("false 3 colorimage\n");
			}
			planeOffset = getStream().getFilePointer();
		}
	}

	/**
	 * Necessary dummy translator, so that an EPS-OMEXML translator can be used.
	 */
	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class EPSTranslator extends DefaultTranslator {

		// -- Translator API Methods --

		@Override
		public Class<? extends io.scif.Metadata> source() {
			return io.scif.Metadata.class;
		}

		@Override
		public Class<? extends io.scif.Metadata> dest() {
			return Metadata.class;
		}

	}
}
