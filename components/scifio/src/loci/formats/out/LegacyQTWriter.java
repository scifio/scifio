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

package loci.formats.out;

import io.scif.formats.LegacyQTFormat;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.SCIFIOFormatWriter;
import loci.legacy.context.LegacyContext;

/**
 * LegacyQTWriter is a file format writer for QuickTime movies. It uses the
 * QuickTime for Java library, and allows the user to choose between a variety
 * of common video codecs.
 *
 * Much of this code was based on the QuickTime Movie Writer for ImageJ
 * (available at http://rsb.info.nih.gov/ij/plugins/movie-writer.html).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/out/LegacyQTWriter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/out/LegacyQTWriter.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @deprecated see io.scif.formats.LegacyQTFormat
 */
@Deprecated
public class LegacyQTWriter extends SCIFIOFormatWriter {

  // -- Constructor --

  public LegacyQTWriter() {
    super("Legacy QuickTime", "mov");
    
    format = LegacyContext.getSCIFIO().format().getFormatFromClass(LegacyQTFormat.class);
    try {
      writer = format.createWriter() ;
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create LegacyQTFormat components");
    }
  }
  
  // -- LegacyQTWriter API --
  
  public void setCodec(int codec) { ((LegacyQTFormat.Writer)writer).setCodec(codec); }

  public void setQuality(int quality) { ((LegacyQTFormat.Writer)writer).setQuality(quality); }
  
  // -- IFormatWriter API methods --

  /*
   * @see loci.formats.IFormatWriter#saveBytes(int, byte[], int, int, int, int)
   */
  public void saveBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      writer.savePlane(getSeries(), no, planeCheck(buf, x, y, w, h), x, y, w, h);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }
}
