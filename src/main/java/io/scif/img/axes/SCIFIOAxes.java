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

package io.scif.img.axes;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

/**
 * A collection of {@link AxisType}s useful to scientific image formats.
 *
 * @author Mark Hiner
 */
public final class SCIFIOAxes {

	// -- Private constructor to prevent instantiation --
	private SCIFIOAxes() {}

	// -- Custom AxisType constants --

	/**
	 * Identifies the <i>Spectra</i> dimensional type, representing a dimension
	 * consisting of spectral channels.
	 */
	public static final AxisType SPECTRA = Axes.get("Spectra");

	/**
	 * Identifies the <i>Lifetime</i> dimensional type, representing a dimension
	 * consisting of a lifetime histogram.
	 */
	public static final AxisType LIFETIME = Axes.get("Lifetime");

	/**
	 * Identifies the <i>Polarization</i> dimensional type, representing a
	 * dimension consisting of polarization states.
	 */
	public static final AxisType POLARIZATION = Axes.get("Polarization");

	/**
	 * Identifies the <i>Phase</i> dimensional type, representing a dimension
	 * consisting of phases.
	 */
	public static final AxisType PHASE = Axes.get("Phase");

	/**
	 * Identifies the <i>Frequency</i> dimensional type, representing a dimension
	 * consisting of frequencies.
	 */
	public static final AxisType FREQUENCY = Axes.get("Frequency");

}
