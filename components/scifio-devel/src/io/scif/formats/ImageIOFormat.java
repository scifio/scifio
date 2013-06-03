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

package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * ImageIOReader is the superclass for file format readers
 * that use the javax.imageio package.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/ImageIOReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/ImageIOReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public abstract class ImageIOFormat extends AbstractFormat {

  // -- Nested classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Metadata extends AbstractMetadata {
    
    // -- Constants --
    
    public static final String CNAME = "io.scif.formats.ImageIOFormat$Metadata";
    
    // -- Fields --

    private BufferedImage img;
    
    // -- ImageIOMetadata API methods --

    public BufferedImage getImg() {
      return img;
    }

    public void setImg(BufferedImage img) {
      this.img = img;
    }
    
    // -- Metadata API Methods --
    
    /*
     * @see io.scif.Metadata#populateImageMetadata()
     */
    public void populateImageMetadata() {
      ImageMetadata iMeta = get(0);
      
      if (img != null) {
        iMeta.setAxisLength(Axes.X, img.getWidth());
        iMeta.setAxisLength(Axes.Y, img.getHeight());
        iMeta.setRGB(img.getRaster().getNumBands() > 1);
        iMeta.setPixelType(AWTImageTools.getPixelType(img));
      }

      iMeta.setAxisLength(Axes.CHANNEL, iMeta.isRGB() ? 3 : 1);
      iMeta.setAxisLength(Axes.Z, 1);
      iMeta.setAxisLength(Axes.TIME, 1);
      iMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(iMeta.getBitsPerPixel()));
      iMeta.setInterleaved(false);
      iMeta.setLittleEndian(false);
      iMeta.setMetadataComplete(true);
      iMeta.setIndexed(false);
      iMeta.setFalseColor(false);
      iMeta.setPlaneCount(1);
    }
    
    /* @see loci.formats.IFormatReader#close(boolean) */
    public void close(boolean fileOnly) throws IOException {
      super.close(fileOnly);
      if (!fileOnly) {
        img = null;
      }
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   * @param <M>
   */
  public static class Parser<M extends Metadata> extends AbstractParser<M> {

    @Override
    protected void typedParse(RandomAccessInputStream stream, M meta)
      throws IOException, FormatException {
      LOGGER.info("Populating metadata");
      DataInputStream dis = new DataInputStream(stream);
      BufferedImage img = ImageIO.read(dis);
      dis.close();
      if (img == null) throw new FormatException("Invalid image stream");
      meta.setImg(img);
      meta.createImageMetadata(1);
    }
  }
  
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   * @param <M>
   */
  public static class Reader<M extends Metadata> extends BufferedImageReader<M> {
    
    // -- Constructor --
    
    public Reader() {
      domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
    }

    // -- Reader API methods --
    
    /*
     * @see io.scif.TypedReader#openPlane(int, int, io.scif.DataPlane, int, int, int, int)
     */
    public BufferedImagePlane openPlane(int imageIndex, int planeIndex,
      BufferedImagePlane plane, int x, int y, int w, int h)
      throws FormatException, IOException
    {
      Metadata meta = getMetadata();
      plane.setData(AWTImageTools.getSubimage(meta.getImg(), meta.isLittleEndian(imageIndex), x, y, w, h));
      return plane;
    }
    
    @Override
    public int getOptimalTileHeight(int imageIndex) {
      return getMetadata().getAxisLength(imageIndex, Axes.Y);
    }
  }
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   * @param <M>
   */
  public static class Writer<M extends Metadata> extends AbstractWriter<M> {

    // -- Fields --

    protected String kind;
    
    // -- Constructors --
    
    public Writer(String kind) {
      this.kind = kind;
    }
    
    /*
     * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
     */
    public void savePlane(int imageIndex, int planeIndex, Plane plane, int x,
      int y, int w, int h) throws FormatException, IOException
    {
      if (!isFullPlane(imageIndex, x, y, w, h)) {
        throw new FormatException("ImageIOWriter does not support writing tiles");
      }

      BufferedImage img = null;
      Metadata meta = getMetadata();
      
      if (!(plane instanceof BufferedImagePlane)) {
        int type = meta.getPixelType(imageIndex);
        img = AWTImageTools.makeImage(plane.getBytes(), meta.getAxisLength(imageIndex, Axes.X),
            meta.getAxisLength(0, Axes.Y), meta.getRGBChannelCount(imageIndex),
            meta.isInterleaved(imageIndex), FormatTools.getBytesPerPixel(type),
            FormatTools.isFloatingPoint(type), meta.isLittleEndian(imageIndex),
            FormatTools.isSigned(type));
      }
      else {
        img = ((BufferedImagePlane)plane).getData();
      }
      
      ImageIO.write(img, kind, out);
    }
    
    @Override
    public int[] getPixelTypes(String codec) {
      return new int[] {FormatTools.UINT8, FormatTools.UINT16};
    }
  }
  
  @Plugin(type = Translator.class, attrs = 
    {@Attr(name = ImageIOTranslator.SOURCE, value = io.scif.Metadata.CNAME),
     @Attr(name = ImageIOTranslator.DEST, value = Metadata.CNAME)},
    priority = Priority.LOW_PRIORITY)
  public static class ImageIOTranslator
    extends AbstractTranslator<io.scif.Metadata, Metadata>
  {

    @Override
    protected void typedTranslate(io.scif.Metadata source, Metadata dest) {
      dest.createImageMetadata(1);
      dest.setAxisLength(0, Axes.X, source.getAxisLength(0, Axes.X));
      dest.setAxisLength(0, Axes.Y, source.getAxisLength(0, Axes.Y));
      dest.setRGB(0, source.isRGB(0));
      dest.setPixelType(0, source.getPixelType(0));
    }
  }
}
