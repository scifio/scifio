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
package ome.scifio.wrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import ome.scifio.AxisGuesser;
import ome.scifio.ImageMetadata;
import ome.scifio.DatasetMetadata;
import ome.scifio.FileInfo;
import ome.scifio.FilePattern;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.io.Location;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * Logic to stitch together files with similar names.
 * Assumes that all files have the same characteristics (e.g., dimensions).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/FileStitcher.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/FileStitcher.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class FileStitcher<M extends Metadata> extends ReaderWrapper<M> {

  // -- Fields --

  /**
   * Whether string ids given should be treated
   * as file patterns rather than single file paths.
   */
  private boolean patternIds = false;

  private boolean doNotChangePattern = false;

  /** Dimensional axis lengths per file. */
  private int[] sizeZ, sizeC, sizeT;
  
  /** Component lengths for each axis type. */
  private int[][] lenZ, lenC, lenT;

  /** Core metadata. */
  private DatasetMetadata core;
  
  private boolean noStitch;
  private boolean group = true;

  private ExternalSeries[] externals;
  
  // -- Constructors --

  /** Constructs a FileStitcher around a new image reader. */
  public FileStitcher() { super(); }

  /**
   * Constructs a FileStitcher with the given reader.
   * @param r The reader to use for reading stitched files.
   */
  public FileStitcher(Reader r) { this(r, false); }

  /**
   * Constructs a FileStitcher with the given reader.
   * @param r The reader to use for reading stitched files.
   * @param patternIds Whether string ids given should be treated as file
   *   patterns rather than single file paths.
   */
  public FileStitcher(Reader r, boolean patternIds) {
    super(r);
    if (r.getClass().getPackage().getName().equals("loci.formats.in")) {
    }
    else {
      setReader(DimensionSwapper.makeDimensionSwapper(r));
    }
    setUsingPatternIds(patternIds);
  }

  // -- FileStitcher API methods --

  /** Sets whether the reader is using file patterns for IDs. */
  public void setUsingPatternIds(boolean patternIds) {
    this.patternIds = patternIds;
  }

  /** Gets whether the reader is using file patterns for IDs. */
  public boolean isUsingPatternIds() { return patternIds; }

  public void setCanChangePattern(boolean doChange) {
    doNotChangePattern = !doChange;
  }

  public boolean canChangePattern() {
    return !doNotChangePattern;
  }

  /** Gets the reader appropriate for use with the given image plane. */
  public Reader getReader(int imageIndex, int planeIndex) throws FormatException, IOException {
    if (noStitch) return getReader();
    int[] q = computeIndices(imageIndex, planeIndex);
    int fno = q[0];
    return getReader(imageIndex, fno);
  }

  /**
   * Gets the reader that should be used with the given series and image plane.
   */
  public DimensionSwapper getExternalsReader(int imageIndex, int planeIndex) {
    if (noStitch) return (DimensionSwapper) getReader();
    DimensionSwapper r = externals[getExternalSeries(imageIndex)].getReaders()[planeIndex];
    initReader(imageIndex, planeIndex);
    return r;
  }

  /** Gets the local reader index for use with the given image plane. */
  public int getAdjustedIndex(int imageIndex, int planeIndex) throws FormatException, IOException {
    if (noStitch) return planeIndex;
    int[] q = computeIndices(imageIndex, planeIndex);
    int ino = q[1];
    return ino;
  }

  /**
   * Gets the axis type for each dimensional block.
   * @return An array containing values from the enumeration:
   *   <ul>
   *     <li>AxisGuesser.Z_AXIS: focal planes</li>
   *     <li>AxisGuesser.T_AXIS: time points</li>
   *     <li>AxisGuesser.C_AXIS: channels</li>
   *     <li>AxisGuesser.S_AXIS: series</li>
   *   </ul>
   */
  public int[] getAxisTypes(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return externals[getExternalSeries(imageIndex)].getAxisGuesser().getAxisTypes();
  }

  /**
   * Sets the axis type for each dimensional block.
   * @param axes An array containing values from the enumeration:
   *   <ul>
   *     <li>AxisGuesser.Z_AXIS: focal planes</li>
   *     <li>AxisGuesser.T_AXIS: time points</li>
   *     <li>AxisGuesser.C_AXIS: channels</li>
   *     <li>AxisGuesser.S_AXIS: series</li>
   *   </ul>
   */
  public void setAxisTypes(int imageIndex, int[] axes) throws FormatException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    externals[getExternalSeries(imageIndex)].getAxisGuesser().setAxisTypes(axes);
    computeAxisLengths(imageIndex);
  }

  /** Gets the file pattern object used to build the list of files. */
  public FilePattern getFilePattern(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? findPattern(getCurrentFile()) :
      externals[getExternalSeries(imageIndex)].getFilePattern();
  }

  /**
   * Gets the axis guesser object used to guess
   * which dimensional axes are which.
   */
  public AxisGuesser getAxisGuesser(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return externals[getExternalSeries(imageIndex)].getAxisGuesser();
  }

  public FilePattern findPattern(String id) {
    return new FilePattern(FilePattern.findPattern(id));
  }

  /**
   * Finds the file pattern for the given ID, based on the state of the file
   * stitcher. Takes both ID map entries and the patternIds flag into account.
   */
  public String[] findPatterns(String id) {
    if (!patternIds) {
      // find the containing patterns
      HashMap<String, Object> map = Location.getIdMap();
      if (map.containsKey(id)) {
        // search ID map for pattern, rather than files on disk
        String[] idList = new String[map.size()];
        map.keySet().toArray(idList);
        return FilePattern.findSeriesPatterns(id, null, idList);
      }
      else {
        // id is an unmapped file path; look to similar files on disk
        return FilePattern.findSeriesPatterns(id);
      }
    }
    if (doNotChangePattern) {
      return new String[] {id};
    }
    patternIds = false;
    String[] patterns = findPatterns(new FilePattern(id).getFiles()[0]);
    if (patterns.length == 0) patterns = new String[] {id};
    else {
      FilePattern test = new FilePattern(patterns[0]);
      if (test.getFiles().length == 0) patterns = new String[] {id};
    }
    patternIds = true;
    return patterns;
  }
  
  public int getPlaneCount(int imageIndex, Reader r) {
    return r.getDatasetMetadata().getPlaneCount(imageIndex);
  }

  public boolean isRGB(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isRGB(imageIndex) : core.isRGB(imageIndex);
  }
  
  public int getDimensionLength(int imageIndex, AxisType t) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getAxisLength(imageIndex, t) :
      core.getAxisLength(imageIndex, t);
  }

  public int getPixelType(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getPixelType(imageIndex) : core.getPixelType(imageIndex);
  }

  public int getBitsPerPixel(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getBitsPerPixel(imageIndex) : core.getBitsPerPixel(imageIndex);
  }

  public boolean isIndexed(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isIndexed(imageIndex) : core.isIndexed(imageIndex);
  }

  public boolean isFalseColor(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isFalseColor(imageIndex) : core.isFalseColor(imageIndex);
  }

  public byte[][] get8BitLookupTable(int imageIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().get8BitLookupTable(imageIndex) :
      getReader(imageIndex, 0).getDatasetMetadata().get8BitLookupTable(imageIndex);
  }

  public short[][] get16BitLookupTable(int imageIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().get16BitLookupTable(imageIndex) :
      getReader(imageIndex, 0).getDatasetMetadata().get16BitLookupTable(imageIndex);
  }

  public int[] getChannelDimLengths(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (noStitch) return datasetMeta().getChannelDimLengths(imageIndex);
    if (core.getChannelDimLengths(imageIndex) == null) {
      return new int[] {core.getAxisLength(imageIndex, Axes.CHANNEL)};
    }
    return core.getChannelDimLengths(imageIndex);
  }

  public String[] getChannelDimTypes(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    if (noStitch) return datasetMeta().getChannelDimTypes(imageIndex);
    if (core.getChannelDimTypes(imageIndex) == null) {
      return new String[] {FormatTools.CHANNEL};
    }
    return core.getChannelDimTypes(imageIndex);
  }

  public int getThumbSizeX(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getThumbSizeX(imageIndex) :
      getExternalsReader(imageIndex, 0).getDatasetMetadata().getThumbSizeX(imageIndex);
  }

  public int getThumbSizeY(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getThumbSizeY(imageIndex) :
      getExternalsReader(imageIndex, 0).getDatasetMetadata().getThumbSizeY(imageIndex);
  }

  public boolean isLittleEndian(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isLittleEndian(imageIndex) :
      getExternalsReader(imageIndex, 0).getDatasetMetadata().isLittleEndian(imageIndex);
  }

  public boolean isOrderCertain(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isOrderCertain(imageIndex) : core.isOrderCertain(imageIndex);
  }

  public boolean isThumbnailSeries(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isThumbnailImage(imageIndex) : core.isThumbnailImage(imageIndex);
  }

  public boolean isInterleaved(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().isInterleaved(imageIndex) :
      getExternalsReader(imageIndex, 0).getDatasetMetadata().isInterleaved(imageIndex);
  }

  public String getDimensionOrder(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    
    return FormatTools.findDimensionOrder((noStitch ? datasetMeta() : core), imageIndex);
  }

  //TODO
