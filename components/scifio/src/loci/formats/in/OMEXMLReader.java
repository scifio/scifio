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

package loci.formats.in;

import io.scif.Metadata;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.MetadataTools;
import loci.formats.SCIFIOFormatReader;
import loci.formats.meta.MetadataStore;
import loci.legacy.context.LegacyContext;
import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLFormat;
import ome.xml.meta.OMEXMLMetadata;

/**
 * OMEXMLReader is the file format reader for OME-XML files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/OMEXMLReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/OMEXMLReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 * 
 * @deprecated see ome.xml.meta.OMEXMLFormat
 */
@Deprecated
public class OMEXMLReader extends SCIFIOFormatReader {

  // -- Constructor --

  /** Constructs a new OME-XML reader. */
  public OMEXMLReader() {
    super("OME-XML", "ome");
    try {
      format = LegacyContext.getSCIFIO().format().getFormatFromClass(OMEXMLFormat.class);
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create OMEXMLFormat components");
    }
  }

  // -- SCIFIOFormatReader API MEthods --
  
  @Override
  protected void setupMetdata() { 
    Metadata meta = reader.getMetadata();
    
    if (meta == null) {
      try {
        meta = reader.getFormat().createMetadata();
        reader.setMetadata(meta);

      } catch (io.scif.FormatException e) {
        LOGGER.error("Exception creating OMEXML Metadata", e);
      } catch (IOException e) {
        LOGGER.error("Exception setting OMEXML Metadata", e);
      }
    }
    
    MetadataStore store = getMetadataStore();
    
    if (meta != null && store instanceof OMEXMLMetadata)
      ((OMEXMLFormat.Metadata)meta).setOMEMeta(
        new OMEMetadata(meta.getContext(), (OMEXMLMetadata) store));
  }
  
  // -- IFormatReader API methods --

  @Override
  public void setId(String id) throws FormatException, IOException {
    super.setId(id);
    
    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);
//    service.convertMetadata(omexmlMeta, store);
  }
}