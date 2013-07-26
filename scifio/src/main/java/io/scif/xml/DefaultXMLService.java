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

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default service for working with XML.
 *
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 */
@Plugin(type = Service.class)
public class DefaultXMLService extends AbstractService implements XMLService {

  // -- Constants --

  private static final String XML_SCHEMA_PATH =
    "http://www.w3.org/2001/XMLSchema";

  private static final SchemaFactory FACTORY =
    SchemaFactory.newInstance(XML_SCHEMA_PATH);

  // -- Fields --

  @Parameter
  private LogService log;

  private ThreadLocal<HashMap<URI, Schema>> schemas =
    new ThreadLocal<HashMap<URI, Schema>>()
  {
    @Override
    protected HashMap<URI, Schema> initialValue() {
      return new HashMap<URI, Schema>();
    }
  };

  // -- XML to/from DOM --

  public Document parseDOM(File file)
    throws ParserConfigurationException, SAXException, IOException
  {
    InputStream is = new FileInputStream(file);
    try {
      Document doc = parseDOM(is);
      return doc;
    } finally {
      is.close();
    }
  }

  public Document parseDOM(String xml)
    throws ParserConfigurationException, SAXException, IOException
  {
    byte[] bytes = xml.getBytes(Constants.ENCODING);
    InputStream is = new ByteArrayInputStream(bytes);
    try {
      Document doc = parseDOM(is);
      return doc;
    } finally {
      is.close();
    }
  }

  public Document parseDOM(InputStream is)
    throws ParserConfigurationException, SAXException, IOException
  {
    final InputStream in =
      is.markSupported() ? is : new BufferedInputStream(is);
    checkUTF8(in);

    // Java XML factories are not declared to be thread safe
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = factory.newDocumentBuilder();
    db.setErrorHandler(new ParserErrorHandler());
    return db.parse(in);
  }

  public String getXML(Document doc)
    throws TransformerConfigurationException, TransformerException
  {
    Source source = new DOMSource(doc);
    StringWriter stringWriter = new StringWriter();
    Result result = new StreamResult(stringWriter);
    // Java XML factories are not declared to be thread safe
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(new XMLListener());
    Transformer transformer = factory.newTransformer();
    transformer.transform(source, result);
    return stringWriter.getBuffer().toString();
  }

  // -- Filtering --

