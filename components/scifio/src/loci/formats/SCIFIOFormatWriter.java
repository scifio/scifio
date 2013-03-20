package loci.formats;

import java.awt.image.ColorModel;
import java.io.IOException;

import net.imglib2.meta.Axes;

import loci.formats.codec.CodecOptions;
import loci.formats.meta.MetadataRetrieve;
import ome.scifio.ByteArrayPlane;
import ome.scifio.Plane;
import ome.scifio.Writer;

/**
 * Abstract superclass of all biological file format writers.
 * Defers to ome.scifio.Writer 
 *
 */
@Deprecated
public abstract class SCIFIOFormatWriter extends FormatWriter {

  // -- Fields --

  /** Scifio Writer for deference */
  protected Writer writer;
  
  
  //TODO could make this a Plane and then cache either a ByteArrayPlane or BufferedImagePlane as needed
  /**
   * Cached ByteArrayPlane for reusing byte arrays with this writer
   */
  private ByteArrayPlane bPlane = null;

  /** */
  protected ome.scifio.DatasetMetadata cMeta;

  // -- Constructor --

  public SCIFIOFormatWriter(String format, String suffix) {
    super(format, suffix);
  }

  public SCIFIOFormatWriter(String format, String[] suffixes) {
    super(format, suffixes);
  }

  // -- IFormatWriter API methods --

