
package io.scif.formats;

import static org.junit.Assert.*;

import io.scif.ByteArrayPlane;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

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
	private static Reader reader;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		format.setContext(context);
		parser = (ScancoISQFormat.Parser) format.createParser();
		reader = format.createReader();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		context.dispose();
	}

	@Test
	public void testIsFormatFalseShortStream() throws Exception {
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			"CTDATA-HEADER_V".getBytes());

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormatIncorrectHeader() throws Exception {
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			"CTDATA-hEADER_V1".getBytes());

		assertFalse(checker.isFormat(stream));
	}

	@Test
	public void testIsFormat() throws Exception {
		// Add an extra byte to the end to check that it doesn't affect the result
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			"CTDATA-HEADER_V1a".getBytes());

		assertTrue(checker.isFormat(stream));
	}

	@Test
	public void testPopulateImageMetadata() throws Exception {
		final ScancoISQFormat.Metadata metadata = (ScancoISQFormat.Metadata) format
			.createMetadata();
		metadata.setWidth(15);
		metadata.setHeight(14);
		metadata.setSlices(13);
		metadata.populateImageMetadata();
		final ImageMetadata imgMeta = metadata.get(0);

		assertTrue(imgMeta.isLittleEndian());
		assertTrue(imgMeta.isOrderCertain());
		assertEquals(16, imgMeta.getBitsPerPixel());
		assertEquals(FormatTools.INT16, imgMeta.getPixelType());
		assertEquals(2, imgMeta.getPlanarAxisCount());
		assertEquals(3, imgMeta.getAxes().size());
		assertEquals(15, imgMeta.getAxisLength(0));
		assertEquals(14, imgMeta.getAxisLength(1));
		assertEquals(13, imgMeta.getAxisLength(2));
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
		final ByteBuffer buffer = ByteBuffer.allocate(
			ScancoISQFormat.Metadata.HEADER_BLOCK);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(28);
		buffer.putInt(patientIndex);
		buffer.putInt(scannerId);
		buffer.putLong(timestamp);
		buffer.putInt(width);
		buffer.putInt(height);
		buffer.putInt(depth);
		buffer.putInt(physicalWidth);
		buffer.putInt(physicalHeight);
		buffer.putInt(physicalDepth);
		buffer.putInt(sliceThickness);
		buffer.putInt(sliceIncrement);
		buffer.putInt(firstSlice);
		buffer.putInt(min);
		buffer.putInt(max);
		buffer.putInt(muScaling);
		buffer.putInt(samples);
		buffer.putInt(projections);
		buffer.putInt(scanDistance);
		buffer.putInt(scannerType);
		buffer.putInt(sampleTime);
		buffer.putInt(measurementIndex);
		buffer.putInt(site);
		buffer.putInt(referenceLine);
		buffer.putInt(algorithm);
		buffer.put(name.getBytes());
		buffer.putInt(energy);
		buffer.putInt(intensity);
		buffer.position(508);
		buffer.putInt(optionalBlocks);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		final SCIFIOConfig config = new SCIFIOConfig();
		final ScancoISQFormat.Metadata metadata = new ScancoISQFormat.Metadata();

		// EXERCISE
		parser.typedParse(stream, metadata, config);

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
	}

	@Test
	public void testOpenPlane() throws Exception {
		// SETUP
		final int width = 5;
		final int height = 5;
		final int depth = 5;
		final int planeBytes = width * height * 2;
		final int imageBytes = width * height * depth * 2;
		final long[] planeMin = { 0, 0 };
		final long[] planeMax = { width, height };
		final int planeIndex = 1;
		final int offset = planeIndex * planeBytes;
		final SCIFIOConfig config = new SCIFIOConfig();
		// Mock a .isq image input stream
		final ByteBuffer buffer = ByteBuffer.allocate(
			ScancoISQFormat.Metadata.HEADER_BLOCK + imageBytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(44);
		buffer.putInt(width);
		buffer.putInt(height);
		buffer.putInt(depth);
		buffer.position(ScancoISQFormat.Metadata.HEADER_BLOCK);
		// Add random image data
		final Random random = new Random(0xC0FFEE);
		final byte[] imageData = new byte[imageBytes];
		random.nextBytes(imageData);
		buffer.put(imageData);
		final RandomAccessInputStream stream = new RandomAccessInputStream(context,
			buffer.array());
		reader.setSource(stream);
		// Create an empty plane where the "image" is read
		final ByteArrayPlane emptyPlane = new ByteArrayPlane(context);
		emptyPlane.setData(new byte[planeBytes]);

		// EXERCISE
		// Read a plane from the image
		final Plane result = reader.openPlane(0, planeIndex, emptyPlane, planeMin,
			planeMax, config);

		// VERIFY
		final byte[] bytes = result.getBytes();
		for (int i = 0; i < planeBytes; i++) {
			assertEquals("Plane pixel data incorrect at index " + i, imageData[i +
				offset], bytes[i]);
		}
	}
}
