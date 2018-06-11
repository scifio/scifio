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

package io.scif.img.converters;

import io.scif.Reader;
import io.scif.SCIFIOService;

import java.util.List;
import java.util.Map;

import net.imagej.ImgPlus;

import org.scijava.plugin.SingletonService;

/**
 * Interface for requesting {@link PlaneConverter}s to convert planes read by
 * SCIFIO {@link Reader}s to {@link ImgPlus} instances.
 *
 * @author Mark Hiner
 */
public interface PlaneConverterService extends SingletonService<PlaneConverter>,
	SCIFIOService
{

	// -- PlaneConverterService methods --

	/**
	 * @return a Map of all PlaneConverter names to instances
	 */
	Map<String, PlaneConverter> getPlaneConverters();

	/**
	 * @return A List of all PlaneConverter names
	 */
	List<String> getPlaneConverterNames();

	/**
	 * @param name The desired PlaneConverter's name
	 * @return The PlaneConverter instance if it exists
	 */
	PlaneConverter getPlaneConverter(String name);

	/**
	 * @return A PlaneConverter for converting to Array Imgs
	 */
	PlaneConverter getArrayConverter();

	/**
	 * @return A PlaneConverter for converting to Planar Imgs
	 */
	PlaneConverter getPlanarConverter();

	/**
	 * @return A PlaneConverter for converting to any Img
	 */
	PlaneConverter getDefaultConverter();
}
