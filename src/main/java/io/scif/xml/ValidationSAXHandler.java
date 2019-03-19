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

import java.util.StringTokenizer;

import org.scijava.log.LogService;
import org.xml.sax.Attributes;

/**
 * Used by validateXML to parse the XML block's schema path using SAX.
 *
 *
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 */
/**  */
public class ValidationSAXHandler extends BaseHandler {

	private String schemaPath;

	private boolean first;

	public ValidationSAXHandler(final LogService log) {
		super(log);
	}

	public String getSchemaPath() {
		return schemaPath;
	}

	@Override
	public void startDocument() {
		schemaPath = null;
		first = true;
	}

	@Override
	public void startElement(final String uri, final String localName,
		final String qName, final Attributes attributes)
	{
		if (!first) return;
		first = false;

		final int len = attributes.getLength();
		String xmlns = null, xsiSchemaLocation = null;
		for (int i = 0; i < len; i++) {
			final String name = attributes.getQName(i);
			if (name.equals("xmlns")) xmlns = attributes.getValue(i);
			else if (name.equals("schemaLocation") || name.endsWith(
				":schemaLocation"))
			{
				xsiSchemaLocation = attributes.getValue(i);
			}
		}
		if (xmlns == null || xsiSchemaLocation == null) return; // not found

		final StringTokenizer st = new StringTokenizer(xsiSchemaLocation);
		while (st.hasMoreTokens()) {
			final String token = st.nextToken();
			if (xmlns.equals(token)) {
				// next token is the actual schema path
				if (st.hasMoreTokens()) schemaPath = st.nextToken();
				break;
			}
		}
	}

}
