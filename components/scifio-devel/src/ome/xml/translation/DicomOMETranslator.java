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

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import ome.scifio.MetadataLevel;
import ome.scifio.common.DateTools;
import ome.scifio.formats.DicomFormat;
import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.Timestamp;

/**
 * Translator class from {@link ome.scifio.formats.DicomFormat.Metadata} to
 * {@link ome.xml.meta.OMEMetadata}
 * <p>
 * NB: Plugin priority is set to high to be selected over the base
 * {@link ome.scifio.Metadata} translator.
 * </p>
 * 
 * @author Mark Hiner
 */
@Plugin(type = ToOMETranslator.class, priority = Priority.HIGH_PRIORITY,
attrs = {
  @Attr(name = BMPOMETranslator.SOURCE, value = DicomFormat.Metadata.CNAME),
  @Attr(name = BMPOMETranslator.DEST, value = OMEMetadata.CNAME)
})
public class DicomOMETranslator extends ToOMETranslator<DicomFormat.Metadata> {
  
  // -- Translator API Methods --

  /*
   * @see OMETranslator#typedTranslate(ome.scifio.Metadata, ome.scifio.Metadata)
   */
  @Override
  protected void typedTranslate(DicomFormat.Metadata source, OMEMetadata dest) {
    // The metadata store we're working with.

    String stamp = null;
    
    OMEXMLMetadata store = dest.getRoot();
    
    String date = source.getDate();
    String time = source.getTime();
    String imageType = source.getImageType();
    String pixelSizeX = source.getPixelSizeX();
    String pixelSizeY = source.getPixelSizeY();
    Double pixelSizeZ = source.getPixelSizeZ();

    if (date != null && time != null) {
      stamp = date + " " + time;
      stamp = DateTools.formatDate(stamp, "yyyy.MM.dd HH:mm:ss.SSSSSS");
    }

    if (stamp == null || stamp.trim().equals("")) stamp = null;

    for (int i=0; i<source.getImageCount(); i++) {
      if (stamp != null) store.setImageAcquisitionDate(new Timestamp(stamp), i);
      store.setImageName("Series " + i, i);
    }

    if (source.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
        for (int i=0; i<source.getImageCount(); i++) {
        store.setImageDescription(imageType, i);

        if (pixelSizeX != null) {
          Double sizeX = new Double(pixelSizeX);
          if (sizeX > 0) {
            store.setPixelsPhysicalSizeX(new PositiveFloat(sizeX), i);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
              sizeX);
          }
        }
        if (pixelSizeY != null) {
          Double sizeY = new Double(pixelSizeY);
          if (sizeY > 0) {
            store.setPixelsPhysicalSizeY(new PositiveFloat(sizeY), i);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
              sizeY);
          }
        }
        if (pixelSizeZ != null && pixelSizeZ > 0) {
          store.setPixelsPhysicalSizeZ(new PositiveFloat(pixelSizeZ), i);
        }
        else {
          LOGGER.warn("Expected positive value for PhysicalSizeZ; got {}",
            pixelSizeZ);
        }
      }
    }
  }
}
