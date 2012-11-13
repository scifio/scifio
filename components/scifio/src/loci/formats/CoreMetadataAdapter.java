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

import java.util.Iterator;

import ome.scifio.DefaultImageMetadata;

import loci.legacy.adapter.AbstractLegacyAdapter;
import loci.legacy.adapter.AdapterTools;

/**
 * {@link LegacyAdapter} for converting between instances of
 * {@link ome.scifio.DatasetMetadata} and an array of {@link loci.formats.CoreMetadata}.
 * <p>
 * Since the legacy CoreMetadata objects were stored in an array, there is no class to
 * extend and no setter methods to override in the Modern -> Legacy direction. Thus 
 * any such delegation has to be managed by the {@link CoreImageMetadataAdapter}.
 * </p>
 * @author Mark Hiner
 *
 */
public class CoreMetadataAdapter extends 
  AbstractLegacyAdapter<loci.formats.CoreMetadata[], ome.scifio.DefaultDatasetMetadata> {

  // -- LegacyAdapter API --
  
  @Override
  protected loci.formats.CoreMetadata[] wrapToLegacy(ome.scifio.DefaultDatasetMetadata modern) {
    CoreMetadata[] legacyArray = new ImageMetadataWrapper[modern.getImageCount()];
    
    Iterator<DefaultImageMetadata> metaIterator = modern.getImageMetadata().iterator();
    
    int i = 0;
    while(metaIterator.hasNext()) {
      legacyArray[i] = 
          AdapterTools.getAdapter(CoreImageMetadataAdapter.class).getLegacy(metaIterator.next());
      i++;
    }
    
    return legacyArray;
  }

  @Override
  protected ome.scifio.DefaultDatasetMetadata wrapToModern(loci.formats.CoreMetadata[] legacy) {
    return new CoreMetadataArrayWrapper(legacy);
  }

}
