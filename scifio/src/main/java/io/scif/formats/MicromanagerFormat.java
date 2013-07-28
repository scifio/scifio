/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.formats;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultTranslator;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Translator;
import io.scif.common.DataTools;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.xml.BaseHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * MicromanagerReader is the file format reader for Micro-Manager files.
 *
 * @author Mark Hiner hinerm at gmail.com
 *
 */
@Plugin(type = MicromanagerFormat.class)
public class MicromanagerFormat extends AbstractFormat {

  // -- Constants --

  /** File containing extra metadata. */
  private static final String METADATA = "metadata.txt";

  /**
   * Optional file containing additional acquisition parameters.
   * (And yes, the spelling is correct.)
   */
  private static final String XML = "Acqusition.xml";

  // -- Format API Methods --

  /*
   * @see io.scif.Format#getFormatName()
   */
  public String getFormatName() {
    return "Micro-Manager";
  }

  /*
   * @see io.scif.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[] {"tif", "tiff", "txt", "xml"};
  }

  // -- Nested Classes --

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {

    // -- Constants --

    public static final String CNAME = "io.scif.formats.MicromanagerFormat$Metadata";

    // -- Fields --

    private Vector<Position> positions;

    // -- MicromanagerMetadata getters and setters --

    public Vector<Position> getPositions() {
      return positions;
    }

    public void setPositions(Vector<Position> positions) {
      this.positions = positions;
    }

    // -- Metadata API methods --

    public void populateImageMetadata() {

      for (int i=0; i<getImageCount(); i++) {
        ImageMetadata ms = get(i);

        if (ms.getAxisLength(Axes.Z) == 0) ms.setAxisLength(Axes.Z, 1);
        if (ms.getAxisLength(Axes.TIME) == 0) ms.setAxisLength(Axes.TIME, 1);

        ms.setAxisTypes(FormatTools.findDimensionList("XYZCT"));
        ms.setInterleaved(false);
        ms.setRGB(false);
        ms.setLittleEndian(false);
        ms.setPlaneCount(ms.getAxisLength(Axes.Z) * ms.getAxisLength(Axes.CHANNEL) * ms.getAxisLength(Axes.TIME));
        ms.setIndexed(false);
        ms.setFalseColor(false);
        ms.setMetadataComplete(true);
        ms.setBitsPerPixel(FormatTools.getBitsPerPixel(ms.getPixelType()));
      }
    }

    @Override
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        positions = null;
      }
    }

  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {

    // -- Checker API Methods --

    /*
     * @see io.scif.Checker#isFormat(io.scif.io.RandomAccessInputStream)
     */
    public boolean isFormat(String name, boolean open) {
      if (!open) return false; // not allowed to touch the file system
      if (name.equals(METADATA) || name.endsWith(File.separator + METADATA) ||
        name.equals(XML) || name.endsWith(File.separator + XML))
      {
        final int blockSize = 1048576;
        try {
          RandomAccessInputStream stream = new RandomAccessInputStream(getContext(), name);
          long length = stream.length();
          String data = stream.readString((int) Math.min(blockSize, length));
          stream.close();
          return length > 0 && (data.indexOf("Micro-Manager") >= 0 ||
            data.indexOf("micromanager") >= 0);
        }
        catch (IOException e) {
          return false;
        }
      }
      try {
        Location parent = new Location(getContext(), name).getAbsoluteFile().getParentFile();
        Location metaFile = new Location(getContext(), parent, METADATA);
        RandomAccessInputStream s = new RandomAccessInputStream(getContext(), name);
        boolean validTIFF = isFormat(s);
        s.close();
        return validTIFF && isFormat(metaFile.getAbsolutePath(), open);
      }
      catch (NullPointerException e) { }
      catch (IOException e) { }
      return false;
    }

