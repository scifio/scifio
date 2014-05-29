/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Translator;
import io.scif.Writer;
import io.scif.common.DataTools;
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

import net.imglib2.exception.ImgLibException;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;

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
	 * saveImg is the entry point for saving an {@link ImgPlus} The goal is to get
	 * to a {@link Writer} and {@link ImgPlus} which are then passed to
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
	// file, to get its full path, to provide the ImgPluSCIFIOImgPlusending that,
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
							// thus was double counted (once in pass 1, once in pass 2)
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

		// Check for PlanarAccess
		final PlanarAccess<?> planarAccess = utils().getPlanarAccess(img);
		if (planarAccess == null) {
			throw new IncompatibleTypeException(new ImgLibException(), "Only " +
				PlanarAccess.class + " images supported at this time.");
		}

		final PlanarImg<?, ?> planarImg = (PlanarImg<?, ?>) planarAccess;

		final int sliceCount = countSlices(img);

		final Class<?> arrayType =
			planarImg.getPlane(0).getCurrentStorageArray().getClass();

		if (w == null) {
			if (id == null || id.length() == 0) {
				throw new ImgIOException(
					"No output destination or pre-configured Writer was provided, and" +
					" no way to determine the desired output path. Default value:" +
					" ImgPlus's source.");
			}
			return writeImg(id, img, planarImg, imageIndex, config, arrayType,
				sliceCount);
		}

		return writeImg(w, id, img, planarImg, imageIndex,
			config, arrayType, sliceCount);
	}

	/**
	 * Intermediate {@link #writeImg} method. Creates a {@link Writer} for the
	 * given id.
	 */
	private Metadata writeImg(final String id, final SCIFIOImgPlus<?> imgPlus,
		final PlanarImg<?, ?> img, final int imageIndex, final SCIFIOConfig config,
		final Class<?> arrayType, final int sliceCount) throws ImgIOException,
		IncompatibleTypeException
	{
		// Create a Writer for the given id
		final Writer w = initializeWriter(id, arrayType);

		return writeImg(w, id, imgPlus, img, imageIndex, config, arrayType,
			sliceCount);
	}

	/**
	 * Intermediate {@link #writeImg} method. Ensures the given writer has proper
	 * {@link Metadata}, or creates it if possible.
	 */
	private Metadata writeImg(final Writer w, final String id,
		final SCIFIOImgPlus<?> imgPlus, final PlanarImg<?, ?> img,
		final int imageIndex, final SCIFIOConfig config, final Class<?> arrayType,
		final int sliceCount) throws ImgIOException, IncompatibleTypeException
	{
		if (w.getMetadata() == null) {
			if (id == null || id.length() == 0) {
				throw new ImgIOException(
					"A Writer with no Metadata was provided, with no way to determine "
						+ "the desired output path. Default value: ImgPlus's source.");
			}
			try {
				populateMeta(w, imgPlus, config, id, imageIndex);
			}
			catch (final FormatException e) {
				throw new ImgIOException(e);
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
		}

		return writeImg(w, img, arrayType, imageIndex, sliceCount);
	}

	/**
	 * Terminal {@link #writeImg} method. Performs actual pixel output.
	 */
	private Metadata writeImg(final Writer w, final PlanarImg<?, ?> img,
		final Class<?> arrayType, final int imageIndex, final int sliceCount)
		throws ImgIOException, IncompatibleTypeException
	{
		if (img.numDimensions() > 0) {
			final long startTime = System.currentTimeMillis();

			// write pixels
			writePlanes(w, imageIndex, img, arrayType);

			// Print time statistics
			final long endTime = System.currentTimeMillis();
			final float time = (endTime - startTime) / 1000f;
			statusService.showStatus(sliceCount, sliceCount, w.getMetadata()
				.getDatasetName() +
				": wrote " + sliceCount + " planes in " + time + " s");
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
	 * byte[]) and saving the plane. Currently only {@link PlanarImg} is
	 * supported.
	 * 
	 * @param arrayType2
	 * @throws IncompatibleTypeException
	 */
	private void writePlanes(final Writer w, final int imageIndex,
		final PlanarImg<?, ?> planarImg, final Class<?> arrayType)
		throws ImgIOException, IncompatibleTypeException
	{
		// Get basic statistics
		final int planeCount = planarImg.numSlices();
		final Metadata mOut = w.getMetadata();
		final int rgbChannelCount =
			mOut.get(imageIndex).isMultichannel() ? (int) mOut.get(imageIndex)
				.getAxisLength(Axes.CHANNEL) : 1;
		final boolean interleaved =
			mOut.get(imageIndex).getInterleavedAxisCount() > 0;

		byte[] sourcePlane = null;

		// iterate over each plane
		final long planeOutCount = w.getMetadata().get(imageIndex).getPlaneCount();

		if (planeOutCount < planeCount / rgbChannelCount) {
			// Warn that some planes were truncated (e.g. going from 4D format to
			// 3D)
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

				final long[] planarLengths =
					meta.get(imageIndex).getAxesLengthsPlanar();
				final long[] planarMin =
					SCIFIOMetadataTools.modifyPlanar(imageIndex, meta,
						new long[planarLengths.length]);
				final ByteArrayPlane destPlane =
					new ByteArrayPlane(getContext(), meta.get(imageIndex), planarMin,
						planarLengths);

				for (int cIndex = 0; cIndex < rgbChannelCount; cIndex++) {
					final Object curPlane =
						planarImg.getPlane(cIndex + (planeIndex * rgbChannelCount))
							.getCurrentStorageArray();

					// Convert current plane if necessary
					if (arrayType == int[].class) {
						sourcePlane = DataTools.intsToBytes((int[]) curPlane, false);
					}
					else if (arrayType == byte[].class) {
						sourcePlane = (byte[]) curPlane;
					}
					else if (arrayType == short[].class) {
						sourcePlane = DataTools.shortsToBytes((short[]) curPlane, false);
					}
					else if (arrayType == long[].class) {
						sourcePlane = DataTools.longsToBytes((long[]) curPlane, false);
					}
					else if (arrayType == double[].class) {
						sourcePlane = DataTools.doublesToBytes((double[]) curPlane, false);
					}
					else if (arrayType == float[].class) {
						sourcePlane = DataTools.floatsToBytes((float[]) curPlane, false);
					}
					else {
						throw new IncompatibleTypeException(new ImgLibException(),
							"PlanarImgs of type " + planarImg.getPlane(0).getClass() +
								" not supported.");
					}

					if (interleaved) {
						final int bpp =
							FormatTools.getBytesPerPixel(meta.get(imageIndex).getPixelType());

						// TODO: Assign all elements in a for loop rather than
						// using many small System.arraycopy calls. Calling
						// System.arraycopy is less efficient than element-by-element
						// copying for small array lengths (~24 elements or less).
						// See: http://stackoverflow.com/a/12366983
						for (int i = 0; i < sourcePlane.length / bpp; i += bpp) {
							System.arraycopy(sourcePlane, i, destPlane.getData(),
								((i * rgbChannelCount) + cIndex) * bpp, bpp);
						}
					}
					else {
						// TODO: Consider using destPlane.setData(sourcePlane) instead.
						// Ideally would also make modifications to avoid the initial
						// allocation overhead of the destPlane's internal buffer.
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
	 * Creates a new {@link Writer} and sets its id to the provided String.
	 */
	private Writer initializeWriter(final String id, final Class<?> arrayType)
		throws ImgIOException
	{
		Writer writer = null;

		// if we know this image will pass to SCIFIO to be saved,
		// then delete the old file if it exists
		if (arrayType == int[].class || arrayType == byte[].class ||
			arrayType == short[].class || arrayType == long[].class ||
			arrayType == double[].class || arrayType == float[].class)
		{
			final File f = new File(id);
			if (f.exists()) {
				f.delete();
			}
		}

		try {
			writer = formatService.getWriterByExtension(id);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}

		return writer;
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
		final List<ImageMetadata> imageMeta = new ArrayList<ImageMetadata>();

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

		for (int i = 0; i < imageMeta.size(); i++) {
			final ImageMetadata iMeta = imageMeta.get(i);
			iMeta.populate(Arrays.asList(axes), axisLengths, pixelType, true, false,
				false, false, true);

			// Adjust for RGB information
			if (img.getCompositeChannelCount() > 1) {
				if (config.imgSaverGetWriteRGB()) {
					iMeta.setPlanarAxisCount(3);
				}
				iMeta.setAxisType(2, Axes.CHANNEL);
				// Split Axes.CHANNEL if necessary
				if (iMeta.getAxisLength(Axes.CHANNEL) > img.getCompositeChannelCount())
				{
					iMeta.addAxis(Axes.get("Channel-planes", false), iMeta
						.getAxisLength(Axes.CHANNEL) /
						img.getCompositeChannelCount());
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
