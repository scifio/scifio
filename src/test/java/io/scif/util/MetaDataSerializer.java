/**
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

package io.scif.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import io.scif.DefaultImageMetadata;
import io.scif.DefaultMetaTable;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;

import java.util.HashMap;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.display.ColorTable;

import org.scijava.Context;
import org.scijava.io.location.Location;
import org.scijava.service.Service;

/**
 * Serializes a Metadata object to JSON
 * 
 * @author Gabriel Einsdorf, KNIME GmbH
 */
public class MetaDataSerializer {

	public static String metaToJson(final Metadata m) {
		final GsonBuilder gb = new GsonBuilder().addSerializationExclusionStrategy(
			new ExclusionStrategy()
			{

				@Override
				public boolean shouldSkipField(final FieldAttributes f) {
					final Class<?> declaredClass = f.getDeclaredClass();
					if (Service.class.isAssignableFrom(declaredClass) || Location.class
						.isAssignableFrom(declaredClass) || ColorTable.class
							.isAssignableFrom(declaredClass))
			{
						return true;
					}
					return false;
				}

				@Override
				public boolean shouldSkipClass(final Class<?> clazz) {
					return Service.class.isAssignableFrom(clazz) ||
						clazz == Context.class;
				}
			});

		gb.registerTypeAdapter(MetaTable.class, (JsonSerializer<MetaTable>) (src,
			typeOfSrc, context) -> context.serialize(typeOfSrc, HashMap.class));

		gb.registerTypeAdapter(MetaTable.class, (JsonDeserializer<MetaTable>) (json,
			typeOfT, context) -> {
			final HashMap map = context.deserialize(json, HashMap.class);
			final DefaultMetaTable metatable = new DefaultMetaTable(map);
			return metatable;
		});

		gb.registerTypeAdapter(ImageMetadata.class,
			(JsonSerializer<ImageMetadata>) (src, typeOfSrc, context) -> context
				.serialize(src));

		gb.registerTypeAdapter(ImageMetadata.class,
			(JsonDeserializer<ImageMetadata>) (json, typeOfT, context) -> {
				return context.deserialize(json, DefaultImageMetadata.class);
			});

		gb.registerTypeAdapter(AxisType.class, (JsonSerializer<AxisType>) (src,
			typeOfSrc, context) -> context.serialize(src.getLabel()));

		gb.registerTypeAdapter(AxisType.class, (JsonDeserializer<AxisType>) (src,
			typeOfSrc, context) -> {
			final String label = src.getAsString();
			return Axes.get(label);
		});

		final Gson g = gb.create();
		final String meta = g.toJson(m);
		return meta;
	}
}
