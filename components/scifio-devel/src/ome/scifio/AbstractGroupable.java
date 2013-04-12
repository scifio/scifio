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

import java.io.IOException;

import ome.scifio.util.FormatTools;

/**
 * Abstract super-class for all {@link ome.scifio.Groupable} components.
 * 
 * @author Mark Hiner
 * 
 * @see ome.scifio.Groupable
 *
 */
public abstract class AbstractGroupable extends AbstractHasSource implements Groupable {

  /** Whether or not to group multi-file formats. */
  private boolean group = true;
  
  /*
   * @see ome.scifio.Groupable#setGroupFiles(boolean)
   */
  public void setGroupFiles(final boolean groupFiles) {
    group = groupFiles;
  }

  /*
   * @see ome.scifio.Groupable#isGroupFiles()
   */
  public boolean isGroupFiles() {
    return group;
  }

  /*
   * @see ome.scifio.Groupable#fileGroupOption(String)
   */
  public int fileGroupOption(final String id)
    throws FormatException, IOException
  {
    return FormatTools.CANNOT_GROUP;
  }
  
  /*
   * @see ome.scifio.Groupable#isSingleFile()
   */
  public boolean isSingleFile(final String id) throws FormatException, IOException
  {
    return true;
  }

  /*
   * @see ome.scifio.Groupable#hasCompanionFiles()
   */
  public boolean hasCompanionFiles() {
    return false;
  }
}
