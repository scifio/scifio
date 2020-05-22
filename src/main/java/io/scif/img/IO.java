/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2019 SCIFIO developers.
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
import io.scif.config.SCIFIOConfig;
import io.scif.refs.RefManagerService;

import java.net.URISyntaxException;
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
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.log.LogService;

import static org.scijava.util.ListUtils.first;

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
	 * @see ImgOpener#openImgs(Location)
	 */
	public static SCIFIOImgPlus<?> open(final Location source) {
		return first(openAll(source));
	}

	/**
	 * @see #open(Location)
	 */
	public static SCIFIOImgPlus<?> open(final String source) {
		return first(openAll(resolve(source)));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed {@link FloatType}
	 * .
	 */
	public static SCIFIOImgPlus<FloatType> openFloat(
			final Location source)
	{
		return first(openAllFloat(source));
	}

	/**
	 * @see #openFloat(Location)
	 */
	public static SCIFIOImgPlus<FloatType> openFloat(final String source) {
		return first(openAllFloat(source));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link DoubleType}.
	 */
	public static SCIFIOImgPlus<DoubleType> openDouble(
			final Location source)
	{
		return first(openAllDouble(source));
	}

	/**
	 * @see #openAllDouble(Location)
	 */
	public static SCIFIOImgPlus<DoubleType> openDouble(
			final String source)
	{
		return first(openAllDouble(source));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static SCIFIOImgPlus<UnsignedByteType> openUnsignedByte(
			final Location source)
	{
		return first(openAllUnsignedByte(source));
	}

	/**
	 * @see #openUnsignedByte(Location)
	 */
	public static SCIFIOImgPlus<UnsignedByteType> openUnsignedByte(
			final String source)
	{
		return first(openAllUnsignedByte(source));
	}

	/**
	 * @see ImgOpener#openImgs(Location)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final T type)
	{
		return first(openAll(source, type));
	}

	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final T type)
	{
		return first(openAll(source, type));
	}

	/**
	 * @see #open(Location, SCIFIOConfig)
	 */
	public static SCIFIOImgPlus<?> open(final String source,
	                                    final SCIFIOConfig config)
	{
		return open(resolve(source), config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static SCIFIOImgPlus<?> open(final Location source,
	                                    final SCIFIOConfig config)
	{
		return first(openAll(source, config));
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final T type, final SCIFIOConfig config)
	{
		return first(openAll(source, type, config));
	}

	/**
	 * @see #open(Location, T, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final T type, final SCIFIOConfig config)
	{
		return open(resolve(source), type, config);
	}

	/**
	 * @see #open(Location, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final ImgFactory imgFactory)
	{
		return open(resolve(source), imgFactory);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final ImgFactory imgFactory)
	{
		return first(openAll(source, imgFactory));
	}

	/**
	 * @see #open(Location, ImgFactory, SCIFIOConfig)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final ImgFactory imgFactory,
	     final SCIFIOConfig config)
	{
		return open(resolve(source), imgFactory, config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory, SCIFIOConfig)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final ImgFactory imgFactory,
	     final SCIFIOConfig config)
	{
		return first(openAll(source, imgFactory, config));
	}

	/**
	 * @see #open(Location, ImgFactory, T)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return open(resolve(source), imgFactory, type);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final ImgFactory<T> imgFactory, final T type)
	{
		return first(openAll(source, imgFactory, type));
	}

	/**
	 * @see ImgOpener#openImgs(Reader, SCIFIOConfig)
	 */
	public static SCIFIOImgPlus<?> open(final Reader reader, final SCIFIOConfig config)
	{
		return first(openAll(reader, config));
	}

	/**
	 * @see ImgOpener#openImgs(Reader, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Reader reader, final T type, final SCIFIOConfig config)
	{
		return first(openAll(reader, type, config));
	}

	/**
	 * @see ImgOpener#openImgs(Reader, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T>> SCIFIOImgPlus<T> open(
			final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return first(openAll(reader, type, imgFactory, config));
	}

	/**
	 * @see ImgOpener#openImgs(Location)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final Location source) {
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
	 * @see #openAll(Location)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final String source) {
		return openAll(resolve(source));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed {@link FloatType}
	 * .
	 */
	public static List<SCIFIOImgPlus<FloatType>> openAllFloat(
		final Location source)
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
	 * @see #openAllFloat(Location)
	 */
	public static List<SCIFIOImgPlus<FloatType>> openAllFloat(final String source) {
		return openAllFloat(resolve(source));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link DoubleType}.
	 */
	public static List<SCIFIOImgPlus<DoubleType>> openAllDouble(
		final Location source)
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
	 * @see #openAllDouble(Location)
	 */
	public static List<SCIFIOImgPlus<DoubleType>> openAllDouble(
		final String source)
	{
		return openAllDouble(resolve(source));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static List<SCIFIOImgPlus<UnsignedByteType>> openAllUnsignedByte(
		final Location source)
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

	public static List<SCIFIOImgPlus<UnsignedByteType>> openAllUnsignedByte(
		final String source)
	{
		return openAllUnsignedByte(resolve(source));
	}

	/**
	 * @see ImgOpener#openImgs(Location)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final T type)
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

	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final String source, final T type)
	{
		return openAll(resolve(source), type);
	}

	/**
	 * @see #openAll(Location, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final String source,
		final SCIFIOConfig config)
	{
		return openAll(resolve(source), config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final Location source,
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
	 * @see #openAll(Location, T, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final T type, final SCIFIOConfig config)
	{
		return openAll(resolve(source), type, config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final T type, final SCIFIOConfig config)
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
	 * @see #openAll(Location, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final ImgFactory imgFactory)
	{
		return openAll(resolve(source), imgFactory);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory imgFactory)
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
	 * @see #openAll(Location, ImgFactory, SCIFIOConfig)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final ImgFactory imgFactory,
	        final SCIFIOConfig config)
	{
		return openAll(resolve(source), imgFactory, config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory, SCIFIOConfig)
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory imgFactory,
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
	 * @see #openAll(Location, ImgFactory, T)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return openAll(resolve(source), imgFactory, type);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory<T> imgFactory, final T type)
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
	 * @see ImgOpener#openImgs(Reader, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>>
	openAll(final Reader reader, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<?>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getSourceLocation(), e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(Reader, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Reader reader, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, type, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getSourceLocation(), e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(Reader, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T>> List<SCIFIOImgPlus<T>> openAll(
			final Reader reader, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, imgFactory, config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getSourceLocation(), e);
		}
		return imgPlus;
	}

	/**
	 * @see ImgOpener#openImgs(Reader, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T>> List<SCIFIOImgPlus<T>> openAll(
		final Reader reader, final T type, final ImgFactory<T> imgFactory,
		final SCIFIOConfig config)
	{
		final ImgOpener opener = opener();
		List<SCIFIOImgPlus<T>> imgPlus = null;
		try {
			imgPlus = opener.openImgs(reader, imgFactory.imgFactory(type), config);
			register(imgPlus, opener);
		}
		catch (final ImgIOException e) {
			openError(reader.getMetadata().getSourceLocation(), e);
		}
		return imgPlus;
	}

	// -- Output Methods --

	/**
	 * @see #save(Location, Img)
	 */
	public static void save(final String dest, final Img<?> img) {
		save(resolve(dest), img);
	}

	/**
	 * @see ImgSaver#saveImg(Location, Img)
	 */
	public static void save(final Location dest, final Img<?> img) {
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
	 * @see #save(Location, SCIFIOImgPlus, int)
	 */
	public static void save(final String dest, final SCIFIOImgPlus<?> imgPlus,
	                        final int imageIndex)
	{
		save(resolve(dest), imgPlus, imageIndex);
	}

	/**
	 * @see ImgSaver#saveImg(Location, SCIFIOImgPlus, int)
	 */
	public static void save(final Location dest, final SCIFIOImgPlus<?> imgPlus,
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

	// -- Helper methods --

	/**
	 * Registers the given ImgPlus with the RefManagerService in the provided
	 * component's Context.
	 */
	private static void register(final List<? extends SCIFIOImgPlus> imgPlus,
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
	public static ImgOpener opener() {
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
	 * @param source the source to resolve
	 * @return the resolved location or <code>null</code> if the resolving failed
	 */
	private static Location resolve(final String source) {
		final LocationService loc = opener().context().getService(
			LocationService.class);
		Location location = null;
		try {
			location = loc.resolve(source);
		}
		catch (final URISyntaxException e) {
			resolveError(source, e);
		}
		return location;
	}

	/**
	 * @param source - Source that failed to open
	 * @param e - Exception to log
	 */
	private static void openError(final Location source, final Exception e) {
		logService.error("Failed to open ImgPlus for source: " + source, e);
	}

	/**
	 * @param dest - Source that failed to be written to
	 * @param e - Exception to log
	 */
	private static void saveError(final Location dest, final Exception e) {
		logService.error("Failed to save ImgPlus to id: " + dest, e);
	}

	/**
	 * @param source the string that could not be resolved
	 * @param e - Exception to log
	 */
	private static void resolveError(final String source, final Exception e) {
		logService.error("Failed to resolve source string: " + source, e);
	}

	// -- Deprecated API --

	/**
	 * @deprecated
	 * @see #openAll(String)
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<?>> openImgs(final String source) {
		return openAll(source);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, SCIFIOConfig)
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<?>> openImgs(final String source, final SCIFIOConfig config) {
		return openAll(source, config);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, ImgFactory)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(final String source, final ImgFactory<T> imgFactory) {
		return openAll(source, imgFactory);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(final String source, final ImgFactory<T> imgFactory, final SCIFIOConfig config) {
		return openAll(source, imgFactory, config);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, ImgFactory, T)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(final String source, final ImgFactory<T> imgFactory, final T type) {
		return openAll(source, imgFactory, type);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, T, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(final String source, final T type, final SCIFIOConfig config) {
		return openAll(source, type, config);
	}

	/**
	 * @deprecated
	 * @see #openAll(String, T)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openImgs(final String source, final T type)
	{
		return openAll(source, type);
	}

	/**
	 * @deprecated
	 * @see #openAll(Reader, T, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openImgs(final Reader reader, final T type, final SCIFIOConfig config)
	{
		return openAll(reader, type, config);
	}

	/**
	 * @deprecated
	 * @see #openAll(Reader, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(
			final Reader reader, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(reader, imgFactory, config);
	}

	/**
	 * @deprecated
	 * @see #openAll(Reader, T, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>> openImgs(
			final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(reader, type, imgFactory, config);
	}

	/**
	 * @deprecated
	 * @see #openAllFloat(String)
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<FloatType>> openFloatImgs(final String source) {
		return openAllFloat(source);
	}

	/**
	 * @deprecated
	 * @see #openAllDouble(String)
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<DoubleType>> openDoubleImgs(
			final String source)
	{
		return openAllDouble(source);
	}

	/**
	 * @deprecated
	 * @see #openAllUnsignedByte(String)
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<UnsignedByteType>> openUnsignedByteImgs(
			final String source)
	{
		return openAllUnsignedByte(source);
	}

	/**
	 * @deprecated
	 * @see #open(String, T)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final String source, final T type)
	{
		return open(source, type);
	}

	/**
	 * @deprecated
	 * @see #open(String, SCIFIOConfig)
	 */
	@Deprecated
	public static SCIFIOImgPlus<?> openImg(final String source,
	                                final SCIFIOConfig config)
	{
		return open(source, config);
	}

	/**
	 * @deprecated
	 * @see #open(String, T, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final String source, final T type, final SCIFIOConfig config)
	{
		return open(source, type, config);
	}

	/**
	 * @deprecated
	 * @see #open(String, ImgFactory)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final String source, final ImgFactory<T> imgFactory)
	{
		return open(source, imgFactory);
	}

	/**
	 * @deprecated
	 * @see #open(String, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config) throws ImgIOException
	{
		return open(source, imgFactory, config);
	}

	/**
	 * @deprecated
	 * @see #open(String, ImgFactory, T)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return open(source, imgFactory, type);
	}

	/**
	 * @deprecated
	 * @see #open(Reader, RealType, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
			final Reader reader, final T type, final SCIFIOConfig config)
	{
		return open(reader, type, config);
	}

	/**
	 * @deprecated
	 * @see #open(Reader, RealType, ImgFactory, SCIFIOConfig)
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	openImg(final Reader reader, final T type, final ImgFactory<T> imgFactory,
	        final SCIFIOConfig config)
	{
		return open(reader, type, imgFactory, config);
	}

	/**
	 * @deprecated
	 * @see #save(String, Img)
	 */
	@Deprecated
	public static void saveImg(final String dest, final Img<?> img) {
		save(dest, img);
	}

	/**
	 * @deprecated
	 * @see #save(String, SCIFIOImgPlus, int)
	 */
	@Deprecated
	public static void saveImg(final String dest, final SCIFIOImgPlus<?> imgPlus,
	                        final int imageIndex)
	{
		save(dest, imgPlus, imageIndex);
	}
}
