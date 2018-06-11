
package io.scif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;

public class AxisGuesserTest {

	/** Axis type for unclassified axes. */
	public static final int UNKNOWN_AXIS = 0;

	/** Axis type for focal planes. */
	public static final int Z_AXIS = 1;

	/** Axis type for time points. */
	public static final int T_AXIS = 2;

	/** Axis type for channels. */
	public static final int C_AXIS = 3;

	/** Axis type for series. */
	public static final int S_AXIS = 4;

	/** Method for testing pattern guessing logic. */

	@Test
	public void testAxisguessing() throws FormatException, IOException {
		final Context context = new Context();
		final SCIFIO scifio = new SCIFIO(context);
		final LogService log = scifio.log();
		final DataHandleService dataHandleService = context.getService(
			DataHandleService.class);
		final URL resource = this.getClass().getResource(
			"img/axisguesser/test_stack/img_000000000_Cy3_000.tif");
		final FileLocation file = new FileLocation(resource.getFile());

		final String pat = scifio.filePattern().findPattern(file);
		if (pat == null) log.info("No pattern found.");
		else {
			assertEquals("Wrong pattern:", "img_00000000<0-1>_Cy3_00<0-4>.tif", pat);
			final FilePattern fp = new FilePattern(file, pat, dataHandleService);
			assertTrue(fp.isValid());
			final Location id = fp.getFiles()[0];

			// read dimensional information from first file
			final Reader reader = scifio.initializer().initializeReader(id);
			final AxisType[] dimOrder = reader.getMetadata().get(0).getAxes().stream()
				.map(CalibratedAxis::type).toArray(AxisType[]::new);
			final long sizeZ = reader.getMetadata().get(0).getAxisLength(Axes.Z);
			final long sizeT = reader.getMetadata().get(0).getAxisLength(Axes.TIME);
			final long sizeC = reader.getMetadata().get(0).getAxisLength(
				Axes.CHANNEL);
			final boolean certain = reader.getMetadata().get(0).isOrderCertain();

			assertArrayEquals("Dimension Order", new AxisType[] { Axes.X, Axes.Y },
				dimOrder);
			assertFalse(certain);

			assertEquals(1, sizeZ);
			assertEquals(1, sizeT);
			assertEquals(1, sizeC);

			// guess axes
			final AxisGuesser ag = new AxisGuesser(fp, dimOrder, sizeZ, sizeT, sizeC,
				certain);

			// output results
			final String[] blocks = fp.getBlocks();
			final String[] prefixes = fp.getPrefixes();
			final AxisType[] axes = ag.getAxisTypes();
			final AxisType[] newOrder = ag.getAdjustedOrder();
			final boolean isCertain = ag.isCertain();
			assertFalse(isCertain);

			assertEquals("<0-1>", blocks[0]);
			assertEquals("img_00000000", prefixes[0]);
			assertEquals(Axes.Z, axes[0]);

			assertEquals("<0-4>", blocks[1]);
			assertEquals("_Cy3_00", prefixes[1]);
			assertEquals(Axes.TIME, axes[1]);

			assertArrayEquals(dimOrder, newOrder);
		}
	}

	@Test
	public void testAxisguessing2() throws FormatException, IOException {
		final Context context = new Context();
		final SCIFIO scifio = new SCIFIO(context);
		final LogService log = scifio.log();
		final DataHandleService dataHandleService = context.getService(
			DataHandleService.class);
		final URL resource = this.getClass().getResource(
			"img/axisguesser/leica_stack/leica_stack_Series014_z000_ch00.tif");
		final FileLocation file = new FileLocation(resource.getFile());

		final String pat = scifio.filePattern().findPattern(file);
		if (pat == null) log.info("No pattern found.");
		else {
			assertEquals("Wrong pattern:", "leica_stack_Series014_z00<0-2>_ch00.tif",
				pat);
			final FilePattern fp = new FilePattern(file, pat, dataHandleService);
			assertTrue(fp.isValid());
			final Location id = fp.getFiles()[0];

			// read dimensional information from first file
			final Reader reader = scifio.initializer().initializeReader(id);
			final AxisType[] dimOrder = reader.getMetadata().get(0).getAxes().stream()
				.map(CalibratedAxis::type).toArray(AxisType[]::new);
			final long sizeZ = reader.getMetadata().get(0).getAxisLength(Axes.Z);
			final long sizeT = reader.getMetadata().get(0).getAxisLength(Axes.TIME);
			final long sizeC = reader.getMetadata().get(0).getAxisLength(
				Axes.CHANNEL);
			final boolean certain = reader.getMetadata().get(0).isOrderCertain();

			assertArrayEquals("Dimension Order", new AxisType[] { Axes.X, Axes.Y },
				dimOrder);
			assertFalse(certain);

			assertEquals(1, sizeZ);
			assertEquals(1, sizeT);
			assertEquals(1, sizeC);

			// guess axes
			final AxisGuesser ag = new AxisGuesser(fp, dimOrder, sizeZ, sizeT, sizeC,
				certain);

			// output results
			final String[] blocks = fp.getBlocks();
			final String[] prefixes = fp.getPrefixes();
			final AxisType[] axes = ag.getAxisTypes();
			final AxisType[] newOrder = ag.getAdjustedOrder();
			final boolean isCertain = ag.isCertain();
			assertFalse(isCertain);

			assertEquals("<0-2>", blocks[0]);
			assertEquals("leica_stack_Series014_z00", prefixes[0]);
			assertEquals(Axes.Z, axes[0]);

			assertArrayEquals(dimOrder, newOrder);
		}
	}

}

// -- Notes --

// INPUTS: file pattern, dimOrder, sizeZ, sizeT, sizeC, isCertain
//
// 1) Fill in all "known" dimensional axes based on known patterns and
//    conventions
//      * known internal axes (ZCT) have isCertain == true
//      * known dimensional axes have a known pattern or convention
//    After that, we are left with only unknown slots, which we must guess.
//
// 2) First, we decide whether we really "believe" the reader. There is a
//    special case where we may decide that it got Z and T mixed up:
//      * if a Z block was found, but not a T block:
//          if !isOrderCertain, and sizeZ > 1, and sizeT == 1, swap 'em
//      * else if a T block was found, but not a Z block:
//          if !isOrderCertain and sizeT > 1, and sizeZ == 1, swap 'em
//    At this point, we can (have to) trust the internal ordering, and use it
//    to decide how to fill in the remaining dimensional blocks.
//
// 3) Set canBeZ to true iff no Z block is assigned and sizeZ == 1.
//    Set canBeT to true iff no T block is assigned and sizeT == 1.
//    Go through the blocks in order from left to right:
//      * If canBeZ, assign Z and set canBeZ to false.
//      * If canBeT, assign T and set canBeT to false.
//      * Otherwise, assign C.
//
// OUTPUTS: list of axis assignments, new dimOrder
