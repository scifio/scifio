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

package ome.xml.translation;

import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.apng.APNGFormat;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.util.FormatTools;
import ome.scifio.util.MetadataTools;
import ome.xml.meta.OMEMetadata;

/**
 * Translator class from {@link APNGMetadata} to
 * {@link OMEMetadata}
 * 
 * @author Mark Hiner
 */
@SCIFIOTranslator(metaIn = APNGFormat.Metadata.class, metaOut = OMEMetadata.class)
public class APNGOMETranslator extends OMETranslator<APNGFormat.Metadata> {

  // -- Constructors --

  public APNGOMETranslator() {
    this(null);
  }

  public APNGOMETranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API Methods --

  @Override
  public void translate(final APNGFormat.Metadata source, final OMEMetadata destination) {
    super.translate(source, destination);

    int sizeC = 1;

    switch (source.getIhdr().getColourType()) {
      case 0x2:
        sizeC = 3;
        break;
      case 0x4:
        sizeC = 2;
        break;
      case 0x6:
        sizeC = 4;
        break;
      default:
        break;
    }

    final String dimOrder = "XYCTZ";
    final int sizeX = source.getIhdr().getWidth();
    final int sizeY = source.getIhdr().getHeight();
    final int sizeT = source.getActl() == null ? 1 : source.getActl().getNumFrames();
    final int sizeZ = 1;
    String pixelType = null;
    try {
      pixelType =
        FormatTools.getPixelTypeString(FormatTools.pixelTypeFromBytes(
          source.getIhdr().getBitDepth() / 8, false, false));
    }
    catch (final FormatException e) {
      LOGGER.debug("Failed to find pixel type from bytes: " + (source.getIhdr().getBitDepth() / 8), e);
    }
    final boolean littleEndian = false;
    final int series = 0;

    //TODO what should these be in APNG?
    final int samplesPerPixel = 1; // = sizeC / effectiveSizeC... just sizeC for APNG? #planes / Z * T
    final String imageName = "";

    loci.formats.MetadataTools.populateMetadata(
      (loci.formats.meta.IMetadata)destination.getRoot(), series, imageName, littleEndian, dimOrder,
      pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT, samplesPerPixel);

    if (source.getFctl() != null && source.getFctl().size() > 0)
      destination.getRoot().setPixelsTimeIncrement(
        (double) source.getFctl().get(0).getDelayNum() /
          source.getFctl().get(0).getDelayDen(), 0);
  }
}
