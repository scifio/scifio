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

package io.scif.formats.qt;

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
import io.scif.UnsupportedCompressionException;
import io.scif.codec.Codec;
import io.scif.codec.CodecOptions;
import io.scif.codec.CodecService;
import io.scif.codec.CompressionType;
import io.scif.codec.JPEGCodec;
import io.scif.codec.MJPBCodec;
import io.scif.codec.MJPBCodecOptions;
import io.scif.codec.QTRLECodec;
import io.scif.codec.ZlibCodec;
import io.scif.config.SCIFIOConfig;
import io.scif.services.FormatService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * NativeQTReader is the file format reader for QuickTime movie files. It does
 * not require any external libraries to be installed. Video codecs currently
 * supported: raw, rle, jpeg, mjpb, rpza. Additional video codecs will be added
 * as time permits.
 *
 * @author Melissa Linkert
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "QuickTime")
public class NativeQTFormat extends AbstractFormat {

	// -- Constants --

	/** List of identifiers for each container atom. */
	private static final String[] CONTAINER_TYPES = { "moov", "trak", "udta",
		"tref", "imap", "mdia", "minf", "stbl", "edts", "mdra", "rmra", "imag",
		"vnrp", "dinf" };

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "mov" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		/** Offset to start of pixel data. */
		private long pixelOffset;

		/** Total number of bytes of pixel data. */
		private long pixelBytes;

		/** Pixel depth. */
		private int bitsPerPixel;

		/** Raw plane size, in bytes. */
		private int rawSize;

		/** Offsets to each plane's pixel data. */
		private List<Integer> offsets;

		/** Pixel data for the previous image plane. */
		private byte[] prevPixels;

		/** Previous plane number. */
		private long prevPlane;

		/** Flag indicating whether we can safely use prevPixels. */
		private boolean canUsePrevious;

		/** Video codec used by this movie. */
		private String codec;

		/** Some movies use two video codecs -- this is the second codec. */
		private String altCodec;

		/** Number of frames that use the alternate codec. */
		private int altPlanes;

		/** Amount to subtract from each offset. */
		private int scale;

		/** Number of bytes in each plane. */
		private List<Integer> chunkSizes;

		/** Set to true if the scanlines in a plane are interlaced (mjpb only). */
		private boolean interlaced;

		/** Flag indicating whether the resource and data fork are separated. */
		private boolean spork;

		private boolean flip;

		// -- NativeQTMetadata getters and setters --

		public long getPixelOffset() {
			return pixelOffset;
		}

		public void setPixelOffset(final long pixelOffset) {
			this.pixelOffset = pixelOffset;
		}

		public long getPixelBytes() {
			return pixelBytes;
		}

		public void setPixelBytes(final long pixelBytes) {
			this.pixelBytes = pixelBytes;
		}

		public int getBitsPerPixel() {
			return bitsPerPixel;
		}

		public void setBitsPerPixel(final int bitsPerPixel) {
			this.bitsPerPixel = bitsPerPixel;
		}

		public int getRawSize() {
			return rawSize;
		}

		public void setRawSize(final int rawSize) {
			this.rawSize = rawSize;
		}

		public List<Integer> getOffsets() {
			return offsets;
		}

		public void setOffsets(final List<Integer> offsets) {
			this.offsets = offsets;
		}

		public byte[] getPrevPixels() {
			return prevPixels;
		}

		public void setPrevPixels(final byte[] prevPixels) {
			this.prevPixels = prevPixels;
		}

		public long getPrevPlane() {
			return prevPlane;
		}

		public void setPrevPlane(final long prevPlane) {
			this.prevPlane = prevPlane;
		}

		public boolean isCanUsePrevious() {
			return canUsePrevious;
		}

		public void setCanUsePrevious(final boolean canUsePrevious) {
			this.canUsePrevious = canUsePrevious;
		}

		public String getCodec() {
			return codec;
		}

		public void setCodec(final String codec) {
			this.codec = codec;
		}

		public String getAltCodec() {
			return altCodec;
		}

		public void setAltCodec(final String altCodec) {
			this.altCodec = altCodec;
		}

		public int getAltPlanes() {
			return altPlanes;
		}

		public void setAltPlanes(final int altPlanes) {
			this.altPlanes = altPlanes;
		}

		public int getScale() {
			return scale;
		}

		public void setScale(final int scale) {
			this.scale = scale;
		}

		public List<Integer> getChunkSizes() {
			return chunkSizes;
		}

		public void setChunkSizes(final List<Integer> chunkSizes) {
			this.chunkSizes = chunkSizes;
		}

		public boolean isInterlaced() {
			return interlaced;
		}

		public void setInterlaced(final boolean interlaced) {
			this.interlaced = interlaced;
		}

		public boolean isSpork() {
			return spork;
		}

		public void setSpork(final boolean spork) {
			this.spork = spork;
		}

		public boolean isFlip() {
			return flip;
		}

		public void setFlip(final boolean flip) {
			this.flip = flip;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			if (getBitsPerPixel() < 40) {
				iMeta.setPlanarAxisCount(3);
				iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y, Axes.TIME);
				iMeta.setAxisLength(Axes.CHANNEL, 3);
			}

