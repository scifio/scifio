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

package io.scif.img.cell;

import io.scif.img.cell.cache.CacheService;

import java.io.Serializable;
import java.util.Arrays;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.AbstractCell;

/**
 * {@link AbstractCell} implenetation. Stores the actual byte array for
 * a given cell position.
 * 
 * @author Mark Hiner hinerm at gmail.com
 */
public class SCIFIOCell<A extends ArrayDataAccess<?>> extends AbstractCell<A> implements Serializable {
  private static final long serialVersionUID = 660070520155729477L;

  // -- Transient Fields --
  // These fields are transient to speed up serialization/deserialization.
  // They should be available externally when the cell is deserialized.
  private transient CacheService<SCIFIOCell<?>> service; // hook used to cache during finalization
  private transient String cacheId; // needed for this cell's hashcode
  private transient int index; // needed for this cell's hashcode
  
  // -- Persistent fields --
  private A data;
  private int currentHash; // current hash of data
  private int cleanHash; // hash of data, unmodified
  private long elementSize = -1L; // element size, in bytes
  
  // -- Constructors --
  
  public SCIFIOCell() { } 
  
  public SCIFIOCell(CacheService<SCIFIOCell<?>> service, String cacheId, int index,
    final int[] dimensions, final long[] min, final A data) { 
    super(dimensions, min);
    this.data = data;
    this.service = service;
    this.cacheId = cacheId;
    this.index = index;
    markClean(); 
  }
  
  // -- SCIFIOCell Methods --
  

  /**
   * @return the data stored in this cell
   */
  public A getData() {
    return data;
  }
  
  /**
   * @param service CacheService reference
   */
  public void setService(CacheService<SCIFIOCell<?>> service) {
    this.service = service;
  }

  /**
   * @param cacheId Identifier for the cache storing this cell.
   */
  public void setCacheId(String cacheId) {
    this.cacheId = cacheId;
  }

  /**
   * @param index Linear index of this cell in its cache
   */
  public void setIndex(int index) {
    this.index = index;
  }
  
  /**
   * Sets the current state of this cell, as determined by the
   * hashcode of its underlying data, as the "clean" state.
   */
  public void markClean() {
    // Take a hash of the underlying data. If this is different
    // at finalization, we know this cell is dirty and should be
    // serialized.
    cleanHash = computeHash(data);
    
    // If data isn't an ArrayAccess object, this will cause it to always
    // look dirty compared to future computeHash calls.
    if (cleanHash == -1) cleanHash = 0;
    
    currentHash = cleanHash;
  }
  
  /**
   * @return Size of the stored data object, in bytes, or
   *         -1 if size not known.
   */
  public long getElementSize() {
    return elementSize;
  }
  
  /**
   * @return True if this cell has been modified since creation
   */
  public boolean dirty() {
    return !(currentHash == cleanHash);
  }
  
  /**
   * Forces this cell to determine if it is dirty or not by computing
   * the hash of its underlying data.
   */
  public void update() {
    currentHash = computeHash(data);
  }
  
  // -- Object method overrides --
  
  /**
   * Two SCIFIOCells are equal iff they come from the 
   * same cache, with the same index, and have the same
   * data state.
   */
  @Override
  public boolean equals(final Object other) {
    if (this == other)
      return true;
    if (other instanceof SCIFIOCell<?>) {
      SCIFIOCell<?> otherCell = (SCIFIOCell<?>)other;
      return cacheId.equals(otherCell.cacheId) &&
        (index == otherCell.index) && currentHash == otherCell.currentHash; 
    }
    return false;
  }

  @Override
  public int hashCode() {
    // Taken from Effective Java 2nd edition
    int result = 17;
    result = 31 * result + index;
    result = 31 * result + cacheId.hashCode();
    result = 31 * result + data.hashCode();
    
    return result; 
  }
  
  @Override
  public void finalize() {
    if (!dirty()) updateDirtyFlag();
   // Writes this cell to disk as it's garbage collected
    service.cache(cacheId, index, this);
  }
  
  // -- Helper Methods --
  
  // Computes a hash of the provided data object.
  // Also computes the size of the data object
  private int computeHash(ArrayDataAccess<?> data) {
    int hashCode = -1;
    
    if (data instanceof ByteArray) {
      byte[] bytes = ((ByteArray)data).getCurrentStorageArray();
      computedataSize(8l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof BitArray) {
      int[] bytes = ((BitArray)data).getCurrentStorageArray();
      computedataSize((long)Integer.SIZE * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof CharArray) {
      char[] bytes = ((CharArray)data).getCurrentStorageArray();
      computedataSize(8l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof DoubleArray) {
      double[] bytes = ((DoubleArray)data).getCurrentStorageArray();
      computedataSize(64l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof IntArray) {
      int[] bytes = ((IntArray)data).getCurrentStorageArray();
      computedataSize(32l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof FloatArray) {
      float[] bytes = ((FloatArray)data).getCurrentStorageArray();
      computedataSize(32l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof ShortArray) {
      short[] bytes = ((ShortArray)data).getCurrentStorageArray();
      computedataSize(16l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    else if (data instanceof LongArray) {
      long[] bytes = ((LongArray)data).getCurrentStorageArray();
      computedataSize(64l * bytes.length);
      hashCode = Arrays.hashCode(bytes);
    }
    
    return hashCode;
  }
  
  private void computedataSize(long bits) {
    elementSize = bits / 8;
  } 
}
