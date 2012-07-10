package ome.scifio.apng;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.AbstractTranslator;
import ome.scifio.CoreImageMetadata;
import ome.scifio.CoreMetadata;
import ome.scifio.CoreTranslator;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.util.FormatTools;

/**
 * File format SCIFIO Translator for Animated Portable Network Graphics
 * (APNG) images to the Core SCIFIO image type.
 *
 */
@SCIFIOTranslator(metaIn = APNGMetadata.class, metaOut = CoreMetadata.class)
public class APNGCoreTranslator
  extends AbstractTranslator<APNGMetadata, CoreMetadata>
  implements CoreTranslator {

  // -- Constructors --

  public APNGCoreTranslator() {
    this(null);
  }

  public APNGCoreTranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API Methods --

  public void translate(final APNGMetadata source, final CoreMetadata dest) {
    final CoreImageMetadata coreMeta = new CoreImageMetadata();
    dest.add(coreMeta);

    coreMeta.setInterleaved(false);
    coreMeta.setOrderCertain(true);
    coreMeta.setFalseColor(true);

    coreMeta.setIndexed(false);

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

    if (indexed) {
      final byte[][] lut = new byte[3][0];

      lut[0] = source.getPlte().getRed();
      lut[1] = source.getPlte().getGreen();
      lut[2] = source.getPlte().getBlue();

      coreMeta.setLut(lut);
    }

    final APNGacTLChunk actl = source.getActl();
    final int planeCount = actl == null ? 1 : actl.getNumFrames();

    coreMeta.setAxisTypes(new AxisType[] {
        Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME, Axes.Z});
    coreMeta.setAxisLengths(new int[] {
        source.getIhdr().getWidth(), source.getIhdr().getHeight(), sizec,
        planeCount, 1});

    final int bpp = source.getIhdr().getBitDepth();

    coreMeta.setBitsPerPixel(bpp);
    try {
      coreMeta.setPixelType(FormatTools.pixelTypeFromBytes(
        bpp / 8, false, false));
    }
    catch (final FormatException e) {
      e.printStackTrace();
    }
    coreMeta.setRgb(rgb);
    coreMeta.setIndexed(indexed);
    coreMeta.setPlaneCount(planeCount);
    coreMeta.setLittleEndian(false);

    // Some anciliary chunks may not have been parsed
    coreMeta.setMetadataComplete(false);

    coreMeta.setThumbnail(false);
    //coreMeta.setThumbSizeX(source.thumbSizeX);
    //coreMeta.setThumbSizeY(source.thumbSizeY);

    //coreMeta.setcLengths(source.cLengths);
    //coreMeta.setcTypes(source.cTypes);

    //TODO could generate this via fields?
    //coreMeta.setImageMetadata(source.imageMetadata);
  }
}
