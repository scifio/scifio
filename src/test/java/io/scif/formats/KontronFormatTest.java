/*-
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

package io.scif.formats;

import static io.scif.formats.KontronFormat.HEADER_BYTES;
import static io.scif.formats.KontronFormat.KONTRON_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.scif.ByteArrayPlane;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link KontronFormat} and its subclasses
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class KontronFormatTest {

	private static final Context context = new Context();
	private static final KontronFormat format = new KontronFormat();;
	private static KontronFormat.Reader reader;
	private static KontronFormat.Parser parser;
	private static final KontronFormat.Checker checker =
		new KontronFormat.Checker();

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		format.setContext(context);
		reader = (KontronFormat.Reader) format.createReader();
		parser = (KontronFormat.Parser) format.createParser();
	}

	@Before
	public void setUp() throws Exception {}

	@AfterClass
	public static void oneTimeTearDown() {
		context.dispose();
	}

	/**
	 * Test isFormat returns false when stream is too short to match the id
	 * sequence
	 */
	@Test
	public void testIsFormatFalseShortStream() throws Exception {
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			new byte[] { 0x1, 0x0, 0x47 });

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatFalseIncorrectBytes() throws Exception {
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			new byte[] { 0x1, 0x0, 0x47, 0x12, 0x6D, (byte) 0xA0 });

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormat() throws Exception {
		// Add an extra byte to the end to check that it doesn't affect the result
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			new byte[] { 0x1, 0x0, 0x47, 0x12, 0x6D, (byte) 0xB0, 0x13 });

		assertTrue(checker.isFormat(stream));
	}

	@Test
	public void testPopulateImageMetadata() throws Exception {
		final Metadata metadata = format.createMetadata();
		metadata.populateImageMetadata();
		final ImageMetadata imgMeta = metadata.get(0);

		assertTrue(imgMeta.isLittleEndian());
		assertTrue(imgMeta.isOrderCertain());
		assertEquals(8, imgMeta.getBitsPerPixel());
		assertEquals(FormatTools.UINT8, imgMeta.getPixelType());
		assertEquals(2, imgMeta.getPlanarAxisCount());
	}

	@Test
	public void testTypedParse() throws Exception {
		// SETUP
		final int width = 15;
		final int height = 10;
		final SCIFIOConfig config = new SCIFIOConfig();
		final KontronFormat.Metadata kontronMeta = new KontronFormat.Metadata();

		// Create a mock input stream with a Kontron header
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES).order(
			ByteOrder.LITTLE_ENDIAN);
		// Mock a Kontron header
		buffer.put(KontronFormat.KONTRON_ID);
		buffer.putShort((short) width);
		buffer.putShort((short) height);
		buffer.position(HEADER_BYTES);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		reader.setSource(stream);

		// EXERCISE
		parser.typedParse(stream, kontronMeta, config);

		// VERIFY
		assertEquals(width, kontronMeta.getWidth());
		assertEquals(height, kontronMeta.getHeight());
	}

	@Test
	public void testOpenPlane() throws Exception {
		// SETUP
		final short width = 10;
		final short height = 10;
		final int planeBytes = width * height * 2;
		final Interval bounds = new FinalInterval(width, height);
		final ByteArrayPlane plane = new ByteArrayPlane(context);
		plane.setData(new byte[planeBytes]);
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + planeBytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(KONTRON_ID.length);
		buffer.putShort(width).putShort(height);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		final Reader reader = format.createReader();
		reader.setSource(stream);

		// EXECUTE
		reader.openPlane(0, 0, plane, bounds, new SCIFIOConfig());

		// VERIFY
		assertEquals(
			"Position of stream incorrect: should point to the end of the file",
			stream.length(), stream.getFilePointer());
	}
}
