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

package ome.scifio;

import java.io.File;
import java.io.IOException;

import ome.scifio.io.RandomAccessInputStream;

/**
 * DelegateReader is a file format reader that selects which reader to use
 * for a format if there are two readers which handle the same format.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/DelegateReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/DelegateReader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public abstract class DelegateReader <M extends Metadata, P extends Plane> 
  extends AbstractReader<M, P> {

  /** Flag indicating whether to use legacy reader by default. */
  protected boolean useLegacy;

  /** Native reader. */
  protected Reader<M, P> nativeReader;

  /** Legacy reader. */
  protected Reader<M, P> legacyReader;

  /** Flag indicating that the native reader was successfully initialized. */
  protected boolean nativeReaderInitialized;

  /** Flag indicating that the legacy reader was successfully initialized. */
  protected boolean legacyReaderInitialized;

  // -- Constructor --

  /** Constructs a new delegate reader. */
  public DelegateReader() {
    this(null, null, null);
  }

  /** Constructs a new delegate reader. */
  public DelegateReader(Reader<M, P> nativeReader, Reader<M, P> legacyReader,
    final SCIFIO ctx) {
    super(ctx);
    this.nativeReader = nativeReader;
    this.legacyReader = legacyReader;
  }

  // -- DelegateReader API methods --

  /** Sets whether to use the legacy reader by default. */
  public void setLegacy(boolean legacy) { useLegacy = legacy; }

  /** Gets whether to use the legacy reader by default. */
  public boolean isLegacy() { return useLegacy; }
  
  // -- HasContext API methods --
  
  public SCIFIO getContext() {
    return useLegacy ? legacyReader.getContext() : nativeReader.getContext();
  }

  public void setContext(SCIFIO ctx) {
    if (useLegacy) legacyReader.setContext(ctx);
    else nativeReader.setContext(ctx);
  }
  
  // -- HasFormat API methods --
  
  public Format<M, ?, ?, ?, ?> getFormat() {
    return (Format<M, ?, ?, ?, ?>) (useLegacy ? legacyReader.getFormat() : nativeReader.getFormat());
  }
  
  // -- Reader API methods --

  public P openPlane(int imageIndex, int planeIndex)
    throws FormatException, IOException
  {
    return useLegacy ? legacyReader.openPlane(imageIndex, planeIndex)
      : nativeReader.openPlane(imageIndex, planeIndex);
  }

  public P openPlane(int imageIndex, int planeIndex, int x, int y, int w,
    int h) throws FormatException, IOException
  {
    return useLegacy ? legacyReader.openPlane(imageIndex, planeIndex, x, y, w, h) 
      : nativeReader.openPlane(imageIndex, planeIndex, x, y, w, h);
  }

  public P openPlane(int imageIndex, int planeIndex, P plane)
    throws FormatException, IOException
  {
    return useLegacy ? legacyReader.openPlane(imageIndex, planeIndex, plane) 
      : nativeReader.openPlane(imageIndex, planeIndex, plane);
  }

  public P openPlane(int imageIndex, int planeIndex, P plane, int x,
    int y, int w, int h) throws FormatException, IOException
  {
    return useLegacy ? legacyReader.openPlane(imageIndex, planeIndex, plane, x, y, w, h) :
      nativeReader.openPlane(imageIndex, planeIndex, plane, x, y, w, h);
  }

  public P openThumbPlane(int imageIndex, int planeIndex)
    throws FormatException, IOException
  {
    return useLegacy ? legacyReader.openThumbPlane(imageIndex, planeIndex) 
      : nativeReader.openThumbPlane(imageIndex, planeIndex);
  }

  public void setGroupFiles(boolean group) {
    if (useLegacy) legacyReader.setGroupFiles(group);
    else nativeReader.setGroupFiles(group);
  }

  public boolean isGroupFiles() {
    return useLegacy ? legacyReader.isGroupFiles() : nativeReader.isGroupFiles();
  }

  public int fileGroupOption(String id) throws FormatException, IOException {
    return useLegacy ? legacyReader.fileGroupOption(id) : nativeReader.fileGroupOption(id);
  }

  public String getCurrentFile() {
    return useLegacy ? legacyReader.getCurrentFile() : nativeReader.getCurrentFile();
  }

  public String[] getDomains() {
    return useLegacy ? legacyReader.getDomains() : nativeReader.getDomains();
  }

  public RandomAccessInputStream getStream() {
    return useLegacy ? legacyReader.getStream() : nativeReader.getStream();
  }

  public Reader<? extends Metadata, ? extends Plane>[] getUnderlyingReaders() {
    return useLegacy ? legacyReader.getUnderlyingReaders() : nativeReader.getUnderlyingReaders();
  }

  public int getOptimalTileWidth(int imageIndex) {
    return useLegacy ? legacyReader.getOptimalTileWidth(imageIndex)
      : nativeReader.getOptimalTileWidth(imageIndex);
  }

  public int getOptimalTileHeight(int imageIndex) {
    return useLegacy ? legacyReader.getOptimalTileHeight(imageIndex)
      : nativeReader.getOptimalTileHeight(imageIndex);
  }

  public void setMetadata(M meta) throws IOException {
    if (useLegacy) legacyReader.setMetadata(meta);
    else  nativeReader.setMetadata(meta);
  }

  public M getMetadata() {
    return useLegacy ? legacyReader.getMetadata() : nativeReader.getMetadata();
  }

  public DatasetMetadata<?> getDatasetMetadata() {
    return useLegacy ? legacyReader.getDatasetMetadata() : nativeReader.getDatasetMetadata();
  }

  public void setNormalized(boolean normalize) {
    if (useLegacy) legacyReader.setNormalized(normalize);
    else nativeReader.setNormalized(normalize);
  }

  public boolean isNormalized() {
    return useLegacy ? legacyReader.isNormalized() : nativeReader.isNormalized();
  }

  public boolean hasCompanionFiles() {
    return useLegacy ? legacyReader.hasCompanionFiles() : nativeReader.hasCompanionFiles();
  }

  public void setSource(File file) throws IOException {
    if (useLegacy) legacyReader.setSource(file);
    else nativeReader.setSource(file);
  }

  public void setSource(String fileName) throws IOException {
    if (useLegacy) legacyReader.setSource(fileName);
    else nativeReader.setSource(fileName);
  }

  public void setSource(RandomAccessInputStream stream) throws IOException {
    if (useLegacy) legacyReader.setSource(stream);
    else nativeReader.setSource(stream);
  }

  public void close(boolean fileOnly) throws IOException {
    if (useLegacy) legacyReader.close(fileOnly);
    else nativeReader.close(fileOnly);
  }

  public void close() throws IOException {
    if (useLegacy) legacyReader.close();
    else nativeReader.close();
  }

  public P readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, P plane) throws IOException
  {
    return useLegacy ? legacyReader.readPlane(s, imageIndex, x, y, w, h, plane)
      : nativeReader.readPlane(s, imageIndex, x, y, w, h, plane);
  }

  public P readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, int scanlinePad, P plane) throws IOException
  {
    return useLegacy ? legacyReader.readPlane(s, imageIndex, x, y, w, h, scanlinePad, plane)
      : nativeReader.readPlane(s, imageIndex, x, y, w, h, scanlinePad, plane);
  }

  public int getPlaneCount(int imageIndex) {
    return useLegacy ? legacyReader.getPlaneCount(imageIndex)
      : nativeReader.getPlaneCount(imageIndex);
  }

  public int getImageCount() {
    return useLegacy ? legacyReader.getImageCount() : nativeReader.getImageCount();
  }
}
