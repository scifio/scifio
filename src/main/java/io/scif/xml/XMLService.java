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

import io.scif.SCIFIOService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interface for services that work with XML.
 *
 * @author Curtis Rueden
 */
public interface XMLService extends SCIFIOService {

	/** Parses a DOM from the given XML file on disk. */
	Document parseDOM(File file) throws ParserConfigurationException,
		SAXException, IOException;

	/** Parses a DOM from the given XML string. */
	Document parseDOM(String xml) throws ParserConfigurationException,
		SAXException, IOException;

	/** Parses a DOM from the given XML input stream. */
	Document parseDOM(InputStream is) throws ParserConfigurationException,
		SAXException, IOException;

	/** Converts the given DOM back to a string. */
	String getXML(Document doc) throws TransformerConfigurationException,
		TransformerException;

	/** Remove invalid characters from an XML string. */
	String sanitizeXML(String s);

	/** Escape special characters. */
	String escapeXML(String s);

	/** Indents XML to be more readable. */
	String indentXML(String xml);

	/** Indents XML by the given spacing to be more readable. */
	String indentXML(String xml, int spacing);

	/**
	 * Indents XML to be more readable, avoiding any whitespace injection into
	 * CDATA if the preserveCData flag is set.
	 */
	String indentXML(String xml, boolean preserveCData);

	/**
	 * Indents XML by the given spacing to be more readable, avoiding any
	 * whitespace injection into CDATA if the preserveCData flag is set.
	 */
	String indentXML(String xml, int spacing, boolean preserveCData);

	/** Parses the given XML string into a list of key/value pairs. */
	Map<String, String> parseXML(String xml) throws IOException;

	/**
	 * Parses the given XML string using the specified XML handler.
	 */
	void parseXML(String xml, DefaultHandler handler) throws IOException;

	/**
	 * Parses the XML contained in the given data handle into using the specified
	 * XML handler. Be very careful, as 'stream' <b>will</b> be closed by the SAX
	 * parser.
	 */
	void parseXML(DataHandle<Location> handle, DefaultHandler handler)
		throws IOException;

	/**
	 * Parses the XML contained in the given byte array into using the specified
	 * XML handler.
	 */
	void parseXML(byte[] xml, DefaultHandler handler) throws IOException;

	/**
	 * Parses the XML contained in the given InputStream using the specified XML
	 * handler.
	 */
	void parseXML(InputStream xml, DefaultHandler handler) throws IOException;

	/** Gets an XSLT template from the given resource location. */
	Templates getStylesheet(String resourcePath, Class<?> sourceClass);

	/** Replaces NS:tag with NS_tag for undeclared namespaces */
	String avoidUndeclaredNamespaces(String xml);

	/** Transforms the given XML string using the specified XSLT stylesheet. */
	String transformXML(String xml, Templates xslt) throws IOException;

	/** Transforms the given XML data using the specified XSLT stylesheet. */
	String transformXML(Source xmlSource, Templates xslt) throws IOException;

	/**
	 * Attempts to validate the given XML string using Java's XML validation
	 * facility. Requires Java 1.5+.
	 *
	 * @param xml The XML string to validate.
	 * @return whether or not validation was successful.
	 */
	boolean validateXML(String xml);

	/**
	 * Attempts to validate the given XML string using Java's XML validation
	 * facility. Requires Java 1.5+.
	 *
	 * @param xml The XML string to validate.
	 * @param label String describing the type of XML being validated.
	 * @return whether or not validation was successful.
	 */
	boolean validateXML(String xml, String label);

}
