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

package io.scif.util;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.Writer;
import io.scif.common.ReflectException;
import io.scif.common.ReflectedUniverse;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.axis.DefaultLinearAxis;
import net.imglib2.meta.axis.LinearAxis;

/**
 * A collection of constants and utility methods applicable for all cycles of
 * image processing within SCIFIO.
 */
public final class FormatTools {

	// -- Constants --

	public static final String[] COMPRESSION_SUFFIXES = { "bz2", "gz" };

	// -- Constants - Thumbnail dimensions --

	/** Default height and width for thumbnails. */
	public static final int THUMBNAIL_DIMENSION = 128;

	// -- Constants - pixel types --

	/** Identifies the <i>INT8</i> data type used to store pixel values. */
	public static final int INT8 = 0;

	/** Identifies the <i>UINT8</i> data type used to store pixel values. */
	public static final int UINT8 = 1;

	/** Identifies the <i>INT16</i> data type used to store pixel values. */
	public static final int INT16 = 2;

	/** Identifies the <i>UINT16</i> data type used to store pixel values. */
	public static final int UINT16 = 3;

	/** Identifies the <i>INT32</i> data type used to store pixel values. */
	public static final int INT32 = 4;

	/** Identifies the <i>UINT32</i> data type used to store pixel values. */
	public static final int UINT32 = 5;

	/** Identifies the <i>FLOAT</i> data type used to store pixel values. */
	public static final int FLOAT = 6;

	/** Identifies the <i>DOUBLE</i> data type used to store pixel values. */
	public static final int DOUBLE = 7;

	/** Human readable pixel type. */
	private static final String[] pixelTypes = makePixelTypes();

	static String[] makePixelTypes() {
		final String[] pixelTypes = new String[8];
		pixelTypes[INT8] = "int8";
		pixelTypes[UINT8] = "uint8";
		pixelTypes[INT16] = "int16";
		pixelTypes[UINT16] = "uint16";
		pixelTypes[INT32] = "int32";
		pixelTypes[UINT32] = "uint32";
		pixelTypes[FLOAT] = "float";
		pixelTypes[DOUBLE] = "double";
		return pixelTypes;
	}

	// -- Constants - miscellaneous --

	/** File grouping options. */
	public static final int MUST_GROUP = 0;
	public static final int CAN_GROUP = 1;
	public static final int CANNOT_GROUP = 2;

	/** Patterns to be used when constructing a pattern for output filenames. */
	public static final String SERIES_NUM = "%s";
	public static final String SERIES_NAME = "%n";
	public static final String CHANNEL_NUM = "%c";
	public static final String CHANNEL_NAME = "%w";
	public static final String Z_NUM = "%z";
	public static final String T_NUM = "%t";
	public static final String TIMESTAMP = "%A";

	// -- Constants - domains --

	/** Identifies the high content screening domain. */
	public static final String HCS_DOMAIN = "High-Content Screening (HCS)";

	/** Identifies the light microscopy domain. */
	public static final String LM_DOMAIN = "Light Microscopy";

	/** Identifies the electron microscopy domain. */
	public static final String EM_DOMAIN = "Electron Microscopy (EM)";

	/** Identifies the scanning probe microscopy domain. */
	public static final String SPM_DOMAIN = "Scanning Probe Microscopy (SPM)";

	/** Identifies the scanning electron microscopy domain. */
	public static final String SEM_DOMAIN = "Scanning Electron Microscopy (SEM)";

	/** Identifies the fluorescence-lifetime domain. */
	public static final String FLIM_DOMAIN = "Fluorescence-Lifetime Imaging";

	/** Identifies the medical imaging domain. */
	public static final String MEDICAL_DOMAIN = "Medical Imaging";

	/** Identifies the histology domain. */
	public static final String HISTOLOGY_DOMAIN = "Histology";

	/** Identifies the gel and blot imaging domain. */
	public static final String GEL_DOMAIN = "Gel/Blot Imaging";

	/** Identifies the astronomy domain. */
	public static final String ASTRONOMY_DOMAIN = "Astronomy";

