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

import io.scif.formats.EPSFormat;
import net.imglib2.meta.Axes;
import ome.xml.meta.OMEMetadata;
import ome.xml.meta.OMEXMLMetadata;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and EPS formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class EPSTranslator {
  /**
   * Translator class from {@link io.scif.formats.EPSFormat.Metadata} to
   * {@link ome.xml.meta.OMEMetadata}.
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = FromOMETranslator.class, priority = Priority.HIGH_PRIORITY,
      attrs = {
    @Attr(name = OMEEPSTranslator.SOURCE, value = OMEMetadata.CNAME),
    @Attr(name = OMEEPSTranslator.DEST, value = EPSFormat.Metadata.CNAME)
  })
  public static class OMEEPSTranslator extends FromOMETranslator<EPSFormat.Metadata> {

    /*
     * @see OMETranslator#typedTranslate(io.scif.Metadata, io.scif.Metadata)
     */
    @Override
    protected void typedTranslate(OMEMetadata source, EPSFormat.Metadata dest) {
      super.typedTranslate(source, dest);

      OMEXMLMetadata meta = source.getRoot();

      int sizeX = meta.getPixelsSizeX(0).getValue().intValue();
      int sizeY = meta.getPixelsSizeY(0).getValue().intValue();
      int sizeC = meta.getChannelSamplesPerPixel(0, 0).getValue().intValue();

      dest.setAxisLength(0, Axes.X, sizeX);
      dest.setAxisLength(0, Axes.Y, sizeY);
      dest.setAxisLength(0, Axes.CHANNEL, sizeC);
    }
  }
}
