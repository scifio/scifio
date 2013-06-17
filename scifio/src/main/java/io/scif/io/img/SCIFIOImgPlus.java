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

package io.scif.io.img;

import io.scif.FormatException;
import io.scif.io.img.cell.SCIFIOCellImg;

import java.io.IOException;

import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.Metadata;

/**
 * SCIFIO extension of {@link ImgPlus} that adds
 * {@link #getColorTable(int, int)} API for
 * specifying both the plane and image index.
 * 
 * @author Mark Hiner hinerm at gmail.com
 */
public class SCIFIOImgPlus<T> extends ImgPlus<T> {

  // -- Constructors --

  public SCIFIOImgPlus(final Img<T> img) {
    super(img);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name) {
    super(img, name);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name, final AxisType[] axes) {
    super(img, name, axes);
  }

  public SCIFIOImgPlus(final Img<T> img, final Metadata metadata) {
    super(img, metadata);
  }

  public SCIFIOImgPlus(final Img<T> img, final String name, final AxisType[] axes,
    final double[] cal)
  {
    super(img, name, axes, cal);
  }
  
  @Override
  public ColorTable getColorTable(final int planeIndex) {
    return getColorTable(0, planeIndex);
  }
  
  /**
   * @param imageIndex - Image index to look up the color table
   * @param planeIndex - Plane index of the desired color table
   * @return The ColorTable of the underlying dataset at the specified indices.
   */
  public ColorTable getColorTable(int imageIndex, final int planeIndex) {
    ColorTable table = super.getColorTable(planeIndex);
    
    if (table == null && SCIFIOCellImg.class.isAssignableFrom(getImg().getClass()))
    {
      try {
        table = ((SCIFIOCellImg<?, ?, ?>)getImg()).getColorTable(imageIndex, planeIndex);
      } catch (FormatException e) {
        return null;
      } catch (IOException e) {
        return null;
      }
      
      setColorTable(table, planeIndex);
    }
    
    return table;
  }
}
