package ome.scifio.io.img.cell;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

import ome.scifio.io.img.cell.SCIFIOImgCells.CellCache;
import ome.scifio.io.img.cell.loaders.SCIFIOArrayLoader;

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

  /**
   * First checks the local (weak) map. If empty, ehcache is checked - which can
   * deserialize from disk. This was done for performance reasons as the pure
   * ehcache in-memory implementation is much slower than just a basic
   * ConcurrentHashMap.
   */
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
