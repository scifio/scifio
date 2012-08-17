package ome.scifio.apng;

import java.io.IOException;

import ome.scifio.AbstractParser;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.io.RandomAccessInputStream;

/**
 * File format SCIFIO Parser for Animated Portable Network Graphics
 * (APNG) images.
 *
 */
public class APNGParser extends AbstractParser<APNGMetadata> {

  // -- Fields --

  // -- Constructor --

  /** Constructs a new APNGParser. */
  public APNGParser() {
    this(null);
  }

  public APNGParser(final SCIFIO ctx) {
    super(ctx, APNGMetadata.class);
  }

  // -- Parser API Methods --

  /* @see ome.scifio.AbstractParser#parse(RandomAccessInputStream stream) */
  @Override
  public APNGMetadata parse(final RandomAccessInputStream stream)
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
