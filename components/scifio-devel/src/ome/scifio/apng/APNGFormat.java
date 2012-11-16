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

package ome.scifio.apng;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.AbstractTranslator;
import ome.scifio.AbstractWriter;
import ome.scifio.BufferedImagePlane;
import ome.scifio.DefaultDatasetMetadata;
import ome.scifio.DefaultImageMetadata;
import ome.scifio.CoreTranslator;
import ome.scifio.Field;
import ome.scifio.FieldPrinter;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.discovery.SCIFIOFormat;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.gui.AWTImageTools;
import ome.scifio.gui.BufferedImageReader;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.io.RandomAccessOutputStream;
import ome.scifio.io.StreamTools;
import ome.scifio.util.FormatTools;

@SCIFIOFormat
public class APNGFormat
  extends
  AbstractFormat<APNGFormat.Metadata, APNGFormat.Checker,
      APNGFormat.Parser, APNGFormat.Reader,
      APNGFormat.Writer> {
 
  // -- Constants --

  public static final byte[] PNG_SIGNATURE = new byte[] {
      (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
  
  // -- Constructor --

  public APNGFormat() throws FormatException {
    this(null);
  }
  
  public APNGFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, "Animated PNG", "png", Metadata.class, Checker.class, Parser.class, Reader.class, Writer.class);
  }

  /**
   * File format SCIFIO Metadata for Animated Portable Network Graphics
   * (APNG) images.
   *
   */
  public static class Metadata extends AbstractMetadata {
  
    // -- Fields --
  
    private List<APNGIDATChunk> idat;
  
    private List<APNGfcTLChunk> fctl;
  
    private APNGacTLChunk actl;
  
    private APNGIHDRChunk ihdr;
  
    private APNGPLTEChunk plte;
    
    private APNGIENDChunk iend;
  
    // true if the default image is not part of the animation
    private boolean separateDefault;
  
    // -- Constructor --
  
    public Metadata() {
      this(null);
    }
  
    public Metadata(final SCIFIO context) {
      super(context);
      fctl = new ArrayList<APNGfcTLChunk>();
      idat = new ArrayList<APNGIDATChunk>();
    }
  
    // -- Getters and Setters --
  
    public List<APNGIDATChunk> getIdat() {
      return idat;
    }
  
    public void setIdat(final List<APNGIDATChunk> idat) {
      this.idat = idat;
    }
  
    public void addIdat(final APNGIDATChunk idat) {
      this.idat.add(idat);
    }
  
    public void setSeparateDefault(final boolean separateDefault) {
      this.separateDefault = separateDefault;
    }
  
    public boolean isSeparateDefault() {
      return separateDefault;
    }
  
    public List<APNGfcTLChunk> getFctl() {
      return fctl;
    }
  
    public void setFctl(final List<APNGfcTLChunk> fctl) {
      this.fctl = fctl;
    }
  
    public APNGacTLChunk getActl() {
      return actl;
    }
  
    public void setActl(final APNGacTLChunk actl) {
      this.actl = actl;
    }
  
    public APNGIHDRChunk getIhdr() {
      return ihdr;
    }
  
    public void setIhdr(final APNGIHDRChunk ihdr) {
      this.ihdr = ihdr;
    }
  
    public APNGPLTEChunk getPlte() {
      return plte;
    }
  
    public void setPlte(final APNGPLTEChunk plte) {
      this.plte = plte;
    }
    
    public APNGIENDChunk getIend() {
    return iend;
    }
  
    public void setIend(APNGIENDChunk iend) {
    this.iend = iend;
    }
  
    // -- Helper Methods --
  
  
  /* @see Metadata#resetMeta() */
    public void reset() {
      super.reset(getClass());
      fctl = new ArrayList<APNGfcTLChunk>();
      idat = new ArrayList<APNGIDATChunk>();
    }
  }

  /**
   * File format SCIFIO Checker for Animated Portable Network Graphics
   * (APNG) images.
   *
   */
  public static class Checker extends AbstractChecker<Metadata> {
  
    // -- Fields --
  
    // -- Constructor --
  
    /** Constructs a new APNGChecker */
    public Checker(final SCIFIO ctx) {
      super(ctx);
      suffixNecessary = false;
    }
  
    public Checker() {
      this(null);
    }
  
    // -- Checker API Methods --
  
    /* @see ome.scifio.Checker#isFormat(RandomAccessInputStream stream) */
    @Override
    public boolean isFormat(final RandomAccessInputStream stream) throws IOException {
      final int blockLen = 8;
      if (!StreamTools.validStream(stream, blockLen, false)) return false;
  
      final byte[] signature = new byte[blockLen];
      stream.read(signature);
  
      if (signature[0] != (byte) 0x89 || signature[1] != 0x50 ||
        signature[2] != 0x4e || signature[3] != 0x47 || signature[4] != 0x0d ||
        signature[5] != 0x0a || signature[6] != 0x1a || signature[7] != 0x0a)
      {
        return false;
      }
      return true;
    }
  
    // -- MetadataHandler API Methods --
  
    /* @see MetadataHandler#getMetadataTypes() */
    public Class<Metadata> getMetadataType() {
      return Metadata.class;
    }
  }

  /**
   * File format SCIFIO Parser for Animated Portable Network Graphics
   * (APNG) images.
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
  
    // -- Fields --
  
    // -- Constructor --
  
    /** Constructs a new APNGParser. */
    public Parser() {
      this(null);
    }
  
    public Parser(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Parser API Methods --
  
    /* @see ome.scifio.AbstractParser#parse(RandomAccessInputStream stream) */
    @Override
    public Metadata parse(final RandomAccessInputStream stream)
      throws IOException, FormatException
    {
      super.parse(stream);
  
      // check that this is a valid PNG file
      final byte[] signature = new byte[8];
      in.read(signature);
  
      if (signature[0] != (byte) 0x89 || signature[1] != 0x50 ||
        signature[2] != 0x4e || signature[3] != 0x47 || signature[4] != 0x0d ||
        signature[5] != 0x0a || signature[6] != 0x1a || signature[7] != 0x0a)
      {
        throw new FormatException("Invalid PNG signature.");
      }
  
      // For determining if the first frame is also the default image
      boolean sawFctl = false;
  
      // read data chunks - each chunk consists of the following:
      // 1) 32 bit length
      // 2) 4 char type
      // 3) 'length' bytes of data
      // 4) 32 bit CRC
  
      while (in.getFilePointer() < in.length()) {
        final int length = in.readInt();
        final String type = in.readString(4);
        final long offset = in.getFilePointer();
  
        APNGChunk chunk = null;
  
        if (type.equals("acTL")) {
          chunk = new APNGacTLChunk();
          ((APNGacTLChunk) chunk).setNumFrames(in.readInt());
          ((APNGacTLChunk) chunk).setNumPlays(in.readInt());
          metadata.setActl(((APNGacTLChunk) chunk));
        }
        else if (type.equals("fcTL")) {
          sawFctl = true;
          chunk = new APNGfcTLChunk();
          ((APNGfcTLChunk) chunk).setSequenceNumber(in.readInt());
          ((APNGfcTLChunk) chunk).setWidth(in.readInt());
          ((APNGfcTLChunk) chunk).setHeight(in.readInt());
          ((APNGfcTLChunk) chunk).setxOffset(in.readInt());
          ((APNGfcTLChunk) chunk).setyOffset(in.readInt());
          ((APNGfcTLChunk) chunk).setDelayNum(in.readShort());
          ((APNGfcTLChunk) chunk).setDelayDen(in.readShort());
          ((APNGfcTLChunk) chunk).setDisposeOp(in.readByte());
          ((APNGfcTLChunk) chunk).setBlendOp(in.readByte());
          metadata.getFctl().add(((APNGfcTLChunk) chunk));
        }
        else if (type.equals("IDAT")) {
          metadata.setSeparateDefault(!sawFctl);
          chunk = new APNGIDATChunk();
          metadata.addIdat(((APNGIDATChunk) chunk));
          in.skipBytes(length);
        }
        else if (type.equals("fdAT")) {
          chunk = new APNGfdATChunk();
          ((APNGfdATChunk) chunk).setSequenceNumber(in.readInt());
          metadata.getFctl()
            .get(metadata.getFctl().size() - 1)
            .addChunk(((APNGfdATChunk) chunk));
          in.skipBytes(length - 4);
        }
        else if (type.equals("IHDR")) {
          chunk = new APNGIHDRChunk();
          ((APNGIHDRChunk) chunk).setWidth(in.readInt());
          ((APNGIHDRChunk) chunk).setHeight(in.readInt());
          ((APNGIHDRChunk) chunk).setBitDepth(in.readByte());
          ((APNGIHDRChunk) chunk).setColourType(in.readByte());
          ((APNGIHDRChunk) chunk).setCompressionMethod(in.readByte());
          ((APNGIHDRChunk) chunk).setFilterMethod(in.readByte());
          ((APNGIHDRChunk) chunk).setInterlaceMethod(in.readByte());
          metadata.setIhdr(((APNGIHDRChunk) chunk));
        }
        else if (type.equals("PLTE")) {
          chunk = new APNGPLTEChunk();
          final byte[] red = new byte[length / 3];
          final byte[] blue = new byte[length / 3];
          final byte[] green = new byte[length / 3];
  
          for (int i = 0; i < length; i += 3) {
            red[i] = in.readByte();
            green[i] = in.readByte();
            blue[i] = in.readByte();
          }
  
          ((APNGPLTEChunk) chunk).setRed(red);
          ((APNGPLTEChunk) chunk).setGreen(green);
          ((APNGPLTEChunk) chunk).setBlue(blue);
  
          metadata.setPlte(((APNGPLTEChunk) chunk));
        }
        else if(type.equals("IEND")) {
          chunk = new APNGIENDChunk();
          in.skipBytes((int) (in.length() - in.getFilePointer()));
          metadata.setIend((APNGIENDChunk) chunk);
        }
        else in.skipBytes(length);
  
        if (chunk != null) {
          chunk.setOffset(offset);
          chunk.setLength(length);
        }
  
        if (in.getFilePointer() < in.length() - 4) {
          in.skipBytes(4); // skip the CRC
        }
      }
  
      return metadata;
    }
  }

  /**
   * File format SCIFIO Reader for Animated Portable Network Graphics (APNG)
   * images.
   * 
   */
  public static class Reader extends BufferedImageReader<Metadata> {
  
    // -- Fields --
  
    // Cached copy of the last plane that was returned.
    private BufferedImagePlane lastPlane;
  
    // Plane index of the last plane that was returned.
    private int lastPlaneIndex = -1;
  
    // -- Constructor --
  
    /** Constructs a new APNGReader. */
  
    public Reader() {
      this(null);
    }
  
    public Reader(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Reader API Methods --
  
    /* @see ome.scifio.Reader#openPlane(int, int, int, int, int) */
    public BufferedImagePlane openPlane(final int imageIndex, final int planeIndex,
      final BufferedImagePlane plane, final int x, final int y, final int w,
      final int h) throws FormatException, IOException
    {
      FormatTools.checkPlaneParameters(
        this, imageIndex, planeIndex, -1, x, y, w, h);
  
      // If the last processed (cached) plane is requested, return it
      if (planeIndex == lastPlaneIndex && lastPlane != null) {
        return lastPlane;
      }
      else if (lastPlane == null) {
        lastPlane = new BufferedImagePlane(getContext(), 
            getDatasetMetadata().get(imageIndex), x, y, w, h);
        
        if (getDatasetMetadata().isIndexed(imageIndex)) {
          APNGPLTEChunk plte = getMetadata().getPlte();
          ColorTable ct = new ColorTable8(plte.getRed(), plte.getGreen(),
              plte.getBlue());
          lastPlane.setColorTable(ct);
        }
      }
  
      // The default frame is requested and we can use the standard 
      // Java ImageIO to extract it
      if (planeIndex == 0) {
        in.seek(0);
        final DataInputStream dis =
          new DataInputStream(new BufferedInputStream(in, 4096));
        BufferedImage subImg = ImageIO.read(dis);
        lastPlane.populate(getDatasetMetadata().get(imageIndex), subImg,
            x, y, w, h);
        
        lastPlaneIndex = 0;
        
        if (x != 0 || y != 0 || w != dMeta.getAxisLength(imageIndex, Axes.X) ||
          h != dMeta.getAxisLength(imageIndex, Axes.Y))
        {
          // updates the data of lastPlane to a sub-image, by reference
          subImg = AWTImageTools.getSubimage(
              lastPlane.getData(), dMeta.isLittleEndian(planeIndex),
              x, y, w, h);
        }

        return lastPlane;
      }
  
      // For a non-default frame, the appropriate chunks will be used to create a new image,
      // which will be read with the standard Java ImageIO and pasted onto frame 0.
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      stream.write(APNGFormat.PNG_SIGNATURE);
  
      final int[] coords = metadata.getFctl().get(planeIndex).getFrameCoordinates();
      // process IHDR chunk
      final APNGIHDRChunk ihdr = metadata.getIhdr();
      processChunk(
        imageIndex, ihdr.getLength(), ihdr.getOffset(), coords, stream, true);
  
      // process fcTL and fdAT chunks
      final APNGfcTLChunk fctl =
        metadata.getFctl().get(
          metadata.isSeparateDefault() ? planeIndex - 1 : planeIndex);
  
      // fdAT chunks are converted to IDAT chunks, as we are essentially building a standalone single-frame image
      for (final APNGfdATChunk fdat : fctl.getFdatChunks()) {
        in.seek(fdat.getOffset() + 4);
        byte[] b = new byte[fdat.getLength() + 8];
        DataTools.unpackBytes(
          fdat.getLength() - 4, b, 0, 4, dMeta.isLittleEndian(imageIndex));
        b[4] = 'I';
        b[5] = 'D';
        b[6] = 'A';
        b[7] = 'T';
        in.read(b, 8, b.length - 12);
        final int crc = (int) computeCRC(b, b.length - 4);
        DataTools.unpackBytes(
          crc, b, b.length - 4, 4, dMeta.isLittleEndian(imageIndex));
        stream.write(b);
        b = null;
      }
  
      // process PLTE chunks
      final APNGPLTEChunk plte = metadata.getPlte();
      if (plte != null) {
        processChunk(
          imageIndex, plte.getLength(), plte.getOffset(), coords, stream, false);
      }
      final RandomAccessInputStream s =
        new RandomAccessInputStream(stream.toByteArray());
      final DataInputStream dis = new DataInputStream(new BufferedInputStream(s, 4096));
      final BufferedImage bi = ImageIO.read(dis);
      dis.close();
  
      // Recover first plane

      openPlane(
        imageIndex, 0, 0, 0, dMeta.getAxisLength(imageIndex, Axes.X),
        dMeta.getAxisLength(imageIndex, Axes.Y));
  
      // paste current image onto first plane
      // NB: last plane read was the first plane
  
      final WritableRaster firstRaster = lastPlane.getData().getRaster();
      final WritableRaster currentRaster = bi.getRaster();
  
      firstRaster.setDataElements(coords[0], coords[1], currentRaster);
      BufferedImage bImg =
        new BufferedImage(lastPlane.getData().getColorModel(), firstRaster, false, null);
      
      lastPlane.populate(getDatasetMetadata().get(imageIndex), bImg,
          x, y, w, h);
      
      lastPlaneIndex = planeIndex;
      return lastPlane;
    }
  
    // -- Helper methods --
  
    private long computeCRC(final byte[] buf, final int len) {
      final CRC32 crc = new CRC32();
      crc.update(buf, 0, len);
      return crc.getValue();
    }
  
    private void processChunk(final int imageIndex, final int length, final long offset,
      final int[] coords, final ByteArrayOutputStream stream, final boolean isIHDR)
      throws IOException
    {
      byte[] b = new byte[length + 12];
      DataTools.unpackBytes(length, b, 0, 4, dMeta.isLittleEndian(imageIndex));
      final byte[] typeBytes = (isIHDR ? "IHDR".getBytes() : "PLTE".getBytes());
      System.arraycopy(typeBytes, 0, b, 4, 4);
      in.seek(offset);
      in.read(b, 8, b.length - 12);
      if (isIHDR) {
        DataTools.unpackBytes(
          coords[2], b, 8, 4, dMeta.isLittleEndian(imageIndex));
        DataTools.unpackBytes(
          coords[3], b, 12, 4, dMeta.isLittleEndian(imageIndex));
      }
      final int crc = (int) computeCRC(b, b.length - 4);
      DataTools.unpackBytes(
        crc, b, b.length - 4, 4, dMeta.isLittleEndian(imageIndex));
      stream.write(b);
      b = null;
    }
  }

  /**
   * The SCIFIO file format writer for PNG and APNG files.
   * 
   */
  public static class Writer extends AbstractWriter<Metadata> {

    // -- Fields --

    // Number of frames written
    private int numFrames = 0;

    // Pointer to position in acTL chunk to write the number of frames in this
    // image
    private long numFramesPointer = 0;

    // Current sequence number, shared by fcTL and fdAT frames to indicate
    // ordering
    private int nextSequenceNumber;
    private boolean littleEndian;

    // -- Constructor --

    public Writer() {
      this(null);
    }

    public Writer(final SCIFIO ctx) {
      super(ctx);
    }

    // -- Writer API Methods --

    /**
     * @see ome.scifio.Writer#saveBytes(int, byte[], int, int, int, int)
     */
    public void saveBytes(final int imageIndex, final int planeIndex,
        final byte[] buf, final int x, final int y, final int w, final int h)
        throws FormatException, IOException {
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);
      if (!isFullPlane(imageIndex, x, y, w, h)) {
        throw new FormatException(
            "APNGWriter does not yet support saving image tiles.");
      }

      final int width = dMeta.getAxisLength(imageIndex, Axes.X);
      final int height = dMeta.getAxisLength(imageIndex, Axes.Y);

      if (!initialized[imageIndex][planeIndex]) {
        if (numFrames == 0) {
          if (!metadata.isSeparateDefault()) {
            // first frame is default image
            writeFCTL(width, height, planeIndex);
          }
          writePLTE();
        }
        initialized[imageIndex][planeIndex] = true;
      }

      // write the data for this frame

      if (numFrames == 0) {
        // This is the first frame, and also the default image
        writePixels(imageIndex, "IDAT", buf, x, y, w, h);
      } else {
        writeFCTL(width, height, planeIndex);
        writePixels(imageIndex, "fdAT", buf, x, y, w, h);
      }
      numFrames++;
    }

    /* @see ome.scifio.Writer#canDoStacks() */
    public boolean canDoStacks() {
      return true;
    }

    /* @see ome.scifio.Writer#getPixelTypes(String) */
    public int[] getPixelTypes(final String codec) {
      return new int[] { FormatTools.INT8, FormatTools.UINT8,
          FormatTools.INT16, FormatTools.UINT16 };
    }

    // -- APNGWriter Methods --

    /* @see ome.scifio.Writer#close() */
    public void close() throws IOException {
      if (out != null) {
        writeFooter();
      }
      super.close();
      numFrames = 0;
      numFramesPointer = 0;
      nextSequenceNumber = 0;
      littleEndian = false;
    }

    /* @see ome.scifio.Writer#setDest(RandomAccessOutputStream, int) */
    public void setDest(final RandomAccessOutputStream out, final int imageIndex)
        throws FormatException, IOException {
      super.setDest(out, imageIndex);
      initialize(imageIndex);
    }

    // -- Helper Methods --

    private void initialize(final int imageIndex) throws FormatException,
        IOException {
      if (out.length() == 0) {
        final int width = dMeta.getAxisLength(imageIndex, Axes.X);
        final int height = dMeta.getAxisLength(imageIndex, Axes.Y);
        final int bytesPerPixel = FormatTools.getBytesPerPixel(dMeta
            .getPixelType(imageIndex));
        final int nChannels = dMeta.getAxisLength(imageIndex, Axes.CHANNEL);
        final boolean indexed = getColorModel() != null
            && (getColorModel() instanceof IndexColorModel);
        littleEndian = dMeta.isLittleEndian(imageIndex);

        // write 8-byte PNG signature
        out.write(APNGFormat.PNG_SIGNATURE);

        // write IHDR chunk
        out.writeInt(13);
        final byte[] b = new byte[17];
        b[0] = 'I';
        b[1] = 'H';
        b[2] = 'D';
        b[3] = 'R';

        DataTools.unpackBytes(width, b, 4, 4, false);
        DataTools.unpackBytes(height, b, 8, 4, false);

        b[12] = (byte) (bytesPerPixel * 8);
        if (indexed)
          b[13] = (byte) 3;
        else if (nChannels == 1)
          b[13] = (byte) 0;
        else if (nChannels == 2)
          b[13] = (byte) 4;
        else if (nChannels == 3)
          b[13] = (byte) 2;
        else if (nChannels == 4)
          b[13] = (byte) 6;
        b[14] = metadata.getIhdr().getCompressionMethod();
        b[15] = metadata.getIhdr().getFilterMethod();
        b[16] = metadata.getIhdr().getInterlaceMethod();

        out.write(b);
        out.writeInt(crc(b));

        // write acTL chunk

        final APNGacTLChunk actl = metadata.getActl();

        out.writeInt(8);
        out.writeBytes("acTL");
        numFramesPointer = out.getFilePointer();
        out.writeInt(actl == null ? 0 : actl.getNumFrames());
        out.writeInt(actl == null ? 0 : actl.getNumPlays());
        out.writeInt(0); // save a place for the CRC
      }
    }

    private int crc(final byte[] buf) {
      return crc(buf, 0, buf.length);
    }

    private int crc(final byte[] buf, final int off, final int len) {
      final CRC32 crc = new CRC32();
      crc.update(buf, off, len);
      return (int) crc.getValue();
    }

    private void writeFCTL(final int width, final int height,
        final int planeIndex) throws IOException {
      out.writeInt(26);
      final APNGfcTLChunk fctl = metadata.getFctl().get(
          metadata.isSeparateDefault() ? planeIndex - 1 : planeIndex);
      final byte[] b = new byte[30];

      DataTools.unpackBytes(22, b, 0, 4, false);
      b[0] = 'f';
      b[1] = 'c';
      b[2] = 'T';
      b[3] = 'L';

      DataTools.unpackBytes(nextSequenceNumber++, b, 4, 4, false);
      DataTools.unpackBytes(fctl.getWidth(), b, 8, 4, false);
      DataTools.unpackBytes(fctl.getHeight(), b, 12, 4, false);
      DataTools.unpackBytes(fctl.getxOffset(), b, 16, 4, false);
      DataTools.unpackBytes(fctl.getyOffset(), b, 20, 4, false);
      DataTools.unpackBytes(fctl.getDelayNum(), b, 24, 2, false);
      DataTools.unpackBytes(fctl.getDelayDen(), b, 26, 2, false);
      b[28] = fctl.getDisposeOp();
      b[29] = fctl.getBlendOp();

      out.write(b);
      out.writeInt(crc(b));
    }

    private void writePLTE() throws IOException {
      if (!(getColorModel() instanceof IndexColorModel))
        return;

      final IndexColorModel model = (IndexColorModel) getColorModel();
      final byte[][] lut = new byte[3][256];
      model.getReds(lut[0]);
      model.getGreens(lut[1]);
      model.getBlues(lut[2]);

      out.writeInt(768);
      final byte[] b = new byte[772];
      b[0] = 'P';
      b[1] = 'L';
      b[2] = 'T';
      b[3] = 'E';

      for (int i = 0; i < lut[0].length; i++) {
        for (int j = 0; j < lut.length; j++) {
          b[i * lut.length + j + 4] = lut[j][i];
        }
      }

      out.write(b);
      out.writeInt(crc(b));
    }

    private void writePixels(final int imageIndex, final String chunk,
        final byte[] stream, final int x, final int y, final int width,
        final int height) throws FormatException, IOException {
      final int sizeC = dMeta.getAxisLength(imageIndex, Axes.CHANNEL);
      final int pixelType = dMeta.getPixelType(imageIndex);
      final boolean signed = FormatTools.isSigned(pixelType);

      if (!isFullPlane(imageIndex, x, y, width, height)) {
        throw new FormatException(
            "APNGWriter does not support writing tiles.");
      }

      final ByteArrayOutputStream s = new ByteArrayOutputStream();
      s.write(chunk.getBytes());
      if (chunk.equals("fdAT")) {
        s.write(DataTools.intToBytes(nextSequenceNumber++, false));
      }
      final DeflaterOutputStream deflater = new DeflaterOutputStream(s);
      final int planeSize = stream.length / sizeC;
      final int rowLen = stream.length / height;
      final int bytesPerPixel = stream.length / (width * height * sizeC);
      final byte[] rowBuf = new byte[rowLen];
      for (int i = 0; i < height; i++) {
        deflater.write(0);
        if (interleaved) {
          if (littleEndian) {
            for (int col = 0; col < width * sizeC; col++) {
              final int offset = (i * sizeC * width + col)
                  * bytesPerPixel;
              final int pixel = DataTools.bytesToInt(stream, offset,
                  bytesPerPixel, littleEndian);
              DataTools.unpackBytes(pixel, rowBuf, col
                  * bytesPerPixel, bytesPerPixel, false);
            }
          } else
            System.arraycopy(stream, i * rowLen, rowBuf, 0, rowLen);
        } else {
          final int max = (int) Math.pow(2, bytesPerPixel * 8 - 1);
          for (int col = 0; col < width; col++) {
            for (int c = 0; c < sizeC; c++) {
              final int offset = c * planeSize + (i * width + col)
                  * bytesPerPixel;
              int pixel = DataTools.bytesToInt(stream, offset,
                  bytesPerPixel, littleEndian);
              if (signed) {
                if (pixel < max)
                  pixel += max;
                else
                  pixel -= max;
              }
              final int output = (col * sizeC + c) * bytesPerPixel;
              DataTools.unpackBytes(pixel, rowBuf, output,
                  bytesPerPixel, false);
            }
          }
        }
        deflater.write(rowBuf);
      }
      deflater.finish();
      final byte[] b = s.toByteArray();

      // write chunk length
      out.writeInt(b.length - 4);
      out.write(b);

      // write checksum
      out.writeInt(crc(b));
    }

    private void writeFooter() throws IOException {
      // write IEND chunk
      out.writeInt(0);
      out.writeBytes("IEND");
      out.writeInt(crc("IEND".getBytes()));

      // update frame count
      out.seek(numFramesPointer);
      out.writeInt(numFrames);
      out.skipBytes(4);
      final byte[] b = new byte[12];
      b[0] = 'a';
      b[1] = 'c';
      b[2] = 'T';
      b[3] = 'L';
      DataTools.unpackBytes(numFrames, b, 4, 4, false);
      DataTools.unpackBytes(metadata.getActl() == null ? 0 : metadata.getActl().getNumPlays(), b, 8, 4, false);
      out.writeInt(crc(b));
    }
  }

  /**
   * This class can be used for translating Metadata in the Core SCIFIO format
   * to Metadata for writing Animated Portable Network Graphics (APNG)
   * files.
   * 
   * Note that Metadata translated from Core is only write-safe.
   * 
   * If trying to read, there should already exist an originally-parsed APNG
   * Metadata object which can be used.
   * 
   * Note also that any APNG image written must be reparsed, as the Metadata used
   * to write it can not be guaranteed valid.
   *
   */
  @SCIFIOTranslator(metaIn = DefaultDatasetMetadata.class, metaOut = Metadata.class)
  public static class CoreAPNGTranslator
    extends AbstractTranslator<DefaultDatasetMetadata, Metadata> {
  
    // -- Constructors --
    
    public CoreAPNGTranslator() {
      this(null);
    }
    
    public CoreAPNGTranslator(SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Translator API Methods -- 
    
    @Override
    public void translate(final DefaultDatasetMetadata source, final Metadata dest) {
      super.translate(source, dest);
  
      final APNGIHDRChunk ihdr =
        dest.getIhdr() == null ? new APNGIHDRChunk() : dest.getIhdr();
      final APNGPLTEChunk plte =
        dest.getPlte() == null ? new APNGPLTEChunk() : dest.getPlte();
      final APNGacTLChunk actl =
        dest.getActl() == null ? new APNGacTLChunk() : dest.getActl();
      final List<APNGfcTLChunk> fctl = new ArrayList<APNGfcTLChunk>();
  
      dest.setIhdr(ihdr);
      dest.setPlte(plte);
      dest.setActl(actl);
      dest.setFctl(fctl);
  
      ihdr.setWidth(source.getAxisLength(0, Axes.X));
      ihdr.setHeight(source.getAxisLength(0, Axes.Y));
      ihdr.setBitDepth((byte) source.getBitsPerPixel(0));
      ihdr.setFilterMethod((byte) 0);
      ihdr.setCompressionMethod((byte) 0);
      ihdr.setInterlaceMethod((byte) 0);
  
      final int sizec = source.getAxisLength(0, Axes.CHANNEL);
      final boolean rgb = source.isRGB(0);
      final boolean indexed = source.isIndexed(0);
  
      if (indexed) {
        ihdr.setColourType((byte) 0x2);
        
        /*
         * NB: not necessary to preserve ColorTable when translating. If
         * an image has a color table it will be parsed and included in
         * whatever plane is returned by an openPlane call. So it doesn't
         * also need to be preserved in the Metadata.
        byte[][] lut = null;
        try {
          lut = source.get8BitLookupTable(0);
          plte.setRed(lut[0]);
          plte.setGreen(lut[1]);
          plte.setBlue(lut[2]);
        }
        catch (final FormatException e) {
          LOGGER.error("Format error when finding 8bit lookup table", e);
        }
        catch (final IOException e) {
          LOGGER.error("IO error when finding 8bit lookup table", e);
        }
        */
      }
      else if (sizec == 2) {
        ihdr.setColourType((byte) 0x4);
      }
      else if (sizec == 4) {
        ihdr.setColourType((byte) 0x6);
      }
      else if (!rgb) {
        ihdr.setColourType((byte) 0x0);
      }
      else {
        ihdr.setColourType((byte) 0x3);
      }
  
      actl.setNumFrames(source.getAxisLength(0, Axes.TIME));
  
      for (int i = 0; i < actl.getNumFrames(); i++) {
        final APNGfcTLChunk frame = new APNGfcTLChunk();
        frame.setHeight(ihdr.getHeight());
        frame.setWidth(ihdr.getWidth());
        frame.setxOffset(0);
        frame.setyOffset(0);
        frame.setSequenceNumber(i);
        frame.setDelayDen((short) 0);
        frame.setDelayNum((short) 0);
        frame.setBlendOp((byte) 0);
        frame.setDisposeOp((byte) 0);
        fctl.add(frame);
      }
  
      dest.setSeparateDefault(true);
    }
  }

  /**
   * File format SCIFIO Translator for Animated Portable Network Graphics
   * (APNG) images to the Core SCIFIO image type.
   *
   */
  @SCIFIOTranslator(metaIn = Metadata.class, metaOut = DefaultDatasetMetadata.class)
  public static class APNGCoreTranslator
    extends AbstractTranslator<Metadata, DefaultDatasetMetadata>
    implements CoreTranslator {
  
    // -- Constructors --
  
    public APNGCoreTranslator() {
      this(null);
    }
  
    public APNGCoreTranslator(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Translator API Methods --
  
    @Override
    public void translate(final Metadata source, final DefaultDatasetMetadata dest) {
      super.translate(source, dest);
      final DefaultImageMetadata imageMeta = new DefaultImageMetadata();
      dest.add(imageMeta);
  
      imageMeta.setInterleaved(false);
      imageMeta.setOrderCertain(true);
      imageMeta.setFalseColor(true);
  
      imageMeta.setIndexed(false);
  
      boolean indexed = false;
      boolean rgb = true;
      int sizec = 1;
  
      switch (source.getIhdr().getColourType()) {
        case 0x0:
          rgb = false;
          break;
        case 0x2:
          indexed = true;
          sizec = 3;
          break;
        case 0x3:
          break;
        case 0x4:
          rgb = false;
          sizec = 2;
          break;
        case 0x6:
          sizec = 4;
          break;
      }
  
      /*
       * TODO: destination metadata doesn't care about the LUT
      if (indexed) {
        final byte[][] lut = new byte[3][0];
  
        lut[0] = source.getPlte().getRed();
        lut[1] = source.getPlte().getGreen();
        lut[2] = source.getPlte().getBlue();
  
        imageMeta.setLut(lut);
      }
      */
      
      final APNGacTLChunk actl = source.getActl();
      final int planeCount = actl == null ? 1 : actl.getNumFrames();
  
      imageMeta.setAxisTypes(new AxisType[] {
          Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME, Axes.Z});
      imageMeta.setAxisLengths(new int[] {
          source.getIhdr().getWidth(), source.getIhdr().getHeight(), sizec,
          planeCount, 1});
  
      final int bpp = source.getIhdr().getBitDepth();
  
      imageMeta.setBitsPerPixel(bpp);
      try {
        imageMeta.setPixelType(FormatTools.pixelTypeFromBytes(
          bpp / 8, false, false));
      }
      catch (final FormatException e) {
        LOGGER.error("Failed to find pixel type from bytes: " + (bpp/8), e);
      }
      imageMeta.setRGB(rgb);
      imageMeta.setIndexed(indexed);
      imageMeta.setPlaneCount(planeCount);
      imageMeta.setLittleEndian(false);
  
      // Some anciliary chunks may not have been parsed
      imageMeta.setMetadataComplete(false);
  
      imageMeta.setThumbnail(false);
      //coreMeta.setThumbSizeX(source.thumbSizeX);
      //coreMeta.setThumbSizeY(source.thumbSizeY);
  
      //coreMeta.setcLengths(source.cLengths);
      //coreMeta.setcTypes(source.cTypes);
  
      //TODO could generate this via fields?
      //coreMeta.setImageMetadata(source.imageMetadata);
    }
  }

  /**
   * A parent class for all APNG Chunk classes.
   * 
   * Provides a length and offset (in the overall file stream)
   * field.
   * 
   * Each chunk should instantiate and define its own CHUNK_SIGNATURE.
   *
   */
  public static class APNGChunk {
  
    // -- Fields --
  
    // Offset in the file data stream. Points to the start of the
    // data of the chunk, which comes after an entry for the length
    // and the chunk's signature.
    private long offset;
  
    // Length of the chunk
    private int length;
  
    // Unique chunk type signature (e.g. "IHDR")
    protected byte[] CHUNK_SIGNATURE;
  
    // -- Methods --
  
    public byte[] getCHUNK_SIGNATURE() {
      return CHUNK_SIGNATURE;
    }
  
    public int[] getFrameCoordinates() {
      return new int[0];
    }
  
    public void setOffset(final long offset) {
      this.offset = offset;
    }
  
    public long getOffset() {
      return offset;
    }
  
    public void setLength(final int length) {
      this.length = length;
    }
  
    public int getLength() {
      return length;
    }
    
    @Override
    public String toString() {
      return new FieldPrinter(this).toString();
    }
  
  }

  /**
   * Represents the IHDR chunk of the APNG image format.
   * 
   * The IHDR chunk is a critical chunk for all APNG
   * and PNG images. It contains basic information
   * about the image.
   * 
   * The IHDR is always the first chunk of a correct
   * PNG or APNG image file.
   *
   */
  public static class APNGIHDRChunk extends APNGChunk {

    // -- Constructor --

    public APNGIHDRChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x48, 0x44, 0x52};
    }

    // -- Fields --

    @Field(label = "Width")
    private int width;

    @Field(label = "height")
    private int height;

    @Field(label = "Bit depth")
    private byte bitDepth;

    @Field(label = "Colour type")
    private byte colourType;

    @Field(label = "Compression Method")
    private byte compressionMethod;

    @Field(label = "Filter method")
    private byte filterMethod;

    @Field(label = "Interlace method")
    private byte interlaceMethod;

    // -- Methods --

    public int getWidth() {
      return width;
    }

    public void setWidth(final int width) {
      this.width = width;
    }

    public int getHeight() {
      return height;
    }

    public void setHeight(final int height) {
      this.height = height;
    }

    public byte getBitDepth() {
      return bitDepth;
    }

    public void setBitDepth(final byte bitDepth) {
      this.bitDepth = bitDepth;
    }

    public byte getColourType() {
      return colourType;
    }

    public void setColourType(final byte colourType) {
      this.colourType = colourType;
    }

    public byte getCompressionMethod() {
      return compressionMethod;
    }

    public void setCompressionMethod(final byte compressionMethod) {
      this.compressionMethod = compressionMethod;
    }

    public byte getFilterMethod() {
      return filterMethod;
    }

    public void setFilterMethod(final byte filterMethod) {
      this.filterMethod = filterMethod;
    }

    public byte getInterlaceMethod() {
      return interlaceMethod;
    }

    public void setInterlaceMethod(final byte interlaceMethod) {
      this.interlaceMethod = interlaceMethod;
    }
  }

  /**
   * Represents the PLTE chunk of the APNG image format.
   * 
   * The PLTE chunk contains color palette data for the current
   * image and is only present in certain ARGB color formats.
   *
   */
  public static class APNGPLTEChunk extends APNGChunk {
  
    // -- Constructor --
  
    public APNGPLTEChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x50, 0x4C, 0x54, 0x45};
    }
  
    // -- Fields --
  
    // Red palette entries
    private byte[] red;
  
    // Green palette entries
    private byte[] green;
  
    // Blue palette entries
    private byte[] blue;
  
    // -- Methods --
  
    public byte[] getRed() {
      return red;
    }
  
    public void setRed(final byte[] red) {
      this.red = red;
    }
  
    public byte[] getGreen() {
      return green;
    }
  
    public void setGreen(final byte[] green) {
      this.green = green;
    }
  
    public byte[] getBlue() {
      return blue;
    }
  
    public void setBlue(final byte[] blue) {
      this.blue = blue;
    }
  
  }

  /**
   * Represents the fcTL chunk of the APNG image format.
   * 
   * The fcTL chunk contains metadata for a matching fdAT
   * chunk, or IDAT chunk (if the default image is also
   * the first frame of the animation).
   *
   */
  public static class APNGfcTLChunk extends APNGChunk {
  
    // -- Fields --
  
    /** Sequence number of the animation chunk, starting from 0 */
    @Field(label = "sequence_number")
    private int sequenceNumber;
  
    /** Width of the following frame */
    @Field(label = "width")
    private int width;
  
    /** Height of the following frame */
    @Field(label = "height")
    private int height;
  
    /** X position at which to render the following frame */
    @Field(label = "x_offset")
    private int xOffset;
  
    /** Y position at which to render the following frame */
    @Field(label = "y_offset")
    private int yOffset;
  
    /** Frame delay fraction numerator */
    @Field(label = "delay_num")
    private short delayNum;
  
    /** Frame delay fraction denominator */
    @Field(label = "delay_den")
    private short delayDen;
  
    /** Type of frame area disposal to be done after rendering this frame */
    @Field(label = "dispose_op")
    private byte disposeOp;
  
    /** Type of frame area rendering for this frame */
    @Field(label = "blend_op")
    private byte blendOp;
  
    private final List<APNGfdATChunk> fdatChunks;
  
    // -- Constructor --
  
    public APNGfcTLChunk() {
      fdatChunks = new ArrayList<APNGfdATChunk>();
      CHUNK_SIGNATURE = new byte[] {(byte) 0x66, 0x63, 0x54, 0x4C};
    }
  
    // -- Methods --
  
    public void addChunk(final APNGfdATChunk chunk) {
      fdatChunks.add(chunk);
    }
  
    public int getSequenceNumber() {
      return sequenceNumber;
    }
  
    public void setSequenceNumber(final int sequenceNumber) {
      this.sequenceNumber = sequenceNumber;
    }
  
    public int getWidth() {
      return width;
    }
  
    public void setWidth(final int width) {
      this.width = width;
    }
  
    public int getHeight() {
      return height;
    }
  
    public void setHeight(final int height) {
      this.height = height;
    }
  
    public int getxOffset() {
      return xOffset;
    }
  
    public void setxOffset(final int xOffset) {
      this.xOffset = xOffset;
    }
  
    public int getyOffset() {
      return yOffset;
    }
  
    public void setyOffset(final int yOffset) {
      this.yOffset = yOffset;
    }
  
    public short getDelayNum() {
      return delayNum;
    }
  
    public void setDelayNum(final short delayNum) {
      this.delayNum = delayNum;
    }
  
    public short getDelayDen() {
      return delayDen;
    }
  
    public void setDelayDen(final short delayDen) {
      this.delayDen = delayDen;
    }
  
    public byte getDisposeOp() {
      return disposeOp;
    }
  
    public void setDisposeOp(final byte disposeOp) {
      this.disposeOp = disposeOp;
    }
  
    public byte getBlendOp() {
      return blendOp;
    }
  
    public void setBlendOp(final byte blendOp) {
      this.blendOp = blendOp;
    }
  
    public List<APNGfdATChunk> getFdatChunks() {
      return fdatChunks;
    }
  
    // -- Helper Method --
    @Override
    public int[] getFrameCoordinates() {
      return new int[] {xOffset, yOffset, width, height};
    }
  }

  /**
   * Represents the IDAT chunk of the APNG image format.
   * 
   * The IDAT chunk is simply a dump of compressed image
   * data for a single plane (the default image for the file).
   *
   */
  public static class APNGIDATChunk extends APNGChunk {
  
    // -- Constructor --
  
    public APNGIDATChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x44, 0x41, 0x54};
    }
  
  }

  /**
   * Represents the acTL chunk of the APNG image format.
   * 
   * There is one acTL chunk per APNG image, and is not
   * present in PNG files.
   * 
   * The acTL chunk contains metadata describing the number
   * of frames in the image, and how many times the animation
   * sequence should be played.
   *
   */
  public static class APNGacTLChunk extends APNGChunk {
  
    // -- Constructor --
  
    public APNGacTLChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x61, 0x63, 0x54, 0x4C};
    }
  
    // -- Fields --
  
    /** Sequence number of the animation chunk, starting from 0 */
    @Field(label = "sequence_number")
    private int sequenceNumber;
  
    /** Number of frames in this APNG file */
    @Field(label = "num_frames")
    private int numFrames;
  
    /** Times to play the animation sequence */
    @Field(label = "num_plays")
    private int numPlays;
  
    // -- Methods --
  
    public int getNumFrames() {
      return numFrames;
    }
  
    public void setNumFrames(final int numFrames) {
      this.numFrames = numFrames;
    }
  
    public int getNumPlays() {
      return numPlays;
    }
  
    public void setNumPlays(final int numPlays) {
      this.numPlays = numPlays;
    }
  
    public int getSequenceNumber() {
      return sequenceNumber;
    }
  
    public void setSequenceNumber(final int sequenceNumber) {
      this.sequenceNumber = sequenceNumber;
    }
  
  }

  /**
   * Represents the fdAT chunk of the APNG image format.
   * 
   * The fdAT chunk is identical in concept to the IDAT chunk:
   * a container for compressed image data for a single frame.
   * 
   * In the case of fdAT chunks, the image is of a non-default
   * frame.
   * 
   * Each fdAT chunk is paired with an fcTL chunk.
   *
   */
  public static class APNGfdATChunk extends APNGChunk {
  
    // -- Constructor --
  
    public APNGfdATChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x66, 0x64, 0x41, 0x54};
    }
  
    // -- Fields --
  
    /** Sequence number of the animation chunk, starting from 0 */
    @Field(label = "sequence_number")
    private int sequenceNumber;
  
    // -- Methods --
  
    public int getSequenceNumber() {
      return sequenceNumber;
    }
  
    public void setSequenceNumber(final int sequenceNumber) {
      this.sequenceNumber = sequenceNumber;
    }
  }

  /**
   * This class represents the critical IEND chunk that signifies
   * the end of a PNG stream.
   * 
   * @author Mark Hiner
   *
   */
  public static class APNGIENDChunk extends APNGChunk {
    
    // -- Constructor --
    public APNGIENDChunk() {
      CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x45, 0x4E, 0x44};
    }
  }

}
