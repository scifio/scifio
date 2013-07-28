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

package io.scif.img;

import io.scif.Reader;
import io.scif.Writer;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.Context;

/**
 * A static utility class for easy access to {@link ImgSaver} and {@link ImgOpener}
 * methods. Also includes type-convenience methods for quickly opening
 * various image types.
 * <p>
 * NB: No exceptions are thrown by any methods in this class. Instead they are all
 * caught, logged, and null is returned.
 * </p>
 * <p>
 * NB: Each of these methods occurs in its own {@link Context}
 * </p>
 * 
 * @author Mark Hiner
 *
 */
public final class IO {

  // -- Static IO Methods --

  /**
   * @param source
   *          - the location of the dataset to assess
   * @return - The number of images in the specified dataset, or 0
   *            if an error occurred.
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static int imageCount(final String source) throws ImgIOException {
    return new ImgOpener().getImageCount(source);
  }

  // -- Static ImgOpener methods --

  /**
   * Opens an {@link Img} in a format it is in (unsigned byte, float, int, ...)
   * using the respective {@link RealType}. It returns an {@link ImgPlus} which
   * contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg}, {@link ArrayImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(final String source) throws ImgIOException
  {
    return open(source, 0);
  }

  /**
   * Opens an {@link Img} in a format it is in (unsigned byte, float, int, ...)
   * using the respective {@link RealType}. It returns an {@link ImgPlus} which
   * contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(final String source, int imageIndex) throws ImgIOException
  {
    return open(source, imageIndex, (T)null);
  }

  /**
   * Opens an {@link Img} as {@link FloatType}. It returns an {@link ImgPlus}
   * which contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg}, {@link ArrayImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static ImgPlus<FloatType> openFloat(final String source, int imageIndex) throws ImgIOException
  {
    return open(source, imageIndex, new FloatType());
  }

  /**
   * Opens an {@link Img} as {@link DoubleType}. It returns an {@link ImgPlus}
   * which contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg}, {@link ArrayImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static ImgPlus<DoubleType> openDouble(final String source, int imageIndex) throws ImgIOException
  {
    return open(source, imageIndex, new DoubleType());
  }

  /**
   * Opens an {@link Img} as {@link UnsignedByteType}. It returns an {@link ImgPlus}
   * which contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg}, {@link ArrayImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static ImgPlus<UnsignedByteType> openUnsignedByte(final String source, int imageIndex) throws ImgIOException
  {
    return open(source, imageIndex, new UnsignedByteType());
  }

  /**
   * Opens an {@link Img} in a format it is in (unsigned byte, float, int, ...)
   * using the respective {@link RealType}. It returns an {@link ImgPlus} which
   * contains the Calibration and name.
   * <p>
   * The {@link Img} containing the data could be either a
   * {@link PlanarImg}, {@link ArrayImg} or {@link CellImg}.
   * </p>
   * 
   * @param source
   *          - the location of the dataset to open
   * @param imageIndex - the index within the dataset to open
   * @param type - the real type to use to open the image
   * @return - the {@link ImgPlus} or null if an exception is caught
   * 
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(
    final String source, int imageIndex, T type) throws ImgIOException
  {
    return new ImgOpener().openImg(source, type, new ImgOptions().setIndex(imageIndex));
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source) throws ImgIOException {
    return new ImgOpener().openImg(source);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String, RealType)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, T type) throws ImgIOException {
    return new ImgOpener().openImg(source, type);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String, ImgOptions)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, ImgOptions imgOptions) throws ImgIOException {
    return new ImgOpener().openImg(source, imgOptions);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String, RealType, ImgOptions)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, T type, ImgOptions imgOptions) throws ImgIOException {
    return new ImgOpener().openImg(source, type, imgOptions);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String, ImgFactory)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(String source, ImgFactory<T> imgFactory) throws ImgIOException {
    return new ImgOpener().openImg(source, imgFactory);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(String, ImgFactory, RealType)
   * @throws ImgIOException if something goes wrong reading the source.
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(String source, ImgFactory<T> imgFactory, T type) throws ImgIOException {
    return new ImgOpener().openImg(source, imgFactory, type);
  }

  /**
   * TODO
   * 
   * @see ImgOpener#openImg(Reader, RealType, ImgFactory, ImgOptions)
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(Reader reader, T type, ImgFactory<T> imgFactory, ImgOptions imgOptions) throws ImgIOException {
    return new ImgOpener().openImg(reader, type, imgFactory, imgOptions);
  }

  // -- Static ImgSaver methods --

  /**
   * TODO
   * 
   * @see ImgSaver#saveImg(String, Img)
   * @throws ImgIOException if something goes wrong writing to the destination.
   */
  public static <T extends RealType<T> & NativeType<T>> void saveImg(String dest, Img<T> img) throws ImgIOException {
    try {
      new ImgSaver().saveImg(dest, img);
    }
    catch (IncompatibleTypeException exc) {
      throw new ImgIOException(exc);
    }
  }

  /**
   * TODO
   * 
   * @see ImgSaver#saveImg(String, ImgPlus, int)
   * @throws ImgIOException if something goes wrong writing to the destination.
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(String dest, ImgPlus<T> imgPlus, int imageIndex) throws ImgIOException {
    try {
      new ImgSaver().saveImg(dest, imgPlus, imageIndex);
    }
    catch (IncompatibleTypeException exc) {
      throw new ImgIOException(exc);
    }
  }

  /**
   * TODO
   * 
   * @see ImgSaver#saveImg(Writer, Img)
   * @throws ImgIOException if something goes wrong writing to the destination.
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(Writer writer, Img<T> imgPlus) throws ImgIOException {
    try {
      new ImgSaver().saveImg(writer, imgPlus);
    }
    catch (IncompatibleTypeException exc) {
      throw new ImgIOException(exc);
    }
  }

  /**
   * TODO
   * 
   * @see ImgSaver#saveImg(Writer, ImgPlus, int)
   * @throws ImgIOException if something goes wrong writing to the destination.
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(Writer writer, ImgPlus<T> imgPlus, int imageIndex) throws ImgIOException {
    try {
      new ImgSaver().saveImg(writer, imgPlus, imageIndex);
    }
    catch (IncompatibleTypeException exc) {
      throw new ImgIOException(exc);
    }
  }

}
