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

import java.io.File;
import java.util.HashMap;

import net.imagej.Dataset;
import net.imagej.ImgPlus;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Saves the current {@link Dataset} to disk.
 *
 * @author Barry DeZonia
 * @author Mark Hiner
 */
@Plugin(type = Command.class, menu = { @Menu(label = MenuConstants.FILE_LABEL,
	weight = MenuConstants.FILE_WEIGHT, mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Save", weight = 20, mnemonic = 's', accelerator = "^S") },
	attrs = { @Attr(name = "no-legacy") })
public class SaveImage extends ContextCommand {

	@Parameter
	private CommandService commandService;

	@Parameter
	private Dataset dataset;

	@Override
	public void run() {
		final HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("dataset", dataset);

		final ImgPlus<?> img = dataset.getImgPlus();
		final String source = img.getSource();

		final File sourceFile = source.isEmpty() ? null : new File(source);

		if (sourceFile != null && sourceFile.isFile()) {
			inputMap.put("outputFile", new File(source));
		}
		commandService.run(SaveAsImage.class, true, inputMap);
	}

	public void setDataset(final Dataset d) {
		dataset = d;
	}

	public Dataset getDataset() {
		return dataset;
	}

}
