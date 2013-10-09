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

package io.scif.filters;

import io.scif.AbstractMetadata;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;
import io.scif.io.RandomAccessInputStream;

import java.io.IOException;

/**
 * Abstract superclass for concrete implementations of {@code MetadataWrapper}.
 * <p>
 * To create a {@code MetadataWrapper} that is paired with a specific
 * {@code Filter}, simply extend this class, override any methods as desired,
 * and annotate the class using {@code DiscoverableMetadataWrapper} as
 * appropriate. The wrapper will automatically be discovered and applied when a
 * new instance of its {@code Filter} is instantiated, assuming the
 * {@code Filter} is a subclass of {@code AbstractReaderFilter}
 * </p>
 * 
 * @author Mark Hiner
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.discovery.DiscoverableMetadataWrapper
 * @see io.scif.filters.AbstractReaderFilter
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
		setSource(meta.getSource());
		populateImageMetadata();
	}

	@Override
	public MetaTable getTable() {
		return super.getTable();
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
	public void setSource(final RandomAccessInputStream source) {
		super.setSource(source);
		meta.setSource(source);
	}

	@Override
	public void populateImageMetadata() {
		meta.populateImageMetadata();
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		meta.close(fileOnly);
	}

}
