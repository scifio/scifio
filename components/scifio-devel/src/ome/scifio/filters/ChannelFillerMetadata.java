package ome.scifio.filters;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.DatasetMetadata;
import ome.scifio.discovery.DiscoverableMetadataWrapper;
import ome.scifio.util.FormatTools;

/**
 * @author Mark Hiner
 *
 */
@DiscoverableMetadataWrapper(filterType = ChannelFiller.class)
public class ChannelFillerMetadata extends AbstractDatasetMetadataWrapper {
  
  private Boolean filled = null;
  
  private int lutLength;
  
  // -- Constructors --
  
  public ChannelFillerMetadata() {
    this(null);
  }
  
  public ChannelFillerMetadata(DatasetMetadata metadata) {
    super(metadata);
  }
  
  // -- ChannelFillerMetadata API Methods --
  
  /** Returns true if the indices are being factored out. */
  public boolean isFilled(int imageIndex) {
    if (!isIndexed(imageIndex)) return false; // cannot fill non-indexed color
    if (lutLength < 1) return false; // cannot fill when LUTs are missing
    return filled == null ? !isFalseColor(imageIndex) : filled;
  }

  /** Toggles whether the indices should be factored out. */
  public void setFilled(boolean filled) {
    this.filled = filled;
  }
  
  /**
   * @param length
   */
  public void setLutLength(int length) {
    lutLength = length;
  }
  
  // -- DatasetMetadata API methods --
  
  /*
   * @see ome.scifio.AbstractDatasetMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isRGB(imageIndex);
    return getRGBChannelCount(imageIndex) > 1;
  }
  
  /*
   * @see ome.scifio.AbstractDatasetMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isIndexed(imageIndex);
    return false;
  }
  
  /*
   * @see ome.scifio.AbstractDatasetMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    int length = unwrap().getAxisLength(imageIndex, t);

    if(!t.equals(Axes.CHANNEL))
      return length;

    return (!isFilled(imageIndex)) ? length : length * lutLength; 
  }
  
  /*
   * @see ome.scifio.AbstractDatasetMetadata#getChannelDimLengths(int)
   */
  public int[] getChannelDimLengths(int imageIndex) {
    int[] cLengths = getChannelDimLengths(imageIndex);
    if (!isFilled(imageIndex)) return cLengths;

    // in the case of a single channel, replace rather than append
    if (cLengths.length == 1 && cLengths[0] == 1) cLengths = new int[0];

    // append filled dimension to channel dim lengths
    int[] newLengths = new int[1 + cLengths.length];
    newLengths[0] = lutLength;
    System.arraycopy(cLengths, 0, newLengths, 1, cLengths.length);
    return newLengths;
  }

  /*
   * @see ome.scifio.AbstractDatasetMetadata#getChannelDimTypes(int)
   */
  public String[] getChannelDimTypes(int imageIndex) {
    String[] cTypes = getChannelDimTypes(imageIndex);
    if (!isFilled(imageIndex)) return cTypes;

    // in the case of a single channel, leave type unchanged
    int[] cLengths = getChannelDimLengths(imageIndex);
    if (cLengths.length == 1 && cLengths[0] == 1) return cTypes;

    // append filled dimension to channel dim types
    String[] newTypes = new String[1 + cTypes.length];
    newTypes[0] = FormatTools.CHANNEL;
    System.arraycopy(cTypes, 0, newTypes, 1, cTypes.length);
    return newTypes;
  }
}
