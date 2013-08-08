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

package io.scif.img.cell.loaders;

import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.img.SubRegion;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.List;

import net.imglib2.meta.Axes;

/**
 * Abstract superclass for all {@link SCIFIOArrayLoader} implementations.
 * <p>
 * Reads the byte array appropriate for the given cell dimensions and delegates
 * to each subclass's array conversion method. See
 * {@link #convertBytes(Object, byte[], int)}.
 * </p>
 * 
 * @author Mark Hiner
 */
public abstract class AbstractArrayLoader<A> implements SCIFIOArrayLoader<A> {

	final private Reader reader;
	final private SubRegion subRegion;

	public AbstractArrayLoader(final Reader reader, final SubRegion subRegion) {
		this.reader = reader;
		this.subRegion = subRegion;
	}

	public A loadArray(final int[] dimensions, final long[] min) {
		synchronized (reader) {
			final Metadata meta = reader.getMetadata();

			StringBuilder dimOrder =
				new StringBuilder(FormatTools.findDimensionOrder(meta, 0).toUpperCase());

			if (meta.getAxisLength(0, Axes.X) == 1) dimOrder =
				dimOrder.deleteCharAt(dimOrder.indexOf("X"));
			if (meta.getAxisLength(0, Axes.Y) == 1) dimOrder =
				dimOrder.deleteCharAt(dimOrder.indexOf("Y"));
			if (meta.getAxisLength(0, Axes.Z) == 1) dimOrder =
				dimOrder.deleteCharAt(dimOrder.indexOf("Z"));
			if (meta.getEffectiveSizeC(0) == 1) dimOrder =
				dimOrder.deleteCharAt(dimOrder.indexOf("C"));
			if (meta.getAxisLength(0, Axes.TIME) == 1) dimOrder =
				dimOrder.deleteCharAt(dimOrder.indexOf("T"));

			final int xIndex = dimOrder.indexOf("X");
			final int yIndex = dimOrder.indexOf("Y");
			final int zIndex = dimOrder.indexOf("Z");
			final int cIndex = dimOrder.indexOf("C");
			final int tIndex = dimOrder.indexOf("T");

			final int zSlice = new Long(zIndex == -1 ? 0 : min[zIndex]).intValue();
			final int tSlice = new Long(tIndex == -1 ? 0 : min[tIndex]).intValue();
			final int cSlice = new Long(cIndex == -1 ? 0 : min[cIndex]).intValue();
			final int zMax = zIndex == -1 ? 1 : dimensions[zIndex] + zSlice;
			final int tMax = tIndex == -1 ? 1 : dimensions[tIndex] + tSlice;
			final int cMax = cIndex == -1 ? 1 : dimensions[cIndex] + cSlice;

			Plane tmpPlane = null;

			int planeSize = -1;

			int[][] iterBounds = null;
			String zctOrder = "";

			if (zIndex < cIndex) {
				if (zIndex < tIndex) {
					if (cIndex < tIndex) {
						zctOrder = "ZCT";
						iterBounds = getBounds(zSlice, zMax, cSlice, cMax, tSlice, tMax);
					}
					else {
						zctOrder = "ZTC";
						iterBounds = getBounds(zSlice, zMax, tSlice, tMax, cSlice, cMax);
					}
				}
				else {
					zctOrder = "TZC";
					iterBounds = getBounds(tSlice, tMax, zSlice, zMax, cSlice, cMax);
				}
			}
			else if (tIndex < cIndex) {
				zctOrder = "TCZ";
				iterBounds = getBounds(tSlice, tMax, cSlice, cMax, zSlice, zMax);
			}
			else {
				if (zIndex < tIndex) {
					zctOrder = "CZT";
					iterBounds = getBounds(cSlice, cMax, zSlice, zMax, tSlice, tMax);
				}
				else {
					zctOrder = "CTZ";
					iterBounds = getBounds(cSlice, cMax, tSlice, tMax, zSlice, zMax);
				}
			}

			int planesRead = 0;
			final int[] index =
				new int[] { iterBounds[0][0], iterBounds[1][0], iterBounds[2][0] };

			int x = new Long(xIndex == -1 ? 0 : min[xIndex]).intValue();
			int y = new Long(yIndex == -1 ? 0 : min[yIndex]).intValue();
			int w = xIndex == -1 ? 1 : dimensions[xIndex];
			int h = yIndex == -1 ? 1 : dimensions[yIndex];

			final int i1 = index[1], i2 = index[2];
			
			if (subRegion != null) {
				x = subRegion.indices(0).get(0).intValue();
				w = subRegion.indices(0).get(subRegion.indices(0).size()).intValue();
				
				if (subRegion.size() > 1) {
					y = subRegion.indices(1).get(0).intValue();
					h = subRegion.indices(1).get(subRegion.indices(1).size()).intValue();
				}
			}

			boolean success = false;

			A data = null;

			while (!success) {
				try {
					data = emptyArray(dimensions);
					success = true;
				}
				catch (final OutOfMemoryError e) {}
			}

			try {
				for (; index[0] < iterBounds[0][1]; index[0]++) {
					for (; index[1] < iterBounds[1][1]; index[1]++) {
						for (; index[2] < iterBounds[2][1]; index[2]++) {
							
							if (!inSubregion(index[0], index[1], index[2])) continue; 
							
							final int z = index[zctOrder.indexOf('Z')];
							final int c = index[zctOrder.indexOf('C')];
							final int t = index[zctOrder.indexOf('T')];

							final int planeIndex = FormatTools.getIndex(reader, 0, z, c, t);

							success = false;
							while (!success) {
								try {
									if (tmpPlane == null) tmpPlane =
										reader.openPlane(0, planeIndex, x, y, w, h);
									else tmpPlane =
										reader.openPlane(0, planeIndex, tmpPlane, x, y, w, h);
									success = true;
								}
								catch (final OutOfMemoryError e) {}
							}
							if (planeSize == -1) planeSize = tmpPlane.getBytes().length;

							convertBytes(data, tmpPlane.getBytes(), planesRead);

							planesRead++;
						}
						index[2] = i2;
					}
					index[1] = i1;
				}
			}
			catch (final FormatException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}
			catch (final IOException e) {
				throw new IllegalStateException(
					"Could not open a plane for the given dimensions", e);
			}

			return data;
		}
	}

	private boolean inSubregion(int... dims) {
		if (subRegion == null) return true;
		
		boolean inSubregion = true;
		
		for (int i=2; inSubregion && i<subRegion.size(); i++) {
			inSubregion = indexContained(subRegion.indices(i), dims[i-2]);
		}

		return inSubregion;
	}

	private boolean indexContained(List<Long> indices, int i) {
		for (Long l : indices)
			if (l.intValue() != i) return false;

		return true;
	}

	private int[][] getBounds(final int start1, final int max1, final int start2,
		final int max2, final int start3, final int max3)
	{
		return new int[][] { { start1, max1 }, { start2, max2 }, { start3, max3 } };
	}

	protected int countEntities(final int[] dimensions) {
		int numEntities = 1;
		for (int i = 0; i < dimensions.length; ++i)
			numEntities *= dimensions[i];
		return numEntities;
	}

	public abstract void convertBytes(A data, byte[] bytes, int planesRead);

	protected Reader reader() {
		return reader;
	}
}
