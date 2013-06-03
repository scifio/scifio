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

import io.scif.formats.MinimalTIFFFormat;

import java.io.IOException;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.MetadataTools;
import loci.formats.SCIFIOFormatReader;
import loci.formats.meta.MetadataStore;
import loci.formats.tiff.IFDList;
import loci.formats.tiff.TiffParser;
import loci.legacy.context.LegacyContext;

/**
 * MinimalTiffReader is the superclass for file format readers compatible with
 * or derived from the TIFF 6.0 file format.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/MinimalTiffReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/MinimalTiffReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 * 
 * @deprecated see io.scif.formats.MinimalTIFFFormat
 */
@Deprecated
public class MinimalTiffReader extends SCIFIOFormatReader {

  // -- Protected fields --
  
  //FIXME these need to be populated to maintain backwards compatibility
  
  /** List of IFDs for the current TIFF. */
  protected IFDList ifds;

  /** List of thumbnail IFDs for the current TIFF. */
  protected IFDList thumbnailIFDs;

  /**
* List of sub-resolution IFDs for each IFD in the current TIFF with the
* same order as <code>ifds</code>.
*/
  protected List<IFDList> subResolutionIFDs;

  protected TiffParser tiffParser;

  protected boolean equalStrips = false;

  protected boolean use64Bit = false;

  protected boolean noSubresolutions = false;
  
  // -- Constructors --

  /** Constructs a new MinimalTiffReader. */
  public MinimalTiffReader() {
    this("Minimal TIFF", new String[] {"tif", "tiff"});
  }

  /** Constructs a new MinimalTiffReader. */
  public MinimalTiffReader(String name, String suffix) {
    this(name, new String[] {suffix});
  }

  /** Constructs a new MinimalTiffReader. */
  public MinimalTiffReader(String name, String[] suffixes) {
    super(name, suffixes);

    try {
      format = LegacyContext.getSCIFIO().format().getFormatFromClass(MinimalTIFFFormat.class);
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create MinimalTIFFFormat components");
    }
  }
  
  // -- MinimalTiffReader API methods --

  /** Gets the list of IFDs associated with the current TIFF's image planes. */
  public IFDList getIFDs() {
    throw new UnsupportedOperationException("Not supported until we can adapt between IFD lists");
  }

  /** Gets the list of IFDs associated with the current TIFF's thumbnails. */
  public IFDList getThumbnailIFDs() {
    throw new UnsupportedOperationException("Not supported until we can adapt between IFD lists");
  }
  
  // -- IFormatReader API methods --

  @Override
  public void setId(String id) throws FormatException, IOException {
    super.setId(id);
    
    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);
  }
}
