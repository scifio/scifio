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
package ome.scifio.services;

import java.io.IOException;

import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Writer;
import ome.scifio.filters.ReaderFilter;

import org.scijava.service.Service;

/**
 * A collection of methods for initializing the
 * IO components of SCIFIO (Readers and Writers). All parsing
 * and Metadata setup is done automatically.
 * <p>
 * NB: The {@link #initializeReader()} line of methods return
 * a {@link ome.scifio.filters.ReaderFilter} instead of a basic Reader.
 * This is a convenience to allow filters to be enabled if desired.
 * </p>
 * 
 * @see ome.scifio.Reader
 * @see ome.scifio.Writer
 * @see ome.scifio.filters.ReaderFilter
 * 
 * @author Mark Hiner
 *
 */
public interface InitializeService extends Service {

  /**
   * See {@link #initializeReader(String, boolean)}. Will not open the image
   * source while parsing metadata.
   * 
   * @param id Name of the image source to be read.
   * @return An initialized {@code Reader}.
   */
  ReaderFilter initializeReader(String id) throws FormatException, IOException;

  /**
   * Convenience method for creating a {@code Reader} component that is ready
   * to open planes of the provided image source. The reader's {@code Metadata}
   * and source fields will be populated.
   * 
   * @param id Name of the image source to be read.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Reader}.
   */
  ReaderFilter initializeReader(String id, boolean openFile)
    throws FormatException, IOException;

  /**
   * See {@link #initializeWriter(String, String, boolean)}. Will not open the
   * image source while parsing metadata.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @return An initialized {@code Writer}.
   */
  Writer initializeWriter(String source, String destination)
    throws FormatException, IOException;

  /**
   * Convenience method for creating a {@code Writer} component that is ready
   * to save planes to the destination image. {@code Metadata} will be parsed
   * from the source, translated to the destination's type if necessary, and
   * set (along with the destination itself) on the resulting {@code Writer}.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @param openFile If true, the image source may be read during metadata
   *        parsing.
   * @return An initialized {@code Writer}.
   */
  Writer initializeWriter(String source, String destination, boolean openSource)
    throws FormatException, IOException;
  
  /**
   * See {@link #initializeWriter(String, String, boolean)}. Will not open the
   * image source while parsing metadata.
   * 
   * @param source Name of the image source to use for parsing metadata.
   * @param destination Name of the writing destination.
   * @return An initialized {@code Writer}.
   */
  Writer initializeWriter(Metadata sourceMeta, String destination)
    throws FormatException, IOException;
}
