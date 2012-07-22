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
import java.util.Hashtable;
import java.util.Set;

import ome.scifio.MetadataLevel;
import ome.scifio.MetadataOptions;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Interface for all SciFIO Parsers.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Parser<M extends Metadata> extends HasContext, HasFormat {

  // -- Parser API methods --

  /**
   * Wraps the file corresponding to the given name in a File handle and returns parse(RandomAccessInputStream).
   * 
   * @param fileName Path to an image file to be parsed.  Parsers are typically
   *   specific to the file type discovered here.
   * @return most specific metadata for this type
   * @throws IOException 
   */
  M parse(String fileName) throws IOException, FormatException;

  /**
   * Wraps the file in a File handle and returns parse(RandomAccessInputStream).
   * 
   * @param file Path to an image file to be parsed.  Parsers are typically
   *   specific to the file type discovered here.
   * @return most specific metadata for this type
   * @throws IOException 
   */
  M parse(File file) throws IOException, FormatException;

  /**
   * Returns the most specific Metadata object possible, for the provided RandomAccessInputStream.
   * 
   * @param stream random access handle to the file to be parsed.
   * @return most specific metadata for this type   
   * @throws IOException 
   */
  M parse(RandomAccessInputStream stream) throws IOException, FormatException;

  /**
   * See {@link Parser#parse(RandomAccessInputStream)}
   * This method will parse into the provided Metadata object instead of
   * creating a new Metadata.
   * 
   * @param fileName Path to an image file to be parsed.  Parsers are typically
   *   specific to the file type discovered here.
   * @return most specific metadata for this type
   * @throws IOException 
   */
  M parse(String fileName, M meta) throws IOException, FormatException;

  /**
   * See {@link Parser#parse(String)}
   * This method will parse into the provided Metadata object instead of
   * creating a new Metadata.
   * 
   * @param file Path to an image file to be parsed.  Parsers are typically
   *   specific to the file type discovered here.
   * @return most specific metadata for this type
   * @throws IOException 
   */
  M parse(File file, M meta) throws IOException, FormatException;

  /**
   * See {@link Parser#parse(File)}
   * This method will parse into the provided Metadata object instead of
   * creating a new Metadata.
   * 
   * NB: this is the bottom of the Parse hierarchy and should always be
   * implicitly called. Thus this is the safest place to put code that
   * should always be executed upon Parse.
   * 
   * @param stream random access handle to the file to be parsed.
   * @return most specific metadata for this type   
   * @throws IOException 
   */
  M parse(RandomAccessInputStream stream, M meta)
    throws IOException, FormatException;

  /**
   * Closes the currently open file. If the flag is set, this is all that
   * happens; if unset, it is equivalent to calling
   */
  void close(boolean fileOnly) throws IOException;

  /** Closes currently open file(s) and frees allocated memory. */
  void close() throws IOException;

  /**
   * Specifies whether or not to save proprietary metadata
   * in the Metadata.
   */
  void setOriginalMetadataPopulated(boolean populate);

  /**
   * Returns true if we should save proprietary metadata
   * in the Metadata.
   */
  boolean isOriginalMetadataPopulated();

  /** Returns an array of filenames needed to open this dataset. */
  String[] getUsedFiles();

  /**
   * Returns an array of filenames needed to open this dataset.
   * If the 'noPixels' flag is set, then only files that do not contain
   * pixel data will be returned.
   */
  String[] getUsedFiles(boolean noPixels);

  /**
   * Specifies whether ugly metadata (entries with unprintable characters,
   * and extremely large entries) should be discarded from the metadata table.
   */
  void setMetadataFiltered(boolean filter);

  /**
   * Returns true if ugly metadata (entries with unprintable characters,
   * and extremely large entries) are discarded from the metadata table.
   */
  boolean isMetadataFiltered();

  /** Returns an array of filenames needed to open the indicated image index. */
  String[] getImageUsedFiles(int imageIndex);

  /**
   * Returns an array of filenames needed to open the indicated image.
   * If the 'noPixels' flag is set, then only files that do not contain
   * pixel data will be returned.
   */
  String[] getImageUsedFiles(int imageIndex, boolean noPixels);

  /**
   * Returns an array of FileInfo objects representing the files needed
   * to open this dataset.
   * If the 'noPixels' flag is set, then only files that do not contain
   * pixel data will be returned.
   */
  FileInfo[] getAdvancedUsedFiles(boolean noPixels);

  /**
   * Returns an array of FileInfo objects representing the files needed to
   * open the current series.
   * If the 'noPixels' flag is set, then only files that do not contain
   * pixel data will be returned.
   */
  FileInfo[] getAdvancedImageUsedFiles(int imageIndex, boolean noPixels);

  /** Adds an entry to the specified Hashtable */
  void addMeta(String key, Object value, Hashtable<String, Object> meta);

  /** Returns a list of MetadataLevel options for determining the granularity of MetadataCollection */
  Set<MetadataLevel> getSupportedMetadataLevels();

  /** Sets the MetadataOptions of this Parser */
  void setMetadataOptions(MetadataOptions options);

  /** Returns the MetadataOptions for this Parser */
  MetadataOptions getMetadataOptions();
}
