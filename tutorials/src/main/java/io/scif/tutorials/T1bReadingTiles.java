/*
 * #%L
 * Tutorials for SCIFIO API
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * To the extent possible under law, the SCIFIO developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 * 
 * See the CC0 1.0 Universal license for details:
 * http://creativecommons.org/publicdomain/zero/1.0/
 * #L%
 */

package io.scif.tutorials;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.SCIFIO;

import java.io.IOException;

import net.imglib2.meta.Axes;

/**
 * Tutorial using the SCIFIO API to open image tiles.
 * 
 * @author Mark Hiner
 */
public class T1bReadingTiles {

	public static void main(final String... args) throws FormatException,
		IOException
	{

		// As always we'll need a SCIFIO for this tutorial
		final SCIFIO scifio = new SCIFIO();

		// This time we're going to set up a huge image path
		final String hugeImage = "hugePlane&sizeX=70000&sizeY=80000.fake";

		// We initialize a reader as we did before
		final Reader reader = scifio.initializer().initializeReader(hugeImage);

		// Now we'll try the naive thing, and just open all the planes in this
		// dataset.
		try {
			for (int i = 0; i < reader.getImageCount(); i++) {
				for (int j = 0; j < reader.getPlaneCount(i); j++) {
					reader.openPlane(i, j);
				}
			}
		}
		catch (final FormatException e) {
			System.out.println("Caught:\n" + e);
		}
		// There should be an exception caught above. Planes that are greater than
		// 2GB in size
		// will not be instantiated. The plane opening code prints each time it
		// opens a tile,
		// so if the output below is uncommented you probably will miss the
		// exception message.

		// We'll need some basic information about this dataset, so let's get a
		// reference to
		// its metadata.
		final Metadata meta = reader.getMetadata();

		for (int i = 0; i < reader.getImageCount(); i++) {

			// These methods will compute the optimal width to use with
			// reader#openPlane
			final int optimalTileWidth = reader.getOptimalTileWidth(i);
			final int optimalTileHeight = reader.getOptimalTileHeight(i);

			// Then we need to figure out how many tiles are actually present in a
			// plane,
			// given the tile height and width
			final int tilesWide =
				(int) Math.ceil((double) meta.getAxisLength(i, Axes.X) /
					optimalTileWidth);
			final int tilesHigh =
				(int) Math.ceil((double) meta.getAxisLength(i, Axes.Y) /
					optimalTileHeight);

			int x, y = 0;

			// now we can open each tile, one at a time, for each plane in this image
			for (int j = 0; j < meta.getPlaneCount(i); j++) {
				for (int tileX = 0; tileX < tilesWide; tileX++) {
					for (int tileY = 0; tileY < tilesHigh; tileY++) {

						// these are pointers to the position in the current plane
						x = tileX * optimalTileWidth;
						y = tileY * optimalTileHeight;

						// and then we compute the actual dimensions of the current tile, in
						// case
						// the image was not perfectly divisible by the optimal dimensions.
						final int actualTileWidth =
							Math.min(optimalTileWidth, meta.getAxisLength(i, Axes.X) - x);
						final int actualTileHeight =
							Math.min(optimalTileHeight, meta.getAxisLength(i, Axes.Y) - y);

						// Finally we open the current plane, using an openPlane signature
						// that allows us
						// to specify a sub-region of the current plane.
						// FIXME: uncomment these lines of code after the first time you run
						// this tutorial.
//            System.out.println("Image:" + i + " Plane:" + j + " Tile:" + (tileX + tileY) + " -- " 
//                + reader.openPlane(i, j, x, y, actualTileWidth, actualTileHeight));

						// Here, if we saved a reference to the returned Plane, we would do
						// any necessary
						// processing of the bytes.

						// NB: the openPlane signature we used creates a new plane each
						// time. If there
						// are a significant number of tiles being read, it may be more
						// efficient to
						// create a Plane ahead of time using the reader.createPlane method,
						// and then
						// just reuse it for all tiles of that size.
					}
				}
			}
		}
	}
}
