/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif;

import io.scif.gui.AWTImageTools;

import java.awt.image.BufferedImage;

import org.scijava.Context;
import org.scijava.util.Bytes;

/**
 * A {@link io.scif.Plane} implementation using a
 * {@link java.awt.image.BufferedImage} for the underlying data representation.
 *
 * @see io.scif.Plane
 * @see io.scif.DataPlane
 * @see java.awt.image.BufferedImage
 * @author Mark Hiner
 */
public class BufferedImagePlane extends
	AbstractPlane<BufferedImage, BufferedImagePlane>
{

	// -- Fields --

	/** Cached byte array representation of this plane's data. */
	byte[] cachedBytes = null;

	// -- Constructor --

	public BufferedImagePlane(final Context context) {
		super(context);
	}

	public BufferedImagePlane(final Context context, final ImageMetadata meta,
		final long[] planeOffsets, final long[] planeBounds)
	{
		super(context, meta, planeOffsets, planeBounds);
	}

	// -- Plane API methods --

	/**
	 * Standardizes this plane's {@link BufferedImage} to a byte[].
	 * <p>
	 * NB: If this plane contains multiple channels, the planes for each channel
	 * will be condensed to a single array. The first entry for each channel's
	 * data will have an offset of: {@code channelIndex * planeSize}.
	 *
	 * @return The byte[] extracted from this Plane's BufferedImage
	 */
	@Override
	public byte[] getBytes() {
		if (cachedBytes == null) {
			switch (getData().getColorModel().getComponentSize(0)) {
				case 8:
					// 8-bit types can directly delegate to AWTImageTools
					cachedBytes = AWTImageTools.getBytes(getData(), false);
					break;
				case 16:
					// Here we need to unpack the channel arrays appropriately
					final short[][] ts = AWTImageTools.getShorts(getData());
					cachedBytes = new byte[ts.length * ts[0].length * 2];
					for (int c = 0; c < ts.length; c++) {
						int offset = c * ts[c].length * 2;
						for (int i = 0; i < ts[c].length; i++) {
							Bytes.unpack(ts[c][i], cachedBytes, offset, 2,
								getImageMetadata().isLittleEndian());
							offset += 2;
						}
					}
					break;
			}
		}

		return cachedBytes;
	}

	// -- AbstractPlane API --

	@Override
	protected BufferedImage blankPlane(final long[] planeOffsets,
		final long[] planeBounds)
	{
		final int type = getImageMetadata().getPixelType();

		final long[] axes = new long[planeOffsets.length];

		for (int i = 0; i < axes.length; i++) {
			axes[i] = planeBounds[i] - planeOffsets[i];
		}

		return AWTImageTools.blankImage(getImageMetadata(), axes, type);
	}

	@Override
	public void setData(final BufferedImage data) {
		super.setData(data);
		cachedBytes = null;
	}
}
