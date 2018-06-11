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

import static io.scif.formats.StratecPQCTFormat.DEVICE_NAME_INDEX;
import static io.scif.formats.StratecPQCTFormat.HEADER_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.scif.ByteArrayPlane;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.StratecPQCTFormat.Checker;
import io.scif.formats.StratecPQCTFormat.Metadata;
import io.scif.formats.StratecPQCTFormat.Parser;
import io.scif.formats.StratecPQCTFormat.Reader;
import io.scif.io.ByteArrayHandle;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests for {@link StratecPQCTFormat}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class StratecPQCTFormatTest {

	private static final String validName = Paths.get("/home/path/I1234567.m02")
		.toString();
	private static final String validDevice = "device.typ";
	private static final StratecPQCTFormat format = new StratecPQCTFormat();
	private static final Context context = new Context();
	private static final Checker checker = new Checker();
	private static final Parser parser = new Parser();

	@BeforeClass
	public static void oneTimeSetup() {
		format.setContext(context);
	}

	@AfterClass
	public static void oneTimeTearDown() {
		context.dispose();
	}

	@Test
	public void testMakeSuffixArray() throws Exception {
		final String[] suffixes = format.makeSuffixArray();

		for (int i = 0; i < 256; i++) {
			assertEquals("Malformed suffix", 0, suffixes[i].indexOf('m'));
			final int hexValue = Integer.parseInt(suffixes[i].substring(1), 16);
			assertEquals("Missing suffix", i, hexValue);
		}
	}

	@Test
	public void testIsFormatFalseNoDevice() throws Exception {
		// Check that method doesn't crash with NPE even if there's no device data
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		final ByteArrayHandle handle = new ByteArrayHandle(buffer);
		buffer.position(DEVICE_NAME_INDEX);
		buffer.put((byte) 1);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			handle, validName);

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatFalseShortStream() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE - 1);
		buffer.position(DEVICE_NAME_INDEX);
		buffer.put((byte) validDevice.length());
		buffer.put(validDevice.getBytes());
		final ByteArrayHandle handle = new ByteArrayHandle(buffer);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			handle, validName);

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatFalseBadDevice() throws Exception {
		final String device = "device.tyq";
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.position(DEVICE_NAME_INDEX);
		buffer.put((byte) device.length());
		buffer.put(device.getBytes());
		final ByteArrayHandle handle = new ByteArrayHandle(buffer);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			handle, validName);

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatFalseBadFilename() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.position(DEVICE_NAME_INDEX);
		buffer.put((byte) validDevice.length());
		buffer.put(validDevice.getBytes());
		final ByteArrayHandle handle = new ByteArrayHandle(buffer);
		final String badFileName = Paths.get("/home/path/I123456G.m02").toString();
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			handle, badFileName);

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormat() throws Exception {
		final String device = "DevIce.TyP"; // Check that case doesn't matter
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.position(DEVICE_NAME_INDEX);
		buffer.put((byte) device.length());
		buffer.put(device.getBytes());
		final ByteArrayHandle handle = new ByteArrayHandle(buffer);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			handle, validName);

		assertTrue(checker.isFormat(stream));
	}

	@Test
	public void testSetDateBadInt() throws Exception {
		final Metadata metadata = (Metadata) format.createMetadata();

		metadata.setPatientBirthDate(0);

		assertEquals(Metadata.DEFAULT_DATE, metadata.getPatientBirthDate());
	}

	@Test
	public void testMeasurementInfoEmptyIfException() throws Exception {
		final Metadata metadata = (Metadata) format.createMetadata();
		final RandomAccessInputStream stream = new RandomAccessInputStream(
			new Context(), new byte[0]);

		metadata.setMeasurementInfo(stream);

		assertTrue(metadata.getMeasurementInfo().isEmpty());
	}

	@Test
	public void testPopulateImageMetadata() throws Exception {
		// SETUP
		final Metadata metadata = (Metadata) format.createMetadata();
		final short width = 10;
		metadata.setWidth(width);
		final short height = 15;
		metadata.setHeight(height);
		final short slices = 5;
		metadata.setSlices(slices);
		final double resolution = 0.5;
		metadata.setResolution(resolution);
		final double distance = 0.1;
		metadata.setSliceDistance(distance);
		final CalibratedAxis[] expectedAxes = new CalibratedAxis[] {
			new DefaultLinearAxis(Axes.X, Metadata.UNIT, resolution),
			new DefaultLinearAxis(Axes.Y, Metadata.UNIT, resolution) };

		// EXERCISE
		metadata.populateImageMetadata();
		final ImageMetadata imgMeta = metadata.get(0);
		final List<CalibratedAxis> axes = imgMeta.getAxes();

		// VERIFY
		assertTrue(imgMeta.isLittleEndian());
		assertTrue(imgMeta.isOrderCertain());
		assertEquals(16, imgMeta.getBitsPerPixel());
		assertEquals(FormatTools.INT16, imgMeta.getPixelType());
		assertEquals(2, imgMeta.getPlanarAxisCount());
		assertEquals(width, imgMeta.getAxisLength(Axes.X));
		assertEquals(height, imgMeta.getAxisLength(Axes.Y));
		assertEquals(expectedAxes.length, axes.size());
		for (int i = 0; i < expectedAxes.length; i++) {
			final CalibratedAxis expected = expectedAxes[i];
			final CalibratedAxis axis = axes.get(i);
			assertEquals(expected.type(), axis.type());
			assertEquals(expected.unit(), axis.unit());
			assertEquals(expected.averageScale(0, 1), axis.averageScale(0, 1), 1e-12);
		}
	}

	@Test
	public void testSlicesAtLeastOne() throws Exception {
		final Metadata metadata = (Metadata) format.createMetadata();
		metadata.setSlices((short) 0);
		assertEquals(1, metadata.getSlices());
	}

	@Test
	public void testTypedParse() throws Exception {
		// SETUP
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final double resolution = 0.1234;
		final double objectSize = 31337;
		final List<String> measurementInfo = Arrays.asList("CT-X", "", "Additional",
			"Info");
		final String expectedInfo = measurementInfo.stream().filter(s -> !s
			.isEmpty()).reduce("", (s, t) -> s + t + "\n");
		final int measurementDate = 20160125;
		final short measurementNumber = 45;
		final int patientNumber = 536345;
		final int patientBirthDate = 19450508;
		final int patientAge = 43;
		final String patientName = "Max Mustermann";
		final String patientId = "MAXM1234";
		final short leftEdge = 5;
		final short topEdge = 6;
		final short width = 250;
		final short height = 200;
		final short slices = 3;
		final double sliceStart = 0.12345;
		final double sliceDistance = 0.6789;
		buffer.putDouble(12, resolution);
		buffer.putShort(30, slices);
		buffer.putDouble(32, sliceStart);
		buffer.putDouble(40, sliceDistance);
		buffer.putDouble(318, objectSize);
		for (int i = 0; i < 4; i++) {
			putShortString(Metadata.INFO_INDICES[i], buffer, measurementInfo.get(i));
		}
		buffer.putInt(986, measurementDate);
		putShortString(DEVICE_NAME_INDEX, buffer, validDevice);
		buffer.putShort(1085, measurementNumber);
		buffer.putInt(1087, patientNumber);
		buffer.putInt(1091, patientBirthDate);
		buffer.putInt(1095, patientAge);
		putShortString(1099, buffer, patientName);
		putShortString(1282, buffer, patientId);
		buffer.putShort(1525, leftEdge);
		buffer.putShort(1527, topEdge);
		buffer.putShort(1529, width);
		buffer.putShort(1531, height);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		final Metadata metadata = new Metadata();
		final SCIFIOConfig config = new SCIFIOConfig();

		// EXERCISE
		parser.typedParse(stream, metadata, config);

		// VERIFY
		assertEquals(resolution, metadata.getResolution(), 1e-12);
		assertEquals(objectSize, metadata.getObjectSize(), 1e-12);
		assertEquals(expectedInfo, metadata.getMeasurementInfo());
		assertEquals(0, metadata.getMeasurementDate().compareTo(
			new GregorianCalendar(2016, 0, 25).getTime()));
		assertEquals(validDevice, metadata.getDeviceName());
		assertEquals(measurementNumber, metadata.getPatientMeasurementNumber());
		assertEquals(patientNumber, metadata.getPatientNumber());
		assertEquals(0, metadata.getPatientBirthDate().compareTo(
			new GregorianCalendar(1945, 4, 8).getTime()));
		assertEquals(patientAge, metadata.getPatientAge());
		assertEquals(patientName, metadata.getPatientName());
		assertEquals(patientId, metadata.getPatientId());
		assertEquals(leftEdge, metadata.getLeftEdge());
		assertEquals(topEdge, metadata.getTopEdge());
		assertEquals(width, metadata.getWidth());
		assertEquals(height, metadata.getHeight());
		assertEquals(slices, metadata.getSlices());
		assertEquals(sliceStart, metadata.getSliceStart(), 1e-12);
		assertEquals(sliceDistance, metadata.getSliceDistance(), 1e-12);
	}

	private void putShortString(final int position, final ByteBuffer buffer,
		final String string)
	{
		buffer.position(position);
		buffer.put((byte) string.length());
		buffer.put(string.getBytes());
	}

	@Test
	public void testOpenPlane() throws Exception {
		final short width = 10;
		final short height = 10;
		final int planeBytes = width * height * 2;
		final ByteArrayPlane plane = new ByteArrayPlane(context);
		plane.setData(new byte[planeBytes]);
		final ByteBuffer buffer = ByteBuffer.allocate(
			StratecPQCTFormat.HEADER_SIZE + planeBytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(1529);
		buffer.putShort(width);
		buffer.putShort(height);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		final Reader reader = (Reader) format.createReader();
		reader.setSource(stream);

		// EXECUTE
		final Interval bounds = new FinalInterval(width, height);
		reader.openPlane(0, 0, plane, bounds, new SCIFIOConfig());

		// VERIFY
		assertEquals(
			"Position of stream incorrect: should point to the end of the stream",
			StratecPQCTFormat.HEADER_SIZE + planeBytes, stream.getFilePointer());
	}
}
