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
import io.scif.config.SCIFIOConfig;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.util.FormatTools;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import net.imagej.axis.Axes;
import net.imglib2.Interval;

import org.scijava.Priority;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;
import org.scijava.util.ReflectException;
import org.scijava.util.ReflectedUniverse;

/**
 * LegacyQTReader is a file format reader for QuickTime movie files. To use it,
 * QuickTime for Java must be installed. Much of this code was based on the
 * <a href="http://imagej.net/plugins/movie-opener.html">QuickTime Movie Opener
 * for ImageJ</a>.
 */
@Plugin(type = Format.class, name = "QuickTime", priority = Priority.LOW)
public class LegacyQTFormat extends AbstractFormat {

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "mov" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		@Parameter
		private QTJavaService qtJavaService;

		/** Time offset for each frame. */
		private int[] times;

		/** Image containing current frame. */
		private Image image;

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

	public static class Parser extends AbstractParser<Metadata> {

		@Parameter
		private QTJavaService qtJavaService;

		// -- Parser API Methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			log().info("Checking for QuickTime Java");

			final ReflectedUniverse r = qtJavaService.getUniverse();
			qtJavaService.checkQTLibrary();

			log().info("Reading movie dimensions");
			try {
				r.exec("QTSession.open()");

				// open movie file
				final Location file = stream.get();
				if (!(file instanceof FileLocation)) {
					throw new IOException(
						"Legacy QTFormat can only read from local disks!");
				}
				r.setVar("path", ((FileLocation) file).getFile().getAbsolutePath());
				r.exec("qtf = new QTFile(path)");
				r.exec("openMovieFile = OpenMovieFile.asRead(qtf)");
				r.exec("m = Movie.fromFile(openMovieFile)");

				final int numTracks = ((Integer) r.exec("m.getTrackCount()"))
					.intValue();
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
				final ImageProducer qtip = (ImageProducer) r.exec(
					"qtip = new QTImageProducer(moviePlayer, dim)");
				meta.setImage(Toolkit.getDefaultToolkit().createImage(qtip));

				r.setVar("zero", 0);
				r.setVar("one", 1f);
				r.exec("timeInfo = new TimeInfo(zero, zero)");
				r.exec("moviePlayer.setTime(zero)");
				final Vector<Integer> v = new Vector<>();
				int time = 0;
				Integer q = new Integer(time);
				do {
					v.add(q);
					r.exec("timeInfo = imageTrack.getNextInterestingTime(" +
						"StdQTConstants.nextTimeMediaSample, timeInfo.time, one)");
					q = (Integer) r.getVar("timeInfo.time");
					time = q.intValue();
				}
				while (time >= 0);

				meta.createImageMetadata(1);
				final ImageMetadata iMeta = meta.get(0);

				iMeta.setAxisLength(Axes.TIME, v.size());

				final int[] times = new int[(int) iMeta.getPlaneCount()];
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

	public static class Reader extends BufferedImageReader<Metadata> {

		@Parameter
		private QTJavaService qtJavaService;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public BufferedImagePlane openPlane(final int imageIndex,
			final long planeIndex, final BufferedImagePlane plane,
			final Interval bounds, final SCIFIOConfig config) throws FormatException,
			IOException
		{
			final ReflectedUniverse r = qtJavaService.getUniverse();
			final Metadata meta = getMetadata();

			// paint frame into image
			try {
				r.setVar("time", meta.getTimes()[(int) planeIndex]);
				r.exec("moviePlayer.setTime(time)");
				r.exec("qtip.redraw(null)");
				r.exec("qtip.updateConsumers(null)");
			}
			catch (final ReflectException re) {
				throw new FormatException("Open movie failed", re);
			}
			final BufferedImage bimg = AWTImageTools.getSubimage(AWTImageTools
				.makeBuffered(meta.getImage()), meta.get(imageIndex).isLittleEndian(),
				bounds);

			plane.populate(meta.get(imageIndex), bimg, bounds);
			return plane;
		}

		@Override
		public void close(final boolean fileOnly) {
			try {
				final ReflectedUniverse r = qtJavaService.getUniverse();

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
	}

	public static class Writer extends AbstractWriter<Metadata> {

		// -- Constants --

		/** Time scale. */
		private static final int TIME_SCALE = 600;

		// -- Fields --

		@Parameter
		private QTJavaService qtJavaService;

		/** Reflection tool for QuickTime for Java calls. */
		private ReflectedUniverse r;

		/** The codec to use. */
		private int codec = NativeQTFormat.Writer.CODEC_RAW;

		/** The quality to use. */
		private int quality = NativeQTFormat.Writer.QUALITY_NORMAL;

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

		// -- AbstractWriter Methods --

		@Override
		protected String[] makeCompressionTypes() {
			return new String[0];
		}

		@Override
		protected void initialize(final int imageIndex, final long planeIndex,
			final Interval bounds) throws FormatException, IOException
		{
			if (!isInitialized(imageIndex, (int) planeIndex)) {
				if (r == null) {
					r = qtJavaService.getUniverse();
				}
				try {
					r.exec("QTSession.open()");
					width = (int) getMetadata().get(imageIndex).getAxisLength(Axes.X);
					height = (int) getMetadata().get(imageIndex).getAxisLength(Axes.Y);
					r.setVar("path", getMetadata().getDatasetName());
					r.setVar("width", (float) width);
					r.setVar("height", (float) height);

					r.exec("movFile = new QTFile(path)");
					r.exec("kMoviePlayer = StdQTConstants.kMoviePlayer");
					final int resFlag = ((Integer) r.exec(
						"StdQTConstants.createMovieFileDontCreateResFile")).intValue();
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

					r.exec("seq = new CSequence(gw, bounds, pixSize, codec, " +
						"CodecComponent.bestFidelityCodec, quality, quality, rate, null, " +
						"zero)");

					r.exec("imgDesc = seq.getDescription()");
				}
				catch (final ReflectException e) {
					log().debug("", e);
					throw new FormatException("Legacy QuickTime writer failed", e);
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
			BufferedImage img = null;
			final Metadata meta = getMetadata();

			if (!(plane instanceof BufferedImagePlane)) {
				final int type = meta.get(imageIndex).getPixelType();
				img = AWTImageTools.makeImage(plane.getBytes(), (int) meta.get(
					imageIndex).getAxisLength(Axes.X), (int) meta.get(imageIndex)
						.getAxisLength(Axes.Y), (int) meta.get(imageIndex).getAxisLength(
							Axes.CHANNEL), meta.get(imageIndex).getInterleavedAxisCount() > 0,
					FormatTools.getBytesPerPixel(type), FormatTools.isFloatingPoint(type),
					meta.get(imageIndex).isLittleEndian(), FormatTools.isSigned(type));
			}
			else {
				img = ((BufferedImagePlane) plane).getData();
			}

			if (r == null) {
				r = qtJavaService.getUniverse();
			}
			qtJavaService.checkQTLibrary();

			try {
				r.exec("pixelData = pixMap.getPixelData()");

				r.exec("intsPerRow = pixelData.getRowBytes()");
				final int intsPerRow = ((Integer) r.getVar("intsPerRow")).intValue() /
					4;

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
					pixels[i] = Bytes.toInt(b, true);
				}

				if (pixels2 == null) pixels2 = new int[intsPerRow * height];
				r.exec("nativeLittle = EndianOrder.isNativeLittleEndian()");
				final boolean nativeLittle = ((Boolean) r.getVar("nativeLittle"))
					.booleanValue();
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
				r.exec(
					"cfInfo = seq.compressFrame(gw, bounds, flags, compressedImage)");

				// see developer.apple.com/qa/qtmcc/qtmcc20.html
				r.exec("similarity = cfInfo.getSimilarity()");
				final int sim = ((Integer) r.getVar("similarity")).intValue();
				final boolean sync = sim == 0;
				r.exec("dataSize = cfInfo.getDataSize()");
				r.setVar("fps", getFramesPerSecond());
				r.setVar("frameRate", TIME_SCALE);
				r.setVar("rate", TIME_SCALE / getFramesPerSecond());

				if (sync) {
					r.setVar("sync", 0);
				}
				else r.exec("sync = StdQTConstants.mediaSampleNotSync");
				r.setVar("one", 1);
				r.exec("videoMedia.addSample(imageHandle, zero, dataSize, " +
					"rate, imgDesc, one, sync)");
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

	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class LegacyQTTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		@Parameter
		private QTJavaService qtJavaService;

		@Parameter
		private LogService log;

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
			dest.get(0).setAxisLength(Axes.TIME, source.get(0).getPlaneCount());

			final int w = (int) source.get(0).getAxisLength(Axes.X);
			final int h = (int) source.get(0).getAxisLength(Axes.Y);
			final int bpp = source.get(0).getBitsPerPixel() / 8;
			final byte[][] data = new byte[(int) source.get(0).getAxisLength(
				Axes.CHANNEL)][w * h * bpp];
			final boolean fp = FormatTools.isFloatingPoint(source.get(0)
				.getPixelType());
			final boolean little = source.get(0).isLittleEndian();
			final boolean signed = FormatTools.isSigned(source.get(0).getPixelType());

			final Image img = AWTImageTools.makeImage(data, w, h, bpp, fp, little,
				signed);

			dest.setImage(img);
		}
	}

}
