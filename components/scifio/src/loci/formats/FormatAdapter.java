/*
 * #%L
 * Legacy layer preserving compatibility between legacy Bio-Formats and SCIFIO.
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

import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;

import java.util.List;

import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.legacy.adapter.AdapterTools;

/**
 * Provides type-safe adapting methods for loci.formats classes using
 * AdapterTools.get().
 * 
 * @see loci.legacy.adapter.AdapterTools
 * 
 * @author Mark Hiner
 *
 */
public final class FormatAdapter {

  // -- CoreImageMetadata Adapter Methods --
  
  /**
   * Maps io.scif.ImageMetadata to loci.formats.CoreMetadata
   */
  public static CoreMetadata get(ImageMetadata meta) {
    return (CoreMetadata)AdapterTools.get(meta);
  }
  
  /**
   * Maps loci.formats.CoreMetadata to io.scif.ImageMetadata
   */
  public static ImageMetadata get(CoreMetadata meta) {
    return (ImageMetadata)AdapterTools.get(meta);
  }
  
  // -- CoreMetadata Adapter methods --
  
  /**
   * Maps io.scif.Metadata to a java.util.List of loci.formats.CoreMetadata
   */
  public static List<CoreMetadata> get(Metadata meta) {
    @SuppressWarnings("unchecked")
    List<CoreMetadata> metaList = (List<CoreMetadata>)AdapterTools.get(meta);
    
    return metaList;
  }
  
  /**
   * Maps a java.util.List of loci.formats.CoreMetadata to io.scif.Metadata 
   */
  public static Metadata get(List<CoreMetadata> meta) {
    return (Metadata)AdapterTools.get(meta);
  }
  
  // -- SCIFIOReader Adapter Methods --
  
  /**
   * Maps io.scif.Reader to loci.formats.IFormatReader
   */
  public static IFormatReader get(Reader reader) {
    return (IFormatReader)AdapterTools.get(reader);
  }
  
  /**
   * Maps loci.formats.IFormatReader to io.scif.Reader
   */
  public static Reader get(IFormatReader reader) {
    return (Reader)AdapterTools.get(reader);
  }
  
  // -- MetadataOptions Adapter Methods --
  
  /**
   * Maps loci.formats.in.MetadataOptions to io.scif.MetadataOptions
   */
  public static io.scif.MetadataOptions get(MetadataOptions opts) {
    return (io.scif.MetadataOptions)AdapterTools.get(opts);
  }
  
  /**
   * Maps io.scif.MetadataOptions to loci.formats.in.MetadataOptions
   */
  public static MetadataOptions get(io.scif.MetadataOptions opts) {
    return (MetadataOptions)AdapterTools.get(opts);
  }
}
