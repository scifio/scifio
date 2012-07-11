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

import java.io.File;
import java.io.IOException;

import ome.scifio.CoreMetadata;
import ome.scifio.Format;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
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
public class SCIFIOReaderAdapter implements ome.scifio.Reader {
  
  // -- Constants --
  
  // -- Fields --
  
  private SCIFIO context;
  private final IFormatReader reader;

  // -- Constructor --
  
  public SCIFIOReaderAdapter(SCIFIO context, IFormatReader reader) {
    this.context = context;
    this.reader = reader;
  }

  // -- ome.scifio.Reader API --
  
  public SCIFIO getContext() {
    return context;
  }

  public void setContext(SCIFIO ctx) {
    context = ctx;
  }

  public Format<?, ?, ?, ?, ?> getFormat() {
    throw new UnsupportedOperationException();
  }

  public byte[] openBytes(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    return reader.openBytes(imageIndex);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    return reader.openBytes(imageIndex, x, y, w, h);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws ome.scifio.FormatException, IOException
  {
    return reader.openBytes(imageIndex, buf);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x,
    int y, int w, int h) throws ome.scifio.FormatException, IOException
  {
    return reader.openBytes(imageIndex, buf, x, y, w, h);
  }

  public Object openPlane(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    return reader.openPlane(imageIndex, x, y, w, h);
  }

  public byte[] openThumbBytes(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    return reader.openThumbBytes(imageIndex);
  }

  public void setGroupFiles(boolean group) {
    reader.setGroupFiles(group);
  }

  public boolean isGroupFiles() {
    return reader.isGroupFiles();
  }

  public int fileGroupOption(String id)
    throws ome.scifio.FormatException, IOException
  {
    return reader.fileGroupOption(id);
  }

  public String getCurrentFile() {
    return reader.getCurrentFile();
  }

  public String[] getDomains() {
    return reader.getDomains();
  }

  public int[] getZCTCoords(int index) {
    return reader.getZCTCoords(index);
  }

  public RandomAccessInputStream getStream() {
    throw new UnsupportedOperationException();
  }

  public Reader[] getUnderlyingReaders() {
    IFormatReader[] iReaders = reader.getUnderlyingReaders();
    Reader[] sReaders = new Reader[iReaders.length];
    
    for(int i = 0; i < iReaders.length; i++) {
      sReaders[i] = new SCIFIOReaderAdapter(context, iReaders[i]);
    }
    
    return sReaders;
  }

  public int getOptimalTileWidth(int imageIndex) {
    return reader.getOptimalTileWidth();
  }

  public int getOptimalTileHeight(int imageIndex) {
    return reader.getOptimalTileHeight();
  }

  public void setMetadata(Metadata meta) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Metadata getMetadata() {
    throw new UnsupportedOperationException();
  }

  public CoreMetadata getCoreMetadata() {
    return reader.getCoreMetadata()[0].convert();
  }

  public void setNormalized(boolean normalize) {
    reader.setNormalized(normalize);
  }

  public boolean isNormalized() {
    return reader.isNormalized();
  }

  public boolean hasCompanionFiles() {
    return reader.hasCompanionFiles();
  }

  public void setSource(File file) throws IOException {
    try {
      reader.setId(file.getAbsolutePath());
    }
    catch (FormatException e) {
      throw new IOException(e);
    }
  }

  public void setSource(String fileName) throws IOException {
    try {
      reader.setId(fileName);
    }
    catch (FormatException e) {
      throw new IOException(e);
    }
  }

  public void setSource(RandomAccessInputStream stream) throws IOException {
    try {
      reader.setId(stream.getFileName());
    }
    catch (FormatException e) {
      throw new IOException(e);
    }
  }

  public void close(boolean fileOnly) throws IOException {
    reader.close(fileOnly);
  }

  public void close() throws IOException {
    reader.close();
  }

  public byte[] readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, byte[] buf) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, int scanlinePad, byte[] buf) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  public int getPlaneCount(int imageIndex) {
    return (reader.getSizeC() * reader.getSizeT() * reader.getSizeZ());
  }

  public int getImageCount() {
    return reader.getImageCount();
  }
  
}