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

import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.ImageRegion;
import io.scif.img.Range;
import io.scif.services.DatasetIOService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 * {@link Command} for opening a given {@code File} as a {@link Dataset}.
 *
 * @author Mark Hiner
 */
@Plugin(type = Command.class, menu = { @Menu(label = MenuConstants.FILE_LABEL,
	weight = MenuConstants.FILE_WEIGHT), @Menu(label = "Import"), @Menu(
		label = "Image... ") })
public class OpenDataset extends ContextCommand {

	@Parameter
	private DatasetIOService datasetIOService;

	@Parameter
	private LogService logService;

	@Parameter
	private UIService uiService;

	@Parameter(label = "File to open")
	private File source;

	@Parameter(required = false)
	private Boolean crop;

	@Parameter(label = "Autoscale", required = false)
	private Boolean autoscale;

	@Parameter(label = "Compute min/max values", required = false)
	private Boolean computeMinMax;

	// TODO callback to enable/disable these fields based on crop value
	@Parameter(required = false, min = "0")
	private final Integer x = 0;

	@Parameter(required = false, min = "0")
	private final Integer y = 0;

	@Parameter(required = false, min = "0")
	private final Integer w = 0;

	@Parameter(required = false, min = "0")
	private final Integer h = 0;

	@Parameter(required = false, label = "Image indices")
	private String range;

	@Parameter(required = false, label = "Group similar files")
	private Boolean groupFiles;

	@Parameter(required = false, label = "Image mode", choices = { "Auto",
		"Planar", "Cell" })
	private final String mode = "Auto";

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset dataset;

	@Override
	public void run() {
		final SCIFIOConfig config = new SCIFIOConfig();

		// Set the image index range if desired
		if (range != null && !range.isEmpty()) {
			try {
				config.imgOpenerSetRange(range);
			}
			catch (final IllegalArgumentException e) {
				logService.warn("Ignoring bad range: " + range);
			}
		}

		// Crop if desired
		if (crop != null && crop) {
			if (validRange()) {
				final Map<AxisType, Range> region = new HashMap<>();
				region.put(Axes.X, new Range(new Long(x), new Long(x + w - 1)));
				region.put(Axes.Y, new Range(new Long(y), new Long(y + h - 1)));
				config.imgOpenerSetRegion(new ImageRegion(region));
			}
			else {
				logService.warn("ignoring bad crop region: " + x + ", " + y + ", " + w +
					", " + h);
			}
		}

		// Set compute min/max if desired
		if (computeMinMax != null) {
			config.imgOpenerSetComputeMinMax(computeMinMax);
		}

		// Set the groupFiles flag if desired
		if (groupFiles != null) {
			config.groupableSetGroupFiles(groupFiles);
		}

		// Set the desired image modes
		if (mode.equals("Planar")) config.imgOpenerSetImgModes(ImgMode.PLANAR);
		else if (mode.equals("Cell")) config.imgOpenerSetImgModes(ImgMode.CELL);

		// Open the dataset
		try {
			dataset = datasetIOService.open(source.getAbsolutePath(), config);
		}
		catch (final IOException e) {
			logService.error(e);
			error(e.getMessage());
		}

		// If autoscaling is requested, clear out any channel min/max on the Dataset
		// so that a DatasetView will know to autoscale.
		if (autoscale != null && autoscale) {
			for (int c = 0; c < dataset.getCompositeChannelCount(); c++) {
				dataset.setChannelMinimum(c, Double.NaN);
				dataset.setChannelMaximum(c, Double.NaN);
			}
		}
	}

	/**
	 * @return true if all params are non-null and positive.
	 */
	private boolean validRange() {
		return (x != null && y != null && w != null && h != null) && (x >= 0 &&
			y >= 0 && w >= 0 && h >= 0);
	}

	// -- Helper methods --

	private void error(final String message) {
		uiService.showDialog(message, DialogPrompt.MessageType.ERROR_MESSAGE);
	}
}
