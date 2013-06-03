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

import io.scif.formats.JavaFormat;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.SCIFIOFormatWriter;
import loci.legacy.context.LegacyContext;

/**
 * JavaWriter is the file format writer for Java source code.
 * At the moment, this code is just a very simple container for pixel data.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/out/JavaWriter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/out/JavaWriter.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @deprectaed see io.scif.formats.JavaFormat
 */
@Deprecated
public class JavaWriter extends SCIFIOFormatWriter {

  // -- Constructor --

  public JavaWriter() { 
    super("Java source code", "java");
    format = LegacyContext.getSCIFIO().format().getFormatFromClass(JavaFormat.class);
    try {
      writer = format.createWriter();
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create JavaFormat components");
    }
  }

  // -- IFormatWriter API methods --

  /*
   * @see loci.formats.IFormatWriter#saveBytes(int, byte[], int, int, int, int)
   */
  public void saveBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      writer.savePlane(getSeries(), no, planeCheck(buf, x, y, w, h), x, y, w, h);
    } catch (io.scif.FormatException e) {
      throw (FormatException)e;
    }
  }

  /* @see loci.formats.IFormatWriter#canDoStacks() */
  public boolean canDoStacks() { return writer.canDoStacks(); }
}
