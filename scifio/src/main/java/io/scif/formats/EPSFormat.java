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

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.DefaultImageMetadata;
import io.scif.DefaultTranslator;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.formats.tiff.IFD;
import io.scif.formats.tiff.IFDList;
import io.scif.formats.tiff.TiffParser;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Reader is the file format reader for Encapsulated PostScript (EPS) files.
 * Some regular PostScript files are also supported.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/EPSReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/EPSReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 * @author Mark Hiner
 */
@Plugin(type = EPSFormat.class)
public class EPSFormat extends AbstractFormat {

  // -- Format API Methods --

  public String getFormatName() {
    return "Encapsulated PostScript";
  }

  public String[] getSuffixes() {
    return new String[] {"eps", "epsi", "ps"};
  }

  // -- Nested classes --

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {
    // -- Constants --

    public static final String CNAME = "io.scif.formats.EPSFormat$Metadata";

    // -- Fields --

    /** Starting line of pixel data. */
    private int start;

    /** Flag indicating binary data. */
    private boolean binary;

    private boolean isTiff;
    private IFDList ifds;

    // -- Constructor --

    public Metadata() {
      super();
      add(new DefaultImageMetadata());
      setLittleEndian(0, true);
    }

    // -- Field accessors and setters

    public IFDList getIfds() {
      return ifds;
    }

    public void setIfds(IFDList ifds) {
      this.ifds = ifds;
    }

    public int getStart() {
      return start;
    }

    public void setStart(int start) {
      this.start = start;
    }

    public boolean isBinary() {
      return binary;
    }

    public void setBinary(boolean binary) {
      this.binary = binary;
    }

    public boolean isTiff() {
      return isTiff;
    }

    public void setTiff(boolean isTiff) {
      this.isTiff = isTiff;
    }

    // -- Metadata API Methods --

    public void populateImageMetadata() {
      if (getAxisLength(0, Axes.CHANNEL) == 0) setAxisLength(0, Axes.CHANNEL, 1);

      setAxisLength(0, Axes.Z, 1);
      setAxisLength(0, Axes.TIME, 1);

      if (getPixelType(0) == 0) setPixelType(0, FormatTools.UINT8);

      setBitsPerPixel(0, FormatTools.getBitsPerPixel(getPixelType(0)));

      setRGB(0, getAxisLength(0, Axes.CHANNEL) == 3);
      setInterleaved(0, true);
      get(0).setPlaneCount(1);
    }

    // -- HasSource API Methods --

    /*
     * @see io.scif.AbstractMetadata#close(boolean)
     */
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);

