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
 * Provides {@link io.scif.Format} implementations for all file formats that are
 * supported by default in SCIFIO.
 * <p>
 * The set of default formats are all open formats that can be freely read and
 * written, to facilitate the inclusion of SCIFIO as an image IO library in
 * external projects.
 * </p>
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 * <li>In Bio-Formats, image input and output was separated by Reader and Writer
 * classes. Although these classes still exist in SCIFIO, some operations have
 * been split out (especially from the Reader) to other components. All of these
 * classes are collected under a single {@link io.scif.Format} as nested
 * classes.</li>
 * </ul>
 * <h3>Future plans</h3>
 * <ul>
 * <li>Convert the remaining open source formats (in the scifio component,
 * loci.formats.in and loci.formats.out) to SCIFIO Formats.</li>
 * </ul>
 *
 * @author Mark Hiner
 * @see io.scif.Format
 */

package io.scif.formats;
