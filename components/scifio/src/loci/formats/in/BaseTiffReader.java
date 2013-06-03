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

import io.scif.formats.TIFFFormat;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.tiff.IFD;


/**
 * BaseTiffReader is the superclass for file format readers compatible with
 * or derived from the TIFF 6.0 file format.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/BaseTiffReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/BaseTiffReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert melissa at glencoesoftware.com
 * 
 * @deprecated see io.scif.formats.TIFFFormat.BaseTiffParser
 */
@Deprecated
public abstract class BaseTiffReader extends MinimalTiffReader {
  
  // -- Constants --
  
  public static final String[] DATE_FORMATS = TIFFFormat.BaseTIFFParser.DATE_FORMATS;

  // -- Constructors --

  /** Constructs a new BaseTiffReader. */
  public BaseTiffReader(String name, String suffix) { super(name, suffix); }

  /** Constructs a new BaseTiffReader. */
  public BaseTiffReader(String name, String[] suffixes) {
    super(name, suffixes);
  }
  
  // -- Protected methods --
  
  protected void initMetadata() throws FormatException, IOException {
    //FIXME implement
  }
  
  protected void initMetadataStore() throws FormatException {
    //FIXME implement
  }
  
  protected void initStandardMetadata() throws FormatException, IOException {
    //FIXME implement
  }
  
  protected void put(String key, Object value) {
    if (value == null) return;
    if (value instanceof String) value = ((String) value).trim();
    reader.getMetadata().getTable().put(key, value);
  }

  protected void put(String key, int value) {
    if (value == -1) return; // indicates missing value
    reader.getMetadata().getTable().put(key, value);
  }

  protected void put(String key, boolean value) {
    put(key, new Boolean(value));
  }
  protected void put(String key, byte value) { put(key, new Byte(value)); }
  protected void put(String key, char value) { put(key, new Character(value)); }
  protected void put(String key, double value) { put(key, new Double(value)); }
  protected void put(String key, float value) { put(key, new Float(value)); }
  protected void put(String key, long value) { put(key, new Long(value)); }
  protected void put(String key, short value) { put(key, new Short(value)); }

  protected void put(String key, IFD ifd, int tag) {
    put(key, ifd.getIFDValue(tag));
  }

  protected void putInt(String key, IFD ifd, int tag) {
    put(key, ifd.getIFDIntValue(tag));
  }
}
