/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.xml;

import io.scif.common.Constants;
import io.scif.io.RandomAccessInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A utility class for working with XML.
 * <dl>
 * <dt><b>Source code:</b></dt>
 * <dd><a href=
 * "http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/src/loci/common/xml/XMLTools.java"
 * >Trac</a>, <a href=
 * "http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/src/loci/common/xml/XMLTools.java;hb=HEAD"
 * >Gitweb</a></dd>
 * </dl>
 * 
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Chris Allan callan at blackcat.ca
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public final class XMLTools {

	// -- Constants --

	static final Logger LOGGER = LoggerFactory.getLogger(XMLTools.class);

	private static final String XML_SCHEMA_PATH =
		"http://www.w3.org/2001/XMLSchema";

	private static final SchemaFactory FACTORY = SchemaFactory
		.newInstance(XML_SCHEMA_PATH);

	// -- Fields --

	private static ThreadLocal<HashMap<URI, Schema>> schemas =
		new ThreadLocal<HashMap<URI, Schema>>() {

			@Override
			protected HashMap<URI, Schema> initialValue() {
				return new HashMap<URI, Schema>();
			}
		};

	// -- Constructor --

	private XMLTools() {}

	// -- XML to/from DOM --

	/** Parses a DOM from the given XML file on disk. */
	public static Document parseDOM(final File file)
		throws ParserConfigurationException, SAXException, IOException
	{
		final InputStream is = new FileInputStream(file);
		try {
			final Document doc = parseDOM(is);
			return doc;
		}
		finally {
			is.close();
		}
	}

	/** Parses a DOM from the given XML string. */
	public static Document parseDOM(final String xml)
		throws ParserConfigurationException, SAXException, IOException
	{
		final byte[] bytes = xml.getBytes(Constants.ENCODING);
		final InputStream is = new ByteArrayInputStream(bytes);
		try {
			final Document doc = parseDOM(is);
			return doc;
		}
		finally {
			is.close();
		}
	}

	/** Parses a DOM from the given XML input stream. */
	public static Document parseDOM(final InputStream is)
		throws ParserConfigurationException, SAXException, IOException
	{
		final InputStream in =
			is.markSupported() ? is : new BufferedInputStream(is);
		checkUTF8(in);

		// Java XML factories are not declared to be thread safe
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = factory.newDocumentBuilder();
		db.setErrorHandler(new ParserErrorHandler());
		return db.parse(in);
	}

	/** Converts the given DOM back to a string. */
	public static String getXML(final Document doc)
		throws TransformerConfigurationException, TransformerException
	{
		final Source source = new DOMSource(doc);
		final StringWriter stringWriter = new StringWriter();
		final Result result = new StreamResult(stringWriter);
		// Java XML factories are not declared to be thread safe
		final TransformerFactory factory = TransformerFactory.newInstance();
		factory.setErrorListener(new XMLListener());
		final Transformer transformer = factory.newTransformer();
		transformer.transform(source, result);
		return stringWriter.getBuffer().toString();
	}

	// -- Filtering --

	/** Remove invalid characters from an XML string. */
	public static String sanitizeXML(final String s) {
		final char[] c = s.toCharArray();
		for (int i = 0; i < s.length(); i++) {
			if ((Character.isISOControl(c[i]) && c[i] != '\n') ||
				!Character.isDefined(c[i]))
			{
				c[i] = ' ';
			}
			// eliminate invalid &# sequences
			if (i > 0 && c[i - 1] == '&' && c[i] == '#') c[i - 1] = ' ';
		}
		return new String(c);
	}

	/** Escape special characters. */
	public static String escapeXML(final String s) {
		final StringBuffer sb = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);

			if (c == '<') {
				sb.append("&lt;");
			}
			else if (c == '>') {
				sb.append("&gt;");
			}
			else if (c == '&') {
				sb.append("&amp;");
			}
			else if (c == '\"') {
				sb.append("&quot;");
			}
			else if (c == '\'') {
				sb.append("&apos;");
			}
			else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/** Indents XML to be more readable. */
	public static String indentXML(final String xml) {
		return indentXML(xml, 3, false);
	}

	/** Indents XML by the given spacing to be more readable. */
	public static String indentXML(final String xml, final int spacing) {
		return indentXML(xml, spacing, false);
	}

	/**
	 * Indents XML to be more readable, avoiding any whitespace injection into
	 * CDATA if the preserveCData flag is set.
	 */
	public static String indentXML(final String xml, final boolean preserveCData)
	{
		return indentXML(xml, 3, preserveCData);
	}

	/**
	 * Indents XML by the given spacing to be more readable, avoiding any
	 * whitespace injection into CDATA if the preserveCData flag is set.
	 */
	public static String indentXML(final String xml, final int spacing,
		final boolean preserveCData)
	{
		if (xml == null) return null; // garbage in, garbage out
		final StringBuffer sb = new StringBuffer();
		final StringTokenizer st = new StringTokenizer(xml, "<>", true);
		int indent = 0, noSpace = 0;
		boolean first = true, element = false;
		while (st.hasMoreTokens()) {
			final String token = st.nextToken().trim();
			if (token.equals("")) continue;
			if (token.equals("<")) {
				element = true;
				continue;
			}
			if (element && token.equals(">")) {
				element = false;
				continue;
			}

			if (!element && preserveCData) noSpace = 2;

			if (noSpace == 0) {
				// advance to next line
				if (first) first = false;
				else sb.append("\n");
			}

			// adjust indent backwards
			if (element && token.startsWith("/")) indent -= spacing;

			if (noSpace == 0) {
				// apply indent
				for (int j = 0; j < indent; j++)
					sb.append(" ");
			}

			// output element contents
			if (element) sb.append("<");
			sb.append(token);
			if (element) sb.append(">");

			if (noSpace == 0) {
				// adjust indent forwards
				if (element && !token.startsWith("?") && // ?xml tag, probably
					!token.startsWith("/") && // end element
					!token.endsWith("/") && // standalone element
					!token.startsWith("!")) // comment
				{
					indent += spacing;
				}
			}

			if (noSpace > 0) noSpace--;
		}
		sb.append("\n");
		return sb.toString();
	}

	// -- Parsing --

	/** Parses the given XML string into a list of key/value pairs. */
	public static Hashtable<String, String> parseXML(final String xml)
		throws IOException
	{
		final MetadataHandler handler = new MetadataHandler();
		parseXML(xml, handler);
		return handler.getMetadata();
	}

	/**
	 * Parses the given XML string using the specified XML handler.
	 */
	public static void parseXML(final String xml, final DefaultHandler handler)
		throws IOException
	{
		parseXML(xml.getBytes(Constants.ENCODING), handler);
	}

	/**
	 * Parses the XML contained in the given input stream into using the specified
	 * XML handler. Be very careful, as 'stream' <b>will</b> be closed by the SAX
	 * parser.
	 */
	public static void parseXML(final RandomAccessInputStream stream,
		final DefaultHandler handler) throws IOException
	{
		parseXML((InputStream) stream, handler);
	}

	/**
	 * Parses the XML contained in the given byte array into using the specified
	 * XML handler.
	 */
	public static void parseXML(final byte[] xml, final DefaultHandler handler)
		throws IOException
	{
		parseXML(new ByteArrayInputStream(xml), handler);
	}

	/**
	 * Parses the XML contained in the given InputStream using the specified XML
	 * handler.
	 */
	public static void parseXML(final InputStream xml,
		final DefaultHandler handler) throws IOException
	{
		try {
			// Java XML factories are not declared to be thread safe
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser parser = factory.newSAXParser();
			parser.parse(xml, handler);
		}
		catch (final ParserConfigurationException exc) {
			final IOException e = new IOException();
			e.initCause(exc);
			throw e;
		}
		catch (final SAXException exc) {
			final IOException e = new IOException();
			e.initCause(exc);
			throw e;
		}
	}

	// -- XSLT --

	/** Gets an XSLT template from the given resource location. */
	public static Templates getStylesheet(final String resourcePath,
		final Class<?> sourceClass)
	{
		InputStream xsltStream;
		if (sourceClass == null) {
			try {
				xsltStream = new FileInputStream(resourcePath);
			}
			catch (final IOException exc) {
				LOGGER.debug("Could not open file", exc);
				return null;
			}
		}
		else {
			xsltStream = sourceClass.getResourceAsStream(resourcePath);
		}

		try {
			final StreamSource xsltSource = new StreamSource(xsltStream);
			// Java XML factories are not declared to be thread safe
			final TransformerFactory transformerFactory =
				TransformerFactory.newInstance();
			transformerFactory.setErrorListener(new XMLListener());
			return transformerFactory.newTemplates(xsltSource);
		}
		catch (final TransformerConfigurationException exc) {
			LOGGER.debug("Could not construct template", exc);
		}
		finally {
			try {
				if (xsltStream != null) xsltStream.close();
			}
			catch (final IOException e) {
				LOGGER.debug("Could not close file", e);
			}
		}
		return null;
	}

	/** Replaces NS:tag with NS_tag for undeclared namespaces */
	public static String avoidUndeclaredNamespaces(String xml) {
		int gt = xml.indexOf('>');
		if (gt > 0 && xml.startsWith("<?xml ")) gt = xml.indexOf('>', gt + 1);
		if (gt > 0) {
			String firstTag = xml.substring(0, gt + 1).toLowerCase();

			// the first tag is a comment; we need to find the first "real" tag
			while (firstTag.endsWith("-->")) {
				gt = xml.indexOf('>', gt + 1);
				firstTag = xml.substring(0, gt + 1).toLowerCase();
			}

			final Set namespaces = new HashSet();
			Pattern pattern = Pattern.compile(" xmlns:(\\w+)");
			Matcher matcher = pattern.matcher(firstTag);
			while (matcher.find()) {
				namespaces.add(matcher.group(1));
			}

			pattern = Pattern.compile("</?(\\w+):");
			matcher = pattern.matcher(xml);
			while (matcher.find()) {
				final String namespace = matcher.group(1);
				if (!namespace.equalsIgnoreCase("OME") && !namespace.startsWith("ns") &&
					!namespaces.contains(namespace.toLowerCase()))
				{
					final int end = matcher.end();
					xml = xml.substring(0, end - 1) + "_" + xml.substring(end);
				}
			}

			final Pattern emptyNamespaces = Pattern.compile(" xmlns:(\\w+)=\"\"");
			matcher = emptyNamespaces.matcher(firstTag);
			while (matcher.find()) {
				final int start = matcher.start();
				final int end = matcher.end();
				xml = xml.substring(0, start + 1) + xml.substring(end);
			}
		}
		return xml;
	}

	/** Transforms the given XML string using the specified XSLT stylesheet. */
	public static String transformXML(String xml, final Templates xslt)
		throws IOException
	{
		xml = avoidUndeclaredNamespaces(xml);
		return transformXML(new StreamSource(new StringReader(xml)), xslt);
	}

	/** Transforms the given XML data using the specified XSLT stylesheet. */
	public static String
		transformXML(final Source xmlSource, final Templates xslt)
			throws IOException
	{
		Transformer trans;
		try {
			trans = xslt.newTransformer();
			trans.setErrorListener(new XMLListener());
		}
		catch (final TransformerConfigurationException exc) {
			final IOException e = new IOException();
			e.initCause(exc);
			throw e;
		}
		final StringWriter xmlWriter = new StringWriter();
		final StreamResult xmlResult = new StreamResult(xmlWriter);
		try {
			trans.transform(xmlSource, xmlResult);
		}
		catch (final TransformerException exc) {
			final IOException e = new IOException();
			e.initCause(exc);
			throw e;
		}
		return xmlWriter.toString();
	}

	// -- Validation --

	/**
	 * Attempts to validate the given XML string using Java's XML validation
	 * facility. Requires Java 1.5+.
	 * 
	 * @param xml The XML string to validate.
	 * @return whether or not validation was successful.
	 */
	public static boolean validateXML(final String xml) {
		return validateXML(xml, null);
	}

	/**
	 * Attempts to validate the given XML string using Java's XML validation
	 * facility. Requires Java 1.5+.
	 * 
	 * @param xml The XML string to validate.
	 * @param label String describing the type of XML being validated.
	 * @return whether or not validation was successful.
	 */
	public static boolean validateXML(final String xml, String label) {
		if (label == null) label = "XML";
		Exception exception = null;

		// get path to schema from root element using SAX
		LOGGER.info("Parsing schema path");
		final ValidationSAXHandler saxHandler = new ValidationSAXHandler();
		try {
			// Java XML factories are not declared to be thread safe
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser saxParser = factory.newSAXParser();
			final InputStream is =
				new ByteArrayInputStream(xml.getBytes(Constants.ENCODING));
			saxParser.parse(is, saxHandler);
		}
		catch (final ParserConfigurationException exc) {
			exception = exc;
		}
		catch (final SAXException exc) {
			exception = exc;
		}
		catch (final IOException exc) {
			exception = exc;
		}
		if (exception != null) {
			LOGGER.warn("Error parsing schema path from {}", label, exception);
			return false;
		}
		final String schemaPath = saxHandler.getSchemaPath();
		if (schemaPath == null) {
			LOGGER.error("No schema path found. Validation cannot continue.");
			return false;
		}
		LOGGER.info(schemaPath);

		LOGGER.info("Validating {}", label);

		// compile the schema
		URI schemaLocation = null;
		try {
			schemaLocation = new URI(schemaPath);
		}
		catch (final URISyntaxException exc) {
			LOGGER.info("Error accessing schema at {}", schemaPath, exc);
			return false;
		}
		Schema schema = schemas.get().get(schemaLocation);
		if (schema == null) {
			try {
				schema = FACTORY.newSchema(schemaLocation.toURL());
				schemas.get().put(schemaLocation, schema);
			}
			catch (final MalformedURLException exc) {
				LOGGER.info("Error parsing schema at {}", schemaPath, exc);
				return false;
			}
			catch (final SAXException exc) {
				LOGGER.info("Error parsing schema at {}", schemaPath, exc);
				return false;
			}
		}

		// get a validator from the schema
		final Validator validator = schema.newValidator();

		// prepare the XML source
		final StringReader reader = new StringReader(xml);
		final InputSource is = new InputSource(reader);
		final SAXSource source = new SAXSource(is);

		// validate the XML
		final ValidationErrorHandler errorHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errorHandler);
		try {
			validator.validate(source);
		}
		catch (final IOException exc) {
			exception = exc;
		}
		catch (final SAXException exc) {
			exception = exc;
		}
		final int errors = errorHandler.getErrorCount();
		if (errors > 0) {
			LOGGER.info("Error validating document: {} errors found", errors);
			return false;
		}
		else LOGGER.info("No validation errors found.");
		return errorHandler.ok();
	}

	// -- Helper methods --

	/**
	 * Checks the given stream for a UTF-8 BOM header, skipping it if present. If
	 * no UTF-8 BOM is present, the position of the stream is unchanged.
	 * <p>
	 * We must discard this character because <a href=
	 * "http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html"
	 * >Java does not handle it correctly</a>.
	 * </p>
	 */
	private static void checkUTF8(final InputStream is) throws IOException {
		// check first 3 bytes of the stream
		is.mark(3);
		if (is.read() != 0xef || is.read() != 0xbb || is.read() != 0xbf) {
			// NB: Data stream does not start with the UTF-8 BOM; reset it.
			is.reset();
		}
	}

	// -- Helper class --

	/** ErrorListener implementation that logs errors and warnings using SLF4J. */
	static class XMLListener implements ErrorListener {

		public void error(final TransformerException e) {
			LOGGER.debug("", e);
		}

		public void fatalError(final TransformerException e) {
			LOGGER.debug("", e);
		}

		public void warning(final TransformerException e) {
			LOGGER.debug("", e);
		}
	}

}
