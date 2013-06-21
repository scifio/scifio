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

package io.scif.io.img.cell;

import io.scif.io.img.cell.SCIFIOImgCells.CellCache;
import io.scif.io.img.cell.loaders.SCIFIOArrayLoader;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;


/**
 * {@link CellCache} implementation for {@link SCIFIOCell}s. Loads
 * the requested cell if it is not already in the cache.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class SCIFIOCellCache<A> implements CellCache<A> {

  public static class Key implements Serializable {
    private static final long serialVersionUID = 6660872720885948546L;
    // NB: cache is per image so index is unique, no worries about clashing
    final int index;

    public Key(final int index) {
      this.index = index;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other)
        return true;
      if (!(other instanceof SCIFIOCellCache.Key))
        return false;
      final Key that = (Key) other;
      return (this.index == that.index);
    }

    @Override
    public int hashCode() {
      return index;
    }
  }

  final protected SCIFIOArrayLoader<A> loader;
  
  private HashMap<Integer, Key> keyMap = new HashMap<Integer, Key>();

  // In-memory cache.
  final protected ConcurrentHashMap<Key, SoftReference<SCIFIOCell<A>>> map = new ConcurrentHashMap<Key, SoftReference<SCIFIOCell<A>>>();

  public SCIFIOCellCache(final SCIFIOArrayLoader<A> loader) {
    this.loader = loader;
  }

  public SCIFIOCell<A> get(int index) {
    final Key k = getKey(index);
    SCIFIOCell<A> cell = checkCache(k);

    if (cell != null)
      return cell;

    return null;
  }

  public SCIFIOCell<A> load(int index, int[] cellDims, long[] cellMin) {
    final Key k = getKey(index);
    SCIFIOCell<A> cell = checkCache(k);

    if (cell != null) {
      return cell;
    }

    cell = new SCIFIOCell<A>(cellDims, cellMin, loader.loadArray(cellDims,
        cellMin));

    cache(k, cell);
    
    int c = loader.getBitsPerElement();
    for (final int l : cellDims)
      c *= l;

    return cell;
  }
  
  private Key getKey(int index) {
    Integer k = index;
    
    Key key = keyMap.get(k);
    
    if (key == null) {
      key = new Key(k);
      keyMap.put(k, key);
    }
    
    return key;
  }
  
  private void cache(Key k, SCIFIOCell<A> cell) {
    map.put(k, new SoftReference<SCIFIOCell<A>>(cell));
  }

  private SCIFIOCell<A> checkCache(Key k) {
    SCIFIOCell<A> cell = null;
    
    // Check the local cache
    SoftReference<SCIFIOCell<A>> ref = map.get(k);
    boolean found = false;
    
    if (ref != null) {
      cell = ref.get();
      found = true;
    }

    return cell;
  }
}
