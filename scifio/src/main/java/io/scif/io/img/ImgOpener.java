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

package io.scif.io.img;

import io.scif.AbstractHasSCIFIO;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.common.DataTools;
import io.scif.filters.ChannelFiller;
import io.scif.filters.ChannelSeparator;
import io.scif.filters.MinMaxFilter;
import io.scif.filters.ReaderFilter;
import io.scif.io.img.ImgOptions.CheckMode;
import io.scif.io.img.ImgOptions.ImgType;
import io.scif.io.img.ImgOptions.Subregion;
import io.scif.io.img.cell.SCIFIOCellImgFactory;
import io.scif.services.InitializeService;
import io.scif.services.TranslatorService;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.cell.AbstractCellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.app.StatusService;

/**
 * Reads in an {@link ImgPlus} using SCIFIO.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class ImgOpener extends AbstractHasSCIFIO {
  
  // -- Constants --
  
  // % of available memory to trigger opening as a CellImg, if surpassed
  private static final double MEMORY_THRESHOLD = 0.75;
  
  // -- Constructors --

  public ImgOpener() {
    this(new Context(StatusService.class, InitializeService.class, TranslatorService.class));
  }

  public ImgOpener(Context ctx) {
    setContext(ctx);
  }

  // -- ImgOpener methods --
  
  /**
  * @param source
   *          - the location of the dataset to assess
   * @return The number of images in the specified
   * dataset.
   */
  public int getImageCount(String source)
    throws ImgIOException
  {
    try {
      Format format = scifio().format().getFormat(source);
      return format.createParser().parse(source).getImageCount();
    } catch (FormatException e) {
      throw new ImgIOException(e);
    } catch (IOException e) {
      throw new ImgIOException(e);
    }
  }

  /**
   * Reads in an {@link ImgPlus} from the first image of the given source.
   * 
   * @param source
   *          - the location of the dataset to open
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T> openImg(String source)
    throws ImgIOException
  { 
    return openImg(source, (T)null);
  }
  
  /**
   * Reads in an {@link ImgPlus} from the first image of the given source.
   * 
   * @param source
   *          - the location of the dataset to open
   * @param type -
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T> openImg(String source, T type) throws ImgIOException
  {
    return openImg(source, type, new ImgOptions());
  }

  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can
   * specify a variety of {@link ImgOptions}.
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type -
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @param imgOptions - {@link ImgOptions} to use when opening this dataset
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T> openImg(final String source, final ImgOptions imgOptions)
    throws ImgIOException
  {
    return openImg(source, null, imgOptions);
  }
  
  /**
   * Reads in an {@link ImgPlus} from the specified index of the given source. Can specify
   * the Type that should be opened.
   * 
   * @param source
   *          - the location of the dataset to open
   * @param type -
   *          The {@link Type} T of the output {@link ImgPlus}.
   * @param imgOptions - {@link ImgOptions} to use when opening this dataset
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T> openImg(
    final String source, T type, final ImgOptions imgOptions)
    throws ImgIOException {
    try {
      final Reader r = createReader(source, imgOptions);
      if (type == null) type = ImgIOUtils.makeType(r.getMetadata().getPixelType(imgOptions.getIndex()));
      
      @SuppressWarnings("unchecked")
      ImgFactory<T> imgFactory = (ImgFactory<T>)createFactory(r.getMetadata(), imgOptions);
      
      return openImg(r, type, imgFactory, imgOptions);
      
    } catch (final FormatException e) {
      throw new ImgIOException(e);
    } catch (final IOException e) {
      throw new ImgIOException(e);
    } catch (final IncompatibleTypeException e) {
      throw new ImgIOException(e);
    }
  }

  /**
   * @param source
   *          - the location of the dataset to open
   * @param imgFactory -
   *          The {@link ImgFactory} to use for creating the resultant
   *          {@link ImgPlus}.   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T>
    openImg(String source, ImgFactory<T> imgFactory)
    throws ImgIOException
  {
    return openImg(source, imgFactory, null);
  }
  
  /**
   * @param source
   *          - the location of the dataset to open
   * @param imgFactory -
   *          The {@link ImgFactory} to use for creating the resultant
   *          {@link ImgPlus}.
   * @param type -
   *          The {@link Type} T of the output {@link ImgPlus}, which must match
   *          the typing of the {@link ImgFactory}.
   * @return - the {@link ImgPlus} or null
   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T>
    openImg(String source, ImgFactory<T> imgFactory, T type) throws ImgIOException {
    
    Reader r;
    ImgOptions imgOptions =  new ImgOptions().setComputeMinMax(true);
    try {
      r = createReader(source, imgOptions);
      if (type == null) type = ImgIOUtils.makeType(r.getMetadata().getPixelType(imgOptions.getIndex()));
    } catch (IOException e) {
      throw new ImgIOException(e);
    } catch (FormatException e) {
      throw new ImgIOException(e);
    }
    
    return openImg(r, type, imgFactory, imgOptions);
  }

  /**
   * Reads in an {@link ImgPlus} from the given initialized {@link Reader},
   * using the given {@link ImgFactory} to construct the {@link Img}. The
   * {@link Type} T to read is defined by the third parameter.
   * 
   * @param reader -
   *          An initialized {@link Reader} to use for reading image data.
   * @param imgFactory -
   *          The {@link ImgFactory} to use for creating the resultant
   *          {@link ImgPlus}.
   * @param type -
   *          The {@link Type} T of the output {@link ImgPlus}, which must match
   *          the typing of the {@link ImgFactory}.
   * @param imgOptions - {@link ImgOptions} to use when opening this dataset
   * @return - the {@link ImgPlus} or null

   * @throws ImgIOException
   *           if there is a problem reading the image data.
   */
  public <T extends RealType<T>> ImgPlus<T> openImg(
    final Reader reader, final T type,
    final ImgFactory<T> imgFactory, final ImgOptions imgOptions) throws ImgIOException {
    final int imageIndex = imgOptions.getIndex();
    
    // create image and read metadata
    final long[] dimLengths = getDimLengths(reader.getMetadata(), imgOptions);
    if (SCIFIOCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
      ((SCIFIOCellImgFactory<?>) imgFactory).setReader(reader);
    }
    final Img<T> img = imgFactory.create(dimLengths, type);
    final ImgPlus<T> imgPlus = makeImgPlus(img, reader);

    String id = reader.getCurrentFile();
    imgPlus.setSource(id);
    imgPlus.initializeColorTables(reader.getPlaneCount(imageIndex));
    
    // If we have a planar img, read the planes now. Otherwise they
    // will be read on demand.
    if (AbstractCellImgFactory.class.isAssignableFrom(imgFactory.getClass())) {
      getContext().getService(StatusService.class).
      showStatus("Created CellImg for dynamic loading");
    }
    else {
      final float startTime = System.currentTimeMillis();
      final int planeCount = reader.getPlaneCount(imageIndex);
      try {
        readPlanes(reader, imageIndex, type, imgPlus, imgOptions);
      } catch (final FormatException e) {
        throw new ImgIOException(e);
      } catch (final IOException e) {
        throw new ImgIOException(e);
      }
      final long endTime = System.currentTimeMillis();
      final float time = (endTime - startTime) / 1000f;
      getContext().getService(StatusService.class).
        showStatus(id + ": read " + planeCount + " planes in " + time + "s");
    }

    return imgPlus;
  }
  
  // -- Helper methods --
  
  /**
   * @param m - Metadata for the associated dataset
   * @param imgOptions - Options to use when creating this Factory
   * @return - An initialized ImgFactory
   */
  private <T extends RealType<T> & NativeType<T>> ImgFactory<T> createFactory(Metadata m, final ImgOptions imgOptions)
    throws IncompatibleTypeException
  {
    ImgFactory<T> tmpFactory = null;
    
    T type = ImgIOUtils.makeType(m.getPixelType(0));
    
    boolean fitsInMemory = Runtime.getRuntime().freeMemory() * MEMORY_THRESHOLD > m.getDatasetSize();
    
    ImgType imgType = imgOptions.getImgType();
    
    if (!fitsInMemory && imgType.equals(ImgType.AUTO) || imgType.equals(ImgType.CELL))
      tmpFactory = new SCIFIOCellImgFactory<T>();
    else if (imgType.equals(ImgType.AUTO) || imgType.equals(ImgType.ARRAY))
      tmpFactory = new ArrayImgFactory<T>();
    else if (imgType.equals(ImgType.PLANAR))
      tmpFactory = new PlanarImgFactory<T>();

    return tmpFactory.imgFactory(type);
  }
  
  /**
   * @param io - An ImgOpener instance
   * @param source - Dataset source to open
   * @param imgOptions - Options object for opening this dataset
   * @return A Reader initialized to open the specified id
   */
  private Reader createReader(final String source,
    final ImgOptions imgOptions)
    throws FormatException, IOException {
    
    boolean openFile = imgOptions.getCheckMode().equals(CheckMode.DEEP);
    boolean computeMinMax = imgOptions.isComputeMinMax();
    getContext().getService(StatusService.class).showStatus("Initializing " + source);

    ReaderFilter r = scifio().initializer().initializeReader(source, openFile);

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
  private long[] getDimLengths(final Metadata m, ImgOptions imgOptions) {
    int imageIndex = imgOptions.getIndex();
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
    
    Subregion subregion = imgOptions.getSubRegion();
    
    for (int i = 0; i < dimLengths.length; i++) {
      
      if (subregion != null && i < subregion.getDimensionCount())
        dimLengths[i] = subregion.getLengths()[i];
      else
        dimLengths[i] = dimLengthsList.get(i);
    }
    return dimLengths;
  }
  
  /** Compiles an N-dimensional list of axis types from the given reader. */
  private AxisType[] getDimTypes(final Metadata m) {
    final int sizeX = m.getAxisLength(0, Axes.X);
    final int sizeY = m.getAxisLength(0, Axes.Y);
    final int sizeZ = m.getAxisLength(0, Axes.Z);
    final int sizeT = m.getAxisLength(0, Axes.TIME);
    final int sizeC = m.getEffectiveSizeC(0);
    final String dimOrder = FormatTools.findDimensionOrder(m, 0);
    final List<AxisType> dimTypes = new ArrayList<AxisType>();

    // add core dimensions
    for (final char dim : dimOrder.toCharArray()) {
      switch (dim) {
      case 'X':
        if (sizeX > 0)
          dimTypes.add(Axes.X);
        break;
      case 'Y':
        if (sizeY > 0)
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
        if (sizeC > 1)
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

    // FIXME: need physical pixel sizes in SCIFIO..
//    final OMEMetadata meta = new OMEMetadata();
//    meta.setContext(getContext());
//    scifio().translator().translate(m, meta, false);
//
//    final PositiveFloat xCalin = meta.getRoot().getPixelsPhysicalSizeX(0);
//    final PositiveFloat yCalin = meta.getRoot().getPixelsPhysicalSizeY(0);
//    final PositiveFloat zCalin = meta.getRoot().getPixelsPhysicalSizeZ(0);
//    Double tCal = meta.getRoot().getPixelsTimeIncrement(0);

    final Double xCal = 1.0, yCal = 1.0, zCal = 1.0, tCal = 1.0;

//    if (xCalin == null)
//      xCal = 1.0;
//    else
//      xCal = xCalin.getValue();
//
//    if (yCalin == null)
//      yCal = 1.0;
//    else
//      yCal = yCalin.getValue();
//
//    if (zCalin == null)
//      zCal = 1.0;
//    else
//      zCal = zCalin.getValue();
//
//    if (tCal == null)
//      tCal = 1.0;

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
    final ImgOptions imgOptions) throws FormatException, IOException {
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
    final boolean isPlanar = planarAccess != null && compatibleTypes;

    Plane plane = null;
    
    StatusService statusService = getContext().getService(StatusService.class);
    
    Subregion subregion = imgOptions.getSubRegion();
    
    int x, y, w,  h, zPos, cPos, tPos;
    
    // Z,C,T offsets and maximum values
    int[] index = new int[]{0, 0, 0};
    int[] bound = new int[]{1, 1, 1};
    
    Metadata meta = r.getMetadata();
    
    // Set the base
    x = y = w = h = 0;
    w = meta.getAxisLength(imageIndex, Axes.X);
    h = meta.getAxisLength(imageIndex, Axes.Y);

    // Default these to the last index position. If any index wasn't seen
    // this will result in index[xPos] = 0 for looking up the plane index,
    // equivalent to a length of 1 for that axis.
    zPos = cPos = tPos = 2;
    
    // subregion dimension index
    int dimsPlaced = 0;
    
    // CZT index
    int cztPlaced = 0;
    
    // current axis
    int axisIndex = 0;
    AxisType[] axes = meta.getAxes(imageIndex);
    
    // Total # planes in this subregion
    int planeCount = 1;

    // Populate subregion, plane count, and CZT information
    while (axisIndex < axes.length) {
      AxisType axisType = axes[axisIndex++];

      if (axisType.equals(Axes.X)) {
        if (dimsPlaced < subregion.getDimensionCount()) {
          x = (int)subregion.getOffsets()[dimsPlaced]; 
          w = (int)subregion.getLengths()[dimsPlaced]; 
          dimsPlaced++;
        }
      }
      else if (axisType.equals(Axes.Y)) {
        if (dimsPlaced < subregion.getDimensionCount()) {
          y = (int)subregion.getOffsets()[dimsPlaced]; 
          h = (int)subregion.getLengths()[dimsPlaced];
          dimsPlaced++;
        }
      }
      else if (axisType.equals(Axes.CHANNEL)) {
        int cStart = 0;
        int c = meta.getAxisLength(imageIndex, Axes.CHANNEL);
        
        if (dimsPlaced < subregion.getDimensionCount()) {
          cStart = (int)subregion.getOffsets()[dimsPlaced]; 
          c = (int)subregion.getLengths()[dimsPlaced]; 
          dimsPlaced++;
        }
        
        index[cztPlaced] = cStart;
        bound[cztPlaced] = cStart + c;
        cPos = cztPlaced++;
        planeCount *= c;
      }
      else if (axisType.equals(Axes.Z)) {
        int zStart = 0;
        int z = meta.getAxisLength(imageIndex, Axes.Z);

        if (dimsPlaced < subregion.getDimensionCount()) {
          zStart = (int)subregion.getOffsets()[dimsPlaced]; 
          z = (int)subregion.getLengths()[dimsPlaced]; 
          dimsPlaced++;
        }
        
        index[cztPlaced] = zStart;
        bound[cztPlaced] = zStart + z;
        zPos = cztPlaced++;
        planeCount *= z;
      }
      else if (axisType.equals(Axes.TIME)) {
        int tStart = 0;
        int t = meta.getAxisLength(imageIndex, Axes.TIME);
        
        if (dimsPlaced < subregion.getDimensionCount()) {
          tStart = (int)subregion.getOffsets()[dimsPlaced]; 
          t = (int)subregion.getLengths()[dimsPlaced]; 
          dimsPlaced++;
        }
        
        index[cztPlaced] = tStart;
        bound[cztPlaced] = tStart + t;
        tPos = cztPlaced++;
        planeCount *= t;
      }
    }

    int currentPlane = 0;
    
    // We have to manually reset the 2nd and 3rd indices after the inner loops
    int idx1 = index[1];
    int idx2 = index[2];
    
    //FIXME I think this is returning multi-channel planes out of order because of ChannelSeparator
    for (; index[0]<bound[0]; index[0]++) {
      for (; index[1]<bound[1]; index[1]++) {
        for (; index[2]<bound[2]; index[2]++) {
          
          // get the plane index in the underlying dataset
          int planeIndex = FormatTools.getIndex(r, imageIndex, index[zPos], index[cPos], index[tPos]);
          statusService.showStatus(currentPlane + 1, planeCount, "Reading plane");
          
          // open the subregion of the current plane
          if (plane == null)
            plane = r.openPlane(imageIndex, planeIndex, x, y, w, h);
          else {
            r.openPlane(0, planeIndex, plane, x, y, w, h);
          }
          
          // copy the data to the ImgPlus
          if (isPlanar)
            populatePlane(r, currentPlane, plane.getBytes(), planarAccess);
          else
            populatePlane(r, imageIndex, currentPlane, plane.getBytes(), imgPlus, imgOptions);

          // store color table
          imgPlus.setColorTable(plane.getColorTable(), currentPlane);
          currentPlane++;
        }
        index[2] = idx2;
      }
      index[1] = idx1;
    }

    if (imgOptions.isComputeMinMax())
      populateMinMax(r, imgPlus, imageIndex);
  }

  /** Populates plane by reference using {@link PlanarAccess} interface. */
  @SuppressWarnings("unchecked")
  private void populatePlane(final Reader r, final int no, final byte[] plane,
    @SuppressWarnings("rawtypes") final PlanarAccess planarAccess) {
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
    final ImgPlus<T> img, ImgOptions imgOptions) {
    Metadata m = r.getMetadata();
    final int pixelType = m.getPixelType(imageIndex);
    final boolean little = m.isLittleEndian(imageIndex);

    final long[] dimLengths = getDimLengths(m, imgOptions);
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