    @Override
    public boolean isFormat(RandomAccessInputStream stream) throws IOException
    {
      io.scif.Checker checker;
      try {
        checker = scifio().format().getFormatFromClass(MinimalTIFFFormat.class).createChecker();
        return checker.isFormat(stream);
      } catch (FormatException e) {
        log().error("Failed to create a MinimalTIFFChecker", e);
        return false;
      }
    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Constants --

    public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    // -- MicromanagerParser API methods --

    public void populateMetadataStore(String[] jsonData, Metadata source, io.scif.Metadata dest)
      throws FormatException, IOException
    {
      String currentId = "in-memory-json";
      source.createImageMetadata(jsonData.length);
      Vector<Position> positions = new Vector<Position>();
      for (int pos=0; pos<jsonData.length; pos++) {
        Position p = new Position();
        p.metadataFile = "Position #" + (pos + 1);
        positions.add(p);
        parsePosition(jsonData[pos], source, pos);
      }

      scifio().translator().translate(source, dest, true);
    }

    // -- Parser API methods --

    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
        throws IOException, FormatException {
      Vector<Position> positions = new Vector<Position>();
      meta.setPositions(positions);

      log().info("Reading metadata file");

      // find metadata.txt

      Location file = new Location(getContext(), stream.getFileName()).getAbsoluteFile();
      Location parentFile = file.getParentFile();
      String metadataFile = METADATA;
      if (file.exists()) {
        metadataFile = new Location(getContext(), parentFile, METADATA).getAbsolutePath();

        // look for other positions

        if (parentFile.getName().indexOf("Pos_") >= 0) {
          parentFile = parentFile.getParentFile();
          String[] dirs = parentFile.list(true);
          Arrays.sort(dirs);
          for (String dir : dirs) {
            if (dir.indexOf("Pos_") >= 0) {
              Position pos = new Position();
              Location posDir = new Location(getContext(), parentFile, dir);
              pos.metadataFile = new Location(getContext(), posDir, METADATA).getAbsolutePath();
              positions.add(pos);
            }
          }
        }
        else {
          Position pos = new Position();
          pos.metadataFile = metadataFile;
          positions.add(pos);
        }
      }

      int imageCount = positions.size();
      meta.createImageMetadata(imageCount);

      for (int i=0; i<imageCount; i++) {
        parsePosition(meta, i);
      }
    }

    public String[] getImgaeUsedFiles(int imageIndex, boolean noPixels) {
      FormatTools.assertId(currentId, true, 1);
      Vector<String> files = new Vector<String>();
      for (Position pos : metadata.getPositions()) {
        files.add(pos.metadataFile);
        if (pos.xmlFile != null) {
          files.add(pos.xmlFile);
        }
        if (!noPixels) {
          for (String tiff : pos.tiffs) {
            if (new Location(getContext(), tiff).exists()) {
              files.add(tiff);
            }
          }
        }
      }
      return files.toArray(new String[files.size()]);
    }

    // -- Groupable API Methods --

    @Override
    public boolean isSingleFile(String id) {
      return false;
    }

    @Override
    public int fileGroupOption(String id) {
      return FormatTools.MUST_GROUP;
    }

    @Override
    public boolean hasCompanionFiles() {
      return true;
    }

    // -- Helper methods --

    private void parsePosition(Metadata meta, int posIndex) throws IOException, FormatException {
      Position p = meta.getPositions().get(posIndex);
      String s = DataTools.readFile(getContext(), p.metadataFile);
      parsePosition(s, meta, posIndex);

      buildTIFFList(meta, posIndex);
    }

    private void buildTIFFList(Metadata meta, int posIndex) throws FormatException {
      Position p = meta.getPositions().get(posIndex);
      ImageMetadata ms = meta.get(posIndex);
      String parent = new Location(getContext(), p.metadataFile).getParent();

      log().info("Finding image file names");

      // find the name of a TIFF file
      p.tiffs = new Vector<String>();

      // build list of TIFF files

      buildTIFFList(meta, posIndex, parent + File.separator + p.baseTiff);

      if (p.tiffs.size() == 0) {
        Vector<String> uniqueZ = new Vector<String>();
        Vector<String> uniqueC = new Vector<String>();
        Vector<String> uniqueT = new Vector<String>();

        Location dir =
          new Location(getContext(), p.metadataFile).getAbsoluteFile().getParentFile();
        String[] files = dir.list(true);
        Arrays.sort(files);
        for (String f : files) {
          if (FormatTools.checkSuffix(f, "tif") || FormatTools.checkSuffix(f, "tiff")) {
            String[] blocks = f.split("_");
            if (!uniqueT.contains(blocks[1])) uniqueT.add(blocks[1]);
            if (!uniqueC.contains(blocks[2])) uniqueC.add(blocks[2]);
            if (!uniqueZ.contains(blocks[3])) uniqueZ.add(blocks[3]);

            p.tiffs.add(new Location(getContext(), dir, f).getAbsolutePath());
          }
        }

        ms.setAxisLength(Axes.Z, uniqueZ.size());
        ms.setAxisLength(Axes.CHANNEL, uniqueC.size());
        ms.setAxisLength(Axes.TIME, uniqueT.size());

        if (p.tiffs.size() == 0) {
          throw new FormatException("Could not find TIFF files.");
        }
      }
    }

    private void parsePosition(String jsonData, Metadata meta, int posIndex)
      throws IOException, FormatException
    {
      Position p = meta.getPositions().get(posIndex);
      ImageMetadata ms = meta.get(posIndex);
      String parent = new Location(getContext(), p.metadataFile).getParent();

      // now parse the rest of the metadata

      // metadata.txt looks something like this:
      //
      // {
      //   "Section Name": {
      //      "Key": "Value",
      //      "Array key": [
      //        first array value, second array value
      //      ]
      //   }
      //
      // }

      log().info("Populating metadata");

      Vector<Double> stamps = new Vector<Double>();
      p.voltage = new Vector<Double>();

      StringTokenizer st = new StringTokenizer(jsonData, "\n");
      int[] slice = new int[3];
      while (st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        boolean open = token.indexOf("[") != -1;
        boolean closed = token.indexOf("]") != -1;
        if (open || (!open && !closed && !token.equals("{") &&
          !token.startsWith("}")))
        {
          int quote = token.indexOf("\"") + 1;
          String key = token.substring(quote, token.indexOf("\"", quote));
          String value = null;

          if (open == closed) {
            value = token.substring(token.indexOf(":") + 1);
          }
          else if (!closed) {
            StringBuffer valueBuffer = new StringBuffer();
            while (!closed) {
              token = st.nextToken();
              closed = token.indexOf("]") != -1;
              valueBuffer.append(token);
            }
            value = valueBuffer.toString();
            value = value.replaceAll("\n", "");
          }
          if (value == null) continue;

          int startIndex = value.indexOf("[");
          int endIndex = value.indexOf("]");
          if (endIndex == -1) endIndex = value.length();

          value = value.substring(startIndex + 1, endIndex).trim();
          if (value.length() == 0) {
            continue;
          }
          value = value.substring(0, value.length() - 1);
          value = value.replaceAll("\"", "");
          if (value.endsWith(",")) value = value.substring(0, value.length() - 1);
          meta.getTable().put(key, value);
          if (key.equals("Channels")) {
            ms.setAxisLength(Axes.CHANNEL, Integer.parseInt(value));
          }
          else if (key.equals("ChNames")) {
            p.channels = value.split(",");
            for (int q=0; q<p.channels.length; q++) {
              p.channels[q] = p.channels[q].replaceAll("\"", "").trim();
            }
          }
          else if (key.equals("Frames")) {
            ms.setAxisLength(Axes.TIME, Integer.parseInt(value));
          }
          else if (key.equals("Slices")) {
            ms.setAxisLength(Axes.Z, Integer.parseInt(value));
          }
          else if (key.equals("PixelSize_um")) {
            p.pixelSize = new Double(value);
          }
          else if (key.equals("z-step_um")) {
            p.sliceThickness = new Double(value);
          }
          else if (key.equals("Time")) {
            p.time = value;
          }
          else if (key.equals("Comment")) {
            p.comment = value;
          }
          else if (key.equals("FileName")) {
            p.fileNameMap.put(new Index(slice), value);
            if (p.baseTiff == null) {
              p.baseTiff = value;
            }
          }
          else if (key.equals("Width")) {
            ms.setAxisLength(Axes.X, Integer.parseInt(value));
          }
          else if (key.equals("Height")) {
            ms.setAxisLength(Axes.Y, Integer.parseInt(value));
          }
          else if (key.equals("IJType")) {
            int type = Integer.parseInt(value);

            switch (type) {
              case 0:
                ms.setPixelType(FormatTools.UINT8);
                break;
              case 1:
                ms.setPixelType(FormatTools.UINT16);
                break;
              default:
                throw new FormatException("Unknown type: " + type);
            }
          }
        }

        if (token.startsWith("\"FrameKey")) {
          int dash = token.indexOf("-") + 1;
          int nextDash = token.indexOf("-", dash);
          slice[2] = Integer.parseInt(token.substring(dash, nextDash));
          dash = nextDash + 1;
          nextDash = token.indexOf("-", dash);
          slice[1] = Integer.parseInt(token.substring(dash, nextDash));
          dash = nextDash + 1;
          slice[0] = Integer.parseInt(token.substring(dash,
            token.indexOf("\"", dash)));

          token = st.nextToken().trim();
          String key = "", value = "";
          boolean valueArray = false;
    int nestedCount = 0;

          while (!token.startsWith("}") || nestedCount > 0) {

      if (token.trim().endsWith("{")) {
          nestedCount++;
          token = st.nextToken().trim();
          continue;
      }
      else if (token.trim().startsWith("}")) {
          nestedCount--;
          token = st.nextToken().trim();
          continue;
      }

            if (valueArray) {
              if (token.trim().equals("],")) {
                valueArray = false;
              }
              else {
                value += token.trim().replaceAll("\"", "");
                token = st.nextToken().trim();
                continue;
              }
            }
            else {
              int colon = token.indexOf(":");
              key = token.substring(1, colon).trim();
              value = token.substring(colon + 1, token.length() - 1).trim();

              key = key.replaceAll("\"", "");
              value = value.replaceAll("\"", "");

              if (token.trim().endsWith("[")) {
                valueArray = true;
                token = st.nextToken().trim();
                continue;
              }
            }

            meta.getTable().put(key, value);

            if (key.equals("Exposure-ms")) {
              double t = Double.parseDouble(value);
              p.exposureTime = new Double(t / 1000);
            }
            else if (key.equals("ElapsedTime-ms")) {
              double t = Double.parseDouble(value);
              stamps.add(new Double(t / 1000));
            }
            else if (key.equals("Core-Camera")) p.cameraRef = value;
            else if (key.equals(p.cameraRef + "-Binning")) {
              if (value.indexOf("x") != -1) p.binning = value;
              else p.binning = value + "x" + value;
            }
            else if (key.equals(p.cameraRef + "-CameraID")) p.detectorID = value;
            else if (key.equals(p.cameraRef + "-CameraName")) {
              p.detectorModel = value;
            }
            else if (key.equals(p.cameraRef + "-Gain")) {
              p.gain = (int) Double.parseDouble(value);
            }
            else if (key.equals(p.cameraRef + "-Name")) {
              p.detectorManufacturer = value;
            }
            else if (key.equals(p.cameraRef + "-Temperature")) {
              p.temperature = Double.parseDouble(value);
            }
            else if (key.equals(p.cameraRef + "-CCDMode")) {
              p.cameraMode = value;
            }
            else if (key.startsWith("DAC-") && key.endsWith("-Volts")) {
              p.voltage.add(new Double(value));
            }
            else if (key.equals("FileName")) {
              p.fileNameMap.put(new Index(slice), value);
              if (p.baseTiff == null) {
                p.baseTiff = value;
              }
            }

            token = st.nextToken().trim();
          }
        }
      }

      p.timestamps = stamps.toArray(new Double[stamps.size()]);
      Arrays.sort(p.timestamps);

      // look for the optional companion XML file

      if (new Location(getContext(), parent, XML).exists()) {
        p.xmlFile = new Location(getContext(), parent, XML).getAbsolutePath();
        parseXMLFile(meta, posIndex);
      }
    }

    /**
     * Populate the list of TIFF files using the given file name as a pattern.
     */
    private void buildTIFFList(Metadata meta, int posIndex, String baseTiff) {
      log().info("Building list of TIFFs");
      Position p = meta.getPositions().get(posIndex);
      String prefix = "";
      if (baseTiff.indexOf(File.separator) != -1) {
        prefix = baseTiff.substring(0, baseTiff.lastIndexOf(File.separator) + 1);
        baseTiff = baseTiff.substring(baseTiff.lastIndexOf(File.separator) + 1);
      }

      String[] blocks = baseTiff.split("_");
      StringBuffer filename = new StringBuffer();
      for (int t=0; t<meta.getAxisLength(posIndex, Axes.TIME); t++) {
        for (int c=0; c<meta.getAxisLength(posIndex, Axes.CHANNEL); c++) {
          for (int z=0; z<meta.getAxisLength(posIndex, Axes.Z); z++) {
            // file names are of format:
            // img_<T>_<channel name>_<T>.tif
            filename.append(prefix);
            if (!prefix.endsWith(File.separator) &&
              !blocks[0].startsWith(File.separator))
            {
              filename.append(File.separator);
            }
            filename.append(blocks[0]);
            filename.append("_");

            int zeros = blocks[1].length() - String.valueOf(t).length();
            for (int q=0; q<zeros; q++) {
              filename.append("0");
            }
            filename.append(t);
            filename.append("_");

            String channel = p.channels[c];
            if (channel.indexOf("-") != -1) {
              channel = channel.substring(0, channel.indexOf("-"));
            }
            filename.append(channel);
            filename.append("_");

            zeros = blocks[3].length() - String.valueOf(z).length() - 4;
            for (int q=0; q<zeros; q++) {
              filename.append("0");
            }
            filename.append(z);
            filename.append(".tif");

            p.tiffs.add(filename.toString());
            filename.delete(0, filename.length());
          }
        }
      }
    }

    /** Parse metadata values from the Acqusition.xml file. */
    private void parseXMLFile(Metadata meta, int imageIndex) throws IOException {
      Position p = meta.getPositions().get(imageIndex);
      String xmlData = DataTools.readFile(getContext(), p.xmlFile);
      xmlData = scifio().xml().sanitizeXML(xmlData);

      DefaultHandler handler = new MicromanagerHandler();
      scifio().xml().parseXML(xmlData, handler);
    }

    // -- Helper classes --

    /** SAX handler for parsing Acqusition.xml. */
    private class MicromanagerHandler extends BaseHandler {
      public MicromanagerHandler() {
        super(log());
      }
      public void startElement(String uri, String localName, String qName,
        Attributes attributes)
      {
        if (qName.equals("entry")) {
          String key = attributes.getValue("key");
          String value = attributes.getValue("value");

          metadata.getTable().put(key, value);
        }
      }
    }

  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {

    // -- Fields --

    /** Helper reader for TIFF files. */
    private MinimalTIFFFormat.Reader<?> tiffReader;

    // -- Constructor --

    public Reader() {
      domains = new String[] {FormatTools.LM_DOMAIN};
    }

    // -- Reader API methods --

    /*
     * @see io.scif.Reader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
        ByteArrayPlane plane, int x, int y, int w, int h)
        throws FormatException, IOException {

      Metadata meta = getMetadata();
      byte[] buf = plane.getBytes();
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);

      String file = meta.getPositions().get(imageIndex).getFile(meta, imageIndex, planeIndex);

      if (file != null && new Location(getContext(), file).exists()) {
        tiffReader.setSource(file);
        return tiffReader.openPlane(imageIndex, 0, plane, x, y, w, h);
      }
      log().warn("File for image #" + planeIndex +
        " (" + file + ") is missing.");
      return plane;
    }

    @Override
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (tiffReader != null) tiffReader.close(fileOnly);
    }

    @Override
    public int getOptimalTileWidth(int imageIndex) {
      if (tiffReader == null || tiffReader.getCurrentFile() == null) {
        setupReader(imageIndex);
      }
      return tiffReader.getOptimalTileWidth(imageIndex);
    }

    @Override
    public int getOptimalTileHeight(int imageIndex) {
      if (tiffReader == null || tiffReader.getCurrentFile() == null) {
        setupReader(imageIndex);
      }
      return tiffReader.getOptimalTileHeight(imageIndex);
    }

    // -- Groupable API Methods --

    @Override
    public boolean hasCompanionFiles() {
      return true;
    }

    // -- Helper methods --

    private void setupReader(int imageIndex) {
      try {
        String file = getMetadata().getPositions().get(imageIndex).getFile(getMetadata(), imageIndex, 0);

        if (tiffReader == null) tiffReader =
            (MinimalTIFFFormat.Reader<?>) scifio().format().getFormatFromClass(MinimalTIFFFormat.class).createReader();

        tiffReader.setSource(file);
      }
      catch (Exception e) {
        log().debug("", e);
      }
    }

  }

  /**
   * Necessary dummy translator, so that a Micromanager-OMEXML translator can be used
   * 
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  @Plugin(type = Translator.class, attrs =
    {@Attr(name = MicromanagerTranslator.SOURCE, value = io.scif.Metadata.CNAME),
     @Attr(name = MicromanagerTranslator.DEST, value = Metadata.CNAME)},
    priority = Priority.LOW_PRIORITY)
  public static class MicromanagerTranslator
    extends DefaultTranslator
  { }

  // -- Helper classes --

  public static class Position {
    public String baseTiff;
    public Vector<String> tiffs;
    public HashMap<Index, String> fileNameMap = new HashMap<Index, String>();

    public String metadataFile;
    public String xmlFile;

    public String[] channels;

    public String comment, time;
    public Double exposureTime, sliceThickness, pixelSize;
    public Double[] timestamps;

    public int gain;
    public String binning, detectorID, detectorModel, detectorManufacturer;
    public double temperature;
    public Vector<Double> voltage;
    public String cameraRef;
    public String cameraMode;

    public String getFile(Metadata meta, int imageIndex, int planeIndex) {
      int[] zct = FormatTools.getZCTCoords(meta, imageIndex, planeIndex);
      for (Index key : fileNameMap.keySet()) {
        if (key.z == zct[0] && key.c == zct[1] && key.t == zct[2]) {
          String file = fileNameMap.get(key);

          if (tiffs != null) {
            for (String tiff : tiffs) {
              if (tiff.endsWith(File.separator + file)) {
                return tiff;
              }
            }
          }
        }
      }
      return fileNameMap.size() == 0 ? tiffs.get(planeIndex) : null;
    }
  }

  private static class Index {
    public int z;
    public int c;
    public int t;

    public Index(int[] zct) {
      z = zct[0];
      c = zct[1];
      t = zct[2];
    }
  }
}
