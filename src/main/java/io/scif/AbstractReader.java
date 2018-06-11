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

package io.scif;

import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

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
	private M metadata;

	/** Whether or not to normalize float data. */
	private boolean normalizeData;

	/** List of domains in which this format is used. */
	private String[] domains;

	private final Class<P> planeClass;

	// -- Constructors --

	/** Constructs a reader and stores a reference to its plane type */
	public AbstractReader(final Class<P> planeClass) {
		this.planeClass = planeClass;
	}

	// -- AbstractReader API Methods --

	/**
	 * Helper method to lazily create the domain array for this reader instance,
	 * to avoid constantly re-creating the array.
	 */
	protected abstract String[] createDomainArray();

	// -- Reader API Methods --

	// TODO Merge common Reader and Writer API methods

	@Override
	public P openPlane(final int imageIndex, final long planeIndex)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, new SCIFIOConfig());
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, bounds, new SCIFIOConfig());
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane));
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane),
			bounds);
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		final Interval bounds = //
			new FinalInterval(metadata.get(imageIndex).getAxesLengthsPlanar());
		return openPlane(imageIndex, planeIndex, bounds, config);
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		P plane = null;

		try {
			plane = createPlane(bounds);
		}
		catch (final IllegalArgumentException e) {
			throw new FormatException("Image plane too large. Only 2GB of data can " +
				"be extracted at one time. You can workaround the problem by opening " +
				"the plane in tiles; for further details, see: " +
				"http://www.openmicroscopy.org/site/support/faq/bio-formats/" +
				"i-see-an-outofmemory-or-negativearraysize-error-message-when-" +
				"attempting-to-open-an-svs-or-jpeg-2000-file.-what-does-this-mean", e);
		}

		return openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane),
			config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, this.<P> castToTypedPlane(plane),
			bounds, config);
	}

	@Override
	public String getCurrentFile() {
		return getStream() == null ? null : getStream().getFileName();
	}

	@Override
	public String[] getDomains() {
		if (domains == null) {
			domains = createDomainArray();
		}
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
		return metadata.get(imageIndex).getAxisLength(Axes.X);
	}

	@Override
	public long getOptimalTileHeight(final int imageIndex) {
		final int bpp = FormatTools.getBytesPerPixel(metadata.get(imageIndex)
			.getPixelType());

		final long width = metadata.get(imageIndex).getAxisLength(Axes.X);
		final long rgbcCount = metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);

		final long maxHeight = (1024 * 1024) / (width * rgbcCount * bpp);
		return Math.min(maxHeight, metadata.get(imageIndex).getAxisLength(Axes.Y));
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
		return false;
	}

	@Override
	public void setSource(final String fileName) throws IOException {
		setSource(fileName, new SCIFIOConfig());
	}

	@Override
	public void setSource(final File file) throws IOException {
		setSource(file, new SCIFIOConfig());
	}

	@Override
	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		setSource(stream, new SCIFIOConfig());
	}

	@Override
	public void setSource(final String fileName, final SCIFIOConfig config)
		throws IOException
	{

		if (getStream() != null && getStream().getFileName() != null && getStream()
			.getFileName().equals(fileName))
		{
			getStream().seek(0);
			return;
		}

		close();
		final RandomAccessInputStream stream = new RandomAccessInputStream(
			getContext(), fileName);
		try {
			setMetadata(getFormat().createParser().parse(stream, config));
		}
		catch (final FormatException e) {
			stream.close();
			throw new IOException(e);
		}
		setSource(stream);
	}

	@Override
	public void setSource(final File file, final SCIFIOConfig config)
		throws IOException
	{
		setSource(file.getName(), config);
	}

	@Override
	public void setSource(final RandomAccessInputStream stream,
		final SCIFIOConfig config) throws IOException
	{
		final String currentSource = getStream() == null ? null : getStream()
			.getFileName();
		final String newSource = stream.getFileName();
		if (metadata != null && (currentSource == null || newSource == null ||
			!getStream().getFileName().equals(stream.getFileName()))) close();

		if (metadata == null) {
			try {
				@SuppressWarnings("unchecked")
				final M meta = (M) getFormat().createParser().parse(stream, config);
				setMetadata(meta);
			}
			catch (final FormatException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final Interval bounds, final Plane plane) throws IOException
	{
		return readPlane(s, imageIndex, bounds, this.<P> castToTypedPlane(plane));
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final Interval bounds, final int scanlinePad, final Plane plane)
		throws IOException
	{
		return readPlane(s, imageIndex, bounds, scanlinePad, this
			.<P> castToTypedPlane(plane));
	}

	@Override
	public long getPlaneCount(final int imageIndex) {
		return metadata.get(imageIndex).getPlaneCount();
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
	public P openPlane(final int imageIndex, final long planeIndex, final P plane)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, new SCIFIOConfig());
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex, final P plane,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, plane.getBounds(), config);
	}

	@Override
	public P openPlane(final int imageIndex, final long planeIndex, final P plane,
		final Interval bounds) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, plane.getBounds(),
			new SCIFIOConfig());
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
		final Interval bounds, final P plane) throws IOException
	{
		return readPlane(s, imageIndex, bounds, 0, plane);
	}

	@Override
	public P readPlane(final RandomAccessInputStream s, final int imageIndex,
		final Interval bounds, final int scanlinePad, final P plane)
		throws IOException
	{
		final int bpp = FormatTools.getBytesPerPixel(metadata.get(imageIndex)
			.getPixelType());

		final byte[] bytes = plane.getBytes();
		final int xIndex = metadata.get(imageIndex).getAxisIndex(Axes.X);
		final int yIndex = metadata.get(imageIndex).getAxisIndex(Axes.Y);
		if (SCIFIOMetadataTools.wholePlane(imageIndex, metadata, bounds) &&
			scanlinePad == 0)
		{
			s.read(bytes);
		}
		else if (SCIFIOMetadataTools.wholeRow(imageIndex, metadata, bounds) &&
			scanlinePad == 0)
		{
			if (metadata.get(imageIndex).getInterleavedAxisCount() > 0) {
				int bytesToSkip = bpp;
				bytesToSkip *= bounds.max(xIndex);
				int bytesToRead = bytesToSkip;
				for (int i = 0; i < bounds.numDimensions(); i++) {
					if (i != xIndex) {
						if (i == yIndex) {
							bytesToSkip *= bounds.min(i);
						}
						else {
							bytesToSkip *= bounds.max(i);
						}
						bytesToRead *= bounds.max(i);
					}
				}
				s.skip(bytesToSkip);
				s.read(bytes, 0, bytesToRead);
			}
			else {
				final int rowLen = (int) (bpp * bounds.max(xIndex));
				final int h = (int) bounds.max(yIndex);
				final int y = (int) bounds.min(yIndex);
				long c = metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);
				if (c <= 0 || !metadata.get(imageIndex).isMultichannel()) c = 1;
				for (int channel = 0; channel < c; channel++) {

					s.skipBytes(y * rowLen);
					s.read(bytes, channel * h * rowLen, h * rowLen);
					if (channel < c - 1) {
						// no need to skip bytes after reading final channel
						s.skipBytes((int) (metadata.get(imageIndex).getAxisLength(Axes.Y) -
							y - h) * rowLen);
					}
				}
			}
		}
		else {
			final int scanlineWidth = (int) metadata.get(imageIndex).getAxisLength(
				Axes.X) + scanlinePad;
			if (metadata.get(imageIndex).getInterleavedAxisCount() > 0) {
				long planeProduct = bpp;
				for (int i = 0; i < bounds.numDimensions(); i++) {
					if (i != xIndex && i != yIndex) planeProduct *= metadata.get(
						imageIndex).getAxisLength(i);
				}
				int bytesToSkip = scanlineWidth * (int) planeProduct;
				s.skipBytes((int) bounds.min(yIndex) * bytesToSkip);

				bytesToSkip = bpp;
				int bytesToRead = bytesToSkip;
				bytesToRead *= bounds.max(xIndex);
				bytesToRead *= planeProduct;
				bytesToSkip *= bounds.min(xIndex);
				bytesToSkip *= planeProduct;

				for (int row = 0; row <= bounds.max(yIndex); row++) {
					s.skipBytes(bytesToSkip);
					s.read(bytes, row * bytesToRead, bytesToRead);
					if (row < bounds.max(yIndex)) {
						// no need to skip bytes after reading final row
						s.skipBytes((int) (planeProduct * (scanlineWidth - bounds.dimension(
							xIndex))));
					}
				}
			}
			else {
				final long c = metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);

				final int w = (int) bounds.max(xIndex);
				final int h = (int) bounds.max(yIndex);
				final int x = (int) bounds.min(xIndex);
				final int y = (int) bounds.min(yIndex);
				for (int channel = 0; channel < c; channel++) {
					s.skipBytes(y * scanlineWidth * bpp);
					for (int row = 0; row < h; row++) {
						s.skipBytes(x * bpp);
						s.read(bytes, channel * w * h * bpp + row * w * bpp, w * bpp);
						if (row < h - 1 || channel < c - 1) {
							// no need to skip bytes after reading final row of
							// final channel
							s.skipBytes(bpp * (scanlineWidth - w - x));
						}
					}
					if (channel < c - 1) {
						// no need to skip bytes after reading final channel
						s.skipBytes(scanlineWidth * bpp * (int) (metadata.get(imageIndex)
							.getAxisLength(Axes.Y) - y - h));
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
		}
	}
}
