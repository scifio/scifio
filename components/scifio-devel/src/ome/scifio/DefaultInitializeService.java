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
package ome.scifio;

import java.io.IOException;

import ome.scifio.filters.ReaderFilter;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * @author Mark Hiner
 *
 */
@Plugin(type=Service.class)
public class DefaultInitializeService extends AbstractService implements InitializeService {

  /**
   * See {@link #initializeReader(String, boolean)}. Will not open the image
   * source while parsing metadata.
   * 
   * @param id Name of the image source to be read.
   * @return An initialized {@code Reader}.
   */
  public ReaderFilter initializeReader(final String id) throws FormatException,
      IOException {
    return initializeReader(id, false);
  }

  /**
   * Convenience method for creating a {@code Reader} component that is ready
   * to open planes of the provided image source. The reader's {@code Metadata}
   * and source fields will be populated.
   * 
   * @param id Name of the image source to be read.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Reader}.
   */
  public ReaderFilter initializeReader(final String id, final boolean openFile)
      throws FormatException, IOException {
    final Reader r = getContext().getService(SCIFIO.class).formats().getFormat(id, openFile).createReader();
    r.setSource(id);
    return new ReaderFilter(r);
  }

  /**
   * See {@link #initializeWriter(String, String, boolean)}. Will not open the
   * image source while parsing metadata.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @return An initialized {@code Writer}.
   */
  public Writer initializeWriter(
      final String source, final String destination) throws FormatException, IOException {
    return initializeWriter(source, destination, false);
  }
  
  /**
   * Convenience method for creating a {@code Writer} component that is ready
   * to save planes to the destination image. {@code Metadata} will be parsed
   * from the source, translated to the destination's type if necessary, and
   * set (along with the destination itself) on the resulting {@code Writer}.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Writer}.
   */
  public Writer initializeWriter(
      final String source, final String destination,
      final boolean openSource) throws FormatException, IOException {
    final Format sFormat = getContext().getService(SCIFIO.class).formats().getFormat(source, openSource);
    final Parser parser = sFormat.createParser();
    final Metadata sourceMeta = parser.parse(source);
    
    return initializeWriter(sourceMeta, destination);
  }

  /*
   * @see ome.scifio.InitializeService#initializeWriter(ome.scifio.Metadata, java.lang.String)
   */
  public Writer initializeWriter(Metadata sourceMeta, String destination)
      throws FormatException, IOException {
    Metadata destMeta = null;
    
    final Format sFormat = sourceMeta.getFormat();
    final Format dFormat = getContext().getService(SCIFIO.class).formats().getFormat(destination, false);

    // if dest is a different format than source, translate..
    if (sFormat == dFormat) {
      // otherwise we can directly cast, since they are the same types
      destMeta = castMeta(sourceMeta, destMeta);
      
    } else if (dFormat.findDestTranslator(sourceMeta) != null) {
      // Can directly translate between these formats
      
      destMeta = dFormat.createMetadata();
      
      dFormat.findDestTranslator(sourceMeta).translate(sourceMeta, destMeta);
      
    } else {
      // Have to translate to and from DatasetMetadata
      destMeta = dFormat.createMetadata();
      
      // TODO should probably make this general wrt DatasetMetadata,
      // but that also requires having a general way to instantiate DatasetMetadata
      final DatasetMetadata transMeta = new DefaultDatasetMetadata(getContext());
      final Translator transToCore = sFormat.findSourceTranslator(transMeta);
      final Translator transFromCore = dFormat.findDestTranslator(transMeta);
//TODO unnecessary?     transMeta.setSource(new RandomAccessInputStream(getContext(), source));
      transToCore.translate(sourceMeta, transMeta);
      transFromCore.translate(transMeta, destMeta);
    }

    final Writer writer = dFormat.createWriter();
    writer.setMetadata(destMeta);
    writer.setDest(destination);

    return writer;
  }
  
  // -- Helper Methods --
  
  /*
   * Hide the suppress warnings in an atomic cast method
   * <p>
   * NB: endType parameter is just there to guarantee a return type
   * </p>
   */
  private <N extends Metadata, M extends Metadata> M castMeta(N metadata, M endType) {
    @SuppressWarnings("unchecked")
    M meta = (M) metadata;
    return meta;
  }
}
