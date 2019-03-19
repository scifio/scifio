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
 * This package provides configuration options for all SCIFIO components.
 * <p>
 * Configuration is achieved through use of the
 * {@link io.scif.config.SCIFIOConfig} class. Methods in this class are prefixed
 * based on what component they are modifying. The {@code SCIFIOConfig} then
 * operates as a single container of state, passed between each consumer, which
 * queries the values that apply to them.
 * </p>
 * <p>
 * Configuration is scoped within method execution, thus SCIFIOConfig objects
 * should not be cached, but instead always passed through method signatures.
 * </p>
 * <p>
 * Adding new configuration options requires three steps:
 * </p>
 * <ul>
 * <li>Add the appropriate field(s) and setter/accessor methods to
 * {@code SCIFIOConfig}.</li>
 * <li>Modify the class being configured to accept {@code SCIFIOConfig} objects
 * and query the desired information.</li>
 * <li>Update any downstream code of the newly configurable class to pass along
 * {@code SCIFIOConfig} objects.</li>
 * </ul>
 *
 * @author Mark Hiner
 */

package io.scif.config;
