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

import io.scif.Checker;
import io.scif.DefaultWriter;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Parser;
import io.scif.Reader;
import io.scif.Writer;
import io.scif.app.SCIFIOApp;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.scijava.app.AppService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Default {@link FormatService} implementation
 *
 * @see io.scif.services.FormatService
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultFormatService extends AbstractService implements
	FormatService
{

	// -- Parameters --

	@Parameter
	private PluginService pluginService;

	@Parameter
	private AppService appService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService logService;

	// -- Fields --

	/*
	 * A list of all available Formats
	 */
	private Set<Format> formats;

	/*
	 * Maps Format classes to their instances.
	 */
	private Map<Class<?>, Format> formatMap;

	/*
	 * Maps Checker classes to their parent Format instance.
	 */
	private Map<Class<?>, Format> checkerMap;

	/*
	 * Maps Parser classes to their parent Format instance.
	 */
	private Map<Class<?>, Format> parserMap;

	/*
	 * Maps Reader classes to their parent Format instance.
	 */
	private Map<Class<?>, Format> readerMap;

	/*
	 * Maps Writer classes to their parent Format instance.
	 */
	private Map<Class<?>, Format> writerMap;

	/*
	 * Maps Metadata classes to their parent Format instance.
	 */
	private Map<Class<?>, Format> metadataMap;

	/*
	 * Maps String ids to their associated Format.
	 * TODO: Update this logic for
	 * https://github.com/scifio/scifio/issues/237
	 */
	private Map<String, Format> formatCache;

	private boolean dirtyFormatCache = false;

	// Flag to mark if this service has been initialized or not.
	private boolean initialized = false;

	// If this value returns true, the current thread has permission to access
	// uninitialized data structures.
	private final ThreadLocal<Boolean> threadLock = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	// -- FormatService API Methods --

	@Override
	public String[] getSuffixes() {
		final TreeSet<String> ts = new TreeSet<>();

		for (final Format f : formats()) {
			for (final String s : f.getSuffixes()) {
				ts.add(s);
			}
		}

		return ts.toArray(new String[ts.size()]);
	}

	@Override
	public boolean addFormat(final Format format) {
		// already have an entry for this format
		if (formatMap().get(format.getClass()) != null) return false;

		synchronized(formats) {
			// synchronized lock to protect format adding
			if (formatMap().get(format.getClass()) == null) {
				formats().add(format);
				formatMap().put(format.getClass(), format);
				addComponents(format);
			}
		}

		if (format.getContext() == null) format.setContext(getContext());
		return true;
	}

	@Override
	public boolean removeFormat(final Format format) {
		removeComponents(format);
		formatMap().remove(format.getClass());
		dirtyFormatCache = true;
		return formats().remove(format);
	}

	@Override
	public void addComponents(final Format format) {
		checkerMap().put(format.getCheckerClass(), format);
		parserMap().put(format.getParserClass(), format);
		readerMap().put(format.getReaderClass(), format);
		writerMap().put(format.getWriterClass(), format);
		metadataMap().put(format.getMetadataClass(), format);
	}

	@Override
	public void removeComponents(final Format format) {
		checkerMap().remove(format.getCheckerClass());
		parserMap().remove(format.getParserClass());
		readerMap().remove(format.getReaderClass());
		writerMap().remove(format.getWriterClass());
		metadataMap().remove(format.getMetadataClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <F extends Format> F getFormatFromClass(final Class<F> formatClass) {
		return (F) formatMap().get(formatClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Format getFormatFromComponent(final Class<?> componentClass) {
		Format fmt = null;

		if (Reader.class.isAssignableFrom(componentClass)) {
			fmt = getFormatFromReader((Class<? extends Reader>) componentClass);
		}
		else if (Writer.class.isAssignableFrom(componentClass)) {
			fmt = getFormatFromWriter((Class<? extends Writer>) componentClass);
		}
		else if (Metadata.class.isAssignableFrom(componentClass)) {
			fmt = getFormatFromMetadata((Class<? extends Metadata>) componentClass);
		}
		else if (Parser.class.isAssignableFrom(componentClass)) {
			fmt = getFormatFromParser((Class<? extends Parser>) componentClass);
		}
		else if (Checker.class.isAssignableFrom(componentClass)) {
			fmt = getFormatFromChecker((Class<? extends Checker>) componentClass);
		}

		return fmt;
	}

	@Override
	public <R extends Reader> Format getFormatFromReader(
		final Class<R> readerClass)
	{
		return readerMap().get(readerClass);
	}

	@Override
	public <W extends Writer> Format getFormatFromWriter(
		final Class<W> writerClass)
	{
		return writerMap().get(writerClass);
	}

	@Override
	public Writer getWriterByExtension(final String fileId)
		throws FormatException
	{
		boolean matched = false;

		Writer w = null;

		for (final Format f : formats()) {
			if (!matched && FormatTools.checkSuffix(fileId, f.getSuffixes())) {

				if (!DefaultWriter.class.isAssignableFrom(f.getWriterClass())) {
					w = f.createWriter();
					matched = true;
				}
			}
		}

		if (w == null) {
			throw new FormatException(
				"No compatible output format found for extension: " + fileId);
		}
		return w;
	}

	@Override
	public <C extends Checker> Format getFormatFromChecker(
		final Class<C> checkerClass)
	{
		return checkerMap().get(checkerClass);
	}

	@Override
	public <P extends Parser> Format getFormatFromParser(
		final Class<P> parserClass)
	{
		return parserMap().get(parserClass);
	}

	@Override
	public <M extends Metadata> Format getFormatFromMetadata(
		final Class<M> metadataClass)
	{
		return metadataMap().get(metadataClass);
	}

	/**
	 * Returns the first Format known to be compatible with the source provided.
	 * Formats are checked in ascending order of their priority. The source is
	 * read if necessary to determine compatibility.
	 *
	 * @param id the source
	 * @return A Format reference compatible with the provided source.
	 */
	@Override
	public Format getFormat(final String id) throws FormatException {
		return getFormat(id, new SCIFIOConfig().checkerSetOpen(false));
	}

	@Override
	public Format getFormat(final String id, final SCIFIOConfig config)
		throws FormatException
	{
		Format format = formatCache().get(id);
		if (format == null) {
			format = getFormatList(id, config, true).get(0);
			synchronized (formats) {
				// Synchronized to protect cache modification
				if (formatCache().get(id) == null) formatCache().put(id, format);
			}
		}
		return format;
	}

	@Override
	public List<Format> getFormatList(final String id) throws FormatException {
		return getFormatList(id, new SCIFIOConfig().checkerSetOpen(false), false);
	}

	@Override
	public List<Format> getFormatList(final String id, final SCIFIOConfig config,
		final boolean greedy) throws FormatException
	{

		final List<Format> formatList = new ArrayList<>();

		boolean found = false;

		for (final Format format : formats()) {
			if (!found && format.isEnabled() &&
				format.createChecker().isFormat(id, config))
			{
				// if greedy is true, we can end after finding the first format
				found = greedy;
				formatList.add(format);
			}
		}

		if (formatList.isEmpty()) {
			throw new FormatException(id + ": No supported format found.");
		}

		return formatList;
	}

	@Override
	public Format getFormat(final RandomAccessInputStream source)
		throws FormatException
	{
		return getFormat(source, new SCIFIOConfig().checkerSetOpen(true));
	}

	@Override
	public Format getFormat(final RandomAccessInputStream source, final SCIFIOConfig config)
		throws FormatException
	{
		return getFormatList(source, config, true).get(0);
	}

	@Override
	public List<Format> getFormatList(final RandomAccessInputStream source)
		throws FormatException
	{
		return getFormatList(source, new SCIFIOConfig().checkerSetOpen(true), false);
	}

	@Override
	public List<Format> getFormatList(final RandomAccessInputStream source,
		final SCIFIOConfig config, final boolean greedy) throws FormatException
	{
		final List<Format> formatList = new ArrayList<>();

		boolean found = false;

		for (final Format format : formats()) {
			try {
				if (!found && format.isEnabled() &&
					format.createChecker().isFormat(source))
				{
					// if greedy is true, we can end after finding the first format
					found = greedy;
					formatList.add(format);
				}
				// Reset the stream
				source.seek(0);
			}
			catch (final IOException e) {
				throw new FormatException(e);
			}
		}

		if (formatList.isEmpty()) {
			throw new FormatException("No supported format found.");
		}

		return formatList;
	}

	@Override
	public Set<Format> getAllFormats() {
		return formats();
	}

	@Override
	public Collection<Format> getOutputFormats() {
		return writerMap().values();
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		return appService.getApp(SCIFIOApp.NAME).getVersion();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// TODO replace with preload implementation.
		// See https://github.com/scijava/scijava-common/issues/145
		threadService.run(new Runnable() {

			@Override
			public void run() {
				// Allow this thread to bypass the initialization check
				threadLock.set(true);

				formats = new TreeSet<>();
				formatMap = new HashMap<>();
				checkerMap = new HashMap<>();
				parserMap = new HashMap<>();
				readerMap = new HashMap<>();
				writerMap = new HashMap<>();
				metadataMap = new HashMap<>();
				formatCache = new WeakHashMap<>();

				// Initialize format information
				for (final Format format : pluginService
					.createInstancesOfType(Format.class))
				{
					addFormat(format);
				}

				initialized = true;
			}
		});
	}

	// -- Private Methods --

	private Set<Format> formats() {
		checkLock();
		return formats;
	}

	private Map<Class<?>, Format> formatMap() {
		checkLock();
		return formatMap;
	}

	private Map<Class<?>, Format> checkerMap() {
		checkLock();
		return checkerMap;
	}

	private Map<Class<?>, Format> parserMap() {
		checkLock();
		return parserMap;
	}

	private Map<Class<?>, Format> readerMap() {
		checkLock();
		return readerMap;
	}

	private Map<Class<?>, Format> writerMap() {
		checkLock();
		return writerMap;
	}

	private Map<Class<?>, Format> metadataMap() {
		checkLock();
		return metadataMap;
	}

	private Map<String, Format> formatCache() {
		checkLock();
		if (dirtyFormatCache) {
			// Double lock so that a cache is only cleared once
			synchronized (formatCache) {
				if (dirtyFormatCache) {
					formatCache.clear();
					dirtyFormatCache = false;
				}
			}
		}
		return formatCache;
	}

	/**
	 * Helper method that checks if one of these is true:
	 * <ul>
	 * <li>This thread is given permission to access data structures before
	 * initialization</li>
	 * <li>The FormatService is initialized</li>
	 * </ul>
	 * If either is true, returns harmlessly. If not, this thread waits for the
	 * initialization thread to complete.
	 */
	private void checkLock() {
		if (!(initialized || threadLock.get())) {
			synchronized (this) {
				// Double locked to avoid missing signals
				while (!(initialized || threadLock.get())) {
					try {
						// Limit excessive polling
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						logService.error("DefaultFormatService: " +
							"Interrupted while waiting for format initialization.", e);
					}
				}
			}
		}
	}
}
