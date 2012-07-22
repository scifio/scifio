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
 * Interface for all SciFIO Readers.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Reader<M extends Metadata> extends HasContext, HasFormat {

  // -- Reader API methods --

  /**
   * Obtains the specified image plane from the current file as a byte array.
   * @see #openBytes(int, byte[])
   */
  byte[] openBytes(int imageIndex, int planeIndex)
    throws FormatException, IOException;

  /**
   * Obtains a sub-image of the specified image plane,
   * whose upper-left corner is given by (x, y).
   */
  byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException;

  /**
   * Obtains the specified image plane from the current file into a
   * pre-allocated byte array of
   * (sizeX * sizeY * bytesPerPixel * RGB channel count).
   *
   * @param imageIndex the image index within the file.
   * @param planeIndex the plane index within the image.
   * @param buf a pre-allocated buffer.
   * @return the pre-allocated buffer <code>buf</code> for convenience.
   * @throws FormatException if there was a problem parsing the metadata of the
   *   file.
   * @throws IOException if there was a problem reading the file.
   */
  byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException;

  /**
   * Obtains a sub-image of the specified image plane
   * into a pre-allocated byte array.
   *
   * @param imageIndex the image index within the file.
   * @param planeIndex the plane index within the image.
   * @param buf a pre-allocated buffer.
   * @param dims a map of dimension labels (e.g., "x", "y") to the size of the
   *             corresponding dimension (e.g., sizeX, sizeY) 
   * @return the pre-allocated buffer <code>buf</code> for convenience.
   * @throws FormatException if there was a problem parsing the metadata of the
   *   file.
   * @throws IOException if there was a problem reading the file.
   */
  byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x, int y,
    int w, int h) throws FormatException, IOException;

  /**
   * Obtains the specified image plane (or sub-image thereof) in the reader's
   * native data structure. For most readers this is a byte array; however,
   * some readers call external APIs that work with other types such as
   * {@link java.awt.image.BufferedImage}. The openPlane method exists to
   * maintain generality and efficiency while avoiding pollution of the API
   * with AWT-specific logic.
   *
   * @see ome.scifio.FormatReader
   * @see ome.scifio.in.BufferedImageReader
   */
  Object openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException;

  /**
   * Obtains a thumbnail for the specified image plane from the current file,
   * as a byte array.
   */
  byte[] openThumbBytes(int imageIndex, int planeIndex)
    throws FormatException, IOException;

  /** Specifies whether or not to force grouping in multi-file formats. */
  void setGroupFiles(boolean group);

  /** Returns true if we should group files in multi-file formats.*/
  boolean isGroupFiles();

  /**
   * Returns an int indicating that we cannot, must, or might group the files
   * in a given dataset.
   */
  int fileGroupOption(String id) throws FormatException, IOException;

  /** Returns the current file. */
  String getCurrentFile();

  /** Returns the list of domains represented by the current file. */
  String[] getDomains();

  /**
   * Gets the Z, C and T coordinates corresponding
   * to the given rasterized index value.
   */
  int[] getZCTCoords(int index);

  /**
   * Retrieves the current input stream for this reader.
   * @return A RandomAccessInputStream
   */
  RandomAccessInputStream getStream();

  /**
   * Retrieves all underlying readers.
   * Returns null if there are no underlying readers.
   */
  Reader<Metadata>[] getUnderlyingReaders();

  /** Returns the optimal sub-image width for use with openBytes. */
  int getOptimalTileWidth(int imageIndex);

  /** Returns the optimal sub-image height for use with openBytes. */
  int getOptimalTileHeight(int imageIndex);

  /** Sets the Metadata for this Reader */
  void setMetadata(M meta) throws IOException;

  /** Gets the type-specific Metadata for this Reader */
  M getMetadata();

  /** Gets the core metadata for this Reader. */
  CoreMetadata getCoreMetadata();

  //TODO remove normalization methods
  /** Specifies whether or not to normalize float data. */
  void setNormalized(boolean normalize);

  /** Returns true if we should normalize float data. */
  boolean isNormalized();

  /** Returns true if this format supports multi-file datasets. */
  boolean hasCompanionFiles();

  /**
   * Sets the source for this reader to read from.
   * @param file
   * @throws IOException 
   */
  void setSource(File file) throws IOException;

  /**
   * Sets the source for this reader to read from.
   * @param fileName
   * @throws IOException 
   */
  void setSource(String fileName) throws IOException;

  /**
   * Sets the source for this reader to read from.
   * @param in
   */
  void setSource(RandomAccessInputStream stream) throws IOException;

  /**
   * Closes the currently open file. If the flag is set, this is all that
   * happens; if unset, it is equivalent to calling
   */
  void close(boolean fileOnly) throws IOException;

  /** Closes currently open file(s) and frees allocated memory. */
  void close() throws IOException;

  /** Reads a raw plane from disk. */
  byte[] readPlane(RandomAccessInputStream s, int imageIndex, int x, int y,
    int w, int h, byte[] buf) throws IOException;

  /** Reads a raw plane from disk. */
  byte[] readPlane(RandomAccessInputStream s, int imageIndex, int x, int y,
    int w, int h, int scanlinePad, byte[] buf) throws IOException;

  /** Determines the number of planes in the current file. */
  int getPlaneCount(int imageIndex);

  /** Determines the number of images in the current file. */
  int getImageCount();
}
