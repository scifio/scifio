/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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

/**
 * Abstract superclass of all {@link PlaneMetadata} implementations.
 * 
 * @see PlaneMetadata
 * @see DefaultPlaneMetadata
 * @author Curtis Rueden
 */
public abstract class AbstractPlaneMetadata implements PlaneMetadata {

	// -- Fields --

	/** The name of the plane. */
	@Field(label = "name")
	private String name;

	/** A table of Field key, value pairs */
	private MetaTable table;

	// -- Constructors --

	public AbstractPlaneMetadata() {
	}

	public AbstractPlaneMetadata(final PlaneMetadata copy) {
		this();
		copy(copy);
	}

	// -- PlaneMetadata methods --

	@Override
	public void copy(final PlaneMetadata toCopy) {
		setName(toCopy.getName());
		this.table = new DefaultMetaTable(toCopy.getTable());
	}

	// -- Named API methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- HasTable API Methods --

	@Override
	public MetaTable getTable() {
		if (table == null) table = new DefaultMetaTable();
		return table;
	}

	@Override
	public void setTable(final MetaTable table) {
		this.table = table;
	}

	// -- Object API --

	@Override
	public String toString() {
		return new FieldPrinter(this).toString();
	}

}
