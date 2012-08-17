package ome.scifio.apng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.meta.Axes;
import ome.scifio.AbstractTranslator;
import ome.scifio.CoreMetadata;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOTranslator;

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
@SCIFIOTranslator(metaIn = CoreMetadata.class, metaOut = APNGMetadata.class)
public class CoreAPNGTranslator
  extends AbstractTranslator<CoreMetadata, APNGMetadata> {

  // -- Constructors --
	
  public CoreAPNGTranslator() {
	  this(null);
  }
	
  public CoreAPNGTranslator(SCIFIO ctx) {
    super(ctx);
  }
	
  // -- Translator API Methods --	
  @Override
  public void translate(final CoreMetadata source, final APNGMetadata dest) {

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
      byte[][] lut = null;
      try {
        lut = source.get8BitLookupTable(0);
        plte.setRed(lut[0]);
        plte.setGreen(lut[1]);
        plte.setBlue(lut[2]);
      }
      catch (final FormatException e) {
        e.printStackTrace();
      }
      catch (final IOException e) {
        e.printStackTrace();
      }

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
