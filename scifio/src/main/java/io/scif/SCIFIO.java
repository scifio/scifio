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

package io.scif;

import io.scif.app.SCIFIOApp;
import io.scif.codec.CodecService;
import io.scif.formats.qt.QTJavaService;
import io.scif.formats.tiff.TiffService;
import io.scif.services.FilePatternService;
import io.scif.services.FormatService;
import io.scif.services.InitializeService;
import io.scif.services.LocationService;
import io.scif.services.TranslatorService;
import io.scif.xml.XMLService;

import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.service.SciJavaService;

/**
 * Convenience {@link org.scijava.Context} wrapper.
 * <p>
 * Provides easy accessor methods for the SCIFIO-specific
 * {@link org.scijava.service.Service} implementations.
 * </p>
 * <p>
 * This class is intended to be a disposable wrapper that can easily be created
 * and forgotten as needed.
 * </p>
 * 
 * @see org.scijava.Context
 * @see io.scif.services.InitializeService
 * @see io.scif.services.FormatService
 * @see io.scif.services.TranslatorService
 * @see io.scif.services.LocationService
 * @see io.scif.services.FilePatternService
 * @author Mark Hiner
 */
public class SCIFIO extends AbstractGateway {

	// -- Constructors --

	/** Creates a new SCIFIO application context with all available services. */
	public SCIFIO() {
		this(new Context(SciJavaService.class, SCIFIOService.class));
	}

	/**
	 * Creates a new SCIFIO application with zero or all services..
	 * 
	 * @param empty If true, the context will be empty; otherwise, it will be
	 *          initialized with all available services.
	 */
	public SCIFIO(final boolean empty) {
		this(new Context(empty));
	}

	/**
	 * Creates a new SCIFIO wrapping the provided context.
	 * 
	 * @param context - The context to wrap
	 */
	public SCIFIO(final Context context) {
		super(SCIFIOApp.NAME, context);
	}

	// -- Service Accessors --

	/**
	 * InitializeService accessor.
	 * 
	 * @return The InitializeService instance associated with the wrapped Context.
	 */
	public InitializeService initializer() {
		return get(InitializeService.class);
	}

	/**
	 * FormatService accessor.
	 * 
	 * @return The FormatService instance associated with the wrapped Context.
	 */
	public FormatService format() {
		return get(FormatService.class);
	}

	/**
	 * MetadataService accessor.
	 * 
	 * @return The MetadataService instance associated with the wrapped Context.
	 */
	public MetadataService metadata() {
		return get(MetadataService.class);
	}

	/**
	 * TranslatorService accessor.
	 * 
	 * @return The TranslatorService instance associated with the wrapped Context.
	 */
	public TranslatorService translator() {
		return get(TranslatorService.class);
	}

	/**
	 * LocationService accessor.
	 * 
	 * @return The LocationService instance associated with the wrapped Context.
	 */
	public LocationService location() {
		return get(LocationService.class);
	}

	/**
	 * FilePatternService accessor.
	 * 
	 * @return The FilePatternService instance associated with the wrapped
	 *         Context.
	 */
	public FilePatternService filePattern() {
		return get(FilePatternService.class);
	}

	/**
	 * CodecService accessor.
	 * 
	 * @return The CodecService instance associated with the wrapped Context.
	 */
	public CodecService codec() {
		return get(CodecService.class);
	}

	/**
	 * TiffService accessor.
	 * 
	 * @return The TiffService instance associated with the wrapped Context.
	 */
	public TiffService tiff() {
		return get(TiffService.class);
	}

	/**
	 * QTJavaService accessor.
	 * 
	 * @return The QTJavaService instance associated with the wrapped Context.
	 */
	public QTJavaService qtJava() {
		return get(QTJavaService.class);
	}

	/**
	 * XMLService accessor.
	 * 
	 * @return The XMLService instance associated with the wrapped Context.
	 */
	public XMLService xml() {
		return get(XMLService.class);
	}

}
