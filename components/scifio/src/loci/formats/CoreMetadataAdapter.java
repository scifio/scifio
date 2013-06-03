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
package loci.formats;

import io.scif.Metadata;

import java.util.ArrayList;
import java.util.List;


import org.scijava.plugin.Plugin;

import loci.legacy.adapter.AbstractLegacyAdapter;

/**
 * {@link LegacyAdapter} for converting between instances of
 * {@link io.scif.Metadata} and a List of {@link loci.formats.CoreMetadata}.
 * 
 * @see loci.legacy.adapter.LegacyAdapter
 * @see loci.formats.CoreMetadata
 * @see io.scif.Metadata
 * 
 * @author Mark Hiner
 *
 */
@Plugin(type=CoreMetadataAdapter.class)
public class CoreMetadataAdapter extends 
  AbstractLegacyAdapter<List<loci.formats.CoreMetadata>, Metadata> {
  
  // -- Constructor --
  
  private static Class<List<CoreMetadata>> metaClass = getListClass();

  @SuppressWarnings("unchecked")
  private static Class<List<CoreMetadata>> getListClass() {
    // *** HACK *** to get around generic casting problems with javac/ant
    List<CoreMetadata> tmpList = new ArrayList<CoreMetadata>();
    Class<List<CoreMetadata>> tmpClass = (Class<List<CoreMetadata>>)tmpList.getClass();
    
    // NB: List is the top-level interface of ArrayList, but this is safer.
    for (Class<?> c : tmpClass.getInterfaces()) {
      if (List.class.isAssignableFrom(c)) tmpClass = (Class<List<CoreMetadata>>) c;
    }
    
    return tmpClass;
  }
  
  public CoreMetadataAdapter() {
    super(metaClass, Metadata.class);
  }

  // -- LegacyAdapter API --
  
  @Override
  protected List<loci.formats.CoreMetadata> wrapToLegacy(Metadata modern) {
    List<CoreMetadata> legacyList = new ArrayList<CoreMetadata>();
    
    for (int i=0; i<modern.getImageCount(); i++) {
      legacyList.add(FormatAdapter.get(modern.get(i)));
    }
    
    return legacyList;
  }

  @Override
  protected io.scif.Metadata wrapToModern(List<loci.formats.CoreMetadata> legacy) {
    return new CoreMetadataListWrapper(legacy);
  }
}
