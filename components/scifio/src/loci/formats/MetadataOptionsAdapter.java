/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
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

import org.scijava.plugin.Plugin;

import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataOptions;
import loci.legacy.adapter.AbstractLegacyAdapter;
import loci.legacy.adapter.LegacyAdapter;

/**
 * {@link LegacyAdapter} implementation for converting between
 * io.scif.MetadataOptions and loci.formats.in.MetadataOptions.
 * 
 * @author Mark Hiner hinerm at gmail.com
 * 
 * @see io.scif.MetadataOptions
 * @see loci.formats.in.MetadataOptions
 */
@Plugin(type=MetadataOptionsAdapter.class)
public class MetadataOptionsAdapter extends
  AbstractLegacyAdapter<MetadataOptions, io.scif.MetadataOptions>
{
  
  // -- Constructor --

  public MetadataOptionsAdapter()
  {
    super(MetadataOptions.class, io.scif.MetadataOptions.class);
  }
  
  // -- AbstractLegacyAdapter API Methods --

  @Override
  protected MetadataOptions wrapToLegacy(io.scif.MetadataOptions modern) {
    return new DefaultMetadataOptions(MetadataLevelAdapter.get(modern.getMetadataLevel()));
  }

  @Override
  protected io.scif.MetadataOptions wrapToModern(MetadataOptions legacy) {
    return new io.scif.DefaultMetadataOptions(
        MetadataLevelAdapter.get(legacy.getMetadataLevel()));
  }
}
