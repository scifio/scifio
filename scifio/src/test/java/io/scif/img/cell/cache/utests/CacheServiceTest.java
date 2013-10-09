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

package io.scif.img.cell.cache.utests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import io.scif.FormatException;
import io.scif.SCIFIO;
import io.scif.filters.ReaderFilter;
import io.scif.img.cell.SCIFIOCell;
import io.scif.img.cell.SCIFIOCellCache;
import io.scif.img.cell.cache.CacheResult;
import io.scif.img.cell.cache.CacheService;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.SCIFIOArrayLoader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for testing the {@link CacheService). Tests
 * storage and retrieval, the various configuration options,
 * and edge cases.
 * 
 * @author Mark Hiner
 *
 */
@Test(groups = "cacheTests")
public class CacheServiceTest {

	// -- Constants --

	// max time, in ms, to wait for cache
	private static final long TIMEOUT = 15000l;

	// -- Fields --

	private static SCIFIO scifio = new SCIFIO();

	@SuppressWarnings("unchecked")
	private static CacheService<SCIFIOCell<?>> cs = scifio.getContext()
		.getService(CacheService.class);

	// -- Post-test hooks --

	@AfterMethod
	public void tearDown() {
		// clear all cached entries
		cs.clearAllCaches();
		// reset any configuration options in case tests failed
		cs.setMaxBytesOnDisk(Long.MAX_VALUE);
		cs.enable(true);
		cs.cacheAll(false);
	}

	// -- Tests --

