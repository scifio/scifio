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
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.codec.JPEG2000CodecOptions;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.PhotoInterp;
import io.scif.formats.tiff.TiffCompression;
import io.scif.formats.tiff.TiffParser;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.FormatService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * MinimalTiffReader is the superclass for file format readers compatible with
 * or derived from the TIFF 6.0 file format.
 *
 * @author Melissa Linkert
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "Minimal TIFF",
	priority = MinimalTIFFFormat.PRIORITY)
public class MinimalTIFFFormat extends AbstractFormat {

	public static final double PRIORITY = Priority.VERY_LOW;

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "tif", "tiff" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Fields --

		/** List of IFDs for the current TIFF. */
		private IFDList ifds;

		/** List of thumbnail IFDs for the current TIFF. */
		private IFDList thumbnailIFDs;

		/**
		 * List of sub-resolution IFDs for each IFD in the current TIFF with the
		 * same order as {@code ifds}.
		 */
		private List<IFDList> subResolutionIFDs;

		private TiffParser tiffParser;

		private boolean equalStrips = false;

		private boolean use64Bit = false;

		private long lastPlane = 0;

		private boolean noSubresolutions = false;

		/** Number of JPEG 2000 resolution levels. */
		private Integer resolutionLevels;

		/** Codec options to use when decoding JPEG 2000 data. */
		private JPEG2000CodecOptions j2kCodecOptions;

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
			if (getImageCount() == 0) createImageMetadata(1);
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
				if (thumbnailIFDs != null && thumbnailIFDs.size() > 0) {
					ms0.setThumbSizeX(thumbnailIFDs.get(0).getImageWidth());
					ms0.setThumbSizeY(thumbnailIFDs.get(0).getImageLength());
				}

				if (samples > 1) {
					ms0.setAxisLength(Axes.CHANNEL, samples);
					planarAxes = 3;
				}

				ms0.setPlanarAxisCount(planarAxes);
				ms0.setPixelType(firstIFD.getPixelType());
				ms0.setMetadataComplete(true);
				ms0.setIndexed(photo == PhotoInterp.RGB_PALETTE && (getColorTable(0,
					0) != null));

				if (ms0.isIndexed()) {
					ms0.setAxisLength(Axes.CHANNEL, 1);
					for (final IFD ifd : ifds) {
						ifd.putIFDValue(IFD.PHOTOMETRIC_INTERPRETATION,
							PhotoInterp.RGB_PALETTE);
					}
				}
				ms0.setBitsPerPixel(firstIFD.getBitsPerSample()[0]);

				// New core metadata now that we know how many sub-resolutions
				// we have.
				if (resolutionLevels != null && subResolutionIFDs.size() > 0) {
					final IFDList ifds = subResolutionIFDs.get(0);

					// FIXME: support sub resolutions in SCIFIO.. or something..
					// int seriesCount = ifds.size() + 1;
					// if (!hasFlattenedResolutions()) {
					// ms0.resolutionCount = seriesCount;
					// }

					if (ifds.size() + 1 < ms0.getAxisLength(Axes.TIME)) {
						ms0.setAxisLength(Axes.TIME, ms0.getAxisLength(Axes.TIME) - (ifds
							.size() + 1));
					}

					for (final IFD ifd : ifds) {
						final ImageMetadata ms = ms0.copy();
						add(ms);
						ms.setAxisLength(Axes.X, (int) ifd.getImageWidth());
						ms.setAxisLength(Axes.Y, (int) ifd.getImageLength());
						ms.setAxisLength(Axes.TIME, ms0.getAxisLength(Axes.TIME));
						ms.setThumbnail(true);
						ms.setThumbSizeX(ms0.getThumbSizeX());
						ms.setThumbSizeY(ms0.getThumbSizeY());
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
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				if (ifds != null) {
					for (final IFD ifd : ifds) {
						if (ifd.getOnDemandStripOffsets() != null) {
							ifd.getOnDemandStripOffsets().close();
						}
					}
				}
				ifds = null;
				thumbnailIFDs = null;
				subResolutionIFDs = new ArrayList<>();
				lastPlane = 0;
				tiffParser = null;
				resolutionLevels = null;
				j2kCodecOptions = JPEG2000CodecOptions.getDefaultOptions();
			}
		}

		// -- HasColorTable API methods --

		@Override
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			if (ifds == null || lastPlane < 0 || lastPlane > ifds.size()) return null;
			IFD lastIFD = ifds.get((int) lastPlane);

