/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2021 SCIFIO developers.
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

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ChannelFiller;
import io.scif.filters.MinMaxFilter;
import io.scif.filters.PlaneSeparator;
import io.scif.filters.ReaderFilter;
import io.scif.img.cell.SCIFIOCellImgFactory;
import io.scif.img.converters.PlaneConverter;
import io.scif.img.converters.PlaneConverterService;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.plugin.Parameter;

/**
 * Reads images from data sources using SCIFIO.
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

	@Parameter
	private InitializeService initializeService;

	@Parameter
	private LocationService locationService;

	// -- Constructors --

	public ImgOpener() {
		super();
	}

	public ImgOpener(final Context ctx) {
		super(ctx);
	}

	// -- ImgOpener methods --

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public List<SCIFIOImgPlus<?>> openImgs(final String source)
		throws ImgIOException
	{
		return openImgs(resolve(source));
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param type The {@link Type} T of the output {@link ImgPlus}es.
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type) throws ImgIOException
	{
		return openImgs(resolve(source), type);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public List<SCIFIOImgPlus<?>> openImgs(final String source,
		final SCIFIOConfig config) throws ImgIOException
	{
		return openImgs(resolve(source), config);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param type The {@link Type} T of the output {@link ImgPlus}es.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final T type, final SCIFIOConfig config)
			throws ImgIOException
	{
		return openImgs(resolve(source), type, config);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the dataset to open
	 * @param imgFactory The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory)
			throws ImgIOException
	{
		return openImgs(resolve(source), imgFactory);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the dataset to open
	 * @param imgFactory The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory,
			final SCIFIOConfig config) throws ImgIOException
	{
		return openImgs(resolve(source), imgFactory, config);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public List<SCIFIOImgPlus<?>> openImgs(final Location source)
		throws ImgIOException
	{
		return openImgs(source, (SCIFIOConfig) null);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param type The {@link Type} T of the output {@link ImgPlus}es.
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(final Location source,
		final T type) throws ImgIOException
	{
		return openImgs(source, type, null);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public List<SCIFIOImgPlus<?>> openImgs(final Location source,
		SCIFIOConfig config) throws ImgIOException
	{
		if (config == null) {
			config = new SCIFIOConfig(getContext());
		}
		final Reader r = createReader(source, config);
		return openImgs(r, config);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the images to open
	 * @param type The {@link Type} T of the output {@link ImgPlus}es.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(final Location source,
		final T type, SCIFIOConfig config) throws ImgIOException
	{
		if (config == null) {
			config = new SCIFIOConfig(getContext()).imgOpenerSetComputeMinMax(true);
		}
		final Reader r = createReader(source, config);

		return openImgs(r, type, config);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the dataset to open
	 * @param imgFactory The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(final Location source,
		final ImgFactory<T> imgFactory) throws ImgIOException
	{
		return openImgs(source, imgFactory, (SCIFIOConfig) null);
	}

	/**
	 * Reads in images from the given source.
	 *
	 * @param source the location of the dataset to open
	 * @param imgFactory The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(final Location source,
		final ImgFactory<T> imgFactory, SCIFIOConfig config) throws ImgIOException
	{
		if (config == null) {
			config = new SCIFIOConfig(getContext()).imgOpenerSetComputeMinMax(true);
		}
		final Reader r = createReader(source, config);
		return openImgs(r, imgFactory, config);
	}

	/**
	 * Reads in images using the given {@link Reader}.
	 *
	 * @param reader An initialized {@link Reader} to use for reading image
	 *          data. Must be wrapped by a {@link PlaneSeparator} filter.
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public List<SCIFIOImgPlus<?>> openImgs(final Reader reader)
		throws ImgIOException
	{
		return openImgs(reader, null);
	}

	/**
	 * Reads in images using the given {@link Reader}.
	 *
	 * @param reader An initialized {@link Reader} to use for reading image
	 *          data. Must be wrapped by a {@link PlaneSeparator} filter.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<SCIFIOImgPlus<?>> openImgs(final Reader reader,
		SCIFIOConfig config) throws ImgIOException
	{
		return (List) openImgs(reader, getType(reader), config);
	}

	/**
	 * Reads in images using the given {@link Reader}.
	 *
	 * @param reader An initialized {@link Reader} to use for reading image data.
	 *          Must be wrapped by a {@link PlaneSeparator} filter.
	 * @param type The {@link Type} T of the output {@link ImgPlus}es.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(final Reader reader, final T type,
		SCIFIOConfig config) throws ImgIOException
	{
		if (config == null) {
			config = new SCIFIOConfig(getContext()).imgOpenerSetComputeMinMax(true);
		}
		final ImgFactoryHeuristic heuristic = getHeuristic(config);
		final ImgFactory<T> imgFactory;
		try {
			imgFactory = heuristic.createFactory(reader.getMetadata(), //
				config.imgOpenerGetImgModes(), type);
		}
		catch (final IncompatibleTypeException e) {
			throw new ImgIOException(e);
		}

		return openImgs(reader, imgFactory, config);
	}

	/**
	 * Reads in images using the given {@link Reader}.
	 *
	 * @param reader An initialized {@link Reader} to use for reading image
	 *          data. Must be wrapped by a {@link PlaneSeparator} filter.
	 * @param imgFactory The {@link ImgFactory} to use for creating the
	 *          resultant {@link ImgPlus}.
	 * @param config {@link SCIFIOConfig} to use when opening the images
	 * @return the images which were read
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T> List<SCIFIOImgPlus<T>> openImgs(Reader reader,
		final ImgFactory<T> imgFactory, SCIFIOConfig config) throws ImgIOException
	{
		if (!ReaderFilter.class.isAssignableFrom(reader.getClass())) {
			reader = new ReaderFilter(reader);
		}

		final List<SCIFIOImgPlus<T>> imgPluses = new ArrayList<>();
		Range imageRange = null;

		if (config == null) {
			config = new SCIFIOConfig(getContext()).imgOpenerSetComputeMinMax(true);
		}

		if (config.imgOpenerIsOpenAllImages()) {
			imageRange = new Range("0-" + (reader.getMetadata().getImageCount() - 1));
		}
		else {
			imageRange = config.imgOpenerGetRange();
		}

		for (final Long imageIndex : imageRange) {

			// create image and read metadata
			final long[] dimLengths = utils().getConstrainedLengths(reader
				.getMetadata(), i(imageIndex), config);
			if (SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
				((SCIFIOCellImgFactory<?>) imgFactory).setReader(reader, i(imageIndex));
				((SCIFIOCellImgFactory<?>) imgFactory).setSubRegion(config
					.imgOpenerGetRegion());
			}
			final Img<T> img = imgFactory.create(dimLengths);
			final SCIFIOImgPlus<T> imgPlus = makeImgPlus(img, reader, i(imageIndex));

			final Location id = reader.getCurrentLocation();
			final URI uri = id.getURI();
			imgPlus.setSource(uri == null ? null : uri.toString());
			imgPlus.initializeColorTables(i(reader.getPlaneCount(i(imageIndex))));

			if (config.imgOpenerIsComputeMinMax()) {
				final long[] defaultMinMax = FormatTools.defaultMinMax(reader
					.getMetadata().get(i(imageIndex)));
				for (int c = 0; c < imgPlus.getCompositeChannelCount(); c++) {
					imgPlus.setChannelMinimum(c, defaultMinMax[0]);
					imgPlus.setChannelMaximum(c, defaultMinMax[1]);
				}
			}

			// Put this image's metadata into the ImgPlus's properties table.
			final Metadata meta = reader.getMetadata();
			imgPlus.setMetadata(meta);
			imgPlus.setImageMetadata(meta.get(i(imageIndex)));
			imgPlus.setROIsAndTablesProperties(meta, i(imageIndex));

			// If we have a planar img, read the planes now. Otherwise they
			// will be read on demand.
			if (!SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
				final float startTime = System.currentTimeMillis();
				final long planeCount = reader.getPlaneCount(i(imageIndex));
				try {
					readPlanes(reader, i(imageIndex), imgPlus, config);
				}
				catch (FormatException | IOException e) {
					throw new ImgIOException(e);
				}
				final long endTime = System.currentTimeMillis();
				final float time = (endTime - startTime) / 1000f;
				statusService.showStatus(id + ": read " +
					planeCount + " planes in " + time + "s");
			}
			imgPluses.add(imgPlus);
		}

		// Close the reader if needed
		if (SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
			statusService.showStatus("Created CellImg for dynamic loading");
		}
		else {
			try {
				reader.close();
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
		}
		return imgPluses;
	}

	// -- Helper methods --

	private Type<?> getType(final Reader r) {
		return utils().makeType(r.getMetadata().get(0).getPixelType());
	}

	private ImgFactoryHeuristic getHeuristic(final SCIFIOConfig imgOptions) {
		ImgFactoryHeuristic heuristic = imgOptions
			.imgOpenerGetImgFactoryHeuristic();

		if (heuristic == null) heuristic = new DefaultImgFactoryHeuristic();

		return heuristic;
	}

	private Reader createReader(final Location source, final SCIFIOConfig config)
		throws ImgIOException
	{

		final boolean computeMinMax = config.imgOpenerIsComputeMinMax();
		statusService.showStatus("Initializing " + source);

		ReaderFilter r = null;
		try {
			if (source instanceof FileLocation && !((FileLocation)source).getFile().exists()) {
				throw new IOException("File does not exist: " + source);
			}
			r = initializeService.initializeReader(source, config);
			r.enable(ChannelFiller.class);
			r.enable(PlaneSeparator.class).separate(axesToSplit(r));
			if (computeMinMax) r.enable(MinMaxFilter.class);
		}
		catch (FormatException | IOException e) {
			throw new ImgIOException(e);
		}
		return r;
	}

	/**
	 * Returns a list of all AxisTypes that should be split out. This is a list of
	 * all non-X,Y planar axes. Always tries to split {@link Axes#CHANNEL}.
	 */
	private AxisType[] axesToSplit(final ReaderFilter r) {
		final Set<AxisType> axes = new HashSet<>();
		final Metadata meta = r.getTail().getMetadata();
		// Split any non-X,Y axis
		for (final CalibratedAxis t : meta.get(0).getAxesPlanar()) {
			final AxisType type = t.type();
			if (!(type == Axes.X || type == Axes.Y)) {
				axes.add(type);
			}
		}
		// Ensure channel is attempted to be split
		axes.add(Axes.CHANNEL);
		return axes.toArray(new AxisType[axes.size()]);
	}

	private AxisType[] getAxisTypes(final int imageIndex, final Metadata m) {
		final AxisType[] types = new AxisType[m.get(imageIndex).getAxes().size()];
		for (int i = 0; i < types.length; i++) {
			types[i] = m.get(imageIndex).getAxis(i).type();
		}
		return types;
	}

	/** Compiles an N-dimensional list of calibration values. */
	private double[] getCalibration(final int imageIndex, final Metadata m) {

		final double[] calibration = new double[m.get(imageIndex).getAxes().size()];
		for (int i = 0; i < calibration.length; i++) {
			calibration[i] = FormatTools.getScale(m, imageIndex, m.get(imageIndex)
				.getAxis(i).type());
		}

		return calibration;
	}

	/**
	 * Wraps the given {@link Img} in an {@link ImgPlus} with metadata
	 * corresponding to the specified initialized {@link Reader}.
	 */
	private <T> SCIFIOImgPlus<T> makeImgPlus(final Img<T> img,
		final Reader r, final int imageIndex)
	{
		final Location id = r.getCurrentLocation();
		String name = null;

		if (id != null) {
			name = id.getName();
		}

		if (name == null || name.equals("")) name = "Image: " + r.getFormatName();

		final double[] cal = getCalibration(imageIndex, r.getMetadata());
		final AxisType[] dimTypes = getAxisTypes(imageIndex, r.getMetadata());

		final Reader base;
		base = unwrap(r);

		final Metadata meta = r.getMetadata();
		final int rgbChannelCount = base.getMetadata().get(0).isMultichannel()
			? (int) base.getMetadata().get(0).getAxisLength(Axes.CHANNEL) : 1;
		final int validBits = meta.get(0).getBitsPerPixel();

		final SCIFIOImgPlus<T> imgPlus = new SCIFIOImgPlus<>(img, name, dimTypes,
			cal);
		final String metaName = meta.get(imageIndex).getName();
		if (metaName != null) imgPlus.setName(metaName);
		imgPlus.setValidBits(validBits);

		int compositeChannelCount = rgbChannelCount;
		if (rgbChannelCount == 1) {
			// HACK: Support ImageJ color mode embedded in TIFF files.
			final String colorMode = (String) meta.getTable().get("Color mode");
			if ("composite".equals(colorMode)) {
				compositeChannelCount = (int) meta.get(0).getAxisLength(Axes.CHANNEL);
			}
		}
		imgPlus.setCompositeChannelCount(compositeChannelCount);

		setCalibrationUnits(imgPlus, meta, imageIndex);

		return imgPlus;
	}

	/**
	 * Populates the calibration units of the given {@link SCIFIOImgPlus}, using
	 * the provided {@link Metadata}.
	 */
	private <T> void setCalibrationUnits(
		final SCIFIOImgPlus<T> imgPlus, final Metadata m, final int imageIndex)
	{
		for (final CalibratedAxis axis : m.get(imageIndex).getAxes()) {
			final int index = imgPlus.dimensionIndex(axis.type());
			if (index >= 0) {
				imgPlus.axis(index).setUnit(axis.unit());
			}
		}
	}

	/**
	 * Finds the lowest level wrapped reader, preferably a {@link ChannelFiller} ,
	 * but otherwise the base reader. This is useful for determining whether the
	 * input data is intended to be viewed with multiple channels composited
	 * together.
	 */
	private Reader unwrap(final Reader r) {
		if (!(r instanceof ReaderFilter)) return r;
		final ReaderFilter rf = (ReaderFilter) r;
		return rf.getTail();
	}

	/**
	 * Reads planes from the given initialized {@link Reader} into the specified
	 * {@link Img}.
	 */
	private <T> void readPlanes(final Reader r,
		final int imageIndex, final ImgPlus<T> imgPlus, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		// TODO - create better container types; either:
		// 1) an array container type using one byte array per plane
		// 2) as #1, but with an Reader reference reading planes on demand
		// 3) as PlanarRandomAccess, but with an Reader reference
		// reading planes on demand

		// PlanarRandomAccess is useful for efficient access to pixels in ImageJ
		// (e.g., getPixels)
		// #1 is useful for efficient SCIFIO import, and useful for tools
		// needing byte arrays (e.g., BufferedImage Java3D texturing by
		// reference)
		// #2 is useful for efficient memory use for tools wanting matching
		// primitive arrays (e.g., virtual stacks in ImageJ)
		// #3 is useful for efficient memory use

		// get container
		final PlanarAccess<?> planarAccess = utils().getPlanarAccess(imgPlus);
		final Type<?> inputType = //
			utils().makeType(r.getMetadata().get(0).getPixelType());
		final T outputType = imgPlus.firstElement();
		final boolean compatibleTypes = outputType.getClass().isAssignableFrom(
			inputType.getClass());

		// populate planes
		final boolean isPlanar = planarAccess != null && compatibleTypes;
		final boolean isArray = utils().getArrayAccess(imgPlus) != null &&
			compatibleTypes;

		final ImageRegion region = config.imgOpenerGetRegion();

		final Metadata m = r.getMetadata();

		final List<CalibratedAxis> planarAxes = m.get(imageIndex).getAxesPlanar();
		final int planarAxisCount = planarAxes.size();

		// [min, max] of the planar dimensions
		final long[] planarMin = new long[planarAxisCount];
		final long[] planarMax = new long[planarAxisCount];
		// Non-planar indices to open
		final Range[] npRanges = new Range[m.get(imageIndex).getAxesNonPlanar()
			.size()];
		final long[] npIndices = new long[npRanges.length];

		// populate plane dimensions
		int index = 0;
		for (final CalibratedAxis planarAxis : planarAxes) {
			if (region != null && region.hasRange(planarAxis.type())) {
				planarMin[index] = region.getRange(planarAxis.type()).head();
				planarMax[index] = region.getRange(planarAxis.type()).tail();
			}
			else {
				planarMin[index] = 0;
				planarMax[index] = m.get(imageIndex).getAxisLength(planarAxis) - 1;
			}
			index++;
		}
		final Interval bounds = new FinalInterval(planarMin, planarMax);

		// determine non-planar indices to open
		index = 0;
		for (final CalibratedAxis npAxis : m.get(imageIndex).getAxesNonPlanar()) {
			if (region != null && region.hasRange(npAxis.type())) {
				npRanges[index++] = region.getRange(npAxis.type());
			}
			else {
				npRanges[index++] = new Range(0l, m.get(imageIndex).getAxisLength(npAxis
					.type()) - 1);
			}
		}

		PlaneConverter converter = config.imgOpenerGetPlaneConverter();

		if (converter == null) {
			// if we have a PlanarAccess we can use a PlanarAccess converter,
			// otherwise we can use a more general RandomAccess approach
			if (isArray) {
				converter = pcService.getArrayConverter();
			}
			else if (isPlanar) {
				converter = pcService.getPlanarConverter();
			}
			else converter = pcService.getDefaultConverter();
		}

		read(imageIndex, imgPlus, r, config, converter, bounds, npRanges,
			npIndices);

		if (config.imgOpenerIsComputeMinMax()) populateMinMax(r, imgPlus,
			imageIndex);
	}

	@SuppressWarnings("rawtypes")
	private void read(final int imageIndex, final ImgPlus imgPlus, final Reader r,
		final SCIFIOConfig config, final PlaneConverter converter,
		final Interval bounds, final Range[] npRanges, final long[] npIndices)
		throws FormatException, IOException
	{
		read(imageIndex, imgPlus, r, config, converter, null, bounds, npRanges,
			npIndices, 0, new int[] { 0 });
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Plane read(final int imageIndex, final ImgPlus imgPlus,
		final Reader r, final SCIFIOConfig config, final PlaneConverter converter,
		Plane tmpPlane, final Interval bounds, final Range[] npRanges,
		final long[] npIndices, final int depth, final int[] planeCount)
		throws FormatException, IOException
	{
		if (depth < npRanges.length) {
			// We need to invert the depth index to get the current non-planar
			// axis index, to ensure axes are iteratead in fastest to slowest
			// order
			final int npPosition = npRanges.length - 1 - depth;
			// Recursive step. Sets the non-planar indices
			for (int i = 0; i < npRanges[npPosition].size(); i++) {
				npIndices[npPosition] = npRanges[npPosition].get(i);
				tmpPlane = read(imageIndex, imgPlus, r, config, converter, tmpPlane,
					bounds, npRanges, npIndices, depth + 1, planeCount);
			}
		}
		else {
			// Terminal step. Reads the plane at the rasterized index, given the
			// non-planar indices
			final int planeIndex = (int) FormatTools.positionToRaster(0, r,
				npIndices);

			if (config.imgOpenerIsComputeMinMax()) {
				populateMinMax(r, imgPlus, imageIndex);
			}
			// FIXME: what if tmpPlane length does not match bounds size?
			// Invent a utility method for checking tmpPlane vs. bounds.
			if (tmpPlane == null) {
				tmpPlane = r.openPlane(imageIndex, planeIndex, bounds);
			}
			else {
				tmpPlane = r.openPlane(imageIndex, planeIndex, tmpPlane, bounds,
					config);
			}

			// copy the data to the ImgPlus
			converter.populatePlane(r, imageIndex, planeCount[0], tmpPlane.getBytes(),
				imgPlus, config);

			// store color table
			imgPlus.setColorTable(tmpPlane.getColorTable(), planeCount[0]);

			// Update plane count
			planeCount[0]++;
		}

		return tmpPlane;
	}

	private void populateMinMax(final Reader r, final ImgPlus<?> imgPlus,
		final int imageIndex)
	{
		final int sizeC = (int) r.getMetadata().get(imageIndex).getAxisLength(
			Axes.CHANNEL);
		final ReaderFilter rf = (ReaderFilter) r;
		final MinMaxFilter minMax = rf.enable(MinMaxFilter.class);
		for (int c = 0; c < sizeC; c++) {
			final Double min = minMax.getAxisKnownMinimum(imageIndex, Axes.CHANNEL,
				c);
			final Double max = minMax.getAxisKnownMinimum(imageIndex, Axes.CHANNEL,
				c);
			imgPlus.setChannelMinimum(c, min == null ? Double.NaN : min);
			imgPlus.setChannelMaximum(c, max == null ? Double.NaN : max);
		}
	}

	/**
	 * Safely downcasts a {@code long} value to an {@code int}.
	 *
	 * @throws IllegalArgumentException if the {@code long} value is not within
	 *           the range of valid {@code int} values.
	 */
	private int i(final long l) {
		// TODO: Move this and similar methods to org.scijava.util.NumberUtils.
		if (l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Value too large: " + l);
		}
		if (l < Integer.MIN_VALUE) {
			throw new IllegalArgumentException("Value too small: " + l);
		}
		return (int) l;
	}

	private Location resolve(final String source) {
		try {
			return locationService.resolve(source);
		}
		catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link #openImgs(String, ImgFactory)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> List<SCIFIOImgPlus<T>>
		openImgs(final String source, final ImgFactory<T> imgFactory,
			@SuppressWarnings("unused") T type) throws ImgIOException
	{
		return openImgs(source, imgFactory);
	}

	/** @deprecated Use {@link #openImgs(Reader, ImgFactory, SCIFIOConfig)}. */
	@Deprecated
	public <T extends RealType<T>> List<SCIFIOImgPlus<T>> openImgs(
		final Reader reader, @SuppressWarnings("unused") final T type,
		final ImgFactory<T> imgFactory, final SCIFIOConfig config)
		throws ImgIOException
	{
		return openImgs(reader, imgFactory, config);
	}

	/** @deprecated Use {@link #openImgs(String)}. */
	@Deprecated
	public SCIFIOImgPlus<?> openImg(final String source) throws ImgIOException {
		return first(openImgs(source));
	}

	/** @deprecated Use {@link #openImgs(String, RealType)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final String source, final T type) throws ImgIOException
	{
		return first(openImgs(source, type));
	}

	/** @deprecated Use {@link #openImgs(String, SCIFIOConfig)}. */
	@Deprecated
	public SCIFIOImgPlus<?> openImg(final String source,
		final SCIFIOConfig config) throws ImgIOException
	{
		return first(openImgs(source, config));
	}

	/** @deprecated Use {@link #openImgs(String, RealType, SCIFIOConfig)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final String source, final T type, final SCIFIOConfig config)
		throws ImgIOException
	{
		return first(openImgs(source, type, config));
	}

	/** @deprecated Use {@link #openImgs(String, ImgFactory)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final String source, final ImgFactory<T> imgFactory) throws ImgIOException
	{
		return first(openImgs(source, imgFactory));
	}

	/** @deprecated Use {@link #openImgs(String, ImgFactory, SCIFIOConfig)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final String source, final ImgFactory<T> imgFactory,
		final SCIFIOConfig config) throws ImgIOException
	{
		return first(openImgs(source, imgFactory, config));
	}

	/**
	 * @deprecated Use {@link #openImgs(String, ImgFactory)} or
	 *             {@link #openImgs(String, RealType)}.
	 */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final String source, final ImgFactory<T> imgFactory, final T type)
		throws ImgIOException
	{
		return first(openImgs(source, imgFactory, type));
	}

	/** @deprecated Use {@link #openImgs(Reader, SCIFIOConfig)}. */
	@Deprecated
	public SCIFIOImgPlus<?> openImg(final Reader reader,
		final SCIFIOConfig config) throws ImgIOException
	{
		return first(openImgs(reader, config));
	}

	/** @deprecated Use {@link #openImgs(Reader, Object, SCIFIOConfig)}. */
	@Deprecated
	public <T extends RealType<T> & NativeType<T>> SCIFIOImgPlus<T> openImg(
		final Reader reader, final T type, final SCIFIOConfig config)
		throws ImgIOException
	{
		return first(openImgs(reader, type, config));
	}

	/**
	 * @deprecated Use {@link #openImgs(Reader, Object, SCIFIOConfig)} or
	 *             {@link #openImgs(Reader, ImgFactory, SCIFIOConfig)}.
	 */
	@Deprecated
	public <T extends RealType<T>> SCIFIOImgPlus<T> openImg(final Reader reader,
		final T type, ImgFactory<T> imgFactory, final SCIFIOConfig config)
		throws ImgIOException
	{
		return first(openImgs(reader, type, imgFactory, config));
	}
}
