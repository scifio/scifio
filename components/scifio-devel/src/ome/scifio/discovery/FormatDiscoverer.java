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

package ome.scifio.discovery;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.Writer;

public class FormatDiscoverer implements
    Discoverer<SCIFIOFormat, Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
        ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
        ? extends Writer<? extends Metadata>>> {

  // -- Discoverer API Methods --
  
  /* Builds a list of all discovered Formats */
  public List<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
      ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
      ? extends Writer<? extends Metadata>>> discover() throws FormatException {
    
    final List<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
        ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
        ? extends Writer<? extends Metadata>>> formats = 
        new ArrayList<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
            ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
            ? extends Writer<? extends Metadata>>>();

    for (final IndexItem<SCIFIOFormat, Format> item : Index
        .load(SCIFIOFormat.class, Format.class)) {
      try {
        @SuppressWarnings("unchecked")
        final Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
            ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
            ? extends Writer<? extends Metadata>> format = item.instance();
        formats.add(format);
      } catch (final InstantiationException e) {
        throw new FormatException(e);
      }
    }

    return formats;
  }
}
