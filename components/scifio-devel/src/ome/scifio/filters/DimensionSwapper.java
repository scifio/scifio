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

package ome.scifio.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.util.FormatTools;

/**
 * Handles swapping the dimension order of an image series. This class is
 * useful for both reassigning ZCT sizes (the input dimension order), and
 * shuffling around the resultant planar order (the output dimension order).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/DimensionSwapper.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/DimensionSwapper.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type=Filter.class, priority=DimensionSwapper.PRIORITY, attrs={
  @Attr(name=DimensionSwapper.FILTER_KEY, value=DimensionSwapper.FILTER_VALUE),
  @Attr(name=DimensionSwapper.ENABLED_KEY, value=DimensionSwapper.ENABLED_VAULE)
  })
public class DimensionSwapper extends AbstractReaderFilter {
  
  // -- Constants --
  
  public static final double PRIORITY = 4.0;
  public static final String FILTER_VALUE = "ome.scifio.Reader";
  
  // -- Contructor --
  
  public DimensionSwapper() {
    super(DimensionSwapperMetadata.class);
  }
  
  // -- DimensionSwapper API methods --

  /**
   * Sets the input dimension order according to the given string (e.g.,
   * "XYZCT"). This string indicates the planar rasterization order from the
   * source, overriding the detected order. It may result in the dimensional
   * axis sizes changing.
   *
   * If the given order is identical to the file's native order, then
   * nothing happens. Note that this method will throw an exception if X and Y
   * do not appear in positions 0 and 1 (although X and Y can be reversed).
   */
  public void swapDimensions(int imageIndex, List<AxisType> newOrder) {
    FormatTools.assertId(getCurrentFile(), true, 2);

    if (newOrder == null) throw new IllegalArgumentException("order is null");

    List<AxisType> oldOrder = getDimensionOrder(imageIndex);
    
    if (newOrder.size() != oldOrder.size()) {
      throw new IllegalArgumentException("newOrder is unexpected length: " +
        newOrder.size() + "; expected: " + oldOrder.size());
    }
    
    for(int i=0; i<newOrder.size(); i++) {
      if(!oldOrder.contains(newOrder.get(i)))
        throw new IllegalArgumentException("newOrder specifies different axes");
    }

    if(newOrder.get(0) != Axes.X && newOrder.get(1) != Axes.X)  {
      throw new IllegalArgumentException("X is not in first two positions");
    }
    if(newOrder.get(0) != Axes.Y && newOrder.get(1) != Axes.Y)  {
      throw new IllegalArgumentException("Y is not in first two positions");
    }

    if (newOrder.indexOf(Axes.CHANNEL) != oldOrder.indexOf(Axes.CHANNEL)
      && getMetadata().getRGBChannelCount(imageIndex) > 1) {
      throw new IllegalArgumentException(
        "Cannot swap C dimension when RGB channel count > 1");
    }

    //core.currentOrder[series] = order;
    if (metaCheck() && 
        !(((DimensionSwapperMetadata)getMetadata()).getOutputOrder() == null)) {
      ((DimensionSwapperMetadata)getMetadata()).getOutputOrder()[imageIndex] = 
          Arrays.asList(getMetadata().getAxes(imageIndex));
    }
    
    getMetadata().setAxisTypes(imageIndex, 
        newOrder.toArray(new AxisType[newOrder.size()]));

    if (newOrder.indexOf(Axes.CHANNEL) != oldOrder.indexOf(Axes.CHANNEL)) {
      // C was overridden; clear the sub-C dimensional metadata
      getMetadata().setChannelDimLengths(imageIndex,
          new int[] {getDimensionLength(imageIndex, Axes.CHANNEL)});
      getMetadata().setChannelDimTypes(imageIndex, new String[] {FormatTools.CHANNEL});
    }
  }

  /**
   * Sets the output dimension order according to the given string (e.g.,
   * "XYZCT"). This string indicates the final planar rasterization
   * order&mdash;i.e., the mapping from 1D plane number to 3D (Z, C, T) tuple.
   * Changing it will not affect the Z, C or T sizes but will alter the order
   * in which planes are returned when iterating.
   *
   * This method is useful when your application requires a particular output
   * dimension order; e.g., ImageJ virtual stacks must be in XYCZT order.
   */
  public void setOutputOrder(int imageIndex, List<AxisType> outputOrder) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    
    if (metaCheck() && 
        !(((DimensionSwapperMetadata)getMetadata()).getOutputOrder() == null))
      ((DimensionSwapperMetadata)getMetadata()).getOutputOrder()[imageIndex] =
        outputOrder;
  }

  /**
   * Returns the original axis order for this dataset. Not affected by swapping
   * dimensions.
   */
  public List<AxisType> getInputOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return Arrays.asList(getMetadata().getAxes(imageIndex));
  }
  
  /**
   * Returns the length of a given axis.
   */
  public int getDimensionLength(int imageIndex, AxisType t) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return getMetadata().getAxisLength(imageIndex, t);
  }

  /**
   * Returns the current axis order, accounting for any swapped dimensions.
   */
  public List<AxisType> getDimensionOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    List<AxisType> outOrder = null;
    
    if(metaCheck()) {
      outOrder = ((DimensionSwapperMetadata)getMetadata()).getOutputOrder()[imageIndex];
    }
    if (outOrder != null) return outOrder;
    return getInputOrder(imageIndex);
  }
  
  // -- AbstractReaderFilter API Methods --
  
  /*
   * @see ome.scifio.filters.AbstractReaderFilter#setSourceHelper()
   */
  @Override
  protected void setSourceHelper(String source) {
    String oldFile = getCurrentFile();
    if (!source.equals(oldFile) || metaCheck() && 
        (((DimensionSwapperMetadata)getMetadata()).getOutputOrder() == null ||
        ((DimensionSwapperMetadata)getMetadata()).getOutputOrder().length != getImageCount()))
    {
      ((DimensionSwapperMetadata)getMetadata()).setOutputOrder(new ArrayList[getImageCount()]);

      // NB: Create our own copy of the Metadata,
      // which we can manipulate safely.
      //TODO should be a copy method
      if(metaCheck())
        ((DimensionSwapperMetadata)getMetadata()).wrap(getParent().getMetadata());
    }
  }
  
  // -- Reader API methods --

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#openPlane(int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    return super.openPlane(imageIndex, reorder(imageIndex, planeIndex));
  }

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#openPlane(int, int, int, int, int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), x, y, w, h);
  }

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#openPlane(int, int, ome.scifio.Plane)
   */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane)
    throws FormatException, IOException
  {
    return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane);
  }

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#openPlane(int, int, ome.scifio.Plane, int, int, int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return super.openPlane(imageIndex, reorder(imageIndex, planeIndex), plane, x, y, w, h);
  }

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#openThumbPlane(int, int)
   */
  public Plane openThumbPlane(int imageIndex, int planeIndex) throws FormatException, IOException {
    return super.openThumbPlane(imageIndex, reorder(imageIndex, planeIndex));
  }

  /*
   * @see ome.scifio.filters.AbstractReaderFilter#getMetadata()
   */
  @Override
  public Metadata getMetadata() {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return super.getMetadata();
  }

  // -- Helper methods --
  
  /* Reorders the ImageMetadata axes associated with this filter */
  protected int reorder(int imageIndex, int planeIndex) {
    if (getInputOrder(imageIndex) == null) return planeIndex;
    List<AxisType> outputOrder = getDimensionOrder(imageIndex);
    AxisType[] outputAxes = outputOrder.toArray(new AxisType[outputOrder.size()]);
    List<AxisType> inputOrder = getInputOrder(imageIndex);
    AxisType[] inputAxes = inputOrder.toArray(new AxisType[inputOrder.size()]);
     
    return FormatTools.getReorderedIndex(FormatTools.findDimensionOrder(inputAxes),
      FormatTools.findDimensionOrder(outputAxes), getDimensionLength(imageIndex, Axes.Z),
      getMetadata().getEffectiveSizeC(imageIndex), getDimensionLength(imageIndex, Axes.TIME),
      getMetadata().getPlaneCount(imageIndex), imageIndex, planeIndex);
  }
}
