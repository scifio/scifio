/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.formats;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Vector;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * @author Mark Hiner hinerm at gmail.com
 *
 */
@Plugin(type = GIFFormat.class)
public class GIFFormat extends AbstractFormat {

  // -- Constants --

  public static final String GIF_MAGIC_STRING = "GIF";

  /*
   * @see io.scif.Format#getFormatName()
   */
  public String getFormatName() {
    return "Graphics Interchange Format";
  }

  /*
   * @see io.scif.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[]{"gif"};
  }
  
  // -- Nested Classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata implements HasColorTable {
    
    // -- Fields --
    
    private ColorTable8 cachedTable;

    /** Global color table. */
    private int[] gct;

    /** Active color table. */
    private int[] act;

    /** Interlace flag. */
    private boolean interlace;

    /** Current image rectangle. */
    private int ix, iy, iw, ih;

    /** Current data block. */
    private byte[] dBlock = new byte[256];

    /** Block size. */
    private int blockSize = 0;

    private int dispose = 0;
    private int lastDispose = 0;

    /** Use transparent color. */
    private boolean transparency = false;

    /** Transparent color index. */
    private int transIndex;

    // LZW working arrays
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private byte[] pixels;

    private Vector<byte[]> images;
    private Vector<int[]> colorTables;
    
    // -- GIFMetadata getters and setters --

    public int[] getGct() {
      return gct;
    }

    public void setGct(int[] gct) {
      this.gct = gct;
    }

    public int[] getAct() {
      return act;
    }

    public void setAct(int[] act) {
      this.act = act;
    }

    public boolean isInterlace() {
      return interlace;
    }

    public void setInterlace(boolean interlace) {
      this.interlace = interlace;
    }

    public int getIx() {
      return ix;
    }

    public void setIx(int ix) {
      this.ix = ix;
    }

    public int getIy() {
      return iy;
    }

    public void setIy(int iy) {
      this.iy = iy;
    }

    public int getIw() {
      return iw;
    }

    public void setIw(int iw) {
      this.iw = iw;
    }

    public int getIh() {
      return ih;
    }

    public void setIh(int ih) {
      this.ih = ih;
    }

    public byte[] getdBlock() {
      return dBlock;
    }

    public void setdBlock(byte[] dBlock) {
      this.dBlock = dBlock;
    }

    public int getBlockSize() {
      return blockSize;
    }

    public void setBlockSize(int blockSize) {
      this.blockSize = blockSize;
    }

    public int getDispose() {
      return dispose;
    }

    public void setDispose(int dispose) {
      this.dispose = dispose;
    }

    public int getLastDispose() {
      return lastDispose;
    }

    public void setLastDispose(int lastDispose) {
      this.lastDispose = lastDispose;
    }

    public boolean isTransparency() {
      return transparency;
    }

    public void setTransparency(boolean transparency) {
      this.transparency = transparency;
    }

    public int getTransIndex() {
      return transIndex;
    }

    public void setTransIndex(int transIndex) {
      this.transIndex = transIndex;
    }

    public short[] getPrefix() {
      return prefix;
    }

    public void setPrefix(short[] prefix) {
      this.prefix = prefix;
    }

    public byte[] getSuffix() {
      return suffix;
    }

    public void setSuffix(byte[] suffix) {
      this.suffix = suffix;
    }

    public byte[] getPixelStack() {
      return pixelStack;
    }

    public void setPixelStack(byte[] pixelStack) {
      this.pixelStack = pixelStack;
    }

    public byte[] getPixels() {
      return pixels;
    }

    public void setPixels(byte[] pixels) {
      this.pixels = pixels;
    }

    public Vector<byte[]> getImages() {
      return images;
    }

    public void setImages(Vector<byte[]> images) {
      this.images = images;
    }

    public Vector<int[]> getColorTables() {
      return colorTables;
    }

    public void setColorTables(Vector<int[]> colorTables) {
      this.colorTables = colorTables;
    }
    
    // -- Metadata API Methods --
    
