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

package io.scif.filters;

import io.scif.ByteArrayPlane;
import io.scif.FormatException;
import io.scif.Plane;
import io.scif.common.DataTools;
import io.scif.util.FormatTools;
import io.scif.util.ImageTools;

import java.io.IOException;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Logic to automatically separate the channels in a file.
 */
@Plugin(type = ChannelSeparator.class, priority = ChannelSeparator.PRIORITY,
	attrs = {
		@Attr(name = ChannelSeparator.FILTER_KEY,
			value = ChannelSeparator.FILTER_VALUE),
		@Attr(name = ChannelSeparator.ENABLED_KEY,
			value = ChannelSeparator.ENABLED_VAULE) })
public class ChannelSeparator extends AbstractReaderFilter {

	// -- Constants --

	public static final double PRIORITY = 2.0;
	public static final String FILTER_VALUE = "io.scif.Reader";

	// -- Fields --

	/** Last plane opened. */
	private Plane lastPlane = null;

	/** Index of last plane opened. */
	private int lastPlaneIndex = -1;

	/** Index of last plane opened. */
	private int lastImageIndex = -1;

	/** X index of last plane opened. */
	private int lastPlaneX = -1;

	/** Y index of last plane opened. */
	private int lastPlaneY = -1;

	/** Width of last plane opened. */
	private int lastPlaneWidth = -1;

	/** Height of last plane opened. */
	private int lastPlaneHeight = -1;

	// -- Constructor --

	public ChannelSeparator() {
		super(ChannelSeparatorMetadata.class);
	}

	// -- ChannelSeparator API methods --

	/**
	 * Returns the image number in the original dataset that corresponds to the
	 * given image number. For instance, if the original dataset was a single RGB
	 * image and the given image number is 2, the return value will be 0.
	 * 
	 * @param planeIndex is a plane number greater than or equal to 0 and less
	 *          than getPlaneCount()
	 * @return the corresponding plane number in the original (unseparated) data.
	 */
	public int getOriginalIndex(final int imageIndex, final int planeIndex) {
		final int planeCount = getPlaneCount(imageIndex);
		final int originalCount = getParent().getPlaneCount(imageIndex);

		if (planeCount == originalCount) return planeIndex;
		final int[] coords = FormatTools.getZCTCoords(this, imageIndex, planeIndex);
		coords[1] /= getParentMeta().getRGBChannelCount(imageIndex);
		return FormatTools.getIndex(getParent(), imageIndex, coords[0], coords[1],
			coords[2]);
	}

	// -- AbstractReaderFilter API Methods --

	/*
	 * @see io.scif.filters.AbstractReaderFilter#setSourceHelper(java.lang.String)
	 */
	@Override
	protected void setSourceHelper(final String source) {
		cleanUp();
	}

