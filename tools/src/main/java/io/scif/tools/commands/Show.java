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
import io.scif.Plane;
import io.scif.Reader;
import io.scif.common.DataTools;
import io.scif.gui.AWTImageTools;
import io.scif.gui.ImageViewer;
import io.scif.io.ByteArrayHandle;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.LocationService;
import io.scif.tools.SCIFIOToolCommand;
import io.scif.util.AsciiImage;
import io.scif.util.FormatTools;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Opens a dataset for viewing. An ascii version can be printed as output for
 * convenience on headless systems, otherwise a simple AWT pane is opened,
 * per {@link ImageViewer}.
 * 
 * @author Mark Hiner
 */
@Plugin(type = SCIFIOToolCommand.class)
public class Show extends AbstractReaderCommand {

	// -- Fields --

	@Parameter
	private LocationService locationService;

	private List<BufferedImage> bImages;

	// -- Arguments --

	@Argument(metaVar = "file", index = 0, usage = "image file to display")
	private String file;

	@Argument(index = 1, multiValued = true)
	private List<String> arguments = new ArrayList<String>();

	// -- Options --

	@Option(
		name = "-A",
		aliases = "--ascii",
		usage = "display an ascii rendering of the image. useful on headless systems")
	private boolean ascii;

	@Option(name = "-b", aliases = "--thumbs",
		usage = "read thumbnails instead of normal pixels")
	private boolean thumbs;

	@Option(name = "-n", aliases = "--normalize",
		usage = "normalize floating point images (may result in loss of precision)")
	private boolean normalize;

	@Option(
		name = "-p",
		aliases = "--preload",
		usage = "pre-load entire file into a buffer. reduces read time, increases memory use.")
	private boolean preload;

	@Option(name = "-w", aliases = "--swap",
		usage = "override the default input dimension order")
	private List<String> swap;

	@Option(name = "-u", aliases = "--shuffle",
		usage = "override the default output dimension order")
	private boolean shuffle;

	// --info

	// -- AbstractSCIFIOToolCommand API --

	@Override
	protected void run() throws CmdLineException {
		try {
			mapLocation();
		}
		catch (IOException e) {
			throw new CmdLineException(null, e.getMessage());
		}

		Reader reader = makeReader(file);
		showPixels(reader);
	}

	@Override
	protected String description() {
		return "command line tool for displaying a dataset.";
	}