  public String sanitizeXML(String s) {
    final char[] c = s.toCharArray();
    for (int i=0; i<s.length(); i++) {
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

  public String escapeXML(String s) {
    StringBuffer sb = new StringBuffer();

    for (int i=0; i<s.length(); i++) {
      char c = s.charAt(i);

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

  public String indentXML(String xml) {
    return indentXML(xml, 3, false);
  }

  public String indentXML(String xml, int spacing) {
    return indentXML(xml, spacing, false);
  }

  public String indentXML(String xml, boolean preserveCData) {
    return indentXML(xml, 3, preserveCData);
  }

  public String indentXML(String xml, int spacing,
    boolean preserveCData)
  {
    if (xml == null) return null; // garbage in, garbage out
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = new StringTokenizer(xml, "<>", true);
    int indent = 0, noSpace = 0;
    boolean first = true, element = false;
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
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
        for (int j=0; j<indent; j++) sb.append(" ");
      }

      // output element contents
      if (element) sb.append("<");
      sb.append(token);
      if (element) sb.append(">");

      if (noSpace == 0) {
        // adjust indent forwards
        if (element &&
          !token.startsWith("?") && // ?xml tag, probably
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

  public Hashtable<String, String> parseXML(String xml)
    throws IOException
  {
    MetadataHandler handler = new MetadataHandler();
    parseXML(xml, handler);
    return handler.getMetadata();
  }

  public void parseXML(String xml, DefaultHandler handler)
    throws IOException
  {
    parseXML(xml.getBytes(Constants.ENCODING), handler);
  }

  public void parseXML(RandomAccessInputStream stream,
    DefaultHandler handler) throws IOException
  {
    parseXML((InputStream) stream, handler);
  }

  public void parseXML(byte[] xml, DefaultHandler handler)
    throws IOException
  {
    parseXML(new ByteArrayInputStream(xml), handler);
  }

  public void parseXML(InputStream xml, DefaultHandler handler)
    throws IOException
  {
    try {
      // Java XML factories are not declared to be thread safe
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(xml, handler);
    }
    catch (ParserConfigurationException exc) {
      IOException e = new IOException();
      e.initCause(exc);
      throw e;
    }
    catch (SAXException exc) {
      IOException e = new IOException();
      e.initCause(exc);
      throw e;
    }
  }

  // -- XSLT --

  public Templates getStylesheet(String resourcePath,
    Class<?> sourceClass)
  {
    InputStream xsltStream;
    if (sourceClass == null) {
      try {
        xsltStream = new FileInputStream(resourcePath);
      }
      catch (IOException exc) {
        log.debug("Could not open file", exc);
        return null;
      }
    }
    else {
      xsltStream = sourceClass.getResourceAsStream(resourcePath);
    }

    try {
      StreamSource xsltSource = new StreamSource(xsltStream);
      // Java XML factories are not declared to be thread safe
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setErrorListener(new XMLListener());
      return transformerFactory.newTemplates(xsltSource);
    }
    catch (TransformerConfigurationException exc) {
      log.debug("Could not construct template", exc);
    }
    finally {
      try {
        if (xsltStream != null) xsltStream.close();
      }
      catch (IOException e) {
        log.debug("Could not close file", e);
      }
    }
    return null;
  }

  public String avoidUndeclaredNamespaces(String xml) {
    int gt = xml.indexOf('>');
    if (gt > 0 && xml.startsWith("<?xml "))
      gt = xml.indexOf('>', gt + 1);
    if (gt > 0) {
      String firstTag = xml.substring(0, gt + 1).toLowerCase();

      // the first tag is a comment; we need to find the first "real" tag
      while (firstTag.endsWith("-->")) {
        gt = xml.indexOf('>', gt + 1);
        firstTag = xml.substring(0, gt + 1).toLowerCase();
      }

      Set namespaces = new HashSet();
      Pattern pattern = Pattern.compile(" xmlns:(\\w+)");
      Matcher matcher = pattern.matcher(firstTag);
      while (matcher.find()) {
        namespaces.add(matcher.group(1));
      }

      pattern = Pattern.compile("</?(\\w+):");
      matcher = pattern.matcher(xml);
      while (matcher.find()) {
        String namespace = matcher.group(1);
        if (!namespace.equalsIgnoreCase("OME") && !namespace.startsWith("ns") &&
          !namespaces.contains(namespace.toLowerCase()))
        {
          int end = matcher.end();
          xml = xml.substring(0, end - 1) + "_" + xml.substring(end);
        }
      }

      Pattern emptyNamespaces = Pattern.compile(" xmlns:(\\w+)=\"\"");
      matcher = emptyNamespaces.matcher(firstTag);
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        xml = xml.substring(0, start + 1) + xml.substring(end);
      }
    }
    return xml;
  }

  public String transformXML(String xml, Templates xslt)
    throws IOException
  {
    xml = avoidUndeclaredNamespaces(xml);
    return transformXML(new StreamSource(new StringReader(xml)), xslt);
  }

  public String transformXML(Source xmlSource, Templates xslt)
    throws IOException
  {
    Transformer trans;
    try {
      trans = xslt.newTransformer();
      trans.setErrorListener(new XMLListener());
    }
    catch (TransformerConfigurationException exc) {
      IOException e = new IOException();
      e.initCause(exc);
      throw e;
    }
    StringWriter xmlWriter = new StringWriter();
    StreamResult xmlResult = new StreamResult(xmlWriter);
    try {
      trans.transform(xmlSource, xmlResult);
    }
    catch (TransformerException exc) {
      IOException e = new IOException();
      e.initCause(exc);
      throw e;
    }
    return xmlWriter.toString();
  }

  // -- Validation --

  public boolean validateXML(String xml) {
    return validateXML(xml, null);
  }

  public boolean validateXML(String xml, String label) {
    if (label == null) label = "XML";
    Exception exception = null;

    // get path to schema from root element using SAX
    log.info("Parsing schema path");
    ValidationSAXHandler saxHandler = new ValidationSAXHandler(log);
    try {
      // Java XML factories are not declared to be thread safe
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      InputStream is =
        new ByteArrayInputStream(xml.getBytes(Constants.ENCODING));
      saxParser.parse(is, saxHandler);
    }
    catch (ParserConfigurationException exc) { exception = exc; }
    catch (SAXException exc) { exception = exc; }
    catch (IOException exc) { exception = exc; }
    if (exception != null) {
      log.warn("Error parsing schema path from " + label, exception);
      return false;
    }
    String schemaPath = saxHandler.getSchemaPath();
    if (schemaPath == null) {
      log.error("No schema path found. Validation cannot continue.");
      return false;
    }
    log.info(schemaPath);

    log.info("Validating " + label);

    // compile the schema
    URI schemaLocation = null;
    try {
      schemaLocation = new URI(schemaPath);
    }
    catch (URISyntaxException exc) {
      log.info("Error accessing schema at " + schemaPath, exc);
      return false;
    }
    Schema schema = schemas.get().get(schemaLocation);
    if (schema == null) {
      try {
        schema = FACTORY.newSchema(schemaLocation.toURL());
        schemas.get().put(schemaLocation, schema);
      }
      catch (MalformedURLException exc) {
        log.info("Error parsing schema at " + schemaPath, exc);
        return false;
      }
      catch (SAXException exc) {
        log.info("Error parsing schema at " + schemaPath, exc);
        return false;
      }
    }

    // get a validator from the schema
    Validator validator = schema.newValidator();

    // prepare the XML source
    StringReader reader = new StringReader(xml);
    InputSource is = new InputSource(reader);
    SAXSource source = new SAXSource(is);

    // validate the XML
    ValidationErrorHandler errorHandler = new ValidationErrorHandler();
    validator.setErrorHandler(errorHandler);
    try {
      validator.validate(source);
    }
    catch (IOException exc) { exception = exc; }
    catch (SAXException exc) { exception = exc; }
    final int errors = errorHandler.getErrorCount();
    if (errors > 0) {
      log.info("Error validating document: " + errors + " errors found");
      return false;
    }
    log.info("No validation errors found.");
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
  private void checkUTF8(InputStream is) throws IOException {
    // check first 3 bytes of the stream
    is.mark(3);
    if (is.read() != 0xef || is.read() != 0xbb || is.read() != 0xbf) {
      // NB: Data stream does not start with the UTF-8 BOM; reset it.
      is.reset();
    }
  }

  // -- Helper class --

  /** ErrorListener implementation that logs errors and warnings using SLF4J. */
  private class XMLListener implements ErrorListener {

    public void error(TransformerException e) {
      log.debug("", e);
    }

    public void fatalError(TransformerException e) {
      log.debug("", e);
    }

    public void warning(TransformerException e) {
      log.debug("", e);
    }

  }

}
