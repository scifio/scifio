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

package io.scif.formats.qt;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.BufferedImagePlane;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.common.DataTools;
import io.scif.common.ReflectException;
import io.scif.common.ReflectedUniverse;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.Vector;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * LegacyQTReader is a file format reader for QuickTime movie files. To use it,
 * QuickTime for Java must be installed. Much of this code was based on the
 * QuickTime Movie Opener for ImageJ (available at
 * http://rsb.info.nih.gov/ij/plugins/movie-opener.html).
 */
@Plugin(type = Format.class, priority = Priority.LOW_PRIORITY)
public class LegacyQTFormat extends AbstractFormat {

	// -- Format API methods --

	@Override
	public String getFormatName() {
		return "QuickTime";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "mov" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		public static final String CNAME =
			"io.scif.formats.LegacyQTFormat$Metadata";

		// -- Fields --

		/** Time offset for each frame. */
		protected int[] times;

		/** Image containing current frame. */
		protected Image image;

		// -- LegacyQTMetadata getters and setters --

		public int[] getTimes() {
			return times;
		}

		public void setTimes(final int[] times) {
			this.times = times;
		}

		public Image getImage() {
			return image;
		}

		public void setImage(final Image image) {
			this.image = image;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			final BufferedImage img = AWTImageTools.makeBuffered(image);
			final ImageMetadata iMeta = get(0);

			iMeta.setAxisTypes(Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME);
			iMeta.setAxisLength(Axes.X, img.getWidth());
			iMeta.setAxisLength(Axes.Y, img.getHeight());
			iMeta.setAxisLength(Axes.CHANNEL, img.getRaster().getNumBands());
			iMeta.setAxisLength(Axes.TIME, iMeta.getPlaneCount());

			iMeta.setPixelType(AWTImageTools.getPixelType(img));
			iMeta.setLittleEndian(false);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				times = null;
				image = null;
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			log().info("Checking for QuickTime Java");

			final ReflectedUniverse r = scifio().qtJava().getUniverse();
			scifio().qtJava().checkQTLibrary();

			log().info("Reading movie dimensions");
			try {
				r.exec("QTSession.open()");

				// open movie file
				final Location file = new Location(getContext(), stream.getFileName());
				r.setVar("path", file.getAbsolutePath());
				r.exec("qtf = new QTFile(path)");
				r.exec("openMovieFile = OpenMovieFile.asRead(qtf)");
				r.exec("m = Movie.fromFile(openMovieFile)");

				final int numTracks =
					((Integer) r.exec("m.getTrackCount()")).intValue();
				int trackMostLikely = 0;
				int trackNum = 0;
				while (++trackNum <= numTracks && trackMostLikely == 0) {
					r.setVar("trackNum", trackNum);
					r.exec("imageTrack = m.getTrack(trackNum)");
					r.exec("d = imageTrack.getSize()");
					final Integer w = (Integer) r.exec("d.getWidth()");
					if (w.intValue() > 0) trackMostLikely = trackNum;
				}

				r.setVar("trackMostLikely", trackMostLikely);
				r.exec("imageTrack = m.getTrack(trackMostLikely)");
				r.exec("d = imageTrack.getSize()");
				final Integer w = (Integer) r.exec("d.getWidth()");
				final Integer h = (Integer) r.exec("d.getHeight()");

				r.exec("moviePlayer = new MoviePlayer(m)");
				r.setVar("dim", new Dimension(w.intValue(), h.intValue()));
				final ImageProducer qtip =
					(ImageProducer) r
						.exec("qtip = new QTImageProducer(moviePlayer, dim)");
				meta.setImage(Toolkit.getDefaultToolkit().createImage(qtip));

				r.setVar("zero", 0);
				r.setVar("one", 1f);
				r.exec("timeInfo = new TimeInfo(zero, zero)");
				r.exec("moviePlayer.setTime(zero)");
				final Vector<Integer> v = new Vector<Integer>();
				int time = 0;
				Integer q = new Integer(time);
				do {
					v.add(q);
					r.exec("timeInfo = imageTrack.getNextInterestingTime("
						+ "StdQTConstants.nextTimeMediaSample, timeInfo.time, one)");
					q = (Integer) r.getVar("timeInfo.time");
					time = q.intValue();
				}
				while (time >= 0);

				meta.createImageMetadata(1);
				final ImageMetadata iMeta = meta.get(0);

				iMeta.setAxisLength(Axes.TIME, v.size());

				final int[] times = new int[iMeta.getPlaneCount()];
				for (int i = 0; i < times.length; i++) {
					q = v.elementAt(i);
					times[i] = q.intValue();
				}

				meta.setTimes(times);
			}
			catch (final ReflectException e) {
				throw new FormatException("Open movie failed", e);
			}
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
			final ReflectedUniverse r = scifio().qtJava().getUniverse();
			final Metadata meta = getMetadata();

			// paint frame into image
			try {
				r.setVar("time", meta.getTimes()[planeIndex]);
				r.exec("moviePlayer.setTime(time)");
				r.exec("qtip.redraw(null)");
				r.exec("qtip.updateConsumers(null)");
			}
			catch (final ReflectException re) {
				throw new FormatException("Open movie failed", re);
			}
			final BufferedImage bimg =
				AWTImageTools.getSubimage(AWTImageTools.makeBuffered(meta.getImage()),
					meta.isLittleEndian(imageIndex), planeMin, planeMax);

			plane.populate(meta.get(imageIndex), bimg, planeMin, planeMax);
			return plane;
		}
	}

	public void close(final boolean fileOnly) throws IOException {
		try {
			final ReflectedUniverse r = scifio().qtJava().getUniverse();

			if (r != null && r.getVar("openMovieFile") != null) {
				r.exec("openMovieFile.close()");
				if (!fileOnly) {
					r.exec("m.disposeQTObject()");
					r.exec("imageTrack.disposeQTObject()");
					r.exec("QTSession.close()");
				}
			}
		}
		catch (final ReflectException e) {
			log().debug("Failed to close QuickTime session", e);
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constants --

		/** Time scale. */
		private static final int TIME_SCALE = 600;

		// -- Fields --

		/** Reflection tool for QuickTime for Java calls. */
		protected ReflectedUniverse r;

		/** The codec to use. */
		protected int codec = NativeQTFormat.Writer.CODEC_RAW;

		/** The quality to use. */
		protected int quality = NativeQTFormat.Writer.QUALITY_NORMAL;

		/** Frame width. */
		private int width;

		/** Frame height. */
		private int height;

		private int[] pixels2 = null;

		// -- LegacyQTWriter API methods --

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

		// -- Writer API Methods --

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			BufferedImage img = null;
			final Metadata meta = getMetadata();

			if (!(plane instanceof BufferedImagePlane)) {
				final int type = meta.getPixelType(imageIndex);
				img =
					AWTImageTools.makeImage(plane.getBytes(), (int) meta.getAxisLength(
						imageIndex, Axes.X), (int) meta.getAxisLength(imageIndex, Axes.Y),
						(int) meta.getAxisLength(imageIndex, Axes.CHANNEL), meta
							.isInterleaved(imageIndex), FormatTools.getBytesPerPixel(type),
						FormatTools.isFloatingPoint(type), meta.isLittleEndian(imageIndex),
						FormatTools.isSigned(type));
			}
			else {
				img = ((BufferedImagePlane) plane).getData();
			}

			if (r == null) {
				r = scifio().qtJava().getUniverse();
			}
			scifio().qtJava().checkQTLibrary();

			if (!initialized[imageIndex][planeIndex]) {
				initialized[imageIndex][planeIndex] = true;

				try {
					r.exec("QTSession.open()");
					width = img.getWidth();
					height = img.getHeight();
					r.setVar("path", getMetadata().getDatasetName());
					r.setVar("width", (float) width);
					r.setVar("height", (float) height);

					r.exec("movFile = new QTFile(path)");
					r.exec("kMoviePlayer = StdQTConstants.kMoviePlayer");
					final int resFlag =
						((Integer) r
							.exec("StdQTConstants.createMovieFileDontCreateResFile"))
							.intValue();
					r.setVar("flags", resFlag);
					r.exec("movie = Movie.createMovieFile(movFile, kMoviePlayer, flags)");
					r.setVar("timeScale", TIME_SCALE);
					r.setVar("zero", 0);
					r.setVar("zeroFloat", (float) 0);
					r.exec("videoTrack = movie.addTrack(width, height, zeroFloat)");
					r.exec("videoMedia = new VideoMedia(videoTrack, timeScale)");
					r.exec("videoMedia.beginEdits()");

					r.setVar("width", width);
					r.setVar("height", height);
					r.exec("bounds = new QDRect(zero, zero, width, height)");
					r.exec("gw = new QDGraphics(bounds)");

					r.exec("pixMap = gw.getPixMap()");
					r.exec("pixSize = pixMap.getPixelSize()");
					r.setVar("codec", codec);
					r.setVar("quality", quality);

					final int rawImageSize = width * height * 4;
					r.setVar("rawImageSize", rawImageSize);

					r.setVar("boolTrue", true);
					r.exec("imageHandle = new QTHandle(rawImageSize, boolTrue)");
					r.exec("imageHandle.lock()");
					r.exec("compressedImage = RawEncodedImage.fromQTHandle(imageHandle)");

					r.setVar("rate", 30);

					r.exec("seq = new CSequence(gw, bounds, pixSize, codec, "
						+ "CodecComponent.bestFidelityCodec, quality, quality, rate, null, "
						+ "zero)");

					r.exec("imgDesc = seq.getDescription()");
				}
				catch (final ReflectException e) {
					log().debug("", e);
					throw new FormatException("Legacy QuickTime writer failed", e);
				}
			}

			try {
				r.exec("pixelData = pixMap.getPixelData()");

				r.exec("intsPerRow = pixelData.getRowBytes()");
				final int intsPerRow =
					((Integer) r.getVar("intsPerRow")).intValue() / 4;

				final byte[][] px = AWTImageTools.getBytes(img);

				final int[] pixels = new int[px[0].length];
				for (int i = 0; i < pixels.length; i++) {
					final byte[] b = new byte[4];
					for (int j = 0; j < px.length; j++) {
						b[j] = px[j][i];
					}
					for (int j = px.length; j < 4; j++) {
						b[j] = px[j % px.length][i];
					}
					pixels[i] = DataTools.bytesToInt(b, true);
				}

				if (pixels2 == null) pixels2 = new int[intsPerRow * height];
				r.exec("nativeLittle = EndianOrder.isNativeLittleEndian()");
				final boolean nativeLittle =
					((Boolean) r.getVar("nativeLittle")).booleanValue();
				if (nativeLittle) {
					int offset1, offset2;
					for (int row = 0; row < height; row++) {
						offset1 = row * width;
						offset2 = row * intsPerRow;
						for (int col = 0; col < width; col++) {
							r.setVar("thisByte", pixels[offset1++]);
							r.exec("b = EndianOrder.flipBigEndianToNative32(thisByte)");
							pixels2[offset2++] = ((Integer) r.getVar("b")).intValue();
						}
					}
				}
				else {
					for (int i = 0; i < height; i++) {
						System.arraycopy(pixels, i * width, pixels2, i * intsPerRow, width);
					}
				}

				r.setVar("pixels2", pixels2);
				r.setVar("len", intsPerRow * height);

				r.exec("pixelData.copyFromArray(zero, pixels2, zero, len)");
				r.exec("flags = StdQTConstants.codecFlagUpdatePrevious");
				r.exec("cfInfo = seq.compressFrame(gw, bounds, flags, compressedImage)");

				// see developer.apple.com/qa/qtmcc/qtmcc20.html
				r.exec("similarity = cfInfo.getSimilarity()");
				final int sim = ((Integer) r.getVar("similarity")).intValue();
				final boolean sync = sim == 0;
				r.exec("dataSize = cfInfo.getDataSize()");
				r.setVar("fps", fps);
				r.setVar("frameRate", TIME_SCALE);
				r.setVar("rate", TIME_SCALE / fps);

				if (sync) {
					r.setVar("sync", 0);
				}
				else r.exec("sync = StdQTConstants.mediaSampleNotSync");
				r.setVar("one", 1);
				r.exec("videoMedia.addSample(imageHandle, zero, dataSize, "
					+ "rate, imgDesc, one, sync)");
			}
			catch (final ReflectException e) {
				log().debug("", e);
				throw new FormatException("Legacy QuickTime writer failed", e);
			}

			if (planeIndex == getMetadata().get(imageIndex).getPlaneCount() - 1) {
				try {
					r.exec("videoMedia.endEdits()");
					r.exec("duration = videoMedia.getDuration()");
					r.setVar("floatOne", (float) 1.0);
					r.exec("videoTrack.insertMedia(zero, zero, duration, floatOne)");
					r.exec("omf = OpenMovieFile.asWrite(movFile)");
					r.exec("name = movFile.getName()");
					r.exec("flags = StdQTConstants.movieInDataForkResID");
					r.exec("movie.addResource(omf, flags, name)");
					r.exec("QTSession.close()");
				}
				catch (final ReflectException e) {
					log().debug("", e);
					throw new FormatException("Legacy QuickTime writer failed", e);
				}
				close();
			}
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public void close() throws IOException {
			super.close();
			r = null;
			width = 0;
			height = 0;
			pixels2 = null;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	@Plugin(type = Translator.class, attrs = {
		@Attr(name = LegacyQTTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = LegacyQTTranslator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class LegacyQTTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			dest.createImageMetadata(1);
			dest.get(0).setAxisLength(Axes.TIME, source.getPlaneCount(0));

			final int w = (int) source.getAxisLength(0, Axes.X);
			final int h = (int) source.getAxisLength(0, Axes.Y);
			final int bpp = source.getBitsPerPixel(0) / 8;
			final byte[][] data =
				new byte[(int) source.getAxisLength(0, Axes.CHANNEL)][w * h * bpp];
			final boolean fp = FormatTools.isFloatingPoint(source.getPixelType(0));
			final boolean little = source.isLittleEndian(0);
			final boolean signed = FormatTools.isSigned(source.getPixelType(0));

			final Image img =
				AWTImageTools.makeImage(data, w, h, bpp, fp, little, signed);

			dest.setImage(img);
		}
	}

}
