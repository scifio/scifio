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

import java.io.IOException;

import ome.scifio.Metadata;
import ome.scifio.apng.APNGFormat;

import loci.formats.FormatException;
import loci.formats.MetadataTools;
import loci.formats.gui.SCIFIOBIFormatReader;
import loci.formats.meta.MetadataStore;
import loci.legacy.context.LegacyContext;

/**
 * APNGReader is the file format reader for
 * Animated Portable Network Graphics (APNG) images.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/APNGReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/APNGReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
@Deprecated
public class APNGReader extends SCIFIOBIFormatReader {

  // -- Constants --

  // -- Fields --

  // -- Constructor --

  /** Constructs a new APNGReader. */
  public APNGReader() {
    super("Animated PNG", "png");

    try {
      format = new APNGFormat(LegacyContext.get());
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (ome.scifio.FormatException e) {
      LOGGER.warn("Failed to create APNGFormat components");
    }
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#get8BitLookupTable() */
  @Deprecated
  public byte[][] get8BitLookupTable() throws IOException, FormatException {
    try {
      return reader.getDatasetMetadata().get8BitLookupTable(getSeries());
    } catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#openBytes(int) */
  @Override
  @Deprecated
  public byte[] openBytes(int no) throws FormatException, IOException {
    try {
      return reader.openPlane(getSeries(), no);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#openBytes(int, byte[]) */
  @Override
  @Deprecated
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    try {
      return reader.openPlane(this.getSeries(), no, buf);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see IFormatReader#openBytes(int, int, int, int, int) */
  @Override
  @Deprecated
  public byte[] openBytes(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return reader.openPlane(this.getSeries(), no, x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see loci.formats.IFormatReader#openPlane(int, int, int, int, int int) */
  @Deprecated
  public Object openPlane(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return reader.openPlane(this.getSeries(), no, x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    parser.close(fileOnly);
    reader.close(fileOnly);
  }

  // -- Internal FormatReader methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  @Deprecated
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);
    Metadata meta = null;
    try {
      meta = parser.parse(id);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
    reader.setMetadata(meta);
    
    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);
  }
}
