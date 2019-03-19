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

package io.scif.gui;

import io.scif.Format;
import io.scif.SCIFIOService;

import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.scijava.service.Service;

/**
 * A {@link Service} for working with graphical user interfaces.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public interface GUIService extends SCIFIOService {

	/** Constructs a list of file filters for the given file format handler. */
	FileFilter[] buildFileFilters(Collection<Format> formats);

	/** Constructs a file chooser for the given file format handler. */
	JFileChooser buildFileChooser(Collection<Format> formats);

	/**
	 * Constructs a file chooser for the given file format handler. If preview
	 * flag is set, chooser has an preview pane showing a thumbnail and other
	 * information for the selected file.
	 */
	JFileChooser buildFileChooser(Collection<Format> formats, boolean preview);

	/**
	 * Builds a file chooser with the given file filters, as well as an "All
	 * supported file types" combo filter.
	 */
	JFileChooser buildFileChooser(FileFilter[] filters);

	/**
	 * Builds a file chooser with the given file filters, as well as an "All
	 * supported file types" combo filter, if one is not already specified.
	 *
	 * @param preview If set, chooser has a preview pane showing a thumbnail and
	 *          other information for the selected file.
	 */
	JFileChooser buildFileChooser(FileFilter[] filters, boolean preview);

}
