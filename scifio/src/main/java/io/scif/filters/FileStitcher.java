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

package io.scif.filters;

import io.scif.AxisGuesser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.FilePattern;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.io.Location;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedAxis;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Logic to stitch together files with similar names. Assumes that all files
 * have the same characteristics (e.g., dimensions).
 */
@Plugin(type = Filter.class, priority = FileStitcher.PRIORITY, attrs = {
	@Attr(name = FileStitcher.FILTER_KEY, value = FileStitcher.FILTER_VALUE),
	@Attr(name = FileStitcher.ENABLED_KEY, value = FileStitcher.ENABLED_VAULE) })
public class FileStitcher extends AbstractReaderFilter {

	// -- Constants --

	public static final double PRIORITY = 3.0;
	public static final String FILTER_VALUE = "io.scif.Reader";

	// -- Fields --

	/**
	 * Cached parent plane
	 */
	private Plane parentPlane = null;

	/**
	 * Whether string ids given should be treated as file patterns rather than
	 * single file paths.
	 */
	private boolean patternIds = false;

	private boolean doNotChangePattern = false;

	/** Dimensional axis lengths per file. */
	private int[] sizeZ, sizeC, sizeT;

	/** Component lengths for each axis type. */
	private int[][] lenZ, lenC, lenT;

	private boolean noStitch;
	private final boolean group = true;

	private List<ExternalSeries> externals;

	// -- Constructors --

	/** Constructs a FileStitcher around a new image reader. */
	public FileStitcher() {
		this(false);
	}

	/**
	 * Constructs a FileStitcher with the given reader.
	 * 
	 * @param r The reader to use for reading stitched files.
	 * @param patternIds Whether string ids given should be treated as file
	 *          patterns rather than single file paths.
	 */
	public FileStitcher(final boolean patternIds) {
		super(FileStitcherMetadata.class);
		setUsingPatternIds(patternIds);
	}

	// -- FileStitcher API methods --

	/** Sets whether the reader is using file patterns for IDs. */
	public void setUsingPatternIds(final boolean patternIds) {
		this.patternIds = patternIds;
	}

	/** Gets whether the reader is using file patterns for IDs. */
	public boolean isUsingPatternIds() {
		return patternIds;
	}

	public void setCanChangePattern(final boolean doChange) {
		doNotChangePattern = !doChange;
	}

	public boolean canChangePattern() {
		return !doNotChangePattern;
	}

	/** Gets the reader appropriate for use with the given image plane. */
	public Reader getReader(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		if (noStitch) return getParent();
		final int[] q = computeIndices(imageIndex, planeIndex);
		final int fno = q[0];
		return getReader(imageIndex, fno);
	}

	/**
	 * Gets the reader that should be used with the given series and image plane.
	 */
	public DimensionSwapper getExternalsReader(final int imageIndex,
		final int planeIndex)
	{
		if (noStitch) return (DimensionSwapper) getParent();
		final DimensionSwapper r =
			externals.get(getExternalSeries(imageIndex)).getReaders()[planeIndex];
		initReader(imageIndex, planeIndex);
		return r;
	}

	/**
	 * Gets the Metadata that should be used with the given series and image
	 * plane.
	 */
	public Metadata getExternalsMetadata(final int imageIndex,
		final int planeIndex)
	{
		return getExternalsReader(imageIndex, planeIndex).getMetadata();
	}

	/** Gets the local reader index for use with the given image plane. */
	public int getAdjustedIndex(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		if (noStitch) return planeIndex;
		final int[] q = computeIndices(imageIndex, planeIndex);
		final int ino = q[1];
		return ino;
	}

	/**
	 * Gets the axis type for each dimensional block.
	 * 
	 * @return An array containing values from the enumeration:
	 *         <ul>
	 *         <li>AxisGuesser.Z_AXIS: focal planes</li>
	 *         <li>AxisGuesser.T_AXIS: time points</li>
	 *         <li>AxisGuesser.C_AXIS: channels</li>
	 *         <li>AxisGuesser.S_AXIS: series</li>
	 *         </ul>
	 */
	public int[] getAxisTypes(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return externals.get(getExternalSeries(imageIndex)).getAxisGuesser()
			.getAxisTypes();
	}

