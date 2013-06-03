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

package loci.common;

/**
 * A legacy delegator class for io.scif.common.Region.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/src/loci/common/Region.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/src/loci/common/Region.java;hb=HEAD">Gitweb</a></dd></dl>
 * 
 * @deprecated see io.scif.common.Region
 */
@Deprecated
public class Region extends io.scif.common.Region {

  // -- Constructors --
  
  public Region() {
    super();
  }
  
  public Region(int x, int y, int w, int h) {
    super(x,y,w,h);
  }
  
  // -- Wrapper API --
  
  public Region intersection(Region r) {
    return convertRegion(super.intersection(r));
  }
  
  // -- Helper Methods --
  
  /*
   * ---HACK---
   * Returns a loci.common Region based on the provided
   *  {@link io.scif.common.Region}.
   *  
   *  This is necessary because loci.common.Region has to directly
   *  extend io.scif.common.Region, because of the public field
   *  contract that was established in this class.
   *  
   *  Extension was not sufficient for full backwards compatibility,
   *  as io.scif.common.Region#intersection always returns an
   *  io.scif.common.Region which can not be safely cast to a loci.common
   *  Region.
   *  
   *  Thus this method constructs a loci.common Region assuming the base
   *  io.scif.common.Region was provided.
   */
  private Region convertRegion(io.scif.common.Region r) {
    return new Region(r.x, r.y, r.width, r.height);
  }

}
