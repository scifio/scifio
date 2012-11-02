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

import ome.scifio.ImageMetadata;
import loci.legacy.adapter.AbstractLegacyAdapter;

/**
 * {@link LegacyAdapter} for converting between instances of
 * {@link ome.scifio.ImageMetadata} and {@link loci.formats.CoreMetadata}.
 * <p>
 * Note that because of the presence of public fields, synchronization
 * between the layers can not be maintained in the Modern -> Legacy direction.
 * It is therefore assumed that any modification of the legacy CoreMetadata object
 * corrupts it.
 * </p>
 * @author Mark Hiner
 *
 */
public class CoreImageMetadataAdapter extends AbstractLegacyAdapter<CoreMetadata, ImageMetadata> {

  // -- LegacyWrapper API Methods --
  
  @Override
  protected CoreMetadata wrapToLegacy(ImageMetadata modern) {
    return new ImageMetadataWrapper(modern);
  }

  @Override
  protected ImageMetadata wrapToModern(CoreMetadata legacy) {
    return new CoreMetadataWrapper(legacy);
  }

}
