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
package loci.formats;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import net.imglib2.meta.Axes;

import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loci.legacy.adapter.CommonAdapter;
import loci.legacy.adapter.Wrapper;

import ome.scifio.AbstractHasSCIFIO;
import ome.scifio.BufferedImagePlane;
import ome.scifio.ByteArrayPlane;
import ome.scifio.Format;
import ome.scifio.ImageMetadata;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.io.RandomAccessInputStream;

/**
 * This class is an adapter from ome.scifio.Reader for loci.formats.IFormatReader.
 * Using a "hasa" relationship, this class can rap an IFormatReader and be
 * passed to ome.scifio.* methods requiring an ome.scifio.Reader, allowing
 * calculations to proceed as normal.
 * <p> 
 * This eliminates the need for redundant method signatures. Instead, the
 * adapter class can be used for direct delegation.
 * </p>
 * <p>
 * Note that not every method in ome.scifio.Reader has a direct analog in
 * IFormatReader.
 * </p>
 * Unsupported methods:
 * <ul>
 * <li>
 * {@link #getFormat()}
 * </li>
 * <li>
 * {@link #setMetadata(ome.scifio.Metadata)}
 * </li>
 * <li>
 * {@link #readPlane(RandomAccessInputStream s, int imageIndex, int x,
 * int y, int w, int h, byte[] buf)}
 * </li>
 * <li>
 * {@link #readlane(RandomAccessInputStream s, int imageIndex, int x,
 * int y, int w, int h, int scanlinePad, byte[] buf)}
 * </li>
 * </ul>
 * 
 * @author Mark Hiner
 */
