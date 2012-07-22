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

/**
 * Abstract superclass of all SCIFIO components that are children of
 * ome.scifio.FormatHandler.
 *
 */
public abstract class AbstractFormatHandler extends AbstractHasContext
  implements FormatHandler {

  // -- Fields --
  /** Name of this file format. */
  protected String formatName;

  /** Valid suffixes for this file format. */
  protected String[] suffixes;

  /** Suffixes for supported compression types. */
  public static final String[] COMPRESSION_SUFFIXES = {"bz2", "gz"};

  // -- Constructors --
  // TODO do Parsers and Translators really need to track format and suffixes?

  /** Constructs a format handler with the given name and default suffix. */
  public AbstractFormatHandler(final String formatName, final String suffix, final SCIFIO ctx) {
    this(formatName, suffix == null ? null : new String[] {suffix}, ctx);
  }

  /** Constructs a format handler with the given name and default suffixes. */
  public AbstractFormatHandler(final String formatName, final String[] suffixes, final SCIFIO ctx)
  {
    super(ctx);
    this.formatName = formatName;
    this.suffixes = suffixes == null ? new String[0] : suffixes;
  }

  // -- FormatHandler API Methods --

  /* @see FormatHandler#getFormatName() */
  public String getFormatName() {
    return formatName;
  }

  /* @see FormatHandler#getSuffixes() */
  public String[] getSuffixes() {
    return suffixes.clone();
  }

  // -- Utility methods --

  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String suffix) {
    return checkSuffix(name, new String[] {suffix});
  }

  /** Performs suffix matching for the given filename. */
  public static boolean checkSuffix(final String name, final String[] suffixList) {
    final String lname = name.toLowerCase();
    for (int i = 0; i < suffixList.length; i++) {
      final String s = "." + suffixList[i];
      if (lname.endsWith(s)) return true;
      for (int j = 0; j < COMPRESSION_SUFFIXES.length; j++) {
        if (lname.endsWith(s + "." + COMPRESSION_SUFFIXES[j])) return true;
      }
    }
    return false;
  }

}
