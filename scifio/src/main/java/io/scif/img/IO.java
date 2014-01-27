/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
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
 * #L%
 */

package io.scif.img;

import io.scif.Reader;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.refs.RefManagerService;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * A static utility class for easy access to {@link ImgSaver} and
 * {@link ImgOpener} methods. Also includes type-convenience methods for quickly
 * opening various image types.
 * <p>
 * NB: No exceptions are thrown by any methods in this class. Instead they are
 * all caught, logged, and null is returned.
 * </p>
 * <p>
 * NB: Each of these methods occurs in its own {@link Context}
 * </p>
 * 
 * @author Mark Hiner
 */
public final class IO {

	// -- Fields --

	private static LogService logService;

	// -- Input Methods --

	/**
	 * @see ImgOpener#openImg(String)
	 */
	public static ImgPlus<?> open(final String source) {
		final ImgOpener opener = opener();
		ImgPlus<?> imgPlus = null;
		try {
			imgPlus = opener.openImg(source);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed {@link FloatType}.
	 */
	public static ImgPlus<FloatType> openFloat(final String source) {
		final ImgOpener opener = opener();
		ImgPlus<FloatType> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, new FloatType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed {@link DoubleType}.
	 */
	public static ImgPlus<DoubleType> openDouble(final String source) {
		final ImgOpener opener = opener();
		ImgPlus<DoubleType> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, new DoubleType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static ImgPlus<UnsignedByteType> openUnsignedByte(final String source)
	{
		final ImgOpener opener = opener();
		ImgPlus<UnsignedByteType> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, new UnsignedByteType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, RealType)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type)
	{
		final ImgOpener opener = opener();
		ImgPlus<T> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, type);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, SCIFIOConfig)
	 */
	public static ImgPlus<?> openImg(final String source,
		final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		ImgPlus<?> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		ImgPlus<T> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, type, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static ImgPlus<?> openImg(final String source,
		final ImgFactory imgFactory)
	{
		final ImgOpener opener = opener();
		ImgPlus<?> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, imgFactory);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, ImgFactory, SCIFIOConfig)
	 */
	@SuppressWarnings("rawtypes")
	public static ImgPlus<?> openImg(final String source,
		final ImgFactory imgFactory, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		ImgPlus<?> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, imgFactory, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(String, ImgFactory, RealType)
	 */
	public static <T extends RealType<T>> ImgPlus<T> openImg(final String source,
		final ImgFactory<T> imgFactory, final T type)
	{
		final ImgOpener opener = opener();
		ImgPlus<T> imgPlus = null;
		try {
			imgPlus = opener.openImg(source, imgFactory, type);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(Reader, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final Reader reader, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		ImgPlus<T> imgPlus = null;
		try {
			imgPlus = opener.openImg(reader, type, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getDatasetName(), e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImg(Reader, RealType, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T>> ImgPlus<T> openImg(final Reader reader,
		final T type, final ImgFactory<T> imgFactory, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		ImgPlus<T> imgPlus = null;
		try {
			imgPlus = opener.openImg(reader, type, imgFactory, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getDatasetName(), e);
		}
		return imgPlus;
	}

	// -- Output Methods --

	/**
	 * @see ImgSaver#saveImg(String, Img)
	 */
	public static void saveImg(final String dest, final Img<?> img) {
		try {
			new ImgSaver().saveImg(dest, img);
		}
		catch (final IncompatibleTypeException e) {
			saveError(dest, e);
		}
		catch (final ImgIOException e) {
			saveError(dest, e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(String, ImgPlus, int)
	 */
	public static void saveImg(final String dest, final ImgPlus<?> imgPlus,
		final int imageIndex)
	{
		try {
			new ImgSaver().saveImg(dest, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			saveError(dest, e);
		}
		catch (final ImgIOException e) {
			saveError(dest, e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(Writer, Img)
	 */
	public static void saveImg(final Writer writer, final Img<?> img) {
		try {
			new ImgSaver().saveImg(writer, img);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDatasetName(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDatasetName(), e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(Writer, ImgPlus, int)
	 */
	public static void saveImg(final Writer writer, final ImgPlus<?> imgPlus,
		final int imageIndex)
	{
		try {
			new ImgSaver().saveImg(writer, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDatasetName(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDatasetName(), e);
		}
	}

	// -- Helper methods --

	/**
	 * Registers the given ImgPlus with the RefManagerService in the provided
	 * component's Context.
	 */
	private static void register(final ImgPlus<?> imgPlus,
		final AbstractImgIOComponent component)
	{
		final Context ctx = component.getContext();
		final RefManagerService refManagerService =
			ctx.getService(RefManagerService.class);
		refManagerService.manage(imgPlus, ctx);
	}

	/**
	 * Creates a new ImgOpener. The first time this method runs, an additional
	 * Context is created and the logService is cached for future logging.
	 * 
	 * @return A new ImgOpener instance with its own Context
	 */
	private static ImgOpener opener() {
		final ImgOpener opener = new ImgOpener();
		if (logService == null) {
			synchronized (IO.class) {
				if (logService == null) {
					logService = opener.getContext().getService(LogService.class);
				}
			}
		}
		return opener;
	}

	/**
	 * @param source - Source that failed to open
	 * @param e - Exception to log
	 */
	private static void openError(final String source, final Exception e) {
		logService.error("Failed to open ImgPlus for source: " + source, e);
	}

	/**
	 * @param source - Source that failed to open
	 * @param e - Exception to log
	 */
	private static void saveError(final String dest, final Exception e) {
		logService.error("Failed to save ImgPlus to id: " + dest, e);
	}
}
