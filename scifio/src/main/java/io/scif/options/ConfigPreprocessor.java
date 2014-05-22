/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif.options;

import io.scif.config.SCIFIOConfig;

import org.scijava.Priority;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link PreprocessorPlugin} implementation for populating {@link SCIFIOConfig}
 * {@link Parameter}s. Requires exactly one unresolved {@code SCIFIOConfig}
 * parameter.
 * 
 * @author Mark Hiner
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.LOW_PRIORITY)
public class ConfigPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private OptionsService optionsService;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (optionsService == null) return;

		final ModuleItem<SCIFIOConfig> configInput = getConfigInput(module);
		if (configInput == null) return;

		SCIFIOConfig result = configInput.getValue(module);

		// show file chooser dialog box
		final OptionsSCIFIO options = optionsService.getOptions(OptionsSCIFIO.class);

		if (options == null) {
			cancel("");
			return;
		}

		if (result == null) {
			result = new SCIFIOConfig();
		}

		options.populate(result);

		configInput.setValue(module, result);
		module.setResolved(configInput.getName(), true);
	}

	// -- Helper methods --

	/**
	 * Gets the single unresolved {@link SCIFIOConfig} input parameter. If there
	 * is not exactly one unresolved {@link SCIFIOConfig} input parameter, this
	 * method returns null.
	 */
	private ModuleItem<SCIFIOConfig> getConfigInput(final Module module) {
		ModuleItem<SCIFIOConfig> result = null;
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (module.isResolved(input.getName())) continue;
			final Class<?> type = input.getType();
			if (SCIFIOConfig.class.isAssignableFrom(type) && result != null) {
				// second SCIFIOConfig parameter; abort
				return null;
			}
			@SuppressWarnings("unchecked")
			final ModuleItem<SCIFIOConfig> configInput =
				(ModuleItem<SCIFIOConfig>) input;
			result = configInput;
		}
		return result;
	}
}
