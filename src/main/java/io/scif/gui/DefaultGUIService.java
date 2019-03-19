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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Basic {@link GUIService} implementation.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultGUIService extends AbstractService implements GUIService {

	// -- Fields --

	/** String to use for "all types" combination filter. */
	private final String ALL_TYPES = "All supported file types";

	// -- GUIService Methods --

	@Override
	public FileFilter[] buildFileFilters(final Collection<Format> formats) {

		final FileFilter[] filters = new FileFilter[formats.size()];
		int i = 0;

		for (final Format f : formats) {
			filters[i++] = new FormatFileFilter(f);
		}

		return filters;
	}

	@Override
	public JFileChooser buildFileChooser(final Collection<Format> formats) {
		return buildFileChooser(formats, true);
	}

	@Override
	public JFileChooser buildFileChooser(final Collection<Format> formats,
		final boolean preview)
	{
		return buildFileChooser(buildFileFilters(formats), preview);
	}

	@Override
	public JFileChooser buildFileChooser(final FileFilter[] filters) {
		return buildFileChooser(filters, true);
	}

	@Override
	public JFileChooser buildFileChooser(final FileFilter[] filters,
		final boolean preview)
	{
		// NB: construct JFileChooser in the AWT worker thread, to avoid
		// deadlocks
		final JFileChooser[] jfc = new JFileChooser[1];
		final Runnable r = new Runnable() {

			@Override
			public void run() {
				final JFileChooser fc = new JFileChooser(System.getProperty(
					"user.dir"));
				final FileFilter[] ff = sortFilters(filters);

				FileFilter combo = null;
				if (ff.length > 0 && ff[0] instanceof ComboFileFilter) {
					// check for existing "All supported file types" filter
					final ComboFileFilter cff = (ComboFileFilter) ff[0];
					if (ALL_TYPES.equals(cff.getDescription())) combo = cff;
				}
				// make an "All supported file types" filter if we don't have
				// one yet
				if (combo == null) {
					combo = makeComboFilter(ff);
					if (combo != null) fc.addChoosableFileFilter(combo);
				}
				for (final FileFilter filter : ff)
					fc.addChoosableFileFilter(filter);
				if (combo != null) fc.setFileFilter(combo);
				if (preview) new PreviewPane(getContext(), fc);
				jfc[0] = fc;
			}
		};
		if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
			// current thread is the AWT event queue thread; just execute the
			// code
			r.run();
		}
		else {
			// execute the code with the AWT event thread
			try {
				SwingUtilities.invokeAndWait(r);
			}
			catch (final InterruptedException exc) {
				return null;
			}
			catch (final InvocationTargetException exc) {
				return null;
			}
		}
		return jfc[0];
	}

	// -- Helper methods --

	/**
	 * Creates an "All supported file types" combo filter encompassing all of the
	 * given filters.
	 */
	private FileFilter makeComboFilter(final FileFilter[] filters) {
		return filters.length > 1 ? new ComboFileFilter(filters, ALL_TYPES) : null;
	}

	/**
	 * Sorts the given list of file filters, keeping the "All supported file
	 * types" combo filter (if any) at the front of the list.
	 */
	private FileFilter[] sortFilters(FileFilter[] filters) {
		filters = ComboFileFilter.sortFilters(filters);
		shuffleAllTypesToFront(filters);
		return filters;
	}

	/**
	 * Looks for an "All supported file types" combo filter and shuffles it to the
	 * front of the list.
	 */
	private void shuffleAllTypesToFront(final FileFilter[] filters) {
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] instanceof ComboFileFilter) {
				if (ALL_TYPES.equals(filters[i].getDescription())) {
					final FileFilter f = filters[i];
					for (int j = i; j >= 1; j--)
						filters[j] = filters[j - 1];
					filters[0] = f;
					break;
				}
			}
		}
	}

}
