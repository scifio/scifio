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

package ome.scifio.wrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.DatasetMetadata;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.io.RandomAccessInputStream;
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
public class DimensionSwapper<M extends Metadata> extends ReaderWrapper<M> {

  // -- Utility methods --

  /** Converts the given reader into a DimensionSwapper, wrapping if needed. */
  public static DimensionSwapper makeDimensionSwapper(Reader r) {
    if (r instanceof DimensionSwapper) return (DimensionSwapper) r;
    return new DimensionSwapper(r);
  }

  // -- Fields --

  /** Core metadata associated with this dimension swapper. */
  private DatasetMetadata core;

  // -- Constructors --

  /** Constructs a DimensionSwapper around a new image reader. */
  public DimensionSwapper() { super(); }

  /** Constructs a DimensionSwapper with the given reader. */
  public DimensionSwapper(Reader<M> r) { super(r); }

  private List<AxisType>[] outputOrder;

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
      throw new IllegalArgumentException("newOrder is unexpected length (" +
        newOrder.size() + ")");
    }
    
    for(int i=0; i<newOrder.size(); i++) {
      if(!oldOrder.contains(newOrder.get(i)))
        throw new IllegalArgumentException("newOrder specifies different axes");
    }

    if(newOrder.get(0) != Axes.X && newOrder.get(1) != Axes.X)  {
      throw new IllegalArgumentException("X is not in first two positions");
    }
    if(newOrder.get(0) != Axes.Y && newOrder.get(1) != Axes.Y)  {
      throw new IllegalArgumentException("Y in unexpected position");
    }

    if (newOrder.indexOf(Axes.CHANNEL) != oldOrder.indexOf(Axes.CHANNEL)
      && core.getRGBChannelCount(imageIndex) > 1) {
      throw new IllegalArgumentException(
        "Cannot swap C dimension when RGB channel count > 1");
    }

    //core.currentOrder[series] = order;
    if (outputOrder[imageIndex] == null) {
      outputOrder[imageIndex] = Arrays.asList(core.getAxes(imageIndex));
    }
    
    FormatTools.setDimensionOrder(core, imageIndex, newOrder.toArray(new AxisType[newOrder.size()]));

    if (newOrder.indexOf(Axes.CHANNEL) != oldOrder.indexOf(Axes.CHANNEL)) {
      // C was overridden; clear the sub-C dimensional metadata
      core.setChannelDimLengths(imageIndex, new int[] {getDimensionLength(imageIndex, Axes.CHANNEL)});
      core.setChannelDimTypes(imageIndex, new String[] {FormatTools.CHANNEL});
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
    this.outputOrder[imageIndex] = outputOrder;
  }

  public List<AxisType> getInputOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return Arrays.asList(core.getAxes(imageIndex));
  }
  
  public int getDimensionLength(int imageIndex, AxisType t) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return core.getAxisLength(imageIndex, t);
  }

  public List<AxisType> getDimensionOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    List<AxisType> outOrder = outputOrder[imageIndex];
    if (outOrder != null) return outOrder;
    return getInputOrder(imageIndex);
  }

  // -- Reader API methods --

  /* @see Reader#openBytes(int) */
  public byte[] openBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    return super.openBytes(imageIndex, reorder(imageIndex, planeIndex));
  }

  /* @see Reader#openBytes(int, int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return super.openBytes(imageIndex, reorder(imageIndex, planeIndex), x, y, w, h);
  }

  /* @see Reader#openBytes(int, byte[]) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException
  {
    return super.openBytes(imageIndex, reorder(imageIndex, planeIndex), buf);
  }

  /* @see Reader#openBytes(int, byte[], int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return super.openBytes(imageIndex, reorder(imageIndex, planeIndex), buf, x, y, w, h);
  }

  /* @see Reader#openThumbImage(int) */
  public byte[] openThumbBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    return super.openThumbBytes(imageIndex, reorder(imageIndex, planeIndex));
  }

  /* @see Reader#getZCTCoords(int) */
  public int[] getZCTCoords(int imageIndex, int planeIndex) {
    return FormatTools.getZCTCoords(this, imageIndex, planeIndex);
  }

  /* @see Reader#getIndex(int, int, int) */
  public int getIndex(int imageIndex, int z, int c, int t) {
    return FormatTools.getIndex(this, imageIndex, z, c, t);
  }

  /* @see Reader#getDatasetMetadata() */
  @Override
  public DatasetMetadata getDatasetMetadata() {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return core;
  }

  /* @see Reader#setSource(String) */
  public void setSource(String id) throws IOException {
    setSource(new RandomAccessInputStream(id));
  }
  
  /* @see Reader#setSource(File) */
  public void setSource(File file) throws IOException {
    setSource(new RandomAccessInputStream(file.getAbsolutePath()));
  }
  
  /* @see Reader#setSource(RandomAccessInputStream) */
  public void setSource(RandomAccessInputStream stream) throws IOException {
    super.setSource(stream);
    String oldFile = getCurrentFile();
    if (!stream.getFileName().equals(oldFile) || outputOrder == null ||
      outputOrder.length != getReader().getImageCount())
    {
      outputOrder = new ArrayList[getReader().getImageCount()];

      // NB: Create our own copy of the DatasetMetadata,
      // which we can manipulate safely.
      core = new DatasetMetadata(core, getReader().getContext());
    }
  }

  // -- Helper methods --

  protected int reorder(int imageIndex, int planeIndex) {
    if (getInputOrder(imageIndex) == null) return planeIndex;
    List<AxisType> outputOrder = getDimensionOrder(imageIndex);
    AxisType[] outputAxes = outputOrder.toArray(new AxisType[outputOrder.size()]);
    List<AxisType> inputOrder = getInputOrder(imageIndex);
    AxisType[] inputAxes = inputOrder.toArray(new AxisType[inputOrder.size()]);
     
    return FormatTools.getReorderedIndex(FormatTools.findDimensionOrder(inputAxes),
      FormatTools.findDimensionOrder(outputAxes), getDimensionLength(imageIndex, Axes.Z),
      core.getEffectiveSizeC(imageIndex), getDimensionLength(imageIndex, Axes.TIME),
      core.getPlaneCount(imageIndex), imageIndex, planeIndex);
  }

}
