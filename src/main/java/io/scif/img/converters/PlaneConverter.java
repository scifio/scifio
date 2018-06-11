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
import io.scif.SCIFIOPlugin;
import io.scif.config.SCIFIOConfig;

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.SingletonPlugin;

/**
 * Interface for using planes read by SCIFIO {@link Reader}s to populate
 * {@link ImgPlus} instances.
 *
 * @author Mark Hiner
 */
public interface PlaneConverter extends SCIFIOPlugin, SingletonPlugin {

	/**
	 * @param reader Reader that was used to open the source plane
	 * @param imageIndex image index within the dataset
	 * @param planeIndex plane index within the image
	 * @param source the opened plane
	 * @param dest the ImgPlus to populate
	 * @param config SCIFIOConfig for opening this plane
	 */
	<T extends RealType<T>> void populatePlane(Reader reader, int imageIndex,
		int planeIndex, byte[] source, ImgPlus<T> dest, SCIFIOConfig config);
}
