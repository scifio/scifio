/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import net.imglib2.meta.Axes;

import ome.scifio.CoreMetadata;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.MetadataLevel;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.common.DateTools;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.ics.ICSFormat;
import ome.xml.meta.FilterMetadata;
import ome.xml.meta.IMetadata;
import ome.xml.meta.MetadataStore;
import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadataTools;
import ome.scifio.util.FormatTools;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;

/**
 * Translator class from {@link ICSMetadata} to
 * {@link OMEMetadata}.
 * 
 * @author hinerm
 */
@SCIFIOTranslator(metaIn = ICSFormat.Metadata.class, metaOut = OMEMetadata.class)
public class ICSOMETranslator extends OMETranslator<ICSFormat.Metadata> {

  // -- Constructors -- 

  public ICSOMETranslator() {
    this(null);
  }

  public ICSOMETranslator(final SCIFIO ctx) {
    super(ctx);
  }
  
  // -- Fields --
  
  private Hashtable<String, String> keyPairs = null;

  // -- Translator API --

  @Override
  public void translate(final ICSFormat.Metadata source, final OMEMetadata destination)
  {
    super.translate(source, destination);

    IMetadata store = new OMEXMLMetadataImpl();
    keyPairs = source.getKeyValPairs();
    boolean lifetime = false;
    int imageIndex = 0; //TODO correct index?
    String[] kv = null;
    Double[] pixelSizes = null, timestamps = null, stagePos = null;
    Double laserPower = null, laserRepetitionRate = null, lensNA = null, workingDistance = null,
      magnification = null, exposureTime = null;
    Integer[] emWaves = null, exWaves = null;
    double[] sizes = null;
    String[] units = null, axes = null;
    String imageName = source.getSource().getFileName();
    String date = null, description = null, microscopeModel = null, microscopeManufacturer = null,
      experimentType = null, laserManufacturer = null, laserModel = null, filterSetModel = null,
      dichroicModel = null, excitationModel = null, emissionModel = null, objectiveModel = null,
      immersion = null, detectorManufacturer = null, detectorModel = null, lastName = null;
    
    Hashtable<Integer, Double> gains = new Hashtable<Integer, Double>();
    Hashtable<Integer, String> channelNames = new Hashtable<Integer, String>();
    Hashtable<Integer, Double> pinholes = new Hashtable<Integer, Double>();
    Hashtable<Integer, Integer> wavelengths = new Hashtable<Integer, Integer>();
    
    FilterMetadata filter = new FilterMetadata(store, source.isFiltered());
    filter.createRoot();
    
    CoreMetadata cMeta = new CoreMetadata();
    Format<?,?,?,?,?> icsFormat = getContext().getFormatFromMetadata(source.getClass());
    
    Translator<ICSFormat.Metadata, CoreMetadata> trans = null;
    try {
      trans = (Translator<ICSFormat.Metadata, CoreMetadata>) icsFormat.findSourceTranslator(CoreMetadata.class);
    } catch (FormatException e) {
      throw new RuntimeException(e);
    }
    
    trans.translate(source, cMeta);
    
    OMEXMLMetadataTools.populatePixels((MetadataStore)filter, cMeta, true);
    
    store.setImageName(imageName, 0);

    
    // populate date data
    
    kv = findValueForKey("history date", "history created on", "history creation date");
    if (kv[0].equalsIgnoreCase("history date") ||
      kv[0].equalsIgnoreCase("history created on"))
    {
      if (kv[1].indexOf(" ") > 0) {
        date = kv[1].substring(0, kv[1].lastIndexOf(" "));
        date = DateTools.formatDate(date, ICSFormat.ICSUtils.DATE_FORMATS);
      }
    }
    else if (kv[0].equalsIgnoreCase("history creation date")) {
      date = DateTools.formatDate(kv[1], ICSFormat.ICSUtils.DATE_FORMATS);
    }
    
    if (date != null) store.setImageAcquisitionDate(new Timestamp(date), 0);

    // > Minimum Metadata population
    if (source.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
      
      kv = findValueForKey("history other text");
      description = kv[1];
      store.setImageDescription(description, 0);

      // link Instrument and Image
      String instrumentID = OMEXMLMetadataTools.createLSID("Instrument", 0);
      store.setInstrumentID(instrumentID, 0);
      
      kv = findValueForKey("history microscope");
      microscopeModel = kv[1];
      store.setMicroscopeModel(microscopeModel, 0);
      
      kv = findValueForKey("history manufacturer");
      microscopeManufacturer = kv[1];
      store.setMicroscopeManufacturer(microscopeManufacturer, 0);

      store.setImageInstrumentRef(instrumentID, 0);

      store.setExperimentID(OMEXMLMetadataTools.createLSID("Experiment", 0), 0);
      
      kv = findValueForKey("history type");
      if (kv[0].equalsIgnoreCase("time resolved") ||
        kv[0].equalsIgnoreCase("FluorescenceLifetime"))
      {
        lifetime = true;
      }
      experimentType = kv[1];
      
      try {
        store.setExperimentType(OMEXMLMetadataTools.getExperimentType(experimentType), 0);
      } catch (FormatException e) {
        LOGGER.debug("Could not set experiment type", e);
      }

      // populate Dimensions data

      kv = findValueForKey("parameter scale");
      if(kv[1] != null) pixelSizes = splitDoubles(kv[1]);
      
      kv = findValueForKey("parameter units");
      if(kv[1] != null) units = kv[1].split("\\s+");
      
      kv = findValueForKey("layout order");
      if (kv[1] != null) {
        StringTokenizer t = new StringTokenizer(kv[1]);
        axes = new String[t.countTokens()];
        for(int n = 0; n < axes.length; n++) {
          axes[n] = t.nextToken().trim();
        }
      }
      
      kv = findValueForKey("history extents");
      if(kv[1] != null) {
        String[] lengths = kv[1].split(" ");
        sizes = new double[lengths.length];
        for(int n = 0; n < sizes.length; n++) {
          try { 
            sizes[n] = Double.parseDouble(lengths[n].trim());
          } catch (NumberFormatException e) {
            LOGGER.debug("Could not parse axis length", e);
          }
        }
      }
      
      if (pixelSizes != null) {
        if (units != null && units.length == pixelSizes.length - 1) {
          // correct for missing units
          // sometimes, the units for the C axis are missing entirely
          ArrayList<String> realUnits = new ArrayList<String>();
          int unitIndex = 0;
          for (int i=0; i<axes.length; i++) {
            if (axes[i].toLowerCase().equals("ch")) {
              realUnits.add("nm");
            }
            else {
              realUnits.add(units[unitIndex++]);
            }
          }
          units = realUnits.toArray(new String[realUnits.size()]);
        }

        for (int i=0; i<pixelSizes.length; i++) {
          Double pixelSize = pixelSizes[i];
          String axis = axes != null && axes.length > i ? axes[i] : "";
          String unit = units != null && units.length > i ? units[i] : "";
          if (axis.equals("x")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeX(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("y")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeY(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("z")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeZ(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeZ; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("t")) {
            if (checkUnit(unit, "ms")) {
              store.setPixelsTimeIncrement(1000 * pixelSize, 0);
            }
            else if (checkUnit(unit, "seconds") || checkUnit(unit, "s")) {
              store.setPixelsTimeIncrement(pixelSize, 0);
            }
          }
        }
      }
      else if (sizes != null) {
        if (sizes.length > 0 && sizes[0] > 0) {
          store.setPixelsPhysicalSizeX(new PositiveFloat(sizes[0]), 0);
        }
        else {
          LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
            sizes[0]);
        }
        if (sizes.length > 1) {
          sizes[1] /= cMeta.getAxisLength(imageIndex, Axes.Y);
          if (sizes[1] > 0) {
            store.setPixelsPhysicalSizeY(new PositiveFloat(sizes[1]), 0);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
              sizes[1]);
          }
        }
      }
    }
    
    // populate Plane data

    kv = findValueForKey("parameter t");
    if(kv[1] != null) 
      timestamps = splitDoubles(kv[1]);
    
    if (timestamps != null) {
      for (int t=0; t<timestamps.length; t++) {
        if (t >= cMeta.getAxisLength(imageIndex, Axes.TIME)) break; // ignore superfluous timestamps
        if (timestamps[t] == null) continue; // ignore missing timestamp
        double deltaT = timestamps[t];
        if (Double.isNaN(deltaT)) continue; // ignore invalid timestamp
        // assign timestamp to all relevant planes
        for (int z=0; z<cMeta.getAxisLength(imageIndex, Axes.Z); z++) {
          for (int c=0; c<cMeta.getEffectiveSizeC(imageIndex); c++) {
            int index = FormatTools.getIndex(FormatTools.findDimensionOrder(cMeta, imageIndex), imageIndex,
              cMeta.getAxisLength(imageIndex,  Axes.Z), cMeta.getEffectiveSizeC(imageIndex),
              cMeta.getAxisLength(imageIndex, Axes.TIME), z, c, t);
            store.setPlaneDeltaT(deltaT, 0, index);
          }
        }
      }
    }
    
    // populate LogicalChannel data

    kv = findValueForKey("parameter ch");
    if(kv[1] != null) {
      String[] names = kv[1].split(" ");
      for (int n = 0; n < names.length; n++) {
        channelNames.put(new Integer(n), names[n].trim());
      }
    }
    
    kv = findValueIteration("history step", "name");
    if(kv[1] != null)
      channelNames.put(new Integer(kv[0].substring(12, kv[0].indexOf(" ", 12))), kv[1]);
      
    kv = findValueForKey("history cube");
    if(kv[1] != null)
      channelNames.put(new Integer(channelNames.size()), kv[1]);
    
    kv = findValueForKey("sensor s_params PinholeRadius");
    if(kv[1] != null) {
      String pins[] = kv[1].split(" ");
      int channel = 0;
      for(int n = 0; n < pins.length; n++) {
        if (pins[n].trim().equals("")) continue;
        try {
          pinholes.put(new Integer(channel++), new Double(pins[n]));
        } catch (NumberFormatException e) {
          LOGGER.debug("Could not parse pinhole", e);
        }
      }
    }
    
    //TODO should this really potentially get overwritten?
    kv = findValueForKey("sensor s_params LambdaEm");
    if(kv[1] != null) {
      String[] waves = kv[1].split(" ");
      emWaves = new Integer[waves.length];
      for (int n = 0; n < emWaves.length; n++) {
        try {
          emWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
        } catch (NumberFormatException e) {
          LOGGER.debug("Could not parse emission wavelength", e);
        }
      }
    }
    
    kv = findValueForKey("history cube emm nm");
    if(kv[1] != null) {
      if(emWaves == null) emWaves = new Integer[1];
      emWaves[0] = new Integer(kv[1].split(" ")[1].trim());
    }
    
    kv = findValueForKey("sensor s_params LambdaEx");
    if (kv[1] != null) {
      String[] waves = kv[1].split(" ");
      exWaves = new Integer[waves.length];
      for (int n = 0; n < exWaves.length; n++) {
        try {
          exWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
        } catch (NumberFormatException e) {
          LOGGER.debug("Could not parse excitation wavelength", e);
        }
      }
    }
    
    kv = findValueForKey("history cube exc nm");
    if(kv[1] != null) {
      if(exWaves == null) exWaves = new Integer[1];
      exWaves[0] = new Integer(kv[1].split(" ")[1].trim());
    }
    
    for (int i=0; i<cMeta.getEffectiveSizeC(imageIndex); i++) {
      if (channelNames.containsKey(i)) {
        store.setChannelName(channelNames.get(i), 0, i);
      }
      if (pinholes.containsKey(i)) {
        store.setChannelPinholeSize(pinholes.get(i), 0, i);
      }
      if (emWaves != null && i < emWaves.length) {
        if (emWaves[i].intValue() > 0) {
          store.setChannelEmissionWavelength(
            new PositiveInteger(emWaves[i]), 0, i);
        }
        else {
          LOGGER.warn(
            "Expected positive value for EmissionWavelength; got {}",
            emWaves[i]);
        }
      }
      if (exWaves != null && i < exWaves.length) {
        if (exWaves[i].intValue() > 0) {
          store.setChannelExcitationWavelength(
            new PositiveInteger(exWaves[i]), 0, i);
        }
        else {
          LOGGER.warn(
            "Expected positive value for ExcitationWavelength; got {}",
            exWaves[i]);
        }
      }
    }

    // populate Laser data
    
    kv = findValueIteration("history laser", "wavelength");
    if(kv[1] != null) {
      int laser = Integer.parseInt(kv[0].substring(13, kv[0].indexOf(" ", 13))) - 1;
      kv[1] = kv[1].replaceAll("nm", "").trim();
      try {
        wavelengths.put(new Integer(laser), new Integer(kv[1]));
      } catch (NumberFormatException e) {
        LOGGER.debug("Could not parse wavelength", e);
      }
    }
    
    kv = findValueForKey("history Wavelength*");
    if(kv[1] != null) {
      String[] waves = kv[1].split(" ");
      for(int n = 0; n < waves.length; n++) {
        wavelengths.put(new Integer(n), new Integer(waves[n]));
      }
    }
    
    kv = findValueForKey("history laser manufacturer");
    if(kv[1] != null) {
      laserManufacturer = kv[1];
    }
    
    kv = findValueForKey("history laser model");
    if(kv[1] != null) {
      laserModel = kv[1];
    }
    
    kv = findValueForKey("history laser power");
    if(kv[1] != null) {
      laserPower = new Double(kv[1]);
    }
    
    kv = findValueForKey("history laser rep rate");
    if(kv[1] != null) {
      laserRepetitionRate = new Double(kv[1]);
    }

    Integer[] lasers = wavelengths.keySet().toArray(new Integer[0]);
    Arrays.sort(lasers);
    for (int i=0; i<lasers.length; i++) {
      store.setLaserID(OMEXMLMetadataTools.createLSID("LightSource", 0, i), 0, i);
      if (wavelengths.get(lasers[i]) > 0) {
        store.setLaserWavelength(
          new PositiveInteger(wavelengths.get(lasers[i])), 0, i);
      }
      else {
        LOGGER.warn("Expected positive value for wavelength; got {}",
          wavelengths.get(lasers[i]));
      }

      try {
        store.setLaserType(OMEXMLMetadataTools.getLaserType("Other"), 0, i);
      } catch (FormatException e) {
        LOGGER.warn("Failed to set laser type", e);
      }

      try {
        store.setLaserLaserMedium(OMEXMLMetadataTools.getLaserMedium("Other"), 0, i);
      } catch (FormatException e) {
        LOGGER.warn("Failed to set laser medium", e);
      }

      store.setLaserManufacturer(laserManufacturer, 0, i);
      store.setLaserModel(laserModel, 0, i);
      store.setLaserPower(laserPower, 0, i);
      store.setLaserRepetitionRate(laserRepetitionRate, 0, i);
    }

    if (lasers.length == 0 && laserManufacturer != null) {
      store.setLaserID(OMEXMLMetadataTools.createLSID("LightSource", 0, 0), 0, 0);

      try {
        store.setLaserType(OMEXMLMetadataTools.getLaserType("Other"), 0, 0);
      } catch (FormatException e) {
        LOGGER.warn("Failed to set laser type", e);
      }
      try {
        store.setLaserLaserMedium(OMEXMLMetadataTools.getLaserMedium("Other"), 0, 0);
      } catch (FormatException e) {
        LOGGER.warn("Failed to set laser medium", e);
      }
      store.setLaserManufacturer(laserManufacturer, 0, 0);
      store.setLaserModel(laserModel, 0, 0);
      store.setLaserPower(laserPower, 0, 0);
      store.setLaserRepetitionRate(laserRepetitionRate, 0, 0);
    }

    // populate FilterSet data
    
    kv = findValueForKey("history filterset");
    if(kv[1] != null) {
      filterSetModel = kv[1];
    }
    
    kv = findValueForKey("history filterset dichroic name");
    if(kv[1] != null) {
      dichroicModel = kv[1];
    }
    
    kv = findValueForKey("history filterset exc name");
    if(kv[1] != null) {
      excitationModel = kv[1];
    }
    
    kv = findValueForKey("history filterset emm name");
    if(kv[1] != null) {
      emissionModel = kv[1];
    }

    if (filterSetModel != null) {
      store.setFilterSetID(OMEXMLMetadataTools.createLSID("FilterSet", 0, 0), 0, 0);
      store.setFilterSetModel(filterSetModel, 0, 0);

      String dichroicID = OMEXMLMetadataTools.createLSID("Dichroic", 0, 0);
      String emFilterID = OMEXMLMetadataTools.createLSID("Filter", 0, 0);
      String exFilterID = OMEXMLMetadataTools.createLSID("Filter", 0, 1);

      store.setDichroicID(dichroicID, 0, 0);
      store.setDichroicModel(dichroicModel, 0, 0);
      store.setFilterSetDichroicRef(dichroicID, 0, 0);

      store.setFilterID(emFilterID, 0, 0);
      store.setFilterModel(emissionModel, 0, 0);
      store.setFilterSetEmissionFilterRef(emFilterID, 0, 0, 0);

      store.setFilterID(exFilterID, 0, 1);
      store.setFilterModel(excitationModel, 0, 1);
      store.setFilterSetExcitationFilterRef(exFilterID, 0, 0, 0);
    }
    
    // populate Objective data

    kv = findValueForKey("history objective type", "history objective");
    if(kv[1] != null) {
      objectiveModel = kv[1];
    }
    
    kv = findValueForKey("history objective immersion");
    if(kv[1] != null) {
      immersion = kv[1];
    }
    
    kv = findValueForKey("history objective NA");
    if(kv[1] != null) {
      //TODO try/catch all double creation... in one spot!?
      lensNA = new Double(kv[1]);
    }
    
    kv = findValueForKey("history objective WorkingDistance");
    if(kv[1] != null) {
      workingDistance = new Double(kv[1]);
    }
    
    kv = findValueForKey("history objective magnification", "history objective mag");
    if(kv[1] != null) {
      magnification = new Double(kv[1]);
    }
    
    if (objectiveModel != null) store.setObjectiveModel(objectiveModel, 0, 0);
    if (immersion == null) immersion = "Other";
    try {
      store.setObjectiveImmersion(OMEXMLMetadataTools.getImmersion(immersion), 0, 0);
    } catch (FormatException e) {
     LOGGER.warn("failed to set objective immersion", e);
    }
    if (lensNA != null) store.setObjectiveLensNA(lensNA, 0, 0);
    if (workingDistance != null) {
      store.setObjectiveWorkingDistance(workingDistance, 0, 0);
    }
    if (magnification != null) {
      store.setObjectiveCalibratedMagnification(magnification, 0, 0);
    }
    try {
      store.setObjectiveCorrection(OMEXMLMetadataTools.getCorrection("Other"), 0, 0);
    } catch (FormatException e) {
      LOGGER.warn("Failed to store objective correction", e);
    }

    // link Objective to Image
    String objectiveID = OMEXMLMetadataTools.createLSID("Objective", 0, 0);
    store.setObjectiveID(objectiveID, 0, 0);
    store.setObjectiveSettingsID(objectiveID, 0);
    
    // populate Detector data

    kv = findValueForKey("history camera manufacturer");
    if(kv[1] != null) {
      detectorManufacturer = kv[1];
    }
    
    kv = findValueForKey("history camera model");
    if(kv[1] != null) {
      detectorModel = kv[1];
    }
    
    String detectorID = OMEXMLMetadataTools.createLSID("Detector", 0, 0);
    store.setDetectorID(detectorID, 0, 0);
    store.setDetectorManufacturer(detectorManufacturer, 0, 0);
    store.setDetectorModel(detectorModel, 0, 0);
    try {
      store.setDetectorType(OMEXMLMetadataTools.getDetectorType("Other"), 0, 0);
    } catch (FormatException e) {
      LOGGER.warn("Failed to store detector type", e);
    }
    
    kv = findValueForKey("history gain");
    if(kv[1] != null) {
      Integer n = new Integer(0);
      try{ 
        n = new Integer(kv[0].substring(12).trim());
        n = new Integer(n.intValue() - 1);
      }
      catch (NumberFormatException e) { }
      gains.put(n, new Double(kv[1]));
    }

    for (Integer key : gains.keySet()) {
      int index = key.intValue();
      if (index < cMeta.getEffectiveSizeC(imageIndex)) {
        store.setDetectorSettingsGain(gains.get(key), 0, index);
        store.setDetectorSettingsID(detectorID, 0, index);
      }
    }
    
    // populate Experimenter data

    kv = findValueForKey("history author", "history experimenter");
    if(kv[1] != null) {
      lastName = kv[1];
    }
    
    if (lastName != null) {
      String experimenterID = OMEXMLMetadataTools.createLSID("Experimenter", 0);
      store.setExperimenterID(experimenterID, 0);
      store.setExperimenterLastName(lastName, 0);
    }
    
    // populate StagePosition data
    
    kv = findValueForKey("history stage_xyzum");
    if(kv[1] != null) {
      String[] positions = kv[1].split(" ");
      stagePos = new Double[positions.length];
      for(int n = 0; n < stagePos.length; n++) {
        try {
          stagePos[n] = new Double(positions[n]);
        } catch (NumberFormatException e) {
          LOGGER.debug("Could not parse stage position", e);
        }
      }
    }
    
    if(stagePos == null) {
      stagePos = new Double[3];
    }
    
    kv = findValueForKey("history stage positionx");
    if(kv[1] != null) {
      stagePos[0] = new Double(kv[1]);
    }
    
    kv = findValueForKey("history stage positiony");
    if(kv[1] != null) {
      stagePos[1] = new Double(kv[1]);
    }
    
    kv = findValueForKey("history stage positionz");
    if(kv[1] != null) {
      stagePos[2] = new Double(kv[1]);
    }
    
    //TODO set global meta - x, y, z positions?

    kv = findValueForKey("history Exposure");
    if (kv[1] != null) {
      String expTime = kv[1];
      if(expTime.indexOf(" ") != -1) {
        exposureTime = new Double(expTime.indexOf(" "));
      }
    }
    
    if (exposureTime != null) {
      for (int i=0; i<cMeta.getImageCount(); i++) {
        store.setPlaneExposureTime(exposureTime, 0, i);
      }
    }
    
    destination.setRoot(store);
  }
  
  // -- Helper methods --

  /*
   * String tokenizer for parsing metadata. Splits on any white-space
   * characters. Tabs and spaces are often used interchangeably in real-life ICS
   * files.
   *
   * Also splits on 0x04 character which appears in "paul/csarseven.ics" and
   * "paul/gci/time resolved_1.ics".
   *
   * Also respects double quote marks, so that
   *   Modules("Confocal C1 Grabber").BarrierFilter(2)
   * is just one token.
   *
   * If not for the last requirement, the one line
   *   String[] tokens = line.split("[\\s\\x04]+");
   * would work.
   */
  private String[] tokenize(String line) {
    List<String> tokens = new ArrayList<String>();
    boolean inWhiteSpace = true;
    boolean withinQuotes = false;
    StringBuffer token = null;
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c) || c == 0x04) {
        if (withinQuotes) {
          // retain white space within quotes
          token.append(c);
        }
        else if (!inWhiteSpace) {
          // put out pending token string
          inWhiteSpace = true;
          if (token.length() > 0) {
            tokens.add(token.toString());
            token = null;
          }
        }
      }
      else {
        if ('"' == c) {
          // toggle quotes
          withinQuotes = !withinQuotes;
        }
        if (inWhiteSpace) {
          inWhiteSpace = false;
          // start a new token string
          token = new StringBuffer();
        }
        // build token string
        token.append(c);
      }
    }
    // put out any pending token strings
    if (null != token && token.length() > 0) {
      tokens.add(token.toString());
    }
    return tokens.toArray(new String[0]);
  }

  /* Given a list of tokens and an array of lists of regular expressions, tries
   * to find a match.  If no match is found, looks in OTHER_KEYS.
   */
  String[] findKeyValue(String[] tokens, String[][] regexesArray) {
    String[] keyValue = findKeyValueForCategory(tokens, regexesArray);
    if (null == keyValue) {
      keyValue = findKeyValueOther(tokens, ICSFormat.ICSUtils.OTHER_KEYS);
    }
    if (null == keyValue) {
      String key = tokens[0];
      String value = concatenateTokens(tokens, 1, tokens.length);
      keyValue = new String[] { key, value };
    }
    return keyValue;
  }

  /*
   * Builds a string from a list of tokens.
   */
  private String concatenateTokens(String[] tokens, int start, int stop) {
    StringBuffer returnValue = new StringBuffer();
    for (int i = start; i < tokens.length && i < stop; ++i) {
      returnValue.append(tokens[i]);
      if (i < stop - 1) {
        returnValue.append(' ');
      }
    }
    return returnValue.toString();
  }
  
  /*
   * Checks the list of keys for non-null values in the global hashtable.
   * 
   * If a non-null value is found, it is returned.
   * 
   * The returned array includes the matching key first, and the value second.
   * 
   */
  private String[] findValueForKey (String... keys) {

    for(String key : keys) {
      String value = keyPairs.get(key);
      if(value != null) return new String[]{key, value};
    }
    
    return new String[]{null, null};
  }
  
  /*
   * Iterates through the key set, looking for a key that starts
   * and/or ends with the provided partial keys.
   * 
   * Returns an array containing the first matching key and its 
   * corresponding value if found, and an empty array otherwise.
   * 
   */
  private String[] findValueIteration (String starts, String ends) {
    
    for (String key : keyPairs.keySet()) {
      if ((starts == null || key.startsWith(starts)) && (ends == null || key.endsWith(ends)))
        return new String[]{key, keyPairs.get(key)};
    }
    
    return new String[]{null, null};
  }

  /*
   * Given a list of tokens and an array of lists of regular expressions, finds
   * a match.  Returns key/value pair if matched, null otherwise.
   *
   * The first element, tokens[0], has already been matched to a category, i.e.
   * 'history', and the regexesArray is category-specific.
   */
  private String[] findKeyValueForCategory(String[] tokens,
                                           String[][] regexesArray) {
    String[] keyValue = null;
    int index = 0;
    for (String[] regexes : regexesArray) {
      if (compareTokens(tokens, 1, regexes, 0)) {
        int splitIndex = 1 + regexes.length; // add one for the category
        String key = concatenateTokens(tokens, 0, splitIndex);
        String value = concatenateTokens(tokens, splitIndex, tokens.length);
        keyValue = new String[] { key, value };
        break;
      }
      ++index;
    }
    return keyValue;
  }

  /* Given a list of tokens and an array of lists of regular expressions, finds
   * a match.  Returns key/value pair if matched, null otherwise.
   *
   * The first element, tokens[0], represents a category and is skipped.  Look
   * for a match of a list of regular expressions anywhere in the list of tokens.
   */
  private String[] findKeyValueOther(String[] tokens, String[][] regexesArray) {
    String[] keyValue = null;
    for (String[] regexes : regexesArray) {
      for (int i = 1; i < tokens.length - regexes.length; ++i) {
        // does token match first regex?
        if (tokens[i].toLowerCase().matches(regexes[0])) {
          // do remaining tokens match remaining regexes?
          if (1 == regexes.length || compareTokens(tokens, i + 1, regexes, 1)) {
            // if so, return key/value
            int splitIndex = i + regexes.length;
            String key = concatenateTokens(tokens, 0, splitIndex);
            String value = concatenateTokens(tokens, splitIndex, tokens.length);
            keyValue = new String[] { key, value };
            break;
          }
        }
      }
      if (null != keyValue) {
        break;
      }
    }
    return keyValue;
  }

  /*
   * Compares a list of tokens with a list of regular expressions.
   */
  private boolean compareTokens(String[] tokens, int tokenIndex,
                                String[] regexes, int regexesIndex) {
    boolean returnValue = true;
    int i, j;
    for (i = tokenIndex, j = regexesIndex; j < regexes.length; ++i, ++j) {
      if (i >= tokens.length || !tokens[i].toLowerCase().matches(regexes[j])) {
        returnValue = false;
        break;
      }
    }
    return returnValue;
  }

  /** Splits the given string into a list of {@link Double}s. */
  private Double[] splitDoubles(String v) {
    StringTokenizer t = new StringTokenizer(v);
    Double[] values = new Double[t.countTokens()];
    for (int n=0; n<values.length; n++) {
      String token = t.nextToken().trim();
      try {
        values[n] = new Double(token);
      }
      catch (NumberFormatException e) {
        LOGGER.debug("Could not parse double value '{}'", token, e);
      }
    }
    return values;
  }

  /** Verifies that a unit matches the expected value. */
  private boolean checkUnit(String actual, String... expected) {
    if (actual == null || actual.equals("")) return true; // undefined is OK
    for (String exp : expected) {
      if (actual.equals(exp)) return true; // unit matches expected value
    }
    LOGGER.debug("Unexpected unit '{}'; expected '{}'", actual, expected);
    return false;
  }
}
