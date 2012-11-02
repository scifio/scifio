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
import java.util.Collection;
import java.util.Hashtable;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import ome.scifio.ImageMetadata;
import ome.scifio.DatasetMetadata;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.MetadataOptions;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;
import ome.scifio.util.ImageTools;

/**
 * For indexed color data representing true color, factors out
 * the indices, replacing them with the color table values directly.
 *
 * For all other data (either non-indexed, or indexed with
 * "false color" tables), does nothing.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ChannelFiller.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ChannelFiller.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class ChannelFiller<M extends Metadata> extends ReaderWrapper<M> {

  // -- Utility methods --

  /** Converts the given reader into a ChannelFiller, wrapping if needed. */
  public static ChannelFiller makeChannelFiller(Reader r) {
    if (r instanceof ChannelFiller) return (ChannelFiller) r;
    return new ChannelFiller(r);
  }

  // -- Fields --

  /**
   * Whether to fill in the indices.
   * By default, indices are filled iff data not false color.
   */
  protected Boolean filled = null;

  /** Number of LUT components. */
  protected int lutLength;

  // -- Constructors --

  /** Constructs a ChannelFiller around a new image reader. */
  public ChannelFiller() { super(); }

  /** Constructs a ChannelFiller with a given reader. */
  public ChannelFiller(Reader<M> r) { super(r); }

  // -- ChannelFiller methods --

  /** Returns true if the indices are being factored out. */
  public boolean isFilled(int imageIndex) {
    if (!datasetMeta().isIndexed(imageIndex)) return false; // cannot fill non-indexed color
    if (lutLength < 1) return false; // cannot fill when LUTs are missing
    return filled == null ? !datasetMeta().isFalseColor(imageIndex) : filled;
  }

  /** Toggles whether the indices should be factored out. */
  public void setFilled(boolean filled) {
    this.filled = filled;
  }
  
  /** */
  public boolean isRGB(int imageIndex) {
    if (!isFilled(imageIndex)) return datasetMeta().isRGB(imageIndex);
    return datasetMeta().getRGBChannelCount(imageIndex) > 1;
  }

  /** */
  public boolean isIndexed(int imageIndex) {
    if (!isFilled(imageIndex)) return datasetMeta().isIndexed(imageIndex);
    return false;
  }
  
  /** */
  public byte[][] get8BitLookupTable(int imageIndex) throws FormatException, IOException {
    if (!isFilled(imageIndex)) return datasetMeta().get8BitLookupTable(imageIndex);
    return null;
  }

  /** */
  public short[][] get16BitLookupTable(int imageIndex) throws FormatException, IOException {
    if (!isFilled(imageIndex)) return datasetMeta().get16BitLookupTable(imageIndex);
    return null;
  }
  
  /** */
  public int[] getChannelDimLengths(int imageIndex) {
    int[] cLengths = datasetMeta().getChannelDimLengths(imageIndex);
    if (!isFilled(imageIndex)) return cLengths;

    // in the case of a single channel, replace rather than append
    if (cLengths.length == 1 && cLengths[0] == 1) cLengths = new int[0];

    // append filled dimension to channel dim lengths
    int[] newLengths = new int[1 + cLengths.length];
    newLengths[0] = lutLength;
    System.arraycopy(cLengths, 0, newLengths, 1, cLengths.length);
    return newLengths;
  }

  /** */
  public String[] getChannelDimTypes(int imageIndex) {
    String[] cTypes = datasetMeta().getChannelDimTypes(imageIndex);
    if (!isFilled(imageIndex)) return cTypes;

    // in the case of a single channel, leave type unchanged
    int[] cLengths = datasetMeta().getChannelDimLengths(imageIndex);
    if (cLengths.length == 1 && cLengths[0] == 1) return cTypes;

    // append filled dimension to channel dim types
    String[] newTypes = new String[1 + cTypes.length];
    newTypes[0] = FormatTools.CHANNEL;
    System.arraycopy(cTypes, 0, newTypes, 1, cTypes.length);
    return newTypes;
  }

  // -- Reader API methods --

  /* @see Reader#getDatasetMetadata() */
  @Override
  public DatasetMetadata getDatasetMetadata() {
    DatasetMetadataWrapper wrapper = new DatasetMetadataWrapper(datasetMeta());
    
    return (DatasetMetadata)wrapper;
  }

  /* @see Reader#openBytes(int) */
  @Override
  public byte[] openBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    return openBytes(imageIndex, planeIndex, 0, 0, 
      datasetMeta().getAxisLength(imageIndex, Axes.X), datasetMeta().getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, byte[]) */
  @Override
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException
  {
    return openBytes(imageIndex, planeIndex, buf, 0, 0, 
      datasetMeta().getAxisLength(imageIndex, Axes.X), datasetMeta().getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, int, int, int) */
  @Override
  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    byte[] buf = DataTools.allocate(w, h, datasetMeta().getRGBChannelCount(imageIndex),
      FormatTools.getBytesPerPixel(datasetMeta().getPixelType(imageIndex)));
    return openBytes(imageIndex, planeIndex, buf, x, y, w, h);
  }

  /* @see Reader#openBytes(int, byte[], int, int, int, int) */
  @Override
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    if (!isFilled(imageIndex)) return getReader().openBytes(imageIndex, planeIndex, buf, x, y, w, h);

    // TODO: The pixel type should change to match the available color table.
    // That is, even if the indices are uint8, if the color table is 16-bit,
    // The pixel type should change to uint16. Similarly, if the indices are
    // uint16 but we are filling with an 8-bit color table, the pixel type
    // should change to uint8.

    // TODO: This logic below is opaque and could use some comments.

    byte[] pix = getReader().openBytes(imageIndex, planeIndex, x, y, w, h);
    if (datasetMeta().getPixelType(imageIndex) == FormatTools.UINT8) {
      byte[][] b = ImageTools.indexedToRGB(get8BitLookupTable(imageIndex), pix);
      if (datasetMeta().isInterleaved(imageIndex)) {
        int pt = 0;
        for (int i=0; i<b[0].length; i++) {
          for (int j=0; j<b.length; j++) {
            buf[pt++] = b[j][i];
          }
        }
      }
      else {
        for (int i=0; i<b.length; i++) {
          System.arraycopy(b[i], 0, buf, i*b[i].length, b[i].length);
        }
      }
      return buf;
    }
    short[][] s = ImageTools.indexedToRGB(get16BitLookupTable(imageIndex),
      pix, datasetMeta().isLittleEndian(imageIndex));

    if (datasetMeta().isInterleaved(imageIndex)) {
      int pt = 0;
      for (int i=0; i<s[0].length; i++) {
        for (int j=0; j<s.length; j++) {
          buf[pt++] = (byte) (datasetMeta().isLittleEndian(imageIndex) ?
            (s[j][i] & 0xff) : (s[j][i] >> 8));
          buf[pt++] = (byte) (datasetMeta().isLittleEndian(imageIndex) ?
            (s[j][i] >> 8) : (s[j][i] & 0xff));
        }
      }
    }
    else {
      int pt = 0;
      for (int i=0; i<s.length; i++) {
        for (int j=0; j<s[i].length; j++) {
          buf[pt++] = (byte) (datasetMeta().isLittleEndian(imageIndex) ?
            (s[i][j] & 0xff) : (s[i][j] >> 8));
          buf[pt++] = (byte) (datasetMeta().isLittleEndian(imageIndex) ?
            (s[i][j] >> 8) : (s[i][j] & 0xff));
        }
      }
    }
    return buf;
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
    setSource(stream, 0);
  }
  
  // -- Augmented setSource methods for updating LutLength --

  public void setSource(String id, int imageIndex) throws IOException {
    setSource(new RandomAccessInputStream(id), imageIndex);
  }
  
  public void setSource(File file, int imageIndex) throws IOException {
    setSource(new RandomAccessInputStream(file.getAbsolutePath()), imageIndex);
  }
  
  public void setSource(RandomAccessInputStream stream, int imageIndex) throws IOException {
    super.setSource(stream);
    
    try {
      lutLength = getLookupTableComponentCount(0);
    }
    catch (FormatException e) {
      LOGGER.debug(e.getMessage());
    }
  }
  
  // -- Helper methods --

  /** Gets the number of color components in the lookup table. */
  private int getLookupTableComponentCount(int imageIndex)
    throws FormatException, IOException
  {
    byte[][] lut8 = get8BitLookupTable(imageIndex);
    if (lut8 != null) return lut8.length;
    short[][] lut16 = get16BitLookupTable(imageIndex);
    if (lut16 != null) return lut16.length;
    lut8 = get8BitLookupTable(imageIndex);
    if (lut8 != null) return lut8.length;
    lut16 = get16BitLookupTable(imageIndex);
    if (lut16 != null) return lut16.length;
    return 0; // LUTs are missing
  }
  
  //-- DatasetMetadata wrapping class --

  /**
   * Wrapping class that allows the returned DatasetMetadata to
   * have the correct axis lengths if isFilled would return
   * true.
   * 
   * The axisLengthod is intercepted and updated if
   * AxisType == Axes.CHANNEL
   * 
   * @author Mark Hiner
   *
   */
  private class DatasetMetadataWrapper extends DatasetMetadata {
    
    // -- Fields (delegation target) --
    
    private DatasetMetadata dMeta;
    
    // -- Constructor --
    
    public DatasetMetadataWrapper(DatasetMetadata meta) {
      dMeta = meta;
    }
    
    // -- DatasetMetadata API methods --

    /* @see ome.scifio.AbstractHasContext#getContext() */
    public SCIFIO getContext() {
      return dMeta.getContext();
    }

    /* @see ome.scifio.AbstractMetadata#getFormat() */
    public Format<?, ?, ?, ?, ?> getFormat() {
      return dMeta.getFormat();
    }

    /* @see ome.scifio.AbstractHasContext#setContext(ome.scifio.SCIFIO) */
    public void setContext(SCIFIO ctx) {
      dMeta.setContext(ctx);
    }

    /* @see ome.scifio.AbstractMetadata#reset(java.lang.Class) */
    public void reset(Class<?> type) {
      dMeta.reset(type);
    }

    /* @see ome.scifio.AbstractHasContext#toString() */
    public String toString() {
      return dMeta.toString();
    }

    /* @see ome.scifio.DatasetMetadata#getMetadataValue(int, java.lang.String) */
    public Object getMetadataValue(int imageIndex, String field) {
      return dMeta.getMetadataValue(imageIndex, field);
    }

    /* @see ome.scifio.DatasetMetadata#getImageMetadataValue(int, java.lang.String) */
    public Object getImageMetadataValue(int imageIndex, String field) {
      return dMeta.getImageMetadataValue(imageIndex, field);
    }

    /* @see ome.scifio.DatasetMetadata#getGlobalMetadata() */
    public Hashtable<String, Object> getDatasetMetadata() {
      return dMeta.getDatasetMetadata();
    }

    /* @see ome.scifio.DatasetMetadata#getImageMetadata(int) */
    public Hashtable<String, Object> getImageMetadata(int imageIndex) {
      return dMeta.getImageMetadata(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getImageCount() */
    public int getImageCount() {
      return dMeta.getImageCount();
    }

    /* @see ome.scifio.DatasetMetadata#getPlaneCount(int) */
    public int getPlaneCount(int imageIndex) {
      return dMeta.getPlaneCount(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isInterleaved(int) */
    public boolean isInterleaved(int imageIndex) {
      return dMeta.isInterleaved(imageIndex);
    }

    /* @see ome.scifio.AbstractMetadata#setSource(ome.scifio.io.RandomAccessInputStream) */
    public void setSource(RandomAccessInputStream source) {
      dMeta.setSource(source);
    }

    /* @see ome.scifio.DatasetMetadata#getPixelType(int) */
    public int getPixelType(int imageIndex) {
      return dMeta.getPixelType(imageIndex);
    }

    /* @see ome.scifio.AbstractMetadata#getSource() */
    public RandomAccessInputStream getSource() {
      return dMeta.getSource();
    }

    /* @see ome.scifio.DatasetMetadata#getEffectiveSizeC(int) */
    public int getEffectiveSizeC(int imageIndex) {
      return dMeta.getEffectiveSizeC(imageIndex);
    }

    /* @see ome.scifio.AbstractMetadata#getMetadataOptions() */
    public MetadataOptions getMetadataOptions() {
      return dMeta.getMetadataOptions();
    }

    /* @see ome.scifio.DatasetMetadata#getRGBChannelCount(int) */
    public int getRGBChannelCount(int imageIndex) {
      return dMeta.getRGBChannelCount(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isLittleEndian(int) */
    public boolean isLittleEndian(int imageIndex) {
      return dMeta.isLittleEndian(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isIndexed(int) */
    public boolean isIndexed(int imageIndex) {
      return dMeta.isIndexed(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getBitsPerPixel(int) */
    public int getBitsPerPixel(int imageIndex) {
      return dMeta.getBitsPerPixel(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isRGB(int) */
    public boolean isRGB(int imageIndex) {
      return dMeta.isRGB(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getChannelDimLengths(int) */
    public int[] getChannelDimLengths(int imageIndex) {
      return dMeta.getChannelDimLengths(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getChannelDimTypes(int) */
    public String[] getChannelDimTypes(int imageIndex) {
      return dMeta.getChannelDimTypes(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getThumbSizeX(int) */
    public int getThumbSizeX(int imageIndex) {
      return dMeta.getThumbSizeX(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getThumbSizeY(int) */
    public int getThumbSizeY(int imageIndex) {
      return dMeta.getThumbSizeY(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getAxisLength(int, net.imglib2.meta.AxisType) */
    public int getAxisLength(int imageIndex, AxisType t) {
      int length = dMeta.getAxisLength(imageIndex, t);

      if(!t.equals(Axes.CHANNEL))
        return length;

      return (!isFilled(imageIndex)) ? length : length * lutLength; 
    }

    /* @see ome.scifio.DatasetMetadata#getAxisType(int, int) */
    public AxisType getAxisType(int imageIndex, int axisIndex) {
      return dMeta.getAxisType(imageIndex, axisIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getAxisLength(int, int) */
    public int getAxisLength(int imageIndex, int index) {
      return dMeta.getAxisLength(imageIndex, index);
    }

    /* @see ome.scifio.DatasetMetadata#addAxis(int, net.imglib2.meta.AxisType) */
    public void addAxis(int imageIndex, AxisType type) {
      dMeta.addAxis(imageIndex, type);
    }

    /* @see ome.scifio.DatasetMetadata#addAxis(int, net.imglib2.meta.AxisType, int) */
    public void addAxis(int imageIndex, AxisType type, int value) {
      dMeta.addAxis(imageIndex, type, value);
    }

    /* @see ome.scifio.DatasetMetadata#isOrderCertain(int) */
    public boolean isOrderCertain(int imageIndex) {
      return dMeta.isOrderCertain(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isThumbnailImage(int) */
    public boolean isThumbnailImage(int imageIndex) {
      return dMeta.isThumbnailImage(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#isMetadataComplete(int) */
    public boolean isMetadataComplete(int imageIndex) {
      return dMeta.isMetadataComplete(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#set8BitLookupTable(int, byte[][]) */
    public void set8BitLookupTable(int imageIndex, byte[][] lut)
      throws FormatException, IOException
    {
      dMeta.set8BitLookupTable(imageIndex, lut);
    }

    /* @see ome.scifio.DatasetMetadata#set16BitLookupTable(int, short[][]) */
    public void set16BitLookupTable(int imageIndex, short[][] lut)
      throws FormatException, IOException
    {
      dMeta.set16BitLookupTable(imageIndex, lut);
    }

    /* @see ome.scifio.DatasetMetadata#setThumbSizeX(int, int) */
    public void setThumbSizeX(int imageIndex, int thumbX) {
      dMeta.setThumbSizeX(imageIndex, thumbX);
    }

    /* @see ome.scifio.DatasetMetadata#setThumbSizeY(int, int) */
    public void setThumbSizeY(int imageIndex, int thumbY) {
      dMeta.setThumbSizeY(imageIndex, thumbY);
    }

    /* @see ome.scifio.DatasetMetadata#setPixelType(int, int) */
    public void setPixelType(int imageIndex, int type) {
      dMeta.setPixelType(imageIndex, type);
    }

    /* @see ome.scifio.DatasetMetadata#setBitsPerPixel(int, int) */
    public void setBitsPerPixel(int imageIndex, int bpp) {
      dMeta.setBitsPerPixel(imageIndex, bpp);
    }

    /* @see ome.scifio.DatasetMetadata#setChannelDimLengths(int, int[]) */
    public void setChannelDimLengths(int imageIndex, int[] cLengths) {
      dMeta.setChannelDimLengths(imageIndex, cLengths);
    }

    /* @see ome.scifio.DatasetMetadata#setChannelDimTypes(int, java.lang.String[]) */
    public void setChannelDimTypes(int imageIndex, String[] cTypes) {
      dMeta.setChannelDimTypes(imageIndex, cTypes);
    }

    /* @see ome.scifio.DatasetMetadata#setOrderCertain(int, boolean) */
    public void setOrderCertain(int imageIndex, boolean orderCertain) {
      dMeta.setOrderCertain(imageIndex, orderCertain);
    }

    /* @see ome.scifio.DatasetMetadata#setRGB(int, boolean) */
    public void setRGB(int imageIndex, boolean rgb) {
      dMeta.setRGB(imageIndex, rgb);
    }

    /* @see ome.scifio.DatasetMetadata#setLittleEndian(int, boolean) */
    public void setLittleEndian(int imageIndex, boolean littleEndian) {
      dMeta.setLittleEndian(imageIndex, littleEndian);
    }

    /* @see ome.scifio.DatasetMetadata#setInterleaved(int, boolean) */
    public void setInterleaved(int imageIndex, boolean interleaved) {
      dMeta.setInterleaved(imageIndex, interleaved);
    }

    /* @see ome.scifio.DatasetMetadata#setIndexed(int, boolean) */
    public void setIndexed(int imageIndex, boolean indexed) {
      dMeta.setIndexed(imageIndex, indexed);
    }

    /* @see ome.scifio.DatasetMetadata#setFalseColor(int, boolean) */
    public void setFalseColor(int imageIndex, boolean falseC) {
      dMeta.setFalseColor(imageIndex, falseC);
    }

    /* @see ome.scifio.DatasetMetadata#setMetadataComplete(int, boolean) */
    public void setMetadataComplete(int imageIndex, boolean metadataComplete) {
      dMeta.setMetadataComplete(imageIndex, metadataComplete);
    }

    /* @see ome.scifio.DatasetMetadata#setImageMetadata(int, java.util.Hashtable) */
    public void setImageMetadata(int imageIndex, Hashtable<String, Object> meta)
    {
      dMeta.setImageMetadata(imageIndex, meta);
    }

    /* @see ome.scifio.DatasetMetadata#setThumbnailImage(int, boolean) */
    public void setThumbnailImage(int imageIndex, boolean thumbnail) {
      dMeta.setThumbnailImage(imageIndex, thumbnail);
    }

    /* @see ome.scifio.DatasetMetadata#setAxisTypes(int, net.imglib2.meta.AxisType[]) */
    public void setAxisTypes(int imageIndex, AxisType[] axisTypes) {
      dMeta.setAxisTypes(imageIndex, axisTypes);
    }

    /* @see ome.scifio.DatasetMetadata#setAxisType(int, int, net.imglib2.meta.AxisType) */
    public void setAxisType(int imageIndex, int axisIndex, AxisType axis) {
      dMeta.setAxisType(imageIndex, axisIndex, axis);
    }

    /* @see ome.scifio.DatasetMetadata#setAxisLengths(int, int[]) */
    public void setAxisLengths(int imageIndex, int[] axisLengths) {
      dMeta.setAxisLengths(imageIndex, axisLengths);
    }

    /* @see ome.scifio.DatasetMetadata#setAxisLength(int, net.imglib2.meta.AxisType, int) */
    public void setAxisLength(int imageIndex, AxisType axis, int length) {
      dMeta.setAxisLength(imageIndex, axis, length);
    }

    /* @see ome.scifio.DatasetMetadata#resetMeta() */
    public void resetMeta() {
      dMeta.resetMeta();
    }

    /* @see ome.scifio.DatasetMetadata#getImageMetadata() */
    public Collection<ImageMetadata> getImageMetadata() {
      return dMeta.getImageMetadata();
    }

    /* @see ome.scifio.DatasetMetadata#add(ome.scifio.ImageMetadata) */
    public void add(ImageMetadata meta) {
      dMeta.add(meta);
    }

    /* @see java.lang.Object#equals(java.lang.Object) */
    public boolean equals(Object obj) {
      return dMeta.equals(obj);
    }

    /* @see ome.scifio.DatasetMetadata#get8BitLookupTable(int) */
    public byte[][] get8BitLookupTable(int imageIndex)
      throws FormatException, IOException
    {
      return dMeta.get8BitLookupTable(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#get16BitLookupTable(int) */
    public short[][] get16BitLookupTable(int imageIndex)
      throws FormatException, IOException
    {
      return dMeta.get16BitLookupTable(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getAxisCount(int) */
    public int getAxisCount(int imageIndex) {
      return dMeta.getAxisCount(imageIndex);
    }

    /* @see ome.scifio.DatasetMetadata#getAxisIndex(int, net.imglib2.meta.AxisType) */
    public int getAxisIndex(int imageIndex, AxisType type) {
      return dMeta.getAxisIndex(imageIndex, type);
    }

    /* @see java.lang.Object#hashCode() */
    public int hashCode() {
      return dMeta.hashCode();
    }

    /* @see ome.scifio.AbstractMetadata#isFiltered() */
    public boolean isFiltered() {
      return dMeta.isFiltered();
    }

    /* @see ome.scifio.DatasetMetadata#isFalseColor(int) */
    public boolean isFalseColor(int imageIndex) {
      return dMeta.isFalseColor(imageIndex);
    }
  }
}