	@Override
	public int getPlaneCount(final int imageIndex) {
		return getMetadata().get(imageIndex).getPlaneCount();
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int)
	 */
	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return openPlane(planeIndex, imageIndex, 0, 0, getMetadata().getAxisLength(
			imageIndex, Axes.X), getMetadata().getAxisLength(imageIndex, Axes.Y));
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, io.scif.Plane)
	 */
	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, plane.getxOffset(), plane
			.getyOffset(), plane.getxLength(), plane.getyLength());
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, int, int, int, int)
	 */
	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final int x, final int y, final int w, final int h) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, createPlane(x, y, w, h), x, y, w,
			h);
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#openPlane(int, int, io.scif.Plane, int, int, int, int)
	 */
	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		Plane plane, final int x, final int y, final int w, final int h)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);
		FormatTools.checkPlaneNumber(this, imageIndex, planeIndex);

		if (getParentMeta().isRGB(imageIndex) &&
			!getParentMeta().isIndexed(imageIndex))
		{
			final int c =
				getMetadata().getAxisLength(imageIndex, Axes.CHANNEL) /
					getParentMeta().getEffectiveSizeC(imageIndex);
			final int source = getOriginalIndex(imageIndex, planeIndex);
			final int channel = planeIndex % c;
			final int bpp =
				FormatTools.getBytesPerPixel(getMetadata().getPixelType(imageIndex));

			if (plane == null ||
				!ByteArrayPlane.class.isAssignableFrom(plane.getClass()))
			{
				final ByteArrayPlane bp = new ByteArrayPlane(getContext());
				final byte[] buf =
					DataTools.allocate(w, h, FormatTools.getBytesPerPixel(getMetadata()
						.getPixelType(imageIndex)));
				bp.populate(buf, x, y, w, h);
				plane = bp;
			}

			if (source != lastPlaneIndex || imageIndex != lastImageIndex ||
				x != lastPlaneX || y != lastPlaneY || w != lastPlaneWidth ||
				h != lastPlaneHeight)
			{

				int strips = 1;

				// check how big the original image is; if it's larger than the
				// available memory, we will need to split it into strips

				final Runtime rt = Runtime.getRuntime();
				final long availableMemory = rt.freeMemory();
				final long planeSize = DataTools.safeMultiply64(w, h, bpp, c);

				if (availableMemory < planeSize || planeSize > Integer.MAX_VALUE) {
					strips = (int) Math.sqrt(h);
				}

				final int stripHeight = h / strips;
				final int lastStripHeight = stripHeight + (h - (stripHeight * strips));
				byte[] strip =
					strips == 1 ? plane.getBytes() : new byte[stripHeight * w * bpp];
				for (int i = 0; i < strips; i++) {
					lastPlane =
						getParent().openPlane(imageIndex, source, x, y + i * stripHeight,
							w, i == strips - 1 ? lastStripHeight : stripHeight);
					lastPlaneIndex = source;
					lastImageIndex = imageIndex;
					lastPlaneX = x;
					lastPlaneY = y + i * stripHeight;
					lastPlaneWidth = w;
					lastPlaneHeight = i == strips - 1 ? lastStripHeight : stripHeight;

					if (strips != 1 && lastStripHeight != stripHeight && i == strips - 1)
					{
						strip = new byte[lastStripHeight * w * bpp];
					}

					ImageTools.splitChannels(lastPlane.getBytes(), strip, channel, c,
						bpp, false, getMetadata().isInterleaved(imageIndex), strips == 1
							? w * h * bpp : strip.length);
					if (strips != 1) {
						System.arraycopy(strip, 0, plane.getBytes(), i * stripHeight * w *
							bpp, strip.length);
					}
				}
			}
			else {
				ImageTools.splitChannels(lastPlane.getBytes(), plane.getBytes(),
					channel, c, bpp, false, getMetadata().isInterleaved(imageIndex), w *
						h * bpp);
			}

			return plane;
		}
		return getParent().openPlane(imageIndex, planeIndex, plane, x, y, w, h);
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#openThumbPlane(int, int)
	 */
	@Override
	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		FormatTools.assertId(getCurrentFile(), true, 2);

		final int source = getOriginalIndex(imageIndex, planeIndex);
		final Plane thumb = getParent().openThumbPlane(source, planeIndex);

		ByteArrayPlane ret = null;

		if (isCompatible(thumb.getClass())) ret = (ByteArrayPlane) thumb;
		else {
			ret = new ByteArrayPlane(thumb.getContext());
			ret.populate(thumb);
		}

		// TODO maybe these imageIndices should be source as well?

		final int c =
			getMetadata().getAxisLength(imageIndex, Axes.CHANNEL) /
				getParentMeta().getEffectiveSizeC(imageIndex);
		final int channel = planeIndex % c;
		final int bpp =
			FormatTools.getBytesPerPixel(getMetadata().getPixelType(imageIndex));

		ret.setData(ImageTools.splitChannels(thumb.getBytes(), channel, c, bpp,
			false, false));
		return ret;
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#close()
	 */
	@Override
	public void close() throws IOException {
		close(false);
	}

	/*
	 * @see io.scif.filters.AbstractReaderFilter#close(boolean)
	 */
	@Override
	public void close(final boolean fileOnly) throws IOException {
		super.close(fileOnly);
		if (!fileOnly) {
			cleanUp();
		}
	}

	@Override
	public Plane createPlane(final int xOffset, final int yOffset,
		final int xLength, final int yLength)
	{
		return createPlane(getMetadata().get(0), xOffset, yOffset, xLength, yLength);
	}

	// -- Helper Methods --

	/* Resets local fields. */
	private void cleanUp() {
		lastPlane = null;
		lastPlaneIndex = -1;
		lastImageIndex = -1;
		lastPlaneX = -1;
		lastPlaneY = -1;
		lastPlaneWidth = -1;
		lastPlaneHeight = -1;
	}
}
