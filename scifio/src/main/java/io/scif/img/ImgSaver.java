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

import io.scif.ByteArrayPlane;
import io.scif.DefaultMetadata;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Writer;
import io.scif.common.DataTools;
import io.scif.config.SCIFIOConfig;
import io.scif.services.FormatService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
	public void saveImg(final String id, final Img<?> img) throws ImgIOException,
		IncompatibleTypeException
	{
		saveImg(id, img, new SCIFIOConfig());
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
	public void saveImg(final String id, final ImgPlus<?> img,
		final int imageIndex) throws ImgIOException, IncompatibleTypeException
	{
		saveImg(id, img, imageIndex, new SCIFIOConfig());
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
	public void saveImg(final String id, final Img<?> img,
		final SCIFIOConfig config) throws ImgIOException, IncompatibleTypeException
	{
		saveImg(id, ImgPlus.wrap(img), 0, config);
	}

	/**
	 * As {@link #saveImg(String, ImgPlus, int)} with configuration options.
	 * 
	 * @param id
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final String id, final ImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		img.setSource(id);
		img.setName(new File(id).getName());
		saveImg(initializeWriter(id, img, imageIndex, config), img, imageIndex,
			false, config);
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
		saveImg(w, img, new SCIFIOConfig());
	}

	// TODO IFormatHandler needs to be promoted to be able to get the current
	// file, to get its full path, to provide the ImgPlus
	// pending that, these two IFormatWriter methods are not guaranteed to be
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
	public void
		saveImg(final Writer w, final ImgPlus<?> img, final int imageIndex)
			throws ImgIOException, IncompatibleTypeException
	{
		saveImg(w, img, imageIndex, new SCIFIOConfig());
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
		saveImg(w, ImgPlus.wrap(img), 0, config);
	}

	// TODO IFormatHandler needs to be promoted to be able to get the current
	// file, to get its full path, to provide the ImgPlus
	// pending that, these two IFormatWriter methods are not guaranteed to be
	// useful
	/**
	 * As {@link #saveImg(Writer, ImgPlus, int)}, with configuration options.
	 * 
	 * @param w
	 * @param img
	 * @param config Configuration information to use for this write.
	 * @throws ImgIOException
	 * @throws IncompatibleTypeException
	 */
	public void saveImg(final Writer w, final ImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		saveImg(w, img, imageIndex, true, config);
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

	/* Entry point for writePlanes method, the actual workhorse to save pixels to disk */
	private void saveImg(final Writer w, final ImgPlus<?> img,
		final int imageIndex, final boolean initializeWriter,
		final SCIFIOConfig config) throws ImgIOException, IncompatibleTypeException
	{

		// use the ImgPlus to calculate necessary metadata if
		if (initializeWriter) {
			populateMeta(w.getMetadata(), img, imageIndex, config);
		}

		if (img.getSource().length() == 0) {
			throw new ImgIOException("Provided Image has no attached source.");
		}

		final long startTime = System.currentTimeMillis();
		final String id = img.getSource();
		final int sliceCount = countSlices(img);

		// write pixels
		writePlanes(w, img, imageIndex, config);

		final long endTime = System.currentTimeMillis();
		final float time = (endTime - startTime) / 1000f;
		statusService.showStatus(sliceCount, sliceCount, id + ": wrote " +
			sliceCount + " planes in " + time + " s");
	}

	// -- Helper Methods --

	/* Counts the number of slices in the provided ImgPlus.
	 * NumSlices = product of the sizes of all non-X,Y planes.
	 */
	private int countSlices(final ImgPlus<?> img) {

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
	 * Iterates through the planes of the provided {@link ImgPlus}, converting
	 * each to a byte[] if necessary (the SCIFIO writer requires a byte[]) and
	 * saving the plane. Currently only {@link PlanarImg} is supported.
	 * 
	 * @throws IncompatibleTypeException
	 */
	private void writePlanes(Writer w, final ImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException,
		IncompatibleTypeException
	{
		final PlanarAccess<?> planarAccess = utils().getPlanarAccess(img);
		if (planarAccess == null) {
			throw new IncompatibleTypeException(new ImgLibException(), "Only " +
				PlanarAccess.class + " images supported at this time.");
		}

		final PlanarImg<?, ?> planarImg = (PlanarImg<?, ?>) planarAccess;
		final int planeCount = planarImg.numSlices();
		final Metadata mOut = w.getMetadata();
		final int rgbChannelCount =
			mOut.get(imageIndex).isMultichannel() ? (int) mOut.get(imageIndex)
				.getAxisLength(Axes.CHANNEL) : 1;
		final boolean interleaved =
			mOut.get(imageIndex).getInterleavedAxisCount() > 0;

		if (img.numDimensions() > 0) {
			final Class<?> arrayType =
				planarImg.getPlane(0).getCurrentStorageArray().getClass();

			byte[] sourcePlane = null;

			// if we know this image will pass to SCIFIO to be saved,
			// then delete the old file if it exists
			if (arrayType == int[].class || arrayType == byte[].class ||
				arrayType == short[].class || arrayType == long[].class ||
				arrayType == double[].class || arrayType == float[].class)
			{
				final File f = new File(img.getSource());
				if (f.exists()) {
					f.delete();
					w = initializeWriter(img.getSource(), img, imageIndex, config);
				}
			}

			// iterate over each plane
			final long planeOutCount =
				w.getMetadata().get(imageIndex).getPlaneCount();

			if (planeOutCount < planeCount / rgbChannelCount) {
				// Warn that some planes were truncated (e.g. going from 4D format to
				// 3D)
				statusService.showStatus(0, 0, "Source dataset contains: " +
					planeCount + " planes, but writer format only supports: " +
					rgbChannelCount * planeOutCount, true);
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
							sourcePlane =
								DataTools.doublesToBytes((double[]) curPlane, false);
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
								FormatTools.getBytesPerPixel(meta.get(imageIndex)
									.getPixelType());

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
	private Writer initializeWriter(final String id, final ImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException
	{
		Writer writer = null;
		Metadata meta = null;

		try {
			writer = formatService.getWriterByExtension(id);
			meta = writer.getFormat().createMetadata();

			populateMeta(meta, img, imageIndex, config);

			writer.setMetadata(meta);

			writer.setDest(id, imageIndex, config);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}

		return writer;
	}

	/**
	 * Uses the provided {@link ImgPlus} to populate the minimum metadata fields
	 * necessary for writing.
	 */
	private void populateMeta(final Metadata meta, final ImgPlus<?> img,
		final int imageIndex, final SCIFIOConfig config) throws ImgIOException
	{
		statusService.showStatus("Initializing " + img.getName());

		final int pixelType = utils().makeType(img.firstElement());

		// TODO is there some way to consolidate this with the isCompressible
		// method?
		final CalibratedAxis[] axes = new CalibratedAxis[img.numDimensions()];
		img.axes(axes);

		final long[] axisLengths = new long[img.numDimensions()];
		img.dimensions(axisLengths);

		final DefaultMetadata imgplusMeta = new DefaultMetadata();

		// Create fresh metadata in source and destination
		imgplusMeta.createImageMetadata(imageIndex + 1);

		imgplusMeta.get(imageIndex).populate(Arrays.asList(axes), axisLengths,
			pixelType, true, false, false, false, true);

		// Adjust for RGB information
		if (img.getCompositeChannelCount() > 1) {
			final ImageMetadata m = imgplusMeta.get(imageIndex);
			if (config.imgSaverGetWriteRGB()) {
				m.setPlanarAxisCount(3);
			}
			m.setAxisType(2, Axes.CHANNEL);
			// Split Axes.CHANNEL if necessary
			if (m.getAxisLength(Axes.CHANNEL) > img.getCompositeChannelCount()) {
				m.addAxis(Axes.get("Channel-planes", false), m
					.getAxisLength(Axes.CHANNEL) /
					img.getCompositeChannelCount());
				m.setAxisLength(Axes.CHANNEL, img.getCompositeChannelCount());
			}
		}

		// Translate to trigger any format-specific translation

		translatorService.translate(imgplusMeta, meta, false);
	}
}
