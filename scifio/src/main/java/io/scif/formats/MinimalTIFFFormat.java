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
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.codec.JPEG2000CodecOptions;
import io.scif.common.DataTools;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.PhotoInterp;
import io.scif.formats.tiff.TiffCompression;
import io.scif.formats.tiff.TiffParser;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * MinimalTiffReader is the superclass for file format readers compatible with
 * or derived from the TIFF 6.0 file format.
 * 
 * @author Melissa Linkert
 */
@Plugin(type = Format.class, priority = MinimalTIFFFormat.PRIORITY)
public class MinimalTIFFFormat extends AbstractFormat {

	public static final double PRIORITY = Priority.VERY_LOW_PRIORITY;

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Minimal TIFF";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "tif", "tiff" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		/** List of IFDs for the current TIFF. */
		protected IFDList ifds;

		/** List of thumbnail IFDs for the current TIFF. */
		protected IFDList thumbnailIFDs;

		/**
		 * List of sub-resolution IFDs for each IFD in the current TIFF with the
		 * same order as <code>ifds</code>.
		 */
		protected List<IFDList> subResolutionIFDs;

		protected TiffParser tiffParser;

		protected boolean equalStrips = false;

		protected boolean use64Bit = false;

		private long lastPlane = 0;

		protected boolean noSubresolutions = false;

		/** Number of JPEG 2000 resolution levels. */
		private Integer resolutionLevels;

		/** Codec options to use when decoding JPEG 2000 data. */
		private JPEG2000CodecOptions j2kCodecOptions;

		// -- Constants --

		public static final String CNAME =
			"io.scif.formats.MinimalTiffFormat$Metadata";

		// -- MinimalTIFFMetadata getters and setters --

		public IFDList getIfds() {
			return ifds;
		}

		public void setIfds(final IFDList ifds) {
			this.ifds = ifds;
		}

		public IFDList getThumbnailIFDs() {
			return thumbnailIFDs;
		}

		public void setThumbnailIFDs(final IFDList thumbnailIFDs) {
			this.thumbnailIFDs = thumbnailIFDs;
		}

		public List<IFDList> getSubResolutionIFDs() {
			return subResolutionIFDs;
		}

		public void setSubResolutionIFDs(final List<IFDList> subResolutionIFDs) {
			this.subResolutionIFDs = subResolutionIFDs;
		}

		public TiffParser getTiffParser() {
			return tiffParser;
		}

		public void setTiffParser(final TiffParser tiffParser) {
			this.tiffParser = tiffParser;
		}

		public boolean isEqualStrips() {
			return equalStrips;
		}

		public void setEqualStrips(final boolean equalStrips) {
			this.equalStrips = equalStrips;
		}

		public boolean isUse64Bit() {
			return use64Bit;
		}

		public void setUse64Bit(final boolean use64Bit) {
			this.use64Bit = use64Bit;
		}

		public long getLastPlane() {
			return lastPlane;
		}

		public void setLastPlane(final long lastPlane) {
			this.lastPlane = lastPlane;
		}

		public boolean isNoSubresolutions() {
			return noSubresolutions;
		}

		public void setNoSubresolutions(final boolean noSubresolutions) {
			this.noSubresolutions = noSubresolutions;
		}

		public Integer getResolutionLevels() {
			return resolutionLevels;
		}

		public void setResolutionLevels(final Integer resolutionLevels) {
			this.resolutionLevels = resolutionLevels;
		}

		public JPEG2000CodecOptions getJ2kCodecOptions() {
			return j2kCodecOptions;
		}

		public void setJ2kCodecOptions(final JPEG2000CodecOptions j2kCodecOptions) {
			this.j2kCodecOptions = j2kCodecOptions;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);
			final ImageMetadata ms0 = get(0);

			final IFD firstIFD = ifds.get(0);

