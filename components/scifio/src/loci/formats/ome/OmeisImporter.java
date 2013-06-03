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

package loci.formats.ome;

import java.io.IOException;
import java.util.Hashtable;

import loci.formats.FormatException;
import loci.legacy.context.LegacyContext;

/**
 * A legacy delegator class for ome.xml.meta.OmeisImporter
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ome/OmeisImporter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ome/OmeisImporter.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Ilya Goldberg igg at nih.gov
 * 
 * @deprecated see ome.xml.meta.OmeisImporter
 */
@Deprecated
public class OmeisImporter {

  // -- Constants --

  // -- Static fields --

  // -- Fields --

  /** Reader for handling file formats. */
  private ome.xml.meta.OmeisImporter omeis;

  // -- Constructor --

  public OmeisImporter() {
    omeis = new ome.xml.meta.OmeisImporter(LegacyContext.get());
  }

  public OmeisImporter(boolean stitchFiles) {
    omeis = new ome.xml.meta.OmeisImporter(LegacyContext.get(), stitchFiles);
  }

  // -- OmeisImporter API methods - main functionality --

  /** Prints out the build date for the Bio-Formats OMEIS utility. */
  public void printVersion() {
    omeis.printVersion();
  }

  /**
   * Tests whether Bio-Formats is potentially capable of importing the given
   * file IDs. Outputs the IDs it can potentially import, one group per line,
   * with elements of the each group separated by spaces.
   */
  public void testIds(int[] fileIds)
    throws OmeisException, FormatException, IOException
  {
    try {
      omeis.testIds(fileIds);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
    catch (io.scif.FormatException e) {
      throw (FormatException)e;
    }
  }

  /**
   * Attempts to import the given file IDs using Bio-Formats, as a single
   * group. Pixels are saved to the pixels file designated by OMEIS, and an
   * OME-XML metadata block describing the successfully imported data is
   * dumped to standard output.
   */
  public void importIds(int[] fileIds)
    throws OmeisException, FormatException, IOException
  {
    try {
      omeis.importIds(fileIds);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
    catch (io.scif.FormatException e) {
      throw (FormatException)e;
    }
  }

  // -- OmeisImporter API methods - OMEIS method calls --

  /** Gets path to original file corresponding to the given file ID. */
  public String getLocalFilePath(int fileId) throws OmeisException {
    try {
      return omeis.getLocalFilePath(fileId);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /**
   * Gets information about the file corresponding to the given file ID.
   * @return hashtable containing the information as key/value pairs
   */
  public Hashtable getFileInfo(int fileId) throws OmeisException {
    try {
      return omeis.getFileInfo(fileId);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /**
   * Instructs OMEIS to construct a new Pixels object.
   * @return pixels ID of the newly created pixels
   */
  public int newPixels(int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int bytesPerPixel, boolean isSigned, boolean isFloat) throws OmeisException
  {
    try {
      return omeis.newPixels(sizeX, sizeY, sizeZ, sizeC, sizeT, bytesPerPixel,
        isSigned, isFloat);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /** Gets whether the local system uses little-endian byte order. */
  public boolean isLittleEndian() throws OmeisException {
    try {
      return omeis.isLittleEndian();
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /** Gets path to Pixels file corresponding to the given pixels ID. */
  public String getLocalPixelsPath(int pixelsId) throws OmeisException {
    try {
      return omeis.getLocalPixelsPath(pixelsId);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /**
   * Instructs OMEIS to process the Pixels file
   * corresponding to the given pixels ID.
   * @return final (possibly changed) pixels ID of the processed pixels
   */
  public int finishPixels(int pixelsId) throws OmeisException {
    try {
      return omeis.finishPixels(pixelsId);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  /** Gets SHA1 hash for the pixels corresponding to the given pixels ID. */
  public String getPixelsSHA1(int pixelsId) throws OmeisException {
    try {
      return omeis.getPixelsSHA1(pixelsId);
    }
    catch (ome.xml.meta.OmeisException e) {
      throw (OmeisException)e;
    }
  }

  // -- Main method --

  /**
   * Run ./omebf with a list of file IDs to import those IDs.
   * Run with the -test flag to ask Bio-Formats whether it
   * thinks it can import those files.
   */
  public static void main(String[] args) {
    ome.xml.meta.OmeisImporter.main(args);
  }

}
