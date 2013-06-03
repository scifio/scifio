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

package loci.formats;

import java.io.IOException;

import loci.legacy.context.LegacyContext;

/**
 * AxisGuesser guesses which blocks in a file pattern correspond to which
 * dimensional axes (Z, T or C), potentially recommending an adjustment in
 * dimension order within the files, depending on the confidence of each guess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/AxisGuesser.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/AxisGuesser.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * 
 * @deprecated see io.scif.AxisGuesser
 */
@Deprecated
public class AxisGuesser extends io.scif.AxisGuesser {
  // -- Constructor --

  /**
   * Guesses dimensional axis assignments corresponding to the given
   * file pattern, using the specified dimensional information from
   * within each file as a guide.
   *
   * @param fp The file pattern of the files
   * @param dimOrder The dimension order (e.g., XYZTC) within each file
   * @param sizeZ The number of Z positions within each file
   * @param sizeT The number of T positions within each file
   * @param sizeC The number of C positions within each file
   * @param isCertain Whether the dimension order given is known to be good,
   *   or merely a guess
   *
   * @see FilePattern
   */
  public AxisGuesser(FilePattern fp, String dimOrder,
    int sizeZ, int sizeT, int sizeC, boolean isCertain)
  {
    super(fp, dimOrder, sizeZ, sizeT, sizeC, isCertain);
  }

  // -- Main method --

  /** Method for testing pattern guessing logic. */
  public static void main(String[] args) throws FormatException, IOException {
    try {
      io.scif.AxisGuesser.main(args, LegacyContext.get());
    } catch (io.scif.FormatException e) {
      throw new FormatException(e);
    }
  }

}

// -- Notes --

// INPUTS: file pattern, dimOrder, sizeZ, sizeT, sizeC, isCertain
//
// 1) Fill in all "known" dimensional axes based on known patterns and
//    conventions
//      * known internal axes (ZCT) have isCertain == true
//      * known dimensional axes have a known pattern or convention
//    After that, we are left with only unknown slots, which we must guess.
//
// 2) First, we decide whether we really "believe" the reader. There is a
//    special case where we may decide that it got Z and T mixed up:
//      * if a Z block was found, but not a T block:
//          if !isOrderCertain, and sizeZ > 1, and sizeT == 1, swap 'em
//      * else if a T block was found, but not a Z block:
//          if !isOrderCertain and sizeT > 1, and sizeZ == 1, swap 'em
//    At this point, we can (have to) trust the internal ordering, and use it
//    to decide how to fill in the remaining dimensional blocks.
//
// 3) Set canBeZ to true iff no Z block is assigned and sizeZ == 1.
//    Set canBeT to true iff no T block is assigned and sizeT == 1.
//    Go through the blocks in order from left to right:
//      * If canBeZ, assign Z and set canBeZ to false.
//      * If canBeT, assign T and set canBeT to false.
//      * Otherwise, assign C.
//
// OUTPUTS: list of axis assignments, new dimOrder
