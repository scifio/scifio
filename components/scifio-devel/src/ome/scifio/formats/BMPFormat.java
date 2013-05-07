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
import ome.scifio.DefaultImageMetadata;
import ome.scifio.FormatException;
import ome.scifio.HasColorTable;
import ome.scifio.ImageMetadata;
import ome.scifio.MetadataLevel;
import ome.scifio.UnsupportedCompressionException;
import ome.scifio.codec.BitBuffer;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;
import ome.scifio.util.ImageTools;

/**
 * BMPReader is the file format reader for Microsoft Bitmap (BMP) files.
 * See <a href="http://astronomy.swin.edu.au/~pbourke/dataformats/bmp/">
 * http://astronomy.swin.edu.au/~pbourke/dataformats/bmp/</a>
 * for a nice description of the BMP file format.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/BMPReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/BMPReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Mark Hiner
 */
@Plugin(type=BMPFormat.class)
public class BMPFormat extends AbstractFormat {
  
  // -- Constants --
  
  public static final String BMP_MAGIC_STRING = "BM";
  
  // -- Compression types --
  
  private static final int RAW = 0;
  private static final int RLE_8 = 1;
  private static final int RLE_4 = 2;
  private static final int RGB_MASK = 3;

  // -- Format API MEthods --
  
  /*
   * @see ome.scifio.Format#getFormatName()
   */
  public String getFormatName() {
    return "Windows Bitmap";
  }

  /*
   * @see ome.scifio.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[]{"bmp"};
  }

  // -- Nested Classes --
  
  /**
  * @author Mark Hiner
   *
   */
  public static class Metadata extends AbstractMetadata implements HasColorTable {
    
    // -- Constants --

    public static final String CNAME = "ome.scifio.formats.BMPFormat$Metadata";

    // -- Fields --
    
    /** The palette for indexed color images. */
    private ColorTable8 palette;

    /** Compression type */
    private int compression;

    /** Offset to image data. */
    private long global;

    private boolean invertY = false;

    // -- Getters and Setters --
    
    public int getCompression() {
      return compression;
    }

    public void setCompression(int compression) {
      this.compression = compression;
    }

    public long getGlobal() {
      return global;
    }

    public void setGlobal(long global) {
      this.global = global;
    }

    public boolean isInvertY() {
      return invertY;
    }

    public void setInvertY(boolean invertY) {
      this.invertY = invertY;
    }
    
    // -- Metadata API Methods --
    
    /*
     * @see ome.scifio.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      LOGGER.info("Populating metadata");

      int bpp = getBitsPerPixel(0);
      ImageMetadata iMeta = get(0);
      
      int sizeC = bpp != 24 ? 1 : 3;
      
      if (bpp == 32) sizeC = 4;
      if (bpp > 8) bpp /= sizeC;
      
      iMeta.setBitsPerPixel(bpp);

      
      switch (bpp) {
        case 16:
          iMeta.setPixelType(FormatTools.UINT16);
          break;
        case 32:
          iMeta.setPixelType(FormatTools.UINT32);
          break;
        default:
          iMeta.setPixelType(FormatTools.UINT8);
      }

      iMeta.setRGB(sizeC > 1);
      iMeta.setLittleEndian(true);
      iMeta.setInterleaved(true);
      iMeta.setPlaneCount(1);
      
      iMeta.setMetadataComplete(true);
      iMeta.setIndexed(getColorTable() != null);
      
      if (iMeta.isIndexed()) {
        sizeC = 1;
        iMeta.setRGB(false);
      }
      
      iMeta.addAxis(Axes.CHANNEL, sizeC);
      iMeta.addAxis(Axes.Z, 1);
      iMeta.addAxis(Axes.TIME, 1);
      
      iMeta.setFalseColor(false);
    }
    
    // -- HasSource API Methods --
    
    /*
     * @see ome.scifio.AbstractMetadata#close()
     */
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      
      if (!fileOnly) {
        compression = 0;
        global = 0;
        palette = null;
        invertY = false;
      }
    }

    // -- HasColorTable API Methods --
    
