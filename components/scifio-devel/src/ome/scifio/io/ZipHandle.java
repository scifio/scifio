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

package ome.scifio.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ome.scifio.common.Constants;

import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * StreamHandle implementation for reading from Zip-compressed files
 * or byte arrays.  Instances of ZipHandle are read-only.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/src/loci/common/ZipHandle.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/src/loci/common/ZipHandle.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @see StreamHandle
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
@Plugin(type = IStreamAccess.class)
public class ZipHandle extends StreamHandle {

  // -- Fields --

  private RandomAccessInputStream in;
  private ZipInputStream zip;
  private String entryName;
  private int entryCount;

  // -- Constructor --
  
  /**
   * Zero-parameter constructor. This instructor can be used first
   * to see if a given file is constructable from this handle. If so,
   * setFile can then be used.
   */
  public ZipHandle() {
    super();
  }
  
  public ZipHandle(Context context) {
    super(context);
  }

  public ZipHandle(Context context, String file) throws IOException {
    super(context);
    setFile(file);
  }

  /**
   * Constructs a new ZipHandle corresponding to the given entry of the
   * specified Zip file.
   *
   * @throws HandleException if the given file is not a Zip file.
   */
  public ZipHandle(String file, ZipEntry entry) throws IOException {
    super();

    seekToEntry();
    resetStream();
    populateLength();
    setFile(file, entry);
  }

  // -- IStreamAccess API methods --

  /** Returns true if the given filename is a Zip file. */
  public boolean isConstructable(String file) throws IOException {
    if (!file.toLowerCase().endsWith(".zip")) return false;

    IRandomAccess handle = getHandle(file);
    byte[] b = new byte[2];
    if (handle.length() >= 2) {
      handle.read(b);
    }
    handle.close();
    return new String(b, Constants.ENCODING).equals("PK");
  }

  // -- ZipHandle API methods --
  
  /** Get the name of the backing Zip entry. */
  public String getEntryName() {
    return entryName;
  }

  /** Returns the DataInputStream corresponding to the backing Zip entry. */
  public DataInputStream getInputStream() {
    return getStream();
  }

  /** Returns the number of entries. */
  public int getEntryCount() {
    return entryCount;
  }
  
  // -- IStreamAccess API methods --
  
  /* @see IStreamAccess#setFile(String) */
  public void setFile(String file, ZipEntry entry) throws IOException {
    super.setFile(file);
    
    in = openStream(file);
    zip = new ZipInputStream(in);
    entryName = entry.getName();
    entryCount = 1;

    seekToEntry();
    resetStream();
    populateLength();
  }

  /* @see IStreamAccess#setFile(String) */
  @Override
  public void setFile(String file) throws IOException {
    super.setFile(file);

    in = openStream(file);
    zip = new ZipInputStream(in);
    entryName = null;
    entryCount = 0;

    // strip off .zip extension and directory prefix
    String innerFile = file.substring(0, file.length() - 4);
    int slash = innerFile.lastIndexOf(File.separator);
    if (slash < 0) slash = innerFile.lastIndexOf("/");
    if (slash >= 0) innerFile = innerFile.substring(slash + 1);

    // look for Zip entry with same prefix as the Zip file itself
    boolean matchFound = false;
    while (true) {
      ZipEntry ze = zip.getNextEntry();
      if (ze == null) break;
      if (entryName == null) entryName = ze.getName();
      if (!matchFound && ze.getName().startsWith(innerFile)) {
        // found entry with matching name
        entryName = ze.getName();
        matchFound = true;
      }
      entryCount++;
    }
    resetStream();

    populateLength();
  }
  
  /* @see IStreamAccess#resetStream() */
  public void resetStream() throws IOException {
    if (getStream() != null) getStream().close();
    if (in != null) {
      in.close();
      in = openStream(getFile());
    }
    if (zip != null) zip.close();
    zip = new ZipInputStream(in);
    if (entryName != null) seekToEntry();
    setStream(new DataInputStream(new BufferedInputStream(
      zip, RandomAccessInputStream.MAX_OVERHEAD)));
    getStream().mark(RandomAccessInputStream.MAX_OVERHEAD);
  }

  // -- IRandomAccess API methods --

  /* @see IRandomAccess#close() */
  public void close() throws IOException {
    super.close();
    zip = null;
    entryName = null;
    if (in != null) in.close();
    in = null;
    entryCount = 0;
  }

  // -- Helper methods --

  private void seekToEntry() throws IOException {
    while (!entryName.equals(zip.getNextEntry().getName()));
  }

  private void populateLength() throws IOException {
    
    int length = -1;
    while (getStream().available() > 0) {
      getStream().skip(1);
      length++;
    }
    setLength(length);
    resetStream();
  }

  private IRandomAccess getHandle(String file) throws IOException {
    return getContext().getService(LocationService.class).getHandle(file, false, false);
  }

  private RandomAccessInputStream openStream(String file)
    throws IOException
  {
    return new RandomAccessInputStream(getContext(), getHandle(file), file);
  }

}
