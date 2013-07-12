/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.gui;

import io.scif.AbstractReader;
import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.TypedMetadata;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.Axes;


/**
 * BufferedImageReader is the superclass for file format readers
 * that use java.awt.image.BufferedImage as the native data type.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/BIFormatReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/BIFormatReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public abstract class BufferedImageReader<M extends TypedMetadata>
  extends AbstractReader<M, BufferedImagePlane> {
  // -- Constructors --

  /** Constructs a new BIFormatReader. */
  public BufferedImageReader() {
    super(BufferedImagePlane.class);
  }
  
  // -- Reader API Methods --
  
  /*
   * @see io.scif.Reader#openThumbPlane(int, int)
   */
  public BufferedImagePlane openThumbPlane(final int imageIndex, final int planeIndex)
    throws FormatException, IOException
  {
    FormatTools.assertStream(getStream(), true, 1);
    int w = getMetadata().getAxisLength(imageIndex, Axes.X);
    int h = getMetadata().getAxisLength(imageIndex, Axes.Y);
    int thumbX = getMetadata().getThumbSizeX(imageIndex);
    int thumbY = getMetadata().getThumbSizeY(imageIndex);
    
    BufferedImagePlane plane = createPlane(0, 0, thumbX, thumbY);
    
    plane.setData(AWTImageTools.openThumbImage(openPlane(imageIndex, planeIndex),
    			        this, imageIndex, w, h, thumbX, thumbY, false));
    
    return plane;
  } 

  /*
   * @see io.scif.Reader#createPlane(int, int, int, int)
   */
  public BufferedImagePlane createPlane(int xOffset, int yOffset, int xLength,
    int yLength)
  {
    return createPlane(getMetadata().get(0), xOffset, yOffset, xLength, yLength);
  }

  /*
   * @see io.scif.TypedReader#createPlane(int, int, int, int)
   */
  public BufferedImagePlane createPlane(ImageMetadata meta, int xOffset, int yOffset, int xLength,
    int yLength)
  {
    return new BufferedImagePlane(getContext(), meta, xOffset, yOffset, xLength, yLength);
  }
}
