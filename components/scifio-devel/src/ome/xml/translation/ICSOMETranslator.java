package ome.xml.translation;

import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.ics.ICSFormat;
import ome.xml.meta.OMEMetadata;

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
    LOGGER.info("Populating OME metadata");

    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this, true);

    // populate Image data

    store.setImageName(imageName, 0);

    if (date != null) store.setImageAcquiredDate(date, 0);
    else MetadataTools.setDefaultCreationDate(store, id, 0);

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
        for (int i=0; i<pixelSizes.length; i++) {
          Double pixelSize = pixelSizes[i];
          String axis = axes != null && axes.length > i ? axes[i] : "";
          String unit = units != null && units.length > i ? units[i] : "";
          if (axis.equals("x")) {
            if (pixelSize > 0 && checkUnit(unit, "um", "microns")) {
              store.setPixelsPhysicalSizeX(new PositiveFloat(pixelSize), 0);
            }
          }
          else if (axis.equals("y")) {
            if (pixelSize > 0 && checkUnit(unit, "um", "microns")) {
              store.setPixelsPhysicalSizeY(new PositiveFloat(pixelSize), 0);
            }
          }
          else if (axis.equals("z")) {
            if (pixelSize > 0 && checkUnit(unit, "um", "microns")) {
              store.setPixelsPhysicalSizeZ(new PositiveFloat(pixelSize), 0);
            }
          }
          else if (axis.equals("t")) {
            if (checkUnit(unit, "ms")) {
              store.setPixelsTimeIncrement(1000 * pixelSize, 0);
            }
          }
        }
      }
      else if (sizes != null) {
        if (sizes.length > 0 && sizes[0] > 0) {
          store.setPixelsPhysicalSizeX(new PositiveFloat(sizes[0]), 0);
        }
        if (sizes.length > 1) {
          sizes[1] /= getSizeY();
          if (sizes[1] > 0) {
            store.setPixelsPhysicalSizeY(new PositiveFloat(sizes[1]), 0);
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
        if (emWaves != null && i < emWaves.length && emWaves[i].intValue() > 0)
        {
          store.setChannelEmissionWavelength(
            new PositiveInteger(emWaves[i]), 0, i);
        }
        if (exWaves != null && i < exWaves.length && exWaves[i].intValue() > 0)
        {
          store.setChannelExcitationWavelength(
            new PositiveInteger(exWaves[i]), 0, i);
        }
      }

      // populate Laser data

      Integer[] lasers = wavelengths.keySet().toArray(new Integer[0]);
      Arrays.sort(lasers);
      for (int i=0; i<lasers.length; i++) {
        store.setLaserID(MetadataTools.createLSID("LightSource", 0, i), 0, i);
        store.setLaserWavelength(
          new PositiveInteger(wavelengths.get(lasers[i])), 0, i);
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
      store.setImageObjectiveSettingsID(objectiveID, 0);

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
        store.setExperimenterDisplayName(lastName, 0);
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
  */

  /*
  private void processLine(String line) {

    // split the line into tokens
    String[] tokens = tokenize(line);

    String token0 = tokens[0].toLowerCase();
    String[] keyValue = null;

    // version category
    if (token0.equals("ics_version")) {
      String value = concatenateTokens(tokens, 1, tokens.length);
      addGlobalMeta(token0, value);
      metadata.icsVersion = value;
      //TODO ARG TEST
      //trackKeyValues(id, token0, value, "");
    }
    // filename category
    else if (token0.equals("filename")) {
      String imageName = concatenateTokens(tokens, 1, tokens.length);
      addGlobalMeta(token0, imageName);
      metadata.filename = imageName;
      //TODO ARG TEST
      //trackKeyValues(id, token0, imageName, "");
    }
    // layout category
    else if (token0.equals("layout")) {
      keyValue = findKeyValue(tokens, LAYOUT_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");

      if (key.equalsIgnoreCase("layout sizes")) {
        StringTokenizer t = new StringTokenizer(value);
        metadata.layoutSizes = new int[t.countTokens()];
        for (int n=0; n<metadata.layoutSizes.length; n++) {
          try {
            metadata.layoutSizes[n] = Integer.parseInt(t.nextToken().trim());
          }
          catch (NumberFormatException e) {
            LOGGER.debug("Could not parse axis length", e);
          }
        }
      }
      else if (key.equalsIgnoreCase("layout order")) {
        StringTokenizer t = new StringTokenizer(value);
        metadata.layoutOrder = new String[t.countTokens()];
        for (int n=0; n<metadata.layoutOrder.length; n++) {
          metadata.layoutOrder[n] = t.nextToken().trim();
        }
      }
      else if (key.equalsIgnoreCase("layout significant_bits")) {
        metadata.bitsPerPixel = Integer.parseInt(value);
      }
    }
    // representation category
    else if (token0.equals("representation")) {
      keyValue = findKeyValue(tokens, REPRESENTATION_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");

      if (key.equalsIgnoreCase("representation byte_order")) {
        metadata.byteOrder = value;
      }
      else if (key.equalsIgnoreCase("representation format")) {
        metadata.rFormat = value;
      }
      else if (key.equalsIgnoreCase("representation compression")) {
        metadata.rCompression = value;
      }
      else if (key.equalsIgnoreCase("representation sign")) {
        metadata.rSigned = value.equals("signed");
      }
    }
    // parameter category
    else if (token0.equals("parameter")) {
      keyValue = findKeyValue(tokens, PARAMETER_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");

      if (key.equalsIgnoreCase("parameter scale")) {
        // parse physical pixel sizes and time increment
        metadata.pixelSizes = splitDoubles(value);
      }
      else if (key.equalsIgnoreCase("parameter t")) {
        // parse explicit timestamps
        metadata.timestamps = splitDoubles(value);
      }
      else if (key.equalsIgnoreCase("parameter units")) {
        // parse units for scale
        metadata.units = value.split("\\s+");
      }
      if (getMetadataOptions().getMetadataLevel() !=
          MetadataLevel.MINIMUM)
      {
        if (key.equalsIgnoreCase("parameter ch")) {
          String[] names = value.split(" ");
          for (int n=0; n<names.length; n++) {
            metadata.channelNames.put(new Integer(n), names[n].trim());
          }
        }
      }
    }
    // history category
    else if (token0.equals("history")) {
      keyValue = findKeyValue(tokens, HISTORY_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      boolean track = true;

      Double doubleValue = null;
      try {
        doubleValue = new Double(value);
      }
      catch (NumberFormatException e) {
        //TODO ARG this happens a lot; spurious error in most cases
        LOGGER.debug("Could not parse double value '{}'", value, e);
      }
      
      if (key.equalsIgnoreCase("history software") &&
          value.indexOf("SVI") != -1) {
        // ICS files written by SVI Huygens are inverted on the Y axis
        metadata.invertY = true;
      }
      else if (key.equalsIgnoreCase("history date") ||
               key.equalsIgnoreCase("history created on"))
      {
        if (value.indexOf(" ") > 0) {
          metadata.hDate = value.substring(0, value.lastIndexOf(" "));
          metadata.hDate = DateTools.formatDate(metadata.hDate, DATE_FORMATS);
          metadata.hCDate = metadata.hDate;
        }
      }
      else if (key.equalsIgnoreCase("history creation date")) {
        metadata.hCrDate = DateTools.formatDate(value, DATE_FORMATS);
      }
      else if (key.equalsIgnoreCase("history type")) {
        // HACK - support for Gray Institute at Oxford's ICS lifetime data
        if (value.equalsIgnoreCase("time resolved") ||
            value.equalsIgnoreCase("FluorescenceLifetime"))
        {
          metadata.lifetime = true;
        }
        metadata.experimentType = value;
      }
      else if (key.equalsIgnoreCase("history labels")) {
          // HACK - support for Gray Institute at Oxford's ICS lifetime data
          metadata.experimentLabels = value;
      }
      else if (getMetadataOptions().getMetadataLevel() !=
                 MetadataLevel.MINIMUM) {

        if (key.equalsIgnoreCase("history") ||
            key.equalsIgnoreCase("history text"))
        {
          metadata.textBlock.append(value);
          metadata.textBlock.append("\n");
          //metadata.remove(key);
          //TODO ARG TEST
          track = false;
        }
        else if (key.startsWith("history gain")) {
          Integer n = new Integer(0);
          try {
            n = new Integer(key.substring(12).trim());
            n = new Integer(n.intValue() - 1);
          }
          catch (NumberFormatException e) { }
          if (doubleValue != null) {
              metadata.gains.put(n, doubleValue);
          }
        }
        else if (key.startsWith("history laser") &&
                 key.endsWith("wavelength")) {
          int laser =
            Integer.parseInt(key.substring(13, key.indexOf(" ", 13))) - 1;
          value = value.replaceAll("nm", "").trim();
          try {
            metadata.wavelengths.put(new Integer(laser), new Integer(value));
          }
          catch (NumberFormatException e) {
            LOGGER.debug("Could not parse wavelength", e);
          }
        }
        else if (key.equalsIgnoreCase("history Wavelength*")) {
          String[] waves = value.split(" ");
          for (int i=0; i<waves.length; i++) {
            metadata.wavelengths.put(new Integer(i), new Integer(waves[i]));
          }
        }
        else if (key.equalsIgnoreCase("history laser manufacturer")) {
          metadata.laserManufacturer = value;
        }
        else if (key.equalsIgnoreCase("history laser model")) {
          metadata.laserModel = value;
        }
        else if (key.equalsIgnoreCase("history laser power")) {
          try {
            metadata.laserPower = new Double(value); //TODO ARG i.e. doubleValue
          }
          catch (NumberFormatException e) { }
        }
        else if (key.equalsIgnoreCase("history laser rep rate")) {
          String repRate = value;
          if (repRate.indexOf(" ") != -1) {
            repRate = repRate.substring(0, repRate.lastIndexOf(" "));
          }
          metadata.laserRepetitionRate = new Double(repRate);
        }
        else if (key.equalsIgnoreCase("history objective type") ||
                 key.equalsIgnoreCase("history objective"))
        {
          objectiveModel = value;
        }
        else if (key.equalsIgnoreCase("history objective immersion")) {
          immersion = value;
        }
        else if (key.equalsIgnoreCase("history objective NA")) {
          lensNA = doubleValue;
        }
        else if (key.equalsIgnoreCase
                   ("history objective WorkingDistance")) {
          workingDistance = doubleValue;
        }
        else if (key.equalsIgnoreCase("history objective magnification") ||
                 key.equalsIgnoreCase("history objective mag"))
        {
          magnification = doubleValue;
        }
        else if (key.equalsIgnoreCase("history camera manufacturer")) {
          detectorManufacturer = value;
        }
        else if (key.equalsIgnoreCase("history camera model")) {
          detectorModel = value;
        }
        else if (key.equalsIgnoreCase("history author") ||
                 key.equalsIgnoreCase("history experimenter"))
        {
          lastName = value;
        }
        else if (key.equalsIgnoreCase("history extents")) {
          String[] lengths = value.split(" ");
          sizes = new double[lengths.length];
          for (int n=0; n<sizes.length; n++) {
            try {
              sizes[n] = Double.parseDouble(lengths[n].trim());
            }
            catch (NumberFormatException e) {
              LOGGER.debug("Could not parse axis length", e);
            }
          }
        }
        else if (key.equalsIgnoreCase("history stage_xyzum")) {
          String[] positions = value.split(" ");
          stagePos = new Double[positions.length];
          for (int n=0; n<stagePos.length; n++) {
            try {
              stagePos[n] = new Double(positions[n]);
            }
            catch (NumberFormatException e) {
              LOGGER.debug("Could not parse stage position", e);
            }
          }
        }
        else if (key.equalsIgnoreCase("history stage positionx")) {
          if (stagePos == null) {
            stagePos = new Double[3];
          }
          stagePos[0] = new Double(value); //TODO doubleValue
        }
        else if (key.equalsIgnoreCase("history stage positiony")) {
          if (stagePos == null) {
            stagePos = new Double[3];
          }
          stagePos[1] = new Double(value);
        }
        else if (key.equalsIgnoreCase("history stage positionz")) {
          if (stagePos == null) {
            stagePos = new Double[3];
          }
          stagePos[2] = new Double(value);
        }
        else if (key.equalsIgnoreCase("history other text")) {
          description = value;
        }
        else if (key.startsWith("history step") && key.endsWith("name")) {
          Integer n = new Integer(key.substring(12, key.indexOf(" ", 12)));
          channelNames.put(n, value);
        }
        else if (key.equalsIgnoreCase("history cube")) {
          channelNames.put(new Integer(channelNames.size()), value);
        }
        else if (key.equalsIgnoreCase("history cube emm nm")) {
          if (emWaves == null) {
            emWaves = new Integer[1];
          }
          emWaves[0] = new Integer(value.split(" ")[1].trim());
        }
        else if (key.equalsIgnoreCase("history cube exc nm")) {
          if (exWaves == null) {
            exWaves = new Integer[1];
          }
          exWaves[0] = new Integer(value.split(" ")[1].trim());
        }
        else if (key.equalsIgnoreCase("history microscope")) {
          microscopeModel = value;
        }
        else if (key.equalsIgnoreCase("history manufacturer")) {
          microscopeManufacturer = value;
        }
        else if (key.equalsIgnoreCase("history Exposure")) {
          String expTime = value;
          if (expTime.indexOf(" ") != -1) {
            expTime = expTime.substring(0, expTime.indexOf(" "));
          }
          exposureTime = new Double(expTime);
        }
        else if (key.equalsIgnoreCase("history filterset")) {
          filterSetModel = value;
        }
        else if (key.equalsIgnoreCase("history filterset dichroic name")) {
          dichroicModel = value;
        }
        else if (key.equalsIgnoreCase("history filterset exc name")) {
          excitationModel = value;
        }
        else if (key.equalsIgnoreCase("history filterset emm name")) {
          emissionModel = value;
        }
      }
      //TODO ARG TEST
      if (track) {
          //trackKeyValues(id, key, value, "");
      }
    }
    // document category
    else if (token0.equals("document")) {
      keyValue = findKeyValue(tokens, DOCUMENT_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");

    }
    // sensor category
    else if (token0.equals("sensor")) {
      keyValue = findKeyValue(tokens, SENSOR_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");

      if (getMetadataOptions().getMetadataLevel() !=
          MetadataLevel.MINIMUM)
      {
        if (key.equalsIgnoreCase("sensor s_params LambdaEm")) {
          String[] waves = value.split(" ");
          emWaves = new Integer[waves.length];
          for (int n=0; n<emWaves.length; n++) {
            try {
              emWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
            }
            catch (NumberFormatException e) {
              LOGGER.debug("Could not parse emission wavelength", e);
            }
          }
        }
        else if (key.equalsIgnoreCase("sensor s_params LambdaEx")) {
          String[] waves = value.split(" ");
          exWaves = new Integer[waves.length];
          for (int n=0; n<exWaves.length; n++) {
            try {
              exWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
            }
            catch (NumberFormatException e) {
              LOGGER.debug("Could not parse excitation wavelength", e);
            }
          }
        }
        else if (key.equalsIgnoreCase("sensor s_params PinholeRadius")) {
          String[] pins = value.split(" ");
          int channel = 0;
          for (int n=0; n<pins.length; n++) {
            if (pins[n].trim().equals("")) continue;
            try {
              pinholes.put(new Integer(channel++), new Double(pins[n]));
            }
            catch (NumberFormatException e) {
              LOGGER.debug("Could not parse pinhole", e);
            }
          }
        }
      }
    }
    // view category
    else if (token0.equals("view")) {
      keyValue = findKeyValue(tokens, VIEW_KEYS);
      String key = keyValue[0];
      String value = keyValue[1];

      // handle "view view color lib lut Green Fire green", etc.
      if (key.equalsIgnoreCase("view view color lib lut")) {
        int index;
        int redIndex = value.toLowerCase().lastIndexOf("red");
        int greenIndex = value.toLowerCase().lastIndexOf("green");
        int blueIndex = value.toLowerCase().lastIndexOf("blue");
        if (redIndex > 0 && redIndex > greenIndex && redIndex > blueIndex) {
          index = redIndex + "red".length();
        }
        else if (greenIndex > 0 &&
                 greenIndex > redIndex && greenIndex > blueIndex) {
          index = greenIndex + "green".length();
        }
        else if (blueIndex > 0 &&
                 blueIndex > redIndex && blueIndex > greenIndex) {
          index = blueIndex + "blue".length();
        }
        else {
            index = value.indexOf(' ');
        }
        if (index > 0) {
          key = key + ' ' + value.substring(0, index);
          value = value.substring(index + 1);
        }
      }
      // handle "view view color mode rgb set Default Colors" and
      // "view view color mode rgb set blue-green-red", etc.
      else if (key.equalsIgnoreCase("view view color mode rgb set")) {
          int index = value.toLowerCase().lastIndexOf("colors");
          if (index > 0) {
              index += "colors".length();
          }
          else {
            index = value.indexOf(' ');
          }
          if (index > 0) {
            key = key + ' ' + value.substring(0, index);
            value = value.substring(index + 1);
          }
      }
      addGlobalMeta(key, value);
      //TODO ARG TEST
      //trackKeyValues(id, key, value, "");
    }
    else {
      LOGGER.debug("Unknown category " + token0);
    }
  }
  */
}
