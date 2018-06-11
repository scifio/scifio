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

package io.scif.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;

/**
 * A simple parser for INI configuration files. Supports pound (#) as comments,
 * and backslash (\) to continue values across multiple lines.
 *
 * @author Curtis Rueden
 */
public class IniParser {

	private String commentDelimiter = "#";

	private boolean slashContinues = true;

	private final LogService log;

	// -- Constructors --

	public IniParser() {
		this(new StderrLogService());
	}

	public IniParser(final LogService log) {
		this.log = log;
	}

	// -- IniParser API methods --

	/**
	 * Set the String that identifies a comment. Defaults to "#".
	 */
	public void setCommentDelimiter(final String delimiter) {
		commentDelimiter = delimiter;
	}

	/**
	 * Set whether or not a '\' at the end of a line signifies that the line
	 * continues on the following line. By default, a '\' does continue the line.
	 */
	public void setBackslashContinuesLine(final boolean slashContinues) {
		this.slashContinues = slashContinues;
	}

	/** Parses the INI-style configuration data from the given resource. */
	public IniList parseINI(final String path) throws IOException {
		return parseINI(openTextResource(path));
	}

	/**
	 * Parses the INI-style configuration data from the given resource, using the
	 * given class to find the resource.
	 */
	public IniList parseINI(final String path, final Class<?> c)
		throws IOException
	{
		return parseINI(openTextResource(path, c));
	}

	/** Parses the INI-style configuration data from the given input stream. */
	public IniList parseINI(final BufferedReader in) throws IOException {
		final IniList list = new IniList();
		IniTable attrs = null;
		String chapter = null;
		int no = 1;
		final StringBuffer sb = new StringBuffer();
		while (true) {
			final int num = readLine(in, sb);
			if (num == 0) break; // eof
			final String line = sb.toString();
			log.debug("Line " + no + ": " + line);

			// ignore blank lines
			if (line.equals("")) {
				no += num;
				continue;
			}

			// check for chapter header
			if (line.startsWith("{")) {
				// strip curly braces
				int end = line.length();
				if (line.endsWith("}")) end--;
				chapter = line.substring(1, end);
				continue;
			}

			// check for section header
			if (line.startsWith("[")) {
				attrs = new IniTable();
				list.add(attrs);

				// strip brackets
				int end = line.length();
				if (line.endsWith("]")) end--;
				String header = line.substring(1, end);
				if (chapter != null) header = chapter + ": " + header;

				attrs.put(IniTable.HEADER_KEY, header);
				no += num;
				continue;
			}

			// parse key/value pair
			final int equals = line.indexOf("=");
			if (equals < 0 || attrs == null) throw new IOException(no + ": bad line");
			final String key = line.substring(0, equals).trim();
			final String value = line.substring(equals + 1).trim();
			attrs.put(key, value);
			no += num;
		}
		return list;
	}

	// -- Helper methods --

	/** Opens a buffered reader for the given resource. */
	private BufferedReader openTextResource(final String path) {
		return openTextResource(path, IniParser.class);
	}

	/** Opens a buffered reader for the given resource. */
	private BufferedReader openTextResource(final String path, final Class<?> c) {
		try {
			return new BufferedReader(new InputStreamReader(c.getResourceAsStream(
				path), Constants.ENCODING));
		}
		catch (final IOException e) {
			log.error("Could not open BufferedReader", e);
		}
		return null;
	}

	/**
	 * Reads (at least) one line from the given input stream into the specified
	 * string buffer.
	 *
	 * @return number of lines read
	 */
	private int readLine(final BufferedReader in, final StringBuffer sb)
		throws IOException
	{
		int no = 0;
		sb.setLength(0);
		boolean blockText = false;
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			no++;

			// strip comments
			if (commentDelimiter != null) {
				final int comment = line.indexOf(commentDelimiter);
				if (comment >= 0) line = line.substring(0, comment);
			}

			// kill whitespace
			if (!blockText) {
				line = line.trim();
			}

			// backslash signifies data continues to next line
			final boolean slash = slashContinues && line.trim().endsWith("\\");
			blockText = slashContinues && line.trim().endsWith("\\n");

			if (blockText) {
				line = line.substring(0, line.length() - 2) + "\n";
			}
			else if (slash) {
				line = line.substring(0, line.length() - 1).trim() + " ";
			}
			sb.append(line);
			if (!slash && !blockText) break;
		}
		return no;
	}

}
