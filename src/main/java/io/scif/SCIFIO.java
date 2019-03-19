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

package io.scif;

import io.scif.app.SCIFIOApp;
import io.scif.codec.CodecService;
import io.scif.formats.qt.QTJavaService;
import io.scif.formats.tiff.TiffService;
import io.scif.gui.GUIService;
import io.scif.img.ImgUtilityService;
import io.scif.img.converters.PlaneConverterService;
import io.scif.services.DatasetIOService;
import io.scif.services.FilePatternService;
import io.scif.services.FormatService;
import io.scif.services.InitializeService;
import io.scif.services.TranslatorService;
import io.scif.xml.XMLService;

import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.io.nio.NIOService;
import org.scijava.plugin.Plugin;
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
 * @see io.scif.services.FilePatternService
 * @author Mark Hiner
 */
@Plugin(type = Gateway.class)
public class SCIFIO extends AbstractGateway {

	// -- Constructors --

	/**
	 * Creates a new SCIFIO application context with all SCIFIO and SciJava
	 * services.
	 */
	public SCIFIO() {
		this(new Context(SciJavaService.class, SCIFIOService.class));
	}

	/**
	 * Creates a new SCIFIO application context which wraps the given existing
	 * SciJava context.
	 *
	 * @see Context
	 */
	public SCIFIO(final Context context) {
		super(SCIFIOApp.NAME, context);
	}

	// -- SCIFIO methods - services --

	/**
	 * Gets this application context's {@link CodecService}.
	 *
	 * @return The {@link CodecService} of this application context.
	 */
	public CodecService codec() {
		return get(CodecService.class);
	}

	/**
	 * Gets this application context's {@link DatasetIOService}.
	 *
	 * @return The {@link DatasetIOService} of this application context.
	 */
	public DatasetIOService datasetIO() {
		return get(DatasetIOService.class);
	}

	/**
	 * Gets this application context's {@link FilePatternService}.
	 *
	 * @return The {@link FilePatternService} of this application context.
	 */
	public FilePatternService filePattern() {
		return get(FilePatternService.class);
	}

	/**
	 * Gets this application context's {@link FormatService}.
	 *
	 * @return The {@link FormatService} of this application context.
	 */
	public FormatService format() {
		return get(FormatService.class);
	}

	/**
	 * Gets this application context's {@link GUIService}.
	 *
	 * @return The {@link GUIService} of this application context.
	 */
	public GUIService gui() {
		return get(GUIService.class);
	}

	/**
	 * Gets this application context's {@link ImgUtilityService}.
	 *
	 * @return The {@link ImgUtilityService} of this application context.
	 */
	public ImgUtilityService imgUtil() {
		return get(ImgUtilityService.class);
	}

	/**
	 * Gets this application context's {@link InitializeService}.
	 *
	 * @return The {@link InitializeService} of this application context.
	 */
	public InitializeService initializer() {
		return get(InitializeService.class);
	}

	/**
	 * Gets this application context's {@link MetadataService}.
	 *
	 * @return The {@link MetadataService} of this application context.
	 */
	public MetadataService metadata() {
		return get(MetadataService.class);
	}

	/**
	 * Gets this application context's {@link NIOService}.
	 *
	 * @return The {@link NIOService} of this application context.
	 */
	public NIOService nio() {
		return get(NIOService.class);
	}

	/**
	 * Gets this application context's {@link PlaneConverterService}.
	 *
	 * @return The {@link PlaneConverterService} of this application context.
	 */
	public PlaneConverterService planeConverter() {
		return get(PlaneConverterService.class);
	}

	/**
	 * Gets this application context's {@link QTJavaService}.
	 *
	 * @return The {@link QTJavaService} of this application context.
	 */
	public QTJavaService qtJava() {
		return get(QTJavaService.class);
	}

	/**
	 * Gets this application context's {@link TiffService}.
	 *
	 * @return The {@link TiffService} of this application context.
	 */
	public TiffService tiff() {
		return get(TiffService.class);
	}

	/**
	 * Gets this application context's {@link TranslatorService}.
	 *
	 * @return The {@link TranslatorService} of this application context.
	 */
	public TranslatorService translator() {
		return get(TranslatorService.class);
	}

	/**
	 * Gets this application context's {@link XMLService}.
	 *
	 * @return The {@link XMLService} of this application context.
	 */
	public XMLService xml() {
		return get(XMLService.class);
	}
}
