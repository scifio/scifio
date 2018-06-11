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

package io.scif.io.location;

import io.scif.MetadataService;
import io.scif.formats.TestImgFormat;
import io.scif.util.FormatTools;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.scijava.io.location.AbstractLocation;
import org.scijava.io.location.Location;

/**
 * A {@link Location} for specifying virtual test images, to be used in
 * combination with {@link TestImgFormat}. Allows setting the Metadata which
 * will be read and interpreted by the {@link TestImgFormat}.
 * <p>
 * To create a {@link TestImgLocation} use the {@link Builder} as follows:
 *
 * <pre>
 * TestImgLocation.Builder b = new Builder();
 * b.axes("X", "Y", "Z"); // set new axis names
 * b.lengths(100, 100, 3); // set new axis lengths
 * Location loc = b.build(); // build the final location
 * </pre>
 *
 * @author Gabriel Einsdorf
 */
public class TestImgLocation extends AbstractLocation {

	public static Builder builder() {
		return new Builder();
	}

	public static TestImgLocation defaultTestImg() {
		return new Builder().build();
	}

	/**
	 * Creates a TestImgLocation with the values from the map set default values
	 *
	 * @param map the map containing the values
	 */
	public static TestImgLocation fromMap(final Map<String, Object> map) {
		final TestImgLocation loc = defaultTestImg();
		final Map<String, Object> metaData = loc.map;

		// replace defaults with values from map
		map.forEach((key, value) -> {
			final Object replaced = metaData.computeIfPresent(key, (k, v) -> value);
			if (replaced == null) {
				throw new IllegalArgumentException("unknown key:" + key);
			}
		});

		return loc;
	}

	public static class Builder {

		// default metadata values
		private String name = "testImage";
		private String[] axes = new String[] { "X", "Y" };
		private long[] lengths = new long[] { 512, 512 };
		private double[] scales = new double[] { 1.0, 1.0 };
		private String[] units = new String[] { "um", "um" };
		private int planarDims = -1;
		private int interleavedDims = -1;
		private int thumbSizeX = 0;
		private int thumbSizeY = 0;
		private String pixelType = FormatTools.getPixelTypeString(
			FormatTools.UINT8);
		private boolean indexed = false;
		private boolean falseColor = false;
		private boolean little = true;
		private boolean metadataComplete = true;
		private boolean thumbnail = false;
		private boolean orderCertain = true;
		private int lutLength = 3;
		private int scaleFactor = 1;
		private int images = 1;

		public TestImgLocation build() {
			return new TestImgLocation(this);
		}

		public Builder name(final String name) {
			this.name = name;
			return this;
		}

		public Builder axes(final String... axes) {
			this.axes = axes;
			return this;
		}

		public Builder lengths(final long... lengths) {
			this.lengths = lengths;
			return this;
		}

		public Builder scales(final double... scales) {
			this.scales = scales;
			return this;
		}

		public Builder units(final String... units) {
			this.units = units;
			return this;
		}

		public Builder planarDims(final int planarDims) {
			this.planarDims = planarDims;
			return this;
		}

		public Builder interleavedDims(final int interleavedDims) {
			this.interleavedDims = interleavedDims;
			return this;
		}

		public Builder thumbSizeX(final int thumbSizeX) {
			this.thumbSizeX = thumbSizeX;
			return this;
		}

		public Builder thumbSizeY(final int thumbSizeY) {
			this.thumbSizeY = thumbSizeY;
			return this;
		}

		public Builder pixelType(final String pixelType) {
			this.pixelType = pixelType;
			return this;
		}

		public Builder indexed(final boolean indexed) {
			this.indexed = indexed;
			return this;
		}

		public Builder falseColor(final boolean falseColor) {
			this.falseColor = falseColor;
			return this;
		}

		public Builder little(final boolean little) {
			this.little = little;
			return this;
		}

		public Builder metadataComplete(final boolean metadataComplete) {
			this.metadataComplete = metadataComplete;
			return this;
		}

		public Builder thumbnail(final boolean thumbnail) {
			this.thumbnail = thumbnail;
			return this;
		}

