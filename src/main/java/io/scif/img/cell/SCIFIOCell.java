/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
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

package io.scif.img.cell;

import io.scif.img.cell.cache.CacheService;

import java.util.Arrays;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.Cell;

/**
 * {@link Cell} implementation. Stores the actual byte array for a given cell
 * position.
 *
 * @author Mark Hiner
 */
public class SCIFIOCell<A extends ArrayDataAccess<A>> extends Cell<A> {

	private static final long serialVersionUID = 660070520155729477L;

	// -- Transient Fields --
	// These fields are transient to speed up serialization/deserialization.
	// They should be available externally when the cell is deserialized.
	private transient CacheService<SCIFIOCell<?>> service; // hook used to
	// cache

	// during
	// finalization
	private transient String cacheId; // needed for this cell's hashcode

	private transient int index; // needed for this cell's hashcode

	private transient boolean[] enabled; // whether or not this cell should be

	// cached

	// -- Persistent fields --

	// These fields need to be objects to Phantom references to cache them by
	// reference
	private int[] hashes;

	private long[] elementSize;

	// -- Constructors --

	/**
	 * Standard constructor
	 */
	public SCIFIOCell(final CacheService<SCIFIOCell<?>> service,
		final String cacheId, final int index, final int[] dimensions,
		final long[] min, final A data)
	{
		super(dimensions, min, data);
		this.service = service;
		this.cacheId = cacheId;
		this.index = index;
		enabled = new boolean[] { true };
		elementSize = new long[] { -1 };
		hashes = new int[2];
		markClean();
	}

	/**
	 * Copying constructor to make another cell with the same metdata and data
	 * reference.
	 */
	public SCIFIOCell(final SCIFIOCell<A> toCopy) {
		super(toCopy.dimensions, toCopy.min, toCopy.getData());
		this.service = toCopy.service;
		this.cacheId = toCopy.cacheId;
		this.index = toCopy.index;
		this.enabled = toCopy.enabled;
		this.hashes[0] = toCopy.hashes[0];
		this.hashes[1] = toCopy.hashes[1];
		this.elementSize[0] = toCopy.elementSize[0];
	}

	/**
	 * {@link SCIFIOCellCleaningProvider} constructor.
	 */
	public SCIFIOCell(final A data, final int currentHash, final int cleanHash,
		final long elementSize, final int[] dimensions, final long[] min)
	{
		super(dimensions, min, data);
		hashes = new int[] { cleanHash, currentHash };
		this.elementSize = new long[] { elementSize };
		enabled = new boolean[1];
	}

	// -- SCIFIOCell Methods --

	/**
	 * Sets whether or not this cell tries to cache itself when no references to
	 * it remain.
	 */
	public void cacheOnFinalize(final boolean e) {
		if (enabled == null) enabled = new boolean[] { e };
		else enabled[0] = e;
	}

	/**
	 * @param service CacheService reference
	 */
	public void setService(final CacheService<SCIFIOCell<?>> service) {
		this.service = service;
	}

	/**
	 * @param cacheId Identifier for the cache storing this cell.
	 */
	public void setCacheId(final String cacheId) {
		this.cacheId = cacheId;
	}

	/**
	 * @param index Linear index of this cell in its cache
	 */
	public void setIndex(final int index) {
		this.index = index;
	}

	/**
	 * @return the cacheId for this cell
	 */
	public String getCacheId() {
		return cacheId;
	}

	/**
	 * @return index for this cell in its cache
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return [true] iff this cell should be cached to disk after it's out of
	 *         scope.
	 */
	public boolean[] isEnabled() {
		return enabled;
	}

	/**
	 * Sets the current state of this cell, as determined by the hashcode of its
	 * underlying data, as the "clean" state.
	 */
	public void markClean() {
		// Take a hash of the underlying data. If this is different
		// at finalization, we know this cell is dirty and should be
		// serialized.
		hashes[0] = computeHash(getData());

		// If data isn't an ArrayAccess object, this will cause it to always
		// look dirty compared to future computeHash calls.
		if (hashes[0] == -1) hashes[0] = 0;

		hashes[1] = hashes[0];
	}

	/**
	 * @return Size of the stored data object, in bytes, or -1 if size not known.
	 */
	public long getElementSize() {
		return elementSize[0];
	}

	/**
	 * @return a reference to the base array containing the clean (at index 0) and
	 *         current (index 1) data hashes.
	 */
	public int[] getHashes() {
		return hashes;
	}

	/**
	 * @return the original (clean) hash of the data
	 */
	public int getCleanHash() {
		return hashes[0];
	}

	/**
	 * @return the current known hash of the data.
	 */
	public int getCurrentHash() {
		return hashes[1];
	}

	/**
	 * @return the array containing the element size. Use this if the actual
	 *         object reference is needed.
	 */
	public long[] getESizeArray() {
		return elementSize;
	}

	/**
	 * @return dimensionality of this cell
	 */
	public int dimCount() {
		return n;
	}

	/**
	 * @return True if this cell has been modified, and {@link #update()} was then
	 *         called, since creation
	 */
	public boolean dirty() {
		return !(hashes[1] == hashes[0]);
	}

	/**
	 * Forces this cell to determine if it is dirty or not by computing the hash
	 * of its underlying data.
	 */
	public void update() {
		hashes[1] = computeHash(getData());
	}

	// -- Object method overrides --

	/**
	 * Two SCIFIOCells are equal iff they come from the same cache, with the same
	 * index, and have the same data state.
	 */
	@Override
	public boolean equals(final Object other) {
		if (this == other) return true;
		if (other instanceof SCIFIOCell<?>) {
			final SCIFIOCell<?> otherCell = (SCIFIOCell<?>) other;
			return cacheId.equals(otherCell.cacheId) && (index == otherCell.index) &&
				hashes[1] == otherCell.hashes[1];
		}
		return false;
	}

	@Override
	public int hashCode() {
		// Taken from Effective Java 2nd edition
		int result = 17;
		result = 31 * result + index;
		result = 31 * result + cacheId.hashCode();
		result = 31 * result + getData().hashCode();

		return result;
	}

	// -- Helper Methods --

	/**
	 * Computes a hash of the provided data object. Also computes the size of the
	 * data object
	 */
	private int computeHash(final ArrayDataAccess<?> data) {
		int hashCode = -1;
		if (data instanceof ByteArray) {
			final byte[] bytes = ((ByteArray) data).getCurrentStorageArray();
			computedataSize(8l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof CharArray) {
			final char[] bytes = ((CharArray) data).getCurrentStorageArray();
			computedataSize(8l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof DoubleArray) {
			final double[] bytes = ((DoubleArray) data).getCurrentStorageArray();
			computedataSize(64l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof IntArray) {
			final int[] bytes = ((IntArray) data).getCurrentStorageArray();
			computedataSize(32l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof FloatArray) {
			final float[] bytes = ((FloatArray) data).getCurrentStorageArray();
			computedataSize(32l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof ShortArray) {
			final short[] bytes = ((ShortArray) data).getCurrentStorageArray();
			computedataSize(16l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}
		else if (data instanceof LongArray) {
			final long[] bytes = ((LongArray) data).getCurrentStorageArray();
			computedataSize(64l * bytes.length);
			hashCode = Arrays.hashCode(bytes);
		}

		return hashCode;
	}

	/**
	 * @param bits update the element size of this cell
	 */
	private void computedataSize(final long bits) {
		elementSize[0] = bits / 8;
	}
}
