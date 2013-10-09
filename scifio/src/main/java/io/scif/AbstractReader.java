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

package io.scif;

import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Reader} implementations.
 * 
 * @see io.scif.Reader
 * @see io.scif.HasFormat
 * @see io.scif.Metadata
 * @see io.scif.Plane
 * @author Mark Hiner
 */
public abstract class AbstractReader<M extends TypedMetadata, P extends DataPlane<?>>
	extends AbstractGroupable implements TypedReader<M, P>
{

	// -- Fields --

	/** Metadata for the current image source. */
	protected M metadata;

	/** Whether or not to normalize float data. */
	protected boolean normalizeData;

	/** List of domains in which this format is used. */
	protected String[] domains = new String[0];

	/** Name of current file. */
	protected String currentId;

	/** Whether this format supports multi-file datasets. */
	protected boolean hasCompanionFiles = false;

	private final Class<P> planeClass;

	// -- Constructors --

	/** Constructs a reader and stores a reference to its plane type */
	public AbstractReader(final Class<P> planeClass) {
		this.planeClass = planeClass;
	}

	// -- Reader API Methods --

	// TODO Merge common Reader and Writer API methods

	@Override
	public P openPlane(final int imageIndex, final int planeNumber)
		throws FormatException, IOException
	{
		final long[] planeMax = metadata.getAxesLengthsPlanar(imageIndex);
		final long[] planeMin = new long[planeMax.length];
		return openPlane(imageIndex, planeNumber, planeMin, planeMax);
	}

	  @Override
	public P openPlane(final int imageIndex, final int planeIndex,
		final long[] planeMin, final long[] planeMax) throws FormatException,
		IOException
	{
		P plane = null;

		try {
			plane = createPlane(planeMin, planeMax);
		}
		catch (final IllegalArgumentException e) {
			throw new FormatException(
				"Image plane too large. Only 2GB of data can "
					+ "be extracted at one time. You can workaround the problem by opening "
					+ "the plane in tiles; for further details, see: "
					+ "http://www.openmicroscopy.org/site/support/faq/bio-formats/"
					+ "i-see-an-outofmemory-or-negativearraysize-error-message-when-"
					+ "attempting-to-open-an-svs-or-jpeg-2000-file.-what-does-this-mean",
				e);
		}

		return openPlane(imageIndex, planeIndex, plane, planeMin, planeMax);
	}

	@Override
	public P openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane));
	}

	@Override
	public P openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final long[] planeMin, final long[] planeMax)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane),
			planeMin, planeMax);
	}

	@Override
	public String getCurrentFile() {
		return getStream() == null ? null : getStream().getFileName();
	}

	@Override
	public String[] getDomains() {
		return domains;
	}

	@Override
	public RandomAccessInputStream getStream() {
		return metadata == null ? null : metadata.getSource();
	}

	@Override
	public Reader[] getUnderlyingReaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getOptimalTileWidth(final int imageIndex) {
		return metadata.getAxisLength(imageIndex, Axes.X);
	}

	@Override
	public long getOptimalTileHeight(final int imageIndex) {
		final int bpp =
			FormatTools.getBytesPerPixel(metadata.getPixelType(imageIndex));

		final long width = metadata.getAxisLength(imageIndex, Axes.X);
		final long rgbcCount = metadata.getAxisLength(imageIndex, Axes.CHANNEL);

		final long maxHeight = (1024 * 1024) / (width * rgbcCount * bpp);
		return Math.min(maxHeight, metadata.getAxisLength(imageIndex, Axes.Y));
	}

	@Override
	public void setMetadata(final io.scif.Metadata meta) throws IOException {
		setMetadata(SCIFIOMetadataTools.<M> castMeta(meta));
	}

	@Override
	public M getMetadata() {
		return metadata;
	}

	@Override
	public void setNormalized(final boolean normalize) {
		normalizeData = normalize;
	}

	@Override
	public boolean isNormalized() {
		return normalizeData;
	}

	@Override
	public boolean hasCompanionFiles() {
		return hasCompanionFiles;
	}

	@Override
	public void setSource(final String fileName) throws IOException {

		if (getStream() != null && getStream().getFileName() != null &&
			getStream().getFileName().equals(fileName))
		{
			getStream().seek(0);
			return;
		}

		close();
		final RandomAccessInputStream stream =
			new RandomAccessInputStream(getContext(), fileName);
		try {
			setMetadata(getFormat().createParser().parse(stream));
		}
		catch (final FormatException e) {
			throw new IOException(e);
		}
		setSource(stream);
	}

	@Override
	public void setSource(final File file) throws IOException {
		setSource(file.getName());
	}

	@Override
	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		if (metadata != null && getStream() != stream) close();

		if (metadata == null) {
			currentId = stream.getFileName();

			try {
				@SuppressWarnings("unchecked")
				final M meta = (M) getFormat().createParser().parse(stream);
				setMetadata(meta);
			}
			catch (final FormatException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final Plane plane)
		throws IOException
	{
		return readPlane(s, imageIndex, planeMin, planeMax, this
			.<P> castToTypedPlane(plane));
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final int scanlinePad,
		final Plane plane) throws IOException
	{
		return readPlane(s, imageIndex, planeMin, planeMax, scanlinePad, this
			.<P> castToTypedPlane(plane));
	}

	@Override
	public int getPlaneCount(final int imageIndex) {
		return metadata.getPlaneCount(imageIndex);
	}

	@Override
	public int getImageCount() {
		return metadata.getImageCount();
	}

	@Override
	public <T extends Plane> T castToTypedPlane(final Plane plane) {
		if (!planeClass.isAssignableFrom(plane.getClass())) {
			throw new IllegalArgumentException("Incompatible plane types. " +
				"Attempted to cast: " + plane.getClass() + " to: " + planeClass);
		}

		@SuppressWarnings("unchecked")
		final T p = (T) plane;
		return p;
	}

	// -- TypedReader API --

	@Override
	public P openPlane(final int imageIndex, final int planeIndex, final P plane)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, plane.getOffsets(), plane.getLengths());
	}

	@Override
	public void setMetadata(final M meta) throws IOException {
		if (metadata != null && metadata != meta) {
			close();
		}

		if (metadata == null) metadata = meta;
	}

	@Override
	public P readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final P plane)
		throws IOException
	{
		return readPlane(s, imageIndex, planeMin, planeMax, 0, plane);
	}

	@Override
	public P readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final int scanlinePad,
		final P plane) throws IOException
	{
		final int bpp =
			FormatTools.getBytesPerPixel(metadata.getPixelType(imageIndex));

		final byte[] bytes = plane.getBytes();
		final int xIndex = metadata.getAxisIndex(imageIndex, Axes.X);
		final int yIndex = metadata.getAxisIndex(imageIndex, Axes.Y);
		if (SCIFIOMetadataTools
			.wholePlane(imageIndex, metadata, planeMin, planeMax) &&
			scanlinePad == 0)
		{
			s.read(bytes);
		}
		else if (SCIFIOMetadataTools.wholeRow(imageIndex, metadata, planeMin,
			planeMax) &&
			scanlinePad == 0)
		{
			if (metadata.isInterleaved(imageIndex)) {
				int bytesToSkip = bpp;
				bytesToSkip *= planeMax[xIndex];
				int bytesToRead = bytesToSkip;
				for (int i = 0; i < planeMin.length; i++) {
					if (i != xIndex) {
						if (i == yIndex) {
							bytesToSkip *= planeMin[i];
						}
						else {
							bytesToSkip *= planeMax[i];
						}
						bytesToRead *= planeMax[i];
					}
				}
				s.skip(bytesToSkip);
				s.read(bytes, 0, bytesToRead);
			}
			else {
				final int rowLen = (int) (bpp * planeMax[xIndex]);
				final int h = (int) planeMax[yIndex];
				final int y = (int) planeMin[yIndex];
				long c = metadata.getAxisLength(imageIndex, Axes.CHANNEL);
				if (c <= 0) c = 1;
				for (int channel = 0; channel < c; channel++) {

					s.skipBytes(y * rowLen);
					s.read(bytes, channel * h * rowLen, h * rowLen);
					if (channel < c - 1) {
						// no need to skip bytes after reading final channel
						s.skipBytes((int)(metadata.getAxisLength(imageIndex, Axes.Y) - y - h) *
							rowLen);
					}
				}
			}
		}
		else {
			final int scanlineWidth =
				(int) metadata.getAxisLength(imageIndex, Axes.X) + scanlinePad;
			if (metadata.isInterleaved(imageIndex)) {
				long planeProduct = bpp;
				for (int i = 0; i < planeMin.length; i++) {
					if (i != xIndex && i != yIndex) planeProduct *=
						metadata.getAxisLength(imageIndex, i);
				}
				int bytesToSkip = scanlineWidth * (int) planeProduct;
				s.skipBytes((int) planeMin[yIndex] * bytesToSkip);

				bytesToSkip = bpp;
				int bytesToRead = bytesToSkip;
				bytesToRead *= planeMax[xIndex];
				bytesToRead *= planeProduct;
				bytesToSkip *= planeMin[xIndex];
				bytesToSkip *= planeProduct;

				for (int row = 0; row < planeMax[yIndex]; row++) {
					s.skipBytes(bytesToSkip);
					s.read(bytes, row * bytesToRead, bytesToRead);
					if (row < planeMax[yIndex] - 1) {
						// no need to skip bytes after reading final row
						s.skipBytes((int) (planeProduct * (scanlineWidth - planeMax[xIndex] - planeMin[xIndex])));
					}
				}
			}
			else {
				final long c = metadata.getAxisLength(imageIndex, Axes.CHANNEL);

				final int w = (int) planeMax[xIndex];
				final int h = (int) planeMax[yIndex];
				final int x = (int) planeMin[xIndex];
				final int y = (int) planeMin[yIndex];
				for (int channel = 0; channel < c; channel++) {
					s.skipBytes(y * scanlineWidth * bpp);
					for (int row = 0; row < h; row++) {
						s.skipBytes(x * bpp);
						s.read(bytes, channel * w * h * bpp + row * w * bpp, w * bpp);
						if (row < h - 1 || channel < c - 1) {
							// no need to skip bytes after reading final row of final channel
							s.skipBytes(bpp * (scanlineWidth - w - x));
						}
					}
					if (channel < c - 1) {
						// no need to skip bytes after reading final channel
						s.skipBytes(scanlineWidth * bpp *
							(int)(metadata.getAxisLength(imageIndex, Axes.Y) - y - h));
					}
				}
			}
		}
		return plane;
	}

	@Override
	public Class<P> getPlaneClass() {
		return planeClass;
	}

	// -- HasSource Format API --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		if (metadata != null) metadata.close(fileOnly);

		if (!fileOnly) {
			metadata = null;
			currentId = null;
		}
	}
}
