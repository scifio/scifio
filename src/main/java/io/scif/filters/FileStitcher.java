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

package io.scif.filters;

import io.scif.AxisGuesser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FilePattern;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.io.location.TestImgLocation;
import io.scif.services.FilePatternService;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Interval;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BrowsableLocation;
import org.scijava.io.location.DummyLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Logic to stitch together files with similar names. Assumes that all files
 * have the same characteristics (e.g., dimensions).
 */
@Plugin(type = Filter.class)
public class FileStitcher extends AbstractReaderFilter {

	// -- Fields --

	@Parameter
	private InitializeService initializeService;

	@Parameter
	private FilePatternService filePatternService;

	@Parameter
	private DataHandleService dataHandleService;

	/**
	 * Whether string ids given should be treated as file patterns rather than
	 * single file paths.
	 */
	private boolean patternIds = false;

	private boolean doNotChangePattern = false;

	/**
	 * Number of images for each file
	 */
	private long[] planesPerFile;

	private FilePattern pattern;

	private boolean noStitch;

	private long totalPlanes = -1;

	private Location[] localFiles;

	// -- Constructors --

	/** Constructs a FileStitcher around a new image reader. */
	public FileStitcher() {
		this(false);
	}

	/**
	 * Constructs a FileStitcher with the given reader.
	 *
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

	/** Gets the file pattern object used to build the list of files. */
	public FilePattern getFilePattern() {
		return pattern;
	}

	/**
	 * Constructs a new FilePattern around the pattern extracted from the given
	 * id.
	 *
	 * @throws IOException
	 */
	public FilePattern findPattern(final BrowsableLocation id)
		throws IOException
	{
		return new FilePattern(id, filePatternService.findPattern(asBrowsable(id)),
			dataHandleService);
	}

	/**
	 * Finds the file pattern for the given ID, based on the state of the file
	 * stitcher. Takes both ID map entries and the patternIds flag into account.
	 *
	 * @throws IOException
	 */
	public String[] findPatterns(final BrowsableLocation id) throws IOException {
		if (!patternIds) {
			// id is an unmapped file path; look to similar files on disk
			return filePatternService.findImagePatterns(asBrowsable(id));
		}
		if (doNotChangePattern) {
			return new String[] { id.getName() };
		}
		patternIds = false;
		String[] patterns = findPatterns(asBrowsable(new FilePattern(
			filePatternService, id, dataHandleService).getFiles()[0]));
		if (patterns.length == 0) patterns = new String[] { id.getName() };
		else {
			final FilePattern test = new FilePattern(id, patterns[0],
				dataHandleService);
			if (test.getFiles().length == 0) patterns = new String[] { id.getName() };
		}
		patternIds = true;
		return patterns;
	}

	// -- AbstractReaderFilter API Methods --

	@Override
	public void setSource(final Location source, final SCIFIOConfig config)
		throws IOException
	{
		setSourceHelper(source, config);
	}

	@Override
	public void setSource(final DataHandle<Location> source,
		final SCIFIOConfig config) throws IOException
	{
		setSourceHelper(source.get(), config);
	}

