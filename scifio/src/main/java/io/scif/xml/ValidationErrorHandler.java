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

import io.scif.HasLog;

import org.scijava.log.LogService;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Used by validateXML to handle XML validation errors.
 * 
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 */
public class ValidationErrorHandler implements ErrorHandler, HasLog {

	private int errors = 0;

	private final LogService log;

	public ValidationErrorHandler(final LogService log) {
		this.log = log;
	}

	public boolean ok() {
		return errors == 0;
	}

	public int getErrorCount() {
		return errors;
	}

	// -- ValidationErrorHandler API methods --

	public void error(final SAXParseException e) {
		log().error(e.getMessage());
		errors++;
	}

	public void fatalError(final SAXParseException e) {
		log().error(e.getMessage());
		errors++;
	}

	public void warning(final SAXParseException e) {
		log().warn(e.getMessage());
		errors++;
	}

	// -- HasLog API methods --

	public LogService log() {
		return log;
	}

}
