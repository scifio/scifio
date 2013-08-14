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

import static org.testng.AssertJUnit.assertTrue;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;

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
			"testImg&sizeX=620&sizeY=512&sizeT=5&dimOrder=XYTZC.fake";	

	/**
	 * Down the middle test that verifies each method of the Metadata API.
	 * @throws FormatException 
	 * @throws IOException 
	 */
	@Test
	public void testDownTheMiddle() throws IOException, FormatException {
		Metadata m = scifio.format().getFormat(id).createParser().parse(id);
		
		// Check getAxisType(int, int)
		assertTrue(m.getAxisType(0, 0).type().equals(Axes.X));
		assertTrue(m.getAxisType(0, 1).type().equals(Axes.Y));
		assertTrue(m.getAxisType(0, 2).type().equals(Axes.TIME));
		assertTrue(m.getAxisType(0, 3).type().equals(Axes.Z));
		assertTrue(m.getAxisType(0, 4).type().equals(Axes.CHANNEL));
		
		// Check getAxisLength(int, int)
		assertTrue(m.getAxisLength(0, 0) == 620);
		assertTrue(m.getAxisLength(0, 1) == 512);
		assertTrue(m.getAxisLength(0, 2) == 5);
		assertTrue(m.getAxisLength(0, 3) == 1);
		assertTrue(m.getAxisLength(0, 4) == 1);
		
		// Check getAxisLength(int, AxisType)
		assertTrue(m.getAxisLength(0, Axes.X) == 620);
		assertTrue(m.getAxisLength(0, Axes.Y) == 512);
		assertTrue(m.getAxisLength(0, Axes.TIME) == 5);
		assertTrue(m.getAxisLength(0, Axes.Z) == 1);
		assertTrue(m.getAxisLength(0, Axes.CHANNEL) == 1);
		
		// Check getAxisIndex(int, AxisType)
		assertTrue(m.getAxisIndex(0, Axes.X) == 0);
		assertTrue(m.getAxisIndex(0, Axes.Y) == 1);
		assertTrue(m.getAxisIndex(0, Axes.TIME) == 2);
		assertTrue(m.getAxisIndex(0, Axes.Z) == 3);
		assertTrue(m.getAxisIndex(0, Axes.CHANNEL) == 4);
	}
}
