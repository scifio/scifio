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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.imglib2.meta.Axes;
import ome.scifio.MetadataLevel;
import ome.scifio.MetadataOptions;
import ome.scifio.formats.ICSFormat;
import ome.scifio.util.FormatTools;
import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Translator class from {@link ome.scifio.formats.ICSFormat.Metadata} to
 * {@link ome.xml.meta.OMEMetadata}.
 * <p>
 * NB: Plugin priority is set to high to be selected over the base
 * {@link ome.scifio.Metadata} translator.
 * </p>
 * 
 * @author Mark Hiner
 */
@Plugin(type = FromOMETranslator.class, priority = Priority.HIGH_PRIORITY,
attrs = {
  @Attr(name = OMEICSTranslator.SOURCE, value = OMEMetadata.CNAME),
  @Attr(name = OMEICSTranslator.DEST, value = ICSFormat.Metadata.CNAME)
})
public class OMEICSTranslator extends FromOMETranslator<ICSFormat.Metadata> {

  /*
   * @see OMETranslator#typedTranslate(ome.scifio.Metadata, ome.scifio.Metadata)
   */
  @Override
  protected void typedTranslate(OMEMetadata source, ICSFormat.Metadata dest) {
    super.typedTranslate(source, dest);
    
    MetadataRetrieve retrieve = source.getRoot();
    
    Timestamp ts = retrieve.getImageAcquisitionDate(0);
    
    if (ts != null) dest.putDate(ts.getValue());
    
    MetadataOptions options = source.getMetadataOptions();
    
    if (options != null && options.getMetadataLevel() != MetadataLevel.MINIMUM) {
      dest.putDescription(retrieve.getImageDescription(0));
      
      if (retrieve.getInstrumentCount() > 0) {
        dest.putMicroscopeModel(retrieve.getMicroscopeModel(0));
        dest.putMicroscopeManufacturer(retrieve.getMicroscopeManufacturer(0));
        Hashtable<Integer, Integer> laserWaves = new Hashtable<Integer, Integer>();
        
        for (int i=0; i<retrieve.getLightSourceCount(0); i++) {
          laserWaves.put(i, retrieve.getLaserWavelength(0, i).getValue());
        }
        
        dest.putWavelengths(laserWaves);
        dest.putLaserManufacturer(retrieve.getLaserManufacturer(0, 0));
        dest.putLaserModel(retrieve.getLaserModel(0, 0));
        dest.putLaserPower(retrieve.getLaserPower(0, 0));
        dest.putLaserRepetitionRate(retrieve.getLaserRepetitionRate(0, 0));
        
        dest.putFilterSetModel(retrieve.getFilterSetModel(0, 0));
        dest.putDichroicModel(retrieve.getDichroicModel(0, 0));
        dest.putExcitationModel(retrieve.getFilterModel(0, 0));
        dest.putEmissionModel(retrieve.getFilterModel(0, 1));
        
        dest.putObjectiveModel(retrieve.getObjectiveModel(0, 0));
        dest.putImmersion(retrieve.getObjectiveImmersion(0, 0).getValue());
        dest.putLensNA(retrieve.getObjectiveLensNA(0, 0));
        dest.putWorkingDistance(retrieve.getObjectiveWorkingDistance(0, 0));
        dest.putMagnification(retrieve.getObjectiveCalibratedMagnification(0, 0));
        
        dest.putDetectorManufacturer(retrieve.getDetectorManufacturer(0, 0));
        dest.putDetectorModel(retrieve.getDetectorModel(0, 0));
      }
      
      if (retrieve.getExperimentCount() > 0) {
        dest.putExperimentType(retrieve.getExperimentType(0).getValue());
        dest.putAuthorLastName(retrieve.getExperimenterLastName(0));
      }
      
      Double[] pixelSizes = new Double[5];
      String[] units = new String[5];
      
      String order = retrieve.getPixelsDimensionOrder(0).getValue();
      PositiveFloat sizex = retrieve.getPixelsPhysicalSizeX(0),
          sizey = retrieve.getPixelsPhysicalSizeY(0),
          sizez = retrieve.getPixelsPhysicalSizeZ(0);
      Double sizet = retrieve.getPixelsTimeIncrement(0);
      
      for (int i=0; i<order.length(); i++) {
        switch (order.toUpperCase().charAt(i)) {
        case 'X': pixelSizes[i] = sizex == null ? 1.0 : sizex.getValue();
                  units[i] = "um";
          break;
        case 'Y': pixelSizes[i] = sizey == null ? 1.0 : sizey.getValue();
                  units[i] = "um";
          break;
        case 'Z': pixelSizes[i] = sizez == null ? 1.0 : sizez.getValue();
                  units[i] = "um";
          break;
        case 'T': pixelSizes[i] = sizet == null ? 1.0 : sizet;
                  units[i] = "s";
          break;
        case 'C': pixelSizes[i] = 1.0;
                  units[i] = "um";
          break;
        default: pixelSizes[i] = 1.0;
                 units[i] = "um";
        }
      }
      
      dest.putPixelSizes(pixelSizes);
      dest.putUnits(units);
      
      if (retrieve.getPlaneCount(0) > 0) {
        Double[] timestamps = new Double[source.getAxisLength(0, Axes.TIME)];

        for (int t=0; t<timestamps.length; t++) {
          for (int z=0; z<source.getAxisLength(0, Axes.Z); z++) {
            for (int c=0; c<source.getEffectiveSizeC(0); c++) {
              int index = FormatTools.getIndex(FormatTools.findDimensionOrder(source, 0), 
                  source.getAxisLength(0,  Axes.Z), source.getEffectiveSizeC(0),
                  source.getAxisLength(0, Axes.TIME), source.getPlaneCount(0), z, c, t);
              timestamps[t] = retrieve.getPlaneDeltaT(0, index);
            }
          }
        }

        dest.putTimestamps(timestamps);
        dest.putExposureTime(retrieve.getPlaneExposureTime(0, 0));
      }

      Hashtable<Integer, String> channelNames = new Hashtable<Integer, String>();
      Hashtable<Integer, Double> pinholes = new Hashtable<Integer, Double>();
      Hashtable<Integer, Double> gains = new Hashtable<Integer, Double>();
      List<Integer> emWaves = new ArrayList<Integer>();
      List<Integer> exWaves = new ArrayList<Integer>();

      for (int i=0; i<source.getEffectiveSizeC(0); i++) {
        String cName = retrieve.getChannelName(0, i);
        if (cName != null) channelNames.put(i, cName);

        Double pinSize = retrieve.getChannelPinholeSize(0, i);
        if (pinSize != null) pinholes.put(i, pinSize);

        PositiveInteger emWave = retrieve.getChannelEmissionWavelength(0, i);
        if (emWave != null) emWaves.add(emWave.getValue());

        PositiveInteger exWave = retrieve.getChannelExcitationWavelength(0, i);
        if (exWave != null) exWaves.add(exWave.getValue());

        if (retrieve.getInstrumentCount() > 0 && retrieve.getDetectorCount(0) > 0) {
          Double chGain = retrieve.getDetectorSettingsGain(0, i);
          if (chGain != null) gains.put(i, chGain);
        }
      }

      dest.putChannelNames(channelNames);
      dest.putPinholes(pinholes);
      dest.putEXWaves(exWaves.toArray(new Integer[exWaves.size()]));
      dest.putEMWaves(emWaves.toArray(new Integer[emWaves.size()]));
      dest.putGains(gains);
    }
  }
}
