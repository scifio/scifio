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

public class SCIFIOReaderAdapter implements ome.scifio.Reader {

  private SCIFIO context;
  private final IFormatReader reader;

  public SCIFIOReaderAdapter(SCIFIO context, IFormatReader reader) {
    this.context = context;
    this.reader = reader;
  }

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
    throw new UnsupportedOperationException();
  }

  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x,
    int y, int w, int h) throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public Object openPlane(int imageIndex, int planeIndex, int x, int y,
    int w, int h) throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] openThumbBytes(int imageIndex, int planeIndex)
    throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public void setGroupFiles(boolean group) {
    throw new UnsupportedOperationException();
  }

  public boolean isGroupFiles() {
    throw new UnsupportedOperationException();
  }

  public int fileGroupOption(String id)
    throws ome.scifio.FormatException, IOException
  {
    throw new UnsupportedOperationException();
  }

  public String getCurrentFile() {
    throw new UnsupportedOperationException();
  }

  public String[] getDomains() {
    throw new UnsupportedOperationException();
  }

  public int[] getZCTCoords(int index) {
    throw new UnsupportedOperationException();
  }

  public RandomAccessInputStream getStream() {
    throw new UnsupportedOperationException();
  }

  public Reader[] getUnderlyingReaders() {
    throw new UnsupportedOperationException();
  }

  public int getOptimalTileWidth(int imageIndex) {
    throw new UnsupportedOperationException();
  }

  public int getOptimalTileHeight(int imageIndex) {
    throw new UnsupportedOperationException();
  }

  public void setMetadata(Metadata meta) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Metadata getMetadata() {
    throw new UnsupportedOperationException();
  }

  public CoreMetadata getCoreMetadata() {
    throw new UnsupportedOperationException();
  }

  public void setNormalized(boolean normalize) {
    throw new UnsupportedOperationException();
  }

  public boolean isNormalized() {
    throw new UnsupportedOperationException();
  }

  public boolean hasCompanionFiles() {
    throw new UnsupportedOperationException();
  }

  public void setSource(File file) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void setSource(String fileName) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void setSource(RandomAccessInputStream stream) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void close(boolean fileOnly) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
    throw new UnsupportedOperationException();
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
    throw new UnsupportedOperationException();
  }

  public int getImageCount() {
    throw new UnsupportedOperationException();
  }
  
}