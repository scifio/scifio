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
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.codec.CompressionType;
import io.scif.codec.JPEG2000BoxType;
import io.scif.codec.JPEG2000Codec;
import io.scif.codec.JPEG2000CodecOptions;
import io.scif.codec.JPEG2000SegmentMarker;
import io.scif.common.DataTools;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * JPEG2000Reader is the file format reader for JPEG-2000 images.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/JPEG2000Reader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/JPEG2000Reader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type = JPEG2000Format.class)
public class JPEG2000Format extends AbstractFormat {

  // -- Format API methods --
  
  /*
   * @see io.scif.Format#getFormatName()
   */
  public String getFormatName() {
    return "JPEG-2000";
  }

  /*
   * @see io.scif.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[]{"jp2", "j2k", "jpf"};
  }
  
  // -- Nested Classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata implements HasColorTable {

    // -- Fields --

    private long pixelsOffset;

    private Index lastIndex = new Index();
    private byte[] lastIndexBytes;

    /** The number of JPEG 2000 resolution levels the file has. */
    private Integer resolutionLevels;

    /** The color lookup table associated with this file. */
    private int[][] lut;
    byte[][] byteLut;
    short[][] shortLut;
    
    // -- JPEG2000Metadata getters and setters --

    public long getPixelsOffset() {
      return pixelsOffset;
    }

    public void setPixelsOffset(long pixelsOffset) {
      this.pixelsOffset = pixelsOffset;
    }

    public Index getLastIndex() {
      if (lastIndex == null) lastIndex = new Index();
      return lastIndex;
    }

    public void setLastIndex(int imageIndex, int planeIndex) {
      if (lastIndex == null) lastIndex = new Index(imageIndex, planeIndex);
      else {
        lastIndex.setImageIndex(imageIndex);
        lastIndex.setPlaneIndex(imageIndex);
      }
    }

    public byte[] getLastIndexBytes() {
      return lastIndexBytes;
    }

    public void setLastIndexBytes(byte[] lastIndexBytes) {
      this.lastIndexBytes = lastIndexBytes;
    }

    public Integer getResolutionLevels() {
      return resolutionLevels;
    }

    public void setResolutionLevels(Integer resolutionLevels) {
      this.resolutionLevels = resolutionLevels;
    }

    public int[][] getLut() {
      return lut;
    }

    public void setLut(int[][] lut) {
      this.lut = lut;
    }
    
    // -- Metadata API Methods --
    
