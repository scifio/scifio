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
package ome.scifio;

import java.awt.image.BufferedImage;

import org.scijava.Context;

import ome.scifio.common.DataTools;
import ome.scifio.gui.AWTImageTools;

/**
 * A {@link ome.scifio.Plane} implementation using a {@link BufferedImage} for the
 * underlying data representation.
 * 
 * @author Mark Hiner
 *
 */
public class BufferedImagePlane extends AbstractPlane<BufferedImage, BufferedImagePlane> {

  // -- Constructor --
  
  public BufferedImagePlane(final Context context) {
    super(context);
  }
  
  public BufferedImagePlane(final Context context, ImageMetadata meta, int xOffset,
      int yOffset, int xLength, int yLength) {
    super(context, meta, xOffset, yOffset, xLength, yLength);
   
    populate(meta, xOffset, yOffset, xLength, yLength);
  }

  // -- Plane API methods --
  
  /**
   * Standardizes this plane's {@link BufferedImage} to a byte[].
   * <p>
   * NB: If this plane contains multiple channels, the planes for each channel
   * will be condensed to a single array. The first entry for each channel's
   * data will have an offset of:
   * <p><code>channelIndex * planeSize</code></p>
   * </p>
   */
  public byte[] getBytes() {
    byte[] t = null;
    
    switch (getData().getColorModel().getComponentSize(0)) {
    case 8:
      t = AWTImageTools.getBytes(getData(), false);
      break;
    case 16:
      short[][] ts = AWTImageTools.getShorts(getData());
      t = new byte[ts.length * ts[0].length * 2];
      for (int c=0; c<ts.length; c++) {
        int offset = c * ts[c].length * 2;
        for (int i=0; i<ts[c].length; i++) {
          DataTools.unpackBytes(ts[c][i], t, offset, 2, getImageMetadata().isLittleEndian());
          offset += 2;
        }
      }
      break;
  }
    
    
    return t;
  }
  
  /*
   * @see ome.scifio.AbstractPlane#populate(ome.scifio.ImageMetadata, int, int, int, int)
   */
  public BufferedImagePlane populate(ImageMetadata meta, BufferedImage data, int xOffset, int yOffset,
      int xLength, int yLength) {
    
    if (data == null) {
//      byte[] bytes = new byte[xLength * yLength * 
//                              (meta.getBitsPerPixel() / 8) *
//                              meta.getRGBChannelCount()];

      int type = meta.getPixelType();
//      boolean signed = FormatTools.isSigned(type);

      data = AWTImageTools.blankImage(xLength, yLength, meta.getRGBChannelCount(), type);
    }
    
    return (BufferedImagePlane) super.populate(meta, data, xOffset, yOffset, xLength, yLength);
  }
}
