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
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  // -- Static fields --
  
  private static Logger logger = LoggerFactory.getLogger(IO.class);
  
  // -- Static IO Methods --
  
  /**
   * @param source
   *          - the location of the dataset to assess
   * @return - The number of images in the specified dataset, or 0
   *            if an error occurred.
   */
  public static int imageCount(final String source) {
    try {
      return new ImgOpener().getImageCount(source);
    } catch (ImgIOException e) {
      logger.error("Failed to get image count for source: " + source, e);
      return 0;
    }
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
   * @throws ImgIOException
   *           - if file could not be found, if it is too big for the memory or
   *           if it is incompatible with the opener
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(final String source)
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
   * @throws ImgIOException
   *           - if file could not be found, if it is too big for the memory or
   *           if it is incompatible with the opener
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(final String source, int imageIndex)
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
   * @throws ImgIOException
   *           - if file could not be found or is too big for the memory
   */
  public static ImgPlus<FloatType> openFloat(final String source, int imageIndex) 
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
   * @throws ImgIOException
   *           - if file could not be found or is too big for the memory
   */
  public static ImgPlus<DoubleType> openDouble(final String source, int imageIndex)
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
   * @throws ImgIOException
   *           - if file could not be found or is too big for the memory
   */
  public static ImgPlus<UnsignedByteType> openUnsignedByte(final String source, int imageIndex)
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
   * @throws ImgIOException
   *           - if file could not be found, if it is too big for the memory or
   *           if it is incompatible with the opener
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> open(
    final String source, int imageIndex, T type)
  {
    try {
      return new ImgOpener().openImg(source, type, new ImgOptions().setIndex(imageIndex));
    } catch (Exception e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
    
  /**
   * @see {@link ImgOpener#openImg(String)}
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source) {
    try {
      return new ImgOpener().openImg(source);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(String, RealType)}
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, T type) {
    try {
      return new ImgOpener().openImg(source, type);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(String, ImgOptions)}
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, ImgOptions imgOptions) {
    try {
      return new ImgOpener().openImg(source, imgOptions);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(String, RealType, ImgOptions)}
   */
  public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(String source, T type, ImgOptions imgOptions) {
    try {
      return new ImgOpener().openImg(source, type, imgOptions);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(String, ImgFactory)}
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(String source, ImgFactory<T> imgFactory) {
    try {
      return new ImgOpener().openImg(source, imgFactory);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(String, ImgFactory, RealType)}
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(String source, ImgFactory<T> imgFactory, T type) {
    try {
      return new ImgOpener().openImg(source, imgFactory, type);
    } catch (ImgIOException e) {
      logger.error("Failed to open image for source: " + source, e);
      return null;
    }
  }
  
  /**
   * @see {@link ImgOpener#openImg(Reader, RealType, ImgFactory, ImgOptions)}
   */
  public static <T extends RealType<T>> ImgPlus<T> openImg(Reader reader, T type, ImgFactory<T> imgFactory, ImgOptions imgOptions) {
    try {
      return new ImgOpener().openImg(reader, type, imgFactory, imgOptions);
    } catch (ImgIOException e) {
      logger.error("Failed to open image", e);
      return null;
    }
  }
  
  // -- Static ImgSaver methods --

  /**
   * @see {@link ImgSaver#saveImg(String, Img)}
   */
  public static <T extends RealType<T> & NativeType<T>> void saveImg(String dest, Img<T> img) {
    try {
      new ImgSaver().saveImg(dest, img);
    } catch (Exception e) {
      logger.error("Failed to write image : " + dest, e);
    }
  }
  
  /**
   * @see {@link ImgSaver#saveImg(String, ImgPlus, int)}
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(String dest, ImgPlus<T> imgPlus, int imageIndex) {
    try {
      new ImgSaver().saveImg(dest, imgPlus, imageIndex);
    } catch (Exception e) {
      logger.error("Failed to write image : " + dest, e);
    } 
  }
  
  /**
   * @see {@link ImgSaver#saveImg(Writer, Img)}
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(Writer writer, Img<T> imgPlus) {
    try {
      new ImgSaver().saveImg(writer, imgPlus);
    } catch (Exception e) {
      logger.error("Failed to write image", e);
    }
  }
  
  /**
   * @see {@link ImgSaver#saveImg(Writer, ImgPlus, int)}
   */
  <T extends RealType<T> & NativeType<T>> void saveImg(Writer writer, ImgPlus<T> imgPlus, int imageIndex) {
    try {
      new ImgSaver().saveImg(writer, imgPlus, imageIndex);
    } catch (Exception e) {
      logger.error("Failed to write image", e);
    }
  }
}
