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

import loci.common.DateTools;
import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.SCIFIOFormatReader;

import ome.scifio.Metadata;
import ome.scifio.ics.ICSFormat;

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
 */
public class ICSReader extends SCIFIOFormatReader {

  // -- Fields --

  // -- Constructor --

  /** Constructs a new ICSReader. */
  @Deprecated
  public ICSReader() {
    super("Image Cytometry Standard", new String[] {"ics", "ids"});
    
    try {
      format = new ICSFormat(FormatTools.CONTEXT);
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (ome.scifio.FormatException e) {
      LOGGER.warn("Failed to create ICSFormat components");
    }
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isSingleFile(String) */
  @Deprecated
  public boolean isSingleFile(String id) throws FormatException, IOException {
    return ((ICSFormat)format).isVersionTwo(reader.getMetadata());
  }

  /* @see loci.formats.IFormatReader#getDomains() */
  @Deprecated
  public String[] getDomains() {
    return reader.getDomains();
  }

  /* @see loci.formats.IFormatReader#getChannelDimLengths() */
  @Deprecated
  public int[] getChannelDimLengths() {
    return reader.getCoreMetadata().getChannelDimLengths(series);
  }

  /* @see loci.formats.IFormatReader#getChannelDimTypes() */
  @Deprecated
  public String[] getChannelDimTypes() {
    return reader.getCoreMetadata().getChannelDimTypes(series);
  }

  /* @see loci.formats.IFormatReader#isInterleaved(int) */
  @Deprecated
  public boolean isInterleaved(int subC) {
    return reader.getCoreMetadata().isInterleaved(series);
  }

  /* @see loci.formats.IFormatReader#fileGroupOption(String) */
  @Deprecated
  public int fileGroupOption(String id) throws FormatException, IOException {
    return ome.scifio.util.FormatTools.MUST_GROUP;
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  @Deprecated
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      return reader.openBytes(series, no, buf, x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
  }

  /* @see loci.formats.IFormatReader#getSeriesUsedFiles(boolean) */
  @Deprecated
  public String[] getSeriesUsedFiles(boolean noPixels) {
    return parser.getImageUsedFiles(series, noPixels);
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  @Deprecated
  public void close(boolean fileOnly) throws IOException {
    parser.close(fileOnly);
    reader.close(fileOnly);
  }

  // -- Internal FormatReader API methods --
  
  /* @see loci.formats.FormatReader#initFile(String) */
  @Deprecated
  protected void initFile(String id) throws FormatException, IOException {
 // ARG for testing protected void oldInitFile(String id) throws FormatException, IOException {
    super.initFile(id);

    Metadata meta = null;
    try {
      meta = parser.parse(id);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e.getCause());
    }
    
    reader.setSource(id);
    reader.setMetadata(meta);
  }

  // -- Helper methods --

  // -- Test methods --
}
