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

package ome.scifio.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import ome.scifio.AbstractReader;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.util.FormatTools;

/**
 * BIFormatReader is the superclass for file format readers
 * that use java.awt.image.BufferedImage as the native data type.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/BIFormatReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/BIFormatReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public abstract class BIFormatReader<M extends Metadata>
  extends AbstractReader<M> {
  // -- Constructors --

  /** Constructs a new BIFormatReader. */
  public BIFormatReader(final String name, final String suffix, final SCIFIO ctx) {
    super(name, suffix, ctx);
  }

  /** Constructs a new BIFormatReader. */
  public BIFormatReader(final String name, final String[] suffixes, final SCIFIO ctx) {
    super(name, suffixes, ctx);
  }

  // -- Reader API methods --

  /**
   * @see ome.scifio.Reader#openBytes(int, byte[], int, int, int, int)
   */
  @Override
  public byte[] openBytes(final int imageIndex, final int planeIndex, final byte[] buf, final int x,
    final int y, final int w, final int h) throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(
      this, imageIndex, planeIndex, buf.length, x, y, w, h);

    final BufferedImage data =
      (BufferedImage) openPlane(imageIndex, planeIndex, x, y, w, h);
    switch (data.getColorModel().getComponentSize(0)) {
      case 8:
        final byte[] t = AWTImageTools.getBytes(data, false);
        System.arraycopy(t, 0, buf, 0, Math.min(t.length, buf.length));
        break;
      case 16:
        final short[][] ts = AWTImageTools.getShorts(data);
        for (int c = 0; c < ts.length; c++) {
          int offset = c * ts[c].length * 2;
          for (int i = 0; i < ts[c].length && offset < buf.length; i++) {
            DataTools.unpackBytes(
              ts[c][i], buf, offset, 2, cMeta.isLittleEndian(planeIndex));
            offset += 2;
          }
        }
        break;
    }
    return buf;
  }

  // -- BIFormatReader methods --

  public Class<?> getNativeDataType() {
    return BufferedImage.class;
  }

}
