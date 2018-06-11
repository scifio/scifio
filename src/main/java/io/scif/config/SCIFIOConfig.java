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

package io.scif.config;

import io.scif.Checker;
import io.scif.Groupable;
import io.scif.MetadataLevel;
import io.scif.Parser;
import io.scif.Writer;
import io.scif.codec.CodecOptions;
import io.scif.img.ImageRegion;
import io.scif.img.ImgFactoryHeuristic;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.Range;
import io.scif.img.converters.PlaneConverter;

import java.awt.image.ColorModel;
import java.util.HashMap;

import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;

import org.scijava.Context;

/**
 * Configuration class for all SCIFIO components. Similar to a {@link Context},
 * this class is effectively a container for state. However, its intended scope
 * is per method call stack, and not through a complete application. If any
 * object in a call stack has behavior that can be modified through this class,
 * a complete method chain accepting {@code SCIFIOConfig} instances should be
 * available - even if the intermediate classes do not require configuration
 * (the need for configuration is, effectively contagious).
 * <p>
 * Note that each getter and setter method signature in this class is prefixed
 * by the component it affects.
 * </p>
 *
 * @author Mark Hiner
 * @see Checker
 * @see Parser
 * @see Writer
 * @see Groupable
 * @see ImgOpener
 * @see ImgSaver
 */
public class SCIFIOConfig extends HashMap<String, Object> {

	// -- Fields --

	// Checker
	private boolean openDataset = true;

	// Parser
	private MetadataLevel level;

	private boolean filterMetadata;

	private boolean saveOriginalMetadata;

	// Writer
	private boolean writeSequential = false;

	private ColorModel model = null;

	private int fps = 10;

	private String compression = null;

	private CodecOptions options = null;

	// Groupable
	/** Whether or not to group multi-file formats. */
	private boolean group = false;

	// ImgOpener

	/**
	 * Access type options for opening datasets.
	 * <ul>
	 * <li>{@link ImgMode#ARRAY} will attempt to use {@link ArrayImgFactory}</li>
	 * <li>{@link ImgMode#AUTO} allows the program to decide, e.g. based on
	 * available memory.</li>
	 * <li>{@link ImgMode#CELL} will attempt to use {@link CellImgFactory}</li>
	 * <li>{@link ImgMode#PLANAR} will attempt to use
	 * {@link PlanarImgFactory}</li>
	 * </ul>
	 *
	 * @author Mark Hiner
	 */
	public static enum ImgMode {
			ARRAY, AUTO, CELL, PLANAR;
	}

	// If true, planarEnabled returns true. If false, cellEnabled returns true.
	// If null, both planar/cell enabled will return false.
	private ImgMode[] imgModes = new ImgMode[] { ImgMode.AUTO };

	// Whether ImgOpeners should open all images
	private boolean openAll = false;

	// Image indices
	private Range range = new Range("0");

	// sub-region specification for opening portions of an image
	private ImageRegion region = null;

	// Whether or not to use a MinMaxFilter
	private boolean computeMinMax = false;

	// Custom plane converter
	private PlaneConverter planeConverter = null;

	// Custom heuristic for choosing an ImgFactory
	private ImgFactoryHeuristic imgFactoryHeuristic = null;

	// ImgSaver
	private boolean writeRGB = true;

	// -- Constructors --

	/**
	 * Zero-param constructor. Creates an empty configuration.
	 */
	public SCIFIOConfig() { /* no-op, empty configuration */}

	/**
	 * Copying constructor. Returns a copy of the given SCIFIOConfig.
	 *
	 * @param config Configuration to copy.
	 */
	public SCIFIOConfig(final SCIFIOConfig config) {
		super(config);
		openDataset = config.openDataset;
		level = config.level;
		filterMetadata = config.filterMetadata;
		saveOriginalMetadata = config.saveOriginalMetadata;
		writeSequential = config.writeSequential;
		model = config.model;
		fps = config.fps;
		compression = config.compression;
		options = config.options;
		group = config.group;
		imgModes = config.imgModes;
		range = config.range;
		region = config.region;
		computeMinMax = config.computeMinMax;
		planeConverter = config.planeConverter;
		imgFactoryHeuristic = config.imgFactoryHeuristic;
		writeRGB = config.writeRGB;
	}