	/**
	 * Sets the axis type for each dimensional block.
	 * 
	 * @param axes An array containing values from the enumeration:
	 *          <ul>
	 *          <li>AxisGuesser.Z_AXIS: focal planes</li>
	 *          <li>AxisGuesser.T_AXIS: time points</li>
	 *          <li>AxisGuesser.C_AXIS: channels</li>
	 *          <li>AxisGuesser.S_AXIS: series</li>
	 *          </ul>
	 */
	public void setAxisTypes(final int imageIndex, final int[] axes)
		throws FormatException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		externals.get(getExternalSeries(imageIndex)).getAxisGuesser().setAxisTypes(
			axes);
		computeAxisLengths(imageIndex);
	}

	/** Gets the file pattern object used to build the list of files. */
	public FilePattern getFilePattern(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return noStitch ? findPattern(getCurrentFile()) : externals.get(
			getExternalSeries(imageIndex)).getFilePattern();
	}

	/**
	 * Gets the axis guesser object used to guess which dimensional axes are
	 * which.
	 */
	public AxisGuesser getAxisGuesser(final int imageIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return externals.get(getExternalSeries(imageIndex)).getAxisGuesser();
	}

	/**
	 * Constructs a new FilePattern around the pattern extracted from the given
	 * id.
	 */
	public FilePattern findPattern(final String id) {
		return new FilePattern(getContext(), scifio().filePattern().findPattern(id));
	}

	/**
	 * Finds the file pattern for the given ID, based on the state of the file
	 * stitcher. Takes both ID map entries and the patternIds flag into account.
	 */
	public String[] findPatterns(final String id) {
		if (!patternIds) {
			// find the containing patterns
			final HashMap<String, Object> map = scifio().location().getIdMap();
			if (map.containsKey(id)) {
				// search ID map for pattern, rather than files on disk
				final String[] idList = new String[map.size()];
				map.keySet().toArray(idList);
				return scifio().filePattern().findImagePatterns(id, null, idList);
			}
			// id is an unmapped file path; look to similar files on disk
			return scifio().filePattern().findImagePatterns(id);
		}
		if (doNotChangePattern) {
			return new String[] { id };
		}
		patternIds = false;
		String[] patterns =
			findPatterns(new FilePattern(getContext(), id).getFiles()[0]);
		if (patterns.length == 0) patterns = new String[] { id };
		else {
			final FilePattern test = new FilePattern(getContext(), patterns[0]);
			if (test.getFiles().length == 0) patterns = new String[] { id };
		}
		patternIds = true;
		return patterns;
	}

	// -- AbstractReaderFilter API Methods --

	/* lutLength is 0 until a plane is opened */
	@Override
	protected void setSourceHelper(final String source) {
		cleanUp();
		try {
			initFile(source, 0);
		}
		catch (final FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// -- Filter API Methods --

	/**
	 * FileStitcher is only compatible with ByteArray formats.
	 */
	@Override
	public boolean isCompatible(final Class<?> c) {
		return ByteArrayReader.class.isAssignableFrom(c);
	}

	// -- Reader API methods --

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return openPlaneHelper(imageIndex, planeIndex, getParent().openPlane(
			planeIndex, imageIndex), null);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		if (parentPlane == null) parentPlane =
			getParent().openPlane(imageIndex, planeIndex);
		else getParent().openPlane(imageIndex, planeIndex, parentPlane);
		return openPlaneHelper(imageIndex, planeIndex, parentPlane, plane);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final int x, final int y, final int w, final int h) throws FormatException,
		IOException
	{
		return openPlaneHelper(imageIndex, planeIndex, getParent().openPlane(
			imageIndex, planeIndex, x, y, w, h), null);

	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final int x, final int y, final int w, final int h)
		throws FormatException, IOException
	{
		if (parentPlane == null) parentPlane =
			getParent().openPlane(imageIndex, planeIndex, x, y, w, h);
		else getParent().openPlane(imageIndex, planeIndex, parentPlane, x, y, w, h);
		return openPlaneHelper(imageIndex, planeIndex, parentPlane, plane);
	}

	/* TODO not sure how this logic ties in
	public Object openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
	  throws FormatException, IOException
	{
	  FormatTools.assertId(getCurrentFile(), true, 2);

	  Reader r = getReader(imageIndex, planeIndex);
	  int ino = getAdjustedIndex(imageIndex, planeIndex);
	  if (ino < getPlaneCount(imageIndex, r)) return r.openPlane(imageIndex, ino, x, y, w, h);

	  return null;
	}
	*/

	@Override
	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		final Reader r = getReader(imageIndex, planeIndex);
		final int ino = getAdjustedIndex(imageIndex, planeIndex);
		if (ino < r.getMetadata().getPlaneCount(imageIndex)) return r
			.openThumbPlane(imageIndex, ino);

		// return a blank image to cover for the fact that
		// this file does not contain enough image planes
		return externals.get(getExternalSeries(imageIndex)).getBlankThumbBytes();
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		if (externals != null) {
			for (final ExternalSeries s : externals) {
				if (s != null && s.getReaders() != null) {
					for (final DimensionSwapper r : s.getReaders()) {
						if (r != null) r.close(fileOnly);
					}
				}
			}
		}
		if (!fileOnly) {
			cleanUp();
		}
	}

	@Override
	public Reader[] getUnderlyingReaders() {
		final List<Reader> list = new ArrayList<Reader>();
		for (final ExternalSeries s : externals) {
			for (final DimensionSwapper r : s.getReaders()) {
				list.add(r);
			}
		}
		return list.toArray(new Reader[0]);
	}

	/**
	 * FileStitcher-specific implementation of {@link FormatTools#getZCTCoords}.
	 */
	public int[] getZCTCoords(final int imageIndex, final int planeIndex) {
		FormatTools.assertId(getCurrentFile(), true, 2);
		return noStitch ? FormatTools.getZCTCoords(getParent(), imageIndex,
			planeIndex) : FormatTools.getZCTCoords(FormatTools.findDimensionOrder(
			getMetadata(), imageIndex), getMetadata().getAxisLength(imageIndex,
			Axes.Z), getMetadata().getEffectiveSizeC(imageIndex), getMetadata()
			.getAxisLength(imageIndex, Axes.TIME), getMetadata().getPlaneCount(
			imageIndex), imageIndex, planeIndex);
	}

	// -- Internal FormatReader API methods --

	/** Initializes the given file or file pattern. */
	protected void initFile(final String id, final int imageIndex)
		throws FormatException, IOException
	{
		log().debug("initFile: " + id);

		FilePattern fp = new FilePattern(getContext(), id);
		if (!patternIds) {
			patternIds = fp.isValid() && fp.getFiles().length > 1;
		}
		else {
			patternIds =
				!new Location(getContext(), id).exists() &&
					scifio().location().getMappedId(id).equals(id);
		}

		boolean mustGroup = false;
		if (patternIds) {
			mustGroup =
				fp.isValid() &&
					getParent().fileGroupOption(fp.getFiles()[0]) == FormatTools.MUST_GROUP;
		}
		else {
			mustGroup = getParent().fileGroupOption(id) == FormatTools.MUST_GROUP;
		}

		if (mustGroup || !group) {
			// reader subclass is handling file grouping
			noStitch = true;
			getParent().close();
			getParent().setGroupFiles(group);

			if (patternIds && fp.isValid()) {
				getParent().setSource(fp.getFiles()[0]);
			}
			else getParent().setSource(id);
			return;
		}

		if (fp.isRegex()) {
			setCanChangePattern(false);
		}

		String[] patterns = findPatterns(id);
		if (patterns.length == 0) patterns = new String[] { id };
		externals = new ArrayList<ExternalSeries>();

		for (int i = 0; i < patterns.length; i++) {
			externals.set(i, new ExternalSeries(new FilePattern(getContext(),
				patterns[i])));
		}
		fp = new FilePattern(getContext(), patterns[0]);

		getParent().close();
		getParent().setGroupFiles(group);

		if (!fp.isValid()) {
			throw new FormatException("Invalid file pattern: " + fp.getPattern());
		}
		getParent().setSource(fp.getFiles()[0]);

		final String msg = " Please rename your files or disable file stitching.";
		if (getParent().getImageCount() > 1 && externals.size() > 1) {
			throw new FormatException("Unsupported grouping: File pattern contains " +
				"multiple files and each file contains multiple series." + msg);
		}

		// TODO need a new UsedFiles interface..
		final int nPixelsFiles = 1;
//      getParent().getUsedFiles().length - getParent().getUsedFiles(true).length;
		if (nPixelsFiles > 1 || fp.getFiles().length == 1) {
			noStitch = true;
			return;
		}

		final AxisGuesser guesser =
			new AxisGuesser(fp, FormatTools.findDimensionOrder(getMetadata(),
				imageIndex), getParent().getMetadata()
				.getAxisLength(imageIndex, Axes.Z), getParent().getMetadata()
				.getAxisLength(imageIndex, Axes.TIME), getParent().getMetadata()
				.getEffectiveSizeC(imageIndex), getParent().getMetadata()
				.isOrderCertain(imageIndex));

		// use the dimension order recommended by the axis guesser
		((DimensionSwapper) getParent()).swapDimensions(imageIndex, Arrays
			.asList(FormatTools.findDimensionList(guesser.getAdjustedOrder())));

		// if this is a multi-series dataset, we need some special logic
		int imageCount = externals.size();
		if (externals.size() == 1) {
			imageCount = getParent().getImageCount();
		}

		// verify that file pattern is valid and matches existing files
		if (!fp.isValid()) {
			throw new FormatException("Invalid " +
				(patternIds ? "file pattern" : "filename") + " (" + id + "): " +
				fp.getErrorMessage() + msg);
		}
		final String[] files = fp.getFiles();

		if (files == null) {
			throw new FormatException("No files matching pattern (" +
				fp.getPattern() + "). " + msg);
		}

		for (int i = 0; i < files.length; i++) {
			final String file = files[i];

			// HACK: skip file existence check for fake files
			if (file.toLowerCase().endsWith(".fake")) continue;

			if (!new Location(getContext(), file).exists()) {
				throw new FormatException("File #" + i + " (" + file +
					") does not exist.");
			}
		}

		// determine reader type for these files; assume all are the same type
		// NB: readerClass is not used anywhere.
//    Class<? extends Reader> readerClass =
//      ((DimensionSwapper) getParent()).unwrap(files[0]).getClass();

		lenZ = new int[imageCount][];
		lenC = new int[imageCount][];
		lenT = new int[imageCount][];

		// analyze first file; assume each file has the same parameters

//  TODO seems unnecessary?
//  core = new DefaultMetadata();

		// TODO globalMetadata?

		final List<ImageMetadata> imgMeta = new ArrayList<ImageMetadata>();

		for (int i = 0; i < imageCount; i++) {
			final Reader rr = getReader(i, 0);
			final Metadata rrMeta = rr.getMetadata();
			final DefaultImageMetadata iMeta =
				new DefaultImageMetadata(rrMeta.get(i));
			imgMeta.add(iMeta);

			sizeZ[i] = rrMeta.getAxisLength(i, Axes.Z);
			sizeC[i] = rrMeta.getAxisLength(i, Axes.CHANNEL);
			sizeT[i] = rrMeta.getAxisLength(i, Axes.TIME);
		}

//    TODO seems unnecessary?
//    core = new DefaultMetadata(imgMeta);

		// order may need to be adjusted
		for (int i = 0; i < imageCount; i++) {
			final AxisGuesser ag =
				externals.get(getExternalSeries(i)).getAxisGuesser();
			getMetadata().setAxes(i,
				FormatTools.findDimensionList(ag.getAdjustedOrder()));
			getMetadata().setOrderCertain(i, ag.isCertain());
			computeAxisLengths(i);
		}
	}

	private int getExternalSeries(final int currentSeries) {
		if (getParent().getImageCount() > 1) return 0;
		return currentSeries;
	}

	/** Computes axis length arrays, and total axis lengths. */
	protected void computeAxisLengths(final int imageIndex)
		throws FormatException
	{
		final int sno = imageIndex;
		final ExternalSeries s = externals.get(getExternalSeries(imageIndex));
		final FilePattern p = s.getFilePattern();

		int[] count = p.getCount();

		initReader(sno, 0);

		final AxisGuesser ag = s.getAxisGuesser();
		int[] axes = ag.getAxisTypes();

		final int numZ = ag.getAxisCountZ();
		final int numC = ag.getAxisCountC();
		int numT = ag.getAxisCountT();

		if (axes.length == 0 && s.getFiles().length > 1) {
			axes = new int[] { AxisGuesser.T_AXIS };
			count = new int[] { s.getFiles().length };
			numT++;
		}

		getMetadata().setAxisLength(sno, Axes.Z, sizeZ[sno]);
		getMetadata().setAxisLength(sno, Axes.CHANNEL, sizeC[sno]);
		getMetadata().setAxisLength(sno, Axes.TIME, sizeT[sno]);
		lenZ[sno] = new int[numZ + 1];
		lenC[sno] = new int[numC + 1];
		lenT[sno] = new int[numT + 1];
		lenZ[sno][0] = sizeZ[sno];
		lenC[sno][0] = sizeC[sno];
		lenT[sno][0] = sizeT[sno];

		for (int i = 0, z = 1, c = 1, t = 1; i < count.length; i++) {
			switch (axes[i]) {
				case AxisGuesser.Z_AXIS:
					getMetadata().setAxisLength(sno, Axes.Z,
						getMetadata().getAxisLength(sno, Axes.Z) * count[i]);
					lenZ[sno][z++] = count[i];
					break;
				case AxisGuesser.C_AXIS:
					getMetadata().setAxisLength(sno, Axes.CHANNEL,
						getMetadata().getAxisLength(sno, Axes.CHANNEL) * count[i]);
					lenC[sno][c++] = count[i];
					break;
				case AxisGuesser.T_AXIS:
					getMetadata().setAxisLength(sno, Axes.TIME,
						getMetadata().getAxisLength(sno, Axes.TIME) * count[i]);
					lenT[sno][t++] = count[i];
					break;
				case AxisGuesser.S_AXIS:
					break;
				default:
					throw new FormatException("Unknown axis type for axis #" + i + ": " +
						axes[i]);
			}
		}
	}

	/**
	 * Gets the file index, and image index into that file, corresponding to the
	 * given global image index.
	 * 
	 * @return An array of size 2, dimensioned {file index, image index}.
	 */
	protected int[] computeIndices(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		if (noStitch) return new int[] { 0, planeIndex };
		final int sno = imageIndex;
		final ExternalSeries s = externals.get(getExternalSeries(imageIndex));

		final int[] axes = s.getAxisGuesser().getAxisTypes();
		final int[] count = s.getFilePattern().getCount();

		// get Z, C and T positions
		final int[] zct = getZCTCoords(imageIndex, planeIndex);
		final int[] posZ = FormatTools.rasterToPosition(lenZ[sno], zct[0]);
		final int[] posC = FormatTools.rasterToPosition(lenC[sno], zct[1]);
		final int[] posT = FormatTools.rasterToPosition(lenT[sno], zct[2]);

		final int[] tmpZ = new int[posZ.length];
		System.arraycopy(posZ, 0, tmpZ, 0, tmpZ.length);
		final int[] tmpC = new int[posC.length];
		System.arraycopy(posC, 0, tmpC, 0, tmpC.length);
		final int[] tmpT = new int[posT.length];
		System.arraycopy(posT, 0, tmpT, 0, tmpT.length);

		// convert Z, C and T position lists into file index and image index
		final int[] pos = new int[axes.length];
		int z = 1, c = 1, t = 1;
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] == AxisGuesser.Z_AXIS) pos[i] = posZ[z++];
			else if (axes[i] == AxisGuesser.C_AXIS) pos[i] = posC[c++];
			else if (axes[i] == AxisGuesser.T_AXIS) pos[i] = posT[t++];
			else if (axes[i] == AxisGuesser.S_AXIS) {
				pos[i] = 0;
			}
			else {
				throw new FormatException("Unknown axis type for axis #" + i + ": " +
					axes[i]);
			}
		}

		final int fno = FormatTools.positionToRaster(count, pos);
		final DimensionSwapper r = getExternalsReader(sno, fno);
		final Metadata datasetMeta = r.getMetadata();

		int ino;
		if (posZ[0] < datasetMeta.getAxisLength(sno, Axes.Z) &&
			posC[0] < datasetMeta.getAxisLength(sno, Axes.CHANNEL) &&
			posT[0] < datasetMeta.getAxisLength(sno, Axes.TIME))
		{
			if (datasetMeta.isRGB(sno) &&
				(posC[0] * datasetMeta.getRGBChannelCount(sno) >= lenC[sno][0]))
			{
				posC[0] /= lenC[sno][0];
			}
			ino = FormatTools.getIndex(r, sno, posZ[0], posC[0], posT[0]);
		}
		else ino = Integer.MAX_VALUE; // coordinates out of range

		return new int[] { fno, ino };
	}

	protected void initReader(final int imageIndex, final int fno) {
		final int external = getExternalSeries(imageIndex);
		final DimensionSwapper r = externals.get(external).getReaders()[fno];
		final Metadata c = r.getMetadata();
		try {
			if (r.getCurrentFile() == null) {
				r.setGroupFiles(false);
			}
			r.setSource(externals.get(external).getFiles()[fno]);
			final List<CalibratedAxis> axes =
				((DimensionSwapper) getParent()).getInputOrder(imageIndex);

			final String newOrder =
				FormatTools.findDimensionOrder(axes.toArray(new CalibratedAxis[axes
					.size()]));
			if ((externals.get(external).getFiles().length > 1 || !c
				.isOrderCertain(imageIndex)) &&
				(c.getRGBChannelCount(imageIndex) == 1 || newOrder.indexOf("C") == FormatTools
					.findDimensionOrder(c, imageIndex).indexOf("C")))
			{
				r.swapDimensions(imageIndex, Arrays.asList(FormatTools
					.findDimensionList(newOrder)));
			}
			r.setOutputOrder(imageIndex, Arrays.asList(FormatTools
				.findDimensionList(newOrder)));
		}
		catch (final IOException e) {
			log().debug("", e);
		}
	}

	public Plane openPlaneHelper(final int imageIndex, final int planeIndex,
		final Plane parentPlane, Plane plane) throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		if (plane == null || !isCompatible(plane.getClass())) {
			final ByteArrayPlane bp = new ByteArrayPlane(parentPlane.getContext());
			bp.populate(parentPlane);
			bp.setData(new byte[parentPlane.getBytes().length]);

			plane = bp;
		}

		final int[] pos = computeIndices(imageIndex, planeIndex);
		final Reader r = getReader(imageIndex, pos[0]);
		final int ino = pos[1];

		if (ino < r.getMetadata().getPlaneCount(imageIndex)) return r.openPlane(
			imageIndex, ino, plane, parentPlane.getxOffset(), parentPlane
				.getyOffset(), parentPlane.getxLength(), parentPlane.getyLength());

		// return a blank image to cover for the fact that
		// this file does not contain enough image planes
		Arrays.fill(plane.getBytes(), (byte) 0);
		return plane;
	}

	private void cleanUp() {
		noStitch = false;
		externals = null;
		lenZ = lenC = lenT = null;
		parentPlane = null;
	}

	// -- Helper classes --

	class ExternalSeries {

		private final DimensionSwapper[] readers;
		private final String[] files;
		private final FilePattern pattern;
		private final Plane blankThumbBytes;
		private final String originalOrder;
		private final AxisGuesser ag;
		private final int imagesPerFile;

		public ExternalSeries(final FilePattern pattern) throws FormatException,
			IOException
		{
			this.pattern = pattern;
			files = this.pattern.getFiles();

			readers = new DimensionSwapper[files.length];
			for (int i = 0; i < readers.length; i++) {
				readers[i] = new DimensionSwapper();
				readers[i].setGroupFiles(false);
			}
			readers[0].setSource(files[0]);

			final Metadata c = readers[0].getMetadata();

			ag =
				new AxisGuesser(this.pattern, FormatTools.findDimensionOrder(c, 0), c
					.getAxisLength(0, Axes.Z), c.getAxisLength(0, Axes.TIME), c
					.getAxisLength(0, Axes.CHANNEL), c.isOrderCertain(0));

			blankThumbBytes = createPlane(0, 0, 0, 0);

			originalOrder = FormatTools.findDimensionOrder(c, 0);
			imagesPerFile = readers[0].getImageCount();
		}

		public DimensionSwapper[] getReaders() {
			return readers;
		}

		public FilePattern getFilePattern() {
			return pattern;
		}

		public String getOriginalOrder() {
			return originalOrder;
		}

		public AxisGuesser getAxisGuesser() {
			return ag;
		}

		public Plane getBlankThumbBytes() {
			return blankThumbBytes;
		}

		public String[] getFiles() {
			return files;
		}

		public int getImagesPerFile() {
			return imagesPerFile;
		}
	}
}
