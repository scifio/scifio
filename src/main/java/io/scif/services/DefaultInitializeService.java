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

import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Parser;
import io.scif.Reader;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;

import java.io.IOException;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default {@link InitializeService} implementation.
 *
 * @see io.scif.services.InitializeService
 * @author Mark Hiner
 */
@Plugin(type = Service.class, priority = Priority.LOW)
public class DefaultInitializeService extends AbstractService implements
	InitializeService
{

	// -- Parameters --

	@Parameter
	private PluginService pluginService;

	@Parameter
	private FormatService formatService;

	@Parameter
	private TranslatorService translatorService;

	@Parameter
	private LocationService locationService;

	// -- InitializeService API Methods --

	@Override
	public ReaderFilter initializeReader(final String id) throws FormatException,
		IOException
	{
		return initializeReader(id, new SCIFIOConfig().checkerSetOpen(false));
	}

	@Override
	public ReaderFilter initializeReader(final String id,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		final Reader r = formatService.getFormat(id, config).createReader();
		r.setSource(id, config);
		return new ReaderFilter(r);
	}

	@Override
	public Writer initializeWriter(final String source, final String destination)
		throws FormatException, IOException
	{
		return initializeWriter(source, destination, new SCIFIOConfig()
			.checkerSetOpen(false));
	}

	@Override
	public Writer initializeWriter(final String source, final String destination,
		final SCIFIOConfig config) throws FormatException, IOException
	{

		final Format sFormat = formatService.getFormat(source, config);
		final Parser parser = sFormat.createParser();
		final Metadata sourceMeta = parser.parse(source, config);

		return initializeWriter(sourceMeta, destination, config);
	}

	@Override
	public Writer initializeWriter(final Metadata sourceMeta,
		final String destination) throws FormatException, IOException
	{
		return initializeWriter(sourceMeta, destination, new SCIFIOConfig()
			.checkerSetOpen(false));
	}

	@Override
	public Writer initializeWriter(final Metadata sourceMeta,
		final String destination, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		final Format sFormat = sourceMeta.getFormat();
		final Format dFormat = formatService.getWriterByExtension(destination)
			.getFormat();
		Metadata destMeta = dFormat.createMetadata();

		// if dest is a different format than source, translate..
		if (sFormat == dFormat) {
			// otherwise we can directly cast, since they are the same types
			destMeta = castMeta(sourceMeta, destMeta.getClass());

		}
		else {
			// Attempt to directly translate between these formats

			destMeta = dFormat.createMetadata();
			translatorService.translate(sourceMeta, destMeta, false);
		}

		destMeta.setDatasetName(destination);

		final Writer writer = dFormat.createWriter();
		writer.setMetadata(destMeta);
		writer.setDest(destination, config);

		return writer;
	}

	@Override
	public Metadata parseMetadata(final String id) throws IOException,
		FormatException
	{
		return parseMetadata(id, new SCIFIOConfig().checkerSetOpen(false));
	}

	@Override
	public Metadata parseMetadata(final String id, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		final Format format = formatService.getFormat(id, config);
		return format.createParser().parse(id, config);
	}

	// -- Helper Methods --

	/*
	 * Hide the suppress warnings in an atomic cast method <p> NB: endType
	 * parameter is just there to guarantee a return type </p>
	 */
	private <N extends Metadata, M extends Metadata> M castMeta(final N metadata,
		@SuppressWarnings("unused") final Class<M> endType)
	{
		@SuppressWarnings("unchecked")
		final M meta = (M) metadata;
		return meta;
	}
}
