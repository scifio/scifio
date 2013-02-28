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
import org.scijava.plugin.SortablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loci.common.adapter.RandomAccessInputStreamAdapter;
import loci.legacy.adapter.Wrapper;
import loci.legacy.adapter.AdapterTools;
import ome.scifio.BufferedImagePlane;
import ome.scifio.ByteArrayPlane;
import ome.scifio.DatasetMetadata;
import ome.scifio.Format;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.io.RandomAccessInputStream;

/**
 * This class is an adapter from ome.scifio.Reader for loci.formats.IFormatReader.
 * Using a "hasa" relationship, this class can be wrap an IFormatReader and be
 * passed to ome.scifio.* methods requiring an ome.scifio.Reader and allow
 * calculations to proceed as normal.
 * 
 * This eliminates the need for redundant method signatures. Instead, the
 * adapter class can be used for direct delegation.
 * 
 * Note that not every method in ome.scifio.Reader has a direct analog in
 * IFormatReader.
 * 
 * Unsupported methods:
 * - getFormat()
 * - getStream()
 * - setMetadata(ome.scifio.Metadata)
 * - getMetadata()
 * - readPlane(RandomAccessInputStream s, int imageIndex, int x,
 *     int y, int w, int h, byte[] buf)
 * - readlane(RandomAccessInputStream s, int imageIndex, int x,
 *   int y, int w, int h, int scanlinePad, byte[] buf)
 * 
 * @author Mark Hiner
 */
public class SCIFIOReaderWrapper extends SortablePlugin implements ome.scifio.Reader, Wrapper<IFormatReader> {
  
  // -- Constants --
  
  protected static final Logger LOGGER =
      LoggerFactory.getLogger(ome.scifio.Reader.class);
  
  // -- Fields --
  
  private final WeakReference<IFormatReader> reader;
  
  // this reference allows the list of CoreMetadata returned by the wrapped reader
  // to exist beyond the scope of a getDatasetMetadata() call, if the reader
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
  
  public IFormatReader unwrap() {
    return reader.get();
  }

  // -- ome.scifio.Reader API --
  
  public void setFormat(Format format) {
    throw new UnsupportedOperationException();
  }
  
  public Format getFormat() {
    throw new UnsupportedOperationException();
  }

  public Plane openPlane(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    Plane plane = null;
    DatasetMetadata m = getDatasetMetadata();
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
      DatasetMetadata m = getDatasetMetadata();
      bp.populate(m.get(imageIndex), buf, 0, 0, 
          m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    }
    
    return bp;
  }

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
      DatasetMetadata m = getDatasetMetadata();
      bp.populate(m.get(imageIndex), buf, 0, 0, 
          m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    }
    
    return bp;
  }

  public Plane openPlane(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    ByteArrayPlane bp = new ByteArrayPlane(getContext());
    DatasetMetadata m = getDatasetMetadata();
    bp.populate(m.get(imageIndex), unwrap().openBytes(planeIndex, x, y, w, h), 0, 0, 
        m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    
    return bp;
  }

  public Plane openThumbPlane(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    ByteArrayPlane bp = new ByteArrayPlane(getContext());
    DatasetMetadata m = getDatasetMetadata();
    bp.populate(m.get(imageIndex), unwrap().openThumbBytes(planeIndex), 0, 0, 
        m.getAxisLength(imageIndex, Axes.X), m.getAxisLength(imageIndex, Axes.Y));
    return bp;
  }

  public void setGroupFiles(boolean group) {
    unwrap().setGroupFiles(group);
  }

  public boolean isGroupFiles() {
    return unwrap().isGroupFiles();
  }

  public int fileGroupOption(String id)
    throws ome.scifio.FormatException, IOException
  {
    return unwrap().fileGroupOption(id);
  }

  public String getCurrentFile() {
    return unwrap().getCurrentFile();
  }

  public String[] getDomains() {
    return unwrap().getDomains();
  }

  public int[] getZCTCoords(int imageIndex, int index) {
    return unwrap().getZCTCoords(index);
  }

  public RandomAccessInputStream getStream() {
    
    RandomAccessInputStream stream = null;
    
    try {
      Field in = unwrap().getClass().getDeclaredField("in");
      in.setAccessible(true);
      loci.common.RandomAccessInputStream legacyStream = 
          (loci.common.RandomAccessInputStream) in.get(reader);
      
      stream = AdapterTools.getAdapter(RandomAccessInputStreamAdapter.class).
              getModern(legacyStream);
      
    } catch (SecurityException e) {
      LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
    } catch (NoSuchFieldException e) {
      LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
    } catch (IllegalArgumentException e) {
      LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
    } catch (IllegalAccessException e) {
      LOGGER.debug("Failed to create RandomAccessIputStream for id: " + getCurrentFile(), e);
    }

    return stream;
  }

  public Reader[] getUnderlyingReaders() {
    IFormatReader[] iReaders = unwrap().getUnderlyingReaders();
    Reader[] sReaders = new Reader[iReaders.length];
    
    for(int i = 0; i < iReaders.length; i++) {
      sReaders[i] = new SCIFIOReaderWrapper(getContext(), iReaders[i]);
    }
    
    return sReaders;
  }

  public int getOptimalTileWidth(int imageIndex) {
    return unwrap().getOptimalTileWidth();
  }

  public int getOptimalTileHeight(int imageIndex) {
    return unwrap().getOptimalTileHeight();
  }

  public void setMetadata(Metadata meta) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Metadata getMetadata() {
    throw new UnsupportedOperationException();
  }

  public DatasetMetadata getDatasetMetadata() {
    // cache the wrapped CoreMetadata list.
    meta = unwrap().getCoreMetadataList();
    
    DatasetMetadata cMeta = AdapterTools.getAdapter(CoreMetadataAdapter.class).
        getModern(meta);
    
    RandomAccessInputStream metaStream = cMeta.getSource();
    
    if (metaStream == null || metaStream.getFileName() == null
        || !metaStream.getFileName().equals(getCurrentFile())) {
      cMeta.setSource(getStream());
      
      if (cMeta.getDatasetName() == null)
        cMeta.setDatasetName(unwrap().getCurrentFile());
    }

    return cMeta;
  }

  public void setNormalized(boolean normalize) {
    unwrap().setNormalized(normalize);
  }

  public boolean isNormalized() {
    return unwrap().isNormalized();
  }

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

  public void setSource(String fileName) throws IOException {
    try {
      unwrap().setId(fileName);
    }
    catch (FormatException e) {
      LOGGER.debug("Format error when creating a RAIS: " + fileName, e);
    }
  }

  public void setSource(RandomAccessInputStream stream) throws IOException {
    try {
      unwrap().setId(stream.getFileName());
    }
    catch (FormatException e) {
      LOGGER.debug("Format error when creating a RAIS: " + stream.getFileName(), e);
    }
  }

  public void close(boolean fileOnly) throws IOException {
    unwrap().close(fileOnly);
  }

  public void close() throws IOException {
    close(false);
  }

  public Plane readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, Plane plane) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  public Plane readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, int scanlinePad, Plane plane) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  public int getPlaneCount(int imageIndex) {
    return unwrap().getImageCount();
  }

  public int getImageCount() {
    return unwrap().getSeriesCount();
  }

  public IFormatReader getReader() {
    return unwrap();
  }

  public Plane createPlane(int xOffset, int yOffset, int xLength, int yLength) {
    throw new UnsupportedOperationException("ReaderWrapper has no associated Plane type");
  }

  public <P extends Plane> P castToTypedPlane(Plane plane) {
    throw new UnsupportedOperationException("ReaderWrapper has no associated Plane type");
  }
}
