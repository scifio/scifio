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

package io.scif.formats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.services.InitializeService;
import io.scif.util.ImageHash;
import io.scif.util.MetaDataSerializer;
import io.scif.util.SampleFileService;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import org.scijava.Context;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

/**
 * Abstract superclass for format tests, provides image testing and sample file
 * retrieval methods.
 *
 * @author Gabriel Einsdorf, KNIME GmbH
 */
public class AbstractFormatTest {

	private FileLocation baseFolder;
	private final Location source;
	private final Context ctx = new Context();
	private final InitializeService init = ctx.getService(
		InitializeService.class);

	public AbstractFormatTest(final Location source) {
		this.source = source;
	}

	public FileLocation baseFolder() {
		if (baseFolder == null) {
			try {
				final SampleFileService sampleFileService = ctx.getService(
					SampleFileService.class);
				baseFolder = sampleFileService.prepareFormatTestFolder(source);
				assertTrue(ctx.getService(DataHandleService.class).exists(baseFolder));
			}
			catch (final IOException e) {
				fail("Could not initialize base folder: " + e);
			}
		}
		return baseFolder;
	}

	public void testImg(final Location imgLoc, final String hash,
		final int[] dims, final AxisType... axes)
	{
		testImg(imgLoc, hash, "", dims, axes);
	}

	public void testImg(final Location imgLoc, final String hash,
		final String metadataJson, final int[] dims, final AxisType... axes)
	{
		try {
			final ImgOpener opener = new ImgOpener(ctx);
			final SCIFIOImgPlus<?> img = opener.openImgs(imgLoc).get(0);

			assertEquals("Wrong number or dims: ", dims.length, img.numDimensions());
			for (int i = 0; i < dims.length; i++) {
				assertEquals("The dimensions of dim " + i + " are wrong!", dims[i], img
					.dimension(i));
			}
			assertEquals("missmatch between axes and dims:", dims.length,
				axes.length);
			for (int i = 0; i < axes.length; i++) {
				// NB: instances of Axes.unknown are all different but share the same
				// label
				if (Axes.UNKNOWN_LABEL.equals(axes[i].getLabel())) {
					assertEquals(Axes.UNKNOWN_LABEL, img.axis(i).type().getLabel());
					continue;
				}
				assertEquals("Axis missmatch at position " + i, axes[i], img.axis(i)
					.type());
			}
			assertEquals(hash, ImageHash.hashImg(img));
			if (!"".equals(metadataJson)) {
				Metadata metadata = init.initializeReader(imgLoc).getMetadata();
				assertEquals(metadataJson, MetaDataSerializer.metaToJson(metadata));
			}
		}
		catch (FormatException | IOException exc) {
			fail("error during image test " + exc);
		}
	}
}
