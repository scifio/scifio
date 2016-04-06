/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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

import io.scif.config.SCIFIOConfig;

import java.io.IOException;
import java.util.List;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.DefaultDatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.service.ServiceIndex;

/**
 * This is a temporary service designed to supercede
 * {@link DefaultDatasetService}. Specifically it implements the deprecated API
 * from {@link DatasetService} by delegating to the {@link DatasetIOService}
 * while the remaining methods are deferred to the next-highest priority
 * {@link DatasetService}.
 *
 * @deprecated Use {@link DatasetIOService} for the {@link #canOpen(String)},
 *             {@link #canSave(String)}, {@link #save}, {@link #open} and
 *             {@link #revert(Dataset)} methods. Use {@link DatasetService} for
 *             all other methods.
 * @author Mark Hiner
 */
@Deprecated
@Plugin(type = Service.class, priority = Priority.NORMAL_PRIORITY + 1)
public class SCIFIODatasetService extends AbstractService implements
	DatasetService
{

	// -- Fields --

	// NB: These are not @Parameters to avoid infinite recursion
	// See the initialize() and datasetIOService() methods for
	// their initialization.
	private DatasetIOService datasetIOService;

	private DatasetService datasetService;

	// --DatasetService API --

	@Override
	public ObjectService getObjectService() {
		return datasetService().getObjectService();
	}

	@Override
	public List<Dataset> getDatasets() {
		return datasetService().getDatasets();
	}

	@Override
	public List<Dataset> getDatasets(final ImageDisplay display) {
		return datasetService().getDatasets(display);
	}

	@Override
	public Dataset create(final long[] dims, final String name,
		final AxisType[] axes, final int bitsPerPixel, final boolean signed,
		final boolean floating)
	{
		return datasetService().create(dims, name, axes, bitsPerPixel, signed,
			floating);
	}

	@Override
	public Dataset create(final long[] dims, final String name,
		final AxisType[] axes, final int bitsPerPixel, final boolean signed,
		final boolean floating, final boolean virtual)
	{
		return datasetService().create(dims, name, axes, bitsPerPixel, signed,
			floating, virtual);
	}

	@Override
	public <T extends RealType<T> & NativeType<T>> Dataset create(final T type,
		final long[] dims, final String name, final AxisType[] axes)
	{
		return datasetService().create(type, dims, name, axes);
	}

	@Override
	public <T extends RealType<T> & NativeType<T>> Dataset create(final T type,
		final long[] dims, final String name, final AxisType[] axes,
		final boolean virtual)
	{
		return datasetService().create(type, dims, name, axes, virtual);
	}

	@Override
	public <T extends RealType<T>> Dataset create(final ImgFactory<T> factory,
		final T type, final long[] dims, final String name, final AxisType[] axes)
	{
		return datasetService().create(factory, type, dims, name, axes);
	}

	@Override
	public <T extends RealType<T>> Dataset create(final ImgPlus<T> imgPlus) {
		return datasetService().create(imgPlus);
	}

	@Override
	public <T extends RealType<T>> Dataset create(
		final RandomAccessibleInterval<T> rai)
	{
		return datasetService().create(rai);
	}

	@Override
	public boolean canOpen(final String source) {
		return datasetIOService().canOpen(source);
	}

	@Override
	public boolean canSave(final String destination) {
		return datasetIOService().canSave(destination);
	}

	@Override
	public Dataset open(final String source) throws IOException {
		return datasetIOService().open(source);
	}

	@Override
	public Dataset open(final String source, final Object config)
		throws IOException
	{
		return datasetIOService().open(source, getConfig(config));
	}

	@Override
	public void revert(final Dataset dataset) throws IOException {
		datasetIOService().revert(dataset);
	}

	@Override
	public Object save(final Dataset dataset, final String destination)
		throws IOException
	{
		return datasetIOService().save(dataset, destination);
	}

	@Override
	public Object save(final Dataset dataset, final String destination,
		final Object config) throws IOException
	{
		return datasetIOService().save(dataset, destination, getConfig(config));
	}

	// -- Service methods --

	@Override
	public void initialize() {}

	// -- Helper methods --

	/**
	 * @return The given parameter cast as a {@link SCIFIOConfig}, or null if not
	 *         compatible.
	 */
	private SCIFIOConfig getConfig(final Object o) {
		if (SCIFIOConfig.class.isAssignableFrom(o.getClass())) {
			return (SCIFIOConfig) o;
		}
		return null;
	}

	/**
	 * Lazy helper method to circumvent the circular dependency between
	 * {@link DefaultDatasetIOService} and this class. This problem will go away
	 * when the API provided by {@link DatasetIOService} is expunged from
	 * {@link DatasetService}.
	 *
	 * @return Active {@code DatasetIOService}.
	 */
	private DatasetIOService datasetIOService() {
		if (datasetIOService == null) {
			datasetIOService = getContext().getService(DatasetIOService.class);
		}

		return datasetIOService;
	}

	/**
	 * Lazy helper method to allow {@link DefaultDatasetService} to perform much
	 * of the work of this class, despite being lower priority.
	 *
	 * @return Active {@code DatasetService}.
	 */
	private DatasetService datasetService() {
		if (datasetService == null) {
			final ServiceIndex index = getContext().getServiceIndex();

			// Set datasetService to the next highest priority service
			datasetService =
				index.getNextService(DatasetService.class, SCIFIODatasetService.class);
		}
		return datasetService;
	}
}