    /*
     * @see io.scif.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      ImageMetadata iMeta = get(0);
      
      iMeta.setAxisLength(Axes.CHANNEL, 1);
      iMeta.setAxisLength(Axes.TIME, iMeta.getPlaneCount());
      iMeta.setAxisLength(Axes.Z, 1);
      
      iMeta.setRGB(false);
      iMeta.setLittleEndian(true);
      iMeta.setInterleaved(true);
      iMeta.setMetadataComplete(true);
      iMeta.setIndexed(true);
      iMeta.setFalseColor(false);
      iMeta.setPixelType(FormatTools.UINT8);
      iMeta.setBitsPerPixel(8);
    }
    
    @Override
    public void close(boolean fileOnly) throws IOException {
      int length = dBlock.length;
      super.close(fileOnly);
      if (!fileOnly) {
        interlace = transparency = false;
        ix = iy = iw = ih = blockSize = 0;
        dispose = lastDispose = transIndex = 0;
        gct = act;
        prefix = null;
        suffix = pixelStack = pixels = null;
        images = null;
        colorTables = null;
        dBlock = new byte[length];
      } 
    }
    
    // -- HasColorTable API Methods --
    
    /*
     * @see io.scif.HasColorTable#getColorTable()
     */
    public ColorTable getColorTable(int imageIndex, int planeIndex) {

      if (cachedTable == null) {
        byte[][] table = new byte[3][act.length];
        for (int i=0; i<act.length; i++) {
          table[0][i] = (byte) ((act[i] >> 16) & 0xff);
          table[1][i] = (byte) ((act[i] >> 8) & 0xff);
          table[2][i] = (byte) (act[i] & 0xff);
        }
        cachedTable = new ColorTable8(table);

      }
      
      return cachedTable;
    }
    
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {
    
    // -- Checker API methods --
    
    @Override
    public boolean isFormat(RandomAccessInputStream in) throws IOException {
      final int blockLen = GIF_MAGIC_STRING.length();
      if (!FormatTools.validStream(in, blockLen, false)) return false;
      return in.readString(blockLen).startsWith(GIF_MAGIC_STRING);
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Constants --
    
    private static final int IMAGE_SEPARATOR = 0x2c;
    private static final int EXTENSION = 0x21;
    private static final int END = 0x3b;
    private static final int GRAPHICS = 0xf9; 
    
    /** Maximum buffer size. */
    private static final int MAX_STACK_SIZE = 4096; 
    
    // -- Parser API Methods --

    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException
    {
      LOGGER.info("Verifying GIF format");

      stream.order(true);
      meta.setImages(new Vector<byte[]>());
      meta.setColorTables(new Vector<int[]>());

      String ident = in.readString(6);

      if (!ident.startsWith(GIF_MAGIC_STRING)) {
        throw new FormatException("Not a valid GIF file.");
      }

      LOGGER.info("Reading dimensions");

      meta.createImageMetadata(1);
      ImageMetadata iMeta = meta.get(0);
      
      iMeta.setAxisLength(Axes.X, stream.readShort());
      iMeta.setAxisLength(Axes.Y, stream.readShort());

      int packed = stream.read() & 0xff;
      boolean gctFlag = (packed & 0x80) != 0;
      int gctSize = 2 << (packed & 7);
      stream.skipBytes(2);
      addGlobalMeta("Global lookup table size", gctSize);

      if (gctFlag) {
        meta.setGct(readLut(gctSize));
      }

      LOGGER.info("Reading data blocks");

      boolean done = false;
      while (!done) {
        int code = stream.read() & 0xff;
        switch (code) {
          case IMAGE_SEPARATOR:
            readImageBlock();
            break;
          case EXTENSION:
            code = stream.read() & 0xff;
            switch (code) {
              case GRAPHICS:
                stream.skipBytes(1);
                packed = stream.read() & 0xff;
                meta.setDispose((packed & 0x1c) >> 1);
                meta.setTransparency((packed & 1) != 0);
                stream.skipBytes(2);
                meta.setTransIndex(stream.read() & 0xff);
                stream.skipBytes(1);
                break;
              default:
                if (readBlock() == -1) {
                  done = true;
                  break;
                }
                skipBlocks();
            }
            break;
          case END:
            done = true;
            break;
        }
      }
      
      meta.setAct(meta.getColorTables().get(0));
    }

    // -- Helper Methods --

    private void skipBlocks() throws IOException {
      int check = 0;
      do { check = readBlock(); }
      while (metadata.getBlockSize() > 0 && check != -1);
    }

    private void readImageBlock() throws FormatException, IOException {
      metadata.setIx(in.readShort());
      metadata.setIy(in.readShort());
      metadata.setIw(in.readShort());
      metadata.setIh(in.readShort());

      int packed = in.read();
      boolean lctFlag = (packed & 0x80) != 0;
      metadata.setInterlace((packed & 0x40) != 0);
      int lctSize = 2 << (packed & 7);

      metadata.setAct(lctFlag ? readLut(lctSize) : metadata.getGct());

      if (metadata.getAct() == null) throw new FormatException("Color table not found.");
      
      int save = 0;

      if (metadata.isTransparency()) {
        save = metadata.getAct()[metadata.getTransIndex()];
        metadata.getAct()[metadata.getTransIndex()] = 0;
      }

      decodeImageData();
      skipBlocks();

      metadata.get(0).setPlaneCount(metadata.getPlaneCount(0) + 1);

      if (metadata.isTransparency()) metadata.getAct()[metadata.getTransIndex()] = save;

      metadata.setLastDispose(metadata.getDispose());
    } 

    /** Decodes LZW image data into a pixel array.  Adapted from ImageMagick. */
    private void decodeImageData() throws IOException {
      int nullCode = -1;
      int npix = metadata.getIw() * metadata.getIh();
      
      byte[] pixels = metadata.getPixels();

      if (pixels == null || pixels.length < npix) pixels = new byte[npix];
      
      short[] prefix = metadata.getPrefix();
      byte[] suffix = metadata.getSuffix();
      byte[] pixelStack = metadata.getPixelStack();

      if (prefix == null) prefix = new short[MAX_STACK_SIZE];
      if (suffix == null) suffix = new byte[MAX_STACK_SIZE];
      if (pixelStack == null) pixelStack = new byte[MAX_STACK_SIZE + 1];
      
      metadata.setPrefix(prefix);
      metadata.setSuffix(suffix);
      metadata.setPixelStack(pixelStack);

      // initialize GIF data stream decoder

      int dataSize = in.read() & 0xff;

      int clear = 1 << dataSize;
      int eoi = clear + 1;
      int available = clear + 2;
      int oldCode = nullCode;
      int codeSize = dataSize + 1;
      int codeMask = (1 << codeSize) - 1;
      int code = 0, inCode = 0;
      for (code=0; code<clear; code++) {
        prefix[code] = 0;
        suffix[code] = (byte) code;
      } 

      // decode GIF pixel stream

      int datum = 0, first = 0, top = 0, pi = 0, bi = 0, bits = 0, count = 0;
      int i = 0;

      for (i=0; i<npix;) {
        if (top == 0) {
          if (bits < codeSize) {
            if (count == 0) {
              count = readBlock();
              if (count <= 0) break;
              bi = 0;
            }
            datum += (metadata.getdBlock()[bi] & 0xff) << bits;
            bits += 8;
            bi++;
            count--;
            continue;
          }

          // get the next code
          code = datum & codeMask;
          datum >>= codeSize;
          bits -= codeSize;

          // interpret the code

          if ((code > available) || (code == eoi)) {
            break;
          }
          if (code == clear) {
            // reset the decoder
            codeSize = dataSize + 1;
            codeMask = (1 << codeSize) - 1;
            available = clear + 2;
            oldCode = nullCode;
            continue;
          }

          if (oldCode == nullCode) {
            pixelStack[top++] = suffix[code];
            oldCode = code;
            first = code;
            continue;
          }

          inCode = code;
          if (code == available) {
            pixelStack[top++] = (byte) first;
            code = oldCode;
          }

          while (code > clear) {
            pixelStack[top++] = suffix[code];
            code = prefix[code];
          }
          first = suffix[code] & 0xff;

          if (available >= MAX_STACK_SIZE) break;
          pixelStack[top++] = (byte) first;
          prefix[available] = (short) oldCode;
          suffix[available] = (byte) first;
          available++;

          if (((available & codeMask) == 0) && (available < MAX_STACK_SIZE)) {
            codeSize++;
            codeMask += available;
          }
          oldCode = inCode;
        }
        top--;
        pixels[pi++] = pixelStack[top];
        i++;
      }

      for (i=pi; i<npix; i++) pixels[i] = 0;
      metadata.setPixels(pixels);
      setPixels();
    }  

    private void setPixels() {
      // expose destination image's pixels as an int array
      byte[] dest = new byte[metadata.getAxisLength(0, Axes.X) *
                             metadata.getAxisLength(0, Axes.Y)];
      int lastImage = -1;

      // fill in starting image contents based on last image's dispose code
      if (metadata.getLastDispose() > 0) {
        if (metadata.getLastDispose() == 3) { // use image before last
          int n = metadata.getPlaneCount(0) - 2;
          if (n > 0) lastImage = n - 1;
        }

        if (lastImage != -1) {
          byte[] prev = metadata.getImages().get(lastImage);
          System.arraycopy(prev, 0, dest, 0, metadata.getAxisLength(0, Axes.X) *
              metadata.getAxisLength(0, Axes.Y));
        }
      }

      // copy each source line to the appropriate place in the destination

      int pass = 1;
      int inc = 8;
      int iline = 0;
      for (int i=0; i<metadata.getIh(); i++) {
        int line = i;
        if (metadata.isInterlace()) {
          if (iline >= metadata.getIh()) {
            pass++;
            switch (pass) {
              case 2:
                iline = 4;
                break;
              case 3:
                iline = 2;
                inc = 4;
                break;
              case 4:
                iline = 1;
                inc = 2;
                break;
            }
          }
          line = iline;
          iline += inc;
        }
        line += metadata.getIy();
        if (line < metadata.getAxisLength(0, Axes.Y)) {
          int k = line * metadata.getAxisLength(0, Axes.X);
          int dx = k + metadata.getIx(); // start of line in dest
          int dlim = dx + metadata.getIw(); // end of dest line
          if ((k + metadata.getAxisLength(0, Axes.X)) < dlim) 
            dlim = k + metadata.getAxisLength(0, Axes.X);
          int sx = i * metadata.getIw(); // start of line in source
          while (dx < dlim) {
            // map color and insert in destination
            int index = metadata.getPixels()[sx++] & 0xff;
            dest[dx++] = (byte) index;
          }
        }
      }
      metadata.getColorTables().add(metadata.getAct());
      metadata.getImages().add(dest);
    } 

    /** Reads the next variable length block. */
    private int readBlock() throws IOException {
      if (in.getFilePointer() == in.length()) return -1;
      metadata.setBlockSize(in.read() & 0xff);
      int n = 0;
      int count;

      if (metadata.getBlockSize() > 0) {
        try {
          while (n < metadata.getBlockSize()) {
            count = in.read(metadata.getdBlock(), n, metadata.getBlockSize() - n);
            if (count == -1) break;
            n += count;
          }
        }
        catch (IOException e) {
          LOGGER.trace("Truncated block", e);
        }
      }
      return n;
    } 
    
    /** Read a color lookup table of the specified size. */
    private int[] readLut(int size) throws FormatException {
      int nbytes = 3 * size;
      byte[] c = new byte[nbytes];
      int n = 0;
      try { n = in.read(c); }
      catch (IOException e) { }

      if (n < nbytes) {
        throw new FormatException("Color table not found");
      }

      int[] lut = new int[256];
      int j = 0;
      for (int i=0; i<size; i++) {
        int r = c[j++] & 0xff;
        int g = c[j++] & 0xff;
        int b = c[j++] & 0xff;
        lut[i] = 0xff000000 | (r << 16) | (g << 8) | b;
      }
      return lut;
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
     * @see io.scif.Reader#openPlane(int, int, Plane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h)
      throws FormatException, IOException
    {
      byte[] buf = plane.getData();
      Metadata meta = getMetadata();
      plane.setColorTable(meta.getColorTable(0, 0));
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex,
          buf.length, x, y, w, h);

      int[] act = meta.getColorTables().get(planeIndex);

      byte[] b = meta.getImages().get(planeIndex);
      if (planeIndex > 0 && meta.isTransparency()) {
        byte[] prev = meta.getImages().get(planeIndex - 1);
        int idx = meta.getTransIndex();
        if (idx >= 127) idx = 0;
        for (int i=0; i<b.length; i++) {
          if ((act[b[i] & 0xff] & 0xffffff) == idx) {
            b[i] = prev[i];
          }
        }
        meta.getImages().setElementAt(b, planeIndex);
      }

      for (int row=0; row<h; row++) {
        System.arraycopy(b, (row + y) * meta.getAxisLength(imageIndex, Axes.X) + x,
            buf, row*w, w);
      }

      return plane;
    }
  }
}