    /*
     * @see io.scif.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      ImageMetadata iMeta = get(0);
      iMeta.setAxisLength(Axes.Z, 1);
      iMeta.setAxisLength(Axes.TIME, 1);
      iMeta.setPlaneCount(1);
      iMeta.setRGB(iMeta.getAxisLength(Axes.CHANNEL) > 1);
      iMeta.setInterleaved(true);
      iMeta.setIndexed(!iMeta.isRGB() && getLut() != null);
      iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));
      

      // New core metadata now that we know how many sub-resolutions we have.
      if (getResolutionLevels() != null) {
        int imageCount = resolutionLevels + 1;
        //TODO set resolution count get(0).resolutionCount = imageCount;

        for (int i = 1; i < imageCount; i++) {
          ImageMetadata ms = new DefaultImageMetadata(iMeta);
          add(ms);
          ms.setAxisLength(Axes.X, iMeta.getAxisLength(Axes.X) / 2);
          ms.setAxisLength(Axes.Y, iMeta.getAxisLength(Axes.Y) / 2);
          ms.setThumbnail(true);
        }
      }
    }
    
    @Override
    public int getImageCount() {
      return 1;
    }
    
    @Override
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        resolutionLevels = null;
        lut = null;
        byteLut = null;
        shortLut = null;
        pixelsOffset = 0;
        lastIndex = null;
        lastIndexBytes = null;
      }
    }
    // -- HasColorTable API Methods --
    
    /*
     * @see io.scif.HasColorTable#getColorTable()
     */
    public ColorTable getColorTable(int imageIndex, int planeIndex) {
      if (lut == null) return null;
      
      if (FormatTools.getBytesPerPixel(getPixelType(0)) == 1) {
        if (byteLut == null) {
          byteLut = new byte[lut.length][lut[0].length];
          for (int i=0; i<lut.length; i++) {
            for (int j=0; j<lut[i].length; j++) {
              byteLut[i][j] = (byte) (lut[i][j] & 0xff);
            }
          }
        }
        return new ColorTable8(byteLut);
      }
      else if (FormatTools.getBytesPerPixel(getPixelType(0)) == 1) {
        if (shortLut == null) {
          shortLut = new short[lut.length][lut[0].length];
          for (int i=0; i<lut.length; i++) {
            for (int j=0; j<lut[i].length; j++) {
              shortLut[i][j] = (short) (lut[i][j] & 0xffff);
            }
          }
        }
        
        return new ColorTable16(shortLut);
      }
      
      return null;
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {
    
    // -- Constructor --
    
    public Checker() {
      suffixSufficient = false;
      suffixNecessary = false;
    }
    
    // -- Checker API methods --
    
    @Override
    public boolean isFormat(RandomAccessInputStream stream) throws IOException {
      final int blockLen = 40;
      if (!FormatTools.validStream(stream, blockLen, false)) return false;
      boolean validStart = (stream.readShort() & 0xffff) == 0xff4f;
      if (!validStart) {
        stream.skipBytes(2);
        validStart = stream.readInt() == JPEG2000BoxType.SIGNATURE.getCode();

        if (validStart) {
          stream.skipBytes(12);
          validStart = !stream.readString(4).equals("jpx ");
        }
      }
      stream.seek(stream.length() - 2);
      boolean validEnd = (stream.readShort() & 0xffff) == 0xffd9;
      return validStart && validEnd;
    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
    
    // -- Fields --

    /** Offset to first contiguous codestream. */
    private long codestreamOffset;

    /** Maximum read offset within in the stream. */
    private long maximumReadOffset;

    /** Width of the image as specified in the header. */
    private Integer headerSizeX;

    /** Height of the image as specified in the header. */
    private Integer headerSizeY;

    /** Number of channels the image has as specified in the header. */
    private Short headerSizeC;

    /** Pixel type as specified in the header. */
    private Integer headerPixelType;

    /** Width of the image as specified in the JPEG 2000 codestream. */
    private Integer codestreamSizeX;

    /** Height of the image as specified in the JPEG 2000 codestream. */
    private Integer codestreamSizeY;

    /** Number of channels the image as specified in the JPEG 2000 codestream. */
    private Short codestreamSizeC;

    /** Pixel type as specified in the JPEG 2000 codestream.. */
    private Integer codestreamPixelType;

    /** Whether or not the codestream is raw and not JP2 boxed. */
    private boolean isRawCodestream = false;

    /** List of comments stored in the file.*/
    private ArrayList<String> comments;
    
    // -- JPEG2000Parse methods --
    
    public void parse(RandomAccessInputStream stream, Metadata meta, long maximumReadOffset)
      throws IOException, FormatException
    {
      
      meta.createImageMetadata(1);
      ImageMetadata iMeta = meta.get(0);
      
      int sizeX, sizeY, sizeC, pixelType;
      
      in = stream;
      this.maximumReadOffset = maximumReadOffset;
      comments = new ArrayList<String>();
      boolean isLittleEndian = stream.isLittleEndian();
      try {
        // Parse boxes may need to change the endianness of the input stream so
        // we're going to reset it when we're done.
        parseBoxes(meta);
      }
      finally {
        in.order(isLittleEndian);
      }

      if (isRawCodestream()) {
        LOGGER.info("Codestream is raw, using codestream dimensions.");
        sizeX = getCodestreamSizeX();
        sizeY = getCodestreamSizeY();
        sizeC = getCodestreamSizeC();
        pixelType = getCodestreamPixelType();
      }
      else {
        LOGGER.info("Codestream is JP2 boxed, using header dimensions.");
        sizeX = getHeaderSizeX();
        sizeY = getHeaderSizeY();
        sizeC = getHeaderSizeC();
        pixelType = getHeaderPixelType();
      }
      iMeta.setAxisLength(Axes.X, sizeX);
      iMeta.setAxisLength(Axes.Y, sizeY);
      iMeta.setAxisLength(Axes.CHANNEL, sizeC);
      iMeta.setPixelType(pixelType);
      
      meta.setPixelsOffset(getCodestreamOffset());
      
      iMeta.setLittleEndian(false);

      ArrayList<String> comments = getComments();
      for (int i=0; i<comments.size(); i++) {
        String comment = comments.get(i);
        int equal = comment.indexOf("=");
        if (equal >= 0) {
          String key = comment.substring(0, equal);
          String value = comment.substring(equal + 1);

          addGlobalMeta(key, value);
        }
        else {
          meta.getTable().put("Comment", comment);
        }
      }
    }
  
  // -- Parser API Methods --

  /*
   * @see io.scif.AbstractParser#typedParse(io.scif.io.RandomAccessInputStream, io.scif.TypedMetadata)
   */
  @Override
  protected void typedParse(RandomAccessInputStream stream, Metadata meta)
    throws IOException, FormatException
  {
    parse(stream, meta, stream.length());
  }
    
    /** Retrieves the offset to the first contiguous codestream. */
    public long getCodestreamOffset() {
      return codestreamOffset;
    }

    /** Retrieves the list of comments stored in the file. */
    public ArrayList<String> getComments() {
      return comments;
    }

    /**
     * Parses the JPEG 2000 JP2 metadata boxes.
     * @throws IOException Thrown if there is an error reading from the file.
     */
    private void parseBoxes(Metadata meta) throws IOException {
      long originalPos = in.getFilePointer(), nextPos = 0;
      long pos = originalPos;
      LOGGER.trace("Parsing JPEG 2000 boxes at {}", pos);
      int length = 0, boxCode;
      JPEG2000BoxType boxType;

      while (pos < maximumReadOffset) {
        pos = in.getFilePointer();
        length = in.readInt();
        boxCode = in.readInt();
        boxType = JPEG2000BoxType.get(boxCode);
        if (boxType == JPEG2000BoxType.SIGNATURE_WRONG_ENDIANNESS) {
          LOGGER.trace("Swapping endianness during box parsing.");
          in.order(!in.isLittleEndian());
          length = DataTools.swap(length);
        }
        nextPos = pos + length;
        if (length >= 8) {
          length -= 8;
        }
        if (boxType == null) {
          LOGGER.warn("Unknown JPEG 2000 box 0x{} at {}",
              Integer.toHexString(boxCode), pos);
          if (pos == originalPos) {
            in.seek(originalPos);
            if (JPEG2000SegmentMarker.get(in.readUnsignedShort()) != null) {
              LOGGER.info("File is a raw codestream not a JP2.");
              isRawCodestream = true;
              in.seek(originalPos);
              parseContiguousCodestream(meta, in.length());
            }
          }
        }
        else {
          LOGGER.trace("Found JPEG 2000 '{}' box at {}", boxType.getName(), pos);
          switch (boxType) {
            case CONTIGUOUS_CODESTREAM: {
              try {
                parseContiguousCodestream(meta, length == 0 ? in.length() : length);
              }
              catch (Exception e) {
                LOGGER.warn("Could not parse contiguous codestream.", e);
              }
              break;
            }
            case HEADER: {
              in.skipBytes(4);
              String s = in.readString(4);
              if (s.equals("ihdr")) {
                headerSizeY = in.readInt();
                headerSizeX = in.readInt();
                headerSizeC = in.readShort();
                int type = in.read();
                in.skipBytes(3);
                headerPixelType = convertPixelType(type);
              }
              parseBoxes(meta);
              break;
            }
            case PALETTE:
              int nEntries = in.readShort();
              int nColumns = in.read();
              int[] bitDepths = new int[nColumns];
              for (int i=0; i<bitDepths.length; i++) {
                bitDepths[i] = in.read() & 0x7f;
                while ((bitDepths[i] % 8) != 0) {
                  bitDepths[i]++;
                }
              }
              int[][] lut = new int[nColumns][nEntries];

              for (int i=0; i<nColumns; i++) {
                for (int j=0; j<lut[i].length; j++) {
                  if (bitDepths[i] == 8) {
                    lut[i][j] = in.read();
                  }
                  else if (bitDepths[i] == 16) {
                    lut[i][j] = in.readShort();
                  }
                }
              }
              
              meta.setLut(lut);

              break;
          }
        }
        // Exit or seek to the next metadata box
        if (nextPos < 0 || nextPos >= maximumReadOffset || length == 0) {
          LOGGER.trace("Exiting box parser loop.");
          break;
        }
        LOGGER.trace("Seeking to next box at {}", nextPos);
        in.seek(nextPos);
      }
    }

    /**
     * Parses the JPEG 2000 codestream metadata.
     * @param length Total length of the codestream block.
     * @throws IOException Thrown if there is an error reading from the file.
     */
    private void parseContiguousCodestream(Metadata meta, long length) throws IOException {
      if (codestreamOffset == 0) {
        codestreamOffset = in.getFilePointer();
      }

      JPEG2000SegmentMarker segmentMarker;
      int segmentMarkerCode = 0, segmentLength = 0;
      long pos = in.getFilePointer(), nextPos = 0;
      LOGGER.trace("Parsing JPEG 2000 contiguous codestream of length {} at {}",
          length, pos);
      long maximumReadOffset = pos + length;
      boolean terminate = false;
      while (pos < maximumReadOffset && !terminate) {
        pos = in.getFilePointer();
        segmentMarkerCode = in.readUnsignedShort();
        segmentMarker = JPEG2000SegmentMarker.get(segmentMarkerCode);
        if (segmentMarker == JPEG2000SegmentMarker.SOC_WRONG_ENDIANNESS) {
          LOGGER.trace("Swapping endianness during segment marker parsing.");
          in.order(!in.isLittleEndian());
          segmentMarkerCode = JPEG2000SegmentMarker.SOC.getCode();
          segmentMarker = JPEG2000SegmentMarker.SOC;
        }
        if (segmentMarker == JPEG2000SegmentMarker.SOC
            || segmentMarker == JPEG2000SegmentMarker.SOD
            || segmentMarker == JPEG2000SegmentMarker.EPH
            || segmentMarker == JPEG2000SegmentMarker.EOC
            || (segmentMarkerCode >= JPEG2000SegmentMarker.RESERVED_DELIMITER_MARKER_MIN.getCode()
                && segmentMarkerCode <= JPEG2000SegmentMarker.RESERVED_DELIMITER_MARKER_MAX.getCode())) {
          // Delimiter marker; no segment.
          segmentLength = 0;
        }
        else {
          segmentLength = in.readUnsignedShort();
        }
        nextPos = pos + segmentLength + 2;
        if (segmentMarker == null) {
          LOGGER.warn("Unknown JPEG 2000 segment marker 0x{} at {}",
              Integer.toHexString(segmentMarkerCode), pos);
        }
        else {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format(
                "Found JPEG 2000 segment marker '%s' of length %d at %d",
                segmentMarker.getName(), segmentLength, pos));
          }
          switch (segmentMarker) {
            case SOT:
            case SOD:
            case EOC:
              terminate = true;
              break;
            case SIZ: {
              // Skipping:
              //  * Capability (uint16)
              in.skipBytes(2);
              codestreamSizeX = in.readInt();
              LOGGER.trace("Read reference grid width {} at {}", codestreamSizeX,
                  in.getFilePointer());
              codestreamSizeY = in.readInt();
              LOGGER.trace("Read reference grid height {} at {}", codestreamSizeY,
                  in.getFilePointer());
              // Skipping:
              //  * Horizontal image offset (uint32)
              //  * Vertical image offset (uint32)
              //  * Tile width (uint32)
              //  * Tile height (uint32)
              //  * Horizontal tile offset (uint32)
              //  * Vertical tile offset (uint32)
              in.skipBytes(24);
              codestreamSizeC = in.readShort();
              LOGGER.trace("Read total components {} at {}",
                  codestreamSizeC, in.getFilePointer());
              int type = in.read();
              in.skipBytes(3);
              codestreamPixelType = convertPixelType(type);
              LOGGER.trace("Read codestream pixel type {} at {}",
                  codestreamPixelType, in.getFilePointer());
              break;
            }
            case COD: {
              // Skipping:
              //  * Segment coding style (uint8)
              //  * Progression order (uint8)
              //  * Total quality layers (uint16)
              //  * Multiple component transform (uint8)
              in.skipBytes(5);
              meta.setResolutionLevels(in.readUnsignedByte());
              LOGGER.trace("Found number of resolution levels {} at {} ",
                  meta.getResolutionLevels(), in.getFilePointer());
              break;
            }
            case COM:
              in.skipBytes(2);
              String comment = in.readString(segmentLength - 4);
              comments.add(comment);
              break;
          }
        }
        // Exit or seek to the next metadata box
        if (nextPos < 0 || nextPos >= maximumReadOffset || terminate) {
          LOGGER.trace("Exiting segment marker parse loop.");
          break;
        }
        LOGGER.trace("Seeking to next segment marker at {}", nextPos);
        in.seek(nextPos);
      }
    }

    /**
     * Whether or not the codestream is raw and not JP2 boxed.
     * @return <code>true</code> if the codestream is raw and <code>false</code>
     * otherwise.
     */
    public boolean isRawCodestream() {
      return isRawCodestream;
    }

    /**
     * Returns the width of the image as specified in the header.
     * @return See above.
     */
    public Integer getHeaderSizeX() {
      return headerSizeX;
    }

    /**
     * Returns the height of the image as specified in the header.
     * @return See above.
     */
    public Integer getHeaderSizeY() {
      return headerSizeY;
    }

    /**
     * Returns the number of channels the image has as specified in the header.
     * @return See above.
     */
    public Short getHeaderSizeC() {
      return headerSizeC;
    }

    /**
     * Returns the pixel type as specified in the header.
     * @return See above.
     */
    public Integer getHeaderPixelType() {
      return headerPixelType;
    }

    /**
     * Returns the width of the image as specified in the header.
     * @return See above.
     */
    public Integer getCodestreamSizeX() {
      return codestreamSizeX;
    }

    /**
     * Returns the height of the image as specified in the header.
     * @return See above.
     */
    public Integer getCodestreamSizeY() {
      return codestreamSizeY;
    }

    /**
     * Returns the number of channels the image has as specified in the header.
     * @return See above.
     */
    public Short getCodestreamSizeC() {
      return codestreamSizeC;
    }

    /**
     * Returns the pixel type as specified in the header.
     * @return See above.
     */
    public Integer getCodestreamPixelType() {
      return codestreamPixelType;
    }

    private int convertPixelType(int type) {
      int bits = (type & 0x7f) + 1;
      boolean isSigned = ((type & 0x80) >> 7) == 1;

      if (bits <= 8) {
        return isSigned ? FormatTools.INT8 : FormatTools.UINT8;
      }
      else if (bits <= 16) {
        return isSigned ? FormatTools.INT16 : FormatTools.UINT16;
      }
      else if (bits <= 32) {
        return isSigned ? FormatTools.INT32 : FormatTools.UINT32;
      }
      return FormatTools.UINT8;
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
     * @see io.scif.TypedReader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h) throws FormatException,
      IOException
    {
      byte[] buf = plane.getBytes();
      Metadata meta = getMetadata();
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);

      if (meta.getLastIndex().getImageIndex() == imageIndex && 
          meta.getLastIndex().getPlaneIndex() == planeIndex && meta.getLastIndexBytes() != null) {
        RandomAccessInputStream s = new RandomAccessInputStream(getContext(), meta.getLastIndexBytes());
        readPlane(s, imageIndex, x, y, w, h, plane);
        s.close();
        return plane;
      }

      JPEG2000CodecOptions options = JPEG2000CodecOptions.getDefaultOptions();
      options.interleaved = meta.isInterleaved(imageIndex);
      options.littleEndian = meta.isLittleEndian(imageIndex);
      if (meta.getResolutionLevels() != null) {
        options.resolution = Math.abs(imageIndex - meta.getResolutionLevels());
      }
      else if (meta.getAll().size() > 1) {
        options.resolution = imageIndex;
      }

      getStream().seek(meta.getPixelsOffset());
      JPEG2000Codec codec = new JPEG2000Codec();
      codec.setContext(getContext());
      byte[] lastIndexPlane = codec.decompress(getStream(), options);
      meta.setLastIndexBytes(lastIndexPlane);
      RandomAccessInputStream s = new RandomAccessInputStream(getContext(), lastIndexPlane);
      readPlane(s, imageIndex, x, y, w, h, plane);
      s.close();
      meta.setLastIndex(imageIndex, planeIndex);
      return plane;
    }
    
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Writer extends AbstractWriter<Metadata> {

    // -- Constructor --
    
    public Writer() {
      compressionTypes = new String[] {CompressionType.J2K_LOSSY.getCompression(), 
          CompressionType.J2K.getCompression()};
      //The default codec options
      options = JPEG2000CodecOptions.getDefaultOptions();
    }
    
    // -- Writer API Methods --
    
    /*
     * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
     */
    public void savePlane(int imageIndex, int planeIndex, Plane plane, int x,
      int y, int w, int h) throws FormatException, IOException
    {
      byte[] buf = plane.getBytes();
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);
      
      /*
      if (!isFullPlane(x, y, w, h)) {
        throw new FormatException(
          "JPEG2000Writer does not yet support saving image tiles.");
      }
      */
      //MetadataRetrieve retrieve = getMetadataRetrieve();
      //int width = retrieve.getPixelsSizeX(series).getValue().intValue();
      //int height = retrieve.getPixelsSizeY(series).getValue().intValue();
     
      out.write(compressBuffer(imageIndex, planeIndex, buf, x, y, w, h));
    }

