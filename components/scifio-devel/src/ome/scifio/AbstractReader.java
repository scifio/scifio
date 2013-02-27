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

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;

import org.scijava.plugin.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.FormatException;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;
import ome.scifio.util.SCIFIOMetadataTools;

/**
 * Abstract superclass of all SCIFIO reader components.
 *
 * @author Mark Hiner
 */
/**
 * @author temp
 *
 * @param <M>
 * @param <P>
 */
public abstract class AbstractReader<M extends TypedMetadata, P extends DataPlane<?>>
  extends AbstractHasFormat implements TypedReader<M, P> {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

  // -- Fields --

  /** Type-specific Metadata values. */
  protected M metadata;

  /** Core Metadata values. */
  protected DatasetMetadata dMeta;

  /** Whether or not to group multi-file formats. */
  protected boolean group = true;

  /** Whether or not to normalize float data. */
  protected boolean normalizeData;

  /** Current file. */
  protected RandomAccessInputStream in;

  /** List of domains in which this format is used. */
  protected String[] domains = new String[0];

  /** Name of current file. */
  protected String currentId;

  /** Whether this format supports multi-file datasets. */
  protected boolean hasCompanionFiles = false;
  
  private Class<P> planeClass;

  // -- Constructors --

  /** Constructs a reader with the given context */
  public AbstractReader(Class<P> planeClass)
  {
    this.planeClass = planeClass;
  }

  // -- Reader API Methods --

  //TODO Merge common Reader and Writer API methods

  /*
   * @see ome.scifio.Reader#openPlane(int, int)
   */
  public P openPlane(final int imageIndex, final int planeNumber)
    throws FormatException, IOException
  {
    return openPlane(
      imageIndex, planeNumber, 0, 0, dMeta.getAxisLength(imageIndex, Axes.X),
      dMeta.getAxisLength(imageIndex, Axes.Y));
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, int, int, int, int)
   */
  public P openPlane(final int imageIndex, final int planeIndex,
    final int x, final int y, final int w, final int h)
    throws FormatException, IOException
  {
    final int bpp =
      FormatTools.getBytesPerPixel(dMeta.getPixelType(imageIndex));
    final int ch = dMeta.getRGBChannelCount(imageIndex);
    final P plane = createPlane(x, y, w, h);
    return openPlane(imageIndex, planeIndex, plane, x, y, w, h);
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, ome.scifio.Plane)
   */
  public P openPlane(int imageIndex, int planeIndex, Plane plane)
      throws FormatException, IOException {
    return openPlane(imageIndex, planeIndex, castToTypedPlane(plane));
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, ome.scifio.Plane, int, int, int, int)
   */
  public P openPlane(int imageIndex, int planeIndex, Plane plane, int x,
      int y, int w, int h) throws FormatException, IOException {
    return openPlane(imageIndex, planeIndex, this.<P>castToTypedPlane(plane), x, y, w, h);
  }

  /*
   * @see ome.scifio.Reader#setGroupFiles(boolean)
   */
  public void setGroupFiles(final boolean groupFiles) {
    group = groupFiles;
  }

  /*
   * @see ome.scifio.Reader#isGroupFiles()
   */
  public boolean isGroupFiles() {
    FormatTools.assertStream(in, false, 1);
    return group;
  }

  /*
   * @see ome.scifio.Reader#fileGroupOption(java.lang.String)
   */
  public int fileGroupOption(final String id)
    throws FormatException, IOException
  {
    return FormatTools.CANNOT_GROUP;
  }

  /*
   * @see ome.scifio.Reader#getCurrentFile()
   */
  public String getCurrentFile() {
    return in == null ? null : in.getFileName();
  }

  /*
   * @see ome.scifio.Reader#getDomains()
   */
  public String[] getDomains() {
    return domains;
  }

  /*
   * @see ome.scifio.Reader#getStream()
   */
  public RandomAccessInputStream getStream() {
    return in;
  }

  /*
   * @see ome.scifio.Reader#getUnderlyingReaders()
   */
  public Reader[] getUnderlyingReaders() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * @see ome.scifio.Reader#getOptimalTileWidth(int)
   */
  public int getOptimalTileWidth(final int imageIndex) {
    return dMeta.getAxisLength(imageIndex, Axes.Y);
  }

  /*
   * @see ome.scifio.Reader#getOptimalTileHeight(int)
   */
  public int getOptimalTileHeight(final int imageIndex) {
    final int bpp =
      FormatTools.getBytesPerPixel(dMeta.getPixelType(imageIndex));
    final int maxHeight =
      (1024 * 1024) /
        (dMeta.getAxisLength(imageIndex, Axes.X) *
          dMeta.getRGBChannelCount(imageIndex) * bpp);
    return Math.min(maxHeight, dMeta.getAxisLength(imageIndex, Axes.X));
  }

  /*
   * @see ome.scifio.Reader#setMetadata(ome.scifio.Metadata)
   */
  public void setMetadata(ome.scifio.Metadata meta) throws IOException {
    setMetadata(SCIFIOMetadataTools.<M>castMeta(meta));
  }

  /*
   * @see ome.scifio.Reader#getMetadata()
   */
  public M getMetadata() {
    return metadata;
  }

  /*
   * @see ome.scifio.Reader#getDatasetMetadata()
   */
  public DatasetMetadata getDatasetMetadata() {
    return dMeta;
  }

  /*
   * @see ome.scifio.Reader#setNormalized(boolean)
   */
  public void setNormalized(final boolean normalize) {
    normalizeData = normalize;
  }

  /*
   * @see ome.scifio.Reader#isNormalized()
   */
  public boolean isNormalized() {
    return normalizeData;
  }

  /*
   * @see ome.scifio.Reader#hasCompanionFiles()
   */
  public boolean hasCompanionFiles() {
    return hasCompanionFiles;
  }

  /*
   * @see ome.scifio.Reader#setSource(java.lang.String)
   */
  public void setSource(final String fileName) throws IOException {
    setSource(new RandomAccessInputStream(getContext(), fileName));
  }
  
  /*
   * @see ome.scifio.Reader#setSource(java.io.File)
   */
  public void setSource(final File file) throws IOException {
    setSource(file.getName());
  }

  /*
   * @see ome.scifio.Reader#setSource(ome.scifio.io.RandomAccessInputStream)
   */
  public void setSource(final RandomAccessInputStream stream)
    throws IOException
  {
    in = stream;
    currentId = stream.getFileName();

    if (metadata == null) {
      try {
        @SuppressWarnings("unchecked")
        final M meta = (M) getFormat().createParser().parse(stream);
        setMetadata(meta);
      }
      catch (final FormatException e) {
        throw new IOException(e);
      }
    }
  }

  /*
   * @see ome.scifio.Reader#close(boolean)
   */
  public void close(final boolean fileOnly) throws IOException {
    if (in != null) in.close();
    if (!fileOnly) {
      in = null;
    }
  }

  /*
   * @see ome.scifio.Reader#close()
   */
  public void close() throws IOException {
    close(false);
  }

  /*
   * @see ome.scifio.Reader#readPlane(ome.scifio.io.RandomAccessInputStream, int, int, int, int, int, ome.scifio.Plane)
   */
  public Plane readPlane(RandomAccessInputStream s, int imageIndex, int x,
      int y, int w, int h, Plane plane) throws IOException {
    return readPlane(s, imageIndex, x, y, w, h, this.<P>castToTypedPlane(plane));
  }

  /*
   * @see ome.scifio.Reader#readPlane(ome.scifio.io.RandomAccessInputStream, int, int, int, int, int, int, ome.scifio.Plane)
   */
  public Plane readPlane(RandomAccessInputStream s, int imageIndex, int x,
      int y, int w, int h, int scanlinePad, Plane plane) throws IOException {
    return readPlane(s, imageIndex, x, y, w, h, scanlinePad, this.<P>castToTypedPlane(plane));
  }

  /*
   * @see ome.scifio.Reader#getPlaneCount(int)
   */
  public int getPlaneCount(final int imageIndex) {
    return dMeta.getPlaneCount(imageIndex);
  }

  /*
   * @see ome.scifio.Reader#getImageCount()
   */
  public int getImageCount() {
    return dMeta.getImageCount();
  }
  
  /*
   * @see ome.scifio.Reader#castToTypedPlane(ome.scifio.Plane)
   */
  public <T extends Plane> T castToTypedPlane(Plane plane) {
    if(!planeClass.isAssignableFrom(plane.getClass())) {
      throw new IllegalArgumentException("Incompatible plane types. " +
          "Attempted to cast: " + plane.getClass() + " to: " + planeClass);
    }
      
    @SuppressWarnings("unchecked")
    T p = (T)plane;
    return p;
  }
  
  // -- TypedReader API --

  /*
   * @see ome.scifio.TypedReader#openPlane(int, int, ome.scifio.DataPlane)
   */
  public P openPlane(final int imageIndex, final int planeIndex,
    final P plane) throws FormatException, IOException
  {
    return openPlane(
      imageIndex, planeIndex, plane, 0, 0,
      dMeta.getAxisLength(imageIndex, Axes.X),
      dMeta.getAxisLength(imageIndex, Axes.Y));
  }
  
  /*
   * @see ome.scifio.TypedReader#setMetadata(ome.scifio.TypedMetadata)
   */
  public void setMetadata(final M meta) throws IOException {
    metadata = meta;
    //FIXME: get rid of datasetmetadata class
    dMeta = getContext().getService(PluginService.class).createInstancesOfType(DatasetMetadata.class).get(0);
    dMeta.setSource(meta.getSource());
    
    if(in == null) setSource(meta.getSource());
    
    Translator t = getContext().getService(SCIFIO.class).
                    translators().findTranslator(meta, dMeta);
    
    t.translate(meta, dMeta);
  }
  
  /*
   * @see ome.scifio.TypedReader#readPlane(ome.scifio.io.RandomAccessInputStream, int, int, int, int, int, ome.scifio.DataPlane)
   */
  public P readPlane(final RandomAccessInputStream s,
    final int imageIndex, final int x, final int y, final int w, final int h,
    final P plane) throws IOException
  {
    return readPlane(s, imageIndex, x, y, w, h, 0, plane);
  }

  /*
   * @see ome.scifio.TypedReader#readPlane(ome.scifio.io.RandomAccessInputStream, int, int, int, int, int, int, ome.scifio.DataPlane)
   */
  public P readPlane(final RandomAccessInputStream s,
    final int imageIndex, final int x, final int y, final int w, final int h,
    final int scanlinePad, final P plane) throws IOException
  {
    final int c = dMeta.getRGBChannelCount(imageIndex);
    final int bpp =
      FormatTools.getBytesPerPixel(dMeta.getPixelType(imageIndex));
    
    byte[] bytes = plane.getBytes();
    
    if (x == 0 && y == 0 && w == dMeta.getAxisLength(imageIndex, Axes.X) &&
      h == dMeta.getAxisLength(imageIndex, Axes.Y) && scanlinePad == 0)
    {
      s.read(bytes);
    }
    else if (x == 0 && w == dMeta.getAxisLength(imageIndex, Axes.Y) &&
      scanlinePad == 0)
    {
      if (dMeta.isInterleaved(imageIndex)) {
        s.skipBytes(y * w * bpp * c);
        s.read(bytes, 0, h * w * bpp * c);
      }
      else {
        final int rowLen = w * bpp;
        for (int channel = 0; channel < c; channel++) {
          s.skipBytes(y * rowLen);
          s.read(bytes, channel * h * rowLen, h * rowLen);
          if (channel < c - 1) {
            // no need to skip bytes after reading final channel
            s.skipBytes((dMeta.getAxisLength(imageIndex, Axes.Y) - y - h) *
              rowLen);
          }
        }
      }
    }
    else {
      final int scanlineWidth =
        dMeta.getAxisLength(imageIndex, Axes.Y) + scanlinePad;
      if (dMeta.isInterleaved(imageIndex)) {
        s.skipBytes(y * scanlineWidth * bpp * c);
        for (int row = 0; row < h; row++) {
          s.skipBytes(x * bpp * c);
          s.read(bytes, row * w * bpp * c, w * bpp * c);
          if (row < h - 1) {
            // no need to skip bytes after reading final row
            s.skipBytes(bpp * c * (scanlineWidth - w - x));
          }
        }
      }
      else {
        for (int channel = 0; channel < c; channel++) {
          s.skipBytes(y * scanlineWidth * bpp);
          for (int row = 0; row < h; row++) {
            s.skipBytes(x * bpp);
            s.read(bytes, channel * w * h * bpp + row * w * bpp, w * bpp);
            if (row < h - 1 || channel < c - 1) {
              // no need to skip bytes after reading final row of final channel
              s.skipBytes(bpp * (scanlineWidth - w - x));
            }
          }
          if (channel < c - 1) {
            // no need to skip bytes after reading final channel
            s.skipBytes(scanlineWidth * bpp *
              (dMeta.getAxisLength(imageIndex, Axes.Y) - y - h));
          }
        }
      }
    }
    return plane;
  }
    
  /*
   * @see ome.scifio.TypedReader#getPlaneClass()
   */
  public Class<P> getPlaneClass() {
    return planeClass;
  }
}
