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

package ome.scifio.discovery;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.FormatException;
import ome.scifio.TypedMetadata;
import ome.scifio.TypedTranslator;

public class TranslatorDiscoverer<M extends TypedMetadata, N extends TypedMetadata>
    implements Discoverer<SCIFIOTranslator, TypedTranslator<M, N>> {

  // -- Constants --
  
  protected static final Logger LOGGER =
      LoggerFactory.getLogger(Discoverer.class);
          
  // -- Fields --
          
  private Class<M> metaInClass;
  private Class<N> metaOutClass;
  
  // -- Constructor --

  public TranslatorDiscoverer(Class<M> metaIn, Class<N> metaOut) {
    this.metaInClass = metaIn;
    this.metaOutClass = metaOut;
  }
  
  // -- Discoverer API Methods --
  
  public List<TypedTranslator<M, N>> discover() throws FormatException {
    List<TypedTranslator<M, N>> transList = new ArrayList<TypedTranslator<M, N>>();
    for (@SuppressWarnings("rawtypes")
    final IndexItem<SCIFIOTranslator, TypedTranslator> item : Index.load(
        SCIFIOTranslator.class, TypedTranslator.class)) {
      if (metaInClass == item.annotation().metaIn()
          && metaOutClass == item.annotation().metaOut()) {
        TypedTranslator<M, N> trans = null;
        try {
          trans = getInstance(item);
        }
        catch (InstantiationException e) {
           LOGGER.debug("Failed to instantiate: " + item, e);
        }
        transList.add(trans);
        return transList;
      }
    }
    return null;
  }

  // -- Helper Methods --
  
  @SuppressWarnings("unchecked")
  private TypedTranslator<M, N> getInstance(
    @SuppressWarnings("rawtypes") IndexItem<SCIFIOTranslator, TypedTranslator> item) throws InstantiationException {
    return item.instance();
  }
}
