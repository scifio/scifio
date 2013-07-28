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

import io.scif.io.RandomAccessInputStream;

import java.io.IOException;


/**
 * Interface for all {@link io.scif.Reader} implementations that use generic
 * parameters.
 * <p>
 * Generics are used in {@code Reader} concrete implementations to type narrow
 * return types, and to provide parallel methods that can type narrow arguments.
 * </p>
 * 
 * @author Mark Hiner
 *
 * @param <M> - {@link io.scif.Metadata} used by this reader for reading images.
 * @param <P> - {@link io.scif.Plane} return and parameter type for this reader's
 *              {@link #openPlane} and {@link #readPlane} methods.
 * 
 * @see {@link #openPlane}
 * @see {@link #readPlane}
 * @see {@link #setMetadata}
 * @see {@link #getMetadata}
 */
public interface TypedReader<M extends TypedMetadata, P extends DataPlane<?>> extends Reader {

  /*
   * @see io.scif.Reader#openPlane(int, int)
   */
  P openPlane(int imageIndex, int planeIndex)
    throws FormatException, IOException;

  /*
   * @see io.scif.Reader#openPlane(int, int, int, int, int, int)
   */
  P openPlane(int imageIndex, int planeIndex, int x, int y, int w, int h)
    throws FormatException, IOException;

  /**
   * Generic-parameterized {@code openPlane} method, using
   * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
   * {@link io.scif.Reader#openPlane(int, int, Plane)}.
   * 
   * @see {@link io.scif.Reader#openPlane(int, int, Plane)}
   */
  P openPlane(int imageIndex, int planeIndex, P plane)
    throws FormatException, IOException;

  /**
   * Generic-parameterized {@code openPlane} method, using
   * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
   * {@link io.scif.Reader#openPlane(int, int, Plane, int, int, int, int)}.
   * 
   * @see {@link io.scif.Reader#openPlane(int, int, Plane, int, int, int, int)}
   */
  P openPlane(int imageIndex, int planeIndex, P plane, int x, int y,
    int w, int h) throws FormatException, IOException;

  /*
   * @see io.scif.Reader#openThumbPlane(int, int)
   */
  P openThumbPlane(int imageIndex, int planeIndex)
    throws FormatException, IOException;

  /**
   * Generic-parameterized {@code setMetadata} method, using
   * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
   * {@link io.scif.Reader#setMetadata(Metadata)}.
   * 
   * @see {@link io.scif.Reader#setMetadata(Metadata)}
   */
  void setMetadata(M meta) throws IOException;

  /*
   * @see io.scif.Reader#getMetadata()
   */
  M getMetadata();

  /**
   * Generic-parameterized {@code readPlane} method, using
   * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
   * {@link io.scif.Reader#readPlane(RandomAccessInputStream, int,
   * int, int, int, int, Plane)}
   * 
   * @see {@link io.scif.Reader#readPlane(RandomAccessInputStream, int,
   *             int, int, int, int, Plane)}
   */
  P readPlane(RandomAccessInputStream s, int imageIndex, int x, int y,
    int w, int h, P plane) throws IOException;

  /**
   * Generic-parameterized {@code readPlane} method, using
   * {@link io.scif.TypedMetadata} to avoid type erasure conflicts with
   * {@link io.scif.Reader#readPlane(RandomAccessInputStream, int,
   * int, int, int, int, int, Plane)}
   * 
   * @see {@link io.scif.Reader#readPlane(RandomAccessInputStream, int,
   *             int, int, int, int, int, Plane)}
   */
  P readPlane(RandomAccessInputStream s, int imageIndex, int x, int y,
    int w, int h, int scanlinePad, P plane) throws IOException;

  /*
   * @see io.scif.Reader#createPlane(int, int, int, int)
   */
  P createPlane(int xOffset, int yOffset, int xLength, int yLength);

  /**
   * Returns the class of {@code Planes} associated with this {@code Reader}.
   * @return
   */
  Class<P> getPlaneClass();
}
