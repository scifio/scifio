package ome.scifio.apng;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import net.imglib2.meta.Axes;

import ome.scifio.common.Constants;
import ome.scifio.CoreMetadata;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.common.DataTools;
import ome.scifio.gui.AWTImageTools;
import ome.scifio.in.BIFormatReader;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * File format SCIFIO Reader for Animated Portable Network Graphics (APNG)
 * images.
 * 
 */
public class APNGReader extends BIFormatReader<APNGMetadata> {

  // -- Fields --

  // Cached copy of the last plane that was returned.
  private BufferedImage lastPlane;

  // Plane index of the last plane that was returned.
  private int lastPlaneIndex = -1;

  // -- Constructor --

  /** Constructs a new APNGReader. */

  public APNGReader() {
    this(null);
  }

  public APNGReader(final SCIFIO ctx) {
    super("Animated PNG", "png", ctx);
  }

  // -- Reader API Methods --

  /* @see ome.scifio.Reader#openPlane(int, int, int, int, int) */
  @Override
  public Object openPlane(final int imageIndex, final int planeIndex, final int x, final int y, final int w,
    final int h) throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(
      this, imageIndex, planeIndex, -1, x, y, w, h);

    // If the last processed (cached) plane is requested, return it
    if (planeIndex == lastPlaneIndex && lastPlane != null) {
      return AWTImageTools.getSubimage(
        lastPlane, cMeta.isLittleEndian(planeIndex), x, y, w, h);
    }

    // The default frame is requested and we can use the standard Java ImageIO to extract it
    if (planeIndex == 0) {
      in.seek(0);
      final DataInputStream dis =
        new DataInputStream(new BufferedInputStream(in, 4096));
      lastPlane = ImageIO.read(dis);
      lastPlaneIndex = 0;
      if (x == 0 && y == 0 && w == cMeta.getAxisLength(imageIndex, Axes.X) &&
        h == cMeta.getAxisLength(imageIndex, Axes.Y))
      {
        return lastPlane;
      }
      return AWTImageTools.getSubimage(
        lastPlane, cMeta.isLittleEndian(imageIndex), x, y, w, h);
    }

    // For a non-default frame, the appropriate chunks will be used to create a new image,
    // which will be read with the standard Java ImageIO and pasted onto frame 0.
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.write(APNGChunk.PNG_SIGNATURE);

    final int[] coords = metadata.getFctl().get(planeIndex).getFrameCoordinates();
    // process IHDR chunk
    final APNGIHDRChunk ihdr = metadata.getIhdr();
    processChunk(
      imageIndex, ihdr.getLength(), ihdr.getOffset(), coords, stream, true);

    // process fcTL and fdAT chunks
    final APNGfcTLChunk fctl =
      metadata.getFctl().get(
        metadata.isSeparateDefault() ? planeIndex - 1 : planeIndex);

    // fdAT chunks are converted to IDAT chunks, as we are essentially building a standalone single-frame image
    for (final APNGfdATChunk fdat : fctl.getFdatChunks()) {
      in.seek(fdat.getOffset() + 4);
      byte[] b = new byte[fdat.getLength() + 8];
      DataTools.unpackBytes(
        fdat.getLength() - 4, b, 0, 4, cMeta.isLittleEndian(imageIndex));
      b[4] = 'I';
      b[5] = 'D';
      b[6] = 'A';
      b[7] = 'T';
      in.read(b, 8, b.length - 12);
      final int crc = (int) computeCRC(b, b.length - 4);
      DataTools.unpackBytes(
        crc, b, b.length - 4, 4, cMeta.isLittleEndian(imageIndex));
      stream.write(b);
      b = null;
    }

    // process PLTE chunks
    final APNGPLTEChunk plte = metadata.getPlte();
    if (plte != null) {
      processChunk(
        imageIndex, plte.getLength(), plte.getOffset(), coords, stream, false);
    }
    final RandomAccessInputStream s =
      new RandomAccessInputStream(stream.toByteArray());
    final DataInputStream dis = new DataInputStream(new BufferedInputStream(s, 4096));
    final BufferedImage bi = ImageIO.read(dis);
    dis.close();

    // Recover first plane

    lastPlane = null;
    openPlane(
      imageIndex, 0, 0, 0, cMeta.getAxisLength(imageIndex, Axes.X),
      cMeta.getAxisLength(imageIndex, Axes.Y));

    // paste current image onto first plane

    final WritableRaster firstRaster = lastPlane.getRaster();
    final WritableRaster currentRaster = bi.getRaster();

    firstRaster.setDataElements(coords[0], coords[1], currentRaster);
    lastPlane =
      new BufferedImage(lastPlane.getColorModel(), firstRaster, false, null);
    lastPlaneIndex = planeIndex;
    return lastPlane;
  }

  /* @see Reader#setMetadata(M) */
  @Override
  public void setMetadata(final APNGMetadata meta) throws IOException {
    super.setMetadata(meta);
    this.metadata = meta;
    final Translator<APNGMetadata, CoreMetadata> t = new APNGCoreTranslator();
    t.translate(meta, cMeta);
  }

  // -- Helper methods --

  private long computeCRC(final byte[] buf, final int len) {
    final CRC32 crc = new CRC32();
    crc.update(buf, 0, len);
    return crc.getValue();
  }

  private void processChunk(final int imageIndex, final int length, final long offset,
    final int[] coords, final ByteArrayOutputStream stream, final boolean isIHDR)
    throws IOException
  {
    byte[] b = new byte[length + 12];
    DataTools.unpackBytes(length, b, 0, 4, cMeta.isLittleEndian(imageIndex));
    final byte[] typeBytes = (isIHDR ? "IHDR".getBytes(Constants.ENCODING) :
      "PLTE".getBytes(Constants.ENCODING));
    System.arraycopy(typeBytes, 0, b, 4, 4);
    in.seek(offset);
    in.read(b, 8, b.length - 12);
    if (isIHDR) {
      DataTools.unpackBytes(
        coords[2], b, 8, 4, cMeta.isLittleEndian(imageIndex));
      DataTools.unpackBytes(
        coords[3], b, 12, 4, cMeta.isLittleEndian(imageIndex));
    }
    final int crc = (int) computeCRC(b, b.length - 4);
    DataTools.unpackBytes(
      crc, b, b.length - 4, 4, cMeta.isLittleEndian(imageIndex));
    stream.write(b);
    b = null;
  }
}
