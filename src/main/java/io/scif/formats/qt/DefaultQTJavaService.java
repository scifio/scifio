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

import io.scif.FormatException;
import io.scif.MissingLibraryException;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ReflectException;
import org.scijava.util.ReflectedUniverse;

/**
 * Default service for working with QuickTime for Java.
 *
 * @author Curtis Rueden
 * @author Melissa Linkert
 */
@Plugin(type = Service.class)
public class DefaultQTJavaService extends AbstractService implements
	QTJavaService
{

	// -- Constants --

	private static final String NO_QT_MSG =
		"QuickTime for Java is required to read some QuickTime files. " +
			"Please install QuickTime for Java from http://www.apple.com/quicktime/";

	private static final String JVM_64BIT_MSG =
		"QuickTime for Java is not supported with a 64-bit JVM. " +
			"Please invoke the 32-bit JVM (-d32) to utilize QTJava functionality.";

	private static final String EXPIRED_QT_MSG =
		"Your version of QuickTime for Java has expired. " +
			"Please reinstall QuickTime for Java from http://www.apple.com/quicktime/";

	private static final boolean MAC_OS_X = System.getProperty("os.name").equals(
		"Mac OS X");

	// -- Static fields --

	/**
	 * This custom class loader searches additional paths for the QTJava.zip
	 * library. Java has a restriction where only one class loader can have a
	 * native library loaded within a JVM. So the class loader must be static,
	 * shared by all service instances, or else an UnsatisfiedLinkError is thrown
	 * when attempting to initialize QTJava multiple times.
	 */
	private static ClassLoader loader;

	// -- Fields --

	@Parameter
	private LogService log;

	/** Flag indicating this class has been initialized. */
	private boolean initialized = false;

	/** Flag indicating QuickTime for Java is not installed. */
	private boolean noQT = false;

	/** Flag indicating 64-bit JVM (does not support QTJava). */
	private boolean jvm64Bit = false;

	/** Flag indicating QuickTime for Java has expired. */
	private boolean expiredQT = false;

	/** Reflection tool for QuickTime for Java calls. */
	private ReflectedUniverse r;

	// -- LegacyQTService API methods --

	@Override
	public boolean canDoQT() {
		if (!initialized) initQTJava();
		return !noQT;
	}

	@Override
	public boolean isJVM64Bit() {
		if (!initialized) initQTJava();
		return jvm64Bit;
	}

	@Override
	public boolean isQTExpired() {
		if (!initialized) initQTJava();
		return expiredQT;
	}

	@Override
	public String getQTVersion() {
		if (isJVM64Bit()) return "Not available";
		else if (isQTExpired()) return "Expired";
		else if (!canDoQT()) return "Missing";
		else {
			try {
				final String qtMajor = r.exec("QTSession.getMajorVersion()").toString();
				final String qtMinor = r.exec("QTSession.getMinorVersion()").toString();
				return qtMajor + "." + qtMinor;
			}
			catch (final Throwable t) {
				log.debug("Could not retrieve QuickTime for Java version", t);
				return "Error";
			}
		}
	}

	@Override
	public ReflectedUniverse getUniverse() {
		if (!initialized) initQTJava();
		return r;
	}

	@Override
	public Dimension getPictDimensions(final byte[] bytes) throws FormatException,
		ReflectException
	{
		checkQTLibrary();
		try {
			r.exec("QTSession.open()");
			r.setVar("bytes", bytes);
			r.exec("pict = new Pict(bytes)");
			r.exec("box = pict.getPictFrame()");
			final int width = ((Integer) r.exec("box.getWidth()")).intValue();
			final int height = ((Integer) r.exec("box.getHeight()")).intValue();
			r.exec("QTSession.close()");
			return new Dimension(width, height);
		}
		catch (final ReflectException e) {
			r.exec("QTSession.close()");
			throw new FormatException("PICT height determination failed", e);
		}
	}

	@Override
	public synchronized Image pictToImage(final byte[] bytes)
		throws FormatException
	{
		checkQTLibrary();
		try {
			r.exec("QTSession.open()");

			// Code adapted from:
			// http://www.onjava.com/pub/a/onjava/2002/12/23/jmf.html?page=2
			r.setVar("bytes", bytes);
			r.exec("pict = new Pict(bytes)");
			r.exec("box = pict.getPictFrame()");
			final int width = ((Integer) r.exec("box.getWidth()")).intValue();
			final int height = ((Integer) r.exec("box.getHeight()")).intValue();
			// note: could get a RawEncodedImage from the Pict, but
			// apparently no way to get a PixMap from the REI
			r.exec("g = new QDGraphics(box)");
			r.exec("pict.draw(g, box)");
			// get data from the QDGraphics
			r.exec("pixMap = g.getPixMap()");
			r.exec("rei = pixMap.getPixelData()");

			// copy bytes to an array
			final int rowBytes = ((Integer) r.exec("pixMap.getRowBytes()"))
				.intValue();
			final int intsPerRow = rowBytes / 4;
			final int pixLen = intsPerRow * height;
			r.setVar("pixLen", pixLen);
			final int[] pixels = new int[pixLen];
			r.setVar("pixels", pixels);
			r.setVar("zero", new Integer(0));
			r.exec("rei.copyToArray(zero, pixels, zero, pixLen)");

			// now coax into image, ignoring alpha for speed
			final int bitsPerSample = 32;
			final int redMask = 0x00ff0000;
			final int greenMask = 0x0000ff00;
			final int blueMask = 0x000000ff;
			final int alphaMask = 0x00000000;
			final DirectColorModel colorModel = new DirectColorModel(bitsPerSample,
				redMask, greenMask, blueMask, alphaMask);

			r.exec("QTSession.close()");
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
				width, height, colorModel, pixels, 0, intsPerRow));
		}
		catch (final ReflectException e) {
			try {
				r.exec("QTSession.close()");
			}
			catch (final ReflectException exc) {
				log.info("Could not close QuickTime session", exc);
			}
			throw new FormatException("PICT extraction failed", e);
		}
	}

	@Override
	public void checkQTLibrary() throws MissingLibraryException {
		if (isJVM64Bit()) throw new MissingLibraryException(JVM_64BIT_MSG);
		if (isQTExpired()) throw new MissingLibraryException(EXPIRED_QT_MSG);
		if (!canDoQT()) throw new MissingLibraryException(NO_QT_MSG);
	}

	// -- Helper methods --

	/** Initializes the class. */
	private void initQTJava() {
		if (initialized) return;

		final String arch = System.getProperty("os.arch");
		if (arch != null && arch.contains("64")) {
			// QTJava is not supported on 64-bit Java; don't even try
			noQT = true;
			jvm64Bit = true;
			initialized = true;
			return;
		}

		boolean needClose = false;
		if (loader == null) loader = constructLoader();
		r = new ReflectedUniverse(loader);
		try {
			r.exec("import quicktime.QTSession");
			r.exec("QTSession.open()");
			needClose = true;

			// for LegacyQTReader and LegacyQTWriter
			r.exec("import quicktime.io.QTFile");
			r.exec("import quicktime.std.movies.Movie");

			// for LegacyQTReader
			r.exec("import quicktime.app.view.MoviePlayer");
			r.exec("import quicktime.app.view.QTImageProducer");
			r.exec("import quicktime.io.OpenMovieFile");
			r.exec("import quicktime.qd.QDDimension");
			r.exec("import quicktime.std.StdQTConstants");
			r.exec("import quicktime.std.movies.TimeInfo");
			r.exec("import quicktime.std.movies.Track");

			// for LegacyQTWriter
			r.exec("import quicktime.qd.QDGraphics");
			r.exec("import quicktime.qd.QDRect");
			r.exec("import quicktime.std.image.CSequence");
			r.exec("import quicktime.std.image.CodecComponent");
			r.exec("import quicktime.std.image.ImageDescription");
			r.exec("import quicktime.std.movies.media.VideoMedia");
			r.exec("import quicktime.util.QTHandle");
			r.exec("import quicktime.util.RawEncodedImage");
			r.exec("import quicktime.util.EndianOrder");
		}
		catch (final ExceptionInInitializerError err) {
			noQT = true;
			final Throwable t = err.getException();
			if (t instanceof SecurityException) {
				final SecurityException exc = (SecurityException) t;
				if (exc.getMessage().contains("expired")) expiredQT = true;
			}
		}
		catch (final Throwable t) {
			noQT = true;
			log.debug("Could not find QuickTime for Java", t);
		}
		finally {
			if (needClose) {
				try {
					r.exec("QTSession.close()");
				}
				catch (final Throwable t) {
					log.debug("Could not close QuickTime session", t);
				}
			}
			initialized = true;
		}
	}

	/** Loads the QTJava native library. */
	private ClassLoader constructLoader() {
		// set up additional QuickTime for Java paths
		URL[] paths = null;

		if (MAC_OS_X) {
			try {
				paths = new URL[] { new URL(
					"file:/System/Library/Java/Extensions/QTJava.zip") };
			}
			catch (final MalformedURLException exc) {
				log.info("", exc);
			}
			return paths == null ? null : new URLClassLoader(paths);
		}

		// case for Windows
		try {
			final String windir = System.getProperty("java.library.path");
			final StringTokenizer st = new StringTokenizer(windir, ";");

			while (st.hasMoreTokens()) {

				final File f = new File(st.nextToken(), "QTJava.zip");
				if (f.exists()) {
					try {
						paths = new URL[] { f.toURI().toURL() };
					}
					catch (final MalformedURLException exc) {
						log.info("", exc);
					}
					return paths == null ? null : new URLClassLoader(paths);
				}
			}
		}
		catch (final SecurityException e) {
			// this is common when using SCIFIO within an applet
			log.warn("Cannot read value of 'java.library.path'", e);
		}

		return null;
	}

}
