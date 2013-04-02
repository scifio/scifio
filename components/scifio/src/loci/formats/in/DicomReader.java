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

package loci.formats.in;

import loci.formats.SCIFIOFormatReader;
import loci.legacy.context.LegacyContext;
import ome.scifio.formats.DicomFormat;

/**
 * DicomReader is the file format reader for DICOM files.
 * Much of this code is adapted from ImageJ's DICOM reader; see
 * http://rsb.info.nih.gov/ij/developer/source/ij/plugin/DICOM.java.html
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/DicomReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/DicomReader.java;hb=HEAD">Gitweb</a></dd></dl>
 * @deprecated see ome.scifio.formats.DicomFormat
 */
@Deprecated
public class DicomReader extends SCIFIOFormatReader {

  // -- Constructor --

  /** Constructs a new DICOM reader. */
  // "Digital Imaging and Communications in Medicine" is nasty long.
  public DicomReader() {
    super("DICOM", new String[]{"dic", "dcm", "dicom", "jp2",
        "j2ki", "j2kr", "raw", "ima"});

    try {
      format = LegacyContext.getSCIFIO().formats().getFormatFromClass(DicomFormat.class);
      checker = format.createChecker();
      parser = format.createParser();
      reader = format.createReader();
    }
    catch (ome.scifio.FormatException e) {
      LOGGER.warn("Failed to create DicomFormat components");
    }
  }

}
