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

package ome.xml.services;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;

import org.scijava.service.Service;

import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.MetadataStore;
import ome.xml.model.enums.Binning;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;

/**
 * A utility class for working with metadata objects,
 * including {@link ome.xml.meta.MetadataStore}, 
 * {@link ome.xml.meta.MetadataRetrieve}, and OME-XML strings.
 * Most of the methods require the optional {@link loci.formats.ome}
 * package, and optional ome-xml.jar library, to be present at runtime.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MetadataTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MetadataTools.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public interface OMEXMLMetadataService extends Service {

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.
   */
  void populatePixels(MetadataStore store, Metadata meta);

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   */
  void populatePixels(MetadataStore store, Metadata meta, boolean doPlane);

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   * If the 'doImageName' flag is set, then the image name will be populated
   * as well.  By default, 'doImageName' is true.
   */
  void populatePixels(MetadataStore store, Metadata meta,
      boolean doPlane, boolean doImageName);

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  void populateMetadata(MetadataStore store, int imageIndex, String imageName,
      boolean littleEndian, String dimensionOrder, String pixelType, int sizeX,
      int sizeY, int sizeZ, int sizeC, int sizeT, int samplesPerPixel);

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the values from the provided {@link Metadata}.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  void populateMetadata(MetadataStore store, int imageIndex, String imageName,
      Metadata meta);

  /**
   * Populates the given {@link MetadataStore}, for the specified imageIndex, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  void populateMetadata(MetadataStore store, String file, int imageIndex,
      String imageName, boolean littleEndian, String dimensionOrder,
      String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
      int samplesPerPixel);

  void populatePixelsOnly(MetadataStore store, Reader r);

  void populatePixelsOnly(MetadataStore store, int imageIndex,
      boolean littleEndian, String dimensionOrder, String pixelType, int sizeX,
      int sizeY, int sizeZ, int sizeC, int sizeT, int samplesPerPixel);

  /**
   * Disables the setting of a default creation date.
   *
   * By default, missing creation dates will be replaced with the corresponding
   * file's last modification date, or the current time if the modification
   * date is not available.
   *
   * Calling this method with the 'enabled' parameter set to 'false' causes
   * missing creation dates to be left as null.
   *
   * @param enabled See above.
   * @see #setDefaultCreationDate(MetadataStore, String, int)
   */
  void setDefaultDateEnabled(boolean enabled);

  /**
   * Sets a default creation date.  If the named file exists, then the creation
   * date is set to the file's last modification date.  Otherwise, it is set
   * to the current date.
   *
   * @see #setDefaultDateEnabled(boolean)
   */
  void setDefaultCreationDate(MetadataStore store, String id, int imageIndex);

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  void verifyMinimumPopulated(MetadataRetrieve src) throws FormatException;

  /**
   *
   * @throws FormatException if there is a missing metadata field,
   *   or the metadata object is uninitialized
   */
  void verifyMinimumPopulated(MetadataRetrieve src, int n)
      throws FormatException;

  /**
   * Adjusts the given dimension order as needed so that it contains exactly
   * one of each of the following characters: 'X', 'Y', 'Z', 'C', 'T'.
   */
  String makeSaneDimensionOrder(String dimensionOrder);

  /**
   * Constructs an LSID, given the object type and indices.
   * For example, if the arguments are "Detector", 1, and 0, the LSID will
   * be "Detector:1:0".
   */
  String createLSID(String type, int... indices);

  /**
   * Retrieves an {@link ome.xml.model.enums.ExperimentType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  ExperimentType getExperimentType(String value) throws FormatException;

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  LaserType getLaserType(String value) throws FormatException;

  /**
   * Retrieves an {@link ome.xml.model.enums.LaserMedium} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  LaserMedium getLaserMedium(String value) throws FormatException;

  /**
   * Retrieves an {@link ome.xml.model.enums.Immersion} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  Immersion getImmersion(String value) throws FormatException;

  /**
   * Retrieves an {@link ome.xml.model.enums.Correction} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  Correction getCorrection(String value) throws FormatException;

  /**
   * Retrieves an {@link ome.xml.model.enums.DetectorType} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  DetectorType getDetectorType(String value) throws FormatException;
  
  /**
   * Retrieves an {@link ome.xml.model.enums.Binning} enumeration
   * value for the given String.
   *
   * @throws ome.xml.model.enums.EnumerationException if an appropriate
   *  enumeration value is not found.
   */
  Binning getBinning(String value) throws FormatException;
  
  /**
   * Uses the provided MetadataRetrieve to populate the format-agnostic
   * image information in the provided Metadata object (that is, the
   * ImageMetadata).
   */
  void populateMetadata(MetadataRetrieve retrieve, Metadata meta);

  /**
   * Populates the provided ImageMetadata object using the specified
   * image index into the MetadataRetrieve.
   */
  void populateImageMetadata(MetadataRetrieve retrieve,
      int imageIndex, ImageMetadata iMeta);
}