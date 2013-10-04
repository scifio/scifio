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
import io.scif.BufferedImagePlane;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class MNGFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Multiple Network Graphics";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "mng" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		private MNGDatasetInfo datasetInfo;

		private boolean isJNG = false;

		// -- MNGMetadata getters and setters --

		public MNGDatasetInfo getDatasetInfo() {
			return datasetInfo;
		}

		public void setDatasetInfo(final MNGDatasetInfo datasetInfo) {
			this.datasetInfo = datasetInfo;
		}

		public boolean isJNG() {
			return isJNG;
		}

		public void setJNG(final boolean isJNG) {
			this.isJNG = isJNG;
		}

		// -- Metadata API methods --

		@Override
		public void populateImageMetadata() {
			final String[] keys = getDatasetInfo().keys;

			final int imageCount = keys.length;
			createImageMetadata(imageCount);

			for (int i = 0; i < getImageCount(); i++) {
				final String[] tokens = keys[i].split("-");
				setAxisLength(i, Axes.X, Integer.parseInt(tokens[0]));
				setAxisLength(i, Axes.Y, Integer.parseInt(tokens[1]));
				setAxisLength(i, Axes.CHANNEL, Integer.parseInt(tokens[2]));
				setPlanarAxisCount(i, getAxisLength(i, Axes.CHANNEL) > 1 ? 3 : 2);
				setPixelType(i, Integer.parseInt(tokens[3]));
				setMetadataComplete(i, true);
				setIndexed(i, false);
				setLittleEndian(i, false);
				setFalseColor(i, false);

				setAxisLength(i, Axes.TIME, getDatasetInfo().imageInfo.get(i).offsets
					.size());
			}
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				datasetInfo = null;
				isJNG = false;
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		public static final long MNG_MAGIC_BYTES = 0x8a4d4e470d0a1a0aL;

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 8;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			return stream.readLong() == MNG_MAGIC_BYTES;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- AbstractParser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			in.order(false);

			log().info("Verifying MNG format");

			final MNGDatasetInfo datasetInfo = new MNGDatasetInfo();
			datasetInfo.imageInfo.add(new MNGImageInfo());

			in.skipBytes(12);

			if (!"MHDR".equals(in.readString(4))) {
				throw new FormatException("Invalid MNG file.");
			}

			log().info("Reading dimensions");

			in.skipBytes(32);

			final Vector<Long> stack = new Vector<Long>();
			int maxIterations = 0;
			int currentIteration = 0;

			log().info("Finding image offsets");

			// read sequence of [len, code, value] tags

			while (in.getFilePointer() < in.length()) {
				final int len = in.readInt();
				final String code = in.readString(4);

				final long fp = in.getFilePointer();

				if (code.equals("IHDR")) {
					datasetInfo.imageInfo.get(0).offsets.add(fp - 8);
				}
				else if (code.equals("JDAT")) {
					meta.setJNG(true);
					datasetInfo.imageInfo.get(0).offsets.add(fp);
				}
				else if (code.equals("IEND")) {
					datasetInfo.imageInfo.get(0).lengths.add(fp + len + 4);
				}
				else if (code.equals("LOOP")) {
					stack.add(fp + len + 4);
					in.skipBytes(1);
					maxIterations = in.readInt();
				}
				else if (code.equals("ENDL")) {
					final long seek = stack.get(stack.size() - 1).longValue();
					if (currentIteration < maxIterations) {
						in.seek(seek);
						currentIteration++;
					}
					else {
						stack.remove(stack.size() - 1);
						maxIterations = 0;
						currentIteration = 0;
					}
				}

				in.seek(fp + len + 4);
			}

			log().info("Populating metadata");

			// easiest way to get image dimensions is by opening the first plane

			final Hashtable<String, Vector<Long>> imageOffsets =
				new Hashtable<String, Vector<Long>>();
			final Hashtable<String, Vector<Long>> imageLengths =
				new Hashtable<String, Vector<Long>>();

			final MNGImageInfo info = datasetInfo.imageInfo.get(0);
			addGlobalMeta("Number of frames", info.offsets.size());
			for (int i = 0; i < info.offsets.size(); i++) {
				final long offset = info.offsets.get(i);
				in.seek(offset);
				final long end = info.lengths.get(i);
				if (end < offset) continue;
				final BufferedImage img = readImage(meta, end);
				final String data =
					img.getWidth() + "-" + img.getHeight() + "-" +
						img.getRaster().getNumBands() + "-" +
						AWTImageTools.getPixelType(img);
				Vector<Long> v = new Vector<Long>();
				if (imageOffsets.containsKey(data)) {
					v = imageOffsets.get(data);
				}
				v.add(new Long(offset));
				imageOffsets.put(data, v);

				v = new Vector<Long>();
				if (imageLengths.containsKey(data)) {
					v = imageLengths.get(data);
				}
				v.add(new Long(end));
				imageLengths.put(data, v);
			}

			final String[] keys = imageOffsets.keySet().toArray(new String[0]);

			if (keys.length == 0) {
				throw new FormatException("Pixel data not found.");
			}

			datasetInfo.imageInfo.clear();
			final int imageCount = keys.length;

			for (int i = 0; i < imageCount; i++) {
				final MNGImageInfo inf = new MNGImageInfo();
				inf.offsets = imageOffsets.get(keys[i]);
				inf.lengths = imageLengths.get(keys[i]);
				datasetInfo.imageInfo.add(inf);
			}
			datasetInfo.keys = keys;
			meta.setDatasetInfo(datasetInfo);
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends BufferedImageReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public BufferedImagePlane openPlane(final int imageIndex,
			final int planeIndex, final BufferedImagePlane plane,
			final long[] planeMin, final long[] planeMax) throws FormatException,
			IOException
		{
			final MNGImageInfo info =
				getMetadata().getDatasetInfo().imageInfo.get(imageIndex);
			final long offset = info.offsets.get(planeIndex);
			getStream().seek(offset);
			final long end = info.lengths.get(planeIndex);
			BufferedImage img = readImage(getMetadata(), end);

			// reconstruct the image to use an appropriate raster
			// ImageIO often returns images that cannot be scaled because a
			// BytePackedRaster is used
			img =
				AWTImageTools.getSubimage(img,
					getMetadata().isLittleEndian(imageIndex), planeMin, planeMax);

			plane.setData(img);
			return plane;
		}
	}

	// -- Helper Methods --

	private static BufferedImage readImage(final Metadata meta, final long end)
		throws IOException
	{
		final int headerSize = meta.isJNG() ? 0 : 8;
		final byte[] b =
			new byte[(int) (end - meta.getSource().getFilePointer() + headerSize)];
		meta.getSource().read(b, headerSize, b.length - headerSize);
		if (!meta.isJNG()) {
			b[0] = (byte) 0x89;
			b[1] = 0x50;
			b[2] = 0x4e;
			b[3] = 0x47;
			b[4] = 0x0d;
			b[5] = 0x0a;
			b[6] = 0x1a;
			b[7] = 0x0a;
		}
		return ImageIO.read(new ByteArrayInputStream(b));
	}

	// -- Helper classes --

	private static class MNGDatasetInfo {

		public Vector<MNGImageInfo> imageInfo = new Vector<MNGImageInfo>();
		public String[] keys;
	}

	private static class MNGImageInfo {

		public Vector<Long> offsets = new Vector<Long>();
		public Vector<Long> lengths = new Vector<Long>();
	}
}
