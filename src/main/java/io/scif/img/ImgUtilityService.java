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

import io.scif.Metadata;
import io.scif.SCIFIOService;
import io.scif.config.SCIFIOConfig;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Helper methods for converting between SCIFIO and ImgLib2 data structures.
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Curtis Rueden
 */
public interface ImgUtilityService extends SCIFIOService {

	/**
	 * Downloads the given URL and caches it to a temporary file, which is deleted
	 * upon JVM shutdown. This is useful in conjuction with {@link ImgOpener} to
	 * open a URL as an {@link Img}.
	 * <p>
	 * Data compressed with zip or gzip is supported. In the case of zip, the
	 * first file in the archive is cached.
	 * </p>
	 */
	String cacheId(final String urlPath) throws ImgIOException;

	/** Obtains planar access instance backing the given img, if any. */
	PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(final ImgPlus<?> img);

	/** Obtains array access instance backing the given img, if any. */
	ArrayImg<?, ?> getArrayAccess(final ImgPlus<?> img);

	/** Converts SCIFIO pixel type to ImgLib2 Type object. */
	RealType<?> makeType(final int pixelType);

	/**
	 * Converts ImgLib2 Type object to SCIFIO pixel type.
	 */
	int makeType(final Object type) throws ImgIOException;

	/** Wraps raw primitive array in ImgLib2 Array object. */
	ArrayDataAccess<?> makeArray(final Object array);

	/** Compiles an N-dimensional list of axis lengths from the given Metadata. */
	long[] getDimLengths(final Metadata m, final int imageIndex,
		final SCIFIOConfig config);

	/**
	 * Returns an N-dimensional list of axis lengths from the given Metadata,
	 * constrained by the provided SubRegion (if present)
	 */
	long[] getConstrainedLengths(final Metadata m, final int imageIndex,
		final SCIFIOConfig config);

	/**
	 * @param source - the location of the dataset to assess
	 * @return The number of images in the specified dataset.
	 */
	int getImageCount(final String source) throws ImgIOException;

	/**
	 * see {@link #isCompressible(ImgPlus)}
	 */
	<T extends RealType<T> & NativeType<T>> boolean isCompressible(
		final Img<T> img);

	/**
	 * Currently there are limits as to what types of Images can be saved. All
	 * images must ultimately adhere to an, at most, five-dimensional structure
	 * using the known axes X, Y, Z, Channel and Time. Unknown axes (U) can
	 * potentially be handled by coercing to the Channel axis. For example, X Y Z
	 * U C U T would be valid, as would X Y Z U T. But X Y C Z U T would not, as
	 * the unknown axis can not be compressed with Channel. This method will
	 * return true if the axes of the provided image can be represented with a
	 * valid 5D String, and false otherwise.
	 */
	<T extends RealType<T> & NativeType<T>> boolean isCompressible(
		final ImgPlus<T> img);

	/**
	 * The ImgLib axes structure can contain multiple unknown axes. This method
	 * will determine if the provided dimension order, obtained from an ImgLib
	 * AxisType array, can be converted to a 5-dimensional sequence compatible
	 * with SCIFIO, and returns that sequence if it exists and null otherwise.
	 *
	 * @param newLengths - updated to hold the lengths of the newly ordered axes
	 */
	String guessDimOrder(final CalibratedAxis[] axes, final long[] dimLengths,
		final long[] newLengths);

	/**
	 * @param plane - byte array from the desired plane
	 * @param index - pixel index into the plane (NOT an array index)
	 * @param pixelType - base pixel type of the plane's data
	 * @param little - endianness of the plane's bytes
	 * @return A double value for the specified pixel.
	 */
	double decodeWord(byte[] plane, int index, int pixelType, boolean little);

	/**
	 * Convenience method for wrapping an {@link Img} as a {@link SCIFIOImgPlus} .
	 *
	 * @param img Img to wrap
	 * @return SCIFIOImgPlus wrapping the provided Img, or if the provided Img is
	 *         a SCIFIOImgPlus, casts and returns.
	 */
	<T> SCIFIOImgPlus<T> makeSCIFIOImgPlus(final Img<T> img);
}
