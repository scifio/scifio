/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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
 * #L%
 */

/**
 * Provides the classes for reading and writing information between image
 * sources.
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 * <li>Added {@link io.scif.io.IStreamAccess} interface. This interface is used
 * as a middle ground for {@link io.scif.io.IRandomAccess} implementations that
 * will be stream based, and extracts many methods that were injected in an
 * abstract layer in Bio-Formats.</li>
 * <li>The static functionality in Location has been extracted to
 * {@link io.scif.services.LocationService}</li>
 * </ul>
 * <h3>Future plans</h3>
 * <ul>
 * <li>{code IRandomAccess} naming. The {@code IRandomAccess} interface should
 * define a {@code getName} and {@code setName} method, which will be
 * implemented in the various handles.
 * <p>
 * This value should be chained up to the {@code RandomAccessInputStream}
 * {@code toString} method.
 * </p>
 * </li>
 * <li>Remove id maps from {@link io.scif.io.Location}. Should be able to
 * construct with just the {@link io.scif.io.RandomAccessInputStream}.</li>
 * </ul>
 *
 * @author Mark Hiner
 */

package io.scif.io;
