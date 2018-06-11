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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.scif.ByteArrayPlane;
import io.scif.ImageMetadata;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.util.FormatTestHelpers;
import io.scif.util.FormatTools;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link ScancoISQFormat} and its subclasses
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class ScancoISQFormatTest {

	private static final Context context = new Context();
	private static final ScancoISQFormat.Checker checker =
		new ScancoISQFormat.Checker();
	private static final ScancoISQFormat format = new ScancoISQFormat();
	private static ScancoISQFormat.Parser parser;
	private static DataHandleService dataHandleService;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		format.setContext(context);
		parser = (ScancoISQFormat.Parser) format.createParser();
		dataHandleService = context.getService(DataHandleService.class);
	}

	@AfterClass
	public static void oneTimeTearDown() {
		context.dispose();
	}

	@Test
	public void testIsFormatFalseShortStream() throws Exception {
		final DataHandle<Location> stream = dataHandleService.create(
			new BytesLocation("CTDATA-HEADER_V".getBytes()));

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatIncorrectHeader() throws Exception {
		final DataHandle<Location> stream = dataHandleService.create(
			new BytesLocation("CTDATA-hEADER_V1".getBytes()));

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormat() throws Exception {
		// Add an extra byte to the end to check that it doesn't affect the result

		final DataHandle<Location> stream = dataHandleService.create(
			new BytesLocation("CTDATA-HEADER_V1a".getBytes()));

		assertTrue(checker.isFormat(stream));
	}

	@Test
	public void testPopulateImageMetadata() throws Exception {
		// SETUP
		final ScancoISQFormat.Metadata metadata = (ScancoISQFormat.Metadata) format
			.createMetadata();
		final int[] dimensions = { 15, 14, 13 };
		final int[] physicalDimensions = { 45, 35, 20 };
		final double[] voxelDimensions = IntStream.range(0, 3).mapToDouble(
			i -> 1.0 * physicalDimensions[i] / dimensions[i]).toArray();
		metadata.setPhysicalWidth(physicalDimensions[0]);
		metadata.setPhysicalHeight(physicalDimensions[1]);
		metadata.setPhysicalDepth(physicalDimensions[2]);
		metadata.setWidth(dimensions[0]);
		metadata.setHeight(dimensions[1]);
		metadata.setSlices(dimensions[2]);
		metadata.populateImageMetadata();

		// EXECUTE
		final ImageMetadata imgMeta = metadata.get(0);

		// VERIFY
		assertTrue(imgMeta.isLittleEndian());
		assertTrue(imgMeta.isOrderCertain());
		assertEquals(16, imgMeta.getBitsPerPixel());
		assertEquals(FormatTools.INT16, imgMeta.getPixelType());
		assertEquals(2, imgMeta.getPlanarAxisCount());
		final List<CalibratedAxis> axes = imgMeta.getAxes();
		assertEquals(3, axes.size());
		for (int i = 0; i < 3; i++) {
			assertEquals(dimensions[i], imgMeta.getAxisLength(i));
			assertEquals(voxelDimensions[i], axes.get(i).averageScale(0, 1), 1e-12);
		}
	}

	@Test
	public void testTypedParse() throws Exception {
		// SETUP
		final int patientIndex = 5;
		final int scannerId = 345;
		final long timestamp = 0x008c_9567_4beb_4000L; // Apr 9 1984
		final int width = 12;
		final int height = 13;
		final int depth = 14;
		final int physicalWidth = 24;
		final int physicalHeight = 26;
		final int physicalDepth = 28;
		final int sliceThickness = 2;
		final int sliceIncrement = 1;
		final int firstSlice = 11;
		final int min = -1337;
		final int max = 1337;
		final int muScaling = 4096;
		final int samples = 100;
		final int projections = 111;
		final int scanDistance = 40;
		final int scannerType = 4;
		final int sampleTime = 1_000_000;
		final int measurementIndex = 1;
		final int site = 4;
		final int referenceLine = 15;
		final int algorithm = 6;
		final String name = "Johann Gambolputty de von Ausfern-schple";
		final int energy = 70_000;
		final int intensity = 200;
		final int optionalBlocks = 0;

		final DataHandle<Location> handle = FormatTestHelpers
			.createLittleEndianHandle(ScancoISQFormat.Metadata.HEADER_BLOCK,
				dataHandleService, true);

		handle.seek(28);
		handle.writeInt(patientIndex);
		handle.writeInt(scannerId);
		handle.writeLong(timestamp);
		handle.writeInt(width);
		handle.writeInt(height);
		handle.writeInt(depth);
		handle.writeInt(physicalWidth);
		handle.writeInt(physicalHeight);
		handle.writeInt(physicalDepth);
		handle.writeInt(sliceThickness);
		handle.writeInt(sliceIncrement);
		handle.writeInt(firstSlice);
		handle.writeInt(min);
		handle.writeInt(max);
		handle.writeInt(muScaling);
		handle.writeInt(samples);
		handle.writeInt(projections);
		handle.writeInt(scanDistance);
		handle.writeInt(scannerType);
		handle.writeInt(sampleTime);
		handle.writeInt(measurementIndex);
		handle.writeInt(site);
		handle.writeInt(referenceLine);
		handle.writeInt(algorithm);
		handle.write(name.getBytes());
		handle.writeInt(energy);
		handle.writeInt(intensity);
		handle.seek(508);
		handle.writeInt(optionalBlocks);

		final SCIFIOConfig config = new SCIFIOConfig();
		final ScancoISQFormat.Metadata metadata = new ScancoISQFormat.Metadata();

		// EXERCISE
		parser.typedParse(handle, metadata, config);
		metadata.populateImageMetadata();

		// VERIFY
		assertEquals(patientIndex, metadata.getPatientIndex());
		assertEquals(scannerId, metadata.getScannerId());
		assertTrue(LocalDate.of(1984, 4, 9).isEqual(metadata.getCreationDate()));
		assertEquals(width, metadata.getWidth());
		assertEquals(height, metadata.getHeight());
		assertEquals(depth, metadata.getSlices());
		assertEquals(physicalWidth, metadata.getPhysicalWidth());
		assertEquals(physicalHeight, metadata.getPhysicalHeight());
		assertEquals(physicalDepth, metadata.getPhysicalDepth());
		assertEquals(sliceThickness, metadata.getSliceThickness());
		assertEquals(sliceIncrement, metadata.getSliceSpacing());
		assertEquals(firstSlice, metadata.getFirstSlicePosition());
		assertEquals(min, metadata.getMinDataValue());
		assertEquals(max, metadata.getMaxDataValue());
		assertEquals(muScaling, metadata.getMuScaling());
		assertEquals(samples, metadata.getSamples());
		assertEquals(projections, metadata.getProjections());
		assertEquals(scanDistance, metadata.getScanDistance());
		assertEquals(scannerType, metadata.getScannerType());
		assertEquals(sampleTime, metadata.getSamplingTime());
		assertEquals(measurementIndex, metadata.getMeasurementIndex());
		assertEquals(site, metadata.getSite());
		assertEquals(referenceLine, metadata.getReferenceLine());
		assertEquals(algorithm, metadata.getReconstructionAlgorithm());
		assertEquals(name, metadata.getPatientName());
		assertEquals(energy, metadata.getEnergy());
		assertEquals(intensity, metadata.getIntensity());
		assertEquals(ScancoISQFormat.Metadata.HEADER_BLOCK, metadata
			.getDataOffset());
		assertEquals(2 * width * height, metadata.getSliceBytes());
	}

	@Test
	public void testOpenPlane() throws Exception {
		// SETUP
		final int width = 10;
		final int height = 10;
		final int depth = 3;
		final int planeBytes = width * height * 2;
		final int imageBytes = depth * planeBytes;
		final ByteArrayPlane plane = new ByteArrayPlane(context);
		plane.setData(new byte[planeBytes]);

		final DataHandle<Location> handle = FormatTestHelpers
			.createLittleEndianHandle(ScancoISQFormat.Metadata.HEADER_BLOCK +
				imageBytes, dataHandleService, true);
		handle.seek(44);
		handle.writeInt(width);
		handle.writeInt(height);
		handle.writeInt(depth);
		final Reader reader = format.createReader();
		reader.setSource(handle);

		// EXECUTE
		final Interval bounds = new FinalInterval(width, height, depth);
		reader.openPlane(0, 1, plane, bounds, new SCIFIOConfig());

		// VERIFY
		assertEquals(
			"Position of stream incorrect: should point to the beginning of the 3rd slice",
			ScancoISQFormat.Metadata.HEADER_BLOCK + 2 * planeBytes, handle.offset());
	}
}
