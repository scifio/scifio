/*
 * #%L
 * Legacy layer preserving compatibility between legacy Bio-Formats and SCIFIO.
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
package loci.legacy.adapter;

import loci.common.IRandomAccess;
import loci.common.RandomAccessInputStream;
import loci.common.StatusListener;
import loci.common.StatusReporter;
import loci.common.enumeration.CodedEnum;

/**
 * Provides type-safe adapting methods for loci.common classes using
 * AdapterTools.get().
 * 
 * @author Mark Hiner
 *
 */
public final class CommonAdapter {

  // -- CodedEnum adapter methods --
  
  public static CodedEnum get(ome.scifio.enumeration.CodedEnum ce) {
    return (CodedEnum)AdapterTools.get(ce);
  }
  
  public static ome.scifio.enumeration.CodedEnum get(CodedEnum ce) {
    return (ome.scifio.enumeration.CodedEnum)AdapterTools.get(ce);
  }
  
  // -- IRandomAccess adapter methods --
  
  public static IRandomAccess get(ome.scifio.io.IRandomAccess ira) {
    return (IRandomAccess)AdapterTools.get(ira);
  }
  
  public static ome.scifio.io.IRandomAccess get(IRandomAccess ira) {
    return (ome.scifio.io.IRandomAccess)AdapterTools.get(ira);
  }
  
  // -- RandomAccessInputStream adapter methods --
  
  public static RandomAccessInputStream get(ome.scifio.io.RandomAccessInputStream stream) {
    return (RandomAccessInputStream)AdapterTools.get(stream);
  }
  
  public static ome.scifio.io.RandomAccessInputStream get(RandomAccessInputStream stream) {
    return (ome.scifio.io.RandomAccessInputStream)AdapterTools.get(stream);
  }
  
  // -- StatusListener adapter methods --
  
  public static StatusListener get(ome.scifio.common.StatusListener sl) {
    return (StatusListener)AdapterTools.get(sl);
  }
  
  public static ome.scifio.common.StatusListener get(StatusListener sl) {
    return (ome.scifio.common.StatusListener)AdapterTools.get(sl);
  }
  
  // -- StatusReporter Adapter Methods --
  
  public static StatusReporter get(ome.scifio.common.StatusReporter sr) {
    return (StatusReporter)AdapterTools.get(sr);
  }
  
  public static ome.scifio.common.StatusReporter get(StatusReporter sr) {
    return (ome.scifio.common.StatusReporter)AdapterTools.get(sr);
  }
}
