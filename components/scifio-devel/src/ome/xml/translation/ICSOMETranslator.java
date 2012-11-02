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

import net.imglib2.meta.Axes;

import ome.scifio.DatasetMetadata;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.MetadataLevel;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
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
  

  // -- Translator API --

  @Override
  public void translate(final ICSFormat.Metadata source, final OMEMetadata destination)
  {
    super.translate(source, destination);

    IMetadata store = new OMEXMLMetadataImpl();
    boolean lifetime = false;
    int imageIndex = 0; //TODO correct index?
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
    
    DatasetMetadata dMeta = new DatasetMetadata();
    Format<?,?,?,?,?> icsFormat = getContext().getFormatFromMetadata(source.getClass());
    
    Translator<ICSFormat.Metadata, DatasetMetadata> trans = null;
    try {
      trans = (Translator<ICSFormat.Metadata, DatasetMetadata>) icsFormat.findSourceTranslator(DatasetMetadata.class);
    } catch (FormatException e) {
      throw new RuntimeException(e);
    }
    
    trans.translate(source, dMeta);
    
    OMEXMLMetadataTools.populatePixels((MetadataStore)filter, dMeta, true);
    
    store.setImageName(imageName, 0);

    
    // populate date data
    
    date = source.getDate();
    
    if (date != null) store.setImageAcquisitionDate(new Timestamp(date), 0);

    // > Minimum Metadata population
    if (source.getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
      
      description = source.getDescription();
      store.setImageDescription(description, 0);

      // link Instrument and Image
      String instrumentID = OMEXMLMetadataTools.createLSID("Instrument", 0);
      store.setInstrumentID(instrumentID, 0);
      
      microscopeModel = source.getMicroscopeModel();
      store.setMicroscopeModel(microscopeModel, 0);
      
      microscopeManufacturer = source.getMicroscopeManufacturer();
      store.setMicroscopeManufacturer(microscopeManufacturer, 0);

      store.setImageInstrumentRef(instrumentID, 0);

      store.setExperimentID(OMEXMLMetadataTools.createLSID("Experiment", 0), 0);
      
      lifetime = source.getLifetime();

      experimentType = source.getExperimentType();
      
      try {
        store.setExperimentType(OMEXMLMetadataTools.getExperimentType(experimentType), 0);
      } catch (FormatException e) {
        LOGGER.debug("Could not set experiment type", e);
      }

      // populate Dimensions data

      pixelSizes = source.getPixelSizes();
      
      units = source.getUnits();
      
      axes = source.getAxes();
      
      sizes = source.getAxesSizes();
      
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
          sizes[1] /= dMeta.getAxisLength(imageIndex, Axes.Y);
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

    timestamps = source.getTimestamps();
    
    if (timestamps != null) {
      for (int t=0; t<timestamps.length; t++) {
        if (t >= dMeta.getAxisLength(imageIndex, Axes.TIME)) break; // ignore superfluous timestamps
        if (timestamps[t] == null) continue; // ignore missing timestamp
        double deltaT = timestamps[t];
        if (Double.isNaN(deltaT)) continue; // ignore invalid timestamp
        // assign timestamp to all relevant planes
        for (int z=0; z<dMeta.getAxisLength(imageIndex, Axes.Z); z++) {
          for (int c=0; c<dMeta.getEffectiveSizeC(imageIndex); c++) {
            int index = FormatTools.getIndex(FormatTools.findDimensionOrder(dMeta, imageIndex), imageIndex,
              dMeta.getAxisLength(imageIndex,  Axes.Z), dMeta.getEffectiveSizeC(imageIndex),
              dMeta.getAxisLength(imageIndex, Axes.TIME), z, c, t);
            store.setPlaneDeltaT(deltaT, 0, index);
          }
        }
      }
    }
    
    // populate LogicalChannel data

    channelNames = source.getChannelNames();
    source.addStepChannel(channelNames);
    source.addCubeChannel(channelNames);
    
    pinholes = source.getPinholes();
    
    emWaves = source.getEMWaves();
    
    if(emWaves == null) {
      emWaves = source.getEMSingleton();
    }
    
    exWaves = source.getEXWaves();
    
    if(exWaves == null) {
      exWaves = source.getEXSingleton();
    }
    
    for (int i=0; i<dMeta.getEffectiveSizeC(imageIndex); i++) {
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
    
    wavelengths = source.getWavelengths();
    source.addLaserWavelength(wavelengths);
    
    laserManufacturer = source.getLaserManufacturer();
    
    laserModel = source.getLaserModel();
    
    laserPower = source.getLaserPower();
    
    laserRepetitionRate = source.getLaserRepetitionRate();

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
    
    filterSetModel = source.getFilterSetModel();
    
    dichroicModel = source.getDichroicModel();
    
    excitationModel = source.getExcitationModel();
    
    emissionModel = source.getEmissionModel();

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

    objectiveModel = source.getObjectiveModel();
    
    immersion = source.getImmersion();
    
    lensNA = source.getLensNA();
    
    workingDistance = source.getWorkingDistance();
    
    magnification = source.getMagnification();
    
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

    detectorManufacturer = source.getDetectorManufacturer();
    
    detectorModel = source.getDetectorModel();
    
    String detectorID = OMEXMLMetadataTools.createLSID("Detector", 0, 0);
    store.setDetectorID(detectorID, 0, 0);
    store.setDetectorManufacturer(detectorManufacturer, 0, 0);
    store.setDetectorModel(detectorModel, 0, 0);
    try {
      store.setDetectorType(OMEXMLMetadataTools.getDetectorType("Other"), 0, 0);
    } catch (FormatException e) {
      LOGGER.warn("Failed to store detector type", e);
    }
    
    gains = source.getGains();

    for (Integer key : gains.keySet()) {
      int index = key.intValue();
      if (index < dMeta.getEffectiveSizeC(imageIndex)) {
        store.setDetectorSettingsGain(gains.get(key), 0, index);
        store.setDetectorSettingsID(detectorID, 0, index);
      }
    }
    
    // populate Experimenter data

    lastName = source.getAuthorLastName();
    
    if (lastName != null) {
      String experimenterID = OMEXMLMetadataTools.createLSID("Experimenter", 0);
      store.setExperimenterID(experimenterID, 0);
      store.setExperimenterLastName(lastName, 0);
    }
    
    // populate StagePosition data
    
    stagePos = source.getStagePositions();
    
    if(stagePos == null) {
      stagePos = new Double[3];
    }
    
    stagePos[0] = source.getStageX();

    stagePos[1] = source.getStageY();

    stagePos[2] = source.getStageZ();
    
    //TODO set global meta - x, y, z positions?

    exposureTime = source.getExposureTime();
    
    if (exposureTime != null) {
      for (int i=0; i<dMeta.getImageCount(); i++) {
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
