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

import java.util.Map;

/**
 * Interface for services that work with SCIFIO metadata.
 *
 * @author Curtis Rueden
 */
public interface MetadataService extends SCIFIOService {

	String NAME_KEY = "name";

	/**
	 * Parses key/value pairs from the given string, using the default separator
	 * regex of {@code "[&\n]"} (i.e., ampersand or newline). Each key/value pair
	 * is assumed to be separated with an equals sign character ('='). The value
	 * can also be a list of values if each value is separated by a comma
	 * character (',').
	 */
	Map<String, Object> parse(String data);

	/**
	 * Parses key/value pairs from the given string, using the specified separator
	 * regex. Each key/value pair is assumed to be separated with an equals sign
	 * character ('='). The value can also be a list of values if each value is
	 * separated by a comma character (',').
	 *
	 * @see String#split(String)
	 */
	Map<String, Object> parse(String data, String regex);

	/**
	 * Populates fields of the given metadata object with information from the
	 * given map. The metadata object can be any Java type (though it will
	 * typically be a SCIFIO {@link Metadata} object), but its instance fields
	 * must be marked with the @{@link Field} annotation.
	 *
	 * @param metadata The metadata object with annotated fields to populate.
	 * @param map The map defining the values to use when populating the metadata.
	 */
	void populate(Object metadata, Map<String, Object> map);

	// TODO: MetaTable dump(Object metadata);

}
