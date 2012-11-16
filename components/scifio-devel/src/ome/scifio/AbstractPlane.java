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
package ome.scifio;

import net.imglib2.display.ColorTable;

/**
 * Abstract superclass for {@link Plane} implementations in SCIFIO.
 * 
 * @author Mark Hiner
 *
 */
public abstract class AbstractPlane<T> extends AbstractHasContext 
  implements DataPlane<T> {

  // -- Fields --
  
  /** Native pixel data for this plane. */
  private T data = null;
  
  /** Color look-up table for this plane. */
  private ColorTable lut = null;
  
  /** Metadata describing the underlying image. */
  private ImageMetadata meta = null;
  
  /** X-axis offset into the underlying image. */
  private int xOffset = 0;
  
  /** Y-axis offset into the underlying image. */
  private int yOffset = 0;
  
  /** Length of the plane in the X-axis. */
  private int xLength = 0;
  
  /** Length of the plane in the Y-axis. */
  private int yLength = 0;
  
  // -- Constructor --
  
  public AbstractPlane(SCIFIO ctx) {
    super(ctx);
  }

  public AbstractPlane(SCIFIO ctx, ImageMetadata meta, int xOffset,
      int yOffset, int xLength, int yLength) {
    super(ctx);
    // TODO bounds checking?
    populate(meta, xOffset, yOffset, xLength, yLength);
  }
  
  // -- DataPlane API methods --
  
  /* @see DataPlane#setData(T) */
  public void setData(T data) {
    this.data = data;
  }
  
  /* @see DataPlane#getData() */
  public T getData() {
    return data;
  }
  
  // -- Plane API methods --

  /* @see Plane#setColorTable(ColorTable) */
  public void setColorTable(ColorTable lut) {
    this.lut = lut;
  }
  
  /* @see Plane#getColorTable() */
  public ColorTable getColorTable() {
    return lut;
  }
  
  /*
   * @see ome.scifio.Plane#getImageMetadata()
   */
  public ImageMetadata getImageMetadata() {
    return meta;
  }
  
  /*
   * @see ome.scifio.Plane#getxOffset()
   */
  public int getxOffset() {
    return xOffset;
  }

  /*
   * @see ome.scifio.Plane#getyOffset()
   */
  public int getyOffset() {
    return yOffset;
  }

  /*
   * @see ome.scifio.Plane#getxLength()
   */
  public int getxLength() {
    return xLength;
  }

  /*
   * @see ome.scifio.Plane#getyLength()
   */
  public int getyLength() {
    return yLength;
  }
  
  /*
   * @see ome.scifio.Plane#initialize(ome.scifio.ImageMetadata, int, int, int,
   *      int)
   */
  public void populate(ImageMetadata meta, int xOffset, int yOffset,
      int xLength, int yLength) {
    populate(meta, null, xOffset, yOffset, xLength, yLength);
  }
  
  /*
   * @see ome.scifio.Plane#initialize(ome.scifio.ImageMetadata, int, int, int,
   *      int)
   */
  public void populate(T data, int xOffset, int yOffset,
      int xLength, int yLength) {
    populate(null, data, xOffset, yOffset, xLength, yLength);
  }
  
  /*
   * @see ome.scifio.Plane#initialize(ome.scifio.ImageMetadata, int, int, int,
   *      int)
   */
  public void populate(ImageMetadata meta, T data, int xOffset, int yOffset,
      int xLength, int yLength) {
    setImageMetadata(meta);
    setData(data);
    setxOffset(xOffset);
    setyOffset(yOffset);
    setxLength(xLength);
    setyLength(yLength);
  }
  
  /*
   * @see ome.scifio.Plane#setImageMetadata(ome.scifio.ImageMetadata)
   */
  public void setImageMetadata(ImageMetadata meta) {
    this.meta = meta;
  }

  /*
   * @see ome.scifio.Plane#setxOffset(int)
   */
  public void setxOffset(int xOffset) {
    this.xOffset = xOffset;
  }

  /*
   * @see ome.scifio.Plane#setyOffset(int)
   */
  public void setyOffset(int yOffset) {
    this.yOffset = yOffset;
  }

  /*
   * @see ome.scifio.Plane#setxLength(int)
   */
  public void setxLength(int xLength) {
    this.xLength = xLength;
  }

  /*
   * @see ome.scifio.Plane#setyLength(int)
   */
  public void setyLength(int yLength) {
    this.yLength = yLength;
  }
}
