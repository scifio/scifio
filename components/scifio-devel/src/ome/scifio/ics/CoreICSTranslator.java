package ome.scifio.ics;

import java.util.Hashtable;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import ome.scifio.AbstractTranslator;
import ome.scifio.CoreMetadata;
import ome.scifio.CoreTranslator;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.util.FormatTools;

/**
 * SCIFIO file format Translator for ICS Metadata objects to the SCIFIO Core
 * metadata type.
 * 
 */
@SCIFIOTranslator(metaIn = CoreMetadata.class, metaOut = ICSMetadata.class)
public class CoreICSTranslator
  extends AbstractTranslator<CoreMetadata, ICSMetadata>
  implements CoreTranslator {

  // -- Constructors --

  public CoreICSTranslator() {
    this(null);
  }

  public CoreICSTranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API Methods --

  public void translate(final CoreMetadata source, final ICSMetadata destination)
  {
    // note that the destination fields will preserve their default values
    // only the keyValPairs will be modified

    Hashtable<String, String> keyValPairs = null;
    if (destination == null || destination.getKeyValPairs() == null) keyValPairs =
      new Hashtable<String, String>();
    else 
      keyValPairs = destination.getKeyValPairs();

    final int numAxes = source.getAxisCount(0);

    String order = "";
    String sizes = "";

    for (int i = 0; i < numAxes; i++) {
      final AxisType axis = source.getAxisType(0, i);

      if (axis.equals(Axes.X)) {
        order += "x";
      }
      else if (axis.equals(Axes.Y)) {
        order += "y";
      }
      else if (axis.equals(Axes.Z)) {
        order += "z";
      }
      else if (axis.equals(Axes.TIME)) {
        order += "t";
      }
      else if (axis.equals(Axes.CHANNEL)) {
        order += "c";
      }
      else if (axis.equals(Axes.PHASE)) {
        order += "p";
      }
      else if (axis.equals(Axes.FREQUENCY)) {
        order += "f";
      }
      else {
        if(axis.getLabel().equals("bits"))
          order += "bits";
        else
          order += "u";
      }
      
      order += " ";
      sizes += source.getAxisLength(0, i) + " ";
    }

    keyValPairs.put("layout sizes", sizes);
    keyValPairs.put("layout order", order);
    
    keyValPairs.put("layout significant_bits", "" + source.getBitsPerPixel(0));
    
    if(source.getChannelDimTypes(0).equals(FormatTools.LIFETIME))
      keyValPairs.put("history type", "time resolved");
    
    boolean signed = false;
    boolean fPoint = false;
    
    switch (source.getPixelType(0)) {
      case FormatTools.INT8:
      case FormatTools.INT16:
      case FormatTools.INT32:
        signed = true;
        break;
      case FormatTools.UINT8:
      case FormatTools.UINT16:
      case FormatTools.UINT32:
        break;
      case FormatTools.FLOAT:
      case FormatTools.DOUBLE:
        fPoint = true;
        signed = true;
        break;

    }
    
    keyValPairs.put("representation sign", signed ? "signed" : "");
    keyValPairs.put("representation format", fPoint ? "real" : "");
    keyValPairs.put("representation compression", "");
    
    String byteOrder = "0";
    
    if(source.isLittleEndian(0))
      byteOrder = fPoint ? "1" : "0";
    else
      byteOrder = fPoint ? "0" : "1";
      
    keyValPairs.put("representation byte_order", byteOrder);
    
    destination.setKeyValPairs(keyValPairs);
  }

}