    /**
     * Compresses the buffer.
     * 
     * @param no the image index within the current file, starting from 0.
     * @param buf the byte array that represents the image tile.
     * @param x the X coordinate of the upper-left corner of the image tile.
     * @param y the Y coordinate of the upper-left corner of the image tile.
     * @param w the width (in pixels) of the image tile.
     * @param h the height (in pixels) of the image tile.
     * @throws FormatException if one of the parameters is invalid.
     * @throws IOException if there was a problem writing to the file.
     */
    public byte[] compressBuffer(int imageIndex, int planeIndex, byte[] buf, int x, int y, int w, int h)
      throws FormatException, IOException
    {
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);
      boolean littleEndian = getMetadata().isLittleEndian(imageIndex);
      int bytesPerPixel = getMetadata().getBitsPerPixel(imageIndex) / 8;
      int nChannels = getMetadata().getRGBChannelCount(imageIndex);

      //To be on the save-side
      if (options == null) options = JPEG2000CodecOptions.getDefaultOptions();
      options = new JPEG2000CodecOptions(options);
      options.width = w;
      options.height = h;
      options.channels = nChannels;
      options.bitsPerSample = bytesPerPixel * 8;
      options.littleEndian = littleEndian;
      options.interleaved = interleaved;
      options.lossless = compression == null || 
      compression.equals(CompressionType.J2K.getCompression());
      options.colorModel = getColorModel();
      
      JPEG2000Codec codec = new JPEG2000Codec();
      codec.setContext(getContext());
      return codec.compress(buf, options);
    }
      
    /**
     * Overridden to indicate that stacks are not supported. 
     * @see loci.formats.IFormatWriter#canDoStacks() 
     */
    public boolean canDoStacks() { return false; }

    /**
     * Overridden to return the formats supported by the writer.
     * @see loci.formats.IFormatWriter#getPixelTypes(String) 
     */
    public int[] getPixelTypes(String codec) {
      return new int[] {FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
        FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32};
    }
  }
  
  // -- Helper class --
  
  public static class Index {
    private int imageIndex;
    private int planeIndex;
    
    public Index() {
      this(-1, -1);
    }
    
    public Index(int image, int plane) {
      imageIndex = image;
      planeIndex = plane;
    }
    
    public void setImageIndex(int image) {
      imageIndex = image;
    }
    
    public void setPlaneIndex(int plane) {
      planeIndex = plane;
    }
    
    public int getImageIndex() {
      return imageIndex;
    }
    
    public int getPlaneIndex() {
      return planeIndex;
    }
  }
}