			final int bytes = (getBitsPerPixel() / 8) % 4;
			iMeta.setPixelType(bytes == 2 ? FormatTools.UINT16 : FormatTools.UINT8);
			iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));

			iMeta.setLittleEndian(false);
			iMeta.setMetadataComplete(true);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				offsets = null;
				prevPixels = null;
				codec = altCodec = null;
				pixelOffset = pixelBytes = bitsPerPixel = rawSize = 0;
				prevPlane = altPlanes = 0;
				canUsePrevious = false;
				scale = 0;
				chunkSizes = null;
				interlaced = spork = flip = false;
			}
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Checker API Methods --

		@Override
		public boolean suffixNecessary() {
			return false;
		}

		@Override
		public boolean isFormat(final DataHandle<Location> stream)
			throws IOException
		{
			final int blockLen = 64;
			if (!FormatTools.validStream(stream, blockLen, false)) return false;
			// use a crappy hack for now
			final String s = stream.readString(blockLen);
			for (final String CONTAINER_TYPE : CONTAINER_TYPES) {
				if (s.contains(CONTAINER_TYPE) && !CONTAINER_TYPE.equals("imag")) {
					return true;
				}
			}
			return s.contains("wide") || s.contains("mdat") || s.contains("ftypqt");
		}
	}

	public static class Parser extends AbstractParser<Metadata> {

		@Parameter
		DataHandleService dataHandleService;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{

			meta.setSpork(true);
			final List<Integer> offsets = new ArrayList<>();
			final List<Integer> chunkSizes = new ArrayList<>();

			meta.setOffsets(offsets);
			meta.setChunkSizes(chunkSizes);
			meta.createImageMetadata(1);
			log().info("Parsing tags");

			NativeQTUtils.parse(stream, meta, 0, 0, stream.length(), log());

			final ImageMetadata iMeta = meta.get(0);

			iMeta.setPlanarAxisCount(2);
			iMeta.setAxisLength(Axes.TIME, offsets.size());

			if (chunkSizes.size() < iMeta.getPlaneCount() && chunkSizes.size() > 0) {
				iMeta.setAxisLength(Axes.TIME, chunkSizes.size());
			}

			log().info("Populating metadata");
			final Location baseLocation = stream.get();
			final String id = baseLocation.getName();

			// this handles the case where the data and resource forks have been
			// separated
			if (meta.isSpork()) {
				BrowsableLocation browsableBaseLoc;
				if (baseLocation instanceof BrowsableLocation) {
					browsableBaseLoc = (BrowsableLocation) baseLocation;
				}
				else {
					throw new IOException(
						"Can not open sporked QT file from an not browsable location!");
				}

				// first we want to check if there is a resource fork present
				// the resource fork will generally have the same name as the
				// data fork, but will have either the prefix "._" or the suffix ".qtr"
				// (or <filename>/rsrc on a Mac)

				String base = null;
				if (id.contains(".")) {
					base = id.substring(0, id.lastIndexOf('.'));
				}
				else base = id;

				BrowsableLocation f = browsableBaseLoc.sibling(base + ".qtr");
				log().debug("Searching for research fork:");
				if (dataHandleService.exists(f)) {
					log().debug("\t Found: " + f);
					if (getSource() != null) getSource().close();
					updateSource(f);

					NativeQTUtils.stripHeader(stream);
					NativeQTUtils.parse(stream, meta, 0, 0, getSource().length(), log());
					meta.get(0).setAxisLength(Axes.TIME, offsets.size());
				}
				else {
					log().debug("\tAbsent: " + f);
					f = browsableBaseLoc.sibling("._" + base);
					if (dataHandleService.exists(f)) {
						log().debug("\t Found: " + f);
						parseLocation(meta, offsets, f);
					}
					else {
						log().debug("\tAbsent: " + f);
						f = browsableBaseLoc.sibling(File.separator + ".." +
							File.separator + "namedfork" + File.separator + "rsrc");
						if (dataHandleService.exists(f)) {
							log().debug("\t Found: " + f);
							parseLocation(meta, offsets, f);
						}
						else {
							log().debug("\tAbsent: " + f);
							throw new FormatException("QuickTime resource fork not found. " +
								" To avoid this issue, please flatten your QuickTime movies " +
								"before importing with SCIFIO.");
						}
					}
				}

				// reset the stream, otherwise openBytes will try to read pixels
				// from the resource fork
//				if (tmpStream != null) stream.close();
			}
		}

		private void parseLocation(final Metadata meta, final List<Integer> offsets,
			final Location f) throws IOException, FormatException
		{
			try (final DataHandle<Location> tmpStream = dataHandleService.create(f)) {

				NativeQTUtils.stripHeader(tmpStream);
				NativeQTUtils.parse(tmpStream, meta, 0, tmpStream.offset(), tmpStream
					.length(), log());
				meta.get(0).setAxisLength(Axes.TIME, offsets.size());
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

			final Metadata meta = getMetadata();
			final byte[] buf = plane.getData();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, buf.length,
				bounds);

			String code = meta.getCodec();
			if (planeIndex >= meta.get(imageIndex).getPlaneCount() - meta
				.getAltPlanes()) code = meta.altCodec;

			int offset = meta.getOffsets().get((int) planeIndex).intValue();
			int nextOffset = (int) meta.getPixelBytes();

			meta.setScale(meta.getOffsets().get(0).intValue());
			offset -= meta.getScale();

			if (planeIndex < meta.getOffsets().size() - 1) {
				nextOffset = meta.getOffsets().get((int) planeIndex + 1).intValue() -
					meta.getScale();
			}

			if ((nextOffset - offset) < 0) {
				final int temp = offset;
				offset = nextOffset;
				nextOffset = temp;
			}

			final byte[] pixs = new byte[nextOffset - offset];

			getHandle().seek(meta.getPixelOffset() + offset);
			getHandle().read(pixs);

			meta.setCanUsePrevious((meta.getPrevPixels() != null) && (meta
				.getPrevPlane() == planeIndex - 1) && !code.equals(meta.getAltCodec()));

			byte[] t = meta.getPrevPlane() == planeIndex && meta
				.getPrevPixels() != null && !code.equals(meta.getAltCodec()) ? meta
					.getPrevPixels() : NativeQTUtils.uncompress(pixs, code, meta);
			if (code.equals("rpza")) {
				for (int i = 0; i < t.length; i++) {
					t[i] = (byte) (255 - t[i]);
				}
				meta.setPrevPlane((int) planeIndex);
				return plane;
			}

			// on rare occassions, we need to trim the data
			if (meta.isCanUsePrevious() && (meta.getPrevPixels().length < t.length)) {
				final byte[] temp = t;
				t = new byte[meta.getPrevPixels().length];
				System.arraycopy(temp, 0, t, 0, t.length);
			}

			meta.setPrevPixels(t);
			meta.setPrevPlane(planeIndex);

			// determine whether we need to strip out any padding bytes

			final int bytes = meta.getBitsPerPixel() < 40 ? meta.getBitsPerPixel() / 8
				: (meta.getBitsPerPixel() - 32) / 8;
			int pad = (4 - (int) (meta.get(imageIndex).getAxisLength(Axes.X) % 4)) %
				4;
			if (meta.getCodec().equals("mjpb")) pad = 0;

			final int expectedSize = (int) FormatTools.getPlaneSize(this, imageIndex);

			if (meta.getPrevPixels().length == expectedSize || (meta
				.getBitsPerPixel() == 32 && (3 * (meta.getPrevPixels().length /
					4)) == expectedSize))
			{
				pad = 0;
			}

			if (pad > 0) {
				t = new byte[meta.getPrevPixels().length - (int) meta.get(imageIndex)
					.getAxisLength(Axes.Y) * pad];

				for (int row = 0; row < meta.get(imageIndex).getAxisLength(
					Axes.Y); row++)
				{
					final int sourceIndex = row * (bytes * (int) meta.get(imageIndex)
						.getAxisLength(Axes.X) + pad);
					final int destIndex = row * (int) meta.get(imageIndex).getAxisLength(
						Axes.X) * bytes;
					final int length = (int) meta.get(imageIndex).getAxisLength(Axes.X) *
						bytes;
					System.arraycopy(meta.getPrevPixels(), sourceIndex, t, destIndex,
						length);
				}
			}

			final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
				.getPixelType());

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xAxis), y = (int) bounds.min(yAxis), //
					w = (int) bounds.dimension(xAxis), h = (int) bounds.dimension(yAxis);
			final int srcRowLen = (int) (meta.get(imageIndex).getAxisLength(Axes.X) *
				bpp * meta.get(imageIndex).getAxisLength(Axes.CHANNEL));
			final int destRowLen = w * bpp * (int) meta.get(imageIndex).getAxisLength(
				Axes.CHANNEL);
			for (int row = 0; row < h; row++) {
				if (meta.getBitsPerPixel() == 32) {
					for (int col = 0; col < w; col++) {
						final int src = (row + y) * (int) meta.get(imageIndex)
							.getAxisLength(Axes.X) * bpp * 4 + (x + col) * bpp * 4 + 1;
						final int dst = row * destRowLen + col * bpp * 3;
						if (src + 3 <= t.length && dst + 3 <= buf.length) {
							System.arraycopy(t, src, buf, dst, 3);
						}
					}
				}
				else {
					System.arraycopy(t, row * srcRowLen + x * bpp * (int) meta.get(
						imageIndex).getAxisLength(Axes.CHANNEL), buf, row * destRowLen,
						destRowLen);
				}
			}

			if ((meta.getBitsPerPixel() == 40 || meta.getBitsPerPixel() == 8) && !code
				.equals("mjpb"))
			{
				// invert the pixels
				for (int i = 0; i < buf.length; i++) {
					buf[i] = (byte) (255 - buf[i]);
				}
			}
			return plane;
		}
	}

	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constants --

		// NB: Writing to Motion JPEG-B with QTJava seems to be broken.
		/** Value indicating Motion JPEG-B codec. */
		public static final int CODEC_MOTION_JPEG_B = 1835692130;

		/** Value indicating Cinepak codec. */
		public static final int CODEC_CINEPAK = 1668704612;

		/** Value indicating Animation codec. */
		public static final int CODEC_ANIMATION = 1919706400;

		/** Value indicating H.263 codec. */
		public static final int CODEC_H_263 = 1748121139;

		/** Value indicating Sorenson codec. */
		public static final int CODEC_SORENSON = 1398165809;

		/** Value indicating Sorenson 3 codec. */
		public static final int CODEC_SORENSON_3 = 0x53565133;

		/** Value indicating MPEG-4 codec. */
		public static final int CODEC_MPEG_4 = 0x6d703476;

		/** Value indicating Raw codec. */
		public static final int CODEC_RAW = 0;

		/** Value indicating Low quality. */
		public static final int QUALITY_LOW = 256;

		/** Value indicating Normal quality. */
		public static final int QUALITY_NORMAL = 512;

		/** Value indicating High quality. */
		public static final int QUALITY_HIGH = 768;

		/** Value indicating Maximum quality. */
		public static final int QUALITY_MAXIMUM = 1023;

		/** Seek to this offset to update the total number of pixel bytes. */
		private static final long BYTE_COUNT_OFFSET = 8;

		// -- Fields --

		@Parameter
		private QTJavaService qtJavaService;

		@Parameter
		private FormatService formatService;

		@Parameter
		private TranslatorService translatorService;

		@Parameter
		private DataHandleService dataHandleService;

		/** The codec to use. */
		private int codec = CODEC_RAW;

		/** The quality to use. */
		private int quality = QUALITY_NORMAL;

		/** Total number of pixel bytes. */
		private int numBytes;

		/** Vector of plane offsets. */
		private List<Integer> offsets;

		/** Time the file was created. */
		private int created;

		/** Number of padding bytes in each row. */
		private int pad;

		/** Whether we need the legacy writer. */
		private boolean needLegacy = false;

		private LegacyQTFormat.Writer legacy;

		private int numWritten = 0;

		// -- AbstractWriter Methods --

		@Override
		protected String[] makeCompressionTypes() {
			if (qtJavaService.canDoQT()) {
				return new String[] { CompressionType.UNCOMPRESSED.getCompression(),
					// NB: Writing to Motion JPEG-B with QTJava seems to be
					// broken.
					/* "Motion JPEG-B", */
					CompressionType.CINEPAK.getCompression(), CompressionType.ANIMATION
						.getCompression(), CompressionType.H_263.getCompression(),
					CompressionType.SORENSON.getCompression(), CompressionType.SORENSON_3
						.getCompression(), CompressionType.MPEG_4.getCompression() };
			}
			return new String[] { CompressionType.UNCOMPRESSED.getCompression() };
		}

		// -- QTWriter API methods --

		/**
		 * Sets the encoded movie's codec.
		 *
		 * @param codec Codec value:
		 *          <ul>
		 *          <li>QTWriter.CODEC_CINEPAK</li>
		 *          <li>QTWriter.CODEC_ANIMATION</li>
		 *          <li>QTWriter.CODEC_H_263</li>
		 *          <li>QTWriter.CODEC_SORENSON</li>
		 *          <li>QTWriter.CODEC_SORENSON_3</li>
		 *          <li>QTWriter.CODEC_MPEG_4</li>
		 *          <li>QTWriter.CODEC_RAW</li>
		 *          </ul>
		 */
		public void setCodec(final int codec) {
			this.codec = codec;
		}

		/**
		 * Sets the quality of the encoded movie.
		 *
		 * @param quality Quality value:
		 *          <ul>
		 *          <li>QTWriter.QUALITY_LOW</li>
		 *          <li>QTWriter.QUALITY_MEDIUM</li>
		 *          <li>QTWriter.QUALITY_HIGH</li>
		 *          <li>QTWriter.QUALITY_MAXIMUM</li>
		 *          </ul>
		 */
		public void setQuality(final int quality) {
			this.quality = quality;
		}

		// -- AbstractWriter Methods --

		@Override
		protected void initialize(final int imageIndex, final long planeIndex,
			final Interval bounds) throws FormatException, IOException
		{
			if (!isInitialized(imageIndex, (int) planeIndex)) {
				setCodec();
				final DataHandle<Location> handle = getHandle();
				if (codec != CODEC_RAW) {
					needLegacy = true;
					legacy.setDest(handle);
					return;
				}

				// update the number of pixel bytes written
				final Metadata meta = getMetadata();
				final int height = (int) meta.get(imageIndex).getAxisLength(Axes.Y);
				numBytes += (meta.get(imageIndex).getPlaneSize() + pad * height);
				handle.seek(BYTE_COUNT_OFFSET);
				handle.writeInt(numBytes + 8);

				handle.seek(offsets.get((int) planeIndex));

				if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, bounds)) {
					handle.skipBytes((int) (meta.get(imageIndex).getPlaneSize() + pad *
						height));
				}
			}
		}

		// -- Writer API methods --

		@Override
		public void writePlane(final int imageIndex, final long planeIndex,
			final Plane plane, final Interval bounds) throws FormatException,
			IOException
		{
			final byte[] buf = plane.getBytes();
			checkParams(imageIndex, planeIndex, buf, bounds);
			if (needLegacy) {
				legacy.savePlane(imageIndex, planeIndex, plane, bounds);
				return;
			}

			final Metadata meta = getMetadata();
			final boolean interleaved = plane.getImageMetadata()
				.getInterleavedAxisCount() > 0;
			// get the width and height of the image
			final int width = (int) meta.get(imageIndex).getAxisLength(Axes.X);
			// need to check if the width is a multiple of 8
			// if it is, great; if not, we need to pad each scanline with enough
			// bytes to make the width a multiple of 8

			final int nChannels = (int) meta.get(imageIndex).getAxisLength(
				Axes.CHANNEL);

			final int xIndex = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yIndex = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) bounds.min(xIndex), y = (int) bounds.min(yIndex), //
					w = (int) bounds.dimension(xIndex), h = (int) bounds.dimension(
						yIndex);

			getHandle().seek(offsets.get((int) planeIndex) + y * (nChannels * width +
				pad));

			// invert each pixel
			// this will makes the colors look right in other readers (e.g.
			// xine),
			// but needs to be reversed in QTReader

			final byte[] tmp = new byte[buf.length];
			if (nChannels == 1 && !needLegacy) {
				for (int i = 0; i < buf.length; i++) {
					tmp[i] = (byte) (255 - buf[i]);
				}
			}
			else System.arraycopy(buf, 0, tmp, 0, buf.length);

			if (!interleaved) {
				// need to write interleaved data
				final byte[] tmp2 = new byte[tmp.length];
				System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
				for (int i = 0; i < tmp.length; i++) {
					final int c = i / (w * h);
					final int index = i % (w * h);
					tmp[index * nChannels + c] = tmp2[i];
				}
			}

			final int rowLen = tmp.length / h;
			for (int row = 0; row < h; row++) {
				getHandle().skipBytes(nChannels * x);
				getHandle().write(tmp, row * rowLen, rowLen);
				for (int i = 0; i < pad; i++) {
					getHandle().writeByte(0);
				}
				if (row < h - 1) {
					getHandle().skipBytes(nChannels * (width - w - x));
				}
			}
			numWritten++;
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.UINT8 };
		}

		@Override
		public void close() throws IOException {
			if (getHandle() != null) writeFooter();
			super.close();
			numBytes = 0;
			created = 0;
			offsets = null;
			pad = 0;
			numWritten = 0;
		}

		@Override
		public void setDest(final DataHandle<Location> stream, final int imageIndex,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			super.setDest(stream, imageIndex, config);
			final Metadata meta = getMetadata();
			SCIFIOMetadataTools.verifyMinimumPopulated(meta, stream);

			final int width = (int) meta.get(imageIndex).getAxisLength(Axes.X);
			final int height = (int) meta.get(imageIndex).getAxisLength(Axes.Y);
			final int nChannels = (int) meta.get(imageIndex).getAxisLength(
				Axes.CHANNEL);
			final int planeSize = width * height * nChannels;

			pad = nChannels > 1 ? 0 : (4 - (width % 4)) % 4;

			if (legacy == null) {
				final LegacyQTFormat legacyFormat = formatService.getFormatFromClass(
					LegacyQTFormat.class);
				legacy = (LegacyQTFormat.Writer) legacyFormat.createWriter();
				final io.scif.Metadata legacyMeta = legacyFormat.createMetadata();
				translatorService.translate(meta, legacyMeta, false);

				legacy.setMetadata(legacyMeta);

				legacy.setCodec(codec);
			}
			offsets = new ArrayList<>();
			created = (int) System.currentTimeMillis();
			numBytes = 0;

			if (getHandle().length() == 0) {
				// -- write the first header --

				writeAtom(8, "wide");
				writeAtom(numBytes + 8, "mdat");
			}
			else {
				getHandle().seek(BYTE_COUNT_OFFSET);

				// read the length from the existing file, using a temporary handle
				try (DataHandle<Location> tmp = dataHandleService.create(stream
					.get()))
				{
					tmp.seek(BYTE_COUNT_OFFSET);
					numBytes = tmp.readInt() - 8;
				}
			}

			for (int i = 0; i < meta.get(0).getPlaneCount(); i++) {
				offsets.add(16 + i * (planeSize + pad * height));
			}
		}

		// -- Helper methods --

		private void setCodec() {
			if (getCompression() == null) return;
			if (getCompression().equals("Uncompressed")) codec = CODEC_RAW;
			// NB: Writing to Motion JPEG-B with QTJava seems to be broken.
			else if (getCompression().equals("Motion JPEG-B")) codec =
				CODEC_MOTION_JPEG_B;
			else if (getCompression().equals("Cinepak")) codec = CODEC_CINEPAK;
			else if (getCompression().equals("Animation")) codec = CODEC_ANIMATION;
			else if (getCompression().equals("H.263")) codec = CODEC_H_263;
			else if (getCompression().equals("Sorenson")) codec = CODEC_SORENSON;
			else if (getCompression().equals("Sorenson 3")) codec = CODEC_SORENSON_3;
			else if (getCompression().equals("MPEG 4")) codec = CODEC_MPEG_4;
		}

		private void writeFooter() throws IOException {
			getHandle().seek(getHandle().length());
			final Metadata meta = getMetadata();
			final int width = (int) meta.get(0).getAxisLength(Axes.X);
			final int height = (int) meta.get(0).getAxisLength(Axes.Y);
			final int nChannels = (int) meta.get(0).getAxisLength(Axes.CHANNEL);

			final int timeScale = 1000;
			final int duration = (int) (numWritten * ((double) timeScale /
				getFramesPerSecond()));
			final int bitsPerPixel = (nChannels > 1) ? 24 : 40;
			final int channels = (bitsPerPixel >= 40) ? 1 : 3;

			// -- write moov atom --

			int atomLength = 685 + 8 * numWritten;
			writeAtom(atomLength, "moov");

			// -- write mvhd atom --

			writeAtom(108, "mvhd");
			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(created); // creation time
			getHandle().writeInt((int) System.currentTimeMillis());
			getHandle().writeInt(timeScale); // time scale
			getHandle().writeInt(duration); // duration
			getHandle().write(new byte[] { 0, 1, 0, 0 }); // preferred rate &
			// volume
			getHandle().write(new byte[] { 0, -1, 0, 0, 0, 0, 0, 0, 0, 0 }); // reserved

			writeRotationMatrix();

			getHandle().writeShort(0); // not sure what this is
			getHandle().writeInt(0); // preview duration
			getHandle().writeInt(0); // preview time
			getHandle().writeInt(0); // poster time
			getHandle().writeInt(0); // selection time
			getHandle().writeInt(0); // selection duration
			getHandle().writeInt(0); // current time
			getHandle().writeInt(2); // next track's id

			// -- write trak atom --

			atomLength -= 116;
			writeAtom(atomLength, "trak");

			// -- write tkhd atom --

			writeAtom(92, "tkhd");
			getHandle().writeShort(0); // version
			getHandle().writeShort(15); // flags

			getHandle().writeInt(created); // creation time
			getHandle().writeInt((int) System.currentTimeMillis());
			getHandle().writeInt(1); // track id
			getHandle().writeInt(0); // reserved

			getHandle().writeInt(duration); // duration
			getHandle().writeInt(0); // reserved
			getHandle().writeInt(0); // reserved
			getHandle().writeShort(0); // reserved
			getHandle().writeInt(0); // unknown

			writeRotationMatrix();

			getHandle().writeInt(width); // image width
			getHandle().writeInt(height); // image height
			getHandle().writeShort(0); // reserved

			// -- write edts atom --

			writeAtom(36, "edts");

			// -- write elst atom --

			writeAtom(28, "elst");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(1); // number of entries in the table
			getHandle().writeInt(duration); // duration
			getHandle().writeShort(0); // time
			getHandle().writeInt(1); // rate
			getHandle().writeShort(0); // unknown

			// -- write mdia atom --

			atomLength -= 136;
			writeAtom(atomLength, "mdia");

			// -- write mdhd atom --

			writeAtom(32, "mdhd");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(created); // creation time
			getHandle().writeInt((int) System.currentTimeMillis());
			getHandle().writeInt(timeScale); // time scale
			getHandle().writeInt(duration); // duration
			getHandle().writeShort(0); // language
			getHandle().writeShort(0); // quality

			// -- write hdlr atom --

			writeAtom(58, "hdlr");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeBytes("mhlr");
			getHandle().writeBytes("vide");
			getHandle().writeBytes("appl");
			getHandle().write(new byte[] { 16, 0, 0, 0, 0, 1, 1, 11, 25 });
			getHandle().writeBytes("Apple Video Media Handler");

			// -- write minf atom --

			atomLength -= 98;
			writeAtom(atomLength, "minf");

			// -- write vmhd atom --

			writeAtom(20, "vmhd");

			getHandle().writeShort(0); // version
			getHandle().writeShort(1); // flags
			getHandle().writeShort(64); // graphics mode
			getHandle().writeShort(32768); // opcolor 1
			getHandle().writeShort(32768); // opcolor 2
			getHandle().writeShort(32768); // opcolor 3

			// -- write hdlr atom --

			writeAtom(57, "hdlr");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeBytes("dhlr");
			getHandle().writeBytes("alis");
			getHandle().writeBytes("appl");
			getHandle().write(new byte[] { 16, 0, 0, 1, 0, 1, 1, 31, 24 });
			getHandle().writeBytes("Apple Alias Data Handler");

			// -- write dinf atom --

			writeAtom(36, "dinf");

			// -- write dref atom --

			writeAtom(28, "dref");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeShort(0); // version 2
			getHandle().writeShort(1); // flags 2
			getHandle().write(new byte[] { 0, 0, 0, 12 });
			getHandle().writeBytes("alis");
			getHandle().writeShort(0); // version 3
			getHandle().writeShort(1); // flags 3

			// -- write stbl atom --

			atomLength -= 121;
			writeAtom(atomLength, "stbl");

			// -- write stsd atom --

			writeAtom(118, "stsd");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(1); // number of entries in the table
			getHandle().write(new byte[] { 0, 0, 0, 102 });
			getHandle().writeBytes("raw "); // codec
			getHandle().write(new byte[] { 0, 0, 0, 0, 0, 0 }); // reserved
			getHandle().writeShort(1); // data reference
			getHandle().writeShort(1); // version
			getHandle().writeShort(1); // revision
			getHandle().writeBytes("appl");
			getHandle().writeInt(0); // temporal quality
			getHandle().writeInt(768); // spatial quality
			getHandle().writeShort(width); // image width
			getHandle().writeShort(height); // image height
			final byte[] dpi = new byte[] { 0, 72, 0, 0 };
			getHandle().write(dpi); // horizontal dpi
			getHandle().write(dpi); // vertical dpi
			getHandle().writeInt(0); // data size
			getHandle().writeShort(1); // frames per sample
			getHandle().writeShort(12); // length of compressor name
			getHandle().writeBytes("Uncompressed"); // compressor name
			getHandle().writeInt(bitsPerPixel); // unknown
			getHandle().writeInt(bitsPerPixel); // unknown
			getHandle().writeInt(bitsPerPixel); // unknown
			getHandle().writeInt(bitsPerPixel); // unknown
			getHandle().writeInt(bitsPerPixel); // unknown
			getHandle().writeShort(bitsPerPixel); // bits per pixel
			getHandle().writeInt(65535); // ctab ID
			getHandle().write(new byte[] { 12, 103, 97, 108 }); // gamma
			getHandle().write(new byte[] { 97, 1, -52, -52, 0, 0, 0, 0 }); // unknown

			// -- write stts atom --

			writeAtom(24, "stts");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(1); // number of entries in the table
			getHandle().writeInt(numWritten); // number of planes
			// milliseconds per frame
			getHandle().writeInt((int) ((double) timeScale / getFramesPerSecond()));

			// -- write stsc atom --

			writeAtom(28, "stsc");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(1); // number of entries in the table
			getHandle().writeInt(1); // chunk
			getHandle().writeInt(1); // samples
			getHandle().writeInt(1); // id

			// -- write stsz atom --

			writeAtom(20 + 4 * numWritten, "stsz");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(0); // sample size
			getHandle().writeInt(numWritten); // number of planes
			for (int i = 0; i < numWritten; i++) {
				// sample size
				getHandle().writeInt(channels * height * (width + pad));
			}

			// -- write stco atom --

			writeAtom(16 + 4 * numWritten, "stco");

			getHandle().writeShort(0); // version
			getHandle().writeShort(0); // flags
			getHandle().writeInt(numWritten); // number of planes
			for (int i = 0; i < numWritten; i++) {
				// write the plane offset
				getHandle().writeInt(offsets.get(i));
			}
		}

		/** Write the 3x3 matrix that describes how to rotate the image. */
		private void writeRotationMatrix() throws IOException {
			getHandle().writeInt(1);
			getHandle().writeInt(0);
			getHandle().writeInt(0);
			getHandle().writeInt(0);
			getHandle().writeInt(1);
			getHandle().writeInt(0);
			getHandle().writeInt(0);
			getHandle().writeInt(0);
			getHandle().writeInt(16384);
		}

		/** Write the atom length and type. */
		private void writeAtom(final int length, final String type)
			throws IOException
		{
			getHandle().writeInt(length);
			getHandle().writeBytes(type);
		}
	}

	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class NativeQTTranslator extends
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
			dest.createImageMetadata(1);
			dest.get(0).setAxisLength(Axes.X, source.get(0).getAxisLength(Axes.X));
			dest.get(0).setAxisLength(Axes.Y, source.get(0).getAxisLength(Axes.Y));
			dest.get(0).setAxisLength(Axes.TIME, source.get(0).getPlaneCount());

			// *** HACK *** the Metadata bitsPerPixel field doesn't really
			// matter if
			// we're translating to this format.
			// But it is used to determine RGB status.
			final int bpp = FormatTools.getBitsPerPixel(source.get(0)
				.getPixelType()) == 8 ? 8 : 16;

			dest.setBitsPerPixel(source.get(0).isMultichannel() ? bpp : (bpp * 5));
		}
	}

	// -- Helper class --

	private static class NativeQTUtils {

		/** Parse all of the atoms in the file. */
		private static void parse(final DataHandle<Location> stream,
			final Metadata meta, int depth, long offset, final long length,
			final LogService log) throws FormatException, IOException
		{

			DataHandleService dataHandleService = null;

			while (offset < length) {
				stream.seek(offset);

				// first 4 bytes are the atom size
				long atomSize = stream.readInt() & 0xffffffffL;

				// read the atom type
				final String atomType = stream.readString(4);

				// if atomSize is 1, then there is an 8 byte extended size
				if (atomSize == 1) {
					atomSize = stream.readLong();
				}

				if (atomSize < 0) {
					log.warn("QTReader: invalid atom size: " + atomSize);
				}

				log.debug("Seeking to " + offset + "; atomType=" + atomType +
					"; atomSize=" + atomSize);

				// if this is a container atom, parse the children
				if (isContainer(atomType)) {
					parse(stream, meta, depth++, stream.offset(), offset + atomSize, log);
				}
				else {
					if (atomSize == 0) atomSize = stream.length();
					final long oldpos = stream.offset();

					if (atomType.equals("mdat")) {
						// we've found the pixel data
						meta.setPixelOffset(stream.offset());
						// "size" includes the size and offset bytes
						meta.setPixelBytes(atomSize - 8);

						if (meta.getPixelBytes() > (stream.length() - meta
							.getPixelOffset()))
						{
							meta.setPixelBytes(stream.length() - meta.getPixelOffset());
						}
					}
					else if (atomType.equals("tkhd")) {
						// we've found the dimensions

						stream.skipBytes(38);
						final int[][] matrix = new int[3][3];

						for (int i = 0; i < matrix.length; i++) {
							for (int j = 0; j < matrix[0].length; j++) {
								matrix[i][j] = stream.readInt();
							}
						}

						// The contents of the matrix we just read determine
						// whether or not
						// we should flip the width and height. We can check the
						// first two
						// rows of the matrix - they should correspond to the
						// first two rows
						// of an identity matrix.

						// TODO : adapt to use the value of flip
						meta.setFlip(matrix[0][0] == 0 && matrix[1][0] != 0);

						if (meta.get(0).getAxisIndex(Axes.X) == -1) meta.get(0)
							.setAxisLength(Axes.X, stream.readInt());
						if (meta.get(0).getAxisIndex(Axes.Y) == -1) meta.get(0)
							.setAxisLength(Axes.Y, stream.readInt());
					}
					else if (atomType.equals("cmov")) {
						stream.skipBytes(8);
						if ("zlib".equals(stream.readString(4))) {
							atomSize = stream.readInt();
							stream.skipBytes(4);
							stream.readInt(); // uncompressedSize

							final byte[] b = new byte[(int) (atomSize - 12)];
							stream.read(b);

							final CodecService codecService = meta.context().service(
								CodecService.class);
							final Codec codec = codecService.getCodec(ZlibCodec.class);
							final byte[] output = codec.decompress(b, null);

							// ensure data
							if (dataHandleService == null) {
								dataHandleService = meta.getContext().getService(
									DataHandleService.class);
							}
							try (DataHandle<Location> tmpStream = dataHandleService.create(
								new BytesLocation(output)))
							{
								parse(tmpStream, meta, 0, 0, output.length, log);
							}
						}
						else {
							throw new UnsupportedCompressionException(
								"Compressed header not supported.");
						}
					}
					else if (atomType.equals("stco")) {
						// we've found the plane offsets

						if (meta.getOffsets().size() > 0) break;
						meta.setSpork(false);
						stream.skipBytes(4);
						final int planeCount = (int) meta.get(0).getAxisLength(Axes.TIME);
						final int numPlanes = stream.readInt();
						if (numPlanes != planeCount) {
							stream.seek(stream.offset() - 4);
							int off = stream.readInt();
							meta.getOffsets().add(off);
							for (int i = 1; i < planeCount; i++) {
								if ((meta.getChunkSizes().isEmpty()) && (i < meta
									.getChunkSizes().size()))
								{
									meta.setRawSize(meta.getChunkSizes().get(i).intValue());
								}
								else i = planeCount;
								off += meta.getRawSize();
								meta.getOffsets().add(off);
							}
						}
						else {
							for (int i = 0; i < numPlanes; i++) {
								meta.getOffsets().add(stream.readInt());
							}
						}
					}
					else if (atomType.equals("stsd")) {
						// found video codec and pixel depth information

						stream.skipBytes(4);
						final int numEntries = stream.readInt();
						stream.skipBytes(4);

						for (int i = 0; i < numEntries; i++) {
							if (i == 0) {
								meta.setCodec(stream.readString(4));

								if (!meta.getCodec().equals("raw ") && !meta.getCodec().equals(
									"rle ") && !meta.getCodec().equals("rpza") && !meta.getCodec()
										.equals("mjpb") && !meta.getCodec().equals("jpeg"))
								{
									throw new UnsupportedCompressionException(
										"Unsupported codec: " + meta.getCodec());
								}

								stream.skipBytes(16);
								if (stream.readShort() == 0) {
									stream.skipBytes(56);

									meta.setBitsPerPixel(stream.readShort());
									if (meta.getCodec().equals("rpza")) meta.setBitsPerPixel(8);
									stream.skipBytes(10);
									meta.setInterlaced(stream.read() == 2);
									meta.getTable().put("Codec", meta.getCodec());
									meta.getTable().put("Bits per pixel", meta.getBitsPerPixel());
									stream.skipBytes(9);
								}
							}
							else {
								meta.setAltCodec(stream.readString(4));
								meta.getTable().put("Second codec", meta.getAltCodec());
							}
						}
					}
					else if (atomType.equals("stsz")) {
						// found the number of planes
						stream.skipBytes(4);
						meta.setRawSize(stream.readInt());
						meta.get(0).setAxisLength(Axes.TIME, stream.readInt());

						if (meta.getRawSize() == 0) {
							stream.seek(stream.offset() - 4);
							for (int b = 0; b < meta.get(0).getAxisLength(Axes.TIME); b++) {
								meta.getChunkSizes().add(stream.readInt());
							}
						}
					}
					else if (atomType.equals("stsc")) {
						stream.skipBytes(4);

						final int numChunks = stream.readInt();

						if (meta.getAltCodec() != null) {
							int prevChunk = 0;
							for (int i = 0; i < numChunks; i++) {
								final int chunk = stream.readInt();
								final int planesPerChunk = stream.readInt();
								final int id = stream.readInt();

								if (id == 2) meta.setAltPlanes(meta.getAltPlanes() +
									planesPerChunk * (chunk - prevChunk));

								prevChunk = chunk;
							}
						}
					}
					else if (atomType.equals("stts")) {
						stream.skipBytes(12);
						final int fps = stream.readInt();
						meta.getTable().put("Frames per second", fps);
					}
					if (oldpos + atomSize < stream.length()) {
						stream.seek(oldpos + atomSize);
					}
					else break;
				}

				if (atomSize == 0) offset = stream.length();
				else offset += atomSize;

				// if a 'udta' atom, skip ahead 4 bytes
				if (atomType.equals("udta")) offset += 4;
				print(depth, atomSize, atomType, log);
			}
		}

		/** Checks if the given String is a container atom type. */
		private static boolean isContainer(final String type) {
			for (final String CONTAINER_TYPE : CONTAINER_TYPES) {
				if (type.equals(CONTAINER_TYPE)) return true;
			}
			return false;
		}

		/** Debugging method; prints information on an atom. */
		private static void print(final int depth, final long size,
			final String type, final LogService log)
		{
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < depth; i++)
				sb.append(" ");
			sb.append(type + " : [" + size + "]");
			log.debug(sb.toString());
		}

		/** Uncompresses an image plane according to the the codec identifier. */
		private static byte[] uncompress(final byte[] pixs, final String code,
			final Metadata meta) throws FormatException
		{
			final CodecService codecService = meta.context().service(
				CodecService.class);
			final CodecOptions options = new MJPBCodecOptions();
			options.width = (int) meta.get(0).getAxisLength(Axes.X);
			options.height = (int) meta.get(0).getAxisLength(Axes.Y);
			options.bitsPerSample = meta.getBitsPerPixel();
			options.channels = meta.getBitsPerPixel() < 40 ? meta.getBitsPerPixel() /
				8 : (meta.getBitsPerPixel() - 32) / 8;
			options.previousImage = meta.isCanUsePrevious() ? meta.getPrevPixels()
				: null;
			options.littleEndian = meta.get(0).isLittleEndian();
			options.interleaved = meta.get(0).isMultichannel();

			final Codec codec;
			if (code.equals("raw ")) return pixs;
			else if (code.equals("rle ")) {
				codec = codecService.getCodec(QTRLECodec.class);
			}
			else if (code.equals("rpza")) {
				codec = codecService.getCodec(QTRLECodec.class);
			}
			else if (code.equals("mjpb")) {
				((MJPBCodecOptions) options).interlaced = meta.isInterlaced();
				codec = codecService.getCodec(MJPBCodec.class);
			}
			else if (code.equals("jpeg")) {
				codec = codecService.getCodec(JPEGCodec.class);
			}
			else {
				throw new UnsupportedCompressionException("Unsupported codec : " +
					code);
			}
			return codec.decompress(pixs, options);
		}

		/** Cut off header bytes from a resource fork file. */
		private static void stripHeader(final DataHandle<Location> stream)
			throws IOException
		{
			stream.seek(0);
			while (!stream.readString(4).equals("moov")) {
				stream.seek(stream.offset() - 2);
			}
			stream.seek(stream.offset() - 8);
		}

	}
}