	/**
	 * Identifies the graphics domain. This includes formats used exclusively by
	 * analysis software.
	 */
	public static final String GRAPHICS_DOMAIN = "Graphics";

	/** Identifies an unknown domain. */
	public static final String UNKNOWN_DOMAIN = "Unknown";

	/** List of non-graphics domains. */
	public static final String[] NON_GRAPHICS_DOMAINS =
		new String[] { LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN,
			MEDICAL_DOMAIN, HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN,
			HCS_DOMAIN, UNKNOWN_DOMAIN };

	/** List of non-HCS domains. */
	public static final String[] NON_HCS_DOMAINS = new String[] { LM_DOMAIN,
		EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN, MEDICAL_DOMAIN,
		HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN, UNKNOWN_DOMAIN };

	/**
	 * List of domains that do not require special handling. Domains that require
	 * special handling are {@link #GRAPHICS_DOMAIN} and {@link #HCS_DOMAIN}.
	 */
	public static final String[] NON_SPECIAL_DOMAINS = new String[] { LM_DOMAIN,
		EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN, MEDICAL_DOMAIN,
		HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN, UNKNOWN_DOMAIN };

	/** List of all supported domains. */
	public static final String[] ALL_DOMAINS = new String[] { HCS_DOMAIN,
		LM_DOMAIN, EM_DOMAIN, SPM_DOMAIN, SEM_DOMAIN, FLIM_DOMAIN, MEDICAL_DOMAIN,
		HISTOLOGY_DOMAIN, GEL_DOMAIN, ASTRONOMY_DOMAIN, GRAPHICS_DOMAIN,
		UNKNOWN_DOMAIN };

	// -- Constructor --

	private FormatTools() {
		// NB: Prevent instantiation of utility class.
	}

	// Utility methods -- dimensional positions --

	/**
	 * Wraps the provided AxisType in a CalibratedAxis with calibration = 1.0um.
	 */
	public static CalibratedAxis createAxis(final AxisType axisType) {
		return new DefaultLinearAxis(axisType, "um");
	}

	/**
	 * Creates an array, wrapping all provided AxisTypes as CalibratedAxis with
	 * calibration = 1.0um.
	 */
	public static CalibratedAxis[] createAxes(final AxisType... axisTypes)
	{
		final CalibratedAxis[] axes = new CalibratedAxis[axisTypes.length];

		for (int i = 0; i < axisTypes.length; i++) {
			axes[i] = createAxis(axisTypes[i]);
		}
		return axes;
	}

	/**
	 * Applies the given scale value, and an origin of 0.0, to each
	 * {@link LinearAxis} in the provided Metadata.
	 */
	public static void calibrate(final Metadata m, final int imageIndex,
		final double[] scale)
	{
		calibrate(m, imageIndex, scale, new double[scale.length]);
	}

	/**
	 * Applies the given scale and origin values to each {@link LinearAxis} in the
	 * provided Metadata.
	 */
	public static void calibrate(final Metadata m, final int imageIndex,
		final double[] scale, final double[] origin)
	{
		int i = 0;

		for (final CalibratedAxis axis : m.getAxes(imageIndex)) {
			if (i >= scale.length || !(axis instanceof LinearAxis)) return;
			((LinearAxis) axis).setScale(scale[i]);
			((LinearAxis) axis).setOrigin(origin[i]);
			i++;
		}
	}

