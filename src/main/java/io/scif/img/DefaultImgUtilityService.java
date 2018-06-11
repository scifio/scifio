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

import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.Bytes;

/**
 * Helper methods for converting between SCIFIO and ImgLib2 data structures.
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultImgUtilityService extends AbstractService implements
	ImgUtilityService
{

	// -- Fields --

	private final int BUFFER_SIZE = 256 * 1024; // 256K

	private SCIFIO scifio = null;

	// -- ImgUtilityService methods --

	/** Compiles an N-dimensional list of axis lengths from the given reader. */
	@Override
	public long[] getDimLengths(final Metadata m, final int imageIndex,
		final SCIFIOConfig config)
	{

		final long[] dimLengths = m.get(imageIndex).getAxesLengths();

		final ImageRegion region = config.imgOpenerGetRegion();

		for (int i = 0; i < dimLengths.length; i++) {

			if (region != null && i < region.size()) {
				final Range range = region.getRange(m.get(imageIndex).getAxis(i)
					.type());
				if (range != null) {
					dimLengths[i] = range.size();
				}
			}
		}
		return dimLengths;
	}

	@Override
	public long[] getConstrainedLengths(final Metadata m, final int imageIndex,
		final SCIFIOConfig config)
	{
		final long[] lengths = getDimLengths(m, imageIndex, config);

		final ImageRegion r = config.imgOpenerGetRegion();

		if (r != null) {
			// set each dimension length = the number of entries for that axis
			for (final CalibratedAxis t : m.get(0).getAxes()) {
				final Range range = r.getRange(t.type());
				if (range != null) lengths[m.get(0).getAxisIndex(t)] = range.size();
			}
		}

		return lengths;
	}

	/**
	 * @param source - the location of the dataset to assess
	 * @return The number of images in the specified dataset.
	 */
	@Override
	public int getImageCount(final String source) throws ImgIOException {
		try {
			final Format format = scifio().format().getFormat(source);
			return format.createParser().parse(source).getImageCount();
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * Downloads the given URL and caches it to a temporary file, which is deleted
	 * upon JVM shutdown. This is useful in conjuction with {@link ImgOpener} to
	 * open a URL as an {@link Img}.
	 * <p>
	 * Data compressed with zip or gzip is supported. In the case of zip, the
	 * first file in the archive is cached.
	 * </p>
	 */
	@Override
	public String cacheId(final String urlPath) throws ImgIOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			final URL url = new URL(urlPath);
			final String path = url.getPath();
			final boolean zip = path.endsWith(".zip");
			final boolean gz = path.endsWith(".gz");
			String filename = path.substring(path.lastIndexOf("/") + 1);

			// save URL to temporary file
			ZipInputStream inZip = null;
			in = url.openStream();
			if (zip) {
				in = inZip = new ZipInputStream(in);
				final ZipEntry zipEntry = inZip.getNextEntry();
				filename = zipEntry.getName(); // use filename in the zip
				// archive
			}
			if (gz) {
				in = new GZIPInputStream(in);
				filename = filename.substring(0, filename.length() - 3); // strip
				// .gz
			}
			final int dot = filename.lastIndexOf(".");
			final String prefix = dot < 0 ? filename : filename.substring(0, dot);
			final String suffix = dot < 0 ? "" : "." + filename.substring(dot + 1);
			final File tmpFile = File.createTempFile(prefix + "-", suffix);
			tmpFile.deleteOnExit();
			out = new FileOutputStream(tmpFile);
			final byte[] buf = new byte[BUFFER_SIZE];
			while (true) {
				final int r = in.read(buf);
				if (r < 0) break; // eof
				out.write(buf, 0, r);
			}
			return tmpFile.getAbsolutePath();
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
		finally {
			try {
				if (in != null) in.close();
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
			try {
				if (out != null) out.close();
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
		}
	}

	/** Obtains planar access instance backing the given img, if any. */
	@Override
	@SuppressWarnings("unchecked")
	public PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(
		final ImgPlus<?> img)
	{
		if (img.getImg() instanceof PlanarAccess) {
			return (PlanarAccess<ArrayDataAccess<?>>) img.getImg();
		}
		return null;
	}

	/** Obtains array access instance backing the given img, if any. */
	@Override
	public ArrayImg<?, ?> getArrayAccess(final ImgPlus<?> img) {
		if (img.getImg() instanceof ArrayImg) {
			return (ArrayImg<?, ?>) img.getImg();
		}
		return null;
	}

	/** Converts SCIFIO pixel type to ImgLib2 Type object. */
	@Override
	public RealType<?> makeType(final int pixelType) {
		final RealType<?> type;
		switch (pixelType) {
			case FormatTools.UINT8:
				type = new UnsignedByteType();
				break;
			case FormatTools.INT8:
				type = new ByteType();
				break;
			case FormatTools.UINT16:
				type = new UnsignedShortType();
				break;
			case FormatTools.INT16:
				type = new ShortType();
				break;
			case FormatTools.UINT32:
				type = new UnsignedIntType();
				break;
			case FormatTools.INT32:
				type = new IntType();
				break;
			case FormatTools.FLOAT:
				type = new FloatType();
				break;
			case FormatTools.DOUBLE:
				type = new DoubleType();
				break;
			default:
				type = null;
		}
		return type;
	}

	/**
	 * Converts ImgLib2 Type object to SCIFIO pixel type.
	 */
	@Override
	public int makeType(final Object type) throws ImgIOException {
		int pixelType = FormatTools.UINT8;
		if (type instanceof UnsignedByteType) {
			pixelType = FormatTools.UINT8;
		}
		else if (type instanceof ByteType) {
			pixelType = FormatTools.INT8;
		}
		else if (type instanceof UnsignedShortType) {
			pixelType = FormatTools.UINT16;
		}
		else if (type instanceof ShortType) {
			pixelType = FormatTools.INT16;
		}
		else if (type instanceof UnsignedIntType) {
			pixelType = FormatTools.UINT32;
		}
		else if (type instanceof IntType) {
			pixelType = FormatTools.INT32;
		}
		else if (type instanceof FloatType) {
			pixelType = FormatTools.FLOAT;
		}
		else if (type instanceof DoubleType) {
			pixelType = FormatTools.DOUBLE;
		}
		else {
			throw new ImgIOException("Pixel type not supported. " +
				"Please convert your image to a supported type.");
		}

		return pixelType;
	}

	/** Wraps raw primitive array in ImgLib2 Array object. */
	@Override
	public ArrayDataAccess<?> makeArray(final Object array) {
		final ArrayDataAccess<?> access;
		if (array instanceof byte[]) {
			access = new ByteArray((byte[]) array);
		}
		else if (array instanceof char[]) {
			access = new CharArray((char[]) array);
		}
		else if (array instanceof double[]) {
			access = new DoubleArray((double[]) array);
		}
		else if (array instanceof int[]) {
			access = new IntArray((int[]) array);
		}
		else if (array instanceof float[]) {
			access = new FloatArray((float[]) array);
		}
		else if (array instanceof short[]) {
			access = new ShortArray((short[]) array);
		}
		else if (array instanceof long[]) {
			access = new LongArray((long[]) array);
		}
		else access = null;
		return access;
	}

	/**
	 * see isCompressible(ImgPlus)
	 */
	@Override
	public <T extends RealType<T> & NativeType<T>> boolean isCompressible(
		final Img<T> img)
	{
		return isCompressible(ImgPlus.wrap(img));
	}

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
	@Override
	public <T extends RealType<T> & NativeType<T>> boolean isCompressible(
		final ImgPlus<T> img)
	{

		final CalibratedAxis[] axes = new CalibratedAxis[img.numDimensions()];
		img.axes(axes);

		final long[] axisLengths = new long[5];
		final long[] oldLengths = new long[img.numDimensions()];

		img.dimensions(oldLengths);

		// true if this img contains an axis that will need to be compressed
		boolean foundUnknown = false;

		for (int i = 0; i < axes.length; i++) {
			final CalibratedAxis axis = axes[i];

			switch (axis.type().getLabel().toUpperCase().charAt(0)) {
				case 'X':
				case 'Y':
				case 'Z':
				case 'C':
				case 'T':
					break;
				default:
					if (oldLengths[i] > 1) foundUnknown = true;
			}
		}

		if (!foundUnknown) return false;

		// This ImgPlus had unknown axes of size > 1, so we will check to see if
		// they can be compressed
		final String dimOrder = guessDimOrder(axes, oldLengths, axisLengths);

		return (dimOrder != null);
	}

	@Override
	public String guessDimOrder(final CalibratedAxis[] axes,
		final long[] dimLengths, final long[] newLengths)
	{
		String oldOrder = "";
		String newOrder = "";

		// initialize newLengths to be 1 for simpler multiplication logic later
		for (int i = 0; i < newLengths.length; i++) {
			newLengths[i] = 1;
		}

		// Signifies if the given axis is present in the dimension order,
		// X=0, Y=1, Z=2, C=3, T=4
		final boolean[] haveDim = new boolean[5];

		// number of "blocks" of unknown axes, e.g. YUUUZU = 2
		int contiguousUnknown = 0;

		// how many axis slots we have to work with
		int missingAxisCount = 0;

		// flag to determine how many contiguous blocks of unknowns present
		boolean unknownBlock = false;

		// first pass to determine which axes are missing and how many
		// unknown blocks are present.
		// We build oldOrder to iterate over on pass 2, for convenience
		for (int i = 0; i < axes.length; i++) {
			switch (axes[i].type().getLabel().toUpperCase().charAt(0)) {
				case 'X':
					oldOrder += "X";
					haveDim[0] = true;
					unknownBlock = false;
					break;
				case 'Y':
					oldOrder += "Y";
					haveDim[1] = true;
					unknownBlock = false;
					break;
				case 'Z':
					oldOrder += "Z";
					haveDim[2] = true;
					unknownBlock = false;
					break;
				case 'C':
					oldOrder += "C";
					haveDim[3] = true;
					unknownBlock = false;
					break;
				case 'T':
					oldOrder += "T";
					haveDim[4] = true;
					unknownBlock = false;
					break;
				default:
					oldOrder += "U";

					// dimensions of size 1 can be skipped, and only will
					// be considered in pass 2 if the number of missing axes is
					// greater than the number of contiguous unknown chunks found
					if (dimLengths[i] > 1) {
						if (!unknownBlock) {
							unknownBlock = true;
							contiguousUnknown++;
						}
					}
					break;
			}
		}

		// determine how many axes are missing
		for (final boolean d : haveDim) {
			if (!d) missingAxisCount++;
		}

		// check to see if we can make a valid dimension ordering
		if (contiguousUnknown > missingAxisCount) {
			return null;
		}

		int axesPlaced = 0;
		unknownBlock = false;

		// Flag to determine if the current unknownBlock was started by
		// an unknown of size 1.
		boolean sizeOneUnknown = false;

		// Second pass to assign new ordering and calculate lengths
		for (int i = 0; i < axes.length; i++) {
			switch (oldOrder.charAt(0)) {
				case 'U':
					// dimensions of size 1 have no effect on the ordering
					if (dimLengths[i] > 1 || contiguousUnknown < missingAxisCount) {
						if (!unknownBlock) {
							unknownBlock = true;

							// length of this unknown == 1
							if (contiguousUnknown < missingAxisCount) {
								contiguousUnknown++;
								sizeOneUnknown = true;
							}

							// assign a label to this dimension
							if (!haveDim[0]) {
								newOrder += "X";
								haveDim[0] = true;
							}
							else if (!haveDim[1]) {
								newOrder += "Y";
								haveDim[1] = true;
							}
							else if (!haveDim[2]) {
								newOrder += "Z";
								haveDim[2] = true;
							}
							else if (!haveDim[3]) {
								newOrder += "C";
								haveDim[3] = true;
							}
							else if (!haveDim[4]) {
								newOrder += "T";
								haveDim[4] = true;
							}
						}
						else if (dimLengths[i] > 1 && sizeOneUnknown) {
							// we are in a block of unknowns that was started by
							// one of size 1, but contains an unknown of size > 1,
							// thus was double counted (once in pass 1, once in pass
							// 2)
							sizeOneUnknown = false;
							contiguousUnknown--;
						}
						newLengths[axesPlaced] *= dimLengths[i];
					}
					break;
				default:
					// "cap" the current unknown block
					if (unknownBlock) {
						axesPlaced++;
						unknownBlock = false;
						sizeOneUnknown = false;
					}

					newOrder += oldOrder.charAt(i);
					newLengths[axesPlaced] = dimLengths[i];
					axesPlaced++;
					break;
			}
		}

		// append any remaining missing axes
		// only have to update order string, as lengths are already 1
		for (int i = 0; i < haveDim.length; i++) {
			if (!haveDim[i]) {
				switch (i) {
					case 0:
						newOrder += "X";
						break;
					case 1:
						newOrder += "Y";
						break;
					case 2:
						newOrder += "Z";
						break;
					case 3:
						newOrder += "C";
						break;
					case 4:
						newOrder += "T";
						break;
				}
			}
		}

		return newOrder;
	}

	@Override
	public double decodeWord(final byte[] plane, final int index,
		final int pixelType, final boolean little)
	{
		final double value;
		switch (pixelType) {
			case FormatTools.UINT8:
				value = plane[index] & 0xff;
				break;
			case FormatTools.INT8:
				value = plane[index];
				break;
			case FormatTools.UINT16:
				value = Bytes.toShort(plane, 2 * index, 2, little) & 0xffff;
				break;
			case FormatTools.INT16:
				value = Bytes.toShort(plane, 2 * index, 2, little);
				break;
			case FormatTools.UINT32:
				value = Bytes.toInt(plane, 4 * index, 4, little) & 0xffffffffL;
				break;
			case FormatTools.INT32:
				value = Bytes.toInt(plane, 4 * index, 4, little);
				break;
			case FormatTools.FLOAT:
				value = Bytes.toFloat(plane, 4 * index, 4, little);
				break;
			case FormatTools.DOUBLE:
				value = Bytes.toDouble(plane, 8 * index, 8, little);
				break;
			default:
				value = Double.NaN;
		}
		return value;
	}

	@Override
	public <T> SCIFIOImgPlus<T> makeSCIFIOImgPlus(final Img<T> img) {
		if (img instanceof SCIFIOImgPlus) return (SCIFIOImgPlus<T>) img;
		if (img instanceof ImgPlus) {
			return new SCIFIOImgPlus<>((ImgPlus<T>) img);
		}
		return new SCIFIOImgPlus<>(img);
	}

	// -- Helper Methods --

	private SCIFIO scifio() {
		if (scifio == null) scifio = new SCIFIO(getContext());
		return scifio;
	}
}
