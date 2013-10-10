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

package io.scif.img;

import io.scif.Reader;
import io.scif.Writer;
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

	// -- Static IO Methods --

	/**
	 * @see ImgOpener#openImg(String)
	 */
	@SuppressWarnings("rawtypes")
	public static ImgPlus open(final String source) throws ImgIOException {
		return new ImgOpener().openImg(source);
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed {@link FloatType}.
	 */
	public static ImgPlus<FloatType> openFloat(final String source)
		throws ImgIOException
	{
		return new ImgOpener().openImg(source, new FloatType());
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed {@link DoubleType}.
	 */
	public static ImgPlus<DoubleType> openDouble(final String source)
		throws ImgIOException
	{
		return new ImgOpener().openImg(source, new DoubleType());
	}

	/**
	 * As {@link ImgOpener#openImg(String)} with a guaranteed
	 * {@link UnsignedByteType}.
	 */
	public static ImgPlus<UnsignedByteType> openUnsignedByte(final String source)
		throws ImgIOException
	{
		return new ImgOpener().openImg(source, new UnsignedByteType());
	}

	/**
	 * @see ImgOpener#openImg(String, RealType)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type) throws ImgIOException
	{
		return new ImgOpener().openImg(source, type);
	}

	/**
	 * @see ImgOpener#openImg(String, ImgOptions)
	 */
	@SuppressWarnings("rawtypes")
	public static ImgPlus
		openImg(final String source, final ImgOptions imgOptions)
			throws ImgIOException
	{
		return new ImgOpener().openImg(source, imgOptions);
	}

	/**
	 * @see ImgOpener#openImg(Reader, RealType, ImgOptions)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type, final ImgOptions imgOptions)
		throws ImgIOException
	{
		return new ImgOpener().openImg(source, type, imgOptions);
	}

	/**
	 * @see ImgOpener#openImg(String, ImgFactory)
	 */
	@SuppressWarnings("rawtypes")
	public static ImgPlus
		openImg(final String source, final ImgFactory imgFactory)
			throws ImgIOException
	{
		return new ImgOpener().openImg(source, imgFactory);
	}

	@SuppressWarnings("rawtypes")
	public static ImgPlus openImg(final String source,
		final ImgFactory imgFactory, final ImgOptions imgOptions)
		throws ImgIOException
	{
		return new ImgOpener().openImg(source, imgFactory, imgOptions);
	}

	/**
	 * @see ImgOpener#openImg(String, ImgFactory, RealType)
	 */
	public static <T extends RealType<T>> ImgPlus<T> openImg(final String source,
		final ImgFactory<T> imgFactory, final T type) throws ImgIOException
	{
		return new ImgOpener().openImg(source, imgFactory, type);
	}

	/**
	 * @see ImgOpener#openImg(Reader, RealType, ImgOptions)
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final Reader reader, final T type, final ImgOptions imgOptions)
		throws ImgIOException
	{
		return new ImgOpener().openImg(reader, type, imgOptions);
	}

	/**
	 * @see ImgOpener#openImg(Reader, RealType, ImgFactory, ImgOptions)
	 */
	public static <T extends RealType<T>> ImgPlus<T> openImg(final Reader reader,
		final T type, final ImgFactory<T> imgFactory, final ImgOptions imgOptions)
		throws ImgIOException
	{
		return new ImgOpener().openImg(reader, type, imgFactory, imgOptions);
	}

	/**
	 * @see ImgSaver#saveImg(String, Img)
	 */
	public static <T extends RealType<T> & NativeType<T>> void saveImg(
		final String dest, final Img<T> img) throws ImgIOException
	{
		try {
			new ImgSaver().saveImg(dest, img);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(String, ImgPlus, int)
	 */
	public static <T extends RealType<T> & NativeType<T>> void saveImg(
		final String dest, final ImgPlus<T> imgPlus, final int imageIndex)
		throws ImgIOException
	{
		try {
			new ImgSaver().saveImg(dest, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(Writer, Img)
	 */
	public static <T extends RealType<T> & NativeType<T>> void saveImg(
		final Writer writer, final Img<T> img) throws ImgIOException
	{
		try {
			new ImgSaver().saveImg(writer, img);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * @see ImgSaver#saveImg(Writer, ImgPlus, int)
	 */
	public static <T extends RealType<T> & NativeType<T>> void saveImg(
		final Writer writer, final ImgPlus<T> imgPlus, final int imageIndex)
		throws ImgIOException
	{
		try {
			new ImgSaver().saveImg(writer, imgPlus, imageIndex);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}
	}

}