	@Override
	protected String getName() {
		return "show";
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

	// -- AbstractReaderCommand API --

	@Override
	protected Plane processPlane(Reader reader, Plane plane, int imageIndex,
		long planeIndex, long planeNo, long[] planeMin, long[] planeMax)
		throws CmdLineException
	{
		try {
			// open the plane
			if (thumbs) {
				plane = reader.openThumbPlane(imageIndex, planeIndex);
			}
			else {
				if (plane == null) {
					plane = reader.openPlane(imageIndex, planeIndex, planeMin, planeMax);
				}
				else {
					plane =
						reader.openPlane(imageIndex, planeIndex, plane, planeMin, planeMax);
				}
			}
		}
		catch (FormatException e) {
			throw new CmdLineException(null, e.getMessage());
		}
		catch (IOException e) {
			throw new CmdLineException(null, e.getMessage());
		}

		int pixelType = reader.getMetadata().get(imageIndex).getPixelType();
		boolean littleEndian =
			reader.getMetadata().get(imageIndex).isLittleEndian();

		// Convert the byte array to an appropriately typed data array
		Object pix =
			DataTools.makeDataArray(plane.getBytes(), FormatTools
				.getBytesPerPixel(pixelType), FormatTools.isFloatingPoint(pixelType),
				littleEndian);

		// Convert the data array back to a simple byte array, normalizing
		// floats and doubles if needed
		byte[] bytes = null;
		if (pix instanceof short[]) {
			bytes = DataTools.shortsToBytes((short[]) pix, littleEndian);
		}
		else if (pix instanceof int[]) {
			bytes = DataTools.intsToBytes((int[]) pix, littleEndian);
		}
		else if (pix instanceof long[]) {
			bytes = DataTools.longsToBytes((long[]) pix, littleEndian);
		}
		else if (pix instanceof float[]) {
			if (normalize) {
				pix = DataTools.normalizeFloats((float[]) pix);
			}
			bytes = DataTools.floatsToBytes((float[]) pix, littleEndian);
		}
		else if (pix instanceof double[]) {
			if (normalize) {
				pix = DataTools.normalizeDoubles((double[]) pix);
			}
			bytes = DataTools.doublesToBytes((double[]) pix, littleEndian);
		}
		else if (pix instanceof byte[]) {
			bytes = (byte[]) pix;
		}

		ImageMetadata meta = reader.getMetadata().get(imageIndex);
		try {
			// Open the potentially modified byte array as a buffered image and
			// add it to the list
			bImages.add(AWTImageTools.openImage(plane, bytes, reader, meta
				.getAxesLengthsPlanar(), imageIndex));
		}
		catch (FormatException e) {
			throw new CmdLineException(null, e.getMessage());
		}
		catch (IOException e) {
			throw new CmdLineException(null, e.getMessage());
		}

		if (bImages.get((int) planeNo) == null) {
			warn("\t************ Failed to read plane #" + planeNo + " ************");
		}
		if (reader.getMetadata().get(imageIndex).isIndexed() &&
			plane.getColorTable() == null)
		{
			warn("\t************ no LUT for plane #{}" + planeNo + " ************");
		}

		// check for pixel type mismatch
		int pixType = AWTImageTools.getPixelType(bImages.get((int) planeNo));
		if (pixType != pixelType && pixType != pixelType + 1) {
			info("\tPlane #" + planeNo + ": pixel type mismatch: " +
				FormatTools.getPixelTypeString(pixType) + "/" +
				FormatTools.getPixelTypeString(pixelType));
		}
		return plane;
	}

	// -- Helper methods --

	/**
	 * Opens all requested image planes as buffered images, and either converts
	 * them to {@link AsciiImage}s or opens the pixels into an {@link ImageViewer}
	 * .
	 * 
	 * @param reader Reader to use for opening pixels
	 */
	private void showPixels(Reader reader) throws CmdLineException {
		bImages = new ArrayList<BufferedImage>();

		read(reader);

		if (ascii) {
			// Print an ascii rendering of the image
			for (int i = 0; i < bImages.size(); i++) {
				final BufferedImage img = bImages.get(i);
				info("");
				info("Image #" + i + ":");
				info(new AsciiImage(img).toString());
			}
		}
		else {
			// display pixels in image viewer
			info("");
			info("Launching image viewer");
			ImageViewer viewer = new ImageViewer(getContext(), false);
			viewer.setImages(reader, bImages
				.toArray(new BufferedImage[bImages.size()]));
			viewer.setVisible(true);
		}
	}

	/**
	 * Helper method to map the input file to a specific {@link Location} if
	 * desired. Also performs preloading if requested.
	 */
	private void mapLocation() throws IOException {
		if (map != null) locationService.mapId(file, map);
		else if (preload) {
			RandomAccessInputStream f =
				new RandomAccessInputStream(getContext(), file);
			int len = (int) f.length();
			info("Caching " + len + " bytes:");
			byte[] b = new byte[len];
			int blockSize = 8 * 1024 * 1024; // 8 MB
			int read = 0, left = len;
			while (left > 0) {
				int r = f.read(b, read, blockSize < left ? blockSize : left);
				read += r;
				left -= r;
				float ratio = (float) read / len;
				int p = (int) (100 * ratio);
				info("\tRead " + read + " bytes (" + p + "% complete)");
			}
			f.close();
			ByteArrayHandle preloaded = new ByteArrayHandle(b);
			locationService.mapFile(file, preloaded);
		}
	}
}
