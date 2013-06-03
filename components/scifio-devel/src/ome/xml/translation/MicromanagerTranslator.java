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

import io.scif.FormatException;
import io.scif.MetadataLevel;
import io.scif.common.DateTools;
import io.scif.formats.MicromanagerFormat;
import io.scif.formats.MicromanagerFormat.Metadata;
import io.scif.formats.MicromanagerFormat.Position;
import io.scif.io.Location;

import java.io.IOException;
import java.util.Vector;

import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.Timestamp;
import ome.xml.services.OMEXMLMetadataService;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and Micromanager formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class MicromanagerTranslator {

  /**
   * Translator class from {@link ome.xml.meta.OMEMetadata} to
   * {@link io.scif.formats.MicromanagerFormat.Metadata}.
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = FromOMETranslator.class, priority = Priority.HIGH_PRIORITY,
      attrs = {
    @Attr(name = MicromanagerOMETranslator.SOURCE, value = MicromanagerFormat.Metadata.CNAME),
    @Attr(name = MicromanagerOMETranslator.DEST, value = OMEMetadata.CNAME)
  })
  public static class MicromanagerOMETranslator extends ToOMETranslator<MicromanagerFormat.Metadata> {

    /*
     * @see OMETranslator#typedTranslate(io.scif.Metadata, io.scif.Metadata)
     */
    @Override
    protected void typedTranslate(MicromanagerFormat.Metadata source, OMEMetadata dest) {
      super.typedTranslate(source, dest);

      try {
        populateMetadata(source, dest.getRoot());
      } catch (FormatException e) {
        LOGGER.error("Error populating Metadata store with Micromanager metadata", e);
      } catch (IOException e) {
        LOGGER.error("Error populating Metadata store with Micromanager metadata", e);
      }
    }

    private void populateMetadata(Metadata meta, OMEXMLMetadata store) throws FormatException, IOException {
      OMEXMLMetadataService service = scifio().get(OMEXMLMetadataService.class);
      String instrumentID = service.createLSID("Instrument", 0);
      store.setInstrumentID(instrumentID, 0);
      Vector<Position> positions = meta.getPositions();

      for (int i=0; i<positions.size(); i++) {
        Position p = positions.get(i);
        if (p.time != null) {
          String date = DateTools.formatDate(p.time, MicromanagerFormat.Parser.DATE_FORMAT);
          if (date != null) {
            store.setImageAcquisitionDate(new Timestamp(date), i);
          }
        }

        if (positions.size() > 1) {
          Location parent = new Location(getContext(), p.metadataFile).getParentFile();
          store.setImageName(parent.getName(), i);
        }

        if (meta.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
          store.setImageDescription(p.comment, i);

          // link Instrument and Image
          store.setImageInstrumentRef(instrumentID, i);

          for (int c=0; c<p.channels.length; c++) {
            store.setChannelName(p.channels[c], i, c);
          }

          if (p.pixelSize != null && p.pixelSize > 0) {
            store.setPixelsPhysicalSizeX(new PositiveFloat(p.pixelSize), i);
            store.setPixelsPhysicalSizeY(new PositiveFloat(p.pixelSize), i);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
                p.pixelSize);
          }
          if (p.sliceThickness != null && p.sliceThickness > 0) {
            store.setPixelsPhysicalSizeZ(new PositiveFloat(p.sliceThickness), i);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeZ; got {}",
                p.sliceThickness);
          }

          int nextStamp = 0;
          for (int q=0; q<meta.getPlaneCount(i); q++) {
            store.setPlaneExposureTime(p.exposureTime, i, q);
            String tiff = positions.get(i).getFile(meta, i, q);
            if (tiff != null && new Location(getContext(), tiff).exists() &&
                nextStamp < p.timestamps.length)
            {
              store.setPlaneDeltaT(p.timestamps[nextStamp++], i, q);
            }
          }

          String serialNumber = p.detectorID;
          p.detectorID = service.createLSID("Detector", 0, i);

          for (int c=0; c<p.channels.length; c++) {
            store.setDetectorSettingsBinning(service.getBinning(p.binning), i, c);
            store.setDetectorSettingsGain(new Double(p.gain), i, c);
            if (c < p.voltage.size()) {
              store.setDetectorSettingsVoltage(p.voltage.get(c), i, c);
            }
            store.setDetectorSettingsID(p.detectorID, i, c);
          }

          store.setDetectorID(p.detectorID, 0, i);
          if (p.detectorModel != null) {
            store.setDetectorModel(p.detectorModel, 0, i);
          }

          if (serialNumber != null) {
            store.setDetectorSerialNumber(serialNumber, 0, i);
          }

          if (p.detectorManufacturer != null) {
            store.setDetectorManufacturer(p.detectorManufacturer, 0, i);
          }

          if (p.cameraMode == null) p.cameraMode = "Other";
          store.setDetectorType(service.getDetectorType(p.cameraMode), 0, i);
          store.setImagingEnvironmentTemperature(p.temperature, i);
        }
      }
    }

  }
}
