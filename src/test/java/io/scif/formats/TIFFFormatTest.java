/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2026 SCIFIO developers.
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

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.Location;

import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTestHelpers;

public class TIFFFormatTest {

	private static final int WIDTH = 2;

	private static final int HEIGHT = 2;

	private static final int PLANE_SIZE = WIDTH * HEIGHT;

	private Context context;

	private DataHandleService dataHandleService;

	@Before
	public void setUp() {
		context = new Context();
		dataHandleService = context.getService(DataHandleService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testImageJImagesMetadataSynthesizesIFDs() throws Exception {
		final byte[] description = ("ImageJ=1.53\n" +
			"images=2\n" +
			"channels=1\n" +
			"slices=1\n" +
			"frames=1\n\0").getBytes(StandardCharsets.US_ASCII);
		final int ifdEnd = 8 + 2 + 9 * 12 + 4;
		final int descriptionOffset = ifdEnd;
		final int pixelOffset = descriptionOffset + description.length;
		final int fileLength = pixelOffset + 2 * PLANE_SIZE;
		final DataHandle<Location> stream = FormatTestHelpers
			.createLittleEndianHandle(fileLength, dataHandleService, true);

		try {
			stream.seek(0);
			stream.writeByte('I');
			stream.writeByte('I');
			stream.writeShort(42);
			stream.writeInt(8);
			stream.seek(8);
			stream.writeShort(9);
			writeLongEntry(stream, 256, WIDTH);
			writeLongEntry(stream, 257, HEIGHT);
			writeShortEntry(stream, 258, 8);
			writeShortEntry(stream, 259, 1);
			writeShortEntry(stream, 262, 1);
			writeAsciiEntry(stream, 270, description.length, descriptionOffset);
			writeLongEntry(stream, 273, pixelOffset);
			writeLongEntry(stream, 278, HEIGHT);
			writeLongEntry(stream, 279, PLANE_SIZE);
			stream.writeInt(0);
			stream.seek(descriptionOffset);
			stream.write(description);
			stream.write(new byte[2 * PLANE_SIZE]);

			final TIFFFormat format = new TIFFFormat();
			format.setContext(context);
			final TIFFFormat.Parser parser = (TIFFFormat.Parser) format.createParser();
			final TIFFFormat.Metadata metadata = (TIFFFormat.Metadata) format
				.createMetadata();
			parser.parse(stream, metadata, new SCIFIOConfig(context));

			assertEquals(2, metadata.getIfds().size());
			assertEquals(2, metadata.get(0).getPlaneCount());
		}
		finally {
			stream.close();
		}
	}

	private void writeLongEntry(final DataHandle<Location> stream, final int tag,
		final int value) throws Exception
	{
		stream.writeShort(tag);
		stream.writeShort(4);
		stream.writeInt(1);
		stream.writeInt(value);
	}

	private void writeShortEntry(final DataHandle<Location> stream, final int tag,
		final int value) throws Exception
	{
		stream.writeShort(tag);
		stream.writeShort(3);
		stream.writeInt(1);
		stream.writeShort(value);
		stream.writeShort(0);
	}

	private void writeAsciiEntry(final DataHandle<Location> stream, final int tag,
		final int count, final int offset) throws Exception
	{
		stream.writeShort(tag);
		stream.writeShort(2);
		stream.writeInt(count);
		stream.writeInt(offset);
	}
}
