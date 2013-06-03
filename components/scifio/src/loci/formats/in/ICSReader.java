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

import io.scif.SCIFIO;
import io.scif.Translator;
import io.scif.formats.ICSFormat;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.SCIFIOFormatReader;
import loci.legacy.context.LegacyContext;

import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.meta.OMEXMLMetadataImpl;
import ome.xml.services.OMEXMLMetadataService;

/**
 * ICSReader is the file format reader for ICS (Image Cytometry Standard)
 * files. More information on ICS can be found at http://libics.sourceforge.net
 *
 * TODO : remove sub-C logic once N-dimensional support is in place
 *        see http://dev.loci.wisc.edu/trac/java/ticket/398
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/ICSReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/ICSReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 * 
 * @deprecated Use io.scif.formats.ICSFormat instead.
 */
@Deprecated
public class ICSReader extends SCIFIOFormatReader {

  // -- Fields --

  // -- Constructor --

  /** Constructs a new ICSReader. */
  public ICSReader() {
    super("Image Cytometry Standard", new String[] {"ics", "ids"});
    
    try {
      format = LegacyContext.getSCIFIO().format().getFormatFromClass(ICSFormat.class);
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create ICSFormat components");
    }
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isSingleFile(String) */
  public boolean isSingleFile(String id) throws FormatException, IOException {
    return ((ICSFormat.Parser)parser).isVersionTwo(id);
  }

  /* @see loci.formats.IFormatReader#fileGroupOption(String) */
  public int fileGroupOption(String id) throws FormatException, IOException {
    return io.scif.util.FormatTools.MUST_GROUP;
  }

  @Override
  public void setId(String id) throws FormatException, IOException {
 // ARG for testing protected void oldInitFile(String id) throws FormatException, IOException {
    super.setId(id);
    OMEXMLMetadata omeRoot = new OMEXMLMetadataImpl() ;
    omeRoot.createRoot();
    LegacyContext.get().getService(OMEXMLMetadataService.class).populatePixels(omeRoot, reader.getMetadata());
    omeRoot.setImageName(id, 0);
    
    OMEMetadata omeMeta = new OMEMetadata(reader.getContext(), omeRoot);
    
    io.scif.MetadataOptions options = null;
    
    switch (metadataOptions.getMetadataLevel()) {
    case MINIMUM: options =
        new io.scif.DefaultMetadataOptions(io.scif.MetadataLevel.MINIMUM);
    break;
    case NO_OVERLAYS: options =
        new io.scif.DefaultMetadataOptions(io.scif.MetadataLevel.NO_OVERLAYS);
    break;
    case ALL: options =
        new io.scif.DefaultMetadataOptions(io.scif.MetadataLevel.ALL);
    break;
    }
    
    omeMeta.setMetadataOptions(options);

    SCIFIO scifio = new SCIFIO(reader.getContext());
    
    Translator t = scifio.translator().findTranslator(reader.getMetadata(), omeMeta, true);

    t.translate(reader.getMetadata(), omeMeta);
    
    loci.formats.ome.OMEXMLMetadata omeStore = new loci.formats.ome.OMEXMLMetadataImpl();
    omeStore.setRoot(omeMeta.getRoot().getRoot());
    
//TODO can't set this here, because currentId is already set.
//    setMetadataStore(omeStore);
  }
}
