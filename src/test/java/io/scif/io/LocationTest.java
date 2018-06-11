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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Unit tests for the loci.common.Location class.
 *
 * @see Location
 */
public class LocationTest {

	// -- Constants --
	private final String OS = System.getProperty("os.name").toLowerCase();

	private final boolean isWindows = OS.contains("win");

	// -- Fields --

	private Location[] files;

	private boolean[] exists;

	private boolean[] isDirectory;

	private boolean[] isHidden;

	private String[] mode;

	private Context context;

	// -- Setup methods --

	@Before
	public void setup() throws IOException, InterruptedException {
		context = new Context();
		final File tmpDirectory = new File(System.getProperty("java.io.tmpdir"),
			System.currentTimeMillis() + "-location-test");
		tmpDirectory.mkdirs();
		tmpDirectory.deleteOnExit();

		final File hiddenFile = File.createTempFile(".hiddenTest", null,
			tmpDirectory);
		hiddenFile.deleteOnExit();

		if (isWindows) {
			final Process p = Runtime.getRuntime().exec("attrib +h " + hiddenFile
				.getAbsolutePath());
			p.waitFor();
		}

		final File invalidFile = File.createTempFile("invalidTest", null,
			tmpDirectory);
		final String invalidPath = invalidFile.getAbsolutePath();
		invalidFile.delete();

		final File validFile = File.createTempFile("validTest", null, tmpDirectory);
		validFile.deleteOnExit();

		files = new Location[] { new Location(context, validFile.getAbsolutePath()),
			new Location(context, invalidPath), new Location(context, tmpDirectory),
			new Location(context, "http://loci.wisc.edu/software/scifio"),
			new Location(context, "http://www.openmicroscopy.org/software/scifio"),
			new Location(context, hiddenFile) };

		exists = new boolean[] { true, false, true, true, false, true };

		isDirectory = new boolean[] { false, false, true, false, false, false };

		isHidden = new boolean[] { false, false, false, false, false, true };

		mode = new String[] { "rw", "", "rw", "r", "", "rw" };

	}

	// -- Tests --

	@Test
	public void testReadWriteMode() {
		for (int i = 0; i < files.length; i++) {
			final String msg = files[i].getName();
			assertEquals(msg, files[i].canRead(), mode[i].contains("r"));
			assertEquals(msg, files[i].canWrite(), mode[i].contains("w"));
		}
	}

	@Test
	public void testAbsolute() {
		for (final Location file : files) {
			assertEquals(file.getName(), file.getAbsolutePath(), file
				.getAbsoluteFile().getAbsolutePath());
		}
	}

	@Test
	public void testExists() {
		for (int i = 0; i < files.length; i++) {
			assertEquals(files[i].getName(), files[i].exists(), exists[i]);
		}
	}

	@Test
	public void testCanonical() throws IOException {
		for (final Location file : files) {
			assertEquals(file.getName(), file.getCanonicalPath(), file
				.getCanonicalFile().getAbsolutePath());
		}
	}

	@Test
	public void testParent() {
		for (final Location file : files) {
			assertEquals(file.getName(), file.getParent(), file.getParentFile()
				.getAbsolutePath());
		}
	}

	@Test
	public void testIsDirectory() {
		for (int i = 0; i < files.length; i++) {
			assertEquals(files[i].getName(), files[i].isDirectory(), isDirectory[i]);
		}
	}

	@Test
	public void testIsFile() {
		for (int i = 0; i < files.length; i++) {
			assertEquals(files[i].getName(), files[i].isFile(), !isDirectory[i] &&
				exists[i]);
		}
	}

	@Test
	public void testIsHidden() {
		for (int i = 0; i < files.length; i++) {
			assertEquals(files[i].getName(), files[i].isHidden(), isHidden[i]);
		}
	}

	@Test
	public void testListFiles() {
		for (final Location file : files) {
			final String[] completeList = file.list();
			final String[] unhiddenList = file.list(true);
			final Location[] fileList = file.listFiles();

			if (!file.isDirectory()) {
				assertEquals(file.getName(), completeList, null);
				assertEquals(file.getName(), unhiddenList, null);
				assertEquals(file.getName(), fileList, null);
				continue;
			}

			assertEquals(file.getName(), completeList.length, fileList.length);

			final List<String> complete = Arrays.asList(completeList);
			for (final String child : unhiddenList) {
				assertEquals(file.getName(), complete.contains(child), true);
				assertEquals(file.getName(), new Location(context, file, child)
					.isHidden(), false);
			}

			for (int f = 0; f < fileList.length; f++) {
				assertEquals(file.getName(), fileList[f].getName(), completeList[f]);
			}
		}
	}

	@Test
	public void testToURL() throws IOException {
		for (final Location file : files) {
			String path = file.getAbsolutePath();
			if (file.isDirectory() && !path.endsWith(File.separator)) {
				path += File.separator;
			}
			if (!path.contains("://") && !path.contains(":/")) {
				path = new File(path).toURI().toURL().toString();
			}
			final URL baseURL = file.toURL();
			final URL compareURL = new URL(path);
			assertEquals(file.getName(), baseURL, compareURL);
		}
	}

	@Test
	public void testToString() {
		for (final Location file : files) {
			assertEquals(file.getName(), file.toString(), file.getAbsolutePath());
		}
	}
}
