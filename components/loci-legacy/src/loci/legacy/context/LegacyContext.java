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
package loci.legacy.context;

import io.scif.SCIFIO;
import io.scif.services.FilePatternService;
import io.scif.services.FormatService;
import io.scif.services.InitializeService;
import io.scif.services.JAIIIOService;
import io.scif.services.LocationService;
import io.scif.services.LuraWaveService;
import io.scif.services.PluginAttributeService;
import io.scif.services.TranslatorService;
import ome.xml.services.OMEXMLMetadataService;
import ome.xml.services.OMEXMLService;

import org.scijava.Context;

/**
 * Provides a single context for legacy classes.
 * <p>
 * This is functionally equivalent to the legacy behavior
 * of a single, static loading of readers by ImageReader.
 * </p>
 * 
 * @author Mark Hiner
 *
 */
public final class LegacyContext {
  
  //-- Constants --

  private static Context context = null;
  private static SCIFIO scifio = null;
  
  // -- LegacyContext API methods --
 
  /**
   * This method ensures a single context is used if all
   * legacy code. It should always be called any time a context is needed.
   * 
   * @return A static org.scijava.Context
   */
  public static Context get() {
    if (context == null) {
      context = new Context(FilePatternService.class, FormatService.class, 
          InitializeService.class, LocationService.class,
          PluginAttributeService.class, TranslatorService.class, 
          OMEXMLMetadataService.class, OMEXMLService.class, JAIIIOService.class,
          LuraWaveService.class);
    }
    return context;
  }
  
  /**
   * Convenience method for statically caching a {@link io.scif.SCIFIO} instance.
   * Use this method if you'd prefer not to construct a new SCIFIO each time, given
   * that the context should always be the same (that is, this method is an alternative
   * to {@code new SCIFIO(LegacyContext.get())}).
   * 
   * @return A static io.scif.SCIFIO wrapping the legacy context
   */
  public static SCIFIO getSCIFIO() {
    if (scifio == null) {
      scifio = new SCIFIO(get());
    }
    return scifio;
  }
}
