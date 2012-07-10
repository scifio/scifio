package ome.scifio.ics;

import java.util.StringTokenizer;
import java.util.Vector;

import net.imglib2.meta.Axes;
import net.imglib2.meta.Axes.CustomAxisType;
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
 * SCIFIO file format Translator for ICS Metadata objects
 * to the SCIFIO Core metadata type. 
 *
 */
@SCIFIOTranslator(metaIn = ICSMetadata.class, metaOut = CoreMetadata.class)
public class ICSCoreTranslator
  extends AbstractTranslator<ICSMetadata, CoreMetadata>
  implements CoreTranslator {

  private ICSMetadata curSource;

  // -- Constructors --

  public ICSCoreTranslator() {
    this(null);
  }

  public ICSCoreTranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API Methods --

  public void translate(final ICSMetadata source, final CoreMetadata destination)
  {
    final CoreImageMetadata coreMeta = new CoreImageMetadata();
    destination.add(coreMeta);
    final int index = destination.getImageCount() - 1;

    curSource = source;

    coreMeta.setRgb(false);

    // find axis sizes

    AxisType[] axisTypes = null;
    int[] axisLengths = null;

    int bitsPerPixel = 0;

    StringTokenizer layoutTokens = getTknz("layout sizes");
    axisLengths = new int[layoutTokens.countTokens()];
    
    for (int n = 0; n < axisLengths.length; n++) {
      try {
        axisLengths[n] = Integer.parseInt(layoutTokens.nextToken().trim());
      }
      catch (final NumberFormatException e) {
        LOGGER.debug("Could not parse axis length", e);
      }
    }
    
    layoutTokens = getTknz("layout order");
    axisTypes = new AxisType[layoutTokens.countTokens()];

    final Vector<Integer> channelLengths = new Vector<Integer>();
    final Vector<String> channelTypes = new Vector<String>();

    for (int n = 0; n < axisTypes.length; n++) {
      final String tkn = layoutTokens.nextToken().trim();
      if (tkn.equals("x")) {
        axisTypes[n] = Axes.X;
      }
      else if (tkn.equals("y")) {
        axisTypes[n] = Axes.Y;
      }
      else if (tkn.equals("z")) {
        axisTypes[n] = Axes.Z;
      }
      else if (tkn.equals("t")) {
        axisTypes[n] = Axes.TIME;
      }
      else if (tkn.startsWith("c")) {
        axisTypes[n] = Axes.CHANNEL;
        channelTypes.add(FormatTools.CHANNEL);
        channelLengths.add(axisLengths[n]);
      }
      else if (tkn.startsWith("p")) {
        axisTypes[n] = Axes.PHASE;
        channelTypes.add(FormatTools.PHASE);
        channelLengths.add(axisLengths[n]);
      }
      else if (tkn.startsWith("f")) {
        axisTypes[n] = Axes.FREQUENCY;
        channelTypes.add(FormatTools.FREQUENCY);
        channelLengths.add(axisLengths[n]);
      }
      else if (tkn.equals("bits")) {
        axisTypes[n] = new CustomAxisType("bits");
        bitsPerPixel = axisLengths[n];
        while (bitsPerPixel % 8 != 0)
          bitsPerPixel++;
        if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
      }
      else {
        axisTypes[n] = Axes.UNKNOWN;
        channelTypes.add("");
        channelLengths.add(axisLengths[n]);
      }
    }

    if (source.get("layout significant_bits") != null) {
      destination.setBitsPerPixel(
        index, Integer.parseInt(source.get("layout significant_bits")));
    }
    else destination.setBitsPerPixel(index, bitsPerPixel);

    coreMeta.setAxisLengths(axisLengths);
    coreMeta.setAxisTypes(axisTypes);
    //storedRGB = getSizeX() == 0;

    if (channelLengths.size() == 0) {
      channelLengths.add(new Integer(1));
      channelTypes.add(FormatTools.CHANNEL);
    }

    final int[] cLengths = new int[channelLengths.size()];
    final String[] cTypes = new String[channelLengths.size()];

    for (int i = 0; i < channelLengths.size(); i++) {
      cLengths[i] = channelLengths.get(i);
      cTypes[i] = channelTypes.get(i);
    }

    coreMeta.setcLengths(cLengths);
    coreMeta.setcTypes(cTypes);

    if (destination.getAxisIndex(index, Axes.Z) == -1) {
      destination.addAxis(index, Axes.Z, 1);
    }
    if (destination.getAxisIndex(index, Axes.CHANNEL) == -1) {
      destination.addAxis(index, Axes.CHANNEL, 1);
    }
    if (destination.getAxisIndex(index, Axes.TIME) == -1) {
      destination.addAxis(index, Axes.TIME, 1);
    }

    if (destination.getAxisLength(index, Axes.Z) == 0)
      coreMeta.setAxisLength(Axes.Z, 1);
    if (destination.getAxisLength(index, Axes.CHANNEL) == 0)
      coreMeta.setAxisLength(Axes.CHANNEL, 1);
    if (destination.getAxisLength(index, Axes.TIME) == 0)
      coreMeta.setAxisLength(Axes.TIME, 1);

    coreMeta.setPlaneCount(destination.getAxisLength(0, Axes.Z) *
      destination.getAxisLength(0, Axes.TIME));

    coreMeta.setInterleaved(destination.isRGB(0));
    //TODO coreMeta.imageCount = getSizeZ() * getSizeT();
    //TODO if (destination.isRGB(0)) coreMeta.imageCount *= getSizeC();
    destination.setIndexed(index, false);
    destination.setFalseColor(index, false);
    destination.setMetadataComplete(index, true);
    destination.setLittleEndian(index, true);

    final String history = source.get("history type");
    boolean lifetime = false;
    if (history != null &&
      (history.equalsIgnoreCase("time resolved") || history.equalsIgnoreCase("FluorescenceLifetime")))
      lifetime = true;

    // HACK - support for Gray Institute at Oxford's ICS lifetime data
    if (lifetime) {
      final int binCount = destination.getAxisLength(index, Axes.Z);
      coreMeta.setAxisLength(
        Axes.Z, (destination.getAxisLength(index, Axes.CHANNEL)));
      coreMeta.setAxisLength(Axes.CHANNEL, binCount);
      coreMeta.setcLengths(new int[] {binCount});
      coreMeta.setcTypes(new String[] {FormatTools.LIFETIME});
      final int cIndex = destination.getAxisIndex(index, Axes.CHANNEL);
      final int zIndex = destination.getAxisIndex(index, Axes.Z);
      coreMeta.setAxisType(cIndex, Axes.Z);
      coreMeta.setAxisType(zIndex, Axes.CHANNEL);
    }

    final String byteOrder = source.get("representation byte_order");
    final String rFormat = source.get("representation format");
    final String compression = source.get("representation compression");

    if (byteOrder != null) {
      final String firstByte = byteOrder.split(" ")[0];
      final int first = Integer.parseInt(firstByte);
      coreMeta.setLittleEndian(rFormat.equals("real") ? first == 1 : first != 1);
    }

    final boolean gzip =
      (compression == null) ? false : compression.equals("gzip");

    final int bytes = bitsPerPixel / 8;

    if (bitsPerPixel < 32)
      coreMeta.setLittleEndian(!destination.isLittleEndian(0));

    final boolean floatingPt = rFormat.equals("real");
    final boolean signed = source.get("representation sign").equals("signed");

    try {
      coreMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed, floatingPt));
    }
    catch (final FormatException e) {
      e.printStackTrace();
    }

  }

  // -- Helper Methods --

  StringTokenizer getTknz(final String target) {
    return new StringTokenizer(curSource.get(target));
  }
}