	@Override
	protected void setSourceHelper(final Location source,
		final SCIFIOConfig config)
	{
		final BrowsableLocation browsableSource = asBrowsable(source);
		try {
			cleanUp();
			log().debug("initFile: " + browsableSource);

			// Determine if we we have a multi-element file pattern
			FilePattern fp = new FilePattern(filePatternService, browsableSource,
				dataHandleService);
			if (!patternIds) {
				patternIds = fp.isValid() && fp.getFiles().length > 1;
			}
			else {
				patternIds = !dataHandleService.exists(browsableSource);
//						&& locationService.getMappedId(source) .equals(source);
			}

			// Determine if the wrapped reader should handle the stitching
			boolean mustGroup = false;
			if (patternIds) {
				mustGroup = fp.isValid() && getParent().fileGroupOption(fp
					.getFiles()[0]) == FormatTools.MUST_GROUP;
			}
			else {
				mustGroup = getParent().fileGroupOption(
					browsableSource) == FormatTools.MUST_GROUP;
			}

			// If the wrapped reader will handle the stitching, we can set its
			// state and return.
			if (mustGroup) {
				noStitch = true;
				getParent().close();

				if (patternIds && fp.isValid()) {
					getParent().setSource(fp.getFiles()[0], config);
				}
				else getParent().setSource(browsableSource, config);
				return;
			}

			// We will handle the stitching here.
			if (fp.isRegex()) {
				setCanChangePattern(false);
			}

			// Get the individual file ids
			String[] patterns = findPatterns(browsableSource);
			if (patterns.length == 0) patterns = new String[] { browsableSource
				.getName() };

			fp = new FilePattern(browsableSource, patterns[0], dataHandleService);

			if (!fp.isValid()) {
				throw new FormatException("Invalid file pattern: " + fp.getPattern());
			}

			final Reader reader = getParent();
			final ImageMetadata firstImgMeta = reader.getMetadata().get(0);
			final AxisType[] dimOrder = firstImgMeta.getAxes().stream().map(
				CalibratedAxis::type).toArray(AxisType[]::new);
			final long sizeZ = firstImgMeta.getAxisLength(Axes.Z);
			final long sizeT = firstImgMeta.getAxisLength(Axes.TIME);
			final long sizeC = firstImgMeta.getAxisLength(Axes.CHANNEL);
			final boolean certain = firstImgMeta.isOrderCertain();

			final AxisGuesser ag = new AxisGuesser(fp, dimOrder, sizeZ, sizeT, sizeC,
				certain);
			final FileStitcherMetadata meta = (FileStitcherMetadata) getMetadata();
			final ImageMetadata imgMeta = firstImgMeta.copy();

			final AxisType[] types = ag.getAxisTypes();
			final int[] count = fp.getCount();

			// overwrite stitched axis lengths
			for (int i = 0; i < types.length; i++) {
				imgMeta.setAxisLength(types[i], count[i]);
			}

			imgMeta.setName(fp.getPattern());
			meta.setImgMeta(imgMeta);
			meta.setSourceLocation(fp.getFiles()[0]);

//			getParent().setSource(fp.getFiles()[0], config);

			final int nPixelsFiles = 1;
			if (nPixelsFiles > 1 || fp.getFiles().length == 1) {
				noStitch = true;
				return;
				// FIXME set number of planes correctly!
			}

			// verify that file pattern is valid and matches existing files
			final String msg = " Please rename your files or disable file stitching.";
			if (!fp.isValid()) {
				throw new FormatException("Invalid " + (patternIds ? "file pattern"
					: "filename") + " (" + browsableSource.getName() + "): " + fp
						.getErrorMessage() + msg);
			}
			localFiles = fp.getFiles();

			if (localFiles == null) {
				throw new FormatException("No files matching pattern (" + fp
					.getPattern() + "). " + msg);
			}

			planesPerFile = new long[localFiles.length];

			for (int i = 0; i < localFiles.length; i++) {
				final Location file = localFiles[i];

				// HACK: skip file existence check for fake files
				if (file instanceof DummyLocation || file instanceof TestImgLocation)
					continue;

				if (!dataHandleService.exists(file)) {
					throw new FormatException("File #" + i + " (" + file +
						") does not exist.");
				}

				Reader r = getParent();
				r.setSource(localFiles[i], config);

				if (r.getImageCount() != 1) {
					cleanUp();
					throw new FormatException(
						"Only one image per source file is supported! \n But " + file
							.toString() + " contains: " + r.getImageCount());
				}
				planesPerFile[i] = r.getPlaneCount(0);

			}
			totalPlanes = Arrays.stream(planesPerFile).sum();
			pattern = fp;
		}
		catch (IOException | FormatException e) {
			throw new IllegalStateException(e);
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
	public long getPlaneCount(final int imageIndex) {
		return totalPlanes;
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Plane plane = createPlane(getMetadata().get(imageIndex), bounds);
		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		// If no stitching, delegate to parent
		if (noStitch) return getParent().openPlane(imageIndex, planeIndex, plane,
			bounds, new SCIFIOConfig(getContext()).groupableSetGroupFiles(false));

		if (plane == null) {
			throw new IllegalArgumentException("Provided plane was null!");
		}

		if (imageIndex != 0) {
			throw new FormatException("only single image sources are supported!");
		}

		// Check for plane compatibility
		Plane bp;
		if (!isCompatible(plane.getClass())) {
			bp = new ByteArrayPlane();
			bp.populate(plane);
			((ByteArrayPlane) bp).setData(new byte[plane.getBytes().length]);
		}
		else bp = plane;

		// If this is a valid image index, get the appropriate reader and
		// return the corresponding plane
		final int[] adjustedIndex = computeFileIndex(planeIndex);
		if (adjustedIndex[0] < localFiles.length &&
			adjustedIndex[1] < planesPerFile[imageIndex])
		{
			final Reader r = getParent();
			r.setSource(localFiles[adjustedIndex[0]]);
			return r.openPlane(0, adjustedIndex[1], bp, bounds, config);
		}

		// return a blank image to cover for the fact that
		// this file does not contain enough image planes
		Arrays.fill(bp.getBytes(), (byte) 0);
		return bp;
	}

	// -- Prioritized API --

	@Override
	public double getPriority() {
		return 3.0;
	}

	// -- Internal FormatReader API methods --

	/**
	 * Returns an int[] containing: - at index 0, the file index containing the
	 * desired global image index - at index 1, the corresponding local image
	 * index.
	 */
	private int[] computeFileIndex(final long planeIndex) {
		int fileIndex = 0;
		long visitedPlanes = 0;
		long localIndex = planeIndex;
		final int[] outIndex = new int[2];
		while (true) {
			visitedPlanes += planesPerFile[fileIndex];
			if (visitedPlanes - 1 >= planeIndex) { // account for index starting at 0
				outIndex[0] = fileIndex;
				outIndex[1] = (int) localIndex;
				break;
			}
			localIndex -= planesPerFile[fileIndex];
			fileIndex++;
		}
		return outIndex;
	}

	private BrowsableLocation asBrowsable(final Location loc) {
		if (loc instanceof BrowsableLocation) {
			return (BrowsableLocation) loc;
		}
		throw new IllegalArgumentException(
			"The provided location is not browsable!");
	}

	@Override
	protected void cleanUp() throws IOException {
		super.cleanUp();
		patternIds = false;
		doNotChangePattern = false;
		planesPerFile = null;
		pattern = null;
		noStitch = false;
	}

}
