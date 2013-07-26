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

package io.scif.util;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.io.RandomAccessOutputStream;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import net.imglib2.meta.AxisType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for working with {@link io.scif.Metadata} objects.
 * 
 * @see io.scif.Metadata <dl>
 *      <dt><b>Source code:</b></dt>
 *      <dd><a href=
 *      "http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MetadataTools.java"
 *      >Trac</a>, <a href=
 *      "http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MetadataTools.java;hb=HEAD"
 *      >Gitweb</a></dd>
 *      </dl>
 * @author Mark Hiner
 */
public class SCIFIOMetadataTools {

	// -- Constants --

	private static final Logger LOGGER = LoggerFactory
		.getLogger(SCIFIOMetadataTools.class);

	// -- Constructor --

	private SCIFIOMetadataTools() {}

	// -- Utility Methods -- DatasetMetadata --

	/**
	 * Casts the provided Metadata object to the generic type of this method.
	 * <p>
	 * Usage: To cast a Metadata instance to ConcreteMetadata, use:
	 * <p>
	 * {@code SCIFIOMetadataTools.<ConcreteMetadata>castMeta(meta)}
	 * </p>
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public static <M extends Metadata> M castMeta(final Metadata meta) {
		// TODO need to check for safe casting here..

		return (M) meta;
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe an Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out) throws FormatException
	{
		verifyMinimumPopulated(src, out, 0, 0);
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe an Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out, final int imageIndex)
		throws FormatException
	{
		verifyMinimumPopulated(src, out, imageIndex, 0);
	}

	/**
	 * Checks whether the given metadata object has the minimum metadata populated
	 * to successfully describe the nth Image.
	 * 
	 * @throws FormatException if there is a missing metadata field, or the
	 *           metadata object is uninitialized
	 */
	public static void verifyMinimumPopulated(final Metadata src,
		final RandomAccessOutputStream out, final int imageIndex,
		final int planeIndex) throws FormatException
	{
		if (src == null) {
			throw new FormatException("Metadata object is null; "
				+ "call Writer.setMetadata() first");
		}

		if (out == null) {
			throw new FormatException("RandomAccessOutputStream object is null; "
				+ "call Writer.setSource(<String/File/RandomAccessOutputStream>) first");
		}

		if (src.getAxisCount(0) == 0) {
			throw new FormatException("Axiscount #" + imageIndex + " is 0");
		}
	}

	/**
	 * Populates the provided ImageMetadata. Automatically looks up bits per pixel
	 * for the provided pixel type.
	 */
	public static void populate(final ImageMetadata iMeta,
		final String dimensionOrder, final int pixelType, final int rgbCCount,
		final boolean orderCertain, final boolean littleEndian,
		final boolean indexed, final boolean falseColor,
		final boolean metadataComplete, final int sizeX, final int sizeY,
		final int sizeZ, final int sizeC, final int sizeT)
	{
		populate(iMeta, dimensionOrder, pixelType, rgbCCount, FormatTools
			.getBitsPerPixel(pixelType), orderCertain, littleEndian, indexed,
			falseColor, metadataComplete, sizeX, sizeY, sizeZ, sizeC, sizeT);
	}

	/**
	 * Populates the provided ImageMetadata.
	 */
	public static void populate(final ImageMetadata iMeta,
		final String dimensionOrder, final int pixelType, final int rgbCCount,
		final int bitsPerPixel, final boolean orderCertain,
		final boolean littleEndian, final boolean indexed,
		final boolean falseColor, final boolean metadataComplete, final int sizeX,
		final int sizeY, final int sizeZ, final int sizeC, final int sizeT)
	{
		iMeta.setPixelType(pixelType);
		iMeta.setBitsPerPixel(bitsPerPixel);
		iMeta.setOrderCertain(orderCertain);
		iMeta.setRGB(rgbCCount > 1);
		iMeta.setPlaneCount(sizeZ * sizeT * sizeC / rgbCCount);
		iMeta.setLittleEndian(littleEndian);
		iMeta.setIndexed(indexed);
		iMeta.setFalseColor(falseColor);
		iMeta.setMetadataComplete(metadataComplete);
		populateDimensions(iMeta, dimensionOrder, sizeX, sizeY, sizeZ, sizeC, sizeT);
	}

	/**
	 * Populates the provided ImageMetadata's axis types and lengths using the
	 * provided dimension order and sizes.
	 * 
	 * @param iMeta - ImageMetadata to populate
	 * @param dimensionOrder - A serialized list of dimensions. Each character is
	 *          interpreted as an Axes enumeration (e.g. 'X' = 'x' = Axes.X)
	 * @param sizeX - Length of Axes.X
	 * @param sizeY - Length of Axes.Y
	 * @param sizeZ - Length of Axes.Z
	 * @param sizeC - Length of Axes.CHANNEL
	 * @param sizeT - Length of Axes.TIME
	 */
	public static void populateDimensions(final ImageMetadata iMeta,
		final String dimensionOrder, final int sizeX, final int sizeY,
		final int sizeZ, final int sizeC, final int sizeT)
	{
		final int[] axisLengths = new int[5];

		for (int i = 0; i < 5; i++) {
			switch (dimensionOrder.toUpperCase().charAt(i)) {
				case 'X':
					axisLengths[i] = Math.max(sizeX, 1);
					break;
				case 'Y':
					axisLengths[i] = Math.max(sizeY, 1);
					break;
				case 'Z':
					axisLengths[i] = Math.max(sizeZ, 1);
					break;
				case 'C':
					axisLengths[i] = Math.max(sizeC, 1);
					break;
				case 'T':
					axisLengths[i] = Math.max(sizeT, 1);
					break;
				default:
					axisLengths[i] = 1;
			}
		}

		populateDimensions(iMeta, dimensionOrder, axisLengths);
	}

	/**
	 * Populates the provided ImageMetadata's axis types and lengths using the
	 * provided dimension order and sizes.
	 * 
	 * @param iMeta - ImageMetadata to populate
	 * @param dimensionOrder - A serialized list of dimensions. Each character is
	 *          interpreted as an Axes enumeration (e.g. 'X' = 'x' = Axes.X)
	 * @param lengths - An array of axis lengths, parallel to the dimensionOrder
	 *          list
	 */
	public static void populateDimensions(final ImageMetadata iMeta,
		final String dimensionOrder, final int[] lengths)
	{
		final AxisType[] axes = FormatTools.findDimensionList(dimensionOrder);

		iMeta.setAxes(axes, lengths);
	}

	// Utility methods -- original metadata --

	/**
	 * Merges the given lists of metadata, prepending the specified prefix for the
	 * destination keys.
	 */
	public static void merge(final Map<String, Object> src,
		final Map<String, Object> dest, final String prefix)
	{
		for (final String key : src.keySet()) {
			dest.put(prefix + key, src.get(key));
		}
	}

	/** Gets a sorted list of keys from the given hashtable. */
	public static String[] keys(final Hashtable<String, Object> meta) {
		final String[] keys = new String[meta.size()];
		meta.keySet().toArray(keys);
		Arrays.sort(keys);
		return keys;
	}
}
