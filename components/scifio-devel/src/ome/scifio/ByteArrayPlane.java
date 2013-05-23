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

import ome.scifio.common.DataTools;
import ome.scifio.util.FormatTools;

import org.scijava.Context;

/**
 * A naive {@link ome.scifio.Plane} implementation that uses {@code byte[]} for its underlying
 * data type.
 * 
 * @see ome.scifio.Plane
 * @see ome.scifio.DataPlane
 * 
 * @author Mark Hiner
 */
public class ByteArrayPlane extends AbstractPlane<byte[], ByteArrayPlane> {

  // -- Constructor --
  
  public ByteArrayPlane(final Context context) {
    super(context);
  }

  public ByteArrayPlane(final Context context, ImageMetadata meta, int xOffset,
      int yOffset, int xLength, int yLength) {
    super(context, meta, xOffset, yOffset, xLength, yLength);

    byte[] buf = null;
    
    buf = DataTools.allocate(xLength, yLength, FormatTools.getBytesPerPixel(getImageMetadata().getPixelType()), meta.getRGBChannelCount());
    
    setData(buf);
  }
  
  // -- Plane API methods --

  /*
   * @see ome.scifio.Plane#getBytes()
   */
  public byte[] getBytes() {
    return getData();
  }
}