			ColorTable table = null;
			try {

				final int[] bits = lastIFD.getBitsPerSample();
				int[] colorMap = lastIFD.getIFDIntArray(IFD.COLOR_MAP);
				if (bits[0] <= 16 && bits[0] > 8) {
					if (colorMap == null || colorMap.length < 65536 * 3) {
						// it's possible that the LUT is only present in the
						// first IFD
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
						// it's possible that the LUT is only present in the
						// first IFD
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

		// -- Checker API Methods --

		@Override
		public boolean suffixNecessary() {
			return false;
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream) {
			return new TiffParser(getContext(), stream).isValidHeader();
		}
	}

	public static class Parser<M extends Metadata> extends AbstractParser<M> {

		@Parameter
		private FormatService formatService;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final M meta, final SCIFIOConfig config) throws IOException,
			FormatException
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
			getSource().order(little);

			log().debug("Reading IFDs");

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

			log().debug("Populating metadata");

			tiffParser.setAssumeEqualStrips(meta.isEqualStrips());
			for (final IFD ifd : ifds) {
				tiffParser.fillInIFD(ifd);
				if (ifd.getCompression() == TiffCompression.JPEG_2000 || ifd
					.getCompression() == TiffCompression.JPEG_2000_LOSSY)
				{
					log().debug("Found IFD with JPEG 2000 compression");
					final long[] stripOffsets = ifd.getStripOffsets();
					final long[] stripByteCounts = ifd.getStripByteCounts();

					if (stripOffsets.length > 0) {
						final long stripOffset = stripOffsets[0];
						stream.seek(stripOffset);
						final JPEG2000Format jp2kFormat = formatService.getFormatFromClass(
							JPEG2000Format.class);
						final JPEG2000Format.Metadata jp2kMeta =
							(JPEG2000Format.Metadata) jp2kFormat.createMetadata();
						((JPEG2000Format.Parser) jp2kFormat.createParser()).parse(stream,
							jp2kMeta, stripOffset + stripByteCounts[0]);
						meta.setResolutionLevels(jp2kMeta.getResolutionLevels());
						if (meta.getResolutionLevels() != null && !meta
							.isNoSubresolutions())
						{
							if (log().isDebug()) {
								log().debug(String.format(
									"Original resolution IFD Levels %d %dx%d Tile %dx%d", meta
										.getResolutionLevels(), ifd.getImageWidth(), ifd
											.getImageLength(), ifd.getTileWidth(), ifd
												.getTileLength()));
							}
							final IFDList theseSubResolutionIFDs = new IFDList();
							meta.getSubResolutionIFDs().add(theseSubResolutionIFDs);
							for (int level = 1; level <= meta
								.getResolutionLevels(); level++)
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
								double remainingWidth = ((double) (imageWidth -
									(evenTilesPerRow * tileWidth))) / factor;
								remainingWidth = remainingWidth < 1 ? Math.ceil(remainingWidth)
									: Math.round(remainingWidth);
								double remainingLength = ((double) (imageLength -
									(evenTilesPerColumn * tileLength))) / factor;
								remainingLength = remainingLength < 1 ? Math.ceil(
									remainingLength) : Math.round(remainingLength);
								final long newImageWidth = (long) ((evenTilesPerRow *
									newTileWidth) + remainingWidth);
								final long newImageLength = (long) ((evenTilesPerColumn *
									newTileLength) + remainingLength);

								final int resolutionLevel = Math.abs(level - meta
									.getResolutionLevels());
								newIFD.put(IFD.IMAGE_WIDTH, newImageWidth);
								newIFD.put(IFD.IMAGE_LENGTH, newImageLength);
								newIFD.put(IFD.TILE_WIDTH, newTileWidth);
								newIFD.put(IFD.TILE_LENGTH, newTileLength);
								if (log().isDebug()) {
									log().debug(String.format(
										"Added JPEG 2000 sub-resolution IFD Level %d %dx%d " +
											"Tile %dx%d", resolutionLevel, newImageWidth,
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

	public static class Reader<M extends Metadata> extends ByteArrayReader<M> {

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
			final Metadata meta = getMetadata();
			plane.setColorTable(meta.getColorTable(imageIndex, planeIndex));
			final byte[] buf = plane.getBytes();
			final IFDList ifds = meta.getIfds();
			final TiffParser tiffParser = meta.getTiffParser();
			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);

			final IFD firstIFD = ifds.get(0);
			meta.setLastPlane(planeIndex);
			final IFD ifd = ifds.get((int) planeIndex);
			if ((firstIFD.getCompression() == TiffCompression.JPEG_2000 || firstIFD
				.getCompression() == TiffCompression.JPEG_2000_LOSSY) && meta
					.getResolutionLevels() != null)
			{
				// FIXME: resolution levels
//        if (getCoreIndex() > 0) {
//          ifd = meta.getSubResolutionIFDs().get(planeIndex).get(getCoreIndex() - 1);
//        }
				setResolutionLevel(ifd);
			}

			tiffParser.getSamples(ifd, buf, x, y, w, h);

			final boolean float16 = meta.get(imageIndex)
				.getPixelType() == FormatTools.FLOAT && firstIFD
					.getBitsPerSample()[0] == 16;
			final boolean float24 = meta.get(imageIndex)
				.getPixelType() == FormatTools.FLOAT && firstIFD
					.getBitsPerSample()[0] == 24;

			if (float16 || float24) {
				final int nPixels = w * h * (int) meta.get(imageIndex).getAxisLength(
					Axes.CHANNEL);
				final int nBytes = float16 ? 2 : 3;
				final int mantissaBits = float16 ? 10 : 16;
				final int exponentBits = float16 ? 5 : 7;
				final int maxExponent = (int) Math.pow(2, exponentBits) - 1;
				final int bits = (nBytes * 8) - 1;

				final byte[] newBuf = new byte[buf.length];
				for (int i = 0; i < nPixels; i++) {
					final int v = Bytes.toInt(buf, i * nBytes, nBytes, meta.get(
						imageIndex).isLittleEndian());
					final int sign = v >> bits;
					int exponent = (v >> mantissaBits) & (int) (Math.pow(2,
						exponentBits) - 1);
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
					Bytes.unpack(value, newBuf, i * 4, 4, meta.get(imageIndex)
						.isLittleEndian());
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
		 *          {@code openBytes()} calling context. It will be the
		 *          sub-resolution IFD if {@code currentSeries > 0}.
		 */
		protected void setResolutionLevel(final IFD ifd) {
			final Metadata meta = getMetadata();
			final JPEG2000CodecOptions j2kCodecOptions = meta.getJ2kCodecOptions();
			j2kCodecOptions.resolution = 0;
			// FIXME: resolution levels
//      j2kCodecOptions.resolution = Math.abs(getCoreIndex() - resolutionLevels);
			log().debug("Using JPEG 2000 resolution level " +
				j2kCodecOptions.resolution);
			meta.getTiffParser().setCodecOptions(j2kCodecOptions);
		}
	}
}
