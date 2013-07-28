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

package io.scif.io.utests.providers;

import io.scif.io.BZip2Handle;
import io.scif.io.IRandomAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.scijava.Context;



/**
 * Implementation of IRandomAccessProvider that produces instances of
 * loci.common.BZip2Handle.
 *
 *
 * @see IRandomAccessProvider
 * @see io.scif.io.BZip2Handle
 */
class BZip2HandleProvider extends ContextualProvider implements IRandomAccessProvider {

  public IRandomAccess createMock(
      byte[] page, String mode, int bufferSize) throws IOException {
    File pageFile = File.createTempFile("page", ".dat");
    FileOutputStream out = new FileOutputStream(pageFile);
    out.write(page);
    out.close();

    Runtime rt = Runtime.getRuntime();
    Process p = rt.exec(new String[] {"bzip2", pageFile.getAbsolutePath()});
    try {
      p.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    pageFile = new File(pageFile.getAbsolutePath() + ".bz2");
    return new BZip2Handle(getContext(), pageFile.getAbsolutePath());
  }

}