  /* @see IFormatWriter#changeOutputFile(String) */
  @Deprecated
  @Override
  public void changeOutputFile(String id) throws FormatException, IOException {
    try {
      writer.changeOutputFile(id);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatWriter#saveBytes(int, byte[]) */
  @Deprecated
  @Override
  public void saveBytes(int no, byte[] buf) throws FormatException, IOException
  {
    try {
      writer.savePlane(getSeries(), no, planeCheck(buf, 0, 0,
          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.X),
          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.Y)));
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatWriter#savePlane(int, Object) */
  @Deprecated
  @Override
  public void savePlane(int no, Object plane)
    throws FormatException, IOException
  {
    // NB: Writers use byte arrays by default as the native type.
    if (!(plane instanceof byte[])) {
      throw new IllegalArgumentException("Object to save must be a byte[]");
    }
    try {
      writer.savePlane(getSeries(), no, planeCheck((byte[])plane, 0, 0,
          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.X),
          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.Y)));
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatWriter#savePlane(int, Object, int, int, int, int) */
  @Deprecated
  @Override
  public void savePlane(int no, Object plane, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    // NB: Writers use byte arrays by default as the native type.
    if (!(plane instanceof byte[])) {
      throw new IllegalArgumentException("Object to save must be a byte[]");
    }
    try {
      writer.savePlane(getSeries(), no, planeCheck((byte[])plane, x, y, w, h), x, y, w, h);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatWriter#setSeries(int) */
  @Deprecated
  @Override
  public void setSeries(int series) throws FormatException {
    if (series < 0) throw new FormatException("Series must be > 0.");
    if (series >= writer.getDatasetMetadata().getImageCount()) {
      throw new FormatException("Series is '" + series +
        "' but MetadataRetrieve only defines " +
        writer.getDatasetMetadata().getImageCount() + " series.");
    }
    this.series = series;
  }

  /* @see IFormatWriter#getSeries() */
  @Override
  public int getSeries() {
    return series;
  }

  /* @see IFormatWriter#setInterleaved(boolean) */
  @Deprecated
  @Override
  public void setInterleaved(boolean interleaved) {
    writer.getDatasetMetadata().setInterleaved(getSeries(), interleaved);
  }

  /* @see IFormatWriter#isInterleaved() */
  @Deprecated
  @Override
  public boolean isInterleaved() {
    return writer.getDatasetMetadata().isInterleaved(getSeries());
  }

  /* @see IFormatWriter#setValidBitsPerPixel(int) */
  @Deprecated
  @Override
  public void setValidBitsPerPixel(int bits) {
    writer.getDatasetMetadata().setBitsPerPixel(getSeries(), bits);
  }

  /* @see IFormatWriter#canDoStacks() */
  @Deprecated
  @Override
  public boolean canDoStacks() {
    return false;
  }

  /* @see IFormatWriter#setMetadataRetrieve(MetadataRetrieve) */
  @Deprecated
  @Override
  public void setMetadataRetrieve(MetadataRetrieve retrieve) {
     metadataRetrieve = retrieve;
  }

  /* @see IFormatWriter#getMetadataRetrieve() */
  @Deprecated
  @Override
  public MetadataRetrieve getMetadataRetrieve() {
    return metadataRetrieve;
  }

  /* @see IFormatWriter#setColorModel(ColorModel) */
  @Deprecated
  @Override
  public void setColorModel(ColorModel model) {
    writer.setColorModel(model);
  }

  /* @see IFormatWriter#getColorModel() */
  @Deprecated
  @Override
  public ColorModel getColorModel() {
    return writer.getColorModel();
  }

  /* @see IFormatWriter#setFramesPerSecond(int) */
  @Deprecated
  @Override
  public void setFramesPerSecond(int rate) {
    writer.setFramesPerSecond(rate);
  }

  /* @see IFormatWriter#getFramesPerSecond() */
  @Deprecated
  @Override
  public int getFramesPerSecond() {
    return writer.getFramesPerSecond();
  }

  /* @see IFormatWriter#getCompressionTypes() */
  @Deprecated
  @Override
  public String[] getCompressionTypes() {
    return writer.getCompressionTypes();
  }

  /* @see IFormatWriter#setCompression(compress) */
  @Deprecated
  @Override
  public void setCompression(String compress) throws FormatException {
    // check that this is a valid type
    try {
      writer.setCompression(compress);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatWriter#setCodecOptions(CodecOptions) */
  @Deprecated
  @Override
  public void setCodecOptions(CodecOptions options) {
    writer.setCodecOptions(options);
  }

  /* @see IFormatWriter#getCompression() */
  @Deprecated
  @Override
  public String getCompression() {
    return writer.getCompression();
  }

  /* @see IFormatWriter#getPixelTypes() */
  @Deprecated
  @Override
  public int[] getPixelTypes() {
    return writer.getPixelTypes(getCompression());
  }

  /* @see IFormatWriter#getPixelTypes(String) */
  @Deprecated
  @Override
  public int[] getPixelTypes(String codec) {
    return writer.getPixelTypes(codec);
  }

  /* @see IFormatWriter#isSupportedType(int) */
  @Deprecated
  @Override
  public boolean isSupportedType(int type) {
    return writer.isSupportedType(type);
  }

  /* @see IFormatWriter#setWriteSequentially(boolean) */
  @Deprecated
  @Override
  public void setWriteSequentially(boolean sequential) {
    writer.setWriteSequentially(sequential);
  }

  // -- Deprecated IFormatWriter API methods --

  // TODO decide to remove or not
//  /**
//   * @deprecated
//   * @Override
//   * @see IFormatWriter#saveBytes(byte[], boolean)
//   */
//  public void saveBytes(byte[] bytes, boolean last)
//    throws FormatException, IOException
//  {
//    try {
//      writer.savePlane(planeCheck(bytes, 0, 0,
//          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.X),
//          writer.getDatasetMetadata().getAxisLength(getSeries(), Axes.Y)));
//    }
//    catch (ome.scifio.FormatException e) {
//      throw new FormatException(e);
//    }
//  }
//
//  /**
//   * @deprecated
//   * @Override
//   * @see IFormatWriter#saveBytes(byte[], int, boolean, boolean)
//   */
//  public void saveBytes(byte[] bytes, int series, boolean lastInSeries,
//    boolean last) throws FormatException, IOException
//  {
//    try {
//      writer.savePlane(bytes, series, lastInSeries, last);
//    }
//    catch (ome.scifio.FormatException e) {
//      throw new FormatException(e);
//    }
//  }
//
//  /**
//   * @deprecated
//   * @Override
//   * @see IFormatWriter#savePlane(Object, boolean)
//   */
//  public void savePlane(Object plane, boolean last)
//    throws FormatException, IOException
//  {
//    try {
//      writer.savePlane(plane, 0, last, last);
//    }
//    catch (ome.scifio.FormatException e) {
//      throw new FormatException(e);
//    }
//  }
//
//  /**
//   * @deprecated
//   * @Override
//   * @see IFormatWriter#savePlane(Object, int, boolean, boolean)
//   */
//  public void savePlane(Object plane, int series, boolean lastInSeries,
//    boolean last) throws FormatException, IOException
//  {
//    // NB: Writers use byte arrays by default as the native type.
//    if (!(plane instanceof byte[])) {
//      throw new IllegalArgumentException("Object to save must be a byte[]");
//    }
//    try {
//      writer.savePlane(planeCheck((byte[])plane), series, lastInSeries, last);
//    }
//    catch (ome.scifio.FormatException e) {
//      throw new FormatException(e);
//    }
//  }

  // -- IFormatHandler API methods --

  /* @see IFormatHandler#setId(String) */
  @Deprecated
  @Override
  public void setId(String id) throws FormatException, IOException {
    if (id.equals(currentId)) return;
    writer.close();
    currentId = id;
    
    try {
      writer.setMetadata(writer.getFormat().createParser().parse(id));
      writer.setDest(id);
    }
    catch (ome.scifio.FormatException e) {
      throw new FormatException(e);
    }
  }

  /* @see IFormatHandler#close() */
  @Deprecated
  @Override
  public void close() throws IOException {
    writer.close();
  }
  
  // -- Helper Methods --
  
  // TODO need this in Reader too.. need to abstract this, and maybe expand the signature options
  
  protected Plane planeCheck(byte[] buf, int x, int y, int w, int h) {
    if (bPlane == null)
      bPlane = new ByteArrayPlane(writer.getContext());
    
    bPlane.populate(writer.getDatasetMetadata().get(getSeries()), buf, x, y, w, h);
    return bPlane;
  }
}
