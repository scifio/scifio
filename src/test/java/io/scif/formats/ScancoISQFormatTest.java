
package io.scif.formats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import net.imagej.axis.CalibratedAxis;

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

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		format.setContext(context);
		parser = (ScancoISQFormat.Parser) format.createParser();
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
}
