package ome.scifio.ics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import net.imglib2.meta.Axes;

import ome.scifio.AbstractReader;
import ome.scifio.CoreMetadata;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * File format SCIFIO Reader for Image Cytometry Standard (ICS)
 * images. Version 1 and 2 supported.
 * 
 */
public class ICSReader extends AbstractReader<ICSMetadata> {

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

  public ICSReader() {
    this(null);
  }

  public ICSReader(final SCIFIO ctx) {
    super("Image Cytometry Standard", new String[] {"ics", "ids"}, ctx);
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

    final int[] coordinates = getZCTCoords(planeIndex);
    final int[] prevCoordinates = getZCTCoords(prevImage);

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
  public void setMetadata(final ICSMetadata meta) throws IOException {
    super.setMetadata(meta);
    this.metadata = meta;
    final Translator<ICSMetadata, CoreMetadata> t = new ICSCoreTranslator();
    t.translate(meta, cMeta);
    gzip = metadata.get("representation compression").equals("gzip");
  }
  
  /* @see Reader#setSource(RandomAccessInputStream) */
  @Override
  public void setSource(final RandomAccessInputStream stream) throws IOException {
	  super.setSource(stream);
	  if(!this.getMetadata().versionTwo)
		  this.in = new RandomAccessInputStream(this.getMetadata().idsId);
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
