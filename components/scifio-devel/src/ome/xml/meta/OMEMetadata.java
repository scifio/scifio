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

package ome.xml.meta;

import io.scif.AbstractMetadata;
import io.scif.services.ServiceException;

import org.scijava.Context;

import ome.xml.services.OMEXMLMetadataService;
import ome.xml.services.OMEXMLService;

/**
 * io.scif.Metadata class wrapping an OME-XML root.
 * 
 * @see ome.xml.meta.OMEXMLMetadata
 * @see io.scif.Metadata
 * 
 * @author Mark Hiner
 *
 */
public class OMEMetadata extends AbstractMetadata {
  
  // -- Constants --
  
  public static final String FORMAT_NAME = "OME-XML"; 
  public static final String CNAME = "ome.xml.meta.OMEMetadata";
  
  // -- Fields --
  
  /** OME core */
  protected OMEXMLMetadata root;

  // -- Constructor --
  
  public OMEMetadata() {
    this(null);
  }
  
  public OMEMetadata(Context context) {
    this(context, null);
  }
  
  public OMEMetadata(Context context, OMEXMLMetadata root) {
    setContext(context);
    setRoot(root);
  }
  
  // -- Metadata API Methods --
  
  /*
   * @see io.scif.AbstractMetadata#getFormatName()
   */
  public String getFormatName() {
    return FORMAT_NAME;
  }
  
  /*
   * @see io.scif.AbstractMetadata#populateImageMetadata()
   */
  public void populateImageMetadata() {
    getContext().getService(OMEXMLMetadataService.class).
      populateMetadata(getRoot(), this);
  }
  
  // -- Helper Methods --
  
  /**
   * Sets the root for this Metadata
   */
  public void setRoot(OMEXMLMetadata root) {
    this.root = root;
  }

  /**
   * Returns the root of this Metadata
   */
  public OMEXMLMetadata getRoot() {
    if (root == null) {
      OMEXMLService service = scifio().format().getInstance(OMEXMLService.class);
      try {
        root = service.createOMEXMLMetadata();
      } catch (ServiceException e) {
        LOGGER.debug("Failed to get OME-XML Service", e);
      }
    }
    return root;
  }
}
