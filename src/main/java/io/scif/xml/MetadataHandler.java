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

package io.scif.xml;

import java.util.HashMap;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.xml.sax.Attributes;

/**
 * Used to retrieve key/value pairs from XML.
 *
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 */
class MetadataHandler extends BaseHandler {

	private String currentQName;

	private final HashMap<String, String> metadata = new HashMap<>();

	public MetadataHandler() {
		this(new StderrLogService());
	}

	public MetadataHandler(final LogService log) {
		super(log);
	}

	// -- MetadataHandler API methods --

	public Map<String, String> getMetadata() {
		return metadata;
	}

	// -- DefaultHandler API methods --

	@Override
	public void characters(final char[] data, final int start, final int len) {
		metadata.put(currentQName, new String(data, start, len));
	}

	@Override
	public void startElement(final String uri, final String localName,
		final String qName, final Attributes attributes)
	{
		if (attributes.getLength() == 0) currentQName += " - " + qName;
		else currentQName = qName;
		for (int i = 0; i < attributes.getLength(); i++) {
			metadata.put(qName + " - " + attributes.getQName(i), attributes.getValue(
				i));
		}
	}
}
