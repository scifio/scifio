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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ome.scifio.io.RandomAccessInputStream;
import ome.scifio.util.FormatTools;

/**
 * Abstract superclass of all SCIFIO checker components. 
 *
 */
public abstract class AbstractChecker<M extends Metadata>
  extends AbstractHasContext implements Checker<M> {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

  // -- Fields --

  /**
   * Whether the file extension matching one of the reader's suffixes
   * is necessary to identify the file as an instance of this format.
   */
  protected boolean suffixNecessary = true;

  /**
   * Whether the file extension matching one of the reader's suffixes
   * is sufficient to identify the file as an instance of this format.
   */
  protected boolean suffixSufficient = true;

  // -- Constructors --

  /** Constructs a checker with the given context */
  public AbstractChecker(final SCIFIO ctx)
  {
    super(ctx);
  }

  // -- HasFormat API --

  @SuppressWarnings("unchecked")
  public Format<M, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromChecker(getClass());
  }

  // -- Checker API Methods --

  /* @see Checker#isFormat(String name, boolean open) */
  public boolean isFormat(final String name) {
    return isFormat(name, true);
  }
  
  /* @see Checker#isFormat(String name, boolean open) */
  public boolean isFormat(final String name, final boolean open) {
    // if file extension ID is insufficient and we can't open the file, give up
    if (!suffixSufficient && !open) return false;

    if (suffixNecessary || suffixSufficient) {
      // it's worth checking the file extension
      final boolean suffixMatch = FormatTools.checkSuffix(name, getFormat().getSuffixes());
      ;

      // if suffix match is required but it doesn't match, failure
      if (suffixNecessary && !suffixMatch) return false;

      // if suffix matches and that's all we need, green light it
      if (suffixMatch && suffixSufficient) return true;
    }

    // suffix matching was inconclusive; we need to analyze the file contents
    if (!open) return false; // not allowed to open any files
    try {
      final RandomAccessInputStream stream = new RandomAccessInputStream(name);
      final boolean isFormat = isFormat(stream);
      stream.close();
      return isFormat;
    }
    catch (final IOException exc) {
      LOGGER.debug("", exc);
      return false;
    }
  }

  /* @see Checker#isFormat(byte[] block) */
  public boolean isFormat(final byte[] block) {
    try {
      final RandomAccessInputStream stream = new RandomAccessInputStream(block);
      final boolean isFormat = isFormat(stream);
      stream.close();
      return isFormat;
    }
    catch (final IOException e) {
      LOGGER.debug("", e);
    }
    return false;
  }

  /* @see Checker#isFormat(RandomAccessInputStream) */
  public boolean isFormat(final RandomAccessInputStream stream)
    throws IOException
  {
    return false;
  }
}
