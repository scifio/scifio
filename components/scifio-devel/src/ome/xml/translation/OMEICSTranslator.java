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
import ome.scifio.ics.ICSFormat;
import ome.scifio.util.FormatTools;
import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.OMEMetadata;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Translator class from {@link ICSMetadata} to
 * {@link OMEMetadata}.
 * 
 * @author Mark Hiner
 */
@Plugin(type = FromOMETranslator.class, priority = Priority.HIGH_PRIORITY,
attrs = {
  @Attr(name = OMEICSTranslator.SOURCE, value = OMEMetadata.CNAME),
  @Attr(name = OMEICSTranslator.DEST, value = ICSFormat.Metadata.CNAME)
})
public class OMEICSTranslator extends FromOMETranslator<ICSFormat.Metadata> {

  @Override
  protected void translate() {
    super.translate();
    
    MetadataRetrieve retrieve = source.getRoot();
    
    dest.putDate(retrieve.getImageAcquisitionDate(0).getValue());
    
    MetadataOptions options = source.getMetadataOptions();
    
    if (options != null && options.getMetadataLevel() != MetadataLevel.MINIMUM) {
      dest.putDescription(retrieve.getImageDescription(0));
      dest.putMicroscopeModel(retrieve.getMicroscopeModel(0));
      dest.putMicroscopeManufacturer(retrieve.getMicroscopeManufacturer(0));
      dest.putExperimentType(retrieve.getExperimentType(0).getValue());
      
      Double[] pixelSizes = new Double[5];
      String[] axes = new String[5];
      String[] units = new String[5];
      String order = retrieve.getPixelsDimensionOrder(0).getValue();
      
      for (int i=0; i<order.length(); i++) {
        switch (order.toUpperCase().charAt(i)) {
        case 'X': pixelSizes[i] = retrieve.getPixelsPhysicalSizeX(0).getValue();
                  axes[i] = "x";
                  units[i] = "um";
          break;
        case 'Y': pixelSizes[i] = retrieve.getPixelsPhysicalSizeY(0).getValue();
                  axes[i] = "y";
                  units[i] = "um";
          break;
        case 'Z': pixelSizes[i] = retrieve.getPixelsPhysicalSizeZ(0).getValue();
                  axes[i] = "z";
                  units[i] = "um";
          break;
        case 'T': pixelSizes[i] = retrieve.getPixelsTimeIncrement(0);
                  axes[i] = "t";
                  units[i] = "s";
          break;
        case 'C': pixelSizes[i] = 1.0;
                  axes[i] = "ch";
                  units[i] = "um";
          break;
        default: pixelSizes[i] = 1.0;
                 axes[i] = "u";
                 units[i] = "um";
        }
      }
      
      dest.putPixelSizes(pixelSizes);
      dest.putAxes(axes);
      dest.putUnits(units);
      
      Double[] timestamps = new Double[source.getAxisLength(0, Axes.TIME)];
      
      for (int t=0; t<timestamps.length; t++) {
        for (int z=0; z<source.getAxisLength(0, Axes.Z); z++) {
          for (int c=0; c<source.getEffectiveSizeC(0); c++) {
            int index = FormatTools.getIndex(FormatTools.findDimensionOrder(source, 0), 0,
                source.getAxisLength(0,  Axes.Z), source.getEffectiveSizeC(0),
                source.getAxisLength(0, Axes.TIME), z, c, t);
            timestamps[t] = retrieve.getPlaneDeltaT(0, index);
          }
        }
      }
      
      dest.putTimestamps(timestamps);
      
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
        
        Integer emWave = retrieve.getChannelEmissionWavelength(0, i).getValue();
        if (emWave != null) emWaves.add(emWave);
        
        Integer exWave = retrieve.getChannelExcitationWavelength(0, i).getValue();
        if (exWave != null) exWaves.add(exWave);
        
        Double chGain = retrieve.getDetectorSettingsGain(0, i);
        if (chGain != null) gains.put(i, chGain);
      }
      
      dest.putChannelNames(channelNames);
      dest.putPinholes(pinholes);
      dest.putEXWaves(exWaves.toArray(new Integer[exWaves.size()]));
      dest.putEMWaves(emWaves.toArray(new Integer[emWaves.size()]));
      dest.putGains(gains);
      
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
      
      dest.putAuthorLastName(retrieve.getExperimenterLastName(0));
      
      dest.putExposureTime(retrieve.getPlaneExposureTime(0, 0));
    }
  }
}
