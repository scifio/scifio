/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.utests;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.img.axes.SCIFIOAxes;
import io.scif.util.FormatTools;

import java.io.IOException;

import net.imglib2.meta.Axes;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link io.scif.Metadata} interface methods.
 * 
 * @author Mark Hiner
 */
@Test(groups = "metadataTests")
public class MetadataTest {
	
	private final SCIFIO scifio = new SCIFIO();
	private final String id =
			"testImg&lengths=620,512,5&axes=X,Y,Time,Z,Channel.fake";
	private final String ndId =
			"ndImg&axes=X,Y,Z,Channel,Time,Lifetime,Spectra,&lengths=256,128,2,6,10,4,8.fake";

	/**
	 * Down the middle test that verifies each method of the Metadata API.
	 * @throws FormatException 
	 * @throws IOException 
	 */
	@Test
	public void testDownTheMiddle() throws IOException, FormatException {
		Metadata m = scifio.format().getFormat(id).createParser().parse(id);
		
		// Check getAxisType(int, int)
		assertEquals(m.getAxis(0, 0).type(), Axes.X);
		assertEquals(m.getAxis(0, 1).type(), Axes.Y);
		assertEquals(m.getAxis(0, 2).type(), Axes.TIME);
		assertEquals(m.getAxis(0, 3).type(), Axes.Z);
		assertEquals(m.getAxis(0, 4).type(), Axes.CHANNEL);
		
		// Check getAxisLength(int, int)
		assertEquals(m.getAxisLength(0, 0), 620);
		assertEquals(m.getAxisLength(0, 1), 512);
		assertEquals(m.getAxisLength(0, 2), 5);
		assertEquals(m.getAxisLength(0, 3), 1);
		assertEquals(m.getAxisLength(0, 4), 1);
		
		// Check getAxisLength(int, AxisType)
		assertEquals(m.getAxisLength(0, Axes.X), 620);
		assertEquals(m.getAxisLength(0, Axes.Y), 512);
		assertEquals(m.getAxisLength(0, Axes.TIME), 5);
		assertEquals(m.getAxisLength(0, Axes.Z), 1);
		assertEquals(m.getAxisLength(0, Axes.CHANNEL), 1);
		
		// Check getAxisIndex(int, AxisType)
		assertEquals(m.getAxisIndex(0, Axes.X), 0);
		assertEquals(m.getAxisIndex(0, Axes.Y), 1);
		assertEquals(m.getAxisIndex(0, Axes.TIME), 2);
		assertEquals(m.getAxisIndex(0, Axes.Z), 3);
		assertEquals(m.getAxisIndex(0, Axes.CHANNEL), 4);
	}
	
	/**
	 * Verify conditions when adding axes
	 * 
	 * @throws FormatException
	 */
	@Test
	public void testAddingAxes() throws FormatException {
		Metadata m = scifio.format().getFormat(id).createMetadata();
		m.createImageMetadata(1);
		
		// Verify that, after adding an axis to a clean metadata, the axis
		// length and type can be looked up properly
		m.setAxisLength(0, Axes.X, 100);
		assertEquals(m.getAxisLength(0, Axes.X), 100);
		assertEquals(m.getAxisIndex(0, Axes.X), 0);
	}
	
	/**
	 * Verify conditions when interrogating non-existant axes
	 * 
	 * @throws FormatException 
	 */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void testMissingAxes() throws FormatException {
		Metadata m = scifio.format().getFormat(id).createMetadata();
		
		// Axis index should be -1, length 0
		assertEquals(m.getAxisIndex(0, Axes.X), -1);
		assertEquals(m.getAxisLength(0, Axes.X), 0);
		
		// Should throw an IndexOutOfBoundsException
		assertEquals(m.getAxisLength(0, 0), 0);
	}
	
	/**
	 * Down the middle testing of constructing an N-D image.
	 */
	@Test
	public void testNDBasic() throws FormatException, IOException {
		Metadata m = scifio.initializer().parseMetadata(ndId);

		// Basic plane + axis length checks
		assertEquals(2 * 6 * 10 * 4 * 8, m.getPlaneCount(0));
		assertEquals(8, m.getAxisLength(0, SCIFIOAxes.SPECTRA));
		assertEquals(4, m.getAxisLength(0, SCIFIOAxes.LIFETIME));
		assertEquals(10, m.getAxisLength(0, Axes.TIME));
		assertEquals(6, m.getAxisLength(0, Axes.CHANNEL));
		assertEquals(2, m.getAxisLength(0, Axes.Z));
	}

	/**
	 * Check Plane Index lookups via
	 * {@link FormatTools#positionToRaster(long[], long[])} with an N-D dataset.
	 */
	@Test
	public void testNDPositions() throws FormatException, IOException {
		Metadata m = scifio.initializer().parseMetadata(ndId);

		// Plane index lookup checks
		long[] pos = { 1, 3, 5, 0, 0 };
		assertEquals(1 + (3 * 2) + (5 * 6 * 2), FormatTools.positionToRaster(m
			.getAxesLengthsNonPlanar(0), pos));

		pos = new long[] { 0, 0, 3, 3, 7 };
		assertEquals((3 * 6 * 2) + (3 * 10 * 6 * 2) + (7 * 4 * 10 * 6 * 2),
			FormatTools.positionToRaster(m.getAxesLengthsNonPlanar(0), pos));
	}

	/**
	 * Test that the plane count reflects updates to the planar axis count in
	 * an N-D dataset.
	 */
	@Test
	public void testNDPlaneCounts() throws FormatException, IOException {
		Metadata m = scifio.initializer().parseMetadata(ndId);

		// Try adjusting the planar axis count.
		m.setPlanarAxisCount(0, 3);
		assertEquals(6 * 10 * 4 * 8, m.getPlaneCount(0));
		m.setPlanarAxisCount(0, 4);
		assertEquals(10 * 4 * 8, m.getPlaneCount(0));
	}

	/**
	 * Test that axis-position-dependent flags (e.g. multichannel, interleaved)
	 * reflect updates to axis positions with an N-D dataset.
	 */
	@Test
	public void testNDFlags() throws FormatException, IOException {
		Metadata m = scifio.initializer().parseMetadata(ndId);
		// Check multichannel. C index < planar axis count, so should be false
		assertFalse(m.isMultichannel(0));
		// Check the interleaved flag
		// XY...C.. so not interleaved
		assertFalse(m.isInterleaved(0));
		m.setPlanarAxisCount(0, 4);
		// Now multichannel
		assertTrue(m.isMultichannel(0));
		// But still XY...C
		assertFalse(m.isInterleaved(0));
		m.setAxisType(0, 0, Axes.CHANNEL);
		// Now we're CXY, so interleaved
		assertEquals(1, m.getAxisIndex(0, Axes.X));
		assertEquals(2, m.getAxisIndex(0, Axes.Y));
		assertTrue(m.isInterleaved(0));
	}
}
