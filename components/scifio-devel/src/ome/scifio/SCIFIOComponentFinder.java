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

package ome.scifio;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class contains a collection of convenience methods
 * that can be used to find a desired component of SCIFIO,
 * given the overlaying context's known formats.
 * 
 * @author Mark Hiner
 *
 */
public class SCIFIOComponentFinder {

  /* 
   * Returns a sorted list of Formats that are compatible with the provided source.
   * if takeFirst is set, returns the first matching Format.
   * Formats are searched in priority order
   */
  public List<Format<?, ?, ?, ?, ?>> findFormats(final String id,
    final boolean openFile, final boolean takeFirst,
    final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException
  {

    final PriorityQueue<Format<?, ?, ?, ?, ?>> formatPriorities =
      new PriorityQueue<Format<?, ?, ?, ?, ?>>(formats);
    final List<Format<?, ?, ?, ?, ?>> formatList =
      new ArrayList<Format<?, ?, ?, ?, ?>>();

    boolean found = false;
    while (!formatPriorities.isEmpty() && (!found || !takeFirst)) {
      final Format<?, ?, ?, ?, ?> format = formatPriorities.poll();

      if (format.createChecker().isFormat(id, openFile)) {
        found = true;
        formatList.add(format);
      }
    }

    if (formatList.isEmpty())
      throw new FormatException(id + ": No supported format found.");

    return formatList;
  }

  /*
  public List<Checker<?>> findCheckers(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Parser<?>> findParsers(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Metadata> findMetadata(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Reader<?>> findReaders(final String id, final boolean openFile, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Writer<?>> findWriters(final String id, final boolean openFile, final String output, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  
  public List<Translator<?, ?>> findTranslators(final String id, final boolean openFile, final String output, final boolean takeFirst, final List<Format<?, ?, ?, ?, ?>> formats) throws FormatException {
    return null;
  }
  */
}
