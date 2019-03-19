/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2018 SCIFIO developers.
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

package io.scif.commands;

import io.scif.services.DatasetIOService;

import java.io.File;
import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.io.location.FileLocation;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

/**
 * Saves the current {@link Dataset} to disk using a user-specified file name.
 *
 * @author Mark Hiner
 */
@Plugin(type = Command.class, menu = { @Menu(label = MenuConstants.FILE_LABEL,
	weight = MenuConstants.FILE_WEIGHT, mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Save As...", weight = 21, accelerator = "shift ^S") },
	attrs = { @Attr(name = "no-legacy") })
public class SaveAsImage extends ContextCommand {

	@Parameter
	private LogService log;

	@Parameter
	private DatasetIOService datasetIOService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private UIService uiService;

	@Parameter(label = "File to save", style = FileWidget.SAVE_STYLE,
		initializer = "initOutputFile", persist = false)
	private File outputFile;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	// -- Runnable methods --

	@Override
	public void run() {
		final Dataset dataset = dataset();
		try {
			datasetIOService.save(dataset, new FileLocation(outputFile.getAbsolutePath()));
		}
		catch (final IOException exc) {
			log.error(exc);
			uiService.showDialog(exc.getMessage(), "Error Saving Image",
				DialogPrompt.MessageType.ERROR_MESSAGE);
			return;
		}

		display.setName(dataset.getName());
	}

	// -- Initializer methods --

	protected void initOutputFile() {
		final Dataset dataset = dataset();
		if (dataset == null) return;
		outputFile = new File(dataset.getSource());
	}

	// -- Helper methods --

	private Dataset dataset() {
		return imageDisplayService.getActiveDataset(display);
	}

}
