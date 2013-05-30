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

package ome.scifio.io.img.cell.loaders;

import java.io.IOException;

import net.imglib2.meta.Axes;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.util.FormatTools;

/**
 * Abstract superclass for all {@link SCIFIOArrayLoader} implementations.
 * 
 * <p>
 * Reads the byte array appropriate for the given cell dimensions and delegates
 * to each subclass's array conversion method. See {@link #convertBytes(Object, byte[], int)}.
 * </p>
 * 
 * @author Mark Hiner hinerm at gmail.com
 */
public abstract class AbstractArrayLoader<A> implements SCIFIOArrayLoader<A>
{
  final private Reader reader;

  public AbstractArrayLoader( final Reader reader )
  {
    this.reader = reader;
  }
  
  public A loadArray(int[] dimensions, long[] min) {
    synchronized( reader )
    {
      Metadata meta = reader.getMetadata();
 
      StringBuilder dimOrder = new StringBuilder(FormatTools.findDimensionOrder(meta, 0).toUpperCase());
      
      if (meta.getAxisLength(0, Axes.X) == 1) dimOrder = dimOrder.deleteCharAt(dimOrder.indexOf("X"));
      if (meta.getAxisLength(0, Axes.Y) == 1) dimOrder = dimOrder.deleteCharAt(dimOrder.indexOf("Y"));
      if (meta.getAxisLength(0, Axes.Z) == 1) dimOrder = dimOrder.deleteCharAt(dimOrder.indexOf("Z"));
      if (meta.getEffectiveSizeC(0) == 1) dimOrder = dimOrder.deleteCharAt(dimOrder.indexOf("C"));
      if (meta.getAxisLength(0, Axes.TIME) == 1) dimOrder = dimOrder.deleteCharAt(dimOrder.indexOf("T"));
      
      int xIndex = dimOrder.indexOf("X");
      int yIndex = dimOrder.indexOf("Y");
      int zIndex = dimOrder.indexOf("Z");
      int cIndex = dimOrder.indexOf("C");
      int tIndex = dimOrder.indexOf("T");
      
      int zSlice = new Long(zIndex == -1 ? 0 : min[zIndex]).intValue();
      int tSlice = new Long(tIndex == -1 ? 0 : min[tIndex]).intValue();
      int cSlice = new Long(cIndex == -1 ? 0 : min[cIndex]).intValue();
      int zMax = zIndex == -1 ? 1 : dimensions[zIndex] + zSlice;
      int tMax = tIndex == -1 ? 1 : dimensions[tIndex] + tSlice;
      int cMax = cIndex == -1 ? 1 : dimensions[cIndex] + cSlice;

      A data = emptyArray(dimensions);
      Plane tmpPlane = null;
      
      int planeSize = -1;
      
      int[][] iterBounds = null;
      String zctOrder = "";

      if (zIndex < cIndex) {
        if (zIndex < tIndex) {
          if (cIndex < tIndex) {
            zctOrder = "ZCT";
            iterBounds = getBounds(zSlice, zMax, cSlice, cMax, tSlice, tMax);
          }
          else {
            zctOrder = "ZTC";
            iterBounds = getBounds(zSlice, zMax, tSlice, tMax, cSlice, cMax);
          }
        }
        else {
          zctOrder = "TZC";
          iterBounds = getBounds(tSlice, tMax, zSlice, zMax, cSlice, cMax);
        }
      }
      else if (tIndex < cIndex) {
        zctOrder = "TCZ";
        iterBounds = getBounds(tSlice, tMax, cSlice, cMax, zSlice, zMax);
      }
      else {
        if (zIndex < tIndex) {
          zctOrder = "CZT";
          iterBounds = getBounds(cSlice, cMax, zSlice, zMax, tSlice, tMax);
        }
        else {
          zctOrder = "CTZ";
          iterBounds = getBounds(cSlice, cMax, tSlice, tMax, zSlice, zMax);
        }
      }
      
      int planesRead = 0;
      int[] index = new int[]{iterBounds[0][0], iterBounds[1][0], iterBounds[2][0]};
      
      int x = new Long(xIndex == -1 ? 0 : min[xIndex]).intValue();
      int y = new Long(yIndex == -1 ? 0 : min[yIndex]).intValue();
      int w = xIndex == -1 ? 1 : dimensions[xIndex];
      int h = yIndex == -1 ? 1 : dimensions[yIndex];
      
      int i1 = index[1], i2 = index[2];
      
      try {
        for (; index[0]<iterBounds[0][1]; index[0]++) {
          for (; index[1]<iterBounds[1][1]; index[1]++) {
            for (; index[2]<iterBounds[2][1]; index[2]++) {
              int z = index[zctOrder.indexOf('Z')];
              int c = index[zctOrder.indexOf('C')];
              int t = index[zctOrder.indexOf('T')];
              
              int planeIndex = FormatTools.getIndex(reader, 0, z, c, t);
              
              if (tmpPlane == null) tmpPlane = reader.openPlane(0, planeIndex, x, y, w, h);
              else tmpPlane = reader.openPlane(0, planeIndex, tmpPlane, x, y, w, h);
              
              if (planeSize == -1) planeSize = tmpPlane.getBytes().length;
              
              convertBytes(data, tmpPlane.getBytes(), planesRead);
              
              planesRead++;
            }
            index[2] = i2;
          }
          index[1] = i1;
        }
      } catch (FormatException e) {
        throw new IllegalStateException("Could not open a plane for the given dimensions", e);
      } catch (IOException e) {
        throw new IllegalStateException("Could not open a plane for the given dimensions", e);
      }
      
      return data;
    }
  }

  private int[][] getBounds(int start1, int max1, int start2, int max2,
      int start3, int max3) {
    return new int[][]{{start1, max1}, {start2, max2}, {start3, max3}};
  }

  protected int countEntities(int[] dimensions) {
    int numEntities = 1;
    for ( int i = 0; i < dimensions.length; ++i )
      numEntities *= dimensions[ i ];
    return numEntities;
  }
  
  protected abstract void convertBytes(A data, byte[] bytes, int planesRead);

  protected Reader reader() {
    return reader;
  }
}
