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

package io.scif.services;

import io.scif.SCIFIOService;
import io.scif.io.IRandomAccess;
import io.scif.io.VirtualHandle;

import java.io.IOException;
import java.util.HashMap;

/**
 * Contains methods for mapping files and ids, and generating
 * {@link io.scif.io.IRandomAccess} handles for reading from these locations.
 *
 * @see io.scif.io.Location
 * @see io.scif.io.IRandomAccess
 * @author Mark Hiner
 */
public interface LocationService extends SCIFIOService {

	/**
	 * Clear all caches and reset cache-related bookkeeping variables to their
	 * original values.
	 */
	void reset();

	/**
	 * Turn cacheing of directory listings on or off. Cacheing is turned off by
	 * default. Reasons to cache - directory listings over network shares can be
	 * very expensive, especially in HCS experiments with thousands of files in
	 * the same directory. Technically, if you use a directory listing and then go
	 * and access the file, you are using stale information. Unlike a database,
	 * there's no transactional integrity to file system operations, so the
	 * directory could change by the time you access the file. Reasons not to
	 * cache - the contents of the directories might change during the program
	 * invocation.
	 *
	 * @param cache - true to turn cacheing on, false to leave it off.
	 */
	void cacheDirectoryListings(boolean cache);

	/**
	 * Cache directory listings for this many seconds before relisting.
	 *
	 * @param sec - use the cache if a directory list was done within this many
	 *          seconds.
	 */
	void setCacheDirectoryTimeout(double sec);

	/**
	 * Clear the directory listings cache. Do this if directory contents might
	 * have changed in a significant way.
	 */
	void clearDirectoryListingsCache();

	/**
	 * Remove any cached directory listings that have expired.
	 */
	void cleanStaleCacheEntries();

	/**
	 * Maps the given id to an actual filename on disk. Typically actual filenames
	 * are used for ids, making this step unnecessary, but in some cases it is
	 * useful; e.g., if the file has been renamed to conform to a standard naming
	 * scheme and the original file extension is lost, then using the original
	 * filename as the id assists format handlers with type identification and
	 * pattern matching, and the id can be mapped to the actual filename for
	 * reading the file's contents.
	 *
	 * @see #getMappedId(String)
	 */
	void mapId(String id, String filename);

	/** Maps the given id to the given IRandomAccess object. */
	void mapFile(String id, IRandomAccess ira);

	/**
	 * Gets the actual filename on disk for the given id. Typically the id itself
	 * is the filename, but in some cases may not be; e.g., if OMEIS has renamed a
	 * file from its original name to a standard location such as Files/101, the
	 * original filename is useful for checking the file extension and doing
	 * pattern matching, but the renamed filename is required to read its
	 * contents.
	 *
	 * @see #mapId(String, String)
	 */
	String getMappedId(String id);

	/** Gets the random access handle for the given id. */
	IRandomAccess getMappedFile(String id);

	/** Return the id mapping. */
	HashMap<String, Object> getIdMap();

	/**
	 * Set the id mapping using the given HashMap.
	 *
	 * @throws IllegalArgumentException if the given HashMap is null.
	 */
	void setIdMap(HashMap<String, Object> map);

	/**
	 * Gets an IRandomAccess object that can read from the given file.
	 * <p>
	 * NB: if the file doesn't exist, a {@link VirtualHandle} will be returned.
	 * </p>
	 *
	 * @see io.scif.io.IRandomAccess
	 */
	IRandomAccess getHandle(String id) throws IOException;

	/**
	 * Gets an IRandomAccess object that can read from or write to the given file.
	 * <p>
	 * NB: if the file doesn't exist, a {@link VirtualHandle} will be returned.
	 * </p>
	 *
	 * @see io.scif.io.IRandomAccess
	 */
	IRandomAccess getHandle(String id, boolean writable) throws IOException;

	/**
	 * Gets an IRandomAccess object that can read from or write to the given file.
	 * <p>
	 * NB: if the file doesn't exist, a {@link VirtualHandle} will be returned.
	 * </p>
	 *
	 * @see io.scif.io.IRandomAccess
	 */
	IRandomAccess getHandle(String id, boolean writable,
		boolean allowArchiveHandles) throws IOException;

	/**
	 * Checks that the given id points at a valid data stream.
	 *
	 * @param id The id string to validate.
	 * @throws IOException if the id is not valid.
	 */
	void checkValidId(String id) throws IOException;

	/**
	 * Returns the set of listings for the provided key.
	 */
	String[] getCachedListing(String key);

	/**
	 * Maps the set of listings to the provided key.
	 */
	void putCachedListing(String key, String[] listing);
}