public class SCIFIOReaderWrapper extends AbstractHasSCIFIO
  implements ome.scifio.Reader, Wrapper<IFormatReader>
{
  
  // -- Constants --
  
  protected static final Logger LOGGER =
      LoggerFactory.getLogger(ome.scifio.Reader.class);
  
  // -- Fields --
  
  private final WeakReference<IFormatReader> reader;
  
  private WeakReference<RandomAccessInputStream> stream;
  
  // this reference allows the list of CoreMetadata returned by the wrapped reader
  // to exist beyond the scope of a getMetadata() call, if the reader
  // type dynamically generates its CoreMetadata list. This field will still
  // be garbage collected with this wrapper, allowing its wrapper to
  // eventually be GC'd as well.
  private List<CoreMetadata> meta;

  // -- Constructor --
  
  public SCIFIOReaderWrapper(Context context, IFormatReader reader) {
    setContext(context);
    this.reader = new WeakReference<IFormatReader>(reader);
  }
  
  // -- Wrapper API Methods --
  
  /*
   * @see loci.legacy.adapter.Wrapper#unwrap()
   */
  public IFormatReader unwrap() {
    return reader.get();
  }

  // -- ome.scifio.Reader API --
  
  /*
   * @see ome.scifio.HasFormat#getFormat()
   */
  public Format getFormat() {
    throw new UnsupportedOperationException();
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    Plane plane = null;
    Metadata m = getMetadata();
    Object o = unwrap().openPlane(planeIndex, 0, 0, m.getAxisLength(imageIndex, Axes.X),
        m.getAxisLength(imageIndex, Axes.Y));

    if(BufferedImage.class.isAssignableFrom(o.getClass())) {
      plane = new BufferedImagePlane(getContext());
      ((BufferedImagePlane)plane).populate((BufferedImage)o, 0, 0, m.getAxisLength(imageIndex, Axes.X),
          m.getAxisLength(imageIndex, Axes.Y));
    }
    else {
      plane = new ByteArrayPlane(getContext());
      ((ByteArrayPlane)plane).populate((byte[])o, 0, 0, m.getAxisLength(imageIndex, Axes.X),
          m.getAxisLength(imageIndex, Axes.Y));
    }
      
    return plane;
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, ome.scifio.Plane)
   */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane)
    throws ome.scifio.FormatException, IOException
  {
    byte[] buf = null;
    ByteArrayPlane bp = null;
    if (ByteArrayPlane.class.isAssignableFrom(plane.getClass())) {
      bp = (ByteArrayPlane)plane;
      buf = bp.getData();
      unwrap().openBytes(imageIndex, buf);
    }
    else {
      bp = new ByteArrayPlane(getContext());
      buf = unwrap().openBytes(imageIndex);
      Metadata m = getMetadata();
      bp.populate(m.get(imageIndex), buf, 0, 0, 
          m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    }
    
    return bp;
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, ome.scifio.Plane, int, int, int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex, Plane plane, int x,
    int y, int w, int h) throws ome.scifio.FormatException, IOException
  {
    byte[] buf = null;
    ByteArrayPlane bp = null;
    if (ByteArrayPlane.class.isAssignableFrom(plane.getClass())) {
      bp = (ByteArrayPlane)plane;
      buf = bp.getData();
      unwrap().openBytes(imageIndex, buf, x, y, w, h);
    }
    else {
      bp = new ByteArrayPlane(getContext());
      buf = unwrap().openBytes(imageIndex, x, y, w, h);
      Metadata m = getMetadata();
      bp.populate(m.get(imageIndex), buf, 0, 0, 
          m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    }
    
    return bp;
  }

  /*
   * @see ome.scifio.Reader#openPlane(int, int, int, int, int, int)
   */
  public Plane openPlane(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    ByteArrayPlane bp = new ByteArrayPlane(getContext());
    Metadata m = getMetadata();
    bp.populate(m.get(imageIndex), unwrap().openBytes(planeIndex, x, y, w, h), 0, 0, 
        m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    
    return bp;
  }

  /*
   * @see ome.scifio.Reader#openThumbPlane(int, int)
   */
  public Plane openThumbPlane(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    ByteArrayPlane bp = new ByteArrayPlane(getContext());
    Metadata m = getMetadata();
    bp.populate(m.get(imageIndex), unwrap().openThumbBytes(planeIndex), 0, 0, 
        m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    return bp;
  }

  /*
   * @see ome.scifio.Reader#setGroupFiles(boolean)
   */
  public void setGroupFiles(boolean group) {
    unwrap().setGroupFiles(group);
  }

  /*
   * @see ome.scifio.Reader#isGroupFiles()
   */
  public boolean isGroupFiles() {
    return unwrap().isGroupFiles();
  }

  /*
   * @see ome.scifio.Reader#fileGroupOption(java.lang.String)
   */
  public int fileGroupOption(String id)
    throws ome.scifio.FormatException, IOException
  {
    return unwrap().fileGroupOption(id);
  }

  /*
   * @see ome.scifio.Reader#getCurrentFile()
   */
  public String getCurrentFile() {
    return unwrap().getCurrentFile();
  }

  /*
   * @see ome.scifio.Reader#getDomains()
   */
  public String[] getDomains() {
    return unwrap().getDomains();
  }

  /*
   * @see ome.scifio.Reader#getStream()
   */
  public RandomAccessInputStream getStream() {
    
    if (stream == null || stream.get() == null) {

      try {
        FormatReader fReader = null;

        // Nneed to get a FormatReader reference to the wrapped Reader
        if (ReaderWrapper.class.isAssignableFrom(unwrap().getClass())) {
          fReader = (FormatReader) ((ReaderWrapper)unwrap()).unwrap(FormatReader.class, null);
        }
        else if (FormatReader.class.isAssignableFrom(unwrap().getClass())) {
          fReader = (FormatReader)unwrap();
        }

        if (fReader == null) return null;

        // Use reflection to extract the RandomAccessInputStream from the underlying reader
        Field in = FormatReader.class.getDeclaredField("in");
        in.setAccessible(true);
        loci.common.RandomAccessInputStream legacyStream = 
            (loci.common.RandomAccessInputStream) in.get(fReader);
        in.setAccessible(false);
        
        if (legacyStream == null) return null;

        // Cache the reference if we found one, to avoid repeating this process in the future.
        // Use a weak reference to avoid garbage collection issues.
        stream = new WeakReference<RandomAccessInputStream>(
            CommonAdapter.get(legacyStream));

      } catch (SecurityException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      } catch (NoSuchFieldException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      } catch (IllegalArgumentException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      } catch (IllegalAccessException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      } catch (FormatException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      } catch (IOException e) {
        LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
      }
    }
    
    return stream.get();
  }

  /*
   * @see ome.scifio.Reader#getUnderlyingReaders()
   */
  public Reader[] getUnderlyingReaders() {
    IFormatReader[] iReaders = unwrap().getUnderlyingReaders();
    Reader[] sReaders = new Reader[iReaders.length];
    
    for(int i = 0; i < iReaders.length; i++) {
      sReaders[i] = new SCIFIOReaderWrapper(getContext(), iReaders[i]);
    }
    
    return sReaders;
  }

  /*
   * @see ome.scifio.Reader#getOptimalTileWidth(int)
   */
  public int getOptimalTileWidth(int imageIndex) {
    return unwrap().getOptimalTileWidth();
  }

  /*
   * @see ome.scifio.Reader#getOptimalTileHeight(int)
   */
  public int getOptimalTileHeight(int imageIndex) {
    return unwrap().getOptimalTileHeight();
  }

  /*
   * @see ome.scifio.Reader#setMetadata(ome.scifio.Metadata)
   */
  public void setMetadata(Metadata meta) throws IOException {
    throw new UnsupportedOperationException();
  }

  /*
   * @see ome.scifio.Reader#getMetadata()
   */
  public Metadata getMetadata() {
    // cache the wrapped CoreMetadata list.
    meta = unwrap().getCoreMetadataList();
    
    Metadata cMeta = FormatAdapter.get(meta);
    
    RandomAccessInputStream metaStream = cMeta.getSource();
    
    if (metaStream == null || metaStream.getFileName() == null
        || !metaStream.getFileName().equals(getCurrentFile())) {
      cMeta.setSource(getStream());
      
      if (cMeta.getDatasetName() == null)
        cMeta.setDatasetName(unwrap().getCurrentFile());
    }

    return cMeta;
  }

  /*
   * @see ome.scifio.Reader#setNormalized(boolean)
   */
  public void setNormalized(boolean normalize) {
    unwrap().setNormalized(normalize);
  }

  /*
   * @see ome.scifio.Reader#isNormalized()
   */
  public boolean isNormalized() {
    return unwrap().isNormalized();
  }

  /*
   * @see ome.scifio.Reader#hasCompanionFiles()
   */
  public boolean hasCompanionFiles() {
    return unwrap().hasCompanionFiles();
  }

  public void setSource(File file) throws IOException {
    try {
      unwrap().setId(file.getAbsolutePath());
    }
    catch (FormatException e) {
      LOGGER.debug("Format error when creating a RAIS: " + file.getAbsolutePath(), e);
    }
  }

  /*
   * @see ome.scifio.Reader#setSource(java.lang.String)
   */
  public void setSource(String fileName) throws IOException {
    try {
      unwrap().setId(fileName);
    }
    catch (FormatException e) {
      LOGGER.debug("Format error when creating a RAIS: " + fileName, e);
    }
  }

  /*
   * @see ome.scifio.Reader#setSource(ome.scifio.io.RandomAccessInputStream)
   */
  public void setSource(RandomAccessInputStream stream) throws IOException {
    try {
      unwrap().setId(stream.getFileName());
    }
    catch (FormatException e) {
      LOGGER.debug("Format error when creating a RAIS: " + stream.getFileName(), e);
    }
  }

  /*
   * @see ome.scifio.Reader#close(boolean)
   */
  public void close(boolean fileOnly) throws IOException {
    unwrap().close(fileOnly);
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
    int y, int w, int h, Plane plane) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  /*
   * @see ome.scifio.Reader#readPlane(
   * ome.scifio.io.RandomAccessInputStream, int, int, int, int, int, int, ome.scifio.Plane)
   */
  public Plane readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, int scanlinePad, Plane plane) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  /*
   * @see ome.scifio.Reader#getPlaneCount(int)
   */
  public int getPlaneCount(int imageIndex) {
    return unwrap().getImageCount();
  }

  /*
   * @see ome.scifio.Reader#getImageCount()
   */
  public int getImageCount() {
    return unwrap().getSeriesCount();
  }

  /*
   * @see ome.scifio.Reader#createPlane(int, int, int, int)
   */
  public Plane createPlane(int xOffset, int yOffset, int xLength, int yLength) {
    throw new UnsupportedOperationException("ReaderWrapper has no associated Plane type");
  }
  
  /*
   * @see ome.scifio.Reader#createPlane(ome.scifio.ImageMetadata, int, int, int, int)
   */
  public Plane createPlane(ImageMetadata meta, int xOffset, int yOffset, int xLength, int yLength) {
    throw new UnsupportedOperationException("ReaderWrapper has no associated Plane type");
  }

  /*
   * @see ome.scifio.Reader#castToTypedPlane(ome.scifio.Plane)
   */
  public <P extends Plane> P castToTypedPlane(Plane plane) {
    throw new UnsupportedOperationException("ReaderWrapper has no associated Plane type");
  }
  
  // -- Groupable API Methods --

  /*
   * @see ome.scifio.Groupable#isSingleFile(java.lang.String)
   */
  public boolean isSingleFile(String id) throws ome.scifio.FormatException,
      IOException {
    return unwrap().isSingleFile(id);
  }
}
