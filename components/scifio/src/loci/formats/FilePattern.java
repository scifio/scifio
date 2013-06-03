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

package loci.formats;

import java.io.File;

import loci.common.Location;
import loci.legacy.context.LegacyContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FilePattern is a collection of methods for handling file patterns, a way of
 * succinctly representing a collection of files meant to be part of the same
 * data series.
 *
 * Examples:
 * <ul>
 *   <li>C:\data\BillM\sdub&lt;1-12&gt;.pic</li>
 *   <li>C:\data\Kevin\80&lt;01-59&gt;0&lt;2-3&gt;.pic</li>
 *   <li>/data/Josiah/cell-Z&lt;0-39&gt;.C&lt;0-1&gt;.tiff</li>
 * </ul>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/FilePattern.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/FilePattern.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * 
 * @deprecated see io.scif.FilePattern
 */
@Deprecated
public class FilePattern extends io.scif.FilePattern {

  // -- Constants --

  private static final Logger LOGGER =
    LoggerFactory.getLogger(FilePattern.class);

  // -- Constructors --

  /** Creates a pattern object using the given file as a template. */
  public FilePattern(Location file) { super(LegacyContext.get(), file.unwrap()); }

  /**
   * Creates a pattern object using the given
   * filename and directory path as a template.
   */
  public FilePattern(String name, String dir) {
    super(LegacyContext.get(), name, dir);
  }

  /** Creates a pattern object for files with the given pattern string. */
  public FilePattern(String pattern) {
    super(LegacyContext.get(), pattern);
  }

  public static String[] findSeriesPatterns(String id, String dir,
      String[] idList) {
    return LegacyContext.getSCIFIO().filePattern().findImagePatterns(id, dir, idList);
  }

  public static String[] findSeriesPatterns(String id) {
    return LegacyContext.getSCIFIO().filePattern().findImagePatterns(id);
  }

  public static Location findPattern(String id) {
    return new Location(LegacyContext.getSCIFIO().filePattern().findPattern(id));
  }

  public static String findPattern(Location idLoc) {
    return LegacyContext.getSCIFIO().filePattern().findPattern(idLoc.unwrap());
  }

  public static String findPattern(String absolutePath, String dirPrefix,
      String[] array) {
    return LegacyContext.getSCIFIO().filePattern().findPattern(
        absolutePath, dirPrefix, array);
  }

  public static String findPattern(File file) {
    return LegacyContext.getSCIFIO().filePattern().findPattern(file);
  }
}
