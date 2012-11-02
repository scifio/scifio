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

import loci.legacy.adapter.AdapterTools;
import loci.legacy.adapter.Wrapper;

/**
 * Wraps an array of legacy {@link loci.formats.CoreMetadata} objects
 * in the equivalent {@link ome.scifio.DatasetMetadata}. Since there are no
 * accessor methods of the array to override, its contents are manually
 * extracted and converted via the {@link CoreImageMetadataAdapter}.
 * 
 * @author Mark Hiner
 *
 */
public class CoreMetadataArrayWrapper extends ome.scifio.DatasetMetadata
  implements Wrapper<CoreMetadata[]>
{

  // -- Fields --
  
  private CoreMetadata[] cMeta;
  
  // -- Constructor --
  
  public CoreMetadataArrayWrapper(CoreMetadata[] core) {
    cMeta = core;
    
    for(int i = 0; i < cMeta.length; i++) {
      add(AdapterTools.getAdapter(CoreImageMetadataAdapter.class).getModern(cMeta[i]));
    }
  }
  
  // -- Wrapper API Methods --
  
  public CoreMetadata[] unwrap() {
    return cMeta;
  }
}
