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

package io.scif.img;

import io.scif.AbstractSCIFIOComponent;
import io.scif.img.cell.cache.CacheService;
import io.scif.img.converters.PlaneConverterService;
import io.scif.services.InitializeService;
import io.scif.services.TranslatorService;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;


/**
 * Abstract superclass for ImgIOComponents (such as {@link ImgOpener}
 * or {@link ImgSaver}). Provides constructors ensuring proper
 * Services are available, and an accessor for the {@link ImgUtilityService}.
 * 
 * @author Mark Hiner hinerm@gmail.com
 *
 */
public abstract class AbstractImgIOComponent extends AbstractSCIFIOComponent {

	// -- Fields --
	
	@Parameter
	private ImgUtilityService imgUtils;

	// -- Constructors --

	public AbstractImgIOComponent() {
		this(new Context(StatusService.class, InitializeService.class, PlaneConverterService.class,
			TranslatorService.class, CacheService.class, ImgUtilityService.class));
	}

	public AbstractImgIOComponent(final Context context) {
		setContext(context);
	}
	
	// -- AbstractImgIOComponent methods --
	
	public ImgUtilityService utils() {
		return imgUtils;
	}
	
}
