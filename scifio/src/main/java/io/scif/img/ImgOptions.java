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

import io.scif.img.converters.PlaneConverter;
import net.imglib2.Interval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;

/**
 * An options class intended for use when opening Imgs, e.g. by
 * {@link ImgOpener}.
 * <p>
 * Mutator methods will return a reference to the ImgOptions instance, allowing
 * for chaining, such as:
 * <p>
 * {@code new ImgOptions().usePlanarImg().computeMinMax()}
 * </p>
 * </p>
 * 
 * @author Mark Hiner
 */
public class ImgOptions {

	/**
	 * Access type options for opening datasets.
	 * <ul>
	 * <li>
   * {@link ImgMode#ARRAY} will attempt to use {@link ArrayImgFactory}</li>
	 * <li>
   * {@link ImgMode#AUTO} allows the program to decide, e.g. based on
	 * available memory.</li>
	 * <li>
   * {@link ImgMode#CELL} will attempt to use {@link CellImgFactory}</li>
	 * <li>
   * {@link ImgMode#CELL_ARRAY} will {@link ArrayImgFactory} if the image
	 * fits in memory, and {@link CellImgFactory} if it does not.</li>
	 * <li>
   * {@link ImgMode#CELL_PLANAR} will use {@link PlanarImgFactory} if the
	 * image fits in memory, and {@link CellImgFactory} if it does not.</li>
	 * <li>
   * {@link ImgMode#PLANAR} will attempt to use {@link PlanarImgFactory}</li>
	 * </ul>
	 * 
	 * @author Mark Hiner
	 */
	public static enum ImgMode {
		ARRAY, AUTO, CELL, PLANAR;
	}

	/**
	 * Options for checking format compatibility.
	 * <ul>
	 * <li>
   * {@link CheckMode#DEEP} may open the dataset source to make this
	 * determination.</li>
	 * <li>
   * {@link CheckMode#SHALLOW} will never open a source.</li>
	 * </ul>
	 * 
	 * @author Mark Hiner
	 */
	public static enum CheckMode {
		DEEP, SHALLOW;
	}

	// If true, planarEnabled returns true. If false, cellEnabled returns true.
	// If null, both planar/cell enabled will return false.
	private ImgMode[] imgModes;

	// Whether or not a source can be opened when checking format compatibility
	private CheckMode checkMode;

	// sub-region specification for opening portions of an image
	private Interval interval;

	// Whether or not to use a MinMaxFilter
	private boolean computeMinMax;

	// Image index
	private int index;

	// Custom plane converter
	private PlaneConverter planeConverter;

	// Custom heuristic for choosing an ImgFactory
	private ImgFactoryHeuristic imgFactoryHeuristic;

	// -- Constructor --

	public ImgOptions() {
		reset();
	}

	// -- ImgOptions Methods --

	/**
	 * Resets all options to their default values.
	 * 
	 * @return A reference to this ImgOptions
	 */
	public ImgOptions reset() {
		imgModes = new ImgMode[] { ImgMode.AUTO };
		checkMode = CheckMode.SHALLOW;
		computeMinMax = false;
		index = 0;
		interval = null;
		planeConverter = null;
		imgFactoryHeuristic = null;

		return this;
	}

	// -- Getters and Setters --

	/**
	 * @return The access type to attempt to open the dataset with. Default:
	 *         imgMode.AUTO, which allows the calling program to decide.
	 */
	public ImgMode[] getImgModes() {
		return imgModes;
	}

	/**
	 * @param imgModes A list of ImgMode access types. How these are interpreted
	 *          is up to the ImgFactoryHeuristic, but it is reasonable to expect
	 *          modes listed earlier to be preferred.
	 * @return A reference to this ImgOptions instance
	 */
	public ImgOptions setImgModes(final ImgMode... imgModes) {
		this.imgModes = imgModes;
		return this;
	}

	/**
	 * @return Mode to use when checking image format. Default: CheckMode.SHALLOW,
	 *         which will not open sources.
	 */
	public CheckMode getCheckMode() {
		return checkMode;
	}

	/**
	 * @param checkMode
	 * @return A reference to this ImgOptions instance
	 */
	public ImgOptions setCheckMode(final CheckMode checkMode) {
		this.checkMode = checkMode;
		return this;
	}

	/**
	 * @return True if the image should be scaled to its min and max intensities.
	 *         Default: false
	 */
	public boolean isComputeMinMax() {
		return computeMinMax;
	}

	/**
	 * @param computeMinMax Whether or not images should be scaled to min/max
	 *          intensities.
	 * @return A reference to this ImgOptions instance
	 */
	public ImgOptions setComputeMinMax(final boolean computeMinMax) {
		this.computeMinMax = computeMinMax;
		return this;
	}

	/**
	 * @return The image index to be opened. Default: 0
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index Image index to open.x
	 * @return A reference to this ImgOptions instance
	 * @throws IllegalArgumentException If index < 0
	 */
	public ImgOptions setIndex(final int index) {
		if (index < 0) throw new IllegalArgumentException("Invalid index: " +
			index + ". Must be >= 0");
		this.index = index;
		return this;
	}

	/**
	 * Returns an array of dimension lengths. This may be of a different
	 * dimensionality than the underlying image, in which case the lengths are
	 * assume to be in the natural ordering of the image.
	 * 
	 * @return An Subregion specifying dimension offsets and lengths. Default:
	 *         null
	 */
	public Interval getInterval() {
		return interval;
	}

	/**
	 * @param interval Region constraints for any image to open
	 * @return A reference to this ImgOptions instance.
	 */
	public ImgOptions setInterval(final Interval interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * @return A custom plane converter. Default: {@code null}
	 */
	public PlaneConverter getPlaneConverter() {
		return planeConverter;
	}

	/**
	 * @param planeConverter Sets a PlaneConverter to use when opening datasets.
	 *          This is useful when using a custom Img type.
	 * @return A reference to this ImgOptions instance.
	 */
	public ImgOptions setPlaneConverter(final PlaneConverter planeConverter) {
		this.planeConverter = planeConverter;
		return this;
	}

	/**
	 * @return The ImgFactoryHeuristic to use when selecting an ImgFactory.
	 *         Default: {@code null}
	 */
	public ImgFactoryHeuristic getImgFactoryHeuristic() {
		return imgFactoryHeuristic;
	}

	/**
	 * @param imgFactoryHeuristic Heuristic to use when selecting an ImgFactory.
	 *          Will not be used if an ImgFactory is provided to the ImgOpener.
	 * @return A reference to this ImgOptions instance.
	 */
	public ImgOptions setImgFactoryHeuristic(
		final ImgFactoryHeuristic imgFactoryHeuristic)
	{
		this.imgFactoryHeuristic = imgFactoryHeuristic;
		return this;
	}

}
