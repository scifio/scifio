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

import org.scijava.menu.MenuConstants;
import org.scijava.options.OptionsPlugin;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Plugin;


/**
 * {@link OptionsPlugin} implementation used to present options and populate
 * {@link SCIFIOConfig} instances. Use the {@link #populate(SCIFIOConfig)}
 * method to transfer or create a corresponding {@code SCIFIOConfig}.
 * 
 * @author Mark Hiner
 */
@Plugin(type = OptionsSCIFIO.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
			mnemonic = MenuConstants.EDIT_MNEMONIC),
		@Menu(label = "Options", mnemonic = 'o'),
		@Menu(label = "SCIFIO", weight = 3) })
public class OptionsSCIFIO extends OptionsPlugin {

	/**
	 * Applies the settings in this options plugin to a given {@link SCIFIOConfig}
	 * instance. Creates a new {@code SCIFIOConfig} if the provided instance is
	 * null.
	 *
	 * @return The populated {@code SCIFIOConfig} instance.
	 */
	public SCIFIOConfig populate(SCIFIOConfig config) {
		if (config == null) {
			config = new SCIFIOConfig();
		}

		// Apply options

		return config;
	}
}
