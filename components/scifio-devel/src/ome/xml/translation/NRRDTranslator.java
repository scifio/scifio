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
import io.scif.formats.NRRDFormat;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and NRRD formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class NRRDTranslator {

  /**
   * Translator class from {@link io.scif.formats.NRRDFormat.Metadata} to
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
    @Attr(name = NRRDOMETranslator.SOURCE, value = NRRDFormat.Metadata.CNAME),
    @Attr(name = NRRDOMETranslator.DEST, value = OMEMetadata.CNAME)
  })
  public static class NRRDOMETranslator extends ToOMETranslator<NRRDFormat.Metadata> {

    // -- Translator API Methods --

    @Override
    protected void typedTranslate(NRRDFormat.Metadata source, OMEMetadata dest) {

      if (source.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {

        String[] pixelSizes = source.getPixelSizes();

        if (pixelSizes != null) {
          for (int i=0; i<pixelSizes.length; i++) {
            if (pixelSizes[i] == null) continue;
            try {
              Double d = new Double(pixelSizes[i].trim());
              if (d > 0) {
                if (i == 0) {
                  dest.getRoot().setPixelsPhysicalSizeX(new PositiveFloat(d), 0);
                }
                else if (i == 1) {
                  dest.getRoot().setPixelsPhysicalSizeY(new PositiveFloat(d), 0);
                }
                else if (i == 2) {
                  dest.getRoot().setPixelsPhysicalSizeZ(new PositiveFloat(d), 0);
                }
              }
              else {
                LOGGER.warn(
                    "Expected positive value for PhysicalSize; got {}", d);
              }
            }
            catch (NumberFormatException e) { }
          }
        }
      }
    }
  }
}