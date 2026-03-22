/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2026 SCIFIO developers.
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
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.scijava.app.AppService;
import org.scijava.desktop.DesktopService;
import org.scijava.desktop.FileType;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.io.location.RemoteLocation;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;

/**
 * Default {@link FormatService} implementation
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultFormatService extends AbstractSingletonService<Format>
	implements FormatService
{

	// -- Parameters --

	@Parameter
	private PluginService pluginService;

	@Parameter
	private AppService appService;

	@Parameter
	private LogService logService;

	@Parameter(required = false)
	private DesktopService desktopService;

	// -- Fields --

	/*
	 * Ordered set of all available Formats.
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
	 * Map of previously analyzed {@link Location}s
	 * to their matched {@link Format}.
	 */
	private Map<Location, Format> formatCache;

	private boolean dirtyFormatCache = false;

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

		synchronized (formats) {
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
		if (format.getWriterClass() != DefaultWriter.class) {
			writerMap().put(format.getWriterClass(), format);
		}
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
	public Writer getWriterForLocation(final Location fileId)
		throws FormatException
	{
		boolean matched = false;

		Writer w = null;

		for (final Format f : formats()) {
			
			if (f.ownsLocationType(fileId)) {
				w = f.createWriter();
				matched = true;
			}
			
			if (!matched && FormatTools.checkSuffix(fileId.getName(), f
				.getSuffixes()))
			{

				if (!DefaultWriter.class.isAssignableFrom(f.getWriterClass())) {
					w = f.createWriter();
					matched = true;
				}
			}
		}

		if (w == null) {
		    Set<String> suffixes = new TreeSet<>();
		    for (Format f : getOutputFormats()) {
		        suffixes.addAll(Arrays.asList(f.getSuffixes()));
		    }
		    throw new FormatException("No compatible output format found for extension: " + fileId + "\n"  +
		     "Available output formats: " + suffixes);
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

	@Override
	public Format getFormat(final Location id) throws FormatException {
		return getFormat(id, new SCIFIOConfig(getContext()).checkerSetOpen(false));
	}

	@Override
	public Format getFormat(final Location id, final SCIFIOConfig config)
		throws FormatException
	{
		// We do not want remote file access by each checker
		if (id instanceof RemoteLocation) {
			config.checkerSetOpen(false);
		}

		Format format = formatCache().get(id);
		if (format == null) {
			List<Format> formatList = getFormatList(id, config, true);
			if(formatList.isEmpty()) return null;
			format = formatList.get(0);
			synchronized (formats) {
				// Synchronized to protect cache modification
				if (formatCache().get(id) == null) formatCache().put(id, format);
			}
		}
		return format;
	}

	@Override
	public List<Format> getFormatList(final Location id) throws FormatException {
		return getFormatList(id, new SCIFIOConfig(getContext()).checkerSetOpen(false), false);
	}

	@Override
	public List<Format> getFormatList(final Location id,
		final SCIFIOConfig config, final boolean greedy) throws FormatException
	{

		final List<Format> formatList = new ArrayList<>();

		for (final Format format : formats()) {
			if (format.isEnabled() && format.createChecker().isFormat(id, config)) {

				formatList.add(format);

				// if greedy is true, we can end after finding the first format
				if (greedy) break;
			}
		}

		return formatList;
	}

	@Override
	public Format getFormat(final DataHandle<Location> source)
		throws FormatException
	{
		return getFormat(source, new SCIFIOConfig(getContext()).checkerSetOpen(true));
	}

	@Override
	public Format getFormat(final DataHandle<Location> source,
		final SCIFIOConfig config) throws FormatException
	{
		List<Format> formatList = getFormatList(source, config, true);
		return formatList.isEmpty() ? null : formatList.get(0);
	}

	@Override
	public List<Format> getFormatList(final DataHandle<Location> source)
		throws FormatException
	{
		return getFormatList(source, new SCIFIOConfig(getContext()).checkerSetOpen(true),
			false);
	}

	@Override
	public List<Format> getFormatList(final DataHandle<Location> source,
		final SCIFIOConfig config, final boolean greedy) throws FormatException
	{
		final List<Format> formatList = new ArrayList<>();

		boolean found = false;

		for (final Format format : formats()) {
			try {
				if (!found && format.isEnabled() && format.createChecker().isFormat(
					source))
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

	// -- PTService methods --

	@Override
	public Class<Format> getPluginType() {
		return Format.class;
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		return appService.getApp(SCIFIOApp.NAME).getVersion();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		super.initialize();

		// Register format file types with the desktop integration layer.
		if (desktopService != null) {
			// NB: The desktop layer infers precise MIME type as needed;
			// we just need to pass the MIME type *prefix* here, plus wildcard.
			desktopService.addFileTypes(() -> {
				return getInstances().stream().flatMap(
					format -> Arrays.stream(format.getSuffixes()).map(
						suffix -> new FileType(suffix, "image/*", format.getFormatName())
					)
				).collect(Collectors.toList());
			});
		}
	}

	// -- Helper methods --

	private Set<Format> formats() {
		if (formats == null) initFormats();
		return formats;
	}

	private Map<Class<?>, Format> formatMap() {
		if (formats == null) initFormats();
		return formatMap;
	}

	private Map<Class<?>, Format> checkerMap() {
		if (formats == null) initFormats();
		return checkerMap;
	}

	private Map<Class<?>, Format> parserMap() {
		if (formats == null) initFormats();
		return parserMap;
	}

	private Map<Class<?>, Format> readerMap() {
		if (formats == null) initFormats();
		return readerMap;
	}

	private Map<Class<?>, Format> writerMap() {
		if (formats == null) initFormats();
		return writerMap;
	}

	private Map<Class<?>, Format> metadataMap() {
		if (formats == null) initFormats();
		return metadataMap;
	}

	private Map<Location, Format> formatCache() {
		if (formats == null) initFormats();
		if (dirtyFormatCache) {
			// NB: Double lock so that a cache is only cleared once.
			synchronized (formatCache) {
				if (dirtyFormatCache) {
					formatCache.clear();
					dirtyFormatCache = false;
				}
			}
		}
		return formatCache;
	}

	// Helper methods - lazy initialization --

	private synchronized void initFormats() {
		if (this.formats != null) return;

		// NB: Build all tables into locals, then publish the formats field
		// last. Downstream accessors treat a non-null formats as the signal
		// that initialization is complete, so we must not expose any
		// half-built structures. This inlines the work addFormat would do,
		// because going through addFormat (and its accessor calls) would
		// recursively re-enter initFormats before publication.
		final TreeSet<Format> formats = new TreeSet<>();
		final Map<Class<?>, Format> formatMap = new HashMap<>();
		final Map<Class<?>, Format> checkerMap = new HashMap<>();
		final Map<Class<?>, Format> parserMap = new HashMap<>();
		final Map<Class<?>, Format> readerMap = new HashMap<>();
		final Map<Class<?>, Format> writerMap = new HashMap<>();
		final Map<Class<?>, Format> metadataMap = new HashMap<>();

		for (final Format format : getInstances()) {
			if (formatMap.containsKey(format.getClass())) continue;
			formats.add(format);
			formatMap.put(format.getClass(), format);
			checkerMap.put(format.getCheckerClass(), format);
			parserMap.put(format.getParserClass(), format);
			readerMap.put(format.getReaderClass(), format);
			if (format.getWriterClass() != DefaultWriter.class) {
				writerMap.put(format.getWriterClass(), format);
			}
			metadataMap.put(format.getMetadataClass(), format);
			if (format.getContext() == null) format.setContext(getContext());
		}

		this.formatMap = formatMap;
		this.checkerMap = checkerMap;
		this.parserMap = parserMap;
		this.readerMap = readerMap;
		this.writerMap = writerMap;
		this.metadataMap = metadataMap;
		this.formatCache = new WeakHashMap<>();
		this.formats = formats; // publish last
	}
}
