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
import java.lang.reflect.InvocationTargetException;

import ome.scifio.AbstractReader;
import ome.scifio.CoreMetadata;
import ome.scifio.DelegateReader;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Abstract superclass of reader logic that wraps other readers.
 * All methods are simply delegated to the wrapped reader.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ReaderWrapper.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ReaderWrapper.java;hb=HEAD">Gitweb</a></dd></dl>
 */
// TODO should I annotate this?
public abstract class ReaderWrapper<M extends Metadata> extends AbstractReader<M> {

  // -- Fields --

  /** Reader used to read the file. */
  private Reader<M> reader;
  
  // -- Constructors --

  public ReaderWrapper() { 
    this(null, null);
    // TODO if annotated could find this format and then create a reader...
  }
  
  public ReaderWrapper(Reader<M> r) {
    this(r, r == null ? null : r.getContext());
  }
  
  public ReaderWrapper(Reader<M> r, SCIFIO ctx) {
    super(r == null? null : r.getFormatName(),
      r == null? null : r.getSuffixes(), ctx);
    
    if(r == null) {
      throw new IllegalArgumentException("Reader can not be null");
    }
    
    reader = r;
  }
  
  // -- ReaderWrapper API methods --

  /** Gets the wrapped reader. */
  public Reader<M> getReader() { return reader; }
  
  /** Sets the wrapped reader. */
  public void setReader(Reader<M> reader) { this.reader = reader; }
 
  /**
   * Unwraps nested wrapped readers until the core reader (i.e., not
   * a {@link ReaderWrapper} or {@link ImageReader}) is found.
   */
  public Reader<M> unwrap() throws FormatException, IOException {
    return unwrap(null, null);
  }

  /**
   * Unwraps nested wrapped readers until the core reader (i.e., not
   * a {@link ReaderWrapper} or {@link ImageReader}) is found.
   *
   * @param id Id to use as a basis when unwrapping any nested
   *   {@link ImageReader}s. If null, the current id is used.
   */
  public Reader<M> unwrap(String id)
    throws FormatException, IOException
  {
    return unwrap(null, id);
  }

  /**
   * Unwraps nested wrapped readers until the given reader class is found.
   *
   * @param readerClass Class of the desired nested reader. If null, the
   *   core reader (i.e., deepest wrapped reader) will be returned.
   * @param id Id to use as a basis when unwrapping any nested
   *   {@link ImageReader}s. If null, the current id is used.
   */
  public Reader<M> unwrap(Class<? extends Reader<M>> readerClass,
    String id) throws FormatException, IOException
  {
    Reader<M> r = this;
    while (r instanceof ReaderWrapper) {
      if (readerClass != null && readerClass.isInstance(r)) break;
      else r = ((ReaderWrapper) r).getReader();
    }
    if (readerClass != null && !readerClass.isInstance(r)) return null;
    return r;
  }

  /**
   * Performs a deep copy of the reader, including nested wrapped readers.
   * Most of the reader state is preserved as well, including:<ul>
   *   <li>{@link #isNormalized()}</li>
   *   <li>{@link #isMetadataFiltered()}</li>
   *   <li>{@link #isMetadataCollected()}</li>
   *   <li>{@link DelegateReader#isLegacy()}</li>
   * </ul>
   *
   * @param imageReaderClass If non-null, any {@link ImageReader}s in the
   *   reader stack will be replaced with instances of the given class.
   * @throws FormatException If something goes wrong during the duplication.
   */
  public ReaderWrapper duplicate(
    Class<? extends Reader<M>> imageReaderClass) throws FormatException
  {
    ReaderWrapper wrapperCopy = duplicateRecurse(imageReaderClass);

    // sync top-level configuration with original reader
    boolean normalized = isNormalized();
    wrapperCopy.setNormalized(normalized);
    return wrapperCopy;
  }
  
  // -- HasContext API methods --
  
  public SCIFIO getContext() {
    return reader.getContext();
  }

  public void setContext(SCIFIO ctx) {
    reader.setContext(ctx);
  }
  
  // -- HasFormat API methods --
  
  public Format<M, ?, ?, ?, ?> getFormat() {
    return (Format<M, ?, ?, ?, ?>) reader.getFormat();
  }
  
  // -- Reader API methods -- 

  public byte[] openBytes(int imageIndex, int planeIndex)
    throws FormatException, IOException
  {
    return reader.openBytes(imageIndex, planeIndex);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w,
    int h) throws FormatException, IOException
  {
    return reader.openBytes(imageIndex, planeIndex, x, y, w, h);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException
  {
    return reader.openBytes(imageIndex, planeIndex, buf);
  }

  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x,
    int y, int w, int h) throws FormatException, IOException
  {
    return reader.openBytes(imageIndex, planeIndex, buf, x, y, w, h);
  }

