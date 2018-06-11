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

package io.scif.gui;

import io.scif.ImageMetadata;

import net.imagej.axis.Axes;

/**
 * Helper class for converting N-D Metadata to x,y,c values for use in AWT
 * classes.
 *
 * @author Mark Hiner
 */
public class XYCTuple {

	// -- Fields --

	private long x, y, c;

	// -- Constructor --

	public XYCTuple(final ImageMetadata meta, final long[] axisLengths) {
		x = 1;
		y = 1;
		c = 1;
		if (meta.getInterleavedAxisCount() > 0) {
			// compress the non-XY planar axes
			for (int i = 0; i < axisLengths.length; i++) {
				if (meta.getAxisIndex(Axes.X) == i) {
					x = axisLengths[i];
				}
				else if (meta.getAxisIndex(Axes.Y) == i) {
					y = axisLengths[i];
				}
				else {
					c *= axisLengths[i];
				}
			}
		}
		else {
			x = axisLengths[meta.getAxisIndex(Axes.X)];
			y = axisLengths[meta.getAxisIndex(Axes.Y)];
			// pull the channel dimension if it's a planar axis.
			final int cIndex = meta.getAxisIndex(Axes.CHANNEL);
			if (cIndex < meta.getPlanarAxisCount() && cIndex >= 0) {
				c = axisLengths[meta.getAxisIndex(Axes.CHANNEL)];
			}

			if (c <= 0) c = 1;
		}
	}

	public XYCTuple(final long x, final long y, final long c) {
		this.x = x;
		this.y = y;
		this.c = c;
	}

	// -- Accessors --

	public int x() {
		return (int) x;
	}

	public int y() {
		return (int) y;
	}

	public int c() {
		return (int) c;
	}
}
