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

package ome.scifio.ics;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import net.imglib2.meta.Axes;
import net.imglib2.meta.Axes.CustomAxisType;
import net.imglib2.meta.AxisType;
import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.AbstractReader;
import ome.scifio.AbstractTranslator;
import ome.scifio.AbstractWriter;
import ome.scifio.CoreImageMetadata;
import ome.scifio.CoreMetadata;
import ome.scifio.CoreTranslator;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.common.DateTools;
import ome.scifio.discovery.SCIFIOFormat;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.io.RandomAccessOutputStream;
import ome.scifio.util.FormatTools;

@SCIFIOFormat
public class ICSFormat
extends
AbstractFormat<ICSFormat.Metadata, ICSFormat.Checker,
    ICSFormat.Parser, ICSFormat.Reader,
    ICSFormat.Writer> {

  // -- Constructor --

  public ICSFormat() throws FormatException {
    this(null);
  }

  public ICSFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, "Image Cytometry Standard", new String[] {"ics", "ids"}, Metadata.class, Checker.class, Parser.class, Reader.class, Writer.class);
  }
  
  // -- Accessor methods for private classes --
  
  public boolean isVersionTwo(Metadata m) {
    return (m instanceof Metadata) && ((Metadata)m).isVersionTwo();
  }

  /**
   * SCIFIO Metadata object for ICS images. 
   *
   */
  public static class Metadata extends AbstractMetadata {
  
    // -- Fields -- 
  
    /** Whether this file is ICS version 2,
     *  and thus does not have an IDS companion */
    protected boolean versionTwo = false;
  
    /** Offset to pixel data */
    protected long offset = -1;
  
    /** */
    protected boolean hasInstrumentData = false;
  
    /** ICS file name */
    protected String icsId = "";
  
    /** IDS file name */
    protected String idsId = "";
  
    /** ICS Metadata */
    protected Hashtable<String, String> keyValPairs;
  
    // -- Constructor --
  
    public Metadata() {
      this(null);
    }
  
    public Metadata(SCIFIO ctx) {
      super(ctx);
      keyValPairs = new Hashtable<String, String>();
    }
  
    // -- Helper Methods --
  
    /* @see Metadata#resetMeta() */
    public void reset() {
      super.reset(getClass());
      keyValPairs = new Hashtable<String, String>();
    }
  
    /**
     * Convenience method to directly access the hashtable.
     * @param key
     * @return
     */
    public String get(final String key) {
      return keyValPairs.get(key);
    }
  
    public String getIcsId() {
      return icsId;
    }
  
    public void setIcsId(final String icsId) {
      this.icsId = icsId;
    }
  
    public String getIdsId() {
      return idsId;
    }
  
    public void setIdsId(final String idsId) {
      this.idsId = idsId;
    }
  
    public boolean hasInstrumentData() {
      return hasInstrumentData;
    }
  
    public void setHasInstrumentData(final boolean hasInstrumentData) {
      this.hasInstrumentData = hasInstrumentData;
    }
  
    public boolean isVersionTwo() {
      return versionTwo;
    }
  
    public void setVersionTwo(final boolean versionTwo) {
      this.versionTwo = versionTwo;
    }
  
    public void setKeyValPairs(Hashtable<String, String> keyValPairs) {
      this.keyValPairs = keyValPairs;
    }
  
    public Hashtable<String, String> getKeyValPairs() {
      return keyValPairs;
    }
    
    // -- Accessor methods for dynamically retrieving common metadata --
    
    public String getDate() {
      String date = null;
      String[] kv = findValueForKey("history date", "history created on", "history creation date");
      if (kv[0].equalsIgnoreCase("history date") ||
        kv[0].equalsIgnoreCase("history created on"))
      {
        if (kv[1].indexOf(" ") > 0) {
          date = kv[1].substring(0, kv[1].lastIndexOf(" "));
        }
      }
      else if (kv[0].equalsIgnoreCase("history creation date")) {
        date = kv[1];
      }
      
      if(date != null)
        date = DateTools.formatDate(date, ICSFormat.ICSUtils.DATE_FORMATS);
      
      return date;
    }
    
    public String getDescription() {
      return findValueForKey("history other text")[1];
    }
    
    public String getMicroscopeModel() {
      return findValueForKey("history microscope")[1];
    }
    
    public String getMicroscopeManufacturer() {
      return findValueForKey("history manufacturer")[1];
    }
    
    public boolean getLifetime() {
      String[] kv = findValueForKey("history type");
      boolean lifetime = false;
      if (kv[1].equalsIgnoreCase("time resolved") ||
        kv[1].equalsIgnoreCase("FluorescenceLifetime"))
      {
        lifetime = true;
      }
      return lifetime;
    }
    
    public String getExperimentType() {
      return findValueForKey("history type")[1];
    }
    
    public Double[] getPixelSizes() {
      String[] kv = findValueForKey("parameter scale");
      return kv[1] == null ? null : splitDoubles(kv[1]);
    }
    
    public String[] getUnits() {
      String[] kv = findValueForKey("parameter units");
      return kv[1] == null ? null : kv[1].split("\\s+");
    }
    
    public String[] getAxes() {
      String[] kv = findValueForKey("layout order");
      String[] axes = null;
      if (kv[1] != null) {
        StringTokenizer t = new StringTokenizer(kv[1]);
        axes = new String[t.countTokens()];
        for(int n = 0; n < axes.length; n++) {
          axes[n] = t.nextToken().trim();
        }
      }
      return axes;
    }
    
    public double[] getAxesSizes() {
      String[] kv = findValueForKey("history extents");
      double[] sizes = null;
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
      return sizes;
    }
    
    public Double[] getTimestamps() {
      String[] kv = findValueForKey("parameter t");
      return kv[1] == null ? null : splitDoubles(kv[1]);
    }
    
    public Hashtable<Integer, String> getChannelNames() {
      String[] kv = findValueForKey("parameter ch");
      Hashtable<Integer, String> channelNames = new Hashtable<Integer, String>();
      if(kv[1] != null) {
        String[] names = kv[1].split(" ");
        for (int n = 0; n < names.length; n++) {
          channelNames.put(new Integer(n), names[n].trim());
        }
      }
      return channelNames;
    }
    
    public void addStepChannel(Hashtable<Integer, String> channelNames) {
      String[] kv = findValueIteration("history step", "name");
      if(kv[1] != null)
        channelNames.put(new Integer(kv[0].substring(12, kv[0].indexOf(" ", 12))), kv[1]);
    }
    
    public void addCubeChannel(Hashtable<Integer, String> channelNames) {
      String[] kv = findValueForKey("history cube");
      if(kv[1] != null)
        channelNames.put(new Integer(channelNames.size()), kv[1]);
    }
    
    public Hashtable<Integer, Double> getPinholes() {
      String[] kv = findValueForKey("sensor s_params PinholeRadius");
      Hashtable<Integer, Double> pinholes = new Hashtable<Integer, Double>();
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
      return pinholes;
    }
    
    public Integer[] getEMWaves() {
      String[] kv = findValueForKey("sensor s_params LambdaEm");
      Integer[] emWaves = null;
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
      return emWaves;
    }
    
    public Integer[] getEMSingleton() {
      String[] kv = findValueForKey("history cube emm nm");
      Integer[] emWaves = null;
      if(kv[1] != null) {
        emWaves = new Integer[1];
        emWaves[0] = new Integer(kv[1].split(" ")[1].trim());
      }
      return emWaves;
    }
    
    public Integer[] getEXWaves() {
      String[] kv = findValueForKey("sensor s_params LambdaEx");
      Integer[] exWaves = null;
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
      return exWaves;
    }
    
    public Integer[] getEXSingleton() {
      String[] kv = findValueForKey("history cube exc nm");
      Integer[] exWaves = null;
      if(kv[1] != null) {
        exWaves = new Integer[1];
        exWaves[0] = new Integer(kv[1].split(" ")[1].trim());
      }
      return exWaves;
    }
    
    public Hashtable<Integer, Integer> getWavelengths() {
      String[] kv = findValueForKey("history Wavelength*");
      Hashtable<Integer, Integer> wavelengths = new Hashtable<Integer, Integer>();
      if(kv[1] != null) {
        String[] waves = kv[1].split(" ");
        for(int n = 0; n < waves.length; n++) {
          wavelengths.put(new Integer(n), new Integer(waves[n]));
        }
      }
      return wavelengths;
    }
    
    public void addLaserWavelength(Hashtable<Integer, Integer> wavelengths) {
      String[] kv = findValueIteration("history laser", "wavelength");
      if(kv[1] != null) {
        int laser = Integer.parseInt(kv[0].substring(13, kv[0].indexOf(" ", 13))) - 1;
        kv[1] = kv[1].replaceAll("nm", "").trim();
        try {
          wavelengths.put(new Integer(laser), new Integer(kv[1]));
        } catch (NumberFormatException e) {
          LOGGER.debug("Could not parse wavelength", e);
        }
      }
    }
    
    public String getByteOrder() {
      return findValueForKey("representation byte_order")[1];
    }
    
    public String getRepFormat() {
      return findValueForKey("representation format")[1];
    }
    
    public String getCompression() {
      return findValueForKey("representation compression")[1];
    }
    
    public boolean isSigned() {
      String signed = findValueForKey("representation sign")[1];
      return signed != null && signed.equals("signed");
    }
    
    public String getLaserManufacturer() {
      return findValueForKey("history laser manufacturer")[1];
    }
    
    public String getLaserModel() {
      return findValueForKey("history laser model")[1];
    }
    
    public Double getLaserRepetitionRate() {
      return new Double(findValueForKey("history laser model")[1]);
    }
    
    public Double getLaserPower() {
      return new Double(findValueForKey("history laser power")[1]);
    }
    
    public String getDichroicModel() {
      return findValueForKey("history filterset dichroic name")[1];
    }
    
    public String getExcitationModel() {
      return findValueForKey("history filterset exc name")[1];
    }
    
    public String getEmissionModel() {
      return findValueForKey("history filterset emm name")[1];
    }
    
    public String getFilterSetModel() {
      return findValueForKey("history filterset")[1];
    }
    
    public String getObjectiveModel() {
      return findValueForKey("history objective type", "history objective")[1];
    }
    
    public String getImmersion() {
      return findValueForKey("history objective immersion")[1];
    }
    
    public Double getLensNA() {
      return new Double(findValueForKey("history objective NA")[1]);
    }
    
    public Double getWorkingDistance() {
      return new Double(findValueForKey("history objective WorkingDistance")[1]);
    }
    
    public Double getMagnification() {
      return new Double(findValueForKey("history objective magnification", "history objective mag")[1]);
    }
    
    public String getDetectorManufacturer() {
      return findValueForKey("history camera manufacturer")[1];
    }
    
    public String getDetectorModel() {
      return findValueForKey("history camera model")[1];
    }
    
    public Integer getBitsPerPixel() {
      String[] kv = findValueForKey("layout significant_bits");
      Integer bpp = null;
      if(kv[1] != null) {
        bpp = new Integer(kv[1]);
      }
      return bpp;
    }
    
    public Hashtable<Integer, Double> getGains() {
      String[] kv = findValueForKey("history gain");
      Hashtable<Integer, Double> gains = new Hashtable<Integer, Double>();
      if(kv[1] != null) {
        Integer n = new Integer(0);
        try{ 
          n = new Integer(kv[0].substring(12).trim());
          n = new Integer(n.intValue() - 1);
        }
        catch (NumberFormatException e) { }
        gains.put(n, new Double(kv[1]));
      }
      return gains;
    }
    
    public String getAuthorLastName() {
      return findValueForKey("history author", "history experimenter")[1];
    }
    
    public Double[] getStagePositions() {
      String[] kv = findValueForKey("history stage_xyzum");
      Double[] stagePos = null;
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
      return stagePos;
    }
    
    public Double getStageX() {
      return new Double(findValueForKey("history stage positionx")[1]);
    }
    
    public Double getStageY() {
      return new Double(findValueForKey("history stage positiony")[1]);
    }
    
    public Double getStageZ() {
      return new Double(findValueForKey("history stage positionz")[1]);
    }
    
    public Double getExposureTime() {
      String[] kv = findValueForKey("history Exposure");
      Double exposureTime = null;
      if (kv[1] != null) {
        String expTime = kv[1];
        if(expTime.indexOf(" ") != -1) {
          exposureTime = new Double(expTime.indexOf(" "));
        }
      } 
      return exposureTime;
    }
    
    // -- Helper methods for finding key values --

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
        String value = keyValPairs.get(key);
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
      
      for (String key : keyValPairs.keySet()) {
        if ((starts == null || key.startsWith(starts)) && (ends == null || key.endsWith(ends)))
          return new String[]{key, keyValPairs.get(key)};
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
  }

  /**
   * SCIFIO file format Checker for ICS images.
   * 
   */
  public static class Checker extends AbstractChecker<Metadata> {

    // -- Constructor --

    public Checker() {
      this(null);
    }

    public Checker(final SCIFIO ctx) {
      super(ctx);
    }

    // -- Checker API Methods --

  }

  /**
   * SCIFIO file format Parser for ICS images. 
   *
   */
  public static class Parser extends AbstractParser<Metadata> {
  
    // -- Constructor --
  
    public Parser() {
      this(null);
    }
  
    public Parser(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Parser API Methods --
  
    /* @see Parser#parse(RandomAccessInputStream) */
    @Override
    public Metadata parse(final RandomAccessInputStream stream)
      throws IOException, FormatException
      {
      super.parse(stream);
  
      if (stream.getFileName() != null) findCompanion(stream.getFileName());
  
      final RandomAccessInputStream reader =
        new RandomAccessInputStream(metadata.getIcsId());
  
      reader.seek(0);
      reader.readString(ICSUtils.NL);
      String line = reader.readString(ICSUtils.NL);
  
      // Extracts the keys, value pairs from each line and inserts them into the ICSMetadata object
      while (line != null && !line.trim().equals("end") &&
        reader.getFilePointer() < reader.length() - 1)
      {
        line = line.trim();
        final String[] tokens = line.split("[ \t]");
        final StringBuffer key = new StringBuffer();
        for (int q = 0; q < tokens.length; q++) {
          tokens[q] = tokens[q].trim();
          if (tokens[q].length() == 0) continue;
  
          boolean foundValue = true;
          for (int i = 0; i < ICSUtils.CATEGORIES.length; i++) {
            if (tokens[q].matches(ICSUtils.CATEGORIES[i])) {
              foundValue = false;
              break;
            }
          }
          if (!foundValue) {
            key.append(tokens[q]);
            key.append(" ");
            continue;
          }
  
          final StringBuffer value = new StringBuffer(tokens[q++]);
          for (; q < tokens.length; q++) {
            value.append(" ");
            value.append(tokens[q].trim());
          }
          final String k = key.toString().trim().replaceAll("\t", " ");
          final String v = value.toString().trim();
          addGlobalMeta(k, v);
          metadata.keyValPairs.put(k.toLowerCase(), v);
        }
        line = reader.readString(ICSUtils.NL);
      }
      reader.close();
  
      if (metadata.versionTwo) {
        String s = in.readString(ICSUtils.NL);
        while (!s.trim().equals("end"))
          s = in.readString(ICSUtils.NL);
      }
  
      metadata.offset = in.getFilePointer();
  
      metadata.hasInstrumentData =
        nullKeyCheck(new String[] {
          "history cube emm nm", "history cube exc nm", "history objective NA",
          "history stage xyzum", "history objective magnification",
          "history objective mag", "history objective WorkingDistance",
          "history objective type", "history objective",
        "history objective immersion"});
  
      return metadata;
      }
  
    // -- Helper Methods --
  
    /* Returns true if any of the keys in testKeys has a non-null value */
    private boolean nullKeyCheck(final String[] testKeys) {
      for (final String key : testKeys) {
        if (metadata.get(key) != null) {
          return true;
        }
      }
      return false;
    }
  
    /* Finds the companion file (ICS and IDS are companions) */
    private void findCompanion(final String id)
      throws IOException, FormatException
      {
      metadata.icsId = id;
      metadata.idsId = id;
      String icsId = id, idsId = id;
      final int dot = id.lastIndexOf(".");
      final String ext = dot < 0 ? "" : id.substring(dot + 1).toLowerCase();
      if (ext.equals("ics")) {
        // convert C to D regardless of case
        final char[] c = idsId.toCharArray();
        c[c.length - 2]++;
        idsId = new String(c);
      }
      else if (ext.equals("ids")) {
        // convert D to C regardless of case
        final char[] c = icsId.toCharArray();
        c[c.length - 2]--;
        icsId = new String(c);
      }
  
      if (icsId == null) throw new FormatException("No ICS file found.");
      final Location icsFile = new Location(icsId);
      if (!icsFile.exists()) throw new FormatException("ICS file not found.");
  
      // check if we have a v2 ICS file - means there is no companion IDS file
      final RandomAccessInputStream f = new RandomAccessInputStream(icsId);
      if (f.readString(17).trim().equals("ics_version\t2.0")) {
        in = new RandomAccessInputStream(icsId);
        metadata.versionTwo = true;
      }
      else {
        if (idsId == null) throw new FormatException("No IDS file found.");
        final Location idsFile = new Location(idsId);
        if (!idsFile.exists()) throw new FormatException("IDS file not found.");
        //currentIdsId = idsId;
        metadata.idsId = idsId;
        in = new RandomAccessInputStream(idsId);
      }
      f.close();
      }
  }

  /**
   * File format SCIFIO Reader for Image Cytometry Standard (ICS)
   * images. Version 1 and 2 supported.
   * 
   */
  public static class Reader extends AbstractReader<Metadata> {
  
    // -- Fields --
  
    private int prevImage;
  
    /** Whether or not the pixels are GZIP-compressed. */
  
    private boolean gzip;
  
    private GZIPInputStream gzipStream;
  
    /** Whether or not the image is inverted along the Y axis. */
    private boolean invertY; //TODO only in oldInitFile
  
    /** Whether or not the channels represent lifetime histogram bins. */
    private boolean lifetime; //TODO only in oldInitFile
  
    /** Image data. */
    private byte[] data;
  
    private boolean storedRGB = false; // TODO only in oldInitFile
  
    // -- Constructor --
  
    public Reader() {
      this(null);
    }
  
    public Reader(final SCIFIO ctx) {
      super(ctx);
      domains =
        new String[] {
        FormatTools.LM_DOMAIN, FormatTools.FLIM_DOMAIN,
        FormatTools.UNKNOWN_DOMAIN};
      hasCompanionFiles = true;
    }
  
    // -- Reader API Methods --
  
    @Override
    public byte[] openBytes(final int imageIndex, final int planeIndex,
      final byte[] buf, final int x, final int y, final int w, final int h)
        throws FormatException, IOException
        {
      FormatTools.checkPlaneParameters(
        this, imageIndex, planeIndex, buf.length, x, y, w, h);
  
      final int bpp =
        FormatTools.getBytesPerPixel(cMeta.getPixelType(imageIndex));
      final int len = FormatTools.getPlaneSize(this, imageIndex);
      final int pixel = bpp * cMeta.getRGBChannelCount(imageIndex);
      final int rowLen = FormatTools.getPlaneSize(this, w, 1, imageIndex);
  
      final int[] coordinates = FormatTools.getZCTCoords(this, imageIndex, planeIndex);
      final int[] prevCoordinates = FormatTools.getZCTCoords(this, imageIndex, prevImage);
  
      if (!gzip) {
        in.seek(metadata.offset + planeIndex * (long) len);
      }
      else {
        long toSkip = (planeIndex - prevImage - 1) * (long) len;
        if (gzipStream == null || planeIndex <= prevImage) {
          FileInputStream fis = null;
          toSkip = planeIndex * (long) len;
          if (metadata.versionTwo) {
            fis = new FileInputStream(metadata.icsId);
            fis.skip(metadata.offset);
          }
          else {
            fis = new FileInputStream(metadata.idsId);
            toSkip += metadata.offset;
          }
          try {
            gzipStream = new GZIPInputStream(fis);
          }
          catch (final IOException e) {
            // the 'gzip' flag is set erroneously
            gzip = false;
            in.seek(metadata.offset + planeIndex * (long) len);
            gzipStream = null;
          }
        }
  
        if (gzipStream != null) {
          while (toSkip > 0) {
            toSkip -= gzipStream.skip(toSkip);
          }
  
          data =
            new byte[len *
                     (storedRGB ? cMeta.getAxisLength(imageIndex, Axes.CHANNEL) : 1)];
          int toRead = data.length;
          while (toRead > 0) {
            toRead -= gzipStream.read(data, data.length - toRead, toRead);
          }
        }
      }
  
      final int sizeC =
        lifetime ? 1 : cMeta.getAxisLength(imageIndex, Axes.CHANNEL);
  
      final int channelLengths = 0;
  
      if (!cMeta.isRGB(imageIndex) &&
          cMeta.getChannelDimLengths(imageIndex).length == 1 && storedRGB)
      {
        // channels are stored interleaved, but because there are more than we
        // can display as RGB, we need to separate them
        in.seek(metadata.offset +
            (long) len *
            FormatTools.getIndex(
                this, imageIndex, coordinates[0], 0, coordinates[2]));
        if (!gzip && data == null) {
          data = new byte[len * cMeta.getAxisLength(imageIndex, Axes.CHANNEL)];
          in.read(data);
        }
        else if (!gzip &&
            (coordinates[0] != prevCoordinates[0] || coordinates[2] != prevCoordinates[2]))
        {
          in.read(data);
        }
  
        for (int row = y; row < h + y; row++) {
          for (int col = x; col < w + x; col++) {
            int src =
                bpp * ((planeIndex % cMeta.getAxisLength(imageIndex, Axes.CHANNEL))
                    + sizeC * (row * (row * cMeta.getAxisLength(imageIndex, Axes.X) + col)));
            int dest = bpp * ((row - y) * w + (col - x));
            System.arraycopy(data, src, buf, dest, bpp); 
          }
        }
      }
      else if (gzip) {
        final RandomAccessInputStream s = new RandomAccessInputStream(data);
        readPlane(s, imageIndex, x, y, w, h, buf);
        s.close();
      }
      else {
        readPlane(in, imageIndex, x, y, w, h, buf);
      }
  
      if (invertY) {
        final byte[] row = new byte[rowLen];
        for (int r = 0; r < h / 2; r++) {
          final int topOffset = r * rowLen;
          final int bottomOffset = (h - r - 1) * rowLen;
          System.arraycopy(buf, topOffset, row, 0, rowLen);
          System.arraycopy(buf, bottomOffset, buf, topOffset, rowLen);
          System.arraycopy(row, 0, buf, bottomOffset, rowLen);
        }
      }
  
      prevImage = planeIndex;
  
      return buf;
        }
  
    /* @see Reader#close(boolean) */
    @Override
    public void close(final boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        data = null;
        gzip = false;
        invertY = false;
        lifetime = false;
        prevImage = 0;
        //TODO hasInstrumentData = false;
        storedRGB = false;
        if (gzipStream != null) {
          gzipStream.close();
        }
        gzipStream = null;
      }
    }
  
    /* @see Reader#setMetadata(Metadata) */
    @Override
    public void setMetadata(final Metadata meta) throws IOException {
      super.setMetadata(meta);
      gzip = metadata.get("representation compression").equals("gzip");
    }
  
    /* @see Reader#setSource(RandomAccessInputStream) */
    @Override
    public void setSource(final RandomAccessInputStream stream) throws IOException {
      super.setSource(stream);
      if(!getMetadata().versionTwo)
        in = new RandomAccessInputStream(getMetadata().idsId);
    }
  
    @Override
    public String[] getDomains() {
      FormatTools.assertStream(in, true, 0);
      final String[] domain = new String[] {FormatTools.GRAPHICS_DOMAIN};
      if (cMeta.getChannelDimLengths(0).length > 1) {
        domain[0] = FormatTools.FLIM_DOMAIN;
      }
      else if (metadata.hasInstrumentData) {
        domain[0] = FormatTools.LM_DOMAIN;
      }
  
      return domain;
    }
  
    // -- ICSReader Methods --
  
  }

  /**
   *  SCIFIO file format writer for ICS version 1 and 2 files.
   *
   */
  public static class Writer extends AbstractWriter<Metadata> {
  
    // -- Fields --
  
    private long dimensionOffset;
    private int dimensionLength;
    private long pixelOffset;
    private int lastPlane = -1;
    private RandomAccessOutputStream pixels;
    
    // NB: write in ZTC order by default.  Certain software (e.g. Volocity)
    //     lacks the capacity to import files with any other dimension
    //     ordering.  Technically, this is not our problem, but it is
    //     easy enough to work around and makes life easier for our users.
    private String outputOrder = "XYZTC";
    
    // -- Constructor --
  
    public Writer() {
      this(null);
    }
  
    public Writer(final SCIFIO ctx) {
      super(ctx);
    }
    
    // -- ICSWriter API methods --

    /**
     * Set the order in which dimensions should be written to the file.
     * Valid values are specified in the documentation for
     * {@link loci.formats.IFormatReader#getDimensionOrder()}
     *
     * By default, the ordering is "XYZTC".
     */
    public void setOutputOrder(String outputOrder) {
      this.outputOrder = outputOrder;
    }
  
    // -- Writer API Methods --
  
    /* @see ome.scifio.Writer#saveBytes(int, int, byte[], int, int, int, int */
    public void saveBytes(final int imageIndex, final int planeIndex,
        final byte[] buf, final int x, final int y, final int w, final int h)
            throws FormatException, IOException
    {
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);
      
      int rgbChannels = cMeta.getRGBChannelCount(imageIndex);

      int sizeZ = cMeta.getAxisLength(imageIndex, Axes.Z);
      int sizeC = cMeta.getAxisLength(imageIndex, Axes.CHANNEL);
      if (rgbChannels <= sizeC) {
        sizeC /= rgbChannels;
      }
      
      int sizeT = cMeta.getAxisLength(imageIndex, Axes.TIME);
      int planes = sizeZ * sizeC * sizeT;

      int[] coords =
          FormatTools.getZCTCoords(outputOrder, sizeZ, sizeC, sizeT,
              planes, imageIndex, planeIndex);
      int realIndex =
          FormatTools.getIndex(outputOrder, sizeZ, sizeC, sizeT,
          planes, coords[0], coords[1], coords[2]);

      int sizeX = cMeta.getAxisLength(imageIndex, Axes.X);
      int sizeY = cMeta.getAxisLength(imageIndex, Axes.Y);
      int pixelType = cMeta.getPixelType(imageIndex);
      int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
      int planeSize = sizeX * sizeY * rgbChannels * bytesPerPixel;

      if(!initialized[planeIndex][realIndex]) {
        initialized[planeIndex][realIndex] = true;

        if (!isFullPlane(imageIndex, x, y, w, h)) {
          pixels.seek(pixelOffset + (realIndex + 1) * planeSize);
        }
      }
      
      pixels.seek(pixelOffset + realIndex * planeSize);
      if (isFullPlane(imageIndex, x, y, w, h)
          && (interleaved || rgbChannels == 1)) {
        pixels.write(buf);
      }
      else {
        pixels.skipBytes(bytesPerPixel * rgbChannels * sizeX * y);
        for (int row=0; row<h; row++) {
          ByteArrayOutputStream strip = new ByteArrayOutputStream();
          for (int col=0; col<w; col++) {
            for (int c=0; c<rgbChannels; c++) {
              int index = interleaved ? rgbChannels * (row * w + col) + c :
                w * (c * h + row) + col;
              strip.write(buf, index * bytesPerPixel, bytesPerPixel);
            }
          }
          pixels.skipBytes(bytesPerPixel * rgbChannels * x);
          pixels.write(strip.toByteArray());
          pixels.skipBytes(bytesPerPixel * rgbChannels * (sizeX - w- x));
        }
      }
      lastPlane = realIndex;
    }
    
    /* @see ome.scifio.Writer#caDoStacks() */
    public boolean canDoStacks() { return true; };
  
    /* @see ome.scifio.Writer#setMetadata(Metadata) */
    public void setMetadata(final Metadata meta) {
      super.setMetadata(meta, new ICSCoreTranslator());
    }
    
    /* @see ome.scifio.Writer#getPixelTypes(String) */
    public int[] getPixelTypes(String codec) {
      return new int[] {FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
        FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32,
        FormatTools.FLOAT};
    }
    
    /* @see ome.scifio.Writer#close() */
    public void close(int imageIndex) throws IOException {
      if (lastPlane != cMeta.getPlaneCount(imageIndex) - 1 && out != null) {
        overwriteDimensions(cMeta, imageIndex);
      }

      super.close();
      pixelOffset = 0;
      lastPlane = -1;
      dimensionOffset = 0;
      dimensionLength = 0;
      if (pixels != null) {
        pixels.close();
      }
      pixels = null;
    }
    
    @Override
    public void setDest(final String id)
      throws FormatException, IOException {
      updateMetadataIds(id);
      setDest(new RandomAccessOutputStream(id), 0);
    }
    
    @Override
    public void setDest(final String id, final int imageIndex)
      throws FormatException, IOException {
      updateMetadataIds(id);
      setDest(new RandomAccessOutputStream(id), imageIndex);
    }
  
    @Override
    public void setDest(final RandomAccessOutputStream out, final int imageIndex)
      throws FormatException, IOException {
      super.setDest(out, imageIndex);
      initialize(imageIndex);
    }
    
    // -- Helper methods --
    
    private void updateMetadataIds(String id) {
      metadata.idsId = FormatTools.checkSuffix(id, "ids") ? id : makeIdsId(id);
      metadata.icsId = FormatTools.checkSuffix(id, "ics") ? id : makeIcsId(id);
    }
    
    private void initialize(final int imageIndex) throws FormatException,
      IOException {
      String currentId = getMetadata().idsId != null ? 
          getMetadata().idsId : getMetadata().icsId;
      
      if (FormatTools.checkSuffix(getMetadata().idsId, "ids")) {
        String metadataFile = makeIcsId(currentId);
        if(out != null) out.close();
        out = new RandomAccessOutputStream(metadataFile);
      }

      if (out.length() == 0) {
        out.writeBytes("\t\n");
        if (FormatTools.checkSuffix(currentId, "ids")) {
          out.writeBytes("ics_version\t1.0\n");
        }
        else {
          out.writeBytes("ics_version\t2.0\n");
        }
        out.writeBytes("filename\t" + currentId + "\n");
        out.writeBytes("layout\tparameters\t6\n");

        CoreMetadata meta = cMeta;
        //SCIFIOMetadataTools.verifyMinimumPopulated(meta, pixels);

        int pixelType = meta.getPixelType(imageIndex);

        dimensionOffset = out.getFilePointer();
        int[] sizes = overwriteDimensions(meta, imageIndex);
        dimensionLength = (int) (out.getFilePointer() - dimensionOffset);

        if (validBits != 0) {
          out.writeBytes("layout\tsignificant_bits\t" + validBits + "\n");
        }

        boolean signed = FormatTools.isSigned(pixelType);
        boolean littleEndian = meta.isLittleEndian(imageIndex);

        out.writeBytes("representation\tformat\t" +
            (pixelType == FormatTools.FLOAT ? "real\n" : "integer\n"));
        out.writeBytes("representation\tsign\t" +
            (signed ? "signed\n" : "unsigned\n"));
        out.writeBytes("representation\tcompression\tuncompressed\n");
        out.writeBytes("representation\tbyte_order\t");
        for (int i=0; i<sizes[0]/8; i++) {
          if ((littleEndian &&
              (sizes[0] < 32 || pixelType == FormatTools.FLOAT)) ||
              (!littleEndian && sizes[0] >= 32 && pixelType != FormatTools.FLOAT))
          {
            out.writeBytes((i + 1) + "\t");
          }
          else {
            out.writeBytes(((sizes[0] / 8) - i) + "\t");
          }
        }

        out.writeBytes("\nparameter\tscale\t1.000000\t");

        StringBuffer units = new StringBuffer();
        for (int i=0; i<outputOrder.length(); i++) {
          char dim = outputOrder.charAt(i);
          Number value = 1.0;
          if (dim == 'X') {
            if (meta.getAxisIndex(imageIndex, Axes.X) != -1) {
              value = meta.getAxisLength(imageIndex, Axes.X);
            }
            units.append("micrometers\t");
          }
          else if (dim == 'Y') {
            if (meta.getAxisIndex(imageIndex, Axes.Y) != -1) {
              value = meta.getAxisLength(imageIndex, Axes.Y);
            }
            units.append("micrometers\t");
          }
          else if (dim == 'Z') {
            if (meta.getAxisIndex(imageIndex, Axes.Z) != -1) {
              value = meta.getAxisLength(imageIndex, Axes.X);
            }
            units.append("micrometers\t");
          }
          else if (dim == 'T') {
            if (meta.getAxisIndex(imageIndex, Axes.TIME) != -1) {
              value = meta.getAxisLength(imageIndex, Axes.TIME);
            };
            units.append("seconds\t");
          }
          out.writeBytes(value + "\t");
        }

        out.writeBytes("\nparameter\tunits\tbits\t" + units.toString() + "\n");
        out.writeBytes("\nend\n");
        pixelOffset = out.getFilePointer();
      }
      else if (FormatTools.checkSuffix(currentId, "ics")) {
        RandomAccessInputStream in = new RandomAccessInputStream(currentId);
        in.findString("\nend\n");
        pixelOffset = in.getFilePointer();
        in.close();
      }

      if (FormatTools.checkSuffix(currentId, "ids")) {
        pixelOffset = 0;
      }

      if (pixels == null) {
        pixels = new RandomAccessOutputStream(currentId);
      }
    }
    
    private int[] overwriteDimensions(CoreMetadata meta, int imageIndex) throws IOException {
      out.seek(dimensionOffset);
      int sizeX = meta.getAxisLength(imageIndex, Axes.X);
      int sizeY = meta.getAxisLength(imageIndex, Axes.Y);
      int z = meta.getAxisLength(imageIndex, Axes.Z);
      int c = meta.getAxisLength(imageIndex, Axes.CHANNEL);
      int t = meta.getAxisLength(imageIndex, Axes.TIME);
      int pixelType = meta.getPixelType(imageIndex);
      int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
      int rgbChannels = meta.getRGBChannelCount(imageIndex);

      if (lastPlane < 0) lastPlane = z * c * t - 1;
      int[] pos =
        FormatTools.getZCTCoords(outputOrder, z, c, t, z * c * t, imageIndex, lastPlane);
      lastPlane = -1;

      StringBuffer dimOrder = new StringBuffer();
      int[] sizes = new int[6];
      int nextSize = 0;
      sizes[nextSize++] = 8 * bytesPerPixel;

      if (rgbChannels > 1) {
        dimOrder.append("ch\t");
        sizes[nextSize++] = pos[1] + 1;
      }

      for (int i=0; i<outputOrder.length(); i++) {
        if (outputOrder.charAt(i) == 'X') sizes[nextSize++] = sizeX;
        else if (outputOrder.charAt(i) == 'Y') sizes[nextSize++] = sizeY;
        else if (outputOrder.charAt(i) == 'Z') sizes[nextSize++] = pos[0] + 1;
        else if (outputOrder.charAt(i) == 'T') sizes[nextSize++] = pos[2] + 1;
        else if (outputOrder.charAt(i) == 'C' && dimOrder.indexOf("ch") == -1) {
          sizes[nextSize++] = pos[1] + 1;
          dimOrder.append("ch");
        }
        if (outputOrder.charAt(i) != 'C') {
          dimOrder.append(String.valueOf(outputOrder.charAt(i)).toLowerCase());
        }
        dimOrder.append("\t");
      }
      out.writeBytes("layout\torder\tbits\t" + dimOrder.toString() + "\n");
      out.writeBytes("layout\tsizes\t");
      for (int i=0; i<sizes.length; i++) {
        out.writeBytes(sizes[i] + "\t");
      }
      while ((out.getFilePointer() - dimensionOffset) < dimensionLength - 1) {
        out.writeBytes(" ");
      }
      out.writeBytes("\n");

      return sizes;
    }
    
    /* Turns the provided id into a .ics path */
    private String makeIcsId(String idsId) {
      return setIdExtension(idsId, ".ics");
    }
    
    /* Turns the provided id into a .ids path */
    private String makeIdsId(String icsId) {
      return setIdExtension(icsId, ".ids");
    }
    
    /* Replaces the current extension of the provided id with the
     * provided extension
     */
    private String setIdExtension(String id, String extension) {
      id = id.substring(0, id.lastIndexOf("."));
      id += extension;
      return id;
    }
  }

  /**
   * Collection of utility methods and constants
   * for working with ICS images.
   *
   */
   public static class ICSUtils {

    // -- Constants --

    /** Newline characters. */
    public static final String NL = "\r\n";

    public static final String[] DATE_FORMATS = {
      "EEEE, MMMM dd, yyyy HH:mm:ss", "EEE dd MMMM yyyy HH:mm:ss",
      "EEE MMM dd HH:mm:ss yyyy", "EE dd MMM yyyy HH:mm:ss z",
    "HH:mm:ss dd\\MM\\yy"};

    // key token value matching regexes within the "document" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] DOCUMENT_KEYS = { {"date"}, // the full key is "document date"
      {"document", "average"}, {"document"}, {"gmtdate"}, {"label"}};

    // key token value matching regexes within the "history" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] HISTORY_KEYS = {
      {"a\\d"}, // the full key is "history a1", etc.
      {"acquisition", "acquire\\..*."},
      {"acquisition", "laserbox\\..*."},
      {"acquisition", "modules\\(.*."},
      {"acquisition", "objective", "position"},
      {"adc", "resolution"},
      {"atd_hardware", "ver"},
      {"atd_libraries", "ver"},
      {"atd_microscopy", "ver"},
      {"author"},
      {"averagecount"},
      {"averagequality"},
      {"beam", "zoom"},
      {"binning"},
      {"bits/pixel"},
      {"black", "level"},
      {"black", "level\\*"},
      {"black_level"},
      {"camera", "manufacturer"},
      {"camera", "model"},
      {"camera"},
      {"cfd", "holdoff"},
      {"cfd", "limit", "high"},
      {"cfd", "limit", "low"},
      {"cfd", "zc", "level"},
      {"channel\\*"},
      {"collection", "time"},
      {"cols"},
      {"company"},
      {"count", "increment"},
      {"created", "on"},
      {"creation", "date"},
      {"cube", "descriptio"}, // sic; not found in sample files
      {"cube", "description"}, // correction; not found in sample files
      {"cube", "emm", "nm"},
      {"cube", "exc", "nm"},
      {"cube"},
      {"date"},
      {"dategmt"},
      {"dead", "time", "comp"},
      {"desc", "exc", "turret"},
      {"desc", "emm", "turret"},
      {"detector", "type"},
      {"detector"},
      {"dimensions"},
      {"direct", "turret"},
      {"dither", "range"},
      {"dwell"},
      {"excitationfwhm"},
      {"experiment"},
      {"experimenter"},
      {"expon.", "order"},
      {"exposure"},
      {"exposure_time"},
      {"ext", "latch", "delay"},
      {"extents"},
      {"filterset", "dichroic", "name"},
      {"filterset", "dichroic", "nm"},
      {"filterset", "emm", "name"},
      {"filterset", "emm", "nm"},
      {"filterset", "exc", "name"},
      {"filterset", "exc", "nm"},
      {"filterset"},
      {"filter\\*"},
      {"firmware"},
      {"fret", "backgr\\d"},
      {"frametime"},
      {"gain"},
      {"gain\\d"},
      {"gain\\*"},
      {"gamma"},
      {"icsviewer", "ver"},
      {"ht\\*"},
      {"id"},
      {"illumination", "mode", "laser"},
      {"illumination", "mode"},
      {"image", "bigendian"},
      {"image", "bpp"},
      {"image", "form"}, // not found in sample files
      {"image", "physical_sizex"},
      {"image", "physical_sizey"},
      {"image", "sizex"},
      {"image", "sizey"},
      {"labels"},
      {"lamp", "manufacturer"},
      {"lamp", "model"},
      {"laser", "firmware"},
      {"laser", "manufacturer"},
      {"laser", "model"},
      {"laser", "power"},
      {"laser", "rep", "rate"},
      {"laser", "type"},
      {"laser\\d", "intensity"},
      {"laser\\d", "name"},
      {"laser\\d", "wavelength"},
      {"left"},
      {"length"},
      {"line", "compressio"}, // sic
      {"line", "compression"}, // correction; not found in sample files
      {"linetime"},
      {"magnification"},
      {"manufacturer"},
      {"max", "photon", "coun"}, // sic
      {"max", "photon", "count"}, // correction; not found in sample files
      {"memory", "bank"}, {"metadata", "format", "ver"},
      {"microscope", "built", "on"}, {"microscope", "name"}, {"microscope"},
      {"mirror", "\\d"}, {"mode"}, {"noiseval"}, {"no.", "frames"},
      {"objective", "detail"}, {"objective", "immersion"},
      {"objective", "mag"}, {"objective", "magnification"},
      {"objective", "na"}, {"objective", "type"},
      {"objective", "workingdistance"}, {"objective"}, {"offsets"},
      {"other", "text"}, {"passcount"}, {"pinhole"}, {"pixel", "clock"},
      {"pixel", "time"}, {"pmt"}, {"polarity"}, {"region"}, {"rep", "period"},
      {"repeat", "time"}, {"revision"}, {"routing", "chan", "x"},
      {"routing", "chan", "y"}, {"rows"}, {"scan", "borders"},
      {"scan", "flyback"}, {"scan", "pattern"}, {"scan", "pixels", "x"},
      {"scan", "pixels", "y"}, {"scan", "pos", "x"}, {"scan", "pos", "y"},
      {"scan", "resolution"}, {"scan", "speed"}, {"scan", "zoom"},
      {"scanner", "lag"}, {"scanner", "pixel", "time"},
      {"scanner", "resolution"}, {"scanner", "speed"}, {"scanner", "xshift"},
      {"scanner", "yshift"}, {"scanner", "zoom"}, {"shutter\\d"},
      {"shutter", "type"}, {"software"}, {"spectral", "bin_definition"},
      {"spectral", "calibration", "gain", "data"},
      {"spectral", "calibration", "gain", "mode"},
      {"spectral", "calibration", "offset", "data"},
      {"spectral", "calibration", "offset", "mode"},
      {"spectral", "calibration", "sensitivity", "mode"},
      {"spectral", "central_wavelength"}, {"spectral", "laser_shield"},
      {"spectral", "laser_shield_width"}, {"spectral", "resolution"},
      {"stage", "controller"}, {"stage", "firmware"},
      {"stage", "manufacturer"}, {"stage", "model"}, {"stage", "pos"},
      {"stage", "positionx"}, {"stage", "positiony"}, {"stage", "positionz"},
      {"stage_xyzum"}, {"step\\d", "channel", "\\d"},
      {"step\\d", "gain", "\\d"}, {"step\\d", "laser"}, {"step\\d", "name"},
      {"step\\d", "pinhole"}, {"step\\d", "pmt", "ch", "\\d"},
      {"step\\d", "shutter", "\\d"}, {"step\\d"}, {"stop", "on", "o'flow"},
      {"stop", "on", "time"}, {"study"}, {"sync", "freq", "div"},
      {"sync", "holdoff"}, {"sync"}, {"tac", "gain"}, {"tac", "limit", "low"},
      {"tac", "offset"}, {"tac", "range"}, {"tau\\d"}, {"tcspc", "adc", "res"},
      {"tcspc", "adc", "resolution"}, {"tcspc", "approx", "adc", "rate"},
      {"tcspc", "approx", "cfd", "rate"}, {"tcspc", "approx", "tac", "rate"},
      {"tcspc", "bh"}, {"tcspc", "cfd", "holdoff"},
      {"tcspc", "cfd", "limit", "high"}, {"tcspc", "cfd", "limit", "low"},
      {"tcspc", "cfd", "zc", "level"}, {"tcspc", "clock", "polarity"},
      {"tcspc", "collection", "time"}, {"tcspc", "count", "increment"},
      {"tcspc", "dead", "time", "enabled"}, {"tcspc", "delay"},
      {"tcspc", "dither", "range"}, {"tcspc", "left", "border"},
      {"tcspc", "line", "compression"}, {"tcspc", "mem", "offset"},
      {"tcspc", "operation", "mode"}, {"tcspc", "overflow"},
      {"tcspc", "pixel", "clk", "divider"}, {"tcspc", "pixel", "clock"},
      {"tcspc", "routing", "x"}, {"tcspc", "routing", "y"},
      {"tcspc", "scan", "x"}, {"tcspc", "scan", "y"},
      {"tcspc", "sync", "divider"}, {"tcspc", "sync", "holdoff"},
      {"tcspc", "sync", "rate"}, {"tcspc", "sync", "threshold"},
      {"tcspc", "sync", "zc", "level"}, {"tcspc", "tac", "gain"},
      {"tcspc", "tac", "limit", "high"}, {"tcspc", "tac", "limit", "low"},
      {"tcspc", "tac", "offset"}, {"tcspc", "tac", "range"},
      {"tcspc", "time", "window"}, {"tcspc", "top", "border"},
      {"tcspc", "total", "frames"}, {"tcspc", "total", "time"},
      {"tcspc", "trigger"}, {"tcspc", "x", "sync", "polarity"},
      {"tcspc", "y", "sync", "polarity"}, {"text"}, {"time"}, {"title"},
      {"top"}, {"transmission"}, {"trigger"}, {"type"}, {"units"}, {"version"},
      {"wavelength\\*"}, {"x", "amplitude"}, {"y", "amplitude"},
      {"x", "delay"}, {"y", "delay"}, {"x", "offset"}, {"y", "offset"},
      {"z", "\\(background\\)"}};

    // key token value matching regexes within the "layout" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] LAYOUT_KEYS = {
      {"coordinates"}, // the full key is "layout coordinates"
      {"order"}, {"parameters"}, {"real_significant_bits"},
      {"significant_bits"}, {"significant_channels"}, {"sizes"}};

    // key token value matching regexes within the "parameter" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] PARAMETER_KEYS = {
      {"allowedlinemodes"}, // the full key is "parameter allowedlinemodes"
      {"ch"}, {"higher_limit"}, {"labels"}, {"lower_limit"}, {"origin"},
      {"range"}, {"sample_width", "ch"}, {"sample_width"}, {"scale"},
      {"units", "adc-units", "channels"}, {"units", "adc-units", "nm"},
      {"units"}};

    // key token value matching regexes within the "representation" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] REPRESENTATION_KEYS = { {"byte_order"}, // the full key is "representation byte_order"
      {"compression"}, {"format"}, {"sign"}};

    // key token value matching regexes within the "sensor" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] SENSOR_KEYS = {
      {"model"}, // the full key is "sensor model"
      {"s_params", "channels"}, {"s_params", "exphotoncnt"},
      {"s_params", "lambdaem"}, {"s_params", "lambdaex"},
      {"s_params", "numaperture"}, {"s_params", "pinholeradius"},
      {"s_params", "pinholespacing"},
      {"s_params", "refinxlensmedium"}, // sic; not found in sample files
      {"s_params", "refinxmedium"}, // sic; not found in sample files
      {"s_params", "refrinxlensmedium"}, {"s_params", "refrinxmedium"},
      {"type"}};

    // key token value matching regexes within the "view" category.
    //
    // this table is alphabetized for legibility only.
    //
    // however it is important that the most qualified regex list goes first,
    // e.g. { "a", "b" } must precede { "a" }.
    public static final String[][] VIEW_KEYS = {
      {"view", "color", "lib", "lut"}, // the full key is
      // "view view color lib lut"
      {"view", "color", "count"}, {"view", "color", "doc", "scale"},
      {"view", "color", "mode", "rgb", "set"},
      {"view", "color", "mode", "rgb"}, {"view", "color", "schemes"},
      {"view", "color", "view", "active"}, {"view", "color"},
      {"view\\d", "alpha"}, {"view\\d", "alphastate"},
      {"view\\d", "annotation", "annellipse"},
      {"view\\d", "annotation", "annpoint"}, {"view\\d", "autoresize"},
      {"view\\d", "axis"}, {"view\\d", "blacklevel"}, {"view\\d", "color"},
      {"view\\d", "cursor"}, {"view\\d", "dimviewoption"},
      {"view\\d", "gamma"}, {"view\\d", "ignoreaspect"},
      {"view\\d", "intzoom"}, {"view\\d", "live"}, {"view\\d", "order"},
      {"view\\d", "port"}, {"view\\d", "position"}, {"view\\d", "saturation"},
      {"view\\d", "scale"}, {"view\\d", "showall"}, {"view\\d", "showcursor"},
      {"view\\d", "showindex"}, {"view\\d", "size"},
      {"view\\d", "synchronize"}, {"view\\d", "tile"}, {"view\\d", "useunits"},
      {"view\\d", "zoom"}, {"view\\d"}, {"view"}};

    // These strings appeared in the former metadata field categories but are not
    // found in the LOCI sample files.
    //
    // The former metadata field categories table did not save the context, i.e.
    // the first token such as "document" or "history" and other intermediate
    // tokens.  The preceding tables such as DOCUMENT_KEYS or HISTORY_KEYS use
    // this full context.
    //
    // In an effort at backward compatibility, these will be used to form key
    // value pairs if key/value pair not already assigned and they match anywhere
    // in the input line.
    //
    public static String[][] OTHER_KEYS = { {"cube", "descriptio"}, // sic; also listed in HISTORY_KEYS
      {"cube", "description"}, // correction; also listed in HISTORY_KEYS
      {"image", "form"}, // also listed in HISTORY_KEYS
      {"refinxlensmedium"}, // Could be a mispelling of "refrinxlensmedium";
      // also listed in SENSOR_KEYS
      {"refinxmedium"}, // Could be a mispelling of "refinxmedium";
      // also listed in SENSOR_KEYS
      {"scil_type"}, {"source"}};

    /** Metadata field categories. */
    public static final String[] CATEGORIES = new String[] {
      "ics_version", "filename", "source", "layout", "representation",
      "parameter", "sensor", "history", "document", "view.*", "end", "file",
      "offset", "parameters", "order", "sizes", "coordinates",
      "significant_bits", "format", "sign", "compression", "byte_order",
      "origin", "scale", "units", "t", "labels", "SCIL_TYPE", "type", "model",
      "s_params", "gain.*", "dwell", "shutter.*", "pinhole", "laser.*",
      "version", "objective", "PassCount", "step.*", "date", "GMTdate",
      "label", "software", "author", "length", "Z (background)", "dimensions",
      "rep period", "image form", "extents", "offsets", "region",
      "expon. order", "a.*", "tau.*", "noiseval", "excitationfwhm",
      "created on", "text", "other text", "mode", "CFD limit low",
      "CFD limit high", "CFD zc level", "CFD holdoff", "SYNC zc level",
      "SYNC freq div", "SYNC holdoff", "TAC range", "TAC gain", "TAC offset",
      "TAC limit low", "ADC resolution", "Ext latch delay", "collection time",
      "repeat time", "stop on time", "stop on O'flow", "dither range",
      "count increment", "memory bank", "sync threshold", "dead time comp",
      "polarity", "line compressio", "scan flyback", "scan borders",
      "pixel time", "pixel clock", "trigger", "scan pixels x", "scan pixels y",
      "routing chan x", "routing chan y", "detector type", "channel.*",
      "filter.*", "wavelength.*", "black.level.*", "ht.*", "scan resolution",
      "scan speed", "scan zoom", "scan pattern", "scan pos x", "scan pos y",
      "transmission", "x amplitude", "y amplitude", "x offset", "y offset",
      "x delay", "y delay", "beam zoom", "mirror .*", "direct turret",
      "desc exc turret", "desc emm turret", "cube", "Stage_XYZum",
      "cube descriptio", "camera", "exposure", "bits/pixel", "binning", "left",
      "top", "cols", "rows", "significant_channels", "allowedlinemodes",
      "real_significant_bits", "sample_width", "range", "ch", "lower_limit",
      "higher_limit", "passcount", "detector", "dateGMT", "RefrInxMedium",
      "RefrInxLensMedium", "Channels", "PinholeRadius", "LambdaEx", "LambdaEm",
      "ExPhotonCnt", "RefInxMedium", "NumAperture", "RefInxLensMedium",
      "PinholeSpacing", "power", "name", "Type", "Magnification", "NA",
      "WorkingDistance", "Immersion", "Pinhole", "Channel .*", "Gain .*",
      "Shutter .*", "Position", "Size", "Port", "Cursor", "Color",
      "BlackLevel", "Saturation", "Gamma", "IntZoom", "Live", "Synchronize",
      "ShowIndex", "AutoResize", "UseUnits", "Zoom", "IgnoreAspect",
      "ShowCursor", "ShowAll", "Axis", "Order", "Tile", "DimViewOption",
      "channels", "pinholeradius", "refrinxmedium", "numaperture",
      "refrinxlensmedium", "image", "microscope", "stage", "filterset",
      "dichroic", "exc", "emm", "manufacturer", "experiment", "experimenter",
      "study", "metadata", "format", "icsviewer", "illumination",
      "exposure_time", "model", "ver", "creation", "date", "bpp", "bigendian",
      "size.", "physical_size.", "controller", "firmware", "pos", "position.",
      "mag", "na", "nm", "lamp", "built", "on", "rep", "rate", "Exposure",
    "Wavelength\\*"};

    // -- Helper Methods --

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
    private String[] tokenize(final String line) {
      final List<String> tokens = new ArrayList<String>();
      boolean inWhiteSpace = true;
      boolean withinQuotes = false;
      StringBuffer token = null;
      for (int i = 0; i < line.length(); ++i) {
        final char c = line.charAt(i);
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
    String[] findKeyValue(final String[] tokens, final String[][] regexesArray) {
      String[] keyValue = findKeyValueForCategory(tokens, regexesArray);
      if (null == keyValue) {
        keyValue = findKeyValueOther(tokens, OTHER_KEYS);
      }
      if (null == keyValue) {
        final String key = tokens[0];
        final String value = concatenateTokens(tokens, 1, tokens.length);
        keyValue = new String[] {key, value};
      }
      return keyValue;
    }

    /*
     * Builds a string from a list of tokens.
     */
    private String concatenateTokens(final String[] tokens, final int start,
      final int stop)
    {
      final StringBuffer returnValue = new StringBuffer();
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
    private String[] findKeyValueForCategory(final String[] tokens,
      final String[][] regexesArray)
    {
      String[] keyValue = null;
      int index = 0;
      for (final String[] regexes : regexesArray) {
        if (compareTokens(tokens, 1, regexes, 0)) {
          final int splitIndex = 1 + regexes.length; // add one for the category
          final String key = concatenateTokens(tokens, 0, splitIndex);
          final String value =
            concatenateTokens(tokens, splitIndex, tokens.length);
          keyValue = new String[] {key, value};
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
    private String[] findKeyValueOther(final String[] tokens,
      final String[][] regexesArray)
    {
      String[] keyValue = null;
      for (final String[] regexes : regexesArray) {
        for (int i = 1; i < tokens.length - regexes.length; ++i) {
          // does token match first regex?
          if (tokens[i].toLowerCase().matches(regexes[0])) {
            // do remaining tokens match remaining regexes?
            if (1 == regexes.length || compareTokens(tokens, i + 1, regexes, 1)) {
              // if so, return key/value
              final int splitIndex = i + regexes.length;
              final String key = concatenateTokens(tokens, 0, splitIndex);
              final String value =
                concatenateTokens(tokens, splitIndex, tokens.length);
              keyValue = new String[] {key, value};
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
    private boolean compareTokens(final String[] tokens, final int tokenIndex,
      final String[] regexes, final int regexesIndex)
    {
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
    private Double[] splitDoubles(final String v) {
      final StringTokenizer t = new StringTokenizer(v);
      final Double[] values = new Double[t.countTokens()];
      for (int n = 0; n < values.length; n++) {
        final String token = t.nextToken().trim();
        try {
          values[n] = new Double(token);
        }
        catch (final NumberFormatException e) {
          LOGGER.debug("Could not parse double value '{}'", token, e);
        }
      }
      return values;
    }

    /** Verifies that a unit matches the expected value. */
    private boolean checkUnit(final String actual, final String... expected) {
      if (actual == null || actual.equals("")) return true; // undefined is OK
      for (final String exp : expected) {
        if (actual.equals(exp)) return true; // unit matches expected value
      }
      //LOGGER.debug("Unexpected unit '{}'; expected '{}'", actual, expected);
      return false;
    }
  }

  /**
   * SCIFIO file format Translator for ICS Metadata objects to the SCIFIO Core
   * metadata type.
   * 
   */
  @SCIFIOTranslator(metaIn = CoreMetadata.class, metaOut = Metadata.class)
  public static class CoreICSTranslator
  extends AbstractTranslator<CoreMetadata, Metadata>
  implements CoreTranslator {
  
    // -- Constructors --
  
    public CoreICSTranslator() {
      this(null);
    }
  
    public CoreICSTranslator(final SCIFIO ctx) {
      super(ctx);
    }
  
    // -- Translator API Methods --
  
    @Override
    public void translate(final CoreMetadata source, final Metadata destination)
    {
      super.translate(source, destination);
      // note that the destination fields will preserve their default values
      // only the keyValPairs will be modified
  
      Hashtable<String, String> keyValPairs = null;
      if (destination == null || destination.getKeyValPairs() == null) keyValPairs =
        new Hashtable<String, String>();
      else 
        keyValPairs = destination.getKeyValPairs();
  
      final int numAxes = source.getAxisCount(0);
  
      String order = "";
      String sizes = "";
  
      for (int i = 0; i < numAxes; i++) {
        final AxisType axis = source.getAxisType(0, i);
  
        if (axis.equals(Axes.X)) {
          order += "x";
        }
        else if (axis.equals(Axes.Y)) {
          order += "y";
        }
        else if (axis.equals(Axes.Z)) {
          order += "z";
        }
        else if (axis.equals(Axes.TIME)) {
          order += "t";
        }
        else if (axis.equals(Axes.CHANNEL)) {
          order += "c";
        }
        else if (axis.equals(Axes.PHASE)) {
          order += "p";
        }
        else if (axis.equals(Axes.FREQUENCY)) {
          order += "f";
        }
        else {
          if(axis.getLabel().equals("bits"))
            order += "bits";
          else
            order += "u";
        }
  
        order += " ";
        sizes += source.getAxisLength(0, i) + " ";
      }
  
      keyValPairs.put("layout sizes", sizes);
      keyValPairs.put("layout order", order);
  
      keyValPairs.put("layout significant_bits", "" + source.getBitsPerPixel(0));
  
      if(source.getChannelDimTypes(0).equals(FormatTools.LIFETIME))
        keyValPairs.put("history type", "time resolved");
  
      boolean signed = false;
      boolean fPoint = false;
  
      switch (source.getPixelType(0)) {
        case FormatTools.INT8:
        case FormatTools.INT16:
        case FormatTools.INT32:
          signed = true;
          break;
        case FormatTools.UINT8:
        case FormatTools.UINT16:
        case FormatTools.UINT32:
          break;
        case FormatTools.FLOAT:
        case FormatTools.DOUBLE:
          fPoint = true;
          signed = true;
          break;
  
      }
  
      keyValPairs.put("representation sign", signed ? "signed" : "");
      keyValPairs.put("representation format", fPoint ? "real" : "");
      keyValPairs.put("representation compression", "");
  
      String byteOrder = "0";
  
      if(source.isLittleEndian(0))
        byteOrder = fPoint ? "1" : "0";
      else
        byteOrder = fPoint ? "0" : "1";
  
      keyValPairs.put("representation byte_order", byteOrder);
  
      destination.setKeyValPairs(keyValPairs);
    }
  
  }

  /**
   * SCIFIO file format Translator for ICS Metadata objects
   * to the SCIFIO Core metadata type. 
   *
   */
  @SCIFIOTranslator(metaIn = Metadata.class, metaOut = CoreMetadata.class)
  public static class ICSCoreTranslator
  extends AbstractTranslator<Metadata, CoreMetadata>
  implements CoreTranslator {

    private Metadata curSource;

    // -- Constructors --

    public ICSCoreTranslator() {
      this(null);
    }

    public ICSCoreTranslator(final SCIFIO ctx) {
      super(ctx);
    }

    // -- Translator API Methods --

    @Override
    public void translate(final Metadata source, final CoreMetadata destination)
    {
      super.translate(source, destination);
      final CoreImageMetadata coreMeta = new CoreImageMetadata();
      destination.add(coreMeta);
      final int index = destination.getImageCount() - 1;

      curSource = source;

      coreMeta.setRgb(false);

      // find axis sizes

      double[] axesSizes = source.getAxesSizes();
      String[] axes = source.getAxes();
      
      AxisType[] axisTypes = null;
      int[] axisLengths = null;

      
      int bitsPerPixel = 0;

      axisLengths = new int[axesSizes.length];

      for (int n = 0; n < axisLengths.length; n++) {
        axisLengths[n] = new Double(axesSizes[n]).intValue();
      }

      axisTypes = new AxisType[axes.length];

      final Vector<Integer> channelLengths = new Vector<Integer>();
      final Vector<String> channelTypes = new Vector<String>();

      for (int n = 0; n < axisTypes.length; n++) {
        final String axis = axes[n].toLowerCase();
        if (axis.equals("x")) {
          axisTypes[n] = Axes.X;
        }
        else if (axis.equals("y")) {
          axisTypes[n] = Axes.Y;
        }
        else if (axis.equals("z")) {
          axisTypes[n] = Axes.Z;
        }
        else if (axis.equals("t")) {
          axisTypes[n] = Axes.TIME;
        }
        else if (axis.startsWith("c")) {
          axisTypes[n] = Axes.CHANNEL;
          channelTypes.add(FormatTools.CHANNEL);
          channelLengths.add(axisLengths[n]);
        }
        else if (axis.startsWith("p")) {
          axisTypes[n] = Axes.PHASE;
          channelTypes.add(FormatTools.PHASE);
          channelLengths.add(axisLengths[n]);
        }
        else if (axis.startsWith("f")) {
          axisTypes[n] = Axes.FREQUENCY;
          channelTypes.add(FormatTools.FREQUENCY);
          channelLengths.add(axisLengths[n]);
        }
        else if (axis.equals("bits")) {
          axisTypes[n] = new CustomAxisType("bits");
          bitsPerPixel = axisLengths[n];
          while (bitsPerPixel % 8 != 0)
            bitsPerPixel++;
          if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
        }
        else {
          axisTypes[n] = Axes.UNKNOWN;
          channelTypes.add("");
          channelLengths.add(axisLengths[n]);
        }
      }

      if(source.getBitsPerPixel() != null)
        bitsPerPixel = source.getBitsPerPixel();
      
      destination.setBitsPerPixel(index, bitsPerPixel);

      coreMeta.setAxisLengths(axisLengths);
      coreMeta.setAxisTypes(axisTypes);
      //storedRGB = getSizeX() == 0;

      if (channelLengths.size() == 0) {
        channelLengths.add(new Integer(1));
        channelTypes.add(FormatTools.CHANNEL);
      }

      final int[] cLengths = new int[channelLengths.size()];
      final String[] cTypes = new String[channelLengths.size()];

      for (int i = 0; i < channelLengths.size(); i++) {
        cLengths[i] = channelLengths.get(i);
        cTypes[i] = channelTypes.get(i);
      }

      coreMeta.setcLengths(cLengths);
      coreMeta.setcTypes(cTypes);

      if (destination.getAxisIndex(index, Axes.Z) == -1) {
        destination.addAxis(index, Axes.Z, 1);
      }
      if (destination.getAxisIndex(index, Axes.CHANNEL) == -1) {
        destination.addAxis(index, Axes.CHANNEL, 1);
      }
      if (destination.getAxisIndex(index, Axes.TIME) == -1) {
        destination.addAxis(index, Axes.TIME, 1);
      }

      if (destination.getAxisLength(index, Axes.Z) == 0)
        coreMeta.setAxisLength(Axes.Z, 1);
      if (destination.getAxisLength(index, Axes.CHANNEL) == 0)
        coreMeta.setAxisLength(Axes.CHANNEL, 1);
      if (destination.getAxisLength(index, Axes.TIME) == 0)
        coreMeta.setAxisLength(Axes.TIME, 1);

      coreMeta.setPlaneCount(destination.getAxisLength(0, Axes.Z) *
        destination.getAxisLength(0, Axes.TIME));

      coreMeta.setInterleaved(destination.isRGB(0));
      //TODO coreMeta.imageCount = getSizeZ() * getSizeT();
      //TODO if (destination.isRGB(0)) coreMeta.imageCount *= getSizeC();
      destination.setIndexed(index, false);
      destination.setFalseColor(index, false);
      destination.setMetadataComplete(index, true);
      destination.setLittleEndian(index, true);

      boolean lifetime = source.getLifetime();

      // HACK - support for Gray Institute at Oxford's ICS lifetime data
      if (lifetime) {
        final int binCount = destination.getAxisLength(index, Axes.Z);
        coreMeta.setAxisLength(
          Axes.Z, (destination.getAxisLength(index, Axes.CHANNEL)));
        coreMeta.setAxisLength(Axes.CHANNEL, binCount);
        coreMeta.setcLengths(new int[] {binCount});
        coreMeta.setcTypes(new String[] {FormatTools.LIFETIME});
        final int cIndex = destination.getAxisIndex(index, Axes.CHANNEL);
        final int zIndex = destination.getAxisIndex(index, Axes.Z);
        coreMeta.setAxisType(cIndex, Axes.Z);
        coreMeta.setAxisType(zIndex, Axes.CHANNEL);
      }

      final String byteOrder = source.getByteOrder();
      final String rFormat = source.getRepFormat();
      final String compression = source.getCompression();

      if (byteOrder != null) {
        final String firstByte = byteOrder.split(" ")[0];
        final int first = Integer.parseInt(firstByte);
        coreMeta.setLittleEndian(rFormat.equals("real") ? first == 1 : first != 1);
      }

      final boolean gzip =
        (compression == null) ? false : compression.equals("gzip");

      final int bytes = bitsPerPixel / 8;

      if (bitsPerPixel < 32)
        coreMeta.setLittleEndian(!destination.isLittleEndian(0));

      final boolean floatingPt = rFormat.equals("real");
      final boolean signed = source.isSigned();

      try {
        coreMeta.setPixelType(FormatTools.pixelTypeFromBytes(bytes, signed, floatingPt));
      }
      catch (final FormatException e) {
        LOGGER.error("Could not get pixel type from bytes: " + bytes, e);
      }

    }

    // -- Helper Methods --

    StringTokenizer getTknz(final String target) {
      return new StringTokenizer(curSource.get(target));
    }
  }
}