//  public void setNormalized(boolean normalize) {
//    FormatTools.assertId(getCurrentFile(), false, 2);
//    if (externals == null) reader.setNormalized(normalize);
//    else {
//      for (ExternalSeries s : externals) {
//        for (DimensionSwapper r : s.getReaders()) {
//          r.setNormalized(normalize);
//        }
//      }
//    }
//  }
//    
//    public void setGroupFiles(boolean group) {
//    this.group = group;
//  }
//
//  public boolean isGroupFiles() {
//    return group;
//  }
//
//  public String[] getUsedFiles() {
//    FormatTools.assertId(getCurrentFile(), true, 2);
//
//    if (noStitch) return reader.getUsedFiles();
//
//    // returning the files list directly here is fast, since we do not
//    // have to call initFile on each constituent file; but we can only do so
//    // when each constituent file does not itself have multiple used files
//
//    Vector<String> files = new Vector<String>();
//    for (ExternalSeries s : externals) {
//      String[] f = s.getFiles();
//      for (String file : f) {
//        if (!files.contains(file)) files.add(file);
//      }
//
//      DimensionSwapper[] readers = s.getReaders();
//      for (int i=0; i<readers.length; i++) {
//        try {
//          readers[i].setId(f[i]);
//          String[] used = readers[i].getUsedFiles();
//          for (String file : used) {
//            if (!files.contains(file)) files.add(file);
//          }
//        }
//        catch (FormatException e) {
//          LOGGER.debug("", e);
//        }
//        catch (IOException e) {
//          LOGGER.debug("", e);
//        }
//      }
//    }
//    return files.toArray(new String[files.size()]);
//  }
//
//  public String[] getUsedFiles(boolean noPixels) {
//    return noPixels && noStitch ?
//      reader.getUsedFiles(noPixels) : getUsedFiles();
//  }
//
//  public String[] getSeriesUsedFiles() {
//    return getUsedFiles();
//  }
//
//  public String[] getSeriesUsedFiles(boolean noPixels) {
//    return getUsedFiles(noPixels);
//  }
//
//  public FileInfo[] getAdvancedUsedFiles(boolean noPixels) {
//    if (noStitch) return reader.getAdvancedUsedFiles(noPixels);
//    String[] files = getUsedFiles(noPixels);
//    if (files == null) return null;
//    FileInfo[] infos = new FileInfo[files.length];
//    for (int i=0; i<infos.length; i++) {
//      infos[i] = new FileInfo();
//      infos[i].filename = files[i];
//      try {
//        infos[i].reader = ((DimensionSwapper) reader).unwrap().getClass();
//      }
//      catch (FormatException e) {
//        LOGGER.debug("", e);
//      }
//      catch (IOException e) {
//        LOGGER.debug("", e);
//      }
//      infos[i].usedToInitialize = files[i].endsWith(getCurrentFile());
//    }
//    return infos;
//  }
//
//  public FileInfo[] getAdvancedSeriesUsedFiles(boolean noPixels) {
//    if (noStitch) return reader.getAdvancedSeriesUsedFiles(noPixels);
//    String[] files = getSeriesUsedFiles(noPixels);
//    if (files == null) return null;
//    FileInfo[] infos = new FileInfo[files.length];
//    for (int i=0; i<infos.length; i++) {
//      infos[i] = new FileInfo();
//      infos[i].filename = files[i];
//      try {
//        infos[i].reader = ((DimensionSwapper) reader).unwrap().getClass();
//      }
//      catch (FormatException e) {
//        LOGGER.debug("", e);
//      }
//      catch (IOException e) {
//        LOGGER.debug("", e);
//      }
//      infos[i].usedToInitialize = files[i].endsWith(getCurrentFile());
//    }
//    return infos;
//  }

  // -- Reader API methods --

  /* @see Reader#getImageCount() */
  public int getImageCount() {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getImageCount() : core.getImageCount();
  }

  /* @see Reader#openBytes(int) */
  public byte[] openBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    return openBytes(imageIndex, planeIndex, 0, 0, getDimensionLength(imageIndex, Axes.X),
      getDimensionLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, byte[]) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf)
    throws FormatException, IOException
  {
    return openBytes(imageIndex, planeIndex, buf, 0, 0, getDimensionLength(imageIndex, Axes.X),
      getDimensionLength(imageIndex, Axes.Y));
  }

  /* @see Reader#openBytes(int, int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    int bpp = FormatTools.getBytesPerPixel(getPixelType(imageIndex));
    int ch = datasetMeta().getRGBChannelCount(imageIndex);
    byte[] buf = DataTools.allocate(w, h, ch, bpp);
    return openBytes(imageIndex, planeIndex, buf, x, y, w, h);
  }

  /* @see Reader#openBytes(int, byte[], int, int, int, int) */
  public byte[] openBytes(int imageIndex, int planeIndex, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);

    int[] pos = computeIndices(imageIndex, planeIndex);
    Reader r = getReader(imageIndex, pos[0]);
    int ino = pos[1];

    if (ino < getPlaneCount(imageIndex, r)) return r.openBytes(imageIndex, ino, buf, x, y, w, h);

    // return a blank image to cover for the fact that
    // this file does not contain enough image planes
    Arrays.fill(buf, (byte) 0);
    return buf;
  }

  /* @see Reader#openPlane(int, int, int, int, int) */
  public Object openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(getCurrentFile(), true, 2);

    Reader r = getReader(imageIndex, planeIndex);
    int ino = getAdjustedIndex(imageIndex, planeIndex);
    if (ino < getPlaneCount(imageIndex, r)) return r.openPlane(imageIndex, ino, x, y, w, h);

    return null;
  }

  /* @see Reader#openThumbBytes(int) */
  public byte[] openThumbBytes(int imageIndex, int planeIndex) throws FormatException, IOException {
    FormatTools.assertId(getCurrentFile(), true, 2);

    Reader r = getReader(imageIndex, planeIndex);
    int ino = getAdjustedIndex(imageIndex, planeIndex);
    if (ino < getPlaneCount(imageIndex, r)) return r.openThumbBytes(imageIndex, ino);

    // return a blank image to cover for the fact that
    // this file does not contain enough image planes
    return externals[getExternalSeries(imageIndex)].getBlankThumbBytes();
  }

  /* @see Reader#close() */
  public void close() throws IOException {
    close(false);
  }

  /* @see Reader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (externals != null) {
      for (ExternalSeries s : externals) {
        if (s != null && s.getReaders() != null) {
          for (DimensionSwapper r : s.getReaders()) {
            if (r != null) r.close(fileOnly);
          }
        }
      }
    }
    if (!fileOnly) {
      noStitch = false;
      externals = null;
      lenZ = lenC = lenT = null;
      core = null;
    }
  }

// 

  /* @see Reader#getIndex(int, int, int) */
  public int getIndex(int imageIndex, int z, int c, int t) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return FormatTools.getIndex(this, imageIndex, z, c, t);
  }

  /* @see Reader#getZCTCoords(int) */
  public int[] getZCTCoords(int imageIndex, int planeIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? FormatTools.getZCTCoords(getReader(), imageIndex, planeIndex) :
      FormatTools.getZCTCoords(FormatTools.findDimensionOrder(core, imageIndex),
      core.getAxisLength(imageIndex, Axes.Z), core.getEffectiveSizeC(imageIndex),
      core.getAxisLength(imageIndex, Axes.TIME), core.getPlaneCount(imageIndex),
      imageIndex, planeIndex);
  }

  /* @see Reader#getSeriesMetadata() */
  public Hashtable<String, Object> getImageMetadata(int imageIndex) {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta().getImageMetadata(imageIndex) :
      core.getImageMetadata(imageIndex);
  }

  
  //TODO could probably replace all coreMeta()/core switch logic calls with this method
  /* @see Reader#getDatasetMetadata() */
  public DatasetMetadata getDatasetMetadata() {
    FormatTools.assertId(getCurrentFile(), true, 2);
    return noStitch ? datasetMeta() : core;
  }

  /* @see Reader#getUnderlyingReaders() */
  public Reader[] getUnderlyingReaders() {
    List<Reader> list = new ArrayList<Reader>();
    for (ExternalSeries s : externals) {
      for (DimensionSwapper r : s.getReaders()) {
        list.add(r);
      }
    }
    return list.toArray(new Reader[0]);
  }

  /* @see Reader#setSource(String) */
  public void setSource(String id) throws IOException {
    setSource(new RandomAccessInputStream(id));
  }
  
  /* @see Reader#setSource(File) */
  public void setSource(File file) throws IOException {
    setSource(new RandomAccessInputStream(file.getAbsolutePath()));
  }
  
  /* @see Reader#setSource(RandomAccessInputStream) */
  public void setSource(RandomAccessInputStream stream) throws IOException {
    setSource(stream, 0);
  }
  
  // -- Augmented setSource methods for specifying which dimensions to swap --

  public void setSource(String id, int imageIndex) throws IOException {
    setSource(new RandomAccessInputStream(id), imageIndex);
  }
  
  public void setSource(File file, int imageIndex) throws IOException {
    setSource(new RandomAccessInputStream(file.getAbsolutePath()), imageIndex);
  }
  
  public void setSource(RandomAccessInputStream stream, int imageIndex) throws IOException {
    super.setSource(stream);
    
    try {
      initFile(stream.getFileName(), imageIndex);
    }
    catch (FormatException e) {
      throw new IOException(e.getCause());
    }
  }
  // -- Internal FormatReader API methods --

  /** Initializes the given file or file pattern. */
  protected void initFile(String id, int imageIndex) throws FormatException, IOException {
    LOGGER.debug("initFile: {}", id);

    FilePattern fp = new FilePattern(id);
    if (!patternIds) {
      patternIds = fp.isValid() && fp.getFiles().length > 1;
    }
    else {
      patternIds =
        !new Location(id).exists() && Location.getMappedId(id).equals(id);
    }

    boolean mustGroup = false;
    if (patternIds) {
      mustGroup = fp.isValid() &&
        getReader().fileGroupOption(fp.getFiles()[0]) == FormatTools.MUST_GROUP;
    }
    else {
      mustGroup = getReader().fileGroupOption(id) == FormatTools.MUST_GROUP;
    }

    if (mustGroup || !group) {
      // reader subclass is handling file grouping
      noStitch = true;
      getReader().close();
      getReader().setGroupFiles(group);

      if (patternIds && fp.isValid()) {
        getReader().setSource(fp.getFiles()[0]);
      }
      else getReader().setSource(id);
      return;
    }

    if (fp.isRegex()) {
      setCanChangePattern(false);
    }

    String[] patterns = findPatterns(id);
    if (patterns.length == 0) patterns = new String[] {id};
    externals = new ExternalSeries[patterns.length];

    for (int i=0; i<externals.length; i++) {
      externals[i] = new ExternalSeries(new FilePattern(patterns[i]));
    }
    fp = new FilePattern(patterns[0]);

    getReader().close();
    getReader().setGroupFiles(group);

    if (!fp.isValid()) {
      throw new FormatException("Invalid file pattern: " + fp.getPattern());
    }
    getReader().setSource(fp.getFiles()[0]);

    String msg = " Please rename your files or disable file stitching.";
    if (getReader().getImageCount() > 1 && externals.length > 1) {
      throw new FormatException("Unsupported grouping: File pattern contains " +
        "multiple files and each file contains multiple series." + msg);
    }

    //TODO need a new UsedFiles interface..
    int nPixelsFiles = 1;
//      getReader().getUsedFiles().length - getReader().getUsedFiles(true).length;
    if (nPixelsFiles > 1 || fp.getFiles().length == 1) {
      noStitch = true;
      return;
    }

    AxisGuesser guesser = new AxisGuesser(fp, FormatTools.findDimensionOrder(core, imageIndex),
      datasetMeta().getAxisLength(imageIndex, Axes.Z), datasetMeta().getAxisLength(imageIndex, Axes.TIME),
      datasetMeta().getEffectiveSizeC(imageIndex),datasetMeta().isOrderCertain(imageIndex));

    // use the dimension order recommended by the axis guesser
    ((DimensionSwapper) getReader()).swapDimensions(imageIndex,
      Arrays.asList(FormatTools.findDimensionList(guesser.getAdjustedOrder())));

    // if this is a multi-series dataset, we need some special logic
    int imageCount = externals.length;
    if (externals.length == 1) {
      imageCount = getReader().getImageCount();
    }

    // verify that file pattern is valid and matches existing files
    if (!fp.isValid()) {
      throw new FormatException("Invalid " +
        (patternIds ? "file pattern" : "filename") +
        " (" + id + "): " + fp.getErrorMessage() + msg);
    }
    String[] files = fp.getFiles();

    if (files == null) {
      throw new FormatException("No files matching pattern (" +
        fp.getPattern() + "). " + msg);
    }

    for (int i=0; i<files.length; i++) {
      String file = files[i];

      // HACK: skip file existence check for fake files
      if (file.toLowerCase().endsWith(".fake")) continue;

      if (!new Location(file).exists()) {
        throw new FormatException("File #" + i +
          " (" + file + ") does not exist.");
      }
    }

    // determine reader type for these files; assume all are the same type
    Class<? extends Reader> readerClass =
      ((DimensionSwapper) getReader()).unwrap(files[0]).getClass();

    boolean[] certain = new boolean[imageCount];
    lenZ = new int[imageCount][];
    lenC = new int[imageCount][];
    lenT = new int[imageCount][];

    // analyze first file; assume each file has the same parameters
    core = new DatasetMetadata();
    
    //TODO globalMetadata?
    
    int oldSeries = imageIndex;
    for (int i=0; i<imageCount; i++) {
      Reader rr = getReader(i, 0);
      DatasetMetadata rrMeta = rr.getDatasetMetadata();

      core.add(new ImageMetadata());

      core.setAxisTypes(i, rrMeta.getAxes(i));
      core.setAxisLengths(i, rrMeta.getAxesLengths(i));
      // NB: core.sizeZ populated in computeAxisLengths below
      // NB: core.sizeC populated in computeAxisLengths below
      // NB: core.sizeT populated in computeAxisLengths below
      core.setPixelType(i, rrMeta.getPixelType(i));

      ExternalSeries external = externals[getExternalSeries(i)];
      //NB: implicitly populated in DatasetMetadata     
      // core[i].imageCount = rr.getPlaneCount() * external.getFiles().length;
      core.setThumbSizeX(i, rrMeta.getThumbSizeX(i));
      core.setThumbSizeY(i, rrMeta.getThumbSizeY(i));
      // NB: core.cLengths[i] populated in computeAxisLengths below
      // NB: core.cTypes[i] populated in computeAxisLengths below

      // NB: core.orderCertain[i] populated below
      core.setRGB(i, rrMeta.isRGB(i));
      core.setLittleEndian(i, rrMeta.isLittleEndian(i));
      core.setInterleaved(i, rrMeta.isInterleaved(i));
      core.setImageMetadata(i, rrMeta.getImageMetadata(i));
      core.setIndexed(i, rrMeta.isIndexed(i));
      core.setFalseColor(i, rrMeta.isFalseColor(i));
      core.setBitsPerPixel(i, rrMeta.getBitsPerPixel(i));
      sizeZ[i] = rrMeta.getAxisLength(i, Axes.Z);
      sizeC[i] = rrMeta.getAxisLength(i, Axes.CHANNEL);
      sizeT[i] = rrMeta.getAxisLength(i, Axes.TIME);
    }

    // order may need to be adjusted
    for (int i=0; i<imageCount; i++) {
      AxisGuesser ag = externals[getExternalSeries(i)].getAxisGuesser();
      FormatTools.setDimensionOrder(core, i,
        FormatTools.findDimensionList(ag.getAdjustedOrder()));
      core.setOrderCertain(i, ag.isCertain());
      computeAxisLengths(i);
    }
  }

  // -- Helper methods --

  private int getExternalSeries(int currentSeries) {
    if (getReader().getImageCount() > 1) return 0;
    return currentSeries;
  }

  /** Computes axis length arrays, and total axis lengths. */
  protected void computeAxisLengths(int imageIndex) throws FormatException {
    int sno = imageIndex;
    ExternalSeries s = externals[getExternalSeries(imageIndex)];
    FilePattern p = s.getFilePattern();

    int[] count = p.getCount();

    initReader(sno, 0);

    AxisGuesser ag = s.getAxisGuesser();
    int[] axes = ag.getAxisTypes();

    int numZ = ag.getAxisCountZ();
    int numC = ag.getAxisCountC();
    int numT = ag.getAxisCountT();

    if (axes.length == 0 && s.getFiles().length > 1) {
      axes = new int[] {AxisGuesser.T_AXIS};
      count = new int[] {s.getFiles().length};
      numT++;
    }

    core.setAxisLength(sno, Axes.Z, sizeZ[sno]);
    core.setAxisLength(sno, Axes.CHANNEL, sizeC[sno]);
    core.setAxisLength(sno, Axes.TIME, sizeT[sno]);
    lenZ[sno] = new int[numZ + 1];
    lenC[sno] = new int[numC + 1];
    lenT[sno] = new int[numT + 1];
    lenZ[sno][0] = sizeZ[sno];
    lenC[sno][0] = sizeC[sno];
    lenT[sno][0] = sizeT[sno];

    for (int i=0, z=1, c=1, t=1; i<count.length; i++) {
      switch (axes[i]) {
        case AxisGuesser.Z_AXIS:
          core.setAxisLength(sno, Axes.Z, core.getAxisLength(sno, Axes.Z) * count[i]);
          lenZ[sno][z++] = count[i];
          break;
        case AxisGuesser.C_AXIS:
          core.setAxisLength(sno, Axes.CHANNEL, core.getAxisLength(sno, Axes.CHANNEL) * count[i]);
          lenC[sno][c++] = count[i];
          break;
        case AxisGuesser.T_AXIS:
          core.setAxisLength(sno, Axes.TIME, core.getAxisLength(sno, Axes.TIME) * count[i]);
          lenT[sno][t++] = count[i];
          break;
        case AxisGuesser.S_AXIS:
          break;
        default:
          throw new FormatException("Unknown axis type for axis #" +
            i + ": " + axes[i]);
      }
    }

    int[] cLengths = datasetMeta().getChannelDimLengths(sno);
    String[] cTypes = datasetMeta().getChannelDimTypes(sno);
    int cCount = 0;
    for (int i=0; i<cLengths.length; i++) {
      if (cLengths[i] > 1) cCount++;
    }
    for (int i=1; i<lenC[sno].length; i++) {
      if (lenC[sno][i] > 1) cCount++;
    }
    if (cCount == 0) {
      core.setChannelDimLengths(sno, new int[] {1});
      core.setChannelDimTypes(sno, new String[] {FormatTools.CHANNEL});
    }
    else {
      core.setChannelDimLengths(sno, new int[cCount]);
      core.setChannelDimTypes(sno, new String[cCount]);
    }
    int c = 0;
    for (int i=0; i<cLengths.length; i++) {
      if (cLengths[i] == 1) continue;
      cLengths[c] = cLengths[i];
      cTypes[c] = cTypes[i];
      c++;
    }
    for (int i=1; i<lenC[sno].length; i++) {
      if (lenC[sno][i] == 1) continue;
      cLengths[c] = lenC[sno][i];
      cTypes[c] = FormatTools.CHANNEL;
    }
    int[] oldCLengths = core.getChannelDimLengths(sno);
    String[] oldCTypes = core.getChannelDimTypes(sno);
    
    for(; c<oldCLengths.length; c++) {
      cLengths[c] = oldCLengths[c];
      cTypes[c] = oldCTypes[c];
    }
    
    core.setChannelDimLengths(sno, cLengths);
    core.setChannelDimTypes(sno, cTypes);
  }

  /**
   * Gets the file index, and image index into that file,
   * corresponding to the given global image index.
   *
   * @return An array of size 2, dimensioned {file index, image index}.
   */
  protected int[] computeIndices(int imageIndex, int planeIndex) throws FormatException, IOException {
    if (noStitch) return new int[] {0, planeIndex};
    int sno = imageIndex;
    ExternalSeries s = externals[getExternalSeries(imageIndex)];

    int[] axes = s.getAxisGuesser().getAxisTypes();
    int[] count = s.getFilePattern().getCount();

    // get Z, C and T positions
    int[] zct = getZCTCoords(imageIndex, planeIndex);
    int[] posZ = FormatTools.rasterToPosition(lenZ[sno], zct[0]);
    int[] posC = FormatTools.rasterToPosition(lenC[sno], zct[1]);
    int[] posT = FormatTools.rasterToPosition(lenT[sno], zct[2]);

    int[] tmpZ = new int[posZ.length];
    System.arraycopy(posZ, 0, tmpZ, 0, tmpZ.length);
    int[] tmpC = new int[posC.length];
    System.arraycopy(posC, 0, tmpC, 0, tmpC.length);
    int[] tmpT = new int[posT.length];
    System.arraycopy(posT, 0, tmpT, 0, tmpT.length);

    // convert Z, C and T position lists into file index and image index
    int[] pos = new int[axes.length];
    int z = 1, c = 1, t = 1;
    for (int i=0; i<axes.length; i++) {
      if (axes[i] == AxisGuesser.Z_AXIS) pos[i] = posZ[z++];
      else if (axes[i] == AxisGuesser.C_AXIS) pos[i] = posC[c++];
      else if (axes[i] == AxisGuesser.T_AXIS) pos[i] = posT[t++];
      else if (axes[i] == AxisGuesser.S_AXIS) {
        pos[i] = 0;
      }
      else {
        throw new FormatException("Unknown axis type for axis #" +
          i + ": " + axes[i]);
      }
    }

    int fno = FormatTools.positionToRaster(count, pos);
    DimensionSwapper r = getExternalsReader(sno, fno);
    DatasetMetadata datasetMeta = r.getDatasetMetadata();
    
    int ino;
    if (posZ[0] < datasetMeta.getAxisLength(sno, Axes.Z) && 
      posC[0] <  datasetMeta.getAxisLength(sno, Axes.CHANNEL) &&
      posT[0] <  datasetMeta.getAxisLength(sno, Axes.TIME))
    {
      if (datasetMeta.isRGB(sno) && (posC[0] * datasetMeta.getRGBChannelCount(sno) >= lenC[sno][0])) {
        posC[0] /= lenC[sno][0];
      }
      ino = FormatTools.getIndex(r, sno, posZ[0], posC[0], posT[0]);
    }
    else ino = Integer.MAX_VALUE; // coordinates out of range

    return new int[] {fno, ino};
  }

  protected void initReader(int imageIndex, int fno) {
    int external = getExternalSeries(imageIndex);
    DimensionSwapper r = externals[external].getReaders()[fno];
    DatasetMetadata c = r.getDatasetMetadata();
    try {
      if (r.getCurrentFile() == null) {
        r.setGroupFiles(false);
      }
      r.setSource(externals[external].getFiles()[fno]);
      List<AxisType> axes = ((DimensionSwapper) getReader()).getInputOrder(imageIndex);
      
      String newOrder = FormatTools.findDimensionOrder(axes.toArray(new AxisType[axes.size()]));
      if ((externals[external].getFiles().length > 1 || !c.isOrderCertain(imageIndex)) &&
        (c.getRGBChannelCount(imageIndex) == 1 ||
        newOrder.indexOf("C") == FormatTools.findDimensionOrder(c, imageIndex).indexOf("C")))
      {
        r.swapDimensions(imageIndex, Arrays.asList(FormatTools.findDimensionList(newOrder)));
      }
      r.setOutputOrder(imageIndex, Arrays.asList(FormatTools.findDimensionList(newOrder)));
    }
    catch (IOException e) {
      LOGGER.debug("", e);
    }
  }

  // -- Helper classes --

  class ExternalSeries<M> {
    private DimensionSwapper[] readers;
    private String[] files;
    private FilePattern pattern;
    private byte[] blankThumbBytes;
    private String originalOrder;
    private AxisGuesser ag;
    private int imagesPerFile;

    public ExternalSeries(FilePattern pattern)
      throws FormatException, IOException
    {
      this.pattern = pattern;
      files = this.pattern.getFiles();

      readers = new DimensionSwapper[files.length];
      for (int i=0; i<readers.length; i++) {
        readers[i] = new DimensionSwapper();
        readers[i].setGroupFiles(false);
      }
      readers[0].setSource(files[0]);

      DatasetMetadata c = readers[0].getDatasetMetadata();
      
      ag = new AxisGuesser(this.pattern, FormatTools.findDimensionOrder(c, 0),
        c.getAxisLength(0, Axes.Z), c.getAxisLength(0, Axes.TIME),
        c.getAxisLength(0, Axes.CHANNEL), c.isOrderCertain(0));

      blankThumbBytes = new byte[FormatTools.getPlaneSize(readers[0], 0,
        c.getThumbSizeX(0), c.getThumbSizeY(0))];

      originalOrder = FormatTools.findDimensionOrder(c, 0);
      imagesPerFile = readers[0].getImageCount();
    }

    public DimensionSwapper[] getReaders() {
      return readers;
    }

    public FilePattern getFilePattern() {
      return pattern;
    }

    public String getOriginalOrder() {
      return originalOrder;
    }

    public AxisGuesser getAxisGuesser() {
      return ag;
    }

    public byte[] getBlankThumbBytes() {
      return blankThumbBytes;
    }

    public String[] getFiles() {
      return files;
    }

    public int getImagesPerFile() {
      return imagesPerFile;
    }

  }

}
