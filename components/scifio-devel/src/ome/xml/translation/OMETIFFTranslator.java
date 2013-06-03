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

package ome.xml.translation;

import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMETIFFFormat;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and OMETIFF formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class OMETIFFTranslator {
  
  /**
   * Translator class from {@link io.scif.formats.OMETIFFFormat.Metadata} to
   * {@link ome.xml.meta.OMEMetadata}
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = ToOMETranslator.class, priority = TIFFTranslator.PRIORITY + 1,
      attrs = {
    @Attr(name = OMEtoOMETIFFTranslator.SOURCE, value = OMETIFFFormat.Metadata.CNAME),
    @Attr(name = OMEtoOMETIFFTranslator.DEST, value = OMEMetadata.CNAME)
  })
  public static class OMEtoOMETIFFTranslator extends ToOMETranslator<OMETIFFFormat.Metadata> {

    // -- Translator API Methods --

    /*
     * @see OMETranslator#typedTranslate(io.scif.Metadata, io.scif.Metadata)
     */
    @Override
    protected void typedTranslate(OMETIFFFormat.Metadata source, OMEMetadata dest) {
      dest.setRoot(source.getOmeMeta().getRoot());
      super.typedTranslate(source, dest);
    }
  }
  
  /**
   * Translator class from {@link io.scif.formats.OMETIFFFormat.Metadata} to
   * {@link ome.xml.meta.OMEMetadata}.
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = FromOMETranslator.class, priority = TIFFTranslator.PRIORITY + 1,
      attrs = {
    @Attr(name = OMETIFFtoOMETranslator.SOURCE, value = OMEMetadata.CNAME),
    @Attr(name = OMETIFFtoOMETranslator.DEST, value = OMETIFFFormat.Metadata.CNAME)
  })
  public static class OMETIFFtoOMETranslator extends FromOMETranslator<OMETIFFFormat.Metadata> {

    /*
     * @see OMETranslator#typedTranslate(io.scif.Metadata, io.scif.Metadata)
     */
    @Override
    protected void typedTranslate(OMEMetadata source, OMETIFFFormat.Metadata dest) {
      OMEMetadata omeMeta = dest.getOmeMeta();
      
      if (omeMeta == null) {
        omeMeta = new OMEMetadata(getContext(), source.getRoot());
        dest.setOmeMeta(omeMeta);
      }
      else omeMeta.setRoot(source.getRoot());
      
      super.typedTranslate(source, dest);
    }
  }
}
