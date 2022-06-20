/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2022 SCIFIO developers.
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

package io.scif.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

import org.freedesktop.BaseDirectory;
import org.scijava.download.DiskLocationCache;
import org.scijava.download.DownloadService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.task.Task;
import org.scijava.util.ByteArray;
import org.scijava.util.DigestUtils;

/**
 * @author Gabriel Einsdorf, KNIME GmbH
 */
@Plugin(type = Service.class)
public class DefaultSampleFilesService extends AbstractService implements
	SampleFileService
{

	@Parameter
	private DownloadService downloadService;

	@Parameter
	private LogService log;

	/** Models which are already cached in memory. */
	private final Map<String, FileLocation> sources = new HashMap<>();

	/** stores retrieved sources locally */
	private DiskLocationCache sourceCache;

	@Override
	public FileLocation prepareFormatTestFolder(final Location... zipSources)
		throws IOException
	{
		if (zipSources.length == 0) {
			throw new IllegalArgumentException(
				"At least one zip source is required!");
		}

		final byte[] bytes = Arrays.stream(zipSources).map(Object::toString).reduce(
			"", (a, b) -> a + b).getBytes();
		final String localFolderName = DatatypeConverter.printHexBinary(DigestUtils
			.sha1(bytes));

		// test if we already downloaded and unpacked the source
		FileLocation out = sources.get(localFolderName);
		if (out == null) {
			// not cached we need to download it

			final File basefolder = new File(sourceCache().getBaseDirectory(),
				localFolderName);
			try {
				if (zipSources.length == 1) { // extract directly into basedir
					downloadAndUnpackResource(zipSources[0], basefolder);
				}
				else {

					for (final Location source : zipSources) {
						String targetName;
						if (source.getName().equals(source.defaultName())) {
							targetName = source.getURI().getPath().substring(1).replaceAll("\\/", "-");
						}
						else {
							targetName = source.getName();
						}

						final File target = new File(basefolder, targetName);
						downloadAndUnpackResource(source, target);
					}
				}
			}
			catch (final IOException | InterruptedException | ExecutionException e) {
				// cleanup
				if (basefolder.exists()) {
					basefolder.delete();
				}
				throw new IOException(e);
			}

			out = new FileLocation(basefolder);
			sources.put(localFolderName, out);
		}

		return out;
	}

	private void downloadAndUnpackResource(final Location source,
		final File targetFolder) throws InterruptedException, ExecutionException,
		IOException
	{
		// allocate array
		final ByteArray byteArray = new ByteArray(1024 * 1024);

		log.debug("Started download of " + source.getURI());
		// Download the zip file
		final BytesLocation bytes = new BytesLocation(byteArray);
		final Task task = //
			downloadService.download(source, bytes, sourceCache()).task();
		task.waitFor();

		// extract to cache dir
		final byte[] buf = new byte[64 * 1024];
		final ByteArrayInputStream bais = new ByteArrayInputStream(//
			byteArray.getArray(), 0, byteArray.size());
		targetFolder.mkdirs();
		log.debug("Unpacking files");
		try (final ZipInputStream zis = new ZipInputStream(bais)) {
			while (true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) break; // All done!
				final String name = entry.getName();
				final File outFile = new File(targetFolder, name);
				if (entry.isDirectory()) {
					outFile.mkdirs();
				}
				else {
					final int size = (int) entry.getSize();
					int len = 0;
					try (final FileOutputStream out = new FileOutputStream(outFile)) {
						while (true) {
							log.debug("Unpacking " + name + "; completion" + (double) len /
								size * 100 + "%");
							final int r = zis.read(buf);
							if (r < 0) break; // end of entry
							len += r;
							out.write(buf, 0, r);
						}
					}
				}
			}
		}
	}

	private DiskLocationCache sourceCache() {
		if (sourceCache == null) initSourceCache();
		return sourceCache;
	}

	private void initSourceCache() {
		final DiskLocationCache cache = new DiskLocationCache();

		// Follow XDG base directory specification:
		// https://specifications.freedesktop.org/basedir-spec/latest/
		//
		// By default, this resolves to:
		//
		// * %LOCALAPPDATA% on Windows
		// * $HOME/Library/Caches/scifio/ on macOS
		// * $HOME/.cache/scifio/ on other platforms
		final File cacheBase =
			new File(BaseDirectory.get(BaseDirectory.XDG_CACHE_HOME), "scifio");
		if (!cacheBase.exists()) cacheBase.mkdirs();
		cache.setBaseDirectory(cacheBase);

		sourceCache = cache;
	}

	@Override
	public void dispose() {
		sources.clear();
	}
}