  public Object openPlane(int imageIndex, int planeIndex, int x, int y, int w,
    int h) throws FormatException, IOException
  {
    return reader.openPlane(imageIndex, planeIndex, x, y, w, h);
  }

  public byte[] openThumbBytes(int imageIndex, int planeIndex)
    throws FormatException, IOException
  {
    return reader.openThumbBytes(imageIndex, planeIndex);
  }

  public void setGroupFiles(boolean group) {
    reader.setGroupFiles(group);
  }

  public boolean isGroupFiles() {
    return reader.isGroupFiles();
  }

  public int fileGroupOption(String id) throws FormatException, IOException {
    return reader.fileGroupOption(id);
  }

  public String getCurrentFile() {
    return reader.getCurrentFile();
  }

  public String[] getDomains() {
    return reader.getDomains();
  }

  public int[] getZCTCoords(int imageIndex, int planeIndex) {
    return reader.getZCTCoords(imageIndex, planeIndex);
  }

  public RandomAccessInputStream getStream() {
    return reader.getStream();
  }

  public Reader<Metadata>[] getUnderlyingReaders() {
    return reader.getUnderlyingReaders();
  }

  public int getOptimalTileWidth(int imageIndex) {
    return reader.getOptimalTileWidth(imageIndex);
  }

  public int getOptimalTileHeight(int imageIndex) {
    return reader.getOptimalTileHeight(imageIndex);
  }

  public void setMetadata(M meta) throws IOException {
    reader.setMetadata(meta);
  }

  public M getMetadata() {
    return reader.getMetadata();
  }

  public CoreMetadata getCoreMetadata() {
    return reader.getCoreMetadata();
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
    reader.setSource(file);
  }

  public void setSource(String fileName) throws IOException {
    reader.setSource(fileName);
  }

  public void setSource(RandomAccessInputStream stream) throws IOException {
    reader.setSource(stream);
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
    return reader.readPlane(s, imageIndex, x, y, w, h, buf);
  }

  public byte[] readPlane(RandomAccessInputStream s, int imageIndex, int x,
    int y, int w, int h, int scanlinePad, byte[] buf) throws IOException
  {
    return reader.readPlane(s, imageIndex, x, y, w, h, scanlinePad, buf);
  }

  public int getPlaneCount(int imageIndex) {
    return reader.getPlaneCount(imageIndex);
  }

  public int getImageCount() {
    return reader.getImageCount();
  }
  
  // -- Helper methods --

  private ReaderWrapper<M> duplicateRecurse(
    Class<? extends Reader<M>> imageReaderClass) throws FormatException
  {
    Reader<M> childCopy = null;
    if (reader instanceof ReaderWrapper) {
      // found a nested reader layer; duplicate via recursion
      childCopy = ((ReaderWrapper) reader).duplicateRecurse(imageReaderClass);
    }
    else {
      @SuppressWarnings("rawtypes")
      Class<? extends Reader> c = reader.getClass();
      try {
        childCopy = c.newInstance();
      }
      catch (IllegalAccessException exc) { throw new FormatException(exc); }
      catch (InstantiationException exc) { throw new FormatException(exc); }

      // preserve reader-specific configuration with original reader
      if (reader instanceof DelegateReader) {
        DelegateReader<M> delegateOriginal = (DelegateReader<M>) reader;
        DelegateReader<M> delegateCopy = (DelegateReader<M>) childCopy;
        delegateCopy.setLegacy(delegateOriginal.isLegacy());
      }
    }

    // use crazy reflection to instantiate a reader of the proper type
    Class<? extends ReaderWrapper> wrapperClass = getClass();
    ReaderWrapper wrapperCopy = null;
    try {
      wrapperCopy = wrapperClass.getConstructor(new Class[]
        {Reader.class}).newInstance(new Object[] {childCopy});
    }
    catch (InstantiationException exc) { throw new FormatException(exc); }
    catch (IllegalAccessException exc) { throw new FormatException(exc); }
    catch (NoSuchMethodException exc) { throw new FormatException(exc); }
    catch (InvocationTargetException exc) { throw new FormatException(exc); }

    return wrapperCopy;
  }
  
  // -- Helper Methods --
  
  public CoreMetadata coreMeta() {
    return getReader().getCoreMetadata();
  }
  
}
