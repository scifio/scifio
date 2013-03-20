/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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
package ome.scifio.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.discovery.DiscoverableFilter;

/**
 * Helper class for {@link ome.scifio.filters.MasterFilter} implementations. Takes the place
 * of an abstract superclass for containing boilerplate code.
 * <p>
 * As all {@code MasterFilter} implementations are also {@code Filters}, it makes sense for
 * concrete implementations of the former to be free to inherit from abstract implementations of the
 * latter. Thus, any boilerplate code that would be common to all {@code MasterFilter} implementations,
 * regardless of their wrapped type, must go in a helper class to avoid multiple inheritance conflicts.
 * </p>
 * <p>
 * On construction, all {@link ome.scifio.discovery.DiscoverableFilter} annotated classes are
 * discovered via {@code SezPoz}. All items whose {@code wrappedClass()} return the
 * same class as was used to construct this {@code MasterFilterHelper} will be added
 * to the list of filters for future use. Filters are initially enabled or disabled
 * based on their {@code isDefaultEnabled()} annotation return value.
 * </p>
 * <p>
 * Filters can be enabled or disabled using the {@link #enable(Class)} and {@link #disable(Class)}
 * methods. This process can be done in any order, as execution ordering of enabled filters is
 * maintained automatically per their natural order.
 * </p>
 * 
 * @author Mark Hiner
 *
 * @see ome.scifio.discovery.DiscoverableFilter
 * @see ome.scifio.filters.MasterFilter
 * @see ome.scifio.fitlers.Filter
 */
public class MasterFilterHelper<T> extends AbstractFilter<T> implements MasterFilter<T> {

  private T tail;
  private HashMap<Class<? extends Filter>, IndexItem<DiscoverableFilter, Filter>> refMap =
      new HashMap<Class<? extends Filter>, IndexItem<DiscoverableFilter, Filter>>();
  private TreeSet<Filter> enabled =
      new TreeSet<Filter>();
  
  // -- Constructor --
  
  public MasterFilterHelper() {
    this(null, null);
  }
  
  @SuppressWarnings("unchecked")
  public MasterFilterHelper(T wrapped, Class<? extends T> wrappedClass) {
    super(wrappedClass);
    tail = wrapped;

    // load sezpoz annotated wrappers
    for (final IndexItem<DiscoverableFilter, Filter> item : 
      Index.load(DiscoverableFilter.class, Filter.class)) {
      // check for matching class type
      if(item.annotation().wrappedClass().isAssignableFrom(wrapped.getClass())) {
        try {
          Class<? extends Filter> filterClass =
              (Class<? extends Filter>) Class.forName(item.className());
          
          // cache item for future instantiation
          refMap.put(filterClass, item);
          
          if(item.annotation().isDefaultEnabled()) enable(filterClass);
        } catch (ClassNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    setParent(tail);
  }
  
  // -- WrapperController API Methods --
  
  /*
   * @see ome.scifio.wrappers.WrapperController#enable(java.lang.Class)
   */
  public <F extends Filter> F enable(Class<F> filterClass) {
    IndexItem<DiscoverableFilter, Filter> item = refMap.get(filterClass);
    
    if(item != null) {
      try {
        @SuppressWarnings("unchecked")
        F filter = (F) item.instance();
        
        enabled.add(filter);
        updateParents();
        
        return filter;
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return null;
  }

  /*
   * @see ome.scifio.wrappers.WrapperController#disable(java.lang.Class)
   */
  public boolean disable(Class<? extends Filter> filterClass) {
    IndexItem<DiscoverableFilter, Filter> item = refMap.get(filterClass);
    
    boolean disabled = false;
    
    if(item != null) {
      try {
        Filter filter = item.instance();
        
        enabled.remove(item);
        updateParents();

        filter.reset();
        disabled = true;
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return disabled;
  }
  
  // -- Filter API Methods --
  
  /*
   * @see ome.scifio.filters.Filter#reset()
   */
  @Override
  public void reset() {
    super.reset();
    enabled.clear();
    updateParents();
  }
  
  /*
   * @see ome.scifio.filters.Filter#getPriority()
   */
  public Double getPriority() {
    throw new UnsupportedOperationException("MasterFilters do not have a priority.");
  }
  
  /*
   * @see ome.scifio.filters.MasterFilter#getFilterClasses()
   */
  public Set<Class<? extends Filter>> getFilterClasses() {
    return refMap.keySet();
  }
  
  // -- Comparable API Methods --

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Filter arg0) {
    return 0;
  }
  
  // -- Helper Methods --
  
  /*
   * Re-generates the hierarchical wrappings between each
   * enabled filter, based on their ordering per
   * {@link java.util.TreeSet}.
   */
  private void updateParents() {
    if(enabled.isEmpty()) setParent(tail);
    else {
      // need to wrap in reverse order to prevent changing the state of a parent's
      // wrapped object
      Iterator<Filter> filterIterator = enabled.descendingIterator();
      Filter currentFilter = filterIterator.next();
      currentFilter.setParent(tail);

      while(filterIterator.hasNext()) {
        Filter nextFilter = filterIterator.next();
        currentFilter.setParent(nextFilter);
        currentFilter = nextFilter;
      }
      
      setParent(currentFilter);
    }
  }
}