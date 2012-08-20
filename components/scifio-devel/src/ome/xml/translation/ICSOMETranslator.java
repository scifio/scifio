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
import java.util.List;
import java.util.StringTokenizer;

import loci.formats.MetadataTools;
import loci.formats.in.MetadataLevel;
import loci.formats.meta.MetadataStore;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.ics.ICSFormat;
import ome.xml.meta.OMEMetadata;
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

  // -- Construcotrs -- 

  public ICSOMETranslator() {
    this(null);
  }

  public ICSOMETranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API --

  @Override
  public void translate(final ICSFormat.Metadata source, final OMEMetadata destination)
  {
    super.translate(source, destination);

  }

  /*
  
      // TODO OME data translation
     MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this, true);

    // populate Image data

    store.setImageName(imageName, 0);

    if (date != null) store.setImageAcquisitionDate(new Timestamp(date), 0);

    if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
      store.setImageDescription(description, 0);

      // link Instrument and Image
      String instrumentID = MetadataTools.createLSID("Instrument", 0);
      store.setInstrumentID(instrumentID, 0);
      store.setMicroscopeModel(microscopeModel, 0);
      store.setMicroscopeManufacturer(microscopeManufacturer, 0);

      store.setImageInstrumentRef(instrumentID, 0);

      store.setExperimentID(MetadataTools.createLSID("Experiment", 0), 0);
      store.setExperimentType(getExperimentType(experimentType), 0);

      // populate Dimensions data

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
          sizes[1] /= getSizeY();
          if (sizes[1] > 0) {
            store.setPixelsPhysicalSizeY(new PositiveFloat(sizes[1]), 0);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
              sizes[1]);
          }
        }
      }

      // populate Plane data

      if (timestamps != null) {
        for (int t=0; t<timestamps.length; t++) {
          if (t >= getSizeT()) break; // ignore superfluous timestamps
          if (timestamps[t] == null) continue; // ignore missing timestamp
          double deltaT = timestamps[t];
          if (Double.isNaN(deltaT)) continue; // ignore invalid timestamp
          // assign timestamp to all relevant planes
          for (int z=0; z<getSizeZ(); z++) {
            for (int c=0; c<getEffectiveSizeC(); c++) {
              int index = getIndex(z, c, t);
              store.setPlaneDeltaT(deltaT, 0, index);
            }
          }
        }
      }

      // populate LogicalChannel data

      for (int i=0; i<getEffectiveSizeC(); i++) {
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

      Integer[] lasers = wavelengths.keySet().toArray(new Integer[0]);
      Arrays.sort(lasers);
      for (int i=0; i<lasers.length; i++) {
        store.setLaserID(MetadataTools.createLSID("LightSource", 0, i), 0, i);
        if (wavelengths.get(lasers[i]) > 0) {
          store.setLaserWavelength(
            new PositiveInteger(wavelengths.get(lasers[i])), 0, i);
        }
        else {
          LOGGER.warn("Expected positive value for wavelength; got {}",
            wavelengths.get(lasers[i]));
        }
        store.setLaserType(getLaserType("Other"), 0, i);
        store.setLaserLaserMedium(getLaserMedium("Other"), 0, i);

        store.setLaserManufacturer(laserManufacturer, 0, i);
        store.setLaserModel(laserModel, 0, i);
        store.setLaserPower(laserPower, 0, i);
        store.setLaserRepetitionRate(laserRepetitionRate, 0, i);
      }

      if (lasers.length == 0 && laserManufacturer != null) {
        store.setLaserID(MetadataTools.createLSID("LightSource", 0, 0), 0, 0);
        store.setLaserType(getLaserType("Other"), 0, 0);
        store.setLaserLaserMedium(getLaserMedium("Other"), 0, 0);
        store.setLaserManufacturer(laserManufacturer, 0, 0);
        store.setLaserModel(laserModel, 0, 0);
        store.setLaserPower(laserPower, 0, 0);
        store.setLaserRepetitionRate(laserRepetitionRate, 0, 0);
      }

      // populate FilterSet data

      if (filterSetModel != null) {
        store.setFilterSetID(MetadataTools.createLSID("FilterSet", 0, 0), 0, 0);
        store.setFilterSetModel(filterSetModel, 0, 0);

        String dichroicID = MetadataTools.createLSID("Dichroic", 0, 0);
        String emFilterID = MetadataTools.createLSID("Filter", 0, 0);
        String exFilterID = MetadataTools.createLSID("Filter", 0, 1);

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

      if (objectiveModel != null) store.setObjectiveModel(objectiveModel, 0, 0);
      if (immersion == null) immersion = "Other";
      store.setObjectiveImmersion(getImmersion(immersion), 0, 0);
      if (lensNA != null) store.setObjectiveLensNA(lensNA, 0, 0);
      if (workingDistance != null) {
        store.setObjectiveWorkingDistance(workingDistance, 0, 0);
      }
      if (magnification != null) {
        store.setObjectiveCalibratedMagnification(magnification, 0, 0);
      }
      store.setObjectiveCorrection(getCorrection("Other"), 0, 0);

      // link Objective to Image
      String objectiveID = MetadataTools.createLSID("Objective", 0, 0);
      store.setObjectiveID(objectiveID, 0, 0);
      store.setObjectiveSettingsID(objectiveID, 0);

      // populate Detector data

      String detectorID = MetadataTools.createLSID("Detector", 0, 0);
      store.setDetectorID(detectorID, 0, 0);
      store.setDetectorManufacturer(detectorManufacturer, 0, 0);
      store.setDetectorModel(detectorModel, 0, 0);
      store.setDetectorType(getDetectorType("Other"), 0, 0);

      for (Integer key : gains.keySet()) {
        int index = key.intValue();
        if (index < getEffectiveSizeC()) {
          store.setDetectorSettingsGain(gains.get(key), 0, index);
          store.setDetectorSettingsID(detectorID, 0, index);
        }
      }

      // populate Experimenter data

      if (lastName != null) {
        String experimenterID = MetadataTools.createLSID("Experimenter", 0);
        store.setExperimenterID(experimenterID, 0);
        store.setExperimenterLastName(lastName, 0);
      }

      // populate StagePosition data

      if (stagePos != null) {
        for (int i=0; i<getImageCount(); i++) {
          if (stagePos.length > 0) {
            store.setPlanePositionX(stagePos[0], 0, i);
            addGlobalMeta("X position for position #1", stagePos[0]);
          }
          if (stagePos.length > 1) {
            store.setPlanePositionY(stagePos[1], 0, i);
            addGlobalMeta("Y position for position #1", stagePos[1]);
          }
          if (stagePos.length > 2) {
            store.setPlanePositionZ(stagePos[2], 0, i);
            addGlobalMeta("Z position for position #1", stagePos[2]);
          }
        }
      }

      if (exposureTime != null) {
        for (int i=0; i<getImageCount(); i++) {
          store.setPlaneExposureTime(exposureTime, 0, i);
        }
      }
    }
  }
  */
  
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
