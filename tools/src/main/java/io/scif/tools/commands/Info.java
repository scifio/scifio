/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.tools.commands;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.MetaTable;
import io.scif.Metadata;
import io.scif.services.InitializeService;
import io.scif.tools.AbstractSCIFIOToolCommand;
import io.scif.tools.SCIFIOToolCommand;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.meta.CalibratedAxis;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Parses and prints the metadata for a given dataset. Convenience tool for
 * verifying image structure.
 * 
 * @author Mark Hiner
 */
@Plugin(type = SCIFIOToolCommand.class)
public class Info extends AbstractSCIFIOToolCommand {

	// -- Fields --

	@Parameter
	InitializeService initializeService;

	// -- Arguments --

	@Argument(metaVar = "file", index = 0, usage = "image dataset to parse")
	private String file;

	@Argument(index = 1, multiValued = true)
	private List<String> arguments = new ArrayList<String>();

	// -- AbstractSCIFIOToolCommand API --

	@Override
	protected void run() throws CmdLineException {
		try {
			Metadata meta = initializeService.parseMetadata(file, true);
			printDatasetMetadata(meta);
			printImageMetadata(meta);
		}
		catch (FormatException e) {
			throw new CmdLineException(null, e.getMessage());
		}
		catch (IOException e) {
			throw new CmdLineException(null, e.getMessage());
		}
	}

	@Override
	protected String description() {
		return "command line tool for printing metadata from a dataset.";
	}

	@Override
	protected String getName() {
		return "info";
	}

	@Override
	protected List<String> getExtraArguments() {
		return arguments;
	}

	@Override
	protected void validateParams() throws CmdLineException {
		if (file == null) {
			throw new CmdLineException(null, "Argument \"file\" is required");
		}
	}

	// -- Helper methods --

	/**
	 * @param meta Prints the ImageMetadata for this dataset Metadata object
	 */
	private void printImageMetadata(Metadata meta) {
		info("");
		info("Reading image metdata");

		for (int i = 0; i < meta.getImageCount(); i++) {
			info("Image: " + (i + 1));
			ImageMetadata iMeta = meta.get(i);
			printTable(iMeta.getTable());
			print(iMeta);
		}

	}

	/**
	 * @param iMeta Prints useful information about this ImageMetadata
	 */
	private void print(ImageMetadata iMeta) {
		info("Image size: " + iMeta.getSize());
		info("Plane size: " + iMeta.getPlaneSize());
		info("Plane count: " + iMeta.getPlaneCount());
		info("Planar axes: " + getAxisLabels(iMeta.getAxesPlanar()));
		info("Planar lenghts: " + getAxisLengths(iMeta.getAxesLengthsPlanar()));
		info("Non-planar axes: " + getAxisLabels(iMeta.getAxesNonPlanar()));
		info("Non-planar lenghts: " +
			getAxisLengths(iMeta.getAxesLengthsNonPlanar()));
		info("Axis calibration: " + getAxisCalibrations(iMeta));
		info("Pixel type: " + FormatTools.getPixelTypeString(iMeta.getPixelType()));
		info("Bits per pixel: " + iMeta.getBitsPerPixel());
		info("Interleaved axes: " + iMeta.getInterleavedAxisCount());
		info("Thumbnail width: " + iMeta.getThumbSizeX());
		info("Thumbnail height: " + iMeta.getThumbSizeY());
		info("False color: " + iMeta.isFalseColor());
		info("Indexed: " + iMeta.isIndexed());
		info("Little endian: " + iMeta.isLittleEndian());
		info("Metadata complete: " + iMeta.isMetadataComplete());
		info("Order certain: " + iMeta.isOrderCertain());
		info("Thumbnail: " + iMeta.isThumbnail());
	}

	/**
	 * @param axesLengths array of axis lengths
	 * @return The provided axesLenghts array converted to a comma-separated
	 *         string
	 */
	private String getAxisLengths(long[] axesLengths) {
		StringBuilder sb = new StringBuilder();
		for (long l : axesLengths) {
			if (sb.length() > 0) sb.append(",");
			sb.append(l);
		}
		return sb.toString();
	}

	/**
	 * @param iMeta - ImageMetadata to gather calibration data for
	 * @return - A comma-separated string list of the calibration values for the
	 *         given ImageMetadata.
	 */
	private String getAxisCalibrations(ImageMetadata iMeta) {
		StringBuilder sb = new StringBuilder();

		for (CalibratedAxis axis : iMeta.getAxes()) {
			if (sb.length() > 0) sb.append(",");
			sb.append(axis.particularEquation() + " " + axis.unit());
		}

		return sb.toString();
	}

	/**
	 * @param axes List of axes used to build type information
	 * @return A comma-spearated string representation of the provided axes' types
	 */
	private String getAxisLabels(List<CalibratedAxis> axes) {
		StringBuilder sb = new StringBuilder();
		for (CalibratedAxis axis : axes) {
			if (sb.length() > 0) sb.append(",");
			sb.append(axis.type().getLabel());
		}
		return sb.toString();
	}

	/**
	 * @param meta Print dataset-level information about this Metadata object.
	 */
	private void printDatasetMetadata(Metadata meta) {
		info("");
		info("Dataset: " + meta.getDatasetName());
		info("Dataset size: " + meta.getDatasetSize());
		info("Image count: " + meta.getImageCount());
		info("Reading dataset metdata");
		printTable(meta.getTable());
	}

	/**
	 * @param table Prints the key:value pairs in the given MetaTable object.
	 *          Expands lists when needed.
	 */
	private void printTable(MetaTable table) {
		for (String key : table.keySet()) {
			Object val = table.get(key);
			if (val instanceof Collection) {
				info(key + ":");
				for (Object listVal : (Collection<?>) val) {
					info("\t" + listVal);
				}
			}
			else {
				info(key + ": " + val);
			}
		}
	}
}
