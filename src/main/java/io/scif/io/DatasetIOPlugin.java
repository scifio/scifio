/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
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

package io.scif.io;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;
import io.scif.services.FormatService;

import java.io.File;
import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.meta.ImgPlus;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * I/O plugin for {@link Dataset}s.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = IOPlugin.class, priority = Priority.LOW_PRIORITY)
public class DatasetIOPlugin extends AbstractIOPlugin<Dataset> {

	@Parameter
	private FormatService formatService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LogService log;

	// -- IOPlugin methods --

	@Override
	public Class<Dataset> getDataType() {
		return Dataset.class;
	}

	@Override
	public boolean supportsOpen(final String source) {
		try {
			return formatService.getFormat(source, new SCIFIOConfig()
				.checkerSetOpen(true)) != null;
		}
		catch (final FormatException exc) {
			log.error(exc);
		}
		return false;
	}

	@Override
	public boolean supportsSave(final String destination) {
		try {
			return formatService.getWriterByExtension(destination) != null;
		}
		catch (final FormatException exc) {
			log.error(exc);
		}
		return false;
	}

	@Override
	public Dataset open(final String source) throws IOException {
		final SCIFIOConfig config = new SCIFIOConfig();
		config.imgOpenerSetIndex(0);
		return open(source, config);
	}

	@Override
	public void save(final Dataset dataset, final String destination)
		throws IOException
	{
		save(dataset, destination, null);
	}

	// -- Helper methods --

	private Dataset open(final String source, final SCIFIOConfig config)
		throws IOException
	{
		final ImgOpener imageOpener = new ImgOpener(getContext());

		// skip min/max computation
		config.imgOpenerSetComputeMinMax(false);

		// prefer planar array structure, for ImageJ1 and ImgSaver compatibility
		config.imgOpenerSetImgModes(ImgMode.PLANAR);

		try {
			final SCIFIOImgPlus<?> imgPlus =
				imageOpener.openImgs(source, config).get(0);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Dataset dataset = datasetService.create((ImgPlus) imgPlus);
			return dataset;
		}
		catch (final ImgIOException exc) {
			throw new IOException(exc);
		}
	}

	private Metadata save(final Dataset dataset, final String destination,
		final SCIFIOConfig config) throws IOException
	{
		@SuppressWarnings("rawtypes")
		final ImgPlus img = dataset.getImgPlus();

		final Metadata metadata;
		final ImgSaver imageSaver = new ImgSaver(getContext());
		try {
			metadata = imageSaver.saveImg(destination, img, config);
		}
		catch (final ImgIOException exc) {
			throw new IOException(exc);
		}
		catch (final IncompatibleTypeException exc) {
			throw new IOException(exc);
		}

		final String name = new File(destination).getName();
		dataset.setName(name);
		dataset.setDirty(false);

		return metadata;
	}

}
