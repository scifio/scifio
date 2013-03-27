package ome.scifio;

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.FormatException;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * Abstract superclass of all SCIFIO reader components.
 *
 */
public abstract class AbstractReader<M extends Metadata>
  extends AbstractFormatHandler implements Reader<M> {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

  /** Default thumbnail width and height. */
  protected static final int THUMBNAIL_DIMENSION = 128;

  // -- Fields --

  /** Type-specific Metadata values. */
  protected M metadata;

  /** Core Metadata values. */
  protected CoreMetadata cMeta;

  /** Whether or not to group multi-file formats. */
  protected boolean group = true;

  /** Whether or not to normalize float data. */
  protected boolean normalizeData;

  /** Current file. */
  protected RandomAccessInputStream in;

  /** List of domains in which this format is used. */
  protected String[] domains = new String[0];

  /** Name of current file. */
  protected String currentId;

  /** Whether this format supports multi-file datasets. */
  protected boolean hasCompanionFiles = false;

  // -- Constructors --

  /** Constructs a reader with the given name and default suffix */
  public AbstractReader(final String format, final String suffix,
    final SCIFIO ctx)
  {
    super(format, suffix, ctx);
    init();
  }

  /** Constructs a reader with the given name and default suffixes */
  public AbstractReader(final String format, final String[] suffixes,
    final SCIFIO ctx)
  {
    super(format, suffixes, ctx);
    init();
  }

  // -- HasFormat API Methods --

  @Override
  @SuppressWarnings("unchecked")
  public Format<M, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromReader(getClass());
  }

  // -- Reader API Methods --

  //TODO Merge common Reader and Writer API methods

  /* @see Reader#setSource(File) */
  @Override
  public void setSource(final File file) throws IOException {
    setSource(file.getName());
  }

  /* @see Reader#setSource(String) */
  @Override
  public void setSource(final String fileName) throws IOException {
    setSource(new RandomAccessInputStream(fileName));
  }

  /* @see Reader#setSource(RandomAccessInputStream) */
  @Override
  public void setSource(final RandomAccessInputStream stream)
    throws IOException
  {
    this.in = stream;

    if (this.metadata == null) {
      try {
        @SuppressWarnings("unchecked")
        final M meta =
          (M) getContext().getFormatFromReader(getClass())
            .createParser()
            .parse(stream);
        this.setMetadata(meta);
      }
      catch (final FormatException e) {
        throw new IOException(e);
      }
    }
  }

  /* @see Reader#openBytes(int, int) */
  @Override
  public byte[] openBytes(final int imageIndex, final int planeNumber)
    throws FormatException, IOException
  {
    return openBytes(
      imageIndex, planeNumber, 0, 0, cMeta.getAxisLength(imageIndex, Axes.X),
      cMeta.getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, int, int, int, int) */
  @Override
  public byte[] openBytes(final int imageIndex, final int planeIndex,
    final int x, final int y, final int w, final int h)
    throws FormatException, IOException
  {
    final int bpp =
      FormatTools.getBytesPerPixel(cMeta.getPixelType(imageIndex));
    final int ch = cMeta.getRGBChannelCount(imageIndex);
    final byte[] newBuffer = new byte[w * h * ch * bpp];
    return openBytes(imageIndex, planeIndex, newBuffer, x, y, w, h);
  }

  /* @see Reader#openBytes(int, int, byte[]) */
  @Override
  public byte[] openBytes(final int imageIndex, final int planeIndex,
    final byte[] buf) throws FormatException, IOException
  {
    return openBytes(
      imageIndex, planeIndex, buf, 0, 0,
      cMeta.getAxisLength(imageIndex, Axes.X),
      cMeta.getAxisLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, byte[], int, int, int, int) */
  @Override
  public abstract byte[] openBytes(int imageIndex, int planeIndex, byte[] buf,
    int x, int y, int w, int h) throws FormatException, IOException;

  /* @see Reader#openPlane(int, int, int, int, int, int int) */
  @Override
  public Object openPlane(final int imageIndex, final int planeIndex,
    final int x, final int y, final int w, final int h)
    throws FormatException, IOException
  {
    // NB: Readers use byte arrays by default as the native type.
    return openBytes(imageIndex, planeIndex, x, y, w, h);
  }

  /* @see Reader#readPlane(RandomAccessInputStream, int, int, int, int, int, int, byte[] */
  @Override
  public byte[] readPlane(final RandomAccessInputStream s,
    final int imageIndex, final int x, final int y, final int w, final int h,
    final int scanlinePad, final byte[] buf) throws IOException
  {
    final int c = cMeta.getRGBChannelCount(imageIndex);
    final int bpp =
      FormatTools.getBytesPerPixel(cMeta.getPixelType(imageIndex));
    if (x == 0 && y == 0 && w == cMeta.getAxisLength(imageIndex, Axes.X) &&
      h == cMeta.getAxisLength(imageIndex, Axes.Y) && scanlinePad == 0)
    {
      s.read(buf);
    }
    else if (x == 0 && w == cMeta.getAxisLength(imageIndex, Axes.Y) &&
      scanlinePad == 0)
    {
      if (cMeta.isInterleaved(imageIndex)) {
        s.skipBytes(y * w * bpp * c);
        s.read(buf, 0, h * w * bpp * c);
      }
      else {
        final int rowLen = w * bpp;
        for (int channel = 0; channel < c; channel++) {
          s.skipBytes(y * rowLen);
          s.read(buf, channel * h * rowLen, h * rowLen);
          if (channel < c - 1) {
            // no need to skip bytes after reading final channel
            s.skipBytes((cMeta.getAxisLength(imageIndex, Axes.Y) - y - h) *
              rowLen);
          }
        }
      }
    }
    else {
      final int scanlineWidth =
        cMeta.getAxisLength(imageIndex, Axes.Y) + scanlinePad;
      if (cMeta.isInterleaved(imageIndex)) {
        s.skipBytes(y * scanlineWidth * bpp * c);
        for (int row = 0; row < h; row++) {
          s.skipBytes(x * bpp * c);
          s.read(buf, row * w * bpp * c, w * bpp * c);
          if (row < h - 1) {
            // no need to skip bytes after reading final row
            s.skipBytes(bpp * c * (scanlineWidth - w - x));
          }
        }
      }
      else {
        for (int channel = 0; channel < c; channel++) {
          s.skipBytes(y * scanlineWidth * bpp);
          for (int row = 0; row < h; row++) {
            s.skipBytes(x * bpp);
            s.read(buf, channel * w * h * bpp + row * w * bpp, w * bpp);
            if (row < h - 1 || channel < c - 1) {
              // no need to skip bytes after reading final row of final channel
              s.skipBytes(bpp * (scanlineWidth - w - x));
            }
          }
          if (channel < c - 1) {
            // no need to skip bytes after reading final channel
            s.skipBytes(scanlineWidth * bpp *
              (cMeta.getAxisLength(imageIndex, Axes.Y) - y - h));
          }
        }
      }
    }
    return buf;
  }

  /* @see Reader#openThumbBytes(int) */
  @Override
  public byte[] openThumbBytes(final int imageIndex, final int planeIndex)
    throws FormatException, IOException
  {
    FormatTools.assertStream(in, true, 1);
    /* TODO move FormatTools implementation here 
    return FormatTools.openThumbBytes(this, no); */
    return null;
  }

  /* @see Reader#setGroupFiles(boolean) */
  @Override
  public void setGroupFiles(final boolean groupFiles) {
    group = groupFiles;
  }

  /* @see Reader#isGroupFiles() */
  @Override
  public boolean isGroupFiles() {
    FormatTools.assertStream(in, false, 1);
    return group;
  }

  /* @see Reader#fileGroupOption(String) */
  @Override
  public int fileGroupOption(final String id)
    throws FormatException, IOException
  {
    return FormatTools.CANNOT_GROUP;
  }

  /* @see Reader#setMetadata() */
  @Override
  public void setMetadata(final M meta) throws IOException {
    this.metadata = meta;
    if(this.in == null) setSource(meta.getSource());
  }

  /* @see Reader#getMetadata() */
  @Override
  public M getMetadata() {
    return this.metadata;
  }

  /* @see Reader#getCurrentFile() */
  @Override
  public String getCurrentFile() {

    FormatTools.assertStream(in, true, 1);
    return in.getFileName();
  }

  /* @see Reader#close(boolean) */
  @Override
  public void close(final boolean fileOnly) throws IOException {
    if (in != null) in.close();
    if (!fileOnly) {
      in = null;
    }
  }

  /* @see Reader#hasCompanionFiles() */
  @Override
  public boolean hasCompanionFiles() {
    return hasCompanionFiles;
  }

  /* @see Reader#close() */
  @Override
  public void close() throws IOException {
    close(false);
  }

  /* @see Reader#isNormalized() */
  @Override
  public boolean isNormalized() {
    return normalizeData;
  }

  /* @see Reader#setNormalized(boolean) */
  @Override
  public void setNormalized(final boolean normalize) {
    normalizeData = normalize;
  }

  /* @see Reader#getOptimalTileWidth(int) */
  @Override
  public int getOptimalTileWidth(final int imageIndex) {
    return cMeta.getAxisLength(imageIndex, Axes.Y);
  }

  /* @see Reader#getOptimalTileHeight(int) */
  @Override
  public int getOptimalTileHeight(final int imageIndex) {
    final int bpp =
      FormatTools.getBytesPerPixel(cMeta.getPixelType(imageIndex));
    final int maxHeight =
      (1024 * 1024) /
        (cMeta.getAxisLength(imageIndex, Axes.X) *
          cMeta.getRGBChannelCount(imageIndex) * bpp);
    return Math.min(maxHeight, cMeta.getAxisLength(imageIndex, Axes.X));
  }

  /* @see Reader#getDomains() */
  @Override
  public String[] getDomains() {
    return domains;
  }

  /* @see Reader#getCoreMetadata() */
  @Override
  public CoreMetadata getCoreMetadata() {
    return cMeta;
  }

  /* @see Reader#getZCTCoords(int) */
  @Override
  public int[] getZCTCoords(final int index) {
    // TODO Auto-generated method stub
    return null;
  }

  /* @see Reader#getStream() */
  @Override
  public RandomAccessInputStream getStream() {
    return in;
  }

  /* @see Reader#getImageCount() */
  @Override
  public int getImageCount() {
    return cMeta.getImageCount();
  }

  /* @see Reader#getPlaneCount(int) */
  @Override
  public int getPlaneCount(final int imageIndex) {
    return cMeta.getPlaneCount(imageIndex);
  }

  @Override
  public Reader<Metadata>[] getUnderlyingReaders() {
    // TODO Auto-generated method stub
    return null;
  }

  // -- AbstractReader Methods --

  private void init() {
    this.cMeta = new CoreMetadata();
  }

  @Override
  public byte[] readPlane(final RandomAccessInputStream s,
    final int imageIndex, final int x, final int y, final int w, final int h,
    final byte[] buf) throws IOException
  {
    return readPlane(s, imageIndex, x, y, w, h, 0, buf);
  }
}
