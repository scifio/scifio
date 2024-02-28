/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2024 SCIFIO developers.
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

import static org.scijava.util.ListUtils.first;

import io.scif.Reader;
import io.scif.Writer;
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
		return open(opener(), source);
	}

	/**
	 * @see #open(Location)
	 */
	public static SCIFIOImgPlus<?> open(final String source) {
		return first(openAll(source));
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
		return openUnsignedByte(opener(), source);
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

	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> open(
		final String source, final T type)
	{
		return first(openAll(source, type));
	}

	/**
	 * @see #open(Location, SCIFIOConfig)
	 */
	public static SCIFIOImgPlus<?> open(final String source,
	                                    final SCIFIOConfig config)
	{
		return first(openAll(source, config));
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
	 * @see #open(Location, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final T type, final SCIFIOConfig config)
	{
		return first(openAll(source, type, config));
	}

	/**
	 * @see #open(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final ImgFactory<T> imgFactory)
	{
		return first(openAll(source, imgFactory));
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final ImgFactory<T> imgFactory)
	{
		return first(openAll(source, imgFactory));
	}

	/**
	 * @see #open(Location, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final String source, final ImgFactory<T> imgFactory,
	     final SCIFIOConfig config)
	{
		return first(openAll(source, imgFactory, config));
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final Location source, final ImgFactory<T> imgFactory,
	     final SCIFIOConfig config)
	{
		return first(openAll(source, imgFactory, config));
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
		return openAll(opener(), source);
	}

	/**
	 * @see #openAll(Location)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final String source) {
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed {@link FloatType}
	 * .
	 */
	public static List<SCIFIOImgPlus<FloatType>> openAllFloat(
		final Location source)
	{
		return openAllFloat(opener(), source);
	}

	/**
	 * @see #openAllFloat(Location)
	 */
	public static List<SCIFIOImgPlus<FloatType>> openAllFloat(final String source) {
		final ImgOpener opener = new ImgOpener();
		return openAllFloat(opener, resolve(source, opener.context()));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link DoubleType}.
	 */
	public static List<SCIFIOImgPlus<DoubleType>> openAllDouble(
		final Location source)
	{
		return openAllDouble(opener(), source);
	}

	/**
	 * @see #openAllDouble(Location)
	 */
	public static List<SCIFIOImgPlus<DoubleType>> openAllDouble(
		final String source)
	{
		final ImgOpener opener = new ImgOpener();
		return openAllDouble(opener, resolve(source, opener.context()));
	}

	/**
	 * As {@link ImgOpener#openImgs(Location)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static List<SCIFIOImgPlus<UnsignedByteType>> openAllUnsignedByte(
		final Location source)
	{
		return openAllUnsignedByte(opener(), source);
	}

	public static List<SCIFIOImgPlus<UnsignedByteType>> openAllUnsignedByte(
		final String source)
	{
		final ImgOpener opener = new ImgOpener();
		return openAllUnsignedByte(opener, resolve(source, opener.context()));
	}

	/**
	 * @see ImgOpener#openImgs(Location)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final T type)
	{
		return openAll(opener(), source, type);
	}

	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final String source, final T type)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), type);
	}

	/**
	 * @see #openAll(Location, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final String source,
		final SCIFIOConfig config)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static List<SCIFIOImgPlus<?>> openAll(final Location source,
		final SCIFIOConfig config)
	{
		return openAll(opener(), source, config);
	}

	/**
	 * @see #openAll(Location, RealType, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final T type, final SCIFIOConfig config)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), type, config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final T type, final SCIFIOConfig config)
	{
		return openAll(opener(), source, type, config);
	}

	/**
	 * @see #openAll(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final ImgFactory<T> imgFactory)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), imgFactory);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory<T> imgFactory)
	{
		return openAll(opener(), source, imgFactory);
	}

	/**
	 * @see #openAll(Location, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final String source, final ImgFactory<T> imgFactory,
	        final SCIFIOConfig config)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), imgFactory, config);
	}

	/**
	 * @see ImgOpener#openImgs(Location, ImgFactory, SCIFIOConfig)
	 */
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(opener(), source, imgFactory, config);
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
		final ImgSaver saver = new ImgSaver();
		save(saver, resolve(dest, saver.context()), img);
	}

	/**
	 * @see ImgSaver#saveImg(Location, Img)
	 */
	public static void save(final Location dest, final Img<?> img) {
		save(new ImgSaver(), dest, img);
	}

	/**
	 * @see #save(Location, SCIFIOImgPlus, int)
	 */
	public static void save(final String dest, final SCIFIOImgPlus<?> imgPlus,
	                        final int imageIndex)
	{
		final ImgSaver saver = new ImgSaver();
		save(saver, resolve(dest, saver.context()), imgPlus, imageIndex);
	}

	/**
	 * @see ImgSaver#saveImg(Location, SCIFIOImgPlus, int)
	 */
	public static void save(final Location dest, final SCIFIOImgPlus<?> imgPlus,
		final int imageIndex)
	{
		save(new ImgSaver(), dest, imgPlus, imageIndex);
	}

	/**
	 * @see ImgSaver#saveImg(Writer, Img)
	 */
	public static void save(final Writer writer, final Img<?> img) {
		try {
			new ImgSaver().saveImg(writer, img);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(Writer, SCIFIOImgPlus, int)
	 */
	public static void save(final Writer writer,
	                           final SCIFIOImgPlus<?> imgPlus, final int imageIndex)
	{
		try {
			new ImgSaver().saveImg(writer, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
	}

	// -- Utility methods --

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

	// -- Helper methods --

	private static SCIFIOImgPlus<?> open(final ImgOpener opener, final Location source) {
		return first(openAll(opener, source));
	}

	private static SCIFIOImgPlus<FloatType> openFloat(
			final ImgOpener opener, final Location source)
	{
		return first(openAllFloat(opener, source));
	}

	private static SCIFIOImgPlus<DoubleType> openDouble(
			final ImgOpener opener, final Location source)
	{
		return first(openAllDouble(opener, source));
	}

	private static SCIFIOImgPlus<UnsignedByteType> openUnsignedByte(
			final ImgOpener opener, final Location source)
	{
		return first(openAllUnsignedByte(opener, source));
	}

	private static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> open(
		final ImgOpener opener, final Location source, final T type)
	{
		return first(openAll(opener, source, type));
	}

	private static SCIFIOImgPlus<?> open(final ImgOpener opener,
	                                     final Location source,
	                                     final SCIFIOConfig config)
	{
		return first(openAll(opener, source, config));
	}

	private static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final ImgOpener opener, final Location source, final T type, final SCIFIOConfig config)
	{
		return first(openAll(opener, source, type, config));
	}

	private static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final ImgOpener opener, final Location source, final ImgFactory<T> imgFactory)
	{
		return first(openAll(opener, source, imgFactory));
	}

	private static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
	open(final ImgOpener opener, final Location source, final ImgFactory<T> imgFactory,
	     final SCIFIOConfig config)
	{
		return first(openAll(opener, source, imgFactory, config));
	}

	private static List<SCIFIOImgPlus<?>> openAll(final ImgOpener opener, final Location source) {
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

	private static List<SCIFIOImgPlus<FloatType>> openAllFloat(
			final ImgOpener opener, final Location source)
	{
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

	private static List<SCIFIOImgPlus<DoubleType>> openAllDouble(
			final ImgOpener opener, final Location source)
	{
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

	private static List<SCIFIOImgPlus<UnsignedByteType>> openAllUnsignedByte(
			final ImgOpener opener, final Location source)
	{
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

	private static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final ImgOpener opener, final Location source, final T type)
	{
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

	private static List<SCIFIOImgPlus<?>> openAll(final ImgOpener opener,
	                                             final Location source,
	                                             final SCIFIOConfig config)
	{
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

	private static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final ImgOpener opener, final Location source, final T type, final SCIFIOConfig config)
	{
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

	private static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final ImgOpener opener, final Location source, final ImgFactory<T> imgFactory)
	{
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

	private static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
	openAll(final ImgOpener opener, final Location source, final ImgFactory<T> imgFactory,
	        final SCIFIOConfig config)
	{
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

	private static void save(final ImgSaver saver, final Location dest, final Img<?> img) {
		try {
			saver.saveImg(dest, img);
		}
		catch (final IncompatibleTypeException e) {
			saveError(dest, e);
		}
		catch (final ImgIOException e) {
			saveError(dest, e);
		}
	}

	private static void save(final ImgSaver saver, final Location dest, final SCIFIOImgPlus<?> imgPlus,
	                        final int imageIndex)
	{
		try {
			saver.saveImg(dest, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			saveError(dest, e);
		}
		catch (final ImgIOException e) {
			saveError(dest, e);
		}
	}

	/**
	 * Registers the given ImgPlus with the RefManagerService in the provided
	 * component's Context.
	 */
	private static void register(
		@SuppressWarnings("rawtypes") final List<? extends SCIFIOImgPlus> imgPlus,
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
	 * @param source the source to resolve
	 * @param context
	 * @return the resolved location or <code>null</code> if the resolving failed
	 */
	private static Location resolve(final String source, final Context context) {
		final LocationService loc = context.getService(
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
	 * @deprecated Use {@link #open(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> open(
		final String source, final ImgFactory<T> imgFactory,
		@SuppressWarnings("unused") final T type)
	{
		final ImgOpener opener = new ImgOpener();
		return open(opener, resolve(source, opener.context()), imgFactory);
	}

	/**
	 * @deprecated Use {@link #open(Location, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> open(
		final Location source, final ImgFactory<T> imgFactory,
		@SuppressWarnings("unused") final T type)
	{
		return open(opener(), source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #openAll(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final String source, final ImgFactory<T> imgFactory,
			@SuppressWarnings("unused") final T type)
	{
		final ImgOpener opener = new ImgOpener();
		return openAll(opener, resolve(source, opener.context()), imgFactory);
	}

	/**
	 * @deprecated Use {@link #openAll(Location, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openAll(final Location source, final ImgFactory<T> imgFactory,
			@SuppressWarnings("unused") final T type)
	{
		return openAll(opener(), source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #openAll(String)}.
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<?>> openImgs(final String source) {
		return openAll(source);
	}

	/**
	 * @deprecated Use {@link #openAll(String, SCIFIOConfig)}.
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<?>> openImgs(final String source,
		final SCIFIOConfig config)
	{
		return openAll(source, config);
	}

	/**
	 * @deprecated Use {@link #openAll(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory)
	{
		return openAll(source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #openAll(String, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(source, imgFactory, config);
	}

	/**
	 * @deprecated Use {@link #openAll(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory, final T type)
	{
		return openAll(source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #openAll(String, RealType, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type, final SCIFIOConfig config)
	{
		return openAll(source, type, config);
	}

	/**
	 * @deprecated Use {@link #openAll(String, RealType)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type)
	{
		return openAll(source, type);
	}

	/**
	 * @deprecated Use {@link #openAll(Reader, RealType, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final T type, final SCIFIOConfig config)
	{
		return openAll(reader, type, config);
	}

	/**
	 * @deprecated Use {@link #openAll(Reader, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(reader, imgFactory, config);
	}

	/**
	 * @deprecated Use
	 *             {@link #openAll(Reader, RealType, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return openAll(reader, type, imgFactory, config);
	}

	/**
	 * @deprecated Use {@link #openAllFloat(String)}.
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<FloatType>> openFloatImgs(
		final String source)
	{
		return openAllFloat(source);
	}

	/**
	 * @deprecated Use {@link #openAllDouble(String)}.
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<DoubleType>> openDoubleImgs(
		final String source)
	{
		return openAllDouble(source);
	}

	/**
	 * @deprecated Use {@link #openAllUnsignedByte(String)}.
	 */
	@Deprecated
	public static List<SCIFIOImgPlus<UnsignedByteType>> openUnsignedByteImgs(
		final String source)
	{
		return openAllUnsignedByte(source);
	}

	/**
	 * @deprecated Use {@link #open(String, RealType)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final T type)
	{
		return open(source, type);
	}

	/**
	 * @deprecated Use {@link #open(String, SCIFIOConfig)}.
	 */
	@Deprecated
	public static SCIFIOImgPlus<?> openImg(final String source,
		final SCIFIOConfig config)
	{
		return open(source, config);
	}

	/**
	 * @deprecated Use {@link #open(String, RealType, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final T type, final SCIFIOConfig config)
	{
		return open(source, type, config);
	}

	/**
	 * @deprecated Use {@link #open(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory)
	{
		return open(source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #open(String, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config) throws ImgIOException
	{
		return open(source, imgFactory, config);
	}

	/**
	 * @deprecated Use {@link #open(String, ImgFactory)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final String source, final ImgFactory<T> imgFactory,
			@SuppressWarnings("unused") final T type)
	{
		return open(source, imgFactory);
	}

	/**
	 * @deprecated Use {@link #open(Reader, RealType, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final Reader reader, final T type, final SCIFIOConfig config)
	{
		return open(reader, type, config);
	}

	/**
	 * @deprecated Use {@link #open(Reader, RealType, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public static <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T>
		openImg(final Reader reader, final T type, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config)
	{
		return open(reader, type, imgFactory, config);
	}

	/**
	 * @deprecated Use {@link #save(String, Img)}.
	 */
	@Deprecated
	public static void saveImg(final String dest, final Img<?> img) {
		save(dest, img);
	}

	/**
	 * @deprecated Use {@link #save(String, SCIFIOImgPlus, int)}.
	 */
	@Deprecated
	public static void saveImg(final String dest, final SCIFIOImgPlus<?> imgPlus,
		final int imageIndex)
	{
		save(dest, imgPlus, imageIndex);
	}


	/**
	 * @deprecated Use {@link #save(Writer, Img)}.
	 */
	@Deprecated
	public static void saveImg(final Writer writer, final Img<?> img) {
		try {
			new ImgSaver().saveImg(writer, img);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
	}

	/**
	 * @deprecated Use {@link #save(Writer, SCIFIOImgPlus, int)}.
	 */
	public static void saveImg(final Writer writer,
	                           final SCIFIOImgPlus<?> imgPlus, final int imageIndex)
	{
		try {
			new ImgSaver().saveImg(writer, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
		catch (final ImgIOException e) {
			saveError(writer.getMetadata().getDestinationLocation(), e);
		}
	}
}