    /*
     * @see ome.scifio.HasColorTable#getColorTable()
     */
    public ColorTable getColorTable() {
      return palette;
    }
  }
  
  /**
   * @author Mark Hiner
   *
   */
  public static class Checker extends AbstractChecker {
    
    public boolean isFormat(final RandomAccessInputStream stream)
      throws IOException
   {
     final int blockLen = 2;
     if (!FormatTools.validStream(stream, blockLen, false)) return false;
     return stream.readString(blockLen).startsWith(BMP_MAGIC_STRING);
   }
  }
    
  
  /**
  * @author Mark Hiner
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
        throws IOException, FormatException
    {
      meta.createImageMetadata(1);
      
      ImageMetadata iMeta = meta.get(0);
      
      stream.order(true);
      
      // read the first header - 14 bytes
      
      addGlobalMeta("Magic identifier", in.readString(2));

      addGlobalMeta("File size (in bytes)", in.readInt());
      in.skipBytes(4);
      
      meta.setGlobal(in.readInt());
      
      // read the second header - 40 bytes

      in.skipBytes(4);
      
      int sizeX = 0, sizeY = 0;
      
      // get the dimensions
      
      sizeX = in.readInt();
      sizeY = in.readInt();
      
      iMeta.addAxis(Axes.X, sizeX);
      iMeta.addAxis(Axes.Y, sizeY);
      
      if (sizeX < 1) {
        LOGGER.trace("Invalid width: {}; using the absolute value", sizeX);
        sizeX = Math.abs(sizeX);
      }
      if (sizeY < 1) {
        LOGGER.trace("Invalid height: {}; using the absolute value", sizeY);
        sizeY = Math.abs(sizeY);
        meta.setInvertY(true);
      }
      
      addGlobalMeta("Color planes", in.readShort());
      
      short bpp = in.readShort();
      
      iMeta.setBitsPerPixel(bpp);
      
      meta.setCompression(in.readInt());
      
      in.skipBytes(4);
      int pixelSizeX = in.readInt();
      int pixelSizeY = in.readInt();
      int nColors = in.readInt();
      if (nColors == 0 && bpp != 32 && bpp != 24) {
        nColors = bpp < 8 ? 1 << bpp : 256;
      }
      in.skipBytes(4);
      
      // read the palette, if it exists
      
      if (nColors != 0 && bpp == 8) {
        byte[][] palette = new byte[3][256];

        for (int i=0; i<nColors; i++) {
          for (int j=palette.length-1; j>=0; j--) {
            palette[j][i] = in.readByte();
          }
          in.skipBytes(1);
        }
        
        meta.palette = new ColorTable8(palette);
      }
      else if (nColors != 0) in.skipBytes(nColors * 4);
      
      if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
        addGlobalMeta("Indexed color", meta.getColorTable() != null);
        addGlobalMeta("Image width", sizeX);
        addGlobalMeta("Image height", sizeY);
        addGlobalMeta("Bits per pixel", bpp);
        String comp = "invalid";

        switch (meta.getCompression()) {
          case RAW:
            comp = "None";
            break;
          case RLE_8:
            comp = "8 bit run length encoding";
            break;
          case RLE_4:
            comp = "4 bit run length encoding";
            break;
          case RGB_MASK:
            comp = "RGB bitmap with mask";
            break;
        }

        addGlobalMeta("Compression type", comp);
        addGlobalMeta("X resolution", pixelSizeX);
        addGlobalMeta("Y resolution", pixelSizeY);
      }
    }
  }
  
  /**
  * @author Mark Hiner
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
      int compression = meta.getCompression();
      int bpp = meta.getBitsPerPixel(imageIndex);
      int sizeX = meta.getAxisLength(imageIndex, Axes.X);
      int sizeY = meta.getAxisLength(imageIndex, Axes.Y);
      int sizeC = meta.getAxisLength(imageIndex, Axes.CHANNEL);
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex,
          buf.length, x, y, w, h);
      
      if (compression != RAW && getStream().length() <
          FormatTools.getPlaneSize(this, imageIndex))
      {
        throw new UnsupportedCompressionException(compression + " not supported");
      }

      int rowsToSkip = meta.isInvertY() ? y : sizeY - (h + y);
      int rowLength = sizeX * (meta.isIndexed(imageIndex) ? 1 : sizeC);
      getStream().seek(meta.getGlobal() + rowsToSkip * rowLength);

      int pad = ((rowLength * bpp) / 8) % 2;
      if (pad == 0) pad = ((rowLength * bpp) / 8) % 4;
      else pad *= sizeC;
      int planeSize = sizeX * sizeC * h;
      if (bpp >= 8) planeSize *= (bpp / 8);
      else planeSize /= (8 / bpp);
      planeSize += pad * h;
      if (planeSize + getStream().getFilePointer() > getStream().length()) {
        planeSize -= (pad * h);

        // sometimes we have RGB images with a single padding byte
        if (planeSize + sizeY + getStream().getFilePointer() <=
            getStream().length())
        {
          pad = 1;
          planeSize += h;
        }
        else {
          pad = 0;
        }
      }

      getStream().skipBytes(rowsToSkip * pad);

      byte[] rawPlane = new byte[planeSize];
      getStream().read(rawPlane);

      BitBuffer bb = new BitBuffer(rawPlane);

      ColorTable palette = meta.getColorTable();
      plane.setColorTable(palette);
      
      int effectiveC = palette != null && palette.getLength() > 0 ? 1 : sizeC;
      for (int row=h-1; row>=0; row--) {
        int rowIndex = meta.isInvertY() ? h - 1 - row : row;
        bb.skipBits(x * bpp * effectiveC);
        for (int i=0; i<w*effectiveC; i++) {
          if (bpp <= 8) {
            buf[rowIndex * w * effectiveC + i] = (byte) (bb.getBits(bpp) & 0xff);
          }
          else {
            for (int b=0; b<bpp/8; b++) {
              buf[(bpp / 8) * (rowIndex * w * effectiveC + i) + b] =
                (byte) (bb.getBits(8) & 0xff);
            }
          }
        }
        if (row > 0) {
          bb.skipBits((sizeX - w - x) * bpp * effectiveC + pad*8);
        }
      }

      if (meta.getRGBChannelCount(imageIndex) > 1) {
        ImageTools.bgrToRgb(buf, meta.isInterleaved(imageIndex), 1,
            meta.getRGBChannelCount(imageIndex));
      }
      return plane;
    }
    
  }
}