	// -- Checker Methods --

	public SCIFIOConfig checkerSetOpen(final boolean open) {
		openDataset = open;
		return this;
	}

	public boolean checkerIsOpen() {
		return openDataset;
	}

	// -- Parser methods --

	/**
	 * @return {@link MetadataLevel} desired for parsing.
	 */
	public MetadataLevel parserGetLevel() {
		return level;
	}

	/**
	 * @param level Desired metadata level for parsing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig parserSetLevel(final MetadataLevel level) {
		this.level = level;
		return this;
	}

	/**
	 * @return True if parsers should filter parsed metadata.
	 */
	public boolean parserIsFiltered() {
		return filterMetadata;
	}

	/**
	 * @param filterMetadata Desired filtering behavior for parsing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig parserSetFiltered(final boolean filterMetadata) {
		this.filterMetadata = filterMetadata;
		return this;
	}

	/**
	 * @return True if parsers should save original metadata.
	 */
	public boolean parserIsSaveOriginalMetadata() {
		return saveOriginalMetadata;
	}

	/**
	 * @param saveOriginalMetadata Desired metadata saving behavior for parsing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig parserSetSaveOriginalMetadata(
		final boolean saveOriginalMetadata)
	{
		this.saveOriginalMetadata = saveOriginalMetadata;
		return this;
	}

	// -- Writer methods --

	/**
	 * Sets whether or not we know that planes will be written sequentially. If
	 * planes are written sequentially and this flag is set, then performance will
	 * be slightly improved.
	 *
	 * @param sequential Flag for writing sequential planes.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig writerSetSequential(final boolean sequential) {
		writeSequential = sequential;
		return this;
	}

	/**
	 * @return True if writers should write image planes sequentially.
	 */
	public boolean writerIsSequential() {
		return writeSequential;
	}

	/**
	 * @param cm ColorModel to use for writing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig writerSetColorModel(final ColorModel cm) {
		model = cm;
		return this;
	}

	/**
	 * @return The ColorModel to use when writing.
	 */
	public ColorModel writerGetColorModel() {
		return model;
	}

	/**
	 * @param rate Desired frames per second to use when writing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig writerSetFramesPerSecond(final int rate) {
		fps = rate;
		return this;
	}

	/**
	 * @return The number of frames per second to use when writing.
	 */
	public int writerGetFramesPerSecond() {
		return fps;
	}

	/**
	 * @param compress Desired compression type to use when writing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig writerSetCompression(final String compress) {
		compression = compress;
		return this;
	}

	/**
	 * @return The compression type writers will use when writing.
	 */
	public String writerGetCompression() {
		return compression;
	}

	/**
	 * @param options Desired CodecOptions to use for writing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig writerSetCodecOptions(final CodecOptions options) {
		this.options = options;
		return this;
	}

	/**
	 * @return The CodecOptions that writers will use when writing.
	 */
	public CodecOptions writerGetCodecOptions() {
		return options;
	}

	// -- Groupable methods --

	/**
	 * @param groupFiles Desired behavior for grouping potential multi-file
	 *          datasets. If true, these will be grouped into one single dataset.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig groupableSetGroupFiles(final boolean groupFiles) {
		group = groupFiles;
		return this;
	}

	/**
	 * @return Whether or not Groupable classes should group similar files when
	 *         operating on them.
	 */
	public boolean groupableIsGroupFiles() {
		return group;
	}

	// -- ImgOpener methods --

	/**
	 * @return The access type to attempt to open the dataset with. Default:
	 *         imgMode.AUTO, which allows the calling program to decide.
	 */
	public ImgMode[] imgOpenerGetImgModes() {
		return imgModes;
	}

