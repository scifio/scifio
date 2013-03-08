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

import org.scijava.AbstractContextual;
import org.scijava.Context;

/**
 * @author Mark Hiner
 *
 */
public class SCIFIO extends AbstractContextual {
  
  // -- Fields --
  
  private InitializeService initializeService = null;
  private FormatService formatService = null;
  private TranslatorService translatorService = null;
  
  // -- Constructors --
  
  /** Creates a new SCIFIO application context with all available services. */
  public SCIFIO() {
    this(new Context());
  }

  /**
   * Creates a new SCIFIO application context.
   *
   * @param empty If true, the context will be empty; otherwise, it will be
   * initialized with all available services.
   */
  public SCIFIO(boolean empty) {
    this(new Context(empty));
  }
  
  public SCIFIO(Context context) {
    setContext(context);
    
    initializeService = context.getService(InitializeService.class);
    formatService = context.getService(FormatService.class);
    translatorService = context.getService(TranslatorService.class);
  }
  
  // -- Service Accessors --
  
  public InitializeService initializer() {
    return initializeService;
  }
  
  public FormatService formats() {
    return formatService;
  }
  
  public TranslatorService translators() {
    return translatorService;
  }
}