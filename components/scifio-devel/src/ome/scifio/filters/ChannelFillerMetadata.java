/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
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
package ome.scifio.filters;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.Metadata;
import ome.scifio.util.FormatTools;

/**
 * {@link ome.scifio.filters.MetadataWrapper} implementation specifically
 * for use with the {@link ome.scifio.filters.ChannelFiller}.
 * 
 * @see ome.scifio.filters.MetadataWrapper
 * @see ome.scifio.filters.ChannelFiller
 * 
 * @author Mark Hiner
 */
@Plugin(type=MetadataWrapper.class, attrs={
  @Attr(name=ChannelFillerMetadata.METADATA_KEY, value=ChannelFillerMetadata.METADATA_VALUE)
  })
public class ChannelFillerMetadata extends AbstractMetadataWrapper {
  
  // -- Constants --
  
  public static final String METADATA_VALUE = "ome.scifio.filters.ChannelFiller";
  
  // -- Fields --
  
  private Boolean filled = null;
  
  private int lutLength;
  
  // -- Constructors --
  
  public ChannelFillerMetadata() {
    this(null);
  }
  
  public ChannelFillerMetadata(Metadata metadata) {
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
   * @param length - Number of components in the lut
   */
  public void setLutLength(int length) {
    lutLength = length;
  }
  
  // -- Metadata API methods --
  
  /*
   * @see ome.scifio.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isRGB(imageIndex);
    return getRGBChannelCount(imageIndex) > 1;
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isIndexed(imageIndex);
    return false;
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    int length = unwrap().getAxisLength(imageIndex, t);

    if(!t.equals(Axes.CHANNEL))
      return length;

    return (!isFilled(imageIndex)) ? length : length * lutLength; 
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#getChannelDimLengths(int)
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
   * @see ome.scifio.AbstractMetadata#getChannelDimTypes(int)
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
