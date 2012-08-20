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

package ome.xml.meta;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.CoreMetadata;
import ome.scifio.FormatException;
import ome.scifio.io.RandomAccessOutputStream;

/**
 * A utility class for working with metadata objects,
 * including {@link MetadataStore}, {@link MetadataRetrieve},
 * and OME-XML strings.
 * Most of the methods require the optional {@link loci.formats.ome}
 * package, and optional ome-xml.jar library, to be present at runtime.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MetadataTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MetadataTools.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class MetadataTools {
  
  // -- Constants --
  
  private static final Logger LOGGER =
    LoggerFactory.getLogger(MetadataTools.class);
  
  // -- Constructor --
  
  private MetadataTools() { }
  
  // -- Utility Methods -- OME-XML -- 
  
  /**
   * Checks whether the given metadata object has the minimum metadata
   * populated to successfully describe an Image.
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(CoreMetadata src,
    RandomAccessOutputStream out) throws FormatException
  {
    verifyMinimumPopulated(src, out, 0, 0);
  }

  /**
   * Checks whether the given metadata object has the minimum metadata
   * populated to successfully describe an Image.
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(CoreMetadata src,
    RandomAccessOutputStream out, int imageIndex) throws FormatException
  {
    verifyMinimumPopulated(src, out, imageIndex, 0);
  }

  /**
   * Checks whether the given metadata object has the minimum metadata
   * populated to successfully describe the nth Image.
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  public static void verifyMinimumPopulated(CoreMetadata src,
    RandomAccessOutputStream out, int imageIndex, int planeIndex)
    throws FormatException
  {
    if (src == null) {
      throw new FormatException("Metadata object is null; "
        + "call Writer.setMetadata() first");
    }

    if (out == null) {
      throw new FormatException("RandomAccessOutputStream object is null; "
        + "call Writer.setSource(<String/File/RandomAccessOutputStream>) first");
    }

    if (src.getAxisCount(0) == 0) {
      throw new FormatException("Axiscount #" + imageIndex + " is 0");
    }
  }

  /**
   * Adjusts the given dimension order as needed so that it contains exactly
   * one of each of the following characters: 'X', 'Y', 'Z', 'C', 'T'.
   */
  public static String makeSaneDimensionOrder(String dimensionOrder) {
    String order = dimensionOrder.toUpperCase();
    order = order.replaceAll("[^XYZCT]", "");
    String[] axes = new String[] {"X", "Y", "C", "Z", "T"};
    for (String axis : axes) {
      if (order.indexOf(axis) == -1) order += axis;
      while (order.indexOf(axis) != order.lastIndexOf(axis)) {
        order = order.replaceFirst(axis, "");
      }
    }
    return order;
  }
  
  /**
   * Constructs an LSID, given the object type and indices.
   * For example, if the arguments are "Detector", 1, and 0, the LSID will
   * be "Detector:1:0".
   */
  public static String createLSID(String type, int... indices) {
    StringBuffer lsid = new StringBuffer(type);
    for (int index : indices) {
      lsid.append(":");
      lsid.append(index);
    }
    return lsid.toString();
  }
  
  // Utility methods -- original metadata --

  /**
   * Merges the given lists of metadata, prepending the
   * specified prefix for the destination keys.
   */
  public static void merge(Map<String, Object> src, Map<String, Object> dest,
    String prefix)
  {
    for (String key : src.keySet()) {
      dest.put(prefix + key, src.get(key));
    }
  }
  
  /** Gets a sorted list of keys from the given hashtable. */
  public static String[] keys(Hashtable<String, Object> meta) {
    String[] keys = new String[meta.size()];
    meta.keySet().toArray(keys);
    Arrays.sort(keys);
    return keys;
  }
}
