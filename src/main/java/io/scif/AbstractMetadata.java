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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Metadata} implementations.
 *
 * @see io.scif.Metadata
 * @see io.scif.Parser
 * @see io.scif.HasFormat
 * @author Mark Hiner
 */
public abstract class AbstractMetadata extends AbstractHasSource implements
	TypedMetadata
{

	// -- Fields --

	/* The image source associated with this Metadata. */
	private DataHandle<Location> source;

	/* The image source location associated with this Metadata. */
	private Location sourceLocation;

	/** The location an image with this metadata will be written to. */
	private Location destinationLocation;

	/* Whether the Metadata should be filtered or not. */
	private boolean filtered;

	/* Contains a list of metadata objects for each image in this dataset */
	@io.scif.Field(label = "imageMeta", isList = true)
	private List<ImageMetadata> imageMeta;

	/* A string id for this dataset. */
	private String datasetName = null;

	/* A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	public AbstractMetadata() {
		this((List<ImageMetadata>) null);
	}

	public AbstractMetadata(final Metadata copy) {
		this(copy.getAll());

		table = new DefaultMetaTable(copy.getTable());
	}

	public AbstractMetadata(final List<ImageMetadata> list) {
		imageMeta = new ArrayList<>();
		table = new DefaultMetaTable();

		if (list != null) {
			for (final ImageMetadata core : list) {
				imageMeta.add(core.copy());
			}
		}
	}

	// -- Metadata API Methods --

	@Override
	public void setSource(final DataHandle<Location> source) {
		this.source = source;

		if (source != null) setDatasetName(source.get().getName());
	}

	@Override
	public void setSourceLocation(Location loc) {
		sourceLocation = loc;
	}
	
	@Override
	public Location getSourceLocation() {
		return sourceLocation;
	}

	@Override
	public Location getDestinationLocation() {
		return destinationLocation;
	}

	@Override
	public void setDestinationLocation(Location loc) {
		this.destinationLocation = loc;
	}

	@Override
	public DataHandle<Location> getSource() {
		return source;
	}

	@Override
	public boolean isFiltered() {
		return filtered;
	}

	// -- Getters --

	@Override
	public String getDatasetName() {
		return datasetName;
	}

	@Override
	public ImageMetadata get(final int imageIndex) {
		return imageMeta.get(imageIndex);
	}

	@Override
	public List<ImageMetadata> getAll() {
		return imageMeta;
	}

	@Override
	public int getImageCount() {
		return imageMeta.size();
	}

	@Override
	public long getDatasetSize() {
		long size = 0;

		for (int i = 0; i < getAll().size(); i++)
			size += get(i).getSize();

		return size;
	}

	// -- Setters --

	@Override
	public void setDatasetName(final String name) {
		datasetName = name;
	}

	@Override
	public void setFiltered(final boolean filtered) {
		this.filtered = filtered;
	}

	@Override
	public void add(final ImageMetadata meta) {
		imageMeta.add(meta);
	}

	@Override
	public void createImageMetadata(final int imageCount) {
		imageMeta.clear();

		for (int i = 0; i < imageCount; i++)
			add(new DefaultImageMetadata());
	}

	// -- HasMetaTable API Methods --

	@Override
	public MetaTable getTable() {
		if (table == null) table = new DefaultMetaTable(isFiltered());
		return table;
	}

	@Override
	public void setTable(final MetaTable table) {
		this.table = table;
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		if (source != null) {
			source.close();
		}

		if (!fileOnly) reset(getClass());
	}

	// -- Helper Methods --

	private void reset(final Class<?> type) {
		if (type == null || type == AbstractMetadata.class) return;

		for (final Field f : type.getDeclaredFields()) {
			f.setAccessible(true);

			if (Modifier.isFinal(f.getModifiers())) continue;

			// only reset annotated fields
			if (f.getAnnotation(io.scif.Field.class) == null) continue;

			final Class<?> fieldType = f.getType();

			try {
				if (fieldType == boolean.class) f.set(this, false);
				else if (fieldType == char.class) f.set(this, 0);
				else if (fieldType == double.class) f.set(this, 0.0);
				else if (fieldType == float.class) f.set(this, 0f);
				else if (fieldType == int.class) f.set(this, 0);
				else if (fieldType == long.class) f.set(this, 0l);
				else if (fieldType == short.class) f.set(this, 0);
				else f.set(this, null);
			}
			catch (final IllegalArgumentException e) {
				log().debug(e.getMessage());
			}
			catch (final IllegalAccessException e) {
				log().debug(e.getMessage());
			}

			table = new DefaultMetaTable();
			imageMeta = new ArrayList<>();

			// check superclasses and interfaces
			reset(type.getSuperclass());
			for (final Class<?> c : type.getInterfaces()) {
				reset(c);
			}
		}
	}
}
