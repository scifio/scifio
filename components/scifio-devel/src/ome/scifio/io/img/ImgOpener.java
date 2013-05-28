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

package ome.scifio.io.img;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import ome.scifio.AbstractHasSCIFIO;
import ome.scifio.FormatException;
import ome.scifio.HasColorTable;
import ome.scifio.Metadata;
import ome.scifio.Plane;
import ome.scifio.Reader;
import ome.scifio.common.DataTools;
import ome.scifio.common.StatusEvent;
import ome.scifio.common.StatusListener;
import ome.scifio.common.StatusReporter;
import ome.scifio.filters.ChannelFiller;
import ome.scifio.filters.ChannelSeparator;
import ome.scifio.filters.ReaderFilter;
import ome.scifio.io.img.cell.SCIFIOCellImgFactory;
import ome.scifio.services.InitializeService;
import ome.scifio.services.TranslatorService;
import ome.scifio.util.FormatTools;
import ome.xml.filters.MinMaxFilter;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.services.OMEXMLService;

import org.scijava.Context;
import org.scijava.InstantiableException;

/**
 * Reads in an {@link ImgPlus} using SCIFIO.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class ImgOpener extends AbstractHasSCIFIO implements StatusReporter {
  
  // -- Constants --
  
  // % of available memory which an image can not exceed to be opened as a PlanarImg
  private static final double PLANAR_THRESHOLD = 0.75;

  // -- Fields --

  private final List<StatusListener> listeners = new ArrayList<StatusListener>();

  // -- static methods --

  /**
   * Opens an {@link Img} in a format it is in (unsigned byte, float, int, ...)
   * using the respective {@link RealType}. It returns an {@link ImgPlus} which
   * contains the Calibration and name.
   * 
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg} or {@link CellImg}.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null
   * 
   * @throws ImgIOException
   *           - if file could not be found, if it is too big for the memory or
   *           if it is incompatible with the opener
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(
    final String id, int imageIndex) throws ImgIOException {
    return open(id, imageIndex, null);
  }

  /**
   * Opens an {@link Img} as {@link FloatType}. It returns an {@link ImgPlus}
   * which contains the Calibration and name.
   * 
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg} or {@link CellImg}.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null
   * 
   * @throws ImgIOException
   *           - if file could not be found or is too big for the memory
   */
  public static ImgPlus<FloatType> openFloat(final String id, int imageIndex)
    throws ImgIOException {
    return open(id, imageIndex, new FloatType());
  }

  /**
   * Opens an {@link Img} as {@link DoubleType}. It returns an {@link ImgPlus}
   * which contains the Calibration and name.
   * 
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg} or {@link CellImg}.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null
   * 
   * @throws ImgIOException
   *           - if file could not be found or is too big for the memory
   */
  public static ImgPlus<DoubleType> openDouble(final String id, int imageIndex)
    throws ImgIOException {
    return open(id, imageIndex, new DoubleType());
  }

  /**
   * Opens an {@link Img} in a format it is in (unsigned byte, float, int, ...)
   * using the respective {@link RealType}. It returns an {@link ImgPlus} which
   * contains the Calibration and name.
   * 
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg} or {@link CellImg}.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type - the real type to use to open the image
   * @return - the {@link ImgPlus} or null
   * 
   * @throws ImgIOException
   *           - if file could not be found, if it is too big for the memory or
   *           if it is incompatible with the opener
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(
    final String id, int imageIndex, T type) throws ImgIOException {
    ImgOpener opener = new ImgOpener();
    try {
      return opener.openImg(id, imageIndex, type);
    } catch (Exception e) {
      throw new ImgIOException("Cannot open file '" + id + "': " + e);
    }
  }
  
  // -- Constructors --

  public ImgOpener() {
    this(new Context(InitializeService.class, TranslatorService.class,
        OMEXMLService.class));
  }

  public ImgOpener(Context ctx) {
    setContext(ctx);
  }

  // -- ImgOpener methods --
  
  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final String id, final int imageIndex) throws ImgIOException {
    return openImg(id, imageIndex, null, false);
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether the dataset source can be read while determining file compatibility.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final String id, int imageIndex, final boolean openFile)
    throws ImgIOException {
    return openImg(id, imageIndex, null, openFile, true);
  }
  
  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether or not to compute min/max pixel values for the image.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
      final String id, int imageIndex, final boolean openFile,
      final boolean computeMinMax) throws ImgIOException {
    return openImg(id, imageIndex, null, openFile, computeMinMax, null);
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether or not to force the {@link Img} return type.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @param openPlanar - ternary flag. If true, a PlanarImg will be returned.
   *     If false, a CellImg. If null, ImgOpener will use a heuristic based
   *     on current available memory.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
      final String id, int imageIndex, final boolean openFile,
      final boolean computeMinMax, final Boolean openPlanar) throws ImgIOException {
    return openImg(id, imageIndex, null, openFile, computeMinMax, openPlanar);
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final String id, final int imageIndex, final T type) throws ImgIOException {
    return openImg(id, imageIndex, type, false);
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether the dataset source can be read while determining file compatibility.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final String id, int imageIndex, T type, final boolean openFile)
    throws ImgIOException {
    return openImg(id, imageIndex, type, openFile, true);
  }
  
  
  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether or not to compute min/max pixel values for the image.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
      final String id, int imageIndex, T type, final boolean openFile,
      final boolean computeMinMax) throws ImgIOException {
    return openImg(id, imageIndex, type, openFile, computeMinMax, null);
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify whether or not to force the {@link Img} return type.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}.  If null, it will
   *          be derived at this point.
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @param openPlanar - ternary flag. If true, a PlanarImg will be returned.
   *     If false, a CellImg. If null, ImgOpener will use a heuristic based
   *     on current available memory.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final String id, int imageIndex, T type, final boolean openFile,
    final boolean computeMinMax, final Boolean openPlanar) throws ImgIOException {
    try {
      final Reader r = initializeReader(id, openFile, computeMinMax);
      if (type == null) type = ImgIOUtils.makeType(r.getMetadata().getPixelType(
          imageIndex));
      return openImg(r, imageIndex, type, computeMinMax, openPlanar);
    } catch (final FormatException e) {
      throw new ImgIOException(e);
    } catch (final IOException e) {
      throw new ImgIOException(e);
    }
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. The
   * provided Reader is used to open the actual pixels.
   * 
   * @param id
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @param openPlanar - ternary flag. If true, a PlanarImg will be returned.
   *     If false, a CellImg. If null, ImgOpener will use a heuristic based
   *     on current available memory.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final Reader r, final int imageIndex, final T type,
    final boolean computeMinMax, final Boolean openPlanar) throws ImgIOException {
    try {
      ImgFactory<T> imgFactory = getFactory(r, type, openPlanar);
      return openImg(r, imageIndex, imgFactory, type, computeMinMax);
    } catch (IncompatibleTypeException e) {
      throw new ImgIOException(e);
    }
  }

  /**
   * Reads in an {@link ImgPlus} from the given initialized {@link Reader},
   * using the given {@link ImgFactory} to construct the {@link Img}. The
   * {@link Type} T to read is defined by the third parameter.
   * 
   * @param r
   *          An initialized {@link Reader} to use for reading image data.
   * @param imgFactory
   *          The {@link ImgFactory} to use for creating the resultant
   *          {@link ImgPlus}.
   * @param type
   *          The {@link Type} T of the output {@link ImgPlus}, which must match
   *          the typing of the {@link ImgFactory}.
   * @param openFile - if true, the underlying dataset can be opened while checking
   *     compatibility.
   * @param computeMinMax - if true, will the min/max values of the dataset will
   *     be available.
   * @param openPlanar - ternary flag. If true, a PlanarImg will be returned.
   *     If false, a CellImg. If null, ImgOpener will use a heuristic based
   *     on current available memory.
   * @return - the {@link ImgPlus} or null

   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
    final Reader r, final int imageIndex, final ImgFactory<T> imgFactory,
    final T type, final boolean computeMinMax) throws ImgIOException {
    // create image and read metadata
    final long[] dimLengths = getDimLengths(r.getMetadata(), imageIndex);
    if (SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
      ((SCIFIOCellImgFactory<T>) imgFactory).setReader(r);
    }
    final Img<T> img = imgFactory.create(dimLengths, type);
    final ImgPlus<T> imgPlus = makeImgPlus(img, r);

    String id = r.getCurrentFile();
    imgPlus.setSource(id);
    imgPlus.initializeColorTables(r.getPlaneCount(imageIndex));
    
    
    // If we have a planar img, read the planes now. Otherwise they
    // will be read on demand.
    if (PlanarImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
      final float startTime = System.currentTimeMillis();
      final int planeCount = r.getPlaneCount(imageIndex);
      try {
        readPlanes(r, imageIndex, type, imgPlus, computeMinMax);
      } catch (final FormatException e) {
        throw new ImgIOException(e);
      } catch (final IOException e) {
        throw new ImgIOException(e);
      }
      final long endTime = System.currentTimeMillis();
      final float time = (endTime - startTime) / 1000f;
      notifyListeners(new StatusEvent(planeCount, planeCount, id +
          ": read " + planeCount + " planes in " + time + "s"));
    }

    return imgPlus;
  }

  // -- StatusReporter methods --

  /** Adds a listener to those informed when progress occurs. */
  public void addStatusListener(final StatusListener l) {
    synchronized (listeners) {
      listeners.add(l);
    }
  }

  /** Removes a listener from those informed when progress occurs. */
  public void removeStatusListener(final StatusListener l) {
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

  /** Notifies registered listeners of progress. */
  public void notifyListeners(final StatusEvent e) {
    synchronized (listeners) {
      for (final StatusListener l : listeners)
        l.statusUpdated(e);
    }
  }

  public static Reader createReader(ImgOpener io, final String id,
    final boolean openFile, final boolean computeMinMax)
    throws FormatException, IOException {
    ReaderFilter r = io.scifio().initializer().initializeReader(id, openFile);

    try {
      r.enable(ChannelSeparator.class);
      if (computeMinMax)
        r.enable(MinMaxFilter.class);
    } catch (InstantiableException e) {
      throw new FormatException(e);
    }
    return r;
  }

  /** Compiles an N-dimensional list of axis lengths from the given reader. */
  public static long[] getDimLengths(final Metadata m, int imageIndex) {
    final long sizeX = m.getAxisLength(imageIndex, Axes.X);
    final long sizeY = m.getAxisLength(imageIndex, Axes.Y);
    final long sizeZ = m.getAxisLength(imageIndex, Axes.Z);
    final long sizeT = m.getAxisLength(imageIndex, Axes.TIME);
    // final String[] cDimTypes = r.getChannelDimTypes();
    // final int[] cDimLengths = m.getChannelDimLengths(0);
    final long sizeC = m.getEffectiveSizeC(imageIndex);
    String dimOrder = FormatTools.findDimensionOrder(m, imageIndex);

    final List<Long> dimLengthsList = new ArrayList<Long>();

    // add core dimensions
    for (int i = 0; i < dimOrder.length(); i++) {
      final char dim = dimOrder.charAt(i);
      switch (dim) {
      case 'X':
        if (sizeX > 0)
          dimLengthsList.add(sizeX);
        break;
      case 'Y':
        if (sizeY > 0)
          dimLengthsList.add(sizeY);
        break;
      case 'Z':
        if (sizeZ > 1)
          dimLengthsList.add(sizeZ);
        break;
      case 'T':
        if (sizeT > 1)
          dimLengthsList.add(sizeT);
        break;
      case 'C':
        if (sizeC > 1)
          dimLengthsList.add(sizeC);
        break;
      }
    }

    // convert result to primitive array
    final long[] dimLengths = new long[dimLengthsList.size()];
    for (int i = 0; i < dimLengths.length; i++) {
      dimLengths[i] = dimLengthsList.get(i);
    }
    return dimLengths;
  }

  // -- Helper methods --

  /*
   * Returns a PlanarImgFactory if it will fit in memory. Otherwise a SCIFIOCellImgFactory.
   */
  private <T extends RealType<T> & NativeType<T>> ImgFactory<T> getFactory(Reader r, T type, Boolean openPlanar)
    throws IncompatibleTypeException
  {
    
    ImgFactory<T> tmpFactory = null;
    
    long freeMem = Runtime.getRuntime().freeMemory();
    long neededMem = r.getMetadata().getDatasetSize();
    
    boolean planar = openPlanar == null ? freeMem * PLANAR_THRESHOLD > neededMem : openPlanar;

    if (planar) 
      tmpFactory = new PlanarImgFactory<T>();
    else
      tmpFactory = new SCIFIOCellImgFactory<T>();

    return tmpFactory.imgFactory(type);
  }

  /**
   * Constructs and initializes a SCIFIO reader for the given file.
   * 
   * @param computeMinMax
   */
  private Reader initializeReader(final String id, final boolean openFile,
    boolean computeMinMax) throws FormatException, IOException {
    notifyListeners(new StatusEvent("Initializing " + id));

    return createReader(this, id, openFile, computeMinMax);
  }

  /** Compiles an N-dimensional list of axis types from the given reader. */
  private AxisType[] getDimTypes(final Metadata m) {
    final int sizeX = m.getAxisLength(0, Axes.X);
    final int sizeY = m.getAxisLength(0, Axes.Y);
    final int sizeZ = m.getAxisLength(0, Axes.Z);
    final int sizeT = m.getAxisLength(0, Axes.TIME);
    final String[] cDimTypes = m.getChannelDimTypes(0);
    final String dimOrder = FormatTools.findDimensionOrder(m, 0);
    final List<AxisType> dimTypes = new ArrayList<AxisType>();

    // add core dimensions
    for (final char dim : dimOrder.toCharArray()) {
      switch (dim) {
      case 'X':
        if (sizeX > 1)
          dimTypes.add(Axes.X);
        break;
      case 'Y':
        if (sizeY > 1)
          dimTypes.add(Axes.Y);
        break;
      case 'Z':
        if (sizeZ > 1)
          dimTypes.add(Axes.Z);
        break;
      case 'T':
        if (sizeT > 1)
          dimTypes.add(Axes.TIME);
        break;
      case 'C':
        if (cDimTypes.length > 0)
          dimTypes.add(Axes.CHANNEL);
        break;
      }
    }

    return dimTypes.toArray(new AxisType[0]);
  }

  /** Compiles an N-dimensional list of calibration values. */
  private double[] getCalibration(final Metadata m) {
    final int sizeX = m.getAxisLength(0, Axes.X);
    final int sizeY = m.getAxisLength(0, Axes.Y);
    final int sizeZ = m.getAxisLength(0, Axes.Z);
    final int sizeT = m.getAxisLength(0, Axes.TIME);
    final int[] cDimLengths = m.getChannelDimLengths(0);
    final String dimOrder = FormatTools.findDimensionOrder(m, 0);

    final OMEMetadata meta = new OMEMetadata();
    meta.setContext(getContext());
    scifio().translator().translate(m, meta, false);

    final PositiveFloat xCalin = meta.getRoot().getPixelsPhysicalSizeX(0);
    final PositiveFloat yCalin = meta.getRoot().getPixelsPhysicalSizeY(0);
    final PositiveFloat zCalin = meta.getRoot().getPixelsPhysicalSizeZ(0);
    Double tCal = meta.getRoot().getPixelsTimeIncrement(0);

    final Double xCal, yCal, zCal;

    if (xCalin == null)
      xCal = 1.0;
    else
      xCal = xCalin.getValue();

    if (yCalin == null)
      yCal = 1.0;
    else
      yCal = yCalin.getValue();

    if (zCalin == null)
      zCal = 1.0;
    else
      zCal = zCalin.getValue();

    if (tCal == null)
      tCal = 1.0;

    final List<Double> calibrationList = new ArrayList<Double>();

    // add core dimensions
    for (int i = 0; i < dimOrder.length(); i++) {
      final char dim = dimOrder.charAt(i);
      switch (dim) {
      case 'X':
        if (sizeX > 1)
          calibrationList.add(xCal);
        break;
      case 'Y':
        if (sizeY > 1)
          calibrationList.add(yCal);
        break;
      case 'Z':
        if (sizeZ > 1)
          calibrationList.add(zCal);
        break;
      case 'T':
        if (sizeT > 1)
          calibrationList.add(tCal);
        break;
      case 'C':
        for (int c = 0; c < cDimLengths.length; c++) {
          final long len = cDimLengths[c];
          if (len > 1)
            calibrationList.add(1.0);
        }
        break;
      }
    }

    // convert result to primitive array
    final double[] calibration = new double[calibrationList.size()];
    for (int i = 0; i < calibration.length; i++) {
      calibration[i] = calibrationList.get(i);
    }
    return calibration;
  }

  /**
   * Wraps the given {@link Img} in an {@link ImgPlus} with metadata
   * corresponding to the specified initialized {@link Reader}.
   */
  private <T extends RealType<T>> ImgPlus<T> makeImgPlus(final Img<T> img,
    final Reader r) throws ImgIOException {
    final String id = r.getCurrentFile();
    final File idFile = new File(id);
    final String name = idFile.exists() ? idFile.getName() : id;

    final AxisType[] dimTypes = getDimTypes(r.getMetadata());
    final double[] cal = getCalibration(r.getMetadata());

    final Reader base;
    try {
      base = unwrap(r);
    } catch (final FormatException exc) {
      throw new ImgIOException(exc);
    } catch (final IOException exc) {
      throw new ImgIOException(exc);
    }

    Metadata meta = r.getMetadata();
    final int rgbChannelCount = base.getMetadata().getRGBChannelCount(0);
    final int validBits = meta.getBitsPerPixel(0);

    final ImgPlus<T> imgPlus = new SCIFIOImgPlus<T>(img, name, dimTypes, cal);
    imgPlus.setValidBits(validBits);

    int compositeChannelCount = rgbChannelCount;
    if (rgbChannelCount == 1) {
      // HACK: Support ImageJ color mode embedded in TIFF files.
      final String colorMode = (String) meta.getTable().get("Color mode");
      if ("composite".equals(colorMode)) {
        compositeChannelCount = meta.getAxisLength(0, Axes.CHANNEL);
      }
    }
    imgPlus.setCompositeChannelCount(compositeChannelCount);

    return imgPlus;
  }

  /**
   * Finds the lowest level wrapped reader, preferably a {@link ChannelFiller},
   * but otherwise the base reader. This is useful for determining whether the
   * input data is intended to be viewed with multiple channels composited
   * together.
   */
  private Reader unwrap(final Reader r) throws FormatException, IOException {
    if (!(r instanceof ReaderFilter))
      return r;
    final ReaderFilter rf = (ReaderFilter) r;
    return rf.getTail();
  }

  /**
   * Reads planes from the given initialized {@link Reader} into the specified
   * {@link Img}.
   */
  private <T extends RealType<T>> void readPlanes(final Reader r,
    int imageIndex, final T type, final ImgPlus<T> imgPlus,
    final boolean computeMinMax) throws FormatException, IOException {
    // TODO - create better container types; either:
    // 1) an array container type using one byte array per plane
    // 2) as #1, but with an Reader reference reading planes on demand
    // 3) as PlanarRandomAccess, but with an Reader reference
    // reading planes on demand

    // PlanarRandomAccess is useful for efficient access to pixels in ImageJ
    // (e.g., getPixels)
    // #1 is useful for efficient SCIFIO import, and useful for tools
    // needing byte arrays (e.g., BufferedImage Java3D texturing by reference)
    // #2 is useful for efficient memory use for tools wanting matching
    // primitive arrays (e.g., virtual stacks in ImageJ)
    // #3 is useful for efficient memory use

    // get container
    final PlanarAccess<?> planarAccess = ImgIOUtils.getPlanarAccess(imgPlus);
    final T inputType = ImgIOUtils.makeType(r.getMetadata().getPixelType(0));
    final T outputType = type;
    final boolean compatibleTypes = outputType.getClass().isAssignableFrom(
        inputType.getClass());

    // populate planes
    final int planeCount = r.getMetadata().getPlaneCount(imageIndex);
    final boolean isPlanar = planarAccess != null && compatibleTypes;

    Plane plane = null;
    for (int planeIndex = 0; planeIndex < planeCount; planeIndex++) {
      
      //TODO replace with StatusService use... needs to trigger in 
      notifyListeners(new StatusEvent(planeIndex, planeCount, "Reading plane "
          + (planeIndex + 1) + "/" + planeCount));
      if (plane == null)
        plane = r.openPlane(0, planeIndex);
      else {
        r.openPlane(0, planeIndex, plane);
      }
      if (isPlanar)
        populatePlane(r, planeIndex, plane.getBytes(), planarAccess);
      else
        populatePlane(r, imageIndex, planeIndex, plane.getBytes(), imgPlus);

      // store color table
      if (HasColorTable.class.isAssignableFrom(r.getMetadata().getClass())) {
        imgPlus.setColorTable(
            ((HasColorTable) r.getMetadata()).getColorTable(imageIndex, 0), planeIndex);
      }
    }
    if (computeMinMax)
      populateMinMax(r, imgPlus, imageIndex);
  }

  /** Populates plane by reference using {@link PlanarAccess} interface. */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void populatePlane(final Reader r, final int no, final byte[] plane,
    final PlanarAccess planarAccess) {
    Metadata m = r.getMetadata();
    final int pixelType = m.getPixelType(0);
    final int bpp = FormatTools.getBytesPerPixel(pixelType);
    final boolean fp = FormatTools.isFloatingPoint(pixelType);
    final boolean little = m.isLittleEndian(0);
    Object planeArray = DataTools.makeDataArray(plane, bpp, fp, little);
    if (planeArray == plane) {
      // array was returned by reference; make a copy
      final byte[] planeCopy = new byte[plane.length];
      System.arraycopy(plane, 0, planeCopy, 0, plane.length);
      planeArray = planeCopy;
    }
    planarAccess.setPlane(no, ImgIOUtils.makeArray(planeArray));
  }

  /**
   * Uses a cursor to populate the plane. This solution is general and works
   * regardless of container, but at the expense of performance both now and
   * later.
   */
  private <T extends RealType<T>> void populatePlane(final Reader r,
    int imageIndex, final int planeIndex, final byte[] plane,
    final ImgPlus<T> img) {
    Metadata m = r.getMetadata();
    final int pixelType = m.getPixelType(imageIndex);
    final boolean little = m.isLittleEndian(imageIndex);

    final long[] dimLengths = getDimLengths(m, imageIndex);
    final long[] pos = new long[dimLengths.length];

    final int planeX = 0;
    final int planeY = 1;

    getPosition(r, planeIndex, pos);

    final int sX = (int) img.dimension(0);
    final int sY = (int) img.dimension(1);

    final RandomAccess<T> randomAccess = img.randomAccess();

    int index = 0;

    for (int y = 0; y < sY; ++y) {
      pos[planeX] = 0;
      pos[planeY] = y;

      randomAccess.setPosition(pos);

      for (int x = 1; x < sX; ++x) {
        randomAccess.get().setReal(
            decodeWord(plane, index++, pixelType, little));
        randomAccess.fwd(planeX);
      }

      randomAccess.get().setReal(decodeWord(plane, index++, pixelType, little));
    }
  }

  private void populateMinMax(final Reader r, final ImgPlus<?> imgPlus,
    int imageIndex) throws FormatException, IOException {
    final int sizeC = r.getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);
    final ReaderFilter rf = (ReaderFilter) r;
    MinMaxFilter minMax = null;
    try {
      minMax = rf.enable(MinMaxFilter.class);
    } catch (InstantiableException e) {
      throw new FormatException(e);
    }
    for (int c = 0; c < sizeC; c++) {
      final Double min = minMax.getChannelKnownMinimum(imageIndex, c);
      final Double max = minMax.getChannelKnownMaximum(imageIndex, c);
      imgPlus.setChannelMinimum(c, min == null ? Double.NaN : min);
      imgPlus.setChannelMaximum(c, max == null ? Double.NaN : max);
    }
  }

  /** Copies the current dimensional position into the given array. */
  private void getPosition(final Reader r, final int no, final long[] pos) {
    Metadata m = r.getMetadata();
    final long sizeX = m.getAxisLength(0, Axes.X);
    final long sizeY = m.getAxisLength(0, Axes.Y);
    final long sizeZ = m.getAxisLength(0, Axes.Z);
    final long sizeT = m.getAxisLength(0, Axes.TIME);
    final int[] cDimLengths = m.getChannelDimLengths(0);
    final String dimOrder = FormatTools.findDimensionOrder(m, 0);

    final int[] zct = FormatTools.getZCTCoords(m, 0, no);

    int index = 0;
    for (int i = 0; i < dimOrder.length(); i++) {
      final char dim = dimOrder.charAt(i);
      switch (dim) {
      case 'X':
        if (sizeX > 1)
          index++; // NB: Leave X axis position alone.
        break;
      case 'Y':
        if (sizeY > 1)
          index++; // NB: Leave Y axis position alone.
        break;
      case 'Z':
        if (sizeZ > 1)
          pos[index++] = zct[0];
        break;
      case 'T':
        if (sizeT > 1)
          pos[index++] = zct[2];
        break;
      case 'C':
        final int[] cPos = FormatTools.rasterToPosition(cDimLengths, zct[1]);
        for (int c = 0; c < cDimLengths.length; c++) {
          if (cDimLengths[c] > 1)
            pos[index++] = cPos[c];
        }
        break;
      }
    }
  }

  private static double decodeWord(final byte[] plane, final int index,
    final int pixelType, final boolean little) {
    final double value;
    switch (pixelType) {
    case FormatTools.UINT8:
      value = plane[index] & 0xff;
      break;
    case FormatTools.INT8:
      value = plane[index];
      break;
    case FormatTools.UINT16:
      value = DataTools.bytesToShort(plane, 2 * index, 2, little) & 0xffff;
      break;
    case FormatTools.INT16:
      value = DataTools.bytesToShort(plane, 2 * index, 2, little);
      break;
    case FormatTools.UINT32:
      value = DataTools.bytesToInt(plane, 4 * index, 4, little) & 0xffffffffL;
      break;
    case FormatTools.INT32:
      value = DataTools.bytesToInt(plane, 4 * index, 4, little);
      break;
    case FormatTools.FLOAT:
      value = DataTools.bytesToFloat(plane, 4 * index, 4, little);
      break;
    case FormatTools.DOUBLE:
      value = DataTools.bytesToDouble(plane, 8 * index, 8, little);
      break;
    default:
      value = Double.NaN;
    }
    return value;
  }

}
