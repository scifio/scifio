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

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.exception.IncompatibleTypeException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default {@link DatasetIOService} implementation.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultDatasetIOService extends AbstractService implements
	DatasetIOService
{

	@Parameter
	private FormatService formatService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LogService log;

	@Override
	public boolean canOpen(final String source) {
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
	public boolean canSave(final String destination) {
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
		// skip min/max computation
		config.imgOpenerSetComputeMinMax(false);
		// prefer planar array structure, for ImageJ1 and ImgSaver compatibility
		config.imgOpenerSetImgModes(ImgMode.PLANAR);
		return open(source, config);
	}

	@Override
	public Dataset open(final String source, final SCIFIOConfig config)
		throws IOException
	{
		final ImgOpener imageOpener = new ImgOpener(getContext());
		try {
			// TODO openImgs we are only using the first image index in the
			// SCIFIOConfig.imgOpenerGetRange - so this image index corresponds to the
			// first ImgPlus in the list returned by the ImgOpener. See
			// https://github.com/scifio/scifio/issues/259

			final SCIFIOImgPlus<?> imgPlus =
				imageOpener.openImgs(source, config).get(0);

			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Dataset dataset = datasetService.create((ImgPlus) imgPlus);

			final ImageMetadata imageMeta = imgPlus.getImageMetadata();
			updateDataset(dataset, imageMeta);
			return dataset;
		}
		catch (final ImgIOException exc) {
			throw new IOException(exc);
		}
	}

        
    @Override
    public List<net.imagej.Dataset> openAll(String source)
            throws IOException
    {
            final SCIFIOConfig config = new SCIFIOConfig();
            config.imgOpenerSetImgModes(ImgMode.PLANAR);
            return openAll(source, config);
    }

    @Override
    public List<net.imagej.Dataset> openAll(String source, SCIFIOConfig config)
            throws IOException
    {
            final ArrayList<Dataset> datasetList = new ArrayList<>();

            final ImgOpener imageOpener = new ImgOpener(getContext());
            try {
                final List<SCIFIOImgPlus<?>> openImgs = imageOpener.openImgs(source, config);
                for (int imgId = 0; imgId != openImgs.size(); imgId++) {
                    
                    final SCIFIOImgPlus<?> imgPlus = openImgs.get(imgId);
                    
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    final Dataset dataset = datasetService.create((ImgPlus) imgPlus);

                    final ImageMetadata imageMeta = imgPlus.getImageMetadata();
                    updateDataset(dataset, imageMeta);
                    datasetList.add(dataset);
                }

            } catch (final ImgIOException exc) {
                throw new IOException(exc);
            }
            return datasetList;
    }


	@Override
	public Metadata save(final Dataset dataset, final String destination)
		throws IOException
	{
		return save(dataset, destination, new SCIFIOConfig());
	}

	@Override
	public Metadata save(final Dataset dataset, final String destination,
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

	@Override
	public void revert(final Dataset dataset) throws IOException {
		final String source = dataset.getSource();
		if (source == null || source.isEmpty()) {
			// no way to revert
			throw new IOException("Cannot revert image of unknown origin");
		}
		final Dataset revertedDataset = open(source);
		revertedDataset.copyInto(dataset);
	}

	// -- Helper methods --

	/**
	 * The {@link DatasetService#create} methods make a best guess for populating
	 * {@link Dataset} information. But this can be incorrect/over-aggressive,
	 * e.g. in the case of RGBMerged state.
	 * <p>
	 * If we have access to the {@link Metadata} instance backing a
	 * {@code Dataset}, we can use it to more accurately populate these settings.
	 * </p>
	 *
	 * @param dataset Dataset instance to update.
	 * @param imageMeta Metadata instance to query for updated information.
	 */
	private void updateDataset(final Dataset dataset,
		final ImageMetadata imageMeta)
	{
		// If the original image had some level of merged channels, we should set
		// RGBmerged to true for the sake of backwards compatibility.
		// See https://github.com/imagej/imagej-legacy/issues/104

		// Look for Axes.CHANNEL in the planar axis list. If found, set RGBMerged to
		// true.
		boolean rgbMerged = false;

		for (final CalibratedAxis axis : imageMeta.getAxesPlanar()) {
			if (axis.type().equals(Axes.CHANNEL)) rgbMerged = true;
		}

		dataset.setRGBMerged(rgbMerged);
	}

    
}
