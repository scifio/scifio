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

package io.scif.io;

import io.scif.services.DatasetIOService;

import java.io.IOException;

import net.imagej.Dataset;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * I/O plugin for {@link Dataset}s.
 *
 * @author Curtis Rueden
 */
@Plugin(type = IOPlugin.class, priority = Priority.LOW)
public class DatasetIOPlugin extends AbstractIOPlugin<Dataset> {

	@Parameter
	private DatasetIOService datasetIOService;

	// -- IOPlugin methods --

	@Override
	public Class<Dataset> getDataType() {
		return Dataset.class;
	}

	@Override
	public boolean supportsOpen(final String source) {
		return datasetIOService.canOpen(source);
	}

	@Override
	public boolean supportsSave(final String destination) {
		return datasetIOService.canSave(destination);
	}

	@Override
	public Dataset open(final String source) throws IOException {
		return datasetIOService.open(source);
	}

	@Override
	public void save(final Dataset dataset, final String destination)
		throws IOException
	{
		datasetIOService.save(dataset, destination);
	}
}