	/**
	 * @param imgModes A list of ImgMode access types. How these are interpreted
	 *          is up to the ImgFactoryHeuristic, but it is reasonable to expect
	 *          modes listed earlier to be preferred.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetImgModes(final ImgMode... imgModes) {
		this.imgModes = imgModes;
		return this;
	}

	/**
	 * @return True if the image should be scaled to its min and max intensities.
	 *         Default: false
	 */
	public boolean imgOpenerIsComputeMinMax() {
		return computeMinMax;
	}

	/**
	 * @param computeMinMax Whether or not images should be scaled to min/max
	 *          intensities.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetComputeMinMax(final boolean computeMinMax) {
		this.computeMinMax = computeMinMax;
		return this;
	}

	/**
	 * Returns a {@link ImageRegion} specifying dimension constraints. This may be
	 * of a different dimensionality than the underlying image, in which case the
	 * lengths are assume to be in the natural ordering of the image.
	 *
	 * @return A Subregion specifying dimension offsets and lengths. Default: null
	 */
	public ImageRegion imgOpenerGetRegion() {
		return region;
	}

	/**
	 * @param region Region constraints for any image to open
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetRegion(final ImageRegion region) {
		this.region = region;
		return this;
	}

	/**
	 * @return A custom plane converter. Default: {@code null}
	 */
	public PlaneConverter imgOpenerGetPlaneConverter() {
		return planeConverter;
	}

	/**
	 * @param planeConverter Sets a PlaneConverter to use when opening datasets.
	 *          This is useful when using a custom Img type.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetPlaneConverter(
		final PlaneConverter planeConverter)
	{
		this.planeConverter = planeConverter;
		return this;
	}

	/**
	 * @return The ImgFactoryHeuristic to use when selecting an ImgFactory.
	 *         Default: {@code null}
	 */
	public ImgFactoryHeuristic imgOpenerGetImgFactoryHeuristic() {
		return imgFactoryHeuristic;
	}

	/**
	 * @param imgFactoryHeuristic Heuristic to use when selecting an ImgFactory.
	 *          Will not be used if an ImgFactory is provided to the ImgOpener.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetImgFactoryHeuristic(
		final ImgFactoryHeuristic imgFactoryHeuristic)
	{
		this.imgFactoryHeuristic = imgFactoryHeuristic;
		return this;
	}

	/**
	 * @return True if all available images should be opened. Useful if the actual
	 *         range of available images is not known.
	 */
	public boolean imgOpenerIsOpenAllImages() {
		return openAll;
	}

	/**
	 * @param openAll Whether or not all available images should be opened.
	 *          Default: false.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetOpenAllImages(final boolean openAll) {
		this.openAll = openAll;
		return this;
	}

	/**
	 * @return The image range to be opened. Default: [0]
	 */
	public Range imgOpenerGetRange() {
		return range;
	}

	/**
	 * @param index Image index within the dataset to open
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgOpenerSetIndex(final int index) {
		return imgOpenerSetRange(new Range(new Long(index)));
	}

	/**
	 * @param range Range of image indices to open.
	 * @return This SCIFIOConfig for method chaining.
	 * @throws IllegalArgumentException If a valid {@link Range} can not be
	 *           parsed.
	 */
	public SCIFIOConfig imgOpenerSetRange(final String range) {
		return imgOpenerSetRange(new Range(range));
	}

	/**
	 * @param range Image index to open.
	 * @return This SCIFIOConfig for method chaining.
	 * @throws IllegalArgumentException If index &lt; 0
	 */
	public SCIFIOConfig imgOpenerSetRange(final Range range) {
		this.range = range;
		return this;
	}

	// -- ImgSaver methods --

	/**
	 * @return True if channels should be composited during ImgSaver operation.
	 */
	public boolean imgSaverGetWriteRGB() {
		return writeRGB;
	}

	/**
	 * @param rgb Whether or not the ImgSaver should composite channels when
	 *          writing.
	 * @return This SCIFIOConfig for method chaining.
	 */
	public SCIFIOConfig imgSaverSetWriteRGB(final boolean rgb) {
		writeRGB = rgb;
		return this;
	}

	// -- Clonable methods --

	@Override
	public SCIFIOConfig clone() {
		return new SCIFIOConfig(this);
	}
}
