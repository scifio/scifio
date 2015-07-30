/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2015 Board of Regents of the University of
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

import java.io.File;
import java.io.IOException;

import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Reader} implementations.
 *
 * @see io.scif.Reader
 * @see io.scif.HasFormat
 * @see io.scif.Metadata
 * @see io.scif.Block
 * @author Mark Hiner
 */
public abstract class AbstractReader<M extends TypedMetadata, T extends NativeType<T>, B extends TypedBlock<T, ?>>
	extends AbstractGroupable implements TypedReader<M, T, B>
{

	// -- Fields --

	/** Metadata for the current image source. */
	private M metadata;

	/** Whether or not to normalize float data. */
	private boolean normalizeData;

	/** List of domains in which this format is used. */
	private String[] domains;

	private final Class<B> blockClass;

	// -- Constructors --

	/** Constructs a reader and stores a reference to its block type */
	public AbstractReader(final Class<B> blockClass) {
		this.blockClass = blockClass;
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
	public B openBlock(final int imageIndex, final long blockIndex)
		throws FormatException, IOException
	{
		return openBlock(imageIndex, blockIndex, new SCIFIOConfig());
	}

	@Override
	public B openBlock(final int imageIndex, final long blockIndex,
		final Block block) throws FormatException, IOException
	{
		return openBlock(imageIndex, blockIndex, this.<B> castToTypedBlock(block));
	}

	@Override
	public B openBlock(final int imageIndex, final long blockIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		//FIXME how should we get Intervals from the Metadata?
		final long[] blockMax = metadata.get(imageIndex).getAxesLengthsPlanar();
		final long[] blockMin = new long[blockMax.length];
		return openBlock(imageIndex, blockIndex, blockMin, blockMax, config);
	}

	@Override
	public B openBlock(final int imageIndex, final long blockIndex,
		final Block block, final SCIFIOConfig config) throws FormatException,
			IOException
	{
		return openBlock(imageIndex, blockIndex, this.<B> castToTypedBlock(block),
			config);
	}

	@Override
	public B openRegion(final int imageIndex, Interval range)
		throws FormatException, IOException
	{
		return openRegion(imageIndex, range, new SCIFIOConfig());
	}

	@Override
	public B openRegion(final int imageIndex,
		final Interval range, final Block block) throws FormatException,
			IOException
	{
		return openRegion(imageIndex, range, this.<B> castToTypedBlock(block));
	}

	@Override
	public B openRegion(final int imageIndex,
		final Interval range, final SCIFIOConfig config) throws FormatException,
			IOException
	{
		B block = null;

		try {
			block = createBlock(range);
		}
		catch (final IllegalArgumentException e) {
			throw new FormatException("Image block too large. Only 2GB of data can " +
				"be extracted at one time. You can workaround the problem by opening " +
				"the block in tiles; for further details, see: " +
				"http://www.openmicroscopy.org/site/support/faq/bio-formats/" +
				"i-see-an-outofmemory-or-negativearraysize-error-message-when-" +
				"attempting-to-open-an-svs-or-jpeg-2000-file.-what-does-this-mean", e);
		}

		return openRegion(imageIndex, range, block, config);
	}

	@Override
	public B openRegion(final int imageIndex,
		final Interval range, final Block block, final SCIFIOConfig config)
			throws FormatException, IOException
	{
		return openRegion(imageIndex, range, this.<B> castToTypedBlock(block),
			config);
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
	public Interval getOptimalBlockSize(final int imageIndex) {
		// actually this maybe shouldn't return an interval, because it's a block SIZE
		// which doesn't specify minimums...
		
		// x optimal
		return metadata.get(imageIndex).getAxisLength(Axes.X);
		// y optimal
		final int bpp = FormatTools.getBytesPerPixel(metadata.get(imageIndex)
			.getPixelType());

		final long width = metadata.get(imageIndex).getAxisLength(Axes.X);
		final long rgbcCount = metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);

		final long maxHeight = (1024 * 1024) / (width * rgbcCount * bpp);
		return Math.min(maxHeight, metadata.get(imageIndex).getAxisLength(Axes.Y));
		//TODO put these with padded 1's for the rest of the dims..?
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
		final String currentSource = getStream().getFileName();
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
	public Block readBlock(final RandomAccessInputStream s, final int imageIndex,
		final Interval range, final Block block)
			throws IOException
	{
		return readBlock(s, imageIndex, range, this
			.<B> castToTypedBlock(block));
	}

	@Override
	public Block readBlock(final RandomAccessInputStream s, final int imageIndex,
		final Interval range, final int scanlinePad,
		final Block block) throws IOException
	{
		return readBlock(s, imageIndex, range, scanlinePad, this
			.<B> castToTypedBlock(block));
	}

	@Override
	public int getImageCount() {
		return metadata.getImageCount();
	}

	@Override
	public <T extends Block> T castToTypedBlock(final Block block) {
		if (!blockClass.isAssignableFrom(block.getClass())) {
			throw new IllegalArgumentException("Incompatible block types. " +
				"Attempted to cast: " + block.getClass() + " to: " + blockClass);
		}

		@SuppressWarnings("unchecked")
		final T b = (T) block;
		return b;
	}

	// -- TypedReader API --

	@Override
	public B openBlock(final int imageIndex, final long blockIndex, final B block)
		throws FormatException, IOException
	{
		return openBlock(imageIndex, blockIndex, block, new SCIFIOConfig());
	}

	@Override
	public B openRegion(final int imageIndex, final Interval range, final B block) throws FormatException, IOException
	{
		return openRegion(imageIndex, range, block, new SCIFIOConfig());
	}

	@Override
	public void setMetadata(final M meta) throws IOException {
		if (metadata != null && metadata != meta) {
			close();
		}

		if (metadata == null) metadata = meta;
	}

	@Override
	public B readBlock(final RandomAccessInputStream s, final int imageIndex,
		final Interval range, final B block)
			throws IOException
	{
		return readBlock(s, imageIndex, range, 0, block);
	}

	@Override
	public B readBlock(final RandomAccessInputStream s, final int imageIndex,
		final Interval range, final int scanlinePad,
		final B block) throws IOException
	{
		final int bpp = FormatTools.getBytesPerPixel(metadata.get(imageIndex)
			.getPixelType());

		final byte[] bytes = block.getBytes();
		final int xIndex = metadata.get(imageIndex).getAxisIndex(Axes.X);
		final int yIndex = metadata.get(imageIndex).getAxisIndex(Axes.Y);
		if (SCIFIOMetadataTools.wholeBlock(imageIndex, metadata,
			range) && scanlinePad == 0)
		{
			s.read(bytes);
		}
		else if (SCIFIOMetadataTools.wholeRow(imageIndex, metadata,
			range) && scanlinePad == 0)
		{
			if (SCIFIOMetadataTools.countInterleavedAxes(metadata, imageIndex) > 0) {
				int bytesToSkip = bpp;
				bytesToSkip *= range.min(xIndex);
				int bytesToRead = bytesToSkip;
				for (int i = 0; i < range.numDimensions(); i++) {
					if (i != xIndex) {
						if (i == yIndex) {
							bytesToSkip *= range.min(i);
						}
						else {
							bytesToSkip *= range.max(i);
						}
						bytesToRead *= range.max(i);
					}
				}
				s.skip(bytesToSkip);
				s.read(bytes, 0, bytesToRead);
			}
			else {
				final int rowLen = (int) (bpp * range.max(xIndex));
				final int h = (int) range.max(yIndex);
				final int y = (int) range.min(yIndex);
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
			if (SCIFIOMetadataTools.countInterleavedAxes(metadata, imageIndex) > 0) {
				long blockProduct = bpp;
				for (int i = 0; i < range.numDimensions(); i++) {
					if (i != xIndex && i != yIndex) blockProduct *= metadata.get(
						imageIndex).getAxisLength(i);
				}
				int bytesToSkip = scanlineWidth * (int) blockProduct;
				s.skipBytes((int) range.min(yIndex) * bytesToSkip);

				bytesToSkip = bpp;
				int bytesToRead = bytesToSkip;
				bytesToRead *= range.max(xIndex);
				bytesToRead *= blockProduct;
				bytesToSkip *= range.min(xIndex);
				bytesToSkip *= blockProduct;

				for (int row = 0; row < range.max(yIndex); row++) {
					s.skipBytes(bytesToSkip);
					s.read(bytes, row * bytesToRead, bytesToRead);
					if (row < range.max(yIndex) - 1) {
						// no need to skip bytes after reading final row
						s.skipBytes((int) (blockProduct * (scanlineWidth -
							range.max(xIndex) - range.min(xIndex))));
					}
				}
			}
			else {
				final long c = metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);

				final int w = (int) range.max(xIndex);
				final int h = (int) range.max(yIndex);
				final int x = (int) range.min(xIndex);
				final int y = (int) range.min(yIndex);
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
		return block;
	}

	@Override
	public Class<B> getBlockClass() {
		return blockClass;
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
