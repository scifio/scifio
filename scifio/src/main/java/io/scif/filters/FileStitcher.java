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

import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FilePattern;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.io.Location;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import net.imglib2.meta.Axes;

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
	 * Whether string ids given should be treated as file patterns rather than
	 * single file paths.
	 */
	private boolean patternIds = false;

	private boolean doNotChangePattern = false;

	/**
	 * Number of images for each file
	 */
	private int[] imagesPerFile = null;

	private Reader[] readers = null;
	private String[] files = null;
	private FilePattern pattern;

	private boolean noStitch;
	private final boolean group = true;

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

	/**
	 * Gets the reader appropriate for use with the given image.
	 */
	public Reader getReader(final int imageIndex) throws FormatException,
		IOException
	{
		if (noStitch) return getParent();
		final int[] fileIndex = computeFileIndex(imageIndex);
		Reader r = readers[fileIndex[0]];
		if (r == null) {
			r = scifio().initializer().initializeReader(files[imageIndex]);
			readers[fileIndex[0]] = r;
		}
		return r;
	}

	/**
	 * Gets the metadata for the given image index.
	 * @throws IOException 
	 * @throws FormatException 
	 */
	public Metadata getMetadata(final int imageIndex) throws FormatException, IOException {
		if (noStitch) return getParent().getMetadata();
		return getReader(imageIndex).getMetadata();
	}

	/** Gets the file pattern object used to build the list of files. */
	public FilePattern getFilePattern() {
		return pattern;
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

	@Override
	protected void setSourceHelper(final String source) {
		try {
			cleanUp();
			log().debug("initFile: " + source);

			// Determine if we we have a multi-element file pattern
			FilePattern fp = new FilePattern(getContext(), source);
			if (!patternIds) {
				patternIds = fp.isValid() && fp.getFiles().length > 1;
			}
			else {
				patternIds =
					!new Location(getContext(), source).exists() &&
						scifio().location().getMappedId(source).equals(source);
			}

			// Determine if the wrapped reader should handle the stitching
			boolean mustGroup = false;
			if (patternIds) {
				mustGroup =
					fp.isValid() &&
						getParent().fileGroupOption(fp.getFiles()[0]) == FormatTools.MUST_GROUP;
			}
			else {
				mustGroup =
					getParent().fileGroupOption(source) == FormatTools.MUST_GROUP;
			}

			// If the wrapped reader will handle the stitching, we can set its
			// state and return.
			if (mustGroup || !group) {
				noStitch = true;
				getParent().close();
				getParent().setGroupFiles(group);

				if (patternIds && fp.isValid()) {
					getParent().setSource(fp.getFiles()[0]);
				}
				else getParent().setSource(source);
				return;
			}

			// We will handle the stitching here.
			if (fp.isRegex()) {
				setCanChangePattern(false);
			}


			// Get the individual file ids
			String[] patterns = findPatterns(source);
			if (patterns.length == 0) patterns = new String[] { source };
			readers = new Reader[patterns.length];

			fp = new FilePattern(getContext(), patterns[0]);

			getParent().close();
			getParent().setGroupFiles(group);

			if (!fp.isValid()) {
				throw new FormatException("Invalid file pattern: " + fp.getPattern());
			}
			getParent().setSource(fp.getFiles()[0]);

			final String msg = " Please rename your files or disable file stitching.";

			// TODO need a new UsedFiles interface..
			final int nPixelsFiles = 1;
//	      getParent().getUsedFiles().length - getParent().getUsedFiles(true).length;
			if (nPixelsFiles > 1 || fp.getFiles().length == 1) {
				noStitch = true;
				return;
			}

			// verify that file pattern is valid and matches existing files
			if (!fp.isValid()) {
				throw new FormatException("Invalid " +
					(patternIds ? "file pattern" : "filename") + " (" + source + "): " +
					fp.getErrorMessage() + msg);
			}
			final String[] files = fp.getFiles();

			if (files == null) {
				throw new FormatException("No files matching pattern (" +
					fp.getPattern() + "). " + msg);
			}

			for (int i = 0; i < files.length; i++) {
				final String file = files[i];

				//TODO remove this when virtual handle is in use
				// HACK: skip file existence check for fake files
				if (file.toLowerCase().endsWith(".fake")) continue;

				if (!new Location(getContext(), file).exists()) {
					throw new FormatException("File #" + i + " (" + file +
						") does not exist.");
				}
			}
			
			this.files = files;
			pattern = fp;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (FormatException e) {
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
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final long[] offsets, final long[] lengths)
		throws FormatException, IOException
	{
		// If no stitching, delegate to parent
		if (noStitch) return getParent().openPlane(imageIndex, planeIndex, plane,
			offsets, lengths);

		// Check for plane compatibility
		Plane bp;
		if (plane == null || !isCompatible(plane.getClass())) {
			bp = new ByteArrayPlane(getContext());
			bp.populate(plane);
			((ByteArrayPlane) bp).setData(new byte[plane.getBytes().length]);
		}
		else bp = plane;

		// If this is a valid image index, get the appropriate reader and 
		// return the corresponding plane
		int[] adjustedIndex = computeFileIndex(imageIndex);
		if (adjustedIndex[0] < readers.length &&
			adjustedIndex[1] < readers[adjustedIndex[0]].getImageCount())
		{
			final Reader r = readers[adjustedIndex[0]];
			return r.openPlane(adjustedIndex[1], planeIndex, bp, offsets, lengths);
		}

		// return a blank image to cover for the fact that
		// this file does not contain enough image planes
		Arrays.fill(bp.getBytes(), (byte) 0);
		return bp;
	}

	@Override
	public Plane openThumbPlane(final int imageIndex, final long planeIndex)
		throws FormatException, IOException
	{
		// If no stitching, delegate to parent
		if (noStitch) return getParent().openThumbPlane(imageIndex, planeIndex);

		// If this is a valid image index, get the appropriate reader and 
		// return the corresponding plane
		int[] adjustedIndex = computeFileIndex(imageIndex);
		if (adjustedIndex[0] < readers.length &&
			adjustedIndex[1] < readers[adjustedIndex[0]].getImageCount())
		{
			final Reader r = readers[adjustedIndex[0]];
			return r.openThumbPlane(adjustedIndex[1], planeIndex);
		}

		// return a blank image to cover for the fact that
		// this file does not contain enough image planes
		long[] thumbPlaneOffsets =
			new long[getParentMeta().getAxesPlanar(0).size()];
		long[] thumbPlaneBounds = getParentMeta().getAxesLengths(0);
		thumbPlaneBounds[getParentMeta().getAxisIndex(0, Axes.X)] =
			getParentMeta().getThumbSizeX(0);
		thumbPlaneBounds[getParentMeta().getAxisIndex(0, Axes.Y)] =
			getParentMeta().getThumbSizeY(0);
		return getParent().createPlane(thumbPlaneOffsets, thumbPlaneBounds);
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		if (!fileOnly) {
			cleanUp();
		}
	}

	@Override
	public Reader[] getUnderlyingReaders() {
		return readers;
	}

	// -- Internal FormatReader API methods --

	/**
	 * Returns an int[] containing: - at index 0, the file index containing the
	 * desired global image index - at index 0, the corresponding local image
	 * index
	 */
	private int[] computeFileIndex(int imageIndex) {
		if (noStitch) return new int[] { imageIndex, 0 };
		int fileIndex = 0;
		while (imageIndex >= 0 + imagesPerFile[fileIndex]) {
			imageIndex -= imagesPerFile[fileIndex++];
		}

		return new int[] { fileIndex, imageIndex };
	}

	private void cleanUp() throws IOException {
		patternIds = false;

		doNotChangePattern = false;

		imagesPerFile = null;

		for (Reader r : readers) {
			if (r != null) r.close();
		}

		readers = null;
		files = null;
		pattern = null;

		noStitch = false;
	}
}
