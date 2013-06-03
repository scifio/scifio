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
package ome.xml.translation;

import io.scif.MetadataLevel;
import io.scif.formats.BMPFormat;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and BMP formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class BMPTranslator {

  /**
   * Translator class from {@link io.scif.formats.BMPFormat.Metadata} to
   * {@link ome.xml.meta.OMEMetadata}
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = ToOMETranslator.class, priority = Priority.HIGH_PRIORITY,
      attrs = {
    @Attr(name = BMPOMETranslator.SOURCE, value = BMPFormat.Metadata.CNAME),
    @Attr(name = BMPOMETranslator.DEST, value = OMEMetadata.CNAME)
  })
  public static class BMPOMETranslator extends ToOMETranslator<BMPFormat.Metadata> {

    // -- Translator API Methods --

    /*
     * @see OMETranslator#typedTranslate(io.scif.Metadata, io.scif.Metadata)
     */
    @Override
    protected void typedTranslate(BMPFormat.Metadata source, OMEMetadata dest) {
      if (source.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
        // resolution is stored as pixels per meter; we want to convert to
        // microns per pixel

        int pixelSizeX = (Integer) source.getTable().get("X resolution");

        int pixelSizeY = (Integer) source.getTable().get("Y resolution");

        double correctedX = pixelSizeX == 0 ? 0.0 : 1000000.0 / pixelSizeX;
        double correctedY = pixelSizeY == 0 ? 0.0 : 1000000.0 / pixelSizeY;

        if (correctedX > 0) {
          dest.getRoot().setPixelsPhysicalSizeX(new PositiveFloat(correctedX), 0);
        }
        else {
          LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
              correctedX);
        }
        if (correctedY > 0) {
          dest.getRoot().setPixelsPhysicalSizeY(new PositiveFloat(correctedY), 0);
        }
        else {
          LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
              correctedY);
        }
      }
    }
  }
}
