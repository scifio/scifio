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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link ome.scifio.filters.Filter} implementations.
 * <p>
 * NB: the {@link #compareTo(Filter)} implementation provided produces
 * a natural ordering in descending order of {@link #getPriority()}.
 * </p>
 * <p>
 * NB: the {@link #setParent(Object)} method can not be typed narrowed with generics
 * due to erasure. Type checking is performed during this call, however,
 * to guarantee type safety for the cached {@link #parent} instance. An
 * {@link IllegalArgumentException} will be thrown if provided the wrong
 * type of parent for this filter.
 * </p>
 * 
 * @author Mark Hiner
 *
 * @param <T> - Parent data type of this filter.
 */
public abstract class AbstractFilter<T> implements Filter {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Filter.class);
  
  // -- Fields --
  
  private T parent = null;
  private Class<? extends T> parentClass;
  
  // -- Constructor --
  
  public AbstractFilter(Class<? extends T> parentClass) {
    this.parentClass = parentClass;
  }

  // -- Filter API Methods --

  /*
   * @see ome.scifio.filters.Filter#getParent()
   */
  public T getParent() {
    return parent;
  }

  /*
   * @see ome.scifio.filters.Filter#setParent(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  public void setParent(Object parent) {
    if(parentClass.isAssignableFrom(parent.getClass()))
      this.parent = (T) parent;
    else {
      throw new IllegalArgumentException("Invalid parent. Provided: " + parent.getClass() + 
              " Expected: " + parentClass);
    }
  }
  
  /*
   * @see ome.scifio.filters.Filter#reset()
   */
  public void reset() {
    parent = null;
  }
  
  /*
   * @see ome.scifio.filters.Filter#isCompatible(java.lang.Class)
   */
  public boolean isCompatible(Class<?> c) {
    return parentClass.isAssignableFrom(c);
  }
  
  // -- Comparable API Methods --
  
  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Filter n) {
    return getPriority().compareTo(n.getPriority());
  }
}
