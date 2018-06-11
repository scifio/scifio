/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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

package io.scif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.scif.formats.FakeFormat;

import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.display.ColorTable;

import org.junit.Test;

/**
 * Unit tests for {@link FakeFormat}. The FakeFormat is used in many other
 * tests, so it is imperative to ensure the format itself is functioning as
 * intended.
 *
 * @author Mark Hiner
 */
public class FakeFormatTest {

	// -- Fields --

	private final SCIFIO scifio = new SCIFIO();

	// -- Indexed color tests --

	/**
	 * Test that the correct number of {@link ColorTable}s are created when
	 * {@link Axes#CHANNEL} is a planar axis of length 1.
	 */
	@Test
	public void testIndexedPlanarChannel() throws FormatException, IOException {
		final String sampleImage =
			"8bit-unsigned&pixelType=uint8&indexed=true&planarDims=3&lengths=50,50,1&axes=X,Y,Channel.fake";

		final Reader reader = scifio.initializer().initializeReader(sampleImage);
		final FakeFormat.Metadata fMeta = (FakeFormat.Metadata) reader
			.getMetadata();
		assertEquals(1, fMeta.getLuts().length);
		assertEquals(1, fMeta.getLuts()[0].length);
		assertNotNull(reader.openPlane(0, 0).getColorTable());
	}

	/**
	 * Test that the correct number of {@link ColorTable}s are created when
	 * {@link Axes#CHANNEL} is a planar axis of length >1.
	 */
	@Test
	public void testIndexedManyPlanarChannels() throws FormatException,
		IOException
	{
		final String sampleImage =
			"8bit-unsigned&pixelType=uint8&indexed=true&planarDims=3&lengths=50,50,4&axes=X,Y,Channel.fake";

		final Reader reader = scifio.initializer().initializeReader(sampleImage);
		final FakeFormat.Metadata fMeta = (FakeFormat.Metadata) reader
			.getMetadata();
		assertEquals(1, fMeta.getLuts().length);
		assertEquals(1, fMeta.getLuts()[0].length);
		assertNotNull(reader.openPlane(0, 0).getColorTable());
	}

	/**
	 * Test that the correct number of {@link ColorTable}s are created when
	 * {@link Axes#CHANNEL} is a planar axis, and there are multiple planes in the
	 * dataset.
	 */
	@Test
	public void testIndexedPlanarChannelManyPlanes() throws FormatException,
		IOException
	{
		final String sampleImage =
			"8bit-unsigned&pixelType=uint8&indexed=true&planarDims=3&lengths=50,50,4,6&axes=X,Y,Channel,Time.fake";

		final Reader reader = scifio.initializer().initializeReader(sampleImage);
		final FakeFormat.Metadata fMeta = (FakeFormat.Metadata) reader
			.getMetadata();
		assertEquals(1, fMeta.getLuts().length);
		assertEquals(6, fMeta.getLuts()[0].length);
		for (int i = 0; i < fMeta.get(0).getPlaneCount(); i++) {
			assertNotNull(reader.openPlane(0, i).getColorTable());
		}
	}

	/**
	 * Test that the correct number of {@link ColorTable}s are created when
	 * {@link Axes#CHANNEL} is a non-planar axis.
	 */
	@Test
	public void testIndexedNonPlanarChannel() throws FormatException,
		IOException
	{
		final String sampleImage =
			"8bit-unsigned&pixelType=uint8&indexed=true&planarDims=2&lengths=50,50,4,6&axes=X,Y,Channel,Time.fake";

		final Reader reader = scifio.initializer().initializeReader(sampleImage);
		final FakeFormat.Metadata fMeta = (FakeFormat.Metadata) reader
			.getMetadata();
		assertEquals(1, fMeta.getLuts().length);
		assertEquals(24, fMeta.getLuts()[0].length);
		for (int i = 0; i < fMeta.get(0).getPlaneCount(); i++) {
			assertNotNull(reader.openPlane(0, i).getColorTable());
		}
	}

	/**
	 * Test that fake images with more axes than lengths can not be constructed.
	 */
	@Test(expected = IllegalStateException.class)
	public void testMisMatchedAxes() throws IOException, FormatException {
		final String moreAxes =
			"8bit-unsigned&pixelType=uint8lengths=50,50,4&axes=X,Y,Channel,Z,Time.fake";

		scifio.initializer().parseMetadata(moreAxes);
	}

	/**
	 * Test that fake images with more lengths than axes can not be constructed.
	 */
	@Test(expected = IllegalStateException.class)
	public void testMisMatchedLengths() throws FormatException, IOException {
		final String moreLengths =
			"8bit-unsigned&pixelType=uint8lengths=50,50,4,7,12&axes=X,Y,Channel.fake";

		scifio.initializer().parseMetadata(moreLengths);
	}
}
