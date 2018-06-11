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

package io.scif.img;

import io.scif.Reader;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.refs.RefManagerService;

import java.util.List;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
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
	 * @see ImgOpener#openImgs(String)
	 */
	public static List<SCIFIOImgPlus<?>> openImgs(final String source) {
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<?>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImgs(String)} with a guaranteed {@link FloatType} .
	 */
	public static List<SCIFIOImgPlus<FloatType>> openFloatImgs(
		final String source)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<FloatType>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, new FloatType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImgs(String)} with a guaranteed {@link DoubleType}.
	 */
	public static List<SCIFIOImgPlus<DoubleType>> openDoubleImgs(
		final String source)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<DoubleType>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, new DoubleType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * As {@link ImgOpener#openImgs(String)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static List<SCIFIOImgPlus<UnsignedByteType>> openUnsignedByteImgs(
		final String source)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<UnsignedByteType>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, new UnsignedByteType());
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(String, RealType)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, type);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(String, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>> openImgs(final String source,
		final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<?>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(String, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, type, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(String, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, imgFactory, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(String, ImgFactory, RealType)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(source, imgFactory);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(source, e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(Reader, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, type, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getDatasetName(), e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(Reader, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, imgFactory, config);
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
	 * @see ImgSaver#saveImg(String, SCIFIOImgPlus, int)
	 */
	public static void saveImg(final String dest, final SCIFIOImgPlus<?> imgPlus,
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
	 * @see ImgSaver#saveImg(Writer, SCIFIOImgPlus, int)
	 */
	public static void saveImg(final Writer writer,
		final SCIFIOImgPlus<?> imgPlus, final int imageIndex)
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
	private static void register(final List<? extends SCIFIOImgPlus<?>> imgPlus,
		final AbstractImgIOComponent component)
	{
		final Context ctx = component.getContext();
		final RefManagerService refManagerService = ctx.getService(
			RefManagerService.class);
		for (final SCIFIOImgPlus<?> img : imgPlus) {
			refManagerService.manage(img, ctx);
		}
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
	 * @param dest - Destination that failed to open
	 * @param e - Exception to log
	 */
	private static void saveError(final String dest, final Exception e) {
		logService.error("Failed to save ImgPlus to id: " + dest, e);
	}

	// -- Deprecated Input Methods --

	/**
	 * @deprecated
	 * @see #openImgs(String)
	 */
	@Deprecated
	public static SCIFIOImgPlus<?> open(final String source) {
		return openImgs(source).get(0);
	}

	/**
	 * @deprecated
	 * @see #openFloatImgs(String)
	 */
	@Deprecated
	public static SCIFIOImgPlus<FloatType> openFloat(final String source) {
		return openFloatImgs(source).get(0);
	}

	/**
	 * @deprecated
	 * @see #openDoubleImgs(String)
	 */
	@Deprecated
	public static SCIFIOImgPlus<DoubleType> openDouble(final String source) {
		return openDoubleImgs(source).get(0);
	}

	/**
	 * @deprecated
	 * @see #openUnsignedByteImgs(String)
	 */
	@Deprecated
	public static SCIFIOImgPlus<UnsignedByteType> openUnsignedByte(
		final String source)
	{
		return openUnsignedByteImgs(source).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImgs(String, ImgFactory)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return openImgs(source, imgFactory.imgFactory(type));
	}

	/**
	 * @deprecated
	 * @see #openImgs(Reader, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openImgs(reader, imgFactory.imgFactory(type), config);
	}

	/**
	 * @deprecated
	 * @see #openImgs(String, RealType)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final T type)
	{
		return openImgs(source, type).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImgs(String, SCIFIOConfig)
	 */
	@Deprecated
	public static SCIFIOImgPlus<?> openImg(final String source,
		final SCIFIOConfig config)
	{
		return openImgs(source, config).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImgs(String, RealType, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final T type, final SCIFIOConfig config)
	{
		return openImgs(source, type, config).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImg(String, ImgFactory)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory)
	{
		return openImgs(source, imgFactory).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImg(String, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openImgs(source, imgFactory, config).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImg(String, ImgFactory, RealType)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return openImgs(source, imgFactory, type).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImgs(Reader, RealType, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final Reader reader, final T type, final SCIFIOConfig config)
	{
		return openImgs(reader, type, config).get(0);
	}

	/**
	 * @deprecated
	 * @see #openImg(Reader, RealType, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openImgs(reader, type, imgFactory, config).get(0);
	}
}
