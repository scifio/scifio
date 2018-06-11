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

package io.scif.formats.qt;

import io.scif.FormatException;
import io.scif.MissingLibraryException;
import io.scif.SCIFIOService;

import java.awt.Dimension;
import java.awt.Image;

import org.scijava.util.ReflectException;
import org.scijava.util.ReflectedUniverse;

/**
 * Interface for services that work with QuickTime for Java.
 *
 * @author Curtis Rueden
 * @author Melissa Linkert
 */
public interface QTJavaService extends SCIFIOService {

	/** Whether QuickTime is available to this JVM. */
	boolean canDoQT();

	/** Whether this JVM is 64-bit. */
	boolean isJVM64Bit();

	/** Whether QuickTime for Java has expired. */
	boolean isQTExpired();

	/** Gets the QuickTime for Java version number. */
	String getQTVersion();

	/** Gets QuickTime for Java reflected universe. */
	ReflectedUniverse getUniverse();

	/** Gets width and height for the given PICT bytes. */
	Dimension getPictDimensions(byte[] bytes) throws FormatException,
		ReflectException;

	/** Converts the given byte array in PICT format to a Java image. */
	Image pictToImage(byte[] bytes) throws FormatException;

	/** Checks whether QTJava is available, throwing an exception if not. */
	void checkQTLibrary() throws MissingLibraryException;

}