			try {

				final PhotoInterp photo = firstIFD.getPhotometricInterpretation();
				int samples = firstIFD.getSamplesPerPixel();
				if (samples <= 1 && photo == PhotoInterp.RGB) samples = 3;
				int planarAxes = 2;
				ms0.setLittleEndian(firstIFD.isLittleEndian());

				ms0.setAxisLength(Axes.X, (int) firstIFD.getImageWidth());
				ms0.setAxisLength(Axes.Y, (int) firstIFD.getImageLength());

				if (samples > 1) {
					ms0.setAxisLength(Axes.CHANNEL, samples);
					planarAxes = 3;
				}
				ms0.setAxisLength(Axes.TIME, ifds.size());

				ms0.setPlanarAxisCount(planarAxes);
				ms0.setPixelType(firstIFD.getPixelType());
				ms0.setMetadataComplete(true);
				ms0.setIndexed(photo == PhotoInterp.RGB_PALETTE &&
					(getColorTable(0, 0) != null));

				if (ms0.isIndexed()) {
					ms0.setAxisLength(Axes.CHANNEL, 1);
					for (final IFD ifd : ifds) {
						ifd.putIFDValue(IFD.PHOTOMETRIC_INTERPRETATION,
							PhotoInterp.RGB_PALETTE);
					}
				}
				ms0.setBitsPerPixel(firstIFD.getBitsPerSample()[0]);

				// New core metadata now that we know how many sub-resolutions we have.
				if (resolutionLevels != null && subResolutionIFDs.size() > 0) {
					final IFDList ifds = subResolutionIFDs.get(0);

					// FIXME: support sub resolutions in SCIFIO.. or something..
					// int seriesCount = ifds.size() + 1;
					// if (!hasFlattenedResolutions()) {
					// ms0.resolutionCount = seriesCount;
					// }

					if (ifds.size() + 1 < ms0.getAxisLength(Axes.TIME)) {
						ms0.setAxisLength(Axes.TIME, ms0.getAxisLength(Axes.TIME) -
							(ifds.size() + 1));
					}

					for (final IFD ifd : ifds) {
						final ImageMetadata ms = ms0.copy();
						add(ms);
						ms.setAxisLength(Axes.X, (int) ifd.getImageWidth());
						ms.setAxisLength(Axes.Y, (int) ifd.getImageLength());
						ms.setAxisLength(Axes.TIME, ms0.getAxisLength(Axes.TIME));
						ms.setThumbnail(true);
						// TODO subresolutions
						// ms.resolutionCount = 1;
					}

				}
			}
			catch (final FormatException e) {
				log().error("Error populating TIFF image metadata", e);
			}
		}

		@Override
		public long getThumbSizeX(final int imageIndex) {
			if (thumbnailIFDs != null && thumbnailIFDs.size() > 0) {
				try {
					return thumbnailIFDs.get(0).getImageWidth();
				}
				catch (final FormatException e) {
					log().debug("Could not retrieve thumbnail width", e);
				}
			}
			return super.getThumbSizeX(imageIndex);
		}

		@Override
		public long getThumbSizeY(final int imageIndex) {
			if (thumbnailIFDs != null && thumbnailIFDs.size() > 0) {
				try {
					return thumbnailIFDs.get(0).getImageLength();
				}
				catch (final FormatException e) {
					log().debug("Could not retrieve thumbnail height", e);
				}
			}
			return super.getThumbSizeY(imageIndex);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				if (ifds != null) {
					for (final IFD ifd : ifds) {
						try {
							if (ifd.getOnDemandStripOffsets() != null) {
								ifd.getOnDemandStripOffsets().close();
							}
						}
						catch (final FormatException e) {
							log().debug("", e);
						}
					}
				}
				ifds = null;
				thumbnailIFDs = null;
				subResolutionIFDs = new ArrayList<IFDList>();
				lastPlane = 0;
				tiffParser = null;
				resolutionLevels = null;
				j2kCodecOptions = JPEG2000CodecOptions.getDefaultOptions();
			}
		}

		// -- HasColorTable API methods --

		@Override
		public ColorTable getColorTable(final int imageIndex, final long planeIndex)
		{
			if (ifds == null || lastPlane < 0 || lastPlane > ifds.size()) return null;
			IFD lastIFD = ifds.get((int)lastPlane);

			ColorTable table = null;
			try {

				final int[] bits = lastIFD.getBitsPerSample();
				int[] colorMap = lastIFD.getIFDIntArray(IFD.COLOR_MAP);
				if (bits[0] <= 16 && bits[0] > 8) {
					if (colorMap == null || colorMap.length < 65536 * 3) {
						// it's possible that the LUT is only present in the first IFD
						if (lastPlane != 0) {
							lastIFD = ifds.get(0);
							colorMap = lastIFD.getIFDIntArray(IFD.COLOR_MAP);
							if (colorMap == null || colorMap.length < 65536 * 3) return null;
						}
						else return null;
					}

					final short[][] table16 = new short[3][colorMap.length / 3];
					int next = 0;
					for (int i = 0; i < table16.length; i++) {
						for (int j = 0; j < table16[0].length; j++) {
							table16[i][j] = (short) (colorMap[next++] & 0xffff);
						}
					}
					table = new ColorTable16(table16);
				}
				else if (bits[0] <= 8) {
					if (colorMap == null) {
						// it's possible that the LUT is only present in the first IFD
						if (lastPlane != 0) {
							lastIFD = ifds.get(0);
							colorMap = lastIFD.getIFDIntArray(IFD.COLOR_MAP);
							if (colorMap == null) return null;
						}
						else return null;
					}

					final byte[][] table8 = new byte[3][colorMap.length / 3];
					int next = 0;
					for (int j = 0; j < table8.length; j++) {
						for (int i = 0; i < table8[0].length; i++) {
							if (colorMap[next] > 255) {
								table8[j][i] = (byte) ((colorMap[next++] >> 8) & 0xff);
							}
							else {
								table8[j][i] = (byte) (colorMap[next++] & 0xff);
							}
						}
					}

					table = new ColorTable8(table8);
				}
			}
			catch (final FormatException e) {
				log().error("Failed to get IFD int array", e);
			}
			return table;
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constructor --

		public Checker() {
			suffixNecessary = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream) {
			return new TiffParser(getContext(), stream).isValidHeader();
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser<M extends Metadata> extends AbstractParser<M> {

		// -- Parser API Methods --

		@Override
		protected void
			typedParse(final RandomAccessInputStream stream, final M meta)
				throws IOException, FormatException
		{
			final TiffParser tiffParser = new TiffParser(getContext(), stream);
			tiffParser.setDoCaching(false);
			tiffParser.setUse64BitOffsets(meta.isUse64Bit());
			meta.setTiffParser(tiffParser);

			final Boolean littleEndian = tiffParser.checkHeader();
			if (littleEndian == null) {
				throw new FormatException("Invalid TIFF file");
			}
			final boolean little = littleEndian.booleanValue();
			in.order(little);

			log().info("Reading IFDs");

			final IFDList allIFDs = tiffParser.getIFDs();

			if (allIFDs == null || allIFDs.size() == 0) {
				throw new FormatException("No IFDs found");
			}

			final IFDList ifds = new IFDList();
			final IFDList thumbnailIFDs = new IFDList();

			meta.setIfds(ifds);
			meta.setThumbnailIFDs(thumbnailIFDs);

			for (final IFD ifd : allIFDs) {
				final Number subfile = (Number) ifd.getIFDValue(IFD.NEW_SUBFILE_TYPE);
				final int subfileType = subfile == null ? 0 : subfile.intValue();
				if (subfileType != 1 || allIFDs.size() <= 1) {
					ifds.add(ifd);
				}
				else if (subfileType == 1) {
					thumbnailIFDs.add(ifd);
				}
			}

			log().info("Populating metadata");

			tiffParser.setAssumeEqualStrips(meta.isEqualStrips());
			for (final IFD ifd : ifds) {
				tiffParser.fillInIFD(ifd);
				if (ifd.getCompression() == TiffCompression.JPEG_2000 ||
					ifd.getCompression() == TiffCompression.JPEG_2000_LOSSY)
				{
					log().debug("Found IFD with JPEG 2000 compression");
					final long[] stripOffsets = ifd.getStripOffsets();
					final long[] stripByteCounts = ifd.getStripByteCounts();

					if (stripOffsets.length > 0) {
						final long stripOffset = stripOffsets[0];
						stream.seek(stripOffset);
						final JPEG2000Format jp2kFormat =
							scifio().format().getFormatFromClass(JPEG2000Format.class);
						final JPEG2000Format.Metadata jp2kMeta =
							(JPEG2000Format.Metadata) jp2kFormat.createMetadata();
						((JPEG2000Format.Parser) jp2kFormat.createParser()).parse(stream,
							jp2kMeta, stripOffset + stripByteCounts[0]);
						meta.setResolutionLevels(jp2kMeta.getResolutionLevels());
						if (meta.getResolutionLevels() != null &&
							!meta.isNoSubresolutions())
						{
							if (log().isDebug()) {
								log().debug(
									String.format(
										"Original resolution IFD Levels %d %dx%d Tile %dx%d", meta
											.getResolutionLevels(), ifd.getImageWidth(), ifd
											.getImageLength(), ifd.getTileWidth(), ifd
											.getTileLength()));
							}
							final IFDList theseSubResolutionIFDs = new IFDList();
							meta.getSubResolutionIFDs().add(theseSubResolutionIFDs);
							for (int level = 1; level <= meta.getResolutionLevels(); level++)
							{
								final IFD newIFD = new IFD(ifd, log());
								final long imageWidth = ifd.getImageWidth();
								final long imageLength = ifd.getImageLength();
								final long tileWidth = ifd.getTileWidth();
								final long tileLength = ifd.getTileLength();
								final long factor = (long) Math.pow(2, level);
								long newTileWidth = Math.round((double) tileWidth / factor);
								newTileWidth = newTileWidth < 1 ? 1 : newTileWidth;
								long newTileLength = Math.round((double) tileLength / factor);
								newTileLength = newTileLength < 1 ? 1 : newTileLength;
								final long evenTilesPerRow = imageWidth / tileWidth;
								final long evenTilesPerColumn = imageLength / tileLength;
								double remainingWidth =
									((double) (imageWidth - (evenTilesPerRow * tileWidth))) /
										factor;
								remainingWidth =
									remainingWidth < 1 ? Math.ceil(remainingWidth) : Math
										.round(remainingWidth);
								double remainingLength =
									((double) (imageLength - (evenTilesPerColumn * tileLength))) /
										factor;
								remainingLength =
									remainingLength < 1 ? Math.ceil(remainingLength) : Math
										.round(remainingLength);
								final long newImageWidth =
									(long) ((evenTilesPerRow * newTileWidth) + remainingWidth);
								final long newImageLength =
									(long) ((evenTilesPerColumn * newTileLength) + remainingLength);

								final int resolutionLevel =
									Math.abs(level - meta.getResolutionLevels());
								newIFD.put(IFD.IMAGE_WIDTH, newImageWidth);
								newIFD.put(IFD.IMAGE_LENGTH, newImageLength);
								newIFD.put(IFD.TILE_WIDTH, newTileWidth);
								newIFD.put(IFD.TILE_LENGTH, newTileLength);
								if (log().isDebug()) {
									log().debug(
										String.format(
											"Added JPEG 2000 sub-resolution IFD Level %d %dx%d "
												+ "Tile %dx%d", resolutionLevel, newImageWidth,
											newImageLength, newTileWidth, newTileLength));
								}
								theseSubResolutionIFDs.add(newIFD);
							}
						}
					}
					else {
						log().warn("IFD has no strip offsets!");
					}
				}
			}
		}

	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader<M extends Metadata> extends ByteArrayReader<M> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openThumbPlane(final int imageIndex,
			final long planeIndex) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final IFDList thumbnailIFDs = meta.getThumbnailIFDs();
			if (thumbnailIFDs == null || thumbnailIFDs.size() <= planeIndex) {
				return super.openThumbPlane(imageIndex, planeIndex);
			}
			final TiffParser tiffParser = meta.getTiffParser();
			tiffParser.fillInIFD(thumbnailIFDs.get((int)planeIndex));
			int[] bps = null;
			try {
				bps = thumbnailIFDs.get((int)planeIndex).getBitsPerSample();
			}
			catch (final FormatException e) {}

			if (bps == null) {
				return super.openThumbPlane(imageIndex, planeIndex);
			}

			int b = bps[0];
			while ((b % 8) != 0)
				b++;
			b /= 8;
			if (b != FormatTools.getBytesPerPixel(meta.getPixelType(imageIndex)) ||
				bps.length != meta.getAxisLength(imageIndex, Axes.CHANNEL))
			{
				return super.openThumbPlane(imageIndex, planeIndex);
			}

			byte[] buf =
				new byte[(int) (meta.getThumbSizeX(imageIndex) *
					meta.getThumbSizeY(imageIndex) *
					meta.getAxisLength(imageIndex, Axes.CHANNEL) * FormatTools
					.getBytesPerPixel(meta.getPixelType(imageIndex)))];

			final ByteArrayPlane plane = new ByteArrayPlane(getContext());
			buf = tiffParser.getSamples(thumbnailIFDs.get((int)planeIndex), buf);
			plane.populate(meta.get(imageIndex), buf, new long[2], new long[]{meta
				.getThumbSizeX(imageIndex), meta.getThumbSizeY(imageIndex)});

			return plane;
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buf = plane.getBytes();
			final IFDList ifds = meta.getIfds();
			final TiffParser tiffParser = meta.getTiffParser();
			final int xAxis = meta.getAxisIndex(imageIndex, Axes.X);
			final int yAxis = meta.getAxisIndex(imageIndex, Axes.Y);
			final int x = (int) planeMin[xAxis],
								y = (int) planeMin[yAxis],
								w = (int) planeMax[xAxis],
								h = (int) planeMax[yAxis];
			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex,
				buf.length, planeMin, planeMax);

			final IFD firstIFD = ifds.get(0);
			meta.setLastPlane(planeIndex);
			final IFD ifd = ifds.get((int)planeIndex);
			if ((firstIFD.getCompression() == TiffCompression.JPEG_2000 || firstIFD
				.getCompression() == TiffCompression.JPEG_2000_LOSSY) &&
				meta.getResolutionLevels() != null)
			{
				// FIXME: resolution levels
//        if (getCoreIndex() > 0) {
//          ifd = meta.getSubResolutionIFDs().get(planeIndex).get(getCoreIndex() - 1);
//        }
				setResolutionLevel(ifd);
			}

			tiffParser.getSamples(ifd, buf, x, y, w, h);

			final boolean float16 =
				meta.getPixelType(imageIndex) == FormatTools.FLOAT &&
					firstIFD.getBitsPerSample()[0] == 16;
			final boolean float24 =
				meta.getPixelType(imageIndex) == FormatTools.FLOAT &&
					firstIFD.getBitsPerSample()[0] == 24;

			if (float16 || float24) {
				final int nPixels =
					w * h * (int) meta.getAxisLength(imageIndex, Axes.CHANNEL);
				final int nBytes = float16 ? 2 : 3;
				final int mantissaBits = float16 ? 10 : 16;
				final int exponentBits = float16 ? 5 : 7;
				final int maxExponent = (int) Math.pow(2, exponentBits) - 1;
				final int bits = (nBytes * 8) - 1;

				final byte[] newBuf = new byte[buf.length];
				for (int i = 0; i < nPixels; i++) {
					final int v =
						DataTools.bytesToInt(buf, i * nBytes, nBytes, meta
							.isLittleEndian(imageIndex));
					final int sign = v >> bits;
					int exponent =
						(v >> mantissaBits) & (int) (Math.pow(2, exponentBits) - 1);
					int mantissa = v & (int) (Math.pow(2, mantissaBits) - 1);

					if (exponent == 0) {
						if (mantissa != 0) {
							while ((mantissa & (int) Math.pow(2, mantissaBits)) == 0) {
								mantissa <<= 1;
								exponent--;
							}
							exponent++;
							mantissa &= (int) (Math.pow(2, mantissaBits) - 1);
							exponent += 127 - (Math.pow(2, exponentBits - 1) - 1);
						}
					}
					else if (exponent == maxExponent) {
						exponent = 255;
					}
					else {
						exponent += 127 - (Math.pow(2, exponentBits - 1) - 1);
					}

					mantissa <<= (23 - mantissaBits);

					final int value = (sign << 31) | (exponent << 23) | mantissa;
					DataTools.unpackBytes(value, newBuf, i * 4, 4, meta
						.isLittleEndian(imageIndex));
				}
				System.arraycopy(newBuf, 0, buf, 0, newBuf.length);
			}

			return plane;
		}

		@Override
		public long getOptimalTileWidth(final int imageIndex) {
			FormatTools.assertId(getStream().getFileName(), true, 1);
			try {
				return getMetadata().getIfds().get(0).getTileWidth();
			}
			catch (final FormatException e) {
				log().debug("Could not retrieve tile width", e);
			}
			return super.getOptimalTileWidth(imageIndex);
		}

		@Override
		public long getOptimalTileHeight(final int imageIndex) {
			FormatTools.assertId(getStream().getFileName(), true, 1);
			try {
				return getMetadata().getIfds().get(0).getTileLength();
			}
			catch (final FormatException e) {
				log().debug("Could not retrieve tile height", e);
			}
			return super.getOptimalTileHeight(imageIndex);
		}

		/**
		 * Sets the resolution level when we have JPEG 2000 compressed data.
		 * 
		 * @param ifd The active IFD that is being used in our current
		 *          <code>openBytes()</code> calling context. It will be the
		 *          sub-resolution IFD if <code>currentSeries > 0</code>.
		 */
		protected void setResolutionLevel(final IFD ifd) {
			final Metadata meta = getMetadata();
			final JPEG2000CodecOptions j2kCodecOptions = meta.getJ2kCodecOptions();
			j2kCodecOptions.resolution = 0;
			// FIXME: resolution levels
//      j2kCodecOptions.resolution = Math.abs(getCoreIndex() - resolutionLevels);
			log().debug(
				"Using JPEG 2000 resolution level " + j2kCodecOptions.resolution);
			meta.getTiffParser().setCodecOptions(j2kCodecOptions);
		}
	}
}
