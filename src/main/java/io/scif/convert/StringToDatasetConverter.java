/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2020 SCIFIO developers.
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

package io.scif.convert;

import java.io.File;
import java.lang.reflect.Type;

import net.imagej.Dataset;

import org.scijava.Priority;
import org.scijava.convert.AbstractDelegateConverter;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link Converter} for loading {@link Dataset}s from {@link String}s.
 *
 * @author Jan Eglinger
 */
@Plugin(type = Converter.class, priority = Priority.LOW)
public class StringToDatasetConverter extends
	AbstractDelegateConverter<String, File, Dataset>
{
	@Parameter
	private ConvertService convertService;

	@Override
	public boolean canConvert(Object src, Class<?> dest) {
		if (!super.canConvert(src, dest)) return false;
		return srcFileSupported((String) src);
	}

	@Override
	public boolean canConvert(Object src, Type dest) {
		if (!super.canConvert(src, dest)) return false;
		return srcFileSupported((String) src);
	}

	private boolean srcFileSupported(String src) {
		File srcFile = convertService.convert(src, getDelegateType());
		return convertService.supports(srcFile, getOutputType());
	}

	@Override
	public Class<Dataset> getOutputType() {
		return Dataset.class;
	}

	@Override
	public Class<String> getInputType() {
		return String.class;
	}

	@Override
	protected Class<File> getDelegateType() {
		return File.class;
	}
}
