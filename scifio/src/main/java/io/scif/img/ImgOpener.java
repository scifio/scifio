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

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.filters.ChannelFiller;
import io.scif.filters.ChannelSeparator;
import io.scif.filters.MinMaxFilter;
import io.scif.filters.ReaderFilter;
import io.scif.img.ImgOptions.CheckMode;
import io.scif.img.cell.SCIFIOCellImgFactory;
import io.scif.img.converters.PlaneConverter;
import io.scif.img.converters.PlaneConverterService;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.cell.AbstractCellImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;

/**
 * Reads in an {@link ImgPlus} using SCIFIO.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class ImgOpener extends AbstractImgIOComponent {

	@Parameter
	private StatusService statusService;

	@Parameter
	private PlaneConverterService pcService;

	// -- Constructors --

	public ImgOpener() {
		super();
	}

	public ImgOpener(final Context ctx) {
		super(ctx);
	}

	// -- Static methods --

	// -- ImgOpener methods --

	/**
	 * Reads in an {@link ImgPlus} from the first image of the given source.
	 * 
	 * @param source - the location of the dataset to open
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	@SuppressWarnings("rawtypes")
	public ImgPlus openImg(final String source) throws ImgIOException {
		return openImg(source, new ImgOptions());
	}

	/**
	 * Reads in an {@link ImgPlus} from the first image of the given source.
	 * 
	 * @param source - the location of the dataset to open
	 * @param type - The {@link Type} T of the output {@link ImgPlus}.
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type) throws ImgIOException
	{
		return openImg(source, type, new ImgOptions());
	}

	/**
	 * Reads in an {@link ImgPlus} from the specified index of the given source.
	 * Can specify a variety of {@link ImgOptions}.
	 * 
	 * @param source - the location of the dataset to open
	 * @param imageIndex - the index within the dataset to open
	 * @param type - The {@link Type} T of the output {@link ImgPlus}.
	 * @param imgOptions - {@link ImgOptions} to use when opening this dataset
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ImgPlus openImg(final String source, final ImgOptions imgOptions)
		throws ImgIOException
	{
		final Reader r = createReader(source, imgOptions);
		final RealType t = getType(r, imgOptions);

		final ImgFactoryHeuristic heuristic = getHeuristic(imgOptions);

		ImgFactory imgFactory;
		try {
			if (NativeType.class.isAssignableFrom(t.getClass())) imgFactory =
				heuristic.createFactory(r.getMetadata(), imgOptions.getImgModes(),
					(NativeType) t);
			else return null;
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}

		return openImg(r, t, imgFactory, imgOptions);
	}

	/**
	 * Reads in an {@link ImgPlus} from the specified index of the given source.
	 * Can specify the Type that should be opened.
	 * 
	 * @param source - the location of the dataset to open
	 * @param type - The {@link Type} T of the output {@link ImgPlus}.
	 * @param imgOptions - {@link ImgOptions} to use when opening this dataset
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String source, final T type, final ImgOptions imgOptions)
		throws ImgIOException
	{
		final Reader r = createReader(source, imgOptions);

		return openImg(r, type, imgOptions);
	}

	/**
	 * @param source - the location of the dataset to open
	 * @param imgFactory - The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	@SuppressWarnings({ "rawtypes" })
	public ImgPlus openImg(final String source, final ImgFactory imgFactory)
		throws ImgIOException
	{
		final ImgOptions imgOptions = new ImgOptions().setComputeMinMax(true);

		return openImg(source, imgFactory, imgOptions);
	}

	/**
	 * @param source - the location of the dataset to open
	 * @param imgFactory - The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param imgOptions - {@link ImgOptions} to use when opening this dataset
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ImgPlus openImg(final String source, final ImgFactory imgFactory,
		final ImgOptions imgOptions) throws ImgIOException
	{
		final Reader r = createReader(source, imgOptions);
		final RealType t = getType(r, imgOptions);
		return openImg(r, t, imgFactory, imgOptions);
	}

	/**
	 * @param source - the location of the dataset to open
	 * @param imgFactory - The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param type - The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @param imgOptions - {@link ImgOptions} to use when opening this dataset
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T>> ImgPlus<T> openImg(final String source,
		final ImgFactory<T> imgFactory, final T type) throws ImgIOException
	{

		final ImgOptions imgOptions = new ImgOptions().setComputeMinMax(true);
		final Reader r = createReader(source, imgOptions);

		return openImg(r, type, imgFactory, imgOptions);
	}

	/**
	 * @param reader - An initialized {@link Reader} to use for reading image
	 *          data.
	 * @param imgFactory - The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param type - The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final Reader reader, final T type, final ImgOptions imgOptions)
		throws ImgIOException
	{

		final ImgFactoryHeuristic heuristic = getHeuristic(imgOptions);

		ImgFactory<T> imgFactory;
		try {
			imgFactory =
				heuristic.createFactory(reader.getMetadata(), imgOptions.getImgModes(),
					type);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}

		return openImg(reader, type, imgFactory, imgOptions);
	}

	/**
	 * Reads in an {@link ImgPlus} from the given initialized {@link Reader},
	 * using the given {@link ImgFactory} to construct the {@link Img}. The
	 * {@link Type} T to read is defined by the third parameter.
	 * <p>
	 * NB: Any Reader provided must be wrapped by a {@link ChannelSeparator}
	 * filter.
	 * </p>
	 * 
	 * @param reader - An initialized {@link Reader} to use for reading image
	 *          data.
	 * @param imgFactory - The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param type - The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @param imgOptions - {@link ImgOptions} to use when opening this dataset
	 * @return - the {@link ImgPlus} or null
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T>> ImgPlus<T> openImg(final Reader reader,
		final T type, final ImgFactory<T> imgFactory, final ImgOptions imgOptions)
		throws ImgIOException
	{
		final int imageIndex = imgOptions.getIndex();

		// create image and read metadata
		final long[] dimLengths =
			utils().getConstrainedLengths(reader.getMetadata(), imgOptions);
		if (SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
			((SCIFIOCellImgFactory<?>) imgFactory).setReader(reader);
			((SCIFIOCellImgFactory<?>) imgFactory).setSubRegion(imgOptions
				.getRegion());
		}
		final Img<T> img = imgFactory.create(dimLengths, type);
		final ImgPlus<T> imgPlus = makeImgPlus(img, reader, imgOptions);

		final String id = reader.getCurrentFile();
		imgPlus.setSource(id);
		imgPlus.initializeColorTables(reader.getPlaneCount(imageIndex));

		// If we have a planar img, read the planes now. Otherwise they
		// will be read on demand.
		if (AbstractCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
			statusService.showStatus("Created CellImg for dynamic loading");
		}
		else {
			final float startTime = System.currentTimeMillis();
			final int planeCount = reader.getPlaneCount(imageIndex);
			try {
				readPlanes(reader, imageIndex, type, imgPlus, imgOptions);
			}
			catch (final FormatException e) {
				throw new ImgIOException(e);
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
			final long endTime = System.currentTimeMillis();
			final float time = (endTime - startTime) / 1000f;
			statusService.showStatus(id + ": read " + planeCount + " planes in " +
				time + "s");
		}

		return imgPlus;
	}

	// -- Helper methods --

	@SuppressWarnings("rawtypes")
	private RealType getType(final Reader r, final ImgOptions options) {
		int imageIndex = 0;
		if (options != null) imageIndex = options.getIndex();

		return utils().makeType(r.getMetadata().getPixelType(imageIndex));
	}

	private ImgFactoryHeuristic getHeuristic(final ImgOptions imgOptions) {
		ImgFactoryHeuristic heuristic = imgOptions.getImgFactoryHeuristic();

		if (heuristic == null) heuristic = new DefaultImgFactoryHeuristic();

		return heuristic;
	}

	/**
	 * @param io - An ImgOpener instance
	 * @param source - Dataset source to open
	 * @param imgOptions - Options object for opening this dataset
	 * @return A Reader initialized to open the specified id
	 */
	private Reader createReader(final String source, final ImgOptions imgOptions)
		throws ImgIOException
	{

		final boolean openFile = imgOptions.getCheckMode().equals(CheckMode.DEEP);
		final boolean computeMinMax = imgOptions.isComputeMinMax();
		statusService.showStatus("Initializing " + source);

		ReaderFilter r = null;
		try {
			r = scifio().initializer().initializeReader(source, openFile);
			r.enable(ChannelSeparator.class);
			if (computeMinMax) r.enable(MinMaxFilter.class);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
		catch (final InstantiableException e) {
			throw new ImgIOException(e);
		}
		return r;
	}

	/** Compiles an N-dimensional list of axis types from the given reader. */
	private AxisType[] getDimTypes(final Metadata m) {
		final int sizeX = m.getAxisLength(0, Axes.X);
		final int sizeY = m.getAxisLength(0, Axes.Y);
		final int sizeZ = m.getAxisLength(0, Axes.Z);
		final int sizeT = m.getAxisLength(0, Axes.TIME);
		final int sizeC = m.getEffectiveSizeC(0);
		final String dimOrder = FormatTools.findDimensionOrder(m, 0);
		final List<AxisType> dimTypes = new ArrayList<AxisType>();

		// add core dimensions
		for (final char dim : dimOrder.toCharArray()) {
			switch (dim) {
				case 'X':
					if (sizeX > 0) dimTypes.add(Axes.X);
					break;
				case 'Y':
					if (sizeY > 0) dimTypes.add(Axes.Y);
					break;
				case 'Z':
					if (sizeZ > 1) dimTypes.add(Axes.Z);
					break;
				case 'T':
					if (sizeT > 1) dimTypes.add(Axes.TIME);
					break;
				case 'C':
					if (sizeC > 1) dimTypes.add(Axes.CHANNEL);
					break;
			}
		}

		return dimTypes.toArray(new AxisType[0]);
	}

	/** Compiles an N-dimensional list of calibration values. */
	private double[] getCalibration(final Metadata m) {
		final int sizeX = m.getAxisLength(0, Axes.X);
		final int sizeY = m.getAxisLength(0, Axes.Y);
		final int sizeZ = m.getAxisLength(0, Axes.Z);
		final int sizeT = m.getAxisLength(0, Axes.TIME);
		final int sizeC = m.getAxisLength(0, Axes.CHANNEL);
		final String dimOrder = FormatTools.findDimensionOrder(m, 0);

		final List<Double> calibrationList = new ArrayList<Double>();

		// add core dimensions
		for (int i = 0; i < dimOrder.length(); i++) {
			final char dim = dimOrder.charAt(i);
			switch (dim) {
				case 'X':
					if (sizeX > 0) {
						calibrationList.add(m.getAxis(0, Axes.X).calibration());
					}
					break;
				case 'Y':
					if (sizeY > 0) {
						calibrationList.add(m.getAxis(0, Axes.Y).calibration());
					}
					break;
				case 'Z':
					if (sizeZ > 0) {
						calibrationList.add(m.getAxis(0, Axes.Z).calibration());
					}
					break;
				case 'T':
					if (sizeT > 0) {
						calibrationList.add(m.getAxis(0, Axes.TIME).calibration());
					}
					break;
				case 'C':
					if (sizeC > 0) {
						calibrationList.add(m.getAxis(0, Axes.CHANNEL).calibration());
					}
					break;
			}
		}

		// convert result to primitive array
		final double[] calibration = new double[calibrationList.size()];
		for (int i = 0; i < calibration.length; i++) {
			calibration[i] = calibrationList.get(i);
		}
		return calibration;
	}

	/**
	 * Wraps the given {@link Img} in an {@link ImgPlus} with metadata
	 * corresponding to the specified initialized {@link Reader}.
	 */
	private <T extends RealType<T>> ImgPlus<T> makeImgPlus(final Img<T> img,
		final Reader r, final ImgOptions options) throws ImgIOException
	{
		final String id = r.getCurrentFile();
		final File idFile = new File(id);
		final String name = idFile.exists() ? idFile.getName() : id;

		final AxisType[] dimTypes = getDimTypes(r.getMetadata());
		final double[] cal = getCalibration(r.getMetadata());

		final Reader base;
		try {
			base = unwrap(r);
		}
		catch (final FormatException exc) {
			throw new ImgIOException(exc);
		}
		catch (final IOException exc) {
			throw new ImgIOException(exc);
		}

		final Metadata meta = r.getMetadata();
		final int rgbChannelCount = base.getMetadata().getRGBChannelCount(0);
		final int validBits = meta.getBitsPerPixel(0);

		final ImgPlus<T> imgPlus = new SCIFIOImgPlus<T>(img, name, dimTypes, cal);
		imgPlus.setValidBits(validBits);

		int compositeChannelCount = rgbChannelCount;
		if (rgbChannelCount == 1) {
			// HACK: Support ImageJ color mode embedded in TIFF files.
			final String colorMode = (String) meta.getTable().get("Color mode");
			if ("composite".equals(colorMode)) {
				compositeChannelCount = meta.getAxisLength(0, Axes.CHANNEL);
			}
		}
		imgPlus.setCompositeChannelCount(compositeChannelCount);

		return imgPlus;
	}

	/**
	 * Finds the lowest level wrapped reader, preferably a {@link ChannelFiller},
	 * but otherwise the base reader. This is useful for determining whether the
	 * input data is intended to be viewed with multiple channels composited
	 * together.
	 */
	private Reader unwrap(final Reader r) throws FormatException, IOException {
		if (!(r instanceof ReaderFilter)) return r;
		final ReaderFilter rf = (ReaderFilter) r;
		return rf.getTail();
	}

	/**
	 * Reads planes from the given initialized {@link Reader} into the specified
	 * {@link Img}.
	 */
	private <T extends RealType<T>> void readPlanes(final Reader r,
		final int imageIndex, final T type, final ImgPlus<T> imgPlus,
		final ImgOptions imgOptions) throws FormatException, IOException
	{
		// TODO - create better container types; either:
		// 1) an array container type using one byte array per plane
		// 2) as #1, but with an Reader reference reading planes on demand
		// 3) as PlanarRandomAccess, but with an Reader reference
		// reading planes on demand

		// PlanarRandomAccess is useful for efficient access to pixels in ImageJ
		// (e.g., getPixels)
		// #1 is useful for efficient SCIFIO import, and useful for tools
		// needing byte arrays (e.g., BufferedImage Java3D texturing by reference)
		// #2 is useful for efficient memory use for tools wanting matching
		// primitive arrays (e.g., virtual stacks in ImageJ)
		// #3 is useful for efficient memory use

		// get container
		final PlanarAccess<?> planarAccess = utils().getPlanarAccess(imgPlus);
		@SuppressWarnings("rawtypes")
		final RealType inputType =
			utils().makeType(r.getMetadata().getPixelType(0));
		final T outputType = type;
		final boolean compatibleTypes =
			outputType.getClass().isAssignableFrom(inputType.getClass());

		// populate planes
		final boolean isPlanar = planarAccess != null && compatibleTypes;
		final boolean isArray =
			utils().getArrayAccess(imgPlus) != null && compatibleTypes;

		Plane plane = null;

		final SubRegion region = imgOptions.getRegion();
		final boolean checkSubregion = region != null;

		int x, y, w, h, zPos, cPos, tPos;

		// Z,C,T offsets and maximum values
		final DimRange[] index =
			new DimRange[] { new DimRange(0l), new DimRange(0l), new DimRange(0l) };

		final Metadata meta = r.getMetadata();

		// Set the base
		x = y = w = h = 0;
		w = meta.getAxisLength(imageIndex, Axes.X);
		h = meta.getAxisLength(imageIndex, Axes.Y);

		// Default these to the last index position. If any index wasn't seen
		// this will result in index[xPos] = 0 for looking up the plane index,
		// equivalent to a length of 1 for that axis.
		zPos = cPos = tPos = 2;

		// subregion dimension index
		int dimsPlaced = 0;

		// CZT index
		int cztPlaced = 0;

		// current axis
		int axisIndex = 0;
		final CalibratedAxis[] axes = meta.getAxes(imageIndex);

		// Total # planes in this subregion
		int planeCount = 1;

		// Populate subregion, plane count, and CZT information
		while (axisIndex < axes.length) {
			final AxisType axisType = axes[axisIndex++].type();

			if (axisType.equals(Axes.X)) {
				if (applySubregion(checkSubregion, Axes.X, dimsPlaced, region))
				{
					x = region.getRange(Axes.X).head().intValue();
					final int maxX = region.getRange(Axes.X).tail().intValue();
					w = maxX - x;
					dimsPlaced++;
				}
			}
			else if (axisType.equals(Axes.Y)) {
				if (applySubregion(checkSubregion, Axes.Y, dimsPlaced, region))
				{
					y = region.getRange(Axes.Y).head().intValue();
					final int maxY = region.getRange(Axes.Y).tail().intValue();
					h = maxY - y;
					dimsPlaced++;
				}
			}
			else if (axisType.equals(Axes.CHANNEL)) {
				final int c = meta.getAxisLength(imageIndex, Axes.CHANNEL);

				DimRange cVals = null;

				if (applySubregion(checkSubregion, Axes.CHANNEL, dimsPlaced, region)) {
					cVals = region.getRange(Axes.CHANNEL);
					dimsPlaced++;
				}

				if (cVals == null) cVals = new DimRange(0l, (long) (c - 1));

				index[cztPlaced] = cVals;
				cPos = cztPlaced++;
				planeCount *= cVals.indices().size();
			}
			else if (axisType.equals(Axes.Z)) {
				final int z = meta.getAxisLength(imageIndex, Axes.Z);
				DimRange zVals = null;

				if (applySubregion(checkSubregion, Axes.Z, dimsPlaced, region)) {
					zVals = region.getRange(Axes.Z);
					dimsPlaced++;
				}

				if (zVals == null) zVals = new DimRange(0l, (long) (z - 1));

				index[cztPlaced] = zVals;
				zPos = cztPlaced++;
				planeCount *= zVals.indices().size();
			}
			else if (axisType.equals(Axes.TIME)) {
				final int t = meta.getAxisLength(imageIndex, Axes.TIME);

				DimRange tVals = null;

				if (applySubregion(checkSubregion, Axes.TIME, dimsPlaced, region)) {
					tVals = region.getRange(Axes.TIME);
					dimsPlaced++;
				}

				if (tVals == null) tVals = new DimRange(0l, (long) (t - 1));

				index[cztPlaced] = tVals;
				tPos = cztPlaced++;
				planeCount *= tVals.indices().size();
			}
		}

		int currentPlane = 0;

		PlaneConverter converter = imgOptions.getPlaneConverter();

		if (converter == null) {
			// if it's we have a PlanarAccess we can use a PlanarAccess converter,
			// otherwise
			// we can use a more general RandomAccess approach
			if (isArray) {
				converter = pcService.getArrayConverter();
			}
			else if (isPlanar) {
				converter = pcService.getPlanarConverter();
			}
			else converter = pcService.getDefaultConverter();
		}

		for (final Long i : index[2].indices()) {
			for (final Long j : index[1].indices()) {
				for (final Long k : index[0].indices()) {

					final Long[] indices = new Long[] { k, j, i };
					final Long z = indices[zPos];
					final Long c = indices[cPos];
					final Long t = indices[tPos];

					// get the plane index in the underlying dataset
					final int planeIndex =
						FormatTools.getIndex(r, imageIndex, z.intValue(), c.intValue(), t
							.intValue());
					statusService.showStatus(currentPlane + 1, planeCount,
						"Reading plane");

					// open the subregion of the current plane
					if (plane == null) plane =
						r.openPlane(imageIndex, planeIndex, x, y, w, h);
					else {
						r.openPlane(0, planeIndex, plane, x, y, w, h);
					}

					// copy the data to the ImgPlus
					converter.populatePlane(r, imageIndex, currentPlane,
						plane.getBytes(), imgPlus, imgOptions);

					// store color table
					imgPlus.setColorTable(plane.getColorTable(), currentPlane);
					currentPlane++;
				}
			}
		}

		if (imgOptions.isComputeMinMax()) populateMinMax(r, imgPlus, imageIndex);
	}
	
	/**
	 * Returns true if the specified Axis is listed in the provided Subregion,
	 * and checkSubregion is enabled, and dimsPlaced < subRegion.size(). Indicates
	 * that it is safe to apply the SubRegion constraints to the specified axis.
	 */
	private boolean applySubregion(boolean checkSubregion, AxisType axis,
		int dimsPlaced, SubRegion region)
	{
		return checkSubregion && dimsPlaced < region.size() &&
			region.hasRange(axis);
	}

	private void populateMinMax(final Reader r, final ImgPlus<?> imgPlus,
		final int imageIndex) throws FormatException, IOException
	{
		final int sizeC = r.getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);
		final ReaderFilter rf = (ReaderFilter) r;
		MinMaxFilter minMax = null;
		try {
			minMax = rf.enable(MinMaxFilter.class);
		}
		catch (final InstantiableException e) {
			throw new FormatException(e);
		}
		for (int c = 0; c < sizeC; c++) {
			final Double min = minMax.getChannelKnownMinimum(imageIndex, c);
			final Double max = minMax.getChannelKnownMaximum(imageIndex, c);
			imgPlus.setChannelMinimum(c, min == null ? Double.NaN : min);
			imgPlus.setChannelMaximum(c, max == null ? Double.NaN : max);
		}
	}
}
