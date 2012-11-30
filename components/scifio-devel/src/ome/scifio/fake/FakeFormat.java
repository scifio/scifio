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
package ome.scifio.fake;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.AbstractTranslator;
import ome.scifio.AbstractWriter;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.DefaultDatasetMetadata;
import ome.scifio.DefaultImageMetadata;
import ome.scifio.DatasetMetadata;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.discovery.DiscoverableHandle;
import ome.scifio.discovery.SCIFIOFormat;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.io.StreamHandle;
import ome.scifio.util.FormatTools;

import org.slf4j.Logger;

/**
 * FakeReader is the file format reader for faking input data.
 * It is mainly useful for testing.
 * <p>Examples:<ul>
 *  <li>showinf 'multi-series&amp;series=11&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake' -series 9</li>
 *  <li>showinf '8bit-signed&amp;pixelType=int8&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '8bit-unsigned&amp;pixelType=uint8&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '16bit-signed&amp;pixelType=int16&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '16bit-unsigned&amp;pixelType=uint16&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '32bit-signed&amp;pixelType=int32&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '32bit-unsigned&amp;pixelType=uint32&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '32bit-floating&amp;pixelType=float&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 *  <li>showinf '64bit-floating&amp;pixelType=double&amp;sizeZ=3&amp;sizeC=5&amp;sizeT=7&amp;sizeY=50.fake'</li>
 * </ul></p>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/FakeReader.java">Trac</a>
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/FakeReader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@SCIFIOFormat
public class FakeFormat
extends
AbstractFormat<FakeFormat.Metadata, FakeFormat.Checker,
               FakeFormat.Parser, FakeFormat.Reader,
               FakeFormat.Writer> {
  
  // -- Constants --
  
  public static final int BOX_SIZE = 10;
  public static final int DEFAULT_SIZE_X = 512;
  public static final int DEFAULT_SIZE_Y = 512;
  public static final int DEFAULT_SIZE_Z = 1;
  public static final int DEFAULT_SIZE_C = 1;
  public static final int DEFAULT_SIZE_T = 1;
  public static final int DEFAULT_THUMB_SIZE_X = 0;
  public static final int DEFAULT_THUMB_SIZE_Y = 0;
  public static final String DEFAULT_PIXEL_TYPE = FormatTools.getPixelTypeString(FormatTools.UINT8);
  public static final int DEFAULT_RGB_CHANNEL_COUNT = 1;
  public static final int DEFAULT_LUT_LENGTH = 3;
  public static final int DEFAULT_SCALE_FACTOR = 1;
  public static final String DEFAULT_DIMENSION_ORDER = "XYZCT";
  
  private static final long SEED = 0xcafebabe;
  
  public static final Logger LOGGER = null;
  
  // -- Allowed keys --
  
  private static final String SIZE_X = "sizeX";
  private static final String SIZE_Y = "sizeY";
  private static final String SIZE_Z = "sizeZ";
  private static final String SIZE_C = "sizeC";
  private static final String SIZE_T = "sizeT";
  private static final String THUMB_X = "thumbSizeX";
  private static final String THUMB_Y = "thumbSizeY";
  private static final String PIXEL_TYPE = "pixelType";
  private static final String BITS_PER_PIXEL = "bitsPerPixel";
  private static final String DIM_ORDER = "dimOrder";
  private static final String INDEXED = "indexed";
  private static final String FALSE_COLOR = "falseColor";
  private static final String LITTLE_ENDIAN = "little";
  private static final String INTERLEAVED = "interleaved";
  private static final String META_COMPLETE = "metadataComplete";
  private static final String THUMBNAIL = "thumbnail";
  private static final String ORDER_CERTAIN = "orderCertain";
  private static final String LUT_LENGTH = "lutLength";
  private static final String SCALE_FACTOR = "scaleFactor";  
  private static final String SERIES = "series";
  private static final String RGB = "rgb";
  private static final String NAME = "name";
  private static final String TOKEN_SEPARATOR = "&";

  // -- Constructor --
  
  public FakeFormat() throws FormatException {
    this(null);
  }
  
  public FakeFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, "Simulated data", "fake", Metadata.class, Checker.class, Parser.class, Reader.class, Writer.class);
  }

  /**
   * Metadata class for Fake format. Actually holds no information
   * about the "image" as everything is stored in the attached
   * RandomAccessInputStream.
   * </br></br>
   * Fake specification should be accessed by {@link Metadata#getSource()}
   */
  public static class Metadata extends AbstractMetadata {
    
    // -- Fields --
    
    private ColorTable[] lut;
    
    private int[][] valueToIndex;
    
    // -- Constructor --
    
    public Metadata() {
      this(null);
    }
    
    public Metadata(SCIFIO ctx) {
      super(ctx);
    }
    
    // -- FakeFormat.Metadata methods --

    public ColorTable[] getLut() {
      return lut;
    }

    public void setLut(ColorTable[] lut) {
      this.lut = lut;
    }

    public int[][] getValueToIndex() {
      return valueToIndex;
    }

    public void setValueToIndex(int[][] valueToIndex) {
      this.valueToIndex = valueToIndex;
    }
  }
  
  /**
   * Checker for Fake file format. Supported extension is ".fake"
   *
   */
  public static class Checker extends AbstractChecker<Metadata> {

    // -- Constructor --
    
    public Checker() {
      this(null);
    }
    
    public Checker(SCIFIO ctx) {
      super(ctx);
    }
  }
  
  /**
   * Parser for Fake file format. The file suffix is sufficient for
   * detection - as the name is the only aspect of a Fake file
   * that is guaranteed to exist.
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
    
    // -- Constructor -
    
    public Parser() {
      this(null);
    }
    
    public Parser(final SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Parser API Methods --
    
    /* @See Parser#Parse(RandomAccessInputStream, M) */
    @Override
    public Metadata parse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException {
      meta = super.parse(stream, meta);
      
      int sizeX = DEFAULT_SIZE_X;
      int sizeY = DEFAULT_SIZE_Y;
      int sizeZ = DEFAULT_SIZE_Z;
      int sizeC = DEFAULT_SIZE_C;
      int sizeT = DEFAULT_SIZE_T;
      int thumbSizeX = DEFAULT_THUMB_SIZE_X;
      int thumbSizeY = DEFAULT_THUMB_SIZE_Y;
      int rgb = DEFAULT_RGB_CHANNEL_COUNT;
      boolean indexed = false;
      boolean falseColor = false;
      int pixelType = FormatTools.pixelTypeFromString(DEFAULT_PIXEL_TYPE);
      
      int imageCount = 1;
      int lutLength = DEFAULT_LUT_LENGTH;
      
      HashMap<String, String> fakeMap = FakeUtils.extractFakeInfo(stream.getFileName());
      
      sizeX = FakeUtils.getIntValue(fakeMap.get(SIZE_X), sizeX);
      sizeY = FakeUtils.getIntValue(fakeMap.get(SIZE_Y), sizeY);
      sizeZ = FakeUtils.getIntValue(fakeMap.get(SIZE_Z), sizeZ);
      sizeC = FakeUtils.getIntValue(fakeMap.get(SIZE_C), sizeC);
      sizeT = FakeUtils.getIntValue(fakeMap.get(SIZE_T), sizeT);

      thumbSizeX = FakeUtils.getIntValue(fakeMap.get(THUMB_X), thumbSizeX);
      thumbSizeY = FakeUtils.getIntValue(fakeMap.get(THUMB_Y), thumbSizeY);
      rgb = FakeUtils.getIntValue(fakeMap.get(RGB), rgb);
      indexed = FakeUtils.getBoolValue(fakeMap.get(INDEXED), indexed);
      falseColor = FakeUtils.getBoolValue(fakeMap.get(FALSE_COLOR), falseColor);
      String mappedPType = fakeMap.get(PIXEL_TYPE);
      pixelType = FormatTools.pixelTypeFromString(mappedPType == null ? DEFAULT_PIXEL_TYPE : mappedPType);

      imageCount = FakeUtils.getIntValue(fakeMap.get(SERIES), imageCount);
      lutLength = FakeUtils.getIntValue(fakeMap.get(LUT_LENGTH), lutLength);
      
      // Sanity checking
      if (sizeX < 1) throw new FormatException("Invalid sizeX: " + sizeX);
      if (sizeY < 1) throw new FormatException("Invalid sizeY: " + sizeY);
      if (sizeZ < 1) throw new FormatException("Invalid sizeZ: " + sizeZ);
      if (sizeC < 1) throw new FormatException("Invalid sizeC: " + sizeC);
      if (sizeT < 1) throw new FormatException("Invalid sizeT: " + sizeT);
      if (thumbSizeX < 0) {
        throw new FormatException("Invalid thumbSizeX: " + thumbSizeX);
      }
      if (thumbSizeY < 0) {
        throw new FormatException("Invalid thumbSizeY: " + thumbSizeY);
      }
      if (rgb < 1 || rgb > sizeC || sizeC % rgb != 0) {
        throw new FormatException("Invalid sizeC/rgb combination: " +
          sizeC + "/" + rgb);
      }
      if (falseColor && !indexed) {
        throw new FormatException("False color images must be indexed");
      }
      if (imageCount < 1) {
        throw new FormatException("Invalid seriesCount: " + imageCount);
      }
      if (lutLength < 1) {
        throw new FormatException("Invalid lutLength: " + lutLength);
      }
      
      
      // for indexed color images, create lookup tables
      if (indexed) {
        int[][] indexToValue = null;
        int[][] valueToIndex = null;
        ColorTable[] luts = null;
        
        if (pixelType == FormatTools.UINT8) {
          // create 8-bit LUTs
          final int num = 256;
          indexToValue = new int[sizeC][num];
          valueToIndex = new int[sizeC][num];
          FakeUtils.createIndexValueMap(indexToValue);
          luts = new ColorTable8[sizeC];
          // linear ramp
          for (int c=0; c<sizeC; c++) {
            byte[][] lutBytes = new byte[lutLength][num];
            for (int i=0; i<lutLength; i++) {
              for (int index=0; index<num; index++) {
                lutBytes[i][index] = (byte) indexToValue[c][index];
              }
            }
            luts[c] = new ColorTable8(lutBytes);
          }
        }
        else if (pixelType == FormatTools.UINT16) {
          // create 16-bit LUTs
          final int num = 65536;
          indexToValue = new int[sizeC][num];
          valueToIndex = new int[sizeC][num];
          FakeUtils.createIndexValueMap(indexToValue);
          luts = new ColorTable16[sizeC];
          // linear ramp
          for (int c=0; c<sizeC; c++) {
            short[][] lutShorts = new short[lutLength][num];
            for (int i=0; i<lutLength; i++) {
              for (int index=0; index<num; index++) {
                lutShorts[i][index] = (short) indexToValue[c][index];
              }
            }
            luts[c] = new ColorTable16(lutShorts);
          }
        }
        
        meta.setLut(luts);
        
        if(valueToIndex != null) {
          FakeUtils.createInverseIndexMap(indexToValue, valueToIndex);
          meta.setValueToIndex(valueToIndex);
        }
        // NB: Other pixel types will have null LUTs.
      }
      

      
      
      return meta;
    }
  }
  
  /**
   * Reader for the Fake file format. Pixel values are simulated
   * based on the specified dimensions and qualities of the "image."
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {
    
    // -- Fields --
    
    /** Channel of last opened image plane. */
    private int ac = 0;
    
    // -- Constructor --
    
    public Reader() {
      this(null);
    }
    
    public Reader(final SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Reader API methods --

    public ByteArrayPlane openPlane(int imageIndex, int planeIndex, 
        ByteArrayPlane plane, int x, int y, int w, int h) 
        throws FormatException, IOException {
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex,
          plane.getData().length, x, y, w, h);

      DatasetMetadata<?> dMeta = getDatasetMetadata();
      plane.setImageMetadata(dMeta.get(imageIndex));
      
      final int pixelType = dMeta.getPixelType(imageIndex);
      final int bpp = FormatTools.getBytesPerPixel(pixelType);
      final boolean signed = FormatTools.isSigned(pixelType);
      final boolean floating = FormatTools.isFloatingPoint(pixelType);
      final int rgb = dMeta.getRGBChannelCount(imageIndex);
      final boolean indexed = dMeta.isIndexed(imageIndex);
      final boolean little = dMeta.isLittleEndian(imageIndex);
      final boolean interleaved = dMeta.isInterleaved(imageIndex);
      final int scaleFactor = ((Double)dMeta.getMetadataValue(SCALE_FACTOR)).intValue();
      final ColorTable[] lut = getMetadata().getLut();
      final int[][] valueToIndex = getMetadata().getValueToIndex();
      
      final int[] zct = FormatTools.getZCTCoords(this, imageIndex, planeIndex);
      final int zIndex = zct[0], cIndex = zct[1], tIndex = zct[2];
      ac = cIndex;

      // integer types start gradient at the smallest value
      long min = signed ? (long) -Math.pow(2, 8 * bpp - 1) : 0;
      if (floating) min = 0; // floating point types always start at 0

      for (int cOffset=0; cOffset<rgb; cOffset++) {
        int channel = rgb * cIndex + cOffset;
        for (int row=0; row<h; row++) {
          int yy = y + row;
          for (int col=0; col<w; col++) {
            int xx = x + col;
            long pixel = min + xx;

            // encode various information into the image plane
            boolean specialPixel = false;
            if (yy < BOX_SIZE) {
              int grid = xx / BOX_SIZE;
              specialPixel = true;
              switch (grid) {
                case 0:
                  pixel = imageIndex;
                  break;
                case 1:
                  pixel = planeIndex;
                  break;
                case 2:
                  pixel = zIndex;
                  break;
                case 3:
                  pixel = channel;
                  break;
                case 4:
                  pixel = tIndex;
                  break;
                default:
                  // just a normal pixel in the gradient
                  specialPixel = false;
              }
            }

            // if indexed color with non-null LUT, convert value to index
            if (indexed && lut != null) {
              int modValue = lut[ac].getLength();
              plane.setColorTable(lut[ac]);
              
              if (valueToIndex != null) pixel = valueToIndex[ac][(int) (pixel % modValue)];
            }

            // scale pixel value by the scale factor
            // if floating point, convert value to raw IEEE floating point bits
            switch (pixelType) {
              case FormatTools.FLOAT:
                float floatPixel;
                if (specialPixel) floatPixel = pixel;
                else floatPixel = (float) (scaleFactor * pixel);
                pixel = Float.floatToIntBits(floatPixel);
                break;
              case FormatTools.DOUBLE:
                double doublePixel;
                if (specialPixel) doublePixel = pixel;
                else doublePixel = scaleFactor * pixel;
                pixel = Double.doubleToLongBits(doublePixel);
                break;
              default:
                if (!specialPixel) pixel = (long) (scaleFactor * pixel);
            }

            // unpack pixel into byte buffer
            int index;
            if (interleaved) index = w * rgb * row + rgb * col + cOffset; // CXY
            else index = h * w * cOffset + w * row + col; // XYC
            index *= bpp;
            DataTools.unpackBytes(pixel, plane.getData(), index, bpp, little);
          }
        }
      }

      return plane;
    }
  }
  
  /**
   * Writer for the Fake file format.
   *
   */
  public static class Writer extends AbstractWriter<Metadata> {

    // -- Constructor --
    
    public Writer() {
      this(null);
    }
    
    public Writer(final SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Writer API Methods --

    public void saveBytes(int imageIndex, int planeIndex, byte[] buf, int x,
        int y, int w, int h) throws FormatException, IOException {
      // TODO Auto-generated method stub
      
    }

    public void setMetadata(Metadata meta) {
      // TODO Auto-generated method stub
      
    }
  }
  
  /**
   * Translator from Fake metadata to {@link DatasetMetadata}.
   */
  @SCIFIOTranslator(metaIn = Metadata.class, metaOut = DefaultDatasetMetadata.class)
  public static class FakeCoreTranslator 
  extends AbstractTranslator<Metadata, DefaultDatasetMetadata> {
    
    // -- Constants --


    // -- Fields --
    
    // -- Constructor --
    
    public FakeCoreTranslator() {
      this(null);
    }
    
    public FakeCoreTranslator(SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Translator API Methods --
    
    @Override
    public void translate(final Metadata source, final DefaultDatasetMetadata destination) {
      super.translate(source, destination);
      
      int sizeX = DEFAULT_SIZE_X;
      int sizeY = DEFAULT_SIZE_Y;
      int sizeZ = DEFAULT_SIZE_Z;
      int sizeC = DEFAULT_SIZE_C;
      int sizeT = DEFAULT_SIZE_T;
      int rgb = DEFAULT_RGB_CHANNEL_COUNT;
      int thumbSizeX = DEFAULT_THUMB_SIZE_X;
      int thumbSizeY = DEFAULT_THUMB_SIZE_Y;
      int pixelType = FormatTools.pixelTypeFromString(DEFAULT_PIXEL_TYPE);
      int bitsPerPixel = 0; // default
      String dimOrder = DEFAULT_DIMENSION_ORDER;
      boolean orderCertain = true;
      boolean little = true;
      boolean interleaved = false;
      boolean indexed = false;
      boolean falseColor = false;
      boolean metadataComplete = true;
      boolean thumbnail = false;
      double scaleFactor = DEFAULT_SCALE_FACTOR;

      int lutLength = DEFAULT_LUT_LENGTH;
      
      HashMap<String, String> fakeMap = FakeUtils.extractFakeInfo(source.getSource().getFileName());
      
      sizeX = FakeUtils.getIntValue(fakeMap.get(SIZE_X), sizeX);
      sizeY = FakeUtils.getIntValue(fakeMap.get(SIZE_Y), sizeY);
      sizeZ = FakeUtils.getIntValue(fakeMap.get(SIZE_Z), sizeZ);
      sizeC = FakeUtils.getIntValue(fakeMap.get(SIZE_C), sizeC);
      sizeT = FakeUtils.getIntValue(fakeMap.get(SIZE_T), sizeT);
      
      rgb = FakeUtils.getIntValue(fakeMap.get(RGB), rgb);

      thumbSizeX = FakeUtils.getIntValue(fakeMap.get(THUMB_X), thumbSizeX);
      thumbSizeY = FakeUtils.getIntValue(fakeMap.get(THUMB_Y), thumbSizeY);
      String mappedPType = fakeMap.get(PIXEL_TYPE);
      pixelType = FormatTools.pixelTypeFromString(mappedPType == null ? DEFAULT_PIXEL_TYPE : mappedPType);
      bitsPerPixel = FormatTools.getBitsPerPixel(pixelType);
      bitsPerPixel = FakeUtils.getIntValue(fakeMap.get(BITS_PER_PIXEL), bitsPerPixel);
      dimOrder = fakeMap.get(DIM_ORDER) == null ? dimOrder : fakeMap.get(DIM_ORDER).toUpperCase();

      indexed = FakeUtils.getBoolValue(fakeMap.get(INDEXED), indexed);
      falseColor = FakeUtils.getBoolValue(fakeMap.get(FALSE_COLOR), falseColor);
      little = FakeUtils.getBoolValue(fakeMap.get(LITTLE_ENDIAN), little);
      interleaved = FakeUtils.getBoolValue(fakeMap.get(INTERLEAVED), interleaved);
      metadataComplete = FakeUtils.getBoolValue(fakeMap.get(META_COMPLETE), metadataComplete);
      thumbnail = FakeUtils.getBoolValue(fakeMap.get(THUMBNAIL), thumbnail);
      orderCertain = FakeUtils.getBoolValue(fakeMap.get(ORDER_CERTAIN), orderCertain);

      lutLength = FakeUtils.getIntValue(fakeMap.get(LUT_LENGTH), lutLength);
      scaleFactor = FakeUtils.getDoubleValue(fakeMap.get(SCALE_FACTOR), scaleFactor);
      
      AxisType[] axes = FormatTools.findDimensionList(dimOrder);
      int[] axisLengths = new int[axes.length];
      
      for(int i = 0; i < axes.length; i++) {
        AxisType t = axes[i];
        if(t.equals(Axes.X))
          axisLengths[i] = sizeX;
        else if(t.equals(Axes.Y))
          axisLengths[i] = sizeY;
        else if(t.equals(Axes.Z))
          axisLengths[i] = sizeZ;
        else if(t.equals(Axes.CHANNEL))
          axisLengths[i] = sizeC;
        else if(t.equals(Axes.TIME))
          axisLengths[i] = sizeT;
        else
          axisLengths[i] = -1; // Unknown axis
      }

      destination.putDatasetMeta(SCALE_FACTOR, scaleFactor);
      destination.putDatasetMeta(LUT_LENGTH, lutLength);
      
      int numImages = 1;
      numImages =  FakeUtils.getIntValue(fakeMap.get(SERIES), numImages);
      
      int effSizeC = sizeC / rgb;
      
      for(int i = 0; i < numImages; i++) {
        DefaultImageMetadata imageMeta = new DefaultImageMetadata();

        imageMeta.setAxisTypes(axes);
        imageMeta.setAxisLengths(axisLengths);
        imageMeta.setPixelType(pixelType);
        imageMeta.setPlaneCount(sizeZ * sizeT);
        imageMeta.setThumbSizeX(thumbSizeX);
        imageMeta.setThumbSizeY(thumbSizeY);
        imageMeta.setIndexed(indexed);
        imageMeta.setFalseColor(falseColor);
        imageMeta.setRGB(rgb > 1);
        imageMeta.setLittleEndian(little);
        imageMeta.setInterleaved(interleaved);
        imageMeta.setMetadataComplete(metadataComplete);
        imageMeta.setThumbnail(thumbnail);
        imageMeta.setOrderCertain(orderCertain);
        imageMeta.setBitsPerPixel(bitsPerPixel);
        imageMeta.setPlaneCount(sizeZ * effSizeC * sizeT);
        
        destination.add(imageMeta);
      }
    }
    
  }
  
  /**
   * Translator from {@link DatasetMetadata} to Fake Metadata.
   */
  @SCIFIOTranslator(metaIn = DefaultDatasetMetadata.class, metaOut = Metadata.class)
  public static class CoreFakeTranslator 
  extends AbstractTranslator<DefaultDatasetMetadata, Metadata> {

    // -- Constructor --
    
    public CoreFakeTranslator() {
      this(null);
    }
    
    public CoreFakeTranslator(SCIFIO ctx) {
      super(ctx);
    }
    
    // -- Translator API Methods --
    
    @Override
    public void translate(final DefaultDatasetMetadata source, final Metadata destination) {
      super.translate(source, destination);
      
      String fakeId = NAME + "=" + source.getSource().getFileName();
      
      fakeId = FakeUtils.appendToken(fakeId, SIZE_X, Integer.toString(source.getAxisLength(0, Axes.X)));
      fakeId = FakeUtils.appendToken(fakeId, SIZE_Y, Integer.toString(source.getAxisLength(0, Axes.Y)));
      fakeId = FakeUtils.appendToken(fakeId, SIZE_Z, Integer.toString(source.getAxisLength(0, Axes.Z)));
      fakeId = FakeUtils.appendToken(fakeId, SIZE_C, Integer.toString(source.getAxisLength(0, Axes.CHANNEL)));
      fakeId = FakeUtils.appendToken(fakeId, SIZE_T, Integer.toString(source.getAxisLength(0, Axes.TIME)));

      fakeId = FakeUtils.appendToken(fakeId, THUMB_X, Integer.toString(source.getThumbSizeX(0)));
      fakeId = FakeUtils.appendToken(fakeId, THUMB_Y, Integer.toString(source.getThumbSizeY(0)));

      fakeId = FakeUtils.appendToken(fakeId, PIXEL_TYPE, Integer.toString(source.getPixelType(0)));
      fakeId = FakeUtils.appendToken(fakeId, BITS_PER_PIXEL, Integer.toString(source.getBitsPerPixel(0)));
      fakeId = FakeUtils.appendToken(fakeId, DIM_ORDER, FormatTools.findDimensionOrder(source, 0));
      fakeId = FakeUtils.appendToken(fakeId, INDEXED, Boolean.toString(source.isIndexed(0)));
      fakeId = FakeUtils.appendToken(fakeId, FALSE_COLOR, Boolean.toString(source.isFalseColor(0)));
      fakeId = FakeUtils.appendToken(fakeId, LITTLE_ENDIAN, Boolean.toString(source.isLittleEndian(0)));
      fakeId = FakeUtils.appendToken(fakeId, INTERLEAVED, Boolean.toString(source.isInterleaved(0)));
      fakeId = FakeUtils.appendToken(fakeId, META_COMPLETE, Boolean.toString(source.isMetadataComplete(0)));
      fakeId = FakeUtils.appendToken(fakeId, THUMBNAIL, Boolean.toString(source.isThumbnailImage(0)));
      fakeId = FakeUtils.appendToken(fakeId, ORDER_CERTAIN, Boolean.toString(source.isOrderCertain(0)));
      fakeId = FakeUtils.appendToken(fakeId, SERIES, Integer.toString(source.getImageCount()));
      fakeId = FakeUtils.appendToken(fakeId, RGB, Integer.toString(source.getRGBChannelCount(0)));
      
      if(source.getMetadataValue(SCALE_FACTOR) != null) {
        double scaleFactor = (Double)source.getMetadataValue(SCALE_FACTOR);
        fakeId = FakeUtils.appendToken(fakeId, SCALE_FACTOR, Double.toString(scaleFactor));
      }
      
      if(source.getMetadataValue(LUT_LENGTH) != null) {
        int lutLength = (Integer)source.getMetadataValue(LUT_LENGTH);
        fakeId = FakeUtils.appendToken(fakeId, LUT_LENGTH, Integer.toString(lutLength));
      }
      
      fakeId += ".fake";
      
      try {
        destination.setSource(new RandomAccessInputStream(new Handle(fakeId), fakeId));
      }
      catch (IOException e) {
        LOGGER.debug("Failed to create RAIS: " + fakeId, e);
      }
    }
  }
  
  /**
   * A special IRandomAccess handle for Fake files. This will ensure a
   * RAIS will never attempt file IO on a "*.fake" String, as this handle
   * is SezPoz-discoverable and handles specifically those files.
   */
  @DiscoverableHandle
  public static class Handle extends StreamHandle {

    // -- Constructor --
    
    public Handle() throws IOException {
      this("");
    }
    
    /**
     * Constructs a FakeHandle around the provided id.
     * No action needs to be taken except setting the file
     * field, as the id does not represetnt a real object. 
     */
    public Handle(String id) throws IOException {
      super();
      setFile(id);
    }
    
    // -- IStreamAccess API Methods --
    
    public boolean isConstructable(String id) throws IOException {
      return id.endsWith("fake");
    }

    public void resetStream() throws IOException {
      // no-op as there is no backing stream
    }
    
    // -- IRandomAccess API Methods --
    
    /* @see IRandomAccess#read(byte[]) */
    @Override
    public int read(byte[] b) throws IOException {
      // no-op
      return 0;
    }
    
    /* @see IRandomAccess#read(byte[], int, int) */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      // no-op
      return 0;
    }
    
    /* @see IRandomAccess#read(ByteBuffer) */
    @Override
    public int read(ByteBuffer buffer) throws IOException {
      // no-op
      return 0;
    }
    
    /* @see IRandomAccess#read(ByteBuffer, int, int) */
    @Override
    public int read(ByteBuffer buffer, int off, int len) throws IOException {
      return 0;
    }
    
    /* @see IRandomAccess#seek(long) */
    @Override
    public void seek(long pos) throws IOException {
      // no-op
    }
    
    /* @see IRandomAccess.write(ByteBuffer) */
    @Override
    public void write(ByteBuffer buf) throws IOException {
      // no-op
    }
    
    /* @see IRandomAccess.write(ByteBuffer, int, int) */
    @Override
    public void write(ByteBuffer buf, int off, int len) throws IOException {
      // no-op
    }
    
    // -- DataInput API Methods --
    
    /* @see java.io.DataInput#readChar() */
    @Override
    public char readChar() throws IOException {
      // no-op
      return 0;
    }
    
    /* @see java.io.DataInput#readDouble() */
    @Override
    public double readDouble() throws IOException {
      // no-op
      return 0.0;
    }
    
    /* @see java.io.DataInput#readFloat() */
    @Override
    public float readFloat() throws IOException {
      // no-op
      return 0f;
    }
    
    /* @see java.io.DataInput#readFully(byte[]) */
    @Override
    public void readFully(byte[] b) throws IOException {
      // no-op
    }
    
    /* @see java.io.DataInput#readFully(byte[], int, int) */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
      // no-op
    }
    
    /* @see java.io.DataInput#readInt() */
    @Override
    public int readInt() throws IOException {
      // no-op
      return 0;
    }
    
    /* @see java.io.DataInput#readLine() */
    public String readLine() throws IOException {
      throw new IOException("Unimplemented");
    }

    /* @see java.io.DataInput#readLong() */
    @Override
    public long readLong() throws IOException {
      // no-op
      return 0l;
    }

    /* @see java.io.DataInput#readShort() */
    @Override
    public short readShort() throws IOException {
      // no-op
      return 0;
    }

    /* @see java.io.DataInput#readUnsignedByte() */
    @Override
    public int readUnsignedByte() throws IOException {
      // no-op
      return 0;
    }

    /* @see java.io.DataInput#readUnsignedShort() */
    @Override
    public int readUnsignedShort() throws IOException {
      // no-op
      return 0;
    }

    /* @see java.io.DataInput#readUTF() */
    @Override
    public String readUTF() throws IOException {
      // no-op
      return "";
    }

    /* @see java.io.DataInput#skipBytes(int) */
    @Override
    public int skipBytes(int n) throws IOException {
      // no-op
      return 0;
    }

    // -- DataOutput API methods --

    /* @see java.io.DataOutput#write(byte[]) */
    @Override
    public void write(byte[] b) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#write(byte[], int, int) */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#write(int) */
    @Override
    public void write(int b) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeBoolean(boolean) */
    @Override
    public void writeBoolean(boolean v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeByte(int) */
    @Override
    public void writeByte(int v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeBytes(String) */
    @Override
    public void writeBytes(String s) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeChar(int) */
    @Override
    public void writeChar(int v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeChars(String) */
    @Override
    public void writeChars(String s) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeDouble(double) */
    @Override
    public void writeDouble(double v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeFloat(float) */
    @Override
    public void writeFloat(float v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeInt(int) */
    @Override
    public void writeInt(int v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeLong(long) */
    @Override
    public void writeLong(long v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeShort(int) */
    @Override
    public void writeShort(int v) throws IOException {
      // no-op
    }

    /* @see java.io.DataOutput#writeUTF(String) */
    @Override
    public void writeUTF(String str) throws IOException {
      // no-op
    }
  }
  
  /**
   * Helper methods for the Fake file format. Methods are provided for parsing
   * the key:value pairs from the name of a Fake file.
   */
  public static class FakeUtils {
    
    /**
     * Parses the provided path and returns a mapping of all known
     * key/value pairs that were discovered.
     * 
     * @param fakePath
     * @return
     */
    public static HashMap<String, String> extractFakeInfo(String fakePath) {
      HashMap<String, String> fakeMap = new HashMap<String, String>();
      
      if (new Location(fakePath).exists()) {
        fakePath = new Location(fakePath).getAbsoluteFile().getName();
      }
      
      String noExt = fakePath.substring(0, fakePath.lastIndexOf("."));
      String[] tokens = noExt.split(TOKEN_SEPARATOR);
      
      boolean named = false;
      
      // parse tokens from filename
      for (String token : tokens) {
        if (!named) {
          // first token is the image name
          fakeMap.put(NAME, token);
          named = true;
          continue;
        }
        int equals = token.indexOf("=");
        if (equals < 0) {
          //TODO  LOGGER.warn("ignoring token: {}", token);
          continue;
        }
        String key = token.substring(0, equals);
        String value = token.substring(equals + 1);
        
        fakeMap.put(key, value);
      }
      
      return fakeMap;
    }
    
    /**
     * Appends the provided key:value pair to the provided base and returns
     * the result.
     * 
     * @return
     */
    public static String appendToken(String base, String key, String value) {
      base += TOKEN_SEPARATOR + key + "=" + value;
      return base;
    }
    
    // -- Value extraction methods --
    
    /**
     * Returns the integer value of the passed String, or the provided
     * int value if the String is null.
     * 
     * @param newValue
     * @param oldValue
     * @return
     */
    public static int getIntValue(String newValue, int oldValue) {
      if(newValue == null) return oldValue;
      
      return Integer.parseInt(newValue);
    }
    
    /**
     * Returns the double value of the passed String, or the provided
     * double value if the String is null.
     * 
     * @param newValue
     * @param oldValue
     * @return
     */
    public static double getDoubleValue(String newValue, double oldValue) {
      if(newValue == null) return oldValue;
      
      return Double.parseDouble(newValue);
    }
    
    /**
     * Returns the boolean value of the passed String, or the provided
     * boolean value if the String is null.
     * 
     * @param newValue
     * @param oldValue
     * @return
     */
    public static boolean getBoolValue(String newValue, boolean oldValue) {
      if(newValue == null) return oldValue;
      
      return Boolean.parseBoolean(newValue);
    }
    
    /**
     * Populates a mapping between indicies and color values,
     * and the inverse mapping of color values to indicies.
     * </br></br>
     * NB: The array parameters will be modified by this method and should simply be
     * empty and initialized to the appropriate dimensions.
     *
     * @param indexToValue - a channel size X num values array, mapping indicies to color values.
     * @param valueToIndex - a channel size X num values array, mapping color values to indicies.
     */
    public static void createIndexMaps(int[][] indexToValue, int[][] valueToIndex) {
      sizeCheck(indexToValue, valueToIndex);
      createIndexValueMap(indexToValue);
      createInverseIndexMap(indexToValue, valueToIndex);
    }
    
    /**
     * Populates the given array with a random mapping of indices to values.
     * 
     * @param indexToValue - An empty array that will be populated with an index:value mapping.
     */
    public static void createIndexValueMap(int[][] indexToValue) {
      for (int c=0; c<indexToValue.length; c++) {
        for (int index=0; index<indexToValue[0].length; index++) indexToValue[c][index] = index;
        shuffle(c, indexToValue[c]);
      }
    }
    
    /**
     * Populates an array with inverse mapping of values and indices,
     * drawn from a base index:value mapping.
     * 
     * @param indexToValue - A populated mapping of indicies to color values.
     * @param valueToIndex - An empty array that will be populated with the inverse of indexToValue.
     */
    public static void createInverseIndexMap(final int[][] indexToValue, int[][] valueToIndex) {
      sizeCheck(indexToValue, valueToIndex);
      
      for (int c=0; c<indexToValue.length; c++) {
        for (int index=0; index<indexToValue[0].length; index++) {
          int value = indexToValue[c][index];
          valueToIndex[c][value] = index;
        }
      }
    }
    
    /** Fisher-Yates shuffle with constant seeds to ensure reproducibility. */
    public static void shuffle(int c, int[] array) {
      Random r = new Random(SEED + c);
      for (int i = array.length; i > 1; i--) {
        int j = r.nextInt(i);
        int tmp = array[j];
        array[j] = array[i - 1];
        array[i - 1] = tmp;
      }
    }
    
    /* Verifies two arrays are of the same size. */
    private static void sizeCheck(int[][] array1, int[][] array2) {
      if(array1.length != array2.length || array1[0].length != array2[0].length)
        throw new IllegalArgumentException("Arrays must be of the same size.");
    }
  }
}
