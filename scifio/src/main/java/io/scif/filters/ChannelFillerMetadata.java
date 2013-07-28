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
package io.scif.filters;

import io.scif.Metadata;
import io.scif.util.FormatTools;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * {@link io.scif.filters.MetadataWrapper} implementation specifically
 * for use with the {@link io.scif.filters.ChannelFiller}.
 * 
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.filters.ChannelFiller
 * 
 * @author Mark Hiner
 */
@Plugin(type=MetadataWrapper.class, attrs={
  @Attr(name=ChannelFillerMetadata.METADATA_KEY, value=ChannelFillerMetadata.METADATA_VALUE)
  })
public class ChannelFillerMetadata extends AbstractMetadataWrapper {

  // -- Constants --

  public static final String METADATA_VALUE = "io.scif.filters.ChannelFiller";

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
   * @see io.scif.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isRGB(imageIndex);
    return getRGBChannelCount(imageIndex) > 1;
  }

  /*
   * @see io.scif.AbstractMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    if (!isFilled(imageIndex)) return super.isIndexed(imageIndex);
    return false;
  }

  /*
   * @see io.scif.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    int length = unwrap().getAxisLength(imageIndex, t);

    if(!t.equals(Axes.CHANNEL))
      return length;

    return (!isFilled(imageIndex)) ? length : length * lutLength;
  }

}
