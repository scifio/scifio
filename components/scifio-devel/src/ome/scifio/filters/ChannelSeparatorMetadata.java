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

import ome.scifio.Metadata;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * {@link ome.scifio.filters.MetadataWrapper} implementation specifically
 * for use with the {@link ome.scifio.filters.ChannelSeparator}.
 * 
 * @see ome.scifio.filters.MetadataWrapper
 * @see ome.scifio.filters.ChannelSeparator
 * 
 * @author Mark Hiner
 */
@Plugin(type=MetadataWrapper.class, attrs={
  @Attr(name=ChannelSeparatorMetadata.METADATA_KEY, value=ChannelSeparatorMetadata.METADATA_VALUE)
  })
public class ChannelSeparatorMetadata extends AbstractMetadataWrapper {
  
  // -- Constants --
  
  public static final String METADATA_VALUE = "ome.scifio.filters.ChannelSeparator";
  
  // -- Fields -- 

  private AxisType[] xyczt = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME};
  private AxisType[] xyctz = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME, Axes.Z}; 
  
  
  // -- Constructors --
  
  public ChannelSeparatorMetadata() {
    this(null);
  }
  
  public ChannelSeparatorMetadata(Metadata metadata) {
    super(metadata);
  }
  
  
  // -- Reader API Methods --
  
  /*
   * @see ome.scifio.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    return isIndexed(imageIndex) && !isFalseColor(imageIndex)
      && getAxisLength(imageIndex, Axes.CHANNEL) > 1;
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#getAxes(int)
   */
  public AxisType[] getAxes(int imageIndex) {
    if (unwrap().isRGB(imageIndex) && !unwrap().isIndexed(imageIndex)) {
      
      if (unwrap().getAxisIndex(imageIndex, Axes.TIME) > unwrap().getAxisIndex(imageIndex, Axes.Z))
        return xyczt;
      else
        return xyctz;
      
    }
    return unwrap().getAxes(imageIndex);
  }
}
