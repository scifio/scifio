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

package io.scif.img.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link PlaneConverterService} implementation.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultPlaneConverterService extends
	AbstractSingletonService<PlaneConverter> implements PlaneConverterService
{

	// -- instance variables --

	private ConcurrentHashMap<String, PlaneConverter> converters;

	private List<String> converterNames;

	// -- PlaneConverterService methods --

	@Override
	public Map<String, PlaneConverter> getPlaneConverters() {
		return Collections.unmodifiableMap(converters());
	}

	@Override
	public List<String> getPlaneConverterNames() {
		return Collections.unmodifiableList(converterNames());
	}

	@Override
	public PlaneConverter getPlaneConverter(final String name) {
		return converters().get(name);
	}

	@Override
	public PlaneConverter getArrayConverter() {
		return getPlaneConverter("ArrayDataAccess");
	}

	@Override
	public PlaneConverter getPlanarConverter() {
		return getPlaneConverter("PlanarAccess");
	}

	@Override
	public PlaneConverter getDefaultConverter() {
		return getPlaneConverter("Default");
	}

	// -- PTService methods --

	@Override
	public Class<PlaneConverter> getPluginType() {
		return PlaneConverter.class;
	}

	// -- helpers --

	private Map<? extends String, ? extends PlaneConverter> converters() {
		if (converters == null) initConverters();
		return converters;
	}

	private List<? extends String> converterNames() {
		if (converterNames == null) initConverterNames();
		return converterNames;
	}

	private synchronized void initConverters() {
		if (converters != null) return; // already initialized

		final ConcurrentHashMap<String, PlaneConverter> map =
			new ConcurrentHashMap<>();
		for (final PlaneConverter converter : getInstances()) {
			final String name = converter.getInfo().getName();
			map.put(name, converter);
		}

		converters = map;
	}

	private synchronized void initConverterNames() {
		if (converterNames != null) return; // already initialized

		final ArrayList<String> list = new ArrayList<>();
		for (final PlaneConverter converter : getInstances()) {
			final String name = converter.getInfo().getName();
			list.add(name);
		}

		converterNames = list;
	}
}