      if (!fileOnly) {
        isTiff = false;
        ifds = null;
        start = 0;
        binary = false;
      }
    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Parser extends AbstractParser<Metadata> {

    @Override
    protected void typedParse(RandomAccessInputStream stream, Metadata meta)
        throws IOException, FormatException
    {
      meta.createImageMetadata(1);

      ImageMetadata m = meta.get(0);

      log().info("Verifying EPS format");

      String line = in.readLine();
      if (!line.trim().startsWith("%!PS")) {
        // read the TIFF preview

        meta.setTiff(true);

        in.order(true);
        in.seek(20);
        int offset = in.readInt();
        int len = in.readInt();

        byte[] b = new byte[len];
        in.seek(offset);
        in.read(b);

        in = new RandomAccessInputStream(getContext(), b);
        TiffParser tp = new TiffParser(getContext(), in);
        meta.setIfds(tp.getIFDs());

        IFD firstIFD = meta.getIfds().get(0);

        m.setAxisLength(Axes.X, (int)firstIFD.getImageWidth());
        m.setAxisLength(Axes.Y, (int)firstIFD.getImageLength());
        m.setAxisLength(Axes.CHANNEL, firstIFD.getSamplesPerPixel());
        m.setAxisLength(Axes.Z, 1);
        m.setAxisLength(Axes.TIME, 1);

        if (m.getAxisLength(Axes.CHANNEL) == 2)
          m.setAxisLength(Axes.CHANNEL, 4);

        m.setLittleEndian(firstIFD.isLittleEndian());
        m.setInterleaved(true);
        m.setRGB(m.getAxisLength(Axes.CHANNEL) > 1);
        m.setPixelType(firstIFD.getPixelType());
        m.setPlaneCount(1);
        m.setMetadataComplete(true);
        m.setIndexed(false);
        m.setFalseColor(false);

        return;
      }

      log().info("Finding image data");

      meta.setBinary(false);

      String image = "image";
      int lineNum = 1;

      line = in.readLine().trim();

      m.setAxisTypes(new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL});

      while (line != null && !line.equals("%%EOF")) {
        if (line.endsWith(image)) {
          if (!line.startsWith(image)) {
            if (line.indexOf("colorimage") != -1) m.setAxisLength(Axes.CHANNEL, 3);
            String[] t = line.split(" ");
            try {
              m.setAxisLength(Axes.X, Integer.parseInt(t[0]));
              m.setAxisLength(Axes.Y, Integer.parseInt(t[1]));
            }
            catch (NumberFormatException exc) {
              log().debug("Could not parse image dimensions", exc);
              m.setAxisLength(Axes.CHANNEL, Integer.parseInt(t[3]));
            }
          }

          meta.setStart(lineNum);
          break;
        }
        else if (line.startsWith("%%")) {
          if (line.startsWith("%%BoundingBox:")) {
            line = line.substring(14).trim();
            String[] t = line.split(" ");
            try {
              int originX = Integer.parseInt(t[0].trim());
              int originY = Integer.parseInt(t[1].trim());
              m.setAxisLength(Axes.X, Integer.parseInt(t[2].trim()) - originY);
              m.setAxisLength(Axes.Y, Integer.parseInt(t[3].trim()) - originY);

              addGlobalMeta("X-coordinate of origin", originX);
              addGlobalMeta("Y-coordinate of origin", originY);
            }
            catch (NumberFormatException e) {
              throw new FormatException(
                "Files without image data are not supported.");
            }
          }
          else if (line.startsWith("%%BeginBinary")) {
            meta.setBinary(true);
          }
          else {
            // parse key/value pairs

            int ndx = line.indexOf(":");
            if (ndx != -1) {
              String key = line.substring(0, ndx);
              String value = line.substring(ndx + 1);
              addGlobalMeta(key, value);
            }
          }
        }
        else if (line.startsWith("%ImageData:")) {
          line = line.substring(11);
          String[] t = line.split(" ");

          m.setAxisLength(Axes.X, Integer.parseInt(t[0]));
          m.setAxisLength(Axes.Y, Integer.parseInt(t[1]));
          m.setAxisLength(Axes.CHANNEL, Integer.parseInt(t[3]));
          for (int i=4; i<t.length; i++) {
            image = t[i].trim();
            if (image.length() > 1) {
              image = image.substring(1, image.length() - 1);
            }
          }
        }
        lineNum++;
        line = in.readLine().trim();
      }

    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Reader extends ByteArrayReader<Metadata> {

    // -- Constructor --

    public Reader() {
      domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
    }

    // -- Reader API Methods --

    public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
        ByteArrayPlane plane, int x, int y, int w, int h)
        throws FormatException, IOException
    {
      byte[] buf = plane.getData();
      Metadata meta = getMetadata();

      FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, buf.length, x, y, w, h);

      if (meta.isTiff()) {
        long[] offsets = meta.getIfds().get(0).getStripOffsets();
        getStream().seek(offsets[0]);

        int[] map = meta.getIfds().get(0).getIFDIntArray(IFD.COLOR_MAP);
        if (map == null) {
          readPlane(getStream(), imageIndex, x, y, w, h, plane);
          return plane;
        }

        byte[] b = new byte[w * h];
        getStream().skipBytes(2 * y * meta.getAxisLength(imageIndex, Axes.X));
        for (int row=0; row<h; row++) {
          getStream().skipBytes(x * 2);
          for (int col=0; col<w; col++) {
            b[row * w + col] = (byte) (getStream().readShort() & 0xff);
          }
          getStream().skipBytes(2 * (meta.getAxisLength(imageIndex, Axes.X) - w - x));
        }

        for (int i=0; i<b.length; i++) {
          int ndx = b[i] & 0xff;
          for (int j=0; j<meta.getAxisLength(imageIndex, Axes.CHANNEL); j++) {
            if (j < 3) {
              buf[i*meta.getAxisLength(imageIndex, Axes.CHANNEL) + j] = (byte) map[ndx + j*256];
            }
            else {
              boolean zero =
                map[ndx] == 0 && map[ndx + 256] == 0 && map[ndx + 512] == 0;
              buf[i * meta.getAxisLength(imageIndex, Axes.CHANNEL) + j] = zero ? (byte) 0 : (byte) 255;
            }
          }
        }

        return plane;
      }

      if (meta.getStart() == 0) {
        throw new FormatException("Vector data not supported.");
      }

      getStream().seek(0);
      for (int line=0; line<=meta.getStart(); line++) {
        getStream().readLine();
      }

      int bytes = FormatTools.getBytesPerPixel(meta.getPixelType(imageIndex));
      if (meta.isBinary()) {
        // pixels are stored as raw bytes
        readPlane(getStream(), imageIndex, x, y, w, h, plane);
      }
      else {
        // pixels are stored as a 2 character hexadecimal value
        String pix = getStream().readString((int) (getStream().length() - getStream().getFilePointer()));
        pix = pix.replaceAll("\n", "");
        pix = pix.replaceAll("\r", "");

        int ndx = meta.getAxisLength(imageIndex, Axes.CHANNEL) * y* bytes *
          meta.getAxisLength(imageIndex, Axes.X);
        int destNdx = 0;

        for (int row=0; row<h; row++) {
          ndx += x * meta.getAxisLength(imageIndex, Axes.CHANNEL) * bytes;
          for (int col=0; col<w*meta.getAxisLength(imageIndex, Axes.CHANNEL)*bytes; col++) {
            buf[destNdx++] =
              (byte) Integer.parseInt(pix.substring(2*ndx, 2*(ndx+1)), 16);
            ndx++;
          }
          ndx += meta.getAxisLength(imageIndex, Axes.CHANNEL) * bytes *
              (meta.getAxisLength(imageIndex, Axes.X) - w - x);
        }
      }
      return plane;
    }

