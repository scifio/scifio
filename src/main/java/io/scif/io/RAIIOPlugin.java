/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2024 SCIFIO developers.
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

package io.scif.io;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.net.URISyntaxException;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * I/O plugin for {@link RandomAccessibleInterval}s.
 * <p>
 * Saving only. Works by wrapping the provided {@link RandomAccessibleInterval}
 * to a {@link Dataset} before saving it with the {@link DatasetIOService}.
 * </p>
 *
 * @author Curtis Rueden
 */
@Plugin(type = IOPlugin.class, priority = Priority.VERY_LOW)
public class RAIIOPlugin<T extends Type<T>> extends
	AbstractIOPlugin<RandomAccessibleInterval<T>>
{

	@Parameter
	private DatasetIOService datasetIOService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LocationService locationService;

	// -- IOPlugin methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<RandomAccessibleInterval<T>> getDataType() {
		return (Class) RandomAccessibleInterval.class;
	}

	@Override
	public boolean supportsSave(final String destination) {
		return datasetIOService.canSave(resolve(destination, "destination"));
	}

	@Override
	public boolean supportsSave(final Location destination) {
		return datasetIOService.canSave(destination);
	}

	@Override
	public void save(final RandomAccessibleInterval<T> rai,
		final String destination) throws IOException
	{
		final Dataset dataset = datasetService.create(rai);
		datasetIOService.save(dataset, resolve(destination, "destination"));
	}

	@Override
	public void save(final RandomAccessibleInterval<T> rai,
		final Location destination) throws IOException
	{
		final Dataset dataset = datasetService.create(rai);
		datasetIOService.save(dataset, destination);
	}

	// -- Helper methods --

	private Location resolve(final String uriString, final String label) {
		try {
			final Location location = locationService.resolve(uriString);
			if (location != null) return location;
			throw new IllegalArgumentException( //
				"Unresolvable " + label + ": " + uriString);
		}
		catch (final URISyntaxException exc) {
			throw new IllegalArgumentException( //
				"Invalid " + label + ": " + uriString, exc);
		}
	}
}
