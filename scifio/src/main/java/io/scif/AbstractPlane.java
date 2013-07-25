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
package io.scif;

import org.scijava.Context;
import org.scijava.plugin.SortablePlugin;

import net.imglib2.display.ColorTable;

/**
 * Abstract superclass for {@link io.scif.Plane} implementations in SCIFIO.
 * 
 * @see io.scif.Plane
 * @see io.scif.DataPlane
 * 
 * @author Mark Hiner
 * 
 * @param <T> - The underlying data type used by this Plane
 * @param <P> - A recursive reference to this concrete class
 */
public abstract class AbstractPlane<T, P extends DataPlane<T>> extends SortablePlugin
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

  public AbstractPlane(final Context context) {
    setContext(context);
  }

  public AbstractPlane(final Context context, ImageMetadata meta, int xOffset,
      int yOffset, int xLength, int yLength) {
    setContext(context);
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
   * @see io.scif.Plane#getImageMetadata()
   */
  public ImageMetadata getImageMetadata() {
    return meta;
  }

  /*
   * @see io.scif.Plane#getxOffset()
   */
  public int getxOffset() {
    return xOffset;
  }

  /*
   * @see io.scif.Plane#getyOffset()
   */
  public int getyOffset() {
    return yOffset;
  }

  /*
   * @see io.scif.Plane#getxLength()
   */
  public int getxLength() {
    return xLength;
  }

  /*
   * @see io.scif.Plane#getyLength()
   */
  public int getyLength() {
    return yLength;
  }

  public P populate(Plane p) {
    return populate(p.getImageMetadata(), p.getxOffset(),
                    p.getyOffset(), p.getxLength(), p.getyLength());
  }

  /*
   * @see io.scif.DataPlane#populate(io.scif.DataPlane)
   */
  public P populate(DataPlane<T> plane) {
    return populate(plane.getImageMetadata(), plane.getData(), plane.getxOffset(),
        plane.getyOffset(), plane.getxLength(), plane.getyLength());
  }

  /*
   * @see io.scif.Plane#initialize(io.scif.ImageMetadata, int, int, int,
   *      int)
   */
  public P populate(ImageMetadata meta, int xOffset, int yOffset,
      int xLength, int yLength) {
    return populate(meta, null, xOffset, yOffset, xLength, yLength);
  }

  /*
   * @see io.scif.Plane#initialize(io.scif.ImageMetadata, int, int, int,
   *      int)
   */
  public P populate(T data, int xOffset, int yOffset,
      int xLength, int yLength) {
    return populate(null, data, xOffset, yOffset, xLength, yLength);
  }

  /*
   * @see io.scif.Plane#initialize(io.scif.ImageMetadata, int, int, int,
   *      int)
   */
  public P populate(ImageMetadata meta, T data, int xOffset, int yOffset,
      int xLength, int yLength) {
    setImageMetadata(meta);
    setData(data);
    setxOffset(xOffset);
    setyOffset(yOffset);
    setxLength(xLength);
    setyLength(yLength);

    @SuppressWarnings("unchecked")
    P pl = (P) this;
    return pl;
  }

  /*
   * @see io.scif.Plane#setImageMetadata(io.scif.ImageMetadata)
   */
  public void setImageMetadata(ImageMetadata meta) {
    this.meta = meta;
  }

  /*
   * @see io.scif.Plane#setxOffset(int)
   */
  public void setxOffset(int xOffset) {
    this.xOffset = xOffset;
  }

  /*
   * @see io.scif.Plane#setyOffset(int)
   */
  public void setyOffset(int yOffset) {
    this.yOffset = yOffset;
  }

  /*
   * @see io.scif.Plane#setxLength(int)
   */
  public void setxLength(int xLength) {
    this.xLength = xLength;
  }

  /*
   * @see io.scif.Plane#setyLength(int)
   */
  public void setyLength(int yLength) {
    this.yLength = yLength;
  }
}