		public Builder orderCertain(final boolean orderCertain) {
			this.orderCertain = orderCertain;
			return this;
		}

		public Builder lutLength(final int lutLength) {
			this.lutLength = lutLength;
			return this;
		}

		public Builder scaleFactor(final int scaleFactor) {
			this.scaleFactor = scaleFactor;
			return this;
		}

		public Builder images(final int images) {
			this.images = images;
			return this;
		}
	}

	private final Map<String, Object> map;

	private static final String[] singleValueKeys = { "planarDims",
		"interleavedDims", "thumbSizeX", "thumbSizeY", "pixelType", "indexed",
		"falseColor", "little", "metadataComplete", "thumbnail", "orderCertain",
		"lutLength", "scaleFactor", "images" };

	private URI uri;

	private TestImgLocation(final Builder builder) {
		map = new LinkedHashMap<>();

		// Consistency checks
		if (builder.lengths.length != builder.axes.length) {
			throw new IllegalArgumentException(
				"Configuration  is not valid. Can not have a differing number of axis types: " +
					builder.axes.length + "; and axis lengths: " +
					builder.lengths.length + "!");
		}

		map.put(MetadataService.NAME_KEY, builder.name);
		map.put("axes", builder.axes);
		map.put("lengths", builder.lengths);
		map.put("scales", builder.scales);
		map.put("units", builder.units);
		map.put("planarDims", builder.planarDims);
		map.put("interleavedDims", builder.interleavedDims);
		map.put("thumbSizeX", builder.thumbSizeX);
		map.put("thumbSizeY", builder.thumbSizeY);
		map.put("pixelType", builder.pixelType);
		map.put("indexed", builder.indexed);
		map.put("falseColor", builder.falseColor);
		map.put("little", builder.little);
		map.put("metadataComplete", builder.metadataComplete);
		map.put("thumbnail", builder.thumbnail);
		map.put("orderCertain", builder.orderCertain);
		map.put("lutLength", builder.lutLength);
		map.put("scaleFactor", builder.scaleFactor);
		map.put("images", builder.images);
	}

	public Map<String, Object> getMetadataMap() {
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		final TestImgLocation other = (TestImgLocation) obj;
		if (map == null) {
			if (other.map != null) return false;
		}
		else if (!map.equals(other.map)) return false;
		return true;
	}

	@Override
	public String getName() {
		return (String) map.get(MetadataService.NAME_KEY) + ".scifioTestImg";
	}

	@Override
	public URI getURI() {
		if (this.uri == null) {
			this.uri = URI.create(getKeyValueStrings());
		}
		return uri;
	}

	@SuppressWarnings("unchecked")
	private String getKeyValueStrings() {
		final StringBuilder b = new StringBuilder();
		b.append("scifioTestImg://");
		b.append(map.get(MetadataService.NAME_KEY));
		final String regex = "\\[|\\]|\\s";

		b.append("?");

		final Stream<Entry<String, Object>> otherEntries = map.entrySet().stream()
			.filter(e -> !e.getKey().equals(MetadataService.NAME_KEY));
		otherEntries.forEach(e -> {
			b.append(e.getKey());
			b.append("=");
			final Object val = e.getValue();
			if (val instanceof String[]) {
				b.append(Arrays.toString((String[]) val).replaceAll(regex, ""));
			}
			else if (val instanceof int[]) {
				b.append(Arrays.toString((int[]) val).replaceAll(regex, ""));
			}
			else if (val instanceof long[]) {
				b.append(Arrays.toString((long[]) val).replaceAll(regex, ""));
			}
			else if (val instanceof double[]) {
				b.append(Arrays.toString((double[]) val).replaceAll(regex, ""));
			}
			else if (val instanceof List) {
				b.append(val.toString().replaceAll(regex, ""));
			}
			else {
				b.append(val.toString());
			}
			b.append("&");
		});

		// delete the last &
		b.replace(b.length() - 1, b.length(), "");
		return b.toString();
	}

	@Override
	public String toString() {
		return getURI().toString();
	}
}