    public int getOptimalTileWidth(int imageIndex) {
      try {
        if (getMetadata().isTiff) {
          return (int) getMetadata().getIfds().get(0).getTileWidth();
        }
      }
      catch (FormatException e) {
        log().debug("Could not retrieve tile width", e);
      }
      return super.getOptimalTileWidth(imageIndex);
    }

    public int getOptimalTileHeight(int imageIndex) {
      try {
        if (getMetadata().isTiff()) {
          return (int) getMetadata().getIfds().get(0).getTileLength();
        }
      }
      catch (FormatException e) {
        log().debug("Could not retrieve tile height", e);
      }
      return super.getOptimalTileHeight(imageIndex);
    }
  }

  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Writer extends AbstractWriter<Metadata> {

    // -- Constants --

    private static final String DUMMY_PIXEL = "00";

    // -- Fields --

    private long planeOffset = 0;

    // -- Writer API Methods --

    public void savePlane(int imageIndex, int planeIndex, Plane plane, int x,
        int y, int w, int h) throws FormatException, IOException
    {

      byte[] buf = plane.getBytes();
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);

      int sizeX = getMetadata().getAxisLength(imageIndex, Axes.X);
      int nChannels = getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);

      // write pixel data
      // for simplicity, write 80 char lines

      if (!initialized[imageIndex][planeIndex]) {
        initialized[imageIndex][planeIndex] = true;

        writeHeader(imageIndex);

        if (!isFullPlane(imageIndex, x, y, w, h)) {
          // write a dummy plane that will be overwritten in sections
          int planeSize = w * h * nChannels;
          for (int i=0; i<planeSize; i++) {
            out.writeBytes(DUMMY_PIXEL);
          }
        }
      }

      int planeSize = w * h;

      StringBuffer buffer = new StringBuffer();

      int offset = y * sizeX * nChannels * 2;
      out.seek(planeOffset + offset);
      for (int row=0; row<h; row++) {
        out.skipBytes(nChannels * x * 2);
        for (int col=0; col<w*nChannels; col++) {
          int i = row * w * nChannels + col;
          int index = interleaved || nChannels == 1 ? i :
            (i % nChannels) * planeSize + (i / nChannels);
          String s = Integer.toHexString(buf[index]);
          // only want last 2 characters of s
          if (s.length() > 1) buffer.append(s.substring(s.length() - 2));
          else {
            buffer.append("0");
            buffer.append(s);
          }
        }
        out.writeBytes(buffer.toString());
        buffer.delete(0, buffer.length());
        out.skipBytes(nChannels * (sizeX - w - x) * 2);
      }

      // write footer

      out.seek(out.length());
      out.writeBytes("\nshowpage\n");
    }

    public int[] getPixelTypes(String codec) {
      return new int[] {FormatTools.UINT8};
    }

    // -- Helper methods --

    private void writeHeader(int imageIndex) throws IOException {
      int width = getMetadata().getAxisLength(imageIndex, Axes.X);
      int height = getMetadata().getAxisLength(imageIndex, Axes.Y);
      int nChannels = getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);

      out.writeBytes("%!PS-Adobe-2.0 EPSF-1.2\n");
      out.writeBytes("%%Title: " + getMetadata().getDatasetName() + "\n");
      out.writeBytes("%%Creator: OME Bio-Formats\n");
      out.writeBytes("%%Pages: 1\n");
      out.writeBytes("%%BoundingBox: 0 0 " + width + " " + height + "\n");
      out.writeBytes("%%EndComments\n\n");

      out.writeBytes("/ld {load def} bind def\n");
      out.writeBytes("/s /stroke ld /f /fill ld /m /moveto ld /l " +
          "/lineto ld /c /curveto ld /rgb {255 div 3 1 roll 255 div 3 1 " +
          "roll 255 div 3 1 roll setrgbcolor} def\n");
      out.writeBytes("0 0 translate\n");
      out.writeBytes(((float) width) + " " + ((float) height) + " scale\n");
      out.writeBytes("/picstr 40 string def\n");
      out.writeBytes(width + " " + height + " 8 [" + width + " 0 0 " +
          (-1 * height) + " 0 " + height +
          "] {currentfile picstr readhexstring pop} ");
      if (nChannels == 1) {
        out.writeBytes("image\n");
      }
      else {
        out.writeBytes("false 3 colorimage\n");
      }
      planeOffset = out.getFilePointer();
    }
  }

  /**
   * Necessary dummy translator, so that an EPS-OMEXML translator can be used
   * 
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  @Plugin(type = Translator.class, attrs =
    {@Attr(name = EPSTranslator.SOURCE, value = io.scif.Metadata.CNAME),
     @Attr(name = EPSTranslator.DEST, value = Metadata.CNAME)},
    priority = Priority.LOW_PRIORITY)
  public static class EPSTranslator
    extends DefaultTranslator
  { }
}
