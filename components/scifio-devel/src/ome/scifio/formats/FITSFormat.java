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

package ome.scifio.formats;

import java.io.IOException;


import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.FormatException;
import ome.scifio.ImageMetadata;
import ome.scifio.common.DataTools;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * FitsReader is the file format reader for
 * Flexible Image Transport System (FITS) images.
 *
 * Much of this code was adapted from ImageJ (http://rsb.info.nih.gov/ij).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/FitsReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/FitsReader.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @author Mark hiner hinerm at gmail.com
 * @author Melissa Linkert
 */
@Plugin(type = FITSFormat.class)
public class FITSFormat extends AbstractFormat {
  
  // -- Format API Methods --
  
  public String getFormatName() {
    return "Flexible Image Transport System";
  }

  public String[] getSuffixes() {
    return new String[] {"fits", "fts"};
  }
  
  // -- Nested Classes --
  
  /**
   * @author Mark hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {
    
    // -- Constants --
    
    public static final String CNAME = "ome.scifio.formats.FITSFormat$Metadata";

    // -- Fields --
    
    private long pixelOffset;
    
    // -- FITS Metadata getters and setters --

    public long getPixelOffset() {
      return pixelOffset;
    }

    public void setPixelOffset(long pixelOffset) {
      this.pixelOffset = pixelOffset;
    }

    // -- Metadata API methods --

    public void populateImageMetadata() {
      ImageMetadata iMeta = get(0);
      
      if (iMeta.getAxisIndex(Axes.Z) == -1) iMeta.setAxisLength(Axes.Z, 1);
      iMeta.setAxisLength(Axes.CHANNEL, 1);
      iMeta.setAxisLength(Axes.TIME, 1);
      
      // correct for truncated files
      int planeSize = iMeta.getAxisLength(Axes.X) * iMeta.getAxisLength(Axes.X) *
          FormatTools.getBytesPerPixel(iMeta.getPixelType());
      
      try {
        if (DataTools.safeMultiply64(planeSize, iMeta.getAxisLength(Axes.Z)) >
        (getSource().length() - pixelOffset))
        {
          iMeta.setAxisLength(Axes.Z, (int) ((getSource().length() - pixelOffset) / planeSize));
        }
      } catch (IOException e) {
        LOGGER.error("Failed to determine input stream length", e);
      }

      iMeta.setPlaneCount(iMeta.getAxisLength(Axes.Z));
      iMeta.setRGB(false);
      iMeta.setLittleEndian(false);
      iMeta.setInterleaved(false);
      iMeta.setIndexed(false);
      iMeta.setFalseColor(false);
      iMeta.setMetadataComplete(true);
    }
    
    public void close() {
      pixelOffset = 0;
    }
  }
  
  /**
   * @author Mark hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
    private static final int LINE_LENGTH = 80;
    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException 
    {
      ImageMetadata iMeta = meta.get(0);
      
      String line = in.readString(LINE_LENGTH);
      if (!line.startsWith("SIMPLE")) {
        throw new FormatException("Unsupported FITS file.");
      }

      String key = "", value = "";
      while (true) {
        line = in.readString(LINE_LENGTH);

        // parse key/value pair
        int ndx = line.indexOf("=");
        int comment = line.indexOf("/", ndx);
        if (comment < 0) comment = line.length();

        if (ndx >= 0) {
          key = line.substring(0, ndx).trim();
          value = line.substring(ndx + 1, comment).trim();
        }
        else key = line.trim();

        // if the file has an extended header, "END" will appear twice
        // the first time marks the end of the extended header
        // the second time marks the end of the standard header
        // image dimensions are only populated by the standard header
        if (key.equals("END") && iMeta.getAxisLength(Axes.X) > 0) break;

        if (key.equals("BITPIX")) {
          int bits = Integer.parseInt(value);
          boolean fp = bits < 0;
          boolean signed = bits != 8;
          int bytes = Math.abs(bits) / 8;
          iMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed, fp));
          iMeta.setBitsPerPixel(Math.abs(bits));
        }
        else if (key.equals("NAXIS1")) iMeta.setAxisLength(Axes.X, Integer.parseInt(value));
        else if (key.equals("NAXIS2")) iMeta.setAxisLength(Axes.Y, Integer.parseInt(value));
        else if (key.equals("NAXIS3")) iMeta.setAxisLength(Axes.Z, Integer.parseInt(value));

        addGlobalMeta(key, value);  
      }
      while (in.read() == 0x20);
      meta.setPixelOffset(in.getFilePointer() - 1);
    }
  }

  /**
   * @author Mark hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {
    
    // -- Constructor --
    
    public Reader() {
      domains =
          new String[] {FormatTools.ASTRONOMY_DOMAIN, FormatTools.UNKNOWN_DOMAIN};
    }
    
    // -- Reader API Methods --

    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h)
      throws FormatException, IOException 
    {
      byte[] buf = plane.getData();
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);

      getStream().seek(getMetadata().getPixelOffset() + planeIndex * 
          FormatTools.getPlaneSize(this, imageIndex));
      return readPlane(getStream(), imageIndex, x, y, w, h, plane);
    }
  }
}
