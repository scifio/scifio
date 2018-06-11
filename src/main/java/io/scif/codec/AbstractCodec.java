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

package io.scif.codec;

import io.scif.AbstractSCIFIOPlugin;
import io.scif.FormatException;

import java.io.IOException;
import java.util.Random;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;

/**
 * BaseCodec contains default implementation and testing for classes
 * implementing the Codec interface, and acts as a base class for any of the
 * compression classes. Base 1D compression and decompression methods are not
 * implemented here, and are left as abstract. 2D methods do simple
 * concatenation and call to the 1D methods
 *
 * @author Eric Kjellman
 */
public abstract class AbstractCodec extends AbstractSCIFIOPlugin implements
	Codec
{

	@Parameter
	private DataHandleService handles;

	// -- BaseCodec API methods --

	/**
	 * Main testing method default implementation. This method tests whether the
	 * data is the same after compressing and decompressing, as well as doing a
	 * basic test of the 2D methods.
	 *
	 * @throws FormatException Can only occur if there is a bug in the compress
	 *           method.
	 */
	public void test() throws FormatException {
		final byte[] testdata = new byte[50000];
		final Random r = new Random();
		log().info("Testing " + this.getClass().getName());
		log().info("Generating random data");
		r.nextBytes(testdata);
		log().info("Compressing data");
		final byte[] compressed = compress(testdata, null);
		log().info("Compressed size: " + compressed.length);
		log().info("Decompressing data");
		final byte[] decompressed = decompress(compressed);
		log().info("Comparing data...");
		if (testdata.length != decompressed.length) {
			log().info("Test data differs in length from uncompressed data");
			log().info("Exiting...");
			System.exit(-1);
		}
		else {
			boolean equalsFlag = true;
			for (int i = 0; i < testdata.length; i++) {
				if (testdata[i] != decompressed[i]) {
					log().info("Test data and uncompressed data differ at byte " + i);
					equalsFlag = false;
				}
			}
			if (!equalsFlag) {
				log().info("Comparison failed. Exiting...");
				System.exit(-1);
			}
		}
		log().info("Success.");
		log().info("Generating 2D byte array test");
		final byte[][] twoDtest = new byte[100][500];
		for (int i = 0; i < 100; i++) {
			System.arraycopy(testdata, 500 * i, twoDtest[i], 0, 500);
		}
		final byte[] twoDcompressed = compress(twoDtest, null);
		log().info("Comparing compressed data...");
		if (twoDcompressed.length != compressed.length) {
			log().info("1D and 2D compressed data not same length");
			log().info("Exiting...");
			System.exit(-1);
		}
		boolean equalsFlag = true;
		for (int i = 0; i < twoDcompressed.length; i++) {
			if (twoDcompressed[i] != compressed[i]) {
				log().info("1D data and 2D compressed data differs at byte " + i);
				equalsFlag = false;
			}
			if (!equalsFlag) {
				log().info("Comparison failed. Exiting...");
				System.exit(-1);
			}
		}
		log().info("Success.");
		log().info("Test complete.");
	}

	// -- Codec API methods --

	/**
	 * 2D data block encoding default implementation. This method simply
	 * concatenates data[0] + data[1] + ... + data[i] into a 1D block of data,
	 * then calls the 1D version of compress.
	 *
	 * @param data The data to be compressed.
	 * @param options Options to be used during compression, if appropriate.
	 * @return The compressed data.
	 * @throws FormatException If input is not a compressed data block of the
	 *           appropriate type.
	 */
	@Override
	public byte[] compress(final byte[][] data, final CodecOptions options)
		throws FormatException
	{
		int len = 0;
		for (int i = 0; i < data.length; i++) {
			len += data[i].length;
		}
		final byte[] toCompress = new byte[len];
		int curPos = 0;
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(data[i], 0, toCompress, curPos, data[i].length);
			curPos += data[i].length;
		}
		return compress(toCompress, options);
	}

	@Override
	public byte[] decompress(final byte[] data) throws FormatException {
		return decompress(data, null);
	}

	@Override
	public byte[] decompress(final byte[][] data) throws FormatException {
		return decompress(data, null);
	}

	@Override
	public byte[] decompress(final byte[] data, final CodecOptions options)
		throws FormatException
	{
		try (DataHandle<Location> handle = handles.create(new BytesLocation(data))) {
			return decompress(handle, options);
		}
		catch (final IOException e) {
			throw new FormatException(e);
		}
	}

	/**
	 * 2D data block decoding default implementation. This method simply
	 * concatenates data[0] + data[1] + ... + data[i] into a 1D block of data,
	 * then calls the 1D version of decompress.
	 *
	 * @param data The data to be decompressed.
	 * @return The decompressed data.
	 * @throws FormatException If input is not a compressed data block of the
	 *           appropriate type.
	 */
	@Override
	public byte[] decompress(final byte[][] data, final CodecOptions options)
		throws FormatException
	{
		if (data == null) throw new IllegalArgumentException(
			"No data to decompress.");
		int len = 0;
		for (final byte[] aData1 : data) {
			len += aData1.length;
		}
		final byte[] toDecompress = new byte[len];
		int curPos = 0;
		for (final byte[] aData : data) {
			System.arraycopy(aData, 0, toDecompress, curPos, aData.length);
			curPos += aData.length;
		}
		return decompress(toDecompress, options);
	}

}
