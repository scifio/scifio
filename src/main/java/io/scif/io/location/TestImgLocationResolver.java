/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2024 SCIFIO developers.
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

package io.scif.io.location;

import io.scif.MetadataService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.io.location.AbstractLocationResolver;
import org.scijava.io.location.LocationResolver;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Implementation of {@link LocationResolver} for {@link TestImgLocation}.
 * 
 * @author Gabriel Einsdorf
 */
@Plugin(type = LocationResolver.class, priority = Priority.HIGH)
public class TestImgLocationResolver extends AbstractLocationResolver {

	public TestImgLocationResolver() {
		super("scifioTestImg");
	}

	@Parameter
	private MetadataService meta;

	@Override
	public boolean supports(final URI uri) {
		final String scheme = uri.getScheme();
		return "scifioTestImg".equals(scheme) || //
			"file".equals(scheme) && uri.getPath().endsWith(".fake");
	}

	@Override
	public TestImgLocation resolve(final URI uri) throws URISyntaxException {
		final String data;
		switch (uri.getScheme()) {
			case "scifioTestImg":
				data = uri.getHost() + "&" + uri.getQuery();
				break;
			case "file":
				final String path = uri.getPath();
				data = path.substring(0, path.length() - 5); // strip .fake extension
				break;
			default:
				throw new UnsupportedOperationException("Invalid scheme: " + uri.getScheme());
		}
		final Map<String, Object> map = meta.parse(data);
		return TestImgLocation.fromMap(map);
	}
}
