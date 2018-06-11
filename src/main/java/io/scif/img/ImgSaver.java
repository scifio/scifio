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

import io.scif.ByteArrayPlane;
import io.scif.DefaultImageMetadata;
import io.scif.DefaultMetadata;
import io.scif.DefaultWriter;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Translator;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.services.FormatService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.exception.ImgLibException;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.GenericIntType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.util.Bytes;

/**
 * Writes out an {@link ImgPlus} using SCIFIO.
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class ImgSaver extends AbstractImgIOComponent {

	@Parameter
	private StatusService statusService;

	@Parameter
	private FormatService formatService;

	@Parameter
	private TranslatorService translatorService;

	// -- Constructors --

	public ImgSaver() {
		super();
	}

	public ImgSaver(final Context context) {
		super(context);
	}

	// -- ImgSaver methods --

	/**
	 * Entry point for saving an {@link ImgPlus}. The goal is to get to a
	 * {@link Writer} and {@link ImgPlus} which are then passed to
	 * {@link #writePlanes}. These saveImg signatures facilitate multiple pathways
	 * to that goal. This method is called when a String id and {@link Img} are
	 * provided.
	 *
	 * @param id
	 * @param img
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public Metadata saveImg(final String id, final Img<?> img)
		throws ImgIOException, IncompatibleTypeException
	{
		return saveImg(id, img, null);
	}

	/**
	 * String id provided. {@link ImgPlus} provided, or wrapped {@link Img} in
	 * previous saveImg.
	 *
	 * @param id
	 * @param img
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public Metadata saveImg(final String id, final SCIFIOImgPlus<?> img,
		final int imageIndex) throws ImgIOException, IncompatibleTypeException
	{
		return saveImg(id, img, imageIndex, null);
	}

	/**
	 * As {@link #saveImg(String, Img)} with configuration options.
	 *
	 * @param id
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public Metadata saveImg(final String id, final Img<?> img,
		final SCIFIOConfig config) throws ImgIOException, IncompatibleTypeException
	{
		return saveImg(id, utils().makeSCIFIOImgPlus(img), 0, config);
	}

	/**
	 * As {@link #saveImg(String, SCIFIOImgPlus, int)} with configuration options.
	 *
	 * @param id
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public Metadata saveImg(final String id, final SCIFIOImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		return writeImg(id, null, img, imageIndex, config);
	}

	/**
	 * {@link Writer} and {@link Img} provided
	 *
	 * @param w
	 * @param img
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final Writer w, final Img<?> img) throws ImgIOException,
		IncompatibleTypeException
	{
		saveImg(w, img, null);
	}

	// TODO IFormatHandler needs to be promoted to be able to get the current
	// file, to get its full path, to provide the ImgPluSCIFIOImgPlusending
	// that,
	// these two IFormatWriter methods are not guaranteed to be
	// useful
	/**
	 * {@link Writer} provided. {@link ImgPlus} provided, or wrapped provided
	 * {@link Img}.
	 *
	 * @param w
	 * @param img
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final Writer w, final SCIFIOImgPlus<?> img,
		final int imageIndex) throws ImgIOException, IncompatibleTypeException
	{
		saveImg(w, img, imageIndex, null);
	}

	/**
	 * As {@link #saveImg(Writer, Img)}, with configuration options.
	 *
	 * @param w
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final Writer w, final Img<?> img,
		final SCIFIOConfig config) throws ImgIOException, IncompatibleTypeException
	{
		saveImg(w, utils().makeSCIFIOImgPlus(img), 0, config);
	}

	// TODO IFormatHandler needs to be promoted to be able to get the current
	// file, to get its full path, to provide the ImgPlus
	// pending that, these two IFormatWriter methods are not guaranteed to be
	// useful
	/**
	 * As {@link #saveImg(Writer, SCIFIOImgPlus, int)}, with configuration
	 * options.
	 *
	 * @param w
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final Writer w, final SCIFIOImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		writeImg(img.getSource(), w, img, imageIndex, config);
	}

	// -- Utility methods --

	/**
	 * The ImgLib axes structure can contain multiple unknown axes. This method
	 * will determine if the provided dimension order, obtained from an ImgLib
	 * AxisType array, can be converted to a 5-dimensional sequence compatible
	 * with SCIFIO, and returns that sequence if it exists and null otherwise.
	 *
	 * @param newLengths - updated to hold the lengths of the newly ordered axes
	 */
	public static String guessDimOrder(final CalibratedAxis[] axes,
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

	// -- Helper methods --

	/**
	 * Entry {@link #writeImg} method. Gathers necessary metadata, creates default
	 * configuration options if needed, and delegates to the appropriate
	 * intermediate {@link #writeImg} method if able.
	 */
	private Metadata writeImg(final String id, final Writer w,
		final SCIFIOImgPlus<?> img, final int imageIndex, SCIFIOConfig config)
		throws ImgIOException, IncompatibleTypeException
	{
		// Create the SCIFIOConfig if needed
		if (config == null) {
			config = new SCIFIOConfig();
		}

		final int sliceCount = countSlices(img);

		if (w == null) {
			if (id == null || id.length() == 0) {
				throw new ImgIOException(
					"No output destination or pre-configured Writer was provided, and" +
						" no way to determine the desired output path. Default value:" +
						" ImgPlus's source.");
			}
			return writeImg(id, img, imageIndex, config, sliceCount);
		}

		return writeImg(w, id, img, imageIndex, config, sliceCount);
	}

	/**
	 * Intermediate {@link #writeImg} method. Creates a {@link Writer} for the
	 * given id.
	 */
	private Metadata writeImg(final String id, final SCIFIOImgPlus<?> imgPlus,
		final int imageIndex, final SCIFIOConfig config, final int sliceCount)
		throws ImgIOException, IncompatibleTypeException
	{
		// Create a Writer for the given id
		Writer w = null;

		try {
			boolean matches = false;
			for (final Format format : formatService.getFormatList(id)) {
				if (!format.getWriterClass().equals(DefaultWriter.class)) {
					matches = true;
					break;
				}
			}

			if (matches) {
				final File f = new File(id);
				if (f.exists()) {
					f.delete();
				}
			}
			w = formatService.getWriterByExtension(id);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}

		return writeImg(w, id, imgPlus, imageIndex, config, sliceCount);
	}

	/**
	 * Intermediate {@link #writeImg} method. Ensures the given writer has proper
	 * {@link Metadata}, or creates it if possible.
	 */
	private Metadata writeImg(final Writer w, final String id,
		final SCIFIOImgPlus<?> imgPlus, final int imageIndex,
		final SCIFIOConfig config, final int sliceCount) throws ImgIOException,
		IncompatibleTypeException
	{
		if (w.getMetadata() == null) {
			if (id == null || id.length() == 0) {
				throw new ImgIOException(
					"A Writer with no Metadata was provided, with no way to determine " +
						"the desired output path. Default value: ImgPlus's source.");
			}
			try {
				populateMeta(w, imgPlus, config, id, imageIndex);
			}
			catch (final FormatException e) {
				throw new ImgIOException(e, "SCIFIO exception when writing to file " +
					id + ":\n" + e.getMessage());
			}
			catch (final IOException e) {
				throw new ImgIOException(e, "I/O exception writing to file " + id +
					":\n" + e.getMessage());
			}
		}

		return writeImg(w, imgPlus, imageIndex, sliceCount);
	}

	/**
	 * Terminal {@link #writeImg} method. Performs actual pixel output.
	 */
	private Metadata writeImg(final Writer w, final SCIFIOImgPlus<?> imgPlus,
		final int imageIndex, final int sliceCount) throws ImgIOException,
		IncompatibleTypeException
	{
		if (imgPlus.numDimensions() > 0) {
			final long startTime = System.currentTimeMillis();

			// write pixels
			writePlanes(w, imageIndex, imgPlus);

			// Print time statistics
			final long endTime = System.currentTimeMillis();
			final float time = (endTime - startTime) / 1000f;
			statusService.showStatus(sliceCount, sliceCount, w.getMetadata()
				.getDatasetName() + ": wrote " + sliceCount + " planes in " + time +
				" s");
		}

		return w.getMetadata();
	}

	/**
	 * Counts the number of slices in the provided ImgPlus.
	 * <p>
	 * NumSlices = product of the sizes of all non-X,Y planes.
	 * </p>
	 */
	private int countSlices(final SCIFIOImgPlus<?> img) {

		int sliceCount = 1;
		for (int i = 0; i < img.numDimensions(); i++) {
			if (!(img.axis(i).type().equals(Axes.X) || img.axis(i).type().equals(
				Axes.Y)))
			{
				sliceCount *= img.dimension(i);
			}
		}

		return sliceCount;
	}

	/**
	 * Iterates through the planes of the provided {@link SCIFIOImgPlus},
	 * converting each to a byte[] if necessary (the SCIFIO writer requires a
	 * byte[]) and saving the plane.
	 */
	private void writePlanes(final Writer w, final int imageIndex,
		final SCIFIOImgPlus<?> imgPlus) throws ImgIOException,
		IncompatibleTypeException
	{
		// Get basic statistics
		final Metadata mOut = w.getMetadata();
		validate(mOut, w);

		final int rgbChannelCount = mOut.get(imageIndex).isMultichannel()
			? (int) mOut.get(imageIndex).getAxisLength(Axes.CHANNEL) : 1;
		final boolean interleaved = mOut.get(imageIndex)
			.getInterleavedAxisCount() > 0;

		byte[] sourcePlane = null;

		// iterate over each plane
		final long planeOutCount = w.getMetadata().get(imageIndex).getPlaneCount();

		final Img<?> img = imgPlus.getImg();
		final int planeCount = getPlaneCount(img);
		if (planeOutCount < planeCount / rgbChannelCount) {
			// Warn that some planes were truncated (e.g. going from 4D format to 3D)
			statusService.showStatus(0, 0, "Source dataset contains: " + planeCount +
				" planes, but writer format only supports: " + rgbChannelCount *
					planeOutCount, true);
		}

		for (int planeIndex = 0; planeIndex < planeOutCount; planeIndex++) {
			statusService.showStatus(planeIndex, (int) planeOutCount,
				"Saving plane " + (planeIndex + 1) + "/" + planeOutCount);
			// save bytes
			try {
				final Metadata meta = w.getMetadata();

				final long[] planarLengths = meta.get(imageIndex)
					.getAxesLengthsPlanar();
				final long[] planarMin = SCIFIOMetadataTools.modifyPlanar(imageIndex,
					meta, new long[planarLengths.length]);
				final long[] planarMax = new long[planarMin.length];
				for (int d = 0; d < planarMax.length; d++)
					planarMax[d] = planarMin[d] + planarLengths[d] - 1;
				final FinalInterval bounds = new FinalInterval(planarMin, planarMax);
				final ByteArrayPlane destPlane = new ByteArrayPlane(getContext(), meta
					.get(imageIndex), bounds);

				for (int cIndex = 0; cIndex < rgbChannelCount; cIndex++) {
					final Object curPlane = getPlaneArray(img, rgbChannelCount, cIndex,
						planeIndex);

					final Class<?> planeClass = curPlane.getClass();

					// Convert current plane if necessary
					if (planeClass == int[].class) {
						sourcePlane = Bytes.fromInts((int[]) curPlane, false);
					}
					else if (planeClass == byte[].class) {
						sourcePlane = (byte[]) curPlane;
					}
					else if (planeClass == short[].class) {
						sourcePlane = Bytes.fromShorts((short[]) curPlane, false);
					}
					else if (planeClass == long[].class) {
						sourcePlane = Bytes.fromLongs((long[]) curPlane, false);
					}
					else if (planeClass == double[].class) {
						sourcePlane = Bytes.fromDoubles((double[]) curPlane, false);
					}
					else if (planeClass == float[].class) {
						sourcePlane = Bytes.fromFloats((float[]) curPlane, false);
					}
					else {
						throw new IncompatibleTypeException(new ImgLibException(),
							"Plane data type: " + planeClass + " not supported.");
					}

					if (interleaved) {
						final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
							.getPixelType());

						// TODO: Assign all elements in a for loop rather than
						// using many small System.arraycopy calls. Calling
						// System.arraycopy is less efficient than
						// element-by-element
						// copying for small array lengths (~24 elements or
						// less).
						// See: http://stackoverflow.com/a/12366983
						for (int i = 0; i < sourcePlane.length / bpp; i += bpp) {
							System.arraycopy(sourcePlane, i, destPlane.getData(), ((i *
								rgbChannelCount) + cIndex) * bpp, bpp);
						}
					}
					else {
						// TODO: Consider using destPlane.setData(sourcePlane)
						// instead.
						// Ideally would also make modifications to avoid the
						// initial
						// allocation overhead of the destPlane's internal
						// buffer.
						System.arraycopy(sourcePlane, 0, destPlane.getData(), cIndex *
							sourcePlane.length, sourcePlane.length);
					}
				}
				w.savePlane(imageIndex, planeIndex, destPlane);
			}
			catch (final FormatException e) {
				throw new ImgIOException(e);
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
		}

		try {
			w.close();
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * Check if the provided Metadata and Writer are sufficiently populated for
	 * writing.
	 */
	private void validate(final Metadata meta, final Writer w) {
		if (meta == null) throw new IllegalStateException(
			"No Metadata attached to " + w.getFormat().getFormatName() + " writer.");
	}

	/**
	 * @return An array of data corresponding to the given plane and channel
	 *         indices.
	 */
	private Object getPlaneArray(final Img<?> img, final int rgbChannelCount,
		final int cIndex, final int planeIndex)
	{
		// PlanarImg case
		if (PlanarImg.class.isAssignableFrom(img.getClass())) {
			final PlanarImg<?, ?> planarImg = (PlanarImg<?, ?>) img;
			return planarImg.getPlane(cIndex + (planeIndex * rgbChannelCount))
				.getCurrentStorageArray();
		}
		final int planeSize = (int) (img.dimension(0) * img.dimension(1));

		// ArrayImg case
		if (ArrayImg.class.isAssignableFrom(img.getClass())) {
			final ArrayImg<?, ?> arrayImg = (ArrayImg<?, ?>) img;
			final Object store = arrayImg.update(null);

			// For each array type, just create an appropriate container array
			// and System.arraycopy the relevant data.
			if (store instanceof ByteArray) {
				final byte[] source = ((ByteArray) store).getCurrentStorageArray();
				final byte[] bytes = new byte[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), bytes, 0, bytes.length);
				return bytes;
			}
			else if (store instanceof ShortArray) {
				final short[] source = ((ShortArray) store).getCurrentStorageArray();
				final short[] shorts = new short[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), shorts, 0, shorts.length);
				return shorts;
			}
			else if (store instanceof LongArray) {
				final long[] source = ((LongArray) store).getCurrentStorageArray();
				final long[] longs = new long[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), longs, 0, longs.length);
				return longs;
			}
			else if (store instanceof CharArray) {
				final char[] source = ((CharArray) store).getCurrentStorageArray();
				final char[] chars = new char[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), chars, 0, chars.length);
				return chars;
			}
			else if (store instanceof DoubleArray) {
				final double[] source = ((DoubleArray) store).getCurrentStorageArray();
				final double[] doubles = new double[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), doubles, 0, doubles.length);
				return doubles;
			}
			else if (store instanceof FloatArray) {
				final float[] source = ((FloatArray) store).getCurrentStorageArray();
				final float[] floats = new float[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), floats, 0, floats.length);
				return floats;
			}
			else if (store instanceof IntArray) {
				final int[] source = ((IntArray) store).getCurrentStorageArray();
				final int[] ints = new int[planeSize];
				System.arraycopy(source, planeSize * (cIndex + (planeIndex *
					rgbChannelCount)), ints, 0, ints.length);
				return ints;
			}
		}

		// Fallback default case - SLOW

		// Get dimensions array
		final long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);

		// Truncate X, Y axes
		final long[] lengths = Arrays.copyOfRange(dimensions, 2, dimensions.length);

		// Get non-X,Y position array
		final long[] planePosition = FormatTools.rasterToPosition(lengths, cIndex +
			(planeIndex * rgbChannelCount));

		// Copy plane positions back to dimensions array and set X, Y to start
		// at 0
		System.arraycopy(planePosition, 0, dimensions, 2, planePosition.length);
		dimensions[0] = dimensions[1] = 0;

		// Create a primitive array appropriate for the ImgPlus type
		final Class<?> typeClass = img.firstElement().getClass();
		Object array = null;

		if (GenericIntType.class.isAssignableFrom(typeClass)) {
			array = new int[planeSize];
		}
		else if (GenericByteType.class.isAssignableFrom(typeClass)) {
			array = new byte[planeSize];
		}
		else if (GenericShortType.class.isAssignableFrom(typeClass)) {
			array = new short[planeSize];
		}
		else if (LongType.class.isAssignableFrom(typeClass)) {
			array = new long[planeSize];
		}
		else if (DoubleType.class.isAssignableFrom(typeClass)) {
			array = new double[planeSize];
		}
		else if (FloatType.class.isAssignableFrom(typeClass)) {
			array = new float[planeSize];
		}

		// Ensure we have a compatible type
		if (array == null) {
			throw new IllegalArgumentException("Unsupported ImgPlus data type: " +
				typeClass);
		}

		// Create a cursor and move it to the first position of the requested
		// plane
		final RandomAccess<?> randomAccess = img.randomAccess();
		randomAccess.setPosition(dimensions);

		// TODO jump ahead to the requested channel? Not sure if that is needed or
		// not..

		// Iterate over the positions in this plane, copying the values at
		// each position to the output array.
		int idx = 0;
		for (int i = 0; i < img.dimension(1); i++) {
			for (int j = 0; j < img.dimension(0); j++) {
				final Object value = randomAccess.get();

				if (GenericIntType.class.isAssignableFrom(typeClass)) {
					((int[]) array)[idx++] = (int) ((ComplexType<?>) value)
						.getRealDouble();
				}
				else if (GenericByteType.class.isAssignableFrom(typeClass)) {
					((byte[]) array)[idx++] = (byte) ((ComplexType<?>) value)
						.getRealDouble();
				}
				else if (GenericShortType.class.isAssignableFrom(typeClass)) {
					((short[]) array)[idx++] = (short) ((ComplexType<?>) value)
						.getRealDouble();
				}
				else if (LongType.class.isAssignableFrom(typeClass)) {
					((long[]) array)[idx++] = (long) ((ComplexType<?>) value)
						.getRealDouble();
				}
				else if (DoubleType.class.isAssignableFrom(typeClass)) {
					((double[]) array)[idx++] = ((ComplexType<?>) value).getRealDouble();
				}
				else if (FloatType.class.isAssignableFrom(typeClass)) {
					((float[]) array)[idx++] = (float) ((ComplexType<?>) value)
						.getRealDouble();
				}
				randomAccess.fwd(0);
			}
			dimensions[1]++;
			randomAccess.setPosition(dimensions);
		}

		// Return the populated array
		return array;
	}

	/**
	 * @return The number of planes in the provided {@link Img}.
	 */
	private int getPlaneCount(final Img<?> img) {
		// PlanarImg case
		if (PlanarImg.class.isAssignableFrom(img.getClass())) {
			final PlanarImg<?, ?> planarImg = (PlanarImg<?, ?>) img;
			return planarImg.numSlices();
		}
		// General case
		int count = 1;

		for (int d = 2; d < img.numDimensions(); d++) {
			count *= img.dimension(d);
		}

		return count;
	}

	/**
	 * Uses the provided {@link SCIFIOImgPlus} to populate the minimum metadata
	 * fields necessary for writing.
	 *
	 * @param imageIndex
	 * @param id
	 */
	private void populateMeta(final Writer w, final SCIFIOImgPlus<?> img,
		final SCIFIOConfig config, final String id, final int imageIndex)
		throws FormatException, IOException, ImgIOException
	{
		statusService.showStatus("Initializing " + img.getName());
		final Metadata meta = w.getFormat().createMetadata();

		// Get format-specific metadata
		Metadata imgMeta = img.getMetadata();
		final List<ImageMetadata> imageMeta = new ArrayList<>();

		if (imgMeta == null) {
			imgMeta = new DefaultMetadata();
			imgMeta.createImageMetadata(1);
			imageMeta.add(imgMeta.get(0));
		}
		else {
			for (int i = 0; i < imgMeta.getImageCount(); i++) {
				imageMeta.add(new DefaultImageMetadata());
			}
		}

		// Create Img-specific ImageMetadata
		final int pixelType = utils().makeType(img.firstElement());

		// TODO is there some way to consolidate this with the isCompressible
		// method?
		final CalibratedAxis[] axes = new CalibratedAxis[img.numDimensions()];
		img.axes(axes);

		final long[] axisLengths = new long[img.numDimensions()];
		img.dimensions(axisLengths);

		for (final ImageMetadata iMeta : imageMeta) {
			iMeta.populate(img.getName(), Arrays.asList(axes), axisLengths, pixelType,
				true, false, false, false, true);

			// Adjust for RGB information
			if (img.getCompositeChannelCount() > 1) {
				if (config.imgSaverGetWriteRGB()) {
					iMeta.setPlanarAxisCount(3);
				}
				iMeta.setAxisType(2, Axes.CHANNEL);
				// Split Axes.CHANNEL if necessary
				if (iMeta.getAxisLength(Axes.CHANNEL) > img
					.getCompositeChannelCount())
				{
					iMeta.addAxis(Axes.get("Channel-planes", false), iMeta.getAxisLength(
						Axes.CHANNEL) / img.getCompositeChannelCount());
					iMeta.setAxisLength(Axes.CHANNEL, img.getCompositeChannelCount());
				}
			}
		}

		// Translate to the output metadata
		final Translator t = translatorService.findTranslator(imgMeta, meta, false);

		t.translate(imgMeta, imageMeta, meta);

		w.setMetadata(meta);
		w.setDest(id, imageIndex, config);
	}
}
