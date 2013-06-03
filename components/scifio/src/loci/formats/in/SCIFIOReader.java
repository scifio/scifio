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
package loci.formats.in;

import io.scif.Checker;
import io.scif.Format;
import io.scif.Parser;
import io.scif.Reader;
import io.scif.gui.BufferedImageReader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.MetadataTools;
import loci.formats.gui.SCIFIOBIFormatReader;
import loci.formats.meta.MetadataStore;
import loci.legacy.context.LegacyContext;

/**
 * General purpose reader that can delegate to any discoverable {@link io.scif.Reader}.
 * 
 * @author Mark Hiner
 *
 */
@Deprecated
public class SCIFIOReader extends SCIFIOBIFormatReader {
  
  // -- Fields --
  
  // Index into all SCIFIO component arrays
  private List<Reader> readers;
  private List<Checker> checkers;
  private List<Format> formats;
  private List<Parser> parsers;
  
  private Map<Format, Integer> formatIndex;
  
  // -- Constructor --
  
  public SCIFIOReader() {
    super("SCIFIO General Reader", LegacyContext.getSCIFIO().format().getSuffixes());
    
    readers = new ArrayList<Reader>();
    checkers = new ArrayList<Checker>();
    parsers = new ArrayList<Parser>();
    
    formats = LegacyContext.getSCIFIO().format().getAllFormats();
    
    formatIndex = new HashMap<Format, Integer>();
    
    for (int i=0; i<formats.size(); i++) {
      Format fmt = formats.get(i);

      try {
        
        readers.add(fmt.createReader());
        checkers.add(fmt.createChecker());
        parsers.add(fmt.createParser());
        
      } catch (io.scif.FormatException e) {
        LOGGER.warn("Exception while creating SCIFIOReader components: " + e);
      }
      
      formatIndex.put(fmt, i);
    }
  }
  
  // -- IFormatReader API Methods --
  
  /* @see IFormatReader#close(boolean) */
  @Deprecated
  @Override
  public void close(boolean fileOnly) throws IOException {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    for (int i=0; i<formats.size(); i++) {
      updateIndex(i);
      super.close(fileOnly);
    }
    if (index >= 0) updateIndex(index);
  }
  
  /* @see IFormatReader#close(boolean) */
  @Deprecated
  @Override
  public void close() throws IOException {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    for (int i=0; i<formats.size(); i++) {
      updateIndex(i);
      super.close();
    }
    if (index >= 0) updateIndex(index);
  }
  
  /*
   * @see loci.formats.SCIFIOFormatReader#isThisType(java.lang.String, boolean)
   */
  public boolean isThisType(String name) {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    boolean isThisType = false;
    
    for (int i=0; i<formats.size() && !isThisType; i++) {
      updateIndex(i);
      isThisType = isThisType || super.isThisType(name, true);
    }
    if (index >= 0) updateIndex(index);
    return isThisType;
  }
  
  /*
   * @see loci.formats.SCIFIOFormatReader#isThisType(java.lang.String, boolean)
   */
  public boolean isThisType(String name, boolean open) {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    boolean isThisType = false;
    
    for (int i=0; i<formats.size() && !isThisType; i++) {
      updateIndex(i);
      isThisType = isThisType || super.isThisType(name, open);
    }
    if (index >= 0) updateIndex(index);
    return isThisType;
  }

  /*
   * @see loci.formats.SCIFIOFormatReader#isThisType(byte[])
   */
  public boolean isThisType(byte[] block) {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    boolean isThisType = false;
    
    for (int i=0; i<formats.size() && !isThisType; i++) {
      updateIndex(i);
      isThisType = isThisType || super.isThisType(block);
    }
    if (index >= 0) updateIndex(index);
    return isThisType;
  }

  /*
   * @see loci.formats.SCIFIOFormatReader#isThisType(loci.common.RandomAccessInputStream)
   */
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    boolean isThisType = false;
    
    for (int i=0; i<formats.size() && !isThisType; i++) {
      updateIndex(i);
      isThisType = isThisType || super.isThisType(stream);
    }
    if (index >= 0) updateIndex(index);
    return isThisType;
  }
  
  /*
   * @see loci.formats.SCIFIOFormatReader#setId(java.lang.String)
   */
  public void setId(String id) throws FormatException, IOException {
    try {
      Format fmt = LegacyContext.getSCIFIO().format().getFormat(id);
      
      if (!format.equals(fmt)) {
        close();
      }
      
      updateIndex(formatIndex.get(fmt));
      
      super.setId(id);
      
      //TODO should really move this to format specific translator behavior
      // reinitialize the MetadataStore
      MetadataStore store = makeFilterMetadata();
      MetadataTools.populatePixels(store, this);
      
      for (int s=0; s<getSeriesCount(); s++) {
        String imageName = id.substring(0, id.lastIndexOf(".")) + (s > 0 ? (s + 1) : "");
        store.setImageName(imageName, s);
      }
    } catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }
  
  public void setNormalized(boolean normalize) {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    for (int i=0; i<formats.size(); i++) {
      updateIndex(i);
      super.setNormalized(normalize);
    }
    if (index >= 0) updateIndex(index);
  }
  
  public void setOriginalMetadataPopulated(boolean populate) {
    for (Parser parser : parsers) {
      parser.setOriginalMetadataPopulated(populate);
    }
  }

  public void setMetadataFiltered(boolean filter) {
    for (Parser parser : parsers) {
      parser.setMetadataFiltered(filter);
    }
  }
  
  public void setMetadataOptions(MetadataOptions options) {
    io.scif.MetadataOptions sOpts = new io.scif.DefaultMetadataOptions();

    switch(options.getMetadataLevel()) {
      case ALL:
        sOpts.setMetadataLevel(io.scif.MetadataLevel.ALL);
        break;
      case NO_OVERLAYS:
        sOpts.setMetadataLevel(io.scif.MetadataLevel.NO_OVERLAYS);
        break;
      case MINIMUM:
        sOpts.setMetadataLevel(io.scif.MetadataLevel.MINIMUM);
        break;
    }
    
    for (Parser parser : parsers) {
      parser.setMetadataOptions(sOpts);
    }
  }
  
  public void setGroupFiles(boolean group) {
    int index = -1;
    if (format != null) index = formatIndex.get(format);
    
    for (int i=0; i<formats.size(); i++) {
      updateIndex(i);
      super.setGroupFiles(group);
    }
    
    if (index >= 0) updateIndex(index);
  }
  
  public boolean isOriginalMetadataPopulated() {
    if (parser == null) return false;
    
    return super.isOriginalMetadataPopulated();
  }
  
  public boolean isMetadataFiltered() {
    if (parser == null) return false;
    
    return super.isMetadataFiltered();
  }
  
  /*
   * @see loci.formats.FormatHandler#getNativeDataType()
   */
  public Class<?> getNativeDataType() {
    if (BufferedImageReader.class.isAssignableFrom(reader.getClass()))
      return BufferedImage.class;
    else
      return byte[].class;
  }
  
  /*
   * @see loci.formats.SCIFIOFormatReader#openPlane(int, int, int, int, int)
   */
  public Object openPlane(int no, int x, int y, int w, int h) throws FormatException, IOException {
    if(getNativeDataType().equals(byte[].class))
      return openBytes(no, x, y, w, h);
    else
      return super.openPlane(no, x, y, w, h);
  }
  
  // -- Helper Methods --
  
  private void updateIndex(int componentIndex) {
    reader = readers.get(componentIndex);
    parser = parsers.get(componentIndex);
    checker = checkers.get(componentIndex);
    format = formats.get(componentIndex);
  }
}
