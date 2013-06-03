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

package loci.formats.out;

import io.scif.formats.TIFFFormat;

import java.io.IOException;

import net.imglib2.meta.Axes;

import loci.formats.FormatException;
import loci.formats.SCIFIOFormatWriter;
import loci.formats.tiff.IFD;
import loci.legacy.context.LegacyContext;

/**
 * TiffWriter is the file format writer for TIFF files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/out/TiffWriter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/out/TiffWriter.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @deprecated see io.scif.formats.TIFFFormat
 */
@Deprecated
public class TiffWriter extends SCIFIOFormatWriter {

  // -- Constructors --

  public TiffWriter() {
    this("Tagged Image File Format", new String[] {"tif", "tiff"});
  }

  public TiffWriter(String fmt, String[] exts) {
    super(fmt, exts);

    format = LegacyContext.getSCIFIO().format().getFormatFromClass(TIFFFormat.class);
    try {
      writer = format.createWriter() ;
    }
    catch (io.scif.FormatException e) {
      LOGGER.warn("Failed to create TIFFFormat components");
    }
  }
  

  /**
   * Saves the given image to the specified (possibly already open) file.
   * The IFD hashtable allows specification of TIFF parameters such as bit
   * depth, compression and units.
   */
  public void saveBytes(int no, byte[] buf, IFD ifd)
    throws IOException, FormatException
  {
    
    int w = writer.getMetadata().getAxisLength(getSeries(), Axes.X);
    int h = writer.getMetadata().getAxisLength(getSeries(), Axes.Y);
    //FIXME: need to convert IFDs
    try {
      ((TIFFFormat.Writer<?>)writer).savePlane(getSeries(), no, planeCheck(buf, 0, 0, w, h),
          null, 0, 0, w, h);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  /**
   * Saves the given image to the specified series in the current file.
   * The IFD hashtable allows specification of TIFF parameters such as bit
   * depth, compression and units.
   */
  public void saveBytes(int i, byte[] buf, IFD ifd, int tileX, int tileY,
    int tileWidth, int tileHeight)
    throws FormatException, IOException
  {
    //FIXME: need to convert IFDs
    try {
      ((TIFFFormat.Writer<?>)writer).savePlane(getSeries(), i, planeCheck(buf, tileX, tileY, tileWidth, tileHeight),
          null, tileX, tileY, tileWidth, tileHeight);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

  // -- TiffWriter API methods --

  /**
   * Sets whether or not BigTIFF files should be written.
   * This flag is not reset when close() is called.
   */
  public void setBigTiff(boolean bigTiff) {
    ((TIFFFormat.Writer<?>)writer).setBigTiff(bigTiff);
  }

  // -- IFormatWriter API methods --

  /*
   * @see loci.formats.IFormatWriter#saveBytes(int, byte[], int, int, int, int)
   */
  public void saveBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    try {
      writer.savePlane(getSeries(), no, planeCheck(buf, x, y, w, h), x, y, w, h);
    }
    catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }
}
