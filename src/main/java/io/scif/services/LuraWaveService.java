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

package io.scif.services;

import io.scif.DependencyException;
import io.scif.SCIFIOService;

import java.io.IOException;
import java.io.InputStream;

import org.scijava.Optional;

/**
 * @author Chris Allan
 */
public interface LuraWaveService extends SCIFIOService, Optional {

	// -- Constants --

	/** System property to check for the LuraWave license code. */
	public static final String LICENSE_PROPERTY = "lurawave.license";

	/** Message displayed if the LuraWave LWF decoder library is not found. */
	public static final String NO_LURAWAVE_MSG =
		"The LuraWave decoding library, lwf_jsdk2.6.jar, is required to decode " +
			"this file.\r\nPlease make sure it is present in your classpath.";

	/** Message to display if no LuraWave license code is given. */
	public static final String NO_LICENSE_MSG =
		"No LuraWave license code was specified.\r\nPlease set one in the " +
			LICENSE_PROPERTY + " system property (e.g., with -D" + LICENSE_PROPERTY +
			"=XXXX from the command line).";

	/** Message to display if an invalid LuraWave license code is given. */
	public static final String INVALID_LICENSE_MSG = "Invalid license code: ";

	/** Identifying field in stub class. */
	public static final String STUB_FIELD = "IS_STUB";

	// -- API Methods --

	/**
	 * Overrides the license code to use when initializing the LuraWave decoder.
	 * By default the license code is loaded from the "lurawave.license" system
	 * property.
	 *
	 * @param license String license code.
	 */
	public void setLicenseCode(String license);

	/**
	 * Retrieves the current license code as a string.
	 *
	 * @return See above.
	 */
	public String getLicenseCode();

	/**
	 * Wraps
	 * {@code com.luratech.lwf.lwfDecoder#lwfDecoder(InputStream, String, String)}
	 * .
	 *
	 * @throws IOException If parsing of the image header fails.
	 * @throws DependencyException If no license code was specified.
	 * @throws ServiceException If the license code is invalid.
	 */
	public void initialize(InputStream stream) throws IOException,
		DependencyException, ServiceException;

	/** Wraps {@code com.luratech.lwf.lwfDecoder#getWidth()} */
	public int getWidth();

	/** Wraps {@code com.luratech.lwf.lwfDecoder#getHeight()} */
	public int getHeight();

	/**
	 * Wraps
	 * {@code com.luratech.lwf.lwfDecoder#decodeToMemoryGray8(byte[], int, int, int)}
	 * .
	 *
	 * @throws ServiceException If the license code is invalid.
	 */
	public void decodeToMemoryGray8(byte[] image, int limit, int quality,
		int scale) throws ServiceException;

	/**
	 * Wraps
	 * {@code com.luratech.lwf.lwfDecoder#decodeToMemoryGray16(short[], int, int, int, int, int, int, int, int, int, int)}
	 * .
	 *
	 * @throws ServiceException If the license code is invalid.
	 */
	public void decodeToMemoryGray16(short[] image, int imageoffset, int limit,
		int quality, int scale, int pdx, int pdy, int clip_x, int clip_y,
		int clip_w, int clip_h) throws ServiceException;

}