	// Tests points along a simple workflow of loading and caching a cell
	@Test
	public void testBasicCacheAndRetrieve() throws FormatException, IOException {
		// create a cache. This implicitly makes it available to the CacheService
		final SCIFIOCellCache<ByteArray> cache1 = makeCache(128l * 128l);

		// load the first cell in that cache
		final SCIFIOCell<ByteArray> cell =
			cache1.load(0, new int[] { 128, 128 }, new long[] { 0l, 0l });

		// Cell is unmodified so this shouldn't cache
		assertEquals(CacheResult.NOT_DIRTY, cs.cache(cache1.toString(), 0, cell));

		// Verify the cell wasn't cached
		assertNull(cs.retrieve(cache1.toString(), 0));

		// Dirty the cell
		cell.getData().setValue(130, (byte) 0xace);

		// Cache the dirtied cell
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell));

		// Try loading the wrong cell position
		assertNull(cs.retrieve(cache1.toString(), 1));

		// Make a second cache
		final SCIFIOCellCache<ByteArray> cache2 = makeCache(128l * 128l);

		// Verify that our cell is not accessible by the new cache
		assertNull(cs.retrieve(cache2.toString(), 0));

		// Verify the original cell can be retrieved properly
		assertEquals(cell, cs.retrieve(cache1.toString(), 0));

		// Dirty the cell again
		cell.getData().setValue(130, (byte) 0xace);

		// Cache the dirtied cell again
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell));

		// Clear cache 1
		cs.clearCache(cache1.toString());

		// Verify our cell was cleared from the cache service
		assertNull(cs.retrieve(cache1.toString(), 0));

		enableCells(false, cell);
	}

	// As cacheAndRetrieve but allowing caching of clean objects
	@Test
	public void testCacheAllToggle() throws FormatException, IOException {
		final SCIFIOCellCache<ByteArray> cache1 = makeCache(128l * 128l);
		final SCIFIOCell<ByteArray> cell =
			cache1.load(0, new int[] { 128, 128 }, new long[] { 0l, 0l });

		// Enable caching non-dirty data
		cs.cacheAll(true);

		// Should be able to cache even though the cell isn't dirty
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell));

		// Verify the cell was cached
		assertEquals(cell, cs.retrieve(cache1.toString(), 0));

		enableCells(false, cell);
	}

	// As cacheAll but testing enabled/disabled
	@Test
	public void testEnabledToggle() throws FormatException, IOException {
		final SCIFIOCellCache<ByteArray> cache1 = makeCache(128l * 128l);
		final SCIFIOCell<ByteArray> cell =
			cache1.load(0, new int[] { 128, 128 }, new long[] { 0l, 0l });

		cs.cacheAll(true);
		cs.enable(false);

		// Caching should be disabled
		assertEquals(CacheResult.CACHE_DISABLED, cs.cache(cache1.toString(), 0,
			cell));

		// Verify the cell wasn't cached
		assertNull(cs.retrieve(cache1.toString(), 0));

		// Re-enable the cache
		cs.enable(true);
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell));

		// Verify the cell was cached
		assertEquals(cell, cs.retrieve(cache1.toString(), 0));

		enableCells(false, cell);
	}

	// Test caching with forced garbage collection to ensure
	// finalization. Should automatically cache in the CacheService
	@SuppressWarnings("unchecked")
	@Test
	public void testFinalization() throws FormatException, IOException {
		final TestCellCache<ByteArray> cache = makeTestCache(256l * 256l);

		SCIFIOCell<ByteArray> cell =
			cache.load(0, new int[] { 128, 128 }, new long[] { 0l, 0l });
		final WeakReference<SCIFIOCell<ByteArray>> ref =
			new WeakReference<SCIFIOCell<ByteArray>>(cell);
		// First, test with no modifications to the cell
		cell = null;
		cache.remove(0);

		// wait for reference to clear
		long time = System.currentTimeMillis();
		while (ref.get() != null && System.currentTimeMillis() - time < TIMEOUT) {
			System.gc();
		}

		// Cell shouldn't have cached since it was unmodified
		assertNull(cs.retrieve(cache.toString(), 0));

		// reload the cell
		cell = cache.load(0, new int[] { 128, 128 }, new long[] { 0l, 0l });

		// dirty it
		cell.getData().setValue(42, (byte) 0xace);

		// Wait for the cell to clear again
		cell = null;
		cache.remove(0);
		time = System.currentTimeMillis();
		while (ref.get() != null && System.currentTimeMillis() - time < TIMEOUT) {
			System.gc();
		}

		// Wait for finalization
		time = System.currentTimeMillis();
		while ((cell = (SCIFIOCell<ByteArray>) cs.retrieve(cache.toString(), 0)) == null &&
			System.currentTimeMillis() - time < TIMEOUT)
		{}

		// Cell should have cached since it was modified
		assertNotNull(cell);

		// Cell should be removed from the cache now (it will be re-cached if GC'd
		// again)
		assertNull(cs.retrieve(cache.toString(), 0));

		// repeat to ensure multiple cachings work
		cell = null;
		cache.remove(0);
		time = System.currentTimeMillis();
		while (ref.get() != null && System.currentTimeMillis() - time < TIMEOUT) {
			System.gc();
		}

		// Wait for finalization
		time = System.currentTimeMillis();
		while ((cell = (SCIFIOCell<ByteArray>) cs.retrieve(cache.toString(), 0)) == null &&
			System.currentTimeMillis() - time < TIMEOUT)
		{}

		// Cell should have cached again as it is still modified relative to what's
		// on disk
		assertNotNull(cell);

		enableCells(false, cell);
	}

	// Check the memory limit methods
	@SuppressWarnings("unchecked")
	@Test
	public void testMemoryControls() throws FormatException, IOException {
		// Make a cache with a 100MB dataset
		final SCIFIOCellCache<ByteArray> cache1 = makeCache(100l * 1024l * 1024l);

		// Set a 6MB limit on the cache
		cs.setMaxBytesOnDisk(1024l * 1024l * 6l);

		// load 4MB of the dataset
		final SCIFIOCell<ByteArray> cell1 =
			cache1.load(0, new int[] { 2048, 2048 }, new long[] { 0l, 0l });

		// Dirty and cache the cell
		cell1.getData().setValue(130, (byte) 0xace);
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell1));

		// Grab another 4MB chunk of the dataset
		final SCIFIOCell<ByteArray> cell2 =
			cache1.load(1, new int[] { 2048, 2048 }, new long[] { 2048l, 0l });

		// dirty the new cell
		cell2.getData().setValue(130, (byte) 0xace);

		// Caching should fail
		assertEquals(CacheResult.DISK_FULL, cs.cache(cache1.toString(), 1, cell2));

		// Verify the first cell was cached and the second cell wasn't
		assertEquals(cell1, cs.retrieveNoRecache(cache1.toString(), 0));
		assertNull(cs.retrieve(cache1.toString(), 1));

		// Re-cache cell 1
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell1));

		// Clear the cache and try caching cell 2 again
		cs.clearCache(cache1.toString());
		enableCells(true, cell1, cell2);
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 1, cell2));

		// Verify cache state
		assertEquals(cell2, cs.retrieve(cache1.toString(), 1));
		assertNull(cs.retrieve(cache1.toString(), 0));

		// Up the max bytes on disk and try caching both cells
		cs.setMaxBytesOnDisk(Long.MAX_VALUE);
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell1));
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 1, cell2));
		final SCIFIOCell<ByteArray> cell1b =
			(SCIFIOCell<ByteArray>) cs.retrieve(cache1.toString(), 0);
		final SCIFIOCell<ByteArray> cell2b =
			(SCIFIOCell<ByteArray>) cs.retrieve(cache1.toString(), 1);
		assertTrue(cell1.equals(cell1b));
		assertTrue(cell2.equals(cell2b));

		enableCells(false, cell1, cell2);
	}

	// Test erroneous API use
	@Test
	public void testMisuesCases() {
		// Fish for exceptions by dropping a cache that wasn't added
		cs.dropCache("fdsajklrewa");

		// Verify null getting from a cache that doesn't exist
		assertNull(cs.retrieve("drsarewa", 432));
	}

	// Basic workflow retrieving multiple cells
	@Test
	public void testMultipleCells() throws FormatException, IOException {
		final SCIFIOCellCache<ByteArray> cache1 = makeCache(100l * 1024l * 1024l);

		// Open the same chunk twice
		final SCIFIOCell<ByteArray> cell1a =
			cache1.load(0, new int[] { 2048, 2048 }, new long[] { 0l, 0l });
		final SCIFIOCell<ByteArray> cell2a =
			cache1.load(1, new int[] { 2048, 2048 }, new long[] { 0l, 0l });

		// Verify the arrays are equal
		assertTrue(Arrays.equals(cell1a.getData().getCurrentStorageArray(), cell2a
			.getData().getCurrentStorageArray()));

		// Dirty and modify the arrays
		cell1a.getData().setValue(42, (byte) 0xace);
		cell2a.getData().setValue(4242, (byte) 0xbeefeed);

		// Cache the arrays
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell1a));
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 1, cell2a));

		// RetrieveSCIFIOCell<ByteArray> cell1a the cells
		@SuppressWarnings("unchecked")
		final SCIFIOCell<ByteArray> cell1b =
			(SCIFIOCell<ByteArray>) cs.retrieve(cache1.toString(), 0);
		@SuppressWarnings("unchecked")
		final SCIFIOCell<ByteArray> cell2b =
			(SCIFIOCell<ByteArray>) cs.retrieve(cache1.toString(), 1);

		// The b cells should == the a cells, and be unequal to each other since
		// they were modified differently
		assertTrue(Arrays.equals(cell1a.getData().getCurrentStorageArray(), cell1b
			.getData().getCurrentStorageArray()));
		assertTrue(Arrays.equals(cell2a.getData().getCurrentStorageArray(), cell2b
			.getData().getCurrentStorageArray()));
		assertFalse(Arrays.equals(cell1b.getData().getCurrentStorageArray(), cell2b
			.getData().getCurrentStorageArray()));

		// Clear all caches
		cs.clearAllCaches();
		enableCells(true, cell1a, cell1b, cell2a, cell2b);

		// Try caching the retrieved cells
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 0, cell1b));
		assertEquals(CacheResult.SUCCESS, cs.cache(cache1.toString(), 1, cell2b));

		// Should have succeeded, as these cells are still modified from what was on
		// disk
		assertNotNull(cs.retrieve(cache1.toString(), 0));
		assertNotNull(cs.retrieve(cache1.toString(), 1));

		enableCells(false, cell1a, cell1b, cell2a, cell2b);
	}

	// -- Helper methods --

	// return a fake id for a file of the specified size
	private String makeFakeFile(final long bytes) {
		final long dim = Math.round(Math.sqrt(bytes));
		return "testImg&lengths=" + dim + "," + dim + ".fake";
	}

	// Creates a SCIFIOCellCache anonymously for a file of the specified size
	private SCIFIOCellCache<ByteArray> makeCache(final long bytes)
		throws FormatException, IOException
	{
		return makeCache(makeFakeFile(bytes));
	}

	// Creates a SCIFIOCellCache for the given id
	private SCIFIOCellCache<ByteArray> makeCache(final String id)
		throws FormatException, IOException
	{
		final ReaderFilter rf = scifio.initializer().initializeReader(id, true);
		final ByteArrayLoader loader = new ByteArrayLoader(rf, null);
		final SCIFIOCellCache<ByteArray> cellCache =
			new SCIFIOCellCache<ByteArray>(cs, loader);
		return cellCache;
	}

	// Creates a TestCellCache for the given id
	private TestCellCache<ByteArray> makeTestCache(final long bytes)
		throws FormatException, IOException
	{
		final String id = makeFakeFile(bytes);
		final ReaderFilter rf = scifio.initializer().initializeReader(id, true);
		final ByteArrayLoader loader = new ByteArrayLoader(rf, null);
		final TestCellCache<ByteArray> cellCache =
			new TestCellCache<ByteArray>(cs, loader);
		return cellCache;
	}

	private void enableCells(final boolean enabled, final SCIFIOCell<?>... cells)
	{
		for (final SCIFIOCell<?> cell : cells) {
			if (cell != null) cell.cacheOnFinalize(enabled);
		}
	}

	// -- HelperClass --

	private static class TestCellCache<A extends ArrayDataAccess<?>> extends
		SCIFIOCellCache<A>
	{

		public TestCellCache(final CacheService<SCIFIOCell<?>> service,
			final SCIFIOArrayLoader<A> loader)
		{
			super(service, loader);
		}

		// Removes the specified cell index if present
		public void remove(final int index) {
			final Integer k = cs.getKey(this.toString(), index);

			map.remove(k);
		}

	}

}
