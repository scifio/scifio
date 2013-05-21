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

import org.scijava.plugin.Plugin;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.FormatException;
import ome.scifio.HasColorTable;
import ome.scifio.ImageMetadata;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * PCXReader is the file format reader for PCX files (originally used by
 * PC Paintbrush; now used in Zeiss' LSM Image Browser).
 * See http://www.qzx.com/pc-gpe/pcx.txt
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/PCXReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/PCXReader.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @author Mark Hiner
 * @author Melissa Linkert
 *
 */
@Plugin(type = PCXFormat.class)
public class PCXFormat extends AbstractFormat {

  // -- Format API Methods --
  
  /*
   * @see ome.scifio.Format#getFormatName()
   */
  public String getFormatName() {
    return "PCX";
  }

  /*
   * @see ome.scifio.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[]{"pcx"};
  }
  
  // -- Nested Classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata implements HasColorTable {
    
    // -- Fields --

    /** Offset to pixel data. */
    private long offset;

    /** Number of bytes per scan line - may be different than image width. */
    private int bytesPerLine;

    private int nColorPlanes;
    private ColorTable8 lut;
    
    // -- PCXMetadata getters and setters --

    public long getOffset() {
      return offset;
    }

    public void setOffset(long offset) {
      this.offset = offset;
    }

    public int getBytesPerLine() {
      return bytesPerLine;
    }

    public void setBytesPerLine(int bytesPerLine) {
      this.bytesPerLine = bytesPerLine;
    }

    public int getnColorPlanes() {
      return nColorPlanes;
    }

    public void setnColorPlanes(int nColorPlanes) {
      this.nColorPlanes = nColorPlanes;
    }
    
    // -- Metadata API methods --
    
    /*
     * @see ome.scifio.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      
      ImageMetadata iMeta = get(0);
      iMeta.setAxisLength(Axes.CHANNEL, nColorPlanes);
      iMeta.setAxisLength(Axes.Z, 1);
      iMeta.setAxisLength(Axes.TIME, 1);
      iMeta.setRGB(nColorPlanes > 1);
      iMeta.setPlaneCount(1);
      iMeta.setPixelType(FormatTools.UINT8);
      iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));
      iMeta.setInterleaved(false);
    }
    
    @Override
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        offset = 0;
        bytesPerLine = 0;
        nColorPlanes = 0;
        lut = null;
      }
    }
    
    // -- HasColorTable API Methods -

    public ColorTable getColorTable(int imageIndex, int planeIndex) {
      return lut;
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {
    
    // -- Constants -- 
    
    public static final byte PCX_MAGIC_BYTE = 10;

    // -- Checker API Methods --
    
    @Override
    public boolean isFormat(RandomAccessInputStream stream) throws IOException {
      final int blockLen = 1;
      if (!FormatTools.validStream(stream, blockLen, false)) return false;
      return stream.read() == PCX_MAGIC_BYTE;
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Parser API Methods --
    
    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException 
    {
      LOGGER.info("Reading file header");

      meta.createImageMetadata(1);
      ImageMetadata iMeta = meta.get(0);
      
      iMeta.setLittleEndian(true);
      stream.order(true);
      stream.seek(1);
      int version = stream.read();
      stream.skipBytes(1);
      int bitsPerPixel = stream.read();
      int xMin = stream.readShort();
      int yMin = stream.readShort();
      int xMax = stream.readShort();
      int yMax = stream.readShort();

      iMeta.setAxisLength(Axes.X, xMax - xMin);
      iMeta.setAxisLength(Axes.Y, yMax - yMin);

      stream.skipBytes(version == 5 ? 53 : 51);

      meta.setnColorPlanes(stream.read());
      meta.setBytesPerLine(stream.readShort());
      int paletteType = stream.readShort();

      meta.setOffset(stream.getFilePointer() + 58);
      
      if (version == 5 && meta.getnColorPlanes() == 1) {
        stream.seek(stream.length() - 768);
        byte[][] lut = new byte[3][256];
        for (int i=0; i<lut[0].length; i++) {
          for (int j=0; j<lut.length; j++) {
            lut[j][i] = stream.readByte();
          }
        }
        
        meta.lut = new ColorTable8(lut);
        iMeta.setIndexed(true);
      }

      addGlobalMeta("Palette type", paletteType);
    }
    
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {
    
    // -- Constructor --
    
    public Reader() {
      domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
    }

    // -- Reader API Methods --
  
    /*
     * @see ome.scifio.Reader#openPlane(int, int, ome.scifio.DataPlane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h)
      throws FormatException, IOException 
    {
      Metadata meta = getMetadata();
      byte[] buf = plane.getData();
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);

      getStream().seek(meta.getOffset());

      // PCX uses a simple RLE compression algorithm

      byte[] b = new byte[meta.getBytesPerLine() * meta.getAxisLength(imageIndex, Axes.Y) * meta.getnColorPlanes()];
      int pt = 0;
      while (pt < b.length) {
        int val = getStream().read() & 0xff;
        if (((val & 0xc0) >> 6) == 3) {
          int len = val & 0x3f;
          val = getStream().read() & 0xff;
          for (int q=0; q<len; q++) {
            b[pt++] = (byte) val;
            if ((pt % meta.getBytesPerLine()) == 0) {
              break;
            }
          }
        }
        else b[pt++] = (byte) (val & 0xff);
      }

      int src = y * meta.getnColorPlanes() * meta.getBytesPerLine();
      for (int row=0; row<h; row++) {
        int rowOffset = row * meta.getnColorPlanes() * meta.getBytesPerLine();
        for (int c=0; c<meta.getnColorPlanes(); c++) {
          System.arraycopy(b, src + rowOffset + x, buf, c * w * h + row * w, w);
          rowOffset += meta.getBytesPerLine();
        }
      }

      return plane;
    }
  }
}