	/**
	 * Gets the average scale over the specified axis of the given image metadata.
	 */
	public static double getScale(final Metadata m, final int imageIndex,
		final AxisType axisType)
	{
		final CalibratedAxis axis = m.getAxis(imageIndex, axisType);
		final long axisLength = m.getAxisLength(0, axis);
		return axis.averageScale(0, axisLength - 1);
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param imageIndex image index within dataset
	 * @param planeIndex rasterized plane index to convert to axis indices
	 * @param reader reader used to open the dataset
	 * @return position along each dimensional axis
	 */
	public static long[] rasterToPosition(final int imageIndex,
		final long planeIndex, final Reader reader)
	{
		return rasterToPosition(imageIndex, planeIndex, reader.getMetadata());
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param imageIndex image index within dataset
	 * @param planeIndex rasterized plane index to convert to axis indices
	 * @param metadata metadata describing the dataset
	 * @return position along each dimensional axis
	 */
	public static long[] rasterToPosition(final int imageIndex,
		final long planeIndex, final Metadata m)
	{
		final long[] axisLengths = m.getAxesLengthsNonPlanar(imageIndex);
		return rasterToPosition(axisLengths, planeIndex);
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param raster rasterized index value
	 * @return position along each dimensional axis
	 */
	public static long[] rasterToPosition(final long[] lengths, final long raster)
	{
		return rasterToPosition(lengths, raster, new long[lengths.length]);
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param raster rasterized index value
	 * @param pos preallocated position array to populate with the result
	 * @return position along each dimensional axis
	 */
	public static long[] rasterToPosition(final long[] lengths, long raster,
		final long[] pos)
	{
		long offset = 1;
		for (int i = 0; i < pos.length; i++) {
			final long offset1 = offset * lengths[i];
			final long q = i < pos.length - 1 ? raster % offset1 : raster;
			pos[i] = q / offset;
			raster -= q;
			offset = offset1;
		}
		return pos;
	}

	/**
	 * Computes a unique 1-D index corresponding to the given multidimensional
	 * position.
	 * 
	 * @param imageIndex image index within dataset
	 * @param reader reader used to open the dataset
	 * @param planeIndices position along each dimensional axis
	 * @return rasterized index value
	 */
	public static long positionToRaster(final int imageIndex,
		final Reader reader, final long[] planeIndices)
	{
		return positionToRaster(imageIndex, reader.getMetadata(), planeIndices);
	}

	/**
	 * Computes a unique 1-D index corresponding to the given multidimensional
	 * position.
	 * 
	 * @param imageIndex image index within dataset
	 * @param metadata metadata describing the dataset
	 * @param planeIndices position along each dimensional axis
	 * @return rasterized index value
	 */
	public static long positionToRaster(final int imageIndex,
		final Metadata m, final long[] planeIndices)
	{
		final long[] planeSizes = m.getAxesLengthsNonPlanar(imageIndex);
		return positionToRaster(planeSizes, planeIndices);
	}

	/**
	 * Computes a unique 1-D index corresponding to the given multidimensional
	 * position.
	 * 
	 * @param lengths the maximum value for each positional dimension
	 * @param pos position along each dimensional axis
	 * @return rasterized index value
	 */
	public static long positionToRaster(final long[] lengths, final long[] pos) {
		long offset = 1;
		long raster = 0l;
		for (int i = 0; i < pos.length; i++) {
			raster += offset * pos[i];
			offset *= lengths[i];
		}
		return raster;
	}

	/**
	 * Computes the number of raster values for a positional array with the given
	 * lengths.
	 */
	public static long getRasterLength(final long[] lengths) {
		long len = 1;
		for (int i = 0; i < lengths.length; i++)
			len *= lengths[i];
		return len;
	}

	// -- Utility methods - sanity checking

	/**
	 * Asserts that the current file is either null, or not, according to the
	 * given flag. If the assertion fails, an IllegalStateException is thrown.
	 * 
	 * @param currentId File name to test.
	 * @param notNull True iff id should be non-null.
	 * @param depth How far back in the stack the calling method is; this name is
	 *          reported as part of the exception message, if available. Use zero
	 *          to suppress output of the calling method name.
	 */
	public static void assertId(final String currentId, final boolean notNull,
		final int depth)
	{
		String msg = null;
		if (currentId == null && notNull) {
			msg = "Current file should not be null; call setId(String) first";
		}
		else if (currentId != null && !notNull) {
			msg =
				"Current file should be null, but is '" + currentId +
					"'; call close() first";
		}
		if (msg == null) return;

		final StackTraceElement[] ste = new Exception().getStackTrace();
		String header;
		if (depth > 0 && ste.length > depth) {
			String c = ste[depth].getClassName();
			if (c.startsWith("io.scif.")) {
				c = c.substring(c.lastIndexOf(".") + 1);
			}
			header = c + "." + ste[depth].getMethodName() + ": ";
		}
		else header = "";
		throw new IllegalStateException(header + msg);
	}

	/**
	 * Asserts that the current file is either null, or not, according to the
	 * given flag. If the assertion fails, an IllegalStateException is thrown.
	 * 
	 * @param currentId File name to test.
	 * @param notNull True iff id should be non-null.
	 * @param depth How far back in the stack the calling method is; this name is
	 *          reported as part of the exception message, if available. Use zero
	 *          to suppress output of the calling method name.
	 */
	public static void assertStream(final RandomAccessInputStream stream,
		final boolean notNull, final int depth)
	{
		String msg = null;
		if (stream == null && notNull) {
			msg = "Current file should not be null; call setId(String) first";
		}
		else if (stream != null && !notNull) {
			msg =
				"Current file should be null, but is '" + stream +
					"'; call close() first";
		}
		if (msg == null) return;

		final StackTraceElement[] ste = new Exception().getStackTrace();
		String header;
		if (depth > 0 && ste.length > depth) {
			String c = ste[depth].getClassName();
			if (c.startsWith("io.scif.")) {
				c = c.substring(c.lastIndexOf(".") + 1);
			}
			header = c + "." + ste[depth].getMethodName() + ": ";
		}
		else header = "";
		throw new IllegalStateException(header + msg);
	}

	/**
	 * Convenience method for checking that the plane number, tile size and buffer
	 * sizes are all valid for the given reader. If 'bufLength' is less than 0,
	 * then the buffer length check is not performed.
	 */
	public static void checkPlaneParameters(final Metadata m, final int imageIndex,
		final long planeIndex, final int bufLength, final long[] planeMin,
		final long[] planeMax) throws FormatException
	{
		assertId(m.getSource().getFileName(), true, 2);
		checkPlaneNumber(m, imageIndex, planeIndex);
		checkTileSize(m, planeMin, planeMax, imageIndex);
		if (bufLength >= 0) checkBufferSize(m, bufLength, planeMax, imageIndex);
	}

	/** Checks that the given plane number is valid for the given reader. */
	public static void checkPlaneNumber(final Metadata m, final int imageIndex,
		final long planeIndex) throws FormatException
	{
		final long planeCount = m.getPlaneCount(imageIndex);
		if (planeIndex < 0 || planeIndex >= planeCount) {
			throw new FormatException("Invalid plane number: " + planeIndex + " (" +
			/* TODO series=" +
			r.getMetadata().getSeries() + ", */"planeCount=" + planeIndex + ")");
		}
	}

	/** Checks that the given tile size is valid for the given reader. */
	public static void checkTileSize(final Metadata m, final long[] planeMin,
		final long[] planeMax, final int imageIndex) throws FormatException
	{
		List<CalibratedAxis> axes = m.getAxesPlanar(imageIndex);
		
		for (int i=0; i<axes.size(); i++) {
			final long start = planeMin[i];
			final long end = planeMax[i];
			final long length = m.getAxisLength(imageIndex, axes.get(i));

			if (start < 0 || end < 0 || (start + end) > length) throw new FormatException(
				"Invalid planar size: start=" + start + ", end=" + end +
					", length in metadata=" + length);
		}
	}

	/**
	 * Checks that the given buffer length is long enough to hold planes of the
	 * specified image index, using the provided Reader.
	 */
	public static void checkBufferSize(final int imageIndex, final Metadata m,
		final int len) throws FormatException
	{
		checkBufferSize(m, len, m.getAxesLengthsPlanar(imageIndex), imageIndex);
	}

	/**
	 * Checks that the given buffer size is large enough to hold an image with the
	 * given planar lengths.
	 * 
	 * @throws FormatException if the buffer is too small
	 */
	public static void checkBufferSize(final Metadata m, final int len,
		final long[] planeLengths, final int imageIndex) throws FormatException
	{
		final long size =
			getPlaneSize(m, new long[planeLengths.length], planeLengths, imageIndex);
		if (size > len) {
			throw new FormatException("Buffer too small (got " + len + ", expected " +
				size + ").");
		}
	}

	/**
	 * Returns true if the given RandomAccessInputStream conatins at least 'len'
	 * bytes.
	 */
	public static boolean validStream(final RandomAccessInputStream stream,
		final int len, final boolean littleEndian) throws IOException
	{
		stream.seek(0);
		stream.order(littleEndian);
		return stream.length() >= len;
	}

	/** Returns the size in bytes of a single plane read by the given Reader. */
	public static long getPlaneSize(final Reader r, final int imageIndex) {
		return getPlaneSize(r.getMetadata(), imageIndex);
	}

	/** Returns the size in bytes of a tile defined by the given Metadata. */
	public static long getPlaneSize(final Metadata m, final int imageIndex) {
		return m.getPlaneSize(imageIndex);
	}

	/** Returns the size in bytes of a w * h tile. */
	public static long getPlaneSize(final Metadata m, final int width,
		final int height, int imageIndex)
	{
		return getPlaneSize(m, new long[2], new long[]{width, height}, imageIndex);
	}

	/** Returns the size in bytes of a plane with the given minima and maxima. */
	public static long getPlaneSize(final Metadata m, final long[] planeMin,
		final long[] planeMax, final int imageIndex)
	{
		if (planeMin.length != planeMax.length) throw new IllegalArgumentException(
			"Plane min array size: " + planeMin.length +
				" does not match plane max array size: " + planeMax.length);
		long length = m.getBitsPerPixel(imageIndex) / 8;
		for (int i = 0; i < planeMin.length; i++) {
			length *= planeMax[i] - planeMin[i];
		}
		return length;
	}

	// -- Utility methods - pixel types --

	/**
	 * Takes a string value and maps it to one of the pixel type enumerations.
	 * 
	 * @param pixelTypeAsString the pixel type as a string.
	 * @return type enumeration value for use with class constants.
	 */
	public static int pixelTypeFromString(final String pixelTypeAsString) {
		final String lowercaseTypeAsString = pixelTypeAsString.toLowerCase();
		for (int i = 0; i < pixelTypes.length; i++) {
			if (pixelTypes[i].equals(lowercaseTypeAsString)) return i;
		}
		throw new IllegalArgumentException("Unknown type: '" + pixelTypeAsString +
			"'");
	}

	/**
	 * Takes a pixel type value and gets a corresponding string representation.
	 * 
	 * @param pixelType the pixel type.
	 * @return string value for human-readable output.
	 */
	public static String getPixelTypeString(final int pixelType) {
		if (pixelType < 0 || pixelType >= pixelTypes.length) {
			throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
		}
		return pixelTypes[pixelType];
	}

	/**
	 * Retrieves how many bytes per pixel the current plane or section has.
	 * 
	 * @param pixelType the pixel type as retrieved from
	 * @return the number of bytes per pixel.
	 * @see io.scif.Metadata#getPixelType()
	 */
	public static int getBytesPerPixel(final int pixelType) {
		switch (pixelType) {
			case INT8:
			case UINT8:
				return 1;
			case INT16:
			case UINT16:
				return 2;
			case INT32:
			case UINT32:
			case FLOAT:
				return 4;
			case DOUBLE:
				return 8;
		}
		throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
	}

	/**
	 * Retrieves how many bytes per pixel the current plane or section has.
	 * 
	 * @param pixelType the pixel type as retrieved from
	 *          {@link io.scif.Metadata#getPixelType()}.
	 * @return the number of bytes per pixel.
	 * @see io.scif.Metadata#getPixelType()
	 */
	public static int getBitsPerPixel(final int pixelType) {
		return 8 * FormatTools.getBytesPerPixel(pixelType);
	}

	/**
	 * Retrieves the number of bytes per pixel in the current plane.
	 * 
	 * @param pixelType the pixel type, as a String.
	 * @return the number of bytes per pixel.
	 * @see #pixelTypeFromString(String)
	 * @see #getBytesPerPixel(int)
	 */
	public static int getBytesPerPixel(final String pixelType) {
		return getBytesPerPixel(pixelTypeFromString(pixelType));
	}

	/**
	 * Determines whether the given pixel type is floating point or integer.
	 * 
	 * @param pixelType the pixel type as retrieved from
	 *          {@link io.scif.Metadata#getPixelType()}.
	 * @return true if the pixel type is floating point.
	 * @see io.scif.Metadata#getPixelType()
	 */
	public static boolean isFloatingPoint(final int pixelType) {
		switch (pixelType) {
			case INT8:
			case UINT8:
			case INT16:
			case UINT16:
			case INT32:
			case UINT32:
				return false;
			case FLOAT:
			case DOUBLE:
				return true;
		}
		throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
	}

	/**
	 * Determines whether the given pixel type is signed or unsigned.
	 * 
	 * @param pixelType the pixel type as retrieved from
	 *          {@link io.scif.Metadata#getPixelType()}.
	 * @return true if the pixel type is signed.
	 * @see io.scif.Metadata#getPixelType()
	 */
	public static boolean isSigned(final int pixelType) {
		switch (pixelType) {
			case INT8:
			case INT16:
			case INT32:
			case FLOAT:
			case DOUBLE:
				return true;
			case UINT8:
			case UINT16:
			case UINT32:
				return false;
		}
		throw new IllegalArgumentException("Unknown pixel type: " + pixelType);
	}

	/**
	 * Returns an appropriate pixel type given the number of bytes per pixel.
	 * 
	 * @param bytes number of bytes per pixel.
	 * @param signed whether or not the pixel type should be signed.
	 * @param fp whether or not these are floating point pixels.
	 */
	public static int pixelTypeFromBytes(final int bytes, final boolean signed,
		final boolean fp) throws FormatException
	{
		switch (bytes) {
			case 1:
				return signed ? INT8 : UINT8;
			case 2:
				return signed ? INT16 : UINT16;
			case 4:
				return fp ? FLOAT : signed ? INT32 : UINT32;
			case 8:
				return DOUBLE;
			default:
				throw new FormatException("Unsupported byte depth: " + bytes);
		}
	}

	// -- Utility methods -- export

	/**
	 * @throws FormatException Never actually thrown.
	 * @throws IOException Never actually thrown.
	 */
	public static String getFilename(final int imageIndex, final int image,
		final Reader r, final String pattern) throws FormatException, IOException
	{

		String filename =
			pattern.replaceAll(SERIES_NUM, String.valueOf(imageIndex));

		String imageName = r.getCurrentFile();
		if (imageName == null) imageName = "Image#" + imageIndex;
		imageName = imageName.replaceAll("/", "_");
		imageName = imageName.replaceAll("\\\\", "_");

		filename = filename.replaceAll(SERIES_NAME, imageName);

		final long[] coordinates =
			FormatTools.rasterToPosition(imageIndex, image, r);
		filename = filename.replaceAll(Z_NUM, String.valueOf(coordinates[0]));
		filename = filename.replaceAll(T_NUM, String.valueOf(coordinates[2]));
		filename = filename.replaceAll(CHANNEL_NUM, String.valueOf(coordinates[1]));

		String channelName = String.valueOf(coordinates[1]);
		channelName = channelName.replaceAll("/", "_");
		channelName = channelName.replaceAll("\\\\", "_");

		filename = filename.replaceAll(CHANNEL_NAME, channelName);

		/*
		//TODO check for date
		String date = retrieve.getImageAcquisitionDate(imageIndex).getValue();
		long stamp = 0;
		if (retrieve.getPlaneCount(imageIndex) > image) {
		  Double deltaT = retrieve.getPlaneDeltaT(imageIndex, image);
		  if (deltaT != null) {
		    stamp = (long) (deltaT * 1000);
		  }
		}
		stamp += DateTools.getTime(date, DateTools.ISO8601_FORMAT);
		date = DateTools.convertDate(stamp, (int) DateTools.UNIX_EPOCH);

		filename = filename.replaceAll(TIMESTAMP, date);
		*/

		return filename;
	}

	/**
	 * @throws FormatException
	 * @throws IOException
	 */
	public static String[] getFilenames(final String pattern, final Reader r)
		throws FormatException, IOException
	{
		final Vector<String> filenames = new Vector<String>();
		String filename = null;
		for (int series = 0; series < r.getImageCount(); series++) {
			for (int image = 0; image < r.getImageCount(); image++) {
				filename = getFilename(series, image, r, pattern);
				if (!filenames.contains(filename)) filenames.add(filename);
			}
		}
		return filenames.toArray(new String[0]);
	}

	/**
	 * @throws FormatException
	 * @throws IOException
	 */
	public static int getImagesPerFile(final String pattern, final Reader r)
		throws FormatException, IOException
	{
		final String[] filenames = getFilenames(pattern, r);
		int totalPlanes = 0;
		for (int series = 0; series < r.getImageCount(); series++) {
			totalPlanes += r.getMetadata().getPlaneCount(series);
		}
		return totalPlanes / filenames.length;
	}

	// -- Utility methods -- other

	/**
	 * Default implementation for {@link IFormatReader#openThumbBytes}. At the
	 * moment, it uses {@link java.awt.image.BufferedImage} objects to resize
	 * thumbnails, so it is not safe for use in headless contexts. In the future,
	 * we may reimplement the image scaling logic purely with byte arrays, but
	 * handling every case would be substantial effort, so doing so is currently a
	 * low priority item.
	 */
	public static byte[] openThumbBytes(final Reader reader,
		final int imageIndex, final long planeIndex) throws FormatException,
		IOException
	{
		// NB: Dependency on AWT here is unfortunate, but very difficult to
		// eliminate in general. We use reflection to limit class loading
		// problems with AWT on Mac OS X.
		final ReflectedUniverse r = new ReflectedUniverse(reader.log());
		byte[][] bytes = null;
		try {
			r.exec("import io.scif.gui.AWTImageTools");

			final long planeSize = getPlaneSize(reader, imageIndex);
			Plane plane = null;
			if (planeSize < 0) {
				final Metadata m = reader.getMetadata();
				final long[] planeMax = m.getAxesLengthsPlanar(imageIndex);
				final long[] planeMin = new long[planeMax.length];

				final int xIndex = m.getAxisIndex(imageIndex, Axes.X);
				final int yIndex = m.getAxisIndex(imageIndex, Axes.Y);
				final long width = m.getThumbSizeX(imageIndex) * 4;
				final long height = m.getThumbSizeY(imageIndex) * 4;

				planeMin[xIndex] = (m.getAxisLength(imageIndex, Axes.X) - width) / 2;
				planeMin[yIndex] =
					(m.getAxisLength(imageIndex, Axes.Y) - height) / 2;
				planeMax[xIndex] = width;
				planeMax[yIndex] = height;

				plane = reader.openPlane(imageIndex, planeIndex, planeMin, planeMax);
			}
			else {
				plane = reader.openPlane(imageIndex, planeIndex);
			}

			r.setVar("plane", plane);
			r.setVar("reader", reader);
			r.setVar("sizeX", reader.getMetadata().getAxisLength(imageIndex, Axes.X));
			r.setVar("sizeY", reader.getMetadata().getAxisLength(imageIndex, Axes.Y));
			r.setVar("thumbSizeX", reader.getMetadata().getThumbSizeX(imageIndex));
			r.setVar("thumbSizeY", reader.getMetadata().getThumbSizeY(imageIndex));
			r.setVar("little", reader.getMetadata().isLittleEndian(imageIndex));
			r.setVar("imageIndex", imageIndex);
			r.exec("thumb = AWTImageTools.openThumbImage(plane, reader, imageIndex, sizeX, sizeY,"
				+ " thumbSizeX, thumbSizeY, false)");

			bytes = (byte[][]) r.exec("AWTImageTools.getPixelBytes(thumb, little)");
		}
		catch (final ReflectException exc) {
			throw new FormatException(exc);
		}

		if (bytes.length == 1) return bytes[0];
		final long rgbChannelCount =
			reader.getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);
		final byte[] rtn = new byte[(int) rgbChannelCount * bytes[0].length];

		if (!(reader.getMetadata().getInterleavedAxisCount(imageIndex) > 0)) {
			for (int i = 0; i < rgbChannelCount; i++) {
				System
					.arraycopy(bytes[i], 0, rtn, bytes[0].length * i, bytes[i].length);
			}
		}
		else {
			final int bpp =
				FormatTools.getBytesPerPixel(reader.getMetadata().getPixelType(
					imageIndex));

			for (int i = 0; i < bytes[0].length / bpp; i += bpp) {
				for (int j = 0; j < rgbChannelCount; j++) {
					System.arraycopy(bytes[j], i, rtn, (int)(i * rgbChannelCount) + j * bpp,
						bpp);
				}
			}
		}
		return rtn;
	}

	// -- Conversion convenience methods --

	/**
	 * Convenience method for writing all of the images and metadata obtained from
	 * the specified Reader into the specified Writer.
	 * 
	 * @param input the pre-initialized Reader used for reading data.
	 * @param output the uninitialized Writer used for writing data.
	 * @param outputFile the full path name of the output file to be created.
	 * @throws FormatException if there is a general problem reading from or
	 *           writing to one of the files.
	 * @throws IOException if there is an I/O-related error.
	 */
	public static void convert(final Reader input, final Writer output,
		final String outputFile) throws FormatException, IOException
	{

		Plane p = null;

		for (int i = 0; i < input.getImageCount(); i++) {
			for (int j = 0; j < input.getPlaneCount(i); j++) {
				p = input.openPlane(i, j);
				output.savePlane(i, j, p);
			}
		}

		input.close();
		output.close();
	}

	/**
	 * Get the default range for the specified pixel type. Note that this is not
	 * necessarily the minimum and maximum value which may be stored, but the
	 * minimum and maximum which should be used for rendering.
	 * 
	 * @param pixelType the pixel type.
	 * @returns an array containing the min and max as elements 0 and 1,
	 *          respectively.
	 * @throws IOException if the pixel type is floating point or invalid.
	 */
	public static long[] defaultMinMax(final int pixelType) {
		long min = 0, max = 0;

		switch (pixelType) {
			case INT8:
				min = Byte.MIN_VALUE;
				max = Byte.MAX_VALUE;
				break;
			case INT16:
				min = Short.MIN_VALUE;
				max = Short.MAX_VALUE;
				break;
			case INT32:
			case FLOAT:
			case DOUBLE:
				min = Integer.MIN_VALUE;
				max = Integer.MAX_VALUE;
				break;
			case UINT8:
				min = 0;
				max = (long) Math.pow(2, 8) - 1;
				break;
			case UINT16:
				min = 0;
				max = (long) Math.pow(2, 16) - 1;
				break;
			case UINT32:
				min = 0;
				max = (long) Math.pow(2, 32) - 1;
				break;
			default:
				throw new IllegalArgumentException("Invalid pixel type");
		}

		final long[] values = { min, max };
		return values;
	}

	/** Performs suffix matching for the given filename. */
	public static boolean checkSuffix(final String name, final String suffix) {
		return checkSuffix(name, new String[] { suffix });
	}

	/** Performs suffix matching for the given filename. */
	public static boolean
		checkSuffix(final String name, final String[] suffixList)
	{
		final String lname = name.toLowerCase();
		for (int i = 0; i < suffixList.length; i++) {
			final String s = "." + suffixList[i];
			if (lname.endsWith(s)) return true;
			for (int j = 0; j < COMPRESSION_SUFFIXES.length; j++) {
				if (lname.endsWith(s + "." + COMPRESSION_SUFFIXES[j])) return true;
			}
		}
		return false;
	}

}
