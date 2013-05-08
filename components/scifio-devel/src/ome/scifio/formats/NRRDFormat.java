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

package ome.scifio.formats;

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;
import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.DefaultMetadataOptions;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.ImageMetadata;
import ome.scifio.MetadataLevel;
import ome.scifio.UnsupportedCompressionException;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

import org.scijava.plugin.Plugin;

/**
 * File format reader for NRRD files; see http://teem.sourceforge.net/nrrd.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/NRRDReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/NRRDReader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type = NRRDFormat.class)
public class NRRDFormat extends AbstractFormat {

  // -- Format API Methods --
  
  /*
   * @see ome.scifio.Format#getFormatName()
   */
  public String getFormatName() {
    return "NRRD";
  }

  /*
   * @see ome.scifio.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[] {"nrrd", "nhdr"};
  }

  // -- Nested classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {
    
    // -- Constants --
    
    public static final String CNAME = "ome.scifio.formats.NRRDFormat$Metadata";

    // -- Fields --

    /** Name of data file, if the current extension is 'nhdr'. */
    private String dataFile;

    /** Data encoding. */
    private String encoding;

    /** Offset to pixel data. */
    private long offset;

    /** Helper format for reading pixel data. */
    private ome.scifio.Reader helper;
    
    private String[] pixelSizes;

    private boolean lookForCompanion = true;
    private boolean initializeHelper = false;
    
    // -- NRRDMetadata getters and setters --

    public void setHelper(ome.scifio.Reader reader) {
      helper = reader;
    }
    
    public ome.scifio.Reader getHelper() {
      return helper;
    }
    
    public String getDataFile() {
      return dataFile;
    }

    public void setDataFile(String dataFile) {
      this.dataFile = dataFile;
    }

    public String getEncoding() {
      return encoding;
    }

    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    public long getOffset() {
      return offset;
    }

    public void setOffset(long offset) {
      this.offset = offset;
    }

    public String[] getPixelSizes() {
      return pixelSizes;
    }

    public void setPixelSizes(String[] pixelSizes) {
      this.pixelSizes = pixelSizes;
    }

    public boolean isLookForCompanion() {
      return lookForCompanion;
    }

    public void setLookForCompanion(boolean lookForCompanion) {
      this.lookForCompanion = lookForCompanion;
    }

    public boolean isInitializeHelper() {
      return initializeHelper;
    }

    public void setInitializeHelper(boolean initializeHelper) {
      this.initializeHelper = initializeHelper;
    }
    
    // -- Metadata API methods --
    
    /*
     * @see ome.scifio.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      ImageMetadata iMeta = get(0);
      
      iMeta.setRGB(iMeta.getAxisLength(Axes.CHANNEL) > 1);
      iMeta.setInterleaved(true);
      iMeta.setPlaneCount(iMeta.getAxisLength(Axes.Z) * iMeta.getAxisLength(Axes.TIME));
      iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getPixelType()));
      iMeta.setIndexed(false);
      iMeta.setFalseColor(false);
      iMeta.setMetadataComplete(true);
    }
    
    /* @see loci.formats.IFormatReader#close(boolean) */
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        dataFile = encoding = null;
        offset = 0;
        pixelSizes = null;
        initializeHelper = false;
        helper = null;
      }
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Checker extends AbstractChecker {
    
    // -- Constants --
    
    public static final String NRRD_MAGIC_STRING = "NRRD";

    // -- Checker API Methods --
    
    @Override
    public boolean isFormat(String name, boolean open) {
      if (super.isFormat(name, open)) return true;
      if (!open) return false;

      // look for a matching .nhdr file
      Location header = new Location(getContext(), name + ".nhdr");
      if (header.exists()) {
        return true;
      }

      if (name.indexOf(".") >= 0) {
        name = name.substring(0, name.lastIndexOf("."));
      }

      header = new Location(getContext(), name + ".nhdr");
      return header.exists();
    }
    
    @Override
    public boolean isFormat(RandomAccessInputStream stream) throws IOException {
      final int blockLen = NRRD_MAGIC_STRING.length();
      if (!FormatTools.validStream(stream, blockLen, false)) return false;
      return stream.readString(blockLen).startsWith(NRRD_MAGIC_STRING);
    } 
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    // -- Parser API Methods --
    
    @Override
    public String[] getImageUsedFiles(int imageIndex, boolean noPixels) {
      FormatTools.assertId(currentId, true, 1);
      if (noPixels) {
        if (metadata.getDataFile() == null) return null;
        return new String[] {currentId};
      }
      if (metadata.getDataFile() == null) return new String[] {currentId};
      return new String[] {currentId, metadata.getDataFile()};
    }
    
    // -- Abstract Parser API Methods --
 
    public Metadata parse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException 
    {
      String id = stream.getFileName();
      
      // make sure we actually have the .nrrd/.nhdr file
      if (!FormatTools.checkSuffix(id, "nhdr") && !FormatTools.checkSuffix(id, "nrrd")) {
        id += ".nhdr";

        if (!new Location(getContext(), id).exists()) {
          id = id.substring(0, id.lastIndexOf("."));
          id = id.substring(0, id.lastIndexOf("."));
          id += ".nhdr";
        }
        id = new Location(getContext(), id).getAbsolutePath();
      }
      stream.close();
      
      stream = new RandomAccessInputStream(getContext(), id);

      return super.parse(stream, meta);
    }
    
    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
      throws IOException, FormatException
    {
      String key, v;

      int numDimensions = 0;

      meta.createImageMetadata(1);
      ImageMetadata iMeta = meta.get(0);
      
      iMeta.setAxisLength(Axes.X, 1);
      iMeta.setAxisLength(Axes.Y, 1);
      iMeta.setAxisLength(Axes.Z, 1);
      iMeta.setAxisLength(Axes.CHANNEL, 1);
      iMeta.setAxisLength(Axes.TIME, 1);

      String line = in.readLine();
      while (line != null && line.length() > 0) {
        if (!line.startsWith("#") && !line.startsWith("NRRD")) {
          // parse key/value pair
          key = line.substring(0, line.indexOf(":")).trim();
          v = line.substring(line.indexOf(":") + 1).trim();
          addGlobalMeta(key, v);

          if (key.equals("type")) {
            if (v.indexOf("char") != -1 || v.indexOf("8") != -1) {
              iMeta.setPixelType(FormatTools.UINT8);
            }
            else if (v.indexOf("short") != -1 || v.indexOf("16") != -1) {
              iMeta.setPixelType(FormatTools.UINT16);
            }
            else if (v.equals("int") || v.equals("signed int") ||
              v.equals("int32") || v.equals("int32_t") || v.equals("uint") ||
              v.equals("unsigned int") || v.equals("uint32") ||
              v.equals("uint32_t"))
            {
              iMeta.setPixelType(FormatTools.UINT32);
            }
            else if (v.equals("float")) iMeta.setPixelType(FormatTools.FLOAT);
            else if (v.equals("double")) iMeta.setPixelType(FormatTools.DOUBLE);
            else throw new FormatException("Unsupported data type: " + v);
          }
          else if (key.equals("dimension")) {
            numDimensions = Integer.parseInt(v);
          }
          else if (key.equals("sizes")) {
            String[] tokens = v.split(" ");
            for (int i=0; i<numDimensions; i++) {
              int size = Integer.parseInt(tokens[i]);

              if (numDimensions >= 3 && i == 0 && size > 1 && size <= 16) {
                iMeta.setAxisLength(Axes.CHANNEL, size);
              }
              else if (i == 0 || (iMeta.getAxisLength(Axes.CHANNEL) > 1 && i == 1)) {
                iMeta.setAxisLength(Axes.X, size);
              }
              else if (i == 1 || (iMeta.getAxisLength(Axes.CHANNEL) > 1 && i == 2)) {
                iMeta.setAxisLength(Axes.Y, size);
              }
              else if (i == 2 || (iMeta.getAxisLength(Axes.CHANNEL) > 1 && i == 3)) {
                iMeta.setAxisLength(Axes.Z, size);
              }
              else if (i == 3 || (iMeta.getAxisLength(Axes.CHANNEL) > 1 && i == 4)) {
                iMeta.setAxisLength(Axes.TIME, size);
              }
            }
          }
          else if (key.equals("data file") || key.equals("datafile")) {
            meta.setDataFile(v);
          }
          else if (key.equals("encoding")) meta.setEncoding(v);
          else if (key.equals("endian")) {
            iMeta.setLittleEndian(v.equals("little"));
          }
          else if (key.equals("spacings")) {
            meta.setPixelSizes(v.split(" "));
          }
          else if (key.equals("byte skip")) {
            meta.setOffset(Long.parseLong(v));
          }
        }

        line = in.readLine();
        if (line != null) line = line.trim();
      }

      // nrrd files store pixel data in addition to metadata
      // nhdr files don't store pixel data, but instead provide a path to the
      //   pixels file (this can be any format)

      if (meta.getDataFile() == null) meta.setOffset(stream.getFilePointer());
      else {
        Location f = new Location(getContext(), currentId).getAbsoluteFile();
        Location parent = f.getParentFile();
        if (f.exists() && parent != null) {
          String dataFile = meta.getDataFile();
          dataFile = dataFile.substring(dataFile.indexOf(File.separator) + 1);
          dataFile = new Location(getContext(), parent, dataFile).getAbsolutePath();
        }
        meta.setInitializeHelper(!meta.getEncoding().equals("raw"));
      }
      
      
      if (meta.isInitializeHelper()) {
        // Find the highest priority non-NRRD format that can support the current
        // image and cache it as a helper
        NRRDFormat nrrd = scifio().format().getFormatFromClass(NRRDFormat.class);
        scifio().format().removeFormat(nrrd);

        Format helperFormat = scifio().format().getFormat(meta.getDataFile());
        ome.scifio.Parser p = helperFormat.createParser();
        p.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.MINIMUM));
        p.setMetadataOptions(meta.getMetadataOptions());
        ome.scifio.Reader helper = helperFormat.createReader();
        helper.setMetadata(p.parse(meta.getDataFile()));
        helper.setSource(meta.getDataFile());
        meta.setHelper(helper);

        scifio().format().addFormat(nrrd); 
      }
    }
    
    // -- Groupable API Methods --
    
    @Override
    public boolean hasCompanionFiles() {
      return true;
    }
    
    @Override
    public boolean isSingleFile(String id) throws FormatException, IOException {
      return FormatTools.checkSuffix(id, "nrrd");
    }
    
    @Override
    public int fileGroupOption(String id) throws FormatException, IOException {
      return FormatTools.MUST_GROUP;
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {
    
    // -- Constructor --
    
    public Reader() {
      domains = new String[] {FormatTools.UNKNOWN_DOMAIN};
    }
    
    // -- Groupable API Methods --
    
    @Override
    public boolean hasCompanionFiles() {
      return true;
    }
    
    @Override
    public boolean isSingleFile(String id) throws FormatException, IOException {
      return FormatTools.checkSuffix(id, "nrrd");
    }
    
    @Override
    public int fileGroupOption(String id) throws FormatException, IOException {
      return FormatTools.MUST_GROUP;
    }
    
    // -- Reader API Methods --
    
    @Override
    public int getOptimalTileHeight(int imageIndex) {
      return getMetadata().getAxisLength(imageIndex, Axes.Y);
    }
    
    /*
     * @see ome.scifio.TypedReader#openPlane(int, int, ome.scifio.DataPlane, int, int, int, int)
     */
    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
      ByteArrayPlane plane, int x, int y, int w, int h) 
      throws FormatException, IOException
    {
      byte[] buf = plane.getData();
      Metadata meta = getMetadata();
      
      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);


      // TODO : add support for additional encoding types
      if (meta.getDataFile() == null) {
        if (meta.getEncoding().equals("raw")) {
          long planeSize = FormatTools.getPlaneSize(this, imageIndex);
          getStream().seek(meta.getOffset() + planeIndex * planeSize);

          readPlane(getStream(), imageIndex, x, y, w, h, plane);
          return plane;
        }
        else {
          throw new UnsupportedCompressionException(
            "Unsupported encoding: " + meta.getEncoding());
        }
      }
      else if (meta.getEncoding().equals("raw")) {
        RandomAccessInputStream s = new RandomAccessInputStream(getContext(), meta.getDataFile());
        s.seek(meta.getOffset() + planeIndex * FormatTools.getPlaneSize(this, imageIndex));
        readPlane(s, imageIndex, x, y, w, h, plane);
        s.close();
        return plane;
      }
      
      // open the data file using our helper format
      if (meta.isInitializeHelper() && meta.getDataFile() != null &&
          meta.getHelper() != null)
      { 
        meta.getHelper().openPlane(imageIndex, planeIndex, plane, x, y, w, h);
        return plane;
      }
      
      throw new FormatException("Could not find a supporting Format");
    }
    
  }
}