/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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
package io.scif;

import io.scif.util.FormatTools;

import java.io.IOException;


/**
 * Abstract super-class for all {@link io.scif.Groupable} components.
 * 
 * @author Mark Hiner
 * 
 * @see io.scif.Groupable
 *
 */
public abstract class AbstractGroupable extends AbstractHasSource implements Groupable {

  /** Whether or not to group multi-file formats. */
  private boolean group = true;

  /*
   * @see io.scif.Groupable#setGroupFiles(boolean)
   */
  public void setGroupFiles(final boolean groupFiles) {
    group = groupFiles;
  }

  /*
   * @see io.scif.Groupable#isGroupFiles()
   */
  public boolean isGroupFiles() {
    return group;
  }

  /*
   * @see io.scif.Groupable#fileGroupOption(String)
   */
  public int fileGroupOption(final String id)
    throws FormatException, IOException
  {
    return FormatTools.CANNOT_GROUP;
  }

  /*
   * @see io.scif.Groupable#isSingleFile()
   */
  public boolean isSingleFile(final String id) throws FormatException, IOException
  {
    return true;
  }

  /*
   * @see io.scif.Groupable#hasCompanionFiles()
   */
  public boolean hasCompanionFiles() {
    return false;
  }
}
