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

package io.scif.filters;

import io.scif.AbstractMetadata;
import io.scif.Format;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;

import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * Abstract superclass for concrete implementations of {@code MetadataWrapper}.
 * <p>
 * To create a {@code MetadataWrapper} that is paired with a specific
 * {@code Filter}, simply extend this class, override any methods as desired,
 * and annotate the class using {@link Plugin} as appropriate. The wrapper will
 * automatically be discovered and applied when a new instance of its
 * {@link Filter} is instantiated, assuming the {@code Filter} is a subclass of
 * {@link AbstractReaderFilter}
 * </p>
 *
 * @author Mark Hiner
 * @see Plugin
 * @see MetadataWrapper
 * @see AbstractReaderFilter
 */
public abstract class AbstractMetadataWrapper extends AbstractMetadata
	implements MetadataWrapper
{

	// -- Fields --

	private Metadata meta;

	// -- Constructor --

	public AbstractMetadataWrapper() {
		this(null);
	}

	public AbstractMetadataWrapper(final Metadata metadata) {
		meta = metadata;
	}

	// -- MetadataWrapper API Methods --

	@Override
	public Metadata unwrap() {
		return meta;
	}

	@Override
	public void wrap(final Metadata meta) {
		this.meta = meta;
		setSourceLocation(meta.getSourceLocation());
		setSource(meta.getSource());
		populateImageMetadata();
	}

	@Override
	public MetaTable getTable() {
		return meta.getTable();
	}

	@Override
	public void setTable(final MetaTable table, final boolean passUp) {
		super.setTable(table);
		if (passUp) meta.setTable(table);
	}

	@Override
	public void add(final ImageMetadata meta, final boolean passUp) {
		super.add(meta);
		if (passUp) this.meta.add(meta);
	}

	@Override
	public void add(final ImageMetadata meta) {
		add(meta, true);
	}

	// -- Metadata API Methods --

	@Override
	public void setSource(final DataHandle<Location> source) {
		if (source != null) {
			meta.setSourceLocation(source.get());
		}
		super.setSource(source);
		meta.setSource(source);
	}

	@Override
	public void setSourceLocation(Location loc) {
		super.setSourceLocation(loc);
		meta.setSourceLocation(loc);
	}

	@Override
	public void populateImageMetadata() {
		meta.populateImageMetadata();
	}

	// -- HasFormat API Methods --

	@Override
	public Format getFormat() {
		return meta.getFormat();
	}

	@Override
	public String getFormatName() {
		return meta.getFormatName();
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		meta.close(fileOnly);
	}

}
