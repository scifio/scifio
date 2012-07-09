/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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

package loci.formats.gui;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * Legacy delegator class for ome.scifio.util.Index16ColorModel.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/gui/Index16ColorModel.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/gui/Index16ColorModel.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class Index16ColorModel extends ColorModel {

  // -- Fields --
  
  private ome.scifio.util.Index16ColorModel cm;

  // -- Constructors --

  public Index16ColorModel(int bits, int size, short[][] table,
    boolean littleEndian) throws IOException
  {
    super(bits);
    cm = new ome.scifio.util.Index16ColorModel(bits, size, table, littleEndian);
  }

  // -- Index16ColorModel API methods --

  /** Return the array of red color components. */
  public short[] getReds() {
    return cm.getReds();
  }

  /** Return the array of green color components. */
  public short[] getGreens() {
    return cm.getGreens();
  }

  /** Return the array of blue color components. */
  public short[] getBlues() {
    return cm.getBlues();
  }

  /** Return the array of alpha color components. */
  public short[] getAlphas() {
    return cm.getAlphas();
  }

  // -- ColorModel API methods --

  /* @see java.awt.image.ColorModel#getDataElements(int, Object) */
  public synchronized Object getDataElements(int rgb, Object pixel) {
    return cm.getDataElements(rgb, pixel);
  }

  /* @see java.awt.image.ColorModel#isCompatibleRaster(Raster) */
  public boolean isCompatibleRaster(Raster raster) {
    return cm.isCompatibleRaster(raster);
  }

  /* @see java.awt.image.ColorModel#createCompatibleWritableRaster(int, int) */
  public WritableRaster createCompatibleWritableRaster(int w, int h) {
    return cm.createCompatibleWritableRaster(w, h);
  }

  /* @see java.awt.image.ColorModel#getAlpha(int) */
  public int getAlpha(int pixel) {
    return cm.getAlpha(pixel);
  }

  /* @see java.awt.image.ColorModel#getBlue(int) */
  public int getBlue(int pixel) {
    return cm.getBlue(pixel);
  }

  /* @see java.awt.image.ColorModel#getGreen(int) */
  public int getGreen(int pixel) {
    return cm.getGreen(pixel);
  }

  /* @see java.awt.image.ColorModel#getRed(int) */
  public int getRed(int pixel) {
    return cm.getRed(pixel);
  }

}
